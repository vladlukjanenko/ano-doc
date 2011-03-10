package net.anotheria.asg.generator.view;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.Generator;
import net.anotheria.asg.generator.GeneratorDataRegistry;
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
public class StrutsConfigGenerator extends AbstractGenerator{
	
	/**
	 * Placeholder in the struts-config.xml where to place the mappings.
	 */
	public static final String MAPPINGS_PLACEHOLDER = "<insert_mappings/>";
	/**
	 * Placeholder in the struts-config.xml where to place the forms.
	 */
	public static final String FORMS_PLACEHOLDER    = "<insert_forms/>";
	/**
	 * Relative path to the template.
	 */
	public static final String TEMPLATE = "etc/templates/struts-config-template.xml";
	/**
	 * Shortcut for the show action.
	 */
	public static final String ACTION_SHOW   = "show";
	/**
	 * Shortcut for the new document action.
	 */
	public static final String ACTION_NEW    = "new";
	/**
	 * Shortcut for the edit document action.
	 */
	public static final String ACTION_EDIT   = "edit";
	/**
	 * Shortcut for the create document action.
	 */
	public static final String ACTION_CREATE = "create";
	/**
	 * Shortcut for the update document action.
	 */
	public static final String ACTION_UPDATE = "update";
	
	/**
	 * Shortcut for the delete document action.
	 */
	public static final String ACTION_DELETE = "delete";
	/**
	 * Shortcut for the show document's version info action.
	 */
	public static final String ACTION_VERSIONINFO = "versioninfo";
	/**
	 * Shortcut for the duplicate document action.
	 */
	public static final String ACTION_DUPLICATE = "duplicate";
	/**
	 * Shortcut for the update deepcopy action (is it even supported?).
	 */
	public static final String ACTION_DEEPCOPY = "deepcopy";
	/**
	 * Prefix for 'add' element to container actions.
	 */
	public static final String ACTION_ADD 	 = "add";
	/**
	 * Shortcut for quickadd container element (by id) actions.
	 */
	public static final String ACTION_QUICK_ADD  = "quickAdd";
	/**
	 * Export actions.
	 */
	public static final String ACTION_EXPORT = "export";
	/**
	 * How query menu action.
	 */
	public static final String ACTION_SHOW_QUERIES = "showQueries";
	/**
	 * Execute query action.
	 */
	public static final String ACTION_EXECUTE_QUERY = "execQuery";
	/**
	 * Shortcut for show all incoming links action.
	 */
	public static final String ACTION_LINKS_TO_ME = "LinksToMe";
	
	/**
	 * Shortcut for move element in a container action.
	 */
	public static final String ACTION_MOVE = "move";
	/**
	 * Constant for search action.
	 */
	public static final String ACTION_SEARCH = "search";
	/**
	 * Constant for copy from lang a to lang b action.
	 */
	public static final String ACTION_COPY_LANG ="copyLang";
	/**
	 * Constant for action switching multilingual support on and off.
	 */
	public static final String ACTION_SWITCH_MULTILANGUAGE_INSTANCE = "switchMultilang";
    /**
     *  Constant for lock action.
     */
    public static final String ACTION_LOCK = "lock";
    /**
     *  Constant for unLock action.
     */
    public static final String ACTION_UNLOCK = "unLock";
	/**
     *  Constant for close action.
     */
    public static final String ACTION_CLOSE = "close";
	/**
	 * Extension for csv generation.
	 */
	public static final String SUFFIX_CSV = ".csv";
	/**
	 * Extension for the xml generation.
	 */
	public static final String SUFFIX_XML = ".xml";
	/**
	 * Default referrer forward is the name of forward action,
	 * that will be used from EditUserSettings page as "last page before edit user settings"
	 * if referrer can not be received manually from request property
	 */
	public static final String USER_SETTINGS_DEFAULT_REFERRER_FORWARD_NAME = "defaultReferrer";
	/**
	 * Default referrer path.
	 * @see {@link USER_SETTINGS_DEFAULT_REFERRER_FORWARD_NAME}
	 */
	public static final String USER_SETTINGS_DEFAULT_REFERRER_FORWARD_PATH = "index";
	
	/**
	 * The view which is being generated.
	 */
	private MetaView view;
	
