package net.anotheria.asg.util.helper.action.permissions;

/**
 * Current Helper only  checks permission for action on LocableObject.
 * Actually should be used in CMS actions - as additional permission checker.
 *
 * @author: h3llka
 */
public enum LockableObjectActionPermissionCheckHelper {
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
     * If document is locked - only locker - can perorm current operation !!!
     * Otherwise RuntimeException should be thrown.
     */
    update() {
        @Override
        public void checkExecutionPermisson(boolean isLocked, boolean userInAdminRole, String lockerId, String currentUser) {
            if (isLocked && !lockerId.equals(currentUser)) {
                throw new RuntimeException("Permission denied, update action can't be performed, document isLocked by - " + lockerId + ".");
            }
        }},
    /**
     * Delete action.
     * If document is locked - only locker - can perorm current operation !!!
     * Otherwise RuntimeException should be thrown.
     */
    delete() {
        @Override
        public void checkExecutionPermisson(boolean isLocked, boolean userInAdminRole, String lockerId, String currentUser) {
            if (isLocked && !lockerId.equals(currentUser)) {
                throw new RuntimeException("Permission denied, delete action can't be performed, document isLocked by - " + lockerId + ".");
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
     * Lock can't be performed on allready locked objects!
     */
    lock() {
        @Override
        public void checkExecutionPermisson(boolean isLocked, boolean userInAdminRole, String lockerId, String currentUser) {
            if (isLocked) {
                String messageContent = lockerId.equals(currentUser) ? "Can't perform locking operation, document is allready locked by You!" :
                        "Permission denied, lock action can't be performed on allready locked document. Locked by - " + lockerId;
                throw new RuntimeException(messageContent);
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
		public void checkExecutionPermisson(boolean isLocked, boolean userInAdminRole, String lockerId, String currentUser) {
			if (isLocked) {
				if (!(userInAdminRole || lockerId.equals(currentUser)))
					throw new RuntimeException("Permission denied, expected one of those: 'user with - admin role', or  locker: user - " + lockerId);
			} else
				throw new RuntimeException("Permission denied, document is not locked. Can't perform unlocking");
		}
    },

    /**
     * MultilanguageSwitch action.
     * Actualy very similar to Update action.
     * For locked document can be executed only by locker.
     */
    multiLanguageSwitch() {
        @Override
        public void checkExecutionPermisson(boolean isLocked, boolean userInAdminRole, String lockerId, String currentUser) {
            if (isLocked && !lockerId.equals(currentUser)) {
                throw new RuntimeException("Permission denied, multiLanguageSwitch action can't be performed, document isLocked by - " + lockerId + ".");
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
        public void checkExecutionPermisson(boolean isLocked, boolean userInAdminRole, String lockerId, String currentUser) {
            if (isLocked && !lockerId.equals(currentUser)) {
                throw new RuntimeException("Permission denied, multiLanguageSwitch action can't be performed, document isLocked by - " + lockerId + ".");
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
     * If document is locked - only locker - can perorm current operation !!!
     * Otherwise RuntimeException should be thrown.
     */
    containerDelete() {
        @Override
        public void checkExecutionPermisson(boolean isLocked, boolean userInAdminRole, String lockerId, String currentUser) {
            if (isLocked && !lockerId.equals(currentUser)) {
                throw new RuntimeException("Permission denied, containerDelete action can't be performed, document isLocked by - " + lockerId + ".");
            }
        }},

    /**
     * ContainerMove action.
     * In current case - Document updation  will be invoked!!!
     * If document is locked - only locker - can perorm current operation !!!
     * Otherwise RuntimeException should be thrown.
     */
    containerMove() {
        @Override
        public void checkExecutionPermisson(boolean isLocked, boolean userInAdminRole, String lockerId, String currentUser) {
            if (isLocked && !lockerId.equals(currentUser)) {
                throw new RuntimeException("Permission denied, containerMove action can't be performed, document isLocked by - " + lockerId + ".");
            }
        }},

    /**
     * ContainerListAddRow - action.
     * In current case - Document updation  will be invoked!!!
     * If document is locked - only locker - can perorm current operation !!!
     * Otherwise RuntimeException should be thrown.
     */
    containerListAddRow() {
        @Override
        public void checkExecutionPermisson(boolean isLocked, boolean userInAdminRole, String lockerId, String currentUser) {
            if (isLocked && !lockerId.equals(currentUser)) {
                throw new RuntimeException("Permission denied, containerListAddRow action can't be performed, document isLocked by - " + lockerId + ".");
            }
        }
    },

    /**
     * ContainerTableAddAction - action.
     * In current case - Document updation  will be invoked!!!
     * If document is locked - only locker - can perorm current operation !!!
     * Otherwise RuntimeException should be thrown.
     */
    containerTableAddAction() {
        @Override
        public void checkExecutionPermisson(boolean isLocked, boolean userInAdminRole, String lockerId, String currentUser) {
            if (isLocked && !lockerId.equals(currentUser)) {
                throw new RuntimeException("Permission denied, containerTableAddAction action can't be performed, document isLocked by - " + lockerId + ".");
            }
        }
    },

    /**
     * ContainerListQuickAdd - action.
     * In current case - Document updation  will be invoked!!!
     * If document is locked - only locker - can perorm current operation !!!
     * Otherwise RuntimeException should be thrown.
     */
    containerListQuickAdd() {
        @Override
        public void checkExecutionPermisson(boolean isLocked, boolean userInAdminRole, String lockerId, String currentUser) {
            if (isLocked && !lockerId.equals(currentUser)) {
                throw new RuntimeException("Permission denied, containerListQuickAdd action can't be performed, document isLocked by - " + lockerId + ".");
            }
        }
    };

    /**
     * Alows additional permissions check.
     * RuntimeException will be thrown if action can't be performed.
     *
     * @param isLocked        - is object locked
     * @param userInAdminRole - is user in "admin" role, actually for unlock operation only
     * @param lockerId        - lockerId itself
     * @param currentUser     - current user id
     */
    public void checkExecutionPermisson(boolean isLocked, boolean userInAdminRole, String lockerId, String currentUser) {
    }


}
