/*
 * Tomas Popela, xpopel11, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - Vips.java
 */

package org.fit.vips;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.imageio.ImageIO;

import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.demo.DOMSource;
import org.fit.cssbox.layout.BrowserCanvas;
import org.fit.cssbox.layout.Viewport;
import org.w3c.dom.Document;

public class Vips {
	private static URL _url = null;
	private static DOMAnalyzer _domAnalyzer = null;
	private static javax.swing.JPanel _browserCanvas = null;
	private static Viewport _viewport = null;

	/*
	 * With help of CssBox gets the DOM tree of page
	 */
	private static void getDomTree(InputStream urlStream)
	{
		DOMSource parser = new DOMSource(urlStream);
		try
		{
			Document domTree = parser.parse();
			_domAnalyzer = new DOMAnalyzer(domTree, _url);
			_domAnalyzer.attributesToStyles();
			_domAnalyzer.addStyleSheet(null, CSSNorm.stdStyleSheet());
			_domAnalyzer.addStyleSheet(null, CSSNorm.userStyleSheet());
			_domAnalyzer.getStyleSheets();
		} catch (Exception e)
		{
			System.err.print(e.getMessage());
		}
	}

	private static void getViewport()
	{
		_browserCanvas = new BrowserCanvas(_domAnalyzer.getRoot(),
				_domAnalyzer, new java.awt.Dimension(1000, 600), _url);
		_viewport = ((BrowserCanvas) _browserCanvas).getViewport();
	}

	private static void exportPageToImage()
	{
		try
		{
			BufferedImage page = ((BrowserCanvas) _browserCanvas).getImage();
			String filename = System.getProperty("user.dir") + "/page.png";
			ImageIO.write(page, "png", new File(filename));
		} catch (Exception e)
		{
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static String generateFolderName()
	{
		String outputFolder = "";

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
		outputFolder += sdf.format(cal.getTime());
		outputFolder += "_";
		outputFolder += _url.getHost().replaceAll("\\.", "_");

		return outputFolder;
	}

	/*
	 * Main entrance to VIPS
	 */
	public static void main(String args[])
	{
		// we've just one argument - web address of page
		if (args.length != 1)
			System.exit(0);

		try
		{
			//boolean graphicsOutput = true;
			boolean graphicsOutput = false;
			boolean outputToFolder = true;
			boolean includeBlocks = false;
			boolean escapeOutput = true;
			int pDoC = 5;

			if (pDoC <= 0 || pDoC> 11)
			{
				System.err.println("pDoC value must be between 1 and 11! Not " + pDoC + "!");
				return;
			}

			String url = args[0];

			if (url.startsWith("http://") || url.startsWith("https://"))
				_url = new URL(url);
			else
				_url = new URL("http://" + url);

			URLConnection urlConnection = _url.openConnection();
			InputStream urlStream = urlConnection.getInputStream();

			getDomTree(urlStream);
			getViewport();

			long startTime = System.nanoTime();

			int numberOfIterations = 3;
			int pageWidth = _viewport.getWidth();
			int pageHeight = _viewport.getHeight();
			int sizeTresholdWidth = 80;
			int sizeTresholdHeight = 80;

			String outputFolder = "";
			String oldWorkingDirectory = "";
			String newWorkingDirectory = "";

			if (outputToFolder)
			{
				outputFolder = generateFolderName();

				if (!new File(outputFolder).mkdir())
				{
					System.err.println("Something goes wrong during directory creation!");
				}
				else
				{
					oldWorkingDirectory = System.getProperty("user.dir");
					newWorkingDirectory += oldWorkingDirectory + "/" + outputFolder + "/";
					System.setProperty("user.dir", newWorkingDirectory);
				}
			}

			if (graphicsOutput)
				exportPageToImage();

			VipsSeparatorGraphicsDetector detector = new VipsSeparatorGraphicsDetector(pageWidth, pageHeight);
			VipsParser vipsParser = new VipsParser(_viewport);
			VisualStructureConstructor constructor = new VisualStructureConstructor();
			constructor.setGraphicsOutput(graphicsOutput);

			for (int i = 1; i < numberOfIterations+1; i++)
			{
				System.err.println();
				System.err.println();
				System.err.println("Beginning of iteration number " + i);
				System.err.println();
				System.err.println();

				//visual blocks detection
				vipsParser.setSizeTresholdHeight(sizeTresholdHeight);
				vipsParser.setSizeTresholdWidth(sizeTresholdWidth);
				vipsParser.parse();
				VipsBlock vipsBlocks = vipsParser.getVipsBlocks();

				if (graphicsOutput)
				{
					//visual separators detection
					detector.setVipsBlock(vipsBlocks);
					detector.fillPool();
					detector.saveToImage("pool" + i);
				}

				if (i == 1)
				{
					if (graphicsOutput)
					{
						// in first round we'll export global separators
						detector.setCleanUpSeparators(true);
						detector.detectHorizontalSeparators();
						detector.detectVerticalSeparators();
						detector.exportHorizontalSeparatorsToImage();
						detector.exportVerticalSeparatorsToImage();
						detector.exportAllToImage();
					}

					// visual structure construction
					constructor.setVipsBlocks(vipsBlocks);
					constructor.setPageSize(pageWidth, pageHeight);
				}
				else
				{
					vipsBlocks = vipsParser.getVipsBlocks();
					constructor.updateVipsBlocks(vipsBlocks);
				}

				// visual structure construction
				constructor.constructVisualStructure();

				// 65 seznam.cz
				sizeTresholdHeight -= 65;
				if (sizeTresholdHeight <= 0)
					sizeTresholdHeight = 1;
				sizeTresholdWidth -= 65;
				if (sizeTresholdWidth <= 0)
					sizeTresholdWidth = 1;

				System.err.println();
				System.err.println();
				System.err.println("End of iteration number " + i);
				System.err.println();
				System.err.println();
			}

			//constructor.zScoreNormalization();
			System.err.println();
			System.err.println();
			constructor.normalizeSeparators();

			VipsOutput vipsOutput = new VipsOutput();
			vipsOutput.setEscapeOutput(escapeOutput);
			vipsOutput.setIncludeBlocks(includeBlocks);
			vipsOutput.setPDoC(pDoC);
			vipsOutput.writeXML(constructor.getVisualStructure(), _viewport);

			if (outputToFolder)
				System.setProperty("user.dir", oldWorkingDirectory);

			urlStream.close();

			long endTime = System.nanoTime();
			long diff = endTime - startTime;
			System.err.println("Execution time of VIPS: " + diff + " ns; " +
					(diff / 1000000.0) + " ms; " +
					(diff / 1000000000.0) + " sec");
		}
		catch (Exception e)
		{
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
