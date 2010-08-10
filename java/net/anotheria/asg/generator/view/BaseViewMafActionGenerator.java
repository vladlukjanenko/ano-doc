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
public class BaseViewMafActionGenerator extends AbstractGenerator {

	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.generator.IGenerator#generate(net.anotheria.anodoc.generator.IGenerateable, net.anotheria.anodoc.generator.Context)
	 */
	public FileEntry generate(IGenerateable g) {
		
		MetaView view = (MetaView)g;
		return new FileEntry(generateViewAction(view));
	}
	
	public static String getViewActionName(MetaView view){
		return "Base"+StringUtils.capitalize(view.getName())+"MafAction";
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
		clazz.addImport("javax.servlet.http.HttpServletRequest");
		clazz.addImport("javax.servlet.http.HttpServletResponse");
		clazz.addImport("net.anotheria.maf.action.ActionMapping");
		clazz.addImport("net.anotheria.maf.bean.FormBean");
		clazz.addImport("net.anotheria.webutils.bean.NavigationItemBean");

		clazz.setAbstractClass(true);
		clazz.setParent(BaseMafActionGenerator.getBaseMafActionName());
		clazz.setGeneric("T extends FormBean");
		clazz.setName(getViewActionName(view));
		
		startClassBody();

		appendStatement("public static final String BEAN_MAIN_NAVIGATION = "+quote("mainNavigation"));
		appendStatement("public static final String BEAN_QUERIES_NAVIGATION = "+quote("queriesNavigation"));
		emptyline();
		
		appendString("protected abstract String getTitle();");
		emptyline();
		
		appendString("@Override");
		appendString("protected String getActiveMainNavi() {");
		appendIncreasedStatement("return \"Content\"");
		append(closeBlock());
		emptyline();
		
		appendString("public void preProcess(ActionMapping mapping, HttpServletRequest req, HttpServletResponse res) throws Exception {");
		increaseIdent();
		appendStatement("super.preProcess(mapping, req, res)");
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
			appendStatement("List<NavigationItemBean> navigation = getMainNavigation(req)");
			appendString("for(NavigationItemBean naviItem: navigation){");
			increaseIdent();
				appendString("if(naviItem.isActive()){");
				increaseIdent();
				appendStatement("List<NavigationItemBean> subNavi = new ArrayList<NavigationItemBean>()");
				appendStatement("naviItem.setSubNavi(subNavi)");
					for (int i=0; i<sections.size(); i++){
						MetaSection section = (MetaSection)sections.get(i);
						if (section instanceof MetaModuleSection)
							appendStatement("subNavi.add(makeMenuItemBean("+quote(section.getTitle())+", "+quote(StrutsConfigGenerator.getPath(((MetaModuleSection)section).getDocument(), StrutsConfigGenerator.ACTION_SHOW))+"))");
						if (section instanceof MetaCustomSection)
							appendStatement("subNavi.add(makeMenuItemBean("+quote(section.getTitle())+", "+quote(((MetaCustomSection)section).getPath())+"))");
								
					}
				append(closeBlock());
			append(closeBlock());
			
		appendStatement("addBeanToRequest(req, BEAN_MAIN_NAVIGATION, navigation)");
		emptyline();
		
		appendStatement("List<NavigationItemBean> queriesMenu = new ArrayList<NavigationItemBean>()");
		for (int i=0; i<sections.size(); i++){
			MetaSection section = (MetaSection)sections.get(i);
			if (section instanceof MetaModuleSection){
				MetaDocument doc = ((MetaModuleSection)section).getDocument();
				if (doc.getLinks().size()>0){
					appendStatement("queriesMenu.add(makeMenuItemBean("+quote(section.getTitle())+", "+quote(StrutsConfigGenerator.getPath(doc, StrutsConfigGenerator.ACTION_SHOW_QUERIES))+"))");
				}
			}
		}
		appendStatement("addBeanToRequest(req, BEAN_QUERIES_NAVIGATION, queriesMenu)");
		
		
		append(closeBlock());
		emptyline();

		appendString("private NavigationItemBean makeMenuItemBean(String title, String link){");
		increaseIdent();
		appendString("NavigationItemBean bean = new NavigationItemBean();");
		appendString("bean.setCaption(title);");
		appendString("bean.setLink(link);");
		appendString("bean.setActive(title.equals(getTitle()));");
		appendString("return bean;");
		decreaseIdent();
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
