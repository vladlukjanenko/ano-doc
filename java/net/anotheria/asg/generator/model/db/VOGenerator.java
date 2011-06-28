package net.anotheria.asg.generator.model.db;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.CommentGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
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
import net.anotheria.util.StringUtils;

/**
 * Generates value objects for db bound document implementation.
 * @author another
 */
public class VOGenerator extends AbstractDataObjectGenerator implements IGenerator{

	/**
	 * Constant for dao-created column name.
	 */
	public static final String DAO_CREATED = "daoCreated";
	/**
	 * Constant for dao-updaten column name.
	 */
	public static final String DAO_UPDATED = "daoUpdated";
	/**
	 * The id property.
	 */
	MetaProperty id = new MetaProperty("id",MetaProperty.Type.STRING);
	/**
	 * The dao created property (not included in document property set).
	 */
	MetaProperty daoCreated = new MetaProperty(DAO_CREATED, MetaProperty.Type.LONG);
	/**
	 * The dao created property (not included in document property set).
	 */
	MetaProperty daoUpdated = new MetaProperty(DAO_UPDATED, MetaProperty.Type.LONG);
	
	public List<FileEntry> generate(IGenerateable gdoc){
		MetaDocument doc = (MetaDocument)gdoc;
		id.setReadonly(true);
		
		
		List<FileEntry> _ret = new ArrayList<FileEntry>();
		_ret.add(new FileEntry(generateDocument(doc)));
		_ret.add(new FileEntry(generateDocumentFactory(doc)));
		return _ret;
	}
	
	public String getDataObjectImplName(MetaDocument doc){
		return getDocumentImplName(doc);
	}
	
	public static String getDocumentImplName(MetaDocument doc){
		return doc.getName()+"VO";
	}
	

	public static String getClassImplName(MetaDocument doc){
		return doc.getName()+"Document";
	}
	
	private GeneratedClass generateDocument(MetaDocument doc){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(getPackageName(doc));
		clazz.setTypeComment(CommentGenerator.generateJavaTypeComment("The implementation of the "+(doc.getName())+".", this));

	 
		for (int i=0; i<doc.getProperties().size(); i++){
			if (doc.getProperties().get(i) instanceof MetaContainerProperty){
				clazz.addImport("java.util.List");
				clazz.addImport("java.util.ArrayList");
			}
		}

		for (int i=0; i<doc.getProperties().size(); i++){
			if (doc.getProperties().get(i) instanceof MetaTableProperty){
				clazz.addImport("java.util.List");
				clazz.addImport("java.util.ArrayList");
			}
		}
		
		for (MetaProperty p : doc.getProperties()){
			if (p.isMultilingual() && GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported()){
				clazz.addImport("net.anotheria.anodoc.util.context.ContextManager");
				break;
			}
		}
		
		clazz.addImport("net.anotheria.asg.data.AbstractVO");
		clazz.addImport("net.anotheria.util.crypt.MD5Util");
		clazz.addImport("java.io.Serializable");
		
		clazz.addInterface(doc.getName());
		clazz.addInterface("Serializable");
		
		if (doc.isComparable()){
			clazz.addImport("net.anotheria.util.sorter.IComparable");
			clazz.addImport("net.anotheria.util.BasicComparable");
			clazz.addInterface("IComparable");
			clazz.addInterface("Comparable<"+doc.getName()+">");
		}
		

		appendMark(1);
		
		clazz.setName(getDocumentImplName(doc));
		clazz.setParent("AbstractVO");

		startClassBody();
		appendMark(2);
		generatePropertyFields(doc);
		emptyline();
		appendMark(3);
		generateDefaultConstructor(doc);
		emptyline();
		appendMark(4);
		generateCloneConstructor(doc);
		emptyline();
		generateBuilderConstructor(doc);
		emptyline();
		generatePropertyAccessMethods(doc);
		emptyline();
		generateToStringMethod(doc);
		emptyline();
		generateCloneMethod(doc);
		emptyline();
		generateCopyMethod(doc);
		emptyline();
		generateGetPropertyValueMethod(doc);
		emptyline();
		generateAdditionalMethods(doc);
		
		if (doc.isComparable()){
			emptyline();
			generateCompareMethod(doc);
		}
		
		emptyline();
		generateDefNameMethod(doc);
		
		emptyline();
		generateDefParentNameMethod(doc);

		emptyline();
		generateGetFootprintMethod(doc);
		
		emptyline();
		generateEqualsMethod(doc);

		return clazz;
	}
	
