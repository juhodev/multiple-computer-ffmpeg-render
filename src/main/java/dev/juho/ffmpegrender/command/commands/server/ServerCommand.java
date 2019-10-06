package dev.juho.ffmpegrender.command.commands.server;

import dev.juho.ffmpegrender.command.Command;
import dev.juho.ffmpegrender.command.CommandType;
import dev.juho.ffmpegrender.server.Server;
import dev.juho.ffmpegrender.server.client.Client;
import dev.juho.ffmpegrender.utils.Logger;
import dev.juho.ffmpegrender.utils.Utils;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ServerCommand extends Command {

	private Server server;

	public ServerCommand(Server server) {
		super(CommandType.SERVER, "Displays server info", "server", "server", "server_info", "info", "i");

		this.server = server;
	}

	@Override
	public void execute(String... args) {
		Logger logger = Logger.getInstance();

		logger.log(Logger.INFO, "FFMPEGRender");
		logger.log(Logger.INFO, "\tPort: " + server.getPort());
		logger.log(Logger.INFO, "\tUptime: " + Utils.formatMilliseconds(server.getUptime()));
		logger.log(Logger.INFO, "\t" + server.getClientPool().getSize() + " clients connected");
		if (server.getClientPool().getSize() != 0) {
			Iterator it = server.getClientPool().getClients().entrySet().iterator();

			while (it.hasNext()) {
				Map.Entry<UUID, Client> pair = (Map.Entry<UUID, Client>) it.next();

				Client c = pair.getValue();

				logger.log(Logger.INFO, "\tClient " + c.getUuid());
				logger.log(Logger.INFO, "\t\tAlive: " + c.isAlive());
				logger.log(Logger.INFO, "\t\tUptime: " + Utils.formatMilliseconds(c.getUptime()));
			}
		}
	}
}
