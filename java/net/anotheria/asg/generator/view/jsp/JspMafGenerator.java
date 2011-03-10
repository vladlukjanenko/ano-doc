package net.anotheria.asg.generator.view.jsp;

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
import net.anotheria.asg.generator.meta.MetaListProperty;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.MetaProperty;
import net.anotheria.asg.generator.meta.MetaTableProperty;
import net.anotheria.asg.generator.meta.StorageType;
import net.anotheria.asg.generator.util.DirectLink;
import net.anotheria.asg.generator.view.AbstractJSPMafGenerator;
import net.anotheria.asg.generator.view.CMSMappingsConfiguratorGenerator.SectionAction;
import net.anotheria.asg.generator.view.ModuleActionsGenerator;
import net.anotheria.asg.generator.view.ModuleMafBeanGenerator;
import net.anotheria.asg.generator.view.StrutsConfigGenerator;
import net.anotheria.asg.generator.view.ViewConstants;
import net.anotheria.asg.generator.view.meta.MetaCustomFunctionElement;
import net.anotheria.asg.generator.view.meta.MetaDialog;
import net.anotheria.asg.generator.view.meta.MetaFieldElement;
import net.anotheria.asg.generator.view.meta.MetaFilter;
import net.anotheria.asg.generator.view.meta.MetaFunctionElement;
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
public class JspMafGenerator extends AbstractJSPMafGenerator implements IGenerator{

	/**
	 * Currently generated section.
	 */
	private MetaSection currentSection;
	

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
				
				files.add(new FileEntry( new DialogJspMafGenerator().generate(currentSection, dialog, section, view)));
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
			return new ListJspMafGenerator().generate(section, doc, (MetaListProperty)p);
		if (p instanceof MetaTableProperty)
			return new TableJspMafGenerator().generate(section, doc, (MetaTableProperty)p);
		throw new RuntimeException("Unsupported container: "+p);
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

		appendString("<?xml version=\"1.0\" encoding="+quote(getContext().getEncoding())+"?>");
		appendString("<ano-xml:xml_write name="+quote(doc.getMultiple().toLowerCase()+ModuleActionsGenerator.exportXMLSufix)+"/>");
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
		if (((MetaModuleSection)currentSection).getDocument().getField(element.getName()).getType() == MetaProperty.Type.IMAGE && element.getDecorator()==null)
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
