package net.anotheria.asg.generator.view.jsp;

import java.util.List;

import net.anotheria.asg.data.LockableObject;
import net.anotheria.asg.generator.GeneratedJSPFile;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaProperty;
import net.anotheria.asg.generator.meta.StorageType;
import net.anotheria.asg.generator.view.CMSMappingsConfiguratorGenerator;
import net.anotheria.asg.generator.view.ViewConstants;
import net.anotheria.asg.generator.view.CMSMappingsConfiguratorGenerator.SectionAction;
import net.anotheria.asg.generator.view.action.ModuleActionsGenerator;
import net.anotheria.asg.generator.view.action.ModuleBeanGenerator;
import net.anotheria.asg.generator.view.meta.MetaCustomFunctionElement;
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
 * Generator for show page.
 */
public class ShowPageJspGenerator extends AbstractJSPGenerator {
	
	private MetaSection currentSection;
	
	public GeneratedJSPFile generate(MetaModuleSection section, MetaView view){
		
		currentSection = section;
		
		GeneratedJSPFile jsp = new GeneratedJSPFile();
		startNewJob(jsp);
		jsp.setName(getShowPageName(section.getDocument()));
		jsp.setPackage(GeneratorDataRegistry.getInstance().getContext().getJspPackageName(section.getModule()));
		
		ident = 0;
		append(getBaseJSPHeader());		
		
		MetaDocument doc = section.getDocument();
		appendString("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"");
		appendString("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
		appendString("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		appendString("<head>");
			appendString("<!--  generated by JspMafViewGenerator.generateShowPage -->");
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
					appendString("<div class=\"left_p\">"+generateNewFunction("", new MetaFunctionElement("add")) +"&nbsp;&nbsp;" + generateDeleteSelectedFunction("", new MetaFunctionElement("delete"))+"</div>");
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
											String pageNumberParam = "?pageNumber=<ano:write name="+quote("currentpage")+" scope="+quote("request")+"/>";
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
								appendString("<ano:iterate name="+quote(ModuleActionsGenerator.getFilterVariableName(f))+" id="+quote("triggerer")+" type="+quote("net.anotheria.asg.util.filter.FilterTrigger")+">");
								increaseIdent();
									appendString("<ano:equal name="+quote("triggerer")+" property="+quote("parameter")+" value="+quote("<%=filterParameter"+i+"%>")+">");
									appendIncreasedString("<li><a href=\"#\" class=\"active\"><ano:write name="+quote("triggerer")+" property="+quote("caption")+"/></a></li>");
									appendString("</ano:equal>");
									appendString("<ano:notEqual name="+quote("triggerer")+" property="+quote("parameter")+" value="+quote("<%=filterParameter"+i+"%>")+">");
									appendIncreasedString("<li><a href="+quote(SectionAction.SHOW.getMappingName(section)+"?pFilter"+i+"=<ano:write name="+quote("triggerer")+" property="+quote("parameter")+"/>")+"><ano:write name="+quote("triggerer")+" property="+quote("caption")+"/></a></li>");
									appendString("</ano:notEqual>");
								decreaseIdent();
								appendString("</ano:iterate>");
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
	appendString("<ano:greaterThan name=\"pagingControl\" property=\"numberOfPages\" value=\"1\">");
		appendString("<ul>");
		increaseIdent();
			appendString("<ano:notEqual name=\"pagingControl\" property=\"first\" value=\"true\">");
			appendIncreasedString("<li class=\"prev\"><a href=\"?pageNumber=<ano:write name=\"pagingControl\" property=\"previousPageNumber\"/>\">prev</a></li>");
			appendString("</ano:notEqual>");
			appendString("<ano:iterate id=\"pageElement\" name=\"pagingControl\" property=\"elements\" indexId=\"index\">");
			increaseIdent();
				appendString("<li>");
				increaseIdent();
					appendString("<ano:equal name=\"pageElement\" property=\"separator\" value=\"true\">...</ano:equal>");
					appendString("<ano:equal name=\"pageElement\" property=\"active\" value=\"true\"><ano:write name=\"pageElement\" property=\"caption\"/></ano:equal>");
					appendString("<ano:equal name=\"pageElement\" property=\"active\" value=\"false\"><a href=\"?pageNumber=<ano:write name=\"pageElement\" property=\"caption\"/>\"><ano:write name=\"pageElement\" property=\"caption\"/></a></ano:equal>");
				decreaseIdent();
				appendString("</li>");
			decreaseIdent();
			appendString("</ano:iterate>");
			appendString("<ano:notEqual name=\"pagingControl\" property=\"last\" value=\"true\">");
			appendIncreasedString("<li class=\"next\"><a href=\"?pageNumber=<ano:write name=\"pagingControl\" property=\"nextPageNumber\"/>\">next</a></li>");
			appendString("</ano:notEqual>");
		decreaseIdent();
		appendString("</ul>");
		appendString("</ano:greaterThan>");

		increaseIdent();
			appendString("<select name=\"itemsOnPage\" onchange=\"window.location='?itemsOnPage=' + this.options[this.selectedIndex].value\">");
			appendString("<ano:iterate name=\"PagingSelector\" type=\"java.lang.String\" id=\"option\">");
			appendString("<option value=\"<ano:write name=\"option\"/>\" <ano:equal name=\"option\" value=\"<%=selectedPaging%>\">selected</ano:equal>><ano:write name=\"option\"/> per page</option>");
			appendString("</ano:iterate>");
			appendString("</select>");
		decreaseIdent();
	decreaseIdent();
	appendString("</div>");
	appendString("<div class=\"clear\"><!-- --></div>");
	//first paging end
		
	//main table start
		appendString("<div class=\"scroll_x\">");
		appendString("<table cellspacing=\"1\" cellpadding=\"1\" width=\"100%\" border=\"0\" class=\"cmsShow pages_table\">");
		increaseIdent();
			appendString("<thead>");
			appendString("<tr class=\"lineCaptions\">");
			increaseIdent();
			boolean opened = false;

			appendString("<td><input type=\"checkbox\" onclick=\"javascript:alert('Fix Me Andrey!')\"></td>");
				for (int i=0; i<elements.size(); i++){
					MetaViewElement element = elements.get(i);
					
					if (element instanceof MetaFunctionElement && opened == false){
						appendString("<td width=\"80\">&nbsp;</td>"); opened = true;
					}
					
					appendString(generateElementHeader(element));
				}
			decreaseIdent();
			appendString("</tr>");
			appendString("</thead>");
		
			appendString("<tbody>");
			increaseIdent();
				String entryName = doc.getName().toLowerCase();
				appendString("<!--TEST-->");
				appendString("<!--"+doc.getFullName()+"-->");
				appendString("<!--"+doc.getListName()+"-->");
				appendString("<!--"+doc.getName()+"-->");
				appendString("<!--"+doc.getTemporaryVariableName()+"-->");
				appendString("<!--"+doc.getVariableName()+"-->");
				appendString("<ano:iterate name="+quote(doc.getMultiple().toLowerCase())+" type="+quote(ModuleBeanGenerator.getListItemBeanImport(getContext(), doc))+" id="+quote(entryName)+" indexId=\"ind\">");
				increaseIdent();
					appendString("<tr class=\"cmsDocument <%=ind.intValue()%2==0 ? \"lineLight\" : \"lineDark\"%> highlightable\">");
					appendString("<td><input type=\"checkbox\" name=\"pId\" value=\"<ano:write filter=\"false\" name="+quote(entryName)+" property=\"plainId\"/>\"></td>");

					opened = false;
				        for (int i = 0; i < elements.size(); i++) {
				            MetaViewElement element = elements.get(i);

							if (element instanceof MetaFunctionElement && opened == false){
								appendString("<td class=\"no_wrap\">"); 
								opened = true;
							}

				            appendString(generateElement(entryName, element,doc));
				        }
				        		opened = false;
				        		appendString("</td>");
				        		appendString("</tr>");
							decreaseIdent();
							appendString("</ano:iterate>");
						decreaseIdent();
						appendString("</tbody>");
					decreaseIdent();
					appendString("</table>");
					appendString("</div>");
					//main table end
					
					//second paging start
					appendString("<div class=\"paginator\">");
					increaseIdent();
					appendString("<ano:greaterThan name=\"pagingControl\" property=\"numberOfPages\" value=\"1\">");
						appendString("<ul>");
						increaseIdent();
							appendString("<ano:notEqual name=\"pagingControl\" property=\"first\" value=\"true\">");
							appendIncreasedString("<li class=\"prev\"><a href=\"?pageNumber=<ano:write name=\"pagingControl\" property=\"previousPageNumber\"/>\">prev</a></li>");
							appendString("</ano:notEqual>");
							appendString("<ano:iterate id=\"pageElement\" name=\"pagingControl\" property=\"elements\" indexId=\"index\">");
							increaseIdent();
								appendString("<li>");
								increaseIdent();
									appendString("<ano:equal name=\"pageElement\" property=\"separator\" value=\"true\">...</ano:equal>");
									appendString("<ano:equal name=\"pageElement\" property=\"active\" value=\"true\"><ano:write name=\"pageElement\" property=\"caption\"/></ano:equal>");
									appendString("<ano:equal name=\"pageElement\" property=\"active\" value=\"false\"><a href=\"?pageNumber=<ano:write name=\"pageElement\" property=\"caption\"/>\"><ano:write name=\"pageElement\" property=\"caption\"/></a></ano:equal>");
								decreaseIdent();
								appendString("</li>");
							decreaseIdent();
							appendString("</ano:iterate>");
							appendString("<ano:notEqual name=\"pagingControl\" property=\"last\" value=\"true\">");
							appendIncreasedString("<li class=\"next\"><a href=\"?pageNumber=<ano:write name=\"pagingControl\" property=\"nextPageNumber\"/>\">next</a></li>");
							appendString("</ano:notEqual>");
						decreaseIdent();
						appendString("</ul>");
						appendString("</ano:greaterThan>");
						increaseIdent();
							appendString("<select name=\"itemsOnPage\" onchange=\"window.location='?itemsOnPage=' + this.options[this.selectedIndex].value\">");
							appendString("<ano:iterate name=\"PagingSelector\" type=\"java.lang.String\" id=\"option\">");
							appendString("<option value=\"<ano:write name=\"option\"/>\" <ano:equal name=\"option\" value=\"<%=selectedPaging%>\">selected</ano:equal>><ano:write name=\"option\"/> per page</option>");
							appendString("</ano:iterate>");
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
	
	private String generateNewFunction(String entryName, MetaFunctionElement element){
		String path = CMSMappingsConfiguratorGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), CMSMappingsConfiguratorGenerator.ACTION_NEW);
		return "<a href="+quote(generateTimestampedLinkPath(path))+" class=\"button\"><span>"+getImage("add", "add new "+((MetaModuleSection)currentSection).getDocument().getName())+"Add new element</span></a>" ;
	}
	
	private String generateDeleteSelectedFunction(String entryName, MetaFunctionElement element){
		String path = CMSMappingsConfiguratorGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), CMSMappingsConfiguratorGenerator.ACTION_DELETE);
		//todo change images names here getImage("add", "add new....
		return "<a href="+quote(generateTimestampedLinkPath(path))+" id=\"deleteSelectedId\" class=\"button\"><span>"+getImage("add", "add new "+((MetaModuleSection)currentSection).getDocument().getName())+"Delete selected</span></a>" ;
	}

