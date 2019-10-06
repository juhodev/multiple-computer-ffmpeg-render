package dev.juho.ffmpegrender.command.commands.server;

import dev.juho.ffmpegrender.command.Command;
import dev.juho.ffmpegrender.command.CommandType;
import dev.juho.ffmpegrender.client.RenderQueue;
import dev.juho.ffmpegrender.server.ClientPool;
import dev.juho.ffmpegrender.server.client.Client;
import dev.juho.ffmpegrender.utils.Logger;

public class RenderCommand extends Command {

	//	Not really something I'd like to do (this should send an event or something)
	private ClientPool clientPool;
	private RenderQueue renderQueue;

	public RenderCommand(ClientPool clientPool, RenderQueue renderQueue) {
		super(CommandType.RENDER, "render files", "render", "render");

		this.clientPool = clientPool;
		this.renderQueue = renderQueue;
	}

	@Override
	public void execute(String... args) {
		if (clientPool.getClients().size() == 0) {
			Logger.getInstance().log(Logger.INFO, "0 clients connected");
			return;
		}

		for (Client client : clientPool.getClients().values()) {
			renderQueue.updateQueue(client);
		}
	}
}
