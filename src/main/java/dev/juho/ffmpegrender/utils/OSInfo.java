package dev.juho.ffmpegrender.utils;

public class OSInfo {

	private static OSType currentOS = null;

	public static OSType getOSType() {
		if (currentOS == null) {
			String osName = System.getProperty("os.name").toLowerCase();

			if (osName.contains("win")) {
				currentOS = OSType.WINDOWS;
			} else if (osName.contains("mac")) {
				currentOS = OSType.MAC;
			} else if (osName.contains("linux")) {
				currentOS = OSType.LINUX;
			} else {
				currentOS = OSType.OTHER;
			}
		}

		return currentOS;
	}

	public enum OSType {
		WINDOWS, LINUX, MAC, OTHER,
	}

}
