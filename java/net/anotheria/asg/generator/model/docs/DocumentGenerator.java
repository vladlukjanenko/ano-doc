package net.anotheria.asg.generator.model.docs;

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
import net.anotheria.asg.generator.meta.MetaEnumerationProperty;
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
public class DocumentGenerator extends AbstractDataObjectGenerator implements IGenerator{

	public static final String PROPERTY_DECLARATION = "public static final String ";	
	public static final String GET_CURRENT_LANG = "ContextManager.getCallContext().getCurrentLanguage()";

	public List<FileEntry> generate(IGenerateable gdoc){
		MetaDocument doc = (MetaDocument)gdoc;
		
		//System.out.println(ret);
		List<FileEntry> _ret = new ArrayList<FileEntry>();
		_ret.add(new FileEntry(generateDocument(doc)));
		_ret.add(new FileEntry(generateDocumentFactory(doc)));
		return _ret;
	}
	
	public static String getDocumentName(MetaDocument doc){
		return doc.getName()+"Document";
	}

	public String getDataObjectImplName(MetaDocument doc){
		return getDocumentImplName(doc);
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
	
	private GeneratedClass generateDocument(MetaDocument doc){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(getPackageName(doc));
		clazz.addImport("net.anotheria.asg.data.AbstractASGDocument");

		for (MetaProperty p:doc.getProperties()){
			if (p instanceof MetaContainerProperty){
//				appendImport("java.util.List");
				clazz.addImport("java.util.List");
				if (p instanceof MetaTableProperty)
					clazz.addImport("java.util.ArrayList");
//					appendImport("java.util.ArrayList");
				
				if(p instanceof MetaListProperty)
					clazz.addImport("net.anotheria.anodoc.data." + StringUtils.capitalize(((MetaListProperty)p).getContainedProperty().toJavaType()) + "Property");
//				appendImport("net.anotheria.anodoc.data.StringProperty");
//				listImported = true;
								
//				break;
			}
		}
		
		if (doc.isMultilingual() && GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported()){
			clazz.addImport("net.anotheria.anodoc.util.context.ContextManager");
			clazz.addImport("net.anotheria.anodoc.data.NoSuchPropertyException");
			clazz.addImport("net.anotheria.anodoc.data.BooleanProperty");
		}
		
		clazz.addImport("net.anotheria.util.crypt.MD5Util");

		
		
		clazz.addInterface(doc.getName());
		if (doc.isComparable()){
			clazz.addInterface("IComparable");
			clazz.addImport("net.anotheria.util.sorter.IComparable");
			clazz.addImport("net.anotheria.util.BasicComparable");
		}
		
		if (doc.isMultilingual()){
			clazz.addImport("net.anotheria.asg.data.MultilingualObject");
			clazz.addInterface("MultilingualObject");
		}
		
		clazz.setName(getDocumentName(doc));
		clazz.setParent("AbstractASGDocument");
		
		startClassBody();
		
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
		
		if (DataFacadeGenerator.hasLanguageCopyMethods(doc)){
			generateLanguageCopyMethods(doc);
			emptyline();
		}

		generateMultilingualSwitchSupport(doc);

		emptyline();
		generateEqualsMethod(doc);
		
		//emptyline();
		//generateCopyMethod(doc);
		
		return clazz;
	}
	
	private void generateLanguageCopyMethods(MetaDocument doc){
		
		Context context = GeneratorDataRegistry.getInstance().getContext(); 
		
		//first the common method lang2lang
		appendString("public void "+DataFacadeGenerator.getCopyMethodName()+"(String sourceLanguage, String destLanguage){");
		increaseIdent();
		for (String srclang : context.getLanguages()){
			for (String targetlang : context.getLanguages()){
				if (!srclang.equals(targetlang)){
					appendString("if (sourceLanguage.equals("+quote(srclang)+") && destLanguage.equals("+quote(targetlang)+"))");
					appendIncreasedStatement(DataFacadeGenerator.getCopyMethodName(srclang, targetlang)+"()");
				}
			}
		}
		
		closeBlockNEW();
		emptyline();
		
		
		//now the concrete methods
		for (String srclang : context.getLanguages()){
			for (String targetlang : context.getLanguages()){
				if (!srclang.equals(targetlang)){
					appendComment("Copies all multilingual properties from language "+srclang+" to language "+targetlang);
					appendString("public void "+DataFacadeGenerator.getCopyMethodName(srclang, targetlang)+"(){");
					increaseIdent();
					for (MetaProperty p : doc.getProperties()){
						if (p.isMultilingual()){
							String copyCall = p.toSetter(targetlang)+"(";
							copyCall += p.toGetter(srclang)+"()";
							copyCall += ")";
							appendStatement(copyCall);
						}
					}
					closeBlockNEW();
					emptyline();
				}
			}
		}
		
	}
	
	
	private String generateDefaultConstructor(MetaDocument doc){
		String ret = "";
		appendString("public "+getDocumentName(doc)+"(String id){");
		increaseIdent();
		appendStatement("super(id)");
		closeBlockNEW();
		return ret;
	}
	
	private String generateCloneConstructor(MetaDocument doc){
		String ret = "";
		appendString("public "+getDocumentName(doc)+"("+getDocumentName(doc)+" toClone){");
		increaseIdent();
		appendStatement("super(toClone)");
		closeBlockNEW();
		return ret;
	}

	private void generateBuilderConstructor(MetaDocument doc){
		appendString(getDocumentImplName(doc)+"("+getDocumentBuilderName(doc)+" builder){");
		increaseIdent();
		appendStatement("super("+quote("")+")");
		for (MetaProperty p : doc.getProperties()){
			appendStatement("set", p.getAccesserName(), "(builder.", p.getName(), ")");
		}
		
		for (MetaProperty p : doc.getLinks()){
			appendStatement("set", p.getAccesserName(), "(builder.", p.getName(), ")");
		}

		closeBlockNEW();
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
			emptyline();
			ret += generatePropertySetterMethod(p);
			emptyline();
		}
		return ret;
	}
	
