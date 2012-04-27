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

	private void writeVisualBlocks(Element parentNode, VisualStructure visualStructure)
	{
		Element layoutNode = doc.createElement("LayoutNode");

		layoutNode.setAttribute("FrameSourceIndex", "neznam");
		layoutNode.setAttribute("SourceIndex", "neznam");
		layoutNode.setAttribute("DoC", String.valueOf(visualStructure.getDoC()));
		layoutNode.setAttribute("ContainImg", String.valueOf(visualStructure.containImg()));
		layoutNode.setAttribute("IsImg", String.valueOf(visualStructure.isImg()));
		layoutNode.setAttribute("ContainTable", String.valueOf(visualStructure.containTable()));
		layoutNode.setAttribute("ContainP", String.valueOf(visualStructure.containP()));
		layoutNode.setAttribute("TextLen", String.valueOf(visualStructure.getTextLength()));
		layoutNode.setAttribute("LinkTextLen", String.valueOf(visualStructure.getLinkTextLength()));
		layoutNode.setAttribute("DOMCldNum", "neznam");
		layoutNode.setAttribute("FontSize", String.valueOf(visualStructure.getFontSize()));
		layoutNode.setAttribute("FontWeight", String.valueOf(visualStructure.getFontWeight()));
		layoutNode.setAttribute("BgColor", visualStructure.getBgColor());
		layoutNode.setAttribute("ObjectRectLeft", String.valueOf(visualStructure.getX()));
		layoutNode.setAttribute("ObjectRectTop", String.valueOf(visualStructure.getY()));
		layoutNode.setAttribute("ObjectRectWidth", String.valueOf(visualStructure.getWidth()));
		layoutNode.setAttribute("ObjectRectHeight", String.valueOf(visualStructure.getHeight()));
		layoutNode.setAttribute("CID", visualStructure.getId());
		layoutNode.setAttribute("order", "neznam");

		// TODO disable character escaping - not working yet
		// Node pi = doc.createProcessingInstruction(StreamResult.PI_DISABLE_OUTPUT_ESCAPING,"");
		// vipsElement.appendChild(pi);
		//<xsl:text disable-output-escaping="yes">
		if (visualStructure.getChildrenVisualStructures().size() == 0)
		{
			//TODO zjistit podle jake verze VIPS toto delat..
			ElementBox elementBox = visualStructure.getNestedBlocks().get(0).getElementBox();

			if (elementBox == null)
				return;

			layoutNode.setAttribute("SRC", getSource(elementBox.getElement()));
			layoutNode.setAttribute("Content", elementBox.getNode().getTextContent());
		}

		parentNode.appendChild(layoutNode);

		for (VisualStructure child : visualStructure.getChildrenVisualStructures())
			writeVisualBlocks(layoutNode, child);
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
			vipsElement.setAttribute("PageRectTop", String.valueOf(pageViewport.getContentY()));
			vipsElement.setAttribute("PageRectWidth", String.valueOf(pageViewport.getContentWidth()));
			vipsElement.setAttribute("PageRectHeight", String.valueOf(pageViewport.getContentHeight()));
			vipsElement.setAttribute("neworder", "neznam");
			vipsElement.setAttribute("order", String.valueOf(pageViewport.getOrder()));

			doc.appendChild(vipsElement);

			writeVisualBlocks(vipsElement, visualStructure);

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
