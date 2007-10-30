package net.anotheria.asg.generator.view;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
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
import net.anotheria.asg.generator.view.meta.MetaCustomFunctionElement;
import net.anotheria.asg.generator.view.meta.MetaDialog;
import net.anotheria.asg.generator.view.meta.MetaEmptyElement;
import net.anotheria.asg.generator.view.meta.MetaFieldElement;
import net.anotheria.asg.generator.view.meta.MetaFilter;
import net.anotheria.asg.generator.view.meta.MetaFunctionElement;
import net.anotheria.asg.generator.view.meta.MetaListElement;
import net.anotheria.asg.generator.view.meta.MetaModuleSection;
import net.anotheria.asg.generator.view.meta.MetaSection;
import net.anotheria.asg.generator.view.meta.MetaViewElement;
import net.anotheria.asg.generator.view.meta.MetaView;
import net.anotheria.asg.generator.view.meta.MultilingualFieldElement;
import net.anotheria.util.StringUtils;

/**
 * Generates the jsps for the edit view.
 * @author another
 */
public class JspViewGenerator extends AbstractJSPGenerator implements IGenerator{
	
	private MetaSection currentSection;
	private MetaDialog currentDialog;

	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.generator.IGenerator#generate(net.anotheria.anodoc.generator.IGenerateable, net.anotheria.anodoc.generator.Context)
	 */
	public List<FileEntry> generate(IGenerateable g, Context aContext) {
		
		setContext(aContext);
		
		List<FileEntry> files = new ArrayList<FileEntry>();
		MetaView view = (MetaView)g;
		
		FileEntry menu = new FileEntry(FileEntry.package2path(getContext().getPackageName(MetaModule.SHARED)+".jsp"), getMenuName(view), generateMenu(view));
		menu.setType(".jsp");
		files.add(menu);
		
		FileEntry footer = new FileEntry(FileEntry.package2path(getContext().getPackageName(MetaModule.SHARED)+".jsp"), getFooterName(view), generateFooter(view, FOOTER_SELECTION_CMS));
		footer.setType(".jsp");
		files.add(footer);
		
		FileEntry searchResultPage = new FileEntry(FileEntry.package2path(getContext().getPackageName(MetaModule.SHARED)+".jsp"), getSearchResultPageName(), generateSearchPage());
		searchResultPage.setType(".jsp");
		files.add(searchResultPage);

		FileEntry versionInfoPage = new FileEntry(FileEntry.package2path(getContext().getPackageName(MetaModule.SHARED)+".jsp"), getVersionInfoPageName(), generateVersionInfoPage());
		versionInfoPage.setType(".jsp");
		files.add(versionInfoPage);
		
		
		
		for (int i=0; i<view.getSections().size(); i++){
			MetaSection s = view.getSections().get(i);
			if (!(s instanceof MetaModuleSection))
				continue;
			MetaModuleSection section = (MetaModuleSection)s;
			FileEntry sectionFile = new FileEntry(FileEntry.package2path(getContext().getJspPackageName(section.getModule())), getShowPageName(section.getDocument()), generateSection(section, view));
			sectionFile.setType(".jsp");
			files.add(sectionFile);
			
			FileEntry csvExportFile = new FileEntry(FileEntry.package2path(getContext().getJspPackageName(section.getModule())), getExportAsCSVPageName(section.getDocument()), generateCSVExport(section, view));
			csvExportFile.setType(".jsp");
			files.add(csvExportFile);

			FileEntry xmlExportFile = new FileEntry(FileEntry.package2path(getContext().getJspPackageName(section.getModule())), getExportAsXMLPageName(section.getDocument()), generateXMLExport(section, view));
			xmlExportFile.setType(".jsp");
			files.add(xmlExportFile);

			List<MetaDialog> dialogs = section.getDialogs();
			for (int d=0; d<dialogs.size(); d++){
				MetaDialog dialog = dialogs.get(d);
				
				FileEntry dialogFile = new FileEntry(FileEntry.package2path(getContext().getJspPackageName(section.getModule())), getDialogName(dialog, section.getDocument()), generateDialog(dialog, section, view));
				dialogFile.setType(".jsp");
				files.add(dialogFile);
			}
			
			MetaDocument doc = section.getDocument();
			for (int p=0; p<doc.getProperties().size(); p++){
				MetaProperty pp = doc.getProperties().get(p);
				if (pp instanceof MetaContainerProperty){
				    FileEntry entry = new FileEntry(FileEntry.package2path(getContext().getJspPackageName(section.getModule())), getContainerPageName(doc, (MetaContainerProperty)pp), generateContainerPage(doc, (MetaContainerProperty)pp)); 
					entry.setType(".jsp");
				    files.add(entry);
				}
			}
			
			
			
		}
		return files;
	}
	
	private String getMenuName(MetaView view){
		return "../../shared/jsp/"+StringUtils.capitalize(view.getName())+"Menu";		
	}
	
	private String getFooterName(MetaView view){
		return "../../shared/jsp/"+StringUtils.capitalize(view.getName())+"Footer";		
	}


