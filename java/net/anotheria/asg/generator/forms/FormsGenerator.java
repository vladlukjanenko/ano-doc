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
import net.anotheria.asg.generator.view.ModuleMafActionsGenerator;
import net.anotheria.asg.generator.view.ModuleMafBeanGenerator;


/**
 * TODO Please remain lrosenberg to comment FormsGenerator.java
 * @author lrosenberg
 * @created on Mar 14, 2005
 */
public class FormsGenerator extends AbstractAnoDocGenerator{
	public void generate(String path, List<MetaForm> forms){
		List<FileEntry> todo = new ArrayList<FileEntry>();
		
		ModuleBeanGenerator beanGenerator = new ModuleBeanGenerator();
		JspViewGenerator jspGenerator = new JspViewGenerator();
		ModuleActionsGenerator actionsGenerator = new ModuleActionsGenerator(null);

		ModuleMafBeanGenerator mafBeanGenerator = new ModuleMafBeanGenerator();
//		JspMafViewGenerator jspMafGenerator = new JspMafViewGenerator();
		ModuleMafActionsGenerator mafActionsGenerator = new ModuleMafActionsGenerator(null);

		GeneratorDataRegistry registry = GeneratorDataRegistry.getInstance();
		
		for (int i=0; i<forms.size(); i++){
			MetaForm form = forms.get(i);
			todo.add(new FileEntry(beanGenerator.generateFormBean(form)));
			todo.add(new FileEntry(actionsGenerator.generateFormAction(form)));
			
			//MAF
//			todo.add(new FileEntry(mafBeanGenerator.generateFormBean(form)));
//			todo.add(new FileEntry(mafActionsGenerator.generateDialogForm(form)));

			FileEntry formPage = new FileEntry(FileEntry.package2path(registry.getContext().getPackageName()+".jsp"), jspGenerator.getFormIncludePageName(form), jspGenerator.generateFormInclude(form));
			formPage.setType(".jsp");
			todo.add(formPage); 
			
		}
		writeFiles(todo);
	}
	

}

