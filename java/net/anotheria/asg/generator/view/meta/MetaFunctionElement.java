package net.anotheria.asg.generator.view.meta;

/**
 * A view element which represents an action.
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
	
	@Override public String toString(){
		return "Fun: "+getName();
	}

	@Override public boolean isComparable(){
		return false;
	}

}
