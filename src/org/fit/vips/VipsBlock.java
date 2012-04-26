/*
 * Tomas Popela, xpopel11, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VipsBlock.java
 */

package org.fit.vips;

import java.util.ArrayList;
import java.util.List;

import org.fit.cssbox.layout.Box;
import org.fit.cssbox.layout.ElementBox;
import org.w3c.dom.Element;

public class VipsBlock {

	//rendered Box, that corresponds to DOM element
	private Box _box = null;
	//children of this node
	private List<VipsBlock> _children = null;
	//node id
	private int _id = 0;
	//node's Degree Of Coherence
	private int _DoC = 0;

	//number of images in node
	private int _containImg = 0;
	//if node is image
	private boolean _isImg = false;
	//if node is visual block
	private boolean _isVisualBlock = false;
	//if node contains table
	private boolean _containTable = false;
	//number of paragraphs in node
	private int _containP = 0;
	//if node was already divided
	private boolean _alreadyDivided = false;
	//if node can be divided
	private boolean _isDividable = true;

	//length of text in node
	private int _textLen = 0;
	//length of text in links in node
	private int _linkTextLen = 0;

	public VipsBlock() {
		this._children = new ArrayList<VipsBlock>();
	}

	public VipsBlock(int id, VipsBlock node) {
		this._children = new ArrayList<VipsBlock>();
		setId(id);
		addChild(node);
	}

	public void setIsVisualBlock(boolean isVisualBlock)
	{
		_isVisualBlock = isVisualBlock;
		checkProperties();
	}

	public boolean isVisualBlock()
	{
		return _isVisualBlock;
	}

	/**
	 * Checks the properties of visual block
	 */
	private void checkProperties()
	{
		checkIsImg();
		checkContainImg(this);
		checkContainTable(this);
		checkContainP(this);
		countTextLength(this);
		countLinkTextLength(this);
	}

	/**
	 * Checks if visual block is an image.
	 */
	private void checkIsImg()
	{
		if (_box.getNode().getNodeName().equals("img"))
			_isImg = true;
		else
			_isImg = false;
	}

	/**
	 * Checks if visual block contains image.
	 * @param vipsBlock Visual block
	 */
	private void checkContainImg(VipsBlock vipsBlock)
	{
		if (vipsBlock.getBox().getNode().getNodeName().equals("img"))
		{
			vipsBlock._isImg = true;
			this._containImg++;
		}

		for (VipsBlock childVipsBlock : vipsBlock.getChildren())
			checkContainImg(childVipsBlock);
	}

	/**
	 * Checks if visual block contains table.
	 * @param vipsBlock Visual block
	 */
	private void checkContainTable(VipsBlock vipsBlock)
	{
		if (vipsBlock.getBox().getNode().getNodeName().equals("table"))
			this._containTable = true;

		for (VipsBlock childVipsBlock : vipsBlock.getChildren())
			checkContainTable(childVipsBlock);
	}

	/**
	 * Checks if visual block contains paragraph.
	 * @param vipsBlock Visual block
	 */
	private void checkContainP(VipsBlock vipsBlock)
	{
		if (vipsBlock.getBox().getNode().getNodeName().equals("p"))
			this._containP++;

		for (VipsBlock childVipsBlock : vipsBlock.getChildren())
			checkContainP(childVipsBlock);
	}

	/**
	 * Counts length of text in links in visual block
	 * @param vipsBlock Visual block
	 */
	private void countLinkTextLength(VipsBlock vipsBlock)
	{
		if (vipsBlock.getBox().getNode().getNodeName().equals("a"))
		{
			int linkLength = vipsBlock.getBox().getNode().getTextContent().length();
			System.err.println(_linkTextLen + " + " + linkLength + "  " + vipsBlock.getBox().getNode().getTextContent());
			_linkTextLen += linkLength;

		}

		for (VipsBlock childVipsBlock : vipsBlock.getChildren())
			countLinkTextLength(childVipsBlock);
	}

	/**
	 * Count length of text in visual block
	 * @param vipsBlock Visual block
	 */
	private void countTextLength(VipsBlock vipsBlock)
	{
		// TODO V ceske verzi www.fit.vutbr.cz je v orginalnim VIPS delka 3802, u me 3810
		_textLen = vipsBlock.getBox().getNode().getTextContent().replaceAll("\n", "").length();
	}

	public void addChild(VipsBlock child)
	{
		_children.add(child);
	}

	public List<VipsBlock> getChildren()
	{
		return _children;
	}

	public void setBox(Box box)
	{
		this._box = box;
	}

	public Box getBox()
	{
		return _box;
	}

	public ElementBox getElementBox()
	{
		if (_box instanceof ElementBox)
			return (ElementBox) _box;
		else
			return null;
	}

	public void setId(int id)
	{
		this._id = id;
	}

	public int getId()
	{
		return _id;
	}

	/**
	 * @return the _DoC
	 */
	public int getDoC()
	{
		return _DoC;
	}

	/**
	 * @param _DoC
	 *            the _DoC to set
	 */
	public void setDoC(int doC)
	{
		this._DoC = doC;
	}

	/**
	 * @return the _isDividable
	 */
	public boolean isDividable()
	{
		return _isDividable;
	}

	/**
	 * @param _isDividable the _isDividable to set
	 */
	public void setIsDividable(boolean isDividable)
	{
		this._isDividable = isDividable;
	}

	/**
	 * @return the _alreadyDivided
	 */
	public boolean isAlreadyDivided()
	{
		return _alreadyDivided;
	}

	/**
	 * @param _alreadyDivided the _alreadyDivided to set
	 */
	public void setAlreadyDivided(boolean alreadyDivided)
	{
		this._alreadyDivided = alreadyDivided;
	}

	public boolean isImg()
	{
		return _isImg;
	}

	public int containImg()
	{
		return _containImg;
	}

	public boolean containTable()
	{
		return _containTable;
	}

	public int getTextLength()
	{
		return _textLen;
	}

	public int getLinkTextLength()
	{
		return _linkTextLen;
	}

	public int containP()
	{
		return _containP;
	}

	private void findBgColor(Element element, String bgColor)
	{
		String backgroundColor = element.getAttribute("background-color");

		if (backgroundColor.isEmpty())
		{
			if (element.getParentNode() != null &&
					!(element.getParentNode() instanceof org.apache.xerces.dom.DeferredDocumentImpl))
				findBgColor((Element) element.getParentNode(), bgColor);
			else
				bgColor = "#ffffff";
		}
		else
			bgColor = backgroundColor;
	}

	public String getBgColor()
	{
		String backgroundColor = this.getElementBox().getStylePropertyValue("background-color");

		if (backgroundColor.isEmpty())
			findBgColor(this.getElementBox().getElement(), backgroundColor);

		return backgroundColor;
	}

	public int getFontSize()
	{
		return this.getElementBox().getVisualContext().getFont().getSize();
	}

	public String getFontWeight()
	{
		String fontWeight = "";

		if (this.getElementBox().getStylePropertyValue("font-weight") == null)
			return fontWeight;

		fontWeight = this.getElementBox().getStylePropertyValue("font-weight");

		if (fontWeight.isEmpty())
			fontWeight = "normal";

		return fontWeight;
	}
}
