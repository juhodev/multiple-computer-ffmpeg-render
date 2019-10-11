package dev.juho.ffmpegrender.command.commands.server;

import dev.juho.ffmpegrender.client.RenderQueue;
import dev.juho.ffmpegrender.command.Command;
import dev.juho.ffmpegrender.command.CommandType;

public class AddFolderCommand extends Command {

	private RenderQueue renderQueue;

	public AddFolderCommand(RenderQueue renderQueue) {
		super(CommandType.ADD_FOLDER, "Add folder to render queue", "addfolder C:/path/to/folder", "addfolder", "add_folder");

		this.renderQueue = renderQueue;
	}

	@Override
	public void execute(String... args) {
		String path = args[0];
		renderQueue.addFolderToQueue(path);
	}
}
