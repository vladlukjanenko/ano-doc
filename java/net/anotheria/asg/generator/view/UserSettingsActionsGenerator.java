package net.anotheria.asg.generator.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.view.meta.MetaView;

/**
 * Generator class for the UserSettings actions in cms.
 * 
 * @author vkazhdan
 */
public class UserSettingsActionsGenerator extends AbstractGenerator {

	public List<FileEntry> generate() {
		
		List<FileEntry> entrys = new ArrayList<FileEntry>();
		
		entrys.add(new FileEntry(generateEditUserSettingsAction()));
		entrys.add(new FileEntry(generateEditUserSettingsDialogAction()));
		
		return entrys;
	}

	public static String getEditUserSettingsActionName() {
		return "EditUserSettingsAction";
	}
	
	public static String getEditUserSettingsDialogActionName() {
		return "EditUserSettingsDialogAction";
	}

	
	/**
	 * Generate EditUserSettingsAction
	 */
	public GeneratedClass generateEditUserSettingsAction() {
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);

		clazz.setPackageName(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.USER_SETTINGS) + ".action");
		
		clazz.addImport("javax.servlet.http.HttpServletRequest");
		clazz.addImport("javax.servlet.http.HttpServletResponse");
		emptyline();
		clazz.addImport("org.apache.struts.action.ActionForm");
		clazz.addImport("org.apache.struts.action.ActionForward");
		clazz.addImport("org.apache.struts.action.ActionMapping");
		
		clazz.addImport(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED) + ".action.BaseActionsAction");
		clazz.addImport(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.USER_SETTINGS) + ".bean.EditUserSettingsForm");
		clazz.addImport(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.USER_SETTINGS) + ".bean.UserSettingsBean");
		clazz.addImport(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.USER_SETTINGS) + ".bean.UserSettingsManager");
		
		
		clazz.setParent("BaseActionsAction");
		clazz.setName(getEditUserSettingsActionName());

		startClassBody();

		appendStatement("public static final String PARAM_REFERRER = \"referrer\"");
		appendStatement("public static final String FORWARD_REFERRER = \"defaultReferrer\"");
		emptyline();
		
		
		appendString("public ActionForward anoDocExecute(ActionMapping mapping, ActionForm af, HttpServletRequest req, HttpServletResponse res) throws Exception {");
		increaseIdent();
		appendStatement("String forward = \"success\"");
		appendStatement("String [] supportedLanguages = getSupportedLanguages().toArray(new String[0])");
		appendStatement("EditUserSettingsForm form = new EditUserSettingsForm()");
		appendStatement("form.setSupportedLanguages(supportedLanguages)");
		emptyline();
		
		// Set referrerUrl
		appendCommentLine("Set referrerUrl");
		appendStatement("String referrerUrl = req.getParameter(PARAM_REFERRER)");
		appendString("if (referrerUrl == null) {");
		increaseIdent();
		appendStatement("ActionForward defaultActionForward = mapping.findForward(FORWARD_REFERRER)");
		appendString("if (defaultActionForward != null) {");
		increaseIdent();
		appendStatement("referrerUrl = defaultActionForward.getPath()");
		append(closeBlock());
		append(closeBlock());
		
		appendString("if (referrerUrl == null) {");
		increaseIdent();
		appendStatement("referrerUrl = \"\"");
		append(closeBlock());
		
		appendStatement("log.debug(\"Set form referrer: \" + referrerUrl)");
		appendStatement("form.setReferrer(referrerUrl)");
		emptyline();
					
		// Set current user settings		
		appendCommentLine("Set current user settings");
		appendStatement("UserSettingsBean userSettings = UserSettingsManager.loadFromCookies(req)");
		appendString("if (userSettings != null) {");
		increaseIdent();
		appendStatement("form.setDisplayAllLanguages(userSettings.isDisplayAllLanguages())");
		appendString("if (!userSettings.isDisplayAllLanguages()) {");
		increaseIdent();
		appendStatement("form.setDisplayedLanguages(userSettings.getDisplayedLanguages().toArray(new String[0]))");
		append(closeBlock());
		appendString("else {");
		increaseIdent();
		appendStatement("form.setDisplayedLanguages(supportedLanguages)");
		append(closeBlock());
		append(closeBlock());
		
		appendString("else {");
		increaseIdent();
		appendCommentLine("Set default values if settings wasn't created yet");
		appendStatement("form.setDisplayAllLanguages(true)");
		appendStatement("form.setDisplayedLanguages(supportedLanguages)");
		append(closeBlock());
		emptyline();
		
		appendStatement("addBeanToRequest(req, \"EditUserSettingsForm\", form)");
		appendStatement("return mapping.findForward(forward)");
		append(closeBlock());
		emptyline();
		
		appendString("protected String getTitle() {");
		increaseIdent();		
		appendStatement("return \"Edit User Settings\"");		
		append(closeBlock());
		
		
		return clazz;
	}
	
	/**
	 * Generate EditUserSettingsDialogAction
	 */
	public GeneratedClass generateEditUserSettingsDialogAction() {
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);

		clazz.setPackageName(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.USER_SETTINGS) + ".action");

		clazz.addImport("java.util.Arrays");
		clazz.addImport("javax.servlet.http.HttpServletRequest");
		clazz.addImport("javax.servlet.http.HttpServletResponse");
		emptyline();
		clazz.addImport("org.apache.struts.action.ActionForm");
		clazz.addImport("org.apache.struts.action.ActionForward");
		clazz.addImport("org.apache.struts.action.ActionMapping");
		
		clazz.addImport(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED) + ".action.BaseActionsAction");
		clazz.addImport(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.USER_SETTINGS) + ".bean.EditUserSettingsForm");
		clazz.addImport(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.USER_SETTINGS) + ".bean.UserSettingsBean");
		clazz.addImport(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.USER_SETTINGS) + ".bean.UserSettingsManager");
		
		clazz.setParent("BaseActionsAction");
		clazz.setName(getEditUserSettingsDialogActionName());

		startClassBody();
		
		appendString("public ActionForward anoDocExecute(ActionMapping mapping, ActionForm af, HttpServletRequest req, HttpServletResponse res) throws Exception {");
		increaseIdent();
		appendStatement("EditUserSettingsForm form = (EditUserSettingsForm) af");
		appendStatement("String forwardUrl");		
		emptyline();
		

		appendString("if (form != null) {");
		increaseIdent();
		appendStatement("UserSettingsBean userSettings = new UserSettingsBean()");
		appendStatement("userSettings.setDisplayAllLanguages(form.getDisplayAllLanguages())");
		appendStatement("userSettings.setDisplayedLanguages(Arrays.asList(form.getDisplayedLanguages()))");
		appendStatement("UserSettingsManager.saveToCookies(userSettings, res)");
		
		appendString("if( form.getReferrer() != null) {");
		increaseIdent();
		appendStatement("log.debug(\"Redirect to referrer: \" + form.getReferrer())");
		appendStatement("forwardUrl = form.getReferrer()");
		append(closeBlock());
		appendString("else {");
		increaseIdent();
		appendStatement("log.debug(\"Referrer URL wasn't setted correctly. Redirecting to root.\")");
		appendStatement("forwardUrl = \"\"");
		append(closeBlock());
		append(closeBlock());
		
		appendString("else {");
		increaseIdent();		
		appendStatement("log.warn(\"Can't update user settings. EditUserSettingsForm bean must not be null. Redirecting to root.\")");
		appendStatement("forwardUrl = \"\"");
		append(closeBlock());
		emptyline();
				
		appendStatement("res.sendRedirect(forwardUrl)");
		appendStatement("return null");
		append(closeBlock());
		emptyline();
		
		
		appendString("protected String getTitle() {");
		increaseIdent();		
		appendStatement("return \"Edit User Settings Dialog Action\"");		
		append(closeBlock());
		
		return clazz;
	}
	
	

}