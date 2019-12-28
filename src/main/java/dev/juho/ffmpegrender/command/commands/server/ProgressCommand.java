package dev.juho.ffmpegrender.command.commands.server;

import dev.juho.ffmpegrender.command.Command;
import dev.juho.ffmpegrender.command.CommandType;
import dev.juho.ffmpegrender.server.ClientPool;
import dev.juho.ffmpegrender.server.stats.RenderHistory;
import dev.juho.ffmpegrender.utils.Logger;
import dev.juho.ffmpegrender.utils.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class ProgressCommand extends Command {

	public ProgressCommand() {
		super(CommandType.PROGRESS, "Get info about clients render progress", "progress", "client_render_progress", "render_progress", "progress", "crp");
	}

	@Override
	public void execute(String... args) {
		Logger.getInstance().log(Logger.INFO, "Bytes sent: " + Utils.humanReadableByteCountSI(RenderHistory.getInstance().getBytesSent()));
		Logger.getInstance().log(Logger.INFO, "Bytes received: " + Utils.humanReadableByteCountSI(RenderHistory.getInstance().getBytesReceived()));
		Logger.getInstance().log(Logger.INFO, "");
		Logger.getInstance().log(Logger.INFO, "Clients: ");
		HashMap<UUID, Double> progress = RenderHistory.getInstance().getProgress();
		progress.forEach(((uuid, aDouble) -> Logger.getInstance().log(Logger.INFO, uuid + ": " + Math.round(aDouble * 100) + "%")));
	}
}
