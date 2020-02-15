package dev.juho.ffmpeg.utils;

import dev.juho.ffmpegrender.utils.Utils;
import org.junit.Assert;
import org.junit.Test;

public class UtilsTests {

	@Test
	public void buildCommandTest() {
		String[] cmd = Utils.buildCommand("ffmpeg -i", "\"C:/test path/to/file\"");

		Assert.assertEquals("ffmpeg", cmd[0]);
		Assert.assertEquals("-i", cmd[1]);
		Assert.assertEquals("\"C:/test path/to/file\"", cmd[2]);
	}

}
