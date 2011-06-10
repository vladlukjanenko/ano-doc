package net.anotheria.asg.util.decorators;

import net.anotheria.anodoc.data.Document;
import net.anotheria.anodoc.data.NoSuchPropertyException;
import net.anotheria.anodoc.data.StringProperty;

/**
 * This decorator decorates a link value with the linked document name.
 * @author another
 */
public abstract class AbstractLinkDecorator implements IAttributeDecorator{

	/* (non-Javadoc)
	 * @see net.anotheria.asg.util.decorators.IAttributeDecorator#decorate(net.anotheria.anodoc.data.Document, java.lang.String, java.lang.String)
	 */
	public String decorate(Document doc, String attributeName, String rule) {
		try{
			String id = ((StringProperty)doc.getProperty(attributeName)).getString();
			return getName(id)+" ["+id+"]";
		}catch(NoSuchPropertyException e1){
			return "";
		}catch(Exception e){
			return "Unknown ["+e.getMessage()+"]";
		}
	}
	
	protected abstract String getName(String id);

}
