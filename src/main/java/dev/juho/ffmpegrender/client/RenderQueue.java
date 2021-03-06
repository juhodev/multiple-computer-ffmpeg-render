package dev.juho.ffmpegrender.client;

import dev.juho.ffmpegrender.events.Event;
import dev.juho.ffmpegrender.events.EventType;
import dev.juho.ffmpegrender.events.Listener;
import dev.juho.ffmpegrender.events.events.ConnectEvent;
import dev.juho.ffmpegrender.server.ClientPool;
import dev.juho.ffmpegrender.server.Server;
import dev.juho.ffmpegrender.server.client.Client;
import dev.juho.ffmpegrender.server.client.ServerClient;
import dev.juho.ffmpegrender.server.client.ServerClientType;
import dev.juho.ffmpegrender.server.message.Message;
import dev.juho.ffmpegrender.server.message.MessageType;
import dev.juho.ffmpegrender.server.stats.RenderHistory;
import dev.juho.ffmpegrender.utils.ArgsParser;
import dev.juho.ffmpegrender.utils.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

public class RenderQueue implements Listener {

	private Queue<File> renderQueue;
	private List<String> filesToIgnore;
	private List<String> folderPaths;
	private Files files;
	private String renderOptions;

	private boolean queueBuilt;

	private ClientPool clientPool;
	private int videosToConcatToOne;
	private List<UUID> newVideosBlocked;

	private Queue<Client> clientQueue;
	private Client currentClient;
	//	The local client doesn't need to be a list because there really shouldn't be multiple clients rendering on the same computer
	private UUID localClient;

	private boolean started;

	private static final int VIDEOS_TO_CONCAT_TO_ONE_BY_DEFAULT = 1;

	public RenderQueue(Files files, ClientPool clientPool) {
		this.renderQueue = new LinkedList<>();
		this.filesToIgnore = new ArrayList<>();
		this.folderPaths = new ArrayList<>();
		this.files = files;
		this.renderOptions = "-f mp4 -vcodec libx264 -profile:v main -r 60 -s 1920x1080";
		this.queueBuilt = false;

		this.clientPool = clientPool;
		this.videosToConcatToOne = ArgsParser.getInstance().has(ArgsParser.Argument.VIDEOS_IN_ONE) ? ArgsParser.getInstance().getInt(ArgsParser.Argument.VIDEOS_IN_ONE) : VIDEOS_TO_CONCAT_TO_ONE_BY_DEFAULT;
		this.newVideosBlocked = new ArrayList<>();

		this.clientQueue = new LinkedList<>();
		this.currentClient = null;
		this.localClient = UUID.randomUUID();

		this.started = false;

		readFoldersFromArgs();
		updateRenderOptions();

		if (ArgsParser.getInstance().has(ArgsParser.Argument.AUTO_BUILD)) {
			build();
		}
	}

