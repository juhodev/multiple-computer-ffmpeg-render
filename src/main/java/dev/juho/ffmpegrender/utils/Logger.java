package dev.juho.ffmpegrender.utils;

import dev.juho.ffmpegrender.server.message.Message;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class Logger {

	public static final int ERROR = 0;
	public static final int INFO = 1;
	public static final int WARNING = 2;
	public static final int DEBUG = 3;

	public static int LOG_LEVEL = INFO;

	private final String PREFIX_LOG_ERROR = "[ERROR]";
	private final String PREFIX_LOG_WARNING = "[WARNING]";
	private final String PREFIX_LOG_INFO = "[INFO]";
	private final String PREFIX_LOG_DEBUG = "[DEBUG]";

	private static Logger instance;

	private HashMap<String, Date> times;

	private HashMap<String, Long> progressBars;
	private double lastPercentage;

	public Logger() {
		this.times = new HashMap<>();
		this.progressBars = new HashMap<>();
		this.lastPercentage = 0;
	}

	public static Logger getInstance() {
		if (instance == null) {
			instance = new Logger();
		}

		return instance;
	}

	public void log(int level, byte[] bytes) {
		StringBuilder builder = new StringBuilder();
		builder.append("bytes: ");

		for (byte b : bytes) {
			builder.append(b + " ");
		}

		log(level, builder.toString());
	}

	public void log(int level, int x) {
		log(level, String.valueOf(x));
	}

	public void log(int level, Message message) {
		log(level, "message length: " + message.getLength() + " - " + message.getType() + " - " + message.getSender() + " - " + message.getData().toString());
	}

	public void log(int level, String message) {
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);

		String timePrefix = "[" + cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR) + " "
				+ (hour < 10 ? ("0" + hour) : hour) + ":"
				+ (minute < 10 ? "0" + minute : minute) + ":"
				+ (second < 10 ? "0" + second : second) + "]";

		String logLevelPrefix;

		switch (level) {
			case ERROR:
				logLevelPrefix = PREFIX_LOG_ERROR;
				break;

			case WARNING:
				logLevelPrefix = PREFIX_LOG_WARNING;
				break;

			case INFO:
				logLevelPrefix = PREFIX_LOG_INFO;
				break;

			case DEBUG:
				logLevelPrefix = PREFIX_LOG_DEBUG;
				break;

			default:
				logLevelPrefix = "[??]";
				break;
		}


		if (LOG_LEVEL >= level) {
			System.out.println(timePrefix + " " + logLevelPrefix + ": " + message);
		}
	}

	public void createProgressbar(String name, long max) {
		progressBars.put(name, max);

		updateProgressbar(name, 0);
	}

	public void updateProgressbar(String name, long value) {
		if (!progressBars.containsKey(name)) {
//			Now that I think about it this probably shouldn't log anything. Or it should log a DEBUG log not sure though
//			log(Logger.WARNING, name + " progress bar doesn't exists!");
			return;
		}

		long max = progressBars.get(name);

		StringBuilder builder = new StringBuilder();

		double percentage = (double) value / (double) max;
		double rounded = Math.round((percentage * 100));

		if (lastPercentage == rounded) {
			return;
		}

		builder.append(name).append(" |");

		double printSize = 35;
		double printPercentage = printSize * percentage;

		for (int i = 0; i < printSize; i++) {
			if (i < printPercentage) {
				builder.append("=");
			} else {
				builder.append(" ");
			}
		}

		builder.append("| ").append(rounded).append("%");
		lastPercentage = rounded;

		System.out.print("\r" + builder.toString());
		if (rounded == 100) {
			System.out.print("\n");
			removeProgressbar(name);
		}
	}

	public void removeProgressbar(String name) {
		progressBars.remove(name);
	}

	/**
	 * The same as console.time & console.timeEnd in JavaScript
	 */

	public void time(String name) {
		times.put(name, new Date());
	}

	public void timeEnd(int level, String name) {
		if (!times.containsKey(name)) {
			log(Logger.WARNING, name + " starting time not found");
			return;
		}

		Date startingDate = times.get(name);
		long elapsedTime = new Date().getTime() - startingDate.getTime();

		log(level, name + " " + elapsedTime + "ms");
		times.remove(name);
	}

}
