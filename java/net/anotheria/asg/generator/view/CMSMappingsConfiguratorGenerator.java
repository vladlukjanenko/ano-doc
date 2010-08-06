package net.anotheria.asg.generator.view;


import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.view.meta.MetaModuleSection;
import net.anotheria.asg.generator.view.meta.MetaSection;
import net.anotheria.asg.generator.view.meta.MetaView;
import net.anotheria.util.StringUtils;

/**
 * Generator class for the CMSFilter.
 * @author dmetelin
 */
public class CMSMappingsConfiguratorGenerator extends AbstractGenerator{
	
	private static enum SectionAction{
		SHOW("Show", false);
		
		private String name;
		private boolean multiop;
		
		private SectionAction(String aName, boolean aMultiop) {
			name = aName;
			multiop = aMultiop;
		}
		
		private String getClassName(MetaModuleSection section){
			return name + section.getDocument().getMultiple()+"MafAction";
		}
		
		private String getMappingName(MetaDocument doc){
			return doc.getParentModule().getName().toLowerCase()+StringUtils.capitalize(doc.getName()) + name;
		}
		
		private String getViewName(MetaDocument doc){
			return name+doc.getMultiple();
		}
		
	}
	
	
	public List<FileEntry> generate(List<MetaView> views) {
		List<FileEntry> ret = new ArrayList<FileEntry>(); 
		try{
			ret.add(new FileEntry(generateCMSMapping(views)));
		}catch(Exception e){
			System.out.println("CMSMappingsConfiguratorGenerator error: " + e.getMessage());
			e.printStackTrace();
		}
		return ret;
	}
	
	
	private GeneratedClass generateCMSMapping(List<MetaView> views){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(getPackageName());
		
		clazz.addImport("net.anotheria.maf.action.ActionForward");
		clazz.addImport("net.anotheria.maf.action.ActionMappings");
		clazz.addImport("net.anotheria.maf.action.ActionMappingsConfigurator");
		clazz.addImport("net.anotheria.maf.action.ActionMappingsConfigurator");


		clazz.addInterface("ActionMappingsConfigurator");
		clazz.setName("CMSMappingsConfigurator");

		startClassBody();
		
		emptyline();
		
		appendString("@Override");
		openFun("public void configureActionMappings()");
		for(MetaView view: views){
			for (MetaSection section: view.getSections()){
				if (!(section instanceof MetaModuleSection))
					continue;
				MetaModuleSection s = (MetaModuleSection)section;
				appendCommentLine("Mapping " + s.getDocument().getName());
				generateSectionMapping(clazz, s);
				emptyline();
			}
		}
		closeBlock("configureActionMappings");
		
		return clazz;
	}
	
	
	private String getPackageName(){
		return GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED) + ".filter";
	}
	
	private void generateSectionMapping(GeneratedClass clazz, MetaModuleSection section){
		MetaModule module = section.getModule();
		MetaDocument doc  = section.getDocument();
	
		String actionsPackage = ModuleActionsGenerator.getPackage(module);
		String jspPath = FileEntry.package2path(JspViewGenerator.getPackage(module)).substring(FileEntry.package2path(JspViewGenerator.getPackage(module)).indexOf('/')) + "/";
		
		
		for(SectionAction action: SectionAction.values()){
			String actionName = action.getClassName(section);
			clazz.addImport(actionsPackage + "." + actionName);
			appendStatement("ActionMappings.addMapping("+ quote(action.getMappingName(doc)) +", "+  actionName +".class, new ActionForward(\"success\"," + quote(jspPath + action.getViewName(doc)+".jsp") + "))");
		}

	}

}