	private void generateDefaultConstructor(MetaDocument doc){
		appendString("public "+getDocumentImplName(doc)+"(String anId){");
		increaseIdent();
		appendStatement("id = anId");
		append(closeBlock());
	}
	
	private void generateCloneConstructor(MetaDocument doc){
		appendString("public "+getDocumentImplName(doc)+"("+getDocumentImplName(doc)+" toClone){");
		increaseIdent();
		//TODO add multilingual support 
		appendStatement("this.id = toClone.id");
		appendStatement("copyAttributesFrom(toClone)");
		append(closeBlock());
	}

	private void generateBuilderConstructor(MetaDocument doc){
		appendString(getDocumentImplName(doc)+"("+getDocumentBuilderName(doc)+" builder){");
		increaseIdent();
		//TODO add multilingual support 
		appendStatement("id = "+quote(""));
		for (MetaProperty p : doc.getProperties()){
			appendStatement(p.getName().toLowerCase()," = ", "builder.", p.getName());
		}
		
		for (MetaProperty p : doc.getLinks()){
			appendStatement(p.getName().toLowerCase()," = ", "builder.", p.getName());
		}

		append(closeBlock());
	}

	private void generatePropertyFields(MetaDocument doc){
		_generatePropertyField(id);
		_generatePropertyFields(doc.getProperties());
		_generatePropertyFields(doc.getLinks());
		
		//support for dao information
		_generatePropertyField(daoCreated);
		_generatePropertyField(daoUpdated);
	}
	
	private MetaProperty getMetaGenericProperty(MetaListProperty p){
		MetaProperty tmp = new MetaGenericProperty(p.getName(), MetaProperty.Type.LIST, p.getContainedProperty());
		if (p.isMultilingual())
			tmp.setMultilingual(true);
		return tmp;
	}
	
	private void _generatePropertyFields(List<MetaProperty> propertyList){
		for (int i=0; i<propertyList.size(); i++){
			MetaProperty p = propertyList.get(i);
			if (p instanceof MetaTableProperty){
				List<MetaProperty> columns = ((MetaTableProperty)p).getColumns();
				for (int t=0; t<columns.size(); t++)
					_generatePropertyField(columns.get(t));
			}else if (p instanceof MetaListProperty){
				_generatePropertyField(getMetaGenericProperty((MetaListProperty)p));
			}else{
				_generatePropertyField(p);
			}
		}
	}
	
	private void _generatePropertyField(MetaProperty p){
		if (GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported() && p.isMultilingual()){
			throw new AssertionError("Multilingual support for VOs not yet implemented!");
			/*
			for (String l: context.getLanguages()){
				String decl = PROPERTY_DECLARATION;
				decl += p.toNameConstant(l);
				decl += "\t= \""+p.getName()+"_"+l+"\"";
				appendStatement(decl);
			}
			return ret;
			*/
		}else{
			appendStatement("private "+p.toJavaType()+" "+p.getName().toLowerCase());
		}
	}
	
	
	private void generatePropertyAccessMethods(MetaDocument doc){
		
		generatePropertyGetterMethod(id);
		_generatePropertyAccessMethods(doc.getProperties());
		_generatePropertyAccessMethods(doc.getLinks());

		// the VO knows this information even its not in the interface 
		generatePropertyGetterMethod(daoCreated);
		generatePropertySetterMethod(daoCreated);
		generatePropertyGetterMethod(daoUpdated);
		generatePropertySetterMethod(daoUpdated);
	}
	
	private void _generatePropertyAccessMethods(List<MetaProperty> properties){
		
		for (int i=0; i<properties.size(); i++){
			MetaProperty p = properties.get(i);
			generatePropertyGetterMethod(p);
			emptyline();
			generatePropertySetterMethod(p);
			emptyline();
		}
	}
	
	private void generatePropertyGetterMethod(MetaProperty p){
		if (p instanceof MetaTableProperty){
			generateTablePropertyGetterMethods((MetaTableProperty)p);
			return;
		}
		if (p instanceof MetaListProperty){
			generateListPropertyGetterMethods((MetaListProperty)p);
			return;
		}
		if (GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported() && p.isMultilingual()){
			generatePropertyGetterMethodMultilingual(p);
			return;
		}
		
		appendString("public "+p.toJavaType()+" get"+p.getAccesserName()+"(){");
		increaseIdent();
		appendStatement("return "+p.getName().toLowerCase());
		append(closeBlock());
	}
	
