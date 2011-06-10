package net.anotheria.asg.util.decorators;

import net.anotheria.anodoc.data.NoSuchPropertyException;
import net.anotheria.asg.data.DataObject;
import net.anotheria.util.StringUtils;

/**
 * This decorator decorates the link to another object with the name property of the linked object.
 * @author lrosenberg
 */
public class EditLinkDecorator implements IAttributeDecorator{

	@Override public String decorate(DataObject obj, String attributeName, String rule) {
	    String docName = obj.getDefinedParentName().toLowerCase()+StringUtils.capitalize(obj.getDefinedName());
		String linkTarget = docName+"Edit?ts="+System.currentTimeMillis()+"&pId="+obj.getId();
		
		String caption = "NoProp";
		try{
			caption = ""+obj.getPropertyValue(attributeName);
		}catch(NoSuchPropertyException e){}
		
		if (linkTarget==null)
			return caption;
		
		return "<a href="+quote(linkTarget)+">"+caption+"</a>";
			
	}
	/**
	 * Surrounds the parameter with double quotes.
	 * @param a
	 * @return
	 */
	private String quote(String a){
		return "\"" + a + "\"";
	}
}
