/* ------------------------------------------------------------------------- *
$Source: /work/cvs/ano-doc/java/net/anotheria/asg/generator/forms/FormsGenerator.java,v $
$Author: lrosenberg $
$Date: 2006/12/28 22:22:04 $
$Revision: 1.3 $


Copyright 2004-2005 by FriendScout24 GmbH, Munich, Germany.
All rights reserved.

This software is the confidential and proprietary information
of FriendScout24 GmbH. ("Confidential Information").  You
shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement
you entered into with FriendScout24 GmbH.
See www.friendscout24.de for details.
** ------------------------------------------------------------------------- */
package net.anotheria.asg.generator.forms;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.AbstractAnoDocGenerator;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.forms.meta.MetaForm;
import net.anotheria.asg.generator.view.JspViewGenerator;
import net.anotheria.asg.generator.view.ModuleActionsGenerator;
import net.anotheria.asg.generator.view.ModuleBeanGenerator;


/**
 * TODO Please remain lrosenberg to comment FormsGenerator.java
 * @author lrosenberg
 * @created on Mar 14, 2005
 */
public class FormsGenerator extends AbstractAnoDocGenerator{
	public void generate(String path, List<MetaForm> forms){
		List<FileEntry> todo = new ArrayList<FileEntry>();
		
		ModuleBeanGenerator beanGenerator = new ModuleBeanGenerator(null);
		JspViewGenerator jspGenerator = new JspViewGenerator();
		ModuleActionsGenerator actionsGenerator = new ModuleActionsGenerator(null);

		GeneratorDataRegistry registry = GeneratorDataRegistry.getInstance();
		
		for (int i=0; i<forms.size(); i++){
			MetaForm form = forms.get(i);
			todo.add(new FileEntry(FileEntry.package2path(ModuleBeanGenerator.getPackage()), ModuleBeanGenerator.getFormBeanName(form), beanGenerator.generateFormBean(form)));
			todo.add(new FileEntry(FileEntry.package2path(ModuleActionsGenerator.getPackage()), actionsGenerator.getFormActionName(form), actionsGenerator.generateFormAction(form)));

			FileEntry formPage = new FileEntry(FileEntry.package2path(registry.getContext().getPackageName()+".jsp"), jspGenerator.getFormIncludePageName(form), jspGenerator.generateFormInclude(form));
			formPage.setType(".jsp");
			todo.add(formPage); 
			
		}
		writeFiles(todo);
	}
	

}

/* ------------------------------------------------------------------------- *
 * $Log: FormsGenerator.java,v $
 * Revision 1.3  2006/12/28 22:22:04  lrosenberg
 * *** empty log message ***
 *
 * Revision 1.2  2006/12/27 23:47:59  lrosenberg
 * *** empty log message ***
 *
 * Revision 1.1  2005/10/20 21:20:12  lro
 * *** empty log message ***
 *
 * Revision 1.5  2005/06/30 00:03:12  lro
 * *** empty log message ***
 *
 * Revision 1.4  2005/03/28 01:12:39  lro
 * *** empty log message ***
 *
 * Revision 1.3  2005/03/15 02:14:47  lro
 * *** empty log message ***
 *
 * Revision 1.2  2005/03/14 20:03:14  lro
 * *** empty log message ***
 *
 * Revision 1.1  2005/03/14 19:31:26  lro
 * *** empty log message ***
 *
 */