	private String generateMenu(MetaView view){
		
		String ret = getBaseJSPHeader();
		
		ret += writeString("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
		increaseIdent();
		
		ret += openTR();
		ret += writeString("<logic:iterate name=\"menu\" type=\"net.anotheria.webutils.bean.MenuItemBean\" id=\"entry\">");
		ret += writeString("<td>");
		increaseIdent();
		ret += writeString("<logic:equal name=\"entry\" property=\"active\" value=\"true\">");
		ret += writeIncreasedString("<td class=\"menuTitleSelected\"><bean:write name=\"entry\" property=\"caption\"/></td>");
		ret += writeString("</logic:equal>");
		ret += writeString("<logic:notEqual name=\"entry\" property=\"active\" value=\"true\">");
		ret += writeIncreasedString("<td class=\"menuTitle\"><a href=\"<ano:tslink><bean:write name=\"entry\" property=\"link\"/></ano:tslink>\"><bean:write name=\"entry\" property=\"caption\"/></a></td>");
		ret += writeString("</logic:notEqual>");
		decreaseIdent();
		ret += writeString("</td>");
		decreaseIdent();
		ret += writeString("</logic:iterate>");
		ret += closeTR();

		decreaseIdent();
		ret += writeString("</table>");
		
		ret += getBaseJSPFooter();
			
		return ret;
	}
	

	private String generateContainerPage(MetaDocument doc, MetaContainerProperty p){
		if (p instanceof MetaListProperty)
			return generateListPage(doc, (MetaListProperty)p);
		if (p instanceof MetaTableProperty)
			return generateTablePage(doc, (MetaTableProperty)p);
		throw new RuntimeException("Unsupported container: "+p);
	}
	
	private String generateListPage(MetaDocument doc, MetaListProperty list){
		String ret = "";
		resetIdent();

		String formName = StrutsConfigGenerator.getContainerEntryFormName(doc, list);
		MetaProperty p = list.getContainedProperty();

		ret += getBaseJSPHeader();
		
		ret += writeString("<html:html>");
		increaseIdent();
		ret += writeString("<head>");
		increaseIdent();
		ret += writeString("<title>Edit "+doc.getName()+StringUtils.capitalize(list.getName())+"</title>");
		ret += generatePragmas();
		ret += writeString("<link href=\""+getCurrentCSSPath("admin.css")+"\" rel=\"stylesheet\" type=\"text/css\">");
		decreaseIdent();
		ret += writeString("</head>");
		ret += writeString("<body>");
		increaseIdent();		

		
		ret += writeString("<table width="+quote("100%")+" cellspacing="+quote("1")+" cellpadding="+quote("1")+" border="+quote("0")+">");
		increaseIdent();
		ret += writeString("<tr class="+quote("lineCaptions")+">");
		ret += writeIncreasedString("<td width=\"1%\">Pos</td>");
		ret += writeIncreasedString("<td>"+StringUtils.capitalize(list.getName())+"</td>");
		ret += writeIncreasedString("<td>"+"Description"+"</td>");
		ret += writeIncreasedString("<td width=\"1%\">&nbsp;</td>");
		ret += writeIncreasedString("<td width=\"1%\">&nbsp;</td>");
		ret += writeIncreasedString("<td width=\"1%\">&nbsp;</td>");
		ret += writeIncreasedString("<td width=\"1%\">&nbsp;</td>");
		ret += writeIncreasedString("<td width=\"1%\">&nbsp;</td>");
		ret += writeString("</tr>");
		ret += writeString("<logic:iterate name="+quote("elements")+" id="+quote("element")+" type="+quote(ModuleBeanGenerator.getContainerEntryFormImport(doc, list))+" indexId="+quote("ind")+">");
		increaseIdent();
		ret += writeString("<tr class=\"<%=ind.intValue()%2==0 ? \"lineLight\" : \"lineDark\"%>\">");
		increaseIdent();
		ret += writeString("<td width="+quote("1%")+"><bean:write name="+quote("element")+" property="+quote("position")+"/></td>");
		
		if (p.isLinked()){
			
			MetaLink link2p = (MetaLink)p;
			MetaDocument linkTarget = GeneratorDataRegistry.getInstance().resolveLink(link2p.getLinkTarget());
			String targetLinkAction = StrutsConfigGenerator.getPath(linkTarget, StrutsConfigGenerator.ACTION_EDIT);
			
			
			ret += writeString("<td><a href=<ano:tslink>"+quote(targetLinkAction+"?pId=<bean:write name="+quote("element")+" property="+quote(list.getContainedProperty().getName())+"/></ano:tslink>")+"><bean:write name="+quote("element")+" property="+quote(list.getContainedProperty().getName())+"/></a></td>");
			ret += writeString("<td><a href=<ano:tslink>"+quote(targetLinkAction+"?pId=<bean:write name="+quote("element")+" property="+quote(list.getContainedProperty().getName())+"/></ano:tslink>")+"><bean:write name="+quote("element")+" property="+quote("description")+"/></a></td>");
		}else{
			ret += writeString("<td><bean:write name="+quote("element")+" property="+quote(list.getContainedProperty().getName())+"/></td>");
			ret += writeString("<td><bean:write name="+quote("element")+" property="+quote("description")+"/></td>");
		}
		
		
		String parameter = "pId=<bean:write name="+quote("element")+" property="+quote("ownerId")+"/>";
		parameter += "&pPosition=<bean:write name="+quote("element")+" property="+quote("position")+"/>";
		ret += writeString("<td width="+quote("1%")+"><a href="+quote(StrutsConfigGenerator.getContainerPath(doc, list, StrutsConfigGenerator.ACTION_MOVE_TOP)+"?"+parameter)+">"+getTopImage("move to top")+"</a></td>");
		ret += writeString("<td width="+quote("1%")+"><a href="+quote(StrutsConfigGenerator.getContainerPath(doc, list, StrutsConfigGenerator.ACTION_MOVE_UP)+"?"+parameter)+">"+getUpImage("move up")+"</a></td>");
		ret += writeString("<td width="+quote("1%")+"><a href="+quote(StrutsConfigGenerator.getContainerPath(doc, list, StrutsConfigGenerator.ACTION_MOVE_DOWN)+"?"+parameter)+">"+getDownImage("move down")+"</a></td>");
		ret += writeString("<td width="+quote("1%")+"><a href="+quote(StrutsConfigGenerator.getContainerPath(doc, list, StrutsConfigGenerator.ACTION_MOVE_BOTTOM)+"?"+parameter)+">"+getBottomImage("move to bottom")+"</a></td>");
		ret += writeString("<td width="+quote("1%")+"><a href="+quote(StrutsConfigGenerator.getContainerPath(doc, list, StrutsConfigGenerator.ACTION_DELETE)+"?"+parameter)+">"+getDeleteImage("delete row")+"</a></td>");
		decreaseIdent();
		ret += writeString("</tr>");
		decreaseIdent();
		ret += writeString("</logic:iterate>");
		decreaseIdent();
		ret += writeString("</table>");
		
//*/
		
		ret += writeString("<br>");
		ret += writeString("<table width="+quote("100%")+" cellspacing="+quote("1")+" cellpadding="+quote("1")+" border="+quote("0")+">");
		increaseIdent();
		ret += writeString("<html:form action="+quote(StrutsConfigGenerator.getContainerPath(doc, list, StrutsConfigGenerator.ACTION_ADD))+">");
		ret += writeString("<html:hidden property="+quote("ownerId")+"/>");
		ret += writeString("<input type="+quote("hidden")+" name="+quote("pId")+" value=\"<bean:write name="+quote(formName)+" property="+quote("ownerId")+"/>\">");
		
		ret += writeString("<tr class="+quote("lineCaptions")+">");
		ret += writeIncreasedString("<td colspan="+quote("2")+">Add row:</td>");
		ret += writeString("</tr>");


		ret += writeString("<tr class="+quote("lineLight")+">");
		increaseIdent();
	        
		ret += writeString("<td align=\"right\" width=\"35%\">");
		increaseIdent();
		String name = p.getName();
		if (name==null || name.length()==0)
			name = "&nbsp;";
		ret += writeString(name+":");
		decreaseIdent(); 
		ret += writeString("</td>");
		decreaseIdent();
	
		ret += writeString("<td align=\"left\" width=\"65%\">&nbsp;");
		if (!p.isLinked()){
			String field = "";
			field += "<input type=\"text\" name="+quote(name);
			field += " value=\"<bean:write name="+quote(StrutsConfigGenerator.getContainerEntryFormName(doc,list ))+" property="+quote(name)+"/>";
			field += "\">";
			ret += writeIncreasedString(field);
		}else{
			//String select = "";
			ret += writeString("<html:select size=\"1\" property="+quote(name)+">");
			ret += writeIncreasedString("<html:optionsCollection property="+quote(name+"Collection")+" filter=\"false\"/>");
			ret += writeString("</html:select>");
		}
			
		ret += writeString("</td>");

		ret += writeString("<tr class="+quote("lineDark")+">");
		ret += writeIncreasedString("<td colspan="+quote("2")+">&nbsp;");
		ret += writeString("</tr>");

		ret += writeString("<tr class="+quote("lineLight")+">");
		increaseIdent();
		ret += writeString("<td align=\"right\" width=\"35%\">&nbsp;</td>");
		ret += writeString("<td align=\"left\" width=\"65%\">");
		ret += writeIncreasedString("<a href="+quote("#")+" onClick="+quote("document."+formName+".submit()")+">&nbsp;&raquo&nbsp;Add&nbsp;</a>");
		ret += writeString("</td>");
		decreaseIdent();
		ret += writeString("</tr>");

		ret += writeString("</html:form>");
		decreaseIdent();
		ret += writeString("</table>");

		//QUICK ADD Form 
		if (p.isLinked()){
			formName = StrutsConfigGenerator.getContainerQuickAddFormName(doc, list);
			ret += writeString("<br>");
			ret += writeString("<table width="+quote("100%")+" cellspacing="+quote("1")+" cellpadding="+quote("1")+" border="+quote("0")+">");
			increaseIdent();
			ret += writeString("<html:form action="+quote(StrutsConfigGenerator.getContainerPath(doc, list, StrutsConfigGenerator.ACTION_QUICK_ADD))+">");
			ret += writeString("<html:hidden property="+quote("ownerId")+"/>");
			ret += writeString("<input type="+quote("hidden")+" name="+quote("pId")+" value=\"<bean:write name="+quote(formName)+" property="+quote("ownerId")+"/>\">");
			
			ret += writeString("<tr class="+quote("lineCaptions")+">");
			ret += writeIncreasedString("<td colspan="+quote("2")+">Quick add some items by id:</td>");
			ret += writeString("</tr>");
	
	
			ret += writeString("<tr class="+quote("lineLight")+">");
			increaseIdent();
		        
			p = list.getContainedProperty();
			ret += writeString("<td align=\"right\" width=\"35%\">");
			increaseIdent();
			name = p.getName();
			if (name==null || name.length()==0)
				name = "&nbsp;";
			ret += writeString("ids to add :");
			decreaseIdent(); 
			ret += writeString("</td>");
			decreaseIdent();
		
			ret += writeString("<td align=\"left\" width=\"65%\">&nbsp;");
			String field = "";
			field += "<input type=\"text\" name="+quote("quickAddIds");
			field += " value=\"\"/>";
			ret += writeIncreasedString(field);
				
			ret += writeString("&nbsp;<i>comma separated list.</td>");
	
			ret += writeString("<tr class="+quote("lineDark")+">");
			ret += writeIncreasedString("<td colspan="+quote("2")+">&nbsp;");
			ret += writeString("</tr>");
	
			ret += writeString("<tr class="+quote("lineLight")+">");
			increaseIdent();
			ret += writeString("<td align=\"right\" width=\"35%\">&nbsp;</td>");
			ret += writeString("<td align=\"left\" width=\"65%\">");
			ret += writeIncreasedString("<a href="+quote("#")+" onClick="+quote("document."+formName+".submit()")+">&nbsp;&raquo&nbsp;QuickAdd&nbsp;</a>");
			ret += writeString("</td>");
			decreaseIdent();
			ret += writeString("</tr>");
	
			ret += writeString("</html:form>");
			decreaseIdent();
			ret += writeString("</table>");
		}
		//QUICK ADD END
		
		decreaseIdent();
		
		ret += writeString("</body>");
		decreaseIdent();
		ret += writeString("</html:html>");
		
		ret += getBaseJSPFooter();
		
	    
		return ret;

	}
	
	private String generateTablePage(MetaDocument doc, MetaTableProperty table){
	    String ret = "";
	    resetIdent();
	    
	    List columns = table.getColumns();
	    String formName = StrutsConfigGenerator.getContainerEntryFormName(doc, table);
	    
		ret += getBaseJSPHeader();
		
		ret += writeString("<html:html>");
		increaseIdent();
		ret += writeString("<head>");
		increaseIdent();
		ret += writeString("<title>Edit "+doc.getName()+StringUtils.capitalize(table.getName())+"</title>");
		ret += generatePragmas();
		ret += writeString("<link href=\""+getCurrentCSSPath("admin.css")+"\" rel=\"stylesheet\" type=\"text/css\">");
		decreaseIdent();
		ret += writeString("</head>");
		ret += writeString("<body>");
		increaseIdent();		
		
		ret += writeString("<table width="+quote("100%")+" cellspacing="+quote("1")+" cellpadding="+quote("1")+" border="+quote("0")+">");
		increaseIdent();
		ret += writeString("<tr class="+quote("lineCaptions")+">");
	    ret += writeIncreasedString("<td width=\"1%\">Pos</td>");
		for (int i=0; i<columns.size(); i++){
		    MetaProperty p = (MetaProperty)columns.get(i);
		    ret += writeIncreasedString("<td>"+StringUtils.capitalize(table.extractSubName(p))+"</td>");
		}
	    ret += writeIncreasedString("<td width=\"1%\">&nbsp;</td>");
		ret += writeString("</tr>");
		ret += writeString("<logic:iterate name="+quote("rows")+" id="+quote("row")+" type="+quote(ModuleBeanGenerator.getContainerEntryFormImport(doc, table))+" indexId="+quote("ind")+">");
		increaseIdent();
		ret += writeString("<tr class=\"<%=ind.intValue()%2==0 ? \"lineLight\" : \"lineDark\"%>\">");
		increaseIdent();
		ret += writeString("<td width="+quote("1%")+"><bean:write name="+quote("row")+" property="+quote("position")+"/></td>");
		for (int i=0; i<columns.size(); i++){
		    MetaProperty p = (MetaProperty)columns.get(i);
		    ret += writeString("<td><bean:write name="+quote("row")+" property="+quote(table.extractSubName(p))+"/></td>");
		}
		String parameter = "pId=<bean:write name="+quote("row")+" property="+quote("ownerId")+"/>";
		parameter += "&pPosition=<bean:write name="+quote("row")+" property="+quote("position")+"/>";
		ret += writeString("<td width="+quote("1%")+"><a href="+quote(StrutsConfigGenerator.getContainerPath(doc, table, StrutsConfigGenerator.ACTION_DELETE)+"?"+parameter)+">"+getDeleteImage("delete row")+"</a></td>");
		decreaseIdent();
		ret += writeString("</tr>");
		decreaseIdent();
		ret += writeString("</logic:iterate>");
		decreaseIdent();
		ret += writeString("</table>");
		ret += writeString("<br>");
		decreaseIdent();
		ret += writeString("<table width="+quote("100%")+" cellspacing="+quote("1")+" cellpadding="+quote("1")+" border="+quote("0")+">");
		increaseIdent();
		ret += writeString("<html:form action="+quote(StrutsConfigGenerator.getContainerPath(doc, table, StrutsConfigGenerator.ACTION_ADD))+">");
		ret += writeString("<html:hidden property="+quote("ownerId")+"/>"); 
		ret += writeString("<input type="+quote("hidden")+" name="+quote("pId")+" value=\"<bean:write name="+quote(formName)+" property="+quote("ownerId")+"/>\">"); 
		ret += writeString("<tr class="+quote("lineCaptions")+">");
	    ret += writeIncreasedString("<td colspan="+quote("2")+">Add row:</td>");
	    ret += writeString("</tr>");
	    for (int i=0; i<columns.size()+2; i++){
	        ret += writeString("<tr class="+quote(i%2==0 ? "lineLight" : "lineDark")+">");
			increaseIdent();
	        
	        if (i<columns.size()){
				MetaProperty p = (MetaProperty)columns.get(i);
				ret += writeString("<td align=\"right\" width=\"35%\">");
				increaseIdent();
				String name = table.extractSubName(p);
				if (name==null || name.length()==0)
					name = "&nbsp;";
				ret += writeString(name+":");
				decreaseIdent(); 
				ret += writeString("</td>");
				decreaseIdent();
	
				ret += writeString("<td align=\"left\" width=\"65%\">&nbsp;");
				String field = "";
				field += "<input type=\"text\" name="+quote(name);
				field += " value=\"<bean:write name="+quote(StrutsConfigGenerator.getContainerEntryFormName(doc,table ))+" property="+quote(name)+"/>";
				field += "\">";
				ret += writeIncreasedString(field);
				ret += writeString("</td>");
	        }else{
	            if (i==columns.size()){
					ret += writeString("<td colspan="+quote("2")+">&nbsp;");
	            }else{
					ret += writeString("<td align=\"right\" width=\"35%\">&nbsp;</td>");
					ret += writeString("<td align=\"left\" width=\"65%\">");
					ret += writeIncreasedString("<a href="+quote("#")+" onClick="+quote("document."+formName+".submit()")+">&nbsp;&raquo&nbsp;Add&nbsp;</a>");
					ret += writeString("</td>");

	            }
	        }
	        decreaseIdent();
	        ret += writeString("</tr>");
	    }
	    ret += writeString("</html:form>");

	    
	    ret += writeString("</body>");
		decreaseIdent();
		ret += writeString("</html:html>");
		
		ret += getBaseJSPFooter();
		
	    
	    return ret;
	}
	
	private String generateDialog(MetaDialog dialog, MetaModuleSection section, MetaView view){
		String ret = "";
		resetIdent();
		
		currentDialog = dialog;
		
		ret += getBaseJSPHeader();
		
		ret += writeString("<html:html>");
		increaseIdent();
		ret += writeString("<head>");
		increaseIdent();
		ret += writeString("<title>"+dialog.getTitle()+"</title>");
		ret += generatePragmas(view);
		ret += writeString("<link href=\""+getCurrentCSSPath("admin.css")+"\" rel=\"stylesheet\" type=\"text/css\">");
		decreaseIdent();
		ret += writeString("</head>");
		ret += writeString("<body>");
		increaseIdent();		
		ret += writeString("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
		increaseIdent();
		ret += writeString("<html:form action="+quote(StrutsConfigGenerator.getPath(section.getDocument(), StrutsConfigGenerator.ACTION_UPDATE))+">");		
		ret += writeString("<tr>");
		ret += writeIncreasedString("<td class=\"menuTitleSelected\">");
		ret += writeIncreasedString("<input type="+quote("hidden")+" name="+quote("_ts")+" value="+quote("<%=System.currentTimeMillis()%>")+">");
		ret += writeIncreasedString("<input type="+quote("hidden")+" name="+quote(ModuleBeanGenerator.FLAG_FORM_SUBMITTED)+" value="+quote("true")+">");
		ret += writeIncreasedString("<input type="+quote("hidden")+" name="+quote("nextAction")+" value="+quote("close")+">");
		ret += writeIncreasedString(dialog.getTitle()+"</td>");
		ret += writeString("</tr>");
		decreaseIdent();
		ret += writeString("</table>");
		
		int colspan=2;
		ret += writeString("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
		increaseIdent();
		ret += writeString("<tr>");
		increaseIdent();
		ret += writeString("<td colspan=\""+colspan+"\"><img src="+quote(getCurrentImagePath("s.gif"))+" width=\"1\" height=\"1\"></td>");
		decreaseIdent(); 
		ret += writeString("</tr>");
		
		List<MetaViewElement> elements = createMultilingualList(dialog.getElements(),section.getDocument(), GeneratorDataRegistry.getInstance().getContext()); 
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);

			ret += writeString("<tr class="+quote(i%2==0 ? "lineLight" : "lineDark")+">");
			increaseIdent();
			ret += writeString("<td align=\"right\" width=\"35%\">");
			increaseIdent();
			String lang = getElementLanguage(element);
			String name = lang == null ? element.getName() : section.getDocument().getField(element.getName()).getName(lang);
			if (name==null || name.length()==0)
				name = "&nbsp;";
			ret += writeString(name);
			decreaseIdent(); 
			ret += writeString("</td>");
			decreaseIdent();
			
			ret += writeString("<td align=\"left\" width=\"65%\">&nbsp;");
			ret += generateElementEditor(section.getDocument(), element);
			ret += writeString("&nbsp;<i><bean:write name=\"description."+element.getName()+"\" ignore=\"true\"/></i>");
			ret += writeString("</td>");
			
			ret += writeString("</tr>");

			
		}
		
		ret += writeString("</html:form>");
		decreaseIdent();
		ret += writeString("</table>");
		
		

		decreaseIdent();
		ret += writeString("</body>");
		decreaseIdent();
		ret += writeString("</html:html>");
		
		ret += getBaseJSPFooter();
		
		return ret;
	}
	
	private String generateElementEditor(MetaDocument doc, MetaViewElement element){
		if (element instanceof MetaEmptyElement)
			return "&nbsp;";
		if (element instanceof MetaFieldElement)
			return generateFieldEditor((MetaFieldElement)element);
		if (element instanceof MetaListElement)
			return generateListEditor(doc, (MetaListElement)element);
		if (element instanceof MetaFunctionElement)
			return generateFunctionEditor(doc, (MetaFunctionElement)element);

		return "";
			
	}
	
	private String generateListEditor(MetaDocument doc, MetaListElement element){
		String ret = "";
		
		List elements = element.getElements();
		for (int i=0; i<elements.size(); i++){
			ret += generateElementEditor(doc, (MetaViewElement)elements.get(i));
			if (i<elements.size()-1)
				ret += "&nbsp;";
		}
			
		
		return ret;
	}
	
	private String generateLinkEditor(MetaProperty p){
		//for now we have only one link...
		String ret = "";
		ret += "<html:select size=\"1\" property="+quote(p.getName())+">";
		ret += "<html:optionsCollection property="+quote(p.getName()+"Collection")+" filter=\"false\"/>";
		ret += "</html:select>";
		ret += "&nbsp;";
		ret += "(<i>old:</i>&nbsp;<bean:write property="+quote(p.getName()+"CurrentValue")+" name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" filter="+quote("false")+"/>)";

		return ret;
	}
	
