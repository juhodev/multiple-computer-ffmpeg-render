package dev.juho.ffmpegrender.server.client;

import dev.juho.ffmpegrender.events.EventBus;
import dev.juho.ffmpegrender.events.events.ClientDisconnectEvent;
import dev.juho.ffmpegrender.events.events.ConnectEvent;
import dev.juho.ffmpegrender.events.events.CrashEvent;
import dev.juho.ffmpegrender.server.Server;
import dev.juho.ffmpegrender.server.client.reader.Reader;
import dev.juho.ffmpegrender.server.message.Message;
import dev.juho.ffmpegrender.server.message.MessageType;
import dev.juho.ffmpegrender.utils.ArgsParser;
import dev.juho.ffmpegrender.utils.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class ServerClient implements Client {

	private Socket socket;
	private UUID uuid;
	private boolean alive;
	private long startTime;

	private Writer writer;
	private Reader reader;

	private ServerClientType clientType;

	public ServerClient(Socket socket) {
		this.socket = socket;
		this.uuid = UUID.randomUUID();
	}

	public void setClientType(ServerClientType clientType) {
		this.clientType = clientType;
	}

	@Override
	public void listen() throws IOException {
		Logger.getInstance().log(Logger.INFO, "Listening to new client!");
		this.startTime = new Date().getTime();
		this.alive = true;
		this.writer = new Writer(socket.getOutputStream());

		this.reader = new Reader(this, socket.getInputStream());
		this.reader.start();

		EventBus.getInstance().publish(new ConnectEvent(this));
		writer.write(Message.build(MessageType.SET_UUID, Server.serverUUID, new JSONObject().put("uuid", uuid.toString())));
	}

	@Override
	public Socket getSocket() {
		return socket;
	}

	@Override
	public boolean isAlive() {
		return alive;
	}

	@Override
	public UUID getUuid() {
		return uuid;
	}

	@Override
	public long getUptime() {
		return new Date().getTime() - startTime;
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
	public Writer getWriter() {
		return writer;
	}

	@Override
	public void kill() throws IOException {
		Logger.getInstance().log(Logger.DEBUG, "Trying to close all client connections");
		EventBus.getInstance().publish(new ClientDisconnectEvent(this));
		reader.close();
		writer.close();
		socket.close();
		Logger.getInstance().log(Logger.DEBUG, "All client connections closed");
	}
}
