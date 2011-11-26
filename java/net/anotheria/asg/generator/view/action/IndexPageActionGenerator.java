package net.anotheria.asg.generator.view.action;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.view.CMSMappingsConfiguratorGenerator;
import net.anotheria.asg.generator.view.meta.MetaView;
import net.anotheria.util.Date;

/**
 * Generator class for the index page action in cms.
 * 
 * @author abolbat
 */
public class IndexPageActionGenerator extends AbstractGenerator {

	
	public List<FileEntry> generate(List<MetaView> views) {
		List<FileEntry> files = new ArrayList<FileEntry>();
		files.add(new FileEntry(generateBaseAction(views)));
		files.add(new FileEntry(generateDocumentChangeFBviews(views)));
		return files;
	}

	public static String getIndexPagePackageName() {
		return GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED) + ".action";
	}
	
	public static String getDocumentChangeFBPackageName() {
		return GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED) + ".bean";
	}
	
	public static String getIndexPageActionName() {
		return "WelcomePageMafAction";
	}
	
	public static String getDocumentChangeFBName() {
		return "DocumentChangeFB";
	}
	
	public static String getIndexPageFullName() {
		return getIndexPagePackageName() + "." + getIndexPageActionName();
	}
	
	public static String getDocumentChangeFBFullName() {
		return getDocumentChangeFBPackageName() + "." + getDocumentChangeFBName();
	}

	public GeneratedClass generateBaseAction(List<MetaView> views) {
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		appendGenerationPoint("generateBaseAction");
		clazz.setPackageName(getIndexPagePackageName());

		
		clazz.addImport("javax.servlet.http.HttpServletRequest");
		clazz.addImport("javax.servlet.http.HttpServletResponse");
		emptyline();
		clazz.addImport("net.anotheria.maf.action.ActionForward");
		clazz.addImport("net.anotheria.maf.action.ActionMapping");
		clazz.addImport("net.anotheria.maf.bean.FormBean");
		clazz.addImport("net.anotheria.webutils.bean.NavigationItemBean");
		clazz.addImport("net.anotheria.asg.util.DocumentChange");
		clazz.addImport("net.anotheria.asg.util.CmsChangesTracker");
		clazz.addImport(CMSMappingsConfiguratorGenerator.getClassName());
		clazz.addImport(getDocumentChangeFBPackageName() + ".DocumentChangeFB");
		clazz.addImport("java.util.Collections");
		clazz.addImport("java.util.Collection");
		clazz.addImport("java.util.List");
		clazz.addImport("java.util.ArrayList");
		clazz.addImport(Date.class);
		
		
		
		
		clazz.setParent(BaseActionGenerator.getBaseActionName());
		clazz.setName(getIndexPageActionName());

		startClassBody();

		appendString("protected boolean isAuthorizationRequired() {");
		increaseIdent();
		appendStatement("return true");
		append(closeBlock());
		emptyline();

		appendString("public ActionForward anoDocExecute(ActionMapping aMapping, FormBean aAf, HttpServletRequest aReq, HttpServletResponse aRes) throws Exception {");
		increaseIdent();
		appendStatement("addBeanToRequest(aReq, FLAG_DISABLED_SEARCH, true)");
		
		appendString("Collection<DocumentChange> changes = CmsChangesTracker.getChanges();");
		appendString("List<DocumentChangeFB> myList = new ArrayList<DocumentChangeFB>();");
		appendString("for (net.anotheria.asg.util.DocumentChange el:changes){");
		appendString("DocumentChangeFB dcFB = new DocumentChangeFB();");
		appendString("dcFB.setUserName(el.getUserName());");
		appendString("dcFB.setDocumentName(el.getDocumentName());");
		appendString("dcFB.setParentName(el.getParentName());");
		appendString("dcFB.setDate(new Date(el.getTimestamp()));");
		appendString("dcFB.setAction(el.getAction().toString());");
		appendString("dcFB.setId(el.getId());");
		appendString("dcFB.setDocumentLink(CMSMappingsConfigurator.getActionPath(el.getParentName(), el.getDocumentName()));");
		appendString("myList.add(dcFB);");
		closeBlock("closed for");
		appendString("aReq.setAttribute(\"changes\", myList);");
		appendStatement("return aMapping.findForward(\"success\")");
		append(closeBlock());
		emptyline();
		
		appendString("@Override");
		increaseIdent();
		appendStatement("protected String getActiveMainNavi() {");
		appendStatement("return null");
		append(closeBlock());
		emptyline();
		
		appendString("@Override");
		appendString("protected List<NavigationItemBean> getSubNavigation() {");
		increaseIdent();
		appendStatement("return Collections.emptyList()");
		append(closeBlock());
		emptyline();
		
		appendString("@Override");
		appendString("protected String getCurrentDocumentDefName() {");
		increaseIdent();
		appendStatement("return null");
		closeBlock("getCurrentDocumentDefName");
		emptyline();
		
		appendString("@Override");
		appendString("protected String getCurrentModuleDefName() {");
		increaseIdent();
		appendStatement("return null");
		closeBlock("getCurrentModuleDefName");
		emptyline();
		
		return clazz;
	}
	
	public GeneratedClass generateDocumentChangeFBviews(List<MetaView> views) {
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		appendGenerationPoint("generateDocumentChangeFBviews");
		clazz.setPackageName(getDocumentChangeFBPackageName());
		clazz.addImport("net.anotheria.maf.bean.FormBean");
		clazz.addImport("net.anotheria.util.Date");
		

		clazz.addInterface("FormBean");
		clazz.setName(getDocumentChangeFBName());

		startClassBody();
		
		appendString("private String userName;");
		appendString("private String documentName;");
		appendString("private String parentName;");
		appendString("private String documentLink;");
		appendString("private String action;");
		appendString("private Date date;");
		appendString("private String id;");
		emptyline();
		increaseIdent();
		appendString("public String getAction() {");
		appendIncreasedString("return action;");
		closeBlock("");
		emptyline();
		increaseIdent();
		appendString("public void setAction(String action) {");
		appendIncreasedString("this.action = action;");
		closeBlock("");
		emptyline();
		increaseIdent();
		appendString("public String getUserName() {");
		appendIncreasedString("return userName;");
		closeBlock("");
		emptyline();
		increaseIdent();
		appendString("public void setUserName(String userName) {");
		appendIncreasedString("this.userName = userName;");
		closeBlock("");
		emptyline();
		appendString("public String getId() {");
		appendIncreasedString("return id;");
		closeBlock("");
		emptyline();
		increaseIdent();
		appendString("public void setId(String id) {");
		appendIncreasedString("this.id = id;");
		closeBlock("");
		emptyline();
		increaseIdent();
		appendString("public String getDocumentName() {");
		appendIncreasedString("return documentName;");
		closeBlock("");
		emptyline();
		increaseIdent();
		appendString("public void setDocumentName(String documentName) {");
		appendIncreasedString("this.documentName = documentName;");
		closeBlock("");
		emptyline();
		increaseIdent();
		appendString("public String getParentName() {");
		appendIncreasedString("return parentName;");
		closeBlock("");
		emptyline();
		increaseIdent();
		appendString("public void setParentName(String parentName) {");
		appendIncreasedString("this.parentName = parentName;");
		closeBlock("");
		emptyline();
		increaseIdent();
		appendString("public String getDocumentLink() {");
		appendIncreasedString("return documentLink;");
		closeBlock("");
		emptyline();
		increaseIdent();
		appendString("public void setDocumentLink(String documentLink) {");
		appendIncreasedString("this.documentLink = documentLink;");
		closeBlock("");
		emptyline();
		increaseIdent();
		appendString("public Date getDate() {");
		appendIncreasedString("return date;");
		closeBlock("");
		emptyline();
		increaseIdent();
		appendString("public void setDate(Date date) {");
		appendIncreasedString("this.date = date;");
		closeBlock("");
		emptyline();
	
		
		emptyline();
		return clazz;
	}

}
