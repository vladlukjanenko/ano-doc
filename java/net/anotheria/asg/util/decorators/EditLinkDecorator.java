package net.anotheria.asg.util.decorators;

import net.anotheria.asg.data.DataObject;
import net.anotheria.asg.util.decorators.IAttributeDecorator;

/**
 * This decorator changes the name field to the edit link
 * 
 * @author another
 *
 */
public class EditLinkDecorator implements IAttributeDecorator{

	public String decorate(DataObject obj, String attributeName, String rule) {
		String docName = obj.getDefinedName().toLowerCase();//Character.toLowerCase(obj.getDefinedName().charAt(0))+obj.getDefinedName().substring(1);
		String linkTarget = docName+"Edit?ts="+System.currentTimeMillis()+"&pId="+obj.getId();
		
		String caption = ""+obj.getPropertyValue(attributeName);
		
		if (linkTarget==null)
			return caption;
		
		return "<a href="+quote(linkTarget)+">"+caption+"</a>";
			
	}
	
	private String quote(String a){
		return "\"" + a + "\"";
	}
}
