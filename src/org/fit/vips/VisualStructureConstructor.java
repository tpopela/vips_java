/*
 * Tomas Popela, xpopel11, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VisualStructureConstructor.java
 */

package org.fit.vips;

import java.util.ArrayList;
import java.util.List;

public class VisualStructureConstructor {

	private VipsBlock _vipsBlocks = null;
	private List<VipsBlock> _visualBlocks = null;
	private VisualStructure _visualStructure = null;
	private List<Separator> _horizontalSeparators = null;
	private List<Separator> _verticalSeparators = null;
	private int _pageWidth = 0;
	private int _pageHeight = 0;
	int _level = 0;

	public VisualStructureConstructor()
	{
		this._horizontalSeparators = new ArrayList<>();
		this._verticalSeparators = new ArrayList<>();
	}

	public VisualStructureConstructor(VipsBlock vipsBlocks, List<Separator> horizontalSeparators, List<Separator> vericalSeparators)
	{
		this._vipsBlocks = vipsBlocks;
		this._horizontalSeparators = horizontalSeparators;
		this._verticalSeparators = vericalSeparators;
	}

	private void getVisualStructuresFromLevel(int level, int actualLevel, VisualStructure visualStructure, List<VisualStructure> results)
	{
		if (actualLevel == level)
		{
			results.add(visualStructure);
			return;
		}

		for (VisualStructure childVisualStructure : visualStructure.getChildrenVisualStructures())
		{
			getVisualStructuresFromLevel(level, actualLevel+1, childVisualStructure, results);
		}
	}

	public void constructVisualStructure()
	{
		List<VisualStructure> results = new ArrayList<>();

		//construct visual structure with visual blocks and horizontal separators
		if (_visualStructure == null)
		{
			// first run
			VipsSeparatorGraphicsDetector detector = new VipsSeparatorGraphicsDetector(_pageWidth, _pageHeight);
			detector.setVisualBlocks(_visualBlocks);
			detector.setVipsBlock(_vipsBlocks);
			detector.detectHorizontalSeparators();
			this._horizontalSeparators = detector.getHorizontalSeparators();

			_visualStructure = new VisualStructure();
			_visualStructure.setId("1");
			_visualStructure.setNestedBlocks(_visualBlocks);
			_visualStructure.setWidth(_pageWidth);
			_visualStructure.setHeight(_pageHeight);
			constructWithHorizontalSeparators(_visualStructure);

		}
		else
		{
			getVisualStructuresFromLevel(_level, 0, _visualStructure, results);

			for (VisualStructure childVisualStructure : results)
			{
				VipsSeparatorGraphicsDetector detector = new VipsSeparatorGraphicsDetector(childVisualStructure.getWidth(), childVisualStructure.getHeight());
				detector.setVisualBlocks(childVisualStructure.getNestedBlocks());
				detector.setVipsBlock(_vipsBlocks);
				detector.detectHorizontalSeparators();
				this._horizontalSeparators = detector.getHorizontalSeparators();
				constructWithHorizontalSeparators(childVisualStructure);
			}
		}
		_level++;

		//construct visual structure with visual blocks and vertical separators
		getVisualStructuresFromLevel(_level, 0, _visualStructure, results);

		for (VisualStructure childVisualStructure : results)
		{
			//detect vertical separators for each horizontal block
			VipsSeparatorGraphicsDetector detector = new VipsSeparatorGraphicsDetector(childVisualStructure.getWidth(), childVisualStructure.getHeight());
			detector.setVipsBlock(_vipsBlocks);
			detector.setVisualBlocks(childVisualStructure.getNestedBlocks());
			detector.detectVerticalSeparators();
			this._verticalSeparators = detector.getVerticalSeparators();
			constructWithVerticalSeparators(childVisualStructure);
		}
		_level++;
	}

