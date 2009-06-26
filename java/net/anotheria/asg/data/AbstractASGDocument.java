package net.anotheria.asg.data;

import net.anotheria.anodoc.data.Document;
import net.anotheria.anodoc.data.Property;

/**
 * Root object for all generated classes of type Document (instead of ano-doc Document used previously).
 * @author another
 */
public abstract class AbstractASGDocument extends Document implements DataObject{
	
	protected static final String INT_PROPERTY_MULTILINGUAL_DISABLED = "ml-disabled";
	
	protected AbstractASGDocument(String anId){
		super(anId);
	}
	
	protected AbstractASGDocument(AbstractASGDocument toClone){
		super(toClone);
	}

	@Override public ObjectInfo getObjectInfo(){
		ObjectInfo ret = new ObjectInfo(this);
		ret.setAuthor("rfu");
		return ret;
	}
	
	protected Property getInternalProperty(String name){
		return getProperty(getInternalPropertyName(name));
	}
	
	protected void setInternalProperty(Property p){
		try{
			Property toPut = p.cloneAs(getInternalPropertyName(p.getId()));
			putProperty(toPut);
		}catch(CloneNotSupportedException e){
			throw new IllegalArgumentException("Property not cloneable: "+p+", clazz: "+p.getClass());
		}
	}

	/**
	 * Returns the name for internally used asg-related property with given name.
	 * @param name
	 * @return
	 */
	private String getInternalPropertyName(String name){
		return "-asg-"+name+"-asg-";
	}
	
}