	private String generatePropertyGetterMethod(MetaProperty p){
		String ret = "";
		
		if (p instanceof MetaTableProperty)
			return generateTablePropertyGetterMethods((MetaTableProperty)p);
		if (p instanceof MetaListProperty)
			return generateListPropertyGetterMethods((MetaListProperty)p);
		if (GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported() && p.isMultilingual())
			return generatePropertyGetterMethodMultilingual(p);
		
		appendString("public "+p.toJavaType()+" get"+p.getAccesserName()+"(){");
		increaseIdent();
		if(p instanceof MetaGenericProperty)
			appendStatement("return "+((MetaGenericProperty)p).toPropertyGetterCall());
		else
			appendStatement("return "+p.toPropertyGetter()+"("+p.toNameConstant()+")");
		closeBlockNEW();
		return ret;
	}
	
	private String generatePropertyGetterMethodMultilingual(MetaProperty p){
		String ret = "";
		for (String l : GeneratorDataRegistry.getInstance().getContext().getLanguages()){
			appendString("public "+p.toJavaType()+" get"+p.getAccesserName(l)+"(){");
			increaseIdent();
			if (p instanceof MetaGenericProperty)
				appendStatement("return "+((MetaGenericProperty)p).toPropertyGetterCall(l));
			else
				appendStatement("return "+p.toPropertyGetter()+"("+p.toNameConstant(l)+")");
			closeBlockNEW();
			emptyline();
		}
		appendString("public "+p.toJavaType()+" get"+p.getAccesserName()+"(){");
		increaseIdent();
		String v = "(isMultilingualDisabledInstance() ? ContextManager.getCallContext().getDefaultLanguage() : ContextManager.getCallContext().getCurrentLanguage())";
		if(p instanceof MetaGenericProperty)
			appendStatement("return "+((MetaGenericProperty)p).toPropertyGetterCallForCurrentLanguage(v));
		else
			appendStatement("return "+p.toPropertyGetter()+"("+quote(p.getName()+"_")+"+"+v+")");
		closeBlockNEW();
		emptyline();
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
		if (GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported() && p.isMultilingual())
			return generatePropertySetterMethodMultilingual(p);

		appendString("public void set"+p.getAccesserName()+"("+p.toJavaType()+" value){");
		increaseIdent();
		if(p instanceof MetaGenericProperty)
			appendStatement(""+((MetaGenericProperty)p).toPropertySetterCall());
		else
			appendStatement(""+p.toPropertySetter()+"("+p.toNameConstant()+", value)");
		closeBlockNEW();
		return ret;
	}
	
