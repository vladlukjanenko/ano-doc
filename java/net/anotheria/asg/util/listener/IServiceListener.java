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

public interface IServiceListener {
	/**
	 * Called if a document has been updated
	 * @param oldVersion
	 * @param newVersion
	 */
	public void documentUpdated(DataObject oldVersion, DataObject newVersion);
	/**
	 * Called if a document has been deleted
	 * @param doc
	 */
	public void documentDeleted(DataObject doc);
	/**
	 * Called if new document is created.
	 * @param doc
	 */
	public void documentCreated(DataObject doc);
}
