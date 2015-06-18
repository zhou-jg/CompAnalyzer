package person.zhoujg.util;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLUtil {
	public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		return dbf.newDocumentBuilder();
	}
	
	public static String getAttribute(Element element, String attName){
		return element.getAttribute(attName);
	}
	
	public static String getAttribute(Element element, String attName, String aDefault){
		String str = getAttribute(element, attName);
	    return str.equals("") ? aDefault : str;
	}
	
	public static Element getFirstSubElement(Node parent) {
		Element element = null;
		for (Node node = parent.getFirstChild(); node != null
				&& element == null; node = node.getNextSibling()) {
			if (node.getNodeType() == Node.ELEMENT_NODE)
				element = (Element) node;
		}

		return element;
	}
	public static Element getFirstElementByName(Element parent, String name){
		NodeList nl = parent.getElementsByTagName(name);
		Element ele = null;
		if (nl!=null){
			ele = (Element) nl.item(0);
		}
		return ele;
	}
	
	public static Element getFirstSubElementByName(Node parent, String name){
		Element element = null;
		for (Node node = parent.getFirstChild(); node != null && element == null; node = node.getNextSibling()){
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(name)){
				element = (Element)node;
			}
		}
		return element;
	}
	
	public static Element getFirstSubElementByNames(Node parent, String... names){
		Element element = null;
		for (Node node = parent.getFirstChild(); node != null && element == null; node = node.getNextSibling()){
			if (node.getNodeType() == Node.ELEMENT_NODE && contains(node.getNodeName(), names)){
				element = (Element)node;
			}
		}
		return element;
	}
	
	public static List<Element> getSubElementsByNames(Node parent, String... names){
		List<Element> result = new ArrayList<Element>();
		for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()){
			if (node.getNodeType() == Node.ELEMENT_NODE && contains(node.getNodeName(), names)){
				result.add((Element)node);
			}
		}
		return result;
	}
	
	public static boolean contains(String name, String... names){
		for (String s : names){
			if (name.equals(s)){
				return true;
			}
		}
		return false;
	}
	
	public static List<Element> getSubElementsByName(Node parent, String name){
		List<Element> result = new ArrayList<Element>();
		for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()){
			if (node.getNodeType() == Node.ELEMENT_NODE && name.equals(node.getNodeName())){
				result.add((Element)node);
			}
		}
		return result;
	}
	
	public static int getAttributeAsInt(Element ele, String attName, int aDefault){
		String valStr = ele.getAttribute(attName);
		if (valStr != null){
			return Integer.parseInt(valStr);
		}else{
			return aDefault;
		}
	}
	
	public static int getAttributeAsInt(Element ele, String attName){
		String valStr = ele.getAttribute(attName);
		if (StringUtil.isNullOrEmpty(valStr)){
			return 0;
		}else {
			return Integer.parseInt(valStr);
		}
	}
	
	public static boolean getAttributeAsBoolean(Element ele, String attName){
		return Boolean.parseBoolean(ele.getAttribute(attName));
	}
	
	public static boolean getAttributeAsBoolean(Element ele, String attName, boolean aDefault){
		String valStr = ele.getAttribute(attName);
		if (valStr != null){
			if (valStr.equals("true")){
				return true;
			}else if (valStr.equals("false")){
				return false;
			}
			else{
				return aDefault;
			}
		}else{
			return aDefault;
		}		
	}
	
	public static List<Element> getElementsByTagName(Element ele, String tagName){
		NodeList list = ele.getElementsByTagName(tagName);
		List<Element> col = new ArrayList<Element>();
		for (int i = 0; i < list.getLength(); i++){
			col.add((Element)list.item(i));
		}
		return col;
	}
	
	public static String getElementValue(Element ele){
		return ele.getTextContent().trim();
	}
}