	private String generatePropertySetterMethodMultilingual(MetaProperty p){
		String ret = "";
		for (String l : GeneratorDataRegistry.getInstance().getContext().getLanguages()){
			appendString("public void set"+p.getAccesserName(l)+"("+p.toJavaType()+" value){");
			increaseIdent();
			if(p instanceof MetaGenericProperty)
				appendStatement(""+((MetaGenericProperty)p).toPropertySetterCall(l));
			else
				appendStatement(""+p.toPropertySetter()+"("+p.toNameConstant(l)+", value)");
			closeBlockNEW();
			emptyline();
		}
		appendString("public void set"+p.getAccesserName()+"("+p.toJavaType()+" value){");
		increaseIdent();
		String v = "(isMultilingualDisabledInstance() ? ContextManager.getCallContext().getDefaultLanguage() : ContextManager.getCallContext().getCurrentLanguage())";
		if(p instanceof MetaGenericProperty)
			appendStatement(""+((MetaGenericProperty)p).toPropertySetterCallForCurrentLanguage(v));
		else
			appendStatement(""+p.toPropertySetter()+"("+"("+quote(p.getName()+"_")+"+"+v+")"+", value)");
		closeBlockNEW();
		emptyline();
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
		return ret;
	}
	
	public static final String getDocumentImport(Context context, MetaDocument doc){
		return context.getDataPackageName(doc)+"."+getDocumentImplName(doc);
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
		
		if (container.isMultilingual()){
			generateContainerMethodsMultilingual(container);
			return;
		}
		
		appendString("public int "+getContainerSizeGetterName(container)+"(){");
		increaseIdent();
		MetaProperty pr = container instanceof MetaTableProperty ? 
			(MetaProperty) ((MetaTableProperty)container).getColumns().get(0) :
			container;
		appendStatement("return getList("+pr.toNameConstant()+").size()"); 
		closeBlockNEW();
		emptyline();
		
		
	}
	
	private void generateContainerMethodsMultilingual(MetaContainerProperty container){
		for (String l : GeneratorDataRegistry.getInstance().getContext().getLanguages()){
			appendString("public int "+getContainerSizeGetterName(container, l)+"(){");
			increaseIdent();
			MetaProperty pr = container instanceof MetaTableProperty ? 
					(MetaProperty) ((MetaTableProperty)container).getColumns().get(0) :
						container;
					appendStatement("return getList("+pr.toNameConstant(l)+").size()"); 
					closeBlockNEW();
					emptyline();
		}
		
		appendString("public int "+getContainerSizeGetterName(container)+"(){");
		increaseIdent();
		appendStatement("return getList("+quote(container.getName()+"_")+"+"+GET_CURRENT_LANG+").size()"); 
		closeBlockNEW();
		emptyline();
//		appendStatement("return "+p.toPropertyGetter()+"("+quote(p.getName()+"_")+"+"+v+")");

		
	}

