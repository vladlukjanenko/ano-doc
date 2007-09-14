package net.anotheria.asg.generator;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class Context {
	private String packageName;
	private String owner;
	private String applicationName;
	private String servletMapping;
	private String encoding;
	
	private boolean multilanguageSupport;
	
	private List<String> languages;
	private String defaultLanguage;
	
	/**
	 * @return
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @param string
	 */
	public void setPackageName(String string) {
		packageName = string;
	}

    /**
     * @return Returns the owner.
     */
    public String getOwner() {
        return owner;
    }
    /**
     * @param owner The owner to set.
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }
	/**
	 * @return
	 */
	public String getApplicationName() {
		return applicationName;
	}

	/**
	 * @param string
	 */
	public void setApplicationName(String string) {
		applicationName = string;
	}

	/**
	 * @return
	 */
	public String getServletMapping() {
		return servletMapping;
	}

	/**
	 * @param string
	 */
	public void setServletMapping(String string) {
		servletMapping = string;
	}

	/**
	 * @return
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * @param string
	 */
	public void setEncoding(String string) {
		encoding = string;
	}

	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}

	public List<String> getLanguages() {
		return languages;
	}

	public void setLanguages(List<String> languages) {
		this.languages = languages;
	}
	
	public boolean areLanguagesSupported(){
		return multilanguageSupport;
	}
	
	public void enableMultiLanguageSupport(){
		multilanguageSupport = true;
		languages = new ArrayList<String>();
	}
	
	public void addLanguage(String l){
		languages.add(l);
	}

}
