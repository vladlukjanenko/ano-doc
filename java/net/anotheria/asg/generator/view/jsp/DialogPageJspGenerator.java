package net.anotheria.asg.generator.view.jsp;

import net.anotheria.asg.data.LockableObject;
import net.anotheria.asg.generator.GeneratedJSPFile;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.*;
import net.anotheria.asg.generator.view.CMSMappingsConfiguratorGenerator;
import net.anotheria.asg.generator.view.action.ModuleBeanGenerator;
import net.anotheria.asg.generator.view.meta.*;
import net.anotheria.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates the jsps for the edit view.
 *
 * @author another
 */
public class DialogPageJspGenerator extends AbstractJSPGenerator {

	/**
	 * Currently generated section.
	 */
	private MetaSection currentSection;
	/**
	 * Currently generated dialog.
	 */
	private MetaDialog currentDialog;
	/**
	 * Is need render JS for enabling DateTime widgets.
	 */
	private boolean isNeedEnableDateTimeWidgets = false;

	public GeneratedJSPFile generate(MetaSection metaSection, MetaDialog dialog, MetaModuleSection section, MetaView view) {
		this.currentSection = metaSection;
		this.currentDialog = dialog;

		GeneratedJSPFile jsp = new GeneratedJSPFile();
		startNewJob(jsp);
		jsp.setName(getDialogName(dialog, section.getDocument()));
		jsp.setPackage(getContext().getJspPackageName(section.getModule()));

		resetIdent();
		currentDialog = dialog;

		append(getBaseJSPHeader());

		appendGenerationPoint("generateDialog");
		appendString("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"");
		appendString("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
		appendString("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		increaseIdent();
		appendString("<head>");
		increaseIdent();
		appendString("<title>" + dialog.getTitle() + "</title>");
		generatePragmas(view);
		// appendString("<link href=\""+getCurrentCSSPath("admin.css")+"\" rel=\"stylesheet\" type=\"text/css\">");
		// *** CMS2.0 START ***

		appendString("<link rel=" + quote("stylesheet") + " type=" + quote("text/css") + " href=" + quote(getCurrentYUIPath("core/build/fonts/fonts-min.css")) + " />");
		appendString("<link rel=" + quote("stylesheet") + " type=" + quote("text/css") + " href=" + quote(getCurrentYUIPath("core/build/assets/skins/sam/skin.css")) + " />");
		appendString("<link rel=" + quote("stylesheet") + " type=" + quote("text/css") + " href=" + quote(getCurrentYUIPath("core/build/container/assets/skins/sam/container.css")) + " />");
		appendString("<link href=\"" + getCurrentCSSPath("newadmin.css") + "\" rel=\"stylesheet\" type=\"text/css\"/>");
		appendString("<link href=\"" + getCurrentCSSPath("fileuploader.css") + "\" rel=\"stylesheet\" type=\"text/css\"/>");
//		appendString("<link rel=" + quote("stylesheet") + " type=" + quote("text/css") + " href=" + quote(getCurrentCSSPath("jquery-ui-1.8.18.custom.css")) + " />");
		appendString("<link rel=" + quote("stylesheet") + " type=" + quote("text/css") + " href=" + quote(getCurrentCSSPath("jquery-ui-1.9.1.custom.min.css")) + " />");

		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/yahoo-dom-event/yahoo-dom-event.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/container/container-min.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/menu/menu-min.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/element/element-min.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/button/button-min.js")) + "></script>");

		// appendString("<script type=" + quote("text/javascript") + " src=" +
		// quote(getCurrentYUIPath("core/build/animation/animation-min.js")) +
		// "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/datasource/datasource-min.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/autocomplete/autocomplete-min.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("core/build/dragdrop/dragdrop-min.js")) + "></script>");
		appendString("<script type=" + quote("text/javascript") + " src=" + quote(getCurrentYUIPath("anoweb/widget/ComboBox.js")) + "></script>");
		// *** CMS2.0 FINISH ***

		// *** CMS3.0 START ***
//		appendString("<script type=\"text/javascript\" src=\"" + getCurrentJSPath("jquery-1.4.min.js") + "\"></script>");
//		appendString("<script type=\"text/javascript\" src=\"" + getCurrentJSPath("jquery-1.5.1.min.js") + "\"></script>");
//		appendString("<script type=\"text/javascript\" src=\"" + getCurrentJSPath("jquery-1.6.2.min.js") + "\"></script>");
        appendString("<script type=\"text/javascript\" src=\"" + getCurrentJSPath("jquery-1.8.2.js") + "\"></script>");
//		appendString("<script type=\"text/javascript\" src=\"" + getCurrentJSPath("jquery-ui-1.8.18.custom.min.js") + "\"></script>");
		appendString("<script type=\"text/javascript\" src=\"" + getCurrentJSPath("jquery-ui-1.9.1.custom.min.js") + "\"></script>");

		appendString("<script type=\"text/javascript\" src=\"" + getCurrentJSPath("datetimpicker.js") + "\"></script>");
		appendString("<script type=\"text/javascript\" src=\"" + getCurrentJSPath("anofunctions.js") + "\"></script>");
		appendString("<script type=\"text/javascript\" src=\"" + getCurrentJSPath("fileuploader.js") + "\"></script>");
		appendString("<script type=\"text/javascript\" src=\"" + getCurrentJSPath("cms-tooltip.js") + "\"></script>");
		appendString("<script type=\"text/javascript\" src=\"" + getCurrentJSPath("tiny_mce/tiny_mce.js") + "\"></script>");
		// *** CMS3.0 FINISH ***


		if(dialog.getJavascript() != null) {
			appendString("<script type=\"text/javascript\" src=\"" + getCurrentJSPath(dialog.getJavascript()) + "\"></script>");
		}

		decreaseIdent();
		appendString("</head>");
		appendString("<body>");
		appendString("<jsp:include page=\"" + getTopMenuPage() + "\" flush=\"true\"/>");
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
		List<MetaViewElement> elements = createMultilingualList(dialog.getElements(), section.getDocument());
		for (int i = 0; i < elements.size(); i++) {
			MetaViewElement element = elements.get(i);
			while (elements.get(i) instanceof MultilingualFieldElement) {
				String caption = element.getCaption() != null ? element.getCaption() : element.getName();
				appendString("<ano:equal name=" + quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, section.getDocument())) + " property="
						+ quote(ModuleBeanGenerator.FIELD_ML_DISABLED) + " value=" + quote("true") + ">");
				//please don't add any style to this <a> tag - JS validation can remove it
				appendString("<li><a href=\"#" + element.getName() + "DEF" + "\" id=\""+element.getName()+"Anchor\" ${validationErrors."+element.getName()+" eq null ? '':'style=\"color:red\"'}>" + caption + "</a></li>");
				appendString("</ano:equal>");
				appendString("<ano:notEqual name=" + quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, section.getDocument())) + " property="
						+ quote(ModuleBeanGenerator.FIELD_ML_DISABLED) + " value=" + quote("true") + ">");
				appendString("<li>");
				increaseIdent();
				String lang = getElementLanguage(element);
				String nameWithLang = section.getDocument().getField(element.getName()).getName(lang);
				//please don't add any style to this <a> tag - JS validation can remove it
				appendString("<a href=\"#" + nameWithLang + "\" id=\""+nameWithLang+"Anchor\" ${validationErrors."+element.getName()+" eq null ? '':'style=\"color:red\"'}>" + caption
						+ "</a><a href=\"javascript:void(0);\" class=\"open_pop\">&nbsp;&nbsp;&nbsp;</a>");
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
				for (String sl : GeneratorDataRegistry.getInstance().getContext().getLanguages()) {
					appendString("<li class=\"lang_" + sl + " lang_hide\"><a href=\"#" + section.getDocument().getField(element.getName()).getName(sl) + "\">"
							+ sl + "</a></li>");
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
				appendString("</ano:notEqual>");
			}
			if (element instanceof MetaFieldElement) {
				String caption = element.getCaption() != null ? element.getCaption() : element.getName();
				//please don't add any style to this <a> tag - JS validation can remove it
				appendString("<li><a href=\"#" + element.getName() + "\" id=\""+element.getName()+"Anchor\" ${validationErrors."+element.getName()+" eq null ? '':'style=\"color:red\"'}>" + caption + "</a></li>");
			}
		}
		decreaseIdent();
		appendString("</ul>");
		appendString("<div class=\"clear\"><!-- --></div>");
		decreaseIdent();
		for (int i = 0; i < elements.size(); i++) {
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
			int colspan = 2;
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

        String entryName = quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection) metaSection).getDocument()));
        if(isAppropriateModule(section)){

            if (!section.getDocument().getName().equalsIgnoreCase("RedirectUrl") && !section.getDocument().getName().equalsIgnoreCase("EntryPoint")){
                String elementName = section.getDocument().getName().equalsIgnoreCase("NaviItem") ? "<ano:write name="+entryName+" property=\"nameEN\"/>" : "<ano:write name="+entryName+" property=\"name\"/>";
                appendString("<a class=\"button\" id=\"display_all_usages\" href=\"/cms/showUsages\"><span><img src=\"/cms_static/img/usage_white.png\" alt=\"add\">Show usages</span></a>");
                appendString("<input class=\"showUsagesDocName\" type=\"hidden\" name=\"docName\" value=\""+section.getDocument().getName()+"\"/>");
                appendString("<input class=\"showUsagesPId\" type=\"hidden\" name=\"pId\" value=\"<ano:write name="+entryName+" property=\"id\"/>\"/>");
                appendString("<br/><br/>");
                appendString("<div id=\"all_usages_of_element\" style=\"display: none;\" title=\"Usages of this "+section.getDocument().getName()+"["+elementName+"]\"></div>");
            }
        }


		if (StorageType.CMS.equals(((MetaModuleSection) metaSection).getDocument().getParentModule().getStorageType())) {

			String result = "<ano:equal name=" + entryName + " property=" + quote(LockableObject.INT_LOCK_PROPERTY_NAME) + " value=" + quote("false") + "> \n";
			String path = CMSMappingsConfiguratorGenerator.getPath(((MetaModuleSection) metaSection).getDocument(), CMSMappingsConfiguratorGenerator.ACTION_LOCK);
			path += "?pId=<ano:write name=" + entryName + " property=\"id\"/>" + "&nextAction=showEdit";
			result += "<a href=\"#\" onClick= "
					+ quote("lightbox('All unsaved data will be lost!!!<br /> Really lock  "
							+ CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection) metaSection).getDocument())
							+ " with id: <ano:write name=" + entryName + " property=\"id\"/>?','<ano:tslink>" + path + "</ano:tslink>');") + ">" + getLockImage()
					+ "&nbsp;Lock</a>";
			result += "</ano:equal>";
			appendString(result);

			appendString("<ano:equal name=" + entryName + " property=" + quote(LockableObject.INT_LOCK_PROPERTY_NAME) + " value=" + quote("true") + ">");

			path = CMSMappingsConfiguratorGenerator.getPath(((MetaModuleSection) metaSection).getDocument(), CMSMappingsConfiguratorGenerator.ACTION_UNLOCK);
			path += "?pId=<ano:write name=" + entryName + " property=\"id\"/>" + "&nextAction=showEdit";

			String alt = ((MetaModuleSection) metaSection).getDocument().getName() + " is locked by: <ano:write name=" + entryName + " property="
					+ quote(LockableObject.INT_LOCKER_ID_PROPERTY_NAME) + "/>, at: <ano:write name=" + entryName + " property="
					+ quote(LockableObject.INT_LOCKING_TIME_PROPERTY_NAME) + "/>";

			appendString("<a href=\"#\" onClick= "
					+ quote("lightbox('" + alt + "<br /> Unlock " + ((MetaModuleSection) metaSection).getDocument().getName()
							+ " with id: <ano:write name=" + entryName + " property=\"id\"/>?','<ano:tslink>" + path + "</ano:tslink>');") + ">"
					+ getUnLockImage(alt) + "" + " Unlock</a><span>&nbsp;Locked by <b><ano:write name="
					+ quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection) metaSection).getDocument())) + " property="
					+ quote(LockableObject.INT_LOCKER_ID_PROPERTY_NAME) + "/></b>");
			appendString("at:  <b><ano:write name="
					+ quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection) metaSection).getDocument())) + " property="
					+ quote(LockableObject.INT_LOCKING_TIME_PROPERTY_NAME) + "/></b></span>");
			appendString("</ano:equal>");
		}

		appendString("<form class=\"cmsDialog\" name=" + quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection) metaSection).getDocument()))
				+ " method=\"post\" action=" + quote(CMSMappingsConfiguratorGenerator.getPath(section.getDocument(), CMSMappingsConfiguratorGenerator.ACTION_UPDATE)) + ">");
		appendIncreasedString("<input type=" + quote("hidden") + " name=" + quote("_ts") + " value=" + quote("<%=System.currentTimeMillis()%>") + ">");
		appendIncreasedString("<input type=" + quote("hidden") + " name=" + quote(ModuleBeanGenerator.FLAG_FORM_SUBMITTED) + " value=" + quote("true") + ">");
		appendIncreasedString("<input type=" + quote("hidden") + " name=" + quote("nextAction") + " value=" + quote("close") + ">");
		appendIncreasedString("<input type=" + quote("hidden") + " name=" + quote("fileName") + " value=" + quote("") + ">");
		appendIncreasedString("<input type=" + quote("hidden") + " name=" + quote("fieldName") + " value=" + quote("") + ">");

		appendIncreasedString("<script type=\"text/javascript\">validators = new Array();validateForm = function() {var result = true;for (i in validators) {try{result = result & validators[i].validate();}catch(e){}}return result;}</script>");

		appendString("<table cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" border=\"0\">");
		appendString("<tbody>");
		increaseIdent();
		appendString("<ano:write name=\"description.null\" ignore=\"true\"/>");
		decreaseIdent();
		appendString("<tr>");
		increaseIdent();
		appendString("<td align=\"left\">");
		appendString("<div class=\"clear\"><!-- --></div>");
		// appendString("</ano:equal>");
		// UNLOCK HERE!!!!!
		appendString("</td>");
		decreaseIdent();
		appendString("</tr>");

		// *** CMS2.0 START ***

		List<MetaViewElement> richTextElementsRegistry = new ArrayList<MetaViewElement>();
		List<String> linkElementsRegistry = new ArrayList<String>();
		// *** CMS2.0 FINISH ***

		MetaDocument document = ((MetaModuleSection) metaSection).getDocument();
		for (int i = 0; i < elements.size(); i++) {
			MetaViewElement element = elements.get(i);
			// *** CMS2.0 START ***
			if (element instanceof MetaListElement) {
				// now we draw control elements upside our page
				i++;
				continue;
			}
			if (element instanceof MetaFieldElement) {
				MetaProperty p = document.getField(element.getName());
				if (element.isRich() && p.getType() == MetaProperty.Type.TEXT)
					richTextElementsRegistry.add(element);

				if (p.isLinked())
					linkElementsRegistry.add(element.getName());
			}
			// *** CMS2.0 FINISH ***

			String lang = getElementLanguage(element);

			// ALTERNATIVE EDITOR FOR DISABLED MODE
			if (lang != null && lang.equals(GeneratorDataRegistry.getInstance().getContext().getDefaultLanguage())) {
				appendString("<ano:equal name="
						+ quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection) metaSection).getDocument())) + " property="
						+ quote(ModuleBeanGenerator.FIELD_ML_DISABLED) + " value=" + quote("true") + ">");
				appendString("<td align=\"right\"> <a id=\"" + element.getName() + "DEF\" name=\"" + element.getName() + "DEF\"></a>");
				increaseIdent();
				String name = section.getDocument().getField(element.getName()).getName();
