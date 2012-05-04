/*
 * Tomas Popela, xpopel11, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - Vips.java
 */

package org.fit.vips;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

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
			String url = args[0];

			if (url.startsWith("http://") || url.startsWith("https://"))
				_url = new URL(url);
			else
				_url = new URL("http://" + url);

			URLConnection urlConnection = _url.openConnection();
			InputStream urlStream = urlConnection.getInputStream();

			getDomTree(urlStream);
			getViewport();

			int numberOfIterations = 2;
			int pageWidth = _viewport.getWidth();
			int pageHeight = _viewport.getHeight();
			int sizeTresholdWidth = 80;
			int sizeTresholdHeight = 80;

			boolean graphicsOutput = true;

			VipsParser vipsParser = new VipsParser(_viewport);
			VipsSeparatorGraphicsDetector detector = new VipsSeparatorGraphicsDetector(
					pageWidth, pageHeight);
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

				detector.setVipsBlock(vipsBlocks);

				if (graphicsOutput)
				{
					//visual separators detection
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

				sizeTresholdHeight -= 25;
				sizeTresholdWidth -= 25;

				System.err.println();
				System.err.println();
				System.err.println("End of iteration number " + i);
				System.err.println();
				System.err.println();

			}

			VipsOutput vipsOutput = new VipsOutput();
			vipsOutput.setEscapeOutput(true);
			vipsOutput.writeXML(constructor.getVisualStructure(), _viewport);

			urlStream.close();
		}
		catch (Exception e)
		{
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
