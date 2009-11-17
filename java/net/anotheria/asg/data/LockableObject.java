package net.anotheria.asg.data;

/**
 * Describes Lockable object.
 *
 * @author: h3llka
 */
public interface LockableObject {
    /**
     * LockableObject "locked" property.
     */
    public static final String INT_LOCK_PROPERTY_NAME = "locked";
    /**
     * LockableObject "lockerId" property.
     */
    public static final String INT_LOCKER_ID_PROPERTY_NAME = "lockerId";
    /**
     * LockableObject "lockingTime" property.
     */
    public static final String INT_LOCKING_TIME_PROPERTY_NAME = "lockingTime";

    /**
     * Return true if current  is locked, false otherwise.
     *
     * @return boolean param
     */
    boolean isLocked();

    /**
     * Allows to lock or unlock current.
     *
     * @param aLock boolean lock, unlock
     */
    void setLocked(boolean aLock);

    /**
     * Returns lockerName, actually name of user - who locked current.
     *
     * @return string user name
     */
    String getLockerId();

    /**
     * Allow to modify  lockerId.
     *
     * @param aLockerId actually user name
     */
    void setLockerId(String aLockerId);

    /**
     * Returns locking time.
     *
     * @return long value
     */
    long getLockingTime();

    /**
     * Allows to modify locking time.
     *
     * @param aLockTime long time
     */
    void setLockingTime(long aLockTime);
}