	private void generatePropertyGetterMethodMultilingual(MetaProperty p){
		for (String l : GeneratorDataRegistry.getInstance().getContext().getLanguages()){
			appendString("public "+p.toJavaType()+" get"+p.getAccesserName(l)+"(){");
			increaseIdent();
			appendStatement("return "+p.toPropertyGetter()+"("+p.toNameConstant(l)+")");
			append(closeBlock());
			emptyline();
		}
		appendString("public "+p.toJavaType()+" get"+p.getAccesserName()+"(){");
		increaseIdent();
		String v = "ContextManager.getCallContext().getCurrentLanguage()";
		appendStatement("return "+p.toPropertyGetter()+"("+quote(p.getName()+"_")+"+"+v+")");
		append(closeBlock());
		emptyline();
		
	}
	
	
	private void generateListPropertyGetterMethods(MetaListProperty p){
		MetaProperty tmp = getMetaGenericProperty(p);
		generatePropertyGetterMethod(tmp);
	}
	
	private void generateTablePropertyGetterMethods(MetaTableProperty p){
		List<MetaProperty> columns = p.getColumns();
		for (int t=0; t<columns.size(); t++)
			generatePropertyGetterMethod(columns.get(t));
	}
	
	private void generatePropertySetterMethod(MetaProperty p){

		if (p instanceof MetaTableProperty){
			generateTablePropertySetterMethods((MetaTableProperty)p);
			return;
		}
		if (p instanceof MetaListProperty){
			generateListPropertySetterMethods((MetaListProperty)p);
			return;
		}
		if (GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported() && p.isMultilingual()){
			generatePropertySetterMethodMultilingual(p);
			return;
		}

		appendString("public void set"+p.getAccesserName()+"("+p.toJavaType()+" value){");
		increaseIdent();
		appendStatement("this."+p.getName().toLowerCase()+" = value");
		append(closeBlock());	
	}
	
	private void generatePropertySetterMethodMultilingual(MetaProperty p){
		for (String l : GeneratorDataRegistry.getInstance().getContext().getLanguages()){
			appendString("public void set"+p.getAccesserName(l)+"("+p.toJavaType()+" value){");
			increaseIdent();
			appendStatement(""+p.toPropertySetter()+"("+p.toNameConstant(l)+", value)");
			append(closeBlock());
			emptyline();
		}
		appendString("public void set"+p.getAccesserName()+"("+p.toJavaType()+" value){");
		increaseIdent();
		String v = "ContextManager.getCallContext().getCurrentLanguage()";
		appendStatement(""+p.toPropertySetter()+"("+"("+quote(p.getName()+"_")+"+"+v+")"+", value)");
		append(closeBlock());
		emptyline();
	}

	private void generateListPropertySetterMethods(MetaListProperty p){
		MetaProperty tmp = getMetaGenericProperty(p);
		generatePropertySetterMethod(tmp);
	}

	private void generateTablePropertySetterMethods(MetaTableProperty p){
		List<MetaProperty> columns = p.getColumns();
		for (int t=0; t<columns.size(); t++)
			generatePropertySetterMethod(columns.get(t));
	}

	private void generateToStringMethod(MetaDocument doc){
		appendString("public String toString(){");
		increaseIdent();
		appendStatement("String ret = "+quote(doc.getName()+" "));
		appendStatement("ret += \"[\"+getId()+\"] \"");
		List<MetaProperty> props = doc.getProperties();
		for (int i=0; i<props.size(); i++){
			MetaProperty p = props.get(i);
			if (p instanceof MetaTableProperty){
				List<MetaProperty> columns = ((MetaTableProperty)p).getColumns();
				for (int t=0; t<columns.size(); t++){
					MetaProperty pp = columns.get(t);
					appendStatement("ret += "+quote(pp.getName()+": ")+"+get"+pp.getAccesserName()+"()");
					if (t<columns.size()-1)
						appendStatement("ret += \", \"");
				}
			}else{
				appendStatement("ret += "+quote(p.getName()+": ")+"+get"+p.getAccesserName()+"()");
			}
			if (i<props.size()-1)
				appendStatement("ret += \", \"");
		}
		appendStatement("return ret"); 
		append(closeBlock());
	}
	
	private void generateCloneMethod(MetaDocument doc){
		appendString("public "+getDocumentImplName(doc)+" clone(){");
		increaseIdent();
		appendStatement("return ("+getDocumentImplName(doc)+") super.clone()");
		append(closeBlock());
	}
	
