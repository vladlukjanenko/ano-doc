package net.anotheria.asg.generator.view;


import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaContainerProperty;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.MetaProperty;
import net.anotheria.asg.generator.meta.StorageType;
import net.anotheria.asg.generator.view.meta.MetaModuleSection;
import net.anotheria.asg.generator.view.meta.MetaSection;
import net.anotheria.asg.generator.view.meta.MetaView;
import net.anotheria.util.StringUtils;

/**
 * Generator class for the CMSFilter.
 * @author dmetelin
 */
public class CMSMappingsConfiguratorGenerator extends AbstractGenerator{
	
	private static enum OperationType{
		SINGLE,
		MULTIPLE_DIALOG,
	}
	
	private static enum SectionAction{
		SHOW("Show", "Show", OperationType.SINGLE, true, false),
		EDIT("Edit", "Edit", OperationType.SINGLE),
		
		CLOSE("Close", "Show", OperationType.MULTIPLE_DIALOG),
		UPDATE("Update", "Show", OperationType.MULTIPLE_DIALOG),
		DELETE("Delete", "Show", OperationType.MULTIPLE_DIALOG),
		DUPLICATE("Duplicate", "Show", OperationType.MULTIPLE_DIALOG),
		LOCK("Lock", "EditBoxDialog", OperationType.MULTIPLE_DIALOG),
		UNLOCK("UnLock", "EditBoxDialog", OperationType.MULTIPLE_DIALOG),
		COPYLANG("CopyLang", "EditBoxDialog", OperationType.MULTIPLE_DIALOG),
		SWITCHMULTILANG("SwitchMultilang", "EditBoxDialog", OperationType.MULTIPLE_DIALOG),
		VERSIONINFO("Versioninfo", "EditBoxDialog", OperationType.MULTIPLE_DIALOG),
		;
		
		private String action;
		private String view;
		private OperationType type;
		private boolean multiDocument;
		private boolean ignoreFederationSections;
		
		private SectionAction(String anAction, String aView, OperationType aType) {
			this(anAction, aView, aType, false, true);
		}
		
		private SectionAction(String anAction, String aView, OperationType aType, boolean aListDocument, boolean anIgnoreFederationSections) {
			action = anAction;
			view = aView;
			type = aType;
			multiDocument = aListDocument;
			ignoreFederationSections = anIgnoreFederationSections;
		}
		
		private String getClassName(MetaModuleSection section) {
			switch (type) {
			case SINGLE:
				return action + section.getDocument().getName(multiDocument) + "MafAction";
			case MULTIPLE_DIALOG:
				return ModuleMafActionsGenerator.getMultiOpDialogActionName(section);
			}
			throw new AssertionError("Unsuported OperationType!");

		}
		
		
		private String getMappingName(MetaDocument doc){
			return doc.getParentModule().getName().toLowerCase()+StringUtils.capitalize(doc.getName()) + action;
		}
		
		private String getViewName(MetaDocument doc){
			return view+doc.getName(multiDocument) + "Maf";
		}

		public boolean isIgnoreForSection(MetaModuleSection section){
			return section.getDialogs().size() == 0 || ignoreFederationSections && StorageType.FEDERATION == section.getModule().getStorageType();
		}
		
	}
	
	private static enum ContainerAction{
		SHOW("Show"),
		DELETE("Delete"),
		;
		
		private String action;
		
		private ContainerAction(String anAction) {
			action = anAction;
		}
		
		public String getClassName(MetaDocument doc, MetaContainerProperty container) {
			return ModuleMafActionsGenerator.getContainerMultiOpActionName(doc, container);
		}
		
		
		public String getMappingName(MetaDocument doc, MetaContainerProperty container){
			return doc.getParentModule().getName().toLowerCase()+StringUtils.capitalize(doc.getName())+StringUtils.capitalize(container.getName())+ action;
		}
		
		public String getViewName(MetaDocument doc, MetaContainerProperty container){
			return JspMafViewGenerator.getContainerPageName(doc, container);
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
				generateSectionMappings(clazz, s);
				emptyline();
				MetaDocument doc = s.getDocument();
				for (int p=0; p<doc.getProperties().size(); p++){
					MetaProperty pp = doc.getProperties().get(p);
					if (pp instanceof MetaContainerProperty){
						generateContainerMappings(clazz, doc, (MetaContainerProperty)pp);
					}
				}
				emptyline();
			}
		}
		closeBlock("configureActionMappings");
		
		return clazz;
	}
	
	
	private String getPackageName(){
		return GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED) + ".filter";
	}
	
	private void generateSectionMappings(GeneratedClass clazz, MetaModuleSection section){
		MetaModule module = section.getModule();
		MetaDocument doc  = section.getDocument();
	
		String actionsPackage = ModuleMafActionsGenerator.getPackage(module);
		String jspPath = FileEntry.package2path(JspMafViewGenerator.getPackage(module)).substring(FileEntry.package2path(JspMafViewGenerator.getPackage(module)).indexOf('/')) + "/";
		
		
		for(SectionAction action: SectionAction.values()){
			if(action.isIgnoreForSection(section))
				continue;
			String actionName = action.getClassName(section);
			clazz.addImport(actionsPackage + "." + actionName);
			appendStatement("ActionMappings.addMapping("+ quote(action.getMappingName(doc)) +", "+  actionName +".class, new ActionForward(\"success\"," + quote(jspPath + action.getViewName(doc)+".jsp") + "))");
		}

	}
	
	private void generateContainerMappings(GeneratedClass clazz, MetaDocument doc, MetaContainerProperty container){
	
		String actionsPackage = ModuleMafActionsGenerator.getPackage(doc);
		String jspPath = FileEntry.package2path(JspMafViewGenerator.getPackage(doc)).substring(FileEntry.package2path(JspMafViewGenerator.getPackage(doc)).indexOf('/'))+"/";
		
		for(ContainerAction action: ContainerAction.values()){
			String actionName = action.getClassName(doc, container);
			clazz.addImport(actionsPackage + "." + actionName);
			appendStatement("ActionMappings.addMapping("+ quote(action.getMappingName(doc, container)) +", "+  actionName +".class, new ActionForward(\"success\"," + quote(jspPath + action.getViewName(doc, container)+".jsp") + "))");
		}

	}

}