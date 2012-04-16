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
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.fit.cssbox.layout.ElementBox;

public class VipsSeparatorDetector extends JPanel {

	private static final long serialVersionUID = 5825509847374498L;

	Graphics2D _pool = null;
	BufferedImage _image = null;
	List<Separator> _horizontalSeparators = null;
	List<Separator> _verticalSeparators = null;

	public VipsSeparatorDetector(int width, int height) {
		_image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
		_horizontalSeparators = new ArrayList<VipsSeparatorDetector.Separator>();
		_verticalSeparators = new ArrayList<VipsSeparatorDetector.Separator>();
		createPool();
	}

	/**
	 * Adds visual block to pool.
	 * 
	 * @param visualStructure
	 *            Visual block
	 */
	public void addVisualBlock(VisualStructure visualStructure)
	{
		ElementBox elementBox = visualStructure.getElementBox();
		Rectangle rect = new Rectangle(elementBox.getAbsoluteContentX(),
				elementBox.getAbsoluteContentY(), elementBox.getContentWidth(),
				elementBox.getContentHeight());
		_pool.draw(rect);
		_pool.fill(rect);
		//saveToImage();
	}

	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.drawImage(_image, 0, 0, null);
	}

	/**
	 * Fills pool with all visual blocks from visual structure.
	 * 
	 * @param visualStructure
	 *            Visual structure
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
		_pool = _image.createGraphics();
		_pool.setColor(Color.white);
		_pool.fillRect(0, 0, _image.getWidth(), _image.getHeight());
		// set drawing color back to white
		_pool.setColor(Color.black);
	}

	/**
	 * Computes vertical visual separators from given visual structure.
	 * @param visualStructure Visual structure
	 */
	private void findVerticalSeparators(VisualStructure visualStructure)
	{
		// if block is representing visual block
		if (visualStructure.isVisualBlock())
		{
			// add new visual block to pool
			addVisualBlock(visualStructure);

			// block vertical coordinates
			int blockStart = visualStructure.getBox().getAbsoluteContentX();
			int blockEnd = blockStart + visualStructure.getBox().getContentWidth();

			// for each separator that we have in pool
			for (Separator separator : _verticalSeparators)
			{
				// find separator, that intersects with our visual block
				if (blockStart <= separator.endPoint)
				{
					// next there are six relations where the separator and visual block can be

					// if separator is inside visual block
					if (blockStart < separator.startPoint && blockEnd > separator.endPoint)
					{
						List<Separator> tempSeparators = new ArrayList<VipsSeparatorDetector.Separator>();
						tempSeparators.addAll(_verticalSeparators);

						//remove all separators, that are included in block
						for (Separator other : tempSeparators)
						{
							if (blockStart < other.startPoint && blockEnd > other.endPoint)
								_verticalSeparators.remove(other);
						}

						//find separator, that is on end of this block (if exists)
						for (Separator other : _verticalSeparators)
						{
							// and if it's necessary change it's start point
							if (blockEnd > other.startPoint && blockEnd < other.endPoint)
							{
								other.startPoint = blockEnd + 1;
								break;
							}
						}
						break;
					}
					// if block is inside another block -> skip it
					if (blockEnd < separator.startPoint)
						break;
					// if separator starts in the middle of block
					if (blockStart < separator.startPoint && blockEnd > separator.startPoint)
					{
						// change separator start's point coordinate
						separator.startPoint = blockEnd+1;
						break;
					}
					// if block is in the middle of separator
					if (blockStart > separator.startPoint && blockEnd < separator.endPoint)
					{
						// add new separator that starts behind the block
						_verticalSeparators.add(_verticalSeparators.indexOf(separator) + 1, new Separator(blockEnd + 1, separator.endPoint));
						// change end point coordinates of separator, that's before block
						separator.endPoint = blockStart - 1;
						break;
					}
					// if in one block is one separator ending and another one starting
					if (blockStart > separator.startPoint && blockStart < separator.endPoint)
					{
						// find the next one
						int nextSeparatorIndex =_verticalSeparators.indexOf(separator);

						// if it's not the last separator
						if (nextSeparatorIndex + 1 < _verticalSeparators.size())
						{
							Separator nextSeparator = _verticalSeparators.get(_verticalSeparators.indexOf(separator) + 1);

							// next separator is really starting before the block ends
							if (blockEnd > nextSeparator.startPoint && blockEnd < nextSeparator.endPoint)
							{
								// change separator start point coordinate
								separator.endPoint = blockStart - 1;
								nextSeparator.startPoint = blockEnd + 1;
								break;
							}
						}
					}
					// if separator ends in the middle of block
					// change it's end point coordinate
					separator.endPoint = blockStart-1;
					break;
				}
			}
		}

		// detect visual separators for each child's structure
		for (VisualStructure childVisualStructure : visualStructure.getChilds())
		{
			findVerticalSeparators(childVisualStructure);
		}
	}

	/**
	 * Computes horizontal visual separators from given visual structure.
	 * @param visualStructure Visual structure
	 */
	private void findHorizontalSeparators(VisualStructure visualStructure)
	{
		// if block is representing visual block
		if (visualStructure.isVisualBlock())
		{
			// add new visual block to pool
			addVisualBlock(visualStructure);

			// block vertical coordinates
			int blockStart = visualStructure.getBox().getAbsoluteContentY();
			int blockEnd = blockStart + visualStructure.getBox().getContentHeight();

			// for each separator that we have in pool
			for (Separator separator : _horizontalSeparators)
			{
				// find separator, that intersects with our visual block
				if (blockStart <= separator.endPoint)
				{
					// next there are six relations where the separator and visual block can be

					// if separator is inside visual block
					if (blockStart < separator.startPoint && blockEnd > separator.endPoint)
					{
						List<Separator> tempSeparators = new ArrayList<VipsSeparatorDetector.Separator>();
						tempSeparators.addAll(_horizontalSeparators);

						//remove all separators, that are included in block
						for (Separator other : tempSeparators)
						{
							if (blockStart < other.startPoint && blockEnd > other.endPoint)
								_horizontalSeparators.remove(other);
						}

						//find separator, that is on end of this block (if exists)
						for (Separator other : _horizontalSeparators)
						{
							// and if it's necessary change it's start point
							if (blockEnd > other.startPoint && blockEnd < other.endPoint)
							{
								other.startPoint = blockEnd + 1;
								break;
							}
						}
						break;
					}
					// if block is inside another block -> skip it
					if (blockEnd < separator.startPoint)
						break;
					// if separator starts in the middle of block
					if (blockStart < separator.startPoint && blockEnd > separator.startPoint)
					{
						// change separator start's point coordinate
						separator.startPoint = blockEnd+1;
						break;
					}
					// if block is in the middle of separator
					if (blockStart > separator.startPoint && blockEnd < separator.endPoint)
					{
						// add new separator that starts behind the block
						_horizontalSeparators.add(_horizontalSeparators.indexOf(separator) + 1, new Separator(blockEnd + 1, separator.endPoint));
						// change end point coordinates of separator, that's before block
						separator.endPoint = blockStart - 1;
						break;
					}
					// if in one block is one separator ending and another one starting
					if (blockStart > separator.startPoint && blockStart < separator.endPoint)
					{
						// find the next one
						int nextSeparatorIndex =_horizontalSeparators.indexOf(separator);

						// if it's not the last separator
						if (nextSeparatorIndex + 1 < _horizontalSeparators.size())
						{
							Separator nextSeparator = _horizontalSeparators.get(_horizontalSeparators.indexOf(separator) + 1);

							// next separator is really starting before the block ends
							if (blockEnd > nextSeparator.startPoint && blockEnd < nextSeparator.endPoint)
							{
								// change separator start point coordinate
								separator.endPoint = blockStart - 1;
								nextSeparator.startPoint = blockEnd + 1;
								break;
							}
						}
					}
					// if separator ends in the middle of block
					// change it's end point coordinate
					separator.endPoint = blockStart-1;
					break;
				}
			}
		}

		// detect visual separators for each child's structure
		for (VisualStructure childVisualStructure : visualStructure.getChilds())
		{
			findHorizontalSeparators(childVisualStructure);
		}
	}

	/**
	 * Detects horizontal visual separators from given visual structure.
	 * @param visualStructure Visual structure
	 */
	public void detectHorizontalSeparators(VisualStructure visualStructure)
	{
		createPool();
		_horizontalSeparators.clear();
		_horizontalSeparators.add(new Separator(0, _image.getHeight()));

		findHorizontalSeparators(visualStructure);

		//remove pool borders
		_horizontalSeparators.remove(0);
		_horizontalSeparators.remove(_horizontalSeparators.size()-1);
	}

	/**
	 * Detects vertical visual separators from given visual structure.
	 * @param visualStructure Visual structure
	 */
	public void detectVerticalSeparators(VisualStructure visualStructure)
	{
		createPool();
		_verticalSeparators.clear();
		_verticalSeparators.add(new Separator(0, _image.getWidth()));

		findVerticalSeparators(visualStructure);

		//remove pool borders
		_verticalSeparators.remove(0);
		_verticalSeparators.remove(_verticalSeparators.size()-1);
	}

	/**
	 * Saves everything (separators + block) to image.
	 * @param visualStructure Visual structure
	 */
	public void exportAllToImage(VisualStructure visualStructure)
	{
		createPool();
		fillPool(visualStructure);

		_pool.setColor(Color.red);
		for (Separator separator : _verticalSeparators)
		{
			Rectangle rect = new Rectangle(separator.startPoint,
					0, separator.endPoint - separator.startPoint,
					_image.getHeight());
			_pool.draw(rect);
			_pool.fill(rect);
		}

		_pool.setColor(Color.blue);
		for (Separator separator : _horizontalSeparators)
		{
			Rectangle rect = new Rectangle(0,
					separator.startPoint, _image.getWidth(),
					separator.endPoint - separator.startPoint);
			_pool.draw(rect);
			_pool.fill(rect);
		}
		saveToImage("all");
	}

	/**
	 * Saves vertical separators to image.
	 * @param visualStructure Visual structure
	 */
	public void exportVerticalSeparatorsToImage()
	{
		createPool();
		_pool.setColor(Color.red);
		for (Separator separator : _verticalSeparators)
		{
			Rectangle rect = new Rectangle(separator.startPoint,
					0, separator.endPoint - separator.startPoint,
					_image.getHeight());
			_pool.draw(rect);
			_pool.fill(rect);
		}

		saveToImage("verticalSeparators");
	}

	/**
	 * Saves horizontal separators to image.
	 * @param visualStructure Visual structure
	 */
	public void exportHorizontalSeparatorsToImage()
	{
		createPool();
		_pool.setColor(Color.blue);
		for (Separator separator : _horizontalSeparators)
		{
			Rectangle rect = new Rectangle(0,
					separator.startPoint, _image.getWidth(),
					separator.endPoint - separator.startPoint);
			_pool.draw(rect);
			_pool.fill(rect);
		}

		saveToImage("horizontalSeparators");
	}

	/**
	 * Saves pool to image
	 */
	public void saveToImage(String filename)
	{
		try
		{
			ImageIO.write(_image, "png", new File(filename));
		} catch (Exception e)
		{
			System.out.print(e.getStackTrace());
		}
	}

	/**
	 * Class that represents visual separator
	 */
	public class Separator {
		public int startPoint;
		public int endPoint;

		public Separator(int start, int end) {
			this.startPoint = start;
			this.endPoint = end;
		}
	}
}
