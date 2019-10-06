package dev.juho.ffmpegrender.events.events;

import dev.juho.ffmpegrender.events.Event;
import dev.juho.ffmpegrender.events.EventType;
import dev.juho.ffmpegrender.server.client.Client;

public class CrashEvent implements Event {

	private Client client;
	private boolean cancelled;

	public CrashEvent(Client client) {
		this.client = client;
		this.cancelled = false;
	}

	@Override
	public EventType getType() {
		return EventType.CRASH;
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
		cancelled = true;
	}
}
