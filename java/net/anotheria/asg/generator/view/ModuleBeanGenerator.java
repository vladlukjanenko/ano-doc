/* ------------------------------------------------------------------------- *
$Source: /work/cvs/ano-doc/java/net/anotheria/asg/generator/view/ModuleBeanGenerator.java,v $
$Author: lrosenberg $
$Date: 2007/06/19 14:14:59 $
$Revision: 1.13 $


Copyright 2004-2005 by FriendScout24 GmbH, Munich, Germany.
All rights reserved.

This software is the confidential and proprietary information
of FriendScout24 GmbH. ("Confidential Information").  You
shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement
you entered into with FriendScout24 GmbH.
See www.friendscout24.de for details.
** ------------------------------------------------------------------------- */
package net.anotheria.asg.generator.view;

import java.util.ArrayList;
import java.util.List;

import sun.rmi.transport.ObjectTable;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.forms.meta.MetaForm;
import net.anotheria.asg.generator.forms.meta.MetaFormField;
import net.anotheria.asg.generator.forms.meta.MetaFormSingleField;
import net.anotheria.asg.generator.forms.meta.MetaFormTableColumn;
import net.anotheria.asg.generator.forms.meta.MetaFormTableField;
import net.anotheria.asg.generator.meta.MetaContainerProperty;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaEnumerationProperty;
import net.anotheria.asg.generator.meta.MetaListProperty;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.MetaProperty;
import net.anotheria.asg.generator.meta.MetaTableProperty;
import net.anotheria.asg.generator.meta.ObjectType;
import net.anotheria.asg.generator.view.meta.MetaDialog;
import net.anotheria.asg.generator.view.meta.MetaFieldElement;
import net.anotheria.asg.generator.view.meta.MetaFunctionElement;
import net.anotheria.asg.generator.view.meta.MetaModuleSection;
import net.anotheria.asg.generator.view.meta.MetaView;
import net.anotheria.asg.generator.view.meta.MetaViewElement;
import net.anotheria.asg.generator.view.meta.MultilingualFieldElement;
import net.anotheria.util.ExecutionTimer;
import net.anotheria.util.StringUtils;

/**
 * TODO Please remain lrosenberg to comment BeanGenerator.java
 * @author lrosenberg
 * @created on Feb 25, 2005
 */
public class ModuleBeanGenerator extends AbstractGenerator implements IGenerator {
    
	//private MetaView view;
	
	private Context context;
    
	/**
	 * Implementation is moved into ano-web, the constant remains.
	 */
	public static final String FLAG_FORM_SUBMITTED = "formSubmittedFlag";

    public ModuleBeanGenerator(MetaView aView){
        //view = aView;
    }
	

	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.generator.IGenerator#generate(net.anotheria.anodoc.generator.IGenerateable, net.anotheria.anodoc.generator.Context)
	 */
	public List<FileEntry> generate(IGenerateable g, Context context) {
		this.context = context;
		List<FileEntry> files = new ArrayList<FileEntry>();
		
		MetaModuleSection section = (MetaModuleSection)g;
		
		//System.out.println("Generate section: "+section);
		
		ExecutionTimer timer = new ExecutionTimer("BeanGenerator");
		timer.startExecution("All");

		timer.startExecution(section.getModule().getName()+"-"+section.getTitle()+"-ListItem");
		files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getListItemBeanName(section.getDocument()), generateListItemBean(section)));
		timer.stopExecution(section.getModule().getName()+"-"+section.getTitle()+"-ListItem");
		String sortTypeContent = generateListItemSortType(section);
		if (sortTypeContent!=null)
			files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getListItemBeanSortTypeName(section.getDocument()), sortTypeContent));
		List<MetaDialog> dialogs = section.getDialogs();
		for (int i=0; i<dialogs.size(); i++){
			MetaDialog dlg = dialogs.get(i);
			files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getDialogBeanName(dlg, section.getDocument()), generateDialogForm(dlg, section.getDocument())));
			
			MetaDocument doc = section.getDocument();
			for (int p=0; p<doc.getProperties().size(); p++){
				MetaProperty pp = doc.getProperties().get(p);
				if (pp instanceof MetaContainerProperty){
					files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getContainerEntryFormName((MetaContainerProperty)pp), generateContainerEntryForm(doc, (MetaContainerProperty)pp)));
					files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getContainerQuickAddFormName((MetaContainerProperty)pp), generateContainerQuickAddForm(doc, (MetaContainerProperty)pp)));
				}
			}

		}
	//	files.add(new FileEntry(FileEntry.package2path(getPackage()), getShowActionName(section), generateShowAction(section)));
		//files.add(new FileEntry(FileEntry.package2path(getPackage()), getDeleteActionName(section), generateDeleteAction(section)));
		
		timer.stopExecution("All");
