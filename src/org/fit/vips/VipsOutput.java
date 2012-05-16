/*
 * Tomas Popela, xpopel11, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VipsOutput.java
 */

package org.fit.vips;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.fit.cssbox.layout.ElementBox;
import org.fit.cssbox.layout.Viewport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class VipsOutput {

	private Document doc = null;
	private boolean _escapeOutput = true;
	private boolean _includeBlocks = false;
	private int _pDoC = 0;
	private int _order = 1;

	public VipsOutput() {
	}

	public VipsOutput(int pDoC) {
		this.setPDoC(pDoC);
	}

	private String getSource(Element node)
	{
		String content = "";
		try
		{
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			StringWriter buffer = new StringWriter();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(new DOMSource(node), new StreamResult(buffer));
			content = buffer.toString().replaceAll("\n", "");
		} catch (TransformerException e) {
			e.printStackTrace();
		}

		return content;
	}

	private void writeVisualBlocks(Element parentNode, VisualStructure visualStructure)
	{
		Element layoutNode = doc.createElement("LayoutNode");

		layoutNode.setAttribute("FrameSourceIndex", String.valueOf(visualStructure.getFrameSourceIndex()));
		layoutNode.setAttribute("SourceIndex", visualStructure.getSourceIndex());
		layoutNode.setAttribute("DoC", String.valueOf(visualStructure.getDoC()));
		layoutNode.setAttribute("ContainImg", String.valueOf(visualStructure.containImg()));
		layoutNode.setAttribute("IsImg", String.valueOf(visualStructure.isImg()));
		layoutNode.setAttribute("ContainTable", String.valueOf(visualStructure.containTable()));
		layoutNode.setAttribute("ContainP", String.valueOf(visualStructure.containP()));
		layoutNode.setAttribute("TextLen", String.valueOf(visualStructure.getTextLength()));
		layoutNode.setAttribute("LinkTextLen", String.valueOf(visualStructure.getLinkTextLength()));
		layoutNode.setAttribute("DOMCldNum", String.valueOf(visualStructure.getChildrenVisualStructures().size()));
		layoutNode.setAttribute("FontSize", String.valueOf(visualStructure.getFontSize()));
		layoutNode.setAttribute("FontWeight", String.valueOf(visualStructure.getFontWeight()));
		layoutNode.setAttribute("BgColor", visualStructure.getBgColor());
		layoutNode.setAttribute("ObjectRectLeft", String.valueOf(visualStructure.getX()));
		layoutNode.setAttribute("ObjectRectTop", String.valueOf(visualStructure.getY()));
		layoutNode.setAttribute("ObjectRectWidth", String.valueOf(visualStructure.getWidth()));
		layoutNode.setAttribute("ObjectRectHeight", String.valueOf(visualStructure.getHeight()));
		layoutNode.setAttribute("ID", visualStructure.getId());
		layoutNode.setAttribute("order", String.valueOf(_order));

		_order++;

		if (_pDoC >= visualStructure.getDoC())
		{
			if (_includeBlocks)
			{
				if (visualStructure.getChildrenVisualStructures().size() == 0)
				{
					if (visualStructure.getNestedBlocks().size() > 0)
					{
						int numberOfBlock = 1;
						for (VipsBlock block : visualStructure.getNestedBlocks())
						{
							ElementBox elementBox = block.getElementBox();

							if (elementBox == null)
								continue;

							String src = "";
							String content = "";

							if (!elementBox.getNode().getNodeName().equals("Xdiv") &&
									!elementBox.getNode().getNodeName().equals("Xspan"))
								src += getSource(elementBox.getElement());
							else
								src += elementBox.getText();

							content += elementBox.getText() + " ";

							layoutNode.setAttribute("innerHTML" + numberOfBlock, src);
							layoutNode.setAttribute("innerText" + numberOfBlock, content);
							layoutNode.setAttribute("textLength" + numberOfBlock, String.valueOf(content.length()));

							numberOfBlock++;
						}
					}
				}
			}
			else
			{
				if (visualStructure.getChildrenVisualStructures().size() == 0)
				{
					if (visualStructure.getNestedBlocks().size() > 0)
					{
						String src = "";
						String content = "";
						for (VipsBlock block : visualStructure.getNestedBlocks())
						{
							ElementBox elementBox = block.getElementBox();

							if (elementBox == null)
								continue;

							if (!elementBox.getNode().getNodeName().equals("Xdiv") &&
									!elementBox.getNode().getNodeName().equals("Xspan"))
								src += getSource(elementBox.getElement());
							else
								src += elementBox.getText();

							content += elementBox.getText() + " ";

						}
						layoutNode.setAttribute("SRC", src);
						layoutNode.setAttribute("Content", content);
					}
				}
			}

			parentNode.appendChild(layoutNode);

			for (VisualStructure child : visualStructure.getChildrenVisualStructures())
				writeVisualBlocks(layoutNode, child);
		}
		else
		{
			if (visualStructure.getNestedBlocks().size() > 0)
			{
				String src = "";
				String content = "";
				for (VipsBlock block : visualStructure.getNestedBlocks())
				{
					ElementBox elementBox = block.getElementBox();

					if (elementBox == null)
						continue;

					if (!elementBox.getNode().getNodeName().equals("Xdiv") &&
							!elementBox.getNode().getNodeName().equals("Xspan"))
						src += getSource(elementBox.getElement());
					else
						src += elementBox.getText();

					content += elementBox.getText() + " ";

				}
				layoutNode.setAttribute("SRC", src);
				layoutNode.setAttribute("Content", content);
			}

			parentNode.appendChild(layoutNode);
		}
	}

	public void writeXML(VisualStructure visualStructure, Viewport pageViewport)
	{
		try
		{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			doc = docBuilder.newDocument();
			Element vipsElement = doc.createElement("VIPSPage");

			String pageTitle = pageViewport.getRootElement().getOwnerDocument().getElementsByTagName("title").item(0).getTextContent();

			vipsElement.setAttribute("Url", pageViewport.getRootBox().getBase().toString());
			vipsElement.setAttribute("PageTitle", pageTitle);
			vipsElement.setAttribute("WindowWidth", String.valueOf(pageViewport.getContentWidth()));
			vipsElement.setAttribute("WindowHeight", String.valueOf(pageViewport.getContentHeight()));
			vipsElement.setAttribute("PageRectTop", String.valueOf(pageViewport.getAbsoluteContentY()));
			vipsElement.setAttribute("PageRectLeft", String.valueOf(pageViewport.getAbsoluteContentX()));
			vipsElement.setAttribute("PageRectWidth", String.valueOf(pageViewport.getContentWidth()));
			vipsElement.setAttribute("PageRectHeight", String.valueOf(pageViewport.getContentHeight()));
			vipsElement.setAttribute("neworder", "0");
			vipsElement.setAttribute("order", String.valueOf(pageViewport.getOrder()));

			doc.appendChild(vipsElement);

			writeVisualBlocks(vipsElement, visualStructure);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);

			if (_escapeOutput)
			{
				StreamResult result = new StreamResult(new File("VIPSResult.xml"));
				transformer.transform(source, result);
			}
			else
			{
				StringWriter writer = new StringWriter();
				transformer.transform(source, new StreamResult(writer));
				String result = writer.toString();

				result = result.replaceAll("&gt;", "<");
				result = result.replaceAll("&lt;", ">");
				result = result.replaceAll("&quot;", "\"");

				FileWriter fstream = new FileWriter("VIPSResult.xml");
				fstream.write(result);
			}
		}
		catch (Exception e)
		{
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void setEscapeOutput(boolean value)
	{
		_escapeOutput = value;
	}

	public void setIncludeBlocks(boolean value)
	{
		_includeBlocks = value;
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
}
