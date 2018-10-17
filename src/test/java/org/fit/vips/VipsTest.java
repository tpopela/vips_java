package org.fit.vips;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class VipsTest {

    @Test
    public void runSimplePageSegmentation() {
	Vips vips = new Vips();

	vips.enableGraphicsOutput(false);
	vips.setPredefinedDoC(8);
	vips.startSegmentation("example.com");

	File result = new File("VIPSResult.xml");
	assertEquals(result.exists(), true);

	result.delete();
    }
}
