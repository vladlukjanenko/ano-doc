package net.anotheria.asg.util.filter;

/**
 * Triggers which control a filter. For example letter A-Z for the DocumentName filter.
 * @author another
 *
 */
public class FilterTrigger implements Cloneable{
	/**
	 * Caption of the trigger.
	 */
	private String caption;
	/**
	 * Parameter assigned to this trigger.
	 */
	private String parameter;
	/**
	 * If true the trigger is currently selected (active).
	 */
	private boolean isSelected;
	
	public FilterTrigger(){
		
	}
	
	public FilterTrigger(String aCaption, String aParameter){
		caption = aCaption;
		parameter = aParameter;
	}
	
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	public boolean isSelected() {
		return isSelected;
	}
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	public String getParameter() {
		return parameter;
	}
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}
	
	@Override public String toString(){
		return "( "+caption+", "+parameter+")";
	}
}
