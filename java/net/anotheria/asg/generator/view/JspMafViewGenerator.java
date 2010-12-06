package net.anotheria.asg.generator.view;

import java.util.ArrayList;
import java.util.List;





import net.anotheria.asg.data.LockableObject;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedJSPFile;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.forms.meta.MetaForm;
import net.anotheria.asg.generator.forms.meta.MetaFormField;
import net.anotheria.asg.generator.forms.meta.MetaFormSingleField;
import net.anotheria.asg.generator.forms.meta.MetaFormTableColumn;
import net.anotheria.asg.generator.forms.meta.MetaFormTableField;
import net.anotheria.asg.generator.forms.meta.MetaFormTableHeader;
import net.anotheria.asg.generator.meta.MetaContainerProperty;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaEnumerationProperty;
import net.anotheria.asg.generator.meta.MetaLink;
import net.anotheria.asg.generator.meta.MetaListProperty;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.MetaProperty;
import net.anotheria.asg.generator.meta.MetaTableProperty;
import net.anotheria.asg.generator.meta.StorageType;
import net.anotheria.asg.generator.util.DirectLink;
import net.anotheria.asg.generator.view.CMSMappingsConfiguratorGenerator.ContainerAction;
import net.anotheria.asg.generator.view.CMSMappingsConfiguratorGenerator.SectionAction;
import net.anotheria.asg.generator.view.meta.MetaCustomFunctionElement;
import net.anotheria.asg.generator.view.meta.MetaDialog;
import net.anotheria.asg.generator.view.meta.MetaEmptyElement;
import net.anotheria.asg.generator.view.meta.MetaFieldElement;
import net.anotheria.asg.generator.view.meta.MetaFilter;
import net.anotheria.asg.generator.view.meta.MetaFunctionElement;
import net.anotheria.asg.generator.view.meta.MetaListElement;
import net.anotheria.asg.generator.view.meta.MetaModuleSection;
import net.anotheria.asg.generator.view.meta.MetaSection;
import net.anotheria.asg.generator.view.meta.MetaView;
import net.anotheria.asg.generator.view.meta.MetaViewElement;
import net.anotheria.asg.generator.view.meta.MultilingualFieldElement;
import net.anotheria.util.StringUtils;

/**
 * Generates the jsps for the edit view.
 * @author another
 */
public class JspMafViewGenerator extends AbstractMafJSPGenerator implements IGenerator{
	
	/**
	 * userSettingsActionName
	 */		
	final String userSettingsEditActionName = "userSettingsEdit";
	/**
	 * Currently generated section.
	 */
	private MetaSection currentSection;
	/**
	 * Currently generated dialog.
	 */
	private MetaDialog currentDialog;

	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.generator.IGenerator#generate(net.anotheria.anodoc.generator.IGenerateable, net.anotheria.anodoc.generator.Context)
	 */
	public List<FileEntry> generate(IGenerateable g) {
		try{
		List<FileEntry> files = new ArrayList<FileEntry>();
		MetaView view = (MetaView)g;
		
		files.add(new FileEntry(generateMenu(view)));
		files.add(new FileEntry(generateFooter(view, FOOTER_SELECTION_CMS, getFooterName(view))));
		files.add(new FileEntry(generateSearchPage()));

		FileEntry versionInfoPage = new FileEntry(generateVersionInfoPage());
		versionInfoPage.setType(".jsp");
		files.add(versionInfoPage);
		
		for (int i=0; i<view.getSections().size(); i++){
			MetaSection s = view.getSections().get(i);
			if (!(s instanceof MetaModuleSection))
				continue;
			MetaModuleSection section = (MetaModuleSection)s;
			files.add(new FileEntry(generateShowPage(section, view)));
			files.add(new FileEntry(generateCSVExport(section, view)));
			files.add(new FileEntry(generateXMLExport(section, view)));

			FileEntry linksToThisFile = new FileEntry(generateLinksToDocument(section, view));
			linksToThisFile.setType(".jsp");
			files.add(linksToThisFile);
			
			List<MetaDialog> dialogs = section.getDialogs();
			for (int d=0; d<dialogs.size(); d++){
				MetaDialog dialog = dialogs.get(d);
				
				files.add(new FileEntry( generateDialog(dialog, section, view)));
			}

			MetaDocument doc = section.getDocument();
			for (int p=0; p<doc.getProperties().size(); p++){
				MetaProperty pp = doc.getProperties().get(p);
				if (pp instanceof MetaContainerProperty){
				    FileEntry entry = new FileEntry(generateContainerPage((MetaModuleSection)section, doc, (MetaContainerProperty)pp)); 
				    files.add(entry);
				}
			}
			
			
			
		}
		return files;
		}catch(NullPointerException e){
			e.printStackTrace();
			throw e;
		}
	}
	
	private String getTopMenuPage(){
		return "../../shared/jsp/"+JspMafMenuGenerator.getMenuPageName();		
	}

	private String getMenuName(MetaView view){
		return "../../shared/jsp/"+StringUtils.capitalize(view.getName())+"MenuMaf";		
	}
	
	private String getFooterName(MetaView view){
		return "../../shared/jsp/"+StringUtils.capitalize(view.getName())+"FooterMaf";		
	}


	private GeneratedJSPFile generateMenu(MetaView view){
		
		GeneratedJSPFile jsp = new GeneratedJSPFile();
		startNewJob(jsp);
		
		jsp.setPackage(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED)+".jsp");
		jsp.setName(getMenuName(view));
		
		append(getBaseJSPHeader());
		
		appendString("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
		increaseIdent();
		
		openTR();
		appendString("<logic:iterate name=\"menu\" type=\"net.anotheria.webutils.bean.MenuItemBean\" id=\"entry\">");
		appendString("<td>");
		increaseIdent();
		appendString("<logic:equal name=\"entry\" property=\"active\" value=\"true\">");
		appendIncreasedString("<td class=\"menuTitleSelected\"><bean:write name=\"entry\" property=\"caption\"/></td>");
		appendString("</logic:equal>");
		appendString("<logic:notEqual name=\"entry\" property=\"active\" value=\"true\">");
		appendIncreasedString("<td class=\"menuTitle\"><a href=\"<ano:tslink><bean:write name=\"entry\" property=\"link\"/></ano:tslink>\"><bean:write name=\"entry\" property=\"caption\"/></a></td>");
		appendString("</logic:notEqual>");
		decreaseIdent();
		appendString("</td>");
		decreaseIdent();
		appendString("</logic:iterate>");
		closeTR();

		decreaseIdent();
		appendString("</table>");
		
		append(getBaseJSPFooter());
		return jsp;
	}
	

	private GeneratedJSPFile generateContainerPage(MetaModuleSection section, MetaDocument doc, MetaContainerProperty p){
		if (p instanceof MetaListProperty)
			return generateListPage(section, doc, (MetaListProperty)p);
		if (p instanceof MetaTableProperty)
			return generateTablePage(section, doc, (MetaTableProperty)p);
		throw new RuntimeException("Unsupported container: "+p);
	}
	
	/**
	 * Generates user settings JSP imports 
	 * @return
	 */
	private String getUserSettingsJSPImports(){
		String ret = "";
		ret += "<%@page import=" 
			+ quote(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.USER_SETTINGS) + ".bean.UserSettingsBean")
			+ "%>" + CRLF;
		ret += "<%@page import=\"java.net.URLEncoder\"%>" + CRLF;
		ret += "<%@page import=\"org.apache.commons.lang.ArrayUtils\"%>" + CRLF;
		