//		timer.printExecutionTimesOrderedByCreation();
		
		
		
		return files;
	}
	
	
	
	public static String getListItemBeanName(MetaDocument doc){
		return doc.getName()+"ListItemBean";
	}
	
	public static String getListItemBeanSortTypeName(MetaDocument doc){
		return getListItemBeanName(doc)+"SortType";
	}
	
	public static String getContainerEntryFormName(MetaContainerProperty p){
		return StringUtils.capitalize(p.getName())+p.getContainerEntryName()+"Form";
	}

	public static String getContainerQuickAddFormName(MetaContainerProperty p){
		return StringUtils.capitalize(p.getName())+"QuickAddForm";
	}

	public static String getContainerEntryFormImport(MetaDocument doc, MetaContainerProperty p){
		return GeneratorDataRegistry.getInstance().getContext().getPackageName(doc)+".bean."+getContainerEntryFormName(p);
	}
	
	public static String getContainerQuickAddFormImport(MetaDocument doc, MetaContainerProperty p){
		return GeneratorDataRegistry.getInstance().getContext().getPackageName(doc)+".bean."+getContainerQuickAddFormName(p);
	}

	private String generateContainerEntryForm(MetaDocument doc, MetaContainerProperty p){
		if (p instanceof MetaTableProperty)
			return generateTableRowForm(doc, (MetaTableProperty)p);
		if (p instanceof MetaListProperty)
			return generateListElementForm(doc, (MetaListProperty)p);
		throw new RuntimeException("Unsupported container type: "+p);
	}
	
	private String generateContainerQuickAddForm(MetaDocument doc, MetaContainerProperty p){
		if (p instanceof MetaListProperty)
			return generateListQuickAddForm(doc, (MetaListProperty)p);
		System.out.println("WARN Unsupported container type: "+p);
		return "";
	}

	private String generateListElementForm(MetaDocument doc, MetaListProperty list){
		String ret = "";
		ret += writeStatement("package "+getPackage(doc));
		ret += emptyline();
		ret += writeImport("net.anotheria.webutils.bean.BaseActionForm");
		ret += writeImport("javax.servlet.http.HttpServletRequest");
		ret += writeImport("org.apache.struts.action.ActionMapping");
		ret += emptyline();
		
		if (list.getContainedProperty().isLinked()){
			ret += writeImport("java.util.List;");
			ret += emptyline();
		}
		
		
		List<MetaProperty> elements = new ArrayList<MetaProperty>();
		elements.add(new MetaProperty("ownerId","string"));
		elements.add(new MetaProperty("position","int"));
		elements.add(list.getContainedProperty());
		elements.add(new MetaProperty("description","string"));

		ret += writeString("public class "+getContainerEntryFormName(list)+" extends BaseActionForm{");
		increaseIdent();
		
		for (int i=0; i<elements.size(); i++){
			MetaProperty p = elements.get(i);
			ret += writeStatement("private "+p.toJavaType()+" "+p.getName());
			if (p.isLinked()){
				MetaProperty collection = new MetaProperty(p.getName()+"Collection","list");
				ret += writeStatement("private "+collection.toJavaType()+" "+collection.getName());
			}
		}
		
		ret += emptyline();
		for (int i=0; i<elements.size(); i++){
			MetaProperty p = elements.get(i);
			ret += generateMethods(null, p);
			if (p.isLinked()){
				String propName = p.getName()+"Collection";
				ret += writeStatement("public void set"+StringUtils.capitalize(propName)+"(List l){");
				increaseIdent();
				ret += writeStatement(propName+" = l");
				ret += closeBlock();
				ret += emptyline();
				ret += writeStatement("public List get"+StringUtils.capitalize(propName)+"(){");
				increaseIdent();
				ret += writeStatement("return "+propName);
				ret += closeBlock();
				ret += emptyline();
			}
			
		}
		ret += emptyline();
		
		

		//generate encoding.
		ret += writeString("public void reset( ActionMapping mapping, HttpServletRequest request ){");
		increaseIdent();
		ret += writeString("try {");
		increaseIdent();
		ret += writeStatement("request.setCharacterEncoding( "+quote(GeneratorDataRegistry.getInstance().getContext().getEncoding())+")");
		ret += closeBlock();
		ret += writeString("catch ( java.io.UnsupportedEncodingException e ) {}");
		ret += closeBlock();
		
		ret += closeBlock();

		return ret;
	}

	private String generateListQuickAddForm(MetaDocument doc, MetaListProperty list){
		String ret = "";
		ret += writeStatement("package "+getPackage(doc));
		ret += emptyline();
		ret += writeImport("net.anotheria.webutils.bean.BaseActionForm");
		ret += writeImport("javax.servlet.http.HttpServletRequest");
		ret += writeImport("org.apache.struts.action.ActionMapping");
		ret += emptyline();
		
		ret += writeString("public class "+getContainerQuickAddFormName(list)+" extends BaseActionForm{");
		increaseIdent();
		
		ret += writeStatement("private String quickAddIds");
		ret += writeStatement("private String ownerId");
		
		ret += emptyline();
		ret += writeString("public void setQuickAddIds(String someIds){");
		ret += writeIncreasedStatement("quickAddIds = someIds");
		ret += writeString("}");
		ret += emptyline();
		ret += writeString("public String getQuickAddIds(){");
		ret += writeIncreasedStatement("return quickAddIds");
		ret += writeString("}");
		
		ret += emptyline();
		
		ret += emptyline();
		ret += writeString("public void setOwnerId(String anId){");
		ret += writeIncreasedStatement("ownerId = anId");
		ret += writeString("}");
		ret += emptyline();
		ret += writeString("public String getOwnerId(){");
		ret += writeIncreasedStatement("return ownerId");
		ret += writeString("}");
		ret += emptyline();
		

		//generate encoding.
		ret += writeString("public void reset( ActionMapping mapping, HttpServletRequest request ){");
		increaseIdent();
		ret += writeString("try {");
		increaseIdent();
		ret += writeStatement("request.setCharacterEncoding( "+quote(GeneratorDataRegistry.getInstance().getContext().getEncoding())+")");
		ret += closeBlock();
		ret += writeString("catch ( java.io.UnsupportedEncodingException e ) {}");
		ret += closeBlock();
		
		ret += closeBlock();

		return ret;
	}

	@SuppressWarnings("unchecked")
	private String generateTableRowForm(MetaDocument doc, MetaTableProperty p){
		String ret = "";
		ret += writeStatement("package "+getPackage(doc));
		ret += emptyline();
		ret += writeImport("net.anotheria.webutils.bean.BaseActionForm");
		ret += writeImport("javax.servlet.http.HttpServletRequest");
		ret += writeImport("org.apache.struts.action.ActionMapping");
		ret += emptyline();
		
		ret += writeString("public class "+getContainerEntryFormName(p)+" extends BaseActionForm{");
		increaseIdent();
		List<MetaProperty> columns = (List<MetaProperty>)((ArrayList)p.getColumns()).clone();
		columns.add(0, new MetaProperty(p.getName()+"_ownerId", "string"));
		columns.add(0, new MetaProperty(p.getName()+"_position", "int"));
		for (MetaProperty pr : columns)
			ret += writeStatement("private String "+p.extractSubName(pr));
		
		ret += emptyline();
		for (int i=0; i<columns.size(); i++){
			MetaProperty pr = columns.get(i);
			ret += writeString("public void set"+StringUtils.capitalize(p.extractSubName(pr))+"(String a"+StringUtils.capitalize(p.extractSubName(pr))+" ){");
			increaseIdent();
			ret += writeStatement("this."+p.extractSubName(pr)+" = a"+StringUtils.capitalize(p.extractSubName(pr)));
			ret += closeBlock();
			ret += writeString("public String get"+StringUtils.capitalize(p.extractSubName(pr))+"(){");
			increaseIdent();
			ret += writeStatement("return "+p.extractSubName(pr));
			ret += closeBlock();
			ret += emptyline();
		}

		//generate encoding.
		ret += writeString("public void reset( ActionMapping mapping, HttpServletRequest request ){");
		increaseIdent();
		ret += writeString("try {");
		increaseIdent();
		ret += writeStatement("request.setCharacterEncoding( "+quote(GeneratorDataRegistry.getInstance().getContext().getEncoding())+")");
		ret += closeBlock();
		ret += writeString("catch ( java.io.UnsupportedEncodingException e ) {}");
		ret += closeBlock();
		
		ret += closeBlock();
		return ret;
	}
	
	
	String generateDialogForm(MetaDialog dialog, MetaDocument doc){
		String ret = "";
		
		ret += writeStatement("package "+getPackage(doc));
		ret += emptyline();
		ret += writeImport("net.anotheria.webutils.bean.BaseActionForm");
		ret += writeImport("javax.servlet.http.HttpServletRequest");
		ret += writeImport("org.apache.struts.action.ActionMapping");
		
		ret += emptyline();

		List<MetaViewElement> elements = createMultilingualList(dialog.getElements(), doc, context);

		for (MetaViewElement element : elements){
			if (element instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)element;
				MetaProperty p = doc.getField(field.getName());
				if (p.isLinked() || p instanceof MetaEnumerationProperty){
					ret += writeImport("java.util.List");
					ret += writeImport("net.anotheria.webutils.bean.LabelValueBean");
					ret += emptyline();
					break;
				}
			}
		}
		
		ret += writeString("public class "+getDialogBeanName(dialog, doc)+" extends BaseActionForm{");
		increaseIdent();
	
		for (MetaViewElement element : elements){
			if (element instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)element;
				String lang = getElementLanguage(field);
				
				MetaProperty p = doc.getField(field.getName());
				MetaProperty tmp = p instanceof MetaListProperty? new MetaProperty(p.getName(),"int"): p;
				ret += writeStatement("private "+tmp.toJavaType()+" "+tmp.getName(lang));
				if (p.isLinked()){
					MetaProperty collection = new MetaProperty(p.getName()+"Collection"+(lang==null?"":lang),"list");
					ret += writeStatement("private "+collection.toJavaType()+"<LabelValueBean> "+collection.getName());//hacky
					ret += writeStatement("private String "+p.getName()+"CurrentValue"+(lang==null?"":lang));
				}
				
				if (p instanceof MetaEnumerationProperty){
					MetaProperty collection = new MetaProperty(p.getName()+"Collection","list");
					ret += writeStatement("private "+collection.toJavaType()+"<LabelValueBean> "+collection.getName());//hacky
					ret += writeStatement("private String "+p.getName()+"CurrentValue");
				}
			}
			
		}

		ret += emptyline();
		for (MetaViewElement element : elements){
			if (element instanceof MetaFieldElement)
				ret += generateFieldMethodsInDialog((MetaFieldElement)element, doc);
			
		}
		
		ret += emptyline();
		
		//generate encoding.
		ret += writeString("public void reset( ActionMapping mapping, HttpServletRequest request ){");
		increaseIdent();
		ret += writeString("try {");
		increaseIdent();
		ret += writeStatement("request.setCharacterEncoding( "+quote(GeneratorDataRegistry.getInstance().getContext().getEncoding())+")");
		ret += closeBlock();
		ret += writeString("catch ( java.io.UnsupportedEncodingException e ) {}");
		ret += closeBlock();
		  
		
		ret += closeBlock();		
		
		return ret;
	}
	
	private String generateListItemSortType(MetaModuleSection section){
		List<MetaViewElement> elements = section.getElements();
		boolean containsComparable = false;
		for (MetaViewElement element : elements){
			if (element.isComparable()){
				containsComparable = true;
				break;
			}
		}

		if (!containsComparable)
			return null;
			
		String ret = "";
		ret += writeStatement("package "+getPackage(section.getDocument()));
		ret += emptyline();
		ret += writeImport("net.anotheria.util.sorter.SortType");
		ret += emptyline();
		
		ret += writeString("public class "+getListItemBeanSortTypeName(section.getDocument())+" extends SortType{");
		increaseIdent();
		
		
		MetaViewElement defaultElem = section.getDefaultSortable();
		String defaultElemName = null;
		int lastIndex = 1;
		
		elements = createMultilingualList(elements, section.getDocument(), context);
		
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			if (element.isComparable()){
				if (element.equals(defaultElem)){
					if (element instanceof MultilingualFieldElement){
						defaultElemName = "SORT_BY_"+section.getDocument().getField(element.getName()).getName(context.getDefaultLanguage()).toUpperCase();
					}else{					
						defaultElemName = "SORT_BY_"+element.getName().toUpperCase();
					}
				}
				if (element instanceof MultilingualFieldElement){
					MetaProperty p = section.getDocument().getField(element.getName());
  					ret += writeStatement("public static final int SORT_BY_"+p.getName(((MultilingualFieldElement)element).getLanguage()).toUpperCase()+" = "+(lastIndex++));
				}else{
					ret += writeStatement("public static final int SORT_BY_"+element.getName().toUpperCase()+" = "+(lastIndex++));
				}

			}
		}
		
		ret += writeStatement("public static final int SORT_BY_DEFAULT = "+defaultElemName);
		ret += emptyline();
		ret += writeString("public "+getListItemBeanSortTypeName(section.getDocument())+"(){");
		increaseIdent();
		ret += writeString("super(SORT_BY_DEFAULT);");
		ret += closeBlock();
		ret += emptyline();

		ret += writeString("public "+getListItemBeanSortTypeName(section.getDocument())+"(int method){");
		increaseIdent();
		ret += writeString("super(method);");
		ret += closeBlock();
		ret += emptyline();
				
		ret += writeString("public "+getListItemBeanSortTypeName(section.getDocument())+"(int method, boolean order){");
		increaseIdent();
		ret += writeString("super(method, order);");
		ret += closeBlock();
		ret += emptyline();
		
		ret += writeString("public static int name2method(String name){");
		increaseIdent();
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			if (element.isComparable()){
				MetaProperty p = section.getDocument().getField(element.getName());
				if (element instanceof MultilingualFieldElement){
					String lang = ((MultilingualFieldElement)element).getLanguage();
					ret += writeString("if ("+quote(p.getName(lang))+".equals(name))");
					ret += writeIncreasedStatement("return SORT_BY_"+p.getName(lang).toUpperCase());
					
				}else{
					ret += writeString("if ("+quote(p.getName())+".equals(name))");
					ret += writeIncreasedStatement("return SORT_BY_"+p.getName().toUpperCase());
/*					ret += writeString("if ("+quote(element.getName())+".equals(name))");
					ret += writeIncreasedStatement("return SORT_BY_"+element.getName().toUpperCase());*/
				}

			}
		}
		ret += writeStatement("throw new RuntimeException("+quote("Unknown sort type name: ")+"+name)");		
		ret += closeBlock();
		ret += emptyline();

// 		GENERATE method2name
		ret += writeString("public static String method2name(int method){");
		increaseIdent();
		ret += writeString("switch (method){");
		increaseIdent();
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			if (element.isComparable()){
				MetaProperty p = section.getDocument().getField(element.getName());
				if (element instanceof MultilingualFieldElement){
					String lang = ((MultilingualFieldElement)element).getLanguage();
					ret += writeString("case SORT_BY_"+p.getName(lang).toUpperCase()+":");
					ret += writeIncreasedStatement("return "+quote(p.getName(lang)));
					
				}else{
					ret += writeString("case SORT_BY_"+p.getName().toUpperCase()+":");
					ret += writeIncreasedStatement("return "+quote(p.getName()));
/*					ret += writeString("if ("+quote(element.getName())+".equals(name))");
					ret += writeIncreasedStatement("return SORT_BY_"+element.getName().toUpperCase());*/
				}

			}
		}
		ret += closeBlock();
		ret += writeStatement("throw new RuntimeException("+quote("Unknown sort type method: ")+"+method)");		
		ret += closeBlock();
		ret += emptyline();
		
		//
		ret += writeString("public String getMethodAndOrderCode(){");
		increaseIdent();
		ret += writeStatement("return method2name(getSortBy())+"+quote("_")+"+(getSortOrder() ? "+quote("ASC")+":"+quote("DESC")+")");
		ret += closeBlock();

		ret += closeBlock();
		
		return ret;
	}
	
	
	private String generateListItemBean(MetaModuleSection section){
		MetaDocument doc = section.getDocument();
		List<MetaViewElement> origElements = section.getElements();

		List<MetaViewElement> elements = createMultilingualList(origElements, doc, context);

		//elements.addAll(origElements);
		MetaFieldElement plainId = new MetaFieldElement("plainId");
		plainId.setComparable(false);
		plainId.setReadonly(true);
		plainId.setDecorator(null);
		elements.add(plainId);

		MetaFieldElement versionInfo = new MetaFieldElement("documentLastUpdateTimestamp");
		plainId.setComparable(false);
		plainId.setReadonly(true);
		plainId.setDecorator(null);
		elements.add(versionInfo);
		
		String ret = "";
		ret += writeStatement("package "+getPackage(section.getDocument()));
		ret += emptyline();

		boolean containsComparable = false;
		for (MetaViewElement element : elements){
			if (element.isComparable()){
				containsComparable = true;
				break; 
			}
		}
		
		if (containsComparable){
			ret += writeImport("net.anotheria.util.sorter.IComparable");
			ret += writeImport("net.anotheria.util.BasicComparable");
			ret += emptyline();
		}
		
		for(MetaViewElement element: elements){
			if (!(element instanceof MetaFieldElement))
				continue;
			MetaFieldElement field = (MetaFieldElement)element;
			MetaProperty p = doc.getField(field.getName());
			if(!(p instanceof MetaListProperty))
				continue;
			ret += writeImport("java.util.List");
			ret += emptyline();
			break;
		}

		String decl = "public class "+getListItemBeanName(section.getDocument());
		if (containsComparable)
			decl += " implements IComparable";
		ret += writeString(decl+"{");
		increaseIdent();
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			if (element instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)element;
				MetaProperty p = doc.getField(field.getName());
				if (p instanceof MetaEnumerationProperty)
					ret += writeStatement("private String "+p.getName());
				else{

//					MetaProperty tmp = p instanceof MetaListProperty? new MetaProperty(p.getName(), "int"):p;
					MetaProperty tmp = p;
					if (field instanceof MultilingualFieldElement){
						if (field.getDecorator()!=null){
							ret += writeStatement("private String "+tmp.getName(((MultilingualFieldElement)field).getLanguage()));
							ret += writeStatement("private "+tmp.toJavaType()+" "+tmp.getName("ForSorting", ((MultilingualFieldElement)field).getLanguage()));
						}else{
							ret += writeStatement("private "+tmp.toJavaType()+" "+tmp.getName(((MultilingualFieldElement)field).getLanguage()));
						}
					}else{
						//ret += writeString("//p: "+p.getName()+", "+p.toJavaType()+", "+p.getClass());
						if (field.getDecorator()!=null){
							ret += writeStatement("private String "+tmp.getName());
//							ret += writeStatement("private "+p.toJavaType()+" "+p.getName()+"ForSorting");
							ret += writeStatement("private "+tmp.toJavaType()+" "+tmp.getName()+"ForSorting");
						}else{
//							ret += writeStatement("private "+p.toJavaType()+" "+p.getName());
							ret += writeStatement("private "+tmp.toJavaType()+" "+tmp.getName());
						}
					}
				}
			}

			if (element instanceof MetaFunctionElement){
				MetaFunctionElement function = (MetaFunctionElement)element;
				ret += writeStatement("private String "+function.getPropertyName());
			}
		}
		ret += emptyline();
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			if (element instanceof MetaFieldElement)
				ret += generateFieldMethods((MetaFieldElement)element, doc);
			if (element instanceof MetaFunctionElement)
				ret += generateFunctionMethods((MetaFunctionElement)element);
			
		}
		
		if (containsComparable){
			ret += emptyline();
			ret += generateCompareMethod(doc, elements);
		}
		
		ret += closeBlock();
		return ret;
	}
	
	private String generateFunctionMethods(MetaFunctionElement function){
		return generateMethods(function, new MetaProperty(function.getPropertyName(), "string"));
	}
	
	private String generateFieldMethodsInDialog(MetaFieldElement element, MetaDocument doc){
		String ret = "";
		MetaProperty p = null;
//		String lang = getElementLanguage(element);
		p = doc.getField(element.getName());

		if (p.isLinked() || p instanceof MetaEnumerationProperty){
			MetaFieldElement pColl = new MetaFieldElement(element.getName()+"Collection");
			MetaFieldElement pCurr = new MetaFieldElement(element.getName()+"CurrentValue");
			//;
			if (p.isMultilingual()){
				String l = getElementLanguage(element);
				ret += generateMethods(new MultilingualFieldElement(l, pColl), new MetaListProperty(element.getName()+"Collection", new MetaProperty("temp", new ObjectType("LabelValueBean"))));
				ret += generateMethods(new MultilingualFieldElement(l, pCurr), new MetaProperty(element.getName()+"CurrentValue", "string"));
			}else{
				ret += generateMethods(pColl, new MetaListProperty(element.getName()+"Collection", new MetaProperty("temp", new ObjectType("LabelValueBean"))));
				ret += generateMethods(pCurr, new MetaProperty(element.getName()+"CurrentValue", "string"));
			}
			
		}
		MetaProperty tmp = p instanceof MetaListProperty? new MetaProperty(p.getName(),"int"): p;
		ret += generateMethods(element, tmp);
		return ret; 
	}

	private String generateFieldMethods(MetaFieldElement element, MetaDocument doc){
		
		MetaProperty p = doc.getField(element.getName());
		if (p instanceof MetaEnumerationProperty){
			MetaProperty tmp = new MetaProperty(p.getName(), "string");
			return generateMethods(element, tmp);		
		}

//		if (p instanceof MetaListProperty && element.getDecorator()!=null){
//			MetaProperty tmp = new MetaProperty(p.getName(), "string");
//			MetaProperty tmpForSorting = new MetaProperty(p.getName()+"ForSorting", "int");
//			return generateMethods(element, tmp)+generateMethods(element, tmpForSorting);		
//		}
		

		String additionalMethods = "";

//		if (p instanceof MetaListProperty)
//			p = new MetaProperty(p.getName(), "int");
		
		if (element.getDecorator()!=null){
			MetaProperty tmpForSorting = (MetaProperty) p.clone();//new MetaProperty(p.getName()+"ForSorting", p.getType());
			tmpForSorting.setName(tmpForSorting.getName()+"ForSorting");
			additionalMethods = generateMethods(element, tmpForSorting);
			//if this field has a decorator we have to generate string methods instaed of original methods.
			p = new MetaProperty(p.getName(), "string");
		}
		
		return additionalMethods + generateMethods(element, p);
	}
	
	private String generateMethods(MetaViewElement element, MetaProperty p){

		if (element instanceof MultilingualFieldElement)
			return generateMethodsMultilinguage((MultilingualFieldElement)element, p);
		
		String ret = "";
		
		ret += writeString("public void "+p.toBeanSetter()+"("+p.toJavaType()+" "+p.getName()+" ){");
		increaseIdent();
		ret += writeStatement("this."+p.getName()+" = "+p.getName());
		ret += closeBlock();			
		ret += emptyline();
			
		ret += writeString("public "+p.toJavaType()+" "+p.toBeanGetter()+"(){");
		increaseIdent();
		ret += writeStatement("return "+p.getName());
		ret += closeBlock();
		ret += emptyline();
		return ret;
	}
	
	private String generateMethodsMultilinguage(MultilingualFieldElement element, MetaProperty p){
		String ret = "";
		
		//System.out.println("--- m "+p+", "+p.getType());
		if (p.getType().equals("list"))
			ret += writeString("@SuppressWarnings(\"unchecked\")");
		ret += writeString("public void "+p.toBeanSetter(element.getLanguage())+"("+p.toJavaType()+" "+p.getName()+" ){");
		increaseIdent();
		ret += writeStatement("this."+p.getName(element.getLanguage())+" = "+p.getName());
		ret += closeBlock();			
		ret += emptyline();
			
		if (p.getType().equals("list"))
			ret += writeString("@SuppressWarnings(\"unchecked\")");
		ret += writeString("public "+p.toJavaType()+" "+p.toBeanGetter(element.getLanguage())+"(){");
		increaseIdent();
		ret += writeStatement("return "+p.getName(element.getLanguage()));
		ret += closeBlock();
		ret += emptyline();
		return ret;
		
	}
	
	private String generateCompareMethod(MetaDocument doc, List<MetaViewElement> elements){
		String ret ="";
		ret += writeString("public int compareTo(IComparable anotherComparable, int method){");
		increaseIdent();
		ret += writeStatement(getListItemBeanName(doc)+" anotherBean = ("+getListItemBeanName(doc)+") anotherComparable");
		ret += writeString("switch(method){");
		increaseIdent();
		for (MetaViewElement element: elements){
			if (!element.isComparable())
				continue;
			
			MetaFieldElement field = (MetaFieldElement)element;
			MetaProperty p = doc.getField(field.getName());
			
			String lang = getElementLanguage(element);
			String caseDecl = lang != null? getListItemBeanSortTypeName(doc)+".SORT_BY_"+p.getName(lang).toUpperCase():
				getListItemBeanSortTypeName(doc)+".SORT_BY_"+p.getName().toUpperCase();
			
			ret += writeString("case "+caseDecl+":");
			
			String type2compare = p instanceof MetaEnumerationProperty? "String": StringUtils.capitalize(p.toJavaErasedType());

			String retDecl = "return BasicComparable.compare"+type2compare;
			retDecl += field.getDecorator()!=null? "("+p.getName("ForSorting", lang)+", anotherBean."+p.getName("ForSorting", lang)+")":
				"("+p.getName(lang)+", anotherBean."+p.getName(lang)+")";

			ret += writeIncreasedStatement(retDecl);
		}
		ret += writeString("default:");
		ret += writeIncreasedStatement("throw new RuntimeException(\"Sort method \"+method+\" is not supported.\")");
		ret += closeBlock();
		ret += closeBlock();
		return ret;
	}
	
	@Deprecated
	public static String getPackage(){
	    return GeneratorDataRegistry.getInstance().getContext().getPackageName()+".bean";
	}
	
	public static String getPackage(MetaModule module){
	    return getPackage(GeneratorDataRegistry.getInstance().getContext(), module);
	}
	
	public static String getPackage(MetaDocument doc){
	    return getPackage(GeneratorDataRegistry.getInstance().getContext(), doc);
	}

	public static String getPackage(Context context, MetaModule module){
	    return context.getPackageName(module)+".bean";
	}
	
	public static String getPackage(Context context, MetaDocument doc){
	    return context.getPackageName(doc)+".bean";
	}

	public static String getListItemBeanSortTypeImport(Context context, MetaDocument doc){
		return getPackage(context, doc)+"."+getListItemBeanSortTypeName(doc);
	}
	
	public static String getListItemBeanImport(Context context, MetaDocument doc){
		return getPackage(context, doc)+"."+getListItemBeanName(doc);
	}
	
	public static String getDialogBeanName(MetaDialog dialog, MetaDocument document){
		return StringUtils.capitalize(dialog.getName())+StringUtils.capitalize(document.getName())+"Form";
	}
	
	public static String getDialogBeanImport(Context context, MetaDialog dialog, MetaDocument doc){
		return getPackage(context, doc)+"."+getDialogBeanName(dialog, doc);
	}
	
	public static String getFormBeanImport(MetaForm form){
		return getPackage()+"."+getFormBeanName(form);
	}

	public static String getFormBeanName(MetaForm form){
	    return StringUtils.capitalize(form.getId())+"AutoForm";
	}
	
	public String generateFormBean(MetaForm form){
	    String ret = "";
	    
		ret += writeStatement("package "+getPackage());
		ret += emptyline();
		ret += writeImport("net.anotheria.webutils.bean.BaseActionForm");
		ret += writeImport("javax.servlet.http.HttpServletRequest");
		ret += writeImport("org.apache.struts.action.ActionMapping");
		
		ret += emptyline();

		List<MetaFormField> elements = new ArrayList<MetaFormField>();
		elements.addAll(form.getElements());
		
		
		
		ret += writeString("public class "+getFormBeanName(form)+" extends BaseActionForm{");
		increaseIdent();

		for (int i=0; i<elements.size(); i++){
			MetaFormField element = elements.get(i);
			if (element.isSingle())
				ret += writeStatement("private "+((MetaFormSingleField)element).getJavaType()+" "+" "+element.getName());
			if (element.isComplex()){
				MetaFormTableField table = (MetaFormTableField) element;
				for (int r = 0; r<table.getRows(); r++){
					List<MetaFormTableColumn> columns = table.getColumns();
					for (int c = 0; c<columns.size(); c++){
						MetaFormTableColumn col = columns.get(c);
						ret += writeStatement("private "+col.getField().getJavaType()+" "+" "+table.getVariableName(r, c));
					}
				}
			}
		}

		ret += emptyline();

		for (int i=0; i<elements.size(); i++){
		    MetaFormField element = elements.get(i);
		    if (element.isSingle()){
		    	MetaFormSingleField field = (MetaFormSingleField )element;
				if (field.isSpacer())
					continue;
				ret += emptyline();
				ret += writeString("public "+field.getJavaType()+" get"+StringUtils.capitalize(element.getName())+"(){");
				increaseIdent();
				ret += writeStatement("return "+element.getName());
				ret += closeBlock();
				ret += emptyline();
				ret += writeString("public void set"+StringUtils.capitalize(element.getName())+"("+field.getJavaType()+" s){");
				increaseIdent();
				ret += writeStatement(element.getName()+" = s");
				ret += closeBlock();
		    }
		    
		    if (element.isComplex()){
				MetaFormTableField table = (MetaFormTableField) element;
				for (int r = 0; r<table.getRows(); r++){
					List<MetaFormTableColumn> columns = table.getColumns();
					for (int c = 0; c<columns.size(); c++){
						MetaFormTableColumn col = columns.get(c);
						
						ret += emptyline();
						ret += writeString("public "+col.getField().getJavaType()+" get"+StringUtils.capitalize(table.getVariableName(r, c))+"(){");
						increaseIdent();
						ret += writeStatement("return "+table.getVariableName(r, c));
						ret += closeBlock();
						ret += emptyline();
						ret += writeString("public void set"+StringUtils.capitalize(table.getVariableName(r, c))+"("+col.getField().getJavaType()+" s){");
						increaseIdent();
						ret += writeStatement(table.getVariableName(r, c)+" = s");
						ret += closeBlock();
					}
				}
		    }
		    
		}

		ret += emptyline();
		
		//generate encoding.
		ret += writeString("public void reset( ActionMapping mapping, HttpServletRequest request ){");
		increaseIdent();
		ret += writeString("try {");
		increaseIdent();
		ret += writeStatement("request.setCharacterEncoding( "+quote(GeneratorDataRegistry.getInstance().getContext().getEncoding())+")");
		ret += closeBlock();
		ret += writeString("catch ( java.io.UnsupportedEncodingException e ) {}");
		ret += closeBlock();
		  
		
		ret += closeBlock();		
	    
	    
	    return ret;
	}
	

}
