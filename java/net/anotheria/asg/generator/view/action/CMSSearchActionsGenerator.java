package net.anotheria.asg.generator.view.action;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.view.CMSMappingsConfiguratorGenerator.SharedAction;
import net.anotheria.asg.generator.view.meta.MetaModuleSection;
import net.anotheria.asg.generator.view.meta.MetaSection;
import net.anotheria.asg.generator.view.meta.MetaView;

import java.util.ArrayList;
import java.util.List;

/**
 * Generator for SMC search action.
 */
public class CMSSearchActionsGenerator extends AbstractGenerator {

	public List<FileEntry> generate(List<MetaView> views) {
		List<FileEntry> files = new ArrayList<FileEntry>();
		files.add(new FileEntry(generateSearchAction(views)));
		files.add(new FileEntry(generateSearchFB()));
		return files;
	}

	public static String getSearchPackageName() {
		return SharedAction.getPackageName();
	}

	public static String getCmsSearchActionName() {
		return SharedAction.SEARCH.getClassName();
	}

	public static String getSearchPageFullName() {
		return getSearchPackageName() + "." + getCmsSearchActionName();
	}

	private GeneratedClass generateSearchAction(List<MetaView> views) {
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		appendGenerationPoint("generateSearchAction");
		clazz.setPackageName(getSearchPackageName());

		clazz.addImport("java.util.ArrayList");
		clazz.addImport("java.util.List");
		clazz.addImport("java.util.Collections");


		clazz.addImport("javax.servlet.http.HttpServletRequest");
		clazz.addImport("javax.servlet.http.HttpServletResponse");
		clazz.addImport("net.anotheria.anodoc.query2.DocumentQuery");
		clazz.addImport("net.anotheria.anodoc.query2.QueryResult");
		clazz.addImport("net.anotheria.anodoc.query2.QueryResultEntry");
		clazz.addImport("net.anotheria.anodoc.query2.ResultEntryBean");
		clazz.addImport("net.anotheria.anodoc.query2.string.ContainsStringQuery");
		clazz.addImport("net.anotheria.asg.exception.ASGRuntimeException");
		clazz.addImport(getSearchFBFullName());
		clazz.addImport("net.anotheria.maf.action.ActionForward");
		clazz.addImport("net.anotheria.maf.action.ActionMapping");
		clazz.addImport("net.anotheria.maf.bean.FormBean");
		clazz.addImport("net.anotheria.maf.bean.annotations.Form");
		clazz.addImport("net.anotheria.webutils.bean.NavigationItemBean");
		clazz.addImport("net.anotheria.util.StringUtils");
		clazz.addImport("net.anotheria.asg.data.DataObject");

		clazz.setParent(BaseActionGenerator.getBaseActionName(), "SearchFB");
		clazz.setName(getCmsSearchActionName());

		startClassBody();

		appendString("@Override");
		appendString("public ActionForward execute(ActionMapping mapping, @Form(SearchFB.class) FormBean formBean, HttpServletRequest req, HttpServletResponse res) throws Exception{");
		increaseIdent();
		appendString("return super.execute(mapping, formBean, req, res);");
		closeBlock("");
		emptyline();

		appendString("public ActionForward anoDocExecute(ActionMapping mapping, SearchFB formBean, HttpServletRequest req, HttpServletResponse res) throws Exception{");
		increaseIdent();
		appendString("DocumentQuery query = new ContainsStringQuery(\"*\" + formBean.getCriteria() + \"*(\\r\\n)?\");");
		appendString("QueryResult result = executeQuery(formBean.getModule(), formBean.getDocument(), query, formBean.getSearchArea());");
		appendStatement("addBeanToRequest(req, BEAN_DOCUMENT_DEF_NAME, formBean.getDocument())");
		appendStatement("addBeanToRequest(req, BEAN_MODULE_DEF_NAME, formBean.getModule())");
		appendStatement("req.getSession().setAttribute(BEAN_SEARCH_SCOPE, formBean.getSearchArea())");
		appendString("if (result.getEntries().size()==0){");
		increaseIdent();
		appendString("req.setAttribute(\"srMessage\", \"Nothing found.\");");
		decreaseIdent();
		appendString("}else{");
		increaseIdent();
		appendString("List<ResultEntryBean> beans = new ArrayList<ResultEntryBean>(result.getEntries().size());");
		appendString("for (QueryResultEntry entry: result.getEntries()){");
		increaseIdent();
		appendStatement("DataObject doc = (DataObject)entry.getMatchedDocument()");
		appendString("ResultEntryBean bean = new ResultEntryBean();");

		appendString("bean.setEditLink(doc.getDefinedParentName().toLowerCase() + StringUtils.capitalize(doc.getDefinedName()) + \"Edit?pId=\" + doc.getId() + \"&ts=\" + System.currentTimeMillis());");

		appendString("bean.setDocumentId(entry.getMatchedDocument().getId());");
		appendString("bean.setDocumentName(doc.getDefinedName());");
		appendString("bean.setPropertyName(entry.getMatchedProperty().getId());");
		appendString("bean.setInfo(entry.getInfo().toHtml());");
		appendString("beans.add(bean);");
		closeBlock("");
		appendString("req.setAttribute(\"result\", beans);");
		closeBlock("");
		appendString("req.setAttribute(\"criteria\", formBean.getCriteria());");
		appendString("return mapping.findForward(\"success\");");
		closeBlock("");
		emptyline();
		appendString("private QueryResult executeQuery(String sectionName, String documentName, DocumentQuery query, String searchArea) throws ASGRuntimeException{");
		increaseIdent();
		appendStatement("QueryResult ret = new QueryResult()");
		appendStatement("boolean wholeCms = \"cms\".equals(searchArea)");
		appendStatement("boolean wholeSection = wholeCms || \"section\".equals(searchArea)");

		for(MetaView view: views){
			emptyline();
			appendString("if(wholeCms || sectionName.equals(\""+view.getTitle()+"\")){");
			increaseIdent();
			for(MetaSection section: view.getSections()){
				if(!(section instanceof MetaModuleSection))
					continue;
				MetaModuleSection s = (MetaModuleSection)section;
				increaseIdent();
				appendString("if(wholeSection || documentName.equals(\""+s.getDocument().getName()+"\"))");
				appendIncreasedString("ret.add(get"+s.getModule().getName()+"Service().executeQueryOn"+s.getDocument().getName(true)+"(query).getEntries());");
				decreaseIdent();
			}
			closeBlock("if");
		}

		emptyline();
		appendStatement("return ret");
		closeBlock("executeQuery");
		emptyline();
		appendString("@Override");
		appendString("protected String getActiveMainNavi() {");
		increaseIdent();
		appendStatement("return null");
		closeBlock("");
		emptyline();
		appendString("@Override");
		appendString("protected List<NavigationItemBean> getSubNavigation(){");
		increaseIdent();
		appendStatement("return Collections.emptyList()");
		closeBlock("");
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

	public static String getSearchFBPackageName() {
		return GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED) + ".bean";
	}

