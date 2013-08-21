package net.anotheria.asg.util.filter;

import net.anotheria.anodoc.data.NoSuchPropertyException;
import net.anotheria.anodoc.util.context.ContextManager;
import net.anotheria.asg.data.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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

	/**
	 * {@link Logger} instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(MissingTranslationFilter.class);

	/**
	 * List of filter triggers.
	 */
	private List<FilterTrigger> triggerer;
	/**
	 * Supported languages.
	 */
	private List<String> supportedLanguages;
	/**
	 * Default language.
	 */
	private String defaultLanguage;
	
		
	/**
	 * Default constructor, sets supportedLanguages and defaultLanguage from CallContext
	 * This constructor will be used in generated applications.
	 * If CallContext can not be getted by ContextManager, default language and supported languages will be setted to "EN"
	 */
	public MissingTranslationFilter() {
		try {									
			this.setSupportedLanguages( ContextManager.getCallContext().getSupportedLanguages() );
			this.defaultLanguage = ContextManager.getCallContext().getDefaultLanguage();
		} catch(Exception e) {
			LOGGER.warn("CallContext can not be getted by ContextManager. Setting default language and supported languages to EN", e);
			
			List<String> defaultSupportedLanguages = new ArrayList<String>();
			defaultSupportedLanguages.add("EN");
			this.setSupportedLanguages(defaultSupportedLanguages);
			this.setDefaultLanguage("EN");			
		}
	}
	
	/**
	 * Constructor with parameters. May be used for unit test.
	 * @param supportedLanguages
	 * @param defaultLanguage
	 */
	public MissingTranslationFilter(List<String> supportedLanguages, String defaultLanguage) {
		this.setSupportedLanguages(supportedLanguages);
		this.setDefaultLanguage(defaultLanguage);				
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
	
	/**
	 * Set supported languages. Method update triggers.
	 * NOTE: Default language will not be included into triggers.
	 */
	public void setSupportedLanguages(List<String> supportedLanguages) {
		this.supportedLanguages = supportedLanguages;
		triggerer = new ArrayList<FilterTrigger>();
		triggerer.add(new FilterTrigger("All",""));
		for(String language : supportedLanguages) {
			if(!language.equals(defaultLanguage)) {
				// add all non-default languages
				triggerer.add(new FilterTrigger(language,language));
			}
			// triggerer.add(new FilterTrigger(language.equals(defaultLanguage) ? (language + "*") : language,language));
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
