package net.anotheria.asg.generator.view;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaLink;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.view.meta.MetaModuleSection;
import net.anotheria.asg.generator.view.meta.MetaSection;
import net.anotheria.asg.generator.view.meta.MetaView;
import net.anotheria.util.StringUtils;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class JspQueriesGenerator 
	extends AbstractJSPGenerator implements IGenerator{
	
	private MetaSection currentSection;
	

	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.generator.IGenerator#generate(net.anotheria.anodoc.generator.IGenerateable, net.anotheria.anodoc.generator.Context)
	 */
	public List<FileEntry> generate(IGenerateable g, Context aContext) {
	
		setContext(aContext);
		List<FileEntry> files = new ArrayList<FileEntry>();
		MetaView view = (MetaView)g;
	
		FileEntry menu = new FileEntry(FileEntry.package2path(getContext().getPackageName()+".jsp"), getMenuName(view), generateMenu(view));
		menu.setType(".jsp");
		files.add(menu);

		FileEntry footer = new FileEntry(FileEntry.package2path(getContext().getPackageName()+".jsp"), getFooterName(view), generateFooter(view, FOOTER_SELECTION_QUERIES));
		footer.setType(".jsp");
		files.add(footer);

		for (int i=0; i<view.getSections().size(); i++){
			MetaSection s = (MetaSection)view.getSections().get(i);
			if (!(s instanceof MetaModuleSection))
				continue;
			MetaModuleSection section = (MetaModuleSection)s;
			MetaDocument doc = section.getDocument();
			if (doc.getLinks().size()>0){
				FileEntry showQueryFile = new FileEntry(FileEntry.package2path(getContext().getPackageName()+".jsp"), getShowQueriesPageName(section.getDocument()), generateShowQueriesPage(section, view));
				showQueryFile.setType(".jsp");
				files.add(showQueryFile);
			}
			
		}
		return files;
	}
	
	private String generateShowQueriesPage(MetaModuleSection section, MetaView view){
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
		ret += generatePragmas(view);
		ret += writeString("<link href=\""+getCurrentCSSPath("admin.css")+"\" rel=\"stylesheet\" type=\"text/css\">");
		decreaseIdent();
		ret += writeString("</head>");
		ret += writeString("<body>");
		increaseIdent();
		ret += writeString("<jsp:include page=\""+getMenuName(view)+".jsp\" flush=\"true\"/>");

/*
		List elements = section.getElements();
		
		*/
		int colspan = 1;
		
		ret += writeString("<table width=\"100%\" border=\"0\" cellspacing=\"1\" cellpadding=\"1\">");
		increaseIdent();
		ret += openTR();
		ret += writeString("<td colspan=\""+colspan+"\"><img src="+quote(getCurrentImagePath("s.gif"))+" width=\"1\" height=\"1\"></td>");
		ret += closeTR();
		
		ret += openTR();
		ret += writeString("<td>&nbsp;</td>");
		ret += closeTR();
		
		ret += openTR("class="+quote("lineCaptions"));
		ret += writeString("<td>&nbsp;Available queries:</td>");
		ret += closeTR();
		
		List links = doc.getLinks();
		for (int i=0; i<links.size(); i++){
			MetaLink link = (MetaLink)links.get(i);
			
			MetaModule mod = GeneratorDataRegistry.getInstance().getModule(link.getTargetModuleName());
			MetaDocument targetDocument = mod.getDocumentByName(link.getTargetDocumentName());
			
			
			ret += openTR("class="+quote(i%2==0 ? "lineLight" : "lineDark"));
			ret += writeString("<form name="+quote(targetDocument.getName())+" action="+quote(StrutsConfigGenerator.getExecuteQueryPath(doc))+" method="+quote("GET")+"><input type="+quote("hidden")+" name="+quote("property")+" value="+quote(link.getName()));
			ret += openTD();
			increaseIdent();
			ret += writeString("&nbsp;Show all "+section.getTitle()+" where "+link.getName()+" is:&nbsp;");
			ret += writeOpeningTag("select", "name="+quote("criteria"));
			increaseIdent();
			
			ret += writeString("<option value=\"\">none</option>");
			ret += writeString("<logic:iterate name="+quote(targetDocument.getMultiple().toLowerCase())+" type="+quote("net.anotheria.webutils.bean.LabelValueBean")+" id="+quote("entry")+">");
			ret += writeIncreasedString("<option value="+quote("<bean:write name="+quote("entry")+" property="+quote("value")+"/>")+">"+"<bean:write name="+quote("entry")+" property="+quote("label")+" filter="+quote("false")+"/>"+"</option>");
			ret += writeString("</logic:iterate>");
			//ret += writeClosingTag("select");
			ret += writeString("</select>&nbsp;<a href="+quote("#")+" onClick="+quote("document.forms."+targetDocument.getName()+".submit(); return false")+">GO</a>");
			decreaseIdent();
			ret += closeTD();
			ret += writeString("</form>");
			ret += closeTR();
		}

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
	

	private String getMenuName(MetaView view){
		return StringUtils.capitalize(view.getName())+"QueriesMenu";		
	}

	private String generateMenu(MetaView view){
		
		String ret = getBaseJSPHeader();
		
		ret += writeString("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
		increaseIdent();
		ret += writeString("<tr>");
		increaseIdent();
		ret += writeString("<logic:iterate name=\"queriesMenu\" type=\"net.anotheria.webutils.bean.MenuItemBean\" id=\"entry\">");
		increaseIdent();
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
		

		decreaseIdent();
		ret += writeString("</tr>");
		decreaseIdent();
		ret += writeString("</table>");
		
		ret += getBaseJSPFooter();
			
		return ret;
	}
	
	private String getFooterName(MetaView view){
		return StringUtils.capitalize(view.getName())+"QueryFooter";		
	}
}
