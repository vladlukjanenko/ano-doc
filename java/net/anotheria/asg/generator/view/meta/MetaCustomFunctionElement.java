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
 * This element allows to specify a custom function element (link or button).
 * @author another
 *
 */
public class MetaCustomFunctionElement extends MetaViewElement{
	/**
	 * Link target of the element.
	 */
	private String link;
	/**
	 * Caption of the link.
	 */
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
	/**
	 * Creates a new MetaCustomFunctionElement.
	 * @param name
	 */
	public MetaCustomFunctionElement(String name){
		super(name);
	}
}

