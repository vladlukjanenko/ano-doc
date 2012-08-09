package net.anotheria.asg.generator.view.jsp;

import net.anotheria.asg.generator.*;
import net.anotheria.asg.generator.forms.meta.*;
import net.anotheria.asg.generator.meta.*;
import net.anotheria.asg.generator.view.CMSMappingsConfiguratorGenerator;
import net.anotheria.asg.generator.view.action.ModuleActionsGenerator;
import net.anotheria.asg.generator.view.meta.*;
import net.anotheria.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates the jsps for the edit view.
 * @author another
 */
public class JspGenerator extends AbstractJSPGenerator implements IGenerator{

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
			files.add(new FileEntry(new ShowPageJspGenerator().generate(section, view)));
			files.add(new FileEntry(generateCSVExport(section, view)));
			files.add(new FileEntry(generateXMLExport(section, view)));

			FileEntry linksToThisFile = new FileEntry(new LinksToMePageJspGenerator().generate(section, view));
			linksToThisFile.setType(".jsp");
			files.add(linksToThisFile);
			
			List<MetaDialog> dialogs = section.getDialogs();
			for (int d=0; d<dialogs.size(); d++){
				MetaDialog dialog = dialogs.get(d);
				
				files.add(new FileEntry( new DialogPageJspGenerator().generate(currentSection, dialog, section, view)));
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
		appendString("<ano:iterate name=\"menu\" type=\"net.anotheria.webutils.bean.MenuItemBean\" id=\"entry\">");
		appendString("<td>");
		increaseIdent();
		appendString("<ano:equal name=\"entry\" property=\"active\" value=\"true\">");
		appendIncreasedString("<td class=\"menuTitleSelected\"><ano:write name=\"entry\" property=\"caption\"/></td>");
		appendString("</ano:equal>");
		appendString("<ano:notEqual name=\"entry\" property=\"active\" value=\"true\">");
		appendIncreasedString("<td class=\"menuTitle\"><a href=\"<ano:tslink><ano:write name=\"entry\" property=\"link\"/></ano:tslink>\"><ano:write name=\"entry\" property=\"caption\"/></a></td>");
		appendString("</ano:notEqual>");
		decreaseIdent();
		appendString("</td>");
		decreaseIdent();
		appendString("</ano:iterate>");
		closeTR();

		decreaseIdent();
		appendString("</table>");
		
		append(getBaseJSPFooter());
		return jsp;
	}
	

