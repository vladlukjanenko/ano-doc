package net.anotheria.asg.generator.view.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.view.CMSMappingsConfiguratorGenerator;
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
		clazz.addImport("net.anotheria.maf.bean.FormBean");
		clazz.addImport("net.anotheria.webutils.bean.NavigationItemBean");

		clazz.setAbstractClass(true);
		clazz.setParent(BaseActionGenerator.getBaseActionName());
		clazz.setGeneric("T extends FormBean");
		clazz.setName(getViewActionName(view));
		
		startClassBody();

		
		appendString("protected abstract String getTitle();");
		emptyline();
		
		appendString("@Override");
		appendString("protected String getActiveMainNavi() {");
		increaseIdent();
		appendStatement("return \""+StringUtils.capitalize(view.getTitle())+"\"");
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
			
		emptyline();		
			appendStatement("protected List<NavigationItemBean> getSubNavigation(){");
			appendStatement("List<NavigationItemBean> subNavi = new ArrayList<NavigationItemBean>()");
			increaseIdent();		
				for (int i=0; i<sections.size(); i++){
						MetaSection section = (MetaSection)sections.get(i);
						if (section instanceof MetaModuleSection)
							appendStatement("subNavi.add(makeMenuItemBean("+quote(section.getTitle())+", "+quote(CMSMappingsConfiguratorGenerator.getPath(((MetaModuleSection)section).getDocument(), CMSMappingsConfiguratorGenerator.ACTION_SHOW))+"))");
						if (section instanceof MetaCustomSection)
							appendStatement("subNavi.add(makeMenuItemBean("+quote(section.getTitle())+", "+quote(((MetaCustomSection)section).getPath())+"))");
								
					}
					appendStatement("return subNavi");
			append(closeBlock());
			
		emptyline();
		
		appendString("private NavigationItemBean makeMenuItemBean(String title, String link){");
		increaseIdent();
		appendString("NavigationItemBean bean = new NavigationItemBean();");
		appendString("bean.setCaption(title);");
		appendString("bean.setLink(link);");
		appendString("bean.setActive(title.equals(getTitle()));");
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
