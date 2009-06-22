package net.anotheria.asg.generator.view;

import java.util.ArrayList;
import java.util.List;


import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.Generator;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.forms.meta.MetaForm;
import net.anotheria.asg.generator.meta.MetaContainerProperty;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaListProperty;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.MetaProperty;
import net.anotheria.asg.generator.meta.MetaTableProperty;
import net.anotheria.asg.generator.view.meta.MetaDialog;
import net.anotheria.asg.generator.view.meta.MetaModuleSection;
import net.anotheria.asg.generator.view.meta.MetaSection;
import net.anotheria.asg.generator.view.meta.MetaView;
import net.anotheria.util.IOUtils;
import net.anotheria.util.StringUtils;

/**
 * Generator for the 'struts-config.xml' files.
 * @author another
 */
public class StrutsConfigGenerator extends AbstractGenerator implements IGenerator {
	public static final String MAPPINGS_PLACEHOLDER = "<insert_mappings/>";
	public static final String FORMS_PLACEHOLDER    = "<insert_forms/>";
	public static final String TEMPLATE = "etc/templates/struts-config-template.xml";
	
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
	
	public static final String SUFFIX_CSV = ".csv";
	public static final String SUFFIX_XML = ".xml";
	
	/**
	 * The view which is being generated.
	 */
	private MetaView view;
	/**
	 * The current context.
	 */
	private Context context;


	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.generator.IGenerator#generate(net.anotheria.anodoc.generator.IGenerateable, net.anotheria.anodoc.generator.Context)
	 */
	public List<FileEntry> generate(IGenerateable g, Context aContext) {
		List<FileEntry> files = new ArrayList<FileEntry>();
	
		context = aContext;
		view = (MetaView)g;
		
		String mappings = generateMappings(view, context);
		String file = "";
		try{
			file = IOUtils.readFileAtOnceAsString(Generator.getBaseDir()+TEMPLATE);
		}catch(Exception e){
			e.printStackTrace();
		}
		String fileContent = StringUtils.replaceOnce(file, MAPPINGS_PLACEHOLDER, mappings);
		
		//forms
		String forms = generateForms(view, context);
		fileContent = StringUtils.replaceOnce(fileContent, FORMS_PLACEHOLDER, forms); 
	
		FileEntry entry = new FileEntry("/etc/appdata", getConfigFileName(view), fileContent);
		entry.setType(".xml");
		files.add(entry);
		
		return files;
	}
	
	/**
	 * Returns the name of the config file name for the given view.
	 * @param view
	 * @return
	 */
	public static final String getConfigFileName(MetaView view){
		return "struts-config-"+view.getName().toLowerCase();
	}
	
	/**
	 * Generates forms for the given view.
	 * @param view
	 * @param context
	 * @return
	 */
	private String generateForms(MetaView view, Context context){
		increaseIdent();
		increaseIdent();
		String ret = "";
		
		List<MetaSection> sections = view.getSections();
		for (int i=0; i<sections.size(); i++){
			MetaSection s = sections.get(i);
			if (!(s instanceof MetaModuleSection))
				continue;
			MetaModuleSection section = (MetaModuleSection)s;
			List<MetaDialog> dialogs = section.getDialogs();
			for (int t=0; t<dialogs.size(); t++)
				ret += generateFormsForDialog(section, dialogs.get(t));
			MetaDocument doc = section.getDocument();
			for (int p=0; p<doc.getProperties().size(); p++){
				MetaProperty pp = doc.getProperties().get(p);
				if (pp instanceof MetaContainerProperty){
					ret += generateContainerForms(doc, (MetaContainerProperty)pp);
				}
			}
			
		}
		
		decreaseIdent();
		decreaseIdent();
		return ret;
	}
	
	public static final String getDialogFormName(MetaDialog dialog, MetaDocument document){
		return dialog.getName()+document.getName()+"Form"; 
	}
	