	private GeneratedJSPFile generateContainerPage(MetaModuleSection section, MetaDocument doc, MetaContainerProperty p){
		if (p instanceof MetaListProperty)
			return new ListPageJspGenerator().generate(section, doc, (MetaListProperty)p);
		if (p instanceof MetaTableProperty)
			return new TablePageJspGenerator().generate(section, doc, (MetaTableProperty)p);
		throw new RuntimeException("Unsupported container: "+p);
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

		appendString("<ano:iterate name="+quote(doc.getMultiple().toLowerCase()+ModuleActionsGenerator.exportCSVSufix)+" type="+quote(GeneratorDataRegistry.getInstance().getContext().getDataPackageName(doc)+"."+doc.getName())+" id="+quote(entryName)+"><%--");
		String bodyLine = "--%>";

		for (MetaViewElement element : elements) {
			String lang = getElementLanguage(element);
			boolean multilangEl = element instanceof MultilingualFieldElement;
			String tag = multilangEl && lang != null ? doc.getField(element.getName()).getName(lang) : generateTag(element);
			if (tag==null)
				continue;
			bodyLine += "<ano:write filter=\"false\" name=" + quote(entryName) + " property=\"" + tag + "\"/>;";
		}
		appendString(bodyLine);
		appendString("</ano:iterate>");
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
	
	

	
	private GeneratedJSPFile generateSearchPage(){
		
		
		GeneratedJSPFile jsp = new GeneratedJSPFile();
		startNewJob(jsp);
		jsp.setName(getSearchResultPageName());
		jsp.setPackage(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED)+".jsp");
		
		ident = 0;
		
		append(getBaseJSPHeader());
		
		appendString("<!--  generated by JspMafViewGenerator.generateSearchPage -->");
		appendString("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
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
		appendString("<h2>Search result: <ano:write name=\"criteria\"/></h2>");
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
		appendString("<ano:present name="+quote("result")+" >");
		appendString("<tbody>");
		appendString("<ano:iterate name="+quote("result")+" type="+quote("net.anotheria.anodoc.query2.ResultEntryBean")+" id="+quote("entry")+" indexId=\"ind\">");
		increaseIdent();
		appendString("<tr class=\"<%=ind.intValue()%2==0 ? \"lineLight\" : \"lineDark\"%>\">");
		appendString("<td style=\"width: 50px;\"><a href="+quote("<ano:write name="+quote("entry")+" property="+quote("editLink")+"/>")+" target="+quote("_blank")+"><ano:write name="+quote("entry")+" property="+quote("documentId")+"/></td>");
		appendString("<td style=\"width: 200px;\"><ano:write name="+quote("entry")+" property="+quote("documentName")+"/></td>");
		appendString("<td style=\"width: 200px;\"><ano:write name="+quote("entry")+" property="+quote("propertyName")+"/></td>");
		appendString("<td><ano:write name="+quote("entry")+" property="+quote("info")+" filter="+quote("false")+"/></td>");
		appendString("</tr>");
		decreaseIdent();
		appendString("</ano:iterate>");
		appendString("</tbody>");
		appendString("</ano:present>");

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
		appendString("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		increaseIdent();
		appendString("<head>");
		increaseIdent();
		appendString("<title>VersionInfo for <ano:write name=\"documentName\"/></title>");
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
		appendIncreasedString("<td width=\"80%\"><ano:write name="+quote("documentName")+"/></td>");
		appendString("</tr>");

		appendString("<tr class=\"lineDark\">");
		appendIncreasedString("<td width=\"20%\">Document type: </td>");
		appendIncreasedString("<td width=\"80%\"><ano:write name="+quote("documentType")+"/></td>");
		appendString("</tr>");

		appendString("<tr class=\"lineLight\">");
		appendIncreasedString("<td width=\"20%\">Last update: </td>");
		appendIncreasedString("<td width=\"80%\"><ano:write name="+quote("lastUpdate")+"/></td>");
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

        if (field.getType().equals("password"))
            return "<input type=\"password\" size="+quote(field.getSize())+" name="+quote(name)+" class="+quote(className)+"/>";

		if (field.getType().equals("spacer"))
			return "";
		
		throw new RuntimeException("Unsupported field type: "+field.getType());
		
	}
	
	public String generateFormInclude(MetaForm form){
		System.out.println("generating form "+form);
	    String ret = "";
	    resetIdent();

		String formName = CMSMappingsConfiguratorGenerator.getFormName(form);
		

		ret += getBaseJSPHeader();
		appendString("<!-- form: "+formName+" -->");
		
		appendString("<table width="+quote("100%")+" cellspacing="+quote("1")+" cellpadding="+quote("1")+" border="+quote("0")+">");
		increaseIdent();
		appendString("<form action="+quote(CMSMappingsConfiguratorGenerator.getFormPath(form))+">");
		List<MetaFormField> elements = form.getElements();
		for (int i=0; i<elements.size(); i++){
///*
		    MetaFormField element = elements.get(i);
		    if (element.isSingle()){
		    	MetaFormSingleField field = (MetaFormSingleField )element;
				appendString("<tr class="+quote("qs_info")+">");
				increaseIdent();
				appendString("<td width=10%><img src="+quote("<ano:message key="+quote("emptyimage")+"/>")+" width="+quote(10)+" height="+quote(1)+"/></td>");
				appendString("<td class="+quote("qs_info")+" align="+quote("left")+">");
	
				String htmlFieldDeclaration = generateFormField(field, field.getType().equals("boolean") ? "qs_input" : "qs_info");
				
				if (field.getType().equals("boolean")){
					appendString(htmlFieldDeclaration);
					appendString("&nbsp;<ano:message key="+quote(field.getTitle())+"/>");
				}else{
					appendString("<ano:message key="+quote(field.getTitle())+"/>"+(!(field.isSpacer()) ? ":&nbsp;<br><br>" : ""));
					if (htmlFieldDeclaration.length()>0)
						appendString(htmlFieldDeclaration);
				}
				if (!field.isSpacer())
					appendString("<br><br>");
				appendString("</td>");
				appendString("<td width=10%><img src="+quote("<ano:message key="+quote("emptyimage")+"/>")+" width="+quote(10)+" height="+quote(1)+"/></td>");
				decreaseIdent();
				appendString("</tr>");
		    }
		    
		    if (element.isComplex()){
		    	MetaFormTableField table = (MetaFormTableField)element;
		    	//now write inner table;
				appendString("<tr class="+quote("qs_info")+">");
				increaseIdent();
				appendString("<td width=10%><img src="+quote("<ano:message key="+quote("emptyimage")+"/>")+" width="+quote(10)+" height="+quote(1)+"/></td>");
				appendString("<td class="+quote("qs_info")+" >");
				increaseIdent();
				appendString("<table width="+quote("100%")+" cellpadding="+quote(0)+" cellspacing="+quote(0)+" border="+quote(0)+">");
				increaseIdent();
				
				//generate table headers
				appendString("<tr>");
				List<MetaFormTableColumn> columns = table.getColumns();
				for (MetaFormTableColumn col : columns){
					MetaFormTableHeader header = col.getHeader();
					appendIncreasedString("<td><strong><ano:message key="+quote(header.getKey())+"/></strong></td>");
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
				appendString("<td width=10%><img src="+quote("<ano:message key="+quote("emptyimage")+"/>")+" width="+quote(10)+" height="+quote(1)+"/></td>");
				appendString("</tr>");		    	
		    }
		    
//*/		    
		}

		decreaseIdent();
		appendString("</form>");
		decreaseIdent();
		appendString("</table>");

		ret += getBaseJSPFooter();
	    return ret;
	}
	

}
