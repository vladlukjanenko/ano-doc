package net.anotheria.asg.generator.parser;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.view.meta.MetaDecorator;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class XMLDecoratorsParser {
	private String content;

	public XMLDecoratorsParser(String content){
		this.content = content;
	}

	@SuppressWarnings("unchecked")
	public List<MetaDecorator> parseDecorators(){
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
		}
		//System.out.println("Parsed forms: "+ret);
		return ret;
	}
	
	private MetaDecorator parseDecorator(Element e){
		String name = e.getAttributeValue("name");
		String className = e.getAttributeValue("class");
		return new MetaDecorator(name, className);
	}

}
