package net.anotheria.asg.generator.forms.meta;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class MetaFormSingleField extends MetaFormField{
	private String title;
	private String type;
	private int size;

	public MetaFormSingleField(String  aName){
		super(aName);
	}

	/**
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param string
	 */
	public void setTitle(String string) {
		title = string;
	}
	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type The type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}

	public String getJavaType(){
		if (type.equals("boolean"))
			return "boolean";
		return "String";
	}
    
	public boolean isSpacer(){
		return type.equals("spacer");
	}

	public boolean isSingle(){
		return true;
	}
    
	public boolean isComplex(){
		return false;
	}
	
	public String toString(){
		return getType()+" "+getName()+" ["+getSize()+"]";
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

}
