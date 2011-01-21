package net.anotheria.asg.generator.view;

import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedJSPFile;
import net.anotheria.asg.generator.meta.MetaModule;

public class IndexPageJspGenerator extends AbstractJSPGenerator {

	public FileEntry generate(Context context) {
		FileEntry page = new FileEntry(FileEntry.package2fullPath(context.getPackageName(MetaModule.SHARED) + ".jsp"), getIndexPageJspName(),
				generateIndexPage().createFileContent());
		page.setType(".jsp");
		return page;
	}

	public static final String getIndexPageJspName() {
		return "IndexPage";
	}

	public static final String getSharedJspFooterPageName() {
		return getIndexPageJspName() + ".jsp";
	}
	
	private GeneratedJSPFile generateIndexPage() {
		GeneratedJSPFile jsp = new GeneratedJSPFile();
		startNewJob(jsp);
		jsp.setName(getIndexPageJspName());

		resetIdent();

		append(getBaseJSPHeader());

		appendString("<html:html>");
		appendString("<head>");
		increaseIdent();
		appendString("<title>" + getIndexPageJspName() + "</title>");
		generatePragmas();
		appendString("<link href=\"" + getCurrentCSSPath("admin.css") + "\" rel=\"stylesheet\" type=\"text/css\">");
		decreaseIdent();
		appendString("</head>");
		appendString("<body>");

		// table
		increaseIdent();
		appendString("<table width=" + quote("100%") + " cellspacing=" + quote("1") + " cellpadding=" + quote("1") + " border="
				+ quote("0") + ">");

		// table header
		increaseIdent();
		appendString("<tr class=" + quote("lineCaptions") + ">");
		appendIncreasedString("<td align=\"center\" width=\"100%\">Views</td>");
		appendString("</tr>");

		// table body
		appendString("<bean:size id=" + quote("listsize") + " name=" + quote("views") + "/>");
		appendString("<logic:iterate name=" + quote("views") + " type=" + quote("net.anotheria.webutils.bean.MenuItemBean") + " id="
				+ quote("v") + " indexId=" + quote("ind") + " >");
		appendString("<tr class=\"<%=ind.intValue() % 2 == 0 ? \"lineLight\" : \"lineDark\"%>\">");
		increaseIdent();
		appendString("<td align=\"center\" width=\"100%\">");
		increaseIdent();
		appendString("<a href=" + quote("<ano:tslink><bean:write name=" + quote("v") + " property=" + quote("link") + "/></ano:tslink>")
				+ ">");
		increaseIdent();
		appendString("<bean:write name=" + quote("v") + " property=" + quote("caption") + "/>");
		decreaseIdent();
		appendString("</a>");
		decreaseIdent();
		appendString("</td>");
		decreaseIdent();
		appendString("</tr>");
		appendString("</logic:iterate>");

		// end table
		decreaseIdent();
		appendString("</table>");
		decreaseIdent();
		appendString("</body>");
		appendString("</html:html>");

		append(getBaseJSPFooter());

		return jsp;
	}

}
