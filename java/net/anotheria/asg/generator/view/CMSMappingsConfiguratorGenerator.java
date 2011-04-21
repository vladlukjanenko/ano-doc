package net.anotheria.asg.generator.view;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.forms.meta.MetaForm;
import net.anotheria.asg.generator.meta.MetaContainerProperty;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaListProperty;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.MetaProperty;
import net.anotheria.asg.generator.meta.MetaTableProperty;
import net.anotheria.asg.generator.meta.StorageType;
import net.anotheria.asg.generator.view.action.IndexPageActionGenerator;
import net.anotheria.asg.generator.view.action.ModuleActionsGenerator;
import net.anotheria.asg.generator.view.jsp.IndexPageJspGenerator;
import net.anotheria.asg.generator.view.jsp.JspGenerator;
import net.anotheria.asg.generator.view.meta.MetaDialog;
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
		SHOW("Show", "Show", OperationType.SINGLE, true),
		EDIT("Edit", "Edit", OperationType.SINGLE),
		NEW("New", "Edit", OperationType.SINGLE),
		LINKSTOME("LinksToMe", "LinksTo", OperationType.SINGLE, false){
			@Override
			public String getClassName(MetaModuleSection section){
				return "Edit" + section.getDocument().getName() + "MafAction";
			}
			@Override
			public String getViewName(MetaModuleSection section){
				MetaDocument doc = section.getDocument();
				return "LinksTo"+doc.getName();
			}
		},
		CLOSE("Close", "Show", OperationType.MULTIPLE_DIALOG),
		UPDATE("Update", "Show", OperationType.MULTIPLE_DIALOG),
		DELETE("Delete", "Show", OperationType.MULTIPLE_DIALOG),
		DUPLICATE("Duplicate", "Show", OperationType.MULTIPLE_DIALOG),
		LOCK("Lock", "EditBoxDialog", OperationType.MULTIPLE_DIALOG),
		UNLOCK("UnLock", "EditBoxDialog", OperationType.MULTIPLE_DIALOG),
		COPYLANG("CopyLang", "EditBoxDialog", OperationType.MULTIPLE_DIALOG),
		SWITCHMULTILANG("SwitchMultilang", "EditBoxDialog", OperationType.MULTIPLE_DIALOG),
		VERSIONINFO("Versioninfo", "EditBoxDialog", OperationType.MULTIPLE_DIALOG),
		EXPORTtoCSV("Export.csv", "Show", OperationType.SINGLE, true){
			@Override
			public String getClassName(MetaModuleSection section){
				return "Export" + section.getDocument().getName(true) + "MafAction";
			}
			@Override
			public String getViewName(MetaModuleSection section){
				MetaDocument doc = section.getDocument();
				return "Show"+doc.getName(true) + "AsCSVMaf";
			}
		},
		EXPORTtoXML("Export.xml", "Show", OperationType.SINGLE, true){
			@Override
			public String getClassName(MetaModuleSection section){
				return "Export" + section.getDocument().getName(true) + "MafAction";
			}
			@Override
			public String getViewName(MetaModuleSection section){
				MetaDocument doc = section.getDocument();
				return "Show"+doc.getName(true) + "AsXMLMaf";
			}
		},
		
		;
		
		private String action;
		private String view;
		private OperationType type;
		private boolean multiDocument;
		
		private SectionAction(String anAction, String aView, OperationType aType) {
			this(anAction, aView, aType, false);
		}
		
		private SectionAction(String anAction, String aView, OperationType aType, boolean aListDocument) {
			action = anAction;
			view = aView;
			type = aType;
			multiDocument = aListDocument;
		}
		
		public String getClassName(MetaModuleSection section) {
			switch (type) {
			case SINGLE:
				return action + section.getDocument().getName(multiDocument) + "MafAction";
			case MULTIPLE_DIALOG:
				return ModuleActionsGenerator.getMultiOpDialogActionName(section);
			}
			throw new AssertionError("Unsuported OperationType!");

		}
		
		public String getMappingName(MetaModuleSection section){
			return getMappingName(section.getDocument());
		}
		
		public String getMappingName(MetaDocument doc){
			return doc.getParentModule().getName().toLowerCase()+StringUtils.capitalize(doc.getName()) + action;
		}
		
		
		public String getViewName(MetaModuleSection section){
			MetaDocument doc = section.getDocument();
			return view+doc.getName(multiDocument) + "Maf";
		}
		
		public String getViewPath(MetaModuleSection section){
			return "/" + FileEntry.package2path(JspGenerator.getPackage(section.getModule())) + "/";
		}
		
		public String getViewFullName(MetaModuleSection section){
			return getViewPath(section) + getViewName(section);
		}

		public boolean isIgnoreForSection(MetaModuleSection section){
			return !multiDocument && (section.getDialogs().size() == 0 || StorageType.FEDERATION == section.getModule().getStorageType());
		}
		
	}
	
	public static enum SharedAction{
		//SHOW("Show", "Show", OperationType.SINGLE, true, false),
		
		SEARCH("CmsSearch", "SearchResultMaf"){
//			@Override
//			public String getViewName(MetaModuleSection section){
//				return "SearchResultMaf";
//			}
//			@Override
//			public String getViewPath(MetaModuleSection section){
//				return "/" + FileEntry.package2path(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED)+".jsp") + "/";
//			}
//			@Override
//			public String getClassName(MetaModuleSection section){
//				return "SearchMafAction";
//			}
		},
		;
		
		private String action;
		private String view;
		
		private SharedAction(String anAction, String aView) {
			action = anAction;
			view = aView;
		}
		
		public String getClassName() {
				return action + "MafAction";
		}
		
		public String getMappingName(){
			return action.toLowerCase();
		}
		
		
		public String getViewName(){
			return view;
		}
		
		public String getViewPath(){
			return "/" + FileEntry.package2path(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED)+".jsp") + "/";
		}
		
		public String getViewFullName(){
			return getViewPath() + getViewName();
		}
		
		public static final String getPackageName(){
			return GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED)+".action";
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
			return ModuleActionsGenerator.getContainerMultiOpActionName(doc, container);
		}
		
		
		public String getMappingName(MetaDocument doc, MetaContainerProperty container){
			return doc.getParentModule().getName().toLowerCase()+StringUtils.capitalize(doc.getName())+StringUtils.capitalize(container.getName())+ action;
		}
		
		public String getViewName(MetaDocument doc, MetaContainerProperty container){
			return JspGenerator.getContainerPageName(doc, container);
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
	
	public static String getClassSimpleName(){
		return "CMSMappingsConfigurator";
	}
	
	public static String getClassName(){
		return getPackageName() + "." + getClassSimpleName();
	}
	
	private GeneratedClass generateCMSMapping(List<MetaView> views){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(getPackageName());
		
		clazz.addImport(Map.class);
		clazz.addImport(HashMap.class);
		clazz.addImport("net.anotheria.maf.action.ActionForward");
		clazz.addImport("net.anotheria.maf.action.ActionMappings");
		clazz.addImport("net.anotheria.maf.action.ActionMappingsConfigurator");
		clazz.addImport(IndexPageActionGenerator.getIndexPageFullName());
		clazz.addImport("net.anotheria.webutils.filehandling.actions.ShowFileMaf");
		clazz.addImport("net.anotheria.webutils.filehandling.actions.FileAjaxUploadMaf");
		clazz.addImport("net.anotheria.webutils.filehandling.actions.ShowTmpFileMaf");
		clazz.addImport("net.anotheria.webutils.filehandling.actions.GetFileMaf");
		clazz.addImport("net.anotheria.webutils.actions.LoginMafAction");
		clazz.addImport("net.anotheria.webutils.actions.LogoutMafAction");
		
		

		clazz.addInterface("ActionMappingsConfigurator");
		clazz.setName(getClassSimpleName());

		startClassBody();
		
		emptyline();
		
		appendString("private static final Map<String, String> showActionsRegistry;");
		appendString("static{");
		appendStatement("showActionsRegistry = new HashMap<String,String>()");
		
		for(MetaView view: views){
			for (MetaSection section: view.getSections()){
				if (!(section instanceof MetaModuleSection))
					continue;
				MetaModuleSection s = (MetaModuleSection)section;
				generateActionsRegistry(s);
			}
		}
		
		closeBlock("close static block");
		
		openFun("public static String getActionPath(String parentName, String documentName)");
		appendStatement("return showActionsRegistry.get(parentName + \".\" + documentName)");
		closeBlock("getActionPath");
		
		appendString("@Override");
		openFun("public void configureActionMappings()");
		appendStatement("ActionMappings.addMapping(\"index\", " + IndexPageActionGenerator.getIndexPageActionName() + ".class, new ActionForward(\"success\", "+quote(IndexPageJspGenerator.getIndexJspFullName())+"))");
		appendStatement("ActionMappings.addMapping(\"fileShow\", ShowFileMaf.class, new ActionForward(\"success\", \"/net/anotheria/webutils/jsp/UploadFile.jsp\"))");
		appendStatement("ActionMappings.addMapping(\"fileUpload\", FileAjaxUploadMaf.class)");
		
		appendStatement("ActionMappings.addMapping(\"showTmpFile\", ShowTmpFileMaf.class)");
		appendStatement("ActionMappings.addMapping(\"getFile\", GetFileMaf.class)");
		
		appendStatement("ActionMappings.addMapping(\"login\", LoginMafAction.class, new ActionForward(\"success\", \"/net/anotheria/webutils/jsp/Login.jsp\"))");
		appendStatement("ActionMappings.addMapping(\"logout\", LogoutMafAction.class, new ActionForward(\"success\", \"/net/anotheria/webutils/jsp/Login.jsp\"))");
		
		
		generateSharedMappings(clazz);
		for(MetaView view: views){
			for (MetaSection section: view.getSections()){
				if (!(section instanceof MetaModuleSection))
					continue;
				MetaModuleSection s = (MetaModuleSection)section;
				
				
//				if(s.getDialogs().size() == 0)
//					continue;
				appendCommentLine("Mapping " + s.getDocument().getName());
				generateSectionMappings(clazz, s);
				emptyline();
				MetaDocument doc = s.getDocument();
				for (int p=0; p<doc.getProperties().size(); p++){
					MetaProperty pp = doc.getProperties().get(p);
					if (pp instanceof MetaContainerProperty){
						generateContainerMappings(clazz, s, (MetaContainerProperty)pp);
					}
				}
				emptyline();
			}
		}
		closeBlock("configureActionMappings");
		
		return clazz;
	}

	
	
	private static String getPackageName(){
		return GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED) + ".filter";
	}
	
	private void generateSectionMappings(GeneratedClass clazz, MetaModuleSection section){
		MetaModule module = section.getModule();
		String actionsPackage = ModuleActionsGenerator.getPackage(module);
		boolean validatedUpdateAction = section.isValidatedOnSave();
		for(SectionAction action: SectionAction.values()){
			if(action.isIgnoreForSection(section))
				continue;
//			String actionName = action.getClassName(section);
//			clazz.addImport(actionsPackage + "." + actionName);
			String actionName = actionsPackage + "." + action.getClassName(section);
			appendStatement("ActionMappings.addMapping("+ quote(action.getMappingName(section)) +", "+  actionName +".class, new ActionForward(\"success\"," + quote(action.getViewFullName(section)+".jsp") + "))");
			if (validatedUpdateAction && action.equals(SectionAction.UPDATE)) {
				appendStatement("ActionMappings.addMapping("+ quote(action.getMappingName(section)) +", "+  actionName +".class, new ActionForward(\"validationError\"," + quote(SectionAction.NEW.getViewFullName(section)+".jsp") + "))");
			}
		}

	}
	
	private void generateActionsRegistry(MetaModuleSection section){
			if(SectionAction.SHOW.isIgnoreForSection(section))
				return;
			appendStatement("showActionsRegistry.put(" + quote(section.getModule().getName() + "." + section.getDocument().getName()) + ", " + quote(SectionAction.SHOW.getMappingName(section)) + ")");
	}
	
	
	private void generateSharedMappings(GeneratedClass clazz){
		String actionsPackage = GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED)+".action";
		for(SharedAction action: SharedAction.values()){
//			String actionName = action.getClassName();
//			clazz.addImport(actionsPackage + "." + actionName);
			String actionName = actionsPackage + "." + action.getClassName();
			appendStatement("ActionMappings.addMapping("+ quote(action.getMappingName()) +", "+  actionName +".class, new ActionForward(\"success\"," + quote(action.getViewFullName()+".jsp") + "))");
		}

	}
	
	private void generateContainerMappings(GeneratedClass clazz, MetaModuleSection section, MetaContainerProperty container){
		if(section.getDialogs().size() == 0)
			return;
		MetaDocument doc = section.getDocument();
		String actionsPackage = ModuleActionsGenerator.getPackage(doc);
		String jspPath = FileEntry.package2fullPath(JspGenerator.getPackage(doc)).substring(FileEntry.package2fullPath(JspGenerator.getPackage(doc)).indexOf('/'))+"/";
		
		
		for(ContainerAction action: ContainerAction.values()){
//			String actionName = action.getClassName(doc, container);
//			clazz.addImport(actionsPackage + "." + actionName);
			String actionName = actionsPackage + "." + action.getClassName(doc, container);
			appendStatement("ActionMappings.addMapping("+ quote(action.getMappingName(doc, container)) +", "+  actionName +".class, new ActionForward(\"success\"," + quote(jspPath + action.getViewName(doc, container)+".jsp") + "))");
		}

	}
	
	
	//TODO: Investigate this methods copied from StrutsCOnfigGenerator
	public static final String ACTION_SHOW   = "show";
	public static final String ACTION_NEW    = "new";
	public static final String ACTION_EDIT   = "edit";
	public static final String ACTION_CREATE = "create";
	public static final String ACTION_UPDATE = "update";
	public static final String ACTION_DELETE = "delete";
	public static final String ACTION_VERSIONINFO = "versioninfo";
	public static final String ACTION_DUPLICATE = "duplicate";
	public static final String ACTION_DEEPCOPY = "deepcopy";
	public static final String ACTION_ADD 	 = "add";
	public static final String ACTION_QUICK_ADD  = "quickAdd";
	public static final String ACTION_EXPORT = "export";
	public static final String ACTION_SHOW_QUERIES = "showQueries";
	public static final String ACTION_EXECUTE_QUERY = "execQuery";
	public static final String ACTION_LINKS_TO_ME = "LinksToMe";
	public static final String ACTION_MOVE = "move";
	public static final String ACTION_SEARCH = "search";
	public static final String ACTION_COPY_LANG ="copyLang";
	public static final String ACTION_SWITCH_MULTILANGUAGE_INSTANCE = "switchMultilang";
    public static final String ACTION_LOCK = "lock";
    public static final String ACTION_UNLOCK = "unLock";
    public static final String ACTION_CLOSE = "close";
	
	public static final String getPath(MetaDocument doc, String action){
		return doc.getParentModule().getName().toLowerCase()+StringUtils.capitalize(doc.getName())+StringUtils.capitalize(action);
	}
	public static String getShowQueriesPath(MetaDocument doc){
		return getPath(doc, ACTION_SHOW_QUERIES);
	}
	public static String getShowCMSPath(MetaDocument doc){
		return getPath(doc, ACTION_SHOW);
	}
	public static final String getDialogFormName(MetaDialog dialog, MetaDocument document) {
		return dialog.getName() + document.getParentModule().getName() + document.getName() + "Form";
	}
	public static final String getContainerPath(MetaDocument doc, MetaContainerProperty container, String action){
		return doc.getParentModule().getName().toLowerCase()+StringUtils.capitalize(doc.getName())+StringUtils.capitalize(container.getName())+StringUtils.capitalize(action);
	}
	public static String getFormName(MetaForm form){
	    return StringUtils.capitalize(form.getId())+"AutoForm";
	}
	public static String getFormPath(MetaForm form){
	    return form.getId()+StringUtils.capitalize(form.getAction());
	}
	public static String getExecuteQueryPath(MetaDocument doc){
		return getPath(doc, ACTION_EXECUTE_QUERY);
	}
	public static final String getContainerEntryFormName(MetaDocument doc, MetaContainerProperty property){
		String nameAddy = "XXX";
		if (property instanceof MetaTableProperty)
			nameAddy = "Row";
		if (property instanceof MetaListProperty)
			nameAddy = "Element";
	    return doc.getParentModule().getName().toLowerCase()+StringUtils.capitalize(doc.getName())+StringUtils.capitalize(property.getName())+nameAddy+"Form";
	}
	public static final String getContainerQuickAddFormName(MetaDocument doc, MetaContainerProperty property){
		String nameAddy = "XXX";
		if (property instanceof MetaListProperty)
			nameAddy = "QuickAdd";
	    return doc.getParentModule().getName().toLowerCase()+StringUtils.capitalize(doc.getName())+StringUtils.capitalize(property.getName())+nameAddy+"Form";
	}
	//TODO: end

}