/* ------------------------------------------------------------------------- *
$Source$
$Author$
$Date$
$Revision$


Copyright 2004-2005 by FriendScout24 GmbH, Munich, Germany.
All rights reserved.

This software is the confidential and proprietary information
of FriendScout24 GmbH. ("Confidential Information").  You
shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement
you entered into with FriendScout24 GmbH.
See www.friendscout24.de for details.
** ------------------------------------------------------------------------- */
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
	 * @param oldVersion
	 * @param newVersion
	 */
	void documentUpdated(DataObject oldVersion, DataObject newVersion);
	/**
	 * Called if a document has been deleted.
	 * @param doc
	 */
	void documentDeleted(DataObject doc);
	/**
	 * Called if new document is created.
	 * @param doc
	 */
	void documentCreated(DataObject doc);
}
