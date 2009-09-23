package net.anotheria.asg.generator.view;

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
public class IndexPageActionGenerator extends AbstractGenerator {

	public FileEntry generate(List<MetaView> views) {
		return new FileEntry(generateBaseAction(views));
	}

	public static String getIndexPageActionName() {
		return "BaseIndexPageAction";
	}

	public GeneratedClass generateBaseAction(List<MetaView> views) {
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);

		clazz.setPackageName(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED) + ".action");

		clazz.addImport("javax.servlet.http.HttpServletRequest");
		clazz.addImport("javax.servlet.http.HttpServletResponse");
		emptyline();
		clazz.addImport("org.apache.struts.action.ActionForm");
		clazz.addImport("org.apache.struts.action.ActionForward");
		clazz.addImport("org.apache.struts.action.ActionMapping");

		clazz.setParent(BaseActionGenerator.getBaseActionName());
		clazz.setName(getIndexPageActionName());

		startClassBody();

		appendString("protected boolean isAuthorizationRequired() {");
		increaseIdent();
		appendStatement("return true");
		append(closeBlock());
		emptyline();

		appendString("protected void init(ActionMapping aMapping, ActionForm aAf, HttpServletRequest aReq, HttpServletResponse aRes) throws Exception {");
		increaseIdent();
		appendStatement("super.init(aMapping, aAf, aReq, aRes)");
		append(closeBlock());
		emptyline();

		appendString("public ActionForward anoDocExecute(ActionMapping aMapping, ActionForm aAf, HttpServletRequest aReq, HttpServletResponse aRes) throws Exception {");
		increaseIdent();
		appendStatement("return aMapping.findForward(\"success\")");
		append(closeBlock());
		emptyline();

		return clazz;
	}

}