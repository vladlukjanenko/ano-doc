package net.anotheria.asg.generator.view.action;

import net.anotheria.asg.generator.*;
import net.anotheria.asg.generator.forms.meta.MetaForm;
import net.anotheria.asg.generator.meta.*;
import net.anotheria.asg.generator.meta.MetaProperty.Type;
import net.anotheria.asg.generator.view.meta.*;
import net.anotheria.util.ExecutionTimer;
import net.anotheria.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Please remain lrosenberg to comment BeanGenerator.java
 * @author lrosenberg
 * @created on Feb 25, 2005
 */
public class ModuleBeanGenerator extends AbstractGenerator implements IGenerator {
	
	/**
	 * Implementation is moved into ano-web, the constant remains.
	 */
	public static final String FLAG_FORM_SUBMITTED = "formSubmittedFlag";
	
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
		
		timer.startExecution(section.getModule().getName()+"-"+section.getTitle()+"-ListItem");
		files.add(new FileEntry(generateListItemBean(section)));
		timer.stopExecution(section.getModule().getName()+"-"+section.getTitle()+"-ListItem");
		files.add(new FileEntry(generateListItemSortType(section)));

		List<MetaDialog> dialogs = section.getDialogs();
		for (int i=0; i<dialogs.size(); i++){
			MetaDialog dlg = dialogs.get(i);
			files.add(new FileEntry(generateDialogForm(dlg, section.getDocument())));
			
			MetaDocument doc = section.getDocument();
			for (int p=0; p<doc.getProperties().size(); p++){
				MetaProperty pp = doc.getProperties().get(p);
				if (pp instanceof MetaContainerProperty){
					files.add(new FileEntry(generateContainerEntryForm(doc, (MetaContainerProperty)pp)));
					files.add(new FileEntry(generateContainerQuickAddForm(doc, (MetaContainerProperty)pp)));
				}
			}
		}
	//	files.add(new FileEntry(FileEntry.package2path(getPackage()), getShowActionName(section), generateShowAction(section)));
		//files.add(new FileEntry(FileEntry.package2path(getPackage()), getDeleteActionName(section), generateDeleteAction(section)));
		
		timer.stopExecution("All");
//		timer.printExecutionTimesOrderedByCreation();
		
		
		
