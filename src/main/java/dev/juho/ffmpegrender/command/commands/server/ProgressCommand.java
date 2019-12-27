package dev.juho.ffmpegrender.command.commands.server;

import dev.juho.ffmpegrender.command.Command;
import dev.juho.ffmpegrender.command.CommandType;
import dev.juho.ffmpegrender.server.ClientPool;
import dev.juho.ffmpegrender.server.stats.RenderHistory;
import dev.juho.ffmpegrender.utils.Logger;

public class ProgressCommand extends Command {

	public ProgressCommand() {
		super(CommandType.PROGRESS, "Get info about clients render progress", "progress", "client_render_progress", "render_progress", "progress", "crp");
	}

	@Override
	public void execute(String... args) {
		Logger.getInstance().log(Logger.INFO, "Bytes sent: " + RenderHistory.getInstance().getBytesSent());
		Logger.getInstance().log(Logger.INFO, "Bytes received: " + RenderHistory.getInstance().getBytesReceived());
	}
}
