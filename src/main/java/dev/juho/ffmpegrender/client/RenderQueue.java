package dev.juho.ffmpegrender.client;

import dev.juho.ffmpegrender.events.Event;
import dev.juho.ffmpegrender.events.EventType;
import dev.juho.ffmpegrender.events.Listener;
import dev.juho.ffmpegrender.events.events.ConnectEvent;
import dev.juho.ffmpegrender.server.ClientPool;
import dev.juho.ffmpegrender.server.Server;
import dev.juho.ffmpegrender.server.client.Client;
import dev.juho.ffmpegrender.server.message.Message;
import dev.juho.ffmpegrender.server.message.MessageType;
import dev.juho.ffmpegrender.utils.ArgsParser;
import dev.juho.ffmpegrender.utils.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
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

	public RenderQueue(Files files, ClientPool clientPool) {
		this.renderQueue = new LinkedList<>();
		this.filesToIgnore = new ArrayList<>();
		this.folderPaths = new ArrayList<>();
		this.files = files;
		this.renderOptions = "-f mp4 -vcodec libx264 -profile:v main -r 60 -s 1920x1080";
		this.queueBuilt = false;

		this.clientPool = clientPool;
		this.videosToConcatToOne = ArgsParser.getInstance().has("-videos_in_one") ? ArgsParser.getInstance().getInt("-videos_in_one") : 4;

		this.newVideosBlocked = new ArrayList<>();

		readFoldersFromArgs();
	}

	public void updateQueue(Client client) {
		if (!queueBuilt) {
			build();
			queueBuilt = true;
		}

		if (renderQueue.isEmpty()) {
			Logger.getInstance().log(Logger.INFO, "RENDER QUEUE IS EMPTY");
			return;
		}

		JSONArray fileArray = new JSONArray();
		client.getWriter().write(Message.build(MessageType.SET_RENDER_OPTIONS, Server.serverUUID, new JSONObject().put("RENDERING_OPTIONS", renderOptions)));

		while (fileArray.length() < videosToConcatToOne && !renderQueue.isEmpty()) {
			File file = renderQueue.poll();
			fileArray.put(file.getName());
			client.getWriter().write(file);
		}

		Logger.getInstance().log(Logger.DEBUG, "sending file info");
		client.getWriter().write(Message.build(MessageType.FILE_INFO, Server.serverUUID, new JSONObject().put("files", fileArray)));
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
				}
				break;

			case CONNECTION:
				ConnectEvent connectEvent = (ConnectEvent) e;
				updateClient(connectEvent.getData());
				break;
		}
	}

	@Override
	public List<EventType> supports() {
		return Arrays.asList(EventType.MESSAGE, EventType.CONNECTION);
	}

	private void build() {
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

			for (File file : f.listFiles()) {
				if (!filesToIgnore.contains(file.getAbsolutePath()) && file.getName().toLowerCase().endsWith(".mp4")) {
					renderQueue.add(file);
				}
			}
		}
	}

	private void updateClient(Client client) {
		if (!newVideosBlocked.contains(client.getUuid())) {
			if (renderQueue.size() != 0) {
				updateQueue(client);
			}
		}
	}

	private void readFoldersFromArgs() {
		if (!ArgsParser.getInstance().has("-folder")) {
			Logger.getInstance().log(Logger.WARNING, "Tried reading folders from args but 0 folders were found!");
			return;
		}

		List<String> foldersToRender = ArgsParser.getInstance().getList("-folder");

		for (String folder : foldersToRender) {
			addFolder(folder);
		}
	}

	private void addFolder(String path) {
		Logger.getInstance().log(Logger.DEBUG, path + " added to render queue");
		folderPaths.add(path);
	}

}
