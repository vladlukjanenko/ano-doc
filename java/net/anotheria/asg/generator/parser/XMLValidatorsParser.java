package net.anotheria.asg.generator.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.view.meta.MetaValidator;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class XMLValidatorsParser {
	@SuppressWarnings("unchecked")
	public static final List<MetaValidator> parseValidators(String content){
		SAXBuilder reader = new SAXBuilder();
		reader.setValidation(false);
		List<MetaValidator> ret = new ArrayList<MetaValidator>();

		try{
			Document doc = reader.build(new StringReader(content));
	
			Element root = doc.getRootElement();
			List<Element> validators = root.getChildren("validator");
			for (int i=0; i<validators.size(); i++){
				Element d = validators.get(i);
				ret.add(parseValidator(d));
			}
	
		}catch(JDOMException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		return ret;
	}
	
	/**
	 * Parses a single validator element.
	 * @param e
	 * @return
	 */
	private static final MetaValidator parseValidator(Element e){
		MetaValidator result = new MetaValidator();
		result.setName(e.getAttributeValue("name"));
		result.setClassName(e.getAttributeValue("class"));
		result.setKey(e.getAttributeValue("key"));
		result.setDefaultError(e.getAttributeValue("defaultError"));
		result.setJsValidation(e.getChildText("jsValidation"));
		
		return result; 
	}
}
