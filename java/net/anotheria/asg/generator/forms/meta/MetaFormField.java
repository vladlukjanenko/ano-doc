package net.anotheria.asg.generator.forms.meta;

/**
 * Base class for FormFields which are parts of a Form.
 * @author lrosenberg
 * @created on Mar 14, 2005
 */
public abstract class MetaFormField {
	/**
	 * Name of the field.
	 */
    private String name;
    
    public MetaFormField(String aName){
        name = aName;
    }
    
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    @Override public String toString(){
        return name;
    }
    
    public abstract boolean isSingle();
    
    public abstract boolean isComplex();
    
}
