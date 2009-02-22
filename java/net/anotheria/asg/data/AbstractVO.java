package net.anotheria.asg.data;

import net.anotheria.util.xml.XMLNode;

public abstract class AbstractVO implements DataObject{

	public long getLastUpdateTimestamp() {
		return getDaoUpdated() == 0 ? 
				getDaoCreated() : getDaoUpdated();
	}
	
	public abstract long  getDaoCreated();
	public abstract long  getDaoUpdated();
	
	public XMLNode toXMLNode(){
		return new XMLNode("NotImplemented "+getId());
	}
	
	public ObjectInfo getObjectInfo(){
		ObjectInfo ret = new ObjectInfo(this);
		ret.setAuthor("none");
		return ret;
	}

	public AbstractVO clone(){
		try{
			return (AbstractVO)super.clone();
		}catch(CloneNotSupportedException e){
			throw new AssertionError("Can not happen");
		}
	}

	public int hashCode(){
		return getId().hashCode();
	}
}
