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
	private List<Separator> _horizontalSeparators = null;
	private List<Separator> _verticalSeparators = null;
	private int _pageWidth = 0;
	private int _pageHeight = 0;
	private VisualStructure _visualStructure = null;
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

	public void setPageSize(int width, int height)
	{
		this._pageHeight = height;
		this._pageWidth = width;
	}

	private void fillVisualStructure(VisualStructure visualStructure, VipsBlock vipsBlock)
	{
		if (vipsBlock.isVisualBlock())
			visualStructure.addNestedBlock(vipsBlock);

		for (VipsBlock childVipsBlock : vipsBlock.getChildren())
			fillVisualStructure(visualStructure, childVipsBlock);
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

		if (_visualStructure == null)
			constructWithHorizontalSeparators();
		else
		{
			getVisualStructuresFromLevel(_level, 0, _visualStructure, results);

			for (VisualStructure childVisualStructure : results)
			{
				VipsSeparatorGraphicsDetector detector = new VipsSeparatorGraphicsDetector(childVisualStructure.getWidth(), childVisualStructure.getHeight());
				detector.setVisualBlocks(childVisualStructure.getNestedBlocks());
				detector.detectHorizontalSeparators();
				this._verticalSeparators = detector.getHorizontalSeparators();
				constructWithHorizontalSeparators();
			}
			_level++;
		}

		getVisualStructuresFromLevel(_level, 0, _visualStructure, results);

		for (VisualStructure childVisualStructure : results)
		{
			//detect vertical separators for each horizontal block
			VipsSeparatorGraphicsDetector detector = new VipsSeparatorGraphicsDetector(childVisualStructure.getWidth(), childVisualStructure.getHeight());
			detector.setVisualBlocks(childVisualStructure.getNestedBlocks());
			detector.detectVerticalSeparators();
			this._verticalSeparators = detector.getVerticalSeparators();
			constructWithVerticalSeparators();
		}
		_level++;
	}

	private void constructWithHorizontalSeparators()
	{
		if (_vipsBlocks == null || _horizontalSeparators.size() == 0)
		{
			System.err.println("I don't have enough informations for visual structure construction!");
			return;
		}

		//first run
		if (_visualStructure == null)
		{
			_visualStructure = new VisualStructure();
			_visualStructure.setId("VB");
			fillVisualStructure(_visualStructure, _vipsBlocks);
		}

		VisualStructure topVisualStructure = null;
		VisualStructure bottomVisualStructure =  null;
		List<VipsBlock> nestedBlocks =  null;

		for (Separator separator : _horizontalSeparators)
		{
			if (_visualStructure.getChildrenVisualStructures().size() == 0)
			{
				topVisualStructure = new VisualStructure();
				topVisualStructure.setX(0);
				topVisualStructure.setY(0);
				topVisualStructure.setHeight(separator.startPoint-1);
				topVisualStructure.setWidth(_pageWidth);
				_visualStructure.addChild(topVisualStructure);

				bottomVisualStructure = new VisualStructure();
				bottomVisualStructure.setX(0);
				bottomVisualStructure.setY(separator.endPoint+1);
				bottomVisualStructure.setHeight(_pageHeight-separator.endPoint+1);
				bottomVisualStructure.setWidth(_pageWidth);
				_visualStructure.addChild(bottomVisualStructure);

				nestedBlocks = _visualStructure.getNestedBlocks();
			}
			else
			{
				VisualStructure oldStructure = null;
				for (VisualStructure childVisualStructure : _visualStructure.getChildrenVisualStructures())
				{
					if (separator.startPoint >= childVisualStructure.getY() &&
							separator.endPoint <= (childVisualStructure.getY() + childVisualStructure.getHeight()))
					{
						topVisualStructure = new VisualStructure();
						topVisualStructure.setX(childVisualStructure.getX());
						topVisualStructure.setY(childVisualStructure.getY());
						topVisualStructure.setHeight(separator.startPoint-1);
						topVisualStructure.setWidth(childVisualStructure.getWidth());
						_visualStructure.addChild(topVisualStructure);

						bottomVisualStructure = new VisualStructure();
						bottomVisualStructure.setX(childVisualStructure.getX());
						bottomVisualStructure.setY(separator.startPoint+1);
						bottomVisualStructure.setHeight(childVisualStructure.getHeight()-separator.endPoint+1);
						bottomVisualStructure.setWidth(childVisualStructure.getWidth());
						_visualStructure.addChild(bottomVisualStructure);

						oldStructure = childVisualStructure;
						break;
					}
				}
				nestedBlocks = oldStructure.getNestedBlocks();
				_visualStructure.getChildrenVisualStructures().remove(oldStructure);
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
	}

	private void constructWithVerticalSeparators()
	{
		if (_vipsBlocks == null || _verticalSeparators.size() == 0)
		{
			System.err.println("I don't have enough informations for visual structure construction!");
			return;
		}

		VisualStructure topVisualStructure = null;
		VisualStructure bottomVisualStructure =  null;
		List<VipsBlock> nestedBlocks =  null;

		for (Separator separator : _verticalSeparators)
		{
			if (_visualStructure.getChildrenVisualStructures().size() == 0)
			{
				//toto by se zde nemelo stat, ale radeji to tu necham
				topVisualStructure = new VisualStructure();
				topVisualStructure.setX(0);
				topVisualStructure.setY(0);
				topVisualStructure.setHeight(separator.startPoint-1);
				topVisualStructure.setWidth(_pageWidth);
				_visualStructure.addChild(topVisualStructure);

				bottomVisualStructure = new VisualStructure();
				bottomVisualStructure.setX(0);
				bottomVisualStructure.setY(separator.endPoint+1);
				bottomVisualStructure.setHeight(_pageHeight-separator.endPoint+1);
				bottomVisualStructure.setWidth(_pageWidth);
				_visualStructure.addChild(bottomVisualStructure);

				nestedBlocks = _visualStructure.getNestedBlocks();
			}
			else
			{
				VisualStructure oldStructure = null;
				for (VisualStructure childVisualStructure : _visualStructure.getChildrenVisualStructures())
				{
					if (separator.startPoint >= childVisualStructure.getY() &&
							separator.endPoint <= (childVisualStructure.getY() + childVisualStructure.getHeight()))
					{
						topVisualStructure = new VisualStructure();
						topVisualStructure.setX(childVisualStructure.getX());
						topVisualStructure.setY(childVisualStructure.getY());
						topVisualStructure.setHeight(separator.startPoint-1);
						topVisualStructure.setWidth(childVisualStructure.getWidth());
						_visualStructure.addChild(topVisualStructure);

						bottomVisualStructure = new VisualStructure();
						bottomVisualStructure.setX(childVisualStructure.getX());
						bottomVisualStructure.setY(separator.startPoint+1);
						bottomVisualStructure.setHeight(childVisualStructure.getHeight()-separator.endPoint+1);
						bottomVisualStructure.setWidth(childVisualStructure.getWidth());
						_visualStructure.addChild(bottomVisualStructure);

						oldStructure = childVisualStructure;
						break;
					}
				}
				nestedBlocks = oldStructure.getNestedBlocks();
				_visualStructure.getChildrenVisualStructures().remove(oldStructure);
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
	}

	/**
	 * @return the _vipsBlocks
	 */
	public VipsBlock getVipsBlocks()
	{
		return _vipsBlocks;
	}

	/**
	 * @param vipsBlocks the vipsBlocks to set
	 */
	public void setVipsBlock(VipsBlock vipsBlocks)
	{
		this._vipsBlocks = vipsBlocks;
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
