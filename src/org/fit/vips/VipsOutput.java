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
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class VipsOutput {

	public VipsOutput() {

	}
	
	private String getContent(Element node)
	{
		String content = "";
		try
		{
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			StringWriter buffer = new StringWriter();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				transformer.transform(new DOMSource(node), new StreamResult(buffer));
			content = buffer.toString();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		
		return content;
	}

	public void writeXML(VisualStructure visualStructure, Viewport pageViewport)
	{
		try
		{
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.newDocument();
			Element vipsElement = doc.createElement("VIPSPage");
			
			String pageTitle = pageViewport.getElement().getElementsByTagName("title").item(0).getNodeValue(); 
			
			vipsElement.setAttribute("Url", pageViewport.getBase().toString());
			vipsElement.setAttribute("PageTitle", pageTitle);
			vipsElement.setAttribute("WindowWidth", String.valueOf(pageViewport.getContentWidth()));
			vipsElement.setAttribute("WindowHeight", String.valueOf(pageViewport.getContentHeight()));
			vipsElement.setAttribute("PageRectTop", "neznam");
			vipsElement.setAttribute("PageRectHeight", String.valueOf(pageViewport.getContentWidth()));
			vipsElement.setAttribute("PageRectHeight", String.valueOf(pageViewport.getContentHeight()));
//			vipsElement.setAttribute("PageRectWidth", "840");
//			vipsElement.setAttribute("PageRectHeight", "928");
			vipsElement.setAttribute("neworder", "neznam");
			vipsElement.setAttribute("order", String.valueOf(pageViewport.getOrder()));

			doc.appendChild(vipsElement);
			
			Element layoutNode = doc.createElement("LayoutNode");
			ElementBox elementBox = visualStructure.getElementBox();
			
			if (elementBox == null)
				return;
				//continue;
			
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
			layoutNode.setAttribute("FontSize", visualStructure.getFontSize());
			layoutNode.setAttribute("FontWeight", visualStructure.getFontWeight());
			layoutNode.setAttribute("BgColor", elementBox.getBgcolor().toString());
			//TODO overit tyto miry dole
			layoutNode.setAttribute("ObjectRectLeft", String.valueOf(elementBox.getContentOffsetY()));
			layoutNode.setAttribute("ObjectRectTop", String.valueOf(elementBox.getContentOffsetX()));
			layoutNode.setAttribute("ObjectRectWidth", String.valueOf(elementBox.getContentWidth()));
			layoutNode.setAttribute("ObjectRectHeight", String.valueOf(elementBox.getContentHeight()));
			layoutNode.setAttribute("Content", getContent(elementBox.getElement()));
			layoutNode.setAttribute("CID", "1-1");
			layoutNode.setAttribute("order", String.valueOf(elementBox.getOrder()));

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("VIPSResult.xml"));
	 
			transformer.transform(source, result);
		} 
		catch (ParserConfigurationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 

	}
}
