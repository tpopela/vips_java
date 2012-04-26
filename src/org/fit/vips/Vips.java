/*
 * Tomas Popela, xpopel11, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - Vips.java
 */

package org.fit.vips;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

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
		} catch (Exception e) {
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

			VipsParser vipsParser = new VipsParser(_viewport);
			VipsOutput vipsOutput = new VipsOutput();
			VipsSeparatorGraphicsDetector vipsSeparatorDetector = new VipsSeparatorGraphicsDetector(_viewport.getContentWidth(), _viewport.getContentHeight());
			VisualStructureConstructor visualStructureConstructor = new VisualStructureConstructor();

			vipsParser.parse();
			VipsBlock vipsBlocks = vipsParser.getVipsBlocks();

			vipsSeparatorDetector.setVipsBlock(vipsBlocks);
			vipsSeparatorDetector.fillPool();
			vipsSeparatorDetector.saveToImage("pool");
			vipsSeparatorDetector.detectHorizontalSeparators();
			vipsSeparatorDetector.detectVerticalSeparators();
			vipsSeparatorDetector.exportHorizontalSeparatorsToImage();
			vipsSeparatorDetector.exportVerticalSeparatorsToImage();
			vipsSeparatorDetector.exportAllToImage();

			List<Separator> horizontalSeparators = vipsSeparatorDetector.getHorizontalSeparators();
			List<Separator> verticalSeparators = vipsSeparatorDetector.getVerticalSeparators();

			visualStructureConstructor.setVipsBlock(vipsBlocks);
			visualStructureConstructor.setSeparators(horizontalSeparators, verticalSeparators);
			visualStructureConstructor.setPageSize(_viewport.getWidth(), _viewport.getHeight());
			visualStructureConstructor.constructVisualStructure();

			vipsOutput.writeXML(vipsParser.getVipsBlocks(), _viewport);

			urlStream.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}

	}
}
