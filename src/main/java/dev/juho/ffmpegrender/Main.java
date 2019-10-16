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

		ArgsParser.getInstance().add(ArgsParser.Argument.CLIENT, ArgsParser.Type.STRING, "-c", "-client");
		ArgsParser.getInstance().add(ArgsParser.Argument.SERVER, ArgsParser.Type.STRING, "-s", "-server");
		ArgsParser.getInstance().add(ArgsParser.Argument.ADDRESS, ArgsParser.Type.STRING, "-address", "-host", "-ip");
		ArgsParser.getInstance().add(ArgsParser.Argument.PORT, ArgsParser.Type.STRING, "-port");
		ArgsParser.getInstance().add(ArgsParser.Argument.VIDEOS_IN_ONE, ArgsParser.Type.STRING, "-videos_in_one", "-in_one", "-videos");
		ArgsParser.getInstance().add(ArgsParser.Argument.SAVE_FOLDER, ArgsParser.Type.STRING, "-s_folder", "-save_folder");
		ArgsParser.getInstance().add(ArgsParser.Argument.RENDER_FOLDER, ArgsParser.Type.LIST, "-r_folder", "-render_folder");
		ArgsParser.getInstance().add(ArgsParser.Argument.IGNORE, ArgsParser.Type.LIST, "-ignore", "-ignore_files_under", "-ignore_subfolder");
		ArgsParser.getInstance().add(ArgsParser.Argument.RECURSIVE, ArgsParser.Type.NONE, "-recursive");
		ArgsParser.getInstance().add(ArgsParser.Argument.DEBUG, ArgsParser.Type.NONE, "-debug");

		ArgsParser.getInstance().parse(args);

		if (ArgsParser.getInstance().has(ArgsParser.Argument.DEBUG)) {
			Logger.LOG_LEVEL = Logger.DEBUG;
			Logger.getInstance().log(Logger.DEBUG, "DEBUG MODE ENABLED");
		}

		setupFolders(ArgsParser.getInstance().has(ArgsParser.Argument.SAVE_FOLDER));

		if (ArgsParser.getInstance().has(ArgsParser.Argument.SERVER) || (!ArgsParser.getInstance().has(ArgsParser.Argument.SERVER) && !ArgsParser.getInstance().has(ArgsParser.Argument.CLIENT))) {
			int serverPort = ArgsParser.getInstance().has(ArgsParser.Argument.PORT) ? ArgsParser.getInstance().getInt(ArgsParser.Argument.PORT) : 7592;

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
		} else if (ArgsParser.getInstance().has(ArgsParser.Argument.CLIENT)) {
			if (!ArgsParser.getInstance().has(ArgsParser.Argument.ADDRESS) && !ArgsParser.getInstance().has(ArgsParser.Argument.PORT)) {
				Logger.getInstance().log(Logger.ERROR, "-host or -port missing!");
				System.exit(0);
			}

			String host = ArgsParser.getInstance().getString(ArgsParser.Argument.ADDRESS);
			int port = ArgsParser.getInstance().getInt(ArgsParser.Argument.PORT);

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
			folders = new String[]{ArgsParser.getInstance().getString(ArgsParser.Argument.SAVE_FOLDER)};
		} else {
			folders = new String[]{"files"};
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
