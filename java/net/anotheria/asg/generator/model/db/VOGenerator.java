package net.anotheria.asg.generator.model.db;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.meta.MetaContainerProperty;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaGenericProperty;
import net.anotheria.asg.generator.meta.MetaListProperty;
import net.anotheria.asg.generator.meta.MetaProperty;
import net.anotheria.asg.generator.meta.MetaTableProperty;
import net.anotheria.asg.generator.model.AbstractDataObjectGenerator;
import net.anotheria.asg.generator.model.DataFacadeGenerator;
import net.anotheria.util.StringUtils;

/**
 * Generates value objects for db bound document implementation.
 * @author another
 */
public class VOGenerator extends AbstractDataObjectGenerator
	implements IGenerator{

	public static final String DAO_CREATED = "daoCreated";
	public static final String DAO_UPDATED = "daoUpdated";
	
	private Context context;
	MetaProperty id = new MetaProperty("id","string");
	
	MetaProperty daoCreated = new MetaProperty(DAO_CREATED, "long");
	MetaProperty daoUpdated = new MetaProperty(DAO_UPDATED, "long");
	
	public List<FileEntry> generate(IGenerateable gdoc, Context context){
		MetaDocument doc = (MetaDocument)gdoc;
		this.context = context;
		id.setReadonly(true);
		
		
		List<FileEntry> _ret = new ArrayList<FileEntry>();
		_ret.add(new FileEntry(FileEntry.package2path(getPackageName(doc)), getDocumentImplName(doc), generateDocument(doc)));
		_ret.add(new FileEntry(FileEntry.package2path(getPackageName(doc)), getDocumentFactoryName(doc), generateDocumentFactory(doc)));
		return _ret;
	}
	
	
	public static String getDocumentImplName(MetaDocument doc){
		return doc.getName()+"VO";
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

		boolean listImported = false;
		for (int i=0; i<doc.getProperties().size(); i++){
			if (doc.getProperties().get(i) instanceof MetaContainerProperty){
				ret += writeImport("java.util.List");
				ret += writeImport("java.util.ArrayList");
				listImported = true;
				break;
			}
		}
		for (int i=0; i<doc.getProperties().size(); i++){
			if (doc.getProperties().get(i) instanceof MetaTableProperty){
				if (!listImported){
					ret += writeImport("java.util.List");
					ret += writeImport("java.util.ArrayList");
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
		
		ret += writeImport("net.anotheria.asg.data.AbstractVO");
		ret += writeImport("java.io.Serializable");
		
		ret += emptyline();
		
		String interfaceDecl = "implements "+doc.getName();
		if (doc.isComparable()){
			ret += writeImport("net.anotheria.util.sorter.IComparable");
			ret += writeImport("net.anotheria.util.BasicComparable");
			ret += emptyline();
			interfaceDecl += ", IComparable, Serializable ";
		}
		

		ret += writeMark(1);
		
		ret += writeString("public class "+getDocumentImplName(doc)+" extends AbstractVO "+interfaceDecl+"{");
		increaseIdent();
		ret += emptyline();
		ret += writeMark(2);
		ret += generatePropertyFields(doc);
		ret += emptyline();
		ret += writeMark(3);
		ret += generateDefaultConstructor(doc);
		ret += emptyline();
		ret += writeMark(4);
		ret += generateCloneConstructor(doc);
		ret += emptyline();
		ret += generatePropertyAccessMethods(doc);
		ret += emptyline();
		ret += generateToStringMethod(doc);
		ret += emptyline();
		ret += generateCloneMethod(doc);
		ret += emptyline();
		ret += generateCopyMethod(doc);
		ret += emptyline();
		ret += generateGetPropertyValueMethod(doc);
		ret += emptyline();
		ret += generateAdditionalMethods(doc);
		
		if (doc.isComparable()){
			ret += emptyline();
			ret += generateCompareMethod(doc);
		}
		
		ret +=emptyline();
		ret += generateDefNameMethod(doc);
		
		ret += closeBlock();
		return ret;
	}
	
	private String generateDefaultConstructor(MetaDocument doc){
		String ret = "";
		ret += writeString("public "+getDocumentImplName(doc)+"(String anId){");
		increaseIdent();
		ret += writeStatement("id = anId");
		ret += closeBlock();
		return ret;
	}
	
	private String generateCloneConstructor(MetaDocument doc){
		String ret = "";
		ret += writeString("public "+getDocumentImplName(doc)+"("+getDocumentImplName(doc)+" toClone){");
		increaseIdent();
		//TODO add multilingual support 
		ret += writeStatement("this.id = toClone.id");
		ret += writeStatement("copyAttributesFrom(toClone)");
		ret += closeBlock();
		return ret;
	}

	private String generatePropertyFields(MetaDocument doc){
		String ret = "";
		ret +=_generatePropertyField(id);
		ret += _generatePropertyFields(doc.getProperties());
		ret += _generatePropertyFields(doc.getLinks());
		
		//support for dao information
		ret += _generatePropertyField(daoCreated);
		ret += _generatePropertyField(daoUpdated);
		
		return ret;
	}
	
	private MetaProperty getMetaGenericProperty(MetaListProperty p){
		MetaProperty tmp = new MetaGenericProperty(p.getName(), "list", p.getContainedProperty());
		if (p.isMultilingual())
			tmp.setMultilingual(true);
		return tmp;
	}
	
	private String _generatePropertyFields(List<MetaProperty> propertyList){
		String ret = "";
		for (int i=0; i<propertyList.size(); i++){
			MetaProperty p = propertyList.get(i);
			if (p instanceof MetaTableProperty){
				List<MetaProperty> columns = ((MetaTableProperty)p).getColumns();
				for (int t=0; t<columns.size(); t++)
					ret += _generatePropertyField(columns.get(t));
			}
			else if (p instanceof MetaListProperty)
				ret += _generatePropertyField(getMetaGenericProperty((MetaListProperty)p));
			else{
				ret += _generatePropertyField(p);
			}
		}
		return ret;
	}
	
	private String _generatePropertyField(MetaProperty p){
		String ret = "";
		if (context.areLanguagesSupported() && p.isMultilingual()){
			System.out.println("Multilingual support for VOs not yet implemented!");
			/*
			for (String l: context.getLanguages()){
				String decl = PROPERTY_DECLARATION;
				decl += p.toNameConstant(l);
				decl += "\t= \""+p.getName()+"_"+l+"\"";
				ret += writeStatement(decl);
			}
			return ret;
			*/
			return "";
		}else{
			ret += "private ";
			ret += p.toJavaType();
			ret += " "+p.getName().toLowerCase();
			return writeStatement(ret);
		}
	}
	
	
	private String generatePropertyAccessMethods(MetaDocument doc){
		String ret = "";
		
		ret += generatePropertyGetterMethod(id);
		ret += _generatePropertyAccessMethods(doc.getProperties());
		ret += _generatePropertyAccessMethods(doc.getLinks());

		// the VO knows this information even its not in the interface 
		ret += generatePropertyGetterMethod(daoCreated);
		ret += generatePropertySetterMethod(daoCreated);
		ret += generatePropertyGetterMethod(daoUpdated);
		ret += generatePropertySetterMethod(daoUpdated);
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
		ret += writeStatement("return "+p.getName().toLowerCase());
		ret += closeBlock();
		return ret;
	}
	
	private String generatePropertyGetterMethodMultilingual(MetaProperty p){
		String ret = "";
		for (String l : context.getLanguages()){
			ret += writeString("public "+p.toJavaType()+" get"+p.getAccesserName(l)+"(){");
			increaseIdent();
			ret += writeStatement("return "+p.toPropertyGetter()+"("+p.toNameConstant(l)+")");
			ret += closeBlock();
			ret += emptyline();
		}
		ret += writeString("public "+p.toJavaType()+" get"+p.getAccesserName()+"(){");
		increaseIdent();
		String v = "ContextManager.getCallContext().getCurrentLanguage()";
		ret += writeStatement("return "+p.toPropertyGetter()+"("+quote(p.getName()+"_")+"+"+v+")");
		ret += closeBlock();
		ret += emptyline();
		return ret;
	}
	
	
	private String generateListPropertyGetterMethods(MetaListProperty p){
		MetaProperty tmp = getMetaGenericProperty(p);
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
		ret += writeStatement(""+p.getName().toLowerCase()+" = value");
		ret += closeBlock();	
		return ret;
	}
	
	private String generatePropertySetterMethodMultilingual(MetaProperty p){
		String ret = "";
		for (String l : context.getLanguages()){
			ret += writeString("public void set"+p.getAccesserName(l)+"("+p.toJavaType()+" value){");
			increaseIdent();
			ret += writeStatement(""+p.toPropertySetter()+"("+p.toNameConstant(l)+", value)");
			ret += closeBlock();
			ret += emptyline();
		}
		ret += writeString("public void set"+p.getAccesserName()+"("+p.toJavaType()+" value){");
		increaseIdent();
		String v = "ContextManager.getCallContext().getCurrentLanguage()";
		ret += writeStatement(""+p.toPropertySetter()+"("+"("+quote(p.getName()+"_")+"+"+v+")"+", value)");
		ret += closeBlock();
		ret += emptyline();
		return ret;
	}

	private String generateListPropertySetterMethods(MetaListProperty p){
		MetaProperty tmp = getMetaGenericProperty(p);
		return generatePropertySetterMethod(tmp);
	}

	private String generateTablePropertySetterMethods(MetaTableProperty p){
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
	
	private String generateCloneMethod(MetaDocument doc){
		String ret = "";
		ret += writeString("public "+doc.getName()+" clone(){");
		increaseIdent();
		ret += writeStatement("return new "+getDocumentImplName(doc)+"(this)");
		ret += closeBlock();
		return ret;
	}
	
	private String generateCopyMethod(MetaDocument doc){
		String ret = "";
		ret += writeString("public void copyAttributesFrom("+doc.getName()+" toCopy){");
		increaseIdent();
		for (MetaProperty p : doc.getProperties()){
			ret += writeStatement("this."+p.getName().toLowerCase() + " = toCopy."+p.toGetter()+"()");
		}
		for (MetaProperty p : doc.getLinks()){
			ret += writeStatement("this."+p.getName().toLowerCase() + " = toCopy."+p.toGetter()+"()");
		}
		ret += closeBlock();
		return ret;
	}

	private String generateGetPropertyValueMethod(MetaDocument doc){
		String ret = "";
		ret += writeString("public Object getPropertyValue(String propertyName){");
		increaseIdent();
		List <MetaProperty>properties = new ArrayList<MetaProperty>(doc.getProperties());
		properties.addAll(doc.getLinks());
		for (MetaProperty p : properties){
			ret += writeString("if ("+p.toNameConstant()+".equals(propertyName))");
			ret += writeIncreasedStatement("return get"+p.getAccesserName()+"()");
					
		}

		ret += writeStatement("throw new RuntimeException("+quote("No property getter for ")+"+propertyName)");
		ret += closeBlock();
		
		return ret;
	}
	


	public static final String getDocumentImport(Context context, MetaDocument doc){
		return context.getPackageName(doc)+".data."+getDocumentImplName(doc);
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
		String ret = "";

		ret += writeString("public int "+getContainerSizeGetterName(container)+"(){");
		increaseIdent();
		MetaProperty pr = container instanceof MetaTableProperty ? 
			(MetaProperty) ((MetaTableProperty)container).getColumns().get(0) :
			container;
//		ret += writeStatement("return getList("+pr.toNameConstant()+").size()"); 
		ret += writeStatement("return get"+container.getAccesserName()+"().size()");
		ret += closeBlock();
		ret += emptyline();
		
		return ret;

	}
	
	private String generateListMethods(MetaListProperty list){
		String ret = "";
		MetaProperty genericList = getMetaGenericProperty(list);
		MetaProperty c = list.getContainedProperty();

		String decl = "public void "+getContainerEntryAdderName(list)+"(";
		decl += c.toJavaType()+" "+c.getName();
		decl += "){";
		ret += writeString(decl);
		increaseIdent();
		
		ret += writeStatement(c.toJavaObjectType()+" p = new "+c.toJavaObjectType()+"("+c.getName()+")");
		ret += writeStatement(genericList.toJavaType() + " tmp = get"+list.getAccesserName()+"()");
		ret += writeStatement("tmp.add(p)");
//		ret += writeStatement("set"+list.getAccesserName()+"(tmp)");
		ret += closeBlock();
		ret += emptyline();
		
		
		ret += writeString("public void "+getContainerEntryDeleterName(list)+"(int index){");
		increaseIdent();
//		ret += writeStatement("getListProperty("+list.toNameConstant()+").remove(index)"); 
		ret += writeStatement("get"+list.getAccesserName()+"().remove(index)");
		ret += closeBlock();
		ret += emptyline();
		
		ret += writeString("public void "+getContainerEntrySwapperName(list)+"(int index1, int index2){");
		increaseIdent();
		ret += writeStatement(c.toJavaType()+" tmp1, tmp2");
//		ret += writeStatement("tmp1 = (("+c.toJavaType()+")getList("+list.toNameConstant()+").get(index1)).get"+c.toJavaType()+"()");
//		ret += writeStatement("tmp2 = (("+c.toJavaType()+")getList("+list.toNameConstant()+").get(index2)).get"+c.toJavaType()+"()");
//		ret += writeStatement("(("+c.toJavaType()+"Property"+")getList("+list.toNameConstant()+").get(index1)).set"+c.toJavaType()+"(tmp2)");
//		ret += writeStatement("(("+c.toJavaType()+"Property"+")getList("+list.toNameConstant()+").get(index2)).set"+c.toJavaType()+"(tmp1)");
		ret += writeStatement(genericList.toJavaType() + " tmpList = get"+list.getAccesserName()+"()");
		ret += writeStatement("tmp1 = (("+c.toJavaObjectType()+")tmpList.get(index1))");
		ret += writeStatement("tmp2 = (("+c.toJavaObjectType()+")tmpList.get(index2))");
		ret += writeStatement("tmpList.set(index1, tmp2)");
		ret += writeStatement("tmpList.set(index2, tmp1)");
		ret += closeBlock();
		ret += emptyline();

		ret += writeString("public "+c.toJavaType()+ " "+getListElementGetterName(list)+"(int index){");
		increaseIdent();
//		ret += writeStatement(c.toJavaType()+"Property p = ("+c.toJavaType()+"Property"+")getList("+list.toNameConstant()+").get(index)");
//		ret += writeStatement("return p.get"+c.toJavaType()+"()");
		ret += writeStatement(c.toJavaType()+" p = ("+c.toJavaObjectType()+""+")get"+list.getAccesserName()+"().get(index)");
		ret += writeStatement("return p");
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
		
		ret += writeString("public List get"+StringUtils.capitalize(table.getName())+"Row(int index){");
		increaseIdent();
		ret += writeStatement("List ret = new ArrayList(1)");
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

		ret += writeString("public List "+getTableGetterName(table)+"(){");
		increaseIdent();
		ret += writeStatement("int size = "+getContainerSizeGetterName(table)+"();");
		ret += writeStatement("List ret = new java.util.ArrayList(size)");
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

		ret += writeStatement(getDocumentImplName(doc)+" anotherDoc = ("+getDocumentImplName(doc)+") anotherComparable");
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

	public static String getTableGetterName(MetaTableProperty p){
		return "get"+StringUtils.capitalize(p.getName())+"Table"; 
	}
	
	public static String getContainerEntryAdderName(MetaContainerProperty p){
	    return "add"+StringUtils.capitalize(p.getName())+p.getContainerEntryName();	    
	}
	public static String getContainerEntryDeleterName(MetaContainerProperty p){
		return "remove"+StringUtils.capitalize(p.getName())+p.getContainerEntryName();	    
	}

	public static String getContainerEntrySwapperName(MetaContainerProperty p){
		return "swap"+StringUtils.capitalize(p.getName())+p.getContainerEntryName();	    
	}
	
	public static String getListElementGetterName(MetaListProperty list){
		return "get"+StringUtils.capitalize(list.getName())+list.getContainerEntryName();
	}

	private String generateDocumentFactory(MetaDocument doc){
		String ret = "";
		
	
		ret += writeStatement("package "+getPackageName(doc));
		ret += emptyline();

		
		
		ret += writeString("public class "+getDocumentFactoryName(doc)+"{");
		increaseIdent();
		ret += writeString("public static "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" template){");
		increaseIdent();
		ret += writeStatement("return new "+getDocumentImplName(doc)+"(("+getDocumentImplName(doc)+")"+"template)");
		ret += closeBlock();

		ret += emptyline();

		ret += writeString("public static "+doc.getName()+" create"+doc.getName()+"(){");
		increaseIdent();
		ret += writeStatement("return new "+getDocumentImplName(doc)+"(\"\")");
		ret += closeBlock();

		ret += emptyline();

		ret += writeComment("For internal use only!");
		ret += writeString("public static "+doc.getName()+" create"+doc.getName()+"(String anId){");
		increaseIdent();
		ret += writeStatement("return new "+getDocumentImplName(doc)+"(anId)");
		ret += closeBlock();

		ret += closeBlock();
		return ret;
	}
	
	private String getDocumentFactoryName(MetaDocument doc){
		return DataFacadeGenerator.getDocumentFactoryName(doc);
	}

}
