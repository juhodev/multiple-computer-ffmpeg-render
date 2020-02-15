package dev.juho.ffmpegrender.client;

import dev.juho.ffmpegrender.events.EventBus;
import dev.juho.ffmpegrender.events.events.RenderProgressEvent;
import dev.juho.ffmpegrender.server.client.Writer;
import dev.juho.ffmpegrender.server.message.Message;
import dev.juho.ffmpegrender.utils.Logger;
import dev.juho.ffmpegrender.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class RenderFile {

	private File originalFile;
	private int duration;
	private int timeRendered;

	public RenderFile() {
		this.duration = -1;
		this.timeRendered = -1;
		this.originalFile = null;
	}

	public void updateProgress(String line) {
		if (line.contains("time=")) {
			String time = line.split("time=")[1].split(" bitrate")[0];
			String[] timeSplit = time.split(":");

			int hours = Integer.parseInt(timeSplit[0]);
			int minutes = Integer.parseInt(timeSplit[1]);
			int seconds = (int) Double.parseDouble(timeSplit[2]);

			// Time rendered in seconds
			int newTimeRendered = hours * 60 * 60 + minutes * 60 + seconds;
			if (newTimeRendered != timeRendered) {
				timeRendered = newTimeRendered;
				EventBus.getInstance().publish(new RenderProgressEvent((double) timeRendered / (double) duration));
			}
			Logger.getInstance().updateProgressbar("render", timeRendered);
		}
	}

	public void init(File f) {
		this.originalFile = f;

		try {
			getFileDuration();
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	private void getFileDuration() throws IOException {
		if (originalFile == null) {
			Logger.getInstance().log(Logger.ERROR, "File not set! Couldn't get file duration");
			return;
		}

		String[] command = Utils.buildCommand("ffmpeg -i", "\"" + originalFile.getAbsolutePath() + "\"");

		ProcessBuilder builder = new ProcessBuilder(command);

		builder.redirectErrorStream(true);
		Process process = builder.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

		String line;
		while ((line = reader.readLine()) != null) {
			if (line.contains("Duration: ")) {
				String time = line.split("Duration: ")[1].split(", ")[0];
				String[] timeSplit = time.split(":");

				int hours = Integer.parseInt(timeSplit[0]);
				int minutes = Integer.parseInt(timeSplit[1]);
				int seconds = (int) Double.parseDouble(timeSplit[2]);

				duration = hours * 60 * 60 + minutes * 60 + seconds;
				Logger.getInstance().createProgressbar("render", duration);
			}
		}
	}

}
