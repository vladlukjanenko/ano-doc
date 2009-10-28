package net.anotheria.asg.data;

import net.anotheria.util.xml.XMLNode;

public abstract class AbstractFederatedVO implements DataObject{
	
	public XMLNode toXMLNode(){
		return new XMLNode("NotImplemented "+getId());
	}
	
	public ObjectInfo getObjectInfo(){
		ObjectInfo ret = new ObjectInfo(this);
		ret.setAuthor("none");
		return ret;
	}
	
	public abstract Object clone() throws CloneNotSupportedException;
	
	@Override public int hashCode(){
		return getId().hashCode();
	}

}
