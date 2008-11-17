package net.anotheria.asg.generator.parser;

import java.io.StringReader;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import net.anotheria.asg.generator.Context;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class XMLContextParser {

	private String content;
	
	public XMLContextParser(String content){
		this.content = content;
	}


	@SuppressWarnings("unchecked")
	public Context parseContext(){
		SAXBuilder reader = new SAXBuilder();
		reader.setValidation(false);
		Context ret = new Context();

		try{
			Document doc = reader.build(new StringReader(content));
			
			Element context = doc.getRootElement();
			ret.setPackageName(context.getChildText("package"));
			ret.setOwner(context.getChildText("owner"));
			ret.setApplicationName(context.getChildText("applicationName"));
			ret.setApplicationURLPath(context.getChildText("applicationURLPath"));
			ret.setServletMapping(context.getChildText("servletMapping"));
			ret.setEncoding(context.getChildText("encoding"));
			try{
				Element languages = context.getChild("languages");
				if (languages!=null)
					ret = parseLanguages(ret, languages);
			}catch(Exception ignored){}
			
			try{
				Element parameters = context.getChild("parameters");
				if (parameters!=null){
					List<Element> params = parameters.getChildren("parameter");
					for (Element e : params){
						ret.addContextParameter(e.getAttributeValue("name"), e.getAttributeValue("value"));
					}
				}
				
			}catch(Exception ignored){}
			
			Element options = context.getChild("options");
			if (options!=null){
				ret.setOptions(OptionsParser.parseOptions(options));
			}
			
		}catch(JDOMException e){
			e.printStackTrace();
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	private Context parseLanguages(Context src, Element languages){
		src.enableMultiLanguageSupport();
		Element supportedLanguages = languages.getChild("supported");
		List<Element> supLangs = supportedLanguages.getChildren("language");
		for (Element e: supLangs){
			src.addLanguage(e.getText());
		}
		
		String defLang = languages.getChild("default").getChildText("language");
		src.setDefaultLanguage(defLang);
		
		return src;
	}

}
