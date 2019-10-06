package dev.juho.ffmpegrender.command.commands.server;

import dev.juho.ffmpegrender.command.Command;
import dev.juho.ffmpegrender.command.CommandType;
import dev.juho.ffmpegrender.client.RenderQueue;
import dev.juho.ffmpegrender.utils.Logger;

import java.io.File;

public class QueueCommand extends Command {

	private RenderQueue renderQueue;

	public QueueCommand(RenderQueue renderQueue) {
		super(CommandType.QUEUE, "displays the current queue", "queue", "queue");

		this.renderQueue = renderQueue;
	}

	@Override
	public void execute(String... args) {
		Object[] queue = renderQueue.getRenderQueue().toArray();

		if (queue.length == 0) {
			Logger.getInstance().log(Logger.INFO, "The render queue is currently empty! It'll be built when the render command is executed");
			return;
		}

		for (int i = 0; i < 10; i++) {
			if (queue.length <= i) {
				return;
			}

			File f = (File) queue[i];
			Logger.getInstance().log(Logger.INFO, i + ": " + f.getAbsolutePath());
		}

		if (queue.length - 10 > 0) {
			Logger.getInstance().log(Logger.INFO, "And " + (queue.length - 10) + " more files");
		}
	}
}
