package dev.juho.ffmpegrender.events.events;

import dev.juho.ffmpegrender.events.Event;
import dev.juho.ffmpegrender.events.EventType;

public class RenderProgressEvent implements Event<EventType, Double> {

	private double progress;
	private boolean cancelled;

	public RenderProgressEvent(double progress) {
		this.progress = progress;
		this.cancelled = false;
	}

	@Override
	public EventType getType() {
		return EventType.RENDER_PROGRESS;
	}

	@Override
	public Double getData() {
		return progress;
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
