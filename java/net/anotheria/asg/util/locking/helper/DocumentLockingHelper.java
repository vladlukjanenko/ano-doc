package net.anotheria.asg.util.locking.helper;

import net.anotheria.asg.data.LockableObject;
import net.anotheria.asg.util.locking.exeption.LockingException;

/**
 * Current Helper only  checks permission for action on LockableObject.
 * Actually should be used in CMS actions - as additional permission checker.
 *
 * @author: h3llka
 */
public enum DocumentLockingHelper {
    /**
     * Show action.
     * Actually allowed for all logged in users.
     */
    //TODO: actually not used in Generator!!! Added for further logic change support :)
    show(),
    /**
     * Search action.
     * Actually allowed for all logged in users.
     */
    //TODO: actually not used in Generator!!! Added for further logic change support :)
    search(),
    /**
     * LinksToMe action.
     * Actually allowed for all logged in users.
     */
    //TODO: actually not used in Generator!!! Added for further logic change support :)
    linksToMe(),
    /**
     * Duplicate action.
     * Actually allowed for all logged in users.
     */
    //TODO: actually not used in Generator!!! Added for further logic change support :)
    duplicate(),
    /**
     * Create action.
     * Actually allowed for all logged in users.
     */
    //TODO: actually not used in Generator!!! Added for further logic change support :)
    create(),
    /**
     * Update action.
     * If document is locked - only locker - can perform current operation !!!
     * Otherwise RuntimeException should be thrown.
     */
    update() {
        @Override
        public void checkExecutionPermission(LockableObject document, boolean userInAdminRole, String currentUser) {
            if (document.isLocked() && !document.getLockerId().equals(currentUser)) {
                throw new LockingException(currentUser+ ". Permission denied, update action can't be performed, document isLocked by - " + document.getLockerId() + ".");
            }
        }},
    /**
     * Delete action.
     * If document is locked - only locker - can perform current operation !!!
     * Otherwise RuntimeException should be thrown.
     */
    delete() {
        @Override
        public void checkExecutionPermission(LockableObject document, boolean userInAdminRole, String currentUser) {
            if (document.isLocked() && !document.getLockerId().equals(currentUser)) {
                throw new LockingException(currentUser+ ". Permission denied, delete action can't be performed, document isLocked by - " + document.getLockerId() + ".");
            }
        }},
    /**
     * Edit action.
     * Actually - edit  just opens  edit - view. So  should be allowed for all users.
     */
    //TODO: actually not used in Generator!!! Added for further logic change support :)
    edit(),

    /**
     * New action. - Similar to Create action.
     * Actually allowed for all logged in users.
     */
    //TODO: actually not used in Generator!!! Added for further logic change support :)
    newDoc(),

    /**
     * Lock - action.
     * Lock can't be performed on all ready locked objects!
     */
    lock() {
        @Override
        public void checkExecutionPermission(LockableObject document, boolean userInAdminRole, String currentUser) {
            if (document.isLocked()) {
                String messageContent = document.getLockerId().equals(currentUser) ? currentUser+ ". Can't perform locking operation, document is allready locked by You!" :
                        currentUser+ ". Permission denied, lock action can't be performed on allready locked document. Locked by - " + document.getLockerId();
                throw new LockingException(messageContent);
            }
        }
	},

    /**
     * UnLock - action.
     * UnLock can be performed only on locked document.
     * Only "admin" or locker - can  execute current action.
     */
    unLock() {
        @Override
		public void checkExecutionPermission(LockableObject document, boolean userInAdminRole, String currentUser) {
			if (document.isLocked()) {
				if (!(userInAdminRole || document.getLockerId().equals(currentUser)))
					throw new LockingException(currentUser+ ". Permission denied, expected one of those: 'user with - admin role', or  locker: user - " + document.getLockerId());
			} else
				throw new LockingException(currentUser+ ". Permission denied, document is not locked. Can't perform unlocking");
		}
	},

    /**
     * MultilanguageSwitch action.
     * Actualy very similar to Update action.
     * For locked document can be executed only by locker.
     */
    multiLanguageSwitch() {
        @Override
        public void checkExecutionPermission(LockableObject document, boolean userInAdminRole, String currentUser) {
            if (document.isLocked() && !document.getLockerId().equals(currentUser)) {
                throw new LockingException(currentUser+ ". Permission denied, multiLanguageSwitch action can't be performed, document isLocked by - " + document.getLockerId() + ".");
            }
        }
	},

