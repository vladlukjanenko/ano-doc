package net.anotheria.asg.util.listener;

import net.anotheria.asg.data.DataObject;
/**
 * A listener which can be configured into a generated service and becomes event on all created/updated/deleted documents (objects).
 * @author another
 *
 */
public interface IServiceListener {
	/**
	 * Called if a document has been updated.
	 * @param oldVersion document previous
	 * @param newVersion document new 
	 */
	void documentUpdated(DataObject oldVersion, DataObject newVersion);
	/**
	 * Called if a document has been deleted.
	 * @param doc actually document
	 */
	void documentDeleted(DataObject doc);
	/**
	 * Called if new document is created.
	 * @param doc actually document
	 */
	void documentCreated(DataObject doc);

    /**
     * called if document has been imported.
     * @param doc actually document
     */
    void documentImported(DataObject doc);

    /**
     * Called if data was changed in persistence.
 	*/
	void persistenceChanged();
}