	private void constructWithHorizontalSeparators(VisualStructure actualStructure)
	{
		// if we have no visual blocks or separators
		if (actualStructure.getNestedBlocks().size() == 0 || _horizontalSeparators.size() == 0)
		{
			return;
		}

		VisualStructure topVisualStructure = null;
		VisualStructure bottomVisualStructure =  null;
		List<VipsBlock> nestedBlocks =  null;

		for (Separator separator : _horizontalSeparators)
		{
			if (actualStructure.getChildrenVisualStructures().size() == 0)
			{
				topVisualStructure = new VisualStructure();
				topVisualStructure.setX(0);
				topVisualStructure.setY(0);
				topVisualStructure.setHeight(separator.startPoint-1);
				topVisualStructure.setWidth(_pageWidth);
				topVisualStructure.addHorizontalSeparator(separator);
				actualStructure.addChild(topVisualStructure);

				bottomVisualStructure = new VisualStructure();
				bottomVisualStructure.setX(0);
				bottomVisualStructure.setY(separator.endPoint+1);
				bottomVisualStructure.setHeight(_pageHeight-separator.endPoint-1);
				bottomVisualStructure.setWidth(_pageWidth);
				bottomVisualStructure.addHorizontalSeparator(separator);
				actualStructure.addChild(bottomVisualStructure);

				nestedBlocks = actualStructure.getNestedBlocks();
			}
			else
			{
				VisualStructure oldStructure = null;
				for (VisualStructure childVisualStructure : actualStructure.getChildrenVisualStructures())
				{
					if (separator.startPoint >= childVisualStructure.getY() &&
							separator.endPoint <= (childVisualStructure.getY() + childVisualStructure.getHeight()))
					{
						topVisualStructure = new VisualStructure();
						topVisualStructure.setX(childVisualStructure.getX());
						topVisualStructure.setY(childVisualStructure.getY());
						topVisualStructure.setHeight(separator.startPoint-1);
						topVisualStructure.setWidth(childVisualStructure.getWidth());
						topVisualStructure.addHorizontalSeparator(separator);
						int index = actualStructure.getChildrenVisualStructures().indexOf(childVisualStructure);
						actualStructure.addChildAt(topVisualStructure, index);

						bottomVisualStructure = new VisualStructure();
						bottomVisualStructure.setX(childVisualStructure.getX());
						bottomVisualStructure.setY(separator.endPoint+1);
						int height = childVisualStructure.getHeight()-separator.endPoint-1;
						if (height < 0)
							height = _pageHeight - separator.endPoint - 1;
						bottomVisualStructure.setHeight(height);
						bottomVisualStructure.setWidth(childVisualStructure.getWidth());
						bottomVisualStructure.addHorizontalSeparator(separator);
						actualStructure.addChildAt(bottomVisualStructure, index+1);

						oldStructure = childVisualStructure;
						break;
					}
				}
				nestedBlocks = oldStructure.getNestedBlocks();
				actualStructure.getChildrenVisualStructures().remove(oldStructure);
			}

			if (topVisualStructure == null || bottomVisualStructure == null)
				return;

			for (VipsBlock vipsBlock : nestedBlocks)
			{
				if (vipsBlock.getBox().getAbsoluteContentY() <= separator.startPoint)
					topVisualStructure.addNestedBlock(vipsBlock);
				else
					bottomVisualStructure.addNestedBlock(vipsBlock);
			}

			topVisualStructure = null;
			bottomVisualStructure = null;
		}

		// set id for visual structures
		int iterator = 1;
		for (VisualStructure visualStructure : actualStructure.getChildrenVisualStructures())
		{
			visualStructure.setId(actualStructure.getId() + "-" + iterator);
			iterator++;
		}
	}

