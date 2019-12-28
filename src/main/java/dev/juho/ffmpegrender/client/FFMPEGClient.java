package dev.juho.ffmpegrender.client;

import dev.juho.ffmpegrender.events.Event;
import dev.juho.ffmpegrender.events.EventBus;
import dev.juho.ffmpegrender.events.EventType;
import dev.juho.ffmpegrender.events.Listener;
import dev.juho.ffmpegrender.events.events.ClientDisconnectEvent;
import dev.juho.ffmpegrender.events.events.CrashEvent;
import dev.juho.ffmpegrender.events.events.RenderProgressEvent;
import dev.juho.ffmpegrender.server.client.Client;
import dev.juho.ffmpegrender.server.client.Writer;
import dev.juho.ffmpegrender.server.client.reader.Reader;
import dev.juho.ffmpegrender.server.message.Message;
import dev.juho.ffmpegrender.server.message.MessageType;
import dev.juho.ffmpegrender.utils.ArgsParser;
import dev.juho.ffmpegrender.utils.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class FFMPEGClient implements Client, Listener {

	private String host;
	private int port;
	private boolean running;
	private UUID uuid;
	private long startDate;

	private Socket socket;

	private Reader reader;
	private Writer writer;

	private FFMPEG ffmpeg;

	private boolean shutdownAfterRender;

	public FFMPEGClient(String host, int port) {
		this.host = host;
		this.port = port;
		this.ffmpeg = new FFMPEG();
		this.shutdownAfterRender = false;
	}

	@Override
	public boolean isAlive() {
		return running;
	}

	@Override
	public Writer getWriter() {
		return writer;
	}

	@Override
	public Socket getSocket() {
		return socket;
	}

	@Override
	public UUID getUuid() {
		return uuid;
	}

	@Override
	public long getUptime() {
		return new Date().getTime() - startDate;
	}

	@Override
	public void listen() throws IOException {
		this.socket = new Socket(host, port);
		this.running = true;
		this.startDate = new Date().getTime();
		this.writer = new Writer(socket.getOutputStream());

		this.reader = new Reader(this, socket.getInputStream());
		this.reader.start();
	}

	@Override
	public void ping() {
		try {
			writer.sendPing();
		} catch (IOException e) {
			if (ArgsParser.getInstance().has(ArgsParser.Argument.DEBUG)) {
				e.printStackTrace();
			}

			EventBus.getInstance().publish(new CrashEvent(this));
		}
	}

	@Override
	public void kill() throws IOException {
		Logger.getInstance().log(Logger.WARNING, "Closing FFMPEGClient");
		Logger.getInstance().log(Logger.DEBUG, "Trying to close all client connections");
		EventBus.getInstance().publish(new ClientDisconnectEvent(this));
		reader.close();
		writer.close();
		socket.close();
		Logger.getInstance().log(Logger.DEBUG, "All client connections closed");
	}

	@Override
	public void handle(Event<EventType, ?> e) {
		switch (e.getType()) {
			case MESSAGE:
				Message message = (Message) e.getData();
				Logger.getInstance().log(Logger.DEBUG, "Received message: " + message.getData().toString());
				readMessage(message);
				break;

			case RENDER_PROGRESS:
				RenderProgressEvent renderProgress = (RenderProgressEvent) e;
				writer.write(Message.build(MessageType.UPDATE_RENDER_PROGRESS, uuid, new JSONObject().put("progress", renderProgress.getData())));
				e.cancel();
				break;
		}
	}

	@Override
	public List<EventType> supports() {
		return Arrays.asList(EventType.MESSAGE, EventType.RENDER_PROGRESS);
	}

	public void shutdownAfterRender() {
		writer.write(Message.build(MessageType.BLOCK_NEW_VIDEOS, uuid, new JSONObject()));
		shutdownAfterRender = true;
	}

	public FFMPEG getFfmpeg() {
		return ffmpeg;
	}

	private void readMessage(Message message) {
		switch (message.getType()) {
			case SET_UUID:
				this.uuid = UUID.fromString(message.getData().getString("uuid"));
				if (ArgsParser.getInstance().has(ArgsParser.Argument.LOCAL)) {
					writer.write(Message.build(MessageType.SET_LOCAL_CLIENT, uuid, new JSONObject()));
				}
				break;

			case FILE_INFO:
				concatVideos(message.getData());
				break;

			case SET_RENDER_OPTIONS:
				ffmpeg.setRenderOptions(message.getData().getString("RENDERING_OPTIONS"));
				break;
		}
	}

	private void concatVideos(JSONObject filesObject) {
		File[] files = findFiles(filesObject.getJSONArray("files"));

		for (File f : files) {
			ffmpeg.addVideo(f);
		}

		String videoName = "";

		if (ffmpeg.getVideoCount() > 1) {
			try {
				videoName = ffmpeg.concat();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			videoName = files[0].getName();
		}

		String finalVideo = "";

		try {
			finalVideo = ffmpeg.render(videoName);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (finalVideo != null) {
			if (finalVideo.startsWith("\"")) finalVideo = finalVideo.substring(1);
			if (finalVideo.endsWith("\"")) finalVideo = finalVideo.substring(0, finalVideo.length() - 1);
			File finalVideoFile = new File(finalVideo);
			writer.write(finalVideoFile);

			JSONObject obj = new JSONObject();
			obj.put("files", filesObject.getJSONArray("files"));
			obj.put("final_video", finalVideoFile.getName());

			ffmpeg.deleteCurrentVideos(videoName, finalVideoFile.getName());
			writer.write(Message.build(MessageType.VIDEO_RENDERED, uuid, obj));

			if (shutdownAfterRender) {
				writer.write(Message.build(MessageType.GOODBYE, uuid, new JSONObject()));

				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				System.exit(0);
			}
		}
	}

	private File[] findFiles(JSONArray fileArray) {
		File[] files = new File[fileArray.length()];
		boolean localClient = ArgsParser.getInstance().has(ArgsParser.Argument.LOCAL);

		for (int i = 0; i < fileArray.length(); i++) {
			if (localClient) {
				File file = new File(fileArray.getString(i));
				if (!file.exists()) {
					Logger.getInstance().log(Logger.ERROR, "Couldn't find file " + file.getAbsolutePath());
					continue;
				}

				Logger.getInstance().log(Logger.DEBUG, "Found the file! " + file.getAbsolutePath());
				files[i] = file;
			} else {
				String saveFolder = "files";
				if (ArgsParser.getInstance().has(ArgsParser.Argument.SAVE_FOLDER))
					saveFolder = ArgsParser.getInstance().getString(ArgsParser.Argument.SAVE_FOLDER);

				File file = new File(saveFolder + "/" + fileArray.getString(i));
				if (!file.exists()) {
					Logger.getInstance().log(Logger.ERROR, "Couldn't find file " + file.getAbsolutePath());
					continue;
				}

				Logger.getInstance().log(Logger.DEBUG, "Found the file! " + file.getAbsolutePath());
				files[i] = file;
			}
		}

		return files;
	}

}
