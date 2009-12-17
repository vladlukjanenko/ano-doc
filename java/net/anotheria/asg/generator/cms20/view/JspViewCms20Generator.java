package net.anotheria.asg.generator.cms20.view;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import net.anotheria.asg.generator.view.AbstractJSPGenerator;
import net.anotheria.asg.generator.view.JspMenuGenerator;
import net.anotheria.asg.generator.view.ModuleActionsGenerator;
import net.anotheria.asg.generator.view.ModuleBeanGenerator;
import net.anotheria.asg.generator.view.StrutsConfigGenerator;
import net.anotheria.asg.generator.view.ViewConstants;
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
public class JspViewCms20Generator extends AbstractJSPGenerator implements IGenerator{
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
		return "../../shared/jsp/"+JspMenuGenerator.getMenuPageName();		
	}

	private String getMenuName(MetaView view){
		return "../../shared/jsp/"+StringUtils.capitalize(view.getName())+"Menu";		
	}
	
	private String getFooterName(MetaView view){
		return "../../shared/jsp/"+StringUtils.capitalize(view.getName())+"Footer";		
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
	
	private GeneratedJSPFile generateListPage(MetaModuleSection section, MetaDocument doc, MetaListProperty list){
		
		GeneratedJSPFile jsp = new GeneratedJSPFile();
		startNewJob(jsp);
		
		jsp.setName(getContainerPageName(doc, list));
		jsp.setPackage(getContext().getJspPackageName(section.getModule()));
		
		resetIdent();

		String formName = StrutsConfigGenerator.getContainerEntryFormName(doc, list);
		MetaProperty p = list.getContainedProperty();

		append(getBaseJSPHeader());
		
		appendString("<html:html>");
		increaseIdent();
		appendString("<head>");
		increaseIdent();
		appendString("<title>Edit "+doc.getName()+StringUtils.capitalize(list.getName())+"</title>");
		generatePragmas();
		appendString("<link href=\""+getCurrentCSSPath("admin.css")+"\" rel=\"stylesheet\" type=\"text/css\">");
		decreaseIdent();
		appendString("</head>");
		appendString("<body>");
		increaseIdent();		

		
		appendString("<table width="+quote("100%")+" cellspacing="+quote("1")+" cellpadding="+quote("1")+" border="+quote("0")+">");
		increaseIdent();
		appendString("<tr class="+quote("lineCaptions")+">");
		appendIncreasedString("<td width=\"1%\">Pos</td>");
		appendIncreasedString("<td>"+StringUtils.capitalize(list.getName())+"</td>");
		appendIncreasedString("<td>"+"Description"+"</td>");
		appendIncreasedString("<td width=\"1%\">&nbsp;</td>");
		appendIncreasedString("<td width=\"1%\">&nbsp;</td>");
		appendIncreasedString("<td width=\"1%\">&nbsp;</td>");
		appendIncreasedString("<td width=\"1%\">&nbsp;</td>");
		appendIncreasedString("<td width=\"1%\">&nbsp;</td>");
		appendString("</tr>");
		appendString("<logic:iterate name="+quote("elements")+" id="+quote("element")+" type="+quote(ModuleBeanGenerator.getContainerEntryFormImport(doc, list))+" indexId="+quote("ind")+">");
		increaseIdent();
		appendString("<tr class=\"<%=ind.intValue()%2==0 ? \"lineLight\" : \"lineDark\"%> highlightable\">");
		increaseIdent();
		appendString("<td width="+quote("1%")+"><bean:write name="+quote("element")+" property="+quote("position")+"/></td>");
		
		if (p.isLinked()){
			
			MetaLink link2p = (MetaLink)p;
			MetaModule targetModule = link2p.getLinkTarget().indexOf('.')== -1 ?
					doc.getParentModule() : GeneratorDataRegistry.getInstance().getModule(link2p.getTargetModuleName());

			MetaDocument linkTarget = targetModule.getDocumentByName(link2p.getTargetDocumentName());
			String targetLinkAction = StrutsConfigGenerator.getPath(linkTarget, StrutsConfigGenerator.ACTION_EDIT);
			 
			
			appendString("<td><a href=<ano:tslink>"+quote(targetLinkAction+"?pId=<bean:write name="+quote("element")+" property="+quote(list.getContainedProperty().getName())+"/></ano:tslink>")+"><bean:write name="+quote("element")+" property="+quote(list.getContainedProperty().getName())+"/></a></td>");
			appendString("<td><a href=<ano:tslink>"+quote(targetLinkAction+"?pId=<bean:write name="+quote("element")+" property="+quote(list.getContainedProperty().getName())+"/></ano:tslink>")+"><bean:write name="+quote("element")+" property="+quote("description")+"/></a></td>");
		}else{
			appendString("<td><bean:write name="+quote("element")+" property="+quote(list.getContainedProperty().getName())+"/></td>");
			appendString("<td><bean:write name="+quote("element")+" property="+quote("description")+"/></td>");
		}
		
		
		String parameter = "pId=<bean:write name="+quote("element")+" property="+quote("ownerId")+"/>";
		parameter += "&pPosition=<bean:write name="+quote("element")+" property="+quote("position")+"/>";
		appendString("<td width="+quote("1%")+"><a href="+quote(StrutsConfigGenerator.getContainerPath(doc, list, StrutsConfigGenerator.ACTION_MOVE)+"?dir=top&"+parameter)+">"+getTopImage("move to top")+"</a></td>");
		appendString("<td width="+quote("1%")+"><a href="+quote(StrutsConfigGenerator.getContainerPath(doc, list, StrutsConfigGenerator.ACTION_MOVE)+"?dir=up&"+parameter)+">"+getUpImage("move up")+"</a></td>");
		appendString("<td width="+quote("1%")+"><a href="+quote(StrutsConfigGenerator.getContainerPath(doc, list, StrutsConfigGenerator.ACTION_MOVE)+"?dir=down&"+parameter)+">"+getDownImage("move down")+"</a></td>");
		appendString("<td width="+quote("1%")+"><a href="+quote(StrutsConfigGenerator.getContainerPath(doc, list, StrutsConfigGenerator.ACTION_MOVE)+"?=bottom&"+parameter)+">"+getBottomImage("move to bottom")+"</a></td>");
		appendString("<td width="+quote("1%")+"><a href="+quote(StrutsConfigGenerator.getContainerPath(doc, list, StrutsConfigGenerator.ACTION_DELETE)+"?"+parameter)+">"+getDeleteImage("delete row")+"</a></td>");
		decreaseIdent();
		appendString("</tr>");
		decreaseIdent();
		appendString("</logic:iterate>");
		decreaseIdent();
		appendString("</table>");
		
//*/
		
		appendString("<br>");
		appendString("<table width="+quote("100%")+" cellspacing="+quote("1")+" cellpadding="+quote("1")+" border="+quote("0")+">");
		increaseIdent();
		appendString("<html:form action="+quote(StrutsConfigGenerator.getContainerPath(doc, list, StrutsConfigGenerator.ACTION_ADD))+">");
		appendString("<html:hidden property="+quote("ownerId")+"/>");
		appendString("<input type="+quote("hidden")+" name="+quote("pId")+" value=\"<bean:write name="+quote(formName)+" property="+quote("ownerId")+"/>\">");
		
		appendString("<tr class="+quote("lineCaptions")+">");
		appendIncreasedString("<td colspan="+quote("2")+">Add row:</td>");
		appendString("</tr>");


		appendString("<tr class="+quote("lineLight")+">");
		increaseIdent();
	        
		appendString("<td align=\"right\" width=\"35%\">");
		increaseIdent();
		String name = p.getName();
		if (name==null || name.length()==0)
			name = "&nbsp;";
		appendString(name+":");
		decreaseIdent(); 
		appendString("</td>");
		decreaseIdent();
	
		appendString("<td align=\"left\" width=\"65%\">&nbsp;");
		if (!p.isLinked() && !(p instanceof MetaEnumerationProperty)){
			String field = "";
			field += "<input type=\"text\" name="+quote(name);
			field += " value=\"<bean:write name="+quote(StrutsConfigGenerator.getContainerEntryFormName(doc,list ))+" property="+quote(name)+"/>";
			field += "\">";
			appendIncreasedString(field);
		}else{
			//String select = "";
			appendString("<html:select size=\"1\" property="+quote(name)+">");
			appendIncreasedString("<html:optionsCollection property="+quote(name+"Collection")+" filter=\"false\"/>");
			appendString("</html:select>");
		}
			
		appendString("</td>");

		appendString("<tr class="+quote("lineDark")+">");
		appendIncreasedString("<td colspan="+quote("2")+">&nbsp;");
		appendString("</tr>");

		appendString("<tr class="+quote("lineLight")+">");
		increaseIdent();
		appendString("<td align=\"right\" width=\"35%\">&nbsp;</td>");
		appendString("<td align=\"left\" width=\"65%\">");
		appendIncreasedString("<a href="+quote("#")+" onClick="+quote("document."+formName+".submit()")+">&nbsp;&raquo&nbsp;Add&nbsp;</a>");
		appendString("</td>");
		decreaseIdent();
		appendString("</tr>");

		appendString("</html:form>");
		decreaseIdent();
		appendString("</table>");

		//QUICK ADD Form 
		if (p.isLinked()){
			formName = StrutsConfigGenerator.getContainerQuickAddFormName(doc, list);
			appendString("<br>");
			appendString("<table width="+quote("100%")+" cellspacing="+quote("1")+" cellpadding="+quote("1")+" border="+quote("0")+">");
			increaseIdent();
			appendString("<html:form action="+quote(StrutsConfigGenerator.getContainerPath(doc, list, StrutsConfigGenerator.ACTION_QUICK_ADD))+">");
			appendString("<html:hidden property="+quote("ownerId")+"/>");
			appendString("<input type="+quote("hidden")+" name="+quote("pId")+" value=\"<bean:write name="+quote(formName)+" property="+quote("ownerId")+"/>\">");
			
			appendString("<tr class="+quote("lineCaptions")+">");
			appendIncreasedString("<td colspan="+quote("2")+">Quick add some items by id:</td>");
			appendString("</tr>");
	
	
			appendString("<tr class="+quote("lineLight")+">");
			increaseIdent();
		        
			p = list.getContainedProperty();
			appendString("<td align=\"right\" width=\"35%\">");
			increaseIdent();
			name = p.getName();
			if (name==null || name.length()==0)
				name = "&nbsp;";
			appendString("ids to add :");
			decreaseIdent(); 
			appendString("</td>");
			decreaseIdent();
		
			appendString("<td align=\"left\" width=\"65%\">&nbsp;");
			String field = "";
			field += "<input type=\"text\" name="+quote("quickAddIds");
			field += " value=\"\"/>";
			appendIncreasedString(field);
				
			appendString("&nbsp;<i>comma separated list.</td>");
	
			appendString("<tr class="+quote("lineDark")+">");
			appendIncreasedString("<td colspan="+quote("2")+">&nbsp;");
			appendString("</tr>");
	
			appendString("<tr class="+quote("lineLight")+">");
			increaseIdent();
			appendString("<td align=\"right\" width=\"35%\">&nbsp;</td>");
			appendString("<td align=\"left\" width=\"65%\">");
			appendIncreasedString("<a href="+quote("#")+" onClick="+quote("document."+formName+".submit()")+">&nbsp;&raquo&nbsp;QuickAdd&nbsp;</a>");
			appendString("</td>");
			decreaseIdent();
			appendString("</tr>");
	
			appendString("</html:form>");
			decreaseIdent();
			appendString("</table>");
		}
		//QUICK ADD END
		
		decreaseIdent();
		
		
		appendString("</body>");
		decreaseIdent();
		appendString("</html:html>");
		
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
		
		appendString("<html:html>");
		increaseIdent();
		appendString("<head>");
		increaseIdent();
		appendString("<title>Edit "+doc.getName()+StringUtils.capitalize(table.getName())+"</title>");
		generatePragmas();
		appendString("<link href=\""+getCurrentCSSPath("admin.css")+"\" rel=\"stylesheet\" type=\"text/css\">");
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
		
		append(getBaseJSPFooter());
		
	    
		return jsp;
	}
	
	private GeneratedJSPFile generateDialog(MetaDialog dialog, MetaModuleSection section, MetaView view){
		
		GeneratedJSPFile jsp = new GeneratedJSPFile();
		startNewJob(jsp);
		jsp.setName(getDialogName(dialog, section.getDocument()));
		jsp.setPackage(getContext().getJspPackageName(section.getModule()));
		
		boolean multilangDialog = GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported() && section.getDocument().isMultilingual();
		
		resetIdent();
		
		currentDialog = dialog;
		
		append(getBaseJSPHeader());
		
		if(multilangDialog){
			//*** Generate definition of bean "multilang" that contains current multilang state: multilangEnabled or multilangDisabled ***//
			appendString("<logic:equal name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" property="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" value="+quote("false")+">");
			tag("bean:define", new TagAttribute("id","multilang"), new TagAttribute("value", "multilangEnabled"));
			appendString("</logic:equal>");
			appendString("<logic:equal name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" property="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" value="+quote("true")+">");
			tag("bean:define", new TagAttribute("id","multilang"), new TagAttribute("value", "multilangDisabled"));
			appendString("</logic:equal>");
		}
		
		appendString("<html:html>");
		increaseIdent();
		appendString("<head>");
		increaseIdent();
		appendString("<title>"+dialog.getTitle()+"</title>");
		generatePragmas(view);
		//appendString("<link href=\""+getCurrentCSSPath("admin.css")+"\" rel=\"stylesheet\" type=\"text/css\">");
		//*** CMS2.0 START ***
		
		
		appendString("<link rel=" + quote("stylesheet") + " type=" + quote("text/css") + " href=" + quote(getCurrentYUIPath("core/build/reset-fonts-grids/reset-fonts-grids.css")) + " />");
		appendString("<link rel=" + quote("stylesheet") + " type=" + quote("text/css") + " href=" + quote(getCurrentYUIPath("core/build/fonts/fonts-min.css")) + " />");
		appendString("<link rel=" + quote("stylesheet") + " type=" + quote("text/css") + " href=" + quote(getCurrentYUIPath("core/build/assets/skins/sam/skin.css")) + " />");
		appendString("<link rel=" + quote("stylesheet") + " type=" + quote("text/css") + " href=" + quote(getCurrentYUIPath("core/build/container/assets/skins/sam/container.css")) + " />");
		appendString("<link rel=" + quote("stylesheet") + " type=" + quote("text/css") + " href=" + quote(getCurrentYUIPath("core/build/tabview/assets/skins/sam/tabview.css")) + " />");
		appendString("<link href=\""+getCurrentCSSPath("admin.css")+"\" rel=\"stylesheet\" type=\"text/css\">");
		
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/yahoo-dom-event/yahoo-dom-event.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/element/element-debug.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/datasource/datasource-min.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/autocomplete/autocomplete-min.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/dragdrop/dragdrop-min.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/container/container-min.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/menu/menu-min.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/button/button-min.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/editor/editor-min.js")) + "></script>");
		
//		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/animation/animation-min.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/tabview/tabview-min.js")) + "></script>");
		
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("anoweb/widget/EditorHider.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("anoweb/widget/ComboBox.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentJavaScriptPath("tiny_mce/tiny_mce.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("anoweb/util/FormUtils.js")) + "></script>");
		
		//*** CMS2.0 FINISH ***
		
		
		
		decreaseIdent();
		appendString("</head>");
		appendString("<body class=\"editDialog yui-skin-sam\">");
		increaseIdent();		
		appendString("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
		increaseIdent();
		appendString("<tr>");
		appendIncreasedString("<td class=\"menuTitleSelected\">");
		appendIncreasedString(dialog.getTitle()+"</td>");
		appendString("</tr>");
		decreaseIdent();
		appendString("</table>");
		
		int colspan=2;
		appendString("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
		increaseIdent();
		appendString("<tr>");
		increaseIdent();
		appendString("<td colspan=\""+colspan+"\"><img src="+quote(getCurrentImagePath("s.gif"))+" width=\"1\" height=\"1\"></td>");
		decreaseIdent(); 
		appendString("</tr>");

		// *** END MULILINGUAL COPY *** //
		if (multilangDialog)
			addMultilanguageOperations(section, colspan);
		// *** END MULILINGUAL COPY *** //
		appendString("</table>");
		
		

		// *** LOCKING INFO *** //
        if (StorageType.CMS.equals(((MetaModuleSection) currentSection).getDocument().getParentModule().getStorageType())) {
            appendString("<logic:equal name=" + quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection) currentSection).getDocument())) + " property=" + quote(LockableObject.INT_LOCK_PROPERTY_NAME) + " value=" + quote("true") + ">");
            appendString("<div> Current Document is Locked by <b><bean:write name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" property="+quote(LockableObject.INT_LOCKER_ID_PROPERTY_NAME)+"/></b>");
            appendString("<div> Locking time :  <b><bean:write name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" property="+quote(LockableObject.INT_LOCKING_TIME_PROPERTY_NAME)+"/></b>");
            appendString("</logic:equal>");
        }
        // *** END LOCKING INFO *** //

        //*** CMS2.0 START ***
		List<MetaViewElement> richTextElementsRegistry = new ArrayList<MetaViewElement>();
		List<MetaViewElement> linkElementsRegistry = new ArrayList<MetaViewElement>();
		//*** CMS2.0 FINISH ***
		
		List<MetaViewElement> elements = createMultilingualList(dialog.getElements(),section.getDocument()); 
		
		
		
		
		String defLanguage = GeneratorDataRegistry.getInstance().getContext().getDefaultLanguage();
		
		tagOpen("div", dialog.getName(), "yui-navset");
		if(multilangDialog) {
			//** Generate LanguageTabs **//
			
			appendString("<logic:equal name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" property="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" value="+quote("false")+">");
			tagOpen("ul", "yui-nav");
			appendString("<li class=\"selected\"><a href=\"#LangALL\"><em>All</em></a></li>");
			for (String lang : GeneratorDataRegistry.getInstance().getContext().getLanguages()){
				tagOpen("li");
				appendString("<a href=\"#Lang"+lang+"\"><em>"+lang+"</em></a>");
				tagClose("li");
			}
			tagClose("ul");
			appendString("</logic:equal>");
		}
		
		tagOpen("div", "yui-content");
		
		for (MetaViewElement element: elements){
			//*** CMS2.0 START ***
			if(element instanceof MetaFieldElement){
				MetaDocument doc = ((MetaModuleSection)currentSection).getDocument();
				MetaProperty p = doc.getField(element.getName());
				if(element.isRich())
					if(p.getType().equals("text"))
							richTextElementsRegistry.add(element);
				
				if(p.isLinked())
					linkElementsRegistry.add(element);
			}
			//*** CMS2.0 FINISH ***
		}
		
		//** Generate LanguageTabs: for language ALL **//
		tagOpen("div", "LangALL", "lang ALL");
		
		appendString("<html:form action="+quote(StrutsConfigGenerator.getPath(section.getDocument(), StrutsConfigGenerator.ACTION_UPDATE))+">");		
		appendIncreasedString("<input type="+quote("hidden")+" name="+quote("_ts")+" value="+quote("<%=System.currentTimeMillis()%>")+">");
		appendIncreasedString("<input type="+quote("hidden")+" name="+quote(ModuleBeanGenerator.FLAG_FORM_SUBMITTED)+" value="+quote("true")+">");
		appendIncreasedString("<input type="+quote("hidden")+" name="+quote("nextAction")+" value="+quote("close")+">");

		String elName = null;
		boolean lineDark = true;
		for (MetaViewElement element: elements){
			//HACK: exclude MetaList cause usually it contains MetaFunction Elements
			if (element instanceof MetaListElement)
				continue;
			lineDark = !StringUtils.isEmpty(element.getName()) && element.getName().equals(elName)? lineDark: !lineDark;
			String lang = getElementLanguage(element);
			boolean monolangEl = !(element instanceof MultilingualFieldElement);
			boolean deflangEl = !monolangEl && defLanguage.equals(lang);
			
			elName = element.getName();
			tagOpen("div", "yui-gd " + (lineDark?"lineDark":"lineLight") + (!monolangEl && !deflangEl? " <bean:write name=\"multilang\"/>":""));
			tagOpen("div", "yui-u first");
			appendString((StringUtils.isEmpty(element.getName())?"&nbsp;": (section.getDocument().getField(element.getName()).getName(lang)) + (deflangEl?("<span class="+quote("defLanguageLabel <bean:write name=\"multilang\"/>")+">" + "_DEF" + "</span>"):"")));
			tagClose("div"/*yui-u first*/);
			
			tagOpen("div", "yui-u");
			appendString(getElementEditor(section.getDocument(), element, "ALL"));
			appendString("&nbsp;<i><bean:write name=\"description."+element.getName()+"\" ignore=\"true\"/></i>");
			tagClose("div"/*yui-u*/);
			
	    	tagClose("div"/*yui-gd*/);
		}
		appendString("</html:form>");
		
		tagClose("div"/*LangALL*/);
		
		//** Generate LanguageTabs: defined languages**//
		if(multilangDialog){
			//** Fills LanguageTabs with current language content **//
			appendString("<logic:equal name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" property="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" value="+quote("false")+">");
			//For each language
			for (String lang : GeneratorDataRegistry.getInstance().getContext().getLanguages()){
				tagOpen("div", "Lang" + lang, "lang " + lang);
				lineDark = true;
				//Generate Edit LanguageTab
				for (MetaViewElement element: elements){
					//HACK: exclude MetaList cause usually it contains MetaFunction Elements
					if (element instanceof MetaListElement)
						continue;
					
					if(element instanceof MultilingualFieldElement && !lang.equals(((MultilingualFieldElement)element).getLanguage()))
						continue;
					tagOpen("div", "yui-gd " + ((lineDark = !lineDark)?"lineDark":"lineLight"));
			
					tagOpen("div", "yui-u first");
					
					appendString(StringUtils.isEmpty(element.getName())?"&nbsp;": section.getDocument().getField(element.getName()).getName(lang));
					tagClose("div"/*yui-u first*/);
					
					tagOpen("div", "yui-u");
					appendString(getElementEditor(section.getDocument(), element, lang));
					appendString("&nbsp;<i><bean:write name=\"description."+element.getName()+"\" ignore=\"true\"/></i>");
					tagClose("div"/*yui-u*/);
	//				
			    	tagClose("div"/*yui-gd*/);
				}
		    	tagClose("div"/*Lang*/);
			}
			appendString("</logic:equal>");
		}
		
		tagClose("div"/*yui-content*/);	
		
		
		tagClose("div"/*yui-navset*/);

		//** And now generate Dialog Functions (create/close/update etc.) **//
		tagOpen("div", "Functions", "functions");		
		for (MetaViewElement element: elements){
			//HACK: exclude not MetaList cause usually it contains MetaFunction Elements
			if(!(element instanceof MetaListElement))
				continue;
			appendString(getElementEditor(section.getDocument(), element, "ALL"));
			appendString("&nbsp;<i><bean:write name=\"description."+element.getName()+"\" ignore=\"true\"/></i>");
		}
		tagClose("div"/*Functions*/);
		
		//appendTagClose("div");
		
		
		appendString("<br/>");
		
		//Link to the Links to Me page
		appendString("<logic:present name="+quote("linksToMe")+" scope="+quote("request")+">");
		String linksToMePagePath = StrutsConfigGenerator.getPath(section.getDocument(), StrutsConfigGenerator.ACTION_LINKS_TO_ME)+"?pId=<bean:write name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" property=\"id\"/>";
		appendString("<a href="+quote("<ano:tslink>"+linksToMePagePath+"</ano:tslink>")+">Show direct links to  this document</a>");
		appendString("</logic:present>");
		
		//HOTFIX: commentation of direct link to me section START
		appendString("<%--");
		appendString("<logic:present name="+quote("linksToMe")+" scope="+quote("request")+">");
		appendString("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
		increaseIdent();
		appendString("<tr>");
		increaseIdent();
		appendString("<td>Direct links to  this document</td>");
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
		//HOTFIX: commentation of direct link to me section END
		appendString("--%>");
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
		
		//object info
		appendString("<p align="+quote("right")+"><small>ObjectInfo: <bean:write name="+quote("objectInfoString")+"/></small></p>");


		decreaseIdent();
		appendString("</body>");
		decreaseIdent();
		
		//generateRichTextEditorJS(section.getDocument(), richTextElementsRegistry, multilangDialog);
		generateLinkElementEditorJS(section.getDocument(), linkElementsRegistry, multilangDialog);
		
		openJavaScript();
		appendString("tinyMCE.init({");
		increaseIdent();
		appendString("mode : \"specific_textareas\",");
		appendString("editor_selector : \"richTextEditor\",");
		appendString("theme : \"advanced\"");
		decreaseIdent();
		appendString("});");
		appendString("function handleSubmit(){};");
		closeJavaScript();
		
		
		if(multilangDialog){
			//** Generate LanguageTabs JS **//
			appendString("<logic:equal name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" property="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" value="+quote("false")+">");
			openJavaScript();
			appendString("(function() {");
			appendIncreasedString("var tabView = new YAHOO.widget.TabView("+quote(dialog.getName())+");");
			appendString("})();");
			//tinymce.dom.Event.add(document, 'init', ...) to be sure that multilanguage editors sync happens after editors initializations.
			appendString("tinymce.dom.Event.add(document, 'init', function() {");
			increaseIdent();
			appendString("var FormUtils = YAHOO.anoweb.util.FormUtils;");
			//LanguageTabs input fields synchronization JS
			for (MetaViewElement element: elements){
				if (element instanceof MetaListElement || element instanceof MetaEmptyElement)
					continue;		
				
				boolean multilangEl = element instanceof MultilingualFieldElement;
				String elLang = getElementLanguage(element);
				elName = element.getName() + (multilangEl? elLang : "");
				
				if(multilangEl){
					appendString("FormUtils.reflectInputs(" + quote(elName + "_ALL") + ","+quote(elName + "_" + elLang)+", true);");
					
				}
				else{	
					Set<String> elementsToSync = new HashSet<String>();
					for (String lang : GeneratorDataRegistry.getInstance().getContext().getLanguages())
						elementsToSync.add(quote(elName + "_" + lang));
					appendString("FormUtils.reflectInputs(" + quote(elName + "_ALL") + ","+elementsToSync+", true);");
				}
			}
			decreaseIdent();
			appendString("});");
			closeJavaScript();
			appendString("</logic:equal>");
		}
		
		decreaseIdent();
		appendString("</html:html>");
		
		append(getBaseJSPFooter());
		
		return jsp;
	}
	
	/**
	 * Creating entries in JSP for Multilanguage Support!!!
	 * @param section
	 * @param colspan
	 */
	private void addMultilanguageOperations(MetaModuleSection section, int colspan) {
		appendString("<logic:equal name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, section.getDocument()))+" property="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" value="+quote("false")+">");
		appendString("<logic:notEqual name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, section.getDocument()))+" property="+quote("id")+" value="+quote("")+">");
		appendString("<tr>");
		increaseIdent();
		appendString("<td align=\"right\" colspan=\""+colspan+"\"><form name=\"CopyLang\" id=\"CopyLang\" method=\"get\" action=\""+StrutsConfigGenerator.getPath(section.getDocument(), StrutsConfigGenerator.ACTION_COPY_LANG)+"\">");
		appendString("<input type=\"hidden\" name=\"ts\" value=\"<%=System.currentTimeMillis()%>\"/><input type=\"hidden\" name=\"pId\" value=\"<bean:write name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, section.getDocument()))+" property="+quote("id")+"/>\"/>From&nbsp;");
		appendString("<select name=\"pSrcLang\">");
		for (String sl : GeneratorDataRegistry.getInstance().getContext().getLanguages()){
			appendString("<option value=\""+sl+"\">"+sl+"</option>");
		}
		appendString("</select>");


		appendString("To&nbsp;");
		appendString("<select name=\"pDestLang\">");
		for (String sl : GeneratorDataRegistry.getInstance().getContext().getLanguages()){
			appendString("<option value=\""+sl+"\">"+sl+"</option>");
		}
		appendString("</select>");

		appendString("&nbsp;");
		appendString("<a href=\"#\" onclick=\"document.CopyLang.submit(); return false\">Copy</a>&nbsp;");

		appendString("</form></td>");
		decreaseIdent();
		appendString("</tr>");
		appendString("<tr>");
		increaseIdent();
		appendString("<td align=\"right\" colspan=\""+colspan+"\"><form name="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" id="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+"  method=\"get\" action=\""+StrutsConfigGenerator.getPath(section.getDocument(), StrutsConfigGenerator.ACTION_SWITCH_MULTILANGUAGE_INSTANCE)+"\">");
		appendString("<input type=\"hidden\" name=\"value\" value=\"true\"/><input type=\"hidden\" name=\"ts\" value=\"<%=System.currentTimeMillis()%>\"/><input type=\"hidden\" name=\"pId\" value=\"<bean:write name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, section.getDocument()))+" property="+quote("id")+"/>\"/>");
		appendString("&nbsp;Languages enabled.&nbsp;");
		appendString("<a href=\"#\" onclick=\"document."+ModuleBeanGenerator.FIELD_ML_DISABLED+".submit(); return false\">Disable</a>&nbsp;");
		appendString("</form></td>");
		appendString("</tr>");
		appendString("</logic:notEqual>");
		appendString("</logic:equal>");
		appendString("<logic:equal name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, section.getDocument()))+" property="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" value="+quote("true")+">");
		appendString("<tr>");
		appendString("<td align=\"right\" colspan=\""+colspan+"\"><form name="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" id="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" method=\"get\" action=\""+StrutsConfigGenerator.getPath(section.getDocument(), StrutsConfigGenerator.ACTION_SWITCH_MULTILANGUAGE_INSTANCE)+"\">");
		appendString("<input type=\"hidden\" name=\"value\" value=\"false\"/><input type=\"hidden\" name=\"ts\" value=\"<%=System.currentTimeMillis()%>\"/><input type=\"hidden\" name=\"pId\" value=\"<bean:write name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, section.getDocument()))+" property="+quote("id")+"/>\"/>");
		appendString("&nbsp;Languages disabled.&nbsp;");
		appendString("<a href=\"#\" onclick=\"document."+ModuleBeanGenerator.FIELD_ML_DISABLED+".submit(); return false\">Enable</a>&nbsp;");
		appendString("</form></td>");
		appendString("</tr>");
		appendString("</logic:equal>");
	}

	private GeneratedJSPFile generateLinksToDocument(MetaModuleSection section, MetaView view){

		GeneratedJSPFile jsp = new GeneratedJSPFile();
		startNewJob(jsp);
		jsp.setName(getLinksToMePageName(section.getDocument()));
		jsp.setPackage(getContext().getJspPackageName(section.getModule()));
		
		resetIdent();
		
		append(getBaseJSPHeader());
		
		appendString("<html:html>");
		increaseIdent();
		appendString("<head>");
		increaseIdent();
		appendString("<title>Direct links to the "+section.getDocument().getName()+"[<bean:write name=\"objectId\"/>]</title>");
		generatePragmas(view);
		appendString("<link href=\""+getCurrentCSSPath("admin.css")+"\" rel=\"stylesheet\" type=\"text/css\">");
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
	private void generateLinkElementEditorJS(MetaDocument doc, List<MetaViewElement> linkElements, boolean multilangDialog){
		appendString("<script type=\"text/javascript\">");
		increaseIdent();
		for(MetaViewElement el: linkElements){
			MetaFieldElement field = (MetaFieldElement)el;
			MetaProperty p = doc.getField(el.getName());
			//FIXME: here is assumed that links can't be multilanguage
			String elName = getElementName(doc, el);
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
			appendString("this."+getHtmlEditorID(field, p, "ALL")+" = new YAHOO.anoweb.widget.ComboBox("+quote(getHtmlEditorID(field, p, "ALL"))+",\""+elCapitalName+"SelectorALL\","+elName+"Json,selection"+elCapitalName+"Json);");
			
			if(multilangDialog){
				appendString("<logic:equal name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" property="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" value="+quote("false")+">");
				for (String lang : GeneratorDataRegistry.getInstance().getContext().getLanguages()){
					appendString("this."+getHtmlEditorID(field, p, lang)+" = new YAHOO.anoweb.widget.ComboBox("+quote(getHtmlEditorID(field, p, lang))+",\""+elCapitalName+"Selector"+lang+"\","+elName+"Json,selection"+elCapitalName+"Json);");
				}
				appendString("</logic:equal>");
			}

		}
		decreaseIdent();
		appendString("</script>");
	}
	
	private void generateRichTextEditorJS(MetaDocument doc, List<MetaViewElement> richTextElements, boolean multilangDialog){
		
		if(richTextElements.size() == 0){
			appendString("<script type=\"text/javascript\">");
			appendString("function handleSubmit(){");
			appendString("	}");
			appendString("</script>");
			return;
		}
		
		appendString("<script type=\"text/javascript\">");
		increaseIdent();
		for(MetaViewElement el: richTextElements){
			appendStatement("var " + getEditorVarName(doc, el));
		}
		appendString("function handleSubmit(){");
		increaseIdent();
		for(MetaViewElement el: richTextElements){
			MetaFieldElement field = (MetaFieldElement)el;
			MetaProperty p = doc.getField(el.getName());
			appendIncreasedString(getHtmlEditorID(field, p, "ALL") + ".saveHtmlIfShowed();");
			if(multilangDialog){
				appendString("<logic:equal name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" property="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" value="+quote("false")+">");
				for (String lang : GeneratorDataRegistry.getInstance().getContext().getLanguages())
					appendIncreasedString(getHtmlEditorID(field, p, lang) + ".saveHtmlIfShowed();");
				appendString("</logic:equal>");
			}
		}
		decreaseIdent();
		appendString("}");

		appendString("(function() {");
		increaseIdent();
		appendString("var Dom = YAHOO.util.Dom,");
		appendString("Event = YAHOO.util.Event,");
		appendString("status = null;"); 
		appendString("//The Editor config");
		appendString("var myConfig = {");
		appendString("width: '660px',"); 
		appendString("height: '200px',"); 
		appendString("dompath: false, ");
		appendString("animate: true, ");
		appendString("handleSubmit: true ");
		appendString("}; ");
		appendString("//Now let's load the Editors..."); 
		for(MetaViewElement el: richTextElements){
			MetaFieldElement field = (MetaFieldElement)el;
			MetaProperty p = doc.getField(el.getName());
			generateRichTextEditorFieldJS(doc, field, "ALL");
			if(multilangDialog){
				appendString("<logic:equal name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" property="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" value="+quote("false")+">");
				for (String lang : GeneratorDataRegistry.getInstance().getContext().getLanguages())
					generateRichTextEditorFieldJS(doc, field, lang);
				appendString("</logic:equal>");
			}
		}

		decreaseIdent();
		appendString("})();"); 
		decreaseIdent();
		appendString("</script>");
		
		
	}
	
	private void generateRichTextEditorFieldJS(MetaDocument doc, MetaFieldElement editorFeild, String editLanguage){
		MetaProperty p = doc.getField(editorFeild.getName());
		String editor = getHtmlEditorID(editorFeild, p, editLanguage);
		String button = editor + "ToggleButton";
		appendString(editor + " = new YAHOO.widget.Editor("+quote(editor)+", myConfig);"); 
		appendString(editor + ".render(); ");
		appendString("YAHOO.util.Event.addListener("+quote(button)+",\"click\","+editor+".toggle,"+editor+",true)");
//		appendString("var " + button +" = new YAHOO.widget.Button({id: "+quote(button)+", onclick:{fn:"+editor+".toggle}});");
//		appendString(button + ".addClass('toggleEditor');");
//		appendString(button + ".on('click', "+editor+".toggle);");
//		appendString(button + ".subscribe(\"click\", function(){");
//		appendString(editor+".toggle();");
//		appendString("alert('Toggle');");
//		appendString("},this,this);");
	}
	
	//*** CMS2.0 FINISH ***
	
	private String getElementEditor(MetaDocument doc, MetaViewElement element, String editlanguage){
		if (element instanceof MetaEmptyElement)
			return "&nbsp;";
		if (element instanceof MetaFieldElement)
			return getFieldEditor((MetaFieldElement)element, editlanguage);
		if (element instanceof MetaListElement)
			return getListEditor(doc, (MetaListElement)element, editlanguage);
		if (element instanceof MetaFunctionElement)
			return getFunctionEditor(doc, (MetaFunctionElement)element);

		return "";
			
	}
	
	private String getListEditor(MetaDocument doc, MetaListElement element, String editlanguage){
		String ret = "";
		
		List<MetaViewElement> elements = element.getElements();
		for (int i=0; i<elements.size(); i++){
			ret += getElementEditor(doc, elements.get(i), editlanguage);
			if (i<elements.size()-1)
				ret += "&nbsp;";
		}
			
		
		return ret;
	}
	
	private String getLinkEditor(MetaFieldElement element, MetaProperty p, String editLanguage){
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
		ret += "<div id="+getHtmlEditorID(element, p, editLanguage)+" name="+quote(p.getName())+" class=\"selectBox\"></div><div id=\""+StringUtils.capitalize(p.getName(lang))+"Selector"+editLanguage+"\"></div>";
		ret += "(<i>old:</i>&nbsp;<bean:write property="+quote(p.getName()+"CurrentValue")+"name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" filter="+quote("false")+"/>)";			
		//*** CMS2.0 FINISH ***
		
		return ret;
	}
	
	private String getEnumerationEditor(MetaFieldElement element, MetaProperty p, String editLanguage){
		//for now we have only one link...
		String lang = getElementLanguage(element); 
		String beanName = StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument());
		String quotedBeanName = quote(beanName);
		String ret = "";
		String elId = quote(getHtmlEditorID(element, p, editLanguage));
		ret += "<select id="+elId+" name="+quote(p.getName(lang))+">";
		ret += "<logic:iterate id='el' name="+quotedBeanName+" property="+quote(p.getName()+"Collection"+(lang==null ? "":lang))+" type=\"net.anotheria.webutils.bean.LabelValueBean\">";
		ret += "<option value="+quote("${el.value}")+" ${"+beanName + "." + element.getName() + "==el.value?\"selected='selected'\":\"\"}>";
		ret += "${el.label}";
		ret += "</option>";
		ret += "</logic:iterate>";
		ret += "</select>";
		ret += "&nbsp;";
		ret += "(<i>old:</i>&nbsp;<bean:write property="+quote(p.getName()+"CurrentValue"+(lang==null ? "":lang))+" name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" filter="+quote("false")+"/>)";
		
//		ret += "<html:select size=\"1\" property="+quote(p.getName(lang))+">";
//		ret += "<html:optionsCollection property="+quote(p.getName()+"Collection"+(lang==null ? "":lang))+" filter=\"false\"/>";
//		ret += "</html:select>";
//		ret += "&nbsp;";
//		ret += "(<i>old:</i>&nbsp;<bean:write property="+quote(p.getName()+"CurrentValue"+(lang==null ? "":lang))+" name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" filter="+quote("false")+"/>)";

		return ret;
	}
	
	private String getFieldEditor(MetaFieldElement element, String editlanguage){
		MetaDocument doc = ((MetaModuleSection)currentSection).getDocument();
		MetaProperty p = doc.getField(element.getName());
		
		if (p.isLinked())
			return getLinkEditor(element, p, editlanguage);
			
		if (p instanceof MetaEnumerationProperty){
			return getEnumerationEditor(element, p, editlanguage);
		}
		
		if (p instanceof MetaContainerProperty)
			return getContainerLinkEditor(element, (MetaContainerProperty)p);
		
		if (p.getType().equals("image")){
			return getImageEditor(element, p);
		}
		
		if (p.getType().equals("string")){
			return getStringEditor(element, p, editlanguage);
		}
		
		if (p.getType().equals("int")){
			return getStringEditor(element, p, editlanguage);
		}

		if (p.getType().equals("double")){
			return getStringEditor(element, p, editlanguage);
		}

		if (p.getType().equals("float")){
			return getStringEditor(element, p, editlanguage);
		}

		if (p.getType().equals("long")){
			return getStringEditor(element, p, editlanguage);
		}

		if (p.getType().equals("text")){
			return getTextEditor(element, p, editlanguage);
		}
		
		if (p.getType().equals("boolean")){
			return getBooleanEditor(element, p, editlanguage);
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
		actionName += "?pId=<bean:write name="+name+" property="+quote("id")+"/>";
		ret += "<a href="+quote(actionName)+" target="+quote("_blank")+">&nbsp;&raquo&nbsp;Edit&nbsp;</a>";
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

	private String getStringEditor(MetaFieldElement element, MetaProperty p, String editLanguage){
		return getHtmlInputEditor(element, p, editLanguage, "text");
	}
	
	private String getHtmlInputEditor(MetaFieldElement element, MetaProperty p, String editLanguage, String inputType){
		String beanName = quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()));
		String elName = quote(p.getName(getElementLanguage(element)));
		String elId = quote(getHtmlEditorID(element, p, editLanguage));
		
		
		String ret ="";
		
		ret += "<input id=" + elId + " type="+quote(inputType)+" name=" + elName;
		if("checkbox".equals(inputType))
			ret += "<logic:equal name=" + beanName + " property="+elName +" value=\"true\"> checked=\"checked\"</logic:equal>";
		else
			ret += " value=\"<bean:write name=" + beanName + " property="+elName + "/>\"";
		if (element.isReadonly())
			ret += " readonly="+quote("true");
		ret += "/>";

		if (element.isReadonly())
			ret += "&nbsp;<i>readonly</i>";
		
		return ret;
	}

	private String getTextEditor(MetaFieldElement element, MetaProperty p, String editLanguage){
		String lang = getElementLanguage(element);
		String editorId = getHtmlEditorID(element, p, editLanguage);
		String quotedEditorId = quote(editorId);
		String ret ="";
		ret += "<div class=\"yui-skin-sam\">";
//		if(element.isRich())
//			ret += "<button id="+quote(editorId + "ToggleButton")+" type=\"button\">Toggle Editor</button><br/>";
		ret += "<textarea cols=\"80\" rows=\"15\" id="+quotedEditorId+" name="+quote(p.getName(lang)) + " class=" + quote(element.isRich()? "richTextEditor": "textEditor");
		ret += ">";
		ret += "<bean:write filter=\"false\" name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" property="+quote(p.getName(lang))+" />";
		ret += "</textarea>";
		ret += "</div>";
		return ret;
	}

	private String getHtmlEditorID(MetaFieldElement element, MetaProperty p, String editLanguage){
		String lang = getElementLanguage(element);
		return p.getName(lang) + "_" + editLanguage;
	}
	
	private String getBooleanEditor(MetaFieldElement element, MetaProperty p, String editlanguage){
		return getHtmlInputEditor(element, p, editlanguage, "checkbox");
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
		List<MetaViewElement> elements = section.getElements();

		String headerLine = "";
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = (MetaViewElement)elements.get(i);
			String tag = generateTag(element);
			if (tag==null)
				continue;
			headerLine += tag+";";
		}
		appendString(headerLine);

		appendString("<logic:iterate name="+quote(doc.getMultiple().toLowerCase())+" type="+quote(ModuleBeanGenerator.getListItemBeanImport(getContext(), doc))+" id="+quote(entryName)+"><%--");
		String bodyLine = "--%>";

		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = (MetaViewElement)elements.get(i);
			String tag = generateTag(element);
			if (tag==null)
				continue;
			bodyLine += "<bean:write filter=\"false\" name="+quote(entryName)+" property=\""+element.getName()+"\"/>;";
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
		appendString("<"+doc.getMultiple()+">");
		appendString("<logic:iterate name="+quote(doc.getMultiple().toLowerCase())+" type="+quote(ModuleBeanGenerator.getListItemBeanImport(getContext(), doc))+" id="+quote(entryName)+">");
		increaseIdent();
		appendString("<"+doc.getName()+">");
		increaseIdent();
		List<MetaViewElement> elements = section.getElements();
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = (MetaViewElement)elements.get(i);
			String tag = generateTag(element);
			if (tag==null)
				continue;
			String line = "<"+tag+">";
			if (((MetaModuleSection)currentSection).getDocument().getField(element.getName()).getType().equals("image"))
				line += "image";
			else
				line += "<bean:write filter=\"false\" name="+quote(entryName)+" property=\""+element.getName()+"\"/>";
			line += "</"+tag+">";
			appendString(line);
		}
		decreaseIdent();
		appendString("</"+doc.getName()+">");
		decreaseIdent();
		appendString("</logic:iterate>");
		appendString("</"+doc.getMultiple()+">");
		return jsp;
	}

	private GeneratedJSPFile generateShowPage(MetaModuleSection section, MetaView view){
		
		GeneratedJSPFile jsp = new GeneratedJSPFile();
		startNewJob(jsp);
		jsp.setName(getShowPageName(section.getDocument()));
		jsp.setPackage(GeneratorDataRegistry.getInstance().getContext().getJspPackageName(section.getModule()));
		
		ident = 0;
		append(getBaseJSPHeader());
		
		currentSection = section;
		MetaDocument doc = section.getDocument();

		appendString("<html>");
		increaseIdent();
		appendString("<head>");
		increaseIdent();
		appendString("<title>"+view.getTitle()+"</title>");
		//appendString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
		generatePragmas(view);
		appendString("<link href=\""+getCurrentCSSPath("admin.css")+"\" rel=\"stylesheet\" type=\"text/css\">");
		appendString("<link href=\""+getCurrentCSSPath("menubar.css")+"\" rel=\"stylesheet\" type=\"text/css\">");
		appendString("<script type=\"text/javascript\" src=\""+getCurrentJSPath("menubar.js")+"\"></script>");
		decreaseIdent();
		appendString("</head>");
		appendString("<body>");
		increaseIdent();
		appendString("<jsp:include page=\""+getTopMenuPage()+"\" flush=\"true\"/>");
		appendString("<jsp:include page=\""+getMenuName(view)+".jsp\" flush=\"true\"/>");

		List<MetaViewElement> elements = createMultilingualList(section.getElements(), doc);
		int colspan = elements.size();
		
		appendString("<table width=\"100%\" border=\"0\" cellspacing=\"1\" cellpadding=\"1\">");
		increaseIdent();
		appendString("<tr>");
		increaseIdent();
		appendString("<td colspan=\""+colspan+"\"><img src="+quote(getCurrentImagePath("s.gif"))+" width=\"1\" height=\"1\"></td>");
		decreaseIdent(); 
		appendString("</tr>");
		
		appendString("<tr>");
		increaseIdent();
		appendString("<td colspan=\""+(2)+"\">"+
		"<a href="+quote(StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_EXPORT+StrutsConfigGenerator.SUFFIX_XML))+">XML</a>&nbsp;"+
		"<a href="+quote(StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_EXPORT+StrutsConfigGenerator.SUFFIX_CSV))+">CSV</a></td>");
		String searchForm = "<form name="+quote("Search")+" action="+quote(StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_SEARCH))+" style=\"margin:0px;padding:0px;border:0px\" target=\"_blank\">";
		String searchFormContent = "<input type="+quote("text")+" name="+quote("criteria")+" size="+quote(10)+"/>";
		searchFormContent += "&nbsp;&nbsp;";
		searchFormContent += "<a href="+quote("#")+" onClick="+quote("document.Search.submit();return false")+">Search</a>";
		
		appendString(searchForm+ "<td colspan=\""+(colspan-3)+"\" align=\"right\">&nbsp;"+searchFormContent+"&nbsp;&nbsp;</td></form>");
		
		appendString("<td align=\"right\">"+generateNewFunction("", new MetaFunctionElement("add"))+"</td>");
		decreaseIdent(); 
		appendString("</tr>");
		
		appendString("<% String selectedPaging = \"\"+request.getAttribute("+quote("currentItemsOnPage")+"); %>");
		appendString("<logic:present name=\"paginglinks\" scope=\"request\">");
		increaseIdent();
		appendString("<tr>");
		increaseIdent();
		appendString("<td align=\"right\" colspan=\""+(colspan)+"\"><div style=\"white-space:nowrap; height:2em; line-height:2em\">");
		increaseIdent();
		appendString("<logic:iterate name="+quote("paginglinks")+" scope="+quote("request")+" id="+quote("link")+" type="+quote("net.anotheria.asg.util.bean.PagingLink")+">");
		increaseIdent();
		appendString("&nbsp;");
		appendString("<logic:equal name="+quote("link")+" property="+quote("linked")+" value="+quote("true")+">");
		appendIncreasedString("<a href=\"?pageNumber=<bean:write name="+quote("link")+" property="+quote("link")+"/>\"><bean:write name="+quote("link")+" property="+quote("caption")+"/></a>");
		appendString("</logic:equal>");
		appendString("<logic:notEqual name="+quote("link")+" property="+quote("linked")+" value="+quote("true")+">");
		appendIncreasedString("<bean:write name="+quote("link")+" property="+quote("caption")+"/>"); 
		appendString("</logic:notEqual>");
		decreaseIdent();
		appendString("</logic:iterate>");
		appendString("<form style=\"display:inline\" name="+quote("ItemsOnPageForm")+" action=\"\" method=\"GET\""+">&nbsp;&nbsp;Items&nbsp;on&nbsp;page:&nbsp;");
		appendString("<select name="+quote("itemsOnPage")+" onchange="+quote("document.ItemsOnPageForm.submit();")+">");
		appendString("<logic:iterate name="+quote("PagingSelector")+" type="+quote("java.lang.String")+" id="+quote("option")+">");
		appendString("<option value="+quote("<bean:write name="+quote("option")+"/>")+" <logic:equal name=\"option\" value=\"<%=selectedPaging%>\">selected</logic:equal>><bean:write name="+quote("option")+"/></option>"); 
		appendString("</logic:iterate>");
		appendString("</select></form>");
		decreaseIdent();
		appendString("</div></td>");
		decreaseIdent();
		appendString("</tr>");
		decreaseIdent();
		appendString("</logic:present>");

		// FILTER START ----------------------------------------
		//filter management line
		for (int i=0; i<section.getFilters().size(); i++){
			MetaFilter f  = section.getFilters().get(i);
			appendString("<!-- Generating Filter: "+ModuleActionsGenerator.getFilterVariableName(f)+" -->");
			appendString("<% String filterParameter"+i+" = (String) request.getAttribute(\"currentFilterParameter"+i+"\");");
			appendString("if (filterParameter"+i+"==null)");
			appendIncreasedString("filterParameter"+i+" = \"\";%>");
			appendString("<tr class=\"lineCaptions\"><td colspan="+quote(colspan)+">Filter <strong>"+StringUtils.capitalize(f.getFieldName())+":</strong>&nbsp;");
			increaseIdent();
			appendString("<logic:iterate name="+quote(ModuleActionsGenerator.getFilterVariableName(f))+" id="+quote("triggerer")+" type="+quote("net.anotheria.asg.util.filter.FilterTrigger")+">");
			increaseIdent();
			appendString("<logic:equal name="+quote("triggerer")+" property="+quote("parameter")+" value="+quote("<%=filterParameter"+i+"%>")+">");
			appendIncreasedString("<strong><bean:write name="+quote("triggerer")+" property="+quote("caption")+"/></strong>");
			appendString("</logic:equal>");
			appendString("<logic:notEqual name="+quote("triggerer")+" property="+quote("parameter")+" value="+quote("<%=filterParameter"+i+"%>")+">");
			appendIncreasedString("<a href="+quote(StrutsConfigGenerator.getPath(section.getDocument(), StrutsConfigGenerator.ACTION_SHOW)+"?pFilter"+i+"=<bean:write name="+quote("triggerer")+" property="+quote("parameter")+"/>")+"><bean:write name="+quote("triggerer")+" property="+quote("caption")+"/></a>");
			appendString("</logic:notEqual>");
			decreaseIdent();
			appendString("</logic:iterate>");
			decreaseIdent();
			appendString("</td></tr>");
		}
		
		// ------------------------------------------ FILTER END 

		//write header
		appendString("<tr class=\"lineCaptions\">");
		increaseIdent(); 
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			appendString(generateElementHeader(element));
		}
		decreaseIdent();
		appendString("</tr>");
		
		String entryName = doc.getName().toLowerCase();
		appendString("<logic:iterate name="+quote(doc.getMultiple().toLowerCase())+" type="+quote(ModuleBeanGenerator.getListItemBeanImport(getContext(), doc))+" id="+quote(entryName)+" indexId=\"ind\">");
		increaseIdent();
		appendString("<tr class=\"<%=ind.intValue()%2==0 ? \"lineLight\" : \"lineDark\"%> highlightable\">");

        for (int i = 0; i < elements.size(); i++) {
            MetaViewElement element = elements.get(i);
            appendString(generateElement(entryName, element,doc));
        }

		appendString("</tr>");
		decreaseIdent();
		appendString("</logic:iterate>");

		appendString("<tr>");
		increaseIdent();
		appendString("<td colspan=\""+(colspan)+"\"><img src="+quote(getCurrentImagePath("s.gif"))+" width=\"1\" height=\"1\"></td>");
		decreaseIdent(); 
		appendString("</tr>");
		appendString("<tr class=\"lineCaptions\">");
		increaseIdent();
		appendString("<td colspan="+quote(colspan)+" align="+quote("right")+
			"><a href="+quote(StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_EXPORT+StrutsConfigGenerator.SUFFIX_XML))+">XML</a>&nbsp;"+
			"<a href="+quote(StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_EXPORT+StrutsConfigGenerator.SUFFIX_CSV))+">CSV</a></td>");
		decreaseIdent();
		appendString("</tr>");
		

		decreaseIdent();
		appendString("</table>");
		decreaseIdent();
		appendString("<jsp:include page=\""+getFooterName(view)+".jsp\" flush=\"true\"/>");
		appendString("</body>");
		decreaseIdent();
		appendString("</html>");
		append(getBaseJSPFooter()); 
		return jsp;
	}
	
	private GeneratedJSPFile generateSearchPage(){
		
		
		GeneratedJSPFile jsp = new GeneratedJSPFile();
		startNewJob(jsp);
		jsp.setName(getSearchResultPageName());
		jsp.setPackage(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED)+".jsp");
		
		ident = 0;
		
		append(getBaseJSPHeader());
		
		appendString("<html>");
		increaseIdent();
		appendString("<head>");
		increaseIdent();
		appendString("<title>Search result</title>");
		//appendString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
		generatePragmas();
		appendString("<link href=\""+getCurrentCSSPath("admin.css")+"\" rel=\"stylesheet\" type=\"text/css\">");
		decreaseIdent();
		appendString("</head>");
		appendString("<body>");
		increaseIdent();
		//appendString("<jsp:include page=\""+getMenuName(view)+".jsp\" flush=\"true\"/>");

		int colspan = 3;
		
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
		appendString("<td width=\"5%\">Id</td>");
		appendString("<td width=\"15%\">Property name</td>");
		appendString("<td width=\"80%\">Match</td>");
		decreaseIdent();
		appendString("</tr>");
		appendString("<logic:present name="+quote("result")+" >");
		appendString("<logic:iterate name="+quote("result")+" type="+quote("net.anotheria.anodoc.query2.ResultEntryBean")+" id="+quote("entry")+" indexId=\"ind\">");
		increaseIdent();
		appendString("<tr class=\"<%=ind.intValue()%2==0 ? \"lineLight\" : \"lineDark\"%>\">");
		appendString("<td width=\"5%\"><a href="+quote("<bean:write name="+quote("entry")+" property="+quote("editLink")+"/>")+" target="+quote("_blank")+"><bean:write name="+quote("entry")+" property="+quote("documentId")+"/></td>");
		appendString("<td width=\"15%\"><bean:write name="+quote("entry")+" property="+quote("propertyName")+"/></td>");
		appendString("<td width=\"80%\"><bean:write name="+quote("entry")+" property="+quote("info")+" filter="+quote("false")+"/></td>");
		appendString("</tr>");
		decreaseIdent();
		appendString("</logic:iterate>");
		appendString("</logic:present>");

		decreaseIdent();
		appendString("</table>");
		decreaseIdent();
		//appendString("<jsp:include page=\""+getFooterName(view)+".jsp\" flush=\"true\"/>");
		appendString("</body>");
		decreaseIdent();
		appendString("</html>");
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
		

		appendString("<html>");
		increaseIdent();
		appendString("<head>");
		increaseIdent();
		appendString("<title>VersionInfo for <bean:write name=\"documentName\"/></title>");
		//appendString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
		generatePragmas();
		appendString("<link href=\""+getCurrentCSSPath("admin.css")+"\" rel=\"stylesheet\" type=\"text/css\">");
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
		append(getBaseJSPFooter()); 
		return jsp;
	}
	
	private String generateElementHeader(MetaViewElement element){
		if (element instanceof MetaFieldElement)
			return generateFieldHeader((MetaFieldElement)element);
		if (element instanceof MetaFunctionElement)
			return generateFunctionHeader((MetaFunctionElement)element);
		if (element instanceof MetaCustomFunctionElement)
			return generateFunctionHeader(null);
		return "";
	}
	
	private String generateFieldHeader(MetaFieldElement element){
		String name = element instanceof MultilingualFieldElement ? element.getVariableName() : element.getName();
		String header =  StringUtils.capitalize(name);
		if (element.isComparable()){
			
			String action = StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_SHOW);
			action = action+"?"+ViewConstants.PARAM_SORT_TYPE_NAME+"="+name;
			String actionAZ = action + "&" + ViewConstants.PARAM_SORT_ORDER + "="+ViewConstants.VALUE_SORT_ORDER_ASC; 
			String actionZA = action + "&" + ViewConstants.PARAM_SORT_ORDER + "="+ViewConstants.VALUE_SORT_ORDER_DESC; 
			header += "&nbsp;<logic:notEqual name="+quote("currentSortCode")+" value="+quote(name+"_"+ViewConstants.VALUE_SORT_ORDER_ASC)+"><a href="+quote(generateTimestampedLinkPath(actionAZ))+">A</a></logic:notEqual><logic:equal name="+quote("currentSortCode")+" value="+quote(name+"_"+ViewConstants.VALUE_SORT_ORDER_ASC)+"><strong>A</strong></logic:equal>";
			header += "&nbsp;<logic:notEqual name="+quote("currentSortCode")+" value="+quote(name+"_"+ViewConstants.VALUE_SORT_ORDER_DESC)+"><a href="+quote(generateTimestampedLinkPath(actionZA))+">Z</a></logic:notEqual><logic:equal name="+quote("currentSortCode")+" value="+quote(name+"_"+ViewConstants.VALUE_SORT_ORDER_DESC)+"><strong>Z</strong></logic:equal>";
		}
		return "<td>"+header+"</td>";
	}
	
	private String generateFunctionHeader(MetaFunctionElement element){
		//"+StringUtils.capitalize(element.getCaption())+"
		return "<td width=\"1%\">&nbsp;</td>";
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
		return "<td><bean:write filter=\"false\" name="+quote(entryName)+" property=\""+elementName+"\"/></td>";
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
		return "<td><a href="+quote(generateTimestampedLinkPath(link))+">"+caption+"</a></td>";
	}

	private String getFunctionEditor(MetaDocument doc, MetaFunctionElement element){
		if (element.getName().equals("cancel")) {
			return "<a href=" + quote(generateTimestampedLinkPath(StrutsConfigGenerator.getPath(doc, StrutsConfigGenerator.ACTION_SHOW))) + ">&nbsp;&raquo&nbsp;Close&nbsp;</a>";
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
			return getLockFunctionLink(doc, element);
		}

		if (element.getName().equals("unlock") && StorageType.CMS.equals(doc.getParentModule().getStorageType())) {
			return getUnLockFunctionLink(doc, element);
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
		result += " \t<a href=" + quote("<ano:tslink>" + path + "</ano:tslink>") + "  onClick= "+quote("return confirm(' All unsaved data will be lost!!! Really unLock  "+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+" : <bean:write name="
				+ quote(StrutsConfigGenerator.getDialogFormName(currentDialog, doc)) + " property=\"id\"/>;');")+">&nbsp;&raquo&nbsp;UnLock&nbsp;</a>";
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
		result += " \t<a href=" + quote("<ano:tslink>" + path + "</ano:tslink>") + "  onClick= "+quote("return confirm('All unsaved data will be lost!!!. Really lock  "+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+" with id : <bean:write name="
				+ quote(StrutsConfigGenerator.getDialogFormName(currentDialog, doc)) + " property=\"id\"/>;');")+">&nbsp;&raquo&nbsp;Lock&nbsp;</a>";
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
		String alt = "Locked by: <bean:write name="+quote(entryName)+" property="+quote(LockableObject.INT_LOCKER_ID_PROPERTY_NAME)+"/>, time: <bean:write name="+quote(entryName)+" property="+quote(LockableObject.INT_LOCKING_TIME_PROPERTY_NAME)+"/>";
        String link = "<td><a href="+quote("<ano:tslink>"+path+"</ano:tslink>")
				+ "onClick= "+quote("return confirm('"+alt+" Really unlock - Locked  "+((MetaModuleSection)currentSection).getDocument().getName()+" with id : <bean:write name="+quote(entryName)+" property=\"id\"/>');")+">"+getUnLockImage(alt)+"</a></td>";
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
        String link =  "<td><a href="+quote("<ano:tslink>"+path+"</ano:tslink>")+ "onClick="+quote("return confirm('Really lock "+
				((MetaModuleSection)currentSection).getDocument().getName()+" with id: <bean:write name="+quote(entryName)+" property=\"id\"/>');")+">"+getLockImage()+"</a></td>" ;
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
			result+="\t<a href=\"#\" onClick=\"handleSubmit(); document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+
					".nextAction.value='stay'; document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+".submit(); return false\">&nbsp;&raquo&nbsp;<bean:write name=\"save.label.prefix\"/>AndStay&nbsp;</a> \n";
			result+="  </logic:equal> \n";
			result+="</logic:equal> \n";
			result+="<logic:equal name=" + quote(StrutsConfigGenerator.getDialogFormName(currentDialog, doc)) + " property=" + quote(LockableObject.INT_LOCK_PROPERTY_NAME) + " value=" + quote("false") + "> \n";
			result+="\t<a href=\"#\" onClick=\"handleSubmit(); document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+
					".nextAction.value='stay'; document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+".submit(); return false\">&nbsp;&raquo&nbsp;<bean:write name=\"save.label.prefix\"/>AndStay&nbsp;</a>\n";
			result+="</logic:equal> \n";
			return result;
		}
		return "<a href=\"#\" onClick=\"handleSubmit(); document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+
				".nextAction.value='stay'; document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+".submit(); return false\">&nbsp;&raquo&nbsp;<bean:write name=\"save.label.prefix\"/>AndStay&nbsp;</a>";
	}
	private String getUpdateAndCloseFunction(MetaDocument doc, MetaFunctionElement element){
		if(StorageType.CMS.equals(doc.getParentModule().getStorageType())){
			//creating logic for hiding or showing current operation link in Locking CASE!!!!!
			String result = "<logic:equal name=" + quote(StrutsConfigGenerator.getDialogFormName(currentDialog, doc)) +
					" property=" + quote(LockableObject.INT_LOCK_PROPERTY_NAME) + " value=" + quote("true") + "> \n";
			result+="  <logic:equal name=" + quote(StrutsConfigGenerator.getDialogFormName(currentDialog, doc)) +
					" property=" + quote(LockableObject.INT_LOCKER_ID_PROPERTY_NAME) + " value=" + quote("<%=(java.lang.String)session.getAttribute(\\"+quote("currentUserId\\")+")%>") + "> \n";
			result+="\t<a href=\"#\" onClick=\"handleSubmit(); document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+
					".nextAction.value='close'; document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+".submit(); return false\">&nbsp;&raquo&nbsp;<bean:write name=\"save.label.prefix\"/>AndClose&nbsp;</a> \n";
			result+="  </logic:equal> \n";
			result+="</logic:equal> \n";
			result+="<logic:equal name=" + quote(StrutsConfigGenerator.getDialogFormName(currentDialog, doc)) + " property=" + quote(LockableObject.INT_LOCK_PROPERTY_NAME) + " value=" + quote("false") + "> \n";
			result+="\t<a href=\"#\" onClick=\"handleSubmit(); document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+
					".nextAction.value='close'; document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+".submit(); return false\">&nbsp;&raquo&nbsp;<bean:write name=\"save.label.prefix\"/>AndClose&nbsp;</a> \n";
			result+="</logic:equal> \n";
			return result;
		}
		return "<a href=\"#\" onClick=\"handleSubmit(); document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+
				".nextAction.value='close'; document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+".submit(); return false\">&nbsp;&raquo&nbsp;<bean:write name=\"save.label.prefix\"/>AndClose&nbsp;</a>";
	}
	
	
	private String getDuplicateFunction(String entryName, MetaFunctionElement element){
		String path = StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_DUPLICATE);
		path += "?pId=<bean:write name="+quote(entryName)+" property=\"plainId\"/>";
		
		return "<td><a href="+quote("<ano:tslink>"+path+"</ano:tslink>")+">"+getDuplicateImage()+"</a></td>" ;
	}

	private String getVersionFunction(String entryName, MetaFunctionElement element){
		String path = StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_VERSIONINFO);
		path += "?pId=<bean:write name="+quote(entryName)+" property=\"plainId\"/>";
		
		return "<td><a href="+quote("<ano:tslink>"+path+"</ano:tslink>")+" title="+quote("LastUpdate: <bean:write name="+quote(entryName)+" property="+quote("documentLastUpdateTimestamp")+"/>")+">"+getVersionImage()+"</a></td>" ;
	}

	private String getDeleteFunction(String entryName, MetaFunctionElement element){
		String path = StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_DELETE);
		path += "?pId=<bean:write name="+quote(entryName)+" property=\"plainId\"/>";
		
		return "<td><a href="+quote("<ano:tslink>"+path+"</ano:tslink>")+">"+getDeleteImage()+"</a></td>" ;
	}

	private String getDeleteWithConfirmationFunction(String entryName, MetaFunctionElement element){
		String path = StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_DELETE);
		path += "?pId=<bean:write name="+quote(entryName)+" property=\"plainId\"/>";
		return "<td><a href="+quote("<ano:tslink>"+path+"</ano:tslink>")+" onClick="+quote("return confirm('Really delete "+
				((MetaModuleSection)currentSection).getDocument().getName()+" with id: <bean:write name="+quote(entryName)+" property=\"id\"/>');")+">"+getDeleteImage()+"</a></td>" ;
	}

	private String getEditFunction(String entryName, MetaFunctionElement element){
		String path = StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_EDIT);
		path += "?pId=<bean:write name="+quote(entryName)+" property=\"plainId\"/>";
		
		return "<td><a href="+quote("<ano:tslink>"+path+"</ano:tslink>")+">"+getEditImage()+"</a></td>" ;
	}

	private String generateNewFunction(String entryName, MetaFunctionElement element){
		String path = StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_NEW);
		
		return "<a href="+quote(generateTimestampedLinkPath(path))+">"+getImage("add", "add new "+((MetaModuleSection)currentSection).getDocument().getName())+"</a>" ;
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
