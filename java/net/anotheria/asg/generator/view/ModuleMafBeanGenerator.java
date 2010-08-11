package net.anotheria.asg.generator.view;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaEnumerationProperty;
import net.anotheria.asg.generator.meta.MetaListProperty;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.MetaProperty;
import net.anotheria.asg.generator.meta.ObjectType;
import net.anotheria.asg.generator.meta.StorageType;
import net.anotheria.asg.generator.view.meta.MetaDialog;
import net.anotheria.asg.generator.view.meta.MetaFieldElement;
import net.anotheria.asg.generator.view.meta.MetaModuleSection;
import net.anotheria.asg.generator.view.meta.MetaViewElement;
import net.anotheria.asg.generator.view.meta.MultilingualFieldElement;
import net.anotheria.util.ExecutionTimer;
import net.anotheria.util.StringUtils;

/**
 * TODO Please remain lrosenberg to comment BeanGenerator.java
 * @author lrosenberg
 * @created on Feb 25, 2005
 */
public class ModuleMafBeanGenerator extends AbstractGenerator implements IGenerator {
	
	public static final String FIELD_ML_DISABLED = "multilingualInstanceDisabled";
    
	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.generator.IGenerator#generate(net.anotheria.anodoc.generator.IGenerateable, net.anotheria.anodoc.generator.Context)
	 */
	public List<FileEntry> generate(IGenerateable g) {
		List<FileEntry> files = new ArrayList<FileEntry>();
		
		MetaModuleSection section = (MetaModuleSection)g;
		
		//System.out.println("Generate section: "+section);
		
		ExecutionTimer timer = new ExecutionTimer("MafBeanGenerator");
		timer.startExecution("All");

		List<MetaDialog> dialogs = section.getDialogs();
		for (int i=0; i<dialogs.size(); i++){
			MetaDialog dlg = dialogs.get(i);
			files.add(new FileEntry(generateDialogForm(dlg, section.getDocument())));
		}
	//	files.add(new FileEntry(FileEntry.package2path(getPackage()), getShowActionName(section), generateShowAction(section)));
		//files.add(new FileEntry(FileEntry.package2path(getPackage()), getDeleteActionName(section), generateDeleteAction(section)));
		
		timer.stopExecution("All");
//		timer.printExecutionTimesOrderedByCreation();
		
		
		
		return files;
	}
	
	
	public GeneratedClass generateDialogForm(MetaDialog dialog, MetaDocument doc){

		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(getPackage(doc));
		clazz.addImport("net.anotheria.maf.bean.FormBean");
		
		startClassBody();
		
		//this is only used if the multilingual support is enabled for the project AND document.
		MetaFieldElement multilingualInstanceDisabledElement = new MetaFieldElement(FIELD_ML_DISABLED);
		
		List<MetaViewElement> elements = createMultilingualList(dialog.getElements(), doc);
		
		for (MetaViewElement element : elements){
			if (element instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)element;
				MetaProperty p = doc.getField(field.getName());
				if (p.isLinked() || p instanceof MetaEnumerationProperty){
					clazz.addImport("java.util.List");
					clazz.addImport("net.anotheria.webutils.bean.LabelValueBean");
					break;
				}
			}
		}
		
		clazz.setName(getDialogBeanName(dialog, doc));
		clazz.addInterface("FormBean");
		
		startClassBody();
	
