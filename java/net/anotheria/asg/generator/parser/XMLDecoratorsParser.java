package net.anotheria.asg.generator.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.view.meta.MetaDecorator;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Parser for the decorator definition file.
 * @author lrosenberg
 */
public final class XMLDecoratorsParser {
	@SuppressWarnings("unchecked")
	public static final List<MetaDecorator> parseDecorators(String content){
		SAXBuilder reader = new SAXBuilder();
		reader.setValidation(false);
		List<MetaDecorator> ret = new ArrayList<MetaDecorator>();

		try{
			Document doc = reader.build(new StringReader(content));
	
			Element root = doc.getRootElement();
			List<Element> decorators = root.getChildren("decorator");
			for (int i=0; i<decorators.size(); i++){
				Element d = decorators.get(i);
				ret.add(parseDecorator(d));
			}
	
		}catch(JDOMException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		//System.out.println("Parsed forms: "+ret);
		return ret;
	}
	
	/**
	 * Parses a single decorator element.
	 * @param e
	 * @return
	 */
	private static final MetaDecorator parseDecorator(Element e){
		String name = e.getAttributeValue("name");
		String className = e.getAttributeValue("class");
		return new MetaDecorator(name, className);
	}

}
