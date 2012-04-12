package org.fit.vips;

import java.util.ArrayList;
import java.util.List;

import org.fit.cssbox.layout.Box;
import org.fit.cssbox.layout.ElementBox;

public class VisualStructure {

	//rendered Box, that corresponds to DOM element
	private Box _box = null;
	//children of this node
	private List<VisualStructure> _children = null;
	//node id
	private int _id = 0;
	//node's Degree Of Coherence
	private int _DoC = 0;

	//if node contains image
	private boolean _containImg = false;
	//if node is image
	private boolean _isImg = false;
	//if node is visual block
	private boolean _isVisualBlock = false;
	//if node contains table
	private boolean _containTable = false;
	//if node contains paragraph
	private boolean _containP = false;
	//if node was already divided
	private boolean _alreadyDivided = false;
	//if node can be divided
	private boolean _isDividable = true;
	
	//length of text in node
	private int _textLen = 0;
	//length of text in links in node
	private int _linkTextLen = 0;
	
	public VisualStructure() {
		this._children = new ArrayList<VisualStructure>();
	}

	public VisualStructure(int id, VisualStructure node) {
		this._children = new ArrayList<VisualStructure>();
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
	 * Checks if visual structure is image.
	 */
	private void checkIsImg()
	{
		if (_box.getNode().getNodeName().equals("img"))
			_isImg = true;
		else
			_isImg = false;
	}

	/**
	 * Checks if visual structure contains image.
	 * @param visualStucture Visual structure
	 */
	private void checkContainImg(VisualStructure visualStucture)
	{
		if (visualStucture.getBox().getNode().getNodeName().equals("img"))
		{
			visualStucture._isImg = true;
			this._containImg = true;
		}
		
		for (VisualStructure childVisualStructure : visualStucture.getChilds())
			checkContainImg(childVisualStructure);
	}
	
	/**
	 * Checks if visual structure contains table.
	 * @param visualStucture Visual structure
	 */
	private void checkContainTable(VisualStructure visualStucture)
	{
		if (visualStucture.getBox().getNode().getNodeName().equals("table"))
			this._containTable = true;
		
		for (VisualStructure childVisualStructure : visualStucture.getChilds())
			checkContainTable(childVisualStructure);
	}
	
	/**
	 * Checks if visual structure contains paragraph.
	 * @param visualStucture Visual structure
	 */
	private void checkContainP(VisualStructure visualStucture)
	{
		if (visualStucture.getBox().getNode().getNodeName().equals("p"))
			this._containP = true;
		
		for (VisualStructure childVisualStructure : visualStucture.getChilds())
			checkContainP(childVisualStructure);
	}
	
	/**
	 * Counts length of text in links in visual block
	 * @param visualStucture Visual structure
	 */
	private void countLinkTextLength(VisualStructure visualStucture)
	{
		if (visualStucture.getBox().getNode().getNodeName().equals("a"))
		{
			int linkLength = visualStucture.getBox().getNode().getTextContent().length();
			System.out.println(_linkTextLen + " + " + linkLength + "  " + visualStucture.getBox().getNode().getTextContent());
			_linkTextLen += linkLength;
			
		}
		
		for (VisualStructure childVisualStructure : visualStucture.getChilds())
			countLinkTextLength(childVisualStructure);
	}
	
	/**
	 * Count length of text in visual block
	 * @param visualStucture Visual structure
	 */
	private void countTextLength(VisualStructure visualStucture)
	{
		// TODO V ceske verzi www.fit.vutbr.cz je v orginalnim VIPS delka 3802, u me 3810
		_textLen = visualStucture.getBox().getNode().getTextContent().replaceAll("\n", "").length();
	}
	
	public void addChild(VisualStructure child)
	{
		_children.add(child);
	}

	public List<VisualStructure> getChilds()
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
	
	public boolean containImg()
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
	
	public boolean containP()
	{
		return _containP;
	}
	
	public String getBgColor()
	{
		if (this.getElementBox().getBgcolor() != null)
			return this.getElementBox().getBgcolor().toString();
		
		return this.getElementBox().getStylePropertyValue("background-color").toString();
	}
	
	public String getFontSize()
	{
		//TODO osetreni pokud je elementBox vlastne textBox
		//if (this.getElementBox().getStylePropertyValue("font-size") == null)
//			return "";
		
		//return this.getElementBox().getStylePropertyValue("font-size").replace("px", "");
		return String.valueOf(this.getElementBox().getVisualContext().getFont().getSize());
	}
	
	public String getFontWeight()
	{
		//return this.getElementBox().getVisualContext().getFontVariant();
		
		if (this.getElementBox().getStylePropertyValue("font-weight") == null)
			return "";
	
		return this.getElementBox().getStylePropertyValue("font-weight").toString();
	}
		
}
