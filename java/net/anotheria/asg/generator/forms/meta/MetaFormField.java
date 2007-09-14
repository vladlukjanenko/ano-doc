/* ------------------------------------------------------------------------- *
$Source: /work/cvs/ano-doc/java/net/anotheria/asg/generator/forms/meta/MetaFormField.java,v $
$Author: lrosenberg $
$Date: 2006/12/27 23:47:59 $
$Revision: 1.2 $


Copyright 2004-2005 by FriendScout24 GmbH, Munich, Germany.
All rights reserved.

This software is the confidential and proprietary information
of FriendScout24 GmbH. ("Confidential Information").  You
shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement
you entered into with FriendScout24 GmbH.
See www.friendscout24.de for details.
** ------------------------------------------------------------------------- */
package net.anotheria.asg.generator.forms.meta;

/**
 * TODO Please remain lrosenberg to comment MetaFormElement.java
 * @author lrosenberg
 * @created on Mar 14, 2005
 */
public abstract class MetaFormField {
    private String name;
    
    public MetaFormField(String aName){
        name = aName;
    }
    
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    public String toString(){
        return name;
    }
    
    public abstract boolean isSingle();
    
    public abstract boolean isComplex();
    
}

/* ------------------------------------------------------------------------- *
 * $Log: MetaFormField.java,v $
 * Revision 1.2  2006/12/27 23:47:59  lrosenberg
 * *** empty log message ***
 *
 * Revision 1.1  2005/10/20 21:20:12  lro
 * *** empty log message ***
 *
 * Revision 1.1  2005/06/30 00:03:12  lro
 * *** empty log message ***
 *
 * Revision 1.2  2005/06/29 00:02:48  lro
 * *** empty log message ***
 *
 * Revision 1.1  2005/03/14 19:31:26  lro
 * *** empty log message ***
 *
 */