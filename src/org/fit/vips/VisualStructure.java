package org.fit.vips;

import java.util.ArrayList;
import java.util.List;

import org.fit.cssbox.layout.Box;
import org.fit.cssbox.layout.ElementBox;
import org.fit.cssbox.layout.TextBox;
import org.w3c.dom.Node;

public class VisualStructure {

	private Box _box = null;
	private List<VisualStructure> _children = null;
	private int _id = 0;
	private int _DoC = 0;
	
	private boolean _containImg = false;
	private boolean _isImg = false;
	private boolean _isVisualBlock = false;
	private boolean _containTable = false;
	private boolean _containP = false;
	private boolean _alreadyDivided = false;
	private boolean _isDividable = false;
	private int _textLen = 0;
	private int _linkTextLen = 0;
	
	private int _windowWidth = 0; 
	private int _windowHeight = 0;
	
	public VisualStructure() {
		this._children = new ArrayList<VisualStructure>();
	}

	public VisualStructure(int id, VisualStructure node) {
		this._children = new ArrayList<VisualStructure>();
		setId(id);
		addChild(node);
	}
	
	private void checkProperities()
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
			_linkTextLen += visualStucture.getBox().getText().length();
		
		for (VisualStructure childVisualStructure : visualStucture.getChilds())
			countLinkTextLength(childVisualStructure);
	}
	
	/**
	 * Count length of text in visual block
	 * @param visualStucture Visual structure
	 */
	private void countTextLength(VisualStructure visualStucture)
	{
		if (visualStucture.getBox().getNode().getNodeName().equals("a"))
			_textLen += visualStucture.getBox().getText().length();
		
		for (VisualStructure childVisualStructure : visualStucture.getChilds())
			countTextLength(childVisualStructure);
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
	 * @return the _windowWidth
	 */
	public int getWindowWidth()
	{
		return _windowWidth;
	}

	/**
	 * @param _windowWidth the _windowWidth to set
	 */
	public void setWindowWidth(int windowWidth)
	{
		this._windowWidth = windowWidth;
	}

	/**
	 * @return the _windowHeight
	 */
	public int getWindowHeight()
	{
		return _windowHeight;
	}

	/**
	 * @param _windowHeight the _windowHeight to set
	 */
	public void setWindowHeight(int windowHeight)
	{
		this._windowHeight = windowHeight;
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
	
	public String getFontSize()
	{
		//TODO osetreni pokud je elementBox vlastne textBox
		if (this.getElementBox().getStylePropertyValue("font-size") == null)
			return "";
		
		return this.getElementBox().getStylePropertyValue("font-size").toString();
	}
	
	public String getFontWeight()
	{
		if (this.getElementBox().getStylePropertyValue("font-weight") == null)
			return "";
		
		return this.getElementBox().getStylePropertyValue("font-weight").toString();
	}
		
}
