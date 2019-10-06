package dev.juho.ffmpegrender.server;

import dev.juho.ffmpegrender.events.EventBus;
import dev.juho.ffmpegrender.server.client.Client;
import dev.juho.ffmpegrender.server.client.ServerClient;
import dev.juho.ffmpegrender.utils.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.UUID;

public class Server extends Thread {

	public static boolean ACCEPT_CONNECTIONS;
	public static UUID serverUUID = UUID.randomUUID();

	private long startTime;
	private int port;
	private ClientPool clientPool;

	public Server(int port) {
		this.port = port;
		this.clientPool = new ClientPool();
		EventBus.getInstance().register(clientPool);
		ACCEPT_CONNECTIONS = true;
	}

	@Override
	public void run() {
		startTime = new Date().getTime();
		Logger.getInstance().log(Logger.INFO, "Running server on port " + port);

		ServerSocket socket = null;
		try {
			socket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (ACCEPT_CONNECTIONS) {
			Socket acceptSocket = null;
			try {
				acceptSocket = socket.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}

			Logger.getInstance().log(Logger.DEBUG, "Accepting a new connection from " + acceptSocket.getInetAddress().getHostAddress());
			ServerClient client = new ServerClient(acceptSocket);
			clientPool.add(client);
		}
	}

	public long getStartTime() {
		return startTime;
	}

	public ClientPool getClientPool() {
		return clientPool;
	}

	public int getPort() {
		return port;
	}

	public long getUptime() {
		return new Date().getTime() - startTime;
	}
}
