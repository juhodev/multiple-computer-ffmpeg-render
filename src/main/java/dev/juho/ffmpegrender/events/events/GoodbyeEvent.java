package dev.juho.ffmpegrender.events.events;

import dev.juho.ffmpegrender.events.Event;
import dev.juho.ffmpegrender.events.EventType;
import dev.juho.ffmpegrender.server.client.Client;

import java.util.UUID;

public class GoodbyeEvent implements Event {

	private UUID uuid;
	private boolean cancelled;

	public GoodbyeEvent(UUID uuid) {
		this.uuid = uuid;
		this.cancelled = false;
	}

	@Override
	public EventType getType() {
		return EventType.GOODBYE;
	}

	@Override
	public UUID getData() {
		return uuid;
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