//				if (name == null || name.length() == 0)
//					name = "&nbsp;";
				String caption = (element.getCaption() != null ? element.getCaption() : name) + "(<b>DEF</b>)";
				appendString(caption);
				if (element.isRich()) {
					appendString("<div class=\"clear\"></div>");
					appendString("<a href=\"javascript:;\" onmousedown=\"tinyMCE.execCommand('mceRemoveControl', true, '" + section.getDocument().getField(element.getName()).getName(lang)
							+ "_ID');\" class=\"rich_on_off\" style=\"display:none;\">off</a>");
					appendString("<a href=\"javascript:;\" onmousedown=\"tinyMCE.execCommand('mceAddControl', true, '" + section.getDocument().getField(element.getName()).getName(lang)
							+ "_ID');\" class=\"rich_on_off\">on</a>");
					appendString("<span class=\"rich_on_off\">Rich:</span>");
				}
				if (element.getDescription() != null)
					append("<a href=\"#\" class=\"showTooltip\"><img src=\"../cms_static/img/tooltip.gif\" alt=\"\"/>",element.getDescription(),"</a>");
				decreaseIdent();
				append("&nbsp;");
				appendString("</td>");
				appendString("<td align=\"left\">");
				if (element.getName() != null){
					String inputName = section.getDocument().getField(element.getName()).getName(lang);
					generateValidationParts(element, inputName, document.getField(element.getName()));
				}
				append(getElementEditor(section.getDocument(), element));
				appendString("&nbsp;<i><ano:write name=\"description." + element.getName() + "\" ignore=\"true\"/></i>");
				appendString("</td>");
				appendString("</tr>");
				appendString("</ano:equal>");
			}// END ALTERNATIVE EDITOR FOR MULTILANG DISABLED FORM

			if (lang != null)
				appendString("<ano:equal name="
						+ quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection) metaSection).getDocument())) + " property="
						+ quote(ModuleBeanGenerator.FIELD_ML_DISABLED) + " value=" + quote("false") + ">");

			// Language Filtering Settings
			String displayLanguageCheck = "";
			if (element instanceof MultilingualFieldElement) {
				MultilingualFieldElement multilangualElement = (MultilingualFieldElement) element;
				displayLanguageCheck = "<ano:equal name=\"display" + multilangualElement.getLanguage()
						+ "\" value=\"false\"> style=\"display:none\"</ano:equal>";
				appendString("<tr class=\"cmsProperty lang_hide lang_" + multilangualElement.getLanguage() + "\"" + displayLanguageCheck + ">");
			} else {
				appendString("<tr class=\"cmsProperty\">");
			}
			increaseIdent();
			increaseIdent();
			appendString("<td align=\"right\">");
			String name = (lang == null) ? element.getName() : section.getDocument().getField(element.getName()).getName(lang);
			if (name == null || name.length() == 0) {
//				appendString("<a  name=\"" + name + "\"></a>");
				name="&nbsp;";
			} else {
				appendString("<a id=\"" + name + "\" name=\"" + name + "\"></a>");
			}
			String caption = element.getCaption();
			if (caption == null) {
				caption = name;
			} else if (lang != null) {
				caption += "("+StringUtils.capitalize(lang)+")";
			}
			appendString(caption);
			if (element.getDescription() != null)
				append("<a href=\"#\" class=\"showTooltip\"><img src=\"../cms_static/img/tooltip.gif\" alt=\"\"/>",element.getDescription(),"</a>");
			append("&nbsp;");
			decreaseIdent();
			if (element.isRich()) {
				appendString("<div class=\"clear\"></div>");
				appendString("<a href=\"javascript:;\" onmousedown=\"tinyMCE.execCommand('mceRemoveControl', true, '" + section.getDocument().getField(element.getName()).getName(lang)
							+ "_ID');\" class=\"rich_on_off\" style=\"display:none;\">off</a>");
				appendString("<a href=\"javascript:;\" onmousedown=\"tinyMCE.execCommand('mceAddControl', true, '" + section.getDocument().getField(element.getName()).getName(lang)
							+ "_ID');\" class=\"rich_on_off\">on</a>");
				appendString("<span class=\"rich_on_off\">Rich:</span>");
			}
			appendString("</td>");
			appendString("<td align=\"left\">");
			increaseIdent();
			if (element.getName() != null)
				generateValidationParts(element, name, document.getField(element.getName()));
			append(getElementEditor(section.getDocument(), element));
			appendString("&nbsp;<i><ano:write name=\"description." + element.getName() + "\" ignore=\"true\"/></i>");
			decreaseIdent();
			appendString("</td>");
			decreaseIdent();
			appendString("</tr>");

			if (lang != null)
				appendString("</ano:equal>");
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

		appendString("<div class=\"generated\"><span><ano:write name=" + quote("objectInfoString") + "/></span>");

		// Link to the Links to Me page
		appendString("<ano:present name=" + quote("linksToMe") + " scope=" + quote("request") + ">");
		String linksToMePagePath = CMSMappingsConfiguratorGenerator.getPath(section.getDocument(), CMSMappingsConfiguratorGenerator.ACTION_LINKS_TO_ME) + "?pId=<ano:write name="
				+ quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection) metaSection).getDocument())) + " property=\"id\"/>";
		appendString("<a href=" + quote("<ano:tslink>" + linksToMePagePath + "</ano:tslink>") + ">Show direct links to  this document</a>");
		appendString("</ano:present>");
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
        addDialogContentToDisplayUsagesOfElement(section);
		generateRichTextEditorJS(section.getDocument(), richTextElementsRegistry);
		generateLinkElementEditorJS(section.getDocument(), linkElementsRegistry);
		generateDateTimeWidgetJS();

		decreaseIdent();
		appendString("</html>");
		appendString("<!-- / generated by JspMafViewGenerator.generateDialog -->");

		return jsp;
	}

	private boolean isAppropriateModule(MetaModuleSection section) {
		return section.getModule().getName().equalsIgnoreCase("aswebdata") ||
                section.getModule().getName().equalsIgnoreCase("aslayoutdata") ||
                section.getModule().getName().equalsIgnoreCase("asgenericdata") ||
                section.getModule().getName().equalsIgnoreCase("ascustomdata") ||
                section.getModule().getName().equalsIgnoreCase("assitedata") ||
				section.getModule().getName().equalsIgnoreCase("asresourcedata");
	}

	private void generateValidationParts(MetaViewElement element, String name, MetaProperty p) {
		if (element.isJSValidated()){
			boolean isTextField = element.isRich() || p.getType() == MetaProperty.Type.TEXT;
			appendString("<script type=\"text/javascript\">");
			appendString("var temp = {validate : function(){ try{");
			String valueSelector;
			if (isTextField) {
				valueSelector = getTextValueSelector(name);
			} else if (p.isLinked()) {
				valueSelector = getLinkValueSelector(name);
			} else {
				valueSelector = getValueSelector(name);
			}
			appendString("var value = ", valueSelector);
			for (MetaValidator validator : element.getValidators()){
				String jsValidation = validator.getJsValidation();
				if (StringUtils.isEmpty(jsValidation))
					continue;
				jsValidation = jsValidation.trim();
				if (jsValidation.endsWith(";"))
					jsValidation =jsValidation.substring(0, jsValidation.length() - 1);
				appendString("if (!("+jsValidation+")) {");
				increaseIdent();
				appendString("$('#showError"+name+" span').text(\""+validator.getDefaultError()+"\");");
				appendString("$('#showError"+name+"').show();");
				appendString("$('#"+name+"Anchor').css('color','red');");
				appendString("return false;");
				decreaseIdent();
				appendString("} else {");
				increaseIdent();
				appendString("$('#showError"+name+"').hide();");
				appendString("$('#"+name+"Anchor').removeAttr('style');");
				closeBlock("validation end");
			}
			appendString("}catch(e){}return true;}};");
			appendString("if(validators instanceof Array){validators[validators.length] = temp;}");
			appendString("</script>");
		}
		appendString("<div class=\"showError\" id=\"showError"+name+"\" ${validationErrors."+name+" eq null ? 'style=\"display:none\"' : ''}><div>");
		appendString("<span>${validationErrors[\""+name+"\"].message}</span>");
		appendString("<img alt=\"\" src=\"../cms_static/img/error_arrow.gif\"/>");
		appendString("</div></div>");
	}

	private String getValueSelector(String fieldName) {
		return "$('input[name="+fieldName+"]').val();";
	}

	private String getLinkValueSelector(String fieldName) {
		return "$('input[id="+StringUtils.capitalize(fieldName)+"CurrentValueInput]').val();";
	}

	private String getTextValueSelector(String fieldName) {
		return "(tinyMCE.get('"+fieldName+"_ID') == null || tinyMCE.get('"+fieldName+"_ID').isHidden()) ? $('textarea[name="+fieldName+"]').val() : tinyMCE.get('"+fieldName+"_ID').getContent();";
	}

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
		ret += "(<i>old:</i>&nbsp;<ano:write property="+quote(p.getName()+"CurrentValue"+(lang==null ? "":lang))+" name="+quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" filter="+quote("false")+"/>)";
		 */

		//*** CMS2.0 START ***
		String editLink = "";
	       if (p.getName().equalsIgnoreCase("handler")) {
	           String anoNotEqualNoneStartTag = "<ano:notEqual value=\"none\" property="+quote(p.getName()+"IdOfCurrentValue")+" name="+quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+">";
	           String anoNotEqualNoneEndTag = "</ano:notEqual>";
	           String anoNotEmptyStartTag = "<ano:notEmpty property="+quote(p.getName()+"IdOfCurrentValue")+" name="+quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+">";
	           String anoNotEmptyEndTag = "</ano:notEmpty>";

	           String path = "ascustomdataCustomBoxHandlerDefEdit"+"?pId="+"<ano:write property="+quote(p.getName()+"IdOfCurrentValue")+" name="+quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" filter="+quote("false")+"/>";

	           editLink = anoNotEmptyStartTag+
	                               anoNotEqualNoneStartTag+
	                                   "<i><a href="+quote("<ano:tslink>"+path+"</ano:tslink>")+">"+" Edit handler"+"</a></i>" +
	                               anoNotEqualNoneEndTag+
	                         anoNotEmptyEndTag;
	       }
	       if (p.getName().equalsIgnoreCase("type")) {
	           String anoNotEqualNoneStartTag = "<ano:notEqual value=\"none\" property="+quote(p.getName()+"IdOfCurrentValue")+" name="+quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+">";
	           String anoNotEqualNoneEndTag = "</ano:notEqual>";
	           String anoNotEmptyStartTag = "<ano:notEmpty property="+quote(p.getName()+"IdOfCurrentValue")+" name="+quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+">";
	           String anoNotEmptyEndTag = "</ano:notEmpty>";

	           String path = "ascustomdataCustomBoxTypeEdit"+"?pId="+"<ano:write property="+quote(p.getName()+"IdOfCurrentValue")+" name="+quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" filter="+quote("false")+"/>";

	           editLink = anoNotEmptyStartTag
	                               +anoNotEqualNoneStartTag+
	                                   "<i><a href="+quote("<ano:tslink>"+path+"</ano:tslink>")+">"+" Edit type"+"</a></i>" +
	                               anoNotEqualNoneEndTag+
	                         anoNotEmptyEndTag;
	       }
			//quoted "name" attr in em, cause w3c validation says it's error
			ret += "<em id="+quote(StringUtils.capitalize(p.getName())+"CurrentValue")+" name="+quote(p.getName())+" class=\"selectBox\"></em><div id=\""+StringUtils.capitalize(p.getName(lang))+"Selector\"></div>";
			ret += " (<i>old:</i>&nbsp;<ano:write property="+quote(p.getName()+"CurrentValue")+" name="+quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" filter="+quote("false")+"/>"+
	                ")&nbsp;"+editLink;

		//*** CMS2.0 FINISH ***

		return ret;
	}

	private String getEnumerationEditor(MetaFieldElement element, MetaProperty p){
		String ret = "";
		String lang = getElementLanguage(element);

		ret += "<select name=\""+p.getName(lang)+"\">";
		ret += "<ano:iterate indexId=\"index\" id=\"element\" property=\""+ p.getName() +"Collection\" name=\""+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument())+ "\">";
		ret += "<option value=\"<ano:write name=\"element\" property=\"value\"/>\" <ano:equal name=\""+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument())+ "\" property=\""+p.getName()+"CurrentValue"+(lang==null ? "":lang)+"\" value=\"${element.label}\">selected</ano:equal>><ano:write name=\"element\" property=\"label\"/></option>";
		ret += "</ano:iterate>";
		ret += "</select>";


		ret += "&nbsp;";
		ret += "(<i>old:</i>&nbsp;<ano:write property="+quote(p.getName()+"CurrentValue"+(lang==null ? "":lang))+" name="+quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" filter="+quote("false")+"/>)";

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


		switch (p.getType()) {
		case STRING:
			return getStringEditor(element, p);
		case PASSWORD:
            return getPasswordEditor(element, p);
		case TEXT:
			return getTextEditor(element, p);
		case LONG:
			return getStringEditor(element, p);
		case INT:
			return getStringEditor(element, p);
		case DOUBLE:
			return getStringEditor(element, p);
		case FLOAT:
			return getStringEditor(element, p);
		case BOOLEAN:
			return getBooleanEditor(element, p);
		case IMAGE:
			return getImageEditor(element, p);
		default:
			return p.getType().getName();
		}

	}

	private String getContainerLinkEditor(MetaFieldElement element, MetaContainerProperty p){
		String ret = "";
		String lang = getElementLanguage(element);
		String name = quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()));
		ret += "<ano:equal name="+name+" property="+quote("id")+" value="+quote("")+">";
		ret += "none";
		ret += "</ano:equal>";
		ret += "<ano:notEqual name="+name+" property="+quote("id")+" value="+quote("")+">";
		ret += "<ano:write name="+name+" property="+quote(p.getName(lang))+"/>";
		ret += "&nbsp;";
		ret += "element";
		ret += "<ano:notEqual name="+name+" property="+quote(p.getName(lang))+" value="+quote("1")+">";
		ret += "s";
		ret += "</ano:notEqual>";
		ret += "&nbsp;";
		String actionName = CMSMappingsConfiguratorGenerator.getContainerPath(((MetaModuleSection)currentSection).getDocument(), p, CMSMappingsConfiguratorGenerator.ACTION_SHOW);
		actionName += "?ownerId=<ano:write name="+name+" property="+quote("id")+"/>";
		ret += "<a href="+quote(actionName)+">&nbsp;&raquo&nbsp;Edit&nbsp;</a>";
		ret += "</ano:notEqual>";

		return ret;
	}



	private String getImageEditor(MetaFieldElement element, MetaProperty p){
		String beanName = CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument());
		String propertyWriter = "<ano:write name="+quote(beanName)+" property="+quote(p.getName()) + "/>";
		String ret ="";
		ret += "<ano:present name="+quote(beanName)+" property="+quote(p.getName()) + ">\r";
		ret += "<a target=\"_blank\" href=\"getFile?pName=" + propertyWriter + "\"><img class=\"thumbnail\" alt=" + quote(propertyWriter) + " src=\"getFile?pName=" + propertyWriter + "\"/></a>\r";
		ret += "</ano:present>\r";
        ret += getUpdateAndDeleteFileAndStayFunction(((MetaModuleSection)currentSection).getDocument(),p);
		ret += "&nbsp;<i><ano:write name=\"description." + p.getName() + "\" ignore=\"true\"/></i>\r";

		ret += "<div id=\"file-uploader-" + p.getName() + "\" class=\"image_uploader\"><!-- --></div>\r";
		ret += "<script>\r";
		ret += "$(document).ready(function() {\r";
		ret += "	var uploader = new qq.FileUploader({\r";
		ret += "	    element: document.getElementById('file-uploader-" + p.getName() +"'),\r";
		ret += "	    action: '${pageContext.request.contextPath}/cms/fileUpload',\r";
		ret += "	    params: {\r";
		ret += "	    	property: '" + p.getName() + "'\r";
	    ret += "	    }\r";

		ret += "	});\r";
		ret += "});\r";
		ret += "</script>\r";
		return ret;
	}

	private String getStringEditor(MetaFieldElement element, MetaProperty p){
		return getInputEditor(element, p, "text");
	}

	private String getPasswordEditor(MetaFieldElement element, MetaProperty p){
        return getInputEditor(element, p, "password");
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
			ret += " <ano:equal name="+quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" property="+quote(p.getName(lang))+" value=\"true\"";
			ret += ">";
			ret += "checked</ano:equal>";
		}
		else {
			ret += " value=\"<ano:write name="+quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" property="+quote(p.getName(lang)) + "/>\"";

			// Required for DateTime widget
			if (element.isDatetime()) {
				ret += " class=" + quote("datetime");
				isNeedEnableDateTimeWidgets = true;
			}
		}
		if (element.isReadonly())
			ret += " readonly="+quote("readonly");
        if (element.isAutocompleteOff())
            ret += " autocomplete="+quote("off");

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
		ret += "<ano:write filter=\"false\" name="+quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument()))+" property="+quote(p.getName(lang))+" />";
		ret += "</textarea>";

		return ret;
	}

	private void generateLinkElementEditorJS(MetaDocument doc, List<String> linkElements){
		appendString("<script type=\"text/javascript\">");
		increaseIdent();
		for(String elName: linkElements){

			//FIXME: here is assumed that links can't be multilanguage
			String elCapitalName = StringUtils.capitalize(elName);
			String beanName = CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument());

			appendString("//Initializing items for " + elName);
			appendString("var " +elName+ "Json = {items:[");
			appendString("<ano:iterate id=\"item\" name="+quote(beanName)+" property=\""+elName+"Collection\" type=\"net.anotheria.webutils.bean.LabelValueBean\">");
			increaseIdent();
			appendString("{id:\"<ano:write name=\"item\" property=\"value\" filter=\"true\"/>\",name:\"<ano:write name=\"item\" property=\"label\" filter=\"true\"/>\"},");
			//appendString("{id:\"${item.value}\",name:\"${item.label}\"},");
			decreaseIdent();
			appendString("</ano:iterate>");
			appendString("]};");
			appendString("var selection"+elCapitalName+"Json = {");
			increaseIdent();
			appendString("id:'<ano:write name="+quote(beanName)+" property="+quote(elName)+"/>',name:'<ano:write name="+quote(beanName)+" property="+quote(elName + "CurrentValue")+"/>'");
			decreaseIdent();
			appendString("};");
			appendString("new YAHOO.anoweb.widget.ComboBox("+quote(elCapitalName+"CurrentValue")+",\""+elCapitalName+"Selector\","+elName+"Json,selection"+elCapitalName+"Json);");
		}
		decreaseIdent();
		appendString("</script>");
	}

	private void generateRichTextEditorJS(MetaDocument doc, List<MetaViewElement> richTextElements){

		appendString("<!-- TinyMCE -->");

		appendString("<script type=\"text/javascript\">");
		appendString("tinyMCE.init({");
		appendString("mode : \"exact\",");
		appendString("theme : \"advanced\",");
		appendString("plugins : \"save, table\",");
		appendString("theme_advanced_layout_manager : \"SimpleLayout\",");
		appendString("theme_advanced_toolbar_align : \"left\",");
		appendString("theme_advanced_toolbar_location : \"top\",");
		appendString("theme_advanced_buttons1 : \"undo, redo, separator, bold, italic, underline, separator, justifyleft, justifycenter, justifyright, justifyfull, formatselect,  fontselect, fontsizeselect, forecolor\",");
		appendString("theme_advanced_buttons2 : \"bullist, numlist, separator, image, link, unlink, separator, table, code\",");
		appendString("theme_advanced_buttons3 : \"\",");
		appendString("theme_advanced_resize_horizontal : true,");
		appendString("});");

		appendString("</script>");
		appendString("<!-- /TinyMCE -->");

	}

	private void generateDateTimeWidgetJS() {
		if (!isNeedEnableDateTimeWidgets)
			return;

		appendString("<!-- JQuery DateTime Widget: START -->");
		appendString("<script type=\"text/javascript\">");
		appendString(" $(document).ready(function() {");
		// HACK for fast solution
		appendString("  serverTimezoneOffset = <%=java.util.TimeZone.getDefault().getOffset(new java.util.Date().getTime())%>;");
		appendString("  $('form').DateTimeStamp();");
		appendString(" });");
		appendString("</script>");
		appendString("<!-- JQuery DateTime Widget: END -->");
	}

	private String getFunctionEditor(MetaDocument doc, MetaFunctionElement element){
		if (element.getName().equals("cancel")) {
			String onClick = "return confirm('All unsaved data will be lost!!!. Document will be unlocked";
			String cancel = CMSMappingsConfiguratorGenerator.getPath(((MetaModuleSection) currentSection).getDocument(), CMSMappingsConfiguratorGenerator.ACTION_CLOSE);
			cancel += "?pId=<ano:write name=" + quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)) + " property=\"id\"/>";
			return "<a href=\"" + cancel + "\" class=\"button\" onClick=\""+onClick+"\"><span>Close</span></a>";
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

    private String getUpdateAndDeleteFileAndStayFunction(MetaDocument doc, MetaProperty p){
        String beanName = CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, ((MetaModuleSection)currentSection).getDocument());
        String propertyWriter = "<ano:write name="+quote(beanName)+" property="+quote(p.getName()) + "/>";
        if(StorageType.CMS.equals(doc.getParentModule().getStorageType())){
            //creating logic for hiding or showing current operation link in Locking CASE!!!!!
            String result = "<ano:notEmpty name="+quote(beanName)+" property="+quote(p.getName())+">";
            result += "<ano:equal name=" + quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)) +
                    " property=" + quote(LockableObject.INT_LOCK_PROPERTY_NAME) + " value=" + quote("true") + "> \n";
            result+="  <ano:equal name=" + quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)) +
                    " property=" + quote(LockableObject.INT_LOCKER_ID_PROPERTY_NAME) + " value=" + quote("<%=(java.lang.String)session.getAttribute(\\"+quote("currentUserId\\")+")%>") + "> \n";
            result+="\t<a href=\"#\" onClick=\"" +
                    "document."+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)+".nextAction.value='stay';" +
                    " document."+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)+".fieldName.value='"+p.getName()+"';" +
                    " document."+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)+".fileName.value='"+propertyWriter+"';" +
                    " if (validateForm()) { FormatTime('datetime');  document."+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)+".submit(); } return false\"><img src=\"/cms_static/img/delete.gif\" alt=\"Delete file\" title=\"Delete file\"></a> \n";
            result+="  </ano:equal> \n";
            result+="</ano:equal> \n";
            result+="<ano:equal name=" + quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)) + " property=" + quote(LockableObject.INT_LOCK_PROPERTY_NAME) + " value=" + quote("false") + "> \n";
            result+="\t<a href=\"#\" onClick=";
            //tinyMCE save hack start
            result+="\"customSubmit(); ";
            //tinyMCE save hack end
            result+="document."+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)+".nextAction.value='stay';" +
                    " document."+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)+".fieldName.value='"+p.getName()+"';" +
                    " document."+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)+".fileName.value='"+propertyWriter+"';" +
                    " if (validateForm()) { FormatTime('datetime');  document."+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)+".submit(); } return false\"><img src=\"/cms_static/img/delete.gif\" alt=\"Delete file\" title=\"Delete file\"></a>\n";
            result+="</ano:equal> \n";
            result+="</ano:notEmpty> \n";
            return result;
        }
        //Delete customSubmit in the bottom, if not using tinyMCE
        return "<ano:notEmpty name="+quote(beanName)+" property="+quote(p.getName())+">"+
                "<a href=\"#\" onClick=\"customSubmit(); " +
                "document."+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)+".nextAction.value='stay';" +
                " document."+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)+".fieldName.value='"+p.getName()+"';" +
                " document."+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)+".fileName.value='"+propertyWriter+"';" +
                " if (validateForm()) { FormatTime('datetime');  document."+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)+".submit(); } return false\"><img src=\"/cms_static/img/delete.gif\" alt=\"Delete file\" title=\"Delete file\"></a>"+
                "</ano:notEmpty>";
	}

    private String getUpdateAndStayFunction(MetaDocument doc, MetaFunctionElement element){
		if(StorageType.CMS.equals(doc.getParentModule().getStorageType())){
			//creating logic for hiding or showing current operation link in Locking CASE!!!!!
			String result = "<ano:equal name=" + quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)) +
					" property=" + quote(LockableObject.INT_LOCK_PROPERTY_NAME) + " value=" + quote("true") + "> \n";
			result+="  <ano:equal name=" + quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)) +
					" property=" + quote(LockableObject.INT_LOCKER_ID_PROPERTY_NAME) + " value=" + quote("<%=(java.lang.String)session.getAttribute(\\"+quote("currentUserId\\")+")%>") + "> \n";
			result+="\t<a href=\"#\" class=\"button\" onClick=\"document."+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)+
					".nextAction.value='stay'; if (validateForm()) { FormatTime('datetime');  document."+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)+".submit(); } return false\"><span><ano:write name=\"apply.label.prefix\"/></span></a> \n";
			result+="  </ano:equal> \n";
			result+="</ano:equal> \n";
			result+="<ano:equal name=" + quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)) + " property=" + quote(LockableObject.INT_LOCK_PROPERTY_NAME) + " value=" + quote("false") + "> \n";
			result+="\t<a href=\"#\" class=\"button\" onClick=";
			//tinyMCE save hack start
			result+="\"customSubmit(); ";
			//tinyMCE save hack end
			result+="document."+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)+
					".nextAction.value='stay'; if (validateForm()) { FormatTime('datetime');  document."+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)+".submit(); } return false\"><span><ano:write name=\"apply.label.prefix\"/></span></a>\n";
			result+="</ano:equal> \n";
			return result;
		}
		//Delete customSubmit in the bottom, if not using tinyMCE
		return "<a href=\"#\" class=\"button\" onClick=\"customSubmit(); document."+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)+
				".nextAction.value='stay'; if (validateForm()) { FormatTime('datetime');  document."+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)+".submit(); } return false\"><span><ano:write name=\"apply.label.prefix\"/></span></a>";
	}
	private String getUpdateAndCloseFunction(MetaDocument doc, MetaFunctionElement element){
		if(StorageType.CMS.equals(doc.getParentModule().getStorageType())){
			//creating logic for hiding or showing current operation link in Locking CASE!!!!!
			String result = "<ano:equal name=" + quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)) +
					" property=" + quote(LockableObject.INT_LOCK_PROPERTY_NAME) + " value=" + quote("true") + "> \n";
			result+="  <ano:equal name=" + quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)) +
					" property=" + quote(LockableObject.INT_LOCKER_ID_PROPERTY_NAME) + " value=" + quote("<%=(java.lang.String)session.getAttribute(\\"+quote("currentUserId\\")+")%>") + "> \n";
			result+="\t<a href=\"#\" class=\"button\" onClick=\"document."+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)+
					".nextAction.value='close'; if (validateForm()) { FormatTime('datetime'); document."+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)+".submit(); } return false\"><span><ano:write name=\"save.label.prefix\"/></span></a> \n";
			result+="  </ano:equal> \n";
			result+="</ano:equal> \n";
			result+="<ano:equal name=" + quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)) + " property=" + quote(LockableObject.INT_LOCK_PROPERTY_NAME) + " value=" + quote("false") + "> \n";
			result+="\t<a href=\"#\" class=\"button\" onClick=";
			//tinyMCE save hack start
			result+="\"customSubmit(); ";
			//tinyMCE save hack end
			result+="document."+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)+
					".nextAction.value='close'; if (validateForm()) { FormatTime('datetime');  document."+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)+".submit(); } return false\"><span><ano:write name=\"save.label.prefix\"/></span></a> \n";
			result+="</ano:equal> \n";
			return result;
		}
		//Delete customSubmit in the bottom, if not using tinyMCE
		return "<a href=\"#\" class=\"button\" onClick=\"customSubmit(); document."+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)+
				".nextAction.value='close'; if (validateForm()) { FormatTime('datetime');  document."+CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, doc)+".submit(); } return false\"><span><ano:write name=\"save.label.prefix\"/></span></a>";
	}

	/**
	 * Creating entries in JSP for Multilanguage Support!!!
	 * @param section
	 * @param colspan
	 */
	private void addMultilanguageOperations(MetaModuleSection section, int colspan) {
		appendString("<ano:equal name="+quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, section.getDocument()))+" property="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" value="+quote("false")+">");
		increaseIdent();
		appendString("<ano:notEqual name="+quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, section.getDocument()))+" property="+quote("id")+" value="+quote("")+">");
		increaseIdent();
		appendString("<form name=\"CopyLang\" id=\"CopyLang\" method=\"get\" action=\""+CMSMappingsConfiguratorGenerator.getPath(section.getDocument(), CMSMappingsConfiguratorGenerator.ACTION_COPY_LANG)+"\">");
		increaseIdent();
		appendString("<input type=\"hidden\" name=\"ts\" value=\"<%=System.currentTimeMillis()%>\"/><input type=\"hidden\" name=\"pId\" value=\"<ano:write name="+quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, section.getDocument()))+" property="+quote("id")+"/>\"/>");
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
		appendString("<form name="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" id="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+"  method=\"get\" action=\""+CMSMappingsConfiguratorGenerator.getPath(section.getDocument(), CMSMappingsConfiguratorGenerator.ACTION_SWITCH_MULTILANGUAGE_INSTANCE)+"\">");
		increaseIdent();
		appendString("<div>");
		appendString("<input type=\"hidden\" name=\"value\" value=\"true\"/><input type=\"hidden\" name=\"ts\" value=\"<%=System.currentTimeMillis()%>\"/><input type=\"hidden\" name=\"pId\" value=\"<ano:write name="+quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, section.getDocument()))+" property="+quote("id")+"/>\"/>");
		decreaseIdent();
		appendString("</div>");
		appendString("<a href=\"#\" class=\"button\" onclick=\"document."+ModuleBeanGenerator.FIELD_ML_DISABLED+".submit(); return false\"><span>Disable languages</span></a>");
		appendString("</form>");
		decreaseIdent();
		appendString("</ano:notEqual>");
		decreaseIdent();
		appendString("</ano:equal>");
		appendString("<ano:equal name="+quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, section.getDocument()))+" property="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" value="+quote("true")+">");
		increaseIdent();
		appendString("<div>");
		appendString("<form name="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" id="+quote(ModuleBeanGenerator.FIELD_ML_DISABLED)+" method=\"get\" action=\""+CMSMappingsConfiguratorGenerator.getPath(section.getDocument(), CMSMappingsConfiguratorGenerator.ACTION_SWITCH_MULTILANGUAGE_INSTANCE)+"\">");
		appendString("<input type=\"hidden\" name=\"value\" value=\"false\"/><input type=\"hidden\" name=\"ts\" value=\"<%=System.currentTimeMillis()%>\"/><input type=\"hidden\" name=\"pId\" value=\"<ano:write name="+quote(CMSMappingsConfiguratorGenerator.getDialogFormName(currentDialog, section.getDocument()))+" property="+quote("id")+"/>\"/>");
		appendString("<a href=\"#\" class=\"button\" onclick=\"document."+ModuleBeanGenerator.FIELD_ML_DISABLED+".submit(); return false\"><span>Enable languages</span></a>");
		decreaseIdent();
		appendString("</div>");
		appendString("</form>");
		appendString("</ano:equal>");
	}

    private void addDialogContentToDisplayUsagesOfElement(MetaModuleSection section){
        if(isAppropriateModule(section)){
            if (!section.getDocument().getName().equalsIgnoreCase("RedirectUrl") && !section.getDocument().getName().equalsIgnoreCase("EntryPoint")){
                increaseIdent();
                appendString("<script type=\"text/javascript\">");
                appendString("$(function(){");
                appendIncreasedString("$('#display_all_usages').bind('click',function(event){");
                appendString("event.preventDefault();");
                appendIncreasedString("var all_usages_of_element = $('#all_usages_of_element');");
                appendString("$.ajax({");
                appendIncreasedString("url:\"/cms/showUsages\",");
                appendString("type:\"POST\",");
                appendString("data:({");
                appendIncreasedString("doc: $('.showUsagesDocName').val(),");
                appendIncreasedString("pId: $('.showUsagesPId').val()");
                decreaseIdent();
                appendString("}),");
                appendString("success:function(data){");
                appendIncreasedString("var referenceList = data.data.references;");
                appendIncreasedString("if (referenceList != undefined && !(referenceList.length == 0)) {");
                appendIncreasedString("all_usages_of_element.append(referenceList);");
                appendIncreasedString("}else{");
                appendIncreasedString("all_usages_of_element.append(\"This element have no usages\");");
                appendString("}");
                appendString("showDialog();");
                decreaseIdent();
                appendString("}");
                decreaseIdent();
                appendString("})");
                decreaseIdent();
                appendString("})");
                decreaseIdent();
                appendString("});");

                appendString("function showDialog(){");
                appendIncreasedString("var all_usages_of_element = $('#all_usages_of_element');");
                appendIncreasedString("all_usages_of_element.dialog({");
                appendString("modal:true,");
                appendString("draggable: false,");
                appendString("minHeight: 150,");
                appendString("maxHeight: 600,");
                appendString("width: 'auto',");
                appendString("buttons:{");
                appendIncreasedString("Close:function(){");
                appendIncreasedString("all_usages_of_element.text(\"\");");
                appendIncreasedString("$(this).dialog(\"close\");");
                decreaseIdent();
                appendString("}");
                decreaseIdent();
                appendString("}");
                decreaseIdent();
                appendString("})");
                decreaseIdent();
                appendString("};");
                appendString("</script>");
            }
        }
    }

}
