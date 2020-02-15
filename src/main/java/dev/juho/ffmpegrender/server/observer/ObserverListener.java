package dev.juho.ffmpegrender.server.observer;

import dev.juho.ffmpegrender.events.Event;
import dev.juho.ffmpegrender.events.EventType;
import dev.juho.ffmpegrender.events.Listener;
import dev.juho.ffmpegrender.events.events.MessageEvent;
import dev.juho.ffmpegrender.server.ClientPool;
import dev.juho.ffmpegrender.server.client.Client;
import dev.juho.ffmpegrender.server.client.ServerClient;
import dev.juho.ffmpegrender.server.message.Message;
import dev.juho.ffmpegrender.server.message.MessageType;
import dev.juho.ffmpegrender.server.stats.RenderHistory;
import dev.juho.ffmpegrender.utils.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class ObserverListener implements Listener {

	private ClientPool clientPool;

	public ObserverListener(ClientPool clientPool) {
		this.clientPool = clientPool;
	}

	@Override
	public void handle(Event<EventType, ?> e) {
		Logger.getInstance().log(Logger.DEBUG, "observer data");
		if (e.getType() == EventType.MESSAGE) {
			Message msg = ((MessageEvent) e).getData();
			Client senderClient = clientPool.get(msg.getSender());
			Logger.getInstance().log(Logger.DEBUG, "observer data");

			switch (msg.getType()) {
				case GET_OBSERVER_DATA:
					Logger.getInstance().log(Logger.DEBUG, "observer data");
					JSONObject dataObj = new JSONObject();
					dataObj.put("clients", getConnectedClients());
					dataObj.put("progress", getProgressObject());
					senderClient.getWriter().write(Message.build(MessageType.OBSERVER_DATA, msg.getSender(), dataObj));
					e.cancel();
					break;
			}
		}
	}

	@Override
	public List<EventType> supports() {
		return Arrays.asList(EventType.MESSAGE);
	}

	private JSONObject getProgressObject() {
		JSONObject progressObj = new JSONObject();
		HashMap<UUID, Double> progress = RenderHistory.getInstance().getProgress();

		progress.forEach(((uuid, aDouble) -> progressObj.put(uuid.toString(), aDouble)));
		return progressObj;
	}

	private JSONArray getConnectedClients() {
		JSONArray connectedClients = new JSONArray();
		HashMap<UUID, Client> clients = clientPool.getClients();

		clients.forEach((uuid, client) -> {
			ServerClient serverClient = (ServerClient) client;
			JSONObject clientObj = new JSONObject();
			clientObj.put("uuid", uuid.toString());
			clientObj.put("uptime", serverClient.getUptime());
			clientObj.put("type", serverClient.getClientType());
			connectedClients.put(clientObj);
		});

		return connectedClients;
	}
}
