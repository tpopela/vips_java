/*
 * Tomas Popela, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VipsBlock.java
 */

package org.fit.vips;

import java.util.ArrayList;
import java.util.List;

import org.fit.cssbox.layout.Box;
import org.fit.cssbox.layout.ElementBox;
import org.fit.cssbox.layout.TextBox;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Class that represents block on page.
 * @author Tomas Popela
 *
 */
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

	private String _bgColor = null;

	private int _frameSourceIndex = 0;
	private int _sourceIndex = 0;
	private int _tmpSrcIndex = 0;
	private int _order = 0;


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

	/**
	 * Sets block as visual block
	 * @param isVisualBlock Value
	 */
	public void setIsVisualBlock(boolean isVisualBlock)
	{
		_isVisualBlock = isVisualBlock;
		checkProperties();
	}

	/**
	 * Checks if block is visual block
	 * @return True if block if visual block, otherwise false
	 */
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
		_linkTextLen = 0;
		_textLen = 0;
		countTextLength(this);
		countLinkTextLength(this);
		setSourceIndex(this.getBox().getNode().getOwnerDocument());
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
			_linkTextLen += vipsBlock.getBox().getText().length();

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
		_textLen = vipsBlock.getBox().getText().replaceAll("\n", "").length();
	}

	/**
	 * Adds new child to blocks children
	 * @param child New child
	 */
	public void addChild(VipsBlock child)
	{
		_children.add(child);
	}

	/**
	 * Gets all blocks children
	 * @return List of children
	 */
	public List<VipsBlock> getChildren()
	{
		return _children;
	}

	/**
	 * Sets block corresponding Box
	 * @param box Box
	 */
	public void setBox(Box box)
	{
		this._box = box;
	}

	/**
	 * Gets Box corresponding to the block
	 * @return Box
	 */
	public Box getBox()
	{
		return _box;
	}

	/**
	 * Gets ElementBox corresponding to the block
	 * @return ElementBox
	 */
	public ElementBox getElementBox()
	{
		if (_box instanceof ElementBox)
			return (ElementBox) _box;
		else
			return null;
	}

	/**
	 * Sets block's id
	 * @param id Id
	 */
	public void setId(int id)
	{
		this._id = id;
	}

	/**
	 * Gets blocks id
	 * @return Id
	 */
	public int getId()
	{
		return _id;
	}

	/**
	 * Returns block's degree of coherence DoC
	 * @return Degree of coherence
	 */
	public int getDoC()
	{
		return _DoC;
	}

	/**
	 * Sets block;s degree of coherence
	 * @param doC Degree of coherence
	 */
	public void setDoC(int doC)
	{
		this._DoC = doC;
	}

	/**
	 * Checks if block is dividable
	 * @return True if is dividable, otherwise false
	 */
	public boolean isDividable()
	{
		return _isDividable;
	}

	/**
	 * Sets dividability of block
	 * @param isDividable True if is dividable otherwise false
	 */
	public void setIsDividable(boolean isDividable)
	{
		this._isDividable = isDividable;
	}

	/**
	 * Checks if node was already divided
	 * @return True if was divided, otherwise false
	 */
	public boolean isAlreadyDivided()
	{
		return _alreadyDivided;
	}

	/**
	 * Sets if block was divided
	 * @param alreadyDivided True if block was divided, otherwise false
	 */
	public void setAlreadyDivided(boolean alreadyDivided)
	{
		this._alreadyDivided = alreadyDivided;
	}

	/**
	 * Checks if block is image
	 * @return True if block is image, otherwise false
	 */
	public boolean isImg()
	{
		return _isImg;
	}

	/**
	 * Checks if block contain images
	 * @return Number of images
	 */
	public int containImg()
	{
		return _containImg;
	}

	/**
	 * Checks if block contains table
	 * @return True if block contains table, otherwise false
	 */
	public boolean containTable()
	{
		return _containTable;
	}

	/**
	 * Gets length of text in block
	 * @return Length of text
	 */
	public int getTextLength()
	{
		return _textLen;
	}

	/**
	 * Gets length of text in links in block
	 * @return Length of links text
	 */
	public int getLinkTextLength()
	{
		return _linkTextLen;
	}

	/**
	 * Gets number of paragraphs in block
	 * @return Number of paragraphs
	 */
	public int containP()
	{
		return _containP;
	}

	/**
	 * Finds background color of element
	 * @param element Element
	 */
	private void findBgColor(Element element)
	{
		String backgroundColor = element.getAttribute("background-color");

		if (backgroundColor.isEmpty())
		{
			if (element.getParentNode() != null &&
					!(element.getParentNode() instanceof org.apache.xerces.dom.DeferredDocumentImpl))
				findBgColor((Element) element.getParentNode());
			else
			{
				_bgColor = "#ffffff";
				return;
			}
		}
		else
		{
			_bgColor = backgroundColor;
			return;
		}
	}

	/**
	 * Gets background color of element
	 * @return Background color
	 */
	public String getBgColor()
	{
		if (_bgColor != null)
			return _bgColor;

		if (this.getBox() instanceof TextBox)
		{
			_bgColor = "#ffffff";
		}
		else
		{
			_bgColor = this.getElementBox().getStylePropertyValue("background-color");
		}


		if (_bgColor.isEmpty())
			findBgColor(this.getElementBox().getElement());

		return _bgColor;
	}

	/**
	 * Gets block's font size
	 * @return Font size
	 */
	public int getFontSize()
	{
		return this.getBox().getVisualContext().getFont().getSize();
	}

	/**
	 * Gets block's font weight
	 * @return Font weight
	 */
	public String getFontWeight()
	{
		String fontWeight = "";

		if (this.getBox() instanceof TextBox)
		{
			return fontWeight;
		}

		if (this.getElementBox().getStylePropertyValue("font-weight") == null)
			return fontWeight;

		fontWeight = this.getElementBox().getStylePropertyValue("font-weight");

		if (fontWeight.isEmpty())
			fontWeight = "normal";

		return fontWeight;
	}

	/**
	 * Gets frame source index of block
	 * @return Frame source index
	 */
	public int getFrameSourceIndex()
	{
		return _frameSourceIndex;
	}

	/**
	 * Sets source index of block
	 * @param node Node
	 */
	private void setSourceIndex(Node node)
	{
		if (!this.getBox().getNode().equals(node))
			_tmpSrcIndex++;
		else
			_sourceIndex = _tmpSrcIndex;

		for (int i = 0; i < node.getChildNodes().getLength(); i++)
		{
			setSourceIndex(node.getChildNodes().item(i));
		}
	}

	/**
	 * Gets source index of block
	 * @return Block's source index
	 */
	public int getSourceIndex()
	{
		return _sourceIndex;
	}

	/**
	 * Gets order of block
	 * @return Block's order
	 */
	public int getOrder()
	{
		return _order;
	}

}
