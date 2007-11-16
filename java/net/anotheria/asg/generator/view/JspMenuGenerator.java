package net.anotheria.asg.generator.view;

import java.util.List;

import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.view.meta.MetaModuleSection;
import net.anotheria.asg.generator.view.meta.MetaSection;
import net.anotheria.asg.generator.view.meta.MetaView;
import net.anotheria.util.StringUtils;

public class JspMenuGenerator extends AbstractJSPGenerator {

	public FileEntry generate(List<MetaView> views , Context context) {
		
		String ret = generateMenu(views, context);
		FileEntry menu = new FileEntry(FileEntry.package2path(context.getPackageName(MetaModule.SHARED)+".jsp"), getMenuName(), ret);
		menu.setType(".jsp");
		return menu;
		
		//return new FileEntry(FileEntry.package2path(context.getPackageName()+".action"), getBaseActionName(context),ret);
	}
	
	public static final String getMenuName(){
		return "Menu";
	}
	
	public static final String getMenuPageName(){
		return getMenuName()+".jsp";
	}
	
	private String generateMenu(List<MetaView> views, Context context){
		String ret = "";
		ret += getBaseJSPHeader();

		
		ret += writeString("<!-- dynamic menu -->");
		ret += writeString("<ul class=\"menuBar\" id=\"mainMenu\">");
		increaseIdent();
		ret += writeString("<li>");
		increaseIdent();
		ret += writeString("<a class=\"menuButton\" href=\"\" onclick=\"return buttonClick(this);\">System</a>");
		ret += writeString("<ul>");
		increaseIdent();
		ret += writeString("<li><a href=\"\">About</a></li>");
		ret += writeString("<li class=\"menuItemSep\"><div>&nbsp;</div></li>");
		ret += writeString("<li><a href=\"\">Preferences</a></li>");
		ret += writeString("<li class=\"menuItemSep\"><div>&nbsp;</div></li>");
		ret += writeString("<li><a href=\"\">Logout</a></li>");
		decreaseIdent();
		ret += writeString("</ul>");
		decreaseIdent();
		ret += writeString("</li>");

		ret += writeString("<li>");
		increaseIdent();
		ret += writeString("<a class=\"menuButton\" href=\"\" onclick=\"return buttonClick(this);\">Modules</a>");
		increaseIdent();
		ret += writeString("<ul>");
		increaseIdent();
		for (MetaView view : views){
			ret += writeString("<li>");
			increaseIdent();
			String viewLink = "";
			if (view.getSections().size()>0){
				MetaSection s = view.getSections().get(0);
				if (s instanceof MetaModuleSection){
					viewLink = StrutsConfigGenerator.getPath(((MetaModuleSection)s).getDocument(), StrutsConfigGenerator.ACTION_SHOW);
					viewLink = "<ano:tslink>"+viewLink+"</ano:tslink>";
				}
			}
			ret += writeString("<a href=\""+viewLink+"\">"+view.getTitle()+"</a>");
			List<MetaSection> sections = view.getSections();
			if (sections.size()>0){
				ret += writeString("<ul>");
				increaseIdent();
				for (MetaSection section : sections){
					String link = "";
					if (section instanceof MetaModuleSection){
						link = StrutsConfigGenerator.getPath(((MetaModuleSection)section).getDocument(), StrutsConfigGenerator.ACTION_SHOW);
						link = "<ano:tslink>"+link+"</ano:tslink>";
					}
					if (section instanceof MetaModuleSection){
						ret += writeString("<li>");
						increaseIdent();
						ret += writeString("<a href=\""+link+"\">"+section.getTitle()+"</a>");
						decreaseIdent();
						ret += writeString("<ul>");
						increaseIdent();
						ret += writeString("<li><a href=\""+link+"\">Show</a></li>");
						link = StrutsConfigGenerator.getPath(((MetaModuleSection)section).getDocument(), StrutsConfigGenerator.ACTION_NEW);
						link = "<ano:tslink>"+link+"</ano:tslink>";
						ret += writeString("<li><a href=\""+link+"\">New</a></li>");
						decreaseIdent();
						ret += writeString("</ul>");
						ret += writeString("</li>");
					}else{
						ret += writeString("<li><a href=\""+link+"\">"+section.getTitle()+"</a></li>");
					}
				}
				decreaseIdent();
				ret += writeString("</ul>");
				
			}
			decreaseIdent();
			ret += writeString("</li>");
		}
		decreaseIdent();
		ret += writeString("</ul>");
		decreaseIdent();
		ret += writeString("</li>");

		
		decreaseIdent();
		ret += writeString("</ul>");
		ret += writeString("<!-- / dynamic menu end-->");

		return ret;
	}
}