	private String generateElementHeader(MetaViewElement element){
		if (element instanceof MetaFieldElement)
			return generateFieldHeader((MetaFieldElement)element);
		return "";
	}
	
	private String generateFieldHeader(MetaFieldElement element){		
		String name = element instanceof MultilingualFieldElement ? element.getVariableName() : element.getName();
		String header =  "";
		String caption;//field caption to show on table header;
		if (element.getCaption() == null) {
			caption = StringUtils.capitalize(name);
		} else {
			caption = element.getCaption(); 
			if (element instanceof MultilingualFieldElement) {
				caption += "("+((MultilingualFieldElement)element).getLanguage().toUpperCase() + ")";
			}
		}
		if (element.isComparable()){
			String action = CMSMappingsConfiguratorGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), CMSMappingsConfiguratorGenerator.ACTION_SHOW);
			action = action+"?"+ViewConstants.PARAM_SORT_TYPE_NAME+"="+name;
			String actionAZ = action + "&" + ViewConstants.PARAM_SORT_ORDER + "="+ViewConstants.VALUE_SORT_ORDER_ASC; 
			String actionZA = action + "&" + ViewConstants.PARAM_SORT_ORDER + "="+ViewConstants.VALUE_SORT_ORDER_DESC; 
			header += "<ano:equal name="+quote("currentSortCode")+" value="+quote(name+"_"+ViewConstants.VALUE_SORT_ORDER_ASC)+">"+
						"<a href="+quote(generateTimestampedLinkPath(actionZA))+"class=\"down\">"+caption+"</a>"+
					"</ano:equal>"+
					"<ano:equal name="+quote("currentSortCode")+" value="+quote(name+"_"+ViewConstants.VALUE_SORT_ORDER_DESC)+">"+
						"<a href="+quote(generateTimestampedLinkPath(actionAZ))+"class=\"up\">"+caption+"</a>"+
					"</ano:equal>"+
					"<ano:notEqual name="+quote("currentSortCode")+" value="+quote(name+"_"+ViewConstants.VALUE_SORT_ORDER_ASC)+">"+
						"<ano:notEqual name="+quote("currentSortCode")+" value="+quote(name+"_"+ViewConstants.VALUE_SORT_ORDER_DESC)+">"+
							"<a href="+quote(generateTimestampedLinkPath(actionAZ))+">"+caption+"</a>"+
						"</ano:notEqual>"+
					"</ano:notEqual>";
		} else {
			header = caption;//StringUtils.capitalize(name);
		}
		String displayLanguageCheck = "";
		if(element instanceof MultilingualFieldElement) {
			MultilingualFieldElement multilangualElement = (MultilingualFieldElement) element;
			displayLanguageCheck = "class=\"lang_hide lang_"+multilangualElement.getLanguage()+"\" <ano:equal name=\"display" + multilangualElement.getLanguage() + "\" value=\"false\">style=\"display:none\"</ano:equal>";
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
			displayLanguageCheck = "class=\"lang_hide lang_"+multilangualElement.getLanguage()+"\" <ano:equal name=\"display" + multilangualElement.getLanguage() + "\" value=\"false\">style=\"display:none\"</ano:equal>";
		}
		
		return "<!--"+quote(entryName)+"_____"+elementName+"--><td " + displayLanguageCheck + "><ano:write filter=\"false\" name="+quote(entryName)+" property=\""+elementName+"\"/></td>";
		//return "<td><ano:write name="+quote(entryName)+" property=\""+element.getName()+"\"/></td>";
	}
	
	private String generateImage(String entryName, MetaFieldElement element){
		String ret = "";
		ret += "<td>";
		ret += "<ano:equal name="+quote(entryName)+" property="+quote(element.getName())+" value="+quote("")+">";
		ret += "none";
		ret += "</ano:equal>";
		ret += "<ano:notEqual name="+quote(entryName)+" property="+quote(element.getName())+" value="+quote("")+">";
		String imagePath = "getFile?pName=<ano:write name="+quote(entryName)+" property="+quote(element.getName())+"/>";
		ret += "<a href="+quote(imagePath)+" target="+quote("_blank")+"><img src="+quote(imagePath)+ " class=\"thumbnail\"></a>";
		ret += "</ano:notEqual>";
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
		
		if (element.getName().equals("preview"))
			return "<a target=\"_blank\" href=../<ano:write filter=\"false\" name=\"pagex\" property=\"nameForSorting\"/>.html>"+getPreviewImage()+"</a>";

		if (element.getName().equals("edit"))
			return getEditFunction(entryName, element);
			
		if (element.getName().equals("duplicate"))
			return getDuplicateFunction(entryName, element);

        if (element.getName().equals("lock") && StorageType.CMS.equals(doc.getParentModule().getStorageType()))
            return getLockFunction(entryName, element);
        
        if (element.getName().equals("unlock") && StorageType.CMS.equals(doc.getParentModule().getStorageType()))
            return getUnLockFunction(entryName, element);

        return "";
		//return "<td><ano:write name="+quote(entryName)+" property=\""+element.getName()+"\"/></td>";
	}
	
	private String getCustomFunction(String entryName, MetaCustomFunctionElement element){
		String caption = element.getCaption();
		String link = element.getLink();
		link = StringUtils.replace(link, "$plainId", "<ano:write name="+quote(entryName)+" property=\"plainId\"/>");
		return "<a href="+quote(generateTimestampedLinkPath(link))+">"+caption+"</a>";
	}


	/**
     * Lock link for List show!
     */
    private String getUnLockFunction(String entryName, MetaFunctionElement element) {
        String path = CMSMappingsConfiguratorGenerator.getPath(((MetaModuleSection) currentSection).getDocument(), CMSMappingsConfiguratorGenerator.ACTION_UNLOCK);
        path += "?pId=<ano:write name=" + quote(entryName) + " property=\"plainId\"/>";
        path+="&nextAction=showList";
		String alt = "Locked by: <ano:write name="+quote(entryName)+" property="+quote(LockableObject.INT_LOCKER_ID_PROPERTY_NAME)+"/>, at: <ano:write name="+quote(entryName)+" property="+quote(LockableObject.INT_LOCKING_TIME_PROPERTY_NAME)+"/>";
        String link = "<a href=\"#\" onClick= "+quote("lightbox('"+alt+"<br /> Unlock "+((MetaModuleSection)currentSection).getDocument().getName()+" with id: <ano:write name="+quote(entryName)+" property=\"id\"/>?','<ano:tslink>"+path+"</ano:tslink>');")+">"+getUnLockImage(alt)+"</a>";
        String result  = "<ano:equal name=" + quote(entryName) + " property=" + quote(LockableObject.INT_LOCK_PROPERTY_NAME) + " value=" + quote("true") + ">";
        result+=link;
        result+= "</ano:equal>";
        return result;
    }

    /**
     * UnLock link for List show!
     */
    private String getLockFunction(String entryName, MetaFunctionElement element) {
        String path = CMSMappingsConfiguratorGenerator.getPath(((MetaModuleSection) currentSection).getDocument(), CMSMappingsConfiguratorGenerator.ACTION_LOCK);
        path += "?pId=<ano:write name=" + quote(entryName) + " property=\"plainId\"/>";
        path+="&nextAction=showList";
        String link =  "<a href=\"#\" onClick= "+quote("lightbox('Lock "+
				((MetaModuleSection)currentSection).getDocument().getName()+" with id: <ano:write name="+quote(entryName)+" property=\"id\"/>?','<ano:tslink>"+path+"</ano:tslink>');")+">"+getLockImage()+"</a>" ;
        String result  = "<ano:equal name=" + quote(entryName) + " property=" + quote(LockableObject.INT_LOCK_PROPERTY_NAME) + " value=" + quote("false") + ">";
        result+=link;
        result+= "</ano:equal>";
        return result;
    }

	private String getDuplicateFunction(String entryName, MetaFunctionElement element){
		String path = CMSMappingsConfiguratorGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), CMSMappingsConfiguratorGenerator.ACTION_DUPLICATE);
		path += "?pId=<ano:write name="+quote(entryName)+" property=\"plainId\"/>";
		
		return "<a href="+quote("<ano:tslink>"+path+"</ano:tslink>")+">"+getDuplicateImage()+"</a>" ;
	}

	private String getVersionFunction(String entryName, MetaFunctionElement element){
		String path = CMSMappingsConfiguratorGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), CMSMappingsConfiguratorGenerator.ACTION_VERSIONINFO);
		path += "?pId=<ano:write name="+quote(entryName)+" property=\"plainId\"/>";
		
		return "<a href="+quote("<ano:tslink>"+path+"</ano:tslink>")+">"+getImage("version", "LastUpdate: <ano:write name="+quote(entryName)+" property="+quote("documentLastUpdateTimestamp")+"/>")+"</a>" ;
	}

	private String getDeleteFunction(String entryName, MetaFunctionElement element){
		String path = CMSMappingsConfiguratorGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), CMSMappingsConfiguratorGenerator.ACTION_DELETE);
		path += "?pId=<ano:write name="+quote(entryName)+" property=\"plainId\"/>";
		
		return "<a href="+quote("<ano:tslink>"+path+"</ano:tslink>")+">"+getDeleteImage()+"</a>" ;
	}

	private String getDeleteWithConfirmationFunction(String entryName, MetaFunctionElement element){
		String path = CMSMappingsConfiguratorGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), CMSMappingsConfiguratorGenerator.ACTION_DELETE);
		path += "?pId=<ano:write name="+quote(entryName)+" property=\"plainId\"/>";
		return "<a href=\"#\" onClick="+quote("lightbox('Really delete "+
				((MetaModuleSection)currentSection).getDocument().getName()+" with id: <ano:write name="+quote(entryName)+" property=\"id\"/>?','<ano:tslink>"+path+"</ano:tslink>');")+">"+getDeleteImage()+"</a>" ;
	}

	private String getEditFunction(String entryName, MetaFunctionElement element){
		String path = CMSMappingsConfiguratorGenerator.getPath(((MetaModuleSection)currentSection).getDocument(), CMSMappingsConfiguratorGenerator.ACTION_EDIT);
		path += "?pId=<ano:write name="+quote(entryName)+" property=\"plainId\"/>";
		
		return "<a href="+quote("<ano:tslink>"+path+"</ano:tslink>")+">"+getEditImage()+"</a>" ;
	}
	
}