	/**
	 * UserSettings struts configuration generator
	 */
	public List<FileEntry> generateUserSettingsStrutsConfig() {
		List<FileEntry> files = new ArrayList<FileEntry>();
		
		// Load template
		String file = "";
		try{
			file = IOUtils.readFileAtOnceAsString(Generator.getBaseDir()+TEMPLATE);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		// Mappings
		String mappings = generateUserSettingsMappings();		
		String fileContent = StringUtils.replaceOnce(file, MAPPINGS_PLACEHOLDER, mappings);
		
		// Forms
		String forms = generateUserSettingsForms();
		fileContent = StringUtils.replaceOnce(fileContent, FORMS_PLACEHOLDER, forms); 
	
		FileEntry entry = new FileEntry("/etc/appdata", getConfigFileName(MetaModule.USER_SETTINGS.getName()) , fileContent);
		
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
	 * Returns the name of the config file name for the given view name.
	 * @param viewName
	 * @return
	 */
	public static final String getConfigFileName(String viewName){
		return "struts-config-" + viewName.toLowerCase();
	}
	
	/**
	 * Generates forms for the given view.
	 * @param view
	 * @param context
	 * @return
	 */
	private String generateForms(MetaView view){
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
	
	/**
	 * UserSettings forms generator.
	 */
	private String generateUserSettingsForms(){
		increaseIdent();
		increaseIdent();
		String ret = "";
		
		ret += writeString("<form-bean name="+quote("EditUserSettingsForm"));
	    increaseIdent();
	    ret += writeString("type="+quote(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.USER_SETTINGS) + ".bean.EditUserSettingsForm")+"/>");	    
	    decreaseIdent();
		
	    decreaseIdent();
		decreaseIdent();
		return ret;
	}
	
	public static final String getDialogFormName(MetaDialog dialog, MetaDocument document) {
		return dialog.getName() + document.getParentModule().getName() + document.getName() + "Form";
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
		ret += writeString("type="+quote(ModuleBeanGenerator.getDialogBeanImport(dialog, section.getDocument()))+"/>");
		decreaseIdent();
		
		return ret;
	}
	
	
	/**
	 * UserSettings mappings generator
	 */
	private String generateUserSettingsMappings(){
		increaseIdent();
		increaseIdent();
		String ret = "";

		ret += writeEmptyline();
		ret += writeString("<!-- Generating mapping for " + MetaModule.USER_SETTINGS.getName() + " -->");
		ret += writeEmptyline();
		
		// Edit Action
		Map<String,String> editForwards = new LinkedHashMap<String, String>();
		editForwards.put("success","/net/anotheria/anosite/gen/usersettings/jsp/EditUserSettingsDialog.jsp");
		editForwards.put(USER_SETTINGS_DEFAULT_REFERRER_FORWARD_NAME,USER_SETTINGS_DEFAULT_REFERRER_FORWARD_PATH);		
		ret += generateActionMapping(
			"/userSettingsEdit", 
			GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.USER_SETTINGS) + ".action.EditUserSettingsAction",
			editForwards
			);
		
		
		// Update Action
		ret += generateActionMapping(
			"/userSettingsUpdate", 
			GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.USER_SETTINGS) + ".action.EditUserSettingsDialogAction",
			null,
			null,
			"EditUserSettingsDialog.jsp",
			"EditUserSettingsForm"
			);
		
		decreaseIdent();
		decreaseIdent();
		
		return ret;
	}
	
	public static final String getPath(MetaDocument doc, String action){
		return doc.getParentModule().getName().toLowerCase()+StringUtils.capitalize(doc.getName())+StringUtils.capitalize(action);
	}
	
	public static final String getContainerPath(MetaDocument doc, MetaContainerProperty container, String action){
		return doc.getParentModule().getName().toLowerCase()+StringUtils.capitalize(doc.getName())+StringUtils.capitalize(container.getName())+StringUtils.capitalize(action);
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

	
	/**
	 * Creates an action mapping.
	 * @param path the path 
	 * @param type type of the redirect.
	 * @param forwardName Optional. If it is null, forward will not be generated for the action
	 * @param forwardPath Optional. If it is null, forward will not be generated for the action
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
		if(forwardName != null && forwardPath != null)
		{
			ret += writeString(" <forward name="+quote(forwardName)+" path="+quote(forwardPath)+"/>");
		}
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
		if(forwardName != null && forwardPath != null) {
			ret += writeIncreasedString("<forward name="+quote(forwardName)+" path="+quote(forwardPath)+"/>");
		}
		ret += writeString("</action>");
		return ret;
	}
	
	/**
	 * Generates action mapping with few forwards
	 * @param forwards Optional. If it is null, forwards will not be generated for the action
	 */
	private String generateActionMapping(String path, String type, Map<String,String> forwards){
		return generateActionMapping(path, type, "request", forwards);
	}
	
	
	private String generateActionMapping(String path, String type, String scope, Map<String,String> forwards){
		String ret = "";
		ret += writeString("<action path="+quote(path)+" type="+quote(type)+" scope="+quote(scope)+">");
		if (forwards != null) {
			for( Map.Entry<String, String> forwardEntry : forwards.entrySet()) {
				ret += writeIncreasedString("<forward name="+quote(forwardEntry.getKey())+" path="+quote(forwardEntry.getValue())+"/>");
			}
		}		
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