	private void constructWithVerticalSeparators(VisualStructure actualStructure)
	{
		// if we have no visual blocks or separators
		if (actualStructure.getNestedBlocks().size() == 0 || _verticalSeparators.size() == 0)
		{
			return;
		}

		VisualStructure topVisualStructure = null;
		VisualStructure bottomVisualStructure =  null;
		List<VipsBlock> nestedBlocks =  null;

		for (Separator separator : _verticalSeparators)
		{
			if (actualStructure.getChildrenVisualStructures().size() == 0)
			{
				topVisualStructure = new VisualStructure();
				topVisualStructure.setX(0);
				topVisualStructure.setY(0);
				topVisualStructure.setHeight(separator.startPoint-1);
				topVisualStructure.setWidth(_pageWidth);
				topVisualStructure.addVerticalSeparator(separator);
				actualStructure.addChild(topVisualStructure);

				bottomVisualStructure = new VisualStructure();
				bottomVisualStructure.setX(0);
				bottomVisualStructure.setY(separator.endPoint+1);
				bottomVisualStructure.setHeight(_pageHeight-separator.endPoint+1);
				bottomVisualStructure.setWidth(_pageWidth);
				bottomVisualStructure.addVerticalSeparator(separator);
				actualStructure.addChild(bottomVisualStructure);

				nestedBlocks = actualStructure.getNestedBlocks();
			}
			else
			{
				VisualStructure oldStructure = null;
				for (VisualStructure childVisualStructure : actualStructure.getChildrenVisualStructures())
				{
					if (separator.startPoint >= childVisualStructure.getY() &&
							separator.endPoint <= (childVisualStructure.getY() + childVisualStructure.getHeight()))
					{
						topVisualStructure = new VisualStructure();
						topVisualStructure.setX(childVisualStructure.getX());
						topVisualStructure.setY(childVisualStructure.getY());
						topVisualStructure.setHeight(separator.startPoint-1);
						topVisualStructure.setWidth(childVisualStructure.getWidth());
						topVisualStructure.addVerticalSeparator(separator);
						actualStructure.addChild(topVisualStructure);

						bottomVisualStructure = new VisualStructure();
						bottomVisualStructure.setX(childVisualStructure.getX());
						bottomVisualStructure.setY(separator.startPoint+1);
						bottomVisualStructure.setHeight(childVisualStructure.getHeight()-separator.endPoint+1);
						bottomVisualStructure.setWidth(childVisualStructure.getWidth());
						bottomVisualStructure.addVerticalSeparator(separator);
						actualStructure.addChild(bottomVisualStructure);

						oldStructure = childVisualStructure;
						break;
					}
				}
				nestedBlocks = oldStructure.getNestedBlocks();
				actualStructure.getChildrenVisualStructures().remove(oldStructure);
			}

			if (topVisualStructure == null || bottomVisualStructure == null)
				return;

			for (VipsBlock vipsBlock : nestedBlocks)
			{
				if (vipsBlock.getBox().getAbsoluteContentX() <= separator.startPoint)
					topVisualStructure.addNestedBlock(vipsBlock);
				else
					bottomVisualStructure.addNestedBlock(vipsBlock);
			}

			topVisualStructure = null;
			bottomVisualStructure = null;
		}
		// set id for visual structures
		int iterator = 1;
		for (VisualStructure visualStructure : actualStructure.getChildrenVisualStructures())
		{
			visualStructure.setId(actualStructure.getId() + "-" + iterator);
			iterator++;
		}
	}

	public void setPageSize(int width, int height)
	{
		this._pageHeight = height;
		this._pageWidth = width;
	}

	/**
	 * @return the _vipsBlocks
	 */
	public VipsBlock getVipsBlocks()
	{
		return _vipsBlocks;
	}

	/**
	 * @return the _visualStructure
	 */
	public VisualStructure getVisualStructure()
	{
		return _visualStructure;
	}

	private void getVisualBlocks(VipsBlock vipsBlock, List<VipsBlock> results)
	{
		if (vipsBlock.isVisualBlock())
			results.add(vipsBlock);

		for (VipsBlock child : vipsBlock.getChildren())
		{
			getVisualBlocks(child, results);
		}
	}

	/**
	 * @param vipsBlocks the vipsBlocks to set
	 */
	public void setVipsBlock(VipsBlock vipsBlocks)
	{
		this._vipsBlocks = vipsBlocks;

		_visualBlocks = new ArrayList<>();
		getVisualBlocks(vipsBlocks, _visualBlocks);

	}

	/**
	 * @return the _horizontalSeparator
	 */
	public List<Separator> getHorizontalSeparators()
	{
		return _horizontalSeparators;
	}

	/**
	 * @param horizontalSeparators the horizontalSeparators to set
	 */
	public void setHorizontalSeparator(List<Separator> horizontalSeparators)
	{
		this._horizontalSeparators = horizontalSeparators;
	}

	/**
	 * @return the _verticalSeparators
	 */
	public List<Separator> getVerticalSeparators()
	{
		return _verticalSeparators;
	}

	/**
	 * @param verticalSeparators the verticalSeparators to set
	 */
	public void setVerticalSeparator(List<Separator> verticalSeparators)
	{
		this._verticalSeparators = verticalSeparators;
	}

	/**
	 * @param verticalSeparators the verticalSeparators to set
	 * @param horizontalSeparators the horizontalSeparators to set
	 */
	public void setSeparators(List<Separator> horizontalSeparators, List<Separator> verticalSeparators)
	{
		this._verticalSeparators = verticalSeparators;
		this._horizontalSeparators = horizontalSeparators;
	}
}
