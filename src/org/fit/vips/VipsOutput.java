/*
 * Tomas Popela, xpopel11, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VipsOutput.java
 */

package org.fit.vips;

import java.io.File;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
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

	public VipsOutput() {

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

	private void writeVisualBlocks(Element parent, VipsBlock vipsBlock)
	{
		if (vipsBlock.isVisualBlock())
		{
			Element layoutNode = doc.createElement("LayoutNode");
			ElementBox elementBox = vipsBlock.getElementBox();

			if (elementBox == null)
				return;

			layoutNode.setAttribute("FrameSourceIndex", "neznam");
			layoutNode.setAttribute("SourceIndex", "neznam");
			layoutNode.setAttribute("DoC", String.valueOf(vipsBlock.getDoC()));
			layoutNode.setAttribute("ContainImg", String.valueOf(vipsBlock.containImg()));
			layoutNode.setAttribute("IsImg", String.valueOf(vipsBlock.isImg()));
			layoutNode.setAttribute("ContainTable", String.valueOf(vipsBlock.containTable()));
			layoutNode.setAttribute("ContainP", String.valueOf(vipsBlock.containP()));
			layoutNode.setAttribute("TextLen", String.valueOf(vipsBlock.getTextLength()));
			layoutNode.setAttribute("LinkTextLen", String.valueOf(vipsBlock.getLinkTextLength()));
			layoutNode.setAttribute("DOMCldNum", "neznam");
			layoutNode.setAttribute("FontSize", String.valueOf(vipsBlock.getFontSize()));
			layoutNode.setAttribute("FontWeight", vipsBlock.getFontWeight());
			layoutNode.setAttribute("BgColor", vipsBlock.getBgColor());
			//TODO overit tyto miry dole
			layoutNode.setAttribute("ObjectRectLeft", String.valueOf(elementBox.getAbsoluteContentX()));
			layoutNode.setAttribute("ObjectRectTop", String.valueOf(elementBox.getAbsoluteContentY()));
			layoutNode.setAttribute("ObjectRectWidth", String.valueOf(elementBox.getContentWidth()));
			layoutNode.setAttribute("ObjectRectHeight", String.valueOf(elementBox.getContentHeight()));
			layoutNode.setAttribute("CID", "1-1");
			layoutNode.setAttribute("order", String.valueOf(elementBox.getOrder()));

			// TODO disable character escaping - not working yet
			// Node pi = doc.createProcessingInstruction(StreamResult.PI_DISABLE_OUTPUT_ESCAPING,"");
			// vipsElement.appendChild(pi);
			//<xsl:text disable-output-escaping="yes">
			layoutNode.setAttribute("SRC", getSource(elementBox.getElement()));
			layoutNode.setAttribute("Content", elementBox.getNode().getTextContent());

			parent.appendChild(layoutNode);
		}

		for (VipsBlock childVipsBlock : vipsBlock.getChilds())
			writeVisualBlocks(parent, childVipsBlock);
	}

	public void writeXML(VipsBlock vipsBlock, Viewport pageViewport)
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
			vipsElement.setAttribute("PageRectTop", "neznam");
			vipsElement.setAttribute("PageRectWidth", String.valueOf(pageViewport.getContentWidth()));
			vipsElement.setAttribute("PageRectHeight", String.valueOf(pageViewport.getContentHeight()));
			vipsElement.setAttribute("neworder", "neznam");
			vipsElement.setAttribute("order", String.valueOf(pageViewport.getOrder()));

			doc.appendChild(vipsElement);

			writeVisualBlocks(vipsElement, vipsBlock);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			//transformer.setOutputProperty(Result.PI_DISABLE_OUTPUT_ESCAPING, "yes");
			DOMSource source = new DOMSource(doc);

			StreamResult result = new StreamResult(new File("VIPSResult.xml"));

			transformer.transform(source, result);
		}
		catch (ParserConfigurationException e)
		{
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		} catch (TransformerConfigurationException e)
		{
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		} catch (TransformerException e)
		{
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}


	}
}
