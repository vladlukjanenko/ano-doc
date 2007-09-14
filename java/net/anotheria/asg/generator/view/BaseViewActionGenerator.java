package net.anotheria.asg.generator.view;

import java.util.ArrayList;
import java.util.List;


import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.view.meta.MetaCustomSection;
import net.anotheria.asg.generator.view.meta.MetaModuleSection;
import net.anotheria.asg.generator.view.meta.MetaSection;
import net.anotheria.asg.generator.view.meta.MetaView;
import net.anotheria.util.StringUtils;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class BaseViewActionGenerator extends AbstractGenerator {

	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.generator.IGenerator#generate(net.anotheria.anodoc.generator.IGenerateable, net.anotheria.anodoc.generator.Context)
	 */
	public FileEntry generate(IGenerateable g, Context context) {
		
		MetaView view = (MetaView)g;
		String ret = generateViewAction(view, context);
		return new FileEntry(FileEntry.package2path(context.getPackageName()+".action"), getViewActionName(view),ret);
	}
	
	public static String getViewActionName(MetaView view){
		return "Base"+StringUtils.capitalize(view.getName())+"Action";
	}
	
	public String generateViewAction(MetaView view, Context context){
		String ret = "";
		
		List<MetaSection> sections = view.getSections();
		List<MetaModule> modules = new ArrayList<MetaModule>();
		
		for (int i=0; i<sections.size(); i++){
			MetaSection section = sections.get(i);
			if (section instanceof MetaModuleSection){
				if (modules.indexOf(((MetaModuleSection)section).getModule())==-1){
					modules.add(((MetaModuleSection)section).getModule());
				}
			}
		}		

		

		ret += writeStatement("package "+context.getPackageName()+".action");
		ret += emptyline();
		
		ret += writeImport("java.util.List");
		ret += writeImport("java.util.ArrayList");
		ret += emptyline();

		//ret += writeImport("net.anotheria.webutils.actions.*");
		ret += writeImport("net.anotheria.webutils.bean.MenuItemBean");
		ret += emptyline();

		ret += writeImport("javax.servlet.http.HttpServletRequest");
		ret += writeImport("javax.servlet.http.HttpServletResponse");
		ret += writeImport("org.apache.struts.action.ActionForm");
		//ret += writeImport("org.apache.struts.action.ActionForward");
		ret += writeImport("org.apache.struts.action.ActionMapping");
		ret += emptyline();
		
		

		ret += writeString("public abstract class "+getViewActionName(view)+" extends "+BaseActionGenerator.getBaseActionName(context)+"{");
		ret += emptyline();
		increaseIdent();
		
		

		ret += writeStatement("public static final String BEAN_MENU = "+quote("menu"));
		ret += writeStatement("public static final String BEAN_QUERIES_MENU = "+quote("queriesMenu"));
		ret += emptyline();
		
		ret += writeString("protected abstract String getTitle();");
		ret += emptyline();
		
		ret += writeString("protected void init(ActionMapping mapping, ActionForm af, HttpServletRequest req, HttpServletResponse res) throws Exception {");
		increaseIdent();
		ret += writeStatement("super.init(mapping, af, req, res)");
		ret += writeStatement("prepareMenu(req)");
		ret += closeBlock();
		ret += emptyline();
					
		ret += writeString("private void prepareMenu(HttpServletRequest req){");
		increaseIdent();
		ret += writeStatement("List<MenuItemBean> menu = new ArrayList<MenuItemBean>()");
		for (int i=0; i<sections.size(); i++){
			MetaSection section = (MetaSection)sections.get(i);
			if (section instanceof MetaModuleSection)
				ret += writeStatement("menu.add(makeMenuItemBean("+quote(section.getTitle())+", "+quote(StrutsConfigGenerator.getPath(((MetaModuleSection)section).getDocument(), StrutsConfigGenerator.ACTION_SHOW))+"))");
			if (section instanceof MetaCustomSection)
				ret += writeStatement("menu.add(makeMenuItemBean("+quote(section.getTitle())+", "+quote(((MetaCustomSection)section).getPath())+"))");
					
		}
		ret += writeStatement("addBeanToRequest(req, BEAN_MENU, menu)");
		
		ret += emptyline();
		ret += writeStatement("List<MenuItemBean> queriesMenu = new ArrayList<MenuItemBean>()");
		for (int i=0; i<sections.size(); i++){
			MetaSection section = (MetaSection)sections.get(i);
			if (section instanceof MetaModuleSection){
				MetaDocument doc = ((MetaModuleSection)section).getDocument();
				if (doc.getLinks().size()>0){
					ret += writeStatement("queriesMenu.add(makeMenuItemBean("+quote(section.getTitle())+", "+quote(StrutsConfigGenerator.getPath(doc, StrutsConfigGenerator.ACTION_SHOW_QUERIES))+"))");
				}
			}
		}
		ret += writeStatement("addBeanToRequest(req, BEAN_QUERIES_MENU, queriesMenu)");
		
		
		ret += closeBlock();
		ret += emptyline();
		
		ret += writeString("private MenuItemBean makeMenuItemBean(String title, String link){");
		increaseIdent();
		ret += writeString("MenuItemBean bean = new MenuItemBean();");
		ret += writeString("bean.setCaption(title);");
		ret += writeString("bean.setLink(link);");
		ret += writeString("if (title.equals(getTitle())){");
		increaseIdent();
		ret += writeString("bean.setActive(true);");
		ret += writeString("bean.setStyle(\"menuTitleSelected\");");
		decreaseIdent();
		ret += writeString("}else{");
		increaseIdent();
		ret += writeString("bean.setActive(false);");
		ret += writeString("bean.setStyle(\"menuTitle\");");
		ret += closeBlock();		
		ret += writeString("return bean;");
		ret += closeBlock();
		ret += emptyline();
		
		//security...
		ret += writeString("protected boolean isAuthorizationRequired(){");
		increaseIdent();
		//System.out.println("Warning authorization is off.");
		ret+= writeStatement("return true");
		//ret+= writeStatement("return false");
		ret += closeBlock();
		ret += emptyline();

		ret += closeBlock();
		
		return ret;
	}

}
