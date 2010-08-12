package net.anotheria.asg.generator.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
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
	public FileEntry generate(IGenerateable g) {
		
		MetaView view = (MetaView)g;
		return new FileEntry(generateViewAction(view));
	}
	
	public static String getViewActionName(MetaView view){
		return "Base"+StringUtils.capitalize(view.getName())+"Action";
	}
	
	public GeneratedClass generateViewAction(MetaView view){

		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		clazz.setPackageName(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED)+".action");
		
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
		clazz.setParent(BaseActionGenerator.getBaseActionName());

		startClassBody();

		appendStatement("public static final String BEAN_MENU = "+quote("menu"));
		appendStatement("public static final String BEAN_QUERIES_MENU = "+quote("queriesMenu"));
		emptyline();
		
		appendString("protected abstract String getTitle();");
		emptyline();
		
		appendString("protected void init(ActionMapping mapping, ActionForm af, HttpServletRequest req, HttpServletResponse res) throws Exception {");
		increaseIdent();
		appendStatement("super.init(mapping, af, req, res)");
		appendStatement("prepareMenu(req)");
		append(closeBlock());
		emptyline();
		
		if (view.getRequiredRoles()!=null && view.getRequiredRoles().size()>0){
			clazz.addImport(Arrays.class);
			String roles = "";
			for (String r : view.getRequiredRoles()){
				if (roles.length()>0)
					roles += ", ";
				roles += quote(r);
			}
			appendStatement("private static final List<String> MY_ROLES = Arrays.asList(new String[]{"+roles+"})");
			appendString("protected List<String> getRequiredRoles(){");
			appendIncreasedStatement("return MY_ROLES");
			appendString("}");
		}
					
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
		
		emptyline();
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
		emptyline();

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
		emptyline();

		//security...
		appendString("protected boolean isAuthorizationRequired(){");
		increaseIdent();
		appendStatement("return true");
		append(closeBlock());
		emptyline();

		return clazz;
	}
}
