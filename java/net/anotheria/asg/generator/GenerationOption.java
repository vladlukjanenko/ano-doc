package net.anotheria.asg.generator;

/**
 * An option used by the generator to enable or disable some generation features.
 * For example generation of rmi or inmemory services is triggered by generation options.
 * @author another
 *
 */
public class GenerationOption {
	/**
	 * Name of the option.
	 */
	private String name;
	/**
	 * Value of the option.
	 */
	private String value;
	/**
	 * Creates a new GenerationOption.
	 * @param aName
	 * @param aValue
	 */
	public GenerationOption(String aName, String aValue){
		name = aName;
		value = aValue;
	}
	/**
	 * Creates a new GenerationOption with empty name and value.
	 */
	public GenerationOption(){
		this("","");
	}
	
	@Override public String toString(){
		return name+" = "+value;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * Returns true if the options value is equal to 'true'.
	 * @return
	 */
	public boolean isTrue(){
		return value!=null && value.equalsIgnoreCase("true");
	}
	/**
	 * Returns isSet &amp;&amp; !true.
	 * @return
	 */
	public boolean isFalse(){
		return value!=null && !(value.equalsIgnoreCase("true"));
	}
	/**
	 * Returns true if a value is set.
	 * @return
	 */
	public boolean isSet(){
		return value!=null;
	}

}
