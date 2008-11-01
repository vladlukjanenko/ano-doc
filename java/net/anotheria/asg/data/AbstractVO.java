package net.anotheria.asg.data;

import net.anotheria.util.xml.XMLNode;

public abstract class AbstractVO implements DataObject{

	public long getLastUpdateTimestamp() {
		return getDaoUpdated() == 0 ? 
				getDaoCreated() : getDaoUpdated();
	}
	
	public abstract long  getDaoCreated();
	public abstract long  getDaoUpdated();
	public abstract Object clone() throws CloneNotSupportedException;

	public XMLNode toXMLNode(){
		return new XMLNode("NotImplemented "+getId());
	}
	
	public ObjectInfo getObjectInfo(){
		ObjectInfo ret = new ObjectInfo(this);
		ret.setAuthor("none");
		return ret;
	}
	
}