	/**
	 * Generates forms for the containers.
	 * @param doc
	 * @param container
	 * @return
	 */
	private String generateContainerForms(MetaDocument doc, MetaContainerProperty container){
	    String ret = "";
	    ret += writeString("<form-bean name="+quote(getContainerEntryFormName(doc, container)));
	    increaseIdent();
	    ret += writeString("type="+quote(ModuleBeanGenerator.getContainerEntryFormImport(doc, container))+"/>");
	    decreaseIdent();
	    
	    if (container instanceof MetaListProperty){
		    ret += writeString("<form-bean name="+quote(getContainerQuickAddFormName(doc, container)));
		    increaseIdent();
		    ret += writeString("type="+quote(ModuleBeanGenerator.getContainerQuickAddFormImport(doc, container))+"/>");
		    decreaseIdent();
	    }
	    
	    return ret;
	}
	
	private String generateFormsForDialog(MetaModuleSection section, MetaDialog dialog){
		String ret = "";
		
		ret += writeString("<form-bean name="+quote(getDialogFormName(dialog, section.getDocument()))+"");
		increaseIdent();
		ret += writeString("type="+quote(ModuleBeanGenerator.getDialogBeanImport(context, dialog, section.getDocument()))+"/>");
		decreaseIdent();
		
		return ret;
	}
	
	private String generateMappings(MetaView view, Context context){
		increaseIdent();
		increaseIdent();
		String ret = "";
		
		List<MetaSection> sections = view.getSections();
		for (int i=0; i<sections.size(); i++){
			MetaSection s = sections.get(i);
			if (!(s instanceof MetaModuleSection))
				continue;
			MetaModuleSection section = (MetaModuleSection)s;
			ret += generateMappingsForSection(section);
			MetaDocument doc = section.getDocument();
			for (int p=0; p<doc.getProperties().size(); p++){
				MetaProperty pp = doc.getProperties().get(p);
				if (pp instanceof MetaContainerProperty){
					ret += generateContainerMappings(doc, (MetaContainerProperty)pp);
				}
			}
		}
		
		decreaseIdent();
		decreaseIdent();
		
		return ret;
	}
	
	public static final String getPath(MetaDocument doc, String action){
		return doc.getName().toLowerCase()+StringUtils.capitalize(action);
	}
	
	public static final String getContainerPath(MetaDocument doc, MetaContainerProperty container, String action){
		return doc.getName().toLowerCase()+StringUtils.capitalize(container.getName())+StringUtils.capitalize(action);
	}
	
	public static final String getContainerEntryFormName(MetaDocument doc, MetaContainerProperty property){
		String nameAddy = "XXX";
		if (property instanceof MetaTableProperty)
			nameAddy = "Row";
		if (property instanceof MetaListProperty)
			nameAddy = "Element";
	    return doc.getName()+StringUtils.capitalize(property.getName())+nameAddy+"Form";
	}

	public static final String getContainerQuickAddFormName(MetaDocument doc, MetaContainerProperty property){
		String nameAddy = "XXX";
		if (property instanceof MetaListProperty)
			nameAddy = "QuickAdd";
	    return doc.getName()+StringUtils.capitalize(property.getName())+nameAddy+"Form";
	}

