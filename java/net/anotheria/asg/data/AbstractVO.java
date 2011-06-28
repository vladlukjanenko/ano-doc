package net.anotheria.asg.data;

import net.anotheria.util.xml.XMLNode;

/**
 * The base class for Value Objects which are DataObjects stored in Databases.
 * @author lrosenberg
 */
public abstract class AbstractVO implements DataObject{

	@Override public long getLastUpdateTimestamp() {
		return getDaoUpdated() == 0 ? 
				getDaoCreated() : getDaoUpdated();
	}
	
	/**
	 * Returns the timestamp in millis of the creation time of this object by the DAO.
	 * @return milliseconds
	 */
	public abstract long  getDaoCreated();
	/**
	 * Returns the timestamp in millis of the last time of this object was been updated by the DAO.
	 * @return milliseconds
	 */
	public abstract long  getDaoUpdated();
	
	@Override public XMLNode toXMLNode(){
		return new XMLNode("NotImplemented "+getId());
	}
	
	@Override public ObjectInfo getObjectInfo(){
		ObjectInfo ret = new ObjectInfo(this);
		ret.setAuthor("none");
		return ret;
	}

	@Override public AbstractVO clone(){
		try{
			return (AbstractVO)super.clone();
		}catch(CloneNotSupportedException e){
			throw new AssertionError("Can not happen");
		}
	}

	@Override public int hashCode(){
		return getId().hashCode();
	}
}