		return files;
	}
	
	private GeneratedClass generateContainerEntryForm(MetaDocument doc, MetaContainerProperty p){
		if (p instanceof MetaTableProperty){
			return generateTableRowForm(doc, (MetaTableProperty)p);
		}
		if (p instanceof MetaListProperty){
			return generateListElementForm(doc, (MetaListProperty)p);
		}
		throw new RuntimeException("Unsupported container type: "+p);
	}
	
	private GeneratedClass generateContainerQuickAddForm(MetaDocument doc, MetaContainerProperty p){
		if (p instanceof MetaListProperty)
			return generateListQuickAddForm(doc, (MetaListProperty)p);
		System.out.println("WARN Unsupported container type: "+p);
		return null;
	}
	
	private GeneratedClass generateListQuickAddForm(MetaDocument doc, MetaListProperty list){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(getPackage(doc));
		
		clazz.addImport("net.anotheria.maf.bean.FormBean");
		
		clazz.setName(getContainerQuickAddFormName(list));
		clazz.addInterface("FormBean");

		startClassBody();
		appendGenerationPoint("generateListQuickAddForm");
		
		appendStatement("private String quickAddIds");
		appendStatement("private String ownerId");
		
		emptyline();
		appendString("public void setQuickAddIds(String someIds){");
		appendIncreasedStatement("quickAddIds = someIds");
		appendString("}");
		emptyline();
		appendString("public String getQuickAddIds(){");
		appendIncreasedStatement("return quickAddIds");
		appendString("}");
		
		emptyline();
		
		emptyline();
		appendString("public void setOwnerId(String anId){");
		appendIncreasedStatement("ownerId = anId");
		appendString("}");
		emptyline();
		appendString("public String getOwnerId(){");
		appendIncreasedStatement("return ownerId");
		appendString("}");
		emptyline();

		return clazz;
	}
	
	private GeneratedClass generateListElementForm(MetaDocument doc, MetaListProperty list){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(getPackage(doc));
		clazz.addImport("net.anotheria.maf.bean.FormBean");
		
		if (list.getContainedProperty().isLinked()  || list.getContainedProperty() instanceof MetaEnumerationProperty){
			clazz.addImport("java.util.List");
		}
		
		List<MetaProperty> elements = new ArrayList<MetaProperty>();
		elements.add(new MetaProperty("ownerId",MetaProperty.Type.STRING));
		elements.add(new MetaProperty("position",MetaProperty.Type.INT));
		elements.add(list.getContainedProperty());
		elements.add(new MetaProperty("description",MetaProperty.Type.STRING));

		clazz.setName(getContainerEntryFormName(list));
		clazz.addInterface("FormBean");
		startClassBody();
		appendGenerationPoint("generateListElementForm");
		
		for (int i=0; i<elements.size(); i++){
			MetaProperty p = elements.get(i);
			appendStatement("private "+p.toJavaType()+" "+p.getName());
			if (p.isLinked() || p instanceof MetaEnumerationProperty){
				MetaProperty collection = new MetaProperty(p.getName()+"Collection",MetaProperty.Type.LIST);
				appendString("@SuppressWarnings(\"unchecked\")");
				appendStatement("private "+collection.toJavaType()+" "+collection.getName());
			}
		}
		
		emptyline();
		for (int i=0; i<elements.size(); i++){
			MetaProperty p = elements.get(i);
			generateMethods(null, p);
			if (p.isLinked() || p instanceof MetaEnumerationProperty){
				String propName = p.getName()+"Collection";
				appendString("@SuppressWarnings(\"unchecked\")");
				appendStatement("public void set"+StringUtils.capitalize(propName)+"(List l){");
				increaseIdent();
				appendStatement(propName+" = l");
				closeBlockNEW();
				emptyline();
				appendString("@SuppressWarnings(\"unchecked\")");
				appendStatement("public List get"+StringUtils.capitalize(propName)+"(){");
				increaseIdent();
				appendStatement("return "+propName);
				closeBlockNEW();
				emptyline();
			}
			
		}
		emptyline();
		
		return clazz;
	}
	
	@SuppressWarnings("unchecked")
	private GeneratedClass generateTableRowForm(MetaDocument doc, MetaTableProperty p){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(getPackage(doc));
		clazz.addImport("net.anotheria.webutils.bean.BaseActionForm");
		clazz.addImport("javax.servlet.http.HttpServletRequest");
		clazz.addImport("org.apache.struts.action.ActionMapping");
		
		clazz.setName(getContainerEntryFormName(p));
		clazz.setParent("BaseActionForm");

		startClassBody();
		appendGenerationPoint("generateTableRowForm");
		
		List<MetaProperty> columns = (List<MetaProperty>)((ArrayList)p.getColumns()).clone();
		columns.add(0, new MetaProperty(p.getName()+"_ownerId", MetaProperty.Type.STRING));
		columns.add(0, new MetaProperty(p.getName()+"_position", MetaProperty.Type.INT));
		for (MetaProperty pr : columns)
			appendStatement("private String "+p.extractSubName(pr));
		
		emptyline();
		for (int i=0; i<columns.size(); i++){
			MetaProperty pr = columns.get(i);
			appendString("public void set"+StringUtils.capitalize(p.extractSubName(pr))+"(String a"+StringUtils.capitalize(p.extractSubName(pr))+" ){");
			increaseIdent();
			appendStatement("this."+p.extractSubName(pr)+" = a"+StringUtils.capitalize(p.extractSubName(pr)));
			closeBlockNEW();
			appendString("public String get"+StringUtils.capitalize(p.extractSubName(pr))+"(){");
			increaseIdent();
			appendStatement("return "+p.extractSubName(pr));
			closeBlockNEW();
			emptyline();
		}

		//generate encoding.
		appendString("public void reset( ActionMapping mapping, HttpServletRequest request ){");
		increaseIdent();
		appendString("try {");
		increaseIdent();
		appendStatement("request.setCharacterEncoding( "+quote(GeneratorDataRegistry.getInstance().getContext().getEncoding())+")");
		closeBlockNEW();
		appendString("catch ( java.io.UnsupportedEncodingException e ) {}");
		closeBlockNEW();
		
		return clazz;
	}
	
	
	public GeneratedClass generateDialogForm(MetaDialog dialog, MetaDocument doc){

		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(getPackage(doc));
		clazz.addImport("net.anotheria.maf.bean.FormBean");
		
		startClassBody();
		appendGenerationPoint("generateDialogForm");
		
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
		appendGenerationPoint("generateDialogForm");
	
		for (MetaViewElement element : elements){
			if (element instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)element;
				String lang = getElementLanguage(field);
				
				MetaProperty p = doc.getField(field.getName());
				MetaProperty tmp = p instanceof MetaListProperty? new MetaProperty(p.getName(),MetaProperty.Type.INT): p;
				if (element.isValidated()) {	//TODO what about list validation?
					for (MetaValidator validator : element.getValidators()){
						clazz.addImport(validator.getClassName());
						String key = StringUtils.isEmpty(validator.getKey())? "" : validator.getKey();
						String message = StringUtils.isEmpty(validator.getDefaultError())? "" : validator.getDefaultError();
						if(validator.isCustomValidator()) {
							clazz.addImport("net.anotheria.maf.validation.annotations.ValidateCustom");
							appendString("@ValidateCustom(validator=" + validator.getClassNameOnly()+".class, key=\""+key+"\", message=\""+message+"\")");
						} else if (validator.isNumericValidator()){
							boolean fractional = p.getType() == Type.FLOAT || p.getType() == Type.DOUBLE;
							appendString("@"+validator.getClassNameOnly()+"(key=\""+key+"\", message=\""+message+"\", fractional="+fractional+")");
						} else {
							appendString("@"+validator.getClassNameOnly()+"(key=\""+key+"\", message=\""+message+"\")");
						}
					}
				}
				appendStatement("private "+tmp.toJavaType()+" "+tmp.getName(lang));
				if (p.isLinked()){
					MetaProperty collection = new MetaProperty(p.getName()+"Collection"+(lang==null?"":lang),MetaProperty.Type.LIST);
					appendStatement("private "+collection.toJavaType()+"<LabelValueBean> "+collection.getName());//hacky
					appendStatement("private String "+p.getName()+"CurrentValue"+(lang==null?"":lang));

					appendStatement("private String "+p.getName()+"IdOfCurrentValue"+(lang==null?"":lang));
				}
				
				if (p instanceof MetaEnumerationProperty){
					MetaProperty collection = new MetaProperty(p.getName()+"Collection",MetaProperty.Type.LIST);
					appendStatement("private "+collection.toJavaType()+"<LabelValueBean> "+collection.getName());//hacky
					appendStatement("private String "+p.getName()+"CurrentValue");

					appendStatement("private String "+p.getName()+"IdOfCurrentValue");
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
        generateAdditionalFields(doc,"locked", MetaProperty.Type.BOOLEAN,"LockableObject \"locked\" property. For object Locking.");
        generateAdditionalFields(doc,"lockerId", MetaProperty.Type.STRING,"LockableObject \"lockerId\" property. For userName containing.");
        generateAdditionalFields(doc,"lockingTime", MetaProperty.Type.STRING,"LockableObject \"lockingTime\" property.");
        
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
			MetaFieldElement pIdOfCurr = new MetaFieldElement(element.getName()+"IdOfCurrentValue");
			//;
			if (p.isMultilingual()){
				String l = getElementLanguage(element);
				generateMethods(new MultilingualFieldElement(l, pColl), new MetaListProperty(element.getName()+"Collection", new MetaProperty("temp", new ObjectType("LabelValueBean"))));
				generateMethods(new MultilingualFieldElement(l, pCurr), new MetaProperty(element.getName()+"CurrentValue", MetaProperty.Type.STRING));
				generateMethods(new MultilingualFieldElement(l, pIdOfCurr), new MetaProperty(element.getName()+"IdOfCurrentValue", MetaProperty.Type.STRING));
			}else{
				generateMethods(pColl, new MetaListProperty(element.getName()+"Collection", new MetaProperty("temp", new ObjectType("LabelValueBean"))));
				generateMethods(pCurr, new MetaProperty(element.getName()+"CurrentValue", MetaProperty.Type.STRING));
				generateMethods(pIdOfCurr, new MetaProperty(element.getName()+"IdOfCurrentValue", MetaProperty.Type.STRING));
			}
			
		}
		MetaProperty tmp = p instanceof MetaListProperty? new MetaProperty(p.getName(),MetaProperty.Type.INT): p;
		generateMethods(element, tmp);
	}
	
	 /**
     * Actually allow us add fields  such Lock - etc.
     * @param doc document itself
     * @param fieldName name of field
     * @param fieldType field type
     * @param comment comment for the field
     */
    private void generateAdditionalFields(MetaDocument doc, String fieldName, MetaProperty.Type fieldType, String comment) {
        if (doc.getParentModule().getStorageType().equals(StorageType.CMS)) {
            MetaFieldElement fieldElement = new MetaFieldElement(fieldName);
            MetaProperty maField = new MetaProperty(fieldElement.getName(),fieldType);
            appendComment(comment);
            appendStatement("private " + maField.toJavaType() + " " + maField.getName());
            emptyline();
            generateMethods(fieldElement,maField);
        }
    }
    
    
    private GeneratedClass generateListItemSortType(MetaModuleSection section){
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
			
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(getPackage(section.getDocument()));
		clazz.addImport("net.anotheria.util.sorter.SortType");
		
		clazz.setName(getListItemBeanSortTypeName(section.getDocument()));
		clazz.setParent("SortType");
		
		startClassBody();
		appendGenerationPoint("generateListItemSortType");
		
		MetaViewElement defaultElem = section.getDefaultSortable();
		String defaultElemName = null;
		int lastIndex = 1;
		
		elements = createMultilingualList(elements, section.getDocument());
		
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			if (element.isComparable()){
				if (element.equals(defaultElem)){
					if (element instanceof MultilingualFieldElement){
						defaultElemName = "SORT_BY_"+section.getDocument().getField(element.getName()).getName(GeneratorDataRegistry.getInstance().getContext().getDefaultLanguage()).toUpperCase();
					}else{					
						defaultElemName = "SORT_BY_"+element.getName().toUpperCase();
					}
				}
				if (element instanceof MultilingualFieldElement){
					MetaProperty p = section.getDocument().getField(element.getName());
  					appendStatement("public static final int SORT_BY_"+p.getName(((MultilingualFieldElement)element).getLanguage()).toUpperCase()+" = "+(lastIndex++));
				}else{
					appendStatement("public static final int SORT_BY_"+element.getName().toUpperCase()+" = "+(lastIndex++));
				}

			}
		}
		
		appendStatement("public static final int SORT_BY_DEFAULT = "+defaultElemName);
		emptyline();
		appendString("public "+getListItemBeanSortTypeName(section.getDocument())+"(){");
		increaseIdent();
		appendString("super(SORT_BY_DEFAULT);");
		closeBlockNEW();
		emptyline();

		appendString("public "+getListItemBeanSortTypeName(section.getDocument())+"(int method){");
		increaseIdent();
		appendString("super(method);");
		closeBlockNEW();
		emptyline();
				
		appendString("public "+getListItemBeanSortTypeName(section.getDocument())+"(int method, boolean order){");
		increaseIdent();
		appendString("super(method, order);");
		closeBlockNEW();
		emptyline();
		
		appendString("public static int name2method(String name){");
		increaseIdent();
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			if (element.isComparable()){
				MetaProperty p = section.getDocument().getField(element.getName());
				if (element instanceof MultilingualFieldElement){
					String lang = ((MultilingualFieldElement)element).getLanguage();
					appendString("if ("+quote(p.getName(lang))+".equals(name))");
					appendIncreasedStatement("return SORT_BY_"+p.getName(lang).toUpperCase());
					
				}else{
					appendString("if ("+quote(p.getName())+".equals(name))");
					appendIncreasedStatement("return SORT_BY_"+p.getName().toUpperCase());
/*					appendString("if ("+quote(element.getName())+".equals(name))");
					appendIncreasedStatement("return SORT_BY_"+element.getName().toUpperCase());*/
				}

			}
		}
		appendStatement("throw new RuntimeException("+quote("Unknown sort type name: ")+"+name)");		
		closeBlockNEW();
		emptyline();

