package dev.juho.ffmpegrender.server.stats;

import java.util.HashMap;
import java.util.UUID;

public class RenderHistory {

	private static RenderHistory instance;

	private HashMap<UUID, Double> progress;
	private long bytesSent, bytesReceived;

	private RenderHistory() {
		this.progress = new HashMap<>();
		this.bytesSent = 0;
		this.bytesReceived = 0;
	}

	public static RenderHistory getInstance() {
		if (instance == null) {
			instance = new RenderHistory();
		}

		return instance;
	}

	public void updateProgress(UUID uuid, double percentage) {
		progress.put(uuid, percentage);
	}

	public void addBytesSent(long bytes) {
		bytesSent += bytes;
	}

	public void addBytesReceived(long bytes) {
		bytesReceived += bytes;
	}

	public long getBytesReceived() {
		return bytesReceived;
	}

	public long getBytesSent() {
		return bytesSent;
	}

	public HashMap<UUID, Double> getProgress() {
		return progress;
	}
}
