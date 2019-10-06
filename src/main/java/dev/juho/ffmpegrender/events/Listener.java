package dev.juho.ffmpegrender.events;

import java.util.List;
import java.util.UUID;

public interface Listener {

	void handle(Event<EventType, ?> e);

	List<EventType> supports();

}
