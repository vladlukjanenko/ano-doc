package net.anotheria.asg.generator.view;

import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.Generator;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaContainerProperty;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.view.meta.MetaDialog;
import net.anotheria.asg.generator.view.meta.MetaModuleSection;
import net.anotheria.asg.generator.view.meta.MetaSection;
import net.anotheria.asg.generator.view.meta.MetaView;
import net.anotheria.util.StringUtils;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public abstract class AbstractJSPGenerator extends AbstractGenerator{
	
	public static final String FOOTER_SELECTION_QUERIES = "Queries";
	public static final String FOOTER_SELECTION_CMS     = "CMS";
	
	private Context context;
	
	protected String getBaseJSPHeader(){
		String ret = "";
		ret += "<%@ page"+CRLF;
		ret += "\tcontentType=\"text/html;charset="+GeneratorDataRegistry.getInstance().getContext().getEncoding()+"\" session=\"true\""+CRLF;
		
		ret += "%><%@ taglib uri=\"/WEB-INF/tlds/struts-bean.tld\" prefix=\"bean\""+CRLF;
		ret += "%><%@ taglib uri=\"/WEB-INF/tlds/struts-html.tld\" prefix=\"html\""+CRLF;
		ret += "%><%@ taglib uri=\"/WEB-INF/tlds/struts-logic.tld\" prefix=\"logic\""+CRLF;
		ret += "%><%@ taglib uri=\"/WEB-INF/tlds/anoweb.tld\" prefix=\"ano\""+CRLF;
		ret += "%>"+CRLF;
		return ret;
	}
	
	protected String getBaseXMLHeader(){
		String ret = "";
		ret += "<%@ page"+CRLF;
		ret += "\tcontentType=\"text/xml;charset="+GeneratorDataRegistry.getInstance().getContext().getEncoding()+"\" session=\"true\""+CRLF;
		
		ret += "%><%@ taglib uri=\"/WEB-INF/tlds/struts-bean.tld\" prefix=\"bean\""+CRLF;
		ret += "%><%@ taglib uri=\"/WEB-INF/tlds/struts-html.tld\" prefix=\"html\""+CRLF;
		ret += "%><%@ taglib uri=\"/WEB-INF/tlds/struts-logic.tld\" prefix=\"logic\""+CRLF;
		ret += "%><%@ taglib uri=\"/WEB-INF/tlds/anoweb.tld\" prefix=\"ano\""+CRLF;
		ret += "%>";
		return ret;
	}

	protected String getBaseCSVHeader(){
		String ret = "";
		ret += "<%@ page"+CRLF;
		ret += "\tcontentType=\"application/msexcel;charset="+GeneratorDataRegistry.getInstance().getContext().getEncoding()+"\" session=\"true\""+CRLF;
		
		ret += "%><%@ taglib uri=\"/WEB-INF/tlds/struts-bean.tld\" prefix=\"bean\""+CRLF;
		ret += "%><%@ taglib uri=\"/WEB-INF/tlds/struts-html.tld\" prefix=\"html\""+CRLF;
		ret += "%><%@ taglib uri=\"/WEB-INF/tlds/struts-logic.tld\" prefix=\"logic\""+CRLF;
		ret += "%><%@ taglib uri=\"/WEB-INF/tlds/anoweb.tld\" prefix=\"ano\""+CRLF;
		ret += "%>";
		return ret;
	}
	
	private String getFooterLink(String selection, String link, String linkCaption){
		if (selection!=null && selection.equals(linkCaption)){
			return "<strong>"+linkCaption+"</strong>"; 
		}
		return "&nbsp;<a href=\"<ano:tslink>"+link+"</ano:tslink>\">"+linkCaption+"</a>";

	}

	protected String generateFooter(MetaView view, String selection){
		
		String ret = getBaseJSPHeader();
		
		ret += writeString("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
		increaseIdent();
		ret += writeString("<tr>");
		increaseIdent();

		ret += writeString("<td>");
		increaseIdent();
		
		ret += writeString(getFooterLink(selection, defineLinkToViewCms(view), FOOTER_SELECTION_CMS));
		ret += writeString("&nbsp;|&nbsp;");
		//ret += writeString("<a href=\""+defineLinkToViewQueries(view)+"\">Queries</a>");
		ret += writeString(getFooterLink(selection, defineLinkToViewQueries(view), FOOTER_SELECTION_QUERIES));
		
		decreaseIdent();
		ret += writeString("</td>");
		ret += writeString("<td>");
		ret += writeIncreasedString("<jsp:include page="+quote(SharedJspFooterGenerator.getSharedJspFooterPageName())+" flush="+quote("false")+"/>");
		ret += writeString("</td>");
		ret += writeString("<td align="+quote("right")+">");
		increaseIdent();
		
		ret += writeString("Generated by "+Generator.getProductString()+" V "+Generator.getVersionString());
		//String timestamp = NumberUtils.makeISO8601TimestampString(System.currentTimeMillis());
		String timestamp = "Timestamp unset";
		ret += writeString(" on "+timestamp+"&nbsp;|&nbsp;");
		ret += writeString("<a href=\"<ano:tslink>logout</ano:tslink>\">Logout</a>&nbsp;");
		
		decreaseIdent();
		ret += writeString("</td>");
		decreaseIdent();
		ret += writeString("</tr>");
		decreaseIdent();
		ret += writeString("</table>");
		
		ret += getBaseJSPFooter();
			
		return ret;
	}
	
	private String defineLinkToViewQueries(MetaView view){
		String ret = "#";
		List<MetaSection> sections = view.getSections();
		for (int i=0; i<sections.size(); i++){
			MetaSection s = sections.get(i);
			if (s instanceof MetaModuleSection){
				MetaModuleSection ms = (MetaModuleSection)s;
				MetaDocument targetDoc = ms.getDocument();
				if (targetDoc.getLinks().size()>0){
					return StrutsConfigGenerator.getShowQueriesPath(targetDoc);
				}
			}
		}
		return ret;
	}
	
	private String defineLinkToViewCms(MetaView view){
		String ret = "#";
		List<MetaSection> sections = view.getSections();
		for (int i=0; i<sections.size(); i++){
			MetaSection s = sections.get(i);
			if (s instanceof MetaModuleSection){
				MetaDocument targetDoc = ((MetaModuleSection)s).getDocument();
				return StrutsConfigGenerator.getShowCMSPath(targetDoc);
			}
		}
		return ret;
	}

	protected String getBaseJSPFooter(){
		
		String ret = "<!-- generated by "+Generator.getProductString()+" V "+Generator.getVersionString()+", visit www.anotheria.net for details -->";
		return ret;
	}
	
	public static String getShowPageName(MetaDocument doc){
		return "Show"+doc.getMultiple();
	}
	
	public static String getSearchResultPageName(){
		return "SearchResult";
	}

	public static String getShowQueriesPageName(MetaDocument doc){
		return "Show"+doc.getMultiple()+"Queries";
	}
	
	public static String getExportAsCSVPageName(MetaDocument doc){
		return "Show"+doc.getMultiple()+"AsCSV";
	}

	public static String getExportAsXMLPageName(MetaDocument doc){
		return "Show"+doc.getMultiple()+"AsXML";
	}

	public static String getEditPageName(MetaDocument doc){
		return "Edit"+doc.getName()+"Dialog";
	}
	
	public static String getDialogName(MetaDialog dialog, MetaDocument doc){
		return dialog.getName()+doc.getName()+"Dialog";
	}

	public static String getContainerPageName(MetaDocument doc, MetaContainerProperty table){
		return "Show"+doc.getName()+StringUtils.capitalize(table.getName());
	}
	
	protected static String generateTimestampedLinkPath(String path){
		return "<ano:tslink>"+path+"</ano:tslink>";
	}

	

	protected String getCurrentImagePath(String imageName){
		return getImagePath(imageName, context);
	}
	
	public static String getImagePath(String imageName, Context context){
		return "/"+context.getApplicationName()+"/img/"+imageName;
	}

	protected String getCurrentCSSPath(String stylesheetName){
		return getCSSPath(stylesheetName, context);
	}
	
	public static String getCSSPath(String stylesheetName, Context context){
		return "/"+context.getApplicationName()+"/css/"+stylesheetName;
	}

	protected String getPackage(){
		return getPackage(GeneratorDataRegistry.getInstance().getContext());
	}

	/**
	 * @deprecated
	 * @param context
	 * @return
	 */
	public static String getPackage(Context context){
		return context.getPackageName()+".jsp";
	}
	
	public static String getPackage(Context context, MetaModule mod){
		return context.getJspPackageName(mod);
	}

	public static String getPackage(Context context, MetaDocument doc){
		return context.getJspPackageName(doc);
	}

	protected String getDuplicateImage(){
		return getDuplicateImage("duplicate");
	}

	protected String getDeleteImage(){
		return getDeleteImage("delete");
	}

	protected String getEditImage(){
		return getEditImage("edit");
	}

	protected String getDeleteImage(String alt){
		return getImage("del",alt);
	}

	protected String getTopImage(String alt){
		return getImage("top",alt);
	}

	protected String getBottomImage(String alt){
		return getImage("bottom",alt);
	}

	protected String getUpImage(String alt){
		return getImage("up",alt);
	}

	protected String getDownImage(String alt){
		return getImage("down",alt);
	}

	protected String getDuplicateImage(String alt){
		return getImage("duplicate",alt);
	}

	protected String getEditImage(String alt){
		return getImage("edit",alt);
	}
	
	protected String getImage(String name, String alt){
		return "<img src=\"/"+context.getApplicationName()+"/img/"+name+".gif"+"\" border=\"0\" alt="+quote(alt)+" title="+quote(alt)+">";
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	protected String generatePragmas(MetaView view){
	    return generatePragmas();
	}
	
	protected String generatePragmas(){
		String ret = "";
		ret += writeString("<META http-equiv=\"pragma\" content=\"no-cache\">");
		ret += writeString("<META http-equiv=\"Cache-Control\" content=\"no-cache, must-revalidate\">");
		ret += writeString("<META name=\"Expires\" content=\"0\">");
		ret += writeString("<META http-equiv=\"Content-Type\" content=\"text/html; charset="+getContext().getEncoding()+"\">");
		return ret;
	}
	
	protected String writeOpeningTag(String tag){
		return writeOpeningTag(tag, "");
	}

	protected String writeClosingTag(String tag){
		return writeClosingTag(tag, "");
	}

	protected String writeOpeningTag(String tag, String params){
		return writeString("<"+tag+(params.length()>0 ? " ":"")+params+">");
	}
	
	protected String writeClosingTag(String tag, String params){
		return writeString("</"+tag+(params.length()>0 ? " ":"")+params+">");
	}

	protected String openTR(String additionalParams){
		String ret = writeOpeningTag("tr", additionalParams);
		increaseIdent();
		return ret;
	}
	
	protected String closeTR(String additionalParams){
		decreaseIdent();
		return writeClosingTag("tr", additionalParams);
	}

	protected String openTR(){
		return openTR("");
	}
	
	protected String closeTR(){
		return closeTR("");
	}
	
	
	
	protected String openTD(){
		return openTD("");
	}
	
	protected String closeTD(){
		return closeTD("");
	}
	
	protected String openTD(String additionalParams){
		return writeOpeningTag("td", additionalParams);
	}
	
	protected String closeTD(String additionalParams){
		return writeClosingTag("td", additionalParams);
	}
	

}