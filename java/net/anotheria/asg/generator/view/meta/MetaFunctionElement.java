package net.anotheria.asg.generator.view.meta;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class MetaFunctionElement extends MetaViewElement{
	private String caption;
	
	public MetaFunctionElement(String aName){
		super(aName);
	}
	
	public String getPropertyName(){
		return getName()+"Link";
	}
	

	/**
	 * @return
	 */
	public String getCaption() {
		return caption == null || caption.length() == 0 ? getName() : caption;
	}

	/**
	 * @param string
	 */
	public void setCaption(String string) {
		caption = string;
	}
	
	public String toString(){
		return "Fun: "+getName();
	}

	public boolean isComparable(){
		return false;
	}

}
