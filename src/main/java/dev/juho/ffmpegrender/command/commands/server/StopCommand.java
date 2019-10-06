package dev.juho.ffmpegrender.command.commands.server;

import dev.juho.ffmpegrender.command.Command;
import dev.juho.ffmpegrender.command.CommandType;
import dev.juho.ffmpegrender.server.Server;
import dev.juho.ffmpegrender.server.message.Message;
import dev.juho.ffmpegrender.server.message.MessageType;
import dev.juho.ffmpegrender.utils.Logger;
import org.json.JSONObject;

import java.io.IOException;

public class StopCommand extends Command {

	private Server server;

	public StopCommand(Server server) {
		super(CommandType.STOP, "Stop SpigotScaler", "stop", "stop", "quit");

		this.server = server;
	}

	@Override
	public void execute(String... args) {
		Logger.getInstance().log(Logger.INFO, "Stopping FFMPEGRender");
		server.getClientPool().sendAll(Message.build(MessageType.GOODBYE, Server.serverUUID, new JSONObject().put("reason", "FFMPEGRender stopping")));
		System.exit(0);
	}
}
