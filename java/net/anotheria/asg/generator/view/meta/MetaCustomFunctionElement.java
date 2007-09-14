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

public class MetaCustomFunctionElement extends MetaViewElement{
	
	private String link;
	private String caption;
	
	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public MetaCustomFunctionElement(String name){
		super(name);
	}
}

/* ------------------------------------------------------------------------- *
 * $Log$
 * Revision 1.2  2006/12/27 23:47:59  lrosenberg
 * *** empty log message ***
 *
 * Revision 1.1  2006/03/07 16:04:44  lrosenberg
 * *** empty log message ***
 *
 */