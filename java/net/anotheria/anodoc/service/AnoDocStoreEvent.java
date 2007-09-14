package net.anotheria.anodoc.service;

import java.io.Serializable;

/**
 * This event will be sent over channels in case of data change between different 
 * IModuleService instances in order to synchronize the data.
 */
class AnoDocStoreEvent implements Serializable{

	/**
	 * The id of the changed module instance.
	 */
	private String moduleId;
	/**
	 * The id of the owner of the changed module instance.
	 */
	private String ownerId;
	/**
	 * The copy id of the changed module instance.
	 */
	private String copyId;

	/**
	 * The instance id of the IModuleService instance which has made the changes.
	 * Currently the cip id of the underlying cip is used, but this will be changed soon.
	 **/
	private int instanceId;

	/**
	 * @return
	 */
	public String getCopyId() {
		return copyId;
	}

	/**
	 * @return
	 */
	public String getModuleId() {
		return moduleId;
	}

	/**
	 * @return
	 */
	public String getOwnerId() {
		return ownerId;
	}

	/**
	 * @param string
	 */
	public void setCopyId(String string) {
		copyId = string;
	}

	/**
	 * @param string
	 */
	public void setModuleId(String string) {
		moduleId = string;
	}

	/**
	 * @param string
	 */
	public void setOwnerId(String string) {
		ownerId = string;
	}

	/**
	 * @return
	 */
	public int getInstanceId() {
		return instanceId;
	}

	/**
	 * @param i
	 */
	public void setInstanceId(int i) {
		instanceId = i;
	}

}
