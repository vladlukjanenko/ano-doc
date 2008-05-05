package net.anotheria.asg.data;

import net.anotheria.anodoc.data.Document;

/**
 * Root object for all generated classes of type Document (instead of ano-doc Document used previously).
 * @author another
 */
public abstract class AbstractASGDocument extends Document implements DataObject{
	
	protected AbstractASGDocument(String anId){
		super(anId);
	}
	
	protected AbstractASGDocument(AbstractASGDocument toClone){
		super(toClone);
	}

	public ObjectInfo getObjectInfo(){
		ObjectInfo ret = new ObjectInfo(this);
		ret.setAuthor("rfu");
		return ret;
	}
	
}