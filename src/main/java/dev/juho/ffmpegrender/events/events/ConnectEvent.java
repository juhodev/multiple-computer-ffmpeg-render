package dev.juho.ffmpegrender.events.events;

import dev.juho.ffmpegrender.events.Event;
import dev.juho.ffmpegrender.events.EventType;
import dev.juho.ffmpegrender.server.client.Client;

public class ConnectEvent implements Event {

	private Client client;
	private boolean cancelled;

	public ConnectEvent(Client client) {
		this.client = client;
	}

	@Override
	public EventType getType() {
		return EventType.CONNECTION;
	}

	@Override
	public Client getData() {
		return client;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void cancel() {
		this.cancelled = true;
	}
}
