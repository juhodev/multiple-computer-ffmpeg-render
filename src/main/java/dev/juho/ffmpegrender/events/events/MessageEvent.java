package dev.juho.ffmpegrender.events.events;

import dev.juho.ffmpegrender.events.Event;
import dev.juho.ffmpegrender.events.EventType;
import dev.juho.ffmpegrender.server.message.Message;
import dev.juho.ffmpegrender.server.message.MessageType;

public class MessageEvent implements Event {

	private Message message;
	private boolean cancelled;

	public MessageEvent(Message message) {
		this.message = message;
		this.cancelled = false;
	}

	@Override
	public EventType getType() {
		return EventType.MESSAGE;
	}

	@Override
	public Message getData() {
		return message;
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
