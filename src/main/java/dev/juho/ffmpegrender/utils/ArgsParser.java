package dev.juho.ffmpegrender.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ArgsParser {

	private HashMap<String, Type> arguments;

	private static ArgsParser instance;
	private HashMap<String, Object> parsedArgs;

	private ArgsParser() {
		this.parsedArgs = new HashMap<>();
	}

	public void setArguments(HashMap<String, Type> arguments) {
		this.arguments = arguments;
	}

	public static ArgsParser getInstance() {
		if (instance == null) {
			instance = new ArgsParser();
		}

		return instance;
	}

	public boolean has(String key) {
		return parsedArgs.containsKey(key);
	}

	public String getString(String key) {
		return (String) parsedArgs.get(key);
	}

	public int getInt(String key) {
		return Integer.parseInt((String) parsedArgs.get(key));
	}

	public List<String> getList(String key) {
		return (List<String>) parsedArgs.get(key);
	}

	public void parse(String[] args) {
		createHashMapFromArgs(args);

		if (parsedArgs.containsKey("-help")) {
			StringBuilder builder = new StringBuilder();
			for (String s : arguments.keySet()) {
				builder.append(s).append(" ");
			}

			Logger.getInstance().log(Logger.INFO, "Args: " + builder.toString());
			System.exit(0);
		}
	}

	private void createHashMapFromArgs(String[] args) {
		String key = "";
		StringBuilder builder = new StringBuilder();

		for (String s : args) {
			if (s.startsWith("-")) {
				if (key.isEmpty()) {
					key = s;
				} else {
					if (!arguments.containsKey(key)) {
						Logger.getInstance().log(Logger.ERROR, key + " not found");
						continue;
					}

					switch (arguments.get(key)) {
						case LIST:
							addList(key, builder.toString().trim());
							break;

						case STRING:
							parsedArgs.put(key, builder.toString().trim());
							break;

						case NONE:
							parsedArgs.put(key, "");
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
			switch (arguments.get(key)) {
				case LIST:
					addList(key, builder.toString().trim());
					break;

				case STRING:
					parsedArgs.put(key, builder.toString().trim());
					break;

				case NONE:
					parsedArgs.put(key, "");
					break;
			}
		}
	}

	private void addList(String argument, String s) {
		Logger.getInstance().log(Logger.INFO, "adding " + argument + " - " + s);
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

}
