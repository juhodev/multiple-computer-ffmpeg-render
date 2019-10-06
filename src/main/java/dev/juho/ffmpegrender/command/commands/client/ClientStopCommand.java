package dev.juho.ffmpegrender.command.commands.client;

import dev.juho.ffmpegrender.client.FFMPEGClient;
import dev.juho.ffmpegrender.command.Command;
import dev.juho.ffmpegrender.command.CommandType;
import dev.juho.ffmpegrender.utils.Logger;

public class ClientStopCommand extends Command {

	private FFMPEGClient client;

	public ClientStopCommand(FFMPEGClient client) {
		super(CommandType.CLIENT_STOP, "Tells the client to shutdown after the current render is complete", "stop", "stop");

		this.client = client;
	}

	@Override
	public void execute(String... args) {
		Logger.getInstance().log(Logger.INFO, "Client will shutdown after the render is complete!");
		client.shutdownAfterRender();
	}
}