	private void generateListMethods(MetaListProperty list){
		
		if (list.isMultilingual()){
			generateListMethodsMultilingual(list);
			return;
		}

		MetaProperty c = list.getContainedProperty();
		String accesserType = StringUtils.capitalize(c.toJavaType()); 

		String decl = "public void "+getContainerEntryAdderName(list)+"(";
		decl += c.toJavaType()+" "+c.getName();
		decl += "){";
		appendString(decl);
		increaseIdent();
		
		
//		appendStatement("getListPropertyAnyCase("+list.toNameConstant()+").add(new "+c.toJavaType()+"Property("+c.getName()+", "+c.getName()+"))");
		if (c instanceof MetaEnumerationProperty) 
			openFun("if (!getListPropertyAnyCase(" + list.toNameConstant() + ").getList().contains(new " + accesserType + "Property(" + quote("") + " + " + c.getName() + ", " + c.getName() + ")))");
		appendStatement("getListPropertyAnyCase("+list.toNameConstant()+").add(new "+accesserType+"Property("+quote("")+" + "+c.getName()+", "+c.getName()+"))");
		if (c instanceof MetaEnumerationProperty)
			closeBlock("if");
		closeBlock("method");
		emptyline();
		
		
		appendString("public void "+getContainerEntryDeleterName(list)+"(int index){");
		increaseIdent();
		appendStatement("getListProperty("+list.toNameConstant()+").remove(index)");
		closeBlock("method");
		emptyline();
		
		appendString("public void "+getContainerEntrySwapperName(list)+"(int index1, int index2){");
		increaseIdent();
		appendStatement(c.toJavaType()+" tmp1, tmp2");
//		appendStatement("tmp1 = (("+c.toJavaType()+"Property"+")getList("+list.toNameConstant()+").get(index1)).get"+c.toJavaType()+"()");
//		appendStatement("tmp2 = (("+c.toJavaType()+"Property"+")getList("+list.toNameConstant()+").get(index2)).get"+c.toJavaType()+"()");
//		appendStatement("(("+c.toJavaType()+"Property"+")getList("+list.toNameConstant()+").get(index1)).set"+c.toJavaType()+"(tmp2)");
//		appendStatement("(("+c.toJavaType()+"Property"+")getList("+list.toNameConstant()+").get(index2)).set"+c.toJavaType()+"(tmp1)");
		appendStatement("tmp1 = (("+accesserType+"Property"+")getList("+list.toNameConstant()+").get(index1)).get"+accesserType+"()");
		appendStatement("tmp2 = (("+accesserType+"Property"+")getList("+list.toNameConstant()+").get(index2)).get"+accesserType+"()");
		appendStatement("(("+accesserType+"Property"+")getList("+list.toNameConstant()+").get(index1)).set"+accesserType+"(tmp2)");
		appendStatement("(("+accesserType+"Property"+")getList("+list.toNameConstant()+").get(index2)).set"+accesserType+"(tmp1)");
		closeBlock("method");
		emptyline();

		appendString("public "+c.toJavaType()+ " "+getListElementGetterName(list)+"(int index){");
		increaseIdent();
//		appendStatement(c.toJavaType()+"Property p = ("+c.toJavaType()+"Property"+")getList("+list.toNameConstant()+").get(index)");
//		appendStatement("return p.get"+c.toJavaType()+"()");
		appendStatement(accesserType+"Property p = ("+accesserType+"Property"+")getList("+list.toNameConstant()+").get(index)");
		appendStatement("return p.get"+accesserType+"()");
		closeBlock("method");
		emptyline();

	}
	