// 		GENERATE method2name
		appendString("public static String method2name(int method){");
		increaseIdent();
		appendString("switch (method){");
		increaseIdent();
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			if (element.isComparable()){
				MetaProperty p = section.getDocument().getField(element.getName());
				if (element instanceof MultilingualFieldElement){
					String lang = ((MultilingualFieldElement)element).getLanguage();
					appendString("case SORT_BY_"+p.getName(lang).toUpperCase()+":");
					appendIncreasedStatement("return "+quote(p.getName(lang)));
					
				}else{
					appendString("case SORT_BY_"+p.getName().toUpperCase()+":");
					appendIncreasedStatement("return "+quote(p.getName()));
/*					appendString("if ("+quote(element.getName())+".equals(name))");
					appendIncreasedStatement("return SORT_BY_"+element.getName().toUpperCase());*/
				}

			}
		}
		closeBlockNEW();
		appendStatement("throw new RuntimeException("+quote("Unknown sort type method: ")+"+method)");		
		closeBlockNEW();
		emptyline();
		
		//
		appendString("public String getMethodAndOrderCode(){");
		increaseIdent();
		appendStatement("return method2name(getSortBy())+"+quote("_")+"+(getSortOrder() ? "+quote("ASC")+":"+quote("DESC")+")");
		closeBlockNEW();

		return clazz;
	}

    private boolean debugTest = false;
    
    private GeneratedClass generateListItemBean(MetaModuleSection section){
    	
    	System.out.println("generate list item bean "+section.getDocument().getName());
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		clazz.setClazzComment("Generated by "+ModuleBeanGenerator.class);
		
		MetaDocument doc = section.getDocument();
		List<MetaViewElement> origElements = section.getElements();
		
		if (doc.getName().equals("BoxType"))
			debugTest= true;
		if (doc.getName().equals("Pagex"))
			debugTest= true;
		if (debugTest){
			System.out.println("%%% DEBUG ON %%%");
		}

		List<MetaViewElement> elements = createMultilingualList(origElements, doc);

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
		
		clazz.setPackageName(getPackage(section.getDocument()));

		boolean containsComparable = false;
		for (MetaViewElement element : elements){
			if (element.isComparable()){
				containsComparable = true;
				break; 
			}
		}
		
		
		for(MetaViewElement element: elements){
			if (!(element instanceof MetaFieldElement))
				continue;
			MetaFieldElement field = (MetaFieldElement)element;
			MetaProperty p = doc.getField(field.getName());
			if(!(p instanceof MetaListProperty))
				continue;
			clazz.addImport("java.util.List");
			break;
		}

		clazz.setName(getListItemBeanName(section.getDocument()));

        //section.getModule().
		
		startClassBody();
		appendGenerationPoint("generateListItemBean");
		
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			if (element instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)element;
				MetaProperty p = doc.getField(field.getName());
				if (p instanceof MetaEnumerationProperty)
					appendStatement("private String "+p.getName());
				else{

//					MetaProperty tmp = p instanceof MetaListProperty? new MetaProperty(p.getName(), "int"):p;
					MetaProperty tmp = p;
					if (field instanceof MultilingualFieldElement){
						if (field.getDecorator()!=null){
							appendStatement("private String "+tmp.getName(((MultilingualFieldElement)field).getLanguage()));
							appendStatement("private "+tmp.toJavaType()+" "+tmp.getName("ForSorting", ((MultilingualFieldElement)field).getLanguage()));
						}else{
							appendStatement("private "+tmp.toJavaType()+" "+tmp.getName(((MultilingualFieldElement)field).getLanguage()));
						}
					}else{
						//appendString("//p: "+p.getName()+", "+p.toJavaType()+", "+p.getClass());
						if (field.getDecorator()!=null){
							appendStatement("private String "+tmp.getName());
							
							if (tmp instanceof MetaListProperty){
								//TODO this is hotfixing the sorting type
								element.setSortingType(SortingType.CONTAINERS);
							}
							appendCommentLine("Elements sort type is "+element.getSortingType());
							if (element.getName().equals("id"))
								appendStatement("private "+element.getSortingType().getJavaType()+" "+tmp.getName()+"ForSorting");
							else
								appendStatement("private "+tmp.toJavaType()+" "+tmp.getName()+"ForSorting");
						}else{
//							appendStatement("private "+p.toJavaType()+" "+p.getName());
							appendStatement("private "+tmp.toJavaType()+" "+tmp.getName());
						}
					}
				}
			}

			if (element instanceof MetaFunctionElement){
				MetaFunctionElement function = (MetaFunctionElement)element;
				appendStatement("private String "+function.getPropertyName());
			}
		}
		emptyline();
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			if (debugTest)
				System.out.println("checking "+element.getName()+" "+element.getClass().getSimpleName());
			if (element instanceof MetaFieldElement)
				generateFieldMethods((MetaFieldElement)element, doc);
			if (element instanceof MetaFunctionElement)
				generateFunctionMethods((MetaFunctionElement)element);
			
		}
		
		

        // add fields!!!! Lock!!!
        generateAdditionalFields(doc, "locked", MetaProperty.Type.BOOLEAN, "LockableObject \"locked\" property. For object Locking.");
        generateAdditionalFields(doc, "lockerId", MetaProperty.Type.STRING, "LockableObject \"lockerId\" property. For userName containing.");
        generateAdditionalFields(doc, "lockingTime", MetaProperty.Type.STRING, "LockableObject \"lockingTime\" property.");


        if (containsComparable){
			clazz.addImport("net.anotheria.util.sorter.IComparable");
			clazz.addImport("net.anotheria.util.BasicComparable");
			
			clazz.addInterface("IComparable");
			
			emptyline();
			generateCompareMethod(doc, elements);
		}
		if (debugTest){
			System.out.println("%%% DEBUG OFF %%%");
		}
		
		return clazz;
	}

	private void generateFunctionMethods(MetaFunctionElement function){
		generateMethods(function, new MetaProperty(function.getPropertyName(), MetaProperty.Type.STRING));
	}

	private void generateCompareMethod(MetaDocument doc, List<MetaViewElement> elements){
		appendString("public int compareTo(IComparable anotherComparable, int method){");
		increaseIdent();
		appendStatement(getListItemBeanName(doc)+" anotherBean = ("+getListItemBeanName(doc)+") anotherComparable");
		appendString("switch(method){");
		increaseIdent();
		for (MetaViewElement element: elements){
			if (!element.isComparable())
				continue;
			
			MetaFieldElement field = (MetaFieldElement)element;
			MetaProperty p = doc.getField(field.getName());
			
			String lang = getElementLanguage(element);
			String caseDecl = lang != null? getListItemBeanSortTypeName(doc)+".SORT_BY_"+p.getName(lang).toUpperCase():
				getListItemBeanSortTypeName(doc)+".SORT_BY_"+p.getName().toUpperCase();
			
			appendString("case "+caseDecl+":");
			
			String type2compare = p instanceof MetaEnumerationProperty? "String": StringUtils.capitalize(p.toJavaErasedType());
			String retDecl = "return BasicComparable.compare"+type2compare;
			if (element.getName().equals("id")){
				retDecl = "return BasicComparable."+element.getSortingType().getCompareCall();
			}
			retDecl += field.getDecorator()!=null? "("+p.getName("ForSorting", lang)+", anotherBean."+p.getName("ForSorting", lang)+")" : "("+p.getName(lang)+", anotherBean."+p.getName(lang)+")";
			appendIncreasedStatement(retDecl);
		}
		appendString("default:");
		appendIncreasedStatement("throw new RuntimeException(\"Sort method \"+method+\" is not supported.\")");
		closeBlockNEW();
		closeBlockNEW();
	}

	private void generateFieldMethods(MetaFieldElement element, MetaDocument doc){
		
		MetaProperty p = doc.getField(element.getName());
		if (p instanceof MetaEnumerationProperty){
			MetaProperty tmp = new MetaProperty(p.getName(), MetaProperty.Type.STRING);
			generateMethods(element, tmp);
			return;
		}
		
		if (debugTest)
			System.out.println(element.getName()+" - "+element.getDecorator());
		
		if (element.getDecorator()!=null){
			MetaProperty tmpForSorting = (MetaProperty) p.clone();//new MetaProperty(p.getName()+"ForSorting", p.getType());
			tmpForSorting.setName(tmpForSorting.getName()+"ForSorting");
			generateMethodsForSorting(element, tmpForSorting);
			//if this field has a decorator we have to generate string methods instaed of original methods.
			p = new MetaProperty(p.getName(), MetaProperty.Type.STRING, p.isMultilingual());
		}
		
		generateMethods(element, p);
	}
    
	private void generateMethods(MetaViewElement element, MetaProperty p){
		
		if (debugTest)
			System.out.println("  GenerateMethods for "+element+" ... "+p);

		if (element instanceof MultilingualFieldElement){
			generateMethodsMultilinguage((MultilingualFieldElement)element, p);
			return;
		}

		appendString("public void "+p.toBeanSetter()+"("+p.toJavaType()+" "+p.getName()+" ){");
		increaseIdent();
		appendStatement("this."+p.getName()+" = "+p.getName());
		closeBlockNEW();		
		emptyline();
		appendString("public "+p.toJavaType()+" "+p.toBeanGetter()+"(){");
		increaseIdent();
		appendStatement("return "+p.getName());
		closeBlockNEW();
		emptyline();
		
	}
	
	private void generateMethodsForSorting(MetaViewElement element, MetaProperty p){

		if (element instanceof MultilingualFieldElement){
			generateMethodsMultilinguage((MultilingualFieldElement)element, p);
			return;
		}

		appendString("public void "+p.toBeanSetter()+"("+p.toJavaType()+" "+p.getName()+" ){");
		increaseIdent();
		appendStatement("this."+p.getName()+" = "+element.getSortingType().convertValue(p.getName()));
		closeBlockNEW();			
		emptyline();

		if (element.getName().equals("id"))
			appendString("public "+element.getSortingType().getJavaType()+" "+p.toBeanGetter()+"(){");
		else
			appendString("public "+p.toJavaType()+" "+p.toBeanGetter()+"(){");
		increaseIdent();
		appendStatement("return "+p.getName());
		closeBlockNEW();
		emptyline();
		
	}
	
	
		private void generateMethodsMultilinguage(MultilingualFieldElement element, MetaProperty p){
		
		//System.out.println("--- m "+p+", "+p.getType());
		if (p.getType() == MetaProperty.Type.LIST)
			appendString("@SuppressWarnings(\"unchecked\")");
		appendString("public void "+p.toBeanSetter(element.getLanguage())+"("+p.toJavaType()+" "+p.getName()+" ){");
		increaseIdent();
		appendStatement("this."+p.getName(element.getLanguage())+" = "+p.getName());
		closeBlockNEW();			
		emptyline();
			
		if (p.getType() == MetaProperty.Type.LIST)
			appendString("@SuppressWarnings(\"unchecked\")");
		appendString("public "+p.toJavaType()+" "+p.toBeanGetter(element.getLanguage())+"(){");
		increaseIdent();
		appendStatement("return "+p.getName(element.getLanguage()));
		closeBlockNEW();
		emptyline();
		
	}
		
		
		
		public static String getListItemBeanSortTypeImport(Context context, MetaDocument doc){
			return getPackage(context, doc)+"."+getListItemBeanSortTypeName(doc);
		}
		
		public static String getListItemBeanSortTypeName(MetaDocument doc){
			return getListItemBeanName(doc)+"SortType";
		}
		
		public static String getListItemBeanName(MetaDocument doc){
			return doc.getName()+"ListItemBean";
		}
		
		public static String getDialogBeanImport(MetaDialog dialog, MetaDocument doc){
			return getPackage(GeneratorDataRegistry.getInstance().getContext(), doc)+"."+getDialogBeanName(dialog, doc);
		}
		
		public static String getListItemBeanImport(Context context, MetaDocument doc){
			return getPackage(context, doc)+"."+getListItemBeanName(doc);
		}
		
		public static String getContainerEntryFormImport(MetaDocument doc, MetaContainerProperty p){
			return GeneratorDataRegistry.getInstance().getContext().getPackageName(doc)+".bean."+getContainerEntryFormName(p);
		}
		
		public static String getContainerEntryFormName(MetaContainerProperty p){
			return StringUtils.capitalize(p.getName())+p.getContainerEntryName()+"FB";
		}
		
		public static String getContainerQuickAddFormImport(MetaDocument doc, MetaContainerProperty p){
			return GeneratorDataRegistry.getInstance().getContext().getPackageName(doc)+".bean."+getContainerQuickAddFormName(p);
		}
		
		public static String getContainerQuickAddFormName(MetaContainerProperty p){
			return StringUtils.capitalize(p.getName())+"QuickAddFB";
		}
		
		public static String getFormBeanImport(MetaForm form){
			return getPackage()+"."+getFormBeanName(form);
		}
		
		@Deprecated
		public static String getPackage(){
		    return GeneratorDataRegistry.getInstance().getContext().getPackageName()+".bean";
		}
		
		public static String getFormBeanName(MetaForm form){
		    return StringUtils.capitalize(form.getId())+"AutoForm";
		}
}
