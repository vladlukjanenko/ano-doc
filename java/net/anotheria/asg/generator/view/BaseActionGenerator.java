package net.anotheria.asg.generator.view;


import java.util.Collection;
import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.model.ServiceGenerator;
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
		appendEmptyline();

		//generate constants for session attributes
		appendCommentLine("prefixes for session attributes.");
		appendStatement("public static final String SA_PREFIX = "+quote(ViewConstants.SA_PREFIX));
		appendStatement("public static final String SA_SORT_TYPE_PREFIX = SA_PREFIX+"+quote(ViewConstants.SA_SORT_TYPE_PREFIX));
		appendStatement("public static final String SA_FILTER_PREFIX = SA_PREFIX+"+quote(ViewConstants.SA_FILTER_PREFIX));
		appendEmptyline();
		
		for (MetaModule m:modules)
			appendStatement("private static "+ServiceGenerator.getInterfaceName(m)+" "+ModuleActionsGenerator.getServiceInstanceName(m));

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

		append(closeBlock());
		appendEmptyline();
		
		appendString("public abstract ActionForward anoDocExecute(");
		increaseIdent();
		appendString("ActionMapping mapping,");
		appendString("ActionForm af,");
		appendString("HttpServletRequest req,");
		appendString("HttpServletResponse res)");
		appendString("throws Exception;");
		decreaseIdent();
		appendEmptyline();

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
		
		appendString("return anoDocExecute(mapping, af, req, res);");
		append(closeBlock());

		//generate service getter
		for (MetaModule m:modules){
			appendString("protected "+ServiceGenerator.getInterfaceName(m)+" "+ModuleActionsGenerator.getServiceGetterCall(m)+"{");
			increaseIdent();
			appendStatement("return "+ModuleActionsGenerator.getServiceInstanceName(m));
			append(closeBlock());
			appendEmptyline();
		}
		
		//security...
		appendString("protected boolean isAuthorizationRequired(){");
		increaseIdent();
		appendStatement("return false");
		append(closeBlock());
		appendEmptyline();
		
		appendString("protected boolean checkAuthorization(HttpServletRequest req){");
		increaseIdent();
		appendStatement("String userId = (String )getBeanFromSession(req, BEAN_USER_ID)");
		appendStatement("return userId!=null");
		append(closeBlock());
		appendEmptyline();
		
		appendString("public String getSubsystem() {");
		increaseIdent();
		appendStatement("return "+quote("asg"));
		append(closeBlock());
		appendEmptyline();
	
		appendString("protected String stripPath(String path){");
		increaseIdent();
		appendString("if (path==null || path.length()==0)");
		appendIncreasedStatement("throw new IllegalArgumentException("+quote("path null or empty")+")");
		appendStatement("return path.startsWith("+quote("/")+") ? path.substring(1) : path");
		append(closeBlock());
		appendEmptyline();

		return clazz;
	}
}