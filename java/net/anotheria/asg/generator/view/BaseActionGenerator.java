package net.anotheria.asg.generator.view;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.model.ServiceGenerator;
import net.anotheria.asg.generator.view.meta.MetaModuleSection;
import net.anotheria.asg.generator.view.meta.MetaSection;
import net.anotheria.asg.generator.view.meta.MetaView;
import net.anotheria.asg.metafactory.Extension;
import net.anotheria.asg.metafactory.MetaFactory;
import net.anotheria.asg.metafactory.MetaFactoryException;
import net.anotheria.util.StringUtils;

/**
 * Generator class for the base action for a generator.
 * @author lrosenberg
 */
public class BaseActionGenerator extends AbstractGenerator {

	/**
	 * Generates all artefacts for this action.
	 * @param views
	 * @param context
	 * @return
	 */
	public FileEntry generate(List<MetaView> views , Context context) {
		return new FileEntry(generateBaseAction(context, views));
	}
	
	/**
	 * Returns the base action name for the application.
	 * @param context
	 * @return
	 */
	public static String getBaseActionName(Context context){
		return "Base"+StringUtils.capitalize(context.getApplicationName())+"Action";
	}
	
	/**
	 * Generates the base action.
	 * @param context
	 * @param views
	 * @return
	 */
	public GeneratedClass generateBaseAction(Context context, List<MetaView> views){

		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(context.getPackageName(MetaModule.SHARED)+".action");

		Collection<MetaModule> modules = GeneratorDataRegistry.getInstance().getModules();
		
		clazz.addImport("net.anotheria.webutils.actions.*");
		clazz.addImport("javax.servlet.http.HttpServletRequest");
		clazz.addImport("javax.servlet.http.HttpServletResponse");
		clazz.addImport("org.apache.struts.action.ActionForm");
		clazz.addImport("org.apache.struts.action.ActionForward");
		clazz.addImport("org.apache.struts.action.ActionMapping");

		clazz.setAbstractClass(true);
		clazz.setParent("BaseAction");
		clazz.setName(getBaseActionName(context));

		startClassBody();
		
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
		
		for (MetaModule m:modules)
			appendStatement("private static "+ServiceGenerator.getInterfaceName(m)+" "+ModuleActionsGenerator.getServiceInstanceName(m));

		appendStatement("private static XMLUserManager userManager");
		clazz.addImport("net.anotheria.webutils.service.XMLUserManager");

		appendString("static{");
		increaseIdent();
		
		clazz.addImport("org.apache.log4j.Logger");
		appendStatement("Logger staticlogger = Logger.getLogger("+getBaseActionName(context)+".class)");
		
		clazz.addImport(MetaFactory.class);
		clazz.addImport(Extension.class);
		clazz.addImport(MetaFactoryException.class);
		
		for (MetaModule m:modules){
			clazz.addImport(ServiceGenerator.getInterfaceImport(context, m));
			appendCommentLine(ModuleActionsGenerator.getServiceInstanceName(m)+" = "+ServiceGenerator.getFactoryName(m)+".create"+ServiceGenerator.getServiceName(m)+"()");
			appendString("try{");
			appendIncreasedStatement(ModuleActionsGenerator.getServiceInstanceName(m)+" = MetaFactory.get("+ServiceGenerator.getInterfaceName(m)+".class, Extension.EDITORINTERFACE)");
			appendString("}catch(MetaFactoryException e){");
			appendIncreasedStatement("staticlogger.fatal("+quote("Can't load editor instance of module service "+m.getName())+", e)");
			appendString("}");
		}

		//init user manager
		emptyline();
		appendComment("//initializing user manager");
		appendString("try{");
		appendIncreasedStatement("userManager = XMLUserManager.getInstance()");
		appendString("}catch(Exception e){");
		appendIncreasedStatement("staticlogger.fatal("+quote("Can't init user manager")+", e)");
		appendString("}");
		//end init user manager

	
		append(closeBlock());
		emptyline();
		
		appendString("public abstract ActionForward anoDocExecute(");
		increaseIdent();
		appendString("ActionMapping mapping,");
		appendString("ActionForm af,");
		appendString("HttpServletRequest req,");
		appendString("HttpServletResponse res)");
		appendString("throws Exception;");
		decreaseIdent();
		emptyline();

		appendString("public ActionForward doExecute(");
		increaseIdent();
		increaseIdent();
		appendString("ActionMapping mapping,");
		appendString("ActionForm af,");
		appendString("HttpServletRequest req,");
		appendString("HttpServletResponse res)");
		appendString("throws Exception{");
		decreaseIdent();
		appendString("if (isAuthorizationRequired()){");
		increaseIdent();
		appendStatement("boolean authorized = checkAuthorization(req)");
		appendString("if (!authorized){");
		increaseIdent();
		appendStatement("String queryString = req.getQueryString()");
		appendString("if (queryString!=null)");
		appendIncreasedStatement("queryString = \"?\"+queryString");
		appendString("else");
		appendIncreasedStatement("queryString = \"\"");
		appendStatement("addBeanToSession(req, BEAN_TARGET_ACTION, \""+context.getApplicationURLPath()+"/"+context.getServletMapping()+"\"+req.getPathInfo()+queryString)");
		appendStatement("String redUrl = "+quote(context.getApplicationURLPath()+"/"+context.getServletMapping()+"/login"));
		appendStatement("res.sendRedirect(redUrl)");
		appendStatement("return null");					
		append(closeBlock());	
		append(closeBlock());
		
		appendStatement("checkAccessPermissions(req)");
		emptyline();

		appendString("return anoDocExecute(mapping, af, req, res);");
		append(closeBlock());

		//generate service getter
		for (MetaModule m:modules){
			appendString("protected "+ServiceGenerator.getInterfaceName(m)+" "+ModuleActionsGenerator.getServiceGetterCall(m)+"{");
			increaseIdent();
			appendStatement("return "+ModuleActionsGenerator.getServiceInstanceName(m));
			append(closeBlock());
			emptyline();
		}
		
		//security...
		appendString("protected boolean isAuthorizationRequired(){");
		increaseIdent();
		appendStatement("return false");
		append(closeBlock());
		emptyline();
		
		clazz.addImport(List.class);
		clazz.addImport(ArrayList.class);
		appendStatement("private static final List<String> EMPTY_ROLE_LIST = new ArrayList<String>()");
		appendString("protected List<String> getRequiredRoles(){");
		increaseIdent();
		appendStatement("return EMPTY_ROLE_LIST");
		append(closeBlock());
		emptyline();

		appendString("protected boolean checkAuthorization(HttpServletRequest req){");
		increaseIdent();
		appendStatement("String userId = (String )getBeanFromSession(req, BEAN_USER_ID)");
		appendStatement("return userId!=null");
		append(closeBlock());
		emptyline();
		
		appendString("public String getSubsystem() {");
		increaseIdent();
		appendStatement("return "+quote("asg"));
		append(closeBlock());
		emptyline();
	
		appendString("protected String stripPath(String path){");
		increaseIdent();
		appendString("if (path==null || path.length()==0)");
		appendIncreasedStatement("throw new IllegalArgumentException("+quote("path null or empty")+")");
		appendStatement("return path.startsWith("+quote("/")+") ? path.substring(1) : path");
		append(closeBlock());
		emptyline();
		
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
		append(closeBlock());
		appendStatement("throw new RuntimeException("+quote("Permission denied, expected one of those: ")+"+requiredRoles)");
		append(closeBlock());
		emptyline();

		
/*		appendString("private boolean isUserInRole(HttpServletRequest req, String role){");
		increaseIdent();
		appendStatement("String userId = getUserId(req)");
		appendStatement("return userId==null ? false : userManager.userInRole(userId, role)");
		append(closeBlock());
		emptyline();
*/
		appendString("private boolean isUserInRole(HttpServletRequest req, String ... roles){");
		increaseIdent();
		appendStatement("String userId = getUserId(req)");
		appendString("if (userId==null)");
		appendIncreasedStatement("return false");
		appendString("for (String role : roles){");
		increaseIdent();
		appendString("if (userManager.userInRole(userId, role))");
		appendIncreasedStatement("return true");
		append(closeBlock());
		appendStatement("return false");
		append(closeBlock());

		
		appendString("private void prepareViewSelection(HttpServletRequest req){");
		increaseIdent();
		clazz.addImport("net.anotheria.webutils.bean.MenuItemBean");
		appendStatement("List<MenuItemBean> menu = new ArrayList<MenuItemBean>()");
		for (int i=0; i<views.size(); i++){
			MetaView view = views.get(i);
			MetaSection first = view.getSections().get(0);
			String statement = "menu.add(makeMenuItemBean("+quote(view.getTitle())+", "+quote(StrutsConfigGenerator.getPath(((MetaModuleSection)first).getDocument(), StrutsConfigGenerator.ACTION_SHOW))+"))";
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
		appendStatement("addBeanToRequest(req, BEAN_VIEW_SELECTOR, menu)");
		append(closeBlock());
		emptyline();

		appendString("protected void init(ActionMapping mapping, ActionForm af, HttpServletRequest req, HttpServletResponse res) throws Exception {");
		increaseIdent();
		appendStatement("super.init(mapping, af, req, res)");
		appendStatement("prepareViewSelection(req)");
		append(closeBlock());
		emptyline();

		appendString("private MenuItemBean makeMenuItemBean(String title, String link){");
		increaseIdent();
		appendString("MenuItemBean bean = new MenuItemBean();");
		appendString("bean.setCaption(title);");
		appendString("bean.setLink(link);");
		appendString("bean.setActive(true);");
		appendString("bean.setStyle(\"menuTitle\");");
		appendString("return bean;");
		append(closeBlock());
		emptyline();

		return clazz;
	}
}