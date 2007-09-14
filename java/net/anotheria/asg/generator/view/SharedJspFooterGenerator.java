package net.anotheria.asg.generator.view;

import java.util.List;

import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.view.meta.MetaModuleSection;
import net.anotheria.asg.generator.view.meta.MetaSection;
import net.anotheria.asg.generator.view.meta.MetaView;
import net.anotheria.util.StringUtils;

public class SharedJspFooterGenerator extends AbstractJSPGenerator {

	public FileEntry generate(List<MetaView> views , Context context) {
		
		String ret = generateSharedFooter(views, context);
		FileEntry footer = new FileEntry(FileEntry.package2path(context.getPackageName()+".jsp"), getSharedJspFooterName(), ret);
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
			for (MetaView v : views){
				if (viewSwitcher.length()!=0)
					viewSwitcher += "&nbsp;|&nbsp;";
				MetaSection asection = v.getSections().get(0);
				//hack, assuming first section is always a module section.
				MetaModuleSection section = (MetaModuleSection)asection;
				String link = StrutsConfigGenerator.getPath(section.getDocument(), StrutsConfigGenerator.ACTION_SHOW);
				viewSwitcher += "<a href="+quote("<ano:tslink>"+link+"</ano:tslink>")+">"+StringUtils.capitalize(v.getName())+"</a>";
			}
			viewSwitcher = "Views:&nbsp;" + viewSwitcher;
			
			ret += writeString(viewSwitcher);
			
		}
		ret += getBaseJSPFooter();
		return ret;
	}
}
