package net.anotheria.asg.generator.view;

import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedJSPFile;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaModule;

/**
 * Generator class for the Edit User Settings Dialog JSP page in cms
 * 
 * @author vkazhdan
 */
public class UserSettingsJspGenerator extends AbstractJSPGenerator{
	
	/**
	 * userSettingsUpdateActionName
	 */		
	private final String userSettingsUpdateActionName = "userSettingsUpdate";
	
	
	public FileEntry generate() {		
		return new FileEntry(generateEditUserSettingsDialog());		
	}
	
	public static String getEditUserSettingsDialogName() {
		return "EditUserSettingsDialog";
	}
	
	public static String getEditUserSettingsDialogTitle() {
		return "Edit User Settings";
	}
	
	private GeneratedJSPFile generateEditUserSettingsDialog(){
		
		GeneratedJSPFile jsp = new GeneratedJSPFile();
		startNewJob(jsp);
		
		jsp.setPackage(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.USER_SETTINGS)+".jsp");
		jsp.setName(getEditUserSettingsDialogName());
		
		append(getBaseJSPHeader());
		
		appendString("<html:html>");
		increaseIdent();
		appendString("<head>");
		increaseIdent();
		appendString("<title>"+getEditUserSettingsDialogTitle()+"</title>");
		
		appendString("<link rel=" + quote("stylesheet") + " type=" + quote("text/css") + " href=" + quote(getCurrentYUIPath("core/build/reset-fonts-grids/reset-fonts-grids.css")) + " />");
		appendString("<link href=\""+getCurrentCSSPath("admin.css")+"\" rel=\"stylesheet\" type=\"text/css\">");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote("http://code.jquery.com/jquery-latest.js") + "></script>");		
		decreaseIdent();		
		appendString("</head>");
		
		appendString("<body class=\"editDialog yui-skin-sam\">");
		increaseIdent();
		appendString("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
		increaseIdent();
		appendString("<tr>");
		appendIncreasedString("<td class=\"menuTitleSelected\">");
		appendIncreasedString(getEditUserSettingsDialogTitle()+"</td>");
		appendString("</tr>");
		decreaseIdent();
		appendString("</table>");
		
		appendString("<br/><br/><br/>");
		tagOpen("div", "yui-content");
		tagOpen("html:form", new TagAttribute("action", userSettingsUpdateActionName));
		appendString("<input id=\"referrer\" type=\"hidden\" name=\"referrer\" value=\"<bean:write name=\"EditUserSettingsForm\" property=\"referrer\"/>\"/>");
		tagOpen("div", "yui-gd lineDark");
		tagOpen("div", "yui-u first");
		appendString("Display All Languages");
		tagClose("div");		
		tagOpen("div", "yui-u");
		appendString("<input id=\"displayAllLanguages\"	type=\"checkbox\" name=\"displayAllLanguages\"");
		appendString("<logic:equal name=\"EditUserSettingsForm\" property=\"displayAllLanguages\" value=\"true\"> checked=\"checked\"</logic:equal> />");
		tagClose("div");
		tagClose("div");
		
		tagOpen("div", "yui-gd lineLight",new TagAttribute("id","displayedLanguages"));
		tagOpen("div", "yui-u first");
		appendString("<br/>Displayed languages");
		tagClose("div");		
		tagOpen("div", "yui-u",new TagAttribute("id","displayedLanguagesInputs"));
		appendString("<button id=\"selectAll\" onclick=\"return false;\">Select All</button> <button id=\"deselectAll\" onclick=\"return false;\">Deselect All</button>");
		appendString("<br/>");
		appendString("<logic:iterate id='lang' name=\"EditUserSettingsForm\" property=\"supportedLanguages\">");
		increaseIdent();
		appendString("<html:multibox property=\"displayedLanguages\">");
		appendIncreasedString("<bean:write name=\"lang\" />");
		appendString("</html:multibox>");
		appendIncreasedString("<bean:write name=\"lang\" />");
		decreaseIdent();
		appendString("</logic:iterate>");		
		tagClose("div");
		tagClose("div");
		
		appendString("</br>");
		tagOpen("div", "functions",new TagAttribute("id","Functions"));
		appendString("&nbsp;&raquo&nbsp;<a href=\"#\" onClick=\"document.EditUserSettingsForm.submit(); return false\">SaveAndClose</a>");
		appendString("&nbsp;&raquo&nbsp;<a href=\"<bean:write name=\"EditUserSettingsForm\" property=\"referrer\"/>\">Close</a>");
		tagClose("div");
		
		tagClose("html:form");
		tagClose("div");
		
		decreaseIdent();
		appendString("</body>");
		
		// Script		
		appendString("<script type=\"text/javascript\">");
		increaseIdent();
		
		appendString("enableDisableDisplayedLanguages = function() {");
		increaseIdent();
		appendString("$(\"div#displayedLanguagesInputs\").find(\"input\").each(function() {");
		appendIncreasedString("$(this).attr(\"disabled\", $(\"#displayAllLanguages\").attr(\"checked\") );");
		appendString("});");
		
		appendString("var fadeTo = $(\"#displayAllLanguages\").attr(\"checked\") ? 0.5 : 1;");
		appendString("$(\"div#displayedLanguages\").fadeTo('slow', fadeTo);");
		emptyline();
		appendString("$(\"#selectAll\").attr( \"disabled\", $(\"#displayAllLanguages\").attr(\"checked\") );");
		appendString("$(\"#deselectAll\").attr( \"disabled\", $(\"#displayAllLanguages\").attr(\"checked\") );");
		decreaseIdent();
		appendString("}");		
				
		appendString("$(document).ready(function() {");
		increaseIdent();
		appendString("enableDisableDisplayedLanguages();");
		appendString("$(\"#displayAllLanguages\").click( function(event) {");
		appendIncreasedString("enableDisableDisplayedLanguages();");
		appendString("});");
		
		appendString("$(\"#selectAll\").click( function(event) {");
		increaseIdent();
		appendString("$(\"div#displayedLanguagesInputs\").find(\"input\").each(function() {");
		appendIncreasedString("$(this).attr( \"checked\", true );");
		appendString("});");
		decreaseIdent();
		appendString("});");
		
		appendString("$(\"#deselectAll\").click( function(event) {");
		increaseIdent();
		appendString("$(\"div#displayedLanguagesInputs\").find(\"input\").each(function() {");
		appendIncreasedString("$(this).attr( \"checked\", false );");
		appendString("});");
		decreaseIdent();
		appendString("});");
		decreaseIdent();
		appendString("});");		
		decreaseIdent();
		appendString("</script>");
		
		decreaseIdent();
		appendString("</html:html>");
		
		append(getBaseJSPFooter());
		
		
		return jsp;
	}

}
