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
package net.anotheria.asg.generator.view.meta;
/**
 * Allows the developer to integrate a custom section into generated frontend.
 * @author another
 *
 */
public class MetaCustomSection extends MetaSection{
	/**
	 * Path to be called whenever the section is selected.
	 */
	private String path;
	
	public MetaCustomSection(String title){
		super(title);
	}
	
	@Override public String toString(){
		return getTitle()+": "+path;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
