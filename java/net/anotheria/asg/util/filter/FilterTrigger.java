package net.anotheria.asg.util.filter;

public class FilterTrigger implements Cloneable{
	private String caption;
	private String parameter;
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
	
	public String toString(){
		return "( "+caption+", "+parameter+")";
	}
}
