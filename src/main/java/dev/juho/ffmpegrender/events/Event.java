package dev.juho.ffmpegrender.events;

public interface Event<EventType, T> {

	EventType getType();

	T getData();

	boolean isCancelled();

	void cancel();

}
