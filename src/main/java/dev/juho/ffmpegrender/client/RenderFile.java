package dev.juho.ffmpegrender.client;

public class RenderFile {

	private int duration;
	private int timeRendered;

	public RenderFile() {
		this.duration = -1;
		this.timeRendered = -1;
	}

	public void updateProgress(String line) {
		String time = line.split("Duration: ")[1].split(", ")[0];

		int seconds = 0;
		String[] split = time.split(":");

		int hours = Integer.parseInt(split[0]);
		seconds += hours * 60 * 60;

		int minutes = Integer.parseInt(split[1]);
		seconds += minutes * 60;

		int splitSeconds = Integer.parseInt(split[2]);
		seconds += splitSeconds;

		timeRendered = seconds;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getDuration() {
		return duration;
	}

	public int getTimeRendered() {
		return timeRendered;
	}

	public void reset() {
		this.duration = -1;
		this.timeRendered = -1;
	}

}
