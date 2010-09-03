package net.anotheria.asg.generator.view;

import java.util.Collections;
import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.view.meta.MetaView;

/**
 * Generator class for the index page action in cms.
 * 
 * @author abolbat
 */
public class IndexPageMafActionGenerator extends AbstractGenerator {

	public FileEntry generate(List<MetaView> views) {
		return new FileEntry(generateBaseAction(views));
	}

	public static String getIndexPagePackageName() {
		return GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED) + ".action";
	}
	
	public static String getIndexPageActionName() {
		return "WelcomePageMafAction";
	}
	
	public static String getIndexPageFullName() {
		return getIndexPagePackageName() + "." + getIndexPageActionName();
	}

	public GeneratedClass generateBaseAction(List<MetaView> views) {
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		appendGenerationPoint("generateBaseMafAction");
		clazz.setPackageName(getIndexPagePackageName());

		
		clazz.addImport("javax.servlet.http.HttpServletRequest");
		clazz.addImport("javax.servlet.http.HttpServletResponse");
		emptyline();
		clazz.addImport("net.anotheria.maf.action.ActionForward");
		clazz.addImport("net.anotheria.maf.action.ActionMapping");
		clazz.addImport("net.anotheria.maf.bean.FormBean");
		clazz.addImport("net.anotheria.webutils.bean.NavigationItemBean");
		clazz.addImport("java.util.Collections");
		clazz.addImport("java.util.List");

		clazz.setParent(BaseMafActionGenerator.getBaseMafActionName());
		clazz.setName(getIndexPageActionName());

		startClassBody();

		appendString("protected boolean isAuthorizationRequired() {");
		increaseIdent();
		appendStatement("return true");
		append(closeBlock());
		emptyline();

		appendString("public ActionForward anoDocExecute(ActionMapping aMapping, FormBean aAf, HttpServletRequest aReq, HttpServletResponse aRes) throws Exception {");
		increaseIdent();
		appendStatement("return aMapping.findForward(\"success\")");
		append(closeBlock());
		emptyline();
		
		appendString("@Override");
		increaseIdent();
		appendStatement("protected String getActiveMainNavi() {");
		appendStatement("return null");
		append(closeBlock());
		emptyline();
		
		appendString("@Override");
		appendString("protected List<NavigationItemBean> getSubNavigation() {");
		increaseIdent();
		appendStatement("return Collections.emptyList()");
		append(closeBlock());
		emptyline();
		
		return clazz;
	}

}