		return ret;
	}
	
	private GeneratedJSPFile generateListPage(MetaModuleSection section, MetaDocument doc, MetaListProperty list){
		
		GeneratedJSPFile jsp = new GeneratedJSPFile();
		startNewJob(jsp);
		
		jsp.setName(getContainerPageName(doc, list));
		jsp.setPackage(getContext().getJspPackageName(section.getModule()));
		
		resetIdent();
		
		String addFormAction = ContainerAction.ADD.getMappingName(doc, list);
		String addFormName = addFormAction + "ElementForm";

		String quickAddFormAction = ContainerAction.QUICKADD.getMappingName(doc, list);
		String quickAddFormName = quickAddFormAction + "Form";
		
		MetaProperty p = list.getContainedProperty();

		append(getBaseJSPHeader());
		
		appendString("<!--  generated by JspMafViewGenerator.generateListPage -->");	
		appendString("<html:html>");
			increaseIdent();
			appendString("<head>");
				increaseIdent();
				appendString("<title>Edit "+doc.getName()+StringUtils.capitalize(list.getName())+"</title>");
				generatePragmas();
				appendString("<link href=\""+getCurrentCSSPath("newadmin.css")+"\" rel=\"stylesheet\" type=\"text/css\">");
				appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/yahoo-dom-event/yahoo-dom-event.js")) + "></script>");
				appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/container/container-min.js")) + "></script>");
				appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/element/element-min.js")) + "></script>");
				appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/datasource/datasource-min.js")) + "></script>");
				appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/autocomplete/autocomplete-min.js")) + "></script>");
				appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("anoweb/widget/ComboBox.js")) + "></script>");
				appendString("<script type=\"text/javascript\" src=\""+getCurrentJSPath("jquery-1.4.min.js")+"\"></script>");
				appendString("<script type=\"text/javascript\" src=\""+getCurrentJSPath("anofunctions.js")+"\"></script>");
			decreaseIdent();
			appendString("</head>");
			appendString("<body>");
			appendString("<jsp:include page=\""+getTopMenuPage()+"\" flush=\"true\"/>");
		
			appendString("<div class=\"right\">");
				increaseIdent();
				appendString("<div class=\"r_w\">");
					increaseIdent();
					appendString("<div class=\"top_nav\">");
						increaseIdent();
						appendString("<div class=\"r_b_l\"><!-- --></div>");
						appendString("<div class=\"r_b_r\"><!-- --></div>");
						appendString("<div class=\"left_p\">");
							increaseIdent();
							appendString("<div class=\"clear\"><!-- --></div>");
						decreaseIdent();
						
						String backButtonHref = SectionAction.EDIT.getMappingName(section);
						backButtonHref += "?pId=<bean:write name=\"ownerId\"/>";
						appendString("<a href=" + backButtonHref + " class=\"button\"><span>Back to "+section.getDocument().getName()+"</span></a>");
						
						// SAVE AND CLOSE BUTTONS SHOULD BE HERE
						appendString("</div>");
					decreaseIdent();
					appendString("</div>");
					
					appendString("<div class=\"main_area\">");
					appendString("<div class=\"c_l\"><!-- --></div>");
					appendString("<div class=\"c_r\"><!-- --></div>");
					appendString("<div class=\"c_b_l\"><!-- --></div>");
					appendString("<div class=\"c_b_r\"><!-- --></div>");
					appendString("<h2>Add new item</h2>");
					appendString("<div class=\"clear\"><!-- --></div>");
			
		appendString("<table class=\"pages_table\" width="+quote("100%")+" cellspacing="+quote("1")+" cellpadding="+quote("1")+" border="+quote("0")+">");
		appendString("<thead>");
		appendString("<tr class="+quote("lineCaptions")+">");
		appendIncreasedString("<td style=\"width:50px;\">Pos</td>");
		appendIncreasedString("<td style=\"width:50px;\">"+StringUtils.capitalize(list.getName())+"</td>");
		appendIncreasedString("<td>"+"Description"+"</td>");
		appendIncreasedString("<td width=\"100\">&nbsp</td>");
		appendString("</tr>");
		
		appendString("</thead>");
		appendString("<tbody>");
		appendString("<logic:iterate name="+quote("elements")+" id="+quote("element")+" type="+quote(ModuleMafBeanGenerator.getContainerEntryFormImport(doc, list))+" indexId="+quote("ind")+">");
		increaseIdent();
		appendString("<tr class=\"<%=ind.intValue()%2==0 ? \"lineLight\" : \"lineDark\"%> highlightable\">");
		increaseIdent();
		appendString("<td><bean:write name="+quote("element")+" property="+quote("position")+"/></td>");
		
		
		if (p.isLinked()){
			
			MetaLink link2p = (MetaLink)p;
			MetaModule targetModule = link2p.getLinkTarget().indexOf('.')== -1 ?
					doc.getParentModule() : GeneratorDataRegistry.getInstance().getModule(link2p.getTargetModuleName());

			MetaDocument linkTarget = targetModule.getDocumentByName(link2p.getTargetDocumentName());
			String targetLinkAction = SectionAction.EDIT.getMappingName(linkTarget);
			
			appendString("<td><a href=<ano:tslink>"+quote(targetLinkAction+"?pId=<bean:write name="+quote("element")+" property="+quote(list.getContainedProperty().getName())+"/></ano:tslink>")+"><bean:write name="+quote("element")+" property="+quote(list.getContainedProperty().getName())+"/></a></td>");
			appendString("<td><a href=<ano:tslink>"+quote(targetLinkAction+"?pId=<bean:write name="+quote("element")+" property="+quote(list.getContainedProperty().getName())+"/></ano:tslink>")+"><bean:write name="+quote("element")+" property="+quote("description")+"/></a></td>");
		}else{
			appendString("<td><bean:write name="+quote("element")+" property="+quote(list.getContainedProperty().getName())+"/></td>");
			appendString("<td><bean:write name="+quote("element")+" property="+quote("description")+"/></td>");
		}
		
		
		String parameter = "ownerId=<bean:write name="+quote("element")+" property="+quote("ownerId")+"/>";
		parameter += "&pPosition=<bean:write name="+quote("element")+" property="+quote("position")+"/>";
		appendString("<td>");
		appendIncreasedString("<a href="+quote(ContainerAction.MOVE.getMappingName(doc, list) + "?dir=top&"+parameter)+">"+getTopImage("move to top")+"</a>");
		appendIncreasedString("<a href="+quote(ContainerAction.MOVE.getMappingName(doc, list) + "?dir=up&"+parameter)+">"+getUpImage("move up")+"</a>");
		appendIncreasedString("<a href="+quote(ContainerAction.MOVE.getMappingName(doc, list) + "?dir=down&"+parameter)+">"+getDownImage("move down")+"</a>");
		appendIncreasedString("<a href="+quote(ContainerAction.MOVE.getMappingName(doc, list) + "?=bottom&"+parameter)+">"+getBottomImage("move to bottom")+"</a>");
		appendIncreasedString("<a href="+quote(ContainerAction.DELETE.getMappingName(doc, list) + "?"+parameter)+">"+getDeleteImage("delete row")+"</a>");
		appendString("</td>");
		decreaseIdent();
		appendString("</tr>");
		decreaseIdent();
		appendString("</logic:iterate>");
		decreaseIdent();
		appendString("</tbody>");
		appendString("</table>");
		
		String name = p.getName();
		if (name==null || name.length()==0)
			name = "&nbsp;";
		
		appendString("<table width="+quote("100%")+" cellspacing="+quote("0")+" cellpadding="+quote("0")+" border="+quote("0")+">");
		appendString("<tbody>");
		increaseIdent();
		
		appendString("<tr>");
		appendIncreasedString("<td align=\"right\">Add&nbsp;"+name+": </td>");
		appendString("<td align=\"left\">");
		appendString("<form name="+quote(addFormName)+" action="+quote(addFormAction)+" method=\"post\">");
		appendString("<input type="+quote("hidden")+" name="+quote("ownerId")+" value=\"<bean:write name="+quote("ownerId")+"/>\">");
	
		if (!p.isLinked() && !(p instanceof MetaEnumerationProperty)){
			String field = "";
			field += "<input class=\"add_id\" type=\"text\" style=\"width:25%\" name="+quote(name);
			field += " value=\"<bean:write name="+quote(addFormAction)+" property="+quote(name)+"/>";
			field += "\">";
			appendIncreasedString(field);
		}else{
			appendString("<em id=\""+ "Value\" name=\"" + list.getContainedProperty().getName().toLowerCase() + "\" class=\"selectBox fll mr_10\">&nbsp;none</em><div id=\""+"ValuesSelector\"></div>");
		}
		appendString("<a href="+quote("#")+" class=\"button\" onClick="+quote("document."+addFormName+".submit()")+"><span>Add</span></a>");
		appendString("</form>");
		appendString("</td>");
		decreaseIdent();
		appendString("</tr>");

		//QUICK ADD Form 
		if (p.isLinked()){
			increaseIdent();
			appendString("<tr>");
			appendString("<td align=\"right\">");
			appendString("Quick&nbsp;add:");
			appendString("</td>");
			appendString("<td align=\"left\">");
			appendString("<form name="+quote(quickAddFormName)+" action="+quote(quickAddFormAction)+" method=\"post\">");
			increaseIdent();
			appendString("<input type="+quote("hidden")+" name="+quote("ownerId")+" value=\"<bean:write name="+quote("ownerId")+"/>\">");
			
			
			p = list.getContainedProperty();
			
			name = p.getName();
			if (name==null || name.length()==0)
				name = "&nbsp;";
			String field = "";
			field += "<input class=\"add_id fll\" type=\"text\" style=\"width:25%;\" name="+quote("quickAddIds");
			field += " value=\"\"/><span class=\"fll mr_10 mt_4 mt_5\">id's comma separated list.</span>";
			appendString(field);
			decreaseIdent();
			decreaseIdent();
			appendString("<a href="+quote("#")+" class=\"button\" onClick="+quote("document."+quickAddFormName+".submit()")+"><span>QuickAdd</span></a>");
			appendString("</form>");
			appendString("</td>");
			appendString("</tr>");
		}
		//QUICK ADD END

		appendString("</tbody>");
		appendString("</table>");
		decreaseIdent();
		appendString("<div class=\"clear\"><!-- --></div>");
		appendString("</div>");
		appendString("</div>");
		appendString("</div>");
		
		appendString("</body>");
		decreaseIdent();
		appendString("</html:html>");
		appendString("<!-- / generated by JspMafViewGenerator.generateListPage -->");
		generateListLinkElementEditorJS(section.getDocument(), name.toLowerCase()+"ValuesCollection");
		
		append(getBaseJSPFooter());
		return jsp;

	}
	
	private GeneratedJSPFile generateTablePage(MetaModuleSection section, MetaDocument doc, MetaTableProperty table){
		
		GeneratedJSPFile jsp = new GeneratedJSPFile();
		startNewJob(jsp);
		jsp.setName(getContainerPageName(doc, table));
		
	    resetIdent();
	    
	    List<MetaProperty> columns = table.getColumns();
	    String formName = StrutsConfigGenerator.getContainerEntryFormName(doc, table);
	    
		append(getBaseJSPHeader());
		
		appendString("<!--  generated by JspMafViewGenerator.generateTablePage -->");
		appendString("<html:html>");
		increaseIdent();
		appendString("<head>");
		increaseIdent();
		appendString("<title>Edit "+doc.getName()+StringUtils.capitalize(table.getName())+"</title>");
		generatePragmas();
		appendString("<link href=\""+getCurrentCSSPath("newadmin.css")+"\" rel=\"stylesheet\" type=\"text/css\">");
		decreaseIdent();
		appendString("</head>");
		appendString("<body>");
		increaseIdent();		
		
		appendString("<table width="+quote("100%")+" cellspacing="+quote("1")+" cellpadding="+quote("1")+" border="+quote("0")+">");
		increaseIdent();
		appendString("<tr class="+quote("lineCaptions")+">");
	    appendIncreasedString("<td width=\"1%\">Pos</td>");
		for (int i=0; i<columns.size(); i++){
		    MetaProperty p = (MetaProperty)columns.get(i);
		    appendIncreasedString("<td>"+StringUtils.capitalize(table.extractSubName(p))+"</td>");
		}
	    appendIncreasedString("<td width=\"1%\">&nbsp;</td>");
		appendString("</tr>");
		appendString("<logic:iterate name="+quote("rows")+" id="+quote("row")+" type="+quote(ModuleBeanGenerator.getContainerEntryFormImport(doc, table))+" indexId="+quote("ind")+">");
		increaseIdent();
		appendString("<tr class=\"<%=ind.intValue()%2==0 ? \"lineLight\" : \"lineDark\"%>\">");
		increaseIdent();
		appendString("<td width="+quote("1%")+"><bean:write name="+quote("row")+" property="+quote("position")+"/></td>");
		for (int i=0; i<columns.size(); i++){
		    MetaProperty p = (MetaProperty)columns.get(i);
		    appendString("<td><bean:write name="+quote("row")+" property="+quote(table.extractSubName(p))+"/></td>");
		}
		String parameter = "pId=<bean:write name="+quote("row")+" property="+quote("ownerId")+"/>";
		parameter += "&pPosition=<bean:write name="+quote("row")+" property="+quote("position")+"/>";
		appendString("<td width="+quote("1%")+"><a href="+quote(StrutsConfigGenerator.getContainerPath(doc, table, StrutsConfigGenerator.ACTION_DELETE)+"?"+parameter)+">"+getDeleteImage("delete row")+"</a></td>");
		decreaseIdent();
		appendString("</tr>");
		decreaseIdent();
		appendString("</logic:iterate>");
		decreaseIdent();
		appendString("</table>");
		appendString("<br>");
		decreaseIdent();
		appendString("<table width="+quote("100%")+" cellspacing="+quote("1")+" cellpadding="+quote("1")+" border="+quote("0")+">");
		increaseIdent();
		appendString("<html:form action="+quote(StrutsConfigGenerator.getContainerPath(doc, table, StrutsConfigGenerator.ACTION_ADD))+">");
		appendString("<html:hidden property="+quote("ownerId")+"/>"); 
		appendString("<input type="+quote("hidden")+" name="+quote("pId")+" value=\"<bean:write name="+quote(formName)+" property="+quote("ownerId")+"/>\">"); 
		appendString("<tr class="+quote("lineCaptions")+">");
	    appendIncreasedString("<td colspan="+quote("2")+">Add row:</td>");
	    appendString("</tr>");
	    for (int i=0; i<columns.size()+2; i++){
	        appendString("<tr class="+quote(i%2==0 ? "lineLight" : "lineDark")+">");
			increaseIdent();
	        
	        if (i<columns.size()){
				MetaProperty p = (MetaProperty)columns.get(i);
				appendString("<td align=\"right\" width=\"35%\">");
				increaseIdent();
				String name = table.extractSubName(p);
				if (name==null || name.length()==0)
					name = "&nbsp;";
				appendString(name+":");
				decreaseIdent(); 
				appendString("</td>");
				decreaseIdent();
	
				appendString("<td align=\"left\" width=\"65%\">&nbsp;");
				String field = "";
				field += "<input type=\"text\" name="+quote(name);
				field += " value=\"<bean:write name="+quote(StrutsConfigGenerator.getContainerEntryFormName(doc,table ))+" property="+quote(name)+"/>";
				field += "\">";
				appendIncreasedString(field);
				appendString("</td>");
	        }else{
	            if (i==columns.size()){
					appendString("<td colspan="+quote("2")+">&nbsp;");
	            }else{
					appendString("<td align=\"right\" width=\"35%\">&nbsp;</td>");
					appendString("<td align=\"left\" width=\"65%\">");
					appendIncreasedString("<a href="+quote("#")+" onClick="+quote("document."+formName+".submit()")+">&nbsp;&raquo&nbsp;Add&nbsp;</a>");
					appendString("</td>");

	            }
	        }
	        decreaseIdent();
	        appendString("</tr>");
	    }
	    appendString("</html:form>");

	    
	    appendString("</body>");
		decreaseIdent();
		appendString("</html:html>");
		appendString("<!-- / generated by JspMafViewGenerator.generateTablePage -->");
		
		append(getBaseJSPFooter());
		
	    
		return jsp;
	}
	
	private GeneratedJSPFile generateDialog(MetaDialog dialog, MetaModuleSection section, MetaView view){
		
		GeneratedJSPFile jsp = new GeneratedJSPFile();
		startNewJob(jsp);
		jsp.setName(getDialogName(dialog, section.getDocument()));
		jsp.setPackage(getContext().getJspPackageName(section.getModule()));
		
		resetIdent();
		currentDialog = dialog;
		
		append(getBaseJSPHeader());
		append(getUserSettingsJSPImports());
		
		// Language filtering settings
		generateProcessLanguageFilteringSettings();
		
		appendGenerationPoint("generateDialog");
		appendString("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"");
		appendString("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
		appendString("<html:html>");
		increaseIdent();
		appendString("<head>");
		increaseIdent();
		appendString("<title>"+dialog.getTitle()+"</title>");
		generatePragmas(view);
		//appendString("<link href=\""+getCurrentCSSPath("admin.css")+"\" rel=\"stylesheet\" type=\"text/css\">");
		//*** CMS2.0 START ***
		
		appendString("<link rel=" + quote("stylesheet") + " type=" + quote("text/css") + " href=" + quote(getCurrentYUIPath("core/build/fonts/fonts-min.css")) + " />");
		appendString("<link rel=" + quote("stylesheet") + " type=" + quote("text/css") + " href=" + quote(getCurrentYUIPath("core/build/assets/skins/sam/skin.css")) + " />");
		appendString("<link rel=" + quote("stylesheet") + " type=" + quote("text/css") + " href=" + quote(getCurrentYUIPath("core/build/container/assets/skins/sam/container.css")) + " />");
		appendString("<link href=\""+getCurrentCSSPath("newadmin.css")+"\" rel=\"stylesheet\" type=\"text/css\"/>");

		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/yahoo-dom-event/yahoo-dom-event.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/container/container-min.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/menu/menu-min.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/element/element-min.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/button/button-min.js")) + "></script>");
		
//		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/animation/animation-min.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/datasource/datasource-min.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/autocomplete/autocomplete-min.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/dragdrop/dragdrop-min.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("anoweb/widget/ComboBox.js")) + "></script>");
		//*** CMS2.0 FINISH ***
		
		//*** CMS3.0 START ***
		appendString("<script type=\"text/javascript\" src=\""+getCurrentJSPath("jquery-1.4.min.js")+"\"></script>");
		appendString("<script type=\"text/javascript\" src=\""+getCurrentJSPath("anofunctions.js")+"\"></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentJSPath("tiny_mce/tiny_mce.js")) + "></script>");
		//*** CMS3.0 FINISH ***
		
		decreaseIdent();
		appendString("</head>");
		appendString("<body>");
		appendString("<jsp:include page=\""+getTopMenuPage()+"\" flush=\"true\"/>");
		appendString("<div class=\"right\">");
		appendString("<div class=\"r_w\">");
		increaseIdent();
		appendString("<div class=\"top_nav\">");
		increaseIdent();
		appendString("<div class=\"r_b_l\"><!-- --></div>");
		appendString("<div class=\"r_b_r\"><!-- --></div>");
		appendString("<div class=\"left_p\">");
		increaseIdent();
		appendString("<ul>");
		increaseIdent();
		appendString("<li class=\"first\">Scroll to:&nbsp;</li>");
		List<MetaViewElement> elements = createMultilingualList(dialog.getElements(),section.getDocument()); 
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			while (elements.get(i) instanceof MultilingualFieldElement){
				appendString("<logic:equal name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, section.getDocument()))+" property="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" value="+quote("true")+">");
				appendString("<li><a href=\"#"+element.getName()+"DEF"+"\">"+element.getName()+"DEF"+"</a></li>");
				appendString("</logic:equal>");
				appendString("<logic:notEqual name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, section.getDocument()))+" property="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" value="+quote("true")+">");
				appendString("<li>");
				increaseIdent();
				String lang = getElementLanguage(element);
				appendString("<a href=\"#"+section.getDocument().getField(element.getName()).getName(lang)+"\">"+element.getName()+"</a><a href=\"javascript:void(0);\" class=\"open_pop\">&nbsp;&nbsp;&nbsp;</a>");
				appendString("<div class=\"pop_up\">");
				increaseIdent();
				appendString("<div class=\"top\">");
				increaseIdent();
				appendString("<div><!-- --></div>");
				decreaseIdent();
				appendString("</div>");
				appendString("<div class=\"in_l\">");
				increaseIdent();
				appendString("<div class=\"in_r\">");
										increaseIdent();
											appendString("<div class=\"in_w\">");
											increaseIdent();
												appendString("<ul>");
												for (String sl : GeneratorDataRegistry.getInstance().getContext().getLanguages()){
													appendString("<li class=\"lang_"+sl+" lang_hide\"><a href=\"#"+section.getDocument().getField(element.getName()).getName(sl)+"\">"+sl+"</a></li>");
													i++;
													element = elements.get(i);
												}
													appendString("</ul>");
												decreaseIdent();
												appendString("</div>");
											decreaseIdent();
											appendString("</div>");
										decreaseIdent();
										appendString("</div>");
										appendString("<div class=\"bot\">");
										appendString("<div><!-- --></div>");
										appendString("</div>");
									decreaseIdent();
									appendString("</div>");
								decreaseIdent();
								appendString("</li>");
								appendString("</logic:notEqual>");
							}
							if(element instanceof MetaFieldElement){
							appendString("<li><a href=\"#"+element.getName()+"\">"+element.getName()+"</a></li>");
							}
						}
					decreaseIdent();
					appendString("</ul>");
					appendString("<div class=\"clear\"><!-- --></div>");
				decreaseIdent();
				for (int i=0; i<elements.size(); i++){
					MetaViewElement element = elements.get(i);
					if (element instanceof MetaListElement)	
						append(getElementEditor(section.getDocument(), element));
					
				}
				// SAVE AND CLOSE BUTTONS SHOULD BE HERE
				appendString("</div>");
				
				
				if (GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported() && section.getDocument().isMultilingual()) {
				appendString("<div class=\"right_p\"><a href=\"#\"><img src=\"../cms_static/img/settings.gif\" alt=\"\"/></a>");
				increaseIdent();
					appendString("<div class=\"pop_up\">");
					increaseIdent();
						appendString("<div class=\"top\">");
							increaseIdent();
							appendString("<div><!-- --></div>");
						decreaseIdent();
						appendString("</div>");
						appendString("<div class=\"in_l\">");
							increaseIdent();
							appendString("<div class=\"in_r\">");
								increaseIdent();
								appendString("<div class=\"in_w\">");
									increaseIdent();
									// *** START MULILINGUAL COPY *** //
									int colspan=2;
										addMultilanguageOperations(section, colspan);
									// *** END MULILINGUAL COPY *** //
									appendString("</div>");
									appendString("</div>");
								decreaseIdent();
								appendString("</div>");
							decreaseIdent();
							appendString("<div class=\"bot\">");
							appendIncreasedString("<div><!-- --></div>");
							appendString("</div>");
						decreaseIdent();
						appendString("</div>");
					decreaseIdent();
					appendString("</div>");
				}
				decreaseIdent();
				appendString("</div>");
				
				
				appendString("<div class=\"main_area\">");
				appendString("<div class=\"c_l\"><!-- --></div>");
				appendString("<div class=\"c_r\"><!-- --></div>");
				appendString("<div class=\"c_b_l\"><!-- --></div>");
				appendString("<div class=\"c_b_r\"><!-- --></div>");

				String entryName = quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection) currentSection).getDocument()));
				String result = "<logic:equal name=" + entryName + " property=" + quote(LockableObject.INT_LOCK_PROPERTY_NAME) + " value=" + quote("false") + "> \n";
				String path = StrutsConfigGenerator.getPath(((MetaModuleSection) currentSection).getDocument(), StrutsConfigGenerator.ACTION_LOCK);
				path += "?pId=<bean:write name=" + entryName + " property=\"id\"/>" + "&nextAction=showEdit";
				result += "<a href=\"#\" onClick= "+quote("lightbox('All unsaved data will be lost!!!<br /> Really lock  "+
						StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection) currentSection).getDocument())+" with id: <bean:write name="
						+ entryName + " property=\"id\"/>?','<ano:tslink>" + path + "</ano:tslink>');")+">"+getLockImage()+"&nbsp;Lock</a>";
				result += "</logic:equal>";
				appendString(result);
				
				if (StorageType.CMS.equals(((MetaModuleSection) currentSection).getDocument().getParentModule().getStorageType())) {
					appendString("<logic:equal name=" + entryName + " property=" + quote(LockableObject.INT_LOCK_PROPERTY_NAME) + " value=" + quote("true") + ">");
					
					path = StrutsConfigGenerator.getPath(((MetaModuleSection) currentSection).getDocument(), StrutsConfigGenerator.ACTION_UNLOCK);
					path+= "?pId=<bean:write name=" + entryName + " property=\"id\"/>" + "&nextAction=showEdit";
					
					String alt = ((MetaModuleSection)currentSection).getDocument().getName() + " is locked by: <bean:write name="+entryName+" property="+quote(LockableObject.INT_LOCKER_ID_PROPERTY_NAME)+
					"/>, at: <bean:write name="+entryName+" property="+quote(LockableObject.INT_LOCKING_TIME_PROPERTY_NAME)+"/>";
					
					appendString("<a href=\"#\" onClick= "+quote("lightbox('"+alt+"<br /> Unlock "+((MetaModuleSection)currentSection).getDocument().getName()+
							" with id: <bean:write name="+entryName+" property=\"id\"/>?','<ano:tslink>"+path+"</ano:tslink>');")+">"+getUnLockImage(alt)+"" +
							" Unlock</a><span>&nbsp;Locked by <b><bean:write name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+
							" property="+quote(LockableObject.INT_LOCKER_ID_PROPERTY_NAME)+"/></b>");
					appendString("at:  <b><bean:write name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+
							" property="+quote(LockableObject.INT_LOCKING_TIME_PROPERTY_NAME)+"/></b></span>");
					appendString("</logic:equal>");
				}
		
		appendString("<form name=" + quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection) currentSection).getDocument())) +" method=\"post\" action="+quote(StrutsConfigGenerator.getPath(section.getDocument(), StrutsConfigGenerator.ACTION_UPDATE))+">");		 
		appendIncreasedString("<input type="+quote("hidden")+" name="+quote("_ts")+" value="+quote("<%=System.currentTimeMillis()%>")+">");
		appendIncreasedString("<input type="+quote("hidden")+" name="+quote(ModuleBeanGenerator.FLAG_FORM_SUBMITTED)+" value="+quote("true")+">");
		appendIncreasedString("<input type="+quote("hidden")+" name="+quote("nextAction")+" value="+quote("close")+">");

		appendString("<table cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" border=\"0\">");
		appendString("<tbody>");
			increaseIdent();
			appendString("<bean:write name=\"description.null\" ignore=\"true\"/>");
		decreaseIdent();
		appendString("<tr>");
			increaseIdent();
			appendString("<td align=\"left\">");
			appendString("<div class=\"clear\"><!-- --></div>");
			//appendString("</logic:equal>");
			//UNLOCK HERE!!!!!
			appendString("</td>");
		decreaseIdent();
		appendString("</tr>");
			
        //*** CMS2.0 START ***
		
		List<MetaViewElement> richTextElementsRegistry = new ArrayList<MetaViewElement>();
		List<String> linkElementsRegistry = new ArrayList<String>();
		//*** CMS2.0 FINISH ***
		
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			//*** CMS2.0 START ***
			if (element instanceof MetaListElement){
				//now we draw control elements upside our page
				i++; continue;
				} 
			if(element instanceof MetaFieldElement){
				MetaDocument doc = ((MetaModuleSection)currentSection).getDocument();
				MetaProperty p = doc.getField(element.getName());
				if(element.isRich())
					if(p.getType().equals("text"))
							richTextElementsRegistry.add(element);
				
				if(p.isLinked())
					linkElementsRegistry.add(element.getName());
			}
			//*** CMS2.0 FINISH ***
			
			
			String lang = getElementLanguage(element);

			//ALTERNATIVE EDITOR FOR DISABLED MODE
			if (lang!=null && lang.equals(GeneratorDataRegistry.getInstance().getContext().getDefaultLanguage())){
				appendString("<logic:equal name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" property="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" value="+quote("true")+">");
				appendString("<td align=\"right\"> <a id=\""+element.getName()+"DEF\" name=\""+element.getName()+"DEF\"></a>");
				increaseIdent();
				String name = section.getDocument().getField(element.getName()).getName()+"<b>DEF</b>";
				if (name==null || name.length()==0)
					name = "&nbsp;";
				appendString(name);
				decreaseIdent(); 
				appendString("</td>");
				appendString("<td align=\"left\">&nbsp;");
				append(getElementEditor(section.getDocument(), element));
				appendString("&nbsp;<i><bean:write name=\"description."+element.getName()+"\" ignore=\"true\"/></i>");
				appendString("</td>");
				appendString("</tr>");
				appendString("</logic:equal>");
			}//END ALTERNATIVE EDITOR FOR MULTILANG DISABLED FORM

			if (lang!=null)
				appendString("<logic:equal name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" property="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" value="+quote("false")+">");

			// Language Filtering Settings
			String displayLanguageCheck = "";			
			if(element instanceof MultilingualFieldElement) {
				MultilingualFieldElement multilangualElement = (MultilingualFieldElement) element;
				displayLanguageCheck = "<logic:equal name=\"display" + multilangualElement.getLanguage() + "\" value=\"false\">style=\"display:none\"</logic:equal> class=\"lang_hide lang_"+multilangualElement.getLanguage()+"\"";						
			}
			
			appendString("<tr " + displayLanguageCheck+">");
				increaseIdent();
					increaseIdent();
					appendString("<td align=\"right\">");
						String name = lang == null ? element.getName() : section.getDocument().getField(element.getName()).getName(lang);
						if (name==null || name.length()==0)
							name = "&nbsp;";
						appendString("<a id=\""+name+"\" name=\""+name+"\"></a>");
						appendString(name);
					decreaseIdent(); 
					if(element.isRich()){
						appendString("<div class=\"clear\"></div>");
						appendString("<a href=\"javascript:;\" onmousedown=\"tinyMCE.get('"+section.getDocument().getField(element.getName()).getName(lang) + "_ID').show();\" class=\"rich_on_off\" style=\"display:none;\">on</a>");
						appendString("<a href=\"javascript:;\" onmousedown=\"tinyMCE.get('"+section.getDocument().getField(element.getName()).getName(lang) + "_ID').hide();\" class=\"rich_on_off\">off</a>");
						appendString("<span class=\"rich_on_off\">Rich:</span>");
					}
					appendString("</td>");
					appendString("<td align=\"left\">&nbsp;");
						increaseIdent();
						append(getElementEditor(section.getDocument(), element));
						appendString("&nbsp;<i><bean:write name=\"description."+element.getName()+"\" ignore=\"true\"/></i>");
					decreaseIdent();
					appendString("</td>");
			decreaseIdent();
			appendString("</tr>");
			
			if (lang!=null)
				appendString("</logic:equal>");
		}
		appendString("<tr>");
		appendString("</tr>");
		appendString("<tr>");
		appendString("</tr>");
		appendString("<tr>");
		appendString("</tr>");
		appendString("<tr>");
		appendString("</tr>");
		decreaseIdent();
		appendString("</tbody>");
		decreaseIdent();
		appendString("</table>");
		appendString("</form>");
		appendString("<div class=\"clear\"><!-- --></div>");
		
		appendString("<div class=\"generated\"><span><bean:write name="+quote("objectInfoString")+"/></span>");
		
		//Link to the Links to Me page
		appendString("<logic:present name="+quote("linksToMe")+" scope="+quote("request")+">");
		String linksToMePagePath = StrutsConfigGenerator.getPath(section.getDocument(), StrutsConfigGenerator.ACTION_LINKS_TO_ME)+"?pId=<bean:write name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" property=\"id\"/>";
		appendString("<a href="+quote("<ano:tslink>"+linksToMePagePath+"</ano:tslink>")+">Show direct links to  this document</a>");
		appendString("</logic:present>");
		appendString("<div class=\"clear\"><!-- --></div>");
		appendString("</div>");
		appendString("</div>");
		appendString("</div>");
		appendString("<div class=\"lightbox\" style=\"display:none;\">");
		appendString("<div class=\"black_bg\"><!-- --></div>");
		appendString("<div class=\"box\">");
		increaseIdent();
			appendString("<div class=\"box_top\">");
			increaseIdent();
				appendString("<div><!-- --></div>");
				appendString("<span><!-- --></span>");
				appendString("<a class=\"close_box\"><!-- --></a>");
				appendString("<div class=\"clear\"><!-- --></div>");
			decreaseIdent();
			appendString("</div>");
			appendString("<div class=\"box_in\">");
			increaseIdent();
				appendString("<div class=\"right\">");
				increaseIdent();
					appendString("<div class=\"text_here\">");
					appendString("</div>");
				decreaseIdent();
				appendString("</div>");
			decreaseIdent();
			appendString("</div>");
			appendString("<div class=\"box_bot\">");
			increaseIdent();
				appendString("<div><!-- --></div>");
				appendString("<span><!-- --></span>");
			decreaseIdent();
			appendString("</div>");
		decreaseIdent();
		appendString("</div>");
		appendString("</div>");
		appendString("</body>");
		decreaseIdent();
		
		generateRichTextEditorJS(section.getDocument(), richTextElementsRegistry);
		generateLinkElementEditorJS(section.getDocument(), linkElementsRegistry);
		
		decreaseIdent();
		appendString("</html:html>");
		appendString("<!-- / generated by JspMafViewGenerator.generateDialog -->");
		
		
		return jsp;
	}

	/**
	 * Creating entries in JSP for Multilanguage Support!!!
	 * @param section
	 * @param colspan
	 */
	private void addMultilanguageOperations(MetaModuleSection section, int colspan) {
		appendString("<logic:equal name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, section.getDocument()))+" property="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" value="+quote("false")+">");
		increaseIdent();
		appendString("<logic:notEqual name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, section.getDocument()))+" property="+quote("id")+" value="+quote("")+">");
		increaseIdent();
		appendString("<form name=\"CopyLang\" id=\"CopyLang\" method=\"get\" action=\""+StrutsConfigGenerator.getPath(section.getDocument(), StrutsConfigGenerator.ACTION_COPY_LANG)+"\">");
		increaseIdent();
		appendString("<input type=\"hidden\" name=\"ts\" value=\"<%=System.currentTimeMillis()%>\"/><input type=\"hidden\" name=\"pId\" value=\"<bean:write name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, section.getDocument()))+" property="+quote("id")+"/>\"/>");
		appendString("<div>");
		increaseIdent();
		appendString("Copy <select name=\"pSrcLang\">");
		increaseIdent();
		for (String sl : GeneratorDataRegistry.getInstance().getContext().getLanguages()){
			appendString("<option value=\""+sl+"\">"+sl+"</option>");
		}
		decreaseIdent();
		appendString("</select>");


		appendString("to");
		appendString("<select name=\"pDestLang\">");
		increaseIdent();
		for (String sl : GeneratorDataRegistry.getInstance().getContext().getLanguages()){
			appendString("<option value=\""+sl+"\">"+sl+"</option>");
		}
		decreaseIdent();
		appendString("</select>");
		decreaseIdent();
		appendString("</div>");
		appendString("<a href=\"#\" class=\"button\" onclick=\"document.CopyLang.submit(); return false\"><span>Copy</span></a>");
		
		decreaseIdent();
		appendString("</form>");
		appendString("<form name="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" id="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+"  method=\"get\" action=\""+StrutsConfigGenerator.getPath(section.getDocument(), StrutsConfigGenerator.ACTION_SWITCH_MULTILANGUAGE_INSTANCE)+"\">");
		increaseIdent();
		appendString("<div>");
		appendString("<input type=\"hidden\" name=\"value\" value=\"true\"/><input type=\"hidden\" name=\"ts\" value=\"<%=System.currentTimeMillis()%>\"/><input type=\"hidden\" name=\"pId\" value=\"<bean:write name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, section.getDocument()))+" property="+quote("id")+"/>\"/>");
		decreaseIdent();
		appendString("</div>");
		appendString("<a href=\"#\" class=\"button\" onclick=\"document."+ModuleBeanGenerator.FIELD_ML_DISABLED+".submit(); return false\"><span>Disable languages</span></a>");
		appendString("</form>");
		decreaseIdent();
		appendString("</logic:notEqual>");
		decreaseIdent();
		appendString("</logic:equal>");
		appendString("<logic:equal name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, section.getDocument()))+" property="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" value="+quote("true")+">");
		increaseIdent();
		appendString("<div>");
		appendString("<form name="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" id="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" method=\"get\" action=\""+StrutsConfigGenerator.getPath(section.getDocument(), StrutsConfigGenerator.ACTION_SWITCH_MULTILANGUAGE_INSTANCE)+"\">");
		appendString("<input type=\"hidden\" name=\"value\" value=\"false\"/><input type=\"hidden\" name=\"ts\" value=\"<%=System.currentTimeMillis()%>\"/><input type=\"hidden\" name=\"pId\" value=\"<bean:write name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, section.getDocument()))+" property="+quote("id")+"/>\"/>");
		appendString("<a href=\"#\" class=\"button\" onclick=\"document."+ModuleBeanGenerator.FIELD_ML_DISABLED+".submit(); return false\"><span>Enable languages</span></a>");
		decreaseIdent();
		appendString("</div>");
		appendString("</form>");
		appendString("</logic:equal>");
	}
	
	private GeneratedJSPFile generateLinksToDocument(MetaModuleSection section, MetaView view){

		GeneratedJSPFile jsp = new GeneratedJSPFile();
		startNewJob(jsp);
		jsp.setName(getLinksToMePageName(section.getDocument()));
		jsp.setPackage(getContext().getJspPackageName(section.getModule()));
		
		resetIdent();
		
		append(getBaseJSPHeader());
		
		appendString("<!--  generated by JspMafViewGenerator.generateLinksToDocument -->");
		appendString("<html:html>");
		increaseIdent();
		appendString("<head>");
		increaseIdent();
		appendString("<title>Direct links to the "+section.getDocument().getName()+"[<bean:write name=\"objectId\"/>]</title>");
		generatePragmas(view);
		appendString("<link href=\""+getCurrentCSSPath("newadmin.css")+"\" rel=\"stylesheet\" type=\"text/css\">");
		decreaseIdent();
		appendString("</head>");
		appendString("<body>");
		increaseIdent();		
		
		appendString("<logic:present name="+quote("linksToMe")+" scope="+quote("request")+">");
		appendString("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
		increaseIdent();
		appendString("<tr>");
		increaseIdent();
		appendString("<td>Direct links to the "+section.getDocument().getName()+"[<bean:write name=\"objectId\"/>]</td>");
		decreaseIdent();
		appendString("</tr>");
		appendString("<logic:iterate name="+quote("linksToMe")+" id="+quote("linkToMe")+" type="+quote("net.anotheria.asg.util.bean.LinkToMeBean")+" >");
		increaseIdent();
		appendString("<tr>");
		increaseIdent();
	
		String docDescriptionStatement = "Type: <bean:write name="+quote("linkToMe")+" property="+quote("targetDocumentType")+"/>";
		docDescriptionStatement += ", Id: <a href="+quote("<bean:write name="+quote("linkToMe")+" property="+quote("targetDocumentLink")+"/>")+" ><bean:write name="+quote("linkToMe")+" property="+quote("targetDocumentId")+"/></a>";
		docDescriptionStatement += "<logic:equal name="+quote("linkToMe")+" property="+quote("descriptionAvailable") +" value="+quote("true")+">, Name: <b> <a href="+quote("<bean:write name="+quote("linkToMe")+" property="+quote("targetDocumentLink")+"/>")+" ><bean:write name="+quote("linkToMe")+" property="+quote("targetDocumentDescription")+"/></a></b></logic:equal>";
		docDescriptionStatement += ", in <b><bean:write name="+quote("linkToMe")+" property="+quote("targetDocumentProperty")+"/></b>.";
		appendString("<td>"+docDescriptionStatement+"</td>");
		decreaseIdent();
		appendString("</tr>");
		decreaseIdent();
		appendString("</logic:iterate>");
		appendString("</table>");
		
		appendString("<br/>");
		appendString("<br/>");
		appendString("</logic:present>");
		appendString("<!-- ");
		appendString("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
		increaseIdent();
		appendString("<tr>");
		increaseIdent();
		appendString("<td>This "+section.getDocument().getName()+" can be used in following documents:</td>");
		decreaseIdent();
		appendString("</tr>");
		
		List<DirectLink> linkee = GeneratorDataRegistry.getInstance().findLinksToDocument(section.getDocument());
		for (DirectLink l : linkee){
			appendString("<tr>");
			increaseIdent();
			appendString("<td>");
			appendString(l.getModule().getName()+"."+l.getDocument().getName()+", property: "+l.getProperty().getName());
			appendString("</td>");
			decreaseIdent();
			appendString("</tr>");
		}
		
		decreaseIdent();
		appendString("</table>");
		appendString("-->");
		

		decreaseIdent();
		appendString("</body>");
		decreaseIdent();
		
		decreaseIdent();
		appendString("</html:html>");
		appendString("<!-- / generated by JspMafViewGenerator.generateLinksToDocument -->");
		
		append(getBaseJSPFooter());
		
		return jsp;
	}	
	
	private String getElementName(MetaDocument doc, MetaViewElement element){
		MetaProperty p = doc.getField(element.getName());
		String lang = getElementLanguage(element);
		return p.getName(lang);
	}
	
	private String getEditorVarName(MetaDocument doc, MetaViewElement element){
		 return getElementName(doc, element) + "Editor";
	}
	
	private String getToggleEditorButtonVarName(MetaDocument doc, MetaViewElement element){
		 return "toggleEditorButton_" + getElementName(doc, element);
	}
	
	
	//*** CMS2.0 START ***
	private void generateListLinkElementEditorJS(MetaDocument doc, String elName){
		String elCapitalName = StringUtils.capitalize(elName);
		
		appendString("<script type=\"text/javascript\">");
		increaseIdent();
			
		appendString("//Initializing items for " + elName);
		appendString("var " +elName+ "Json = {items:[");
		appendString("<logic:iterate id=\"item\" name=\""+elName+"\" type=\"net.anotheria.webutils.bean.LabelValueBean\">");
		increaseIdent();
		appendString("{id:\"<bean:write name=\"item\" property=\"value\" filter=\"true\"/>\",name:\"<bean:write name=\"item\" property=\"label\" filter=\"true\"/>\"},");
		//appendString("{id:\"${item.value}\",name:\"${item.label}\"},");
		decreaseIdent();
		appendString("</logic:iterate>");
		appendString("]};");
		appendString("new YAHOO.anoweb.widget.ComboBox("+quote("Value")+",\""+"ValuesSelector\","+elName+"Json);");

		decreaseIdent();
		appendString("</script>");

	}
	private void generateLinkElementEditorJS(MetaDocument doc, List<String> linkElements){
		appendString("<script type=\"text/javascript\">");
		increaseIdent();
		for(String elName: linkElements){

			//FIXME: here is assumed that links can't be multilanguage
			String elCapitalName = StringUtils.capitalize(elName);
			String beanName = StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument());
			
			appendString("//Initializing items for " + elName);
			appendString("var " +elName+ "Json = {items:[");
			appendString("<logic:iterate id=\"item\" name="+quote(beanName)+" property=\""+elName+"Collection\" type=\"net.anotheria.webutils.bean.LabelValueBean\">");
			increaseIdent();
			appendString("{id:\"<bean:write name=\"item\" property=\"value\" filter=\"true\"/>\",name:\"<bean:write name=\"item\" property=\"label\" filter=\"true\"/>\"},");
			//appendString("{id:\"${item.value}\",name:\"${item.label}\"},");
			decreaseIdent();
			appendString("</logic:iterate>");
			appendString("]};");
			appendString("var selection"+elCapitalName+"Json = {");
			increaseIdent();
			appendString("id:\"${"+beanName+"."+elName+"}\",name:\"${"+beanName+"."+elName+"CurrentValue}\"");
			decreaseIdent();
			appendString("};");
			appendString("new YAHOO.anoweb.widget.ComboBox("+quote(elCapitalName)+",\""+elCapitalName+"Selector\","+elName+"Json,selection"+elCapitalName+"Json);");
		}
		decreaseIdent();
		appendString("</script>");
	}
	
	private void generateRichTextEditorJS(MetaDocument doc, List<MetaViewElement> richTextElements){
		
		appendString("<!-- TinyMCE -->");
		
		appendString("<script type=\"text/javascript\">");
		appendString("tinyMCE.init({");
		appendString("mode : \"exact\",");
		String allRichTextElements = "elements:\"";
		String lang = "";
		for (int i=0; i<richTextElements.size(); i++){
			allRichTextElements+=richTextElements.get(i).getName()+getElementLanguage(richTextElements.get(i))+"_ID";
			if (i+1!=richTextElements.size())
				allRichTextElements+=", ";
		}
		appendString(allRichTextElements+"\",");
		appendString("theme : \"advanced\",");
		appendString("plugins : \"save, table\",");
		appendString("theme_advanced_layout_manager : \"SimpleLayout\",");
		appendString("theme_advanced_toolbar_align : \"left\",");
		appendString("theme_advanced_toolbar_location : \"top\",");
		appendString("theme_advanced_buttons1 : \"undo, redo, separator, bold, italic, underline, separator, justifyleft, justifycenter, justifyright, justifyfull, formatselect,  fontselect, fontsizeselect, forecolor\",");
		appendString("theme_advanced_buttons2 : \"bullist, numlist, separator, image, link, unlink, separator, table, code\",");
		appendString("theme_advanced_buttons3 : \"\",");
		appendString("theme_advanced_resize_horizontal : true");
		appendString("});");
		appendString("</script>");
		appendString("<!-- /TinyMCE -->");
		
	}
	//*** CMS2.0 FINISH ***
	
	private String getElementEditor(MetaDocument doc, MetaViewElement element){
		if (element instanceof MetaEmptyElement)
			return "&nbsp;";
		if (element instanceof MetaFieldElement)
			return getFieldEditor((MetaFieldElement)element);
		if (element instanceof MetaListElement)
			return getListEditor(doc, (MetaListElement)element);
		if (element instanceof MetaFunctionElement)
			return getFunctionEditor(doc, (MetaFunctionElement)element);

		return "";
			
	}
	
	private String getListEditor(MetaDocument doc, MetaListElement element){
		String ret = "";
		
		List<MetaViewElement> elements = element.getElements();
		for (int i=0; i<elements.size(); i++){
			ret += getElementEditor(doc, elements.get(i));
			if (i<elements.size()-1)
				ret += "&nbsp;";
		}
			
		
		return ret;
	}
	
	private String getLinkEditor(MetaFieldElement element, MetaProperty p){
		//for now we have only one link...
		String ret = "";
		String lang = getElementLanguage(element); 
		
		/* CMS1.0
		ret += "<html:select size=\"1\" property="+quote(p.getName(lang))+">";
		ret += "<html:optionsCollection property="+quote(p.getName()+"Collection"+(lang==null ? "":lang))+" filter=\"false\"/>";
		ret += "</html:select>";
		ret += "&nbsp;";
		ret += "(<i>old:</i>&nbsp;<bean:write property="+quote(p.getName()+"CurrentValue"+(lang==null ? "":lang))+" name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" filter="+quote("false")+"/>)";
		 */
		
		//*** CMS2.0 START ***
		ret += "<em id="+quote(StringUtils.capitalize(p.getName()))+" name="+quote(p.getName())+" class=\"selectBox\"></em><div id=\""+StringUtils.capitalize(p.getName(lang))+"Selector\"></div>";
		ret += " (<i>old:</i>&nbsp;<bean:write property="+quote(p.getName()+"CurrentValue")+"name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" filter="+quote("false")+"/>)";			
		//*** CMS2.0 FINISH ***
		
		return ret;
	}
	
	private String getEnumerationEditor(MetaFieldElement element, MetaProperty p){
		String ret = "";
		String lang = getElementLanguage(element); 
		
		ret += "<select name=\""+p.getName(lang)+"\">";
		ret += "<logic:iterate indexId=\"index\" id=\"element\" property=\""+ p.getName() +"Collection\" name=\""+StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument())+ "\">";
		ret += "<option value=\"<bean:write name=\"element\" property=\"value\"/>\" <logic:equal name=\""+StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument())+ "\" property=\""+p.getName()+"CurrentValue"+(lang==null ? "":lang)+"\" value=\"${element.label}\">selected</logic:equal>><bean:write name=\"element\" property=\"label\"/></option>";
		ret += "</logic:iterate>";
		ret += "</select>";
		
		
		ret += "&nbsp;";
		ret += "(<i>old:</i>&nbsp;<bean:write property="+quote(p.getName()+"CurrentValue"+(lang==null ? "":lang))+" name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" filter="+quote("false")+"/>)";
		
		return ret;
	}
	
	private String getFieldEditor(MetaFieldElement element){
		MetaDocument doc = ((MetaModuleSection)currentSection).getDocument();
		MetaProperty p = doc.getField(element.getName());
		
		if (p.isLinked())
			return getLinkEditor(element, p);
			
		if (p instanceof MetaEnumerationProperty){
			return getEnumerationEditor(element, p);
		}
		
		if (p instanceof MetaContainerProperty) {
			return getContainerLinkEditor(element, (MetaContainerProperty)p);
		}
		

		
		if (p.getType().equals("image")){
			return getImageEditor(element, p);
		}
		
		if (p.getType().equals("string")){
			return getStringEditor(element, p);
		}
		
		if (p.getType().equals("int")){
			return getStringEditor(element, p);
		}

		if (p.getType().equals("double")){
			return getStringEditor(element, p);
		}

		if (p.getType().equals("float")){
			return getStringEditor(element, p);
		}

		if (p.getType().equals("long")){
			return getStringEditor(element, p);
		}

		if (p.getType().equals("text")){
			return getTextEditor(element, p);
		}
		
		if (p.getType().equals("boolean")){
			return getBooleanEditor(element, p);
		}
		
		
		return p.getType();
	}
	
	private String getContainerLinkEditor(MetaFieldElement element, MetaContainerProperty p){
		String ret = "";
		String lang = getElementLanguage(element); 
		String name = quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()));
		ret += "<logic:equal name="+name+" property="+quote("id")+" value="+quote("")+">";
		ret += "none";
		ret += "</logic:equal>";
		ret += "<logic:notEqual name="+name+" property="+quote("id")+" value="+quote("")+">";
		ret += "<bean:write name="+name+" property="+quote(p.getName(lang))+"/>";
		ret += "&nbsp;";
		ret += "element";
		ret += "<logic:notEqual name="+name+" property="+quote(p.getName(lang))+" value="+quote("1")+">";
		ret += "s";
		ret += "</logic:notEqual>";
		ret += "&nbsp;";
		String actionName = StrutsConfigGenerator.getContainerPath(((MetaModuleSection)currentSection).getDocument(), p, StrutsConfigGenerator.ACTION_SHOW);
		actionName += "?ownerId=<bean:write name="+name+" property="+quote("id")+"/>";
		ret += "<a href="+quote(actionName)+">&nbsp;&raquo&nbsp;Edit&nbsp;</a>";
		ret += "</logic:notEqual>";
		
		return ret;
	}
	
	

	private String getImageEditor(MetaFieldElement element, MetaProperty p){
		String ret ="";
		ret += "<table height=86 width=100% cellpadding=6>";
		ret += "<tr><td>";
		ret += "<iframe src=\"fileShow?nocache=<%=System.currentTimeMillis()%>\" frameborder=\"0\" width=100% height=80 scrolling=\"no\"></iframe>";		
		ret += "</tr></td>";
		ret += "</table>";
		return ret;
	}

	private String getStringEditor(MetaFieldElement element, MetaProperty p){
		return getInputEditor(element, p, "text");
	}
	
	private String getBooleanEditor(MetaFieldElement element, MetaProperty p){
		return getInputEditor(element, p, "checkbox");
	}
	
	private String getInputEditor(MetaFieldElement element, MetaProperty p, String inputType){
		String ret ="";
		String lang = getElementLanguage(element); 
		
		ret += "<input type=" + quote(inputType) + " name="+quote(p.getName(lang));
		
		//ret += "<html:text filter=\"false\" property="+quote(element.getName());
		if (inputType.equalsIgnoreCase("checkbox"))	{
			ret += " <logic:equal name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" property="+quote(p.getName(lang))+" value=\"true\""; 
			ret += ">";
			ret += "checked</logic:equal>";
		}
		else {
			ret += " value=\"<bean:write name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" property="+quote(p.getName(lang));
			ret += "/>\"";
		}
		if (element.isReadonly())
			ret += " readonly="+quote("true");
		ret += "/>";

		if (element.isReadonly())
			ret += "&nbsp;<i>readonly</i>";
		
		return ret;
	}

	private String getTextEditor(MetaFieldElement element, MetaProperty p){
		String lang = getElementLanguage(element);
		String ret ="";
		
		ret += "<textarea cols=\"\" rows=\"16\" id="+quote(p.getName(lang) + "_ID")+" name="+quote(p.getName(lang));
		ret += ">";
		ret += "<bean:write filter=\"false\" name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" property="+quote(p.getName(lang))+" />";
		ret += "</textarea>";

		return ret;
	}

	private GeneratedJSPFile generateCSVExport(MetaModuleSection section, MetaView view){
		
		GeneratedJSPFile jsp = new GeneratedJSPFile();
		startNewJob(jsp);
		jsp.setName(getExportAsCSVPageName(section.getDocument()));
		jsp.setPackage(getContext().getJspPackageName(section.getModule()));
		
		ident = 0;
		append(getBaseCSVHeader());
		
		currentSection = section;
		MetaDocument doc = section.getDocument();

		String entryName = doc.getName().toLowerCase();
		List<MetaViewElement> elements = createMultilingualList(section.getElements(),doc);

		String headerLine = "";
		for (MetaViewElement element : elements){
			String lang = getElementLanguage(element);
			boolean multilangEl = element instanceof MultilingualFieldElement;
			String tag = multilangEl && lang != null ? doc.getField(element.getName()).getName(lang) : generateTag(element);
			if (tag==null)
				continue;
			headerLine += tag+";";
		}
		appendString(headerLine);

		appendString("<logic:iterate name="+quote(doc.getMultiple().toLowerCase()+ModuleActionsGenerator.exportCSVSufix)+" type="+quote(GeneratorDataRegistry.getInstance().getContext().getDataPackageName(doc)+"."+doc.getName())+" id="+quote(entryName)+"><%--");
		String bodyLine = "--%>";

		for (MetaViewElement element : elements) {
			String lang = getElementLanguage(element);
			boolean multilangEl = element instanceof MultilingualFieldElement;
			String tag = multilangEl && lang != null ? doc.getField(element.getName()).getName(lang) : generateTag(element);
			if (tag==null)
				continue;
			bodyLine += "<bean:write filter=\"false\" name=" + quote(entryName) + " property=\"" + tag + "\"/>;";
		}
		appendString(bodyLine);
		appendString("</logic:iterate>");
		return jsp;
	}


	private String generateTag(MetaViewElement elem){
		if (!(elem instanceof MetaFieldElement))
			return null;
		return ((MetaFieldElement)elem).getName();
		
	}
	
	private GeneratedJSPFile generateXMLExport(MetaModuleSection section, MetaView view){
		
		GeneratedJSPFile jsp = new GeneratedJSPFile();
		startNewJob(jsp);
		jsp.setName(getExportAsXMLPageName(section.getDocument()));
		jsp.setPackage(getContext().getJspPackageName(section.getModule()));
		
		ident = 0;
		append(getBaseXMLHeader());
		
		currentSection = section;
		MetaDocument doc = section.getDocument();

		String entryName = doc.getName().toLowerCase();

		appendString("<?xml version=\"1.0\" encoding="+quote(getContext().getEncoding())+"?>");
		appendString("<ano-xml:xml_write name="+quote(doc.getMultiple().toLowerCase()+ModuleActionsGenerator.exportXMLSufix)+"/>");
		return jsp;
	}
	
	
	private void generateProcessLanguageFilteringSettings() {
		// Language filtering settings
		appendString("<!-- Process language filterring settings -->");
		appendString("<logic:equal name=\"userSettings\" property=\"displayAllLanguages\" value=\"false\">");
		increaseIdent();
		for (String sl : GeneratorDataRegistry.getInstance().getContext().getLanguages()){
			appendString("<bean:define id=\"display" + sl + "\" value='<%= ((UserSettingsBean) request.getAttribute(\"userSettings\")).getDisplayedLanguages().contains(\"" + sl + "\") ? \"true\" : \"false\" %>'/>");
		}
		decreaseIdent();
		appendString("</logic:equal>");
		appendString("<logic:notEqual name=\"userSettings\" property=\"displayAllLanguages\" value=\"false\">");
		increaseIdent();
		for (String sl : GeneratorDataRegistry.getInstance().getContext().getLanguages()){
			appendString("<bean:define id=\"display" + sl + "\" value='true'/>");
		}
		decreaseIdent();
		appendString("</logic:notEqual>");
		appendString("<!-- / End language filterring settings -->");
	}
	
	private void generateLanguageFilteringSettingsViewComponent(MetaModuleSection section) {
		
		appendString("<a href=\""+userSettingsEditActionName+"?referrer=<%= URLEncoder.encode(" + quote(StrutsConfigGenerator.getPath(section.getDocument(), StrutsConfigGenerator.ACTION_SHOW)) + " + ((request.getQueryString() != null) ? (\"?\" + request.getQueryString()) : \"\")," + quote(getContext().getEncoding()) +") %>\">Displayed languages:</a> [");		
		appendString("<logic:equal name=\"userSettings\" property=\"displayAllLanguages\" value=\"false\">");
		increaseIdent();
		appendString("<logic:iterate id=\"lang\" name=\"userSettings\" property=\"displayedLanguages\" >");
		appendString("&nbsp;<bean:write name=\"lang\"/>");
		appendString("</logic:iterate>");
		decreaseIdent();
		appendString("</logic:equal>");						
		appendString("<logic:notEqual name=\"userSettings\" property=\"displayAllLanguages\" value=\"false\">");		
		appendIncreasedString("ALL");		
		appendString("</logic:notEqual>");	
		appendString("]");
		
	}
	
	private void generateLanguageFilteringSettingsViewComponentForDialog(MetaDialog dialog, MetaModuleSection section) {
		
		appendString("<a href=\""+userSettingsEditActionName+"?referrer=<%= URLEncoder.encode(" + quote(StrutsConfigGenerator.getPath(section.getDocument(), StrutsConfigGenerator.ACTION_EDIT)) + " + ((request.getQueryString() != null) ? (\"?\" + request.getQueryString()) : \"\")," + quote(getContext().getEncoding()) +") %>\""
			+" onclick=\"if ( confirm('Do you want to save changes before edit settings?')) {document."+StrutsConfigGenerator.getDialogFormName(dialog, ((MetaModuleSection)section).getDocument())+".nextAction.value='stay'; document."+StrutsConfigGenerator.getDialogFormName(dialog, ((MetaModuleSection)section).getDocument())+".submit(); }\" >Displayed languages:</a> [");		
		appendString("<logic:equal name=\"userSettings\" property=\"displayAllLanguages\" value=\"false\">");
		increaseIdent();
		appendString("<logic:iterate id=\"lang\" name=\"userSettings\" property=\"displayedLanguages\" >");
		appendString("&nbsp;<bean:write name=\"lang\"/>");
		appendString("</logic:iterate>");
		decreaseIdent();
		appendString("</logic:equal>");						
		appendString("<logic:notEqual name=\"userSettings\" property=\"displayAllLanguages\" value=\"false\">");		
		appendIncreasedString("ALL");		
		appendString("</logic:notEqual>");	
		appendString("]");
		
	}
	
	private GeneratedJSPFile generateShowPage(MetaModuleSection section, MetaView view){
		
		
		GeneratedJSPFile jsp = new GeneratedJSPFile();
		startNewJob(jsp);
		jsp.setName(getShowPageName(section.getDocument()));
		jsp.setPackage(GeneratorDataRegistry.getInstance().getContext().getJspPackageName(section.getModule()));
		
		ident = 0;
		append(getBaseJSPHeader());		
		append(getUserSettingsJSPImports());
		
		currentSection = section;
		MetaDocument doc = section.getDocument();
		// Language filtering settings
		generateProcessLanguageFilteringSettings();
		
		appendString("<!--  generated by JspMafViewGenerator.generateShowPage -->");
		appendString("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"");
		appendString("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
		appendString("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		appendString("<head>");
			increaseIdent();
			appendString("<title>"+view.getTitle()+"</title>");
			generatePragmas(view);
			appendString("<link href=\""+getCurrentCSSPath("newadmin.css")+"\" rel=\"stylesheet\" type=\"text/css\">");
			appendString("<script type=\"text/javascript\" src=\""+getCurrentJSPath("jquery-1.4.min.js")+"\"></script>");
			appendString("<script type=\"text/javascript\" src=\""+getCurrentJSPath("anofunctions.js")+"\"></script>");
			decreaseIdent();
		appendString("</head>");
		appendString("<body>");
			increaseIdent();
			appendString("<jsp:include page=\""+getTopMenuPage()+"\" flush=\"true\"/>");

		List<MetaViewElement> elements = createMultilingualList(section.getElements(), doc);

		appendString("<div class=\"right\">");
			increaseIdent();
			appendString("<div class=\"r_w\">");
				increaseIdent();
				//top navigation start
				appendString("<div class=\"top_nav\">");
					increaseIdent();
					appendString("<div class=\"r_b_l\"><!-- --></div>");
					appendString("<div class=\"r_b_r\"><!-- --></div>");
					appendString("<div class=\"left_p\">"+generateNewFunction("", new MetaFunctionElement("add"))+"</div>");
					appendString("<div class=\"right_p\"><a href=\"#\"><img src=\"../cms_static/img/settings.gif\" alt=\"\"/></a>");
						increaseIdent();
						appendString("<div class=\"pop_up\">");
							increaseIdent();
							appendString("<div class=\"top\">");
								increaseIdent();
								appendString("<div><!-- --></div>");
							decreaseIdent();
							appendString("</div>");
								increaseIdent();
								appendString("<div class=\"in_l\">");
									increaseIdent();
									appendString("<div class=\"in_r\">");
										increaseIdent();
										appendString("<div class=\"in_w\">");
											increaseIdent();
											//actually  - currentPage  parameter ! - for export paging!!!
											String pageNumberParam = "?pageNumber=<bean:write name="+quote("currentpage")+" scope="+quote("request")+"/>";
											appendString("<span>Export to <a href="+quote(SectionAction.EXPORTtoXML.getMappingName(section)+pageNumberParam)+">XML</a> or <a href="+quote(SectionAction.EXPORTtoCSV.getMappingName(section)+pageNumberParam)+">CSV</a></span>");
										decreaseIdent();
										appendString("</div>");
									decreaseIdent();
									appendString("</div>");
								decreaseIdent();
								appendString("</div>");
								appendString("<div class=\"bot\">");
								appendString("<div><!-- --></div>");
								appendString("</div>");
							decreaseIdent();
						decreaseIdent();
						appendString("</div>");
					decreaseIdent();
					appendString("</div>");
				decreaseIdent();
				appendString("</div>");
				//top navigation end
				
				
		appendString("<div class=\"main_area\">");
		increaseIdent();
			appendString("<div class=\"c_l\"><!-- --></div>");
			appendString("<div class=\"c_r\"><!-- --></div>");
			appendString("<div class=\"c_b_l\"><!-- --></div>");
			appendString("<div class=\"c_b_r\"><!-- --></div>");
		
			//filters start
				appendString("<a href=\"#\" class=\"filter_open\">Filters</a>");
				appendString("<div class=\"filters\" style= \"display:none;\">");
				increaseIdent();
					appendString("<div class=\"f_l\"><!-- --></div>");
					appendString("<div class=\"f_r\"><!-- --></div>");
					appendString("<div class=\"f_b_l\"><!-- --></div>");
					appendString("<div class=\"f_b_r\"><!-- --></div>");
				
					for (int i=0; i<section.getFilters().size(); i++){
						MetaFilter f  = section.getFilters().get(i);
						appendString("<!-- Generating Filter: "+ModuleActionsGenerator.getFilterVariableName(f)+" -->");
						appendString("<% String filterParameter"+i+" = (String) request.getAttribute(\"currentFilterParameter"+i+"\");");
						appendString("if (filterParameter"+i+"==null)");
						appendIncreasedString("filterParameter"+i+" = \"\";%>");
						
						appendString("<ul>");
						increaseIdent();
							appendString("<li>"+StringUtils.capitalize(f.getName())+" "+StringUtils.capitalize(f.getFieldName())+":</li>");
							increaseIdent();
								appendString("<logic:iterate name="+quote(ModuleActionsGenerator.getFilterVariableName(f))+" id="+quote("triggerer")+" type="+quote("net.anotheria.asg.util.filter.FilterTrigger")+">");
								increaseIdent();
									appendString("<logic:equal name="+quote("triggerer")+" property="+quote("parameter")+" value="+quote("<%=filterParameter"+i+"%>")+">");
									appendIncreasedString("<li><a href=\"#\" class=\"active\"><bean:write name="+quote("triggerer")+" property="+quote("caption")+"/></a></li>");
									appendString("</logic:equal>");
									appendString("<logic:notEqual name="+quote("triggerer")+" property="+quote("parameter")+" value="+quote("<%=filterParameter"+i+"%>")+">");
									appendIncreasedString("<li><a href="+quote(SectionAction.SHOW.getMappingName(section)+"?pFilter"+i+"=<bean:write name="+quote("triggerer")+" property="+quote("parameter")+"/>")+"><bean:write name="+quote("triggerer")+" property="+quote("caption")+"/></a></li>");
									appendString("</logic:notEqual>");
								decreaseIdent();
								appendString("</logic:iterate>");
							decreaseIdent();
						decreaseIdent();
						appendString("</ul>");
						appendString("<br/>");
			}
			//filters end 
			
		appendString("<div class=\"clear\"><!-- --></div>");
	decreaseIdent();
	appendString("</div>");
	
	appendString("<% String selectedPaging = \"\"+request.getAttribute("+quote("currentItemsOnPage")+"); %>");
	//first paging start
	appendString("<div class=\"paginator\">");
	increaseIdent();
	appendString("<logic:greaterThan name=\"pagingControl\" property=\"numberOfPages\" value=\"1\">");
		appendString("<ul>");
		increaseIdent();
			appendString("<logic:notEqual name=\"pagingControl\" property=\"first\" value=\"true\">");
			appendIncreasedString("<li class=\"prev\"><a href=\"?pageNumber=<bean:write name=\"pagingControl\" property=\"previousPageNumber\"/>\">prev</a></li>");
			appendString("</logic:notEqual>");
			appendString("<logic:iterate id=\"pageElement\" name=\"pagingControl\" property=\"elements\" indexId=\"index\">");
			increaseIdent();
				appendString("<li>");
				increaseIdent();
					appendString("<logic:equal name=\"pageElement\" property=\"separator\" value=\"true\">...</logic:equal>");
					appendString("<logic:equal name=\"pageElement\" property=\"active\" value=\"true\"><bean:write name=\"pageElement\" property=\"caption\"/></logic:equal>");
					appendString("<logic:equal name=\"pageElement\" property=\"active\" value=\"false\"><a href=\"?pageNumber=<bean:write name=\"pageElement\" property=\"caption\"/>\"><bean:write name=\"pageElement\" property=\"caption\"/></a></logic:equal>");
				decreaseIdent();
				appendString("</li>");
			decreaseIdent();
			appendString("</logic:iterate>");
			appendString("<logic:notEqual name=\"pagingControl\" property=\"last\" value=\"true\">");
			appendIncreasedString("<li class=\"next\"><a href=\"?pageNumber=<bean:write name=\"pagingControl\" property=\"nextPageNumber\"/>\">next</a></li>");
			appendString("</logic:notEqual>");
		decreaseIdent();
		appendString("</ul>");
		appendString("</logic:greaterThan>");

		increaseIdent();
			appendString("<select name=\"itemsOnPage\" onchange=\"window.location='?itemsOnPage=' + this.options[this.selectedIndex].value\">");
			appendString("<logic:iterate name=\"PagingSelector\" type=\"java.lang.String\" id=\"option\">");
			appendString("<option value=\"<bean:write name=\"option\"/>\" <logic:equal name=\"option\" value=\"<%=selectedPaging%>\">selected</logic:equal>><bean:write name=\"option\"/> per page</option>");
			appendString("</logic:iterate>");
			appendString("</select>");
		decreaseIdent();
	decreaseIdent();
	appendString("</div>");
	appendString("<div class=\"clear\"><!-- --></div>");
	//first paging end
		
	//main table start
		appendString("<div class=\"scroll_x\">");
		appendString("<table cellspacing=\"1\" cellpadding=\"1\" width=\"100%\" border=\"0\" class=\"pages_table\">");
		increaseIdent();
			appendString("<thead>");
			appendString("<tr class=\"lineCaptions\">");
			increaseIdent();
			boolean opened = false;
				for (int i=0; i<elements.size(); i++){
					MetaViewElement element = elements.get(i);
					
					if (element instanceof MetaFunctionElement && opened == false)
					{appendString("<td width=\"80\">&nbsp;</td>"); opened = true;}
					
					appendString(generateElementHeader(element));
				}
			decreaseIdent();
			appendString("</tr>");
			appendString("</thead>");
		
			appendString("<tbody>");
			increaseIdent();
				String entryName = doc.getName().toLowerCase();
				appendString("<logic:iterate name="+quote(doc.getMultiple().toLowerCase())+" type="+quote(ModuleMafBeanGenerator.getListItemBeanImport(getContext(), doc))+" id="+quote(entryName)+" indexId=\"ind\">");
				increaseIdent();
					appendString("<tr class=\"<%=ind.intValue()%2==0 ? \"lineLight\" : \"lineDark\"%> highlightable\">");
					opened = false;
				        for (int i = 0; i < elements.size(); i++) {
				            MetaViewElement element = elements.get(i);
				        	
							if (element instanceof MetaFunctionElement && opened == false)
							{appendString("<td class=\"no_wrap\">"); opened = true;}
							
				            appendString(generateElement(entryName, element,doc));
				        }
				        		opened = false;
				        		appendString("</td>");
				        		appendString("</tr>");
							decreaseIdent();
							appendString("</logic:iterate>");
						decreaseIdent();
						appendString("</tbody>");
					decreaseIdent();
					appendString("</table>");
					appendString("</div>");
					//main table end
					
					//second paging start
					appendString("<div class=\"paginator\">");
					increaseIdent();
					appendString("<logic:greaterThan name=\"pagingControl\" property=\"numberOfPages\" value=\"1\">");
						appendString("<ul>");
						increaseIdent();
							appendString("<logic:notEqual name=\"pagingControl\" property=\"first\" value=\"true\">");
							appendIncreasedString("<li class=\"prev\"><a href=\"?pageNumber=<bean:write name=\"pagingControl\" property=\"previousPageNumber\"/>\">prev</a></li>");
							appendString("</logic:notEqual>");
							appendString("<logic:iterate id=\"pageElement\" name=\"pagingControl\" property=\"elements\" indexId=\"index\">");
							increaseIdent();
								appendString("<li>");
								increaseIdent();
									appendString("<logic:equal name=\"pageElement\" property=\"separator\" value=\"true\">...</logic:equal>");
									appendString("<logic:equal name=\"pageElement\" property=\"active\" value=\"true\"><bean:write name=\"pageElement\" property=\"caption\"/></logic:equal>");
									appendString("<logic:equal name=\"pageElement\" property=\"active\" value=\"false\"><a href=\"?pageNumber=<bean:write name=\"pageElement\" property=\"caption\"/>\"><bean:write name=\"pageElement\" property=\"caption\"/></a></logic:equal>");
								decreaseIdent();
								appendString("</li>");
							decreaseIdent();
							appendString("</logic:iterate>");
							appendString("<logic:notEqual name=\"pagingControl\" property=\"last\" value=\"true\">");
							appendIncreasedString("<li class=\"next\"><a href=\"?pageNumber=<bean:write name=\"pagingControl\" property=\"nextPageNumber\"/>\">next</a></li>");
							appendString("</logic:notEqual>");
						decreaseIdent();
						appendString("</ul>");
						appendString("</logic:greaterThan>");
						increaseIdent();
							appendString("<select name=\"itemsOnPage\" onchange=\"window.location='?itemsOnPage=' + this.options[this.selectedIndex].value\">");
							appendString("<logic:iterate name=\"PagingSelector\" type=\"java.lang.String\" id=\"option\">");
							appendString("<option value=\"<bean:write name=\"option\"/>\" <logic:equal name=\"option\" value=\"<%=selectedPaging%>\">selected</logic:equal>><bean:write name=\"option\"/> per page</option>");
							appendString("</logic:iterate>");
							appendString("</select>");
						decreaseIdent();
					decreaseIdent();
					appendString("</div>");
					appendString("<div class=\"clear\"><!-- --></div>");
				decreaseIdent();
				appendString("</div>");
			decreaseIdent();
			appendString("</div>");
		decreaseIdent();
		appendString("</div>");
		//second paging end
		
		//lightbox start
		appendString("<div class=\"lightbox\" style=\"display:none;\">");
		appendString("<div class=\"black_bg\"><!-- --></div>");
		appendString("<div class=\"box\">");
		increaseIdent();
			appendString("<div class=\"box_top\">");
			increaseIdent();
				appendString("<div><!-- --></div>");
				appendString("<span><!-- --></span>");
				appendString("<a class=\"close_box\"><!-- --></a>");
				appendString("<div class=\"clear\"><!-- --></div>");
			decreaseIdent();
			appendString("</div>");
			appendString("<div class=\"box_in\">");
			increaseIdent();
				appendString("<div class=\"right\">");
				increaseIdent();
					appendString("<div class=\"text_here\">");
					appendString("</div>");
				decreaseIdent();
				appendString("</div>");
			decreaseIdent();
			appendString("</div>");
			appendString("<div class=\"box_bot\">");
			increaseIdent();
				appendString("<div><!-- --></div>");
				appendString("<span><!-- --></span>");
			decreaseIdent();
			appendString("</div>");
		decreaseIdent();
		//lightbox end
		
		appendString("</div>");
		appendString("</div>");
		appendString("</body>");
		appendString("</html>");
		appendString("<!-- / generated by JspMafViewGenerator.generateShowPage -->");
		return jsp;
	}
	
	private GeneratedJSPFile generateSearchPage(){
		
		
		GeneratedJSPFile jsp = new GeneratedJSPFile();
		startNewJob(jsp);
		jsp.setName(getSearchResultPageName());
		jsp.setPackage(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED)+".jsp");
		
		ident = 0;
		
		append(getBaseJSPHeader());
		
		appendString("<!--  generated by JspMafViewGenerator.generateSearchPage -->");
		appendString("<html>");
		increaseIdent();
		appendString("<head>");
			increaseIdent();
			appendString("<title>Search result</title>");
			generatePragmas();
			appendString("<link href=\""+getCurrentCSSPath("newadmin.css")+"\" rel=\"stylesheet\" type=\"text/css\">");
			appendString("<script type=\"text/javascript\" src=\""+getCurrentJSPath("jquery-1.4.min.js")+"\"></script>");
			appendString("<script type=\"text/javascript\" src=\""+getCurrentJSPath("anofunctions.js")+"\"></script>");
		decreaseIdent();
		appendString("</head>");
		appendString("<body>");
		appendString("<jsp:include page=\""+getTopMenuPage()+"\" flush=\"true\"/>");
	
		appendString("<div class=\"right\">");
			increaseIdent();
			appendString("<div class=\"r_w\">");
				increaseIdent();
				appendString("<div class=\"top_nav\">");
					increaseIdent();
					appendString("<div class=\"r_b_l\"><!-- --></div>");
					appendString("<div class=\"r_b_r\"><!-- --></div>");
					appendString("<div class=\"left_p\">");
						increaseIdent();
						appendString("<div class=\"clear\"><!-- --></div>");
					decreaseIdent();
					
					
					// SAVE AND CLOSE BUTTONS SHOULD BE HERE
					appendString("</div>");
				decreaseIdent();
				appendString("</div>");
				
				
				
				
		appendString("<div class=\"main_area\">");
		appendString("<div class=\"c_l\"><!-- --></div>");
		appendString("<div class=\"c_r\"><!-- --></div>");
		appendString("<div class=\"c_b_l\"><!-- --></div>");
		appendString("<div class=\"c_b_r\"><!-- --></div>");
		appendString("<h2>Search result: <bean:write name=\"criteria\"/></h2>");
		appendString("<div class=\"clear\"><!-- --></div>");
		appendString("<div class=\"clear\"><!-- --></div>");
		
		appendString("<table cellspacing=\"1\" cellpadding=\"1\" border=\"0\" width=\"100%\" class=\"pages_table\">");
		appendString("<thead>");
		increaseIdent();
		appendString("<tr class=\"linecaptions\">");
		increaseIdent();
		appendString("<td style=\"width: 50px;\">Id</td>");
		appendString("<td style=\"width: 200px;\">Document</td>");
		appendString("<td style=\"width: 200px;\">Property</td>");
		appendString("<td>Match</td>");
		decreaseIdent();
		appendString("</tr>");
		appendString("</thead>");
		appendString("<logic:present name="+quote("result")+" >");
		appendString("<tbody>");
		appendString("<logic:iterate name="+quote("result")+" type="+quote("net.anotheria.anodoc.query2.ResultEntryBean")+" id="+quote("entry")+" indexId=\"ind\">");
		increaseIdent();
		appendString("<tr class=\"<%=ind.intValue()%2==0 ? \"lineLight\" : \"lineDark\"%>\">");
		appendString("<td style=\"width: 50px;\"><a href="+quote("<bean:write name="+quote("entry")+" property="+quote("editLink")+"/>")+" target="+quote("_blank")+"><bean:write name="+quote("entry")+" property="+quote("documentId")+"/></td>");
		appendString("<td style=\"width: 200px;\"><bean:write name="+quote("entry")+" property="+quote("documentName")+"/></td>");
		appendString("<td style=\"width: 200px;\"><bean:write name="+quote("entry")+" property="+quote("propertyName")+"/></td>");
		appendString("<td><bean:write name="+quote("entry")+" property="+quote("info")+" filter="+quote("false")+"/></td>");
		appendString("</tr>");
		decreaseIdent();
		appendString("</logic:iterate>");
		appendString("</tbody>");
		appendString("</logic:present>");

		decreaseIdent();
		appendString("</table>");
		appendString("<div class=\"clear\"><!-- --></div>");
		decreaseIdent();
		appendString("</div>");
		appendString("</div>");
		appendString("</div>");
		appendString("</body>");
		decreaseIdent();
		appendString("</html>");
		appendString("<!-- / generated by JspMafViewGenerator.generateSearchPage -->");
		append(getBaseJSPFooter()); 
		return jsp;
	}
 
	private GeneratedJSPFile generateVersionInfoPage(){
		
		GeneratedJSPFile jsp = new GeneratedJSPFile();
		startNewJob(jsp);
		jsp.setName(getVersionInfoPageName());
		jsp.setPackage(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED)+".jsp");
		
		ident = 0;
		append(getBaseJSPHeader());
		
		appendString("<!--  generated by JspMafViewGenerator.generateVersionInfoPage -->");
		appendString("<html>");
		increaseIdent();
		appendString("<head>");
		increaseIdent();
		appendString("<title>VersionInfo for <bean:write name=\"documentName\"/></title>");
		//appendString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
		generatePragmas();
		appendString("<link href=\""+getCurrentCSSPath("newadmin.css")+"\" rel=\"stylesheet\" type=\"text/css\">");
		decreaseIdent();
		appendString("</head>");
		appendString("<body>");
		increaseIdent();
		//appendString("<jsp:include page=\""+getMenuName(view)+".jsp\" flush=\"true\"/>");

		int colspan = 2;
		
		appendString("<table width=\"100%\" border=\"0\" cellspacing=\"1\" cellpadding=\"1\">");
		increaseIdent();
		appendString("<tr>");
		increaseIdent();
		appendString("<td colspan=\""+colspan+"\"><img src="+quote(getCurrentImagePath("s.gif"))+" width=\"1\" height=\"1\"></td>");
		decreaseIdent(); 
		appendString("</tr>");
		
		//write header
		appendString("<tr class=\"lineCaptions\">");
		increaseIdent();
		appendString("<td colspan=\"2\">VersionInfo for document</td>");
		decreaseIdent();
		appendString("</tr>");
		
		appendString("<tr class=\"lineLight\">");
		appendIncreasedString("<td width=\"20%\">Document name: </td>");
		appendIncreasedString("<td width=\"80%\"><bean:write name="+quote("documentName")+"/></td>");
		appendString("</tr>");

		appendString("<tr class=\"lineDark\">");
		appendIncreasedString("<td width=\"20%\">Document type: </td>");
		appendIncreasedString("<td width=\"80%\"><bean:write name="+quote("documentType")+"/></td>");
		appendString("</tr>");

		appendString("<tr class=\"lineLight\">");
		appendIncreasedString("<td width=\"20%\">Last update: </td>");
		appendIncreasedString("<td width=\"80%\"><bean:write name="+quote("lastUpdate")+"/></td>");
		appendString("</tr>");

		appendString("<tr class=\"lineDark\">");
		increaseIdent();
		appendString("<td colspan=\"2\">&nbsp;</td>");
		decreaseIdent();
		appendString("</tr>");

		appendString("<tr class=\"lineLight\">");
		appendIncreasedString("<td width=\"20%\">&nbsp;</td>");
		appendIncreasedString("<td width=\"80%\"><a href=\"javascript:history.back();\">Back</a></td>");
		appendString("</tr>");

		decreaseIdent();
		appendString("</table>");
		decreaseIdent();
		//appendString("<jsp:include page=\""+getFooterName(view)+".jsp\" flush=\"true\"/>");
		appendString("</body>");
		decreaseIdent();
		appendString("</html>");
		appendString("<!-- / generated by JspMafViewGenerator.generateVersionInfoPage -->");
		append(getBaseJSPFooter()); 
		return jsp;
	}
	
	private String generateElementHeader(MetaViewElement element){
		if (element instanceof MetaFieldElement)
			return generateFieldHeader((MetaFieldElement)element);
		return "";
	}
	
	private String generateFieldHeader(MetaFieldElement element){		
		String name = element instanceof MultilingualFieldElement ? element.getVariableName() : element.getName();
		String header =  "";
		if (element.isComparable()){
			
			String action = StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_SHOW);
			action = action+"?"+ViewConstants.PARAM_SORT_TYPE_NAME+"="+name;
			String actionAZ = action + "&" + ViewConstants.PARAM_SORT_ORDER + "="+ViewConstants.VALUE_SORT_ORDER_ASC; 
			String actionZA = action + "&" + ViewConstants.PARAM_SORT_ORDER + "="+ViewConstants.VALUE_SORT_ORDER_DESC; 
			header += "<logic:equal name="+quote("currentSortCode")+" value="+quote(name+"_"+ViewConstants.VALUE_SORT_ORDER_ASC)+"><a href="+quote(generateTimestampedLinkPath(actionZA))+"class=\"down\">"+StringUtils.capitalize(name)+"</a></logic:equal><logic:equal name="+quote("currentSortCode")+" value="+quote(name+"_"+ViewConstants.VALUE_SORT_ORDER_DESC)+"><a href="+quote(generateTimestampedLinkPath(actionAZ))+"class=\"up\">"+StringUtils.capitalize(name)+"</a></logic:equal><logic:notEqual name="+quote("currentSortCode")+" value="+quote(name+"_"+ViewConstants.VALUE_SORT_ORDER_ASC)+"><logic:notEqual name="+quote("currentSortCode")+" value="+quote(name+"_"+ViewConstants.VALUE_SORT_ORDER_DESC)+"><a href="+quote(generateTimestampedLinkPath(actionAZ))+">"+StringUtils.capitalize(name)+"</a></logic:notEqual></logic:notEqual>";
		}
		else {header =  StringUtils.capitalize(name);}
		String displayLanguageCheck = "";
		if(element instanceof MultilingualFieldElement) {
			MultilingualFieldElement multilangualElement = (MultilingualFieldElement) element;
			displayLanguageCheck = "class=\"lang_hide lang_"+multilangualElement.getLanguage()+"\" <logic:equal name=\"display" + multilangualElement.getLanguage() + "\" value=\"false\">style=\"display:none\"</logic:equal>";			
		}
		
		return "<td " + displayLanguageCheck + ">"+header+"</td>";
	}
	
	private String generateElement(String entryName, MetaViewElement element,MetaDocument doc){
		if (element instanceof MetaFieldElement)
			return getField(entryName, (MetaFieldElement)element);
		if (element instanceof MetaFunctionElement)
			return getFunction(entryName, (MetaFunctionElement)element,doc);
		if (element instanceof MetaCustomFunctionElement)
			return getCustomFunction(entryName, (MetaCustomFunctionElement)element);
		
		return "";
	}
	
	private String getField(String entryName, MetaFieldElement element){
		if (((MetaModuleSection)currentSection).getDocument().getField(element.getName()).getType().equals("image") && element.getDecorator()==null)
			return generateImage(entryName, element);
		String elementName = element instanceof MultilingualFieldElement ? element.getVariableName() : element.getName();
		
		String displayLanguageCheck = "";
		if(element instanceof MultilingualFieldElement) {
			MultilingualFieldElement multilangualElement = (MultilingualFieldElement) element;
			displayLanguageCheck = "class=\"lang_hide lang_"+multilangualElement.getLanguage()+"\" <logic:equal name=\"display" + multilangualElement.getLanguage() + "\" value=\"false\">style=\"display:none\"</logic:equal>";			
		}
		
		return "<td " + displayLanguageCheck + "><bean:write filter=\"false\" name="+quote(entryName)+" property=\""+elementName+"\"/></td>";
		//return "<td><bean:write name="+quote(entryName)+" property=\""+element.getName()+"\"/></td>";
	}
	
	private String generateImage(String entryName, MetaFieldElement element){
		String ret = "";
		ret += "<td>";
		ret += "<logic:equal name="+quote(entryName)+" property="+quote(element.getName())+" value="+quote("")+">";
		ret += "none";
		ret += "</logic:equal>";
		ret += "<logic:notEqual name="+quote(entryName)+" property="+quote(element.getName())+" value="+quote("")+">";
		String imagePath = "getFile?pName=<bean:write name="+quote(entryName)+" property="+quote(element.getName())+"/>";
		ret += "<a href="+quote(imagePath)+" target="+quote("_blank")+"><img src="+quote(imagePath)+ " width="+quote(50)+" height="+quote(50)+" border="+quote(0)+"></a>";
		ret += "</logic:notEqual>";
		ret += "</td>";
		return ret;
	}

	private String getFunction(String entryName, MetaFunctionElement element, MetaDocument doc){
		
		if (element.getName().equals("version")){
			return getVersionFunction(entryName, element);
		}

		if (element.getName().equals("delete")){
			return getDeleteFunction(entryName, element);
		}

		if (element.getName().equals("deleteWithConfirmation")){
			return getDeleteWithConfirmationFunction(entryName, element);
		}

		if (element.getName().equals("edit"))
			return getEditFunction(entryName, element);
			
		if (element.getName().equals("duplicate"))
			return getDuplicateFunction(entryName, element);

        if (element.getName().equals("lock") && StorageType.CMS.equals(doc.getParentModule().getStorageType()))
            return getLockFunction(entryName, element);
        
        if (element.getName().equals("unlock") && StorageType.CMS.equals(doc.getParentModule().getStorageType()))
            return getUnLockFunction(entryName, element);

        return "";
		//return "<td><bean:write name="+quote(entryName)+" property=\""+element.getName()+"\"/></td>";
	}
	
	private String getCustomFunction(String entryName, MetaCustomFunctionElement element){
		String caption = element.getCaption();
		String link = element.getLink();
		link = StringUtils.replace(link, "$plainId", "<bean:write name="+quote(entryName)+" property=\"plainId\"/>");
		return "<a href="+quote(generateTimestampedLinkPath(link))+">"+caption+"</a>";
	}

	private String getFunctionEditor(MetaDocument doc, MetaFunctionElement element){
		if (element.getName().equals("cancel")) {
			String onClick = "return confirm('All unsaved data will be lost!!!. Document will be unlocked"; 
			String cancel = StrutsConfigGenerator.getPath(((MetaModuleSection) currentSection).getDocument(), StrutsConfigGenerator.ACTION_CLOSE);
			cancel += "?pId=<bean:write name=" + quote(StrutsConfigGenerator.getDialogFormName(currentDialog, doc)) + " property=\"id\"/>";
			return "<a href=" + cancel + " class=\"button\" onClick="+onClick+"><span>Close</span></a>";
		}
		if (element.getName().equals("update")) {
			return getUpdateAndCloseFunction(doc, element);
		}

		if (element.getName().equals("updateAndStay")) {
			return getUpdateAndStayFunction(doc, element);
		}
		if (element.getName().equals("updateAndClose")) {
			return getUpdateAndCloseFunction(doc, element);
		}

		if (element.getName().equals("lock") && StorageType.CMS.equals(doc.getParentModule().getStorageType())) {
			//For now we dont draw Lock and Unlock functions here
			//return getLockFunctionLink(doc, element);
		}

		if (element.getName().equals("unlock") && StorageType.CMS.equals(doc.getParentModule().getStorageType())) {
			//For now we dont draw Lock and Unlock functions here
			//return getUnLockFunctionLink(doc, element);
		}


		return "";
	}

	/**
	 * Dialog unlock link.
	 *
	 * @param doc
	 * @param element
	 * @return
	 */
	private String getUnLockFunctionLink(MetaDocument doc, MetaFunctionElement element) {
		String result = "<logic:equal name=" + quote(StrutsConfigGenerator.getDialogFormName(currentDialog, doc)) + " property=" + quote(LockableObject.INT_LOCK_PROPERTY_NAME) + " value=" + quote("true") + "> \n";
		String path = StrutsConfigGenerator.getPath(doc, StrutsConfigGenerator.ACTION_UNLOCK);
		path += "?pId=<bean:write name=" + quote(StrutsConfigGenerator.getDialogFormName(currentDialog, doc)) + " property=\"id\"/>";
		path += "&nextAction=showEdit";
		result += " \t<a href=\"#\" onClick= "+quote("lightbox(' All unsaved data will be lost!!! UnLock  "+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+" : <bean:write name="
				+ quote(StrutsConfigGenerator.getDialogFormName(currentDialog, doc)) + " property=\"id\"/>;','<ano:tslink>"+path+"</ano:tslink>');")+">&nbsp;&raquo&nbsp;UnLock&nbsp;</a>";
		result += "</logic:equal>";
		return result;
	}

	/**
	 * Dialog Lock link.
	 *
	 * @param doc
	 * @param element
	 * @return
	 */
	private String getLockFunctionLink(MetaDocument doc, MetaFunctionElement element) {
		String result = "<logic:equal name=" + quote(StrutsConfigGenerator.getDialogFormName(currentDialog, doc)) + " property=" + quote(LockableObject.INT_LOCK_PROPERTY_NAME) + " value=" + quote("false") + "> \n";
		String path = StrutsConfigGenerator.getPath(doc, StrutsConfigGenerator.ACTION_LOCK);
		path += "?pId=<bean:write name=" + quote(StrutsConfigGenerator.getDialogFormName(currentDialog, doc)) + " property=\"id\"/>";
		path += "&nextAction=showEdit";
		result += " \t<a href=\"#\" onClick= "+quote("lightbox('All unsaved data will be lost!!!. Really lock  "+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+" with id : <bean:write name="
				+ quote(StrutsConfigGenerator.getDialogFormName(currentDialog, doc)) + " property=\"id\"/>;?','<ano:tslink>"+path+"</ano:tslink>');")+">&nbsp;&raquo&nbsp;Lock&nbsp;</a>";
		result += "</logic:equal>";
		return result;
	}

	/**
     * Lock link for List show!
     */
    private String getUnLockFunction(String entryName, MetaFunctionElement element) {
        String path = StrutsConfigGenerator.getPath(((MetaModuleSection) currentSection).getDocument(), StrutsConfigGenerator.ACTION_UNLOCK);
        path += "?pId=<bean:write name=" + quote(entryName) + " property=\"plainId\"/>";
        path+="&nextAction=showList";
		String alt = "Locked by: <bean:write name="+quote(entryName)+" property="+quote(LockableObject.INT_LOCKER_ID_PROPERTY_NAME)+"/>, at: <bean:write name="+quote(entryName)+" property="+quote(LockableObject.INT_LOCKING_TIME_PROPERTY_NAME)+"/>";
        String link = "<a href=\"#\" onClick= "+quote("lightbox('"+alt+"<br /> Unlock "+((MetaModuleSection)currentSection).getDocument().getName()+" with id: <bean:write name="+quote(entryName)+" property=\"id\"/>?','<ano:tslink>"+path+"</ano:tslink>');")+">"+getUnLockImage(alt)+"</a>";
        String result  = "<logic:equal name=" + quote(entryName) + " property=" + quote(LockableObject.INT_LOCK_PROPERTY_NAME) + " value=" + quote("true") + ">";
        result+=link;
        result+= "</logic:equal>";
        return result;
    }

    /**
     * UnLock link for List show!
     */
    private String getLockFunction(String entryName, MetaFunctionElement element) {
        String path = StrutsConfigGenerator.getPath(((MetaModuleSection) currentSection).getDocument(), StrutsConfigGenerator.ACTION_LOCK);
        path += "?pId=<bean:write name=" + quote(entryName) + " property=\"plainId\"/>";
        path+="&nextAction=showList";
        String link =  "<a href=\"#\" onClick= "+quote("lightbox('Lock "+
				((MetaModuleSection)currentSection).getDocument().getName()+" with id: <bean:write name="+quote(entryName)+" property=\"id\"/>?','<ano:tslink>"+path+"</ano:tslink>');")+">"+getLockImage()+"</a>" ;
        String result  = "<logic:equal name=" + quote(entryName) + " property=" + quote(LockableObject.INT_LOCK_PROPERTY_NAME) + " value=" + quote("false") + ">";
        result+=link;
        result+= "</logic:equal>";
        return result;
    }

    private String getUpdateAndStayFunction(MetaDocument doc, MetaFunctionElement element){
		if(StorageType.CMS.equals(doc.getParentModule().getStorageType())){
			//creating logic for hiding or showing current operation link in Locking CASE!!!!!
			String result = "<logic:equal name=" + quote(StrutsConfigGenerator.getDialogFormName(currentDialog, doc)) +
					" property=" + quote(LockableObject.INT_LOCK_PROPERTY_NAME) + " value=" + quote("true") + "> \n";
			result+="  <logic:equal name=" + quote(StrutsConfigGenerator.getDialogFormName(currentDialog, doc)) +
					" property=" + quote(LockableObject.INT_LOCKER_ID_PROPERTY_NAME) + " value=" + quote("<%=(java.lang.String)session.getAttribute(\\"+quote("currentUserId\\")+")%>") + "> \n";
			result+="\t<a href=\"#\" class=\"button\" onClick=\"document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+
					".nextAction.value='stay'; document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+".submit(); return false\"><span><bean:write name=\"apply.label.prefix\"/></span></a> \n";
			result+="  </logic:equal> \n";
			result+="</logic:equal> \n";
			result+="<logic:equal name=" + quote(StrutsConfigGenerator.getDialogFormName(currentDialog, doc)) + " property=" + quote(LockableObject.INT_LOCK_PROPERTY_NAME) + " value=" + quote("false") + "> \n";
			result+="\t<a href=\"#\" class=\"button\" onClick=\"document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+
					".nextAction.value='stay'; document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+".submit(); return false\"><span><bean:write name=\"apply.label.prefix\"/></span></a>\n";
			result+="</logic:equal> \n";
			return result;
		}
		return "<a href=\"#\" class=\"button\" onClick=\"document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+
				".nextAction.value='stay'; document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+".submit(); return false\"><span><bean:write name=\"apply.label.prefix\"/></span></a>";
	}
	private String getUpdateAndCloseFunction(MetaDocument doc, MetaFunctionElement element){
		if(StorageType.CMS.equals(doc.getParentModule().getStorageType())){
			//creating logic for hiding or showing current operation link in Locking CASE!!!!!
			String result = "<logic:equal name=" + quote(StrutsConfigGenerator.getDialogFormName(currentDialog, doc)) +
					" property=" + quote(LockableObject.INT_LOCK_PROPERTY_NAME) + " value=" + quote("true") + "> \n";
			result+="  <logic:equal name=" + quote(StrutsConfigGenerator.getDialogFormName(currentDialog, doc)) +
					" property=" + quote(LockableObject.INT_LOCKER_ID_PROPERTY_NAME) + " value=" + quote("<%=(java.lang.String)session.getAttribute(\\"+quote("currentUserId\\")+")%>") + "> \n";
			result+="\t<a href=\"#\" class=\"button\" onClick=\"document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+
					".nextAction.value='close'; document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+".submit(); return false\"><span><bean:write name=\"save.label.prefix\"/></span></a> \n";
			result+="  </logic:equal> \n";
			result+="</logic:equal> \n";
			result+="<logic:equal name=" + quote(StrutsConfigGenerator.getDialogFormName(currentDialog, doc)) + " property=" + quote(LockableObject.INT_LOCK_PROPERTY_NAME) + " value=" + quote("false") + "> \n";
			result+="\t<a href=\"#\" class=\"button\" onClick=\"document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+
					".nextAction.value='close'; document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+".submit(); return false\"><span><bean:write name=\"save.label.prefix\"/></span></a> \n";
			result+="</logic:equal> \n";
			return result;
		}
		return "<a href=\"#\" class=\"button\" onClick=\"document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+
				".nextAction.value='close'; document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+".submit(); return false\"><span><bean:write name=\"save.label.prefix\"/></span></a>";
	}			
	
	
	private String getDuplicateFunction(String entryName, MetaFunctionElement element){
		String path = StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_DUPLICATE);
		path += "?pId=<bean:write name="+quote(entryName)+" property=\"plainId\"/>";
		
		return "<a href="+quote("<ano:tslink>"+path+"</ano:tslink>")+">"+getDuplicateImage()+"</a>" ;
	}

	private String getVersionFunction(String entryName, MetaFunctionElement element){
		String path = StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_VERSIONINFO);
		path += "?pId=<bean:write name="+quote(entryName)+" property=\"plainId\"/>";
		
		return "<a href="+quote("<ano:tslink>"+path+"</ano:tslink>")+">"+getImage("version", "LastUpdate: <bean:write name="+quote(entryName)+" property="+quote("documentLastUpdateTimestamp")+"/>")+"</a>" ;
	}

	private String getDeleteFunction(String entryName, MetaFunctionElement element){
		String path = StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_DELETE);
		path += "?pId=<bean:write name="+quote(entryName)+" property=\"plainId\"/>";
		
		return "<a href="+quote("<ano:tslink>"+path+"</ano:tslink>")+">"+getDeleteImage()+"</a>" ;
	}

	private String getDeleteWithConfirmationFunction(String entryName, MetaFunctionElement element){
		String path = StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_DELETE);
		path += "?pId=<bean:write name="+quote(entryName)+" property=\"plainId\"/>";
		return "<a href=\"#\" onClick="+quote("lightbox('Really delete "+
				((MetaModuleSection)currentSection).getDocument().getName()+" with id: <bean:write name="+quote(entryName)+" property=\"id\"/>?','<ano:tslink>"+path+"</ano:tslink>');")+">"+getDeleteImage()+"</a>" ;
	}

	private String getEditFunction(String entryName, MetaFunctionElement element){
		String path = StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_EDIT);
		path += "?pId=<bean:write name="+quote(entryName)+" property=\"plainId\"/>";
		
		return "<a href="+quote("<ano:tslink>"+path+"</ano:tslink>")+">"+getEditImage()+"</a>" ;
	}

	private String generateNewFunction(String entryName, MetaFunctionElement element){
		String path = StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_NEW);
		return "<a href="+quote(generateTimestampedLinkPath(path))+" class=\"button\"><span>"+getImage("add", "add new "+((MetaModuleSection)currentSection).getDocument().getName())+"Add new element</span></a>" ;
	}

	
	public String getFormIncludePageName(MetaForm form){
	    return StringUtils.capitalize(form.getId())+"AutoForm";
	}

	private String generateFormField(MetaFormSingleField field, String className){
		return generateFormField(field.getName(), field, className);
	}


	private String generateFormField(String name, MetaFormSingleField field, String className){
		if (field.getType().equals("boolean"))
			return "<input type="+quote("checkbox")+" name="+quote(name)+" class="+quote(className)+"/>";

		if (field.getType().equals("text"))
			return "<textarea id="+quote(name)+"  name="+quote(name)+" class="+quote(className)+" rows="+quote(3)+" cols="+quote(quote(field.getSize()))+"></textarea>";
		
		if (field.getType().equals("string"))
			return "<input type=\"text\" size="+quote(field.getSize())+" name="+quote(name)+" class="+quote(className)+"/>";
			
		if (field.getType().equals("spacer"))
			return "";
		
		throw new RuntimeException("Unsupported field type: "+field.getType());
		
	}
	
	public String generateFormInclude(MetaForm form){
		System.out.println("generating form "+form);
	    String ret = "";
	    resetIdent();

		String formName = StrutsConfigGenerator.getFormName(form);
		

		ret += getBaseJSPHeader();
		appendString("<!-- form: "+formName+" -->");
		
		appendString("<table width="+quote("100%")+" cellspacing="+quote("1")+" cellpadding="+quote("1")+" border="+quote("0")+">");
		increaseIdent();
		appendString("<html:form action="+quote(StrutsConfigGenerator.getFormPath(form))+">");
		List<MetaFormField> elements = form.getElements();
		for (int i=0; i<elements.size(); i++){
///*
		    MetaFormField element = (MetaFormField)elements.get(i);
		    if (element.isSingle()){
		    	MetaFormSingleField field = (MetaFormSingleField )element;
				appendString("<tr class="+quote("qs_info")+">");
				increaseIdent();
				appendString("<td width=10%><img src="+quote("<bean:message key="+quote("emptyimage")+"/>")+" width="+quote(10)+" height="+quote(1)+"/></td>");
				appendString("<td class="+quote("qs_info")+" align="+quote("left")+">");
	
				String htmlFieldDeclaration = generateFormField(field, field.getType().equals("boolean") ? "qs_input" : "qs_info");
				
				if (field.getType().equals("boolean")){
					appendString(htmlFieldDeclaration);
					appendString("&nbsp;<bean:message key="+quote(field.getTitle())+"/>");
				}else{
					appendString("<bean:message key="+quote(field.getTitle())+"/>"+(!(field.isSpacer()) ? ":&nbsp;<br><br>" : ""));
					if (htmlFieldDeclaration.length()>0)
						appendString(htmlFieldDeclaration);
				}
				if (!field.isSpacer())
					appendString("<br><br>");
				appendString("</td>");
				appendString("<td width=10%><img src="+quote("<bean:message key="+quote("emptyimage")+"/>")+" width="+quote(10)+" height="+quote(1)+"/></td>");
				decreaseIdent();
				appendString("</tr>");
		    }
		    
		    if (element.isComplex()){
		    	MetaFormTableField table = (MetaFormTableField)element;
		    	//now write inner table;
				appendString("<tr class="+quote("qs_info")+">");
				increaseIdent();
				appendString("<td width=10%><img src="+quote("<bean:message key="+quote("emptyimage")+"/>")+" width="+quote(10)+" height="+quote(1)+"/></td>");
				appendString("<td class="+quote("qs_info")+" >");
				increaseIdent();
				appendString("<table width="+quote("100%")+" cellpadding="+quote(0)+" cellspacing="+quote(0)+" border="+quote(0)+">");
				increaseIdent();
				
				//generate table headers
				appendString("<tr>");
				List<MetaFormTableColumn> columns = table.getColumns();
				for (MetaFormTableColumn col : columns){
					MetaFormTableHeader header = col.getHeader();
					appendIncreasedString("<td><strong><bean:message key="+quote(header.getKey())+"/></strong></td>");
				}

				appendString("</tr>");
					
				//generate table rows
				for (int r=0; r<table.getRows(); r++){
					appendString("<tr>");
					increaseIdent();
					for (int c=0; c<columns.size(); c++){
						MetaFormTableColumn col = (MetaFormTableColumn)columns.get(c);
						//System.out.println("Generating column: "+col);
						appendString("<td width="+quote(col.getHeader().getWidth())+">");
						appendIncreasedString(generateFormField(table.getVariableName(r,c), col.getField(), ""));
						appendString("</td>");
					}					
					decreaseIdent();
					appendString("</tr>");
				}
				
				
				decreaseIdent();
				appendString("</table>");
				decreaseIdent();
				appendString("</td>");
				appendString("<td width=10%><img src="+quote("<bean:message key="+quote("emptyimage")+"/>")+" width="+quote(10)+" height="+quote(1)+"/></td>");
				appendString("</tr>");		    	
		    }
		    
//*/		    
		}

		decreaseIdent();
		appendString("</html:form>");
		decreaseIdent();
		appendString("</table>");

		ret += getBaseJSPFooter();
	    return ret;
	}
	

}
