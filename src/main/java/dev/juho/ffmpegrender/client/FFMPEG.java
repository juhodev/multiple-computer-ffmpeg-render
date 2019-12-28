package dev.juho.ffmpegrender.client;

import dev.juho.ffmpegrender.events.EventBus;
import dev.juho.ffmpegrender.events.events.RenderProgressEvent;
import dev.juho.ffmpegrender.utils.ArgsParser;
import dev.juho.ffmpegrender.utils.Logger;
import dev.juho.ffmpegrender.utils.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FFMPEG {

	private List<File> currentVideos;
	private String renderOptions;
	private RenderFile renderFile;

	public FFMPEG() {
		this.currentVideos = new ArrayList<>();
		this.renderOptions = "";
		this.renderFile = new RenderFile();
	}

	public void setRenderOptions(String renderOptions) {
		this.renderOptions = renderOptions;
	}

	public void addVideo(File video) {
		currentVideos.add(video);
	}

	/**
	 * Concatenates all video files received from the server
	 *
	 * @return Name of the concat file
	 * @throws IOException IOException
	 */
	public String concat() throws IOException {
		if (currentVideos.size() == 1) {
			Logger.getInstance().log(Logger.ERROR, "There is only one video in currentVideos");
			return null;
		}

		Logger.getInstance().log(Logger.INFO, "Starting to concat video");

		String saveFolder = "files";
		if (ArgsParser.getInstance().has(ArgsParser.Argument.SAVE_FOLDER))
			saveFolder = ArgsParser.getInstance().getString(ArgsParser.Argument.SAVE_FOLDER);

		File file = new File("tempConcatFile.txt");
		PrintWriter writer = new PrintWriter(new FileWriter(file));

		for (File f : currentVideos) {
			writer.println("file '" + f.getAbsolutePath().replaceAll("'", "'\\\\''") + "'");
		}

		writer.flush();
		writer.close();

		String videoName = createVideoName();

		String[] command = Utils.buildCommand("ffmpeg -f concat -safe 0 -i tempConcatFile.txt -c copy \"" + saveFolder + "/" + videoName + "\"");
		Logger.getInstance().log(Logger.DEBUG, "Running ffmpeg concat command: ");
		Logger.getInstance().log(Logger.DEBUG, command);

		ProcessBuilder builder = new ProcessBuilder(command);

		builder.redirectErrorStream(true);
		Process process = builder.start();

		new Thread(() -> {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

				String line;
				while ((line = reader.readLine()) != null) {
					Logger.getInstance().log(Logger.DEBUG, line);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();

		try {
			process.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Logger.getInstance().log(Logger.INFO, "Video concat done");
		return videoName;
	}

	public String render(String video) throws IOException {
		Logger.getInstance().log(Logger.INFO, "Starting rendering " + video);
		if (video.isEmpty()) {
			Logger.getInstance().log(Logger.ERROR, "Couldn't render the video! Name has not been set (current name: " + video + ")");
			return null;
		}

		String saveFolder = "files";
		if (ArgsParser.getInstance().has(ArgsParser.Argument.SAVE_FOLDER))
			saveFolder = ArgsParser.getInstance().getString(ArgsParser.Argument.SAVE_FOLDER);

		if (renderOptions.isEmpty()) {
			Logger.getInstance().log(Logger.ERROR, "Render options not set for some reason");
			return null;
		}

		renderFile.reset();
		renderFile.init(new File(saveFolder + "/" + video));

		String finalName = " \"" + saveFolder + "/RENDER_" + video + "\"";

		String[] command = Utils.buildCommand("ffmpeg -i \"" + saveFolder + "/" + video + "\" ", renderOptions, finalName);

		Logger.getInstance().log(Logger.DEBUG, "Running ffmpeg render command: ");
		Logger.getInstance().log(Logger.DEBUG, command);

		ProcessBuilder builder = new ProcessBuilder(command);

		builder.redirectErrorStream(true);
		Process process = builder.start();
		new Thread(() -> {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

				String line;
				while ((line = reader.readLine()) != null) {
					renderFile.updateProgress(line);
					Logger.getInstance().log(Logger.DEBUG, line);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Logger.getInstance().log(Logger.INFO, "Video rendered");
		Logger.getInstance().removeProgressbar("render");
		EventBus.getInstance().publish(new RenderProgressEvent(0));
		return finalName.trim();
	}

	public void deleteCurrentVideos(String concatVideo, String finalVideo) {
		if (!ArgsParser.getInstance().has(ArgsParser.Argument.LOCAL)) {
			for (File f : currentVideos) {
				boolean deleted = f.delete();
				if (deleted) {
					Logger.getInstance().log(Logger.DEBUG, f.getAbsolutePath() + " deleted");
				} else {
					Logger.getInstance().log(Logger.WARNING, "Couldn't delete " + f.getAbsolutePath());
				}
			}
		} else {
			Logger.getInstance().log(Logger.DEBUG, "Not deleting video files because this is a local client!");
		}

		String saveFolder = "files";
		if (ArgsParser.getInstance().has(ArgsParser.Argument.SAVE_FOLDER))
			saveFolder = ArgsParser.getInstance().getString(ArgsParser.Argument.SAVE_FOLDER);

		File concatFile = new File(saveFolder + "/" + concatVideo);
		if (concatFile.exists()) {
			boolean deleted = concatFile.delete();
			if (deleted) {
				Logger.getInstance().log(Logger.DEBUG, concatFile.getAbsolutePath() + " deleted");
			} else {
				Logger.getInstance().log(Logger.WARNING, "Couldn't delete " + concatFile.getAbsolutePath());
			}
		} else {
			Logger.getInstance().log(Logger.WARNING, "Couldn't find the concat video! (" + concatFile.getAbsolutePath() + ")");
		}

		File renderFile = new File(saveFolder + "/" + finalVideo);
		if (renderFile.exists()) {
			boolean deleted = renderFile.delete();
			if (deleted) {
				Logger.getInstance().log(Logger.DEBUG, renderFile.getAbsolutePath() + " deleted");
			} else {
				Logger.getInstance().log(Logger.WARNING, "Couldn't delete " + renderFile.getAbsolutePath());
			}
		} else {
			Logger.getInstance().log(Logger.WARNING, "Couldn't find the final video! (" + renderFile.getAbsolutePath() + ")");
		}

		currentVideos.clear();
	}

	public int getVideoCount() {
		return currentVideos.size();
	}

	public RenderFile getRenderFile() {
		return renderFile;
	}

	private String createVideoName() {
		String name = currentVideos.get(0).getName();
		name += "-";
		name += currentVideos.get(currentVideos.size() - 1).getName();
		name += "_" + Utils.randomString() + ".mp4";
		return name;
	}

	private String[] buildCommand(String... commands) {
		List<String> cmd = new ArrayList<>();

		boolean insideQuotes = false;
		StringBuilder builder = new StringBuilder();
		for (String command : commands) {
			for (int i = 0; i < command.length(); i++) {
				char c = command.charAt(i);

				if (c == ' ' && !insideQuotes) {
					cmd.add(builder.toString());
					builder.setLength(0);
				} else if (c == '\"') {
					insideQuotes = !insideQuotes;
				} else {
					builder.append(command.charAt(i));
				}
			}
		}

		if (builder.length() > 0) {
			cmd.add(builder.toString());
		}

		String[] cmdArray = new String[cmd.size()];
		cmd.toArray(cmdArray);

		return cmdArray;
	}

}
