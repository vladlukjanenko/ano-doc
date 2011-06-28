package net.anotheria.asg.generator.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import net.anotheria.asg.generator.Context;
import net.anotheria.util.StringUtils;

/**
 * Parser for the context-xml.
 * @author lrosenberg
 */
public final class XMLContextParser {

	/**
	 * Prevent instantiation.
	 */
	private XMLContextParser(){
	}

	/**
	 * @return parsed generation context.
	 */
	public static Context parseContext(String content){
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
			
			String cmsVersion1Value = context.getChildText("cmsVersion1");
			if(!StringUtils.isEmpty(cmsVersion1Value))
				ret.setCmsVersion1(Boolean.parseBoolean(cmsVersion1Value));
			String cmsVersion2Value = context.getChildText("cmsVersion2");
			if(!StringUtils.isEmpty(cmsVersion2Value))
				ret.setCmsVersion2(Boolean.parseBoolean(cmsVersion2Value));
			
			try{
				Element languages = context.getChild("languages");
				if (languages!=null)
					ret = parseLanguages(ret, languages);
			}catch(Exception ignored){}
			
			try{
				Element parameters = context.getChild("parameters");
				if (parameters!=null){
					@SuppressWarnings("unchecked")List<Element> params = parameters.getChildren("parameter");
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
		}catch(IOException e){
			e.printStackTrace();
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	private static  Context parseLanguages(Context src, Element languages){
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
