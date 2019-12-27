package dev.juho.ffmpegrender.server.client;

import dev.juho.ffmpegrender.server.client.reader.FileData;
import dev.juho.ffmpegrender.server.message.Message;
import dev.juho.ffmpegrender.server.stats.RenderHistory;
import dev.juho.ffmpegrender.utils.Logger;

import java.io.*;
import java.nio.ByteBuffer;

public class Writer {

	private OutputStream os;

	public Writer(OutputStream os) {
		this.os = os;
	}

	public void write(Message message) {
		Logger.getInstance().log(Logger.DEBUG, "Writing a message " + message.getData().toString());
		try {
			os.write(message.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void write(File file) {
		ByteBuffer infoBuffer = createFileInfoBuffer(file.length(), file.getName());
		Logger.getInstance().log(Logger.DEBUG, "sending " + file.getAbsolutePath());
		Logger.getInstance().createProgressbar("Sending file", file.length());

		long fileSent = 0;
		try {
			os.write(infoBuffer.array());

			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			byte[] buffer = new byte[4096];

			int read;
			while ((read = bis.read(buffer)) != -1) {
				fileSent += read;
				os.write(buffer, 0, read);
				Logger.getInstance().updateProgressbar("Sending file", fileSent);
				RenderHistory.getInstance().addBytesSent(read);
			}

			Logger.getInstance().removeProgressbar("Sending file");
			bis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendPing() throws IOException {
		os.write(MessageIdentifier.PING);
	}

	private ByteBuffer createFileInfoBuffer(long fileLength, String name) {
		int nameLength = name.getBytes().length;

		ByteBuffer infoBuffer = ByteBuffer.allocate(FileData.MESSAGE_IDENTIFIER_SIZE + FileData.NAME_LENGTH_BUFFER_SIZE + nameLength + FileData.FILE_LENGTH_BUFFER_SIZE);
		infoBuffer.put(MessageIdentifier.FILE_INFO);
		infoBuffer.putInt(nameLength);
		infoBuffer.put(name.getBytes());
		infoBuffer.putLong(fileLength);
		return infoBuffer;
	}

	public void close() throws IOException {
		os.flush();
		os.close();
	}

}