	private void generateGetPropertyValueMethod(MetaDocument doc){
		appendString("public Object getPropertyValue(String propertyName){");
		increaseIdent();
		List <MetaProperty>properties = new ArrayList<MetaProperty>(doc.getProperties());
		properties.addAll(doc.getLinks());
		// Generate getter for ID property
		appendString("if ("+id.toNameConstant()+".equals(propertyName))");
		appendIncreasedStatement("return get"+id.getAccesserName()+"()");
		// Generate getters for other properties 
		for (MetaProperty p : properties){
			appendString("if ("+p.toNameConstant()+".equals(propertyName))");
			appendIncreasedStatement("return get"+p.getAccesserName()+"()");
					
		}

		appendStatement("throw new RuntimeException("+quote("No property getter for ")+"+propertyName)");
		append(closeBlock());
	}
	


	public static final String getDocumentImport(Context context, MetaDocument doc){
		return context.getPackageName(doc)+".data."+getDocumentImplName(doc);
	}
	
	private void generateAdditionalMethods(MetaDocument doc){
		
		List <MetaProperty>properties = doc.getProperties();
		for (MetaProperty p : properties){
			if (p instanceof MetaContainerProperty)
				generateContainerMethods((MetaContainerProperty)p);
			if (p instanceof MetaTableProperty)
				generateTableMethods((MetaTableProperty)p);
			if (p instanceof MetaListProperty)
				generateListMethods((MetaListProperty)p);
		}
		
	}
	
	private void generateContainerMethods(MetaContainerProperty container){

		appendString("public int "+getContainerSizeGetterName(container)+"(){");
		increaseIdent();
//		MetaProperty pr = container instanceof MetaTableProperty ? 
//			((MetaTableProperty)container).getColumns().get(0) :
//			container;
//		appendStatement("return getList("+pr.toNameConstant()+").size()"); 
		appendStatement("return get"+container.getAccesserName()+"().size()");
		append(closeBlock());
		emptyline();
	}
	
	private void generateListMethods(MetaListProperty list){
		MetaProperty genericList = getMetaGenericProperty(list);
		MetaProperty c = list.getContainedProperty();

		String decl = "public void "+getContainerEntryAdderName(list)+"(";
		decl += c.toJavaType()+" "+c.getName();
		decl += "){";
		appendString(decl);
		increaseIdent();
		
		appendStatement(c.toJavaObjectType()+" p = new "+c.toJavaObjectType()+"("+c.getName()+")");
		appendStatement(genericList.toJavaType() + " tmp = get"+list.getAccesserName()+"()");
		appendStatement("tmp.add(p)");
//		appendStatement("set"+list.getAccesserName()+"(tmp)");
		append(closeBlock());
		emptyline();
		
		
		appendString("public void "+getContainerEntryDeleterName(list)+"(int index){");
		increaseIdent();
//		appendStatement("getListProperty("+list.toNameConstant()+").remove(index)"); 
		appendStatement("get"+list.getAccesserName()+"().remove(index)");
		append(closeBlock());
		emptyline();
		
		appendString("public void "+getContainerEntrySwapperName(list)+"(int index1, int index2){");
		increaseIdent();
		appendStatement(c.toJavaType()+" tmp1, tmp2");
//		appendStatement("tmp1 = (("+c.toJavaType()+")getList("+list.toNameConstant()+").get(index1)).get"+c.toJavaType()+"()");
//		appendStatement("tmp2 = (("+c.toJavaType()+")getList("+list.toNameConstant()+").get(index2)).get"+c.toJavaType()+"()");
//		appendStatement("(("+c.toJavaType()+"Property"+")getList("+list.toNameConstant()+").get(index1)).set"+c.toJavaType()+"(tmp2)");
//		appendStatement("(("+c.toJavaType()+"Property"+")getList("+list.toNameConstant()+").get(index2)).set"+c.toJavaType()+"(tmp1)");
		appendStatement(genericList.toJavaType() + " tmpList = get"+list.getAccesserName()+"()");
		appendStatement("tmp1 = (("+c.toJavaObjectType()+")tmpList.get(index1))");
		appendStatement("tmp2 = (("+c.toJavaObjectType()+")tmpList.get(index2))");
		appendStatement("tmpList.set(index1, tmp2)");
		appendStatement("tmpList.set(index2, tmp1)");
		append(closeBlock());
		emptyline();

		appendString("public "+c.toJavaType()+ " "+getListElementGetterName(list)+"(int index){");
		increaseIdent();
//		appendStatement(c.toJavaType()+"Property p = ("+c.toJavaType()+"Property"+")getList("+list.toNameConstant()+").get(index)");
//		appendStatement("return p.get"+c.toJavaType()+"()");
		appendStatement(c.toJavaType()+" p = ("+c.toJavaObjectType()+""+")get"+list.getAccesserName()+"().get(index)");
		appendStatement("return p");
		append(closeBlock());
		emptyline();
	}
	
