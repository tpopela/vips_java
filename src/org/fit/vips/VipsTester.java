/*
 * Tomas Popela, xpopel11, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VipsTester.java
 */

package org.fit.vips;

public class VipsTester {

	/**
	 * Main function
	 * @param args Internet address of web page.
	 */
	public static void main(String args[])
	{
		// we've just one argument - web address of page
		if (args.length != 1)
			return;

		String url = args[0];

		try
		{
			Vips vips = new Vips();
			vips.enableGraphicsOutput(false);
			vips.enableOutputToFolder(false);
			vips.setPredefinedDoC(11);
			vips.startSegmentation(url);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
