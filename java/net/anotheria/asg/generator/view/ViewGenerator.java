package net.anotheria.asg.generator.view;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.AbstractAnoDocGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.cms20.view.JspViewCms20Generator;
import net.anotheria.asg.generator.view.meta.MetaCustomSection;
import net.anotheria.asg.generator.view.meta.MetaModuleSection;
import net.anotheria.asg.generator.view.meta.MetaSection;
import net.anotheria.asg.generator.view.meta.MetaView;
import net.anotheria.util.ExecutionTimer;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class ViewGenerator extends AbstractAnoDocGenerator{
	
	
	
	public void generate(String path, List<MetaView> views){
		
		ExecutionTimer timer = new ExecutionTimer("ViewGenerator");
		timer.startExecution("common");
		List<FileEntry> files = new ArrayList<FileEntry>();
		Context context = GeneratorDataRegistry.getInstance().getContext();
		
		//MAF Filter and Mapping generation
//		files.addAll(new CMSFilterGenerator().generate());
//		files.addAll(new CMSMappingsConfiguratorGenerator().generate(views));
		
		//hack, works only with one view.
		files.add(new BaseActionGenerator().generate(views));
		files.add(new IndexPageActionGenerator().generate(views));
		files.add(new IndexPageJspGenerator().generate(context));
		files.add(new SharedJspFooterGenerator().generate(views, context));
		files.add(new JspMenuGenerator().generate(views, context));
		files.addAll(new WebXMLGenerator().generate(views, context));
		// UserSettings
		files.addAll(new UserSettingsBeansGenerator().generate());
		files.addAll(new UserSettingsActionsGenerator().generate());		
		files.add(new UserSettingsJspGenerator().generate());
		
		timer.stopExecution("common");
		
		timer.startExecution("views");
		for (int i=0; i<views.size(); i++){
			MetaView view = views.get(i);
			timer.startExecution("view-"+view.getName());

			timer.startExecution("v-"+view.getName()+"-BaseViewAction");
			files.add(new BaseViewActionGenerator().generate(view));
			timer.stopExecution("v-"+view.getName()+"-BaseViewAction");
			
			timer.startExecution("v-"+view.getName()+"-View");
			files.addAll(generateView(path, view));
			timer.stopExecution("v-"+view.getName()+"-View");

			timer.startExecution("v-"+view.getName()+"-Jsp");
			
			if(view.isCms20())
				files.addAll(new JspViewCms20Generator().generate(view));
			else
				files.addAll(new JspViewGenerator().generate(view));
			
			timer.stopExecution("v-"+view.getName()+"-Jsp");
			
			timer.startExecution("v-"+view.getName()+"-JspQueries");
			files.addAll(new JspQueriesGenerator().generate(view));
			timer.stopExecution("v-"+view.getName()+"-JspQueries");
			
			timer.startExecution("v-"+view.getName()+"-StrutsConfig");
			files.addAll(new StrutsConfigGenerator().generate(view));
			timer.stopExecution("v-"+view.getName()+"-StrutsConfig");
			
			timer.stopExecution("view-"+view.getName());
		}
		// UserSettings
		timer.startExecution("UserSettings-StrutsConfig");
		files.addAll(new StrutsConfigGenerator().generateUserSettingsStrutsConfig());
		timer.stopExecution("UserSettings-StrutsConfig");
		
		timer.stopExecution("views");
		
		writeFiles(files);
		
		//timer.printExecutionTimesOrderedByCreation();
	}
	
	public List<FileEntry> generateView(String path, MetaView view){
		List<FileEntry> ret = new ArrayList<FileEntry>();
		
		List<MetaSection> sections = view.getSections();
		for (int i=0; i<sections.size(); i++){
		    MetaSection section = sections.get(i);
		    boolean recognized = false;
		    if (section instanceof MetaModuleSection){
		        recognized = true;
		        ret.addAll(generateMetaModuleSection(path, (MetaModuleSection)section, view));
		    }
		    
		    if (section instanceof MetaCustomSection){
		    	//custom sections are skipped sofar.
		    	recognized = true;
		    }
		    
		    if (!recognized)
		        throw new RuntimeException("Unsupported section type: "+section+", "+section.getClass());
		}
		
		return ret;
	}
	
	private List<FileEntry> generateMetaModuleSection(String path, MetaModuleSection section, MetaView view){
		List<FileEntry> ret = new ArrayList<FileEntry>();
		ret.addAll(new ModuleBeanGenerator().generate(section));
	    ret.addAll(new ModuleActionsGenerator(view).generate(section));
	    return ret;
	}

}