	public void start() {
		started = true;
		Timer timer = new Timer();

		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (!clientQueue.isEmpty() && currentClient == null) {
					currentClient = clientQueue.poll();
					updateQueue();
				}
			}
		}, 1000, 1000);
	}

	public Queue<File> getRenderQueue() {
		return renderQueue;
	}

	@Override
	public void handle(Event<EventType, ?> e) {
		switch (e.getType()) {
			case MESSAGE:
				Message msg = (Message) e.getData();

				switch (msg.getType()) {
					case VIDEO_RENDERED:
						files.saveRendered(msg.getData());
						updateClient(clientPool.get(msg.getSender()));
						e.cancel();
						break;

					case BLOCK_NEW_VIDEOS:
						Logger.getInstance().log(Logger.DEBUG, "Client " + msg.getSender() + " will not be sent more videos!");
						newVideosBlocked.add(msg.getSender());
						e.cancel();
						break;

					case SET_LOCAL_CLIENT:
						Logger.getInstance().log(Logger.DEBUG, "Setting the local client UUID to " + msg.getSender());
						localClient = msg.getSender();
						break;

					case UPDATE_RENDER_PROGRESS:
						RenderHistory.getInstance().updateProgress(msg.getSender(), msg.getData().getDouble("progress"));
						break;

					case SET_CLIENT_TYPE:
						setClientType(msg.getSender(), ServerClientType.valueOf(msg.getData().getString("client_type")));
						break;

					case GET_OBSERVER_DATA:
						Logger.getInstance().log(Logger.DEBUG, "get obserer data");
						break;
				}
				break;

			case CONNECTION:
				if (started) {
					ConnectEvent connectEvent = (ConnectEvent) e;
					updateClient(connectEvent.getData());
				}
				break;
		}
	}

	@Override
	public List<EventType> supports() {
		return Arrays.asList(EventType.MESSAGE, EventType.CONNECTION);
	}

	public void addFolderToQueue(String path) {
		File f = new File(path);
		Logger.getInstance().log(Logger.INFO, "Adding " + path + " to render queue");

		if (!f.exists()) {
			Logger.getInstance().log(Logger.ERROR, "Couldn't find folder " + f.getAbsolutePath());
			return;
		}

		if (!f.isDirectory()) {
			Logger.getInstance().log(Logger.ERROR, path + " isn't a directory!");
			return;
		}

		addVideoFilesToQueue(f, ArgsParser.getInstance().has(ArgsParser.Argument.RECURSIVE));
	}

	public void updateClient(Client client) {
		if (!newVideosBlocked.contains(client.getUuid())) {
			clientQueue.add(client);
		}
	}

	private void updateQueue() {
		if (!queueBuilt) {
			build();
		}

		if (renderQueue.isEmpty()) {
			Logger.getInstance().log(Logger.INFO, "RENDER QUEUE IS EMPTY");
			return;
		}

		JSONArray fileArray = new JSONArray();
		currentClient.getWriter().write(Message.build(MessageType.SET_RENDER_OPTIONS, Server.serverUUID, new JSONObject().put("RENDERING_OPTIONS", renderOptions)));

		while (fileArray.length() < videosToConcatToOne && !renderQueue.isEmpty()) {
			File file = renderQueue.poll();

//			If the current client is a local client just send the full path and not the file
			if (!localClient.equals(currentClient.getUuid())) {
				fileArray.put(file.getName());
				currentClient.getWriter().write(file);
			} else {
				fileArray.put(file.getAbsolutePath());
			}
		}

		Logger.getInstance().log(Logger.DEBUG, "sending file info");
		currentClient.getWriter().write(Message.build(MessageType.FILE_INFO, Server.serverUUID, new JSONObject().put("files", fileArray)));
		currentClient = null;
	}

	private void build() {
		try {
			readVideoInfo();
		} catch (IOException e) {
			e.printStackTrace();
		}

		ignoreFolders();

		for (String path : folderPaths) {
			File f = new File(path);

			if (!f.exists()) {
				Logger.getInstance().log(Logger.ERROR, "Couldn't find folder " + path);
				continue;
			}

			if (!f.isDirectory()) {
				Logger.getInstance().log(Logger.ERROR, path + " isn't a directory!");
				continue;
			}

			addVideoFilesToQueue(f, ArgsParser.getInstance().has(ArgsParser.Argument.RECURSIVE));
		}

		queueBuilt = true;
	}

	private void addVideoFilesToQueue(File folder, boolean recursive) {
		for (File f : folder.listFiles()) {
			if (f.isDirectory()) {
				if (!filesToIgnore.contains(f.getName()) && recursive) {
					addVideoFilesToQueue(f, true);
				}
			}

			if (!filesToIgnore.contains(f.getName()) && f.getName().toLowerCase().endsWith(".mp4")) {
				renderQueue.add(f);
			}
		}
	}

	private void readVideoInfo() throws IOException {
		String saveFolder = "files";
		if (ArgsParser.getInstance().has(ArgsParser.Argument.SAVE_FOLDER))
			saveFolder = ArgsParser.getInstance().getString(ArgsParser.Argument.SAVE_FOLDER);

		File infoFile = new File(saveFolder + "/videoInfo.json");

		if (!infoFile.exists()) {
			Logger.getInstance().log(Logger.DEBUG, "Couldn't find videoInfo.json");
			return;
		}

		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(infoFile)));

		String line;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}

		JSONArray array = new JSONArray(builder.toString());

		for (int i = 0; i < array.length(); i++) {
			JSONObject video = array.getJSONObject(i);
			JSONArray filesArray = video.getJSONArray("files");

			for (int j = 0; j < filesArray.length(); j++) {
				String videoName = filesArray.getString(j);

				// HACKHACKHACK my videoInfo.json contains BOTH full paths and file names and when creating a queue
				// files get ignored with their name and not the full path to file :)
				File f = new File(videoName);
				if (f.exists()) {
					filesToIgnore.add(f.getName());
				} else {
					filesToIgnore.add(videoName);
				}
			}
		}

		files.setFileArray(array);
	}

	private void ignoreFolders() {
		if (ArgsParser.getInstance().has(ArgsParser.Argument.IGNORE)) {
			List<String> foldersToIgnore = ArgsParser.getInstance().getList(ArgsParser.Argument.IGNORE);

			for (String folder : foldersToIgnore) {
				filesToIgnore.add(folder);
				Logger.getInstance().log(Logger.DEBUG, "Ignoring files under " + folder);
			}
		}
	}

	private void readFoldersFromArgs() {
		if (!ArgsParser.getInstance().has(ArgsParser.Argument.RENDER_FOLDER)) {
			Logger.getInstance().log(Logger.WARNING, "Tried reading folders from args but 0 folders were found!");
			return;
		}

		List<String> foldersToRender = ArgsParser.getInstance().getList(ArgsParser.Argument.RENDER_FOLDER);

		for (String folder : foldersToRender) {
			addFolder(folder);
		}
	}

	private void addFolder(String path) {
		Logger.getInstance().log(Logger.DEBUG, path + " added to render queue");
		folderPaths.add(path);
	}

	private void updateRenderOptions() {
		boolean fileCreated = false;
		try {
			fileCreated = writeDefaultRenderOptions();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (!fileCreated) {
			try {
				readFFMPEGRenderOptions();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Write default render options to a file if it doesn't exists
	 *
	 * @return If the default file was created
	 * @throws IOException
	 */
	private boolean writeDefaultRenderOptions() throws IOException {
		String saveFolder = "files";
		if (ArgsParser.getInstance().has(ArgsParser.Argument.SAVE_FOLDER))
			saveFolder = ArgsParser.getInstance().getString(ArgsParser.Argument.SAVE_FOLDER);

		File saveFile = new File(saveFolder + "/render-options.txt");

		if (!saveFile.exists()) {
			Logger.getInstance().log(Logger.DEBUG, "render-options.txt doesn't exist! Creating it with default render options");
			FileWriter writer = new FileWriter(saveFile);
			writer.write("# Default render options (the full command will be ffmpeg -i [file] [render options] [out_file]\n" + renderOptions + "\n");
			writer.flush();
			writer.close();
			return true;
		}

		return false;
	}

	private void readFFMPEGRenderOptions() throws IOException {
		String saveFolder = "files";
		if (ArgsParser.getInstance().has(ArgsParser.Argument.SAVE_FOLDER))
			saveFolder = ArgsParser.getInstance().getString(ArgsParser.Argument.SAVE_FOLDER);

		File saveFile = new File(saveFolder + "/render-options.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(saveFile)));

		String line;
		while ((line = reader.readLine()) != null) {
			// Ignore empty lines and comments
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}

			Logger.getInstance().log(Logger.DEBUG, "Setting render options to " + line);
			renderOptions = line;
			break;
		}
	}

	private void setClientType(UUID sender, ServerClientType type) {
		ServerClient serverClient = (ServerClient) clientPool.get(sender);
		serverClient.setClientType(type);
	}

}
