package net.anotheria.asg.generator.model;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaContainerProperty;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.MetaProperty;
import net.anotheria.util.StringUtils;
/**
 * Base class for the data objects generators (documents, VOs and more).
 * @author lrosenberg
 *
 */
public abstract class AbstractDataObjectGenerator extends AbstractGenerator{
	
	/**
	 * Returns the implementation name of this abstract data object flavour.
	 * @param doc
	 * @return
	 */
	protected abstract String getDataObjectImplName(MetaDocument doc);
	/**
	 * Returns the package name for the given document.
	 * @param doc
	 * @return
	 */
	protected String getPackageName(MetaDocument doc){
		return GeneratorDataRegistry.getInstance().getContext().getDataPackageName(doc);
	}
	
	public static String getPackageName(MetaModule module){
		return GeneratorDataRegistry.getInstance().getContext().getPackageName(module);
	}
	
	
	/**
	 * Generates getFootprint method. 
	 * @param doc 
	 * @return
	 */
	protected void generateGetFootprintMethod(MetaDocument doc){
		appendString("public String getFootprint(){");
		increaseIdent();
		appendStatement("StringBuilder footprint = new StringBuilder()");

		generatePropertyListFootprint(doc.getProperties());
		generatePropertyListFootprint(doc.getLinks());
		
		appendStatement("return MD5Util.getMD5Hash(footprint)");
		append(closeBlock());
	}
	
	protected void generatePropertyListFootprint(List<MetaProperty> properties){
		Context c = GeneratorDataRegistry.getInstance().getContext();

		for (MetaProperty p : properties){
			if (c.areLanguagesSupported() && p.isMultilingual()){
				for (String l : c.getLanguages())
					appendStatement("footprint.append(get"+p.getAccesserName(l)+"())");
			}else{
				appendStatement("footprint.append(get"+p.getAccesserName()+"())");
			}
		}
	}

	protected void generateCompareMethod(MetaDocument doc){
		appendString("public int compareTo("+doc.getName()+" comparable){");
		appendIncreasedStatement("return compareTo(comparable, "+getSortTypeName(doc)+".SORT_BY_DEFAULT)");
		appendString("}");
		emptyline();

		appendString("public int compareTo(IComparable anotherComparable, int method){");
		increaseIdent();

		appendStatement(getDataObjectImplName(doc)+" anotherDoc = ("+getDataObjectImplName(doc)+") anotherComparable");
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

	protected List<MetaProperty> extractSortableProperties(MetaDocument doc){
		List<MetaProperty> properties = new ArrayList<MetaProperty>();
		properties.add(new MetaProperty("id",MetaProperty.Type.STRING));
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

	public static String getSortTypeName(MetaDocument doc){
		return doc.getName()+"SortType";
	}
	
	public static String getSortTypeImport(MetaDocument doc){
		return GeneratorDataRegistry.getInstance().getContext().getDataPackageName(doc)+"."+getSortTypeName(doc);
	}
	
	protected final void generateDefNameMethod(MetaDocument doc){
		appendString("public String getDefinedName(){");
		increaseIdent();
		appendStatement("return "+quote(doc.getName()));
		append(closeBlock());
	}
	
	protected final void generateDefParentNameMethod(MetaDocument doc){
		appendString("public String getDefinedParentName(){");
		increaseIdent();
		appendStatement("return "+quote(doc.getParentModule().getName()));
		append(closeBlock());
	}
	
	/**
	 * Returns the name for the builder for this document.
	 * @param doc
	 * @return
	 */
	public String getDocumentBuilderName(MetaDocument doc){
		return doc.getName()+"Builder";
	}

	protected GeneratedClass generateDocumentFactory(MetaDocument doc){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(getPackageName(doc));
		clazz.setName(getDocumentFactoryName(doc));
		
		startClassBody();
		appendString("public static "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" template){");
		increaseIdent();
		appendStatement("return new "+getDataObjectImplName(doc)+"(("+getDataObjectImplName(doc)+")"+"template)");
		append(closeBlock());

		emptyline();

		appendString("public static "+doc.getName()+" create"+doc.getName()+"(){");
		increaseIdent();
		appendStatement("return new "+getDataObjectImplName(doc)+"(\"\")");
		append(closeBlock());

		emptyline();

		appendString("static "+doc.getName()+" create"+doc.getName()+"("+getDocumentBuilderName(doc)+" builder){");
		increaseIdent();
		appendStatement("return new "+getDataObjectImplName(doc)+"(builder)");
		append(closeBlock());

		emptyline();

		appendString("public static "+doc.getName()+" create"+doc.getName()+"ForImport(String anId){");
		increaseIdent();
		appendStatement("return new "+getDataObjectImplName(doc)+"(anId)");
		append(closeBlock());

		emptyline();

		appendComment("For internal use only!");
		appendString("public static "+doc.getName()+" create"+doc.getName()+"(String anId){");
		increaseIdent();
		appendStatement("return new "+getDataObjectImplName(doc)+"(anId)");
		append(closeBlock());
		
		return clazz;
	}

	private String getDocumentFactoryName(MetaDocument doc){
		return DataFacadeGenerator.getDocumentFactoryName(doc);
	}
	
	protected void generateEqualsMethod(MetaDocument doc){
		appendString("public boolean equals(Object o){");
		increaseIdent();
		appendStatement("return o == this || ((o instanceof "+getDataObjectImplName(doc)+") && (("+getDataObjectImplName(doc)+")o).getId().equals(getId()))");
		append(closeBlock());
	}



}
