package dev.juho.ffmpegrender;

import dev.juho.ffmpegrender.client.FFMPEGClient;
import dev.juho.ffmpegrender.command.CmdExecutor;
import dev.juho.ffmpegrender.command.commands.client.ClientStopCommand;
import dev.juho.ffmpegrender.command.commands.server.*;
import dev.juho.ffmpegrender.events.EventBus;
import dev.juho.ffmpegrender.client.Files;
import dev.juho.ffmpegrender.client.RenderQueue;
import dev.juho.ffmpegrender.server.Server;
import dev.juho.ffmpegrender.utils.ArgsParser;
import dev.juho.ffmpegrender.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Main {

	public static void main(String[] args) {
		Logger.LOG_LEVEL = Logger.INFO;
		Logger.getInstance().log(Logger.INFO, "Starting ffmpegrender!");

		HashMap<String, ArgsParser.Type> programArgs = new HashMap<>();
		programArgs.put("-client", ArgsParser.Type.STRING);
		programArgs.put("-server", ArgsParser.Type.STRING);
		programArgs.put("-address", ArgsParser.Type.STRING);
		programArgs.put("-port", ArgsParser.Type.STRING);
		programArgs.put("-videos_in_one", ArgsParser.Type.STRING);
		programArgs.put("-s_folder", ArgsParser.Type.STRING);
		programArgs.put("-r_folder", ArgsParser.Type.LIST);
		programArgs.put("-ignore", ArgsParser.Type.LIST);
		programArgs.put("-recursive", ArgsParser.Type.NONE);
		programArgs.put("-debug", ArgsParser.Type.NONE);

		ArgsParser.getInstance().setArguments(programArgs);
		ArgsParser.getInstance().parse(args);

		if (ArgsParser.getInstance().has("-debug")) {
			Logger.LOG_LEVEL = Logger.DEBUG;
			Logger.getInstance().log(Logger.DEBUG, "DEBUG MODE ENABLED");
		}

		setupFolders(ArgsParser.getInstance().has("-s_folder"));

		if (ArgsParser.getInstance().has("-server") || (!ArgsParser.getInstance().has("-server") && !ArgsParser.getInstance().has("-client"))) {
			int serverPort = ArgsParser.getInstance().has("-port") ? ArgsParser.getInstance().getInt("-port") : 7592;

			Server server = new Server(serverPort);
			server.start();

			Files files = new Files();

			RenderQueue renderQueue = new RenderQueue(files, server.getClientPool());
			EventBus.getInstance().register(renderQueue);

			CmdExecutor cmdExecutor = new CmdExecutor();
			cmdExecutor.start();
			cmdExecutor.register(new ServerCommand(server));
			cmdExecutor.register(new StopCommand(server));
			cmdExecutor.register(new RenderCommand(server.getClientPool(), renderQueue));
			cmdExecutor.register(new QueueCommand(renderQueue));
			cmdExecutor.register(new AddFolderCommand(renderQueue));
		} else if (ArgsParser.getInstance().has("-client")) {
			if (!ArgsParser.getInstance().has("-host") && !ArgsParser.getInstance().has("-port")) {
				Logger.getInstance().log(Logger.ERROR, "-host or -port missing!");
				System.exit(0);
			}

			String host = ArgsParser.getInstance().getString("-host");
			int port = ArgsParser.getInstance().getInt("-port");

			FFMPEGClient ffmpegClient = new FFMPEGClient(host, port);
			EventBus.getInstance().register(ffmpegClient);

			CmdExecutor cmdExecutor = new CmdExecutor();
			cmdExecutor.start();
			cmdExecutor.register(new ClientStopCommand(ffmpegClient));
			try {
				ffmpegClient.listen();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void setupFolders(boolean customSaveFolder) {
		String[] folders;

		if (customSaveFolder) {
			folders = new String[]{"files"};
		} else {
			folders = new String[]{ArgsParser.getInstance().getString("-s_folder")};
		}

		for (String folder : folders) {
			File f = new File(folder);

			if (!f.exists()) {
				Logger.getInstance().log(Logger.INFO, "Creating " + folder + " folder");
				f.mkdir();
			}
		}
	}

}