	private void generateTableMethods(MetaTableProperty table){
		List<MetaProperty> columns = table.getColumns();
		
		String decl = "public void "+getContainerEntryAdderName(table)+"(";
		for (int i =0; i<columns.size(); i++){
			MetaProperty p = columns.get(i);
			decl += "String "+table.extractSubName(p);
			if (i<columns.size()-1)
				decl += ", ";
		}
		decl += "){";
		appendString(decl);
		increaseIdent();
		
		appendStatement("List tmp");
		
		for (int i=0; i<columns.size(); i++){
			MetaProperty p = columns.get(i);
			appendStatement("tmp = getList("+p.toNameConstant()+")"); 
			appendStatement("tmp.add(new StringProperty(\"\", "+table.extractSubName(p)+"))");
			appendStatement("setList("+p.toNameConstant()+", tmp)");
			emptyline();
		}

		append(closeBlock());
		emptyline();
		
		appendString("public void "+getContainerEntryDeleterName(table)+"(int index){");
		increaseIdent();
		for (int i=0; i<columns.size(); i++){
			MetaProperty p = columns.get(i);
			appendStatement("getListProperty("+p.toNameConstant()+").remove(index)"); 
		}
		
		append(closeBlock());
		emptyline();
		
		appendString("public List get"+StringUtils.capitalize(table.getName())+"Row(int index){");
		increaseIdent();
		appendStatement("List ret = new ArrayList(1)");
		for (int i=0; i<columns.size(); i++){
			MetaProperty p = columns.get(i);
			appendString("try{");
			appendIncreasedStatement("ret.add(((StringProperty)getList("+p.toNameConstant()+").get(index)).getString())");
			appendString("}catch(IndexOutOfBoundsException e){ ");
			appendIncreasedStatement("ret.add(\"\")");
			appendString("}");  
		}
		appendStatement("return ret");
		append(closeBlock());
		emptyline();

		appendString("public List "+getTableGetterName(table)+"(){");
		increaseIdent();
		appendStatement("int size = "+getContainerSizeGetterName(table)+"();");
		appendStatement("List ret = new java.util.ArrayList(size)");
		appendString("for (int i=0; i<size; i++)");
		appendIncreasedStatement("ret.add(get"+StringUtils.capitalize(table.getName())+"Row(i))");
		appendStatement("return ret");
		append(closeBlock());
		emptyline();
	}
	
/*	private void generateCompareMethod(MetaDocument doc){
		appendString("public int compareTo("+doc.getName()+" comparable){");
		appendIncreasedStatement("return compareTo(comparable, "+getSortTypeName(doc)+".SORT_BY_DEFAULT)");
		appendString("}");
		append(emptyline());

		appendString("public int compareTo(IComparable anotherComparable, int method){");
		increaseIdent();

		appendStatement(getDocumentImplName(doc)+" anotherDoc = ("+getDocumentImplName(doc)+") anotherComparable");
		appendString("switch(method){");
		increaseIdent();
		List<MetaProperty> properties = extractSortableProperties(doc);

		for (int i=0; i<properties.size(); i++){
			MetaProperty p = properties.get(i);

			String caseDecl = getSortTypeName(doc)+".SORT_BY_"+p.getName().toUpperCase();
			appendString("case "+caseDecl+":");
			String type2compare = null; 
			type2compare = StringUtils.capitalize(p.toJavaType());
			String retDecl = "return BasicComparable.compare"+type2compare;
			retDecl += "(get"+p.getAccesserName()+"(), anotherDoc.get"+p.getAccesserName()+"())";
			appendIncreasedStatement(retDecl);
		}
		appendString("default:");
		appendIncreasedStatement("throw new RuntimeException(\"Sort method \"+method+\" is not supported.\")");
		append(closeBlock());
		append(closeBlock());
	}
	*/
	

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
	
	private void generateCopyMethod(MetaDocument doc){
		appendString("public void copyAttributesFrom("+doc.getName()+" toCopy){");
		increaseIdent();
		for (MetaProperty p : doc.getProperties()){
			appendStatement("this."+p.getName().toLowerCase() + " = toCopy."+p.toGetter()+"()");
		}
		for (MetaProperty p : doc.getLinks()){
			appendStatement("this."+p.getName().toLowerCase() + " = toCopy."+p.toGetter()+"()");
		}
		append(closeBlock());
	}



}
