package dev.juho.ffmpegrender.server.client;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

public interface Client {

	boolean isAlive();

	Writer getWriter();

	Socket getSocket();

	UUID getUuid();

	long getUptime();

	void ping();

	void listen() throws IOException;

	void kill() throws IOException;

}
