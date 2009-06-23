package net.anotheria.asg.generator.parser;

import java.util.List;

import org.jdom.Element;

import net.anotheria.asg.generator.GenerationOption;
import net.anotheria.asg.generator.GenerationOptions;

public final class OptionsParser {
	public static final GenerationOptions parseOptions(Element element){
		GenerationOptions ret = new GenerationOptions();
		
		if (element==null)
			return ret;
		
		@SuppressWarnings("unchecked")
		List<Element> options = (List<Element>)element.getChildren("option");
		
		for (Element option : options){
			ret.set(new GenerationOption(option.getAttributeValue("name"), option.getText()));
		}
		
		return ret;
	}
	
	/**
	 * Prevent instantiation.
	 */
	private OptionsParser(){
		
	}
}
