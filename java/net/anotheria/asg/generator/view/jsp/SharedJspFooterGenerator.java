package net.anotheria.asg.generator.view.jsp;

import java.util.List;

import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.view.AbstractJSPMafGenerator;
import net.anotheria.asg.generator.view.meta.MetaView;

public class SharedJspFooterGenerator extends AbstractJSPMafGenerator {
	
	/**
	 * A constant for quoting in the scriptlets.
	 */
    private static final String QUOTE = "\\\"\\\"";

    public FileEntry generate(List<MetaView> views , Context context) {
		
		String ret = generateSharedFooter(views, context);
		FileEntry footer = new FileEntry(FileEntry.package2fullPath(context.getPackageName(MetaModule.SHARED)+".jsp"), getSharedJspFooterName(), ret);
		footer.setType(".jsp");
		return footer;
		
		//return new FileEntry(FileEntry.package2path(context.getPackageName()+".action"), getBaseActionName(context),ret);
	}
	
	public static final String getSharedJspFooterName(){
		return "SharedFooter";
	}
	
	public static final String getSharedJspFooterPageName(){
		return getSharedJspFooterName()+".jsp";
	}
	
	private String generateSharedFooter(List<MetaView> views, Context context){
		String ret = "";
		ret += getBaseJSPHeader();
		if (views.size()>1){
		
			String viewSwitcher = "";
			viewSwitcher += "<bean:size id="+quote("listsize")+" name="+quote("views")+"/>";
			viewSwitcher +="<logic:iterate name="+quote("views")+" type="+quote("net.anotheria.webutils.bean.MenuItemBean")+" id="+quote("v")+" indexId="+quote("ind")+" >";
			viewSwitcher += "<a href="+quote("<ano:tslink><bean:write name="+quote("v")+" property="+quote("link")+"/></ano:tslink>")+">";
			viewSwitcher += "<bean:write name="+quote("v")+" property="+quote("caption")+"/>";
			viewSwitcher += "</a>";
			viewSwitcher += "<logic:notEqual name="+quote("ind")+" value="+quote("<%"+QUOTE+"+(listsize-1)%>")+">&nbsp;|&nbsp;</logic:notEqual>";
			viewSwitcher += "</logic:iterate>";
			viewSwitcher = "Views:&nbsp;" + viewSwitcher;
			
			ret += writeString(viewSwitcher);
			
		}
		ret += getBaseJSPFooter();
		return ret;
	}
}
