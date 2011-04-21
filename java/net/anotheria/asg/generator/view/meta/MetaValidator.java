package net.anotheria.asg.generator.view.meta;

import java.util.ArrayList;
import java.util.List;

public class MetaValidator implements Cloneable {
	
	/** Array containing classNames of validators that have custom handling in ano-maf*/
	private static List<String> predefinedMafValidators = new ArrayList<String>();
	static {
		predefinedMafValidators.add("net.anotheria.maf.validation.annotations.ValidateNotEmpty");
		predefinedMafValidators.add("net.anotheria.maf.validation.annotations.ValidateNumber");
	}
	/**
	 * The name of a validator.
	 */
	private String name;
	/**
	 * The class name of the validator.
	 */
	private String className;
	/**
	 * The key to look error text under.
	 */
	private String key;
	/**
	 * The default error message if localized is not present.
	 */
	private String defaultError;
	/**
	 * JS code (optional) that can validate value on client-side.
	 */
	private String jsValidation;

	/**
	 * Creates a new metavalidator.
	 */
	public MetaValidator(){
		
	}
	/**
	 * Creates a new meta metavalidator.
	 * @param aName
	 * @param aClassName
	 */
	public MetaValidator(String aName, String aClassName){
		name = aName;
		className = aClassName;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getClassName() {
		return className;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getDefaultError() {
		return defaultError;
	}
	
	public void setDefaultError(String defaultError) {
		this.defaultError = defaultError;
	}
	
	@Override public Object clone(){
		try{
			return super.clone();
		}catch(Exception e){
			throw new AssertionError("Can't happen.");
		}
		
	}

	@Override public String toString(){
		return name+" = "+className;
	}
	
	@Override public boolean equals(Object o){
		return o instanceof MetaValidator ?
			((MetaValidator)o).getName().equals(name) : false; 
	}
	
	@Override public int hashCode(){
		return name == null ? 0 : name.hashCode();
	}
	
	/**
	 * Returns the name of the class of the metaValidator without a package.
	 * @return
	 */
	public String getClassNameOnly(){
		if (className.lastIndexOf('.')==-1)
			return className;
		return className.substring(className.lastIndexOf('.')+1);
	}
	
	/** Returns true if form field should get @ValidateCustom() annotation, e.g. custom project validator */
	public boolean isCustomValidator() {
		return !predefinedMafValidators.contains(className);
	}
	public boolean isNumericValidator() {
		return className.equals("net.anotheria.maf.validation.annotations.ValidateNumber");
	}
	public void setJsValidation(String jsValidation) {
		this.jsValidation = jsValidation;
	}
	public String getJsValidation() {
		return jsValidation;
	}
	
}