	private String generateFieldEditor(MetaFieldElement element){
		MetaDocument doc = ((MetaModuleSection)currentSection).getDocument();
		MetaProperty p = doc.getField(element.getName());
		
		if (p.isLinked())
			return generateLinkEditor(p);
			
		if (p instanceof MetaEnumerationProperty){
			return generateLinkEditor(p);
		}
		
		if (p instanceof MetaContainerProperty)
			return generateContainerLinkEditor(element, (MetaContainerProperty)p);
		
		if (p.getType().equals("image")){
			return generateImageEditor(element, p);
		}
		
		if (p.getType().equals("string")){
			return generateStringEditor(element, p);
		}
		
		if (p.getType().equals("int")){
			return generateStringEditor(element, p);
		}

		if (p.getType().equals("double")){
			return generateStringEditor(element, p);
		}

		if (p.getType().equals("float")){
			return generateStringEditor(element, p);
		}

		if (p.getType().equals("long")){
			return generateStringEditor(element, p);
		}

		if (p.getType().equals("text")){
			return generateTextEditor(element, p);
		}
		
		if (p.getType().equals("boolean")){
			return generateBooleanEditor(element, p);
		}
		
		
		return p.getType();
	}
	
	private String generateContainerLinkEditor(MetaFieldElement element, MetaContainerProperty p){
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
	
	

	private String generateImageEditor(MetaFieldElement element, MetaProperty p){
		String ret ="";
		ret += "<table height=86 width=100% cellpadding=6>";
		ret += "<tr><td>";
		ret += "<iframe src=\"fileShow?nocache=<%=System.currentTimeMillis()%>\" frameborder=\"0\" width=100% height=80 scrolling=\"no\"></iframe>";		
		ret += "</tr></td>";
		ret += "</table>";
		return ret;
	}

	private String generateStringEditor(MetaFieldElement element, MetaProperty p){
		String ret ="";
		String lang = getElementLanguage(element); 
		
		
		ret += "<input type=\"text\" name="+quote(p.getName(lang));
		//ret += "<html:text filter=\"false\" property="+quote(element.getName());
		ret += " value=\"<bean:write name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" property="+quote(p.getName(lang));
		ret += "/>\"";
		if (element.isReadonly())
			ret += " readonly="+quote("true");
		ret += "/>";

		if (element.isReadonly())
			ret += "&nbsp;<i>readonly</i>";
		
		return ret;
	}

	private String generateTextEditor(MetaFieldElement element, MetaProperty p){
		String lang = getElementLanguage(element);
		String ret ="";
		ret += "<textarea cols=\"80\" rows=\"15\" name="+quote(p.getName(lang));
		ret += ">";
		ret += "<bean:write filter=\"false\" name="+quote(StrutsConfigGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" property="+quote(p.getName(lang))+" />";
		ret += "</textarea>";
		return ret;
	}

	private String generateBooleanEditor(MetaFieldElement element, MetaProperty p){
		String ret ="";
		ret += "<html:checkbox property="+quote(element.getName());
		ret += "/>";
		return ret;
	}
	
	private String generateCSVExport(MetaModuleSection section, MetaView view){
		ident = 0;
		String ret = "";
		ret += getBaseCSVHeader();
		
		currentSection = section;
		MetaDocument doc = section.getDocument();

		String entryName = doc.getName().toLowerCase();
		List elements = section.getElements();

		String headerLine = "";
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = (MetaViewElement)elements.get(i);
			String tag = generateTag(element);
			if (tag==null)
				continue;
			headerLine += tag+";";
		}
		ret += writeString(headerLine);

		ret += writeString("<logic:iterate name="+quote(doc.getMultiple().toLowerCase())+" type="+quote(ModuleBeanGenerator.getListItemBeanImport(getContext(), doc))+" id="+quote(entryName)+"><%--");
		String bodyLine = "--%>";

		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = (MetaViewElement)elements.get(i);
			String tag = generateTag(element);
			if (tag==null)
				continue;
			bodyLine += "<bean:write filter=\"false\" name="+quote(entryName)+" property=\""+element.getName()+"\"/>;";
		}
		ret += writeString(bodyLine);
		ret += writeString("</logic:iterate>");
		return ret;
	}


