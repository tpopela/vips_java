/*
 * Tomas Popela, xpopel11, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VipsSeparatorDetector.java
 */

package org.fit.vips;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.fit.cssbox.layout.ElementBox;

public class VipsSeparatorDetector extends JPanel {

	private static final long serialVersionUID = 5825509847374498L;
	
	Graphics2D _pool = null;
	BufferedImage image = null;

	public VipsSeparatorDetector(int width, int height) {
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
		createPool();
	}

	/**
	 * Adds visual block to pool.
	 * @param visualStructure Visual block
	 */
	public void addVisualBlock(VisualStructure visualStructure)
	{
		ElementBox elementBox = visualStructure.getElementBox();
		Rectangle rect = new Rectangle(elementBox.getAbsoluteContentX(), elementBox.getAbsoluteContentY(), elementBox.getContentWidth(), elementBox.getContentHeight());
		_pool.draw(rect);
		_pool.fill(rect);
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.drawImage(image, 0, 0, null);
	}

	/**
	 * Fills pool with visual blocks from visual structure.
	 * @param visualStructure Visual structure
	 */
	public void fillPool(VisualStructure visualStructure)
	{
		if (visualStructure.isVisualBlock())
			addVisualBlock(visualStructure);
		
		for (VisualStructure childVisualStructure : visualStructure.getChilds())
			fillPool(childVisualStructure);
	}

	/**
	 * Creates pool
	 */
	private void createPool()
	{
		// set black as pool background color
        _pool = image.createGraphics();
        _pool.setColor(Color.black);
        _pool.fillRect(0, 0, image.getWidth(), image.getHeight());
        // set drawing color back to white
        _pool.setColor(Color.white);		
	}
	
	/**
	 * Saves pool to image
	 */
	public void saveToImage()
	{
		try
		{
			ImageIO.write(image, "png", new File("pool.png"));
		} catch (Exception e)
		{
			System.out.print(e.getStackTrace());
		}
	}
}