    /**
     * CopyLang action.
     * Actualy very similar to Update action.
     * For locked document can be executed only by locker.
     */
    copyLang() {
        @Override
        public void checkExecutionPermission(LockableObject document, boolean userInAdminRole, String currentUser) {
            if (document.isLocked() && !document.getLockerId().equals(currentUser)) {
                throw new LockingException(currentUser+ ". Permission denied, copyLang action can't be performed, document isLocked by - " + document.getLockerId() + ".");
            }
        }
	},

    /**
     * VersionInfo action.
     * Can be executed by everyOne!
     */
    //TODO: actually not used in Generator!!! Added for further logic change support :)
    versionInfo(),

    //Container Operations!!!

    /**
     * ContainerDelete action. Similar to Delete action - but actually used  for linked entries deletion.
     * In current case - Document updation  will be invoked!!!
     * If document is locked - only locker - can perform current operation !!!
     * Otherwise RuntimeException should be thrown.
     */
    containerDelete() {
        @Override
        public void checkExecutionPermission(LockableObject document, boolean userInAdminRole, String currentUser) {
            if (document.isLocked() && !document.getLockerId().equals(currentUser)) {
                throw new LockingException(currentUser+ ". Permission denied, containerDelete action can't be performed, document isLocked by - " + document.getLockerId() + ".");
            }
        }},

    /**
     * ContainerMove action.
     * In current case - Document updation  will be invoked!!!
     * If document is locked - only locker - can perform current operation !!!
     * Otherwise RuntimeException should be thrown.
     */
    containerMove() {
        @Override
        public void checkExecutionPermission(LockableObject document, boolean userInAdminRole, String currentUser) {
            if (document.isLocked() && !document.getLockerId().equals(currentUser)) {
                throw new LockingException(currentUser+ ". Permission denied, containerMove action can't be performed, document isLocked by - " + document.getLockerId() + ".");
            }
        }},

    /**
     * ContainerListAddRow - action.
     * In current case - Document updation  will be invoked!!!
     * If document is locked - only locker - can perform current operation !!!
     * Otherwise RuntimeException should be thrown.
     */
    containerListAddRow() {
        @Override
        public void checkExecutionPermission(LockableObject document, boolean userInAdminRole, String currentUser) {
            if (document.isLocked() && !document.getLockerId().equals(currentUser)) {
                throw new LockingException(currentUser+ ". Permission denied, containerListAddRow action can't be performed, document isLocked by - " + document.getLockerId() + ".");
            }
        }
	},

    /**
     * ContainerTableAddAction - action.
     * In current case - Document updation  will be invoked!!!
     * If document is locked - only locker - can perform current operation !!!
     * Otherwise RuntimeException should be thrown.
     */
    containerTableAddAction() {
        @Override
        public void checkExecutionPermission(LockableObject document, boolean userInAdminRole, String currentUser) {
            if (document.isLocked() && !document.getLockerId().equals(currentUser)) {
                throw new LockingException(currentUser+ ". Permission denied, containerTableAddAction action can't be performed, document isLocked by - " + document.getLockerId() + ".");
            }
        }
	},

    /**
     * ContainerListQuickAdd - action.
     * In current case - Document updation  will be invoked!!!
     * If document is locked - only locker - can perform current operation !!!
     * Otherwise RuntimeException should be thrown.
     */
    containerListQuickAdd() {
        @Override
        public void checkExecutionPermission(LockableObject document, boolean userInAdminRole, String currentUser) {
            if (document.isLocked() && !document.getLockerId().equals(currentUser)) {
                throw new LockingException(currentUser+ ". Permission denied, containerListQuickAdd action can't be performed, document isLocked by - " + document.getLockerId() + ".");
            }
        }
	};


    /**
     * Allows additional permissions check.
     * RuntimeException will be thrown if action can't be performed.
     *
     * @param document - actually LockableObject instance
	 * @param userInAdminRole - is user in "admin" role, actually for unlock operation only
	 * @param currentUser     - current user id
	 */
    public void checkExecutionPermission(LockableObject document, boolean userInAdminRole, String currentUser) {
    }


}
