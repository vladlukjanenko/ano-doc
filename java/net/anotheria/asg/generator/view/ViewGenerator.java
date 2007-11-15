package net.anotheria.asg.generator.view;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.AbstractAnoDocGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.view.meta.MetaCustomSection;
import net.anotheria.asg.generator.view.meta.MetaModuleSection;
import net.anotheria.asg.generator.view.meta.MetaSection;
import net.anotheria.asg.generator.view.meta.MetaView;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class ViewGenerator extends AbstractAnoDocGenerator{
	
	
	
	public void generate(String path, List<MetaView> views){
		List<FileEntry> files = new ArrayList<FileEntry>();
		Context context = GeneratorDataRegistry.getInstance().getContext();
		//hack, works only with one view.
		files.add(new BaseActionGenerator().generate(views, context));
		files.add(new SharedJspFooterGenerator().generate(views, context));
		files.add(new JspMenuGenerator().generate(views, context));
		files.addAll(new WebXMLGenerator().generate(views, context));
		
		for (int i=0; i<views.size(); i++){
			MetaView view = views.get(i);
			files.add(new BaseViewActionGenerator().generate(view, context));
			files.addAll(generateView(path, view));
			files.addAll(new JspViewGenerator().generate(view, context));
			files.addAll(new JspQueriesGenerator().generate(view, context));
			//TODO das muss eins nach oben...
			files.addAll(new StrutsConfigGenerator().generate(view, context));
			
		}
		
		writeFiles(files);
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
		ret.addAll(new ModuleBeanGenerator(view).generate(section, GeneratorDataRegistry.getInstance().getContext()));
	    ret.addAll(new ModuleActionsGenerator(view).generate(section, GeneratorDataRegistry.getInstance().getContext()));
	    return ret;
	}

}
