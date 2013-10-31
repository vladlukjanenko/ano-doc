package net.anotheria.asg.generator.view.action;


import net.anotheria.anodoc.util.context.ContextManager;
import net.anotheria.anoprise.metafactory.Extension;
import net.anotheria.anoprise.metafactory.MetaFactory;
import net.anotheria.anoprise.metafactory.MetaFactoryException;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.model.ServiceGenerator;
import net.anotheria.asg.generator.view.CMSMappingsConfiguratorGenerator;
import net.anotheria.asg.generator.view.ViewConstants;
import net.anotheria.asg.generator.view.meta.MetaCustomSection;
import net.anotheria.asg.generator.view.meta.MetaModuleSection;
import net.anotheria.asg.generator.view.meta.MetaSection;
import net.anotheria.asg.generator.view.meta.MetaView;
import net.anotheria.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Generator class for the base action for a generator.
 * @author lrosenberg
 */
public class BaseActionGenerator extends AbstractActionGenerator {

	/**
	 * Generates all artefacts for this action.
	 * @param views
	 * @return generated artifacts
	 */
	public FileEntry generate(List<MetaView> views) {
		return new FileEntry(generateBaseAction(views));
	}
	
	/**
	 * Generates the base action.
	 * @param views
	 * @return generated base action
	 */
	public GeneratedClass generateBaseAction(List<MetaView> views){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(getSharedActionPackageName());

		Collection<MetaModule> modules = GeneratorDataRegistry.getInstance().getModules();
		appendCommentLine(BaseActionGenerator.class.getName());
		clazz.addImport("net.anotheria.util.StringUtils");
		clazz.addImport(ContextManager.class);
		clazz.addImport("net.anotheria.webutils.actions.*");
		clazz.addImport("javax.servlet.http.HttpServletRequest");
		clazz.addImport("javax.servlet.http.HttpServletResponse");
		clazz.addImport("net.anotheria.maf.action.ActionForward");
		clazz.addImport("net.anotheria.maf.action.ActionMapping");
		clazz.addImport("net.anotheria.maf.bean.FormBean");	
		clazz.addImport("net.anotheria.webutils.bean.NavigationItemBean");	

		clazz.setAbstractClass(true);
		clazz.setParent("BaseAction");
		clazz.setGeneric("T extends FormBean");
		clazz.setName(getBaseActionName());

		startClassBody();
		
		appendGenerationPoint("generateBaseAction");
		
		appendStatement("public static final String BEAN_MAIN_NAVIGATION = \"mainNavigation\"");
		appendStatement("public static final String BEAN_SEARCH_SCOPE = \"searchScope\"");
		appendStatement("public static final String BEAN_DOCUMENT_DEF_NAME = \"documentName\"");
		appendStatement("public static final String BEAN_MODULE_DEF_NAME = \"moduleName\"");
		appendStatement("public static final String FLAG_DISABLED_SEARCH = \"disabledSearchFlag\"");
		appendStatement("public static final String PARAM_SORT_TYPE = "+quote(ViewConstants.PARAM_SORT_TYPE));
		appendStatement("public static final String PARAM_SORT_TYPE_NAME = "+quote(ViewConstants.PARAM_SORT_TYPE_NAME));
		appendStatement("public static final String PARAM_SORT_ORDER = "+quote(ViewConstants.PARAM_SORT_ORDER));
		emptyline();

		//generate constants for session attributes
		appendCommentLine("prefixes for session attributes.");
		appendStatement("public static final String SA_PREFIX = "+quote(ViewConstants.SA_PREFIX));
		appendStatement("public static final String SA_SORT_TYPE_PREFIX = SA_PREFIX+"+quote(ViewConstants.SA_SORT_TYPE_PREFIX));
		appendStatement("public static final String SA_FILTER_PREFIX = SA_PREFIX+"+quote(ViewConstants.SA_FILTER_PREFIX));
		emptyline();
		appendStatement("public static final String BEAN_VIEW_SELECTOR = "+quote("views"));
        emptyline();
        appendStatement("public static final String BEAN_USER_DEF_ID = "+quote("currentUserDefId"));
        emptyline();

        appendStatement("private static Object serviceInstantiationLock = new Object()");
		for (MetaModule m:modules)
			appendStatement("private static volatile "+ServiceGenerator.getInterfaceName(m)+" "+ModuleActionsGenerator.getServiceInstanceName(m));

		appendStatement("private static CMSUserManager userManager");
		clazz.addImport("net.anotheria.anosite.cms.user.CMSUserManager");
		clazz.addImport("net.anotheria.asg.util.locking.config.LockingConfig");
		appendStatement("private static LockingConfig lockConfig;");
		appendStatement("private static Logger log = LoggerFactory.getLogger("+getBaseActionName()+".class)");
		clazz.addImport("org.slf4j.Logger");
		clazz.addImport("org.slf4j.LoggerFactory");
		clazz.addImport("org.slf4j.MarkerFactory");
		appendString("static{");
		increaseIdent();
		
		clazz.addImport(MetaFactory.class);
		clazz.addImport(Extension.class);
		clazz.addImport(MetaFactoryException.class);
		
		
		for (MetaModule m:modules){
			clazz.addImport(ServiceGenerator.getInterfaceImport(m));
		}

		//init user manager
		emptyline();
		appendComment("//initializing user manager");
		appendString("try{");
		appendIncreasedStatement("userManager = CMSUserManager.getInstance()");
		appendString("}catch(Exception e){");
		appendIncreasedStatement("log.error(MarkerFactory.getMarker(\"FATAL\"), "+quote("Can't init user manager")+", e)");
		appendString("}");
		//end init user manager
		//initing Lock Config
		emptyline();
		appendComment("initializing lockConfig");
		appendString("try{");
		appendIncreasedStatement("lockConfig = LockingConfig.getInstance()");
		appendString("}catch(Exception e){");
		appendIncreasedStatement("log.error(MarkerFactory.getMarker(\"FATAL\"), "+quote("Can't init lockConfig")+", e)");
		appendString("}");
        // end initing Lock Config
	
		closeBlock("");
		emptyline();
		
		appendString("public void preProcess(ActionMapping mapping, HttpServletRequest req, HttpServletResponse res) throws Exception {");
		increaseIdent();
		appendString("super.preProcess(mapping, req, res);");
        emptyline();
        appendStatement("String userId = (String)getBeanFromSession(req, BEAN_USER_DEF_ID)");
        appendString("if (userId != null) {");
            increaseIdent();
                appendStatement("String login = CMSUserManager.getLoginById(userId)");
                appendStatement("addBeanToSession(req, BEAN_USER_ID, login)");
            closeBlock("if");
        emptyline();
        appendString("prepareMenu(req);");
		closeBlock("preProcess");
		emptyline();
		
		appendString("public abstract ActionForward anoDocExecute(ActionMapping mapping, T formBean, HttpServletRequest req, HttpServletResponse res) throws Exception;");
		emptyline();
		appendGenerationPoint("generateBaseAction");
		appendString("@Override");
		appendString("public ActionForward execute(ActionMapping mapping, FormBean formBean, HttpServletRequest req, HttpServletResponse res) throws Exception {");
		increaseIdent();
		appendString("if (isAuthorizationRequired()){");
			increaseIdent();
				appendStatement("boolean authorized = checkAuthorization(req)");
				appendString("if (!authorized){");
				increaseIdent();
				//build url.
					appendStatement("String url = req.getRequestURI()");
					appendStatement("String qs = req.getQueryString()");
					appendString("if (!StringUtils.isEmpty(qs))");
					appendIncreasedStatement("url += \"?\"+qs;");
					appendStatement("addBeanToSession(req, BEAN_TARGET_ACTION, url)");
					appendStatement("String redUrl = "+quote(GeneratorDataRegistry.getInstance().getContext().getApplicationURLPath()+"/"+GeneratorDataRegistry.getInstance().getContext().getServletMapping()+"/login"));
					appendStatement("res.sendRedirect(redUrl)");
					appendStatement("return null");		
				closeBlock("if");
			closeBlock("if");
        emptyline();
        appendStatement("checkAccessPermissions(req)");
		emptyline();
		appendStatement("addBeanToRequest(req, BEAN_DOCUMENT_DEF_NAME, getCurrentDocumentDefName())");
		appendStatement("addBeanToRequest(req, BEAN_MODULE_DEF_NAME, getActiveMainNavi())");
		emptyline();
		appendString("return anoDocExecute(mapping, (T) formBean, req, res);");
		closeBlock("execute");
		emptyline();
		//generate service getter
		for (MetaModule m:modules){
			appendString("protected "+ServiceGenerator.getInterfaceName(m)+" "+ModuleActionsGenerator.getServiceGetterCall(m)+"{");
			increaseIdent();
			appendString("if ("+ModuleActionsGenerator.getServiceInstanceName(m)+"==null){");
			increaseIdent();
			appendString("synchronized(serviceInstantiationLock){");
			increaseIdent();
			appendString("if ("+ModuleActionsGenerator.getServiceInstanceName(m)+"==null){");
			increaseIdent();
			appendString("try{");
			appendIncreasedStatement(ModuleActionsGenerator.getServiceInstanceName(m)+" = MetaFactory.get("+ServiceGenerator.getInterfaceName(m)+".class, Extension.EDITORINTERFACE)");
			appendString("}catch(MetaFactoryException e){");
			appendIncreasedStatement("log.error(MarkerFactory.getMarker(\"FATAL\"), "+quote("Can't load editor instance of module service "+m.getName())+", e)");
			appendString("}");
			closeBlock("... if null");
			closeBlock("... synch");
			closeBlock("... if");
			
			appendStatement("return "+ModuleActionsGenerator.getServiceInstanceName(m));
			closeBlock("end  "+ModuleActionsGenerator.getServiceGetterCall(m));
			emptyline();
		}
		
		//security...
		appendString("protected boolean isAuthorizationRequired(){");
		increaseIdent();
		appendStatement("return false");
		closeBlockNEW();
		emptyline();
		
		clazz.addImport(List.class);
		clazz.addImport(ArrayList.class);
		appendStatement("private static final List<String> EMPTY_ROLE_LIST = new ArrayList<String>()");
		appendString("protected List<String> getRequiredRoles(){");
		increaseIdent();
		appendStatement("return EMPTY_ROLE_LIST");
		closeBlockNEW();
		emptyline();

		appendString("protected boolean checkAuthorization(HttpServletRequest req){");
		increaseIdent();
		appendStatement("String userId = (String )getBeanFromSession(req, BEAN_USER_ID)");
		appendString("if(userId == null)");
		appendIncreasedStatement("return false");
		appendStatement("ContextManager.getCallContext().setCurrentAuthor(userId)");
		appendStatement("return true");
		closeBlock("checkAuthorization");
		emptyline();
		
		appendString("public String getSubsystem() {");
		increaseIdent();
		appendStatement("return "+quote("asg"));
		closeBlockNEW();
		emptyline();
	
		appendString("protected String stripPath(String path){");
		increaseIdent();
		appendString("if (path==null || path.length()==0)");
		appendIncreasedStatement("throw new IllegalArgumentException("+quote("path null or empty")+")");
		appendStatement("return path.startsWith("+quote("/")+") ? path.substring(1) : path");
		closeBlockNEW();
		emptyline();

		//  ading  2  methods  actually -- for  LockConfig ussage....
		appendString("protected long getLockingTimeout(){");
		increaseIdent();
		appendStatement("return lockConfig.getTimeout()");
		closeBlockNEW();
		emptyline();

		appendString("protected boolean isAutoLockingEnabled(){");
		increaseIdent();
		appendStatement("return lockConfig.isAutolocking()");
		closeBlockNEW();
		emptyline();

		//
		appendString("private void checkAccessPermissions(HttpServletRequest req){");
		increaseIdent();
		appendStatement("List<String> requiredRoles = getRequiredRoles()");
		appendString("if (requiredRoles==null || requiredRoles.size()==0)");
		appendIncreasedStatement("return");
		appendStatement("String userId = getUserId(req)");
		appendString("if (userId==null || userId.length()==0)");
		appendIncreasedStatement("throw new RuntimeException("+quote("Permission denied, uid not found!")+")");
		appendString("for (String role : requiredRoles){");
		increaseIdent();
		appendString("if (userManager.userInRole(userId, role))");
		appendIncreasedStatement("return");
		closeBlockNEW();
		appendStatement("throw new RuntimeException("+quote("Permission denied, expected one of those: ")+"+requiredRoles)");
		closeBlockNEW();
		emptyline();

		
/*		appendString("private boolean isUserInRole(HttpServletRequest req, String role){");
		increaseIdent();
		appendStatement("String userId = getUserId(req)");
		appendStatement("return userId==null ? false : userManager.userInRole(userId, role)");
		closeBlockNEW();
		emptyline();
*/
		appendString("protected boolean isUserInRole(HttpServletRequest req, String ... roles){");
		increaseIdent();
		appendStatement("String userId = getUserId(req)");
		appendString("if (userId==null)");
		appendIncreasedStatement("return false");
		appendString("for (String role : roles){");
		increaseIdent();
		appendString("if (userManager.userInRole(userId, role))");
		appendIncreasedStatement("return true");
		closeBlockNEW();
		appendStatement("return false");
		closeBlockNEW();
		
		appendString("protected void prepareMenu(HttpServletRequest req) {");
		increaseIdent();
		appendString("List<NavigationItemBean> navigation = getMainNavigation(req);");
		appendString("for (NavigationItemBean naviItem : navigation)");
		appendString("if (naviItem.isActive())");
		appendString("naviItem.setSubNavi(getSubNavigation());");
		appendString("addBeanToRequest(req, BEAN_MAIN_NAVIGATION, navigation);");
		closeBlockNEW();
		emptyline();

		appendString("protected abstract List<NavigationItemBean> getSubNavigation();");
		emptyline();
		
		appendString("protected List<NavigationItemBean> getMainNavigation(HttpServletRequest req) {");
		increaseIdent();
		appendString("List<NavigationItemBean> menu = new ArrayList<NavigationItemBean>();");
		for (int i=0; i<views.size(); i++){
			MetaView view = views.get(i);
			MetaSection first = view.getSections().get(0);
			
			String firstPath = null;
			if(first instanceof MetaModuleSection)
				firstPath = CMSMappingsConfiguratorGenerator.getPath(((MetaModuleSection)first).getDocument(), CMSMappingsConfiguratorGenerator.ACTION_SHOW);
			else if(first instanceof MetaCustomSection)
				firstPath = ((MetaCustomSection)first).getPath();
			else
				throw new RuntimeException("MetaSection extension Class: " + first.getClass());
			
			String statement = "menu.add(makeMenuItemBean("+quote(view.getTitle())+", "+quote(firstPath)+"))";
			if (view.getRequiredRoles()!=null && view.getRequiredRoles().size()>0){
				String roles = "";
				for (String r :view.getRequiredRoles()){
					if (roles.length()>0)
						roles += ", ";
					roles += quote(r);
				}
				appendString("if (isUserInRole(req, new String[]{"+roles+"})){");
				appendIncreasedStatement(statement);
				appendString("}");
			}else{
				appendStatement(statement);
			}
		}
	
		appendString("return menu;");
		closeBlockNEW();
		emptyline();
		
		appendString("protected abstract String getActiveMainNavi();");
		emptyline();
		
		appendString("protected abstract String getCurrentModuleDefName();");
		appendString("protected abstract String getCurrentDocumentDefName();");
		emptyline();
		
		appendComment("Get current application supported languages wrapper method.");
		appendString("public static List<String> getSupportedLanguages() {");
		increaseIdent();
		// Process multilanguage support
		if (GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported()) {
			clazz.addImport(GeneratorDataRegistry.getInstance().getContext().getServicePackageName(MetaModule.SHARED) + "." + StringUtils.capitalize(GeneratorDataRegistry.getInstance().getContext().getApplicationName()) + "LanguageUtils");
			appendStatement("return " + StringUtils.capitalize(GeneratorDataRegistry.getInstance().getContext().getApplicationName())
					+ "LanguageUtils.getSupportedLanguages()");			
		} else {
			appendStatement("return new ArrayList<String>()");
		}			
		closeBlockNEW();
		emptyline();
		
		
		appendString("private NavigationItemBean makeMenuItemBean(String title, String link){");
		increaseIdent();
		appendString("NavigationItemBean bean = new NavigationItemBean();");
		appendString("bean.setCaption(title);");
		appendString("bean.setLink(link);");
		appendString("bean.setActive(title.equals(getActiveMainNavi()));");
		appendString("return bean;");
		closeBlockNEW();
		emptyline();
		emptyline();
		emptyline();


		//// Actually Section  for  Session working with  Session attribute -- Locking && unlocking!!!

		appendComment("Actually Session Attribute name for holding collection of locked objects....");
		appendStatement("private static final String LOCKED_OBJECTS_COLLECTION_SESSION_ATTRIBUTE_NAME = \"lokedDocumentsCollection\"");
		clazz.addImport("java.util.Collection");
		clazz.addImport("java.util.List");
		clazz.addImport("java.util.ArrayList");
		clazz.addImport("net.anotheria.asg.data.AbstractASGDocument");
		clazz.addImport("java.io.Serializable");
		emptyline();
		emptyline();

		appendComment("Adding attribute to the session list as <a>LockedDocumentAttribute</a>. for Locking && Unlocking functionality");
		appendString("protected void addLockedAttribute(HttpServletRequest req, AbstractASGDocument doc) {");
		increaseIdent();
		appendStatement("List<LockedDocumentAttribute> attributes = getLockedAttributesList(req)");
		appendStatement("LockedDocumentAttribute docAtt = new LockedDocumentAttribute(doc)");
		appendString("if (!attributes.contains(docAtt)) {");
		appendIncreasedStatement("attributes.add(docAtt)");
		appendIncreasedStatement("refreshSessionLockedAtribute(req, attributes)");
		appendString("}");
		closeBlockNEW();
		emptyline();

		appendComment("Putting attribute to the HttpSession.");
        appendString("private void refreshSessionLockedAtribute(HttpServletRequest req, List<LockedDocumentAttribute> attributes) {");
		increaseIdent();
		appendStatement("req.getSession().setAttribute(LOCKED_OBJECTS_COLLECTION_SESSION_ATTRIBUTE_NAME, attributes)");
        closeBlockNEW();
		emptyline();

		appendComment("Removing attributes List<LockedDocumentAttribute> - from session.");
		appendString("protected void removeLockedAttribute(HttpServletRequest req, AbstractASGDocument doc) {");
		increaseIdent();
		appendStatement("List<LockedDocumentAttribute> attributes = getLockedAttributesList(req)");
		appendStatement("LockedDocumentAttribute docAtt = new LockedDocumentAttribute(doc)");
		appendString("if (attributes.contains(docAtt)) {");
		appendIncreasedStatement("attributes.remove(docAtt)");
		appendIncreasedStatement("refreshSessionLockedAtribute(req, attributes)");
		appendString("}");
		closeBlockNEW();
		emptyline();

		appendComment("Return true if current document represented as  <a>LockedDocumentAttribute</a>. in session attributes list.");
		appendString("protected boolean containsLockedAttribute(HttpServletRequest req, AbstractASGDocument doc) {");
		increaseIdent();
		appendStatement("List<LockedDocumentAttribute> attributes = getLockedAttributesList(req)");
		appendStatement("LockedDocumentAttribute docAtt = new LockedDocumentAttribute(doc)");
	    appendStatement("return attributes.contains(docAtt)");
		closeBlockNEW();
		emptyline();


		appendComment("Getting attributes List<LockedDocumentAttribute> - from session.");
		appendString("@SuppressWarnings(\"unchecked\")");
		appendString("private List<LockedDocumentAttribute> getLockedAttributesList(HttpServletRequest req) {");
		increaseIdent();
		appendStatement("List<LockedDocumentAttribute> attributes = null");
		appendStatement("Object sessionBean = req.getSession().getAttribute(LOCKED_OBJECTS_COLLECTION_SESSION_ATTRIBUTE_NAME)");
		appendString("if (sessionBean != null && sessionBean instanceof Collection<?>){ ");
		appendIncreasedStatement("attributes = (List<LockedDocumentAttribute>) sessionBean");
		appendString("}");
	    appendStatement("return attributes == null ? new ArrayList<LockedDocumentAttribute>() : attributes");
		closeBlockNEW();
		emptyline();
		emptyline();

		appendComment("Actually simplest been - which shoul hold information  about document ID  && Clazz.");
		appendString("public static class LockedDocumentAttribute implements Serializable{");
		emptyline();
		increaseIdent();
		appendComment("SerialVersionUID UID.");
		appendStatement("private static final long serialVersionUID = 1L");
		appendComment("LockedDocumentAttribute \"docId\".");
		appendStatement("private String docId");
		emptyline();
		appendComment("LockedDocumentAttribute \"documentClazz\".");
		appendStatement("private Class<? extends AbstractASGDocument> documentClazz");
		emptyline();

		appendComment("Public constructor.");
		appendString("public LockedDocumentAttribute(AbstractASGDocument doc) {");
		increaseIdent();
		appendStatement("this.docId = doc.getId()");
        appendStatement("this.documentClazz = doc.getClass()");
		closeBlockNEW();
		emptyline();

		appendString("public String getDocId() {");
		increaseIdent();
		appendStatement("return docId");
		closeBlockNEW();
		emptyline();

		appendString("public Class<? extends AbstractASGDocument> getDocumentClazz() {");
		increaseIdent();
		appendStatement("return documentClazz");
		closeBlockNEW();
		emptyline();

		appendString("public void setDocId(String aDocId) {");
		increaseIdent();
		appendStatement("this.docId = aDocId");
		closeBlockNEW();
		emptyline();

		appendString("public void setDocumentClazz(Class<? extends AbstractASGDocument> aDocumentClazz) {");
		increaseIdent();
		appendStatement("this.documentClazz = aDocumentClazz");
		closeBlockNEW();
		emptyline();				

		appendString("public boolean equals(Object o) {");
		increaseIdent();
		appendStatement("return o!=null && (this == o || o instanceof LockedDocumentAttribute && ((LockedDocumentAttribute) o).getDocId().equals(getDocId()) && \n \t\t\t\t ((LockedDocumentAttribute) o).getDocumentClazz().equals(getDocumentClazz()))");
		closeBlockNEW();
		emptyline();


		appendString("public int hashCode() {");
		increaseIdent();
		appendStatement("int result = docId != null ? docId.hashCode() : 0");
		appendStatement("final int mult = 31");
		appendStatement("result = mult * result + (documentClazz != null ? documentClazz.hashCode() : 0)");
		appendStatement("return result");
		closeBlockNEW();
		emptyline();

		appendString("public String toString() {");
		increaseIdent();
		appendStatement("final StringBuilder sb = new StringBuilder()");
		appendStatement("sb.append(\"LockedDocumentAttribute\")");
		appendStatement("sb.append(\"{docId='\").append(docId).append('\\'')");
		appendStatement("sb.append(\", documentClazz=\").append(documentClazz)");
		appendStatement("sb.append('}')");
		appendStatement("return sb.toString()");
		closeBlockNEW();
		emptyline();


		emptyline();
		closeBlockNEW();

		return clazz;
	}

}