	private String generateContainerMappings(MetaDocument doc, MetaContainerProperty container){
	    String ret = "";
	    
	    if (ModuleActionsGenerator.USE_MULTIOP_ACTIONS){
		    ret += generateActionMapping(
		            "/"+getContainerPath(doc, container, ACTION_SHOW),
		            ModuleActionsGenerator.getPackage(context, doc)+"."+ModuleActionsGenerator.getContainerMultiOpActionName(doc, container),
		            "success",
		            FileEntry.package2path(JspViewGenerator.getPackage(context, doc)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, doc)).indexOf('/'))+"/"+JspViewGenerator.getContainerPageName(doc, container)+".jsp"
		            );
		    ret += generateActionMapping(
		            "/"+getContainerPath(doc, container, ACTION_DELETE),
		            ModuleActionsGenerator.getPackage(context, doc)+"."+ModuleActionsGenerator.getContainerMultiOpActionName(doc, container),
		            "success",
		            FileEntry.package2path(JspViewGenerator.getPackage(context, doc)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, doc)).indexOf('/'))+"/"+JspViewGenerator.getContainerPageName(doc, container)+".jsp"
		            );
		    ret += generateActionMapping(
		            "/"+getContainerPath(doc, container, ACTION_MOVE),
		            ModuleActionsGenerator.getPackage(context,doc )+"."+ModuleActionsGenerator.getContainerMultiOpActionName(doc, container),
		            "success",
		            FileEntry.package2path(JspViewGenerator.getPackage(context, doc)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, doc)).indexOf('/'))+"/"+JspViewGenerator.getContainerPageName(doc, container)+".jsp"
		            );
		
		    ret += generateActionMapping(
		            "/"+getContainerPath(doc, container, ACTION_ADD),
		            ModuleActionsGenerator.getPackage(context, doc)+"."+ModuleActionsGenerator.getContainerMultiOpActionName(doc, container),
		            "success",
		            FileEntry.package2path(JspViewGenerator.getPackage(context, doc)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, doc)).indexOf('/'))+"/"+JspViewGenerator.getContainerPageName(doc, container)+".jsp",
		            JspViewGenerator.getContainerPageName(doc, container)+".jsp",
		            getContainerEntryFormName(doc, container)
		            );
		    
		    ret += generateActionMapping(
		            "/"+getContainerPath(doc, container, ACTION_QUICK_ADD),
		            ModuleActionsGenerator.getPackage(context, doc)+"."+ModuleActionsGenerator.getContainerMultiOpActionName(doc, container),
		            "success",
		            FileEntry.package2path(JspViewGenerator.getPackage(context, doc)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, doc)).indexOf('/'))+"/"+JspViewGenerator.getContainerPageName(doc, container)+".jsp",
		            JspViewGenerator.getContainerPageName(doc, container)+".jsp",
		            getContainerQuickAddFormName(doc, container)
		            );
	    }else{
		    ret += generateActionMapping(
		            "/"+getContainerPath(doc, container, ACTION_SHOW),
		            ModuleActionsGenerator.getPackage(context, doc)+"."+ModuleActionsGenerator.getContainerShowActionName(doc, container),
		            "success",
		            FileEntry.package2path(JspViewGenerator.getPackage(context, doc)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, doc)).indexOf('/'))+"/"+JspViewGenerator.getContainerPageName(doc, container)+".jsp"
		            );
		    ret += generateActionMapping(
		            "/"+getContainerPath(doc, container, ACTION_DELETE),
		            ModuleActionsGenerator.getPackage(context, doc)+"."+ModuleActionsGenerator.getContainerDeleteEntryActionName(doc, container),
		            "success",
		            FileEntry.package2path(JspViewGenerator.getPackage(context, doc)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, doc)).indexOf('/'))+"/"+JspViewGenerator.getContainerPageName(doc, container)+".jsp"
		            );
		    ret += generateActionMapping(
		            "/"+getContainerPath(doc, container, ACTION_MOVE),
		            ModuleActionsGenerator.getPackage(context,doc )+"."+ModuleActionsGenerator.getContainerMoveEntryActionName(doc, container),
		            "success",
		            FileEntry.package2path(JspViewGenerator.getPackage(context, doc)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, doc)).indexOf('/'))+"/"+JspViewGenerator.getContainerPageName(doc, container)+".jsp"
		            );
		
		    ret += generateActionMapping(
		            "/"+getContainerPath(doc, container, ACTION_ADD),
		            ModuleActionsGenerator.getPackage(context, doc)+"."+ModuleActionsGenerator.getContainerAddEntryActionName(doc, container),
		            "success",
		            FileEntry.package2path(JspViewGenerator.getPackage(context, doc)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, doc)).indexOf('/'))+"/"+JspViewGenerator.getContainerPageName(doc, container)+".jsp",
		            JspViewGenerator.getContainerPageName(doc, container)+".jsp",
		            getContainerEntryFormName(doc, container)
		            );
		    
		    ret += generateActionMapping(
		            "/"+getContainerPath(doc, container, ACTION_QUICK_ADD),
		            ModuleActionsGenerator.getPackage(context, doc)+"."+ModuleActionsGenerator.getContainerQuickAddActionName(doc, container),
		            "success",
		            FileEntry.package2path(JspViewGenerator.getPackage(context, doc)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, doc)).indexOf('/'))+"/"+JspViewGenerator.getContainerPageName(doc, container)+".jsp",
		            JspViewGenerator.getContainerPageName(doc, container)+".jsp",
		            getContainerQuickAddFormName(doc, container)
		            );
	    }
	    return ret;
	}
	
	private String generateMappingsForSection(MetaModuleSection section){
		String ret = "";

		MetaModule module = section.getModule();
		MetaDocument doc  = section.getDocument();
		
		ret += writeEmptyline();
		ret += writeString("<!-- Generating mapping for "+module.getName()+"."+doc.getName()+" -->");
		ret += writeEmptyline();
		
		ret += generateActionMapping(
			"/"+getPath(doc, ACTION_SHOW), 
			ModuleActionsGenerator.getPackage(context, module)+"."+ModuleActionsGenerator.getShowActionName(section),
			"success",
			FileEntry.package2path(JspViewGenerator.getPackage(context, module)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, module)).indexOf('/'))+"/"+JspViewGenerator.getShowPageName(doc)+".jsp"
			);
		ret += generateActionMapping(
			"/"+getPath(doc, ACTION_SEARCH), 
			ModuleActionsGenerator.getPackage(context, module)+"."+ModuleActionsGenerator.getSearchActionName(section),
			"success",
			FileEntry.package2path(JspViewGenerator.getPackage(context, MetaModule.SHARED)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, module)).indexOf('/'))+"/"+JspViewGenerator.getSearchResultPageName()+".jsp"
			);
		
		ret += generateActionMapping(
			"/"+getPath(doc, ACTION_EXPORT+SUFFIX_CSV), 
			ModuleActionsGenerator.getPackage(context, module)+"."+ModuleActionsGenerator.getShowActionName(section),
			"success",
			FileEntry.package2path(JspViewGenerator.getPackage(context, module)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, module)).indexOf('/'))+"/"+JspViewGenerator.getExportAsCSVPageName(doc)+".jsp"
			);
		ret += generateActionMapping(
			"/"+getPath(doc, ACTION_EXPORT+SUFFIX_XML), 
			ModuleActionsGenerator.getPackage(context, module)+"."+ModuleActionsGenerator.getShowActionName(section),
			"success",
			FileEntry.package2path(JspViewGenerator.getPackage(context, module)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, module)).indexOf('/'))+"/"+JspViewGenerator.getExportAsXMLPageName(doc)+".jsp"
			);
		ret += generateActionMapping(
			"/"+getPath(doc, ACTION_DELETE), 
			ModuleActionsGenerator.getPackage(context, module)+"."+ModuleActionsGenerator.getMultiOpDialogActionName(section),
			"success",
			FileEntry.package2path(JspViewGenerator.getPackage(context, module)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, module)).indexOf('/'))+"/"+JspViewGenerator.getShowPageName(doc)+".jsp"
			);
		ret += generateActionMapping(
			"/"+getPath(doc, ACTION_DUPLICATE), 
			ModuleActionsGenerator.getPackage(context, module)+"."+ModuleActionsGenerator.getMultiOpDialogActionName(section),
			"success",
			FileEntry.package2path(JspViewGenerator.getPackage(context, module)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, module)).indexOf('/'))+"/"+JspViewGenerator.getShowPageName(doc)+".jsp"
			);
		ret += generateActionMapping(
				"/"+getPath(doc, ACTION_LINKS_TO_ME), 
				ModuleActionsGenerator.getPackage(context, module)+"."+ModuleActionsGenerator.getEditActionName(section),
				"success",
				FileEntry.package2path(JspViewGenerator.getPackage(context, module)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, module)).indexOf('/'))+"/"+JspViewGenerator.getLinksToMePageName(doc)+".jsp"
				);
		
		
		if (doc.getLinks().size()>0){
			ret += generateActionMapping(
					"/"+getPath(doc, ACTION_SHOW_QUERIES), 
					ModuleActionsGenerator.getPackage(context, module)+"."+ModuleActionsGenerator.getShowQueryActionName(section),
					"success",
					FileEntry.package2path(JspViewGenerator.getPackage(context, module)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, module)).indexOf('/'))+"/"+JspViewGenerator.getShowQueriesPageName(doc)+".jsp"
					);
			ret += generateActionMapping(
					"/"+getPath(doc, ACTION_EXECUTE_QUERY), 
					ModuleActionsGenerator.getPackage(context, module)+"."+ModuleActionsGenerator.getExecuteQueryActionName(section),
					"success",
					FileEntry.package2path(JspViewGenerator.getPackage(context, module)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, module)).indexOf('/'))+"/"+JspViewGenerator.getShowPageName(doc)+".jsp"
					);
		}
		
		boolean generateWithForms = false;
		MetaDialog dialog = null;
		if (section instanceof MetaModuleSection){
			MetaModuleSection m = (MetaModuleSection)section;
			List<MetaDialog> dialogs = m.getDialogs();
			if (dialogs.size()>0){
				generateWithForms = true;
				dialog = dialogs.get(0);
			}
				
		}
		
		if (generateWithForms){
			ret += generateActionMapping(
				"/"+getPath(doc, ACTION_CREATE), 
				ModuleActionsGenerator.getPackage(context, module)+"."+ModuleActionsGenerator.getCreateActionName(section),
				"success",
				FileEntry.package2path(JspViewGenerator.getPackage(context, module)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, module)).indexOf('/'))+"/"+JspViewGenerator.getShowPageName(doc)+".jsp",
				JspViewGenerator.getEditPageName(doc)+".jsp",
				getDialogFormName(dialog, doc)				
				);
			ret += generateActionMapping(
				"/"+getPath(doc, ACTION_UPDATE), 
				ModuleActionsGenerator.getPackage(context, module)+"."+ModuleActionsGenerator.getMultiOpDialogActionName(section),
				"success",
				FileEntry.package2path(JspViewGenerator.getPackage(context, module)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, module)).indexOf('/'))+"/"+JspViewGenerator.getShowPageName(doc)+".jsp",
				JspViewGenerator.getEditPageName(doc)+".jsp",
				getDialogFormName(dialog, doc)				
				);
		}else{
			ret += generateActionMapping(
				"/"+getPath(doc, ACTION_CREATE), 
				ModuleActionsGenerator.getPackage(context, module)+"."+ModuleActionsGenerator.getCreateActionName(section),
				"success",
				FileEntry.package2path(JspViewGenerator.getPackage(context, module)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, module)).indexOf('/'))+"/"+JspViewGenerator.getShowPageName(doc)+".jsp"
				);
			ret += generateActionMapping(
				"/"+getPath(doc, ACTION_UPDATE), 
				ModuleActionsGenerator.getPackage(context, module)+"."+ModuleActionsGenerator.getMultiOpDialogActionName(section),
				"success",
				FileEntry.package2path(JspViewGenerator.getPackage(context, module)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, module)).indexOf('/'))+"/"+JspViewGenerator.getShowPageName(doc)+".jsp"
				);
			
		}
		
		ret += generateActionMapping(
			"/"+getPath(doc, ACTION_NEW), 
			ModuleActionsGenerator.getPackage(context, module)+"."+ModuleActionsGenerator.getNewActionName(section),
			"success",
			FileEntry.package2path(JspViewGenerator.getPackage(context, module)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, module)).indexOf('/'))+"/"+JspViewGenerator.getEditPageName(doc)+".jsp"
			);
		ret += generateActionMapping(
			"/"+getPath(doc, ACTION_EDIT), 
			ModuleActionsGenerator.getPackage(context, module)+"."+ModuleActionsGenerator.getEditActionName(section),
			"success",
			FileEntry.package2path(JspViewGenerator.getPackage(context, module)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, module)).indexOf('/'))+"/"+JspViewGenerator.getEditPageName(doc)+".jsp"
			);
		
		if (GeneratorDataRegistry.hasLanguageCopyMethods(doc)){
			ret += generateActionMapping(
					"/"+getPath(doc, ACTION_COPY_LANG), 
					ModuleActionsGenerator.getPackage(context, module)+"."+ModuleActionsGenerator.getMultiOpDialogActionName(section),
					"success",
					FileEntry.package2path(JspViewGenerator.getPackage(context, module)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, module)).indexOf('/'))+"/"+JspViewGenerator.getEditPageName(doc)+".jsp"
					);
			ret += generateActionMapping(
					"/"+getPath(doc, ACTION_SWITCH_MULTILANGUAGE_INSTANCE), 
					ModuleActionsGenerator.getPackage(context, module)+"."+ModuleActionsGenerator.getMultiOpDialogActionName(section),
					"success",
					FileEntry.package2path(JspViewGenerator.getPackage(context, module)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, module)).indexOf('/'))+"/"+JspViewGenerator.getEditPageName(doc)+".jsp"
					);
		}
		
		ret += generateActionMapping(
			"/"+getPath(doc, ACTION_VERSIONINFO), 
			ModuleActionsGenerator.getPackage(context, module)+"."+ModuleActionsGenerator.getMultiOpDialogActionName(section),
			"success",
			FileEntry.package2path(JspViewGenerator.getPackage(context, MetaModule.SHARED)).substring(FileEntry.package2path(JspViewGenerator.getPackage(context, module)).indexOf('/'))+"/"+JspViewGenerator.getVersionInfoPageName(doc)+".jsp"
			);
		
		ret += writeEmptyline();
		ret += writeString("<!-- Finished mapping for "+module.getName()+"."+doc.getName()+" -->");
		ret += writeEmptyline();

		
		return ret;
	}
	
	/**
	 * Creates an action mapping.
	 * @param path the path 
	 * @param type type of the redirect.
	 * @param forwardName
	 * @param forwardPath
	 * @param input
	 * @param form
	 * @return
	 */
	private String generateActionMapping(String path, String type, String forwardName, String forwardPath, String input, String form){
		return generateActionMapping(path, type, "request", forwardName, forwardPath, input, form);
	}

	private String generateActionMapping(String path, String type, String scope, String forwardName, String forwardPath, String input, String form){
		String ret = "";
		ret += writeString("<action");
		increaseIdent();
		ret += writeString("path="+quote(path));
		ret += writeString("name="+quote(form));
		ret += writeString("type="+quote(type));
		ret += writeString("input="+quote(input));
		ret += writeString("scope="+quote(scope)+">");
		ret += writeString(" <forward name="+quote(forwardName)+" path="+quote(forwardPath)+"/>");
		decreaseIdent();
		ret += writeString("</action>");
		return ret;
	}

	private String generateActionMapping(String path, String type, String forwardName, String forwardPath){
		return generateActionMapping(path, type, "request", forwardName, forwardPath);
	}
	

	private String generateActionMapping(String path, String type, String scope, String forwardName, String forwardPath){
		String ret = "";
		ret += writeString("<action path="+quote(path)+" type="+quote(type)+" scope="+quote(scope)+">");
		ret += writeIncreasedString("<forward name="+quote(forwardName)+" path="+quote(forwardPath)+"/>");
		ret += writeString("</action>");
		return ret;
	}

	public static String getActionSuffix(MetaModuleSection section){
		return section.getDocument().getName()+"Action";
	}

	public static String getActionSuffix(MetaDocument doc){
		return doc.getName()+"Action";
	}

	public static String getShowActionName(MetaModuleSection section){
		return "Show"+section.getDocument().getMultiple()+"Action";
	}

	public static String getEditActionName(MetaModuleSection section){
		return "Edit"+getActionSuffix(section);
	}

	public static String getEditActionName(MetaDocument doc){
		return "Edit"+getActionSuffix(doc);
	}

	public static String getUpdateActionName(MetaModuleSection section){
		return "Update"+getActionSuffix(section);
	}

	public static String getNewActionName(MetaModuleSection section){
		return "New"+getActionSuffix(section);
	}

	public static String getCreateActionName(MetaModuleSection section){
		return "Create"+getActionSuffix(section);
	}

	public static String getDeleteActionName(MetaModuleSection section){
		return "Delete"+getActionSuffix(section);
	}
	
	
	public static String getFormName(MetaForm form){
	    return StringUtils.capitalize(form.getId())+"AutoForm";
	}

	public static String getFormPath(MetaForm form){
	    return form.getId()+StringUtils.capitalize(form.getAction());
	}

	public static String getShowQueriesPath(MetaDocument doc){
		return getPath(doc, ACTION_SHOW_QUERIES);
	}

	public static String getExecuteQueryPath(MetaDocument doc){
		return getPath(doc, ACTION_EXECUTE_QUERY);
	}

	public static String getShowCMSPath(MetaDocument doc){
		return getPath(doc, ACTION_SHOW);
	}

}
