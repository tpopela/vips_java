/*
 * Tomas Popela, xpopel11, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VisualStructureConstructor.java
 */

package org.fit.vips;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class VisualStructureConstructor {

	private VipsBlock _vipsBlocks = null;
	private List<VipsBlock> _visualBlocks = null;
	private VisualStructure _visualStructure = null;
	private List<Separator> _horizontalSeparators = null;
	private List<Separator> _verticalSeparators = null;
	private int _pageWidth = 0;
	private int _pageHeight = 0;
	private int _srcOrder = 1;
	private int _iteration = 1;

	private boolean _graphicsOutput = true;

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

	public void constructVisualStructure()
	{
		List<VisualStructure> results = new ArrayList<>();

		//construct visual structure with visual blocks and horizontal separators
		if (_visualStructure == null)
		{
			// first run
			VipsSeparatorDetector detector = null;

			if (_graphicsOutput)
				detector = new VipsSeparatorGraphicsDetector(_pageWidth, _pageHeight);
			else
				detector = new VipsSeparatorNonGraphicsDetector(_pageWidth, _pageHeight);

			detector.setVipsBlock(_vipsBlocks);
			detector.setVisualBlocks(_visualBlocks);
			detector.setCleanUpSeparators(true);
			detector.detectHorizontalSeparators();
			this._horizontalSeparators = detector.getHorizontalSeparators();
			detector.setCleanUpSeparators(false);

			_visualStructure = new VisualStructure();
			_visualStructure.setId("1");
			_visualStructure.setNestedBlocks(_visualBlocks);
			_visualStructure.setWidth(_pageWidth);
			_visualStructure.setHeight(_pageHeight);

			for (Separator separator : _horizontalSeparators)
			{
				separator.setLeftUp(_visualStructure.getX(), separator.startPoint);
				separator.setRightDown(_visualStructure.getX()+_visualStructure.getWidth(), separator.endPoint);
			}
			constructWithHorizontalSeparators(_visualStructure);
		}
		else
		{
			findListVisualStructures(_visualStructure, results);

			for (VisualStructure childVisualStructure : results)
			{
				VipsSeparatorDetector detector = null;

				if (_graphicsOutput)
					detector = new VipsSeparatorGraphicsDetector(_pageWidth, _pageHeight);
				else
					detector = new VipsSeparatorNonGraphicsDetector(_pageWidth, _pageHeight);

				detector.setVipsBlock(_vipsBlocks);
				detector.setVisualBlocks(childVisualStructure.getNestedBlocks());
				detector.detectHorizontalSeparators();
				this._horizontalSeparators = detector.getHorizontalSeparators();

				for (Separator separator : _horizontalSeparators)
				{
					separator.setLeftUp(childVisualStructure.getX(), separator.startPoint);
					separator.setRightDown(childVisualStructure.getX()+childVisualStructure.getWidth(), separator.endPoint);
				}

				constructWithHorizontalSeparators(childVisualStructure);
			}
		}

		//construct visual structure with visual blocks and vertical separators
		results.clear();
		findListVisualStructures(_visualStructure, results);

		for (VisualStructure childVisualStructure : results)
		{
			VipsSeparatorDetector detector = null;

			if (_graphicsOutput)
				detector = new VipsSeparatorGraphicsDetector(_pageWidth, _pageHeight);
			else
				detector = new VipsSeparatorNonGraphicsDetector(_pageWidth, _pageHeight);
			//detect vertical separators for each horizontal block
			detector.setVipsBlock(_vipsBlocks);
			detector.setVisualBlocks(childVisualStructure.getNestedBlocks());
			detector.detectVerticalSeparators();
			this._verticalSeparators = detector.getVerticalSeparators();

			for (Separator separator : _verticalSeparators)
			{
				separator.setLeftUp(separator.startPoint, childVisualStructure.getY());
				separator.setRightDown(separator.endPoint, childVisualStructure.getY()+childVisualStructure.getHeight());
			}

			constructWithVerticalSeparators(childVisualStructure);
		}

		_srcOrder = 1;
		setOrder(_visualStructure);

		// first run
		if (_graphicsOutput)
		{
			exportSeparators();
		}
		_iteration++;
	}

	private void exportSeparators()
	{
		VipsSeparatorGraphicsDetector detector = new VipsSeparatorGraphicsDetector(_pageWidth, _pageHeight);
		List<Separator> allSeparators = new ArrayList<>();

		getAllHorizontalSeparators(_visualStructure, allSeparators);
		Collections.sort(allSeparators);

		detector.setHorizontalSeparators(allSeparators);
		detector.exportHorizontalSeparatorsToImage(_iteration);

		allSeparators.clear();

		getAllVerticalSeparators(_visualStructure, allSeparators);
		Collections.sort(allSeparators);

		detector.setVerticalSeparators(allSeparators);
		detector.exportVerticalSeparatorsToImage(_iteration);

		detector.setVisualBlocks(_visualBlocks);
		detector.exportAllToImage(_iteration);
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
				topVisualStructure.setX(actualStructure.getX());
				topVisualStructure.setY(actualStructure.getY());
				topVisualStructure.setHeight((separator.startPoint-1)-actualStructure.getY());
				topVisualStructure.setWidth(actualStructure.getWidth());
				actualStructure.addChild(topVisualStructure);

				bottomVisualStructure = new VisualStructure();
				bottomVisualStructure.setX(actualStructure.getX());
				bottomVisualStructure.setY(separator.endPoint+1);
				bottomVisualStructure.setHeight((actualStructure.getHeight()+actualStructure.getY())-separator.endPoint-1);
				bottomVisualStructure.setWidth(actualStructure.getWidth());
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
						topVisualStructure.setHeight((separator.startPoint-1) - childVisualStructure.getY());
						topVisualStructure.setWidth(childVisualStructure.getWidth());
						int index = actualStructure.getChildrenVisualStructures().indexOf(childVisualStructure);
						actualStructure.addChildAt(topVisualStructure, index);

						bottomVisualStructure = new VisualStructure();
						bottomVisualStructure.setX(childVisualStructure.getX());
						bottomVisualStructure.setY(separator.endPoint+1);
						int height = (childVisualStructure.getHeight()+childVisualStructure.getY())-separator.endPoint-1;
						bottomVisualStructure.setHeight(height);
						bottomVisualStructure.setWidth(childVisualStructure.getWidth());
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

		for (Separator separator : _horizontalSeparators)
		{
			for (VisualStructure visualStructure : actualStructure.getChildrenVisualStructures())
			{
				if (separator.endPoint == visualStructure.getY() + 1)
					visualStructure.addHorizontalSeparator(separator);

				if (separator.startPoint == visualStructure.getY() + visualStructure.getHeight() + 1)
					visualStructure.addHorizontalSeparator(separator);
			}
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
				topVisualStructure.setX(actualStructure.getX());
				topVisualStructure.setY(actualStructure.getY());
				topVisualStructure.setHeight(actualStructure.getHeight());
				topVisualStructure.setWidth((separator.startPoint-1)-actualStructure.getX());
				actualStructure.addChild(topVisualStructure);

				bottomVisualStructure = new VisualStructure();
				bottomVisualStructure.setX(separator.endPoint+1);
				bottomVisualStructure.setY(actualStructure.getY());
				bottomVisualStructure.setHeight(actualStructure.getHeight());
				bottomVisualStructure.setWidth((actualStructure.getWidth()+actualStructure.getX()) - separator.endPoint-1);
				actualStructure.addChild(bottomVisualStructure);

				nestedBlocks = actualStructure.getNestedBlocks();
			}
			else
			{
				VisualStructure oldStructure = null;
				for (VisualStructure childVisualStructure : actualStructure.getChildrenVisualStructures())
				{
					if (separator.startPoint >= childVisualStructure.getX() &&
							separator.endPoint <= (childVisualStructure.getX() + childVisualStructure.getWidth()))
					{
						topVisualStructure = new VisualStructure();
						topVisualStructure.setX(childVisualStructure.getX());
						topVisualStructure.setY(childVisualStructure.getY());
						topVisualStructure.setHeight(childVisualStructure.getHeight());
						topVisualStructure.setWidth((separator.startPoint-1)-childVisualStructure.getX());
						int index = actualStructure.getChildrenVisualStructures().indexOf(childVisualStructure);
						actualStructure.addChildAt(topVisualStructure, index);

						bottomVisualStructure = new VisualStructure();
						bottomVisualStructure.setX(separator.endPoint+1);
						bottomVisualStructure.setY(childVisualStructure.getY());
						bottomVisualStructure.setHeight(childVisualStructure.getHeight());
						int width = (childVisualStructure.getWidth()+childVisualStructure.getX())-separator.endPoint-1;
						bottomVisualStructure.setWidth(width);
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

		for (Separator separator : _verticalSeparators)
		{
			for (VisualStructure visualStructure : actualStructure.getChildrenVisualStructures())
			{
				if (separator.endPoint == visualStructure.getX() + 1)
					visualStructure.addVerticalSeparator(separator);

				if (separator.startPoint == visualStructure.getX() + visualStructure.getWidth() + 1)
					visualStructure.addVerticalSeparator(separator);
			}
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
	public void setVipsBlocks(VipsBlock vipsBlocks)
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

	private void findListVisualStructures(VisualStructure visualStructure, List<VisualStructure> results)
	{
		if (visualStructure.getChildrenVisualStructures().size() == 0)
			results.add(visualStructure);

		for (VisualStructure child : visualStructure.getChildrenVisualStructures())
			findListVisualStructures(child, results);
	}

	public void updateVipsBlocks(VipsBlock vipsBlocks)
	{
		setVipsBlocks(vipsBlocks);

		List<VisualStructure> listsVisualStructures = new ArrayList<>();
		List<VipsBlock> oldNestedBlocks = new ArrayList<>();
		findListVisualStructures(_visualStructure, listsVisualStructures);

		for (VisualStructure visualStructure : listsVisualStructures)
		{
			oldNestedBlocks.addAll(visualStructure.getNestedBlocks());
			visualStructure.clearNestedBlocks();
			for (VipsBlock visualBlock : _visualBlocks)
			{
				if (visualBlock.getBox().getAbsoluteContentX() >= visualStructure.getX() &&
						visualBlock.getBox().getAbsoluteContentX() <= (visualStructure.getX() + visualStructure.getWidth()))
				{
					if (visualBlock.getBox().getAbsoluteContentY() >= visualStructure.getY() &&
							visualBlock.getBox().getAbsoluteContentY() <= (visualStructure.getY() + visualStructure.getHeight()))
					{
						visualStructure.addNestedBlock(visualBlock);
					}

				}
			}
			if (visualStructure.getNestedBlocks().size() == 0)
			{
				visualStructure.addNestedBlocks(oldNestedBlocks);
				_visualBlocks.addAll(oldNestedBlocks);
			}
			oldNestedBlocks.clear();
		}
	}

	private void setOrder(VisualStructure visualStructure)
	{
		visualStructure.setOrder(_srcOrder);
		_srcOrder++;

		for (VisualStructure child : visualStructure.getChildrenVisualStructures())
			setOrder(child);
	}

	private void getAllSeparators(VisualStructure visualStructure, List<Separator> result)
	{
		findAllHorizontalSeparators(visualStructure, result);
		findAllVerticalSeparators(visualStructure, result);
		removeDuplicates(result);
	}

	private void getAllHorizontalSeparators(VisualStructure visualStructure, List<Separator> result)
	{
		findAllHorizontalSeparators(visualStructure, result);
		removeDuplicates(result);
	}

	private void getAllVerticalSeparators(VisualStructure visualStructure, List<Separator> result)
	{
		findAllVerticalSeparators(visualStructure, result);
		removeDuplicates(result);
	}

	private void findAllHorizontalSeparators(VisualStructure visualStructure, List<Separator> result)
	{
		result.addAll(visualStructure.getHorizontalSeparators());

		for (Separator sep : visualStructure.getHorizontalSeparators())
		{
			if ((sep.endPoint - sep.startPoint + 1) == 0 || (sep.endPoint - sep.startPoint + 1) == -1)
			{
				System.err.println(sep.startPoint + " " + sep.endPoint + " " + " " + sep.weight + " " + visualStructure.getId());
			}
		}

		for (VisualStructure child : visualStructure.getChildrenVisualStructures())
		{
			findAllHorizontalSeparators(child, result);
		}
	}

	private void findAllVerticalSeparators(VisualStructure visualStructure, List<Separator> result)
	{
		result.addAll(visualStructure.getVerticalSeparators());

		for (Separator sep : visualStructure.getVerticalSeparators())
		{
			if ((sep.endPoint - sep.startPoint + 1) == 0 || (sep.endPoint - sep.startPoint + 1) == -1)
			{
				System.err.println(sep.startPoint + " " + sep.endPoint + " " + " " + sep.weight + " " + visualStructure.getId());
			}
		}
		for (VisualStructure child : visualStructure.getChildrenVisualStructures())
		{
			findAllVerticalSeparators(child, result);
		}
	}

	private void removeDuplicates(List<Separator> separators)
	{
		//TODO nejspis neni potreba
		HashSet<Separator> hashSet = new HashSet<Separator>(separators);
		separators.clear();
		separators.addAll(hashSet);
	}

	public void setGraphicsOutput(boolean value)
	{
		this._graphicsOutput = value;
	}

	private int getInverseValue(int value)
	{
		if (value == 11)
			return 1;
		if (value == 10)
			return 2;
		if (value == 9)
			return 3;
		if (value == 8)
			return 4;
		if (value == 7)
			return 5;
		if (value == 6)
			return 6;
		if (value == 5)
			return 7;
		if (value == 4)
			return 8;
		if (value == 3)
			return 9;
		if (value == 2)
			return 10;
		if (value == 1)
			return 11;

		return 0;
	}

	public void normalizeSeparators()
	{
		List<Separator> separators = new ArrayList<>();

		getAllSeparators(_visualStructure, separators);
		Collections.sort(separators);

		double minWeight = separators.get(0).weight;
		double maxWeight = separators.get(separators.size()-1).weight;

		for (Separator separator : separators)
		{
			double normalizedValue = (separator.weight - minWeight) / (maxWeight - minWeight) * (11 - 1) + 1;
			separator.normalizedWeight = getInverseValue((int) Math.round(normalizedValue));
		}
	}
}
