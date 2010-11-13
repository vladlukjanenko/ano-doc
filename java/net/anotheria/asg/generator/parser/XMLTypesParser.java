package net.anotheria.asg.generator.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.types.meta.DataType;
import net.anotheria.asg.generator.types.meta.EnumerationType;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public final class XMLTypesParser {
	@SuppressWarnings("unchecked")
	public static final List<DataType> parseTypes(String content){
		SAXBuilder reader = new SAXBuilder();
		reader.setValidation(false);
		List<DataType> ret = new ArrayList<DataType>();

		try{
			Document doc = reader.build(new StringReader(content));
		
			Element root = doc.getRootElement();
			List<Element> types = root.getChildren();
			for (int i=0; i<types.size(); i++){
				Element elem = types.get(i);
				ret.add(parseType(elem));
			}
		
		}catch(JDOMException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		return ret;
	}
	
	private static final DataType parseType(Element elem){
		String name = elem.getName();
		if ("enumeration".equals(name))
			return parseEnumeration(elem);
		throw new RuntimeException("Unsupported data type: "+name);
	}
	
	@SuppressWarnings("unchecked")
	private static final EnumerationType parseEnumeration(Element elem){
		String name = elem.getAttributeValue("name");
		EnumerationType type = new EnumerationType(name);
		List<Element> children = elem.getChildren("element");
		for (int i=0; i<children.size(); i++){
			Element e = children.get(i);
			type.addValue(e.getText());
		}
		return type;
	}

	private XMLTypesParser(){}
}
