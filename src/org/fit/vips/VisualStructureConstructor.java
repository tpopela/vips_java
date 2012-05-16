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
	private int _iteration = 0;
	private int _pDoC = 5;
	private final int _maxDoC = 11;

	private boolean _graphicsOutput = true;

	public VisualStructureConstructor()
	{
		this._horizontalSeparators = new ArrayList<>();
		this._verticalSeparators = new ArrayList<>();
	}

	public VisualStructureConstructor(int pDoC)
	{
		this._horizontalSeparators = new ArrayList<>();
		this._verticalSeparators = new ArrayList<>();
		setPDoC(pDoC);
	}

	public VisualStructureConstructor(VipsBlock vipsBlocks)
	{
		this._horizontalSeparators = new ArrayList<>();
		this._verticalSeparators = new ArrayList<>();
		this._vipsBlocks = vipsBlocks;
	}

	public VisualStructureConstructor(VipsBlock vipsBlocks, int pDoC)
	{
		this._horizontalSeparators = new ArrayList<>();
		this._verticalSeparators = new ArrayList<>();
		this._vipsBlocks = vipsBlocks;
		setPDoC(pDoC);
	}

	public void setPDoC(int pDoC)
	{
		if (pDoC <= 0 || pDoC> 11)
		{
			System.err.println("pDoC value must be between 1 and 11! Not " + pDoC + "!");
			return;
		}
		else
		{
			_pDoC = pDoC;
		}
	}

	public void setGraphicsOutput(boolean enabled)
	{
		this._graphicsOutput = enabled;
	}

	public void constructVisualStructure()
	{
		_iteration++;

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
			Collections.sort(_horizontalSeparators);

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
			if (_iteration == 1)
				detector.setCleanUpSeparators(true);
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

		List<Separator> allSeparatorsInBlock = new ArrayList<>();
		allSeparatorsInBlock.addAll(_horizontalSeparators);

		for (VisualStructure vs : actualStructure.getChildrenVisualStructures())
		{
			vs.getHorizontalSeparators().clear();
		}

		actualStructure.addHorizontalSeparators(_horizontalSeparators);
	}

	private void constructWithVerticalSeparators(VisualStructure actualStructure)
	{
		// if we have no visual blocks or separators
		if (actualStructure.getNestedBlocks().size() == 0 || _verticalSeparators.size() == 0)
		{
			return;
		}

		VisualStructure leftVisualStructure = null;
		VisualStructure rightVisualStructure =  null;
		List<VipsBlock> nestedBlocks =  null;

		for (Separator separator : _verticalSeparators)
		{
			if (actualStructure.getChildrenVisualStructures().size() == 0)
			{
				leftVisualStructure = new VisualStructure();
				leftVisualStructure.setX(actualStructure.getX());
				leftVisualStructure.setY(actualStructure.getY());
				leftVisualStructure.setHeight(actualStructure.getHeight());
				leftVisualStructure.setWidth((separator.startPoint-1)-actualStructure.getX());
				actualStructure.addChild(leftVisualStructure);

				rightVisualStructure = new VisualStructure();
				rightVisualStructure.setX(separator.endPoint+1);
				rightVisualStructure.setY(actualStructure.getY());
				rightVisualStructure.setHeight(actualStructure.getHeight());
				rightVisualStructure.setWidth((actualStructure.getWidth()+actualStructure.getX()) - separator.endPoint-1);
				actualStructure.addChild(rightVisualStructure);

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
						leftVisualStructure = new VisualStructure();
						leftVisualStructure.setX(childVisualStructure.getX());
						leftVisualStructure.setY(childVisualStructure.getY());
						leftVisualStructure.setHeight(childVisualStructure.getHeight());
						leftVisualStructure.setWidth((separator.startPoint-1)-childVisualStructure.getX());
						int index = actualStructure.getChildrenVisualStructures().indexOf(childVisualStructure);
						actualStructure.addChildAt(leftVisualStructure, index);

						rightVisualStructure = new VisualStructure();
						rightVisualStructure.setX(separator.endPoint+1);
						rightVisualStructure.setY(childVisualStructure.getY());
						rightVisualStructure.setHeight(childVisualStructure.getHeight());
						int width = (childVisualStructure.getWidth()+childVisualStructure.getX())-separator.endPoint-1;
						rightVisualStructure.setWidth(width);
						actualStructure.addChildAt(rightVisualStructure, index+1);

						oldStructure = childVisualStructure;
						break;
					}
				}
				nestedBlocks = oldStructure.getNestedBlocks();
				actualStructure.getChildrenVisualStructures().remove(oldStructure);
			}

			if (leftVisualStructure == null || rightVisualStructure == null)
				return;

			for (VipsBlock vipsBlock : nestedBlocks)
			{
				if (vipsBlock.getBox().getAbsoluteContentX() <= separator.startPoint)
					leftVisualStructure.addNestedBlock(vipsBlock);
				else
					rightVisualStructure.addNestedBlock(vipsBlock);
			}

			leftVisualStructure = null;
			rightVisualStructure = null;
		}

		// set id for visual structures
		int iterator = 1;
		for (VisualStructure visualStructure : actualStructure.getChildrenVisualStructures())
		{
			visualStructure.setId(actualStructure.getId() + "-" + iterator);
			iterator++;
		}

		List<Separator> allSeparatorsInBlock = new ArrayList<>();
		allSeparatorsInBlock.addAll(_verticalSeparators);

		for (VisualStructure vs : actualStructure.getChildrenVisualStructures())
		{
			vs.getVerticalSeparators().clear();
		}

		actualStructure.addVerticalSeparators(_verticalSeparators);
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

	private void findVisualBlocks(VipsBlock vipsBlock, List<VipsBlock> results)
	{
		if (vipsBlock.isVisualBlock())
			results.add(vipsBlock);

		for (VipsBlock child : vipsBlock.getChildren())
		{
			findVisualBlocks(child, results);
		}
	}

	/**
	 * @param vipsBlocks the vipsBlocks to set
	 */
	public void setVipsBlocks(VipsBlock vipsBlocks)
	{
		this._vipsBlocks = vipsBlocks;

		_visualBlocks = new ArrayList<>();
		findVisualBlocks(vipsBlocks, _visualBlocks);

	}

	public List<VipsBlock> getVisualBlocks()
	{
		return _visualBlocks;
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
						if (visualBlock.getBox().getContentHeight() != 0 && visualBlock.getBox().getContentWidth() != 0)
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

		for (VisualStructure child : visualStructure.getChildrenVisualStructures())
		{
			findAllHorizontalSeparators(child, result);
		}
	}

	private void findAllVerticalSeparators(VisualStructure visualStructure, List<Separator> result)
	{
		result.addAll(visualStructure.getVerticalSeparators());

		for (VisualStructure child : visualStructure.getChildrenVisualStructures())
		{
			findAllVerticalSeparators(child, result);
		}
	}

	private void removeDuplicates(List<Separator> separators)
	{
		HashSet<Separator> hashSet = new HashSet<Separator>(separators);
		separators.clear();
		separators.addAll(hashSet);
	}

	private int getDoCValue(int value)
	{
		if (value == 0)
			return _maxDoC;

		return ((_maxDoC + 1) - value);
	}

	/**
	 * Normalizes separators weights with linear normalization
	 */
	public void normalizeSeparators()
	{
		List<Separator> separators = new ArrayList<>();

		getAllSeparators(_visualStructure, separators);
		Separator newSep = new Separator(0, _pageHeight);
		Collections.sort(separators);
		newSep.weight = 29;
		separators.add(newSep);

		double minWeight = separators.get(0).weight;
		double maxWeight = separators.get(separators.size()-1).weight;

		_pDoC = 11;

		for (Separator separator : separators)
		{
			double normalizedValue = (separator.weight - minWeight) / (maxWeight - minWeight) * (_pDoC - 1) + 1;
			separator.normalizedWeight = getDoCValue((int) Math.ceil(normalizedValue));
			System.out.println(separator.startPoint + "\t" + separator.endPoint + "\t" + (separator.endPoint - separator.startPoint + 1) + "\t" + separator.weight + "\t" + separator.normalizedWeight + "\t" + normalizedValue);
		}

		updateDoC(_visualStructure);

		_visualStructure.setDoC(1);
	}

	/**
	 * Updates DoC of all visual structures nodes
	 * @param visualStructure Visual Structure
	 */
	private void updateDoC(VisualStructure visualStructure)
	{
		for (VisualStructure child : visualStructure.getChildrenVisualStructures())
		{
			updateDoC(child);
		}

		visualStructure.updateToNormalizedDoC();
	}
}
