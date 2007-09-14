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

/* ------------------------------------------------------------------------- *
 * $Log$
 * Revision 1.3  2007/06/07 23:40:19  lrosenberg
 * added db functionality
 *
 * Revision 1.2  2006/12/27 23:47:59  lrosenberg
 * *** empty log message ***
 *
 * Revision 1.1  2006/03/07 16:04:44  lrosenberg
 * *** empty log message ***
 *
 */