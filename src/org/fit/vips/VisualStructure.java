/*
 * Tomas Popela, xpopel11, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VisualStructurejava
 */

package org.fit.vips;

import java.util.ArrayList;
import java.util.List;

public class VisualStructure {

	private List<VipsBlock> _nestedBlocks = null;
	private List<VisualStructure> _childrenVisualStructures = null;
	private List<Separator> _horizontalSeparators = null;
	private List<Separator> _verticalSeparators = null;
	private int _width = 0;
	private int _height = 0;
	private int _x = 0;
	private int _y = 0;
	private int _doC = -1;
	private int _containImg = -1;
	private int _containP = -1;
	private int _textLength = -1;
	private int _linkTextLength = -1;
	private boolean _containTable = false;
	private String _id = null;

	public VisualStructure()
	{
		_nestedBlocks = new ArrayList<>();
		_childrenVisualStructures = new ArrayList<>();
		_horizontalSeparators = new ArrayList<>();
		_verticalSeparators = new ArrayList<>();
	}

	/**
	 * @return the _nestedBlocks
	 */
	public List<VipsBlock> getNestedBlocks()
	{
		return _nestedBlocks;
	}

	public void addNestedBlock(VipsBlock vipsBlock)
	{
		this._nestedBlocks.add(vipsBlock);
	}

	public void setNestedBlocks(List<VipsBlock> vipsBlocks)
	{
		this._nestedBlocks = vipsBlocks;
	}

	public void clearNestedBlocks()
	{
		this._nestedBlocks.clear();
	}

	public void removeNestedBlockAt(int index)
	{
		this._nestedBlocks.remove(index);
	}

	public void addChild(VisualStructure visualStructure)
	{
		this._childrenVisualStructures.add(visualStructure);
	}

	public void addChildAt(VisualStructure visualStructure, int index)
	{
		this._childrenVisualStructures.add(index, visualStructure);
	}

	/**
	 * @return the _childrenVisualStructures
	 */
	public List<VisualStructure> getChildrenVisualStructures()
	{
		return _childrenVisualStructures;
	}

	/**
	 * @param _childrenVisualStructures the _childrenVisualStructures to set
	 */
	public void setChildrenVisualStructures(List<VisualStructure> _childrenVisualStructures)
	{
		this._childrenVisualStructures = _childrenVisualStructures;
	}

	/**
	 * @return the _horizontalSeparators
	 */
	public List<Separator> getHorizontalSeparators()
	{
		return _horizontalSeparators;
	}

	/**
	 * @param _horizontalSeparators the _horizontalSeparators to set
	 */
	public void setHorizontalSeparators(List<Separator> _horizontalSeparators)
	{
		this._horizontalSeparators = _horizontalSeparators;
	}

	/**
	 * @param _horizontalSeparators the _horizontalSeparators to set
	 */
	public void addHorizontalSeparator(Separator horizontalSeparator)
	{
		this._horizontalSeparators.add(horizontalSeparator);

	}

	public int getX()
	{
		return this._x;
	}

	public int getY()
	{
		return this._y;
	}

	public void setX(int x)
	{
		this._x = x;
	}

	public void setY(int y)
	{
		this._y = y;
	}

	public void setWidth(int width)
	{
		this._width = width;
	}

	public void setHeight(int height)
	{
		this._height = height;
	}

	public int getWidth()
	{
		return this._width;
	}

	public int getHeight()
	{
		return this._height;
	}

	/**
	 * @return the _verticalSeparators
	 */
	public List<Separator> getVerticalSeparators()
	{
		return _verticalSeparators;
	}

	/**
	 * @param _verticalSeparators the _verticalSeparators to set
	 */
	public void setVerticalSeparators(List<Separator> _verticalSeparators)
	{
		this._verticalSeparators = _verticalSeparators;
	}

	/**
	 * @param _horizontalSeparators the _horizontalSeparators to set
	 */
	public void addVerticalSeparator(Separator verticalSeparator)
	{
		this._verticalSeparators.add(verticalSeparator);
	}

	public void setId(String id)
	{
		this._id = id;
	}

	public String getId()
	{
		return this._id;
	}

	public void setDoC(int doC)
	{
		this._doC = doC;
	}

	public int getDoC()
	{
		if (_doC != -1)
			return _doC;

		_doC = 0;

		for (Separator separator : _horizontalSeparators)
		{
			if (separator.weight > _doC)
				_doC = separator.weight;
		}

		for (Separator separator : _verticalSeparators)
		{
			if (separator.weight > _doC)
				_doC = separator.weight;
		}

		return _doC;
	}

	public int containImg()
	{
		if (_containImg != -1)
			return _containImg;

		_containImg = 0;

		for (VipsBlock vipsBlock : _nestedBlocks)
		{
			_containImg += vipsBlock.containImg();
		}

		return _containImg;
	}

	public int containP()
	{
		if (_containP != -1)
			return _containP;

		_containP = 0;

		for (VipsBlock vipsBlock : _nestedBlocks)
		{
			_containP += vipsBlock.containP();
		}

		return _containP;
	}

	public boolean containTable()
	{
		if (_containTable)
			return _containTable;

		for (VipsBlock vipsBlock : _nestedBlocks)
		{
			if (vipsBlock.containTable())
			{
				_containTable = true;
				break;
			}
		}

		return _containTable;
	}

	public boolean isImg()
	{
		if (_nestedBlocks.size() != 1)
			return false;

		return _nestedBlocks.get(0).isImg();
	}

	public int getTextLength()
	{
		if (_textLength != -1)
			return _textLength;

		_textLength = 0;
		for (VipsBlock vipsBlock : _nestedBlocks)
		{
			_textLength += vipsBlock.getTextLength();
		}

		return _textLength;
	}

	public int getLinkTextLength()
	{
		if (_linkTextLength != -1)
			return _linkTextLength;

		_linkTextLength = 0;
		for (VipsBlock vipsBlock : _nestedBlocks)
		{
			_linkTextLength += vipsBlock.getTextLength();
		}

		return _linkTextLength;
	}

	public int getFontSize()
	{
		if (_nestedBlocks.size() > 0)
			return _nestedBlocks.get(0).getFontSize();
		else
			return -1;
	}

	public String getFontWeight()
	{
		if (_nestedBlocks.size() > 0)
			return _nestedBlocks.get(0).getFontWeight();
		else
			return "undef";
	}

	public String getBgColor()
	{
		if (_nestedBlocks.size() > 0)
			return _nestedBlocks.get(0).getBgColor();
		else
			return "undef";
	}
}
