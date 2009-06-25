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
 * A Service Listener which simply prints out all created/updated/deleted documents.
 * @author lrosenberg
 */
public class SysOutServiceListener implements IServiceListener{

	@Override public void documentCreated(DataObject doc) {
		System.out.println("Created new document of type: "+doc.getClass()+" : "+doc);
	}

	@Override public void documentDeleted(DataObject doc) {
		System.out.println("Deleted document of type: "+doc.getClass()+" : "+doc);
	}

	@Override public void documentUpdated(DataObject oldVersion, DataObject newVersion) {
		System.out.println("Updated a document of type: "+oldVersion.getClass()+" old: "+oldVersion+" new: "+newVersion);		
	}
}
