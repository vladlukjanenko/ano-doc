package net.anotheria.asg.util.filter;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import net.anotheria.anodoc.data.NoSuchPropertyException;
import net.anotheria.anodoc.util.context.ContextManager;
import net.anotheria.asg.data.DataObject;

/**
 * Missing Translation Filter. Pass documents if given property:
 * 
 * - multilanguage support is enabled
 * - value for default language (context.xml) is set.
 * - value for selected language (selectable in filter) is not set (empty)
 *
 * Using:
 * Viewdef filter definition examples:
 * 
 * <filter name="MissingTranslation" field="name" />
 * <filter name="MissingTranslation" field="title" />
 * 
 */
public class MissingTranslationFilter implements DocumentFilter{
	
	private static final Logger log = Logger.getLogger(MissingTranslationFilter.class);
	/**
	 * List of filter triggers
	 */
	private List<FilterTrigger> triggerer;
	/**
	 * Supported languages
	 */
	private List<String> supportedLanguages;
	/**
	 * Default language
	 */
	private String defaultLanguage;
	
		
	/**
	 * Default constructor, sets supportedLanguages and defaultLanguage from CallContext
	 * This constructor will be used in generated applications.
	 * If CallContext can not be getted by ContextManager, default language and supported languages will be setted to "EN"
	 */
	public MissingTranslationFilter() {
		try {									
			supportedLanguages = ContextManager.getCallContext().getSupportedLanguages();
			defaultLanguage = ContextManager.getCallContext().getDefaultLanguage();
		} catch(Exception e) {
			supportedLanguages = new ArrayList<String>();
			supportedLanguages.add("EN");
			defaultLanguage = "EN";
			log.warn("CallContext can not be getted by ContextManager. Setting default language and supported languages to EN",e);
			
		}
		
		triggerer = new ArrayList<FilterTrigger>();
		triggerer.add(new FilterTrigger("All",""));		
		for(String language : supportedLanguages) {
			triggerer.add(new FilterTrigger(language.equals(defaultLanguage) ? (language + "*") : language,language));
		}
	}
	
	/**
	 * Constructor with parameters. May be used for unit test  
	 * @param supportedLanguages
	 * @param defaultLanguage
	 */
	public MissingTranslationFilter(List<String> supportedLanguages, String defaultLanguage) {
		this.setSupportedLanguages(supportedLanguages);
		this.setDefaultLanguage(defaultLanguage);
		triggerer = new ArrayList<FilterTrigger>();
		triggerer.add(new FilterTrigger("All",""));
		for(String language : supportedLanguages) {
			triggerer.add(new FilterTrigger(language.equals(defaultLanguage) ? (language + "*") : language,language));
		}
		
	}
	
	@Override public List<FilterTrigger> getTriggerer(String storedFilterParameter) {
		return triggerer;
	}
		
	
	@Override public boolean mayPass(DataObject document, String attributeName, String filterParameter) {
		if (filterParameter == null || filterParameter.length() == 0) {
			return true;
		}
		
		boolean mayPass;
		String propertyValue = "";
		String defaultLanguagePropertyValue = "";
		
		try{
			propertyValue = "" + document.getPropertyValue(attributeName + "_" + filterParameter);				
									
			if (filterParameter.equals(defaultLanguage)) {
				defaultLanguagePropertyValue = propertyValue;
				mayPass = defaultLanguagePropertyValue.isEmpty() || defaultLanguagePropertyValue.equals("null");
			} else {
				defaultLanguagePropertyValue = "" + document.getPropertyValue(attributeName + "_" + defaultLanguage);
				mayPass = (propertyValue.isEmpty() || propertyValue.equals("null") ) 
						&& !defaultLanguagePropertyValue.equals("null") && !defaultLanguagePropertyValue.isEmpty(); 
			}
			
		}catch(NoSuchPropertyException e){
			mayPass = false;
		}catch(Exception e){
			mayPass = false;
		}
		
		
		return mayPass;
	}
	
	
	public void setSupportedLanguages(List<String> supportedLanguages) {
		this.supportedLanguages = supportedLanguages;
		triggerer = new ArrayList<FilterTrigger>();
		triggerer.add(new FilterTrigger("All",""));
		for(String language : supportedLanguages) {
			triggerer.add(new FilterTrigger(language,language));
		}
	}

	public List<String> getSupportedLanguages() {
		return supportedLanguages;
	}

	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}

	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	
}
