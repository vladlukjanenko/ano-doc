package net.anotheria.asg.generator.view;


import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.model.ServiceGenerator;
import net.anotheria.asg.generator.view.meta.MetaModuleSection;
import net.anotheria.asg.generator.view.meta.MetaSection;
import net.anotheria.asg.generator.view.meta.MetaView;
import net.anotheria.util.StringUtils;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class BaseActionGenerator extends AbstractGenerator {

	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.generator.IGenerator#generate(net.anotheria.anodoc.generator.IGenerateable, net.anotheria.anodoc.generator.Context)
	 */
	public FileEntry generate(List<MetaView> views , Context context) {
		
		String ret = generateBaseAction(context, views);
		
		
		return new FileEntry(FileEntry.package2path(context.getPackageName(MetaModule.SHARED)+".action"), getBaseActionName(context),ret);
	}
	
	public static String getBaseActionName(Context context){
		return "Base"+StringUtils.capitalize(context.getApplicationName())+"Action";
	}
	
	public String generateBaseAction(Context context, List<MetaView> views){

		String ret = "";

		List<MetaModule> modules = new ArrayList<MetaModule>();
		for (MetaView view:views){
			List<MetaSection> sections = view.getSections();
			
			for (int i=0; i<sections.size(); i++){
				MetaSection section = sections.get(i);
				if (section instanceof MetaModuleSection){
					if (modules.indexOf(((MetaModuleSection)section).getModule())==-1){
						modules.add(((MetaModuleSection)section).getModule());
					}
				}
			}		
		}

		ret += writeStatement("package "+context.getPackageName(MetaModule.SHARED)+".action");
		ret += emptyline();
		
		ret += writeImport("net.anotheria.webutils.actions.*");
		ret += emptyline();

		ret += writeImport("javax.servlet.http.HttpServletRequest");
		ret += writeImport("javax.servlet.http.HttpServletResponse");
		ret += writeImport("org.apache.struts.action.ActionForm");
		ret += writeImport("org.apache.struts.action.ActionForward");
		ret += writeImport("org.apache.struts.action.ActionMapping");
		ret += emptyline();

		for (int i=0; i<modules.size(); i++){
			MetaModule m = modules.get(i);
			ret += writeImport(ServiceGenerator.getInterfaceImport(context, m));
			ret += writeImport(ServiceGenerator.getFactoryImport(context, m));
		}

		ret += emptyline();

		ret += writeString("public abstract class "+getBaseActionName(context)+" extends BaseAction{");
		ret += emptyline();
		increaseIdent();
		ret += writeStatement("public static final String PARAM_SORT_TYPE = "+quote(ViewConstants.PARAM_SORT_TYPE));
		ret += writeStatement("public static final String PARAM_SORT_TYPE_NAME = "+quote(ViewConstants.PARAM_SORT_TYPE_NAME));
		ret += writeStatement("public static final String PARAM_SORT_ORDER = "+quote(ViewConstants.PARAM_SORT_ORDER));
		ret += emptyline();

		//generate constants for session attributes
		ret += writeCommentLine("prefixes for session attributes.");
		ret += writeStatement("public static final String SA_PREFIX = "+quote(ViewConstants.SA_PREFIX));
		ret += writeStatement("public static final String SA_SORT_TYPE_PREFIX = SA_PREFIX+"+quote(ViewConstants.SA_SORT_TYPE_PREFIX));
		ret += writeStatement("public static final String SA_FILTER_PREFIX = SA_PREFIX+"+quote(ViewConstants.SA_FILTER_PREFIX));
		ret += emptyline();
		
		for (int i=0; i<modules.size(); i++){
			MetaModule m = modules.get(i);
			ret += writeStatement("private static "+ServiceGenerator.getInterfaceName(m)+" "+ModuleActionsGenerator.getServiceInstanceName(m));
		}

		ret += writeString("static{");
		increaseIdent();
		for (int i=0; i<modules.size(); i++){
			MetaModule m = (MetaModule)modules.get(i);
			ret += writeStatement(ModuleActionsGenerator.getServiceInstanceName(m)+" = "+ServiceGenerator.getFactoryName(m)+".create"+ServiceGenerator.getServiceName(m)+"()");
			
		}

		ret += closeBlock();
		ret += emptyline();
		
		ret += writeString("public abstract ActionForward anoDocExecute(");
		increaseIdent();
		ret += writeString("ActionMapping mapping,");
		ret += writeString("ActionForm af,");
		ret += writeString("HttpServletRequest req,");
		ret += writeString("HttpServletResponse res)");
		ret += writeString("throws Exception;");
		decreaseIdent();
		ret += emptyline();

		ret += writeString("public ActionForward doExecute(");
		increaseIdent();
		increaseIdent();
		ret += writeString("ActionMapping mapping,");
		ret += writeString("ActionForm af,");
		ret += writeString("HttpServletRequest req,");
		ret += writeString("HttpServletResponse res)");
		ret += writeString("throws Exception{");
		decreaseIdent();
		ret += writeString("if (isAuthorizationRequired()){");
		increaseIdent();
		ret += writeStatement("boolean authorized = checkAuthorization(req)");
		ret += writeString("if (!authorized){");
		increaseIdent();
		ret += writeStatement("String queryString = req.getQueryString()");
		ret += writeString("if (queryString!=null)");
		ret += writeIncreasedStatement("queryString = \"?\"+queryString");
		ret += writeString("else");
		ret += writeIncreasedStatement("queryString = \"\"");
		ret += writeStatement("addBeanToSession(req, BEAN_TARGET_ACTION, \""+context.getApplicationURLPath()+"/"+context.getServletMapping()+"\"+req.getPathInfo()+queryString)");
		ret += writeStatement("String redUrl = "+quote(context.getApplicationURLPath()+"/"+context.getServletMapping()+"/login"));
		ret += writeStatement("res.sendRedirect(redUrl)");
		ret += writeStatement("return null");					
		ret += closeBlock();	
		ret += closeBlock();
		
		ret += writeString("return anoDocExecute(mapping, af, req, res);");
		ret += closeBlock();

		//generate service getter
		for (int i=0; i<modules.size(); i++){
			MetaModule m = (MetaModule)modules.get(i);
			ret += writeString("protected "+ServiceGenerator.getInterfaceName(m)+" "+ModuleActionsGenerator.getServiceGetterCall(m)+"{");
			increaseIdent();
			ret += writeStatement("return "+ModuleActionsGenerator.getServiceInstanceName(m));
			ret += closeBlock();
			ret += emptyline();
		}
		
		//security...
		ret += writeString("protected boolean isAuthorizationRequired(){");
		increaseIdent();
		ret+= writeStatement("return false");
		ret += closeBlock();
		ret += emptyline();
		
		ret += writeString("protected boolean checkAuthorization(HttpServletRequest req){");
		increaseIdent();
		ret+= writeStatement("String userId = (String )getBeanFromSession(req, BEAN_USER_ID)");
		ret+= writeStatement("return userId!=null");
		ret += closeBlock();
		ret += emptyline();
		
		ret += writeString("public String getSubsystem() {");
		increaseIdent();
		ret += writeStatement("return "+quote("asg"));
		ret += closeBlock();
	

	
		ret += closeBlock();
		return ret;
	}
}