	private void generateListMethodsMultilingual(MetaListProperty list){

		MetaProperty c = list.getContainedProperty();
		String accesserType = StringUtils.capitalize(c.toJavaType());

		for (String l : GeneratorDataRegistry.getInstance().getContext().getLanguages()){
			String decl = "public void "+getContainerEntryAdderName(list, l)+"(";
			decl += c.toJavaType()+" "+c.getName();
			decl += "){";
			appendString(decl);
			increaseIdent();
			
//			appendStatement("getListPropertyAnyCase("+list.toNameConstant(l)+").add(new "+c.toJavaType()+"Property("+c.getName()+", "+c.getName()+"))");
			appendStatement("getListPropertyAnyCase("+list.toNameConstant(l)+").add(new "+accesserType+"Property("+quote("")+" + "+c.getName()+", "+c.getName()+"))");
			closeBlockNEW();
			emptyline();
			
			
			appendString("public void "+getContainerEntryDeleterName(list, l)+"(int index){");
			increaseIdent();
			appendStatement("getListProperty("+list.toNameConstant(l)+").remove(index)"); 
			closeBlockNEW();
			emptyline();
			
			appendString("public void "+getContainerEntrySwapperName(list, l)+"(int index1, int index2){");
			increaseIdent();
			appendStatement(c.toJavaType()+" tmp1, tmp2");
//			appendStatement("tmp1 = (("+c.toJavaType()+"Property"+")getList("+list.toNameConstant(l)+").get(index1)).get"+c.toJavaType()+"()");
//			appendStatement("tmp2 = (("+c.toJavaType()+"Property"+")getList("+list.toNameConstant(l)+").get(index2)).get"+c.toJavaType()+"()");
//			appendStatement("(("+c.toJavaType()+"Property"+")getList("+list.toNameConstant(l)+").get(index1)).set"+c.toJavaType()+"(tmp2)");
//			appendStatement("(("+c.toJavaType()+"Property"+")getList("+list.toNameConstant(l)+").get(index2)).set"+c.toJavaType()+"(tmp1)");
			appendStatement("tmp1 = (("+accesserType+"Property"+")getList("+list.toNameConstant(l)+").get(index1)).get"+accesserType+"()");
			appendStatement("tmp2 = (("+accesserType+"Property"+")getList("+list.toNameConstant(l)+").get(index2)).get"+accesserType+"()");
			appendStatement("(("+accesserType+"Property"+")getList("+list.toNameConstant(l)+").get(index1)).set"+accesserType+"(tmp2)");
			appendStatement("(("+accesserType+"Property"+")getList("+list.toNameConstant(l)+").get(index2)).set"+accesserType+"(tmp1)");
			closeBlockNEW();
			emptyline();
	
			appendString("public "+c.toJavaType()+ " "+getListElementGetterName(list, l)+"(int index){");
			increaseIdent();
//			appendStatement(c.toJavaType()+"Property p = ("+c.toJavaType()+"Property"+")getList("+list.toNameConstant(l)+").get(index)");
//			appendStatement("return p.get"+c.toJavaType()+"()");
			appendStatement(accesserType+"Property p = ("+accesserType+"Property"+")getList("+list.toNameConstant(l)+").get(index)");
			appendStatement("return p.get"+accesserType+"()");
			closeBlockNEW();
			emptyline();
		}
		
//		quote(container.getName()+"_")+"+"+GET_CURRENT_LANG+
		String decl = "public void "+getContainerEntryAdderName(list )+"(";
		decl += c.toJavaType()+" "+c.getName();
		decl += "){";
		appendString(decl);
		increaseIdent();
		
//		appendStatement("getListPropertyAnyCase("+quote(list.getName()+"_")+"+"+GET_CURRENT_LANG+").add(new "+c.toJavaType()+"Property("+c.getName()+", "+c.getName()+"))");
		appendStatement("getListPropertyAnyCase("+quote(list.getName()+"_")+"+"+GET_CURRENT_LANG+").add(new "+accesserType+"Property("+quote("")+" + "+c.getName()+", "+c.getName()+"))");
		closeBlockNEW();
		emptyline();
		
		
		appendString("public void "+getContainerEntryDeleterName(list)+"(int index){");
		increaseIdent();
		appendStatement("getListProperty("+quote(list.getName()+"_")+"+"+GET_CURRENT_LANG+").remove(index)"); 
		closeBlockNEW();
		emptyline();
		
		appendString("public void "+getContainerEntrySwapperName(list)+"(int index1, int index2){");
		increaseIdent();
		appendStatement(c.toJavaType()+" tmp1, tmp2");
//		appendStatement("tmp1 = (("+c.toJavaType()+"Property"+")getList("+quote(list.getName()+"_")+"+"+GET_CURRENT_LANG+").get(index1)).get"+c.toJavaType()+"()");
//		appendStatement("tmp2 = (("+c.toJavaType()+"Property"+")getList("+quote(list.getName()+"_")+"+"+GET_CURRENT_LANG+").get(index2)).get"+c.toJavaType()+"()");
//		appendStatement("(("+c.toJavaType()+"Property"+")getList("+quote(list.getName()+"_")+"+"+GET_CURRENT_LANG+").get(index1)).set"+c.toJavaType()+"(tmp2)");
//		appendStatement("(("+c.toJavaType()+"Property"+")getList("+quote(list.getName()+"_")+"+"+GET_CURRENT_LANG+").get(index2)).set"+c.toJavaType()+"(tmp1)");
		appendStatement("tmp1 = (("+accesserType+"Property"+")getList("+quote(list.getName()+"_")+"+"+GET_CURRENT_LANG+").get(index1)).get"+accesserType+"()");
		appendStatement("tmp2 = (("+accesserType+"Property"+")getList("+quote(list.getName()+"_")+"+"+GET_CURRENT_LANG+").get(index2)).get"+accesserType+"()");
		appendStatement("(("+accesserType+"Property"+")getList("+quote(list.getName()+"_")+"+"+GET_CURRENT_LANG+").get(index1)).set"+accesserType+"(tmp2)");
		appendStatement("(("+accesserType+"Property"+")getList("+quote(list.getName()+"_")+"+"+GET_CURRENT_LANG+").get(index2)).set"+accesserType+"(tmp1)");

		closeBlockNEW();
		emptyline();

		appendString("public "+c.toJavaType()+ " "+getListElementGetterName(list)+"(int index){");
		increaseIdent();
//		appendStatement(c.toJavaType()+"Property p = ("+c.toJavaType()+"Property"+")getList("+quote(list.getName()+"_")+"+"+GET_CURRENT_LANG+").get(index)");
//		appendStatement("return p.get"+c.toJavaType()+"()");
		appendStatement(accesserType+"Property p = ("+accesserType+"Property"+")getList("+quote(list.getName()+"_")+"+"+GET_CURRENT_LANG+").get(index)");
		appendStatement("return p.get"+accesserType+"()");
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
		
		appendString("public List<String> get"+StringUtils.capitalize(table.getName())+"Row(int index){");
		increaseIdent();
		appendStatement("List<String> ret = new ArrayList<String>(1)");
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

		appendString("public List<List<String>> "+getTableGetterName(table)+"(){");
		increaseIdent();
		appendStatement("int size = "+getContainerSizeGetterName(table)+"();");
		appendStatement("List<List<String>> ret = new java.util.ArrayList<List<String>>(size)");
		appendString("for (int i=0; i<size; i++)");
		appendIncreasedStatement("ret.add(get"+StringUtils.capitalize(table.getName())+"Row(i))");
		appendStatement("return ret");
		closeBlockNEW();
		emptyline();
	}
	
/*	
	private void generateCompareMethod(MetaDocument doc){
		appendString("public int compareTo(IComparable anotherComparable, int method){");
		increaseIdent();

		appendStatement(getDocumentName(doc)+" anotherDoc = ("+getDocumentName(doc)+") anotherComparable");
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
		closeBlockNEW();

		closeBlockNEW();
	}
	*/

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

