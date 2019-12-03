package dev.juho.ffmpegrender.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ArgsParser {

	private HashMap<String, Argument> aliases;
	private HashMap<Argument, Type> arguments;

	private static ArgsParser instance;
	private HashMap<Argument, Object> parsedArgs;

	private ArgsParser() {
		this.parsedArgs = new HashMap<>();
		this.aliases = new HashMap<>();
		this.arguments = new HashMap<>();
	}

	public static ArgsParser getInstance() {
		if (instance == null) {
			instance = new ArgsParser();
		}

		return instance;
	}

	public boolean has(Argument key) {
		return parsedArgs.containsKey(key);
	}

	public String getString(Argument key) {
		return (String) parsedArgs.get(key);
	}

	public int getInt(Argument key) {
		return Integer.parseInt((String) parsedArgs.get(key));
	}

	public List<String> getList(Argument key) {
		return (List<String>) parsedArgs.get(key);
	}

	public void add(Argument name, Type type, String... aliases) {
		arguments.put(name, type);

		for (String alias : aliases) {
			Logger.getInstance().log(Logger.DEBUG, "Setting argument alias " + alias + " -> " + name);
			this.aliases.put(alias, name);
		}
	}

	public void parse(String[] args) {
		createHashMapFromArgs(args);
	}

	private void createHashMapFromArgs(String[] args) {
		String key = "";
		StringBuilder builder = new StringBuilder();

		for (String s : args) {
			if (s.startsWith("-")) {
				if (key.isEmpty()) {
					key = s;
				} else {
					if (!aliases.containsKey(key)) {
						Logger.getInstance().log(Logger.ERROR, key + " not found");
						continue;
					}

					Argument arg = aliases.get(key);

					switch (arguments.get(arg)) {
						case LIST:
							addList(arg, builder.toString().trim());
							break;

						case STRING:
							parsedArgs.put(arg, builder.toString().trim());
							break;

						case NONE:
							parsedArgs.put(arg, "");
							break;
					}

					key = s;
					builder.setLength(0);
				}
			} else {
				builder.append(s).append(" ");
			}
		}

		if (!key.isEmpty()) {
			if (!aliases.containsKey(key)) {
				Logger.getInstance().log(Logger.ERROR, key + " not found");
				return;
			}

			Argument arg = aliases.get(key);

			switch (arguments.get(arg)) {
				case LIST:
					addList(arg, builder.toString().trim());
					break;

				case STRING:
					parsedArgs.put(arg, builder.toString().trim());
					break;

				case NONE:
					parsedArgs.put(arg, "");
					break;
			}
		}
	}

	private void addList(Argument argument, String s) {
		if (parsedArgs.containsKey(argument)) {
			List<String> strs = (List<String>) parsedArgs.get(argument);
			strs.add(s);
			parsedArgs.put(argument, strs);
		} else {
			List<String> list = new ArrayList<>();
			list.add(s);
			parsedArgs.put(argument, list);
		}
	}

	public enum Type {
		STRING, LIST, NONE,
	}

	public enum Argument {
		CLIENT,
		SERVER,
		ADDRESS,
		PORT,
		VIDEOS_IN_ONE,
		SAVE_FOLDER,
		RENDER_FOLDER,
		IGNORE,
		RECURSIVE,
		DEBUG,
		LOCAL,
		HELP,
	}

}
