package dev.juho.ffmpegrender.command;

import dev.juho.ffmpegrender.utils.Logger;

import java.util.HashMap;
import java.util.Scanner;

public class CmdExecutor extends Thread {

	private HashMap<String, CommandType> aliases;
	private HashMap<CommandType, Command> commands;

	public CmdExecutor() {
		this.aliases = new HashMap<>();
		this.commands = new HashMap<>();
	}

	@Override
	public void start() {
		Logger.getInstance().log(Logger.INFO, "Starting command executor!");
		listen();
	}

	public void register(Command command) {
		Logger.getInstance().log(Logger.DEBUG, "Registering command " + command.getType());
		for (String alias : command.getAliases()) {
			Logger.getInstance().log(Logger.DEBUG, "Setting alias " + alias + " -> " + command.getType());
			aliases.put(alias.toUpperCase(), command.getType());
		}

		commands.put(command.getType(), command);
	}

	private void execute(String command, String... args) {
		CommandType commandType = aliases.get(command.toUpperCase());

		if (commandType == null) {
			Logger.getInstance().log(Logger.ERROR, "Command " + command + " not found!");
			return;
		}

		Command cmd = commands.get(commandType);
		cmd.execute(args);
	}

	private void listen() {
		new Thread(() -> {
			Scanner scanner = new Scanner(System.in);

			while (true) {
				String line = scanner.nextLine();

				String command;
				String[] args;

				if (line.contains(" ")) {
					command = line.substring(0, line.indexOf(" "));
					args = line.substring(line.indexOf(" ") + 1).split(" ");
				} else {
					command = line;
					args = new String[0];
				}

				execute(command, args);
			}
		}).start();
	}

}