	private String generateTag(MetaViewElement elem){
		if (!(elem instanceof MetaFieldElement))
			return null;
		return ((MetaFieldElement)elem).getName();
		
	}
	
	private String generateXMLExport(MetaModuleSection section, MetaView view){
		ident = 0;
		String ret = "";
		ret += getBaseXMLHeader();
		
		currentSection = section;
		MetaDocument doc = section.getDocument();

		String entryName = doc.getName().toLowerCase();

		ret += writeString("<?xml version=\"1.0\" encoding="+quote(getContext().getEncoding())+"?>");
		ret += writeString("<"+doc.getMultiple()+">");
		ret += writeString("<logic:iterate name="+quote(doc.getMultiple().toLowerCase())+" type="+quote(ModuleBeanGenerator.getListItemBeanImport(getContext(), doc))+" id="+quote(entryName)+">");
		increaseIdent();
		ret += writeString("<"+doc.getName()+">");
		increaseIdent();
		List elements = section.getElements();
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
			ret += writeString(line);
		}
		decreaseIdent();
		ret += writeString("</"+doc.getName()+">");
		decreaseIdent();
		ret += writeString("</logic:iterate>");
		ret += writeString("</"+doc.getMultiple()+">");
		return ret;
	}

	private String generateSection(MetaModuleSection section, MetaView view){
		ident = 0;
		String ret = "";
		ret += getBaseJSPHeader();
		
		currentSection = section;
		MetaDocument doc = section.getDocument();

		ret += writeString("<html>");
		increaseIdent();
		ret += writeString("<head>");
		increaseIdent();
		ret += writeString("<title>"+view.getTitle()+"</title>");
		//ret += writeString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
		ret += generatePragmas(view);
		ret += writeString("<link href=\""+getCurrentCSSPath("admin.css")+"\" rel=\"stylesheet\" type=\"text/css\">");
		decreaseIdent();
		ret += writeString("</head>");
		ret += writeString("<body>");
		increaseIdent();
		ret += writeString("<jsp:include page=\""+getMenuName(view)+".jsp\" flush=\"true\"/>");

		List<MetaViewElement> elements = createMultilingualList(section.getElements(), doc, GeneratorDataRegistry.getInstance().getContext());
		int colspan = elements.size();
		
		ret += writeString("<table width=\"100%\" border=\"0\" cellspacing=\"1\" cellpadding=\"1\">");
		increaseIdent();
		ret += writeString("<tr>");
		increaseIdent();
		ret += writeString("<td colspan=\""+colspan+"\"><img src="+quote(getCurrentImagePath("s.gif"))+" width=\"1\" height=\"1\"></td>");
		decreaseIdent(); 
		ret += writeString("</tr>");
		
		ret += writeString("<tr>");
		increaseIdent();
		ret += writeString("<td colspan=\""+(2)+"\">"+
		"<a href="+quote(StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_EXPORT+StrutsConfigGenerator.SUFFIX_XML))+">XML</a>&nbsp;"+
		"<a href="+quote(StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_EXPORT+StrutsConfigGenerator.SUFFIX_CSV))+">CSV</a></td>");
		String searchForm = "<form name="+quote("Search")+" action="+quote(StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_SEARCH))+" style=\"margin:0px;padding:0px;border:0px\" target=\"_blank\">";
		String searchFormContent = "<input type="+quote("text")+" name="+quote("criteria")+" size="+quote(10)+"/>";
		searchFormContent += "&nbsp;&nbsp;";
		searchFormContent += "<a href="+quote("#")+" onClick="+quote("document.Search.submit();return false")+">Search</a>";
		
		ret += writeString(searchForm+ "<td colspan=\""+(colspan-3)+"\" align=\"right\">&nbsp;"+searchFormContent+"&nbsp;&nbsp;</td></form>");
		
		ret += writeString("<td align=\"right\">"+generateNewFunction("", new MetaFunctionElement("add"))+"</td>");
		decreaseIdent(); 
		ret += writeString("</tr>");
		
		ret += writeString("<% String selectedPaging = \"\"+request.getAttribute("+quote("currentItemsOnPage")+"); %>");
		ret += writeString("<logic:present name=\"paginglinks\" scope=\"request\">");
		increaseIdent();
		ret += writeString("<tr>");
		increaseIdent();
		ret += writeString("<td align=\"right\" colspan=\""+(colspan)+"\"><div style=\"white-space:nowrap; height:2em; line-height:2em\">");
		increaseIdent();
		ret += writeString("<logic:iterate name="+quote("paginglinks")+" scope="+quote("request")+" id="+quote("link")+" type="+quote("net.anotheria.asg.util.bean.PagingLink")+">");
		increaseIdent();
		ret += writeString("&nbsp;");
		ret += writeString("<logic:equal name="+quote("link")+" property="+quote("linked")+" value="+quote("true")+">");
		ret += writeIncreasedString("<a href=\"?pageNumber=<bean:write name="+quote("link")+" property="+quote("link")+"/>\"><bean:write name="+quote("link")+" property="+quote("caption")+"/></a>");
		ret += writeString("</logic:equal>");
		ret += writeString("<logic:notEqual name="+quote("link")+" property="+quote("linked")+" value="+quote("true")+">");
		ret += writeIncreasedString("<bean:write name="+quote("link")+" property="+quote("caption")+"/>"); 
		ret += writeString("</logic:notEqual>");
		decreaseIdent();
		ret += writeString("</logic:iterate>");
		ret += writeString("<form name="+quote("ItemsOnPageForm")+" action=\"\" method=\"GET\""+">&nbsp;&nbsp;Items&nbsp;on&nbsp;page:&nbsp;");
		ret += writeString("<select name="+quote("itemsOnPage")+" onchange="+quote("document.ItemsOnPageForm.submit();")+">");
		ret += writeString("<logic:iterate name="+quote("PagingSelector")+" type="+quote("java.lang.String")+" id="+quote("option")+">");
		ret += writeString("<option value="+quote("<bean:write name="+quote("option")+"/>")+" <logic:equal name=\"option\" value=\"<%=selectedPaging%>\">selected</logic:equal>><bean:write name="+quote("option")+"/></option>"); 
		ret += writeString("</logic:iterate>");
		ret += writeString("</select></form>");
		decreaseIdent();
		ret += writeString("</div></td>");
		decreaseIdent();
		ret += writeString("</tr>");
		decreaseIdent();
		ret += writeString("</logic:present>");

		//filter management line
		for (MetaFilter f : section.getFilters()){
			ret += writeString("<!-- Generating Filter: "+ModuleActionsGenerator.getFilterVariableName(f)+" -->");
			ret += writeString("<% String filterParameter = (String) request.getAttribute(\"currentFilterParameter\");");
			ret += writeString("if (filterParameter==null)");
			ret += writeIncreasedString("filterParameter = \"\";%>");
			ret += writeString("<tr class=\"lineCaptions\"><td colspan="+quote(colspan)+">Filter <strong>"+StringUtils.capitalize(f.getFieldName())+":</strong>&nbsp;");
			increaseIdent();
			ret += writeString("<logic:iterate name="+quote(ModuleActionsGenerator.getFilterVariableName(f))+" id="+quote("triggerer")+" type="+quote("net.anotheria.asg.util.filter.FilterTrigger")+">");
			increaseIdent();
			ret += writeString("<logic:equal name="+quote("triggerer")+" property="+quote("parameter")+" value="+quote("<%=filterParameter%>")+">");
			ret += writeIncreasedString("<strong><bean:write name="+quote("triggerer")+" property="+quote("caption")+"/></strong>");
			ret += writeString("</logic:equal>");
			ret += writeString("<logic:notEqual name="+quote("triggerer")+" property="+quote("parameter")+" value="+quote("<%=filterParameter%>")+">");
			ret += writeIncreasedString("<a href="+quote(StrutsConfigGenerator.getPath(section.getDocument(), StrutsConfigGenerator.ACTION_SHOW)+"?pFilter=<bean:write name="+quote("triggerer")+" property="+quote("parameter")+"/>")+"><bean:write name="+quote("triggerer")+" property="+quote("caption")+"/></a>");
			ret += writeString("</logic:notEqual>");
			decreaseIdent();
			ret += writeString("</logic:iterate>");
			decreaseIdent();
			ret += writeString("</td></tr>");
		}

		//write header
		ret += writeString("<tr class=\"lineCaptions\">");
		increaseIdent(); 
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			ret += writeString(generateElementHeader(element));
		}
		decreaseIdent();
		ret += writeString("</tr>");
		
		String entryName = doc.getName().toLowerCase();
		ret += writeString("<logic:iterate name="+quote(doc.getMultiple().toLowerCase())+" type="+quote(ModuleBeanGenerator.getListItemBeanImport(getContext(), doc))+" id="+quote(entryName)+" indexId=\"ind\">");
		increaseIdent();
		ret += writeString("<tr class=\"<%=ind.intValue()%2==0 ? \"lineLight\" : \"lineDark\"%>\">");

		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			ret += writeString(generateElement(entryName, element));
		}

		ret += writeString("</tr>");
		decreaseIdent();
		ret += writeString("</logic:iterate>");

		ret += writeString("<tr>");
		increaseIdent();
		ret += writeString("<td colspan=\""+(colspan)+"\"><img src="+quote(getCurrentImagePath("s.gif"))+" width=\"1\" height=\"1\"></td>");
		decreaseIdent(); 
		ret += writeString("</tr>");
		ret += writeString("<tr class=\"lineCaptions\">");
		increaseIdent();
		ret += writeString("<td colspan="+quote(colspan)+" align="+quote("right")+
			"><a href="+quote(StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_EXPORT+StrutsConfigGenerator.SUFFIX_XML))+">XML</a>&nbsp;"+
			"<a href="+quote(StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_EXPORT+StrutsConfigGenerator.SUFFIX_CSV))+">CSV</a></td>");
		decreaseIdent();
		ret += writeString("</tr>");
		

		decreaseIdent();
		ret += writeString("</table>");
		decreaseIdent();
		ret += writeString("<jsp:include page=\""+getFooterName(view)+".jsp\" flush=\"true\"/>");
		ret += writeString("</body>");
		decreaseIdent();
		ret += writeString("</html>");
		ret += getBaseJSPFooter(); 
		return ret;
	}
	
	private String generateSearchPage(){
		ident = 0;
		String ret = "";
		ret += getBaseJSPHeader();
		

		ret += writeString("<html>");
		increaseIdent();
		ret += writeString("<head>");
		increaseIdent();
		ret += writeString("<title>Search result</title>");
		//ret += writeString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
		ret += generatePragmas();
		ret += writeString("<link href=\""+getCurrentCSSPath("admin.css")+"\" rel=\"stylesheet\" type=\"text/css\">");
		decreaseIdent();
		ret += writeString("</head>");
		ret += writeString("<body>");
		increaseIdent();
		//ret += writeString("<jsp:include page=\""+getMenuName(view)+".jsp\" flush=\"true\"/>");

		int colspan = 3;
		
		ret += writeString("<table width=\"100%\" border=\"0\" cellspacing=\"1\" cellpadding=\"1\">");
		increaseIdent();
		ret += writeString("<tr>");
		increaseIdent();
		ret += writeString("<td colspan=\""+colspan+"\"><img src="+quote(getCurrentImagePath("s.gif"))+" width=\"1\" height=\"1\"></td>");
		decreaseIdent(); 
		ret += writeString("</tr>");
		
		//write header
		ret += writeString("<tr class=\"lineCaptions\">");
		increaseIdent();
		ret += writeString("<td width=\"5%\">Id</td>");
		ret += writeString("<td width=\"15%\">Property name</td>");
		ret += writeString("<td width=\"80%\">Match</td>");
		decreaseIdent();
		ret += writeString("</tr>");
		
		ret += writeString("<logic:iterate name="+quote("result")+" type="+quote("net.anotheria.anodoc.query2.ResultEntryBean")+" id="+quote("entry")+" indexId=\"ind\">");
		increaseIdent();
		ret += writeString("<tr class=\"<%=ind.intValue()%2==0 ? \"lineLight\" : \"lineDark\"%>\">");
		ret += writeString("<td width=\"5%\"><a href="+quote("<bean:write name="+quote("entry")+" property="+quote("editLink")+"/>")+" target="+quote("_blank")+"><bean:write name="+quote("entry")+" property="+quote("documentId")+"/></td>");
		ret += writeString("<td width=\"15%\"><bean:write name="+quote("entry")+" property="+quote("propertyName")+"/></td>");
		ret += writeString("<td width=\"80%\"><bean:write name="+quote("entry")+" property="+quote("info")+" filter="+quote("false")+"/></td>");
		ret += writeString("</tr>");
		decreaseIdent();
		ret += writeString("</logic:iterate>");

		decreaseIdent();
		ret += writeString("</table>");
		decreaseIdent();
		//ret += writeString("<jsp:include page=\""+getFooterName(view)+".jsp\" flush=\"true\"/>");
		ret += writeString("</body>");
		decreaseIdent();
		ret += writeString("</html>");
		ret += getBaseJSPFooter(); 
		return ret;
	}
 
	private String generateVersionInfoPage(){
		ident = 0;
		String ret = "";
		ret += getBaseJSPHeader();
		

		ret += writeString("<html>");
		increaseIdent();
		ret += writeString("<head>");
		increaseIdent();
		ret += writeString("<title>VersionInfo for <bean:write name=\"documentName\"/></title>");
		//ret += writeString("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
		ret += generatePragmas();
		ret += writeString("<link href=\""+getCurrentCSSPath("admin.css")+"\" rel=\"stylesheet\" type=\"text/css\">");
		decreaseIdent();
		ret += writeString("</head>");
		ret += writeString("<body>");
		increaseIdent();
		//ret += writeString("<jsp:include page=\""+getMenuName(view)+".jsp\" flush=\"true\"/>");

		int colspan = 2;
		
		ret += writeString("<table width=\"100%\" border=\"0\" cellspacing=\"1\" cellpadding=\"1\">");
		increaseIdent();
		ret += writeString("<tr>");
		increaseIdent();
		ret += writeString("<td colspan=\""+colspan+"\"><img src="+quote(getCurrentImagePath("s.gif"))+" width=\"1\" height=\"1\"></td>");
		decreaseIdent(); 
		ret += writeString("</tr>");
		
		//write header
		ret += writeString("<tr class=\"lineCaptions\">");
		increaseIdent();
		ret += writeString("<td colspan=\"2\">VersionInfo for document</td>");
		decreaseIdent();
		ret += writeString("</tr>");
		
		ret += writeString("<tr class=\"lineLight\">");
		ret += writeIncreasedString("<td width=\"20%\">Document name: </td>");
		ret += writeIncreasedString("<td width=\"80%\"><bean:write name="+quote("documentName")+"/></td>");
		ret += writeString("</tr>");

		ret += writeString("<tr class=\"lineDark\">");
		ret += writeIncreasedString("<td width=\"20%\">Document type: </td>");
		ret += writeIncreasedString("<td width=\"80%\"><bean:write name="+quote("documentType")+"/></td>");
		ret += writeString("</tr>");

		ret += writeString("<tr class=\"lineLight\">");
		ret += writeIncreasedString("<td width=\"20%\">Last update: </td>");
		ret += writeIncreasedString("<td width=\"80%\"><bean:write name="+quote("lastUpdate")+"/></td>");
		ret += writeString("</tr>");

		ret += writeString("<tr class=\"lineDark\">");
		increaseIdent();
		ret += writeString("<td colspan=\"2\">&nbsp;</td>");
		decreaseIdent();
		ret += writeString("</tr>");

		ret += writeString("<tr class=\"lineLight\">");
		ret += writeIncreasedString("<td width=\"20%\">&nbsp;</td>");
		ret += writeIncreasedString("<td width=\"80%\"><a href=\"javascript:history.back();\">Back</a></td>");
		ret += writeString("</tr>");

		decreaseIdent();
		ret += writeString("</table>");
		decreaseIdent();
		//ret += writeString("<jsp:include page=\""+getFooterName(view)+".jsp\" flush=\"true\"/>");
		ret += writeString("</body>");
		decreaseIdent();
		ret += writeString("</html>");
		ret += getBaseJSPFooter(); 
		return ret;
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

	private String generateElement(String entryName, MetaViewElement element){
		if (element instanceof MetaFieldElement)
			return generateField(entryName, (MetaFieldElement)element);
		if (element instanceof MetaFunctionElement)
			return generateFunction(entryName, (MetaFunctionElement)element);
		if (element instanceof MetaCustomFunctionElement)
			return generateCustomFunction(entryName, (MetaCustomFunctionElement)element);
		
		return "";
	}
	
	private String generateField(String entryName, MetaFieldElement element){
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

	private String generateFunction(String entryName, MetaFunctionElement element){
		
		if (element.getName().equals("version")){
			return generateVersionFunction(entryName, element);
		}

		if (element.getName().equals("delete")){
			return generateDeleteFunction(entryName, element);
		}

		if (element.getName().equals("deleteWithConfirmation")){
			return generateDeleteWithConfirmationFunction(entryName, element);
		}

		if (element.getName().equals("edit"))
			return generateEditFunction(entryName, element);
			
		if (element.getName().equals("duplicate"))
			return generateDuplicateFunction(entryName, element);

			
		return "";
		//return "<td><bean:write name="+quote(entryName)+" property=\""+element.getName()+"\"/></td>";
	}
	
	private String generateCustomFunction(String entryName, MetaCustomFunctionElement element){
		String caption = element.getCaption();
		String link = element.getLink();
		link = StringUtils.replace(link, "$plainId", "<bean:write name="+quote(entryName)+" property=\"plainId\"/>");
		return "<td><a href="+quote(generateTimestampedLinkPath(link))+">"+caption+"</a></td>";
	}

	private String generateFunctionEditor(MetaDocument doc, MetaFunctionElement element){
		if (element.getName().equals("cancel")){
			return "<a href="+quote(generateTimestampedLinkPath(StrutsConfigGenerator.getPath(doc, StrutsConfigGenerator.ACTION_SHOW)))+">&nbsp;&raquo&nbsp;Close&nbsp;</a>";
		}
		
		if (element.getName().equals("update")){
			return generateUpdateAndCloseFunction(doc, element);
		}

		if (element.getName().equals("updateAndStay")){
			return generateUpdateAndStayFunction(doc, element);
		}
		if (element.getName().equals("updateAndClose")){
			return generateUpdateAndCloseFunction(doc, element);
		}
		
		return "";
	}
	
	private String generateUpdateAndStayFunction(MetaDocument doc, MetaFunctionElement element){
		return "<a href=\"#\" onClick=\"document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+".nextAction.value='stay'; document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+".submit(); return false\">&nbsp;&raquo&nbsp;<bean:write name=\"save.label.prefix\"/>AndStay&nbsp;</a>";
	}
	private String generateUpdateAndCloseFunction(MetaDocument doc, MetaFunctionElement element){
		return "<a href=\"#\" onClick=\"document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+".nextAction.value='close'; document."+StrutsConfigGenerator.getDialogFormName(currentDialog, doc)+".submit(); return false\">&nbsp;&raquo&nbsp;<bean:write name=\"save.label.prefix\"/>AndClose&nbsp;</a>";
	}
	
	
	private String generateDuplicateFunction(String entryName, MetaFunctionElement element){
		String path = StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_DUPLICATE);
		path += "?pId=<bean:write name="+quote(entryName)+" property=\"plainId\"/>";
		
		return "<td><a href="+quote("<ano:tslink>"+path+"</ano:tslink>")+">"+getDuplicateImage()+"</a></td>" ;
	}

	private String generateVersionFunction(String entryName, MetaFunctionElement element){
		String path = StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_VERSIONINFO);
		path += "?pId=<bean:write name="+quote(entryName)+" property=\"plainId\"/>";
		
		return "<td><a href="+quote("<ano:tslink>"+path+"</ano:tslink>")+" title="+quote("LastUpdate: <bean:write name="+quote(entryName)+" property="+quote("documentLastUpdateTimestamp")+"/>")+">"+getVersionImage()+"</a></td>" ;
	}

	private String generateDeleteFunction(String entryName, MetaFunctionElement element){
		String path = StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_DELETE);
		path += "?pId=<bean:write name="+quote(entryName)+" property=\"plainId\"/>";
		
		return "<td><a href="+quote("<ano:tslink>"+path+"</ano:tslink>")+">"+getDeleteImage()+"</a></td>" ;
	}

	private String generateDeleteWithConfirmationFunction(String entryName, MetaFunctionElement element){
		String path = StrutsConfigGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), StrutsConfigGenerator.ACTION_DELETE);
		path += "?pId=<bean:write name="+quote(entryName)+" property=\"plainId\"/>";
		return "<td><a href="+quote("<ano:tslink>"+path+"</ano:tslink>")+" onClick="+quote("return confirm('Really delete "+((MetaModuleSection)currentSection).getDocument().getName()+" with id: <bean:write name="+quote(entryName)+" property=\"id\"/>');")+">"+getDeleteImage()+"</a></td>" ;
	}

	private String generateEditFunction(String entryName, MetaFunctionElement element){
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
			return "<textarea  name="+quote(name)+" class="+quote(className)+" rows="+quote(3)+" cols="+quote(quote(field.getSize()))+"></textarea>";
		
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
		ret += writeString("<!-- form: "+formName+" -->");
		
		ret += writeString("<table width="+quote("100%")+" cellspacing="+quote("1")+" cellpadding="+quote("1")+" border="+quote("0")+">");
		increaseIdent();
		ret += writeString("<html:form action="+quote(StrutsConfigGenerator.getFormPath(form))+">");
		List elements = form.getElements();
		for (int i=0; i<elements.size(); i++){
///*
		    MetaFormField element = (MetaFormField)elements.get(i);
		    if (element.isSingle()){
		    	MetaFormSingleField field = (MetaFormSingleField )element;
				ret += writeString("<tr class="+quote("qs_info")+">");
				increaseIdent();
				ret += writeString("<td width=10%><img src="+quote("<bean:message key="+quote("emptyimage")+"/>")+" width="+quote(10)+" height="+quote(1)+"/></td>");
				ret += writeString("<td class="+quote("qs_info")+" align="+quote("left")+">");
	
				String htmlFieldDeclaration = generateFormField(field, field.getType().equals("boolean") ? "qs_input" : "qs_info");
				
				if (field.getType().equals("boolean")){
					ret += writeString(htmlFieldDeclaration);
					ret += writeString("&nbsp;<bean:message key="+quote(field.getTitle())+"/>");
				}else{
					ret += writeString("<bean:message key="+quote(field.getTitle())+"/>"+(!(field.isSpacer()) ? ":&nbsp;<br><br>" : ""));
					if (htmlFieldDeclaration.length()>0)
						ret += writeString(htmlFieldDeclaration);
				}
				if (!field.isSpacer())
					ret += writeString("<br><br>");
				ret += writeString("</td>");
				ret += writeString("<td width=10%><img src="+quote("<bean:message key="+quote("emptyimage")+"/>")+" width="+quote(10)+" height="+quote(1)+"/></td>");
				decreaseIdent();
				ret += writeString("</tr>");
		    }
		    
		    if (element.isComplex()){
		    	MetaFormTableField table = (MetaFormTableField)element;
		    	//now write inner table;
				ret += writeString("<tr class="+quote("qs_info")+">");
				increaseIdent();
				ret += writeString("<td width=10%><img src="+quote("<bean:message key="+quote("emptyimage")+"/>")+" width="+quote(10)+" height="+quote(1)+"/></td>");
				ret += writeString("<td class="+quote("qs_info")+" >");
				increaseIdent();
				ret += writeString("<table width="+quote("100%")+" cellpadding="+quote(0)+" cellspacing="+quote(0)+" border="+quote(0)+">");
				increaseIdent();
				
				//generate table headers
				ret += writeString("<tr>");
				List columns = table.getColumns();
				for (int c=0; c<columns.size(); c++){
					MetaFormTableColumn col = (MetaFormTableColumn)columns.get(c);
					MetaFormTableHeader header = col.getHeader();
					ret += writeIncreasedString("<td><strong><bean:message key="+quote(header.getKey())+"/></strong></td>");
				}

				ret += writeString("</tr>");
					
				//generate table rows
				for (int r=0; r<table.getRows(); r++){
					ret += writeString("<tr>");
					increaseIdent();
					for (int c=0; c<columns.size(); c++){
						MetaFormTableColumn col = (MetaFormTableColumn)columns.get(c);
						//System.out.println("Generating column: "+col);
						ret += writeString("<td width="+quote(col.getHeader().getWidth())+">");
						ret += writeIncreasedString(generateFormField(table.getVariableName(r,c), col.getField(), ""));
						ret += writeString("</td>");
					}					
					decreaseIdent();
					ret += writeString("</tr>");
				}
				
				
				decreaseIdent();
				ret += writeString("</table>");
				decreaseIdent();
				ret += writeString("</td>");
				ret += writeString("<td width=10%><img src="+quote("<bean:message key="+quote("emptyimage")+"/>")+" width="+quote(10)+" height="+quote(1)+"/></td>");
				ret += writeString("</tr>");		    	
		    }
		    
//*/		    
		}

		decreaseIdent();
		ret += writeString("</html:form>");
		decreaseIdent();
		ret += writeString("</table>");

		ret += getBaseJSPFooter();
	    return ret;
	}
	

}
