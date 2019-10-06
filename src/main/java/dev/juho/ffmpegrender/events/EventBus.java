package dev.juho.ffmpegrender.events;

import dev.juho.ffmpegrender.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventBus {

	private List<Listener> listeners;
	private static EventBus instance;

	private EventBus() {
		this.listeners = new ArrayList<>();
	}

	public void register(Listener l) {
		Logger.getInstance().log(Logger.DEBUG, "New listener registered!");
		listeners.add(l);
	}

	public void publish(Event e) {
		for (int i = 0; i < listeners.size(); i++) {
			Listener l = listeners.get(i);

			if (e.isCancelled()) {
				return;
			}

			if (l.supports().contains(e.getType())) {
				l.handle(e);
			}
		}
	}

	public static EventBus getInstance() {
		if (instance == null) {
			instance = new EventBus();
		}

		return instance;
	}
}
