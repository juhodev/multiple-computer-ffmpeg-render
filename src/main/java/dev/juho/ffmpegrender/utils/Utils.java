package dev.juho.ffmpegrender.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Utils {

	private static char[] alphabet = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

	public static String formatMilliseconds(long milliseconds) {
		return String.format("%d hours, %d min, %d sec", (int) (milliseconds / 3600000L % 24L), (int) (milliseconds / 60000L % 60L), milliseconds / 1000L % 60L);
	}

	public static boolean hasEnum(Enum[] enumArray, String eStr) {
		for (Enum e : enumArray) {
			if (e.toString().equalsIgnoreCase(eStr)) {
				return true;
			}
		}

		return false;
	}

	public static String randomString() {
		char[] rand = new char[8];

		Random random = new Random();

		for (int i = 0; i < rand.length; i++) {
			rand[i] = alphabet[random.nextInt(alphabet.length)];
		}
		return new String(rand);
	}

	public static String[] buildCommand(String... commands) {
		List<String> cmd = new ArrayList<>();

		boolean insideQuotes = false;
		StringBuilder builder = new StringBuilder();
		for (String command : commands) {
			for (int i = 0; i < command.length(); i++) {
				char c = command.charAt(i);

				if (c == ' ' && !insideQuotes) {
					cmd.add(builder.toString());
					builder.setLength(0);
				} else if (c == '\"') {
					insideQuotes = !insideQuotes;
					builder.append("\"");
				} else {
					builder.append(command.charAt(i));
				}
			}

			if (builder.length() > 0) {
				cmd.add(builder.toString());
				builder.setLength(0);
			}
		}

		if (builder.length() > 0) {
			cmd.add(builder.toString());
		}

		String[] cmdArray = new String[cmd.size()];
		cmd.toArray(cmdArray);

		return cmdArray;
	}

}
