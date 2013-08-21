package net.anotheria.asg.util.filter;

import net.anotheria.anodoc.data.Document;
import net.anotheria.anodoc.data.NoSuchPropertyException;
import net.anotheria.anodoc.data.Property;
import net.anotheria.anodoc.util.context.ContextManager;
import net.anotheria.asg.data.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Missing Any Translation Filter. Pass documents if any of properties:
 * 
 * - multilanguage support is enabled
 * - value for default language (context.xml) is set.
 * - value for selected language (selectable in filter) is not set (empty)
 *  
 * Using:
 * 
 * The main feature of this filter realization is that attributeName is ignored in mayPass() method,
 * so viewdef filter definition may be not linked with real field, for example:
 * 
 * <filter name="MissingAnyTranslation" field="Any" />
 * or
 * <filter name="MissingAnyTranslation" field="*" />   
 * 
 */
public class MissingAnyTranslationFilter implements DocumentFilter{

	/**
	 * {@link Logger} instance.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(MissingAnyTranslationFilter.class);

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
	 * If CallContext can not be got by ContextManager, default language and supported languages will be setted to "EN"
	 */
	public MissingAnyTranslationFilter() {
		try {									
			this.defaultLanguage = ContextManager.getCallContext().getDefaultLanguage();
			this.setSupportedLanguages( ContextManager.getCallContext().getSupportedLanguages() );			
		} catch(Exception e) {
			LOGGER.warn("CallContext can not be getted by ContextManager. Setting default language and supported languages to EN", e);
			
			this.setDefaultLanguage("EN");
			List<String> defaultSupportedLanguages = new ArrayList<String>();
			defaultSupportedLanguages.add("EN");
			this.setSupportedLanguages(defaultSupportedLanguages);
						
		}
			
	}
	
	/**
	 * Constructor with parameters. May be used for unit test.
	 * @param supportedLanguages
	 * @param defaultLanguage
	 */
	public MissingAnyTranslationFilter(List<String> supportedLanguages, String defaultLanguage) {
		this.setDefaultLanguage(defaultLanguage);
		this.setSupportedLanguages(supportedLanguages);								
	}
	
	@Override public List<FilterTrigger> getTriggerer(String storedFilterParameter) {
		return triggerer;
	}
		
	/*
	 * @param attributeName Ignored, all properties for given attributeName (Language) will be checked
	 */
	@Override public boolean mayPass(DataObject document, String attributeName, String filterParameter) {
		if (filterParameter == null || filterParameter.length() == 0) {
			return true;
		}
		if (!(document instanceof Document)){
			return false;
		}
		
		
		boolean mayPass = false; // Here false mean also that no any multilanguage fields will be founded
		try{
			
			List<Property> propertys = ((Document)document).getProperties();								
			String defaultLanguagePropertyValue;
			boolean languageIsDefault = filterParameter.equals(defaultLanguage);
			String propertyName;
			int charIdx;
			
			// Check property value and default language property value
			for (Property property : propertys) {
				propertyName = property.getId();
				
				charIdx = propertyName.indexOf('_');				
				if (charIdx != -1 
					&& propertyName.equals( propertyName.substring(0,charIdx + 1) + filterParameter )) {						
					
					if (property.getValue() == null || property.getValue().toString().isEmpty()) {
						
						// First empty value was founded
						if (languageIsDefault) {
							// Pass empty default language value if given language equals to default
							mayPass = true;
							break;
						} else {
							// Pass empty value if not empty default language property value in case when given language not equals to default
							defaultLanguagePropertyValue 
								= document.getPropertyValue(property.getId().replace("_"+filterParameter, "_"+defaultLanguage)).toString();
							if (!defaultLanguagePropertyValue.equals("null") && !defaultLanguagePropertyValue.isEmpty()) {								
								mayPass = true;
								break;
							}
						}
						
					} // if empty value
				} // if andsWith
			} // for
			
			
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
