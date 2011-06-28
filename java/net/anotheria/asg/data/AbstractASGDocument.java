package net.anotheria.asg.data;

import net.anotheria.anodoc.data.*;

/**
 * Root object for all generated classes of type Document (instead of ano-doc Document used previously).
 * @author another
 */
public abstract class AbstractASGDocument extends Document implements DataObject, LockableObject{
	
	protected static final String INT_PROPERTY_MULTILINGUAL_DISABLED = "ml-disabled";
	
	protected AbstractASGDocument(String anId){
		super(anId);
	}
	
	protected AbstractASGDocument(AbstractASGDocument toClone){
		super(toClone);
	}

	@Override public ObjectInfo getObjectInfo(){
		ObjectInfo ret = new ObjectInfo(this);
		ret.setId(getId());
		ret.setAuthor(getAuthor());
		ret.setLastChangeTimestamp(getLastUpdateTimestamp());
		ret.setFootprint(getFootprint());
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

    @Override
    public boolean isLocked() {
        try {
            return ((BooleanProperty) getInternalProperty(INT_LOCK_PROPERTY_NAME)).getboolean();
        } catch (NoSuchPropertyException e) {
            return false;
        }
    }

    @Override
    public void setLocked(boolean aLock) {
        setInternalProperty(new BooleanProperty(INT_LOCK_PROPERTY_NAME, aLock));
    }

    @Override
    public String getLockerId() {
        try {
            return ((StringProperty) getInternalProperty(INT_LOCKER_ID_PROPERTY_NAME)).getString();
        } catch (NoSuchPropertyException e) {
            return null;
        }
    }

    @Override
    public void setLockerId(String aLockerId) {
        setInternalProperty(new StringProperty(INT_LOCKER_ID_PROPERTY_NAME, aLockerId));
    }

    @Override
    public long getLockingTime() {
        try {
            return ((LongProperty) getInternalProperty(INT_LOCKING_TIME_PROPERTY_NAME)).getlong();
        } catch (NoSuchPropertyException e) {
            return 0;
        }
    }

    @Override
    public void setLockingTime(long aLockTime) {
        setInternalProperty(new LongProperty(INT_LOCKING_TIME_PROPERTY_NAME, aLockTime));
    }
}
