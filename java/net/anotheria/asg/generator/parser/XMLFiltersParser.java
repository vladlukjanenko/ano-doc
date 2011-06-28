package net.anotheria.asg.generator.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.view.meta.MetaFilter;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Parser for the filters definition.
 * @author another
 */
public final class XMLFiltersParser {

	private XMLFiltersParser(){
	}

	/**
	 * Parses the filter definition.
	 * @param content
	 * @return
	 */
	public static List<MetaFilter> parseFilters(String content){
		SAXBuilder reader = new SAXBuilder();
		reader.setValidation(false);
		List<MetaFilter> ret = new ArrayList<MetaFilter>();

		try{
			Document doc = reader.build(new StringReader(content));
	
			Element root = doc.getRootElement();
			@SuppressWarnings("unchecked")List<Element> filters = root.getChildren("filter");
			for (int i=0; i<filters.size(); i++){
				Element d = filters.get(i);
				ret.add(parseFilter(d));
			}
	
		}catch(JDOMException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		return ret;
	}
	
	private static MetaFilter parseFilter(final Element e){
		String name = e.getAttributeValue("name");
		String className = e.getAttributeValue("class");
		return new MetaFilter(name, className);
	}

}
