/*
 * Tomas Popela, xpopel11, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VipsSeparatorGraphicsDetector.java
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

import org.fit.cssbox.layout.Box;
import org.fit.cssbox.layout.TextBox;

public class VipsSeparatorGraphicsDetector extends JPanel {

	private static final long serialVersionUID = 5825509847374498L;

	Graphics2D _pool = null;
	BufferedImage _image = null;
	VisualStructure _visualStructure = null;
	List<Separator> _horizontalSeparators = null;
	List<Separator> _verticalSeparators = null;

	/**
	 * Defaults constructor.
	 * @param width Pools width
	 * @param height Pools height
	 */
	public VipsSeparatorGraphicsDetector(int width, int height) {
		_image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
		_horizontalSeparators = new ArrayList<VipsSeparatorGraphicsDetector.Separator>();
		_verticalSeparators = new ArrayList<VipsSeparatorGraphicsDetector.Separator>();
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
		Box elementBox = visualStructure.getBox();

		Rectangle rect = new Rectangle(elementBox.getAbsoluteContentX(),
				elementBox.getAbsoluteContentY(), elementBox.getContentWidth(),
				elementBox.getContentHeight());

		_pool.draw(rect);
		_pool.fill(rect);
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
	 * Sets visual structure, that will be user for separators compute.
	 * @param visualStructure Visual structure
	 */
	public void setVisualStructure(VisualStructure visualStructure)
	{
		this._visualStructure = visualStructure;
	}

	/**
	 * Gets visual structure, that is used for separators compute.
	 * @return Visual structure
	 */
	public VisualStructure getVisualStructure()
	{
		return _visualStructure;
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
						List<Separator> tempSeparators = new ArrayList<VipsSeparatorGraphicsDetector.Separator>();
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
						List<Separator> tempSeparators = new ArrayList<VipsSeparatorGraphicsDetector.Separator>();
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
					if (blockStart <= separator.startPoint && blockEnd > separator.startPoint)
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

		computeHorizontalWeights();
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

		computeVerticalWeights();
	}

	/**
	 * Computes weights for vertical separators.
	 */
	private void computeVerticalWeights()
	{
		for (Separator separator : _verticalSeparators)
		{
			ruleOne(separator);
			ruleTwo(separator, false);
			ruleThree(separator, false);
		}
	}

	/**
	 * Computes weights for horizontal separators.
	 */
	private void computeHorizontalWeights()
	{
		for (Separator separator : _horizontalSeparators)
		{
			ruleOne(separator);
			ruleTwo(separator, true);
			ruleThree(separator,true);
			ruleFour(separator);
			ruleFive(separator);
		}
	}

	/**
	 * The greater the distance between blocks on different
	 * side of the separator, the higher the weight. <p>
	 * For every 5 points of width we increase weight by 2 points.
	 * @param separator Separator
	 */
	private void ruleOne(Separator separator)
	{
		int width = separator.endPoint - separator.startPoint;
		int weight = 0;
		if (width < 5)
			weight = 1;
		else
			weight = (width / 5);

		separator.weight += weight * 2;
	}

	/**
	 * If a visual separator is overlapped with some certain HTML
	 * tags (e.g., the &lt;HR&gt; HTML tag), its weight is set to be higher.
	 * @param separator Separator
	 */
	private void ruleTwo(Separator separator, boolean horizontal)
	{
		List<VisualStructure> overlappedElements = new ArrayList<VisualStructure>();
		if (horizontal)
			findHorizontalOverlappedElements(separator, _visualStructure, overlappedElements);
		else
			findVerticalOverlappedElements(separator, _visualStructure, overlappedElements);

		if (overlappedElements.size() == 0)
			return;

		for (VisualStructure visualStructure : overlappedElements)
		{
			if (visualStructure.getBox().getNode().getNodeName().equals("hr"))
			{
				separator.weight += 2;
				break;
			}
		}
	}

	/**
	 * Finds elements that are overlapped with horizontal separator.
	 * @param separator Separator, that we look at
	 * @param visualStructure Visual structure of element
	 * @param result Elements, that we found
	 */
	private void findHorizontalOverlappedElements(Separator separator,
			VisualStructure visualStructure, List<VisualStructure> result)
	{
		int topEdge = visualStructure.getBox().getAbsoluteContentY();
		int bottomEdge = topEdge + visualStructure.getBox().getContentHeight();

		// two upper edges of element are overlapped with separator
		if (topEdge > separator.startPoint && topEdge < separator.endPoint && bottomEdge > separator.endPoint)
		{
			result.add(visualStructure);
		}

		// two bottom edges of element are overlapped with separator
		if (topEdge < separator.startPoint && bottomEdge > separator.startPoint && bottomEdge < separator.endPoint)
		{
			result.add(visualStructure);
		}

		// all edges of element are overlapped with separator
		if (topEdge >= separator.startPoint && bottomEdge <= separator.endPoint)
		{
			result.add(visualStructure);
		}

		for (VisualStructure childVisualStructure : visualStructure.getChilds())
			findHorizontalOverlappedElements(separator, childVisualStructure, result);
	}

	/**
	 * Finds elements that are overlapped with vertical separator.
	 * @param separator Separator, that we look at
	 * @param visualStructure Visual structure of element
	 * @param result Elements, that we found
	 */
	private void findVerticalOverlappedElements(Separator separator,
			VisualStructure visualStructure, List<VisualStructure> result)
	{
		int leftEdge = visualStructure.getBox().getAbsoluteContentX();
		int rightEdge = leftEdge + visualStructure.getBox().getContentWidth();

		// two left edges of element are overlapped with separator
		if (leftEdge > separator.startPoint && leftEdge < separator.endPoint && rightEdge > separator.endPoint)
		{
			result.add(visualStructure);
		}

		// two right edges of element are overlapped with separator
		if (leftEdge < separator.startPoint && rightEdge > separator.startPoint && rightEdge < separator.endPoint)
		{
			result.add(visualStructure);
		}

		// all edges of element are overlapped with separator
		if (leftEdge >= separator.startPoint && rightEdge <= separator.endPoint)
		{
			result.add(visualStructure);
		}

		for (VisualStructure childVisualStructure : visualStructure.getChilds())
			findVerticalOverlappedElements(separator, childVisualStructure, result);
	}

	/**
	 * If background colors of the blocks on two sides of the separator
	 * are different, the weight will be increased.
	 * @param separator Separator
	 */
	private void ruleThree(Separator separator, boolean horizontal)
	{
		// for vertical is represents elements on left side
		List<VisualStructure> topAdjacentElements = new ArrayList<VisualStructure>();
		// for vertical is represents elements on right side
		List<VisualStructure> bottomAdjacentElements = new ArrayList<VisualStructure>();
		if (horizontal)
			findHorizontalAdjacentBlocks(separator, _visualStructure, topAdjacentElements, bottomAdjacentElements);
		else
			findVerticalAdjacentBlocks(separator, _visualStructure, topAdjacentElements, bottomAdjacentElements);

		if (topAdjacentElements.size() < 1 || bottomAdjacentElements.size() < 1)
			return;

		for (VisualStructure top : topAdjacentElements)
		{
			for (VisualStructure bottom : bottomAdjacentElements)
			{
				if (!top.getBgColor().equals(bottom.getBgColor()))
					separator.weight += 2;
			}
		}
	}

	/**
	 * Finds elements that are adjacent to horizontal separator.
	 * @param separator Separator, that we look at
	 * @param visualStructure Visual structure of element
	 * @param resultTop Elements, that we found on top side of separator
	 * @param resultBottom Elements, that we found on bottom side side of separator
	 */
	private void findHorizontalAdjacentBlocks(Separator separator,
			VisualStructure visualStructure, List<VisualStructure> resultTop, List<VisualStructure> resultBottom)
	{
		if (visualStructure.isVisualBlock())
		{
			int topEdge = visualStructure.getBox().getAbsoluteContentY();
			int bottomEdge = topEdge + visualStructure.getBox().getContentHeight();

			// if box is adjancent to separator from bottom
			if (topEdge == separator.endPoint + 1 && bottomEdge > separator.endPoint + 1)
			{
				resultBottom.add(visualStructure);
			}

			// if box is adjancent to separator from top
			if (bottomEdge == separator.startPoint - 1 && topEdge < separator.startPoint - 1)
			{
				resultTop.add(0, visualStructure);
			}
		}

		for (VisualStructure childVisualStructure : visualStructure.getChilds())
			findHorizontalAdjacentBlocks(separator, childVisualStructure, resultTop, resultBottom);
	}

	/**
	 * Finds elements that are adjacent to vertical separator.
	 * @param separator Separator, that we look at
	 * @param visualStructure Visual structure of element
	 * @param resultLeft Elements, that we found on left side of separator
	 * @param resultRight Elements, that we found on right side side of separator
	 */
	private void findVerticalAdjacentBlocks(Separator separator,
			VisualStructure visualStructure, List<VisualStructure> resultLeft, List<VisualStructure> resultRight)
	{
		if (visualStructure.isVisualBlock())
		{
			int leftEdge = visualStructure.getBox().getAbsoluteContentX() + 1;
			int rightEdge = leftEdge + visualStructure.getBox().getContentWidth();

			// if box is adjancent to separator from right
			if (leftEdge == separator.endPoint + 1 && rightEdge > separator.endPoint + 1)
			{
				resultRight.add(visualStructure);
			}

			// if box is adjancent to separator from left
			if (rightEdge == separator.startPoint - 1 && leftEdge < separator.startPoint - 1)
			{
				resultLeft.add(0, visualStructure);
			}
		}
		for (VisualStructure childVisualStructure : visualStructure.getChilds())
			findVerticalAdjacentBlocks(separator, childVisualStructure, resultLeft, resultRight);
	}

	/**
	 * For horizontal separators, if the differences of font properties
	 * such as font size and font weight are bigger on two
	 * sides of the separator, the weight will be increased.
	 * Moreover, the weight will be increased if the font size of the block
	 * above the separator is smaller than the font size of the block
	 * below the separator.
	 * @param separator Separator
	 */
	private void ruleFour(Separator separator)
	{
		List<VisualStructure> topAdjacentElements = new ArrayList<VisualStructure>();
		List<VisualStructure> bottomAdjacentElements = new ArrayList<VisualStructure>();

		findHorizontalAdjacentBlocks(separator, _visualStructure, topAdjacentElements, bottomAdjacentElements);

		if (topAdjacentElements.size() < 1 || bottomAdjacentElements.size() < 1)
			return;

		boolean weightIncreased = false;

		for (VisualStructure top : topAdjacentElements)
		{
			for (VisualStructure bottom : bottomAdjacentElements)
			{
				int diff = Math.abs(top.getFontSize() - bottom.getFontSize());
				if (diff != 0)
				{
					diff /= 2;
					separator.weight += 2;
					weightIncreased = true;
					break;
				}
			}
			if (weightIncreased)
				break;
		}

		weightIncreased = false;
		//TODO font weight

		for (VisualStructure top : topAdjacentElements)
		{
			for (VisualStructure bottom : bottomAdjacentElements)
			{
				if (top.getFontSize() < bottom.getFontSize())
				{
					separator.weight += 2;
					weightIncreased = true;
					break;
				}
			}
			if (weightIncreased)
				break;
		}
	}

	/**
	 * For horizontal separators, when the structures of the blocks on the two
	 * sides of the separator are very similar (e.g. both are text),
	 * the weight of the separator will be decreased.
	 * @param separator Separator
	 */
	private void ruleFive(Separator separator)
	{
		List<VisualStructure> topAdjacentElements = new ArrayList<VisualStructure>();
		List<VisualStructure> bottomAdjacentElements = new ArrayList<VisualStructure>();

		findHorizontalAdjacentBlocks(separator, _visualStructure, topAdjacentElements, bottomAdjacentElements);

		if (topAdjacentElements.size() < 1 || bottomAdjacentElements.size() < 1)
			return;

		boolean weightDecreased = false;

		for (VisualStructure top : topAdjacentElements)
		{
			for (VisualStructure bottom : bottomAdjacentElements)
			{
				if (top.getBox() instanceof TextBox &&
						bottom.getBox() instanceof TextBox)
				{
					separator.weight -= 2;
					weightDecreased = true;
					break;
				}
			}
			if (weightDecreased)
				break;
		}
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
			System.err.print(e.getStackTrace());
		}
	}

	/**
	 * Class that represents visual separator
	 */
	public class Separator {
		public int startPoint;
		public int endPoint;
		public int weight = 10;
		public boolean horizontal = true;

		public Separator(int start, int end) {
			this.startPoint = start;
			this.endPoint = end;
		}

		public Separator(int start, int end, boolean horizontal) {
			this.startPoint = start;
			this.endPoint = end;
			this.horizontal = horizontal;
		}

		public Separator(int start, int end, int weight) {
			this.startPoint = start;
			this.endPoint = end;
			this.weight = weight;
		}
	}
}
