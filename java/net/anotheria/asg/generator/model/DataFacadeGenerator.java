package net.anotheria.asg.generator.model;

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
import net.anotheria.util.StringUtils;

/**
 * This generator generates the data facade - the interface which defines the behaviour of the document and its attributes. It also generates the 
 * sort type. 
 * @author another
 *
 */
public class DataFacadeGenerator extends AbstractDataObjectGenerator implements IGenerator{

	public static final String PROPERTY_DECLARATION = "public static final String ";	
	
	private Context context;
	
	public List<FileEntry> generate(IGenerateable gdoc, Context context){
		MetaDocument doc = (MetaDocument)gdoc;
		this.context = context;
		
		//System.out.println(ret);
		List<FileEntry> _ret = new ArrayList<FileEntry>();
		_ret.add(new FileEntry(FileEntry.package2path(getPackageName(doc)), getDocumentName(doc), generateDocument(doc)));
		_ret.add(new FileEntry(FileEntry.package2path(getPackageName(doc)), getSortTypeName(doc), generateSortType(doc)));
		return _ret;
	}
	
	public String getDocumentName(MetaDocument doc){
		return doc.getName();
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
	
	private String generateSortType(MetaDocument doc){
		String ret = "";

		List<MetaProperty> properties = extractSortableProperties(doc);

		// ??? boolean containsComparable = false;
		
		if (properties.size()==0)
			return null;
			
		ret += writeStatement("package "+getPackageName(doc));
		ret += emptyline();
		ret += writeImport("net.anotheria.util.sorter.SortType");
		ret += emptyline();
		
		ret += writeString("public class "+getSortTypeName(doc)+" extends SortType{");
		increaseIdent();
		
		
		int lastIndex = 1;
		for (int i=0; i<properties.size(); i++){
			MetaProperty p = properties.get(i);
			ret += writeStatement("public static final int SORT_BY_"+p.getName().toUpperCase()+" = "+(lastIndex++));
		}
		ret += writeStatement("public static final int SORT_BY_DEFAULT = SORT_BY_ID");

		ret += emptyline();

		ret += writeString("public "+getSortTypeName(doc)+"(){");
		increaseIdent();
		ret += writeString("super(SORT_BY_DEFAULT);");
		ret += closeBlock();
		ret += emptyline();

		ret += writeString("public "+getSortTypeName(doc)+"(int method){");
		increaseIdent();
		ret += writeString("super(method);");
		ret += closeBlock();
		ret += emptyline();
				
		ret += writeString("public "+getSortTypeName(doc)+"(int method, boolean order){");
		increaseIdent();
		ret += writeString("super(method, order);");
		ret += closeBlock();
		ret += emptyline();
		
		ret += writeString("public static int name2method(String name){");
		increaseIdent();
		for (int i=0; i<properties.size(); i++){
			MetaProperty p = properties.get(i);
				ret += writeString("if ("+quote(p.getName())+".equals(name))");
				ret += writeIncreasedStatement("return SORT_BY_"+p.getName().toUpperCase());
		}
		ret += writeStatement("throw new RuntimeException("+quote("Unknown sort type name: ")+"+name)");		
		ret += closeBlock();

		ret += closeBlock();
		

		return ret;
		
		
	}
	
	private String generateDocument(MetaDocument doc){
		String ret = "";
		
	
		ret += writeStatement("package "+getPackageName(doc));
		ret += emptyline();
		ret += writeImport("net.anotheria.asg.data.DataObject");
		
		boolean listImported = false;
		for (int i=0; i<doc.getProperties().size(); i++){
			if (doc.getProperties().get(i) instanceof MetaContainerProperty){
				ret += writeImport("java.util.List");
				listImported = true;
				break;
			}
		}
		for (int i=0; i<doc.getProperties().size(); i++){
			if (doc.getProperties().get(i) instanceof MetaTableProperty){
				if (!listImported){
					ret += writeImport("java.util.List");
				}
				break;
			}
		}
		
		String interfaceDecl = " extends DataObject";
		if (doc.isComparable()){
			ret += writeImport("net.anotheria.util.sorter.IComparable");
			ret += emptyline();
			interfaceDecl += ", IComparable ";
		}
		
		
		ret += writeString("public interface "+doc.getName()+interfaceDecl+"{");
		increaseIdent();
		ret += generatePropertyConstants(doc);
		ret += emptyline();
		ret += generatePropertyAccessMethods(doc);
		ret += emptyline();
		ret += generateAdditionalMethods(doc);
		ret += closeBlock();
		return ret;
	}
	
	private String generatePropertyConstants(MetaDocument doc){
		String ret = "";
		ret += _generatePropertyConstants(doc.getProperties());
		ret += _generatePropertyConstants(doc.getLinks());
		return ret;
	}
	
	private String _generatePropertyConstants(List<MetaProperty> propertyList){
		String ret = "";
		for (int i=0; i<propertyList.size(); i++){
			MetaProperty p = propertyList.get(i);
			if (p instanceof MetaTableProperty){
				List<MetaProperty> columns = ((MetaTableProperty)p).getColumns();
				for (int t=0; t<columns.size(); t++)
					ret += _generatePropertyConstant(columns.get(t));
			}else{
				ret += _generatePropertyConstant(p);
			}
		}
		return ret;
	}
	
	private String _generatePropertyConstant(MetaProperty p){
		String ret = "";
		if (context.areLanguagesSupported() && p.isMultilingual()){
			for (String l: context.getLanguages()){
				String decl = PROPERTY_DECLARATION;
				decl += p.toNameConstant(l);
				decl += "\t= \""+p.getName()+"_"+l+"\"";
				ret += writeComment("Constant property name for \""+p.getName()+"\" and domain \""+l+"\" for internal storage and queries.");
				ret += writeStatement(decl);
			}
			return ret;
		}else{
			String r = PROPERTY_DECLARATION;
			r += p.toNameConstant();
			r += "\t= \""+p.getName()+"\"";
			ret += writeComment("Constant property name for \""+p.getName()+"\" for internal storage and queries.");
			ret += writeStatement(r);
			return ret;
		}
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
			if (!p.isReadonly()){
				ret += generatePropertySetterMethod(p);
				ret += emptyline();
			}
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
		
		ret += writeComment("Returns the value of the "+p.getName()+" attribute.");
		ret += writeString("public "+p.toJavaType()+" get"+p.getAccesserName()+"();");
		return ret;
	}
	
	private String generatePropertyGetterMethodMultilingual(MetaProperty p){
		String ret = "";
		for (String l : context.getLanguages()){
			ret += writeComment("Returns the value of the "+p.getName()+" attribute in the \""+l+"\" domain.");
			ret += writeString("public "+p.toJavaType()+" get"+p.getAccesserName(l)+"();");
			ret += emptyline();
		}
		ret += writeComment("Returns the current value of the "+p.getName()+" attribute.\nCurrent means in the currently selected domain.");
		ret += writeString("public "+p.toJavaType()+" get"+p.getAccesserName()+"();");
		ret += emptyline();
		return ret;
	}
	
	
	private String generateListPropertyGetterMethods(MetaListProperty p){
		MetaProperty tmp = new MetaGenericProperty(p.getName(), "list", p.getContainedProperty());
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

		ret += writeComment("Sets the value of the "+p.getName()+" attribute.");
		ret += writeString("public void set"+p.getAccesserName()+"("+p.toJavaType()+" value);");
		return ret;
	}
	
	private String generatePropertySetterMethodMultilingual(MetaProperty p){
		String ret = "";
		for (String l : context.getLanguages()){
			ret += writeComment("Sets the value of the "+p.getName()+" attribute in the domain \""+l+"\"");
			ret += writeString("public void set"+p.getAccesserName(l)+"("+p.toJavaType()+" value);");
			ret += emptyline();
		}
		ret += writeComment("Sets the value of the "+p.getName()+" attribute in the current domain. Current means in the currently selected domain.");
		ret += writeString("public void set"+p.getAccesserName()+"("+p.toJavaType()+" value);");
		return ret;
	}

	private String generateListPropertySetterMethods(MetaListProperty p){
		MetaProperty tmp = new MetaGenericProperty(p.getName(), "list", p.getContainedProperty());
		if (p.isMultilingual())
			tmp.setMultilingual(true);
		return generatePropertySetterMethod(tmp);
	}

	private String generateTablePropertySetterMethods(MetaTableProperty p){
		String ret = "";
		List<MetaProperty> columns = p.getColumns();
		for (int t=0; t<columns.size(); t++)
			ret += generatePropertySetterMethod(columns.get(t));
		return ret;
	}

	
	public static final String getDocumentImport(Context context, MetaDocument doc){
		return context.getDataPackageName(doc)+"."+doc.getName();
	}
	
	public static final String getSortTypeImport(MetaDocument doc){
		return GeneratorDataRegistry.getInstance().getContext().getDataPackageName(doc)+"."+getSortTypeName(doc);
		
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
		if (container.isMultilingual())
			ret += generateContainerMethodsMultilingual(container);
		ret += writeComment("Returns the number of elements in the \""+container.getName()+"\" container");
		ret += writeString("public int "+getContainerSizeGetterName(container)+"();");
		ret += emptyline();
		return ret;
	}
	
	private String generateContainerMethodsMultilingual(MetaContainerProperty container){
		String ret = "";
		for (String l : context.getLanguages()){
			ret += writeComment("Returns the number of elements in the \""+container.getName()+"\" container");
			ret += writeString("public int "+getContainerSizeGetterName(container, l)+"();");
			ret += emptyline();
		}
		return ret;
	}

	private String generateListMethods(MetaListProperty list){
		String ret = "";

		if (list.isMultilingual())
			ret += generateListMethodsMultilingual(list);
		
		MetaProperty c = list.getContainedProperty();
		ret += writeComment("Adds a new element to the list.");
		String decl = "public void "+getContainerEntryAdderName(list)+"(";
		decl += c.toJavaType()+" "+c.getName();
		decl += ");";
		ret += writeString(decl);
		ret += emptyline();
		
		ret += writeComment("Removes the element at position index from the list.");
		ret += writeString("public void "+getContainerEntryDeleterName(list)+"(int index);");
		ret += emptyline();
		
		ret += writeComment("Swaps elements at positions index1 and index2 in the list.");
		ret += writeString("public void "+getContainerEntrySwapperName(list)+"(int index1, int index2);");
		ret += emptyline();
		
		ret += writeComment("Returns the element at the position index in the list.");
		ret += writeString("public "+c.toJavaType()+ " "+getListElementGetterName(list)+"(int index);");
		ret += emptyline();

		return ret;
	}
	
	private String generateListMethodsMultilingual(MetaListProperty list){
		String ret = "";

		for (String l : context.getLanguages()){
		
			MetaProperty c = list.getContainedProperty();
			ret += writeComment("Adds a new element to the list.");
			String decl = "public void "+getContainerEntryAdderName(list, l)+"(";
			decl += c.toJavaType()+" "+c.getName();
			decl += ");";
			ret += writeString(decl);
			ret += emptyline();
			
			ret += writeComment("Removes the element at position index from the list.");
			ret += writeString("public void "+getContainerEntryDeleterName(list, l)+"(int index);");
			ret += emptyline();
			
			ret += writeComment("Swaps elements at positions index1 and index2 in the list.");
			ret += writeString("public void "+getContainerEntrySwapperName(list, l)+"(int index1, int index2);");
			ret += emptyline();
			
			ret += writeComment("Returns the element at the position index in the list.");
			ret += writeString("public "+c.toJavaType()+ " "+getListElementGetterName(list, l)+"(int index);");
			ret += emptyline();
		}

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
		decl += ");";
		ret += writeString(decl);
		ret += emptyline();
		
		ret += writeString("public void "+getContainerEntryDeleterName(table)+"(int index);");
		ret += emptyline();
		
		ret += writeString("public List<String> get"+StringUtils.capitalize(table.getName())+"Row(int index);");
		ret += emptyline();

		ret += writeString("public List<List<String>> "+getTableGetterName(table)+"();");
		ret += emptyline();
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
	    return "add"+StringUtils.capitalize(p.getName())+p.getContainerEntryName();	    
	}

	public static String getContainerEntryAdderName(MetaContainerProperty p, String language){
	    return "add"+StringUtils.capitalize(p.getName(language))+p.getContainerEntryName();	    
	}
	
	public static String getContainerEntryDeleterName(MetaContainerProperty p){
		return "remove"+StringUtils.capitalize(p.getName())+p.getContainerEntryName();	    
	}

	public static String getContainerEntryDeleterName(MetaContainerProperty p, String language){
		return "remove"+StringUtils.capitalize(p.getName(language))+p.getContainerEntryName();	    
	}

	public static String getContainerEntrySwapperName(MetaContainerProperty p){
		return "swap"+StringUtils.capitalize(p.getName())+p.getContainerEntryName();	    
	}
	
	public static String getContainerEntrySwapperName(MetaContainerProperty p, String language){
		return "swap"+StringUtils.capitalize(p.getName(language))+p.getContainerEntryName();	    
	}

	public static String getListElementGetterName(MetaListProperty list){
		return "get"+StringUtils.capitalize(list.getName())+list.getContainerEntryName();
	}

	public static String getListElementGetterName(MetaListProperty list, String language){
		return "get"+StringUtils.capitalize(list.getName(language))+list.getContainerEntryName();
	}

	public static String getDocumentFactoryName(MetaDocument doc){
		return doc.getName()+"Factory";
	}

	public static final String getDocumentFactoryImport(Context context, MetaDocument doc){
		return context.getDataPackageName(doc)+"."+getDocumentFactoryName(doc);
	}


}