	protected void generateMultilingualSwitchSupport(MetaDocument doc){
		if (!doc.isMultilingual())
			return ;
			
		appendString("public boolean isMultilingualDisabledInstance(){");
		increaseIdent();
		appendString("try{");
		increaseIdent();
		appendStatement("return ((BooleanProperty)getInternalProperty(INT_PROPERTY_MULTILINGUAL_DISABLED)).getboolean()");
		decreaseIdent();
		appendString("}catch(NoSuchPropertyException e){");
		appendIncreasedString("return false;");
		appendString("}");
		closeBlockNEW();

		emptyline();
		
		appendString("public void setMultilingualDisabledInstance(boolean value){");
		increaseIdent();
		appendStatement("setInternalProperty(new BooleanProperty(INT_PROPERTY_MULTILINGUAL_DISABLED, value))");
		closeBlockNEW();
	}

	/* This method is yet not finished, multilinguality should be added ..
	private void generateCopyMethod(MetaDocument doc){
		appendString("public void copyAttributesFrom("+doc.getName()+" toCopy){");
		increaseIdent();
		for (MetaProperty p : doc.getProperties()){
			appendStatement(p.toSetter() + "(toCopy."+p.toGetter()+"())");
		}
		for (MetaProperty p : doc.getLinks()){
			appendStatement(p.toSetter() + "(toCopy."+p.toGetter()+"())");
		}
		closeBlockNEW();
	}*/




}
