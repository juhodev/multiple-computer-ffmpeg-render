package dev.juho.ffmpegrender.server;

import dev.juho.ffmpegrender.events.Event;
import dev.juho.ffmpegrender.events.EventType;
import dev.juho.ffmpegrender.events.Listener;
import dev.juho.ffmpegrender.server.client.Client;
import dev.juho.ffmpegrender.server.message.Message;
import dev.juho.ffmpegrender.server.message.MessageType;
import dev.juho.ffmpegrender.utils.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

public class ClientPool implements Listener {

	private HashMap<UUID, Client> clients;

	public ClientPool() {
		this.clients = new HashMap<>();
	}

	public void add(Client client) {
		Logger.getInstance().log(Logger.DEBUG, client.getUuid().toString() + " added to client pool");
		this.clients.put(client.getUuid(), client);
		try {
			client.listen();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getSize() {
		return clients.size();
	}

	public HashMap<UUID, Client> getClients() {
		return clients;
	}

	public Client get(UUID uuid) {
		return clients.get(uuid);
	}

	public void killClient(Client client) {
		Logger.getInstance().log(Logger.INFO, "Trying to kill client " + client.getUuid());
		if (clients.containsKey(client.getUuid())) {
			try {
				clients.get(client.getUuid()).kill();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Logger.getInstance().log(Logger.ERROR, "Didn't find a client to kill (UUID: " + client.getUuid() + ")");
			return;
		}

		Logger.getInstance().log(Logger.INFO, "Client " + client.getUuid() + " killed");
		clients.remove(client.getUuid());
	}

	public void sendAll(Message message) {
		Iterator it = clients.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<UUID, Client> pair = (Map.Entry<UUID, Client>) it.next();

			pair.getValue().getWriter().write(message);
		}
	}

	@Override
	public void handle(Event<EventType, ?> e) {
		switch (e.getType()) {
			case MESSAGE:
				Message msg = (Message) e.getData();
				if (msg.getType() == MessageType.GOODBYE) {
					UUID uuid = msg.getSender();
					clients.remove(uuid);
					Logger.getInstance().log(Logger.DEBUG, "Received a goodbye message from client " + uuid);
				}
				break;

			case CRASH:
				Client client = (Client) e.getData();
				Logger.getInstance().log(Logger.WARNING, "Received a crash event from " + client.getUuid());
				killClient(client);
				break;
		}
	}

	@Override
	public List<EventType> supports() {
		return Arrays.asList(EventType.MESSAGE, EventType.CRASH);
	}
}
