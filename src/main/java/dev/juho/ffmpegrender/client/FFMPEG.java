package dev.juho.ffmpegrender.client;

import dev.juho.ffmpegrender.utils.ArgsParser;
import dev.juho.ffmpegrender.utils.Logger;
import dev.juho.ffmpegrender.utils.OSInfo;
import dev.juho.ffmpegrender.utils.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FFMPEG {

	private List<File> currentVideos;
	private String renderOptions;

	public FFMPEG() {
		this.currentVideos = new ArrayList<>();
		this.renderOptions = "";
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
		Logger.getInstance().log(Logger.DEBUG, "Starting to concat video");

		String saveFolder = "files";
		if (ArgsParser.getInstance().has("-save_folder"))
			saveFolder = ArgsParser.getInstance().getString("-save_folder");

		File file = new File("tempConcatFile.txt");
		PrintWriter writer = new PrintWriter(new FileWriter(file));

		for (File f : currentVideos) {
			writer.println("file '" + f.getAbsolutePath() + "'");
		}

		writer.flush();
		writer.close();

		String videoName = createVideoName();

		String builderCommand = "ffmpeg -f concat -safe 0 -i tempConcatFile.txt -c copy \"" + saveFolder + "/" + videoName + "\"";
		ProcessBuilder builder;

		if (OSInfo.getOSType() == OSInfo.OSType.WINDOWS) {
			builder = new ProcessBuilder("cmd.exe", "/C", builderCommand);
		} else {
			builder = new ProcessBuilder("ffmpeg", "-f", "concat", "-safe", "0", "-i", "tempConcatFile.txt", "-c", "copy", saveFolder + "/" + videoName);
		}

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

		Logger.getInstance().log(Logger.DEBUG, "Video concat done");
		return videoName;
	}

	public String render(String video) throws IOException {
		Logger.getInstance().log(Logger.DEBUG, "Starting rendering " + video);
		if (video.isEmpty()) {
			Logger.getInstance().log(Logger.ERROR, "Couldn't render the video! Name has not been set (current name: " + video + ")");
			return null;
		}

		String saveFolder = "files";
		if (ArgsParser.getInstance().has("-save_folder"))
			saveFolder = ArgsParser.getInstance().getString("-save_folder");

		if (renderOptions.isEmpty()) {
			Logger.getInstance().log(Logger.ERROR, "Render options not set for some reason");
			return null;
		}

		String finalName = saveFolder + "/RENDER_" + video;

		String ffmpegCommand = "ffmpeg -i \"" + saveFolder + "/" + video + "\" " + renderOptions + " \"" + finalName + "\"";
		Logger.getInstance().log(Logger.DEBUG, "FFMPEG command: " + ffmpegCommand);

		ProcessBuilder builder;

		String[] renderOptionsSplit = renderOptions.split(" ");
		String[] cmd = new String[4 + renderOptionsSplit.length];

		int i = 0;
		cmd[i++] = "ffmpeg";
		cmd[i++] = "-i";
		cmd[i++] = saveFolder + "/" + video;

		for (String s : renderOptionsSplit) {
			cmd[i++] = s;
		}

		cmd[i] = finalName;

		if (OSInfo.getOSType() == OSInfo.OSType.WINDOWS) {
			builder = new ProcessBuilder("cmd.exe", "/C", ffmpegCommand);
		} else {
			builder = new ProcessBuilder(cmd);
		}

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

		Logger.getInstance().log(Logger.DEBUG, "Video rendered");
		return finalName;
	}

	public void deleteCurrentVideos(String concatVideo, String finalVideo) {
		for (File f : currentVideos) {
			boolean deleted = f.delete();
			if (deleted) {
				Logger.getInstance().log(Logger.DEBUG, f.getAbsolutePath() + " deleted");
			} else {
				Logger.getInstance().log(Logger.WARNING, "Couldn't delete " + f.getAbsolutePath());
			}
		}

		String saveFolder = "files";
		if (ArgsParser.getInstance().has("-save_folder"))
			saveFolder = ArgsParser.getInstance().getString("-save_folder");

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

		File renderFile = new File(finalVideo);
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

	private String createVideoName() {
		String name = currentVideos.get(0).getName();
		name += "-";
		name += currentVideos.get(currentVideos.size() - 1).getName();
		name += "_" + Utils.randomString() + ".mp4";
		return name;
	}

}