	public static String getSearchFBName() {
		return "SearchFB";
	}

	public static String getSearchFBFullName() {
		return getSearchFBPackageName() + "." + getSearchFBName();
	}

	private GeneratedClass generateSearchFB() {
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		appendGenerationPoint("generateSearchFB");
		clazz.setPackageName(getSearchFBPackageName());

		clazz.addImport("net.anotheria.maf.bean.FormBean");

		clazz.addInterface("FormBean");
		clazz.setName(getSearchFBName());

		startClassBody();

		appendString("private String criteria;");
		appendString("private String module;");
		appendString("private String document;");
		appendString("private String searchArea;");
		emptyline();
		increaseIdent();
		appendString("public String getCriteria() {");
		appendIncreasedString("return criteria;");
		closeBlock("");
		emptyline();
		increaseIdent();
		appendString("public void setCriteria(String criteria) {");
		appendIncreasedString("this.criteria = criteria;");
		closeBlock("");
		emptyline();
		increaseIdent();
		appendString("public String getModule() {");
		appendIncreasedString("return module;");
		closeBlock("");
		emptyline();
		increaseIdent();
		appendString("public void setModule(String module) {");
		appendIncreasedString("this.module = module;");
		closeBlock("");
		emptyline();
		increaseIdent();
		appendString("public String getDocument() {");
		appendIncreasedString("return document;");
		closeBlock("");
		emptyline();
		increaseIdent();
		appendString("public void setDocument(String document) {");
		appendIncreasedString("this.document = document;");
		closeBlock("");
		emptyline();
		increaseIdent();
		appendString("public String getSearchArea() {");
		appendIncreasedString("return searchArea;");
		closeBlock("");
		emptyline();
		increaseIdent();
		appendString("public void setSearchArea(String searchArea) {");
		appendIncreasedString("this.searchArea = searchArea;");
		closeBlock("");
		emptyline();


		emptyline();
		return clazz;
	}


}
