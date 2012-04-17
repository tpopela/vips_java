/*
 * Tomas Popela, xpopel11, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VipsSeparatorDetector.java
 */

package org.fit.vips;

import java.util.ArrayList;
import java.util.List;

public class VipsSeparatorDetector {

	List<Separator> _horizontalSeparators = null;
	List<Separator> _verticalSeparators = null;
	int _width = 0;
	int _height = 0;

	/**
	 * Defaults constructor.
	 * @param width Pools width
	 * @param height Pools height
	 */
	public VipsSeparatorDetector(int width, int height) {
		_horizontalSeparators = new ArrayList<VipsSeparatorDetector.Separator>();
		_verticalSeparators = new ArrayList<VipsSeparatorDetector.Separator>();
		_width = width;
		_height = height;
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
		_horizontalSeparators.clear();
		_horizontalSeparators.add(new Separator(0, _height));

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
		_verticalSeparators.clear();
		_verticalSeparators.add(new Separator(0, _width));

		findVerticalSeparators(visualStructure);

		//remove pool borders
		_verticalSeparators.remove(0);
		_verticalSeparators.remove(_verticalSeparators.size()-1);
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
