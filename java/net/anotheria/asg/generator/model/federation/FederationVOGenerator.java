package net.anotheria.asg.generator.model.federation;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.meta.MetaContainerProperty;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaListProperty;
import net.anotheria.asg.generator.meta.MetaProperty;
import net.anotheria.asg.generator.meta.MetaTableProperty;
import net.anotheria.asg.generator.model.AbstractDataObjectGenerator;
import net.anotheria.util.StringUtils;

/**
 * This generator generates VO objects for a Module Federation.
 * @author another
 */
public class FederationVOGenerator extends AbstractDataObjectGenerator
	implements IGenerator{

	/**
	 * Id property. Most documents do not include id properties, but its required by the application and has to be added here.
	 */
	private final MetaProperty id = new MetaProperty("id",MetaProperty.Type.STRING);
	/**
	 * Last update timestamp property.
	 */
	private final MetaProperty lastUpdate = new MetaProperty("lastUpdateTimestamp", MetaProperty.Type.LONG);
	
	public List<FileEntry> generate(IGenerateable gdoc){
		MetaDocument doc = (MetaDocument)gdoc;
		id.setReadonly(true);
		
		
		//System.out.println(ret);
		List<FileEntry> ret = new ArrayList<FileEntry>();
		ret.add(new FileEntry(generateDocument(doc)));
		ret.add(new FileEntry(generateDocumentFactory(doc)));
		return ret;
	}
	
	public String getDataObjectImplName(MetaDocument doc){
		return getDocumentImplName(doc);
	}
	
	public static String getDocumentImplName(MetaDocument doc){
		return doc.getName()+"VO";
	}
	

	/**
	 * Generates the federated VO.
	 * @param doc
	 * @return
	 */
	private GeneratedClass generateDocument(MetaDocument doc){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(getPackageName(doc));
		
		for (int i=0; i<doc.getProperties().size(); i++){
			if (doc.getProperties().get(i) instanceof MetaContainerProperty){
				clazz.addImport("java.util.List");
				clazz.addImport("java.util.ArrayList");
				break;
			}
		}
		for (int i=0; i<doc.getProperties().size(); i++){
			if (doc.getProperties().get(i) instanceof MetaTableProperty){
				clazz.addImport("java.util.List");
				clazz.addImport("java.util.ArrayList");
				break;
			}
		}
		
		for (MetaProperty p : doc.getProperties()){
			if (p.isMultilingual() && GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported()){
				clazz.addImport("net.anotheria.anodoc.util.context.ContextManager");
				break;
			}
		}

		clazz.addImport("net.anotheria.util.xml.XMLNode");
		clazz.addImport("net.anotheria.util.crypt.MD5Util");
		clazz.addImport("net.anotheria.asg.data.AbstractFederatedVO");

		
		clazz.addInterface(doc.getName());
		
		if (doc.isComparable()){
			clazz.addImport("net.anotheria.util.sorter.IComparable");
			clazz.addImport("net.anotheria.util.BasicComparable");
			clazz.addInterface("IComparable");
		}
		
		clazz.setName(getDocumentImplName(doc));
		clazz.setParent("AbstractFederatedVO");
		
		startClassBody();
		generatePropertyFields(doc);
		emptyline();
		generateDefaultConstructor(doc);
		emptyline();
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
		closeBlockNEW();
	}
	
	private void generateCloneConstructor(MetaDocument doc){
		appendString("public "+getDocumentImplName(doc)+"("+getDocumentImplName(doc)+" toClone){");
		increaseIdent();
		//TODO add multilingual support 
		appendStatement("this.id = toClone.id");
		appendStatement("copyAttributesFrom(toClone)");
		closeBlockNEW();
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

		closeBlockNEW();
	}


	private void generatePropertyFields(MetaDocument doc){
		generatePropertyField(id);
		generatePropertyFields(doc.getProperties());
		generatePropertyFields(doc.getLinks());
		generatePropertyField(lastUpdate);
		
	}
	
	private void generatePropertyFields(List<MetaProperty> propertyList){
		for (int i=0; i<propertyList.size(); i++){
			MetaProperty p = propertyList.get(i);
			if (p instanceof MetaTableProperty){
				List<MetaProperty> columns = ((MetaTableProperty)p).getColumns();
				for (int t=0; t<columns.size(); t++)
					generatePropertyField(columns.get(t));
			}else{
				generatePropertyField(p);
			}
		}
	}
	
	private void generatePropertyField(MetaProperty p){
		if (GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported() && p.isMultilingual()){
			System.err.println("Multilingual support for federated VOs not yet implemented!");
			/*
			for (String l: context.getLanguages()){
				String decl = PROPERTY_DECLARATION;
				decl += p.toNameConstant(l);
				decl += "\t= \""+p.getName()+"_"+l+"\"";
				appendStatement(decl);
			}
			return ret;
			*/
			return ;
		}else{
			appendStatement("private "+p.toJavaType()+" "+p.getName().toLowerCase());
		}
	}
	
	
	private void generatePropertyAccessMethods(MetaDocument doc){
		generatePropertyGetterMethod(id);
		List<MetaProperty> lastUpdList = new ArrayList<MetaProperty>();
		lastUpdList.add(lastUpdate);
		generatePropertyAccessMethods(lastUpdList);
		generatePropertyAccessMethods(doc.getProperties());
		generatePropertyAccessMethods(doc.getLinks());
	}
	
	private void generatePropertyAccessMethods(List<MetaProperty> properties){
		
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
		closeBlockNEW();
	}
	
	private void generatePropertyGetterMethodMultilingual(MetaProperty p){
		for (String l : GeneratorDataRegistry.getInstance().getContext().getLanguages()){
			appendString("public "+p.toJavaType()+" get"+p.getAccesserName(l)+"(){");
			increaseIdent();
			appendStatement("return "+p.toPropertyGetter()+"("+p.toNameConstant(l)+")");
			closeBlockNEW();
			emptyline();
		}
		appendString("public "+p.toJavaType()+" get"+p.getAccesserName()+"(){");
		increaseIdent();
		String v = "ContextManager.getCallContext().getCurrentLanguage()";
		appendStatement("return "+p.toPropertyGetter()+"("+quote(p.getName()+"_")+"+"+v+")");
		closeBlockNEW();
		emptyline();
	}
	
	
	private void generateListPropertyGetterMethods(MetaListProperty p){
		MetaProperty tmp = new MetaProperty(p.getName(), MetaProperty.Type.LIST);
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
			return ;
		}
		if (p instanceof MetaListProperty){
			generateListPropertySetterMethods((MetaListProperty)p);
			return ;
		}
		if (GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported() && p.isMultilingual()){
			generatePropertySetterMethodMultilingual(p);
			return;
		}

		appendString("public void set"+p.getAccesserName()+"("+p.toJavaType()+" value){");
		increaseIdent();
		appendStatement(""+p.getName().toLowerCase()+" = value");
		closeBlockNEW();	
	}
	
	private String generatePropertySetterMethodMultilingual(MetaProperty p){
		String ret = "";
		for (String l : GeneratorDataRegistry.getInstance().getContext().getLanguages()){
			appendString("public void set"+p.getAccesserName(l)+"("+p.toJavaType()+" value){");
			increaseIdent();
			appendStatement(""+p.toPropertySetter()+"("+p.toNameConstant(l)+", value)");
			closeBlockNEW();
			emptyline();
		}
		appendString("public void set"+p.getAccesserName()+"("+p.toJavaType()+" value){");
		increaseIdent();
		String v = "ContextManager.getCallContext().getCurrentLanguage()";
		appendStatement(""+p.toPropertySetter()+"("+"("+quote(p.getName()+"_")+"+"+v+")"+", value)");
		closeBlockNEW();
		emptyline();
		return ret;
	}

	private void generateListPropertySetterMethods(MetaListProperty p){
		MetaProperty tmp = new MetaProperty(p.getName(), MetaProperty.Type.LIST);
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
		closeBlockNEW();
	}
	
	private void generateCloneMethod(MetaDocument doc){
		appendString("public "+doc.getName()+" clone(){");
		increaseIdent();
		appendStatement("return new "+getDocumentImplName(doc)+"(this)");
		closeBlockNEW();
	}
	
	private void generateCopyMethod(MetaDocument doc){
		appendString("public void copyAttributesFrom("+doc.getName()+" toCopy){");
		increaseIdent();
		for (MetaProperty p : doc.getProperties()){
			appendStatement("this."+p.getName().toLowerCase() + " = toCopy."+p.toGetter()+"()");
		}
		closeBlockNEW();
	}

	private void generateGetPropertyValueMethod(MetaDocument doc){
		appendString("public Object getPropertyValue(String propertyName){");
		increaseIdent();
		appendStatement("throw new RuntimeException("+quote("Not yet implemented.")+")");
		closeBlockNEW();
	}
	


	public static final String getDocumentImport(Context context, MetaDocument doc){
		return context.getDataPackageName(doc)+"."+getDocumentImplName(doc);
	}
	
	private void generateAdditionalMethods(MetaDocument doc){
		List<MetaProperty> properties = doc.getProperties();
		for (MetaProperty p : properties){
			if (p instanceof MetaContainerProperty)
				generateContainerMethods((MetaContainerProperty)p);
			if (p instanceof MetaTableProperty)
				generateTableMethods((MetaTableProperty)p);
			if (p instanceof MetaListProperty)
				generateListMethods((MetaListProperty)p);
		}
		
		appendString("public XMLNode toXMLNode(){");
		increaseIdent();
		appendStatement("return new XMLNode("+quote("not_imlpemented_fed_vo")+")");
		closeBlockNEW();
	}
	
	private void generateContainerMethods(MetaContainerProperty container){
		appendString("public int "+getContainerSizeGetterName(container)+"(){");
		increaseIdent();
		MetaProperty pr = container instanceof MetaTableProperty ? 
			(MetaProperty) ((MetaTableProperty)container).getColumns().get(0) :
			container;
		appendStatement("return getList("+pr.toNameConstant()+").size()"); 
		closeBlockNEW();
		emptyline();
		
	}
	
	private void generateListMethods(MetaListProperty list){
		MetaProperty c = list.getContainedProperty();

		String decl = "public void "+getContainerEntryAdderName(list)+"(";
		decl += c.toJavaType()+" "+c.getName();
		decl += "){";
		appendString(decl);
		increaseIdent();
		
		appendStatement(c.toJavaType()+"Property p = new "+c.toJavaType()+"Property("+c.getName()+", "+c.getName()+")");
		appendStatement("List tmp = get"+list.getAccesserName()+"()");
		appendStatement("tmp.add(p)");
		appendStatement("set"+list.getAccesserName()+"(tmp)");
		closeBlockNEW();
		emptyline();
		
		
		appendString("public void "+getContainerEntryDeleterName(list)+"(int index){");
		increaseIdent();
		appendStatement("getListProperty("+list.toNameConstant()+").remove(index)"); 
		closeBlockNEW();
		emptyline();
		
		appendString("public void "+getContainerEntrySwapperName(list)+"(int index1, int index2){");
		increaseIdent();
		appendStatement(c.toJavaType()+" tmp1, tmp2");
		appendStatement("tmp1 = (("+c.toJavaType()+"Property"+")getList("+list.toNameConstant()+").get(index1)).get"+c.toJavaType()+"()");
		appendStatement("tmp2 = (("+c.toJavaType()+"Property"+")getList("+list.toNameConstant()+").get(index2)).get"+c.toJavaType()+"()");
		appendStatement("(("+c.toJavaType()+"Property"+")getList("+list.toNameConstant()+").get(index1)).set"+c.toJavaType()+"(tmp2)");
		appendStatement("(("+c.toJavaType()+"Property"+")getList("+list.toNameConstant()+").get(index2)).set"+c.toJavaType()+"(tmp1)");
		closeBlockNEW();
		emptyline();

		appendString("public "+c.toJavaType()+ " "+getListElementGetterName(list)+"(int index){");
		increaseIdent();
		appendStatement(c.toJavaType()+"Property p = ("+c.toJavaType()+"Property"+")getList("+list.toNameConstant()+").get(index)");
		appendStatement("return p.get"+c.toJavaType()+"()");
		closeBlockNEW();
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

		closeBlockNEW();
		emptyline();
		
		appendString("public void "+getContainerEntryDeleterName(table)+"(int index){");
		increaseIdent();
		for (int i=0; i<columns.size(); i++){
			MetaProperty p = columns.get(i);
			appendStatement("getListProperty("+p.toNameConstant()+").remove(index)"); 
		}
		
		closeBlockNEW();
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
		closeBlockNEW();
		emptyline();

		appendString("public List "+getTableGetterName(table)+"(){");
		increaseIdent();
		appendStatement("int size = "+getContainerSizeGetterName(table)+"();");
		appendStatement("List ret = new java.util.ArrayList(size)");
		appendString("for (int i=0; i<size; i++)");
		appendIncreasedStatement("ret.add(get"+StringUtils.capitalize(table.getName())+"Row(i))");
		appendStatement("return ret");
		closeBlockNEW();
		emptyline();
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

}
