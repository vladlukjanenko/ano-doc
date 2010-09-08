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
	
	public static enum SectionAction{
		SHOW("Show", "Show", OperationType.SINGLE, true, false),
		EDIT("Edit", "Edit", OperationType.SINGLE),
		NEW("New", "Edit", OperationType.SINGLE),
//		LINKSTOME("LinksToMe", "LinksTo", OperationType.SINGLE, false, false){
//			@Override
//			public String getClassName(MetaModuleSection section){
//				//return action + section.getDocument().getName() + "MafAction";
//			}
//		},
		CLOSE("Close", "Show", OperationType.MULTIPLE_DIALOG),
		UPDATE("Update", "Show", OperationType.MULTIPLE_DIALOG),
		DELETE("Delete", "Show", OperationType.MULTIPLE_DIALOG),
		DUPLICATE("Duplicate", "Show", OperationType.MULTIPLE_DIALOG),
		LOCK("Lock", "EditBoxDialog", OperationType.MULTIPLE_DIALOG),
		UNLOCK("UnLock", "EditBoxDialog", OperationType.MULTIPLE_DIALOG),
		COPYLANG("CopyLang", "EditBoxDialog", OperationType.MULTIPLE_DIALOG),
		SWITCHMULTILANG("SwitchMultilang", "EditBoxDialog", OperationType.MULTIPLE_DIALOG),
		VERSIONINFO("Versioninfo", "EditBoxDialog", OperationType.MULTIPLE_DIALOG),
		
		SEARCH("Search", "SearchResultMaf", OperationType.SINGLE, true, false){
			@Override
			public String getViewName(MetaModuleSection section){
				return "SearchResultMaf";
			}
			@Override
			public String getViewPath(MetaModuleSection section){
				return "/" + FileEntry.package2path(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED)+".jsp") + "/";
			}
		},
		EXPORT("Export", "Show2", OperationType.SINGLE, true, false),
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
		
		public String getClassName(MetaModuleSection section) {
			switch (type) {
			case SINGLE:
				return action + section.getDocument().getName(multiDocument) + "MafAction";
			case MULTIPLE_DIALOG:
				return ModuleMafActionsGenerator.getMultiOpDialogActionName(section);
			}
			throw new AssertionError("Unsuported OperationType!");

		}
		
		public String getMappingName(MetaModuleSection section){
			MetaDocument doc = section.getDocument();
			return doc.getParentModule().getName().toLowerCase()+StringUtils.capitalize(doc.getName()) + action;
		}
		
		
		public String getViewName(MetaModuleSection section){
			MetaDocument doc = section.getDocument();
			return view+doc.getName(multiDocument) + "Maf";
		}
		
		public String getViewPath(MetaModuleSection section){
			return "/" + FileEntry.package2path(JspMafViewGenerator.getPackage(section.getModule())) + "/";
		}
		
		public String getViewFullName(MetaModuleSection section){
			return getViewPath(section) + getViewName(section);
		}

		public boolean isIgnoreForSection(MetaModuleSection section){
			return section.getDialogs().size() == 0 || ignoreFederationSections && StorageType.FEDERATION == section.getModule().getStorageType();
		}
		
	}
	
	public static enum ContainerAction{
		SHOW("Show"),
		DELETE("Delete"),
		MOVE("Move"),
		ADD("Add"),
		QUICKADD("QuickAdd"),
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
		clazz.addImport(IndexPageMafActionGenerator.getIndexPageFullName());
		clazz.addImport("net.anotheria.webutils.filehandling.actions.ShowFile");
		clazz.addImport("net.anotheria.webutils.filehandling.actions.UploadFile");
		clazz.addImport("net.anotheria.webutils.filehandling.actions.ShowTmpFile");
		clazz.addImport("net.anotheria.webutils.filehandling.actions.GetFile");
		clazz.addImport("net.anotheria.webutils.actions.LoginAction");
		clazz.addImport("net.anotheria.webutils.actions.LogoutAction");
		

		clazz.addInterface("ActionMappingsConfigurator");
		clazz.setName("CMSMappingsConfigurator");

		startClassBody();
		
		emptyline();
		
		appendString("@Override");
		openFun("public void configureActionMappings()");
		appendStatement("ActionMappings.addMapping(\"index\", " + IndexPageMafActionGenerator.getIndexPageActionName() + ".class, new ActionForward(\"success\", "+quote(IndexPageJspMafGenerator.getIndexJspFullName())+"))");
//		appendStatement("ActionMappings.addMapping(\"fileShow\", ShowFile.class, new ActionForward(\"success\", \"/net/anotheria/webutils/jsp/UploadFile.jsp\"))");
//		appendStatement("ActionMappings.addMapping(\"fileUpload\", UploadFile.class, new ActionForward(\"success\", \"/net/anotheria/webutils/jsp/UploadFileResult.jsp\"))");
//		appendStatement("ActionMappings.addMapping(\"login\", LoginAction.class, new ActionForward(\"success\", \"/net/anotheria/webutils/jsp/Login.jsp\"))");
//		appendStatement("ActionMappings.addMapping(\"logout\", LogoutAction.class, new ActionForward(\"success\", \"/net/anotheria/webutils/jsp/Login.jsp\"))");
		
		for(MetaView view: views){
			for (MetaSection section: view.getSections()){
				if (!(section instanceof MetaModuleSection))
					continue;
				MetaModuleSection s = (MetaModuleSection)section;
				if(s.getDialogs().size() == 0)
					continue;
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
		String actionsPackage = ModuleMafActionsGenerator.getPackage(module);
		for(SectionAction action: SectionAction.values()){
			if(action.isIgnoreForSection(section))
				continue;
			String actionName = action.getClassName(section);
			clazz.addImport(actionsPackage + "." + actionName);
			appendStatement("ActionMappings.addMapping("+ quote(action.getMappingName(section)) +", "+  actionName +".class, new ActionForward(\"success\"," + quote(action.getViewFullName(section)+".jsp") + "))");
		}

	}
	
	private void generateContainerMappings(GeneratedClass clazz, MetaDocument doc, MetaContainerProperty container){
		String actionsPackage = ModuleMafActionsGenerator.getPackage(doc);
		String jspPath = FileEntry.package2fullPath(JspMafViewGenerator.getPackage(doc)).substring(FileEntry.package2fullPath(JspMafViewGenerator.getPackage(doc)).indexOf('/'))+"/";
		
		
		for(ContainerAction action: ContainerAction.values()){
			String actionName = action.getClassName(doc, container);
			clazz.addImport(actionsPackage + "." + actionName);
			appendStatement("ActionMappings.addMapping("+ quote(action.getMappingName(doc, container)) +", "+  actionName +".class, new ActionForward(\"success\"," + quote(jspPath + action.getViewName(doc, container)+".jsp") + "))");
		}

	}

}