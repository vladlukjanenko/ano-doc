package net.anotheria.asg.generator.parser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.view.meta.MetaFilter;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class XMLFiltersParser {
	private String content;

	public XMLFiltersParser(String content){
		this.content = content;
	}

	@SuppressWarnings("unchecked")
	public List<MetaFilter> parseFilters(){
		SAXBuilder reader = new SAXBuilder();
		reader.setValidation(false);
		List<MetaFilter> ret = new ArrayList<MetaFilter>();

		try{
			Document doc = reader.build(new StringReader(content));
	
			Element root = doc.getRootElement();
			List<Element> filters = root.getChildren("filter");
			for (int i=0; i<filters.size(); i++){
				Element d = filters.get(i);
				ret.add(parseFilter(d));
			}
	
		}catch(JDOMException e){
			e.printStackTrace();
		}
		//System.out.println("Parsed forms: "+ret);
		return ret;
	}
	
	private MetaFilter parseFilter(Element e){
		String name = e.getAttributeValue("name");
		String className = e.getAttributeValue("class");
		return new MetaFilter(name, className);
	}

}