		for (MetaViewElement element : elements){
			if (element instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)element;
				String lang = getElementLanguage(field);
				
				MetaProperty p = doc.getField(field.getName());
				MetaProperty tmp = p instanceof MetaListProperty? new MetaProperty(p.getName(),"int"): p;
				appendStatement("private "+tmp.toJavaType()+" "+tmp.getName(lang));
				if (p.isLinked()){
					MetaProperty collection = new MetaProperty(p.getName()+"Collection"+(lang==null?"":lang),"list");
					appendStatement("private "+collection.toJavaType()+"<LabelValueBean> "+collection.getName());//hacky
					appendStatement("private String "+p.getName()+"CurrentValue"+(lang==null?"":lang));
				}
				
				if (p instanceof MetaEnumerationProperty){
					MetaProperty collection = new MetaProperty(p.getName()+"Collection","list");
					appendStatement("private "+collection.toJavaType()+"<LabelValueBean> "+collection.getName());//hacky
					appendStatement("private String "+p.getName()+"CurrentValue");
				}
			}
			
		}

		emptyline();
		for (MetaViewElement element : elements){
			if (element instanceof MetaFieldElement)
				generateFieldMethodsInDialog((MetaFieldElement)element, doc);
		}
		
		if (doc.isMultilingual()){
			MetaProperty mlDisProp = doc.getField(multilingualInstanceDisabledElement.getName());
			appendStatement("private "+mlDisProp.toJavaType()+" "+mlDisProp.getName());
            emptyline();
			generateFieldMethodsInDialog(multilingualInstanceDisabledElement, doc);
		}
        // add fields!!!! Lock!!!
        generateAdditionalFields(doc,"locked","boolean","LockableObject \"locked\" property. For object Locking.");
        generateAdditionalFields(doc,"lockerId","string","LockableObject \"lockerId\" property. For userName containing.");
        generateAdditionalFields(doc,"lockingTime","string","LockableObject \"lockingTime\" property.");
        

		appendString("@Override");
		appendString("public String toString() {");
		appendIncreasedStatement("return \"EditBoxFB [id=\" + id + \", name=\" + name + \", type=\" + type + \", handler=\" + handler + \"]\"");
		append(closeBlock());

        emptyline();
		
		return clazz;
	}
	
	public static String getPackage(MetaDocument doc){
	    return getPackage(GeneratorDataRegistry.getInstance().getContext(), doc);
	}
	
	public static String getPackage(MetaModule module){
	    return getPackage(GeneratorDataRegistry.getInstance().getContext(), module);
	}
	
	public static String getPackage(Context context, MetaModule module){
	    return context.getPackageName(module)+".bean";
	}
	
	public static String getPackage(Context context, MetaDocument doc){
	    return context.getPackageName(doc)+".bean";
	}
	
	public static String getDialogBeanName(MetaDialog dialog, MetaDocument document){
		return StringUtils.capitalize(dialog.getName())+StringUtils.capitalize(document.getName())+"FB";
	}
	
	private void generateFieldMethodsInDialog(MetaFieldElement element, MetaDocument doc){
		MetaProperty p = null;
//		String lang = getElementLanguage(element);
		p = doc.getField(element.getName());

		if (p.isLinked() || p instanceof MetaEnumerationProperty){
			MetaFieldElement pColl = new MetaFieldElement(element.getName()+"Collection");
			MetaFieldElement pCurr = new MetaFieldElement(element.getName()+"CurrentValue");
			//;
			if (p.isMultilingual()){
				String l = getElementLanguage(element);
				generateMethods(new MultilingualFieldElement(l, pColl), new MetaListProperty(element.getName()+"Collection", new MetaProperty("temp", new ObjectType("LabelValueBean"))));
				generateMethods(new MultilingualFieldElement(l, pCurr), new MetaProperty(element.getName()+"CurrentValue", "string"));
			}else{
				generateMethods(pColl, new MetaListProperty(element.getName()+"Collection", new MetaProperty("temp", new ObjectType("LabelValueBean"))));
				generateMethods(pCurr, new MetaProperty(element.getName()+"CurrentValue", "string"));
			}
			
		}
		MetaProperty tmp = p instanceof MetaListProperty? new MetaProperty(p.getName(),"int"): p;
		generateMethods(element, tmp);
	}
	
	 /**
     * Actually allow us add fields  such Lock - etc.
     * @param doc document itself
     * @param fieldName name of field
     * @param fieldType field type
     * @param comment comment for the field
     */
    private void generateAdditionalFields(MetaDocument doc, String fieldName, String fieldType, String comment) {
        if (doc.getParentModule().getStorageType().equals(StorageType.CMS)) {
            MetaFieldElement fieldElement = new MetaFieldElement(fieldName);
            MetaProperty maField = new MetaProperty(fieldElement.getName(),fieldType);
            appendComment(comment);
            appendStatement("private " + maField.toJavaType() + " " + maField.getName());
            emptyline();
            generateMethods(fieldElement,maField);
        }
    }
    
	private void generateMethods(MetaViewElement element, MetaProperty p){

		if (element instanceof MultilingualFieldElement){
			generateMethodsMultilinguage((MultilingualFieldElement)element, p);
			return;
		}
		
		appendString("public void "+p.toBeanSetter()+"("+p.toJavaType()+" "+p.getName()+" ){");
		increaseIdent();
		appendStatement("this."+p.getName()+" = "+p.getName());
		append(closeBlock());			
		emptyline();
			
		appendString("public "+p.toJavaType()+" "+p.toBeanGetter()+"(){");
		increaseIdent();
		appendStatement("return "+p.getName());
		append(closeBlock());
		emptyline();
		
	}
	
		private void generateMethodsMultilinguage(MultilingualFieldElement element, MetaProperty p){
		
		//System.out.println("--- m "+p+", "+p.getType());
		if (p.getType().equals("list"))
			appendString("@SuppressWarnings(\"unchecked\")");
		appendString("public void "+p.toBeanSetter(element.getLanguage())+"("+p.toJavaType()+" "+p.getName()+" ){");
		increaseIdent();
		appendStatement("this."+p.getName(element.getLanguage())+" = "+p.getName());
		append(closeBlock());			
		emptyline();
			
		if (p.getType().equals("list"))
			appendString("@SuppressWarnings(\"unchecked\")");
		appendString("public "+p.toJavaType()+" "+p.toBeanGetter(element.getLanguage())+"(){");
		increaseIdent();
		appendStatement("return "+p.getName(element.getLanguage()));
		append(closeBlock());
		emptyline();
		
	}
}
