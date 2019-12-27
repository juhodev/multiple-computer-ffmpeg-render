package dev.juho.ffmpegrender.server.client.reader;

import dev.juho.ffmpegrender.events.EventBus;
import dev.juho.ffmpegrender.events.events.CrashEvent;
import dev.juho.ffmpegrender.server.client.Client;
import dev.juho.ffmpegrender.server.client.MessageIdentifier;
import dev.juho.ffmpegrender.utils.ArgsParser;
import dev.juho.ffmpegrender.utils.Logger;

import java.io.*;

public class Reader extends Thread {

	private boolean running;
	private BufferedInputStream bis;
	private Client client;

	private ReaderData readerData;

	public Reader(Client client, InputStream in) {
		this.client = client;
		this.bis = new BufferedInputStream(in);
		this.running = true;
	}

	@Override
	public void run() {
		Logger.getInstance().log(Logger.DEBUG, "Running " + client.getUuid() + " reader");
		while (running) {
			try {
				byte[] buffer = new byte[2048];

				int read;
				while ((read = bis.read(buffer)) != -1) {
					int offset = 0;
					while (offset < read) {
						if (readerData == null) {
							byte identifier = buffer[offset];
							offset += 1;
							if (identifier == MessageIdentifier.MESSAGE) {
								readerData = new MessageData();
							} else if (identifier == MessageIdentifier.FILE_INFO) {
								String saveFolder = "files";
								if (ArgsParser.getInstance().has(ArgsParser.Argument.SAVE_FOLDER))
									saveFolder = ArgsParser.getInstance().getString(ArgsParser.Argument.SAVE_FOLDER);

								readerData = new FileData(saveFolder);
							} else if (identifier == MessageIdentifier.PING) {
								break;
							}
						} else {
							readerData.resetLength();
						}

						readerData.write(buffer, offset, read);
						offset += readerData.getLength();

						if (readerData.isFull()) {
							readerData.close();
							readerData = null;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				Logger.getInstance().log(Logger.DEBUG, "Can't read bis. Killing reader.");
				this.running = false;

				EventBus.getInstance().publish(new CrashEvent(client));
			}
		}
	}

	public void close() throws IOException {
		this.running = false;
		this.bis.close();
	}

}
