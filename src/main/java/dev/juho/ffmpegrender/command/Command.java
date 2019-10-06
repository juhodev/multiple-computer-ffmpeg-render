package dev.juho.ffmpegrender.command;

public abstract class Command {

	private String[] aliases;
	private String help, example;
	private CommandType type;

	public Command(CommandType type, String help, String example, String... aliases){
		this.type = type;
		this.help = help;
		this.example = example;
		this.aliases = aliases;
	}

	public abstract void execute(String... args);

	public CommandType getType() {
		return type;
	}

	public String getExample() {
		return example;
	}

	public String getHelp() {
		return help;
	}

	public String[] getAliases() {
		return aliases;
	}
}
