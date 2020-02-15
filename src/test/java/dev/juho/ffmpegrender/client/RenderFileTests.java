package dev.juho.ffmpegrender.client;

import org.junit.Assert;
import org.junit.Test;

public class RenderFileTests {

	@Test
	public void testUpdateProgress() {
		RenderFile renderFile = new RenderFile();

		renderFile.setDuration(1000);
		renderFile.updateProgress("frame=100828 fps= 46 q=31.0 size= 1540352kB time=00:2:00.57 bitrate=7508.5kbits/s dup=2 drop=0 speed=0.769x");
		Assert.assertEquals(120, renderFile.getTimeRendered());
	}

}
