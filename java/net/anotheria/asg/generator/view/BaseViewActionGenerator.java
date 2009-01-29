package net.anotheria.asg.generator.view;

import java.util.ArrayList;
import java.util.List;


import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.view.meta.MetaCustomSection;
import net.anotheria.asg.generator.view.meta.MetaModuleSection;
import net.anotheria.asg.generator.view.meta.MetaSection;
import net.anotheria.asg.generator.view.meta.MetaView;
import net.anotheria.util.StringUtils;

/**
 * This generator generates the base action for a view.
 * @author another
 */
public class BaseViewActionGenerator extends AbstractGenerator {

	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.generator.IGenerator#generate(net.anotheria.anodoc.generator.IGenerateable, net.anotheria.anodoc.generator.Context)
	 */
	public FileEntry generate(IGenerateable g, Context context) {
		
		MetaView view = (MetaView)g;
		return new FileEntry(generateViewAction(view, context));
	}
	
	public static String getViewActionName(MetaView view){
		return "Base"+StringUtils.capitalize(view.getName())+"Action";
	}
	
	public GeneratedClass generateViewAction(MetaView view, Context context){

		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		clazz.setPackageName(context.getPackageName(MetaModule.SHARED)+".action");
		
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

		clazz.addImport("java.util.List");
		clazz.addImport("java.util.ArrayList");
		clazz.addImport("net.anotheria.webutils.bean.MenuItemBean");
		clazz.addImport("javax.servlet.http.HttpServletRequest");
		clazz.addImport("javax.servlet.http.HttpServletResponse");
		clazz.addImport("org.apache.struts.action.ActionForm");
		clazz.addImport("org.apache.struts.action.ActionMapping");

		clazz.setAbstractClass(true);
		clazz.setName(getViewActionName(view));
		clazz.setParent(BaseActionGenerator.getBaseActionName(context));

		startClassBody();

		appendStatement("public static final String BEAN_MENU = "+quote("menu"));
		appendStatement("public static final String BEAN_QUERIES_MENU = "+quote("queriesMenu"));
		appendEmptyline();
		
		appendString("protected abstract String getTitle();");
		appendEmptyline();
		
		appendString("protected void init(ActionMapping mapping, ActionForm af, HttpServletRequest req, HttpServletResponse res) throws Exception {");
		increaseIdent();
		appendStatement("super.init(mapping, af, req, res)");
		//add check for roles
		if (view.getRequiredRoles()!=null && view.getRequiredRoles().size()>0){
			appendCommentLine("check whether user has one of following roles: "+view.getRequiredRoles());
		}
		appendStatement("prepareMenu(req)");
		append(closeBlock());
		appendEmptyline();
					
		appendString("private void prepareMenu(HttpServletRequest req){");
		increaseIdent();
		appendStatement("List<MenuItemBean> menu = new ArrayList<MenuItemBean>()");
		for (int i=0; i<sections.size(); i++){
			MetaSection section = (MetaSection)sections.get(i);
			if (section instanceof MetaModuleSection)
				appendStatement("menu.add(makeMenuItemBean("+quote(section.getTitle())+", "+quote(StrutsConfigGenerator.getPath(((MetaModuleSection)section).getDocument(), StrutsConfigGenerator.ACTION_SHOW))+"))");
			if (section instanceof MetaCustomSection)
				appendStatement("menu.add(makeMenuItemBean("+quote(section.getTitle())+", "+quote(((MetaCustomSection)section).getPath())+"))");
					
		}
		appendStatement("addBeanToRequest(req, BEAN_MENU, menu)");
		
		appendEmptyline();
		appendStatement("List<MenuItemBean> queriesMenu = new ArrayList<MenuItemBean>()");
		for (int i=0; i<sections.size(); i++){
			MetaSection section = (MetaSection)sections.get(i);
			if (section instanceof MetaModuleSection){
				MetaDocument doc = ((MetaModuleSection)section).getDocument();
				if (doc.getLinks().size()>0){
					appendStatement("queriesMenu.add(makeMenuItemBean("+quote(section.getTitle())+", "+quote(StrutsConfigGenerator.getPath(doc, StrutsConfigGenerator.ACTION_SHOW_QUERIES))+"))");
				}
			}
		}
		appendStatement("addBeanToRequest(req, BEAN_QUERIES_MENU, queriesMenu)");
		
		
		append(closeBlock());
		appendEmptyline();
		
		appendString("private MenuItemBean makeMenuItemBean(String title, String link){");
		increaseIdent();
		appendString("MenuItemBean bean = new MenuItemBean();");
		appendString("bean.setCaption(title);");
		appendString("bean.setLink(link);");
		appendString("if (title.equals(getTitle())){");
		increaseIdent();
		appendString("bean.setActive(true);");
		appendString("bean.setStyle(\"menuTitleSelected\");");
		decreaseIdent();
		appendString("}else{");
		increaseIdent();
		appendString("bean.setActive(false);");
		appendString("bean.setStyle(\"menuTitle\");");
		append(closeBlock());		
		appendString("return bean;");
		append(closeBlock());
		appendEmptyline();
		
		//security...
		appendString("protected boolean isAuthorizationRequired(){");
		increaseIdent();
		//System.out.println("Warning authorization is off.");
		appendStatement("return true");
		//ret+= writeStatement("return false");
		append(closeBlock());
		appendEmptyline();

		return clazz;
	}
}
