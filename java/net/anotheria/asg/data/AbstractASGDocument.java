package net.anotheria.asg.data;

import net.anotheria.anodoc.data.Document;

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
