package net.anotheria.asg.generator.view;

import java.util.ArrayList;
import java.util.List;


import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaModule;

/**
 * Generator class for the user settings beans in cms.
 * 
 * @author vkazhdan
 */
public class UserSettingsBeansGenerator extends AbstractGenerator {

	public List<FileEntry> generate() {
		
		List<FileEntry> entrys = new ArrayList<FileEntry>();
		
		entrys.add(new FileEntry(generateUserSettingsBean()));
		entrys.add(new FileEntry(generateUserSettingsManager()));
		entrys.add(new FileEntry(generateEditUserSettingsForm()));
		
		return entrys;
	}

	public static String getUserSettingsBeanName() {
		return "UserSettingsBean";
	}
	
	public static String getUserSettingsManagerName() {
		return "UserSettingsManager";
	}
	
	public static String getEditUserSettingsFormName() {
		return "EditUserSettingsForm";
	}

	
	/**
	 * Generate UserSettingsBean 
	 */
	public GeneratedClass generateUserSettingsBean() {
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);

		clazz.setPackageName(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.USER_SETTINGS) + ".bean");

		clazz.addImport("java.util.List");
		clazz.setClazzComment("User settings bean");
		clazz.setName(getUserSettingsBeanName());

		startClassBody();

		appendComment("Display all languages if true");		
		appendStatement("private boolean displayAllLanguages");
		appendComment("Displayed Languages. Optional if displayAllLanguages is true.");		
		appendStatement("private List<String> displayedLanguages");
		emptyline();
		
		appendString("public void setDisplayAllLanguages(boolean displayAllLanguages) {");
		increaseIdent();
		appendStatement("this.displayAllLanguages = displayAllLanguages");
		append(closeBlock());
		emptyline();
		
		appendString("public boolean isDisplayAllLanguages() {");
		increaseIdent();
		appendStatement("return displayAllLanguages");
		append(closeBlock());
		emptyline();
		
		appendString("public List<String> getDisplayedLanguages() {");
		increaseIdent();
		appendStatement("return displayedLanguages");
		append(closeBlock());
		emptyline();
		
		appendString("public void setDisplayedLanguages(List<String> displayedLanguages) {");
		increaseIdent();
		appendStatement("this.displayedLanguages = displayedLanguages");
		append(closeBlock());
		emptyline();
		
		return clazz;
	}
	
	
	
	/**
	 * Generate UserSettingsManager 
	 */
	public GeneratedClass generateUserSettingsManager() {
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);

		clazz.setPackageName(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.USER_SETTINGS) + ".bean");

		clazz.addImport("java.util.List");
		clazz.addImport("java.util.ArrayList");
		
		clazz.addImport("javax.servlet.http.Cookie");
		clazz.addImport("javax.servlet.http.HttpServletRequest");
		clazz.addImport("javax.servlet.http.HttpServletResponse");
		
		clazz.addImport("net.anotheria.util.StringUtils");
				
		clazz.setClazzComment("Class for manage User Settings");
		clazz.setName(getUserSettingsManagerName());

		startClassBody();

		appendComment("Displayed languages cookie name. Stored as \"ALL\" or \"L1,L2,L3,L4,...\"");		
		appendStatement("public static final String COOKIE_DISPLAYED_LANGUAGES = \"anosite.displayedLanguages\"");		
		emptyline();
		
		// loadFromCookies()
		appendComment("Load settings from cookies, or set to default if some of settings is not exists or incorrect");
		appendString("public static UserSettingsBean loadFromCookies(HttpServletRequest req) {");
		increaseIdent();
		appendStatement("UserSettingsBean ret = new UserSettingsBean()");
		appendStatement("List<String> displayedLanguages = new ArrayList<String>()");
		emptyline();
		
		appendCommentLine("Load displayed languages");					
		appendStatement("String displayedLanguagesString = getCookieValue(req.getCookies(),COOKIE_DISPLAYED_LANGUAGES,\"ALL\").replaceAll(\" \", \"\")");
		appendString("if (displayedLanguagesString.startsWith(\"ALL\")) {");
		increaseIdent();
		appendStatement("ret.setDisplayAllLanguages(true)");
		append(closeBlock());
		
		appendString("else {");
		increaseIdent();
		appendStatement("List<String> tokenizedDisplayedLanguages = StringUtils.tokenize2list(displayedLanguagesString, ',')");
		appendString("if(tokenizedDisplayedLanguages != null) {");
		increaseIdent();
		appendStatement("ret.setDisplayAllLanguages(false)");
		appendStatement("displayedLanguages = tokenizedDisplayedLanguages");			
		append(closeBlock());		
		appendString("else {");
		increaseIdent();
		appendStatement("ret.setDisplayAllLanguages(true)");					
		append(closeBlock());
		
		append(closeBlock());		
		
		appendStatement("ret.setDisplayedLanguages(displayedLanguages)");
		appendStatement("return ret");
						
		append(closeBlock());
		emptyline();

		
		// saveToCookies()
		appendComment("Save settings to cookies");
		appendString("public static void saveToCookies(UserSettingsBean userSettings, HttpServletResponse res) {");
		increaseIdent();
		appendStatement("String displayedLanguagesString");		
		emptyline();
		
		appendString("if (userSettings.isDisplayAllLanguages()) {");
		increaseIdent();
		appendStatement("displayedLanguagesString = \"ALL\"");				
		append(closeBlock());		
		appendString("else {");
		increaseIdent();
		appendStatement("displayedLanguagesString = userSettings.getDisplayedLanguages().toString().replaceAll(\"[ \\\\[\\\\]]\", \"\" )");					
		append(closeBlock());
			
		appendStatement("res.addCookie(new Cookie(COOKIE_DISPLAYED_LANGUAGES, displayedLanguagesString))");
		append(closeBlock());
		
			
		// getCookieValue()
		appendComment("Returns cookie value by cookie name" +
				"\n@param cookies" + 
				"\n@param name cookie name" + 
				"\n@param defaultValue if cookie with given name is not exists, default value will be returned" + 
				"\n@return cookie value, or defaultValue if cookie with given name is not exists");
		appendString("public static String getCookieValue(Cookie[] cookies, String name,	String defaultValue) {");
		increaseIdent();
		appendStatement("String retValue = defaultValue");		
		emptyline();
		
		appendString("if (cookies != null) {");
		increaseIdent();
		appendString("for (int i = 0; i < cookies.length; i++) {");
		increaseIdent();
		appendString("if(cookies[i].getName().equals(name)) {");
		increaseIdent();
		appendStatement("retValue = cookies[i].getValue()");
		appendStatement("break");
		append(closeBlock());
		append(closeBlock());
		append(closeBlock());		
		
		emptyline();
		appendStatement("return retValue");
		
		append(closeBlock());
		
		return clazz;
	}

	
	/**
	 * Generate EditUserSettingsForm 
	 */
	public GeneratedClass generateEditUserSettingsForm() {
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);

		clazz.setPackageName(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.USER_SETTINGS) + ".bean");

		clazz.addImport("net.anotheria.webutils.bean.BaseActionForm");
		
		clazz.setClazzComment("User settings bean");
		clazz.setParent("BaseActionForm");
		clazz.setName(getEditUserSettingsFormName());
		
		startClassBody();

				
		appendStatement("private static final long serialVersionUID = 1L");		
		appendComment("All supported languages");
		appendStatement("private String[] supportedLanguages");
		appendComment("Is display all languages");
		appendStatement("private boolean displayAllLanguages");
		appendComment("Displayed languages list. Optional if displayAllLanguages == true");
		appendStatement("private String[] displayedLanguages");
		appendComment("Referrer url. Used to forward user to last page before edit settings");
		appendStatement("private String referrer");		
		emptyline();
		
		
		appendString("public EditUserSettingsForm() {");
		increaseIdent();
		appendStatement("this.supportedLanguages = new String [] {}");
		appendStatement("this.displayedLanguages = new String [] {}");
		append(closeBlock());
		emptyline();
		
		appendString("public void setDisplayAllLanguages(boolean displayAllLanguages) {");
		increaseIdent();
		appendStatement("this.displayAllLanguages = displayAllLanguages");
		append(closeBlock());
		emptyline();
		
		appendString("public boolean isDisplayAllLanguages() {");
		increaseIdent();
		appendStatement("return displayAllLanguages");
		append(closeBlock());
		emptyline();
		
		appendString("public boolean getDisplayAllLanguages() {");
		increaseIdent();
		appendStatement("return displayAllLanguages");
		append(closeBlock());
		emptyline();
		
		appendString("public String[] getSupportedLanguages() {");
		increaseIdent();
		appendStatement("return supportedLanguages");
		append(closeBlock());
		emptyline();
		
		appendString("public void setSupportedLanguages(String[] supportedLanguages) {");
		increaseIdent();
		appendStatement("this.supportedLanguages = supportedLanguages");
		append(closeBlock());
		emptyline();
		
		appendString("public String[] getDisplayedLanguages() {");
		increaseIdent();
		appendStatement("return displayedLanguages");
		append(closeBlock());
		emptyline();
		
		appendString("public void setDisplayedLanguages(String[] displayedLanguages) {");
		increaseIdent();
		appendStatement("this.displayedLanguages = displayedLanguages");
		append(closeBlock());
		emptyline();
		
		appendString("public void setReferrer(String referrer) {");
		increaseIdent();
		appendStatement("this.referrer = referrer");
		append(closeBlock());
		emptyline();
		
		appendString("public String getReferrer() {");
		increaseIdent();
		appendStatement("return referrer");
		append(closeBlock());
		emptyline();
						
				
		return clazz;
	}
	
}