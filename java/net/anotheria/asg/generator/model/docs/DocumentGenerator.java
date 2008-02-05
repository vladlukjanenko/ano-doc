package net.anotheria.asg.generator.model.docs;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.meta.MetaContainerProperty;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaGenericListProperty;
import net.anotheria.asg.generator.meta.MetaGenericProperty;
import net.anotheria.asg.generator.meta.MetaListProperty;
import net.anotheria.asg.generator.meta.MetaProperty;
import net.anotheria.asg.generator.meta.MetaTableProperty;
import net.anotheria.asg.generator.model.AbstractDataObjectGenerator;
import net.anotheria.asg.generator.model.DataFacadeGenerator;
import net.anotheria.util.StringUtils;

/**
 * This generator generates an ano-doc framework based implementation of the data object interface previously generated
 * by the DataFacadeGenerator. It also generates an according factory. 
 * @author another
 */
public class DocumentGenerator extends AbstractDataObjectGenerator
	implements IGenerator{

	public static final String PROPERTY_DECLARATION = "public static final String ";	
	public static final String GET_CURRENT_LANG = "ContextManager.getCallContext().getCurrentLanguage()";

	private Context context;
	
	public List<FileEntry> generate(IGenerateable gdoc, Context context){
		MetaDocument doc = (MetaDocument)gdoc;
		this.context = context;
		
		
		
		//System.out.println(ret);
		List<FileEntry> _ret = new ArrayList<FileEntry>();
		_ret.add(new FileEntry(FileEntry.package2path(getPackageName(doc)), getDocumentName(doc), generateDocument(doc)));
		_ret.add(new FileEntry(FileEntry.package2path(getPackageName(doc)), getDocumentFactoryName(doc), generateDocumentFactory(doc)));
		return _ret;
	}
	
	public static String getDocumentName(MetaDocument doc){
		return doc.getName()+"Document";
	}

	public static String getDocumentImplName(MetaDocument doc){
		return doc.getName()+"Document";
	}
	

	public static String getClassImplName(MetaDocument doc){
		return doc.getName()+"Document";
	}

	public static String getSortTypeName(MetaDocument doc){
		return doc.getName()+"SortType";
	}
	
	private List<MetaProperty> extractSortableProperties(MetaDocument doc){
		List<MetaProperty> properties = new ArrayList<MetaProperty>();
		properties.add(new MetaProperty("id","string"));
		properties.addAll(doc.getProperties());
		properties.addAll(doc.getLinks());

		for (int i=0; i<properties.size(); i++){
			MetaProperty p = properties.get(i);
			if (p instanceof MetaContainerProperty){
				properties.remove(p);
				i--;
			}
		}

		return properties;
	}
	
	private String generateDocument(MetaDocument doc){
		String ret = "";
		
	
		ret += writeStatement("package "+getPackageName(doc));
		ret += emptyline();

		ret += writeImport("net.anotheria.anodoc.data.Document");
		boolean listImported = false;
		for (int i=0; i<doc.getProperties().size(); i++){
			if (doc.getProperties().get(i) instanceof MetaContainerProperty){
				ret += writeImport("java.util.List");
				if (doc.getProperties().get(i) instanceof MetaTableProperty)
					ret += writeImport("java.util.ArrayList");
				ret += writeImport("net.anotheria.anodoc.data.StringProperty");
				listImported = true;
				break;
			}
		}
		for (int i=0; i<doc.getProperties().size(); i++){
			if (doc.getProperties().get(i) instanceof MetaTableProperty){
				if (!listImported){
					ret += writeImport("java.util.List");
					//ret += writeImport("java.util.ArrayList");
					ret += writeImport("net.anotheria.anodoc.data.StringProperty");
				}
				break;
			}
		}
		
		for (MetaProperty p : doc.getProperties()){
			if (p.isMultilingual() && context.areLanguagesSupported()){
				ret += writeImport("net.anotheria.anodoc.util.context.ContextManager");
				break;
			}
		}
		ret += emptyline();
		
		String interfaceDecl = "implements "+doc.getName();
		if (doc.isComparable()){
			ret += writeImport("net.anotheria.util.sorter.IComparable");
			ret += writeImport("net.anotheria.util.BasicComparable");
			ret += emptyline();
			interfaceDecl += ", IComparable ";
		}
		
		
		ret += writeString("public class "+getDocumentName(doc)+" extends Document "+interfaceDecl+"{");
		increaseIdent();
		ret += emptyline();
		ret += generateDefaultConstructor(doc);
		ret += emptyline();
		ret += generateCloneConstructor(doc);
		ret += emptyline();
		ret += generatePropertyAccessMethods(doc);
		ret += emptyline();
		ret += generateToStringMethod(doc);
		ret += emptyline();
		ret += generateAdditionalMethods(doc);
		
		if (doc.isComparable()){
			ret += emptyline();
			ret += generateCompareMethod(doc);
		}
		
		ret +=emptyline();
		ret += generateDefNameMethod(doc);
		
		if (DataFacadeGenerator.hasLanguageCopyMethods(doc)){
			ret += generateLanguageCopyMethods(doc);
			ret += emptyline();
		}

		
		ret += closeBlock();
		return ret;
	}
	
	private String generateLanguageCopyMethods(MetaDocument doc){
		String ret = "";
		
		//first the common method lang2lang
		ret += writeString("public void "+DataFacadeGenerator.getCopyMethodName()+"(String sourceLanguage, String destLanguage){");
		increaseIdent();
		for (String srclang : context.getLanguages()){
			for (String targetlang : context.getLanguages()){
				if (!srclang.equals(targetlang)){
					ret += writeString("if (sourceLanguage.equals("+quote(srclang)+") && destLanguage.equals("+quote(targetlang)+"))");
					ret += writeIncreasedStatement(DataFacadeGenerator.getCopyMethodName(srclang, targetlang)+"()");
				}
			}
		}
		
		ret += closeBlock();
		ret += emptyline();
		
		
		//now the concrete methods
		for (String srclang : context.getLanguages()){
			for (String targetlang : context.getLanguages()){
				if (!srclang.equals(targetlang)){
					ret += writeComment("Copies all multilingual properties from language "+srclang+" to language "+targetlang);
					ret += writeString("public void "+DataFacadeGenerator.getCopyMethodName(srclang, targetlang)+"(){");
					increaseIdent();
					for (MetaProperty p : doc.getProperties()){
						if (p.isMultilingual()){
							String copyCall = p.toSetter(targetlang)+"(";
							copyCall += p.toGetter(srclang)+"()";
							copyCall += ")";
							ret += writeStatement(copyCall);
						}
					}
					ret += closeBlock();
					ret += emptyline();
				}
			}
		}
		
		return ret;
	}
	
	
	private String generateDefaultConstructor(MetaDocument doc){
		String ret = "";
		ret += writeString("public "+getDocumentName(doc)+"(String id){");
		increaseIdent();
		ret += writeStatement("super(id)");
		ret += closeBlock();
		return ret;
	}
	
	private String generateCloneConstructor(MetaDocument doc){
		String ret = "";
		ret += writeString("public "+getDocumentName(doc)+"("+getDocumentName(doc)+" toClone){");
		increaseIdent();
		ret += writeStatement("super(toClone)");
		ret += closeBlock();
		return ret;
	}

	private String generatePropertyAccessMethods(MetaDocument doc){
		String ret = "";
		
		ret += _generatePropertyAccessMethods(doc.getProperties());
		ret += _generatePropertyAccessMethods(doc.getLinks());
		return ret;
	}
	
	private String _generatePropertyAccessMethods(List<MetaProperty> properties){
		String ret = "";
		
		for (int i=0; i<properties.size(); i++){
			MetaProperty p = properties.get(i);
			ret += generatePropertyGetterMethod(p);
			ret += emptyline();
			ret += generatePropertySetterMethod(p);
			ret += emptyline();
		}
		return ret;
	}
	
	private String generatePropertyGetterMethod(MetaProperty p){
		String ret = "";
		
		if (p instanceof MetaTableProperty)
			return generateTablePropertyGetterMethods((MetaTableProperty)p);
		if (p instanceof MetaListProperty)
			return generateListPropertyGetterMethods((MetaListProperty)p);
		if (context.areLanguagesSupported() && p.isMultilingual())
			return generatePropertyGetterMethodMultilingual(p);
		
		ret += writeString("public "+p.toJavaType()+" get"+p.getAccesserName()+"(){");
		increaseIdent();
		if(p instanceof MetaGenericProperty)
			ret += writeStatement("return "+((MetaGenericProperty)p).toPropertyGetterCall());
		else
			ret += writeStatement("return "+p.toPropertyGetter()+"("+p.toNameConstant()+")");
		ret += closeBlock();
		return ret;
	}
	
	private String generatePropertyGetterMethodMultilingual(MetaProperty p){
		String ret = "";
		for (String l : context.getLanguages()){
			ret += writeString("public "+p.toJavaType()+" get"+p.getAccesserName(l)+"(){");
			increaseIdent();
			if (p instanceof MetaGenericProperty)
				ret += writeStatement("return "+((MetaGenericProperty)p).toPropertyGetterCall(l));
			else
				ret += writeStatement("return "+p.toPropertyGetter()+"("+p.toNameConstant(l)+")");
			ret += closeBlock();
			ret += emptyline();
		}
		ret += writeString("public "+p.toJavaType()+" get"+p.getAccesserName()+"(){");
		increaseIdent();
		String v = "ContextManager.getCallContext().getCurrentLanguage()";
		if(p instanceof MetaGenericProperty)
			ret += writeStatement("return "+((MetaGenericProperty)p).toPropertyGetterCallForCurrentLanguage(v));
		else
			ret += writeStatement("return "+p.toPropertyGetter()+"("+quote(p.getName()+"_")+"+"+v+")");
		ret += closeBlock();
		ret += emptyline();
		return ret;
	}
	
	
	private String generateListPropertyGetterMethods(MetaListProperty p){
		MetaProperty tmp = new MetaGenericListProperty(p.getName(), p.getContainedProperty());
		if (p.isMultilingual())
			tmp.setMultilingual(true);
		return generatePropertyGetterMethod(tmp);
	}
	
	private String generateTablePropertyGetterMethods(MetaTableProperty p){
		String ret = "";
		List<MetaProperty> columns = p.getColumns();
		for (int t=0; t<columns.size(); t++)
			ret += generatePropertyGetterMethod(columns.get(t));
		return ret;
	}
	
	private String generatePropertySetterMethod(MetaProperty p){
		String ret = "";

		if (p instanceof MetaTableProperty)
			return generateTablePropertySetterMethods((MetaTableProperty)p);
		if (p instanceof MetaListProperty)
			return generateListPropertySetterMethods((MetaListProperty)p);
		if (context.areLanguagesSupported() && p.isMultilingual())
			return generatePropertySetterMethodMultilingual(p);

		ret += writeString("public void set"+p.getAccesserName()+"("+p.toJavaType()+" value){");
		increaseIdent();
		if(p instanceof MetaGenericProperty)
			ret += writeStatement(""+((MetaGenericProperty)p).toPropertySetterCall());
		else
			ret += writeStatement(""+p.toPropertySetter()+"("+p.toNameConstant()+", value)");
		ret += closeBlock();	
		return ret;
	}
	
	private String generatePropertySetterMethodMultilingual(MetaProperty p){
		String ret = "";
		for (String l : context.getLanguages()){
			ret += writeString("public void set"+p.getAccesserName(l)+"("+p.toJavaType()+" value){");
			increaseIdent();
			if(p instanceof MetaGenericProperty)
				ret += writeStatement(""+((MetaGenericProperty)p).toPropertySetterCall(l));
			else
				ret += writeStatement(""+p.toPropertySetter()+"("+p.toNameConstant(l)+", value)");
			ret += closeBlock();
			ret += emptyline();
		}
		ret += writeString("public void set"+p.getAccesserName()+"("+p.toJavaType()+" value){");
		increaseIdent();
		String v = "ContextManager.getCallContext().getCurrentLanguage()";
		if(p instanceof MetaGenericProperty)
			ret += writeStatement(""+((MetaGenericProperty)p).toPropertySetterCallForCurrentLanguage(v));
		else
			ret += writeStatement(""+p.toPropertySetter()+"("+"("+quote(p.getName()+"_")+"+"+v+")"+", value)");
		ret += closeBlock();
		ret += emptyline();
		return ret;
	}

	private String generateListPropertySetterMethods(MetaListProperty p){
		MetaProperty tmp = new MetaGenericListProperty(p.getName(), p.getContainedProperty());
		if (p.isMultilingual())
			tmp.setMultilingual(true);
		return generatePropertySetterMethod(tmp);
	}

	private String generateTablePropertySetterMethods(MetaTableProperty p){
		System.out.println("Generating table property "+p+", contained: "+p.getColumns());
		String ret = "";
		List<MetaProperty> columns = p.getColumns();
		for (int t=0; t<columns.size(); t++)
			ret += generatePropertySetterMethod(columns.get(t));
		return ret;
	}

	private String generateToStringMethod(MetaDocument doc){
		String ret = "";
		ret += writeString("public String toString(){");
		increaseIdent();
		ret += writeStatement("String ret = "+quote(doc.getName()+" "));
		ret += writeStatement("ret += \"[\"+getId()+\"] \"");
		List<MetaProperty> props = doc.getProperties();
		for (int i=0; i<props.size(); i++){
			MetaProperty p = props.get(i);
			if (p instanceof MetaTableProperty){
				List<MetaProperty> columns = ((MetaTableProperty)p).getColumns();
				for (int t=0; t<columns.size(); t++){
					MetaProperty pp = columns.get(t);
					ret += writeStatement("ret += "+quote(pp.getName()+": ")+"+get"+pp.getAccesserName()+"()");
					if (t<columns.size()-1)
						ret += writeStatement("ret += \", \"");
				}
			}else{
				ret += writeStatement("ret += "+quote(p.getName()+": ")+"+get"+p.getAccesserName()+"()");
			}
			if (i<props.size()-1)
				ret += writeStatement("ret += \", \"");
		}
		ret += writeStatement("return ret"); 
		ret += closeBlock();
		return ret;
	}
	
	public static final String getDocumentImport(Context context, MetaDocument doc){
		return context.getDataPackageName(doc)+"."+getDocumentImplName(doc);
	}
	
	public static final String getSortTypeImport(MetaDocument doc){
		return GeneratorDataRegistry.getInstance().getContext().getPackageName(doc)+".data."+getSortTypeName(doc);
	}
	
	private String generateAdditionalMethods(MetaDocument doc){
		String ret = "";
		
		List <MetaProperty>properties = doc.getProperties();
		for (MetaProperty p : properties){
			if (p instanceof MetaContainerProperty)
				ret += generateContainerMethods((MetaContainerProperty)p);
			if (p instanceof MetaTableProperty)
				ret += generateTableMethods((MetaTableProperty)p);
			if (p instanceof MetaListProperty)
				ret += generateListMethods((MetaListProperty)p);
		}
		
		return ret;		
	}
	
	private String generateContainerMethods(MetaContainerProperty container){
		
		if (container.isMultilingual())
			return generateContainerMethodsMultilingual(container);
		
		String ret = "";

		ret += writeString("public int "+getContainerSizeGetterName(container)+"(){");
		increaseIdent();
		MetaProperty pr = container instanceof MetaTableProperty ? 
			(MetaProperty) ((MetaTableProperty)container).getColumns().get(0) :
			container;
		ret += writeStatement("return getList("+pr.toNameConstant()+").size()"); 
		ret += closeBlock();
		ret += emptyline();
		
		return ret;
	}
	
	private String generateContainerMethodsMultilingual(MetaContainerProperty container){
		String ret = "";

		for (String l : context.getLanguages()){
			ret += writeString("public int "+getContainerSizeGetterName(container, l)+"(){");
			increaseIdent();
			MetaProperty pr = container instanceof MetaTableProperty ? 
					(MetaProperty) ((MetaTableProperty)container).getColumns().get(0) :
						container;
					ret += writeStatement("return getList("+pr.toNameConstant(l)+").size()"); 
					ret += closeBlock();
					ret += emptyline();
		}
		
		ret += writeString("public int "+getContainerSizeGetterName(container)+"(){");
		increaseIdent();
		ret += writeStatement("return getList("+quote(container.getName()+"_")+"+"+GET_CURRENT_LANG+").size()"); 
		ret += closeBlock();
		ret += emptyline();
//		ret += writeStatement("return "+p.toPropertyGetter()+"("+quote(p.getName()+"_")+"+"+v+")");

		
		return ret;

	}

	private String generateListMethods(MetaListProperty list){
		String ret = "";
		
		if (list.isMultilingual())
			return generateListMethodsMultilingual(list);

		MetaProperty c = list.getContainedProperty();

		String decl = "public void "+getContainerEntryAdderName(list)+"(";
		decl += c.toJavaType()+" "+c.getName();
		decl += "){";
		ret += writeString(decl);
		increaseIdent();
		
		
		ret += writeStatement("getListPropertyAnyCase("+list.toNameConstant()+").add(new "+c.toJavaType()+"Property("+c.getName()+", "+c.getName()+"))");
		ret += closeBlock();
		ret += emptyline();
		
		
		ret += writeString("public void "+getContainerEntryDeleterName(list)+"(int index){");
		increaseIdent();
		ret += writeStatement("getListProperty("+list.toNameConstant()+").remove(index)"); 
		ret += closeBlock();
		ret += emptyline();
		
		ret += writeString("public void "+getContainerEntrySwapperName(list)+"(int index1, int index2){");
		increaseIdent();
		ret += writeStatement(c.toJavaType()+" tmp1, tmp2");
		ret += writeStatement("tmp1 = (("+c.toJavaType()+"Property"+")getList("+list.toNameConstant()+").get(index1)).get"+c.toJavaType()+"()");
		ret += writeStatement("tmp2 = (("+c.toJavaType()+"Property"+")getList("+list.toNameConstant()+").get(index2)).get"+c.toJavaType()+"()");
		ret += writeStatement("(("+c.toJavaType()+"Property"+")getList("+list.toNameConstant()+").get(index1)).set"+c.toJavaType()+"(tmp2)");
		ret += writeStatement("(("+c.toJavaType()+"Property"+")getList("+list.toNameConstant()+").get(index2)).set"+c.toJavaType()+"(tmp1)");
		ret += closeBlock();
		ret += emptyline();

		ret += writeString("public "+c.toJavaType()+ " "+getListElementGetterName(list)+"(int index){");
		increaseIdent();
		ret += writeStatement(c.toJavaType()+"Property p = ("+c.toJavaType()+"Property"+")getList("+list.toNameConstant()+").get(index)");
		ret += writeStatement("return p.get"+c.toJavaType()+"()");
		ret += closeBlock();
		ret += emptyline();

		return ret;
	}
	
	private String generateListMethodsMultilingual(MetaListProperty list){
		String ret = "";

		MetaProperty c = list.getContainedProperty();

		for (String l : GeneratorDataRegistry.getInstance().getContext().getLanguages()){
			String decl = "public void "+getContainerEntryAdderName(list, l)+"(";
			decl += c.toJavaType()+" "+c.getName();
			decl += "){";
			ret += writeString(decl);
			increaseIdent();
			
			ret += writeStatement("getListPropertyAnyCase("+list.toNameConstant(l)+").add(new "+c.toJavaType()+"Property("+c.getName()+", "+c.getName()+"))");
			ret += closeBlock();
			ret += emptyline();
			
			
			ret += writeString("public void "+getContainerEntryDeleterName(list, l)+"(int index){");
			increaseIdent();
			ret += writeStatement("getListProperty("+list.toNameConstant(l)+").remove(index)"); 
			ret += closeBlock();
			ret += emptyline();
			
			ret += writeString("public void "+getContainerEntrySwapperName(list, l)+"(int index1, int index2){");
			increaseIdent();
			ret += writeStatement(c.toJavaType()+" tmp1, tmp2");
			ret += writeStatement("tmp1 = (("+c.toJavaType()+"Property"+")getList("+list.toNameConstant(l)+").get(index1)).get"+c.toJavaType()+"()");
			ret += writeStatement("tmp2 = (("+c.toJavaType()+"Property"+")getList("+list.toNameConstant(l)+").get(index2)).get"+c.toJavaType()+"()");
			ret += writeStatement("(("+c.toJavaType()+"Property"+")getList("+list.toNameConstant(l)+").get(index1)).set"+c.toJavaType()+"(tmp2)");
			ret += writeStatement("(("+c.toJavaType()+"Property"+")getList("+list.toNameConstant(l)+").get(index2)).set"+c.toJavaType()+"(tmp1)");
			ret += closeBlock();
			ret += emptyline();
	
			ret += writeString("public "+c.toJavaType()+ " "+getListElementGetterName(list, l)+"(int index){");
			increaseIdent();
			ret += writeStatement(c.toJavaType()+"Property p = ("+c.toJavaType()+"Property"+")getList("+list.toNameConstant(l)+").get(index)");
			ret += writeStatement("return p.get"+c.toJavaType()+"()");
			ret += closeBlock();
			ret += emptyline();
		}
		
//		quote(container.getName()+"_")+"+"+GET_CURRENT_LANG+
		String decl = "public void "+getContainerEntryAdderName(list )+"(";
		decl += c.toJavaType()+" "+c.getName();
		decl += "){";
		ret += writeString(decl);
		increaseIdent();
		
		ret += writeStatement("getListPropertyAnyCase("+quote(list.getName()+"_")+"+"+GET_CURRENT_LANG+").add(new "+c.toJavaType()+"Property("+c.getName()+", "+c.getName()+"))");
		ret += closeBlock();
		ret += emptyline();
		
		
		ret += writeString("public void "+getContainerEntryDeleterName(list)+"(int index){");
		increaseIdent();
		ret += writeStatement("getListProperty("+quote(list.getName()+"_")+"+"+GET_CURRENT_LANG+").remove(index)"); 
		ret += closeBlock();
		ret += emptyline();
		
		ret += writeString("public void "+getContainerEntrySwapperName(list)+"(int index1, int index2){");
		increaseIdent();
		ret += writeStatement(c.toJavaType()+" tmp1, tmp2");
		ret += writeStatement("tmp1 = (("+c.toJavaType()+"Property"+")getList("+quote(list.getName()+"_")+"+"+GET_CURRENT_LANG+").get(index1)).get"+c.toJavaType()+"()");
		ret += writeStatement("tmp2 = (("+c.toJavaType()+"Property"+")getList("+quote(list.getName()+"_")+"+"+GET_CURRENT_LANG+").get(index2)).get"+c.toJavaType()+"()");
		ret += writeStatement("(("+c.toJavaType()+"Property"+")getList("+quote(list.getName()+"_")+"+"+GET_CURRENT_LANG+").get(index1)).set"+c.toJavaType()+"(tmp2)");
		ret += writeStatement("(("+c.toJavaType()+"Property"+")getList("+quote(list.getName()+"_")+"+"+GET_CURRENT_LANG+").get(index2)).set"+c.toJavaType()+"(tmp1)");
		ret += closeBlock();
		ret += emptyline();

		ret += writeString("public "+c.toJavaType()+ " "+getListElementGetterName(list)+"(int index){");
		increaseIdent();
		ret += writeStatement(c.toJavaType()+"Property p = ("+c.toJavaType()+"Property"+")getList("+quote(list.getName()+"_")+"+"+GET_CURRENT_LANG+").get(index)");
		ret += writeStatement("return p.get"+c.toJavaType()+"()");
		ret += closeBlock();
		ret += emptyline();

		
		return ret;
	}

	private String generateTableMethods(MetaTableProperty table){
		String ret = "";
		List<MetaProperty> columns = table.getColumns();
		
		String decl = "public void "+getContainerEntryAdderName(table)+"(";
		for (int i =0; i<columns.size(); i++){
			MetaProperty p = columns.get(i);
			decl += "String "+table.extractSubName(p);
			if (i<columns.size()-1)
				decl += ", ";
		}
		decl += "){";
		ret += writeString(decl);
		increaseIdent();
		
		ret += writeStatement("List tmp");
		
		for (int i=0; i<columns.size(); i++){
			MetaProperty p = columns.get(i);
			ret += writeStatement("tmp = getList("+p.toNameConstant()+")"); 
			ret += writeStatement("tmp.add(new StringProperty(\"\", "+table.extractSubName(p)+"))");
			ret += writeStatement("setList("+p.toNameConstant()+", tmp)");
			ret += emptyline();
		}

		ret += closeBlock();
		ret += emptyline();
		
		ret += writeString("public void "+getContainerEntryDeleterName(table)+"(int index){");
		increaseIdent();
		for (int i=0; i<columns.size(); i++){
			MetaProperty p = columns.get(i);
			ret += writeStatement("getListProperty("+p.toNameConstant()+").remove(index)"); 
		}
		
		ret += closeBlock();
		ret += emptyline();
		
		ret += writeString("public List<String> get"+StringUtils.capitalize(table.getName())+"Row(int index){");
		increaseIdent();
		ret += writeStatement("List<String> ret = new ArrayList<String>(1)");
		for (int i=0; i<columns.size(); i++){
			MetaProperty p = columns.get(i);
			ret += writeString("try{");
			ret += writeIncreasedStatement("ret.add(((StringProperty)getList("+p.toNameConstant()+").get(index)).getString())");
			ret += writeString("}catch(IndexOutOfBoundsException e){ ");
			ret += writeIncreasedStatement("ret.add(\"\")");
			ret += writeString("}");  
		}
		ret += writeStatement("return ret");
		ret += closeBlock();
		ret += emptyline();

		ret += writeString("public List<List<String>> "+getTableGetterName(table)+"(){");
		increaseIdent();
		ret += writeStatement("int size = "+getContainerSizeGetterName(table)+"();");
		ret += writeStatement("List<List<String>> ret = new java.util.ArrayList<List<String>>(size)");
		ret += writeString("for (int i=0; i<size; i++)");
		ret += writeIncreasedStatement("ret.add(get"+StringUtils.capitalize(table.getName())+"Row(i))");
		ret += writeStatement("return ret");
		ret += closeBlock();
		ret += emptyline();
		return ret;
	}
	
	private String generateCompareMethod(MetaDocument doc){
		String ret = "";
		
		ret += writeString("public int compareTo(IComparable anotherComparable, int method){");
		increaseIdent();

		ret += writeStatement(getDocumentName(doc)+" anotherDoc = ("+getDocumentName(doc)+") anotherComparable");
		ret += writeString("switch(method){");
		increaseIdent();
		List<MetaProperty> properties = extractSortableProperties(doc);

		for (int i=0; i<properties.size(); i++){
			MetaProperty p = properties.get(i);

			String caseDecl = getSortTypeName(doc)+".SORT_BY_"+p.getName().toUpperCase();
			ret += writeString("case "+caseDecl+":");
			String type2compare = null; 
			type2compare = StringUtils.capitalize(p.toJavaType());
			String retDecl = "return BasicComparable.compare"+type2compare;
			retDecl += "(get"+p.getAccesserName()+"(), anotherDoc.get"+p.getAccesserName()+"())";
			ret += writeIncreasedStatement(retDecl);
		}
		ret += writeString("default:");
		ret += writeIncreasedStatement("throw new RuntimeException(\"Sort method \"+method+\" is not supported.\")");
		ret += closeBlock();

		ret += closeBlock();
		
		return ret;
	}
	
	private String generateDefNameMethod(MetaDocument doc){
		String ret = "";
		ret += writeString("public String getDefinedName(){");
		increaseIdent();
		ret += writeStatement("return "+quote(doc.getName()));
		ret += closeBlock();
		return ret;
	}

	public static String getContainerSizeGetterName(MetaContainerProperty p){
		return "get"+StringUtils.capitalize(p.getName())+"Size"; 
	}

	public static String getContainerSizeGetterName(MetaContainerProperty p, String language){
		return "get"+StringUtils.capitalize(p.getName(language))+"Size"; 
	}

	public static String getTableGetterName(MetaTableProperty p){
		return "get"+StringUtils.capitalize(p.getName())+"Table"; 
	}
	
	public static String getContainerEntryAdderName(MetaContainerProperty p){
	    return DataFacadeGenerator.getContainerEntryAdderName(p);	    
	}

	public static String getContainerEntryAdderName(MetaContainerProperty p, String language){
		return DataFacadeGenerator.getContainerEntryAdderName(p, language);	    
	}

	public static String getContainerEntryDeleterName(MetaContainerProperty p){
		return DataFacadeGenerator.getContainerEntryDeleterName(p);	    
	}

	public static String getContainerEntryDeleterName(MetaContainerProperty p, String language){
		return DataFacadeGenerator.getContainerEntryDeleterName(p, language);	    
	}

	public static String getContainerEntrySwapperName(MetaContainerProperty p){
		return DataFacadeGenerator.getContainerEntrySwapperName(p);	    
	}
	
	public static String getContainerEntrySwapperName(MetaContainerProperty p, String language){
		return DataFacadeGenerator.getContainerEntrySwapperName(p, language);	    
	}

	public static String getListElementGetterName(MetaListProperty list){
		return DataFacadeGenerator.getListElementGetterName(list);	    
	}

	public static String getListElementGetterName(MetaListProperty list, String language){
		return DataFacadeGenerator.getListElementGetterName(list, language);	    
	}

	private String generateDocumentFactory(MetaDocument doc){
		String ret = "";
		
	
		ret += writeStatement("package "+getPackageName(doc));
		ret += emptyline();

		
		
		ret += writeString("public class "+getDocumentFactoryName(doc)+"{");
		increaseIdent();
		ret += writeString("public static "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" template){");
		increaseIdent();
		ret += writeStatement("return new "+getDocumentName(doc)+"(("+getDocumentName(doc)+")"+"template)");
		ret += closeBlock();

		ret += emptyline();

		ret += writeString("public static "+doc.getName()+" create"+doc.getName()+"(){");
		increaseIdent();
		ret += writeStatement("return new "+getDocumentName(doc)+"(\"\")");
		ret += closeBlock();

		ret += emptyline();

		ret += closeBlock();
		return ret;
	}
	
	private String getDocumentFactoryName(MetaDocument doc){
		return DataFacadeGenerator.getDocumentFactoryName(doc);
	}

}
