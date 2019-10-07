package dev.juho.ffmpegrender.client;

import dev.juho.ffmpegrender.utils.ArgsParser;
import dev.juho.ffmpegrender.utils.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Files {

	private JSONArray fileArray;
	private File saveFileForFiles;

	public Files() {
		this.fileArray = new JSONArray();

		String saveFolder = "files";
		if (ArgsParser.getInstance().has("-save_folder"))
			saveFolder = ArgsParser.getInstance().getString("-save_folder");

		this.saveFileForFiles = new File(saveFolder + "/videoInfo.json");
	}

	public void setFileArray(JSONArray fileArray) {
		this.fileArray = fileArray;
	}

	public void saveRendered(JSONObject videoInfo) {
		Logger.getInstance().log(Logger.DEBUG, "Received render info");
		fileArray.put(videoInfo);

		try {
			saveToDisk();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveToDisk() throws IOException {
		FileWriter writer = new FileWriter(saveFileForFiles);
		writer.write(fileArray.toString());
		writer.flush();
		writer.close();
	}

}
