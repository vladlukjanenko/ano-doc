package net.anotheria.asg.generator.view.action;

import net.anotheria.asg.data.LockableObject;
import net.anotheria.asg.exception.ConstantNotFoundException;
import net.anotheria.asg.generator.*;
import net.anotheria.asg.generator.forms.meta.*;
import net.anotheria.asg.generator.meta.*;
import net.anotheria.asg.generator.model.AbstractDataObjectGenerator;
import net.anotheria.asg.generator.model.DataFacadeGenerator;
import net.anotheria.asg.generator.model.ServiceGenerator;
import net.anotheria.asg.generator.types.EnumTypeGenerator;
import net.anotheria.asg.generator.types.meta.EnumerationType;
import net.anotheria.asg.generator.util.DirectLink;
import net.anotheria.asg.generator.view.CMSMappingsConfiguratorGenerator;
import net.anotheria.asg.generator.view.ViewConstants;
import net.anotheria.asg.generator.view.meta.*;
import net.anotheria.util.ExecutionTimer;
import net.anotheria.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This generator generate module-based actions like delete, create, edit, new, update, show and so on.
 * @author another
 */
public class ModuleActionsGenerator extends AbstractGenerator implements IGenerator {
    /**
     * Generated view.
     */
    private MetaView view;

    /**
     * If true multiop actions are generated instead of one-action for each link.
     */
    static final boolean USE_MULTIOP_ACTIONS = true;
	/**
	 *  Sufix for export XML - bean.
	 */
	public static final String exportXMLSufix = "XML";
	/**
	 *   Sufix for export CSV - bean.
	 */
	public static final String exportCSVSufix = "CSV";

	/**
     * Creates a new ModuleActionsGenerator.
     * @param aView
     */
    public ModuleActionsGenerator(MetaView aView){
        view = aView;
    }
	
    /**
     * Generates all artefacts.
     */
	@Override public List<FileEntry> generate(IGenerateable g) {
		 List<FileEntry> files = new ArrayList<FileEntry>();
		
		MetaModuleSection section = (MetaModuleSection)g;
		//System.out.println("Generate section: "+section);
		
		ExecutionTimer timer = new ExecutionTimer(section.getTitle()+" Actions");
		timer.startExecution(section.getModule().getName()+"-"+section.getTitle()+"-All");
		
		timer.startExecution(section.getModule().getName()+"-view");
		files.add(new FileEntry(generateBaseAction(section)));
		files.add(new FileEntry(generateShowAction(section)));
		files.add(new FileEntry(generateMultiOpAction(section)));
		files.add(new FileEntry(generateSearchAction(section)));
		files.add(new FileEntry(generateDeleteAction(section)));
		files.add(new FileEntry(generateDuplicateAction(section)));
		files.add(new FileEntry(generateVersionInfoAction(section)));
		files.add(new FileEntry(generateExportAction(section)));

		timer.stopExecution(section.getModule().getName()+"-view");
		try{
			if (section.getDialogs().size()>0){
				//works only if the section has a dialog.
				timer.startExecution(section.getModule().getName()+"-dialog-base");
				files.add(new FileEntry(generateMultiOpDialogAction(section)));
				files.add(new FileEntry(generateUpdateAction(section)));
				files.add(new FileEntry(generateEditAction(section)));
				files.add(new FileEntry(generateNewAction(section)));
				timer.stopExecution(section.getModule().getName()+"-dialog-base");

				MetaDocument doc = section.getDocument();
				
				timer.startExecution(section.getModule().getName()+"-dialog-copylang");
				if (GeneratorDataRegistry.hasLanguageCopyMethods(doc)){
					files.add(new FileEntry(generateLanguageCopyAction(section)));
					files.add(new FileEntry(generateSwitchMultilingualityAction(section)));
				}
				timer.stopExecution(section.getModule().getName()+"-dialog-copylang");
				
				timer.startExecution(section.getModule().getName()+"-dialog-container");
				for (int p=0; p<doc.getProperties().size(); p++){
					MetaProperty pp = doc.getProperties().get(p);
					//System.out.println("checking "+pp+" "+(pp instanceof MetaContainerProperty));
					if (pp instanceof MetaContainerProperty){
						MetaContainerProperty mcp = (MetaContainerProperty)pp;
					
						files.add(new FileEntry(generateContainerMultiOpAction(section, mcp)));
						
						files.add(new FileEntry(generateContainerShowAction(section, mcp)));
						files.add(new FileEntry(generateContainerAddRowAction(section, mcp)));
						files.add(new FileEntry(generateContainerQuickAddAction(section, mcp)));
						files.add(new FileEntry(generateContainerDeleteEntryAction(section, mcp)));
						files.add(new FileEntry(generateContainerMoveEntryAction(section, mcp)));
					}
				}
				timer.stopExecution(section.getModule().getName()+"-dialog-container");
			}

		}catch(Exception ignored){
			System.out.println("Exception occured in generation of section "+section);
			ignored.printStackTrace();
		}
		
		timer.startExecution(section.getModule().getName()+"-additional");
		MetaDocument targetDocument = section.getDocument();
		List<MetaProperty> links = targetDocument.getLinks();
		if (links.size()>0){
			files.add(new FileEntry(generateShowQueryAction(section)));
			files.add(new FileEntry(generateExecuteQueryAction(section)));
		}
		timer.stopExecution(section.getModule().getName()+"-additional");
		timer.stopExecution(section.getModule().getName()+"-"+section.getTitle()+"-All");

		//timer.printExecutionTimesOrderedByCreation();
			
		return files;
	}

	/**
	 * Generates standalone export actions!
	 * Should differs from simple List actions due to decorator ussages! - etc!
	 *
	 * @param section
	 * @return GeneratedArtefact entity
	 */
	private GeneratedArtefact generateExportAction(MetaModuleSection section) {
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		MetaDocument doc = section.getDocument();
		List<MetaViewElement> elements = section.getElements();

		boolean containsComparable = section.containsComparable();

		clazz.setPackageName(getPackage(section.getModule()));

		clazz.addImport("java.util.List");
		clazz.addImport("java.util.ArrayList");
		clazz.addImport("net.anotheria.asg.util.filter.DocumentFilter");
		clazz.addImport("net.anotheria.util.xml.XMLNode");
		addStandardActionImports(clazz);
		clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
		clazz.addImport(DataFacadeGenerator.getSortTypeImport(doc));

		clazz.addImport("net.anotheria.util.slicer.Slicer");
		clazz.addImport("net.anotheria.util.slicer.Slice");
		clazz.addImport("net.anotheria.util.slicer.Segment");
		clazz.addImport("net.anotheria.asg.util.bean.PagingLink");
		clazz.addImport("org.slf4j.Logger");
		clazz.addImport("org.slf4j.LoggerFactory");
		clazz.addImport("org.slf4j.MarkerFactory");


		for (MetaViewElement element : elements) {
			if (element instanceof MetaFieldElement) {
				MetaFieldElement field = (MetaFieldElement) element;
				MetaProperty p = doc.getField(field.getName());
				if (p instanceof MetaEnumerationProperty) {
					MetaEnumerationProperty enumeration = (MetaEnumerationProperty) p;
					EnumerationType type = (EnumerationType) GeneratorDataRegistry.getInstance().getType(enumeration.getEnumeration());
					clazz.addImport(EnumTypeGenerator.getEnumImport(type));
				}
			}
		}

		clazz.setName(getExportActionName(section));
		clazz.setParent(getBaseActionName(section));
		startClassBody();

		//generate session attributes constants
		appendStatement("public static final String SA_SORT_TYPE = SA_SORT_TYPE_PREFIX+", quote(doc.getName()));
		appendStatement("public static final String SA_FILTER = SA_FILTER_PREFIX+", quote(doc.getName()));
		appendStatement("private static final List<String> ITEMS_ON_PAGE_SELECTOR = java.util.Arrays.asList(new String[]{\"5\",\"10\",\"20\",\"25\",\"50\",\"100\",\"500\",\"1000\"})");
		appendStatement("private static final Logger log = LoggerFactory.getLogger("+getExportActionName(section)+".class)");
		
		if (containsComparable) {
			clazz.addImport("net.anotheria.util.sorter.Sorter");
			clazz.addImport("net.anotheria.util.sorter.QuickSorter");
			clazz.addImport(ModuleBeanGenerator.getListItemBeanSortTypeImport(GeneratorDataRegistry.getInstance().getContext(), doc));

			appendStatement("private Sorter<"+doc.getName()+ "> sorter");
			emptyline();
		}
		if (section.getFilters().size() > 0) {
			for (MetaFilter f : section.getFilters()) {
				appendStatement("private DocumentFilter " + getFilterVariableName(f));
			}
			emptyline();
		}


		appendString("public " + getExportActionName(section) + "(){");
		increaseIdent();
		appendStatement("super()");
		if (containsComparable)
			appendStatement("sorter = new QuickSorter<" + doc.getName() + ">()");

		//add filters
		if (section.getFilters().size() > 0) {
			appendString("try{ ");
			increaseIdent();
			for (MetaFilter f : section.getFilters()) {
				appendStatement(getFilterVariableName(f), " = (DocumentFilter) Class.forName(", quote(f.getClassName()), ").newInstance()");
			}
			decreaseIdent();
			appendString("} catch(Exception e){");
			appendIncreasedStatement("log.error(MarkerFactory.getMarker(\"FATAL\"), \"Couldn't instantiate filter:\", e)");
			appendString("}");
		}
		closeBlockNEW();


		appendString(getExecuteDeclaration());
		increaseIdent();

		if (section.getFilters().size() > 0) {
			for (int i = 0; i < section.getFilters().size(); i++) {
				//FIX: Is never used
//	    		MetaFilter f = section.getFilters().get(i);
				String filterParameterName = "filterParameter" + i;
				//hacky, only one filter at time allowed. otherwise, we must submit the filter name.
				appendStatement("String filterParameter" + i + " = " + quote(""));
				appendString("try{ ");
				appendIncreasedStatement(filterParameterName + " = getStringParameter(req, " + quote("pFilter" + i) + ")");
				appendIncreasedStatement("addBeanToSession(req, SA_FILTER+" + quote(i) + ", " + filterParameterName + ")");
				appendString("}catch(Exception ignored){");
				increaseIdent();
				appendCommentLine("no filter parameter given, tring to check in the session.");
				appendStatement(filterParameterName + " = (String)getBeanFromSession(req, SA_FILTER+" + quote(i) + ")");
				appendString("if (" + filterParameterName + "==null)");
				appendIncreasedStatement(filterParameterName + " = " + quote(""));
				closeBlockNEW();
				appendStatement("req.setAttribute(" + quote("currentFilterParameter" + i) + ", " + filterParameterName + ")");
				emptyline();
			}
		}

		//check if its sortable.
		if (containsComparable) {
			String sortType = ModuleBeanGenerator.getListItemBeanSortTypeName(doc);
			appendStatement("int sortMethod = " + sortType + ".SORT_BY_DEFAULT");
			appendStatement("boolean sortOrder = " + sortType + ".ASC");
			appendStatement("boolean sortParamSet = false");
			emptyline();
			appendString("try{");
			appendIncreasedStatement("sortMethod = getIntParameter(req, PARAM_SORT_TYPE)");
			appendIncreasedStatement("sortParamSet = true");
			appendString("}catch(Exception ignored){}");
			emptyline();
			appendString("try{");
			appendIncreasedStatement("String sortMethodName = getStringParameter(req, PARAM_SORT_TYPE_NAME)");
			appendIncreasedStatement("sortMethod = " + sortType + ".name2method(sortMethodName)");
			appendIncreasedStatement("sortParamSet = true");
			appendString("}catch(Exception ignored){}");
			emptyline();
			appendString("try{");
			increaseIdent();
			appendString("sortOrder = getStringParameter(req, PARAM_SORT_ORDER).equals(" + quote(ViewConstants.VALUE_SORT_ORDER_ASC) + ") ? ");
			appendIncreasedStatement("" + sortType + ".ASC : " + sortType + ".DESC");
			decreaseIdent();
			appendString("}catch(Exception ignored){}");
			emptyline();
			String docSortType = doc.getName() + "SortType";
			appendStatement(sortType +" sessionSortType  = null");
			appendStatement(docSortType + " sortType = null");
			appendString("if (sortParamSet){");
			increaseIdent();
			appendStatement("sessionSortType = new " + sortType + "(sortMethod, sortOrder)");
			appendStatement("sortType = new " +docSortType + "(sortMethod, sortOrder)");
			appendStatement("addBeanToSession(req, SA_SORT_TYPE, sessionSortType)");
			decreaseIdent();
			appendString("}else{");
			increaseIdent();
			appendStatement("sessionSortType = (" + sortType + ")getBeanFromSession(req, SA_SORT_TYPE)");
			appendStatement("sortType = sessionSortType == null ? new " + docSortType + "(sortMethod, sortOrder) : new " +docSortType+"(sessionSortType.getSortBy(), sessionSortType.getSortOrder())");
			appendStatement("sessionSortType = sessionSortType == null ? new " + sortType + "(sortMethod, sortOrder) : sessionSortType");
			closeBlockNEW();
			appendStatement("req.setAttribute(" + quote("currentSortCode") + ", sessionSortType.getMethodAndOrderCode())");
			emptyline();
		}

		String listName = doc.getMultiple().toLowerCase();
		if (section.getFilters().size() > 0) {
			String unfilteredListName = "_unfiltered_" + listName;
			//change this if more than one filter can be triggered at once.
			appendStatement("List<" + doc.getName() + "> " + unfilteredListName + " = " + getServiceGetterCall(section.getModule()) + ".get" + doc.getMultiple() + "()");
			appendStatement("List<" + doc.getName() + "> " + listName + " = new ArrayList<" + doc.getName() + ">()");
			appendString("for ("+ doc.getName() +" element : "+unfilteredListName+" ){");
			increaseIdent();
			appendStatement("boolean mayPass = true");
			for (int i = 0; i < section.getFilters().size(); i++) {
				MetaFilter activeFilter = section.getFilters().get(i);
				String filterVarName = getFilterVariableName(activeFilter);
				appendStatement("mayPass = mayPass && (" + filterVarName + ".mayPass( element, " + quote(activeFilter.getFieldName()) + ", filterParameter" + i + "))");

			}
			appendString("if (mayPass)");
			append(writeIncreasedStatement(listName + ".add(element)"));
			closeBlockNEW();
		} else {
			appendStatement("List<" + doc.getName() + "> " + listName + " = " + getServiceGetterCall(section.getModule()) + ".get" + doc.getMultiple() + "()");
		}

		if (containsComparable) {
			appendStatement(listName + " = sorter.sort(" + listName + ", sortType)");
		}
		emptyline();

		//paging start
		appendCommentLine("paging");
		appendStatement("int pageNumber = 1");
		appendString("try{");
		appendIncreasedStatement("pageNumber = Integer.parseInt(req.getParameter(" + quote("pageNumber") + "))");
		appendString("}catch(Exception ignored){}");
		appendStatement("Integer lastItemsOnPage = (Integer)req.getSession().getAttribute(\"currentItemsOnPage\")");
		appendStatement("int itemsOnPage = lastItemsOnPage == null ? 20 : lastItemsOnPage");
		appendString("try{");
		appendIncreasedStatement("itemsOnPage = Integer.parseInt(req.getParameter(" + quote("itemsOnPage") + "))");
		appendString("}catch(Exception ignored){}");
		appendStatement("Slice<" + doc.getName() + "> slice = Slicer.slice(new Segment(pageNumber, itemsOnPage), "+listName+")");
		appendStatement(listName+"= slice.getSliceData()");
		emptyline();

		appendStatement("XMLNode beans = " + getServiceGetterCall(section.getModule()) + ".export" + doc.getMultiple() + "ToXML(" + listName + ")");
		emptyline();
		
		appendStatement("req.setAttribute(" + quote("currentpage") + ", pageNumber)");
		appendStatement("req.setAttribute(" + quote("currentItemsOnPage") + ", itemsOnPage)");
		appendStatement("req.getSession().setAttribute(" + quote("currentItemsOnPage") + ", itemsOnPage)");
		appendStatement("req.setAttribute(" + quote("PagingSelector") + ", ITEMS_ON_PAGE_SELECTOR)");
		emptyline();
		//paging end
		appendComment("for XML node - page");
		appendStatement("addBeanToRequest(req, " + quote(listName+ exportXMLSufix) + ", beans)");
		appendComment("for CSV - page");
		appendStatement("addBeanToRequest(req, " + quote(listName+ exportCSVSufix) + ", "+listName+")");
		//add filters
		for (MetaFilter f : section.getFilters()) {
			appendStatement("addBeanToRequest(req, ", quote(getFilterVariableName(f)), ", ", getFilterVariableName(f), ".getTriggerer(\"\"))");
		}

		appendStatement("return mapping.findForward(\"success\")");
		closeBlockNEW();
		emptyline();


		return clazz;
	}

	/**
	 * Returns the name of the base action for the given section.
	 * @param section
	 * @return
	 */
	public static String getBaseActionName(MetaModuleSection section){
	    return "Base"+getActionSuffix(section);
	}
	

	/**
	 * Returns the right part of all action names tied to this section (like ***FooAction).
	 * @param section
	 * @return
	 */
	public static String getActionSuffix(MetaModuleSection section){
	    return section.getDocument().getName()+"Action";
	}
	
	public static String getMultiOpActionName(MetaModuleSection section){
	    return "MultiOp"+section.getDocument().getMultiple()+"Action";
	}

	public static String getMultiOpDialogActionName(MetaModuleSection section){
	    return "MultiOpDialog"+section.getDocument().getMultiple()+"Action";
	}

	public static String getShowActionName(MetaModuleSection section){
	    return "Show"+section.getDocument().getMultiple()+"Action";
	}
	
	public static String getExportActionName(MetaModuleSection section){
		return "Export"+section.getDocument().getMultiple()+"Action";
	}

	public static String getSearchActionName(MetaModuleSection section){
	    return "Search"+section.getDocument().getMultiple()+"Action";
	}

	public static String getShowQueryActionName(MetaModuleSection section){
		return "Show"+section.getDocument().getMultiple()+"QueriesAction";
	}

	public static String getExecuteQueryActionName(MetaModuleSection section){
		return "Execute"+section.getDocument().getMultiple()+"QueriesAction";
	}

	public static String getEditActionName(MetaModuleSection section){
	    return "Edit"+getActionSuffix(section);
	}

	public static String getUpdateActionName(MetaModuleSection section){
	    return "Update"+getActionSuffix(section);
	}
	
	public static String getLanguageCopyActionName(MetaModuleSection section){
	    return "CopyLang"+getActionSuffix(section);
	}

	public static String getSwitchMultilingualityActionName(MetaModuleSection section){
	    return "SwitchMultilang"+getActionSuffix(section);
	}

	public static String getVersionInfoActionName(MetaModuleSection section){
	    return "VersionInfo"+getActionSuffix(section);
	}

	public static String getNewActionName(MetaModuleSection section){
	    return "New"+getActionSuffix(section);
	}
	
	public static String getCreateActionName(MetaModuleSection section){
	    return "Create"+getActionSuffix(section);
	}
	
	public static String getDeleteActionName(MetaModuleSection section){
	    return "Delete"+getActionSuffix(section);
	}

	public static String getDuplicateActionName(MetaModuleSection section){
		return "Duplicate"+getActionSuffix(section);
	}

    public static String getLockActionName(MetaModuleSection section) {
        return "Lock"+getActionSuffix(section);
    }

    public static String getUnLockActionName(MetaModuleSection section) {
        return "UnLock"+getActionSuffix(section);
    }
	
	/**
	 * Generates a cumulated action which bundles multiple view operation in one class to reduce the number of generated classes.
	 * @param section
	 * @return
	 */
	private GeneratedClass generateMultiOpAction(MetaModuleSection section){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		MetaDocument doc = section.getDocument();

	    clazz.setPackageName(getPackage(section.getModule()));
	    clazz.addImport("net.anotheria.util.NumberUtils");
		addStandardActionImports(clazz);
		clazz.addImport(DataFacadeGenerator.getDocumentFactoryImport(GeneratorDataRegistry.getInstance().getContext(), doc));
		clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));

		clazz.setName(getMultiOpActionName(section));
		clazz.setParent(getBaseActionName(section));

		startClassBody();
		
		appendString( getExecuteDeclaration(null));
	    increaseIdent();
	    appendStatement("String path = stripPath(mapping.getPath())");
	    //MOVE THIS TO MULTIOP WITHOUT DIALOG
	    writePathResolveForMultiOpAction(doc,CMSMappingsConfiguratorGenerator.ACTION_VERSIONINFO);
	    
	    appendStatement("throw new IllegalArgumentException("+quote("Unknown path: ")+"+path)");
	    closeBlockNEW();
	    emptyline();
	    
		
	    generateVersionInfoActionMethod(section, CMSMappingsConfiguratorGenerator.getPath(doc, CMSMappingsConfiguratorGenerator.ACTION_VERSIONINFO));
		return clazz;
	}
	
	/**
	 * Generates a cumulated action which bundles multiple view operation for the dialog in one class to reduce the number of generated classes.
	 * @param section
	 * @return
	 */
	private GeneratedClass generateMultiOpDialogAction(MetaModuleSection section){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		appendGenerationPoint("generateMultiOpDialogAction");
		
		MetaDocument doc = section.getDocument();
		MetaDialog dialog = section.getDialogs().get(0);
        final boolean cMSStorageType = StorageType.CMS.equals(doc.getParentModule().getStorageType());
		
		clazz.setName(getMultiOpDialogActionName(section));
		clazz.setParent(getBaseActionName(section), ModuleBeanGenerator.getDialogBeanName(dialog, doc));
		clazz.setPackageName(getPackage(section.getModule()));
		Context context = GeneratorDataRegistry.getInstance().getContext();
		
	    //write imports...
	    clazz.addImport("net.anotheria.util.NumberUtils");
	    addStandardActionImports(clazz);
		
	    clazz.addImport(DataFacadeGenerator.getDocumentFactoryImport(context, doc));
	    clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
	    clazz.addImport(ModuleBeanGenerator.getDialogBeanImport(dialog, doc));
        if (cMSStorageType){
            clazz.addImport("net.anotheria.asg.data.LockableObject");
            clazz.addImport("net.anotheria.asg.util.locking.helper.DocumentLockingHelper");
        }
		if (GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported() && doc.isMultilingual())
			clazz.addImport("net.anotheria.asg.data.MultilingualObject");

	    boolean validationAwareAction = false;
		List<MetaViewElement> elements = createMultilingualList(dialog.getElements(), doc);
		for (MetaViewElement elem:elements){
			if (elem instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)elem;
				MetaProperty p = doc.getField(field.getName());
				if (p.getType() == MetaProperty.Type.IMAGE || (p.getType() == MetaProperty.Type.LIST && ((MetaListProperty)p).getContainedProperty().getType() == MetaProperty.Type.IMAGE)){
					clazz.addImport("net.anotheria.webutils.filehandling.actions.FileStorage");
					clazz.addImport("net.anotheria.webutils.filehandling.beans.TemporaryFileHolder");
                    clazz.addImport("java.lang.reflect.Method");
                    clazz.addImport("java.lang.reflect.InvocationTargetException");
                    clazz.addImport("net.anotheria.util.StringUtils");
                    clazz.addImport("org.apache.commons.lang.WordUtils");
					break;
				}
				if (elem.isValidated()) {
					validationAwareAction = true;
					clazz.addInterface("net.anotheria.maf.validation.ValidationAware");
					clazz.addImport("net.anotheria.maf.validation.ValidationError");
					clazz.addImport(List.class);
				}
			}
		}
	    emptyline();

	    startClassBody();
	    generateExecuteMethod(clazz, dialog, doc);
		appendString( getExecuteDeclaration(null));
	    increaseIdent();
	    appendStatement("String path = stripPath(mapping.getPath())");
	    writePathResolveForMultiOpAction(doc,CMSMappingsConfiguratorGenerator.ACTION_DELETE);
	    writePathResolveForMultiOpAction(doc,CMSMappingsConfiguratorGenerator.ACTION_DUPLICATE);
	    writePathResolveForMultiOpAction(doc,CMSMappingsConfiguratorGenerator.ACTION_UPDATE);
	    writePathResolveForMultiOpAction(doc,CMSMappingsConfiguratorGenerator.ACTION_CLOSE);
	    if (GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported() && doc.isMultilingual()){
	    	writePathResolveForMultiOpAction(doc, CMSMappingsConfiguratorGenerator.ACTION_COPY_LANG);
	    	writePathResolveForMultiOpAction(doc, CMSMappingsConfiguratorGenerator.ACTION_SWITCH_MULTILANGUAGE_INSTANCE);
	    }
        //Lock && Unlock!!!
	    
        if(cMSStorageType){
            writePathResolveForMultiOpAction(doc,CMSMappingsConfiguratorGenerator.ACTION_LOCK);
            writePathResolveForMultiOpAction(doc,CMSMappingsConfiguratorGenerator.ACTION_UNLOCK);
        }
	    
	    appendStatement("throw new IllegalArgumentException("+quote("Unknown path: ")+"+path)");
	    closeBlockNEW();
	    emptyline();
	    
		
	    generateDeleteActionMethod(section, CMSMappingsConfiguratorGenerator.getPath(doc, CMSMappingsConfiguratorGenerator.ACTION_DELETE));
	    emptyline();
	    generateDuplicateActionMethod(section, CMSMappingsConfiguratorGenerator.getPath(doc, CMSMappingsConfiguratorGenerator.ACTION_DUPLICATE));
	    emptyline();
	    generateUpdateActionMethod(section, CMSMappingsConfiguratorGenerator.getPath(doc, CMSMappingsConfiguratorGenerator.ACTION_UPDATE));
	    emptyline();
	    if (GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported() && doc.isMultilingual()){
	    	generateLanguageCopyActionMethod(section, CMSMappingsConfiguratorGenerator.getPath(doc, CMSMappingsConfiguratorGenerator.ACTION_COPY_LANG));
	    	emptyline();
	    	generateSwitchMultilingualityActionMethod(section, CMSMappingsConfiguratorGenerator.getPath(doc, CMSMappingsConfiguratorGenerator.ACTION_SWITCH_MULTILANGUAGE_INSTANCE));
	    	emptyline();
	    }

        if (cMSStorageType) {
            //Actually Locking
            generateLockManagementActionMethod(section, CMSMappingsConfiguratorGenerator.getPath(doc, CMSMappingsConfiguratorGenerator.ACTION_LOCK),true);
            emptyline();
            //Actually Unlocking
            generateLockManagementActionMethod(section, CMSMappingsConfiguratorGenerator.getPath(doc, CMSMappingsConfiguratorGenerator.ACTION_UNLOCK),false);
            emptyline();
            generateRedirectPathMethod(section);
			emptyline();
        }
		generateCloseAction(section, CMSMappingsConfiguratorGenerator.getPath(doc,CMSMappingsConfiguratorGenerator.ACTION_CLOSE));
		emptyline();
		
		if (validationAwareAction) {
			clazz.addImport("net.anotheria.asg.util.helper.cmsview.CMSViewHelperUtil");
			clazz.addImport("net.anotheria.asg.util.helper.cmsview.CMSViewHelperRegistry");
			clazz.addImport("java.util.Map");
			clazz.addImport("java.util.HashMap");
			clazz.addImport("net.anotheria.maf.validation.ValidationError");
			//check if we have to import list.
			for (MetaViewElement element : elements) {
				if (element instanceof MetaFieldElement) {
					MetaFieldElement field = (MetaFieldElement) element;
					MetaProperty p = doc.getField(field.getName());
					if (p.isLinked() || p instanceof MetaEnumerationProperty) {
						clazz.addImport("java.util.List");
						clazz.addImport("java.util.ArrayList");
						clazz.addImport("net.anotheria.webutils.bean.LabelValueBean");
					}
					if (p instanceof MetaEnumerationProperty) {
	                    MetaEnumerationProperty mep = (MetaEnumerationProperty) p;
	                    EnumerationType type = (EnumerationType) GeneratorDataRegistry.getInstance().getType(mep.getEnumeration());
	                    clazz.addImport(EnumTypeGenerator.getEnumImport(type));
	                }
				}
			}

			generateExecuteOnValidationErrorMethod(section, doc, dialog, elements);
			emptyline();
		}
	    
	    return clazz;
	}
	
	private void generateExecuteOnValidationErrorMethod(MetaModuleSection section, MetaDocument doc, MetaDialog dialog, List<MetaViewElement> elements) {
		appendString("public ActionForward executeOnValidationError(ActionMapping mapping, FormBean formBean, List<ValidationError> errors, HttpServletRequest req, HttpServletResponse res) throws Exception {");
		increaseIdent();
		
		appendString("Map<String, ValidationError> errorsMap = new HashMap<String,ValidationError>();");
		appendString("for (ValidationError error : errors) {");
		increaseIdent();
		appendString("errorsMap.put(error.getField(), error);");
		closeBlock("errorsMap ready");
		String formClassName = ModuleBeanGenerator.getDialogBeanName(dialog, doc);
		appendString(formClassName+" form = ("+formClassName+")formBean;");
		appendPrepareFormForEditView(elements, doc, false);
		appendString("if (form.getId() == null || form.getId().isEmpty()) {");
			increaseIdent();
			decreaseIdent();
		appendString("} else {");
			increaseIdent();
			//add locking/ML/checks
			appendStatement("String id = form.getId()");
			appendStatement(doc.getName()," ",doc.getVariableName()," = ",getServiceGetterCall(section.getModule()),".get",doc.getName(),"(id)");

			if (doc.isMultilingual()){
				MetaProperty p = doc.getField(ModuleBeanGenerator.FIELD_ML_DISABLED);
				String propertyCopy = "form."+p.toBeanSetter()+"(((MultilingualObject)"+doc.getVariableName()+").isMultilingualDisabledInstance())";
				appendStatement(propertyCopy);
			}

	        //adding additional Lock properties
			final boolean isCMS = StorageType.CMS.equals(doc.getParentModule().getStorageType());
			if(isCMS){
	            MetaProperty prop = new MetaProperty(LockableObject.INT_LOCK_PROPERTY_NAME,MetaProperty.Type.BOOLEAN);
	            String propertyCopy = "form."+prop.toBeanSetter()+"(((LockableObject)"+doc.getVariableName()+").isLocked())";
	            appendStatement(propertyCopy);
	            prop =  new MetaProperty(LockableObject.INT_LOCKER_ID_PROPERTY_NAME,MetaProperty.Type.STRING);
	            propertyCopy = "form."+prop.toBeanSetter()+"(((LockableObject)"+doc.getVariableName()+").getLockerId())";
	            appendStatement(propertyCopy);
	            prop =  new MetaProperty(LockableObject.INT_LOCKING_TIME_PROPERTY_NAME,MetaProperty.Type.STRING);
			    propertyCopy = "form."+prop.toBeanSetter()+"(net.anotheria.util.NumberUtils.makeISO8601TimestampString(((LockableObject)"+doc.getVariableName()+").getLockingTime()) +" +
						" \" automatic unlock expected AT : \" + net.anotheria.util.NumberUtils.makeISO8601TimestampString(((LockableObject)"+doc.getVariableName()+").getLockingTime() + getLockingTimeout()))";

	            appendStatement(propertyCopy);
	        }
			//add container sizes
			for (MetaViewElement element : elements) {
				if (element instanceof MetaFieldElement) {
					MetaFieldElement field = (MetaFieldElement) element;
					MetaProperty p = doc.getField(field.getName());
					if (p instanceof MetaContainerProperty){
						appendString( "// "+p.getName()+" is a table, storing size only");
						String lang = getElementLanguage(element);
//						appendString("if (form.getId()!=null && !form.getId().isEmpty())");
						appendStatement("form."+p.toBeanSetter(lang)+"("+doc.getVariableName()+"."+DataFacadeGenerator.getContainerSizeGetterName((MetaContainerProperty)p, lang)+"())");
					}
				}
			}
			
		closeBlock("form prepared");
		
		appendString("addBeanToRequest(req, \"validationErrors\" , errorsMap);");
		
		appendString("addBeanToRequest(req, "+quote(CMSMappingsConfiguratorGenerator.getDialogFormName(dialog, doc))+" , formBean);");
		appendString("addBeanToRequest(req, \"save.label.prefix\", \"Save\");");
		appendString("addBeanToRequest(req, \"apply.label.prefix\" , \"Apply\");");
		appendString("addBeanToRequest(req, \"objectInfoString\" , \"none\");");
		
		//add field descriptions ...
		emptyline();
		appendStatement("addFieldExplanations(req, null)");	
		emptyline();
		
		appendString("return mapping.findForward(\"validationError\");");
		closeBlock("executeOnValidationError");

		emptyline();
		appendAddFieldExplanationsMethod(doc);
	}
	
	private void generateExecuteMethod(GeneratedClass clazz, MetaDialog dialog, MetaDocument document){
		String formBeanName = ModuleBeanGenerator.getDialogBeanName(dialog, document);
		clazz.addImport("net.anotheria.maf.bean.annotations.Form");
//		clazz.addImport(clazz);
		appendString("@Override");
		appendString("public ActionForward execute(ActionMapping mapping, @Form("+formBeanName+".class) FormBean formBean, HttpServletRequest req, HttpServletResponse res) throws Exception{");
		increaseIdent();
		appendStatement("return super.execute(mapping, formBean, req, res)");
		closeBlock("execute");
		emptyline();
	}

	/**
	 * Generate close action with unlock funcional
	 *
	 * @param section
	 * @param methodName
	 */
	private void generateCloseAction(MetaModuleSection section, String methodName) {
		MetaDocument doc = section.getDocument();
		appendString(getExecuteDeclaration(methodName));
		increaseIdent();
		if (StorageType.CMS.equals(doc.getParentModule().getStorageType())) {
			appendStatement("String id = getStringParameter(req, PARAM_ID)");
			appendStatement(doc.getName() + " " + doc.getVariableName() + "Curr = id != null && !id.equals(\"\") ? " + getServiceGetterCall(section.getModule()) + ".get" + doc.getName() + "(id) : null");
			appendString("if("+ doc.getVariableName() +"Curr != null && " + doc.getVariableName() + "Curr instanceof LockableObject && ((LockableObject)" + doc.getVariableName() + "Curr).isLocked()) ");
			appendIncreasedStatement("unLock" + doc.getMultiple() + "(" + doc.getVariableName() + "Curr, req, false)");
		}
		appendStatement("res.sendRedirect(" + getShowActionRedirect(doc) + ")");
		appendStatement("return null");
		closeBlockNEW();
	}

	private void generateRedirectPathMethod(MetaModuleSection section) {
        MetaDocument doc = section.getDocument();
        appendComment("Simplest method for redirect url creation. nextAction == showEdit - going to 'editView', to 'listView' otherwise. ");
        appendString("private String getRedirectUrl(HttpServletRequest req, "+doc.getName()+" item){");
        increaseIdent();
        appendStatement("String nextAction = req.getParameter("+quote("nextAction")+")");
		appendString("if (item==null || nextAction == null || nextAction.length() == 0)");
        appendIncreasedStatement("return "+getShowActionRedirect(doc));
        appendString("else");
        appendIncreasedString("return nextAction.equals(\"showEdit\") ? "+getEditActionRedirect(doc)+"+"+quote("&pId=")+"+item.getId()");
        appendIncreasedString("       : "+getShowActionRedirect(doc)+";"); 
        closeBlockNEW();
    }


    /**
     * Generates Lock && Unlock Actions!!!
     * @param section
     * @param methodName
     * @param isLock
     */
    private void generateLockManagementActionMethod(MetaModuleSection section, String methodName, boolean isLock) {
       	MetaDocument doc = section.getDocument();
		appendString(getExecuteDeclaration(methodName));
		increaseIdent();
		appendStatement("String id = getStringParameter(req, PARAM_ID)");
		appendStatement(doc.getName() + " " + doc.getVariableName() + "Curr = id != null && !id.equals(\"\") ? " + getServiceGetterCall(section.getModule()) + ".get" + doc.getName() + "(id) : null");
        appendString("if("+ doc.getVariableName() +"Curr != null && "+doc.getVariableName()+"Curr instanceof LockableObject){ ");
        appendIncreasedStatement("LockableObject lockable = (LockableObject)" + doc.getVariableName()+"Curr");
        if (isLock) {
            //Locking CASE
            //Actually We does not Care - about admin role in Lock action!  So checkExecutionPermission  2-nd parameter  can be anything!
            appendIncreasedStatement("DocumentLockingHelper.lock.checkExecutionPermission(lockable,false,getUserId(req))");
            appendIncreasedStatement("lock" + doc.getMultiple() + "("+doc.getVariableName()+"Curr, req)");
        } else {
            //Unlocking CASE
            appendIncreasedStatement("DocumentLockingHelper.unLock.checkExecutionPermission(lockable,isUserInRole(req, \"admin\"),getUserId(req))");
            appendIncreasedStatement("unLock" + doc.getMultiple() + "("+doc.getVariableName()+"Curr, req, false)");
        }
        appendString("}");
        appendStatement("res.sendRedirect(getRedirectUrl(req, "+doc.getVariableName()+"Curr))");
	    appendStatement("return null");
	    closeBlockNEW();
    }

    private void writePathResolveForMultiOpAction(MetaDocument doc,String action){
		String path = CMSMappingsConfiguratorGenerator.getPath(doc, action);
		appendString("if (path.equals("+quote(path)+"))");
		appendIncreasedStatement("return "+path+"(mapping, af, req, res)");
	}

	private void writePathResolveForContainerMultiOpAction(MetaDocument doc, MetaContainerProperty container, String action){
		String path = CMSMappingsConfiguratorGenerator.getContainerPath(doc, container, action);
		appendString("if (path.equals("+quote(path)+"))");
		appendIncreasedStatement("return "+path+"(mapping, af, req, res)");
	}
	
	/**
	 * Generates the list presentation action.
	 * @param section
	 * @return
	 */
	private GeneratedClass generateShowAction(MetaModuleSection section){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		appendGenerationPoint("generateShowAction");
		
	    MetaDocument doc = section.getDocument();
		List<MetaViewElement> elements = section.getElements();
	    
	    boolean containsComparable = section.containsComparable();
	    
	    clazz.setPackageName(getPackage(section.getModule()));

	    clazz.addImport("java.util.List");
	    clazz.addImport("java.util.ArrayList");
	    clazz.addImport("net.anotheria.asg.util.decorators.IAttributeDecorator");
	    clazz.addImport("net.anotheria.asg.util.filter.DocumentFilter");
	    clazz.addImport("net.anotheria.util.NumberUtils");
	    clazz.addImport("net.anotheria.anoplass.api.util.paging.PagingControl");
	    addStandardActionImports(clazz);
	    clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
	    clazz.addImport(ModuleBeanGenerator.getListItemBeanImport(GeneratorDataRegistry.getInstance().getContext(), doc));
		
		clazz.addImport("net.anotheria.util.slicer.Slicer");
		clazz.addImport("net.anotheria.util.slicer.Slice");
		clazz.addImport("net.anotheria.util.slicer.Segment");
        //LOckableObject import!!!
        if(StorageType.CMS.equals(doc.getParentModule().getStorageType())){
           clazz.addImport("net.anotheria.asg.data.LockableObject");
        }

		clazz.addImport("org.slf4j.Logger");
		clazz.addImport("org.slf4j.LoggerFactory");
		clazz.addImport("org.slf4j.MarkerFactory");
		//check if we have to property definition files.
		//check if we have decorators
		List<MetaDecorator> neededDecorators = new ArrayList<MetaDecorator>();
		
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			if (element instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)element;
				MetaProperty p = doc.getField(field.getName());
				if (p instanceof MetaEnumerationProperty){
					MetaEnumerationProperty enumeration = (MetaEnumerationProperty)p;
					EnumerationType type = (EnumerationType)GeneratorDataRegistry.getInstance().getType(enumeration.getEnumeration());
					clazz.addImport(EnumTypeGenerator.getEnumImport(type));
				}
				
				MetaDecorator d = field.getDecorator();
				if (d!=null){
					 if (neededDecorators.indexOf(d)==-1)
					 	neededDecorators.add(d);
				}
			}
		}
		
		clazz.setName(getShowActionName(section));
		clazz.setParent(getBaseActionName(section));

		startClassBody();
	    //generate session attributes constants
	    appendStatement("public static final String SA_SORT_TYPE = SA_SORT_TYPE_PREFIX+", quote(doc.getName()));
	    appendStatement("public static final String SA_FILTER = SA_FILTER_PREFIX+", quote(doc.getName()));
	    appendStatement("private static final List<String> ITEMS_ON_PAGE_SELECTOR = java.util.Arrays.asList(new String[]{\"5\",\"10\",\"20\",\"25\",\"50\",\"100\",\"500\",\"1000\"})");
	    
	    appendStatement("private static Logger log = LoggerFactory.getLogger("+getShowActionName(section)+".class)");
	    
	    boolean containsDecorators = neededDecorators.size() >0;
	    
		if (containsComparable){
			clazz.addImport(ModuleBeanGenerator.getListItemBeanSortTypeImport(GeneratorDataRegistry.getInstance().getContext(), doc));
			clazz.addImport("net.anotheria.util.sorter.Sorter");
			clazz.addImport("net.anotheria.util.sorter.QuickSorter");

			appendStatement("private Sorter<", ModuleBeanGenerator.getListItemBeanName(doc), "> sorter");
			emptyline();
		}
		
		if (containsDecorators){
			for (int i=0; i<elements.size();i++){
				MetaViewElement element = (MetaViewElement)elements.get(i);
				if (element.getDecorator()!=null){
					appendStatement("private IAttributeDecorator "+getDecoratorVariableName(element));
				}
			}
			emptyline();
		}
		
		if (section.getFilters().size()>0){
			for (MetaFilter f : section.getFilters()){
				appendStatement("private DocumentFilter "+getFilterVariableName(f));
			}
			emptyline();
		}
		
		
		appendString( "public "+getShowActionName(section)+"(){");
		increaseIdent();
		appendStatement("super()");
		if (containsComparable)
			appendStatement("sorter = new QuickSorter<"+ModuleBeanGenerator.getListItemBeanName(doc)+">()");
		if (containsDecorators){
			appendString( "try{ ");
			increaseIdent();
			for (int i=0; i<elements.size();i++){
				MetaViewElement element = elements.get(i);
				if (element.getDecorator()!=null){
					appendStatement(getDecoratorVariableName(element)+" = (IAttributeDecorator)Class.forName("+quote(element.getDecorator().getClassName())+").newInstance()");
				}
			}
			decreaseIdent();
			appendString( "} catch(Exception e){");
			appendIncreasedStatement("log.error(MarkerFactory.getMarker(\"FATAL\"), \"Couldn't instantiate decorator:\", e)");
			appendString( "}");
		}
	    //add filters
		if (section.getFilters().size()>0){
			appendString( "try{ ");
			increaseIdent();
			for (MetaFilter f : section.getFilters()){
				appendStatement(getFilterVariableName(f), " = (DocumentFilter) Class.forName(", quote(f.getClassName()), ").newInstance()");
			}
			decreaseIdent();
			appendString( "} catch(Exception e){");
			appendIncreasedStatement("log.error(MarkerFactory.getMarker(\"FATAL\"), \"Couldn't instantiate filter:\", e)");
			appendString( "}");
		}
	    closeBlockNEW();
		

	    appendString( getExecuteDeclaration());
	    increaseIdent();
	    
	    if (section.getFilters().size()>0){
	    	for (int i=0 ; i<section.getFilters().size(); i++){
	    		//FIX: Is never used
//	    		MetaFilter f = section.getFilters().get(i);
	    		String filterParameterName = "filterParameter"+i;
		    	//hacky, only one filter at time allowed. otherwise, we must submit the filter name.
		    	appendStatement("String filterParameter"+i+" = "+quote(""));
		    	appendString( "try{ ");
		    	appendIncreasedStatement(filterParameterName+" = getStringParameter(req, "+quote("pFilter"+i)+")");
		    	appendIncreasedStatement("addBeanToSession(req, SA_FILTER+"+quote(i)+", "+filterParameterName+")");
		    	appendString( "}catch(Exception ignored){");
		    	increaseIdent();
		    	appendCommentLine("no filter parameter given, tring to check in the session.");
		    	appendStatement(filterParameterName+" = (String)getBeanFromSession(req, SA_FILTER+"+quote(i)+")");
		    	appendString( "if ("+filterParameterName+"==null)");
		    	appendIncreasedStatement(filterParameterName+" = "+quote(""));
			    closeBlockNEW();
		    	appendStatement("req.setAttribute("+quote("currentFilterParameter"+i)+", "+filterParameterName+")");
		    	emptyline();
	    	}
	    }
	    
	    //check if its sortable.
		if (containsComparable){
			String sortType = ModuleBeanGenerator.getListItemBeanSortTypeName(doc);
			appendStatement("int sortMethod = "+sortType+".SORT_BY_DEFAULT");
			appendStatement("boolean sortOrder = "+sortType+".ASC");
			appendStatement("boolean sortParamSet = false");
			emptyline();
			appendString( "try{");
			appendIncreasedStatement("sortMethod = getIntParameter(req, PARAM_SORT_TYPE)");
			appendIncreasedStatement("sortParamSet = true");
			appendString( "}catch(Exception ignored){}");
			emptyline	();    
			appendString( "try{");
			appendIncreasedStatement("String sortMethodName = getStringParameter(req, PARAM_SORT_TYPE_NAME)");
			appendIncreasedStatement("sortMethod = "+sortType+".name2method(sortMethodName)");
			appendIncreasedStatement("sortParamSet = true");
			appendString( "}catch(Exception ignored){}");
			emptyline();
			appendString( "try{");
			increaseIdent();
			appendString( "sortOrder = getStringParameter(req, PARAM_SORT_ORDER).equals("+quote(ViewConstants.VALUE_SORT_ORDER_ASC)+") ? ");
			appendIncreasedStatement(""+sortType+".ASC : "+sortType+".DESC");
			decreaseIdent();
			appendString( "}catch(Exception ignored){}");
			emptyline();
			appendStatement(ModuleBeanGenerator.getListItemBeanSortTypeName(doc)+" sortType = null");
			appendString( "if (sortParamSet){");
			increaseIdent();
			appendStatement("sortType = new "+ModuleBeanGenerator.getListItemBeanSortTypeName(doc)+"(sortMethod, sortOrder)");
			appendStatement("addBeanToSession(req, SA_SORT_TYPE, sortType)");
			decreaseIdent();
			appendString( "}else{");
			increaseIdent();
			appendStatement("sortType = ("+ModuleBeanGenerator.getListItemBeanSortTypeName(doc)+")getBeanFromSession(req, SA_SORT_TYPE)");
			appendString( "if (sortType==null)");
			appendIncreasedStatement("sortType = new "+ModuleBeanGenerator.getListItemBeanSortTypeName(doc)+"(sortMethod, sortOrder)");
		    closeBlockNEW();
			appendStatement("req.setAttribute("+quote("currentSortCode")+", sortType.getMethodAndOrderCode())");
			emptyline();
		}
	    
	    String listName = doc.getMultiple().toLowerCase();
	    if (section.getFilters().size()>0){
		    String unfilteredListName = "_unfiltered_"+listName;
		    //change this if more than one filter can be triggered at once.
		    appendStatement("List<"+doc.getName()+"> "+unfilteredListName+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getMultiple()+"()");
		    appendStatement("List<"+doc.getName()+"> "+listName+" = new ArrayList<"+doc.getName()+">()");
		    appendString( "for (int i=0; i<"+unfilteredListName+".size(); i++){");
		    increaseIdent();
		    appendStatement("boolean mayPass = true");
		    for (int i=0; i<section.getFilters().size(); i++){
			    MetaFilter activeFilter = section.getFilters().get(i);
			    String filterVarName = getFilterVariableName(activeFilter);
			    appendStatement("mayPass = mayPass && ("+filterVarName+".mayPass("+unfilteredListName+".get(i), "+quote(activeFilter.getFieldName())+", filterParameter"+i+"))");
		    	
		    }
		    appendString( "if (mayPass)");
		    append(writeIncreasedStatement(listName+".add("+unfilteredListName+".get(i))"));
		    closeBlockNEW();
	    }else{
		    appendStatement("List<"+doc.getName()+"> "+listName+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getMultiple()+"()");
	    }

		appendStatement("List<"+ModuleBeanGenerator.getListItemBeanName(doc)+"> beans = new ArrayList<"+ModuleBeanGenerator.getListItemBeanName(doc)+">("+listName+".size())");
		appendString("for ("+doc.getName()+" "+doc.getVariableName()+" : "+listName+"){");
		increaseIdent();
		//autoUnlocking!
		if (StorageType.CMS.equals(doc.getParentModule().getStorageType())) {
			appendStatement("check" + doc.getMultiple() + "(" + doc.getVariableName() + ", req)");
		}
		appendStatement(ModuleBeanGenerator.getListItemBeanName(doc)+" bean = "+getMakeBeanFunctionName(ModuleBeanGenerator.getListItemBeanName(doc))+"("+doc.getVariableName()+")");
		appendStatement("beans.add(bean)");
		closeBlockNEW();
	    emptyline();
	    if (containsComparable){
	    	appendStatement("beans = sorter.sort(beans, sortType)");
	    }

	    //paging start
	    appendCommentLine("paging");
	    appendStatement("int pageNumber = 1"); 
	    appendString( "try{");
	    appendIncreasedStatement("pageNumber = Integer.parseInt(req.getParameter("+quote("pageNumber")+"))");
	    appendString( "}catch(Exception ignored){}");
	    appendStatement("Integer lastItemsOnPage = (Integer)req.getSession().getAttribute(\"currentItemsOnPage\")");
	    appendStatement("int itemsOnPage = lastItemsOnPage == null ? 20 : lastItemsOnPage"); 
	    appendString( "try{");
	    appendIncreasedStatement("itemsOnPage = Integer.parseInt(req.getParameter("+quote("itemsOnPage")+"))");
	    appendString( "}catch(Exception ignored){}");
	    appendStatement("Slice<"+ModuleBeanGenerator.getListItemBeanName(doc)+"> slice = Slicer.slice(new Segment(pageNumber, itemsOnPage), beans)");
	    appendStatement("beans = slice.getSliceData()");
	    emptyline();
	    
	    appendCommentLine("prepare paging control");
	    appendStatement("PagingControl pagingControl = new PagingControl(slice.getCurrentSlice(), slice.getElementsPerSlice(), slice.getTotalNumberOfItems())");
	    appendCommentLine("end paging control");
	    
	    appendStatement("req.setAttribute("+quote("pagingControl")+", pagingControl)");
	    appendStatement("req.setAttribute("+quote("currentpage")+", pageNumber)");
	    appendStatement("req.setAttribute("+quote("currentItemsOnPage")+", itemsOnPage)");
	    appendStatement("req.getSession().setAttribute("+quote("currentItemsOnPage")+", itemsOnPage)");
	    appendStatement("req.setAttribute("+quote("PagingSelector")+", ITEMS_ON_PAGE_SELECTOR)");
	    
	    emptyline();
	    //paging end
	    
	    
	    
	    appendStatement("addBeanToRequest(req, "+quote(listName)+", beans)");
	    
	    //add filters
	    for (MetaFilter f : section.getFilters()){
	    	appendStatement("addBeanToRequest(req, ", quote(getFilterVariableName(f)), ", ", getFilterVariableName(f), ".getTriggerer(\""+f.getFieldName()+"\"))");
	    }
	    
	    appendStatement("return mapping.findForward(\"success\")");
	    closeBlockNEW();
	    emptyline();
	    
	    
	    // BEAN creation function
	    appendString( "protected "+ModuleBeanGenerator.getListItemBeanName(doc)+" "+getMakeBeanFunctionName(ModuleBeanGenerator.getListItemBeanName(doc))+"("+doc.getName()+" "+doc.getVariableName()+") {");
	    increaseIdent();
	    appendStatement(ModuleBeanGenerator.getListItemBeanName(doc)+" bean = new "+ModuleBeanGenerator.getListItemBeanName(doc)+"()");
	    //set the properties.
	    //this is a hack...
	    appendStatement("bean.setPlainId("+doc.getVariableName()+".getId())");

		elements = createMultilingualList(elements, doc);
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			if (element instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement) element;
				String lang = null;
				if (field instanceof MultilingualFieldElement)
					lang = ((MultilingualFieldElement)field).getLanguage();
				MetaProperty p = doc.getField(field.getName());
//				if (p instanceof MetaContainerProperty){
//					String value = "";
//					value = doc.getVariableName()+"."+DataFacadeGenerator.getContainerSizeGetterName((MetaContainerProperty)p, lang)+"()";
//					if (element.getDecorator()!=null){
//						//if decorated, save original value for sorting and replace with decorated value
//						MetaProperty tmp ;
//						if (element instanceof MultilingualFieldElement)
//							tmp = new MetaProperty(p.getName("ForSorting", ((MultilingualFieldElement)element).getLanguage()), p.getType());
//						else
//							tmp = new MetaProperty(p.getName()+"ForSorting", p.getType());
//							
//						appendStatement("bean."+tmp.toBeanSetter(lang)+"("+value+")");
//						MetaDecorator d = element.getDecorator();
//						value = getDecoratorVariableName(element)+".decorate("+doc.getVariableName()+", "+quote(p.getName())+", "+quote(d.getRule())+")";
//					}
//					appendStatement("bean."+p.toBeanSetter(lang)+"("+value+")");
//				}else{
					if (p instanceof MetaEnumerationProperty){
						MetaEnumerationProperty mep = (MetaEnumerationProperty)p;
						EnumerationType type = (EnumerationType)GeneratorDataRegistry.getInstance().getType(mep.getEnumeration());
						openTry();
						appendStatement("bean."+p.toBeanSetter(lang)+"("+EnumTypeGenerator.getEnumClassName(type)+".getConstantByValue("+doc.getVariableName()+".get"+p.getAccesserName()+"()).name())");
						appendCatch(ConstantNotFoundException.class);
						appendStatement("bean."+p.toBeanSetter(lang)+"("+quote("-----")+")");
						closeBlock("try");
						
					}else {
						String value = "";
						value = doc.getVariableName()+".get"+p.getAccesserName(lang)+"()";
						if (element.getDecorator()!=null){
							MetaProperty tmp = null; 
							if (lang !=null)
								tmp = new MetaProperty(p.getName("ForSorting", lang), p.getType());
							else
								tmp = new MetaProperty(p.getName()+"ForSorting", p.getType());

							appendStatement("bean."+tmp.toBeanSetter()+"("+value+")");
							MetaDecorator d = element.getDecorator();
							value = getDecoratorVariableName(element)+".decorate("+doc.getVariableName()+", "+quote(p.getName()+(lang==null?"":"_"+lang))+", "+quote(d.getRule())+")";
						}
						appendStatement("bean."+p.toBeanSetter(lang)+"("+value+")");
					}
//				}
			}
		}

         //adding additional Lock properties
        if(StorageType.CMS.equals(doc.getParentModule().getStorageType())){
            MetaProperty prop = new MetaProperty(LockableObject.INT_LOCK_PROPERTY_NAME,MetaProperty.Type.BOOLEAN);
            String propertyCopy = "bean."+prop.toBeanSetter()+"(((LockableObject)"+doc.getVariableName()+").isLocked())";
            appendStatement(propertyCopy);
            prop =  new MetaProperty(LockableObject.INT_LOCKER_ID_PROPERTY_NAME,MetaProperty.Type.STRING);
            propertyCopy = "bean."+prop.toBeanSetter()+"(((LockableObject)"+doc.getVariableName()+").getLockerId())";
            appendStatement(propertyCopy);
            prop =  new MetaProperty(LockableObject.INT_LOCKING_TIME_PROPERTY_NAME,MetaProperty.Type.STRING);
            propertyCopy = "bean."+prop.toBeanSetter()+"(NumberUtils.makeISO8601TimestampString(((LockableObject)"+doc.getVariableName()+").getLockingTime()) +" +
					" \" till: \" + NumberUtils.makeISO8601TimestampString(((LockableObject)"+doc.getVariableName()+").getLockingTime() + getLockingTimeout()))";
            appendStatement(propertyCopy);
        }

		appendStatement("bean.setDocumentLastUpdateTimestamp(NumberUtils.makeISO8601TimestampString("+doc.getVariableName()+".getLastUpdateTimestamp()))");
	    
	    appendStatement("return bean");
	    closeBlockNEW();
	    return clazz;
	}
	
	///////////////////////////////////////////////////
	////////              SEARCH             //////////
	///////////////////////////////////////////////////
	private GeneratedClass generateSearchAction(MetaModuleSection section){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
	    MetaDocument doc = section.getDocument();
		List<MetaViewElement> elements = section.getElements();
	    
	    clazz.setPackageName(getPackage(section.getModule()));
	    
	    //write imports...
	    clazz.addImport("java.util.List");
	    clazz.addImport("java.util.ArrayList");
	    addStandardActionImports(clazz);
	    clazz.addImport("net.anotheria.anodoc.query2.DocumentQuery");
	    clazz.addImport("net.anotheria.anodoc.query2.string.ContainsStringQuery");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryResult");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryResultEntry");
	    clazz.addImport("net.anotheria.anodoc.query2.ResultEntryBean");
		
		
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			if (element instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)element;
				MetaProperty p = doc.getField(field.getName());
				if (p instanceof MetaEnumerationProperty){
					MetaEnumerationProperty enumeration = (MetaEnumerationProperty)p;
					EnumerationType type = (EnumerationType)GeneratorDataRegistry.getInstance().getType(enumeration.getEnumeration());
					clazz.addImport(EnumTypeGenerator.getEnumImport(type));
				}
				
			}
		}
		

		clazz.setName(getSearchActionName(section));
		clazz.setParent(getBaseActionName(section));

		startClassBody();

	    appendString(getExecuteDeclaration());
	    increaseIdent();
	    
/*	    
	    String listName = doc.getMultiple().toLowerCase();
	    if (section.getFilters().size()>0){
		    String unfilteredListName = "_unfiltered_"+listName;
		    //change this if more than one filter can be triggered at once.
		    MetaFilter activeFilter = section.getFilters().get(0);
		    String filterVarName = getFilterVariableName(activeFilter);
		    appendStatement("List<"+doc.getName()+"> "+unfilteredListName+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getMultiple()+"()");
		    appendStatement("List<"+doc.getName()+"> "+listName+" = new ArrayList<"+doc.getName()+">()");
		    appendString("for (int i=0; i<"+unfilteredListName+".size(); i++)");
		    appendIncreasedString("if ("+filterVarName+".mayPass("+unfilteredListName+".get(i), "+quote(activeFilter.getFieldName())+", filterParameter))");
		    appendIncreasedStatement("\t"+listName+".add("+unfilteredListName+".get(i))");
	    }else{
		    appendStatement("List<"+doc.getName()+"> "+listName+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getMultiple()+"()");
	    }
		appendStatement("List<"+ModuleBeanGenerator.getListItemBeanName(doc)+"> beans = new ArrayList<"+ModuleBeanGenerator.getListItemBeanName(doc)+">("+listName+".size())");
		appendString("for ("+doc.getName()+" "+doc.getVariableName()+" : "+listName+"){");
		increaseIdent();
		appendStatement(ModuleBeanGenerator.getListItemBeanName(doc)+" bean = "+getMakeBeanFunctionName(ModuleBeanGenerator.getListItemBeanName(doc))+"("+doc.getVariableName()+")");
		appendStatement("beans.add(bean)");
	    closeBlockNEW();
	    ret += emptyline();
	    appendStatement("addBeanToRequest(req, "+quote(listName)+", beans)");
	
*/
	    
	    appendStatement("String criteria = req.getParameter("+quote("criteria")+")");
	    //appendStatement("System.out.println("+quote("Criteria: ")+" + criteria)");
	    appendStatement("DocumentQuery query = new ContainsStringQuery(criteria)");
	    appendStatement("QueryResult result = "+getServiceGetterCall(section.getModule())+".executeQueryOn"+doc.getMultiple()+"(query)");
	    //appendStatement("System.out.println("+quote("Result: ")+" + result)");
	    appendString("if (result.getEntries().size()==0){");
	    appendIncreasedStatement("req.setAttribute("+quote("srMessage")+", "+quote("Nothing found.")+")");
	    appendString("}else{");
	    increaseIdent();
	    appendStatement("List<ResultEntryBean> beans = new ArrayList<ResultEntryBean>(result.getEntries().size())");
	    appendString("for (int i=0; i<result.getEntries().size(); i++){");
	    increaseIdent();
	    appendStatement("QueryResultEntry entry = result.getEntries().get(i)");
	    appendStatement("ResultEntryBean bean = new ResultEntryBean()");
	    appendStatement("bean.setEditLink("+quote(CMSMappingsConfiguratorGenerator.getPath(doc, CMSMappingsConfiguratorGenerator.ACTION_EDIT)+"?pId=")+"+entry.getMatchedDocument().getId()+"+quote("&ts=")+"+System.currentTimeMillis())");
	    appendStatement("bean.setDocumentId(entry.getMatchedDocument().getId())");
	    appendStatement("bean.setPropertyName(entry.getMatchedProperty().getId())");
	    appendStatement("bean.setInfo(entry.getInfo().toHtml())");
	    appendStatement("beans.add(bean)");
	    closeBlockNEW();
	    appendStatement("req.setAttribute("+quote("result")+", beans)");
	    closeBlockNEW();
	    
	    appendStatement("return mapping.findForward(\"success\")");
	    closeBlockNEW();
	    return clazz;
	}

	private GeneratedClass generateShowQueryAction(MetaModuleSection section){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		MetaDocument doc = section.getDocument();
	    
		clazz.setPackageName(getPackage(section.getModule()));
		addStandardActionImports(clazz);
		clazz.addImport("net.anotheria.webutils.bean.LabelValueBean");
		clazz.addImport("java.util.List");
		clazz.addImport("java.util.ArrayList");
		clazz.addImport("java.util.Iterator");
	
		List<MetaProperty> links = doc.getLinks();
		
		Set<String> linkTargets = new HashSet<String>(); 

		for (int i=0; i<links.size(); i++){
			MetaLink link = (MetaLink)links.get(i);
			String lt = link.getLinkTarget();
			if (linkTargets.contains(lt))
				continue;
			int dotIndex = lt.indexOf('.');
			if (dotIndex>0){
				String targetModuleName = lt.substring(0,dotIndex);
				String targetDocumentName = lt.substring(dotIndex+1);
				MetaModule mod = GeneratorDataRegistry.getInstance().getModule(targetModuleName);
				MetaDocument targetDocument = mod.getDocumentByName(targetDocumentName);
			
				clazz.addImport(DataFacadeGenerator.getDocumentImport(targetDocument));
			
				linkTargets.add(lt);
			}else{
				//WARN implement relative linking.
			}
		}
		
		
		clazz.setName(getShowQueryActionName(section));
		clazz.setParent(getBaseActionName(section));
		
		startClassBody();
		appendString(getExecuteDeclaration());
		increaseIdent();
		emptyline();
		
		//appendStatement("Iterator it");
		
		linkTargets = new HashSet<String>(); 

		for (int i=0; i<links.size(); i++){
			MetaLink link = (MetaLink)links.get(i);
			String lt = link.getLinkTarget();
			if (linkTargets.contains(lt))
				continue;
			int dotIndex = lt.indexOf('.');
			if (dotIndex>0){
				String targetModuleName = lt.substring(0,dotIndex);
				String targetDocumentName = lt.substring(dotIndex+1);
				MetaModule mod = GeneratorDataRegistry.getInstance().getModule(targetModuleName);
				MetaDocument targetDocument = mod.getDocumentByName(targetDocumentName);
			
				appendStatement("List<"+targetDocument.getName()+"> "+targetDocument.getMultiple().toLowerCase()+" = "+getServiceGetterCall(mod)+".get"+targetDocument.getMultiple()+"()");
				appendStatement("List<LabelValueBean> "+targetDocument.getMultiple().toLowerCase()+"Beans = new ArrayList<LabelValueBean>("+targetDocument.getMultiple().toLowerCase()+".size())");
				appendString("for(Iterator<"+targetDocument.getName()+"> it="+targetDocument.getMultiple().toLowerCase()+".iterator(); it.hasNext(); ){");
				increaseIdent();
				appendStatement(targetDocument.getName()+" "+targetDocument.getVariableName()+" = ("+targetDocument.getName()+") it.next()");
				String beanCreationCall = targetDocument.getMultiple().toLowerCase()+"Beans";
				beanCreationCall+=".add(";
				beanCreationCall+="new LabelValueBean(";
				beanCreationCall+=targetDocument.getVariableName()+".getId(), ";
				beanCreationCall+=targetDocument.getVariableName()+".getName()))";
				appendStatement(beanCreationCall);
				closeBlockNEW();
				appendStatement("addBeanToRequest(req, "+quote(targetDocument.getMultiple().toLowerCase())+", "+targetDocument.getMultiple().toLowerCase()+"Beans)"); 
				emptyline();
				
				linkTargets.add(lt);
			}
			
		}
		
		
		appendStatement("return mapping.findForward(\"success\")");
		closeBlockNEW();
		return clazz;
	}

	private GeneratedClass generateExecuteQueryAction(MetaModuleSection section){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		appendGenerationPoint("generateExecuteQueryAction");
		MetaDocument doc = section.getDocument();
		//List<MetaViewElement> elements = section.getElements();
	    
		clazz.setPackageName(getPackage(section.getModule()));
		addStandardActionImports(clazz);
		clazz.addImport("java.util.List");
		clazz.addImport("java.util.ArrayList");
		clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
		clazz.addImport(ModuleBeanGenerator.getListItemBeanImport(GeneratorDataRegistry.getInstance().getContext(), doc));
		
		clazz.setName(getExecuteQueryActionName(section));
		clazz.setParent(getShowActionName(section));

		startClassBody();
		appendString(getExecuteDeclaration());
		increaseIdent();
		emptyline();
		appendStatement("String property = req.getParameter("+quote("property")+")");
		appendStatement("String criteria = req.getParameter("+quote("criteria")+")");
		appendString("if( criteria!=null && criteria.length()==0)");
		appendIncreasedStatement("criteria = null;");
		//appendStatement("System.out.println(property+\"=\"+criteria)");
		
		String listName = doc.getMultiple().toLowerCase();
		appendStatement("List<"+doc.getName()+"> "+listName+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getMultiple()+"ByProperty(property, criteria)");
		//appendStatement("System.out.println(\"result: \"+"+listName+")");

		appendStatement("List<"+ModuleBeanGenerator.getListItemBeanName(doc)+"> beans = new ArrayList<"+ModuleBeanGenerator.getListItemBeanName(doc)+">("+listName+".size())");
		appendString("for (int i=0; i<"+listName+".size(); i++){");
		increaseIdent();
		appendStatement("beans.add("+getMakeBeanFunctionName(ModuleBeanGenerator.getListItemBeanName(doc))+"(("+doc.getName()+")"+listName+".get(i)))");
		closeBlockNEW();
		emptyline();
		
		appendStatement("return mapping.findForward(\"success\")");
		closeBlockNEW();

		return clazz;
	}


	private String getDecoratorVariableName(MetaViewElement element){
		return element.getName()+"Decorator";
	}
	
	public static String getFilterVariableName(MetaFilter filter){
		return filter.getFieldName()+"Filter"+StringUtils.capitalize(filter.getName());
	}
	
	private String getMakeBeanFunctionName(String beanName){
		return "make"+StringUtils.capitalize(beanName);
	}
	
	private GeneratedClass generateVersionInfoAction(MetaModuleSection section){
		if (USE_MULTIOP_ACTIONS)
			return null;
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		MetaDocument doc = section.getDocument();
		
		clazz.setPackageName(getPackage(section.getModule()));
		addStandardActionImports(clazz);

	    clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
	    clazz.addImport("net.anotheria.util.NumberUtils");

	    
		clazz.setName(getVersionInfoActionName(section));
		clazz.setParent(getBaseActionName(section));
		startClassBody();
	
		generateVersionInfoActionMethod(section, null);
		
		return clazz;
}
	
	private void generateVersionInfoActionMethod(MetaModuleSection section, String methodName){
	    
		MetaDocument doc = section.getDocument();
		appendString( getExecuteDeclaration(methodName));
		increaseIdent();
	
		appendStatement("String id = getStringParameter(req, PARAM_ID)");
		appendStatement(doc.getName()+" "+doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id);");
		//autoUnlocking!
		if (StorageType.CMS.equals(doc.getParentModule().getStorageType()))
			appendStatement("check" + doc.getMultiple() + "(" + doc.getVariableName() + ", req)");
		appendStatement("long timestamp = "+doc.getVariableName()+".getLastUpdateTimestamp()");
		appendStatement("String lastUpdateDate = NumberUtils.makeDigitalDateStringLong(timestamp)");
		appendStatement("lastUpdateDate += \" \"+NumberUtils.makeTimeString(timestamp)");

		try{
			doc.getField("name");
			appendStatement("req.setAttribute("+quote("documentName")+", "+doc.getVariableName()+".getName())");
		}catch(Exception ignored){
			appendStatement("req.setAttribute("+quote("documentName")+", \"Id:\"+"+doc.getVariableName()+".getId())");
		}
		appendStatement("req.setAttribute(",quote("documentType"),", ",doc.getVariableName(),".getClass())");
		appendStatement("req.setAttribute(",quote("lastUpdate"),", lastUpdateDate)");
		
		appendStatement("return mapping.findForward("+quote("success")+")");
	    
		closeBlockNEW(); 
	}
	
	/**
	 * Generates update action, which is called by the dialog to update.
	 * @param section
	 * @return
	 */
	private GeneratedClass generateUpdateAction(MetaModuleSection section){
        //TODO: Locking not supported Here! only in MultiOP!!!
		if (USE_MULTIOP_ACTIONS)
			return null;
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		MetaDocument doc = section.getDocument();
		MetaDialog dialog = section.getDialogs().get(0);
		clazz.setPackageName(getPackage(section.getModule()));
		addStandardActionImports(clazz);
		Context context = GeneratorDataRegistry.getInstance().getContext();
		clazz.addImport(ModuleBeanGenerator.getDialogBeanImport(dialog, doc));
		clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
		clazz.addImport(DataFacadeGenerator.getDocumentFactoryImport(context, doc));
	    
		List<MetaViewElement> elements = createMultilingualList(dialog.getElements(), doc);
		for (int i=0; i<elements.size(); i++){
			MetaViewElement elem = elements.get(i);
			if (elem instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)elem;
				MetaProperty p = doc.getField(field.getName());
				if (p.getType() == MetaProperty.Type.IMAGE){
					clazz.addImport("net.anotheria.webutils.filehandling.actions.FileStorage");
					clazz.addImport("net.anotheria.webutils.filehandling.beans.TemporaryFileHolder");
					break;
				}
			}
		}

		clazz.setName(getUpdateActionName(section));
		clazz.setParent(getBaseActionName(section));
		startClassBody();
		generateUpdateActionMethod(section, null);
		
		return clazz;
	}
	/**
	 * Generates the working part of the update action which is used in both multiop and standalone update action. 
	 * @param section
	 * @param methodName
	 */
	private void generateUpdateActionMethod(MetaModuleSection section, String methodName){
		appendGenerationPoint("generateUpdateActionMethod");
		MetaDocument doc = section.getDocument();
		MetaDialog dialog = section.getDialogs().get(0);
		List<MetaViewElement> elements = createMultilingualList(dialog.getElements(), doc);

		appendString( getExecuteDeclaration(methodName));
		increaseIdent();
	
		appendStatement(ModuleBeanGenerator.getDialogBeanName(dialog, doc)+" form = ("+ModuleBeanGenerator.getDialogBeanName(dialog, doc)+") af");
		//check if we have a form submission at all.
//		appendString( "if (!form.isFormSubmittedFlag())");
//		appendIncreasedStatement("throw new RuntimeException(\"Request broken!\")");
		//if update, then first get the target object.
		appendStatement("boolean create = false");
		appendStatement(doc.getName()+" "+doc.getVariableName()+" = null");


		appendString("if (form.getId()==null) {");
		appendString("res.sendRedirect(\"asresourcedataLocalizationBundleShow?ts=\"+System.currentTimeMillis());");
		appendString("return null;");
		closeBlockNEW();

		appendString( "if (form.getId().length()>0){");
		appendIncreasedString(doc.getVariableName()+" = ("+doc.getName()+")"+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(form.getId()).clone();");
		appendString( "}else{");
		increaseIdent();
		appendString( doc.getVariableName()+" = "+DataFacadeGenerator.getDocumentFactoryName(doc)+".create"+doc.getName()+"();");
		appendString( "create = true;");
		closeBlockNEW();
		emptyline();
		
		appendStatement("String nextAction = req.getParameter("+quote("nextAction")+")");
		appendString( "if (nextAction == null || nextAction.length() == 0)");
		appendIncreasedStatement("nextAction = \"close\"");
		emptyline();
		
		//set fields
		for (int i=0; i<elements.size(); i++){
			MetaViewElement elem = elements.get(i);
			if (elem instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)elem;
				String lang = getElementLanguage(field);
				//System.out.println(ret, "checking field:"+field);
				if (field.isReadonly()){
					appendString( "//skipped "+field.getName()+" because it's readonly.");
				}else{
					MetaProperty p = doc.getField(field.getName());
					//handle images.
					if (p.getType() == MetaProperty.Type.IMAGE){
						//will work with multiple images.
						String varName = p.getName();
						String holderName = "holder_"+varName;
						appendString( "//handle image");
						appendStatement("TemporaryFileHolder "+holderName+" = FileStorage.getTemporaryFile(req,\""+varName+"\")");
						appendString( "if ("+holderName+"!=null && "+holderName+".getData()!=null){");
						increaseIdent();
						appendStatement("FileStorage.removeFilePermanently( "+doc.getVariableName()+"."+p.toGetter()+"() )");
						appendStatement("FileStorage.storeFilePermanently(req, "+holderName+".getFileName(),\""+varName+"\")");
						appendStatement(doc.getVariableName()+"."+p.toSetter()+"("+holderName+".getFileName())");
						appendStatement("FileStorage.removeTemporaryFile(req,\""+varName+"\")");
						closeBlockNEW();
						continue;
					}
					if (! (p instanceof MetaContainerProperty)){
						String propertyCopy = "";
						propertyCopy += doc.getVariableName()+"."+p.toSetter(lang)+"(";
						propertyCopy += "form."+p.toBeanGetter(lang)+"())";
						appendStatement(propertyCopy);
					}else{
						appendString( "// skipped container "+p.getName());
					}
					
				}
			}
		}

        // check if document has property with type IMAGE
        boolean hasImageField = false;
        for (int i=0; i<elements.size(); i++){
            MetaViewElement elem = elements.get(i);
            if (elem instanceof MetaFieldElement){
                MetaFieldElement field = (MetaFieldElement)elem;
                MetaProperty p = doc.getField(field.getName());
                if (p.getType() == MetaProperty.Type.IMAGE){
                    hasImageField = true;
                    break;
                }
            }
        }
        // generating functionality to delete image
        if (hasImageField) {
            emptyline();
            appendString("// delete image method start");
            appendStatement("String fieldName = req.getParameter(\"fieldName\")");
            appendStatement("String fileName = req.getParameter(\"fileName\")");
            emptyline();
            appendString("if (!StringUtils.isEmpty(fieldName) && !StringUtils.isEmpty(fileName)) {");
            increaseIdent();
            appendStatement("String setMethodName = \"set\"+WordUtils.capitalize(fieldName)");
            appendStatement("Class<?> c = "+doc.getVariableName()+".getClass()");
            appendString("try{");
            increaseIdent();
            appendStatement("Method m = c.getDeclaredMethod(setMethodName, new Class[]{String.class})");
            appendStatement("m.invoke("+doc.getVariableName()+", new Object[]{\"\"})");
            appendStatement("FileStorage.removeFilePermanently(fileName)");
            decreaseIdent();
            appendString("}catch (NoSuchMethodException e){");
            increaseIdent();
            appendStatement("e.printStackTrace()");
            decreaseIdent();
            appendString("}catch (InvocationTargetException ignored) {");
            increaseIdent();
            appendStatement("ignored.printStackTrace()");
            decreaseIdent();
            appendString("}");
            decreaseIdent();
            appendString("}");
            appendString("// delete image method end");
            emptyline();
        }
		
		emptyline();
		appendStatement(doc.getName(), " updatedCopy = null");
		
		appendString( "if (create){");
		//appendIncreasedStatement("System.out.println(\"creating\")");
		appendIncreasedStatement("updatedCopy = "+getServiceGetterCall(section.getModule())+".create"+doc.getName()+"("+doc.getVariableName()+")");
		appendString( "}else{");
		 //additional permissions check For Locked Objects!!!!
        if (StorageType.CMS.equals(section.getDocument().getParentModule().getStorageType())) {
		    //This is SomeHow related  to Document Updation! SO next method should be invoked!
           appendIncreasedStatement("canUpdate" + doc.getMultiple() +"("+doc.getVariableName()+", req)" );
		   //autoUnlocking!
		   appendIncreasedStatement("check" + doc.getMultiple() + "("+doc.getVariableName()+", req)");
        }
		appendIncreasedStatement("updatedCopy = "+getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");

		//appendIncreasedStatement("System.out.println(\"updating\")");
		appendString( "}");
		appendString( "if (nextAction.equalsIgnoreCase("+quote("stay")+")){");

	    appendIncreasedStatement("res.sendRedirect("+getEditActionRedirect(doc)+"+"+quote("&pId=")+"+updatedCopy.getId())");
		appendString( "}else{");
		if (StorageType.CMS.equals(section.getDocument().getParentModule().getStorageType())) {
			//unlocking document
			appendIncreasedStatement("unlockAfterUpdate("+doc.getVariableName()+", req)");
		}
	    appendIncreasedStatement("res.sendRedirect("+getShowActionRedirect(doc)+")");
		appendString("}");
	    appendStatement("return null");
		closeBlockNEW();

		if (StorageType.CMS.equals(section.getDocument().getParentModule().getStorageType())) {
			appendComment("Simply unlocks document after updation.");
			appendString("private void unlockAfterUpdate(" + doc.getName() + " " + doc.getVariableName() + ", HttpServletRequest req) throws Exception{");
			increaseIdent();
			appendString("if(((LockableObject)" + doc.getVariableName() + ").isLocked())");
			appendIncreasedStatement("unLock" + doc.getMultiple() + "(" + doc.getVariableName() + ", req, false)");
			closeBlockNEW();
		}
	}
	
	/**
	 * Generates the switch multilinguality action which switches the multi language support for a single document on and off.
	 * @param section
	 * @return
	 */
	private GeneratedClass generateSwitchMultilingualityAction(MetaModuleSection section){
		if (USE_MULTIOP_ACTIONS)
			return null;
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		MetaDocument doc = section.getDocument();
		clazz.setPackageName(getPackage(section.getModule()));

		//write imports...
		addStandardActionImports(clazz);
	    clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
	    clazz.addImport("net.anotheria.asg.data.MultilingualObject");

	    clazz.setTypeComment("This class enables or disables support for multiple languages for a particular document.");
		clazz.setName(getSwitchMultilingualityActionName(section));
		clazz.setParent(getBaseActionName(section));

		startClassBody();
		generateSwitchMultilingualityActionMethod(section, null);
		return clazz;
	}
	
	/**
	 * Generates the working part of the switch multilinguality action.
	 * @param section
	 * @param methodName
	 */
	private void generateSwitchMultilingualityActionMethod(MetaModuleSection section, String methodName){
	    
		MetaDocument doc = section.getDocument();
	    
		appendString( getExecuteDeclaration(methodName));
		increaseIdent();
		
		appendStatement("String id = getStringParameter(req, PARAM_ID)");
		appendStatement("String value = getStringParameter(req, "+quote("value")+")");

		appendStatement(doc.getName()+" "+doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");
        if(StorageType.CMS.equals(section.getDocument().getParentModule().getStorageType())){
          // appendString("if("+doc.getVariableName()+" instanceof LockableObject){ ");
          // appendStatement("LockableObject lockable = (LockableObject)" + doc.getVariableName());
		  //This is SomeHow related  to Document Updation! SO next method should be invoked!
           appendStatement("canUpdate" + doc.getMultiple() +"("+doc.getVariableName()+", req)" );
		   //autoUnlocking!
		   appendStatement("check" + doc.getMultiple() + "("+doc.getVariableName()+", req)");
          // appendString("}");
        }
		appendStatement("((MultilingualObject)"+doc.getVariableName()+").setMultilingualDisabledInstance(Boolean.valueOf(value))");
		appendStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
	    appendStatement("res.sendRedirect("+getEditActionRedirect(doc)+"+"+quote("&pId=")+"+id)");
		
	    appendStatement("return null");
		closeBlockNEW(); //end doExecute
	}

	/**
	 * Generates the language copy action which allows copying content from one language to another on per-document base.
	 * @param section
	 * @return
	 */
	private GeneratedClass generateLanguageCopyAction(MetaModuleSection section){
		if (USE_MULTIOP_ACTIONS)
			return null;
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		MetaDocument doc = section.getDocument();
		clazz.setPackageName(getPackage(section.getModule()));

		//write imports...
		addStandardActionImports(clazz);
	    clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
	    
	    clazz.setTypeComment("This class copies multilingual contents from one language to another in a given document");
		clazz.setName(getLanguageCopyActionName(section));
		clazz.setParent(getBaseActionName(section));

		startClassBody();
		generateLanguageCopyActionMethod(section, null);
		
		return clazz;
	}
	
	/**
	 * Generates the working part of the language copy action.
	 * @param section
	 * @param methodName
	 */
	private void generateLanguageCopyActionMethod(MetaModuleSection section, String methodName){
	    
		MetaDocument doc = section.getDocument();
		appendString( getExecuteDeclaration(methodName));
		increaseIdent();
		
		appendStatement("String sourceLanguage = req.getParameter("+quote("pSrcLang")+")");
		appendString( "if (sourceLanguage==null || sourceLanguage.length()==0)");
		appendIncreasedStatement("throw new RuntimeException("+quote("No source language")+")");
		emptyline();

		appendStatement("String destLanguage = req.getParameter("+quote("pDestLang")+")");
		appendString( "if (destLanguage==null || destLanguage.length()==0)");
		appendIncreasedStatement("throw new RuntimeException("+quote("No destination language")+")");
		emptyline();

		appendStatement("String id = getStringParameter(req, PARAM_ID)");
		appendStatement(doc.getName()+" "+doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");
         if(StorageType.CMS.equals(section.getDocument().getParentModule().getStorageType())){

           //appendIncreasedStatement("LockableObject lockable = (LockableObject)" + doc.getVariableName());
		   //This is SomeHow related  to Document Updation! SO next method should be invoked!
           appendStatement("canUpdate" + doc.getMultiple() +"("+doc.getVariableName()+", req)" );
		   //autoUnlocking!
		   appendStatement("check" + doc.getMultiple() + "("+doc.getVariableName()+", req)");

        }
		appendStatement(doc.getVariableName()+"."+DataFacadeGenerator.getCopyMethodName()+"(sourceLanguage, destLanguage)");
		appendStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
	    appendStatement("res.sendRedirect("+getEditActionRedirect(doc)+"+"+quote("&pId=")+"+id)");
		
	    appendStatement("return null");
		closeBlockNEW(); //end doExecute
	}

	/**
	 * Generates the edit action which presents a document for editing in the edit dialog.
	 * @param section
	 * @return
	 */
	private GeneratedClass generateEditAction(MetaModuleSection section){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		appendGenerationPoint("generateEditAction");
		
		MetaDocument doc = section.getDocument();
		MetaDialog dialog = section.getDialogs().get(0);
		List<MetaViewElement> elements = createMultilingualList(dialog.getElements(), doc);
		
		clazz.setPackageName(getPackage(section.getModule()));
		addStandardActionImports(clazz);
		clazz.addImport(ModuleBeanGenerator.getDialogBeanImport(dialog, doc));
		clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
		clazz.addImport("net.anotheria.asg.util.helper.cmsview.CMSViewHelperUtil");
		clazz.addImport("net.anotheria.asg.util.helper.cmsview.CMSViewHelperRegistry");
		if (doc.isMultilingual())
			clazz.addImport("net.anotheria.asg.data.MultilingualObject");
		
		//check if we have to import list.
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			if (element instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)element;
				MetaProperty p = doc.getField(field.getName());
				if (p.isLinked() || p instanceof MetaEnumerationProperty){
					clazz.addImport("java.util.List");
					clazz.addImport("java.util.ArrayList");
					clazz.addImport("net.anotheria.webutils.bean.LabelValueBean");
					break;
				}
			}
		}
		
		//check if we have to property definition files.
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			if (element instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)element;
				MetaProperty p = doc.getField(field.getName());
				if (p instanceof MetaEnumerationProperty){
					MetaEnumerationProperty mep = (MetaEnumerationProperty)p;
					EnumerationType type = (EnumerationType)GeneratorDataRegistry.getInstance().getType(mep.getEnumeration());
					clazz.addImport(EnumTypeGenerator.getEnumImport(type));
				}
			}
		}
		
	    List<DirectLink> backlinks = GeneratorDataRegistry.getInstance().findLinksToDocument(doc);
	    if (backlinks.size()>0){
	    	clazz.addImport("net.anotheria.anodoc.query2.QueryProperty");
	    	clazz.addImport("net.anotheria.asg.util.bean.LinkToMeBean");
	    	clazz.addImport("java.util.List");
	    	clazz.addImport("java.util.ArrayList");
	    	for (DirectLink l : backlinks){
    			clazz.addImport(DataFacadeGenerator.getDocumentImport(l.getDocument()));
	    	}
	    }
        //LockableObject import!!!
        if(StorageType.CMS.equals(doc.getParentModule().getStorageType())){
           clazz.addImport("net.anotheria.asg.data.LockableObject");
        }
		
		startClassBody();
		
		clazz.setName(getEditActionName(section));
		clazz.setParent(getShowActionName(section));

		startClassBody();
		generateEditActionMethod(clazz, section, "anoDocExecute");

		return clazz;

	}

	/**
	 * Generates the working part of the edit action.
	 * @param section
	 * @return
	 */
	private void generateEditActionMethod(GeneratedClass clazz, MetaModuleSection section, String methodname){
		appendGenerationPoint("generateEditActionMethod");

		MetaDocument doc = section.getDocument();
		MetaDialog dialog = section.getDialogs().get(0);
		List<MetaViewElement> elements = createMultilingualList(dialog.getElements(), doc);
		EnumerationPropertyGenerator enumProGenerator = new EnumerationPropertyGenerator(doc);
	    List<DirectLink> backlinks = GeneratorDataRegistry.getInstance().findLinksToDocument(doc);
		
		appendString( getExecuteDeclaration(methodname));
		increaseIdent();
	
		appendStatement("String id = getStringParameter(req, PARAM_ID)");
		appendStatement(ModuleBeanGenerator.getDialogBeanName(dialog, doc), " form = new ", ModuleBeanGenerator.getDialogBeanName(dialog, doc), "() ");	

		appendStatement(doc.getName()," ",doc.getVariableName()," = ",getServiceGetterCall(section.getModule()),".get",doc.getName(),"(id)");
        final boolean isCMS = StorageType.CMS.equals(doc.getParentModule().getStorageType());
		if(isCMS){
			//autoUnlocking!
		    appendStatement("check" + doc.getMultiple() + "("+doc.getVariableName()+", req)");
			//autoLocking! - actually!
			appendString("if("+doc.getVariableName()+" instanceof LockableObject && !((LockableObject)"+doc.getVariableName()+").isLocked() && isAutoLockingEnabled())");
			appendIncreasedStatement("lock" + doc.getMultiple() + "("+doc.getVariableName()+", req)");
		}
		
		//set field
		for (int i=0; i<elements.size(); i++){
			MetaViewElement elem = elements.get(i);
			//System.out.println("checking elem:"+elem);
			if (elem instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)elem;
				MetaProperty p = doc.getField(field.getName());
				if (p instanceof MetaContainerProperty){
					appendString( "// "+p.getName()+" is a table, storing size only");
					String lang = getElementLanguage(elem);
					appendStatement("form."+p.toBeanSetter(lang)+"("+doc.getVariableName()+"."+DataFacadeGenerator.getContainerSizeGetterName((MetaContainerProperty)p, lang)+"())");
				}else{
					String lang = getElementLanguage(elem);
					String propertyCopy = "";
					propertyCopy += "form."+p.toBeanSetter(lang)+"(";
					propertyCopy += doc.getVariableName()+"."+p.toGetter(lang)+"())";
					appendStatement(propertyCopy);
				}
			}
		}
		
		if (doc.isMultilingual()){
			MetaProperty p = doc.getField(ModuleBeanGenerator.FIELD_ML_DISABLED);
			String propertyCopy = "form."+p.toBeanSetter()+"(((MultilingualObject)"+doc.getVariableName()+").isMultilingualDisabledInstance())";
			appendStatement(propertyCopy);
		}

        //adding additional Lock properties

		if(isCMS){
            MetaProperty prop = new MetaProperty(LockableObject.INT_LOCK_PROPERTY_NAME,MetaProperty.Type.BOOLEAN);
            String propertyCopy = "form."+prop.toBeanSetter()+"(((LockableObject)"+doc.getVariableName()+").isLocked())";
            appendStatement(propertyCopy);
            prop =  new MetaProperty(LockableObject.INT_LOCKER_ID_PROPERTY_NAME,MetaProperty.Type.STRING);
            propertyCopy = "form."+prop.toBeanSetter()+"(((LockableObject)"+doc.getVariableName()+").getLockerId())";
            appendStatement(propertyCopy);
            prop =  new MetaProperty(LockableObject.INT_LOCKING_TIME_PROPERTY_NAME,MetaProperty.Type.STRING);
		    propertyCopy = "form."+prop.toBeanSetter()+"(net.anotheria.util.NumberUtils.makeISO8601TimestampString(((LockableObject)"+doc.getVariableName()+").getLockingTime()) +" +
					" \" automatic unlock expected AT : \" + net.anotheria.util.NumberUtils.makeISO8601TimestampString(((LockableObject)"+doc.getVariableName()+").getLockingTime() + getLockingTimeout()))";

            appendStatement(propertyCopy);
        }
		
		emptyline();
		
		Set<String> linkTargets = new HashSet<String>();
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			if (element instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)element;
				MetaProperty p = doc.getField(field.getName());
				if (p.isLinked()){
					MetaLink link = (MetaLink)p;
					
					MetaModule targetModule = link.getLinkTarget().indexOf('.')== -1 ?
							doc.getParentModule() : GeneratorDataRegistry.getInstance().getModule(link.getTargetModuleName());
					if (targetModule == null){
						throw new RuntimeException("Can't resolve link: "+p+" in document "+doc.getName()+" and dialog "+dialog.getName());
					}
					String tDocName = link.getTargetDocumentName(); 
					MetaDocument targetDocument = targetModule.getDocumentByName(tDocName);
					String listName = targetDocument.getMultiple().toLowerCase();
					emptyline();
					
					if (linkTargets.contains(link.getLinkTarget())){
						appendString( "//reusing collection for "+link.getName()+" to "+link.getLinkTarget()+".");
					}else{
					
						appendString( "//link "+link.getName()+" to "+link.getLinkTarget());
						clazz.addImport(DataFacadeGenerator.getDocumentImport(targetDocument));
						appendStatement("List<"+ targetDocument.getName() +"> "+listName+" = "+getServiceGetterCall(targetModule)+".get"+targetDocument.getMultiple()+"()");						
						appendStatement("List<LabelValueBean> "+listName+"Values = new ArrayList<LabelValueBean>("+listName+".size()+1)");
						appendStatement(listName+"Values.add(new LabelValueBean("+quote("")+", \"-----\"))");
						appendString( "for ("+(DataFacadeGenerator.getDocumentImport(targetDocument))+" "+targetDocument.getVariableName()+"Temp : "+listName+"){");
						increaseIdent();
						
						String linkDecorationStr = generateLinkDecoration(targetDocument, link);
						appendStatement("LabelValueBean bean = new LabelValueBean("+targetDocument.getVariableName()+"Temp.getId(), " + linkDecorationStr + " )");
						appendStatement(listName,"Values.add(bean)");
						closeBlockNEW();

					}

					String lang = getElementLanguage(element);
					appendStatement("form."+p.toBeanSetter()+"Collection"+(lang==null ? "":lang)+"("+listName+"Values"+")");
					
					appendString( "try{");
					increaseIdent();
					String getter = getServiceGetterCall(targetModule)+".get"+targetDocument.getName()+"("+doc.getVariableName()+"."+p.toGetter()+"()).getName()";

					String getterUnknown = getServiceGetterCall(targetModule)+".get"+targetDocument.getName()+"("+doc.getVariableName()+"."+p.toGetter()+"())";


                    if (doc.getName().equalsIgnoreCase("box")) {
                        if (element.getName().equalsIgnoreCase("handler")) {
                            appendString("if ( "+getterUnknown+" instanceof BoxHandlerDef ) {");
                            appendIncreasedStatement("String " + p.getName() + "Id = (String)" + getterUnknown + ".getId()");
                            appendIncreasedStatement("int index = " + p.getName() + "Id.indexOf(\"C-\")");
                            appendIncreasedString("if ( index != -1 ) {");
                            increaseIdent();
                            appendIncreasedStatement("form."+p.toBeanSetter()+"IdOfCurrentValue"+(lang==null ? "":lang)+"( "+p.getName()+"Id.substring(index+2) )");
                            decreaseIdent();
                            appendIncreasedString("} else {");
                            increaseIdent();
                            appendIncreasedStatement("form."+p.toBeanSetter()+"IdOfCurrentValue"+(lang==null ? "":lang)+"( \"none\" )");
                            decreaseIdent();
                            appendIncreasedString("}");

                            appendString("}");
                        }
                        if (element.getName().equalsIgnoreCase("type")) {
                            appendString("if ( "+getterUnknown+" instanceof BoxType ) {");
                            appendIncreasedStatement("String " + p.getName() + "Id = (String)" + getterUnknown + ".getId()");
                            appendIncreasedStatement("int index = " + p.getName() + "Id.indexOf(\"C-\")");
                            appendIncreasedString("if ( index != -1 ) {");
                            increaseIdent();
                            appendIncreasedStatement("form."+p.toBeanSetter()+"IdOfCurrentValue"+(lang==null ? "":lang)+"( "+p.getName()+"Id.substring(index+2) )");
                            decreaseIdent();
                            appendIncreasedString("} else {");
                            increaseIdent();
                            appendIncreasedStatement("form."+p.toBeanSetter()+"IdOfCurrentValue"+(lang==null ? "":lang)+"( \"none\" )");
                            decreaseIdent();
                            appendIncreasedString("}");
                            appendString("}");
                        }
                    }

					appendStatement("form."+p.toBeanSetter()+"CurrentValue"+(lang==null ? "":lang)+"("+getter+")");

					decreaseIdent();
					appendString( "}catch(Exception e){");
					appendIncreasedStatement("form."+p.toBeanSetter()+"CurrentValue"+(lang==null ? "":lang)+"("+quote("none")+")");
					appendIncreasedStatement("form."+p.toBeanSetter()+"IdOfCurrentValue"+(lang==null ? "":lang)+"("+quote("none")+")");
					appendString( "}");
					linkTargets.add(link.getLinkTarget());
					
				}
				
				if (p instanceof MetaEnumerationProperty){
				    enumProGenerator.generateEnumerationPropertyHandling((MetaEnumerationProperty)p, true);
				}
			}
		}
		
		
		appendStatement("addBeanToRequest(req, "+quote("objectId")+" , "+doc.getVariableName()+".getId())");
		appendStatement("addBeanToRequest(req, "+quote(CMSMappingsConfiguratorGenerator.getDialogFormName(dialog, doc))+" , form)");
		appendStatement("addBeanToRequest(req, "+quote("objectInfoString")+" , "+doc.getVariableName()+".getObjectInfo().toString())");
		appendStatement("addBeanToRequest(req, "+quote("apply.label.prefix")+", "+quote("Apply")+")");
		appendStatement("addBeanToRequest(req, "+quote("save.label.prefix")+", "+quote("Save")+")");
		
		//add field descriptions ...
		emptyline();
		appendStatement("addFieldExplanations(req, "+doc.getVariableName()+")");
		emptyline();
		
	    if (backlinks.size()>0){
			emptyline();
			appendCommentLine("Generating back link handling...");
	    	appendStatement("List<LinkToMeBean> linksToMe = findLinksToCurrentDocument("+doc.getVariableName()+".getId())");
	    	appendString( "if (linksToMe.size()>0)");
	    	appendIncreasedStatement("req.setAttribute("+quote("linksToMe")+", linksToMe)");
	    }

		
		appendStatement("return mapping.findForward(\"success\")");
		closeBlockNEW(); 
		emptyline();
		
		appendAddFieldExplanationsMethod(doc);
		emptyline();
		
		Context context = GeneratorDataRegistry.getInstance().getContext();
		
	    //backlinks
		if (backlinks.size()>0){
			appendString( "private List<LinkToMeBean> findLinksToCurrentDocument(String documentId){");
			increaseIdent();
			appendStatement("List<LinkToMeBean> ret = new ArrayList<LinkToMeBean>()");
			for (DirectLink l : backlinks){
				appendString( "try{");
				String methodName = "";
				if (l.getProperty().isMultilingual()){
					for (String lang : context.getLanguages()){
						methodName = "findLinkToCurrentDocumentIn"+l.getModule().getName()+l.getDocument().getName()+StringUtils.capitalize(l.getProperty().getName(lang));
						appendIncreasedStatement("ret.addAll("+methodName+"(documentId))");
					} 
				}else{
					methodName = "findLinkToCurrentDocumentIn"+l.getModule().getName()+l.getDocument().getName()+StringUtils.capitalize(l.getProperty().getName());
					appendIncreasedStatement("ret.addAll("+methodName+"(documentId))");	
				}
				appendString( "}catch(Exception ignored){");
				//check!!!
				//				appendIncreasedStatement("log.warn(\""+methodName+"(\"+documentId+\")\", ignored)");
				appendString( "}");
			}
			appendStatement("return ret");
			closeBlockNEW();
			
			for (DirectLink l : backlinks){
				if (l.getProperty().isMultilingual()){
					for (String lang : context.getLanguages()){
						appendString( "private List<LinkToMeBean> findLinkToCurrentDocumentIn"+l.getModule().getName()+l.getDocument().getName()+StringUtils.capitalize(l.getProperty().getName(lang))+"(String documentId) throws "+ServiceGenerator.getExceptionImport(l.getModule())+"{");
						increaseIdent();
						appendStatement("List<LinkToMeBean> ret = new ArrayList<LinkToMeBean>()");
						appendStatement("QueryProperty p = new QueryProperty("+l.getDocument().getName()+"."+l.getProperty().toNameConstant(lang)+", documentId)");
						//appendStatement("List<"+l.getDocument().getName()+"> list = "+getServiceGetterCall(l.getModule())+".get"+l.getDocument().getMultiple()+"ByProperty(p)");
						appendCommentLine("temporarly - replacy with query property");
						appendStatement("List<"+l.getDocument().getName()+"> list = "+getServiceGetterCall(l.getModule())+".get"+l.getDocument().getMultiple()+"ByProperty(p.getName(), p.getValue())");
						appendString( "for ("+l.getDocument().getName() +" doc : list ){");
						increaseIdent();
						appendStatement("ret.add(new LinkToMeBean(doc, "+quote(l.getProperty().getName())+"))");
						closeBlockNEW();
						appendStatement("return ret");
						closeBlockNEW();
					}
				}else{
					appendString( "private List<LinkToMeBean> findLinkToCurrentDocumentIn"+l.getModule().getName()+l.getDocument().getName()+StringUtils.capitalize(l.getProperty().getName())+"(String documentId) throws "+ServiceGenerator.getExceptionImport(l.getModule())+"{");
					increaseIdent();
					appendStatement("List<LinkToMeBean> ret = new ArrayList<LinkToMeBean>()");
					appendStatement("QueryProperty p = new QueryProperty("+l.getDocument().getName()+"."+l.getProperty().toNameConstant()+", documentId)");
					//appendStatement("List<"+l.getDocument().getName()+"> list = "+getServiceGetterCall(l.getModule())+".get"+l.getDocument().getMultiple()+"ByProperty(p)");
					appendCommentLine("temporarly - replacy with query property");
					appendStatement("List<"+l.getDocument().getName()+"> list = "+getServiceGetterCall(l.getModule())+".get"+l.getDocument().getMultiple()+"ByProperty(p.getName(), p.getValue())");
					appendString( "for ("+l.getDocument().getName() +" doc : list ){");
					increaseIdent();
					appendStatement("ret.add(new LinkToMeBean(doc, "+quote(l.getProperty().getName())+"))");
					closeBlockNEW();
					appendStatement("return ret");
					closeBlockNEW();
				}
			}
		}
	}
	
	private String generateLinkDecoration(MetaDocument doc, MetaLink link){
		String tDocName = link.getTargetDocumentName();
		
		MetaModule targetModule = link.getLinkTarget().indexOf('.')== -1 ? doc.getParentModule(): GeneratorDataRegistry.getInstance().getModule(link.getTargetModuleName());
		MetaDocument targetDocument = targetModule.getDocumentByName(tDocName);
		
		String linkDecorationStr = "";
		boolean first = true;
		for(String decoration: link.getLinkDecoration()){
			if(!first)
				linkDecorationStr += " + \" - \" + ";
			first = false;
			linkDecorationStr += targetDocument.getVariableName()+ "Temp.get"+StringUtils.capitalize(decoration)+"()";
		}
		
		linkDecorationStr += " + \" [\" + " + targetDocument.getVariableName()+ "Temp.getId() + \"]\"";
		
		return linkDecorationStr;
	}

	private GeneratedClass generateDeleteAction(MetaModuleSection section){
        //TODO: Locking not supported Here! only in MultiOP!!!
		if (USE_MULTIOP_ACTIONS)
			return null;
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		clazz.setPackageName(getPackage(section.getModule()));

	    //write imports...
	    addStandardActionImports(clazz);
	    clazz.setName(getDeleteActionName(section));
	    clazz.setParent(getBaseActionName(section));
		
	    startClassBody();
	    generateDeleteActionMethod(section, null);
	    
	    return clazz;
	}

	private void generateDeleteActionMethod(MetaModuleSection section, String methodName){

		MetaDocument doc = section.getDocument();
	    appendString( getExecuteDeclaration(methodName));
	    increaseIdent();
	    //appendStatement("String id = getStringParameter(req, PARAM_ID)");
		//todo formating of generated code is wrong
	    appendStatement("String[] iDs = req.getParameterValues(PARAM_ID)");
		appendString("if (iDs == null){");
		appendStatement("throw new RuntimeException(\"Parameter \" + PARAM_ID + \" is not set.\")");
		closeBlockNEW();
		appendString("for (String id : iDs){");
		increaseIdent();
			appendStatement(doc.getName()+" "+doc.getVariableName()+"Curr = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");
         if(StorageType.CMS.equals(section.getDocument().getParentModule().getStorageType())){
			appendString("if ("+doc.getVariableName()+"Curr instanceof LockableObject){ ");
			appendStatement("LockableObject lockable = (LockableObject)" + doc.getVariableName() + "Curr");
			//Actually We does not Care - about admin role in Delete action!  So checkExecutionPermission  2-nd parameter  can be anything!
			appendStatement("DocumentLockingHelper.delete.checkExecutionPermission(lockable, false, getUserId(req))");
			appendString("}");
        }
	    appendStatement(getServiceGetterCall(section.getModule())+".delete"+doc.getName()+"(id)");
		//delete images from file system
		for (MetaProperty property : section.getDocument().getProperties()){
			if ( property.getType() == MetaProperty.Type.IMAGE)
				appendStatement("FileStorage.removeFilePermanently( "+doc.getVariableName()+"Curr."+property.toGetter()+"() )");
			if (property instanceof MetaListProperty){
				MetaListProperty mlp = (MetaListProperty)property;
				MetaProperty containerProperty = mlp.getContainedProperty();
				if (containerProperty.getType() == MetaProperty.Type.IMAGE){
					appendString("for ( String image : " + doc.getVariableName() + "Curr." + property.toGetter() + "() )");
					appendStatement("FileStorage.removeFilePermanently(image)");
				}
			}
		}

		closeBlockNEW();
		appendStatement("res.sendRedirect("+getShowActionRedirect(doc)+")");
	    appendStatement("return null");
	    closeBlockNEW();
	    
	}

	private GeneratedClass generateDuplicateAction(MetaModuleSection section){
		if (USE_MULTIOP_ACTIONS)
			return null;
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		MetaDocument doc = section.getDocument();
		clazz.setPackageName(getPackage(section.getModule()));

		//write imports...
		addStandardActionImports(clazz);
		clazz.addImport(DataFacadeGenerator.getDocumentFactoryImport(GeneratorDataRegistry.getInstance().getContext(), doc));
		clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
		clazz.setName(getDuplicateActionName(section));
		clazz.setParent(getBaseActionName(section));

		startClassBody();
		generateDuplicateActionMethod(section, null);
		return clazz;
	    
	}
	
	private void generateDuplicateActionMethod(MetaModuleSection section, String methodName){
	    
		MetaDocument doc = section.getDocument();
		appendString(getExecuteDeclaration(methodName));
		increaseIdent();
		appendStatement("String id = getStringParameter(req, PARAM_ID)");
		appendStatement(doc.getName()+" "+doc.getVariableName()+"Src = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");
		appendStatement(doc.getName()+" "+doc.getVariableName()+"Dest = "+DataFacadeGenerator.getDocumentFactoryName(doc)+".create"+doc.getName()+"("+doc.getVariableName()+"Src)");
		emptyline();

		//clone images from file system
		for (MetaProperty property : section.getDocument().getProperties()){
			if ( property.getType() == MetaProperty.Type.IMAGE)
				appendStatement(doc.getVariableName()+"Dest."+property.toSetter()+"(FileStorage.cloneFilePermanently( "+doc.getVariableName()+"Dest."+property.toGetter()+"() ))");

			if (property instanceof MetaListProperty){
				emptyline();
				MetaListProperty mlp = (MetaListProperty)property;
				MetaProperty containerProperty = mlp.getContainedProperty();
				if (containerProperty.getType() == MetaProperty.Type.IMAGE) {
					String listName = "newImages" + property.getName();
					appendStatement("List<String> " + listName + " = new ArrayList<String>()");
					appendString("for ( String image : " + doc.getVariableName() + "Dest." + property.toGetter() + "() )");
					appendStatement(listName+".add(FileStorage.cloneFilePermanently(image))");
					appendStatement(doc.getVariableName() + "Dest." + property.toSetter() + "("+listName+")");
				}
			}
		}
		emptyline();
		appendStatement(doc.getName()+" "+doc.getVariableName()+"Created = "+getServiceGetterCall(section.getModule())+".create"+doc.getName()+"("+doc.getVariableName()+"Dest"+")");
	    appendStatement("res.sendRedirect("+getEditActionRedirect(doc)+"+"+quote("&")+"+PARAM_ID+"+quote("=")+"+"+doc.getVariableName()+"Created.getId()"+")");
		emptyline();
	    appendStatement("return null");
	    closeBlockNEW();
	}

	private GeneratedClass generateNewAction(MetaModuleSection section){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		
		MetaDocument doc = section.getDocument();
		MetaDialog dialog = section.getDialogs().get(0);
		//List<MetaViewElement> elements = dialog.getElements();
		List<MetaViewElement> elements = createMultilingualList(dialog.getElements(), doc);
		
//		EnumerationPropertyGenerator enumPropGen = new EnumerationPropertyGenerator(doc);
		
		clazz.setPackageName(getPackage(section.getModule()));
	    
		//write imports...
		addStandardActionImports(clazz);
		clazz.addImport(ModuleBeanGenerator.getDialogBeanImport(dialog, doc));
		clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
		clazz.addImport("net.anotheria.asg.util.helper.cmsview.CMSViewHelperUtil");
		clazz.addImport("net.anotheria.asg.util.helper.cmsview.CMSViewHelperRegistry");
	    
		//check if we have to import list.
		for (MetaViewElement element : elements) {
			if (element instanceof MetaFieldElement) {
				MetaFieldElement field = (MetaFieldElement) element;
				MetaProperty p = doc.getField(field.getName());
				if (p.isLinked() || p instanceof MetaEnumerationProperty) {
					clazz.addImport("java.util.List");
					clazz.addImport("java.util.ArrayList");
					clazz.addImport("net.anotheria.webutils.bean.LabelValueBean");
				}
			}
		}

		//check if we have to property definition files.
//		HashMap<String, MetaEnumerationProperty> importedEnumerations = new HashMap<String, MetaEnumerationProperty>();

        for (MetaViewElement element : elements) {
            if (element instanceof MetaFieldElement) {
                MetaFieldElement field = (MetaFieldElement) element;
                MetaProperty p = doc.getField(field.getName());
                if (p instanceof MetaEnumerationProperty) {
                    MetaEnumerationProperty mep = (MetaEnumerationProperty) p;
                    EnumerationType type = (EnumerationType) GeneratorDataRegistry.getInstance().getType(mep.getEnumeration());
                    clazz.addImport(EnumTypeGenerator.getEnumImport(type));
                }
            }
        }
		

		clazz.setName(getNewActionName(section));
		clazz.setParent(getShowActionName(section));
		
		
		startClassBody();
		appendString( getExecuteDeclaration());
		increaseIdent();
	
		appendStatement(ModuleBeanGenerator.getDialogBeanName(dialog, doc)+" form = new "+ModuleBeanGenerator.getDialogBeanName(dialog, doc)+"() ");	
		appendStatement("form.setId("+quote("")+")");
		
//		Set<String> linkTargets = new HashSet<String>();
//
//		for (MetaViewElement element : elements) {
//			if (element instanceof MetaFieldElement) {
//				MetaFieldElement field = (MetaFieldElement) element;
//				MetaProperty p = doc.getField(field.getName());
//				if (p.isLinked()) {
//					MetaLink link = (MetaLink) p;
//
//					MetaModule targetModule = link.isRelative() ?
//							doc.getParentModule() :
//							GeneratorDataRegistry.getInstance().getModule(link.getTargetModuleName());
//					String tDocName = link.getTargetDocumentName();
//					MetaDocument targetDocument = targetModule.getDocumentByName(tDocName);
//					String listName = targetDocument.getMultiple().toLowerCase();
//					emptyline();
//
//					if (linkTargets.contains(link.getLinkTarget())) {
//						appendString("//link " + link.getName() + " to " + link.getLinkTarget() + " reuses collection.");
//					} else {
//						appendString("//link " + link.getName() + " to " + link.getLinkTarget());
//						appendStatement("List<" + DataFacadeGenerator.getDocumentImport(targetDocument) + "> " + listName + " = " + getServiceGetterCall(targetModule) + ".get" + targetDocument.getMultiple() + "()");
//						appendStatement("List<LabelValueBean> " + listName + "Values = new ArrayList<LabelValueBean>(" + listName + ".size()+1)");
//						appendStatement(listName + "Values.add(new LabelValueBean(" + quote("") + ", \"-----\"))");
//						appendString("for (" + (DataFacadeGenerator.getDocumentImport(targetDocument)) + " " + targetDocument.getVariableName() + " : " + listName + "){");
//						increaseIdent();
//
//						appendStatement("LabelValueBean bean = new LabelValueBean(" + targetDocument.getVariableName() + ".getId(), " + targetDocument.getVariableName() + ".getName() )");
//						appendStatement(listName + "Values.add(bean)");
//						closeBlockNEW();
//					}
//
//					String lang = getElementLanguage(element);
//					appendStatement("form." + p.toBeanSetter() + "Collection" + (lang == null ? "" : lang) + "(" + listName + "Values" + ")");
//					linkTargets.add(link.getLinkTarget());
//				}//...end if (p.isLinked())
//
//				if (p instanceof MetaEnumerationProperty) {
//					enumPropGen.generateEnumerationPropertyHandling((MetaEnumerationProperty) p, false);
//				}
//
//			}
//		}
		appendPrepareFormForEditView(elements, doc, true);
		
		emptyline();
		appendStatement("addBeanToRequest(req, "+quote(CMSMappingsConfiguratorGenerator.getDialogFormName(dialog, doc))+" , form)");
		appendStatement("addBeanToRequest(req, "+quote("save.label.prefix")+", "+quote("Save")+")");
		appendStatement("addBeanToRequest(req, "+quote("apply.label.prefix")+" , "+quote("Apply")+")");
		appendStatement("addBeanToRequest(req, "+quote("objectInfoString")+" , "+quote("none")+")");
		
		//add field descriptions ...
		emptyline();
		appendStatement("addFieldExplanations(req, null)");	
		emptyline();
		
		appendStatement("return mapping.findForward(\"success\")");
		closeBlock("");
		
		emptyline();
		appendAddFieldExplanationsMethod(doc);
		
		return clazz;
	}
	
	private void appendPrepareFormForEditView(List<MetaViewElement> elements, MetaDocument doc, boolean newDocument) {
		Set<String> linkTargets = new HashSet<String>();
		EnumerationPropertyGenerator enumPropGen = new EnumerationPropertyGenerator(doc);
		for (MetaViewElement element : elements) {
			if (element instanceof MetaFieldElement) {
				MetaFieldElement field = (MetaFieldElement) element;
				MetaProperty p = doc.getField(field.getName());
				if (p.isLinked()) {
					MetaLink link = (MetaLink) p;

					MetaModule targetModule = link.isRelative() ?
							doc.getParentModule() :
							GeneratorDataRegistry.getInstance().getModule(link.getTargetModuleName());
					String tDocName = link.getTargetDocumentName();
					MetaDocument targetDocument = targetModule.getDocumentByName(tDocName);
					String listName = targetDocument.getMultiple().toLowerCase();
					emptyline();

					if (linkTargets.contains(link.getLinkTarget())) {
						appendString("//link " + link.getName() + " to " + link.getLinkTarget() + " reuses collection.");
					} else {
						appendString("//link " + link.getName() + " to " + link.getLinkTarget());
						appendStatement("List<" + DataFacadeGenerator.getDocumentImport(targetDocument) + "> " + listName + " = " + getServiceGetterCall(targetModule) + ".get" + targetDocument.getMultiple() + "()");
						appendStatement("List<LabelValueBean> " + listName + "Values = new ArrayList<LabelValueBean>(" + listName + ".size()+1)");
						appendStatement(listName + "Values.add(new LabelValueBean(" + quote("") + ", \"-----\"))");
						appendString("for (" + (DataFacadeGenerator.getDocumentImport(targetDocument)) + " " + targetDocument.getVariableName() + " : " + listName + "){");
						increaseIdent();

						appendStatement("LabelValueBean bean = new LabelValueBean(" + targetDocument.getVariableName() + ".getId(), " + targetDocument.getVariableName() + ".getName() )");
						appendStatement(listName + "Values.add(bean)");
						closeBlockNEW();
					}

					String lang = getElementLanguage(element);
					appendStatement("form." + p.toBeanSetter() + "Collection" + (lang == null ? "" : lang) + "(" + listName + "Values" + ")");
					if (!newDocument){//if it is not new document (validation errors,etc) - set "current value"
						appendString( "try{");
						increaseIdent();
						String getter = getServiceGetterCall(targetModule)+".get"+targetDocument.getName()+"(form."+p.toBeanGetter(lang)+"()).getName()";
						appendStatement("form."+p.toBeanSetter()+"CurrentValue"+(lang==null ? "":lang)+"("+getter+")");
						decreaseIdent();
						appendString( "}catch(Exception e){");
						appendIncreasedStatement("form."+p.toBeanSetter()+"CurrentValue"+(lang==null ? "":lang)+"("+quote("none")+")");
						appendString( "}");
					}
					linkTargets.add(link.getLinkTarget());
				}//...end if (p.isLinked())

				if (p instanceof MetaEnumerationProperty) {
					enumPropGen.generateEnumerationPropertyHandling((MetaEnumerationProperty) p, false);
					if (!newDocument){//if it is not new document (validation errors,etc) - set "current value"
						MetaEnumerationProperty mep = (MetaEnumerationProperty) p;
						EnumerationType type = (EnumerationType )GeneratorDataRegistry.getInstance().getType(mep.getEnumeration());
						openTry();
						appendStatement("form."+mep.toBeanSetter()+"CurrentValue("+EnumTypeGenerator.getEnumClassName(type)+".getConstantByValue(form."+mep.toBeanGetter()+"()).name())");
						appendCatch(ConstantNotFoundException.class);
						closeBlock("try");
					}
				}

			}
		}
	}
	
	private void appendAddFieldExplanationsMethod(MetaDocument doc) {
		appendString("private void addFieldExplanations(HttpServletRequest req, "+doc.getName()+" "+doc.getVariableName()+") {");
		increaseIdent();
		appendString("if (!CMSViewHelperRegistry.getCMSViewHelpers("+quote(doc.getParentModule().getName()+"."+doc.getName())+").isEmpty()) {");
		increaseIdent();
		appendStatement("String fieldDescription = null");
		for (MetaProperty p : doc.getProperties()) {
			appendStatement("fieldDescription = CMSViewHelperUtil.getFieldExplanation("+quote(doc.getParentModule().getName()+"."+doc.getName())+ ", "+doc.getVariableName()+", "+quote(p.getName())+")");
			appendString( "if (fieldDescription!=null && fieldDescription.length()>0)");
			appendIncreasedStatement("req.setAttribute("+quote("description."+p.getName())+", fieldDescription)");
		}
		closeBlock("");
		closeBlock("addFieldExplanations END");
		emptyline();
	}

	/**
	 * Generates the base action for a module, which is extended by all other module based actions.
	 * @param section
	 * @return
	 */
	private GeneratedClass generateBaseAction(MetaModuleSection section){
		MetaDocument doc = section.getDocument();
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		appendGenerationPoint("generateBaseAction");

	    clazz.setPackageName(getPackage(section.getModule()));
	    clazz.setAbstractClass(true);

		boolean isCMS = StorageType.CMS.equals(section.getModule().getStorageType());

	    emptyline();
	    clazz.addImport(GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED)+".action."+BaseViewActionGenerator.getViewActionName(view));
		if (isCMS) {
			clazz.addImport("javax.servlet.http.HttpServletRequest");
			clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
			clazz.addImport("net.anotheria.asg.data.AbstractASGDocument");
			clazz.addImport("net.anotheria.asg.data.LockableObject");
			clazz.addImport("net.anotheria.asg.util.locking.exeption.LockingException");
			clazz.addImport("net.anotheria.asg.util.locking.helper.DocumentLockingHelper");
			clazz.addImport("org.slf4j.Logger");
			clazz.addImport("org.slf4j.LoggerFactory");
		}
		clazz.addImport("net.anotheria.maf.bean.FormBean");
		clazz.setGeneric("T extends FormBean");
		clazz.setName(getBaseActionName(section));
	    clazz.setParent(BaseViewActionGenerator.getViewActionName(view), "T");
	    
	    startClassBody();

		if(isCMS)
		appendStatement("private final Logger logger = LoggerFactory.getLogger(\"cms-lock-log\")");
	    //generate getTitle
	    appendString( "protected String getTitle(){");
	    increaseIdent();
	    appendStatement("return "+quote(section.getTitle()));
	    closeBlock("getTitle");
	    emptyline();
	    
	    //generate getCurrentModuleDefName
	    appendString( "protected String getCurrentModuleDefName(){");
	    increaseIdent();
	    appendStatement("return "+quote(section.getModule().getName()));
	    closeBlock("getCurrentModuleDefName");
	    emptyline();
	    
		//generate getCurrentDocumentDefName
	    appendString( "protected String getCurrentDocumentDefName(){");
	    increaseIdent();
	    appendStatement("return "+quote(section.getDocument().getName()));
	    closeBlock("getCurrentDocumentDefName");
	    emptyline();

		//starting additional methods generation!!!!!! Actually Lock & Unlock!!! + state checker!!!

		if (isCMS) {
			appendComment("Executing locking. Actually.");
			appendString("protected void lock" + doc.getMultiple() + "(" + doc.getName() + " " + doc.getVariableName() + ", HttpServletRequest req) throws Exception{");
			increaseIdent();
			appendString("if("+doc.getVariableName()+" instanceof AbstractASGDocument){");
			appendIncreasedStatement("AbstractASGDocument lock = (AbstractASGDocument)"+doc.getVariableName());
			appendIncreasedStatement("lock.setLocked(true)");
			appendIncreasedStatement("lock.setLockerId(getUserId(req))");
			appendIncreasedStatement("lock.setLockingTime(System.currentTimeMillis())");
			appendIncreasedStatement(getServiceGetterCall(section.getModule()) + ".update" + doc.getName() + "( " + doc.getVariableName() + ")");
			appendIncreasedStatement("logger.info("+quote("Lock-OPERATION, document with id : [") +"+"+doc.getVariableName()+".getId()+"+quote("] was locked by: ")+" + getUserId(req)"+")");
			//putting to cache!
			appendIncreasedStatement("addLockedAttribute(req, lock)");
			appendString("}");
			closeBlockNEW();

			appendComment("Executing unlocking. Actually.");
			appendString("protected void unLock" + doc.getMultiple() + "(" + doc.getName() + " " + doc.getVariableName() + ", HttpServletRequest req, boolean unlockByTimeoout) throws Exception{");
			increaseIdent();
			appendString("if("+doc.getVariableName()+" instanceof AbstractASGDocument){");
			appendIncreasedStatement("AbstractASGDocument lock = (AbstractASGDocument)"+doc.getVariableName());
			appendIncreasedStatement("lock.setLocked(false)");
			appendIncreasedStatement("lock.setLockerId(\"\")");
			appendIncreasedStatement("lock.setLockingTime(0)");
			appendIncreasedStatement(getServiceGetterCall(section.getModule()) + ".update" + doc.getName() + "( " + doc.getVariableName() + ")");
			appendIncreasedString("if (!unlockByTimeoout){");
			appendIncreasedStatement("   logger.info("+quote("UnLock-OPERATION, document with id : [") +"+"+doc.getVariableName()+".getId()+"+quote("] was unlocked by: ")+" + getUserId(req) +"+quote("  with 'admin' role :")+" + isUserInRole(req, \"admin\") " +")");
			appendIncreasedString(" } else { ");
			appendIncreasedStatement("   logger.info("+quote("UnLock-OPERATION, document with id : [") +"+"+doc.getVariableName()+".getId()+"+quote("] was unlocked by: timeOut")+")");
			appendIncreasedString("}");
	        appendIncreasedStatement("removeLockedAttribute(req, lock)");
			appendString("}");
			closeBlockNEW();

			
		    appendComment("Executing auto-unlocking check....");
			appendString("protected void check" + doc.getMultiple() + "(" + doc.getName() + " " + doc.getVariableName() + ", HttpServletRequest req) throws Exception{");
			increaseIdent();
			appendStatement("boolean shouldUnlock = "+doc.getVariableName()+ " instanceof AbstractASGDocument && \n \t \t \t \t ((AbstractASGDocument)"+doc.getVariableName()+
					").isLocked() && \n \t \t \t \t ( System.currentTimeMillis() >= ((AbstractASGDocument)"+doc.getVariableName()+").getLockingTime() + getLockingTimeout())");
			appendString("if(shouldUnlock)");
            appendIncreasedStatement("unLock"+ doc.getMultiple() + "(" +doc.getVariableName() + ", req, true)");
			closeBlockNEW();

			appendComment("Checking UpdateCapability rights");
			appendString("protected void canUpdate" + doc.getMultiple() + "(" + doc.getName() + " " + doc.getVariableName() + ", HttpServletRequest req) throws Exception{");
			increaseIdent();
			appendString("if("+doc.getVariableName()+ " instanceof LockableObject ){");
			appendString("//Actually - simplest Check! --  exception - if anything happens!!!!");
			appendIncreasedStatement("DocumentLockingHelper.update.checkExecutionPermission((LockableObject)"+doc.getVariableName()+", false, getUserId(req))");
			appendString("}");
			appendString("if (isTimeoutReached("+ doc.getVariableName() +")) {");
			appendIncreasedStatement("check"+ doc.getMultiple() + "(" +doc.getVariableName() + ", req)");
			appendIncreasedStatement("throw new LockingException(getUserId(req)+\" . Document can't be updated! Due to lock - timeout!!!\")");
            appendString("}");
			appendString("if (wasUnlockedByAdmin("+ doc.getVariableName() +", req)) {");
			appendIncreasedStatement("throw new LockingException(getUserId(req)+\" . Document can't be updated! It was unlocked by user in 'admin' role!!!\")");
            appendString("}");
			closeBlockNEW();

			appendComment("");
			appendString("private boolean isTimeoutReached("+doc.getName() +" "+ doc.getVariableName()+"){");
            increaseIdent();
			appendString("if ("+ doc.getVariableName()+" instanceof LockableObject) {");
			appendIncreasedStatement("LockableObject lock = (LockableObject)"+ doc.getVariableName());
			appendIncreasedStatement("return lock.isLocked() && lock.getLockingTime() + getLockingTimeout() <= System.currentTimeMillis()");
            appendString("}");
			appendStatement("return false");
			closeBlockNEW();

			appendComment("");
			appendString("private boolean wasUnlockedByAdmin("+doc.getName() +" "+ doc.getVariableName()+", HttpServletRequest req){");
            increaseIdent();
			appendString("if ("+ doc.getVariableName()+" instanceof AbstractASGDocument) {");
			appendIncreasedStatement("AbstractASGDocument lock = (AbstractASGDocument)"+ doc.getVariableName());
			appendIncreasedStatement("return !lock.isLocked() && containsLockedAttribute(req, lock)");
            appendString("}");
			appendStatement("return false");
			closeBlockNEW();
			
		}
	    
	    return clazz;
	}
	
	/**
	 * @deprecated
	 * @return
	 */
	public static String getPackage(){
	    return getPackage(GeneratorDataRegistry.getInstance().getContext());
	}

	public static String getPackage(MetaModule module){
	    return getPackage(GeneratorDataRegistry.getInstance().getContext(), module);
	}

	/**
	 * @deprecated
	 * @param context
	 * @return
	 */
	public static String getPackage(Context context){
	    return context.getPackageName()+".action";
	}

	public static String getPackage(Context context, MetaModule module){
	    return context.getPackageName(module)+".action";
	}
	
	public static String getPackage(MetaDocument doc){
	    return GeneratorDataRegistry.getInstance().getContext().getPackageName(doc)+".action";
	}

	public static String getServiceInstanceName(MetaModule module){
	    return module.getName().toLowerCase()+"Service";
	}

	public static String getServiceGetterName(MetaModule module){
	    return "get"+module.getName()+"Service";
	}

	public static String getServiceGetterCall(MetaModule module){
	    return getServiceGetterName(module)+"()";
	}

	private String getExecuteDeclaration(){
		return getExecuteDeclaration(null);
	}
	
	/**
	 * Creates the execute method declaration.
	 * @param methodName the name of the "execute" method. Null means anoDocExecute.
	 * @return
	 */
	private String getExecuteDeclaration(String methodName){
		return getExecuteDeclaration(methodName, "FormBean");
	}
	
	private String getExecuteDeclaration(String methodName, String formBeanName){
	    String ret = "";
	    ret += "public ActionForward "+(methodName == null ? "anoDocExecute" : methodName ) + "(";
		ret += "ActionMapping mapping, ";
		ret += formBeanName + " af, ";
		ret += "HttpServletRequest req, ";
		ret += "HttpServletResponse res) ";
		ret += "throws Exception{";
		return ret;
	}
	
	private String getSuperCall(){
	    String ret = "";
	    ret += "super.anoDocExecute(";
		ret += "mapping, ";
		ret += "af, ";
		ret += "req, ";
		ret += "res) ";
		return ret;
	}

	/**
	 * Adds standard imports for an action to the clazz. 
	 * @param clazz
	 */
	private void addStandardActionImports(GeneratedClass clazz){
	    clazz.addImport("javax.servlet.http.HttpServletRequest");
	    clazz.addImport("javax.servlet.http.HttpServletResponse");
	    clazz.addImport("net.anotheria.maf.action.ActionForward");
	    clazz.addImport("net.anotheria.maf.action.ActionMapping");
	    clazz.addImport("net.anotheria.maf.bean.FormBean");

		clazz.addImport("java.util.List");
		clazz.addImport("java.util.ArrayList");

	}
	

	//////////////////////////////////////////////////////////////////////////
	// TABLE
	
	public static String getContainerMultiOpActionName(MetaDocument doc, MetaContainerProperty property){
		return "MultiOp"+doc.getMultiple()+StringUtils.capitalize(property.getName())+"Action";
	}

	public static String getContainerShowActionName(MetaDocument doc, MetaContainerProperty property){
		return "Show"+doc.getMultiple()+StringUtils.capitalize(property.getName())+"Action";
	}
	
	public static String getContainerAddEntryActionName(MetaDocument doc, MetaContainerProperty property){
		return "Add"+doc.getMultiple()+StringUtils.capitalize(property.getName())+getContainerNameAddy(property)+"Action";
	}

	public static String getContainerQuickAddActionName(MetaDocument doc, MetaContainerProperty property){
		return "QuickAdd"+doc.getMultiple()+StringUtils.capitalize(property.getName())+getContainerNameAddy(property)+"Action";
	}

	public static String getContainerDeleteEntryActionName(MetaDocument doc, MetaContainerProperty property){
		return "Delete"+doc.getMultiple()+StringUtils.capitalize(property.getName())+getContainerNameAddy(property)+"Action";
	}
	
	public static String getContainerMoveEntryActionName(MetaDocument doc, MetaContainerProperty property){
		return "Move"+doc.getMultiple()+StringUtils.capitalize(property.getName())+getContainerNameAddy(property)+"Action";
	}

	private static String getContainerNameAddy(MetaContainerProperty p){
		return p.getContainerEntryName();
	}
	

	private GeneratedClass generateContainerMultiOpAction(MetaModuleSection section, MetaContainerProperty containerProperty){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		appendGenerationPoint("generateContainerMultiOpAction");
		
		MetaDocument doc = section.getDocument();

	    clazz.setPackageName(getPackage(section.getModule()));

	    //write imports...
		clazz.addImport("java.util.List");
		clazz.addImport("java.util.ArrayList");
		addStandardActionImports(clazz);
		clazz.addImport(DataFacadeGenerator.getDocumentFactoryImport(GeneratorDataRegistry.getInstance().getContext(), doc));
		clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
		clazz.addImport(ModuleBeanGenerator.getContainerEntryFormImport(doc, containerProperty));
//        if(StorageType.CMS.equals(section.getModule().getStorageType())){
//            clazz.addImport("net.anotheria.asg.data.LockableObject");
//            clazz.addImport("net.anotheria.asg.util.locking.helper.DocumentLockingHelper");
//        }
		if (containerProperty instanceof MetaListProperty){
			MetaProperty containedProperty = ((MetaListProperty)containerProperty).getContainedProperty();
			if(containedProperty.isLinked()){
				MetaListProperty list = ((MetaListProperty)containerProperty);
				clazz.addImport(ModuleBeanGenerator.getContainerQuickAddFormImport(doc, containerProperty));
	
				MetaLink link = (MetaLink)list.getContainedProperty();
				
				String tDocName = link.getTargetDocumentName(); 
				MetaModule targetModule = link.getLinkTarget().indexOf('.')== -1 ?
						doc.getParentModule() : GeneratorDataRegistry.getInstance().getModule(link.getTargetModuleName());
				MetaDocument targetDocument = targetModule.getDocumentByName(tDocName);
				clazz.addImport(DataFacadeGenerator.getDocumentImport(targetDocument));
				clazz.addImport(AbstractDataObjectGenerator.getSortTypeImport(targetDocument));
				clazz.addImport("net.anotheria.webutils.bean.LabelValueBean");
				clazz.addImport("net.anotheria.anodoc.data.NoSuchDocumentException");
				clazz.addImport("net.anotheria.util.StringUtils");
			}
			if(containedProperty instanceof MetaEnumerationProperty){
				clazz.addImport("net.anotheria.webutils.bean.LabelValueBean");
				EnumerationType type = (EnumerationType)GeneratorDataRegistry.getInstance().getType(((MetaEnumerationProperty) containedProperty).getEnumeration());
				clazz.addImport(EnumTypeGenerator.getEnumImport(type));
			}
			if (containedProperty.getType() == MetaProperty.Type.IMAGE){
				clazz.addImport("net.anotheria.webutils.filehandling.actions.FileStorage");
				clazz.addImport("net.anotheria.webutils.filehandling.beans.TemporaryFileHolder");
			}
		}
		
		clazz.addImport("net.anotheria.asg.exception.ASGRuntimeException");

		clazz.setName(getContainerMultiOpActionName(doc, containerProperty));
		clazz.setParent(getBaseActionName(section));
		startClassBody();
		
	    appendString( getExecuteDeclaration(null));
	    increaseIdent();
	    appendStatement("String path = stripPath(mapping.getPath())");

		if (containerProperty instanceof MetaListProperty ){
			writePathResolveForContainerMultiOpAction(doc, containerProperty, CMSMappingsConfiguratorGenerator.ACTION_SHOW);
			writePathResolveForContainerMultiOpAction(doc, containerProperty, CMSMappingsConfiguratorGenerator.ACTION_ADD);
			writePathResolveForContainerMultiOpAction(doc, containerProperty, CMSMappingsConfiguratorGenerator.ACTION_DELETE);
			writePathResolveForContainerMultiOpAction(doc, containerProperty, CMSMappingsConfiguratorGenerator.ACTION_MOVE);

			if (((MetaListProperty)containerProperty).getContainedProperty().isLinked()){
				writePathResolveForContainerMultiOpAction(doc, containerProperty, CMSMappingsConfiguratorGenerator.ACTION_QUICK_ADD);
			}
		}
	    
	    appendStatement("throw new IllegalArgumentException("+quote("Unknown path: ")+"+path)");
	    closeBlockNEW();
	    emptyline();
	    
		
		if (containerProperty instanceof MetaListProperty ){
			MetaListProperty list = (MetaListProperty)containerProperty;
			generateListShowActionMethod(section, list, CMSMappingsConfiguratorGenerator.getContainerPath(doc, containerProperty, CMSMappingsConfiguratorGenerator.ACTION_SHOW));
		    emptyline();
			generateContainerDeleteEntryActionMethod(section, list, CMSMappingsConfiguratorGenerator.getContainerPath(doc, containerProperty, CMSMappingsConfiguratorGenerator.ACTION_DELETE));
		    emptyline();
		    generateContainerMoveEntryActionMethod(section, list, CMSMappingsConfiguratorGenerator.getContainerPath(doc, containerProperty, CMSMappingsConfiguratorGenerator.ACTION_MOVE));
		    emptyline();
			generateListAddRowActionMethod(section, list, CMSMappingsConfiguratorGenerator.getContainerPath(doc, containerProperty, CMSMappingsConfiguratorGenerator.ACTION_ADD));
		    emptyline();
		    
		    if (list.getContainedProperty().isLinked()){
		    	generateListQuickAddActionMethod(section, list, CMSMappingsConfiguratorGenerator.getContainerPath(doc, containerProperty, CMSMappingsConfiguratorGenerator.ACTION_QUICK_ADD));
		    	emptyline();
		    }
		}

		return clazz;
	}

	
	private GeneratedClass generateContainerAddRowAction(MetaModuleSection section, MetaContainerProperty container){
		if (container instanceof MetaTableProperty)
			return generateTableAddRowAction(section, (MetaTableProperty)container);

		if (container instanceof MetaListProperty)
			return generateListAddRowAction(section, (MetaListProperty)container);
			
		throw new RuntimeException("Unsupported container type: "+container.getClass().getName());
	}
	
	private GeneratedClass generateContainerQuickAddAction(MetaModuleSection section, MetaContainerProperty container){
		if (container instanceof MetaListProperty && ((MetaListProperty)container).getContainedProperty().isLinked())
			return generateListQuickAddAction(section, (MetaListProperty)container);
		return null;
		//throw new RuntimeException("Unsupported container type: "+container.getClass().getName());
	}

	private GeneratedClass generateListAddRowAction(MetaModuleSection section, MetaListProperty list){
        //TODO: locking && unlocking currently supported via MultiOP  for container!!! ONLY
		if (USE_MULTIOP_ACTIONS)
			return null;
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);

		MetaDocument doc = section.getDocument(); 

		clazz.setPackageName(getPackage(section.getModule()));
		addStandardActionImports(clazz);
		clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
		clazz.addImport(ModuleBeanGenerator.getContainerEntryFormImport(doc, list));
		
		clazz.setName(getContainerAddEntryActionName(doc, list));
		clazz.setParent(getContainerShowActionName(doc, list));	

		generateListAddRowActionMethod(section, list, null);
		
		return clazz;
	}
	
	private void generateListAddRowActionMethod(MetaModuleSection section, MetaListProperty list, String methodName){
		appendGenerationPoint("generateListAddRowActionMethod");
		MetaDocument doc = section.getDocument();
		appendString( getExecuteDeclaration(methodName));
		increaseIdent();
		appendStatement(ModuleBeanGenerator.getContainerEntryFormName(list)+" form = new "+ModuleBeanGenerator.getContainerEntryFormName(list)+"()");
		appendStatement("populateFormBean(req, form)");
		appendStatement("String id = form.getOwnerId()");

		
		appendStatement(doc.getName()+" "+doc.getVariableName());
		appendStatement(doc.getVariableName()," = ",getServiceGetterCall(section.getModule()),".get",doc.getName(),"(id)");

        if(StorageType.CMS.equals(section.getDocument().getParentModule().getStorageType())){
         //  appendString("if("+doc.getVariableName()+" instanceof LockableObject){ ");
          // appendIncreasedStatement("LockableObject lockable = (LockableObject)" + doc.getVariableName());
		   //This is SomeHow related  to Document Updation! SO next method should be invoked!
           appendStatement("canUpdate" + doc.getMultiple() +"("+doc.getVariableName()+", req)" );
		   //autoUnlocking!
		   appendStatement("check" + doc.getMultiple() + "("+doc.getVariableName()+", req)");
          // appendString("}");
        }
        

		MetaProperty p = list.getContainedProperty();
		//handle images.
		
		if (p.getType() == MetaProperty.Type.IMAGE){
			//will work with multiple images.
			String varName = p.getName();
			appendString( "//handle image");
			appendStatement("TemporaryFileHolder holder = FileStorage.getTemporaryFile(req, \""+varName+"\")");
			appendString( "if (holder!=null && holder.getData()!=null){");
			increaseIdent();
			appendStatement("FileStorage.storeFilePermanently(req, holder.getFileName(), \""+varName+"\")");
			appendStatement(doc.getVariableName()+"."+DataFacadeGenerator.getContainerEntryAdderName(list)+"(holder.getFileName())");
			appendStatement("FileStorage.removeTemporaryFile(req, \""+varName+"\")");
			closeBlockNEW();
		} else {
			String getter = "form."+p.toBeanGetter()+"()";
			appendStatement(doc.getVariableName()+"."+DataFacadeGenerator.getContainerEntryAdderName(list)+"("+getter+")");
		}
		
		appendStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		if (methodName==null)
			appendStatement("return "+getSuperCall());
		else
			appendStatement("return "+CMSMappingsConfiguratorGenerator.getContainerPath(doc, list, CMSMappingsConfiguratorGenerator.ACTION_SHOW)+"(mapping, af, req, res)");
		closeBlockNEW();
		
	}
	
	private GeneratedClass generateListQuickAddAction(MetaModuleSection section, MetaListProperty list){
           //TODO: locking && unlocking currently supported via MultiOP  for container!!! ONLY
		if (USE_MULTIOP_ACTIONS)
			return null;

		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);

		MetaDocument doc = section.getDocument();

		clazz.setPackageName(getPackage(section.getModule()));
		addStandardActionImports(clazz);
		clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
		clazz.addImport(ModuleBeanGenerator.getContainerQuickAddFormImport(doc, list));
		clazz.addImport("net.anotheria.util.StringUtils");
		
		clazz.setName(getContainerQuickAddActionName(doc, list));
		clazz.setParent(getContainerShowActionName(doc, list));	

		startClassBody();
		generateListQuickAddActionMethod(section, list, null);
		
		return clazz;
	}

	private void generateListQuickAddActionMethod(MetaModuleSection section, MetaListProperty list, String methodName){
		appendGenerationPoint("generateListQuickAddActionMethod");
		MetaDocument doc = section.getDocument();
		
		appendString(getExecuteDeclaration(methodName));
		increaseIdent();
		appendStatement(ModuleBeanGenerator.getContainerQuickAddFormName(list)+" form = new "+ModuleBeanGenerator.getContainerQuickAddFormName(list)+"()");
		appendStatement("populateFormBean(req, form)");
		appendStatement("String id = form.getOwnerId()");
		appendStatement(doc.getName()+" "+doc.getVariableName());
		appendStatement(doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");

         if(StorageType.CMS.equals(section.getDocument().getParentModule().getStorageType())){
          /////// appendString("if("+doc.getVariableName()+" instanceof LockableObject){ ");
          // appendIncreasedStatement("LockableObject lockable = (LockableObject)" + doc.getVariableName());
		   //This is SomeHow related  to Document Updation! SO next method should be invoked!
           appendStatement("canUpdate" + doc.getMultiple() +"("+doc.getVariableName()+", req)" );
		   //autoUnlocking!
		   appendStatement("check" + doc.getMultiple() + "("+doc.getVariableName()+", req)");
          ///// appendString("}");
        }

		appendStatement("String paramIdsToAdd = form.getQuickAddIds()");

		emptyline();
		appendStatement("String idParameters[] = StringUtils.tokenize(paramIdsToAdd, ',')");
		appendString("for (String anIdParam : idParameters){");
		increaseIdent();
		
		appendString("String ids[] = StringUtils.tokenize(anIdParam, '-');");
		appendString("for (int i=Integer.parseInt(ids[0]); i<=Integer.parseInt(ids[ids.length-1]); i++){");
		increaseIdent();
		appendStatement(doc.getVariableName()+"."+DataFacadeGenerator.getContainerEntryAdderName(list)+"("+quote("")+"+i)");
		closeBlockNEW();
		
		closeBlockNEW();
		String call = "";
		MetaProperty p = list.getContainedProperty();
		String getter = "form."+p.toBeanGetter()+"()";
		call += getter;
		appendStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		if (methodName==null)
			appendStatement("return "+getSuperCall());
		else
			appendStatement("return "+CMSMappingsConfiguratorGenerator.getContainerPath(doc, list, CMSMappingsConfiguratorGenerator.ACTION_SHOW)+"(mapping, af, req, res)");
		closeBlockNEW();
		
	}

	private GeneratedClass generateTableAddRowAction(MetaModuleSection section, MetaTableProperty table){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		MetaDocument doc = section.getDocument();
		clazz.setPackageName(getPackage(section.getModule()));
	    
		//write imports...
		addStandardActionImports(clazz);
		clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
		clazz.addImport(ModuleBeanGenerator.getContainerEntryFormImport(doc, table));
         if(StorageType.CMS.equals(section.getModule().getStorageType())){
            clazz.addImport("net.anotheria.asg.data.LockableObject");
            clazz.addImport("net.anotheria.asg.util.locking.helper.DocumentLockingHelper");
        }
		
		clazz.setName(getContainerAddEntryActionName(doc, table));
		clazz.setParent(getContainerShowActionName(doc, table));	

		startClassBody();
		appendString(getExecuteDeclaration());
		increaseIdent();
		appendStatement(ModuleBeanGenerator.getContainerEntryFormName(table)+" form = new "+ModuleBeanGenerator.getContainerEntryFormName(table)+"()");
		appendStatement("populateFormBean(req, form)");
		appendStatement("String id = form.getOwnerId()");
		appendStatement(doc.getName()+" "+doc.getVariableName());
		appendStatement(doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");

        if(StorageType.CMS.equals(section.getDocument().getParentModule().getStorageType())){
          // appendString("if("+doc.getVariableName()+" instanceof LockableObject){ ");
          // appendIncreasedStatement("LockableObject lockable = (LockableObject)" + doc.getVariableName());
			//This is SomeHow related  to Document Updation! SO next method should be invoked!
           appendStatement("canUpdate" + doc.getMultiple() +"("+doc.getVariableName()+", req)" );
		   //autoUnlocking!
		   appendStatement("check" + doc.getMultiple() + "("+doc.getVariableName()+", req)");
          // appendString("}");
        }

		String call = "";
		List<MetaProperty> columns = table.getColumns();
		for (int i =0; i<columns.size(); i++){
		    MetaProperty p = columns.get(i);
		    String getter = "form.get"+StringUtils.capitalize(table.extractSubName(p))+"()";
		    call += getter;
		    if (i<columns.size()-1)
		        call += ", ";
		}
		appendStatement(doc.getVariableName()+"."+DataFacadeGenerator.getContainerEntryAdderName(table)+"("+call+")");
		appendStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		appendStatement("return "+getSuperCall());
		closeBlockNEW();
		return clazz;
	}

	private GeneratedClass generateContainerDeleteEntryAction(MetaModuleSection section, MetaContainerProperty container){
        //TODO: Locking && Unlocking support only included in MultiOPContainer generator!!!
		if (USE_MULTIOP_ACTIONS)
			return null;

		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		 
		MetaDocument doc = section.getDocument();

		clazz.setPackageName(getPackage(section.getModule()));
		addStandardActionImports(clazz);
		clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
		
		clazz.setName(getContainerDeleteEntryActionName(doc, container));
		clazz.setParent(getContainerShowActionName(doc, container));	

		startClassBody();
		generateContainerDeleteEntryActionMethod(section, container, null);
		return clazz;
	}

	private void generateContainerDeleteEntryActionMethod(MetaModuleSection section, MetaContainerProperty container, String methodName){		
		MetaDocument doc = section.getDocument();
		appendString(getExecuteDeclaration(methodName));
		increaseIdent();
		appendStatement("String id = getStringParameter(req, \"ownerId\")");
	 	if(StorageType.CMS.equals(section.getDocument().getParentModule().getStorageType())){
           appendStatement(doc.getName()+" "+doc.getVariableName()+"Curr = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");
           //appendString("if("+doc.getVariableName()+"Curr instanceof LockableObject){ ");
           //appendIncreasedStatement("LockableObject lockable = (LockableObject)" + doc.getVariableName() + "Curr");
		   //This is SomeHow related  to Document Updation! SO next method should be invoked!
           appendStatement("canUpdate" + doc.getMultiple() +"("+doc.getVariableName()+"Curr, req)" );
		   //autoUnlocking!
		   appendStatement("check" + doc.getMultiple() + "("+doc.getVariableName()+"Curr, req)");
           //appendString("}");
        }
		appendStatement("int position = getIntParameter(req, "+quote("pPosition")+")"); 
		appendStatement(doc.getName()+" "+doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");


		//deleting of image
		if (container instanceof MetaListProperty){
			MetaListProperty mlp = (MetaListProperty)container;
			MetaProperty containerProperty = mlp.getContainedProperty();
			if (containerProperty.getType() == MetaProperty.Type.IMAGE)
			appendStatement("FileStorage.removeFilePermanently( "+doc.getVariableName()+"."+DataFacadeGenerator.getListElementGetterName(mlp)+"(position) )");
		}

		appendStatement(doc.getVariableName()+"."+DataFacadeGenerator.getContainerEntryDeleterName(container)+"(position)");

		appendStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		if (methodName==null)
			appendStatement("return "+getSuperCall());
		else
			appendStatement("return "+CMSMappingsConfiguratorGenerator.getContainerPath(doc, container, CMSMappingsConfiguratorGenerator.ACTION_SHOW)+"(mapping, af, req, res)");
		closeBlockNEW();
	}

	private GeneratedClass generateContainerMoveEntryAction(MetaModuleSection section, MetaContainerProperty container){
         //TODO: Locking && Unlocking support only included in MultiOPContainer generator!!!
		if (!(container instanceof MetaListProperty)){
			return null;
		}
		
		if (USE_MULTIOP_ACTIONS)
			return null;

		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
			
		MetaListProperty sourceProperty = (MetaListProperty)container;
		MetaDocument doc = section.getDocument();

		clazz.setPackageName(getPackage(section.getModule()));
	    
		//write imports...
		addStandardActionImports(clazz);
		clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
		clazz.addImport("net.anotheria.asg.exception.ASGRuntimeException");
		clazz.addImport("java.util.List");
		
		clazz.setName(getContainerMoveEntryActionName(doc, container));
		clazz.setParent(getContainerShowActionName(doc, container));	

		startClassBody();
		generateContainerMoveEntryActionMethod(section, container, null);
		
		return clazz;

		
	}
	/**
	 * Generates the action which moves an entry in a container up, down, top or bottom.
	 * @param section
	 * @param container
	 * @param methodName
	 */
	private void generateContainerMoveEntryActionMethod(MetaModuleSection section, MetaContainerProperty container, String methodName){
		MetaDocument doc = section.getDocument();
		MetaListProperty sourceProperty = (MetaListProperty)container;
		MetaGenericProperty generic = new MetaGenericListProperty(sourceProperty.getName(), sourceProperty.getContainedProperty());

		appendString(getExecuteDeclaration(methodName));
		increaseIdent();
		appendStatement("String id = getStringParameter(req, \"ownerId\")");
		appendStatement("int position = getIntParameter(req, "+quote("pPosition")+")");
		appendStatement("String direction = getStringParameter(req, "+quote("dir")+")");

		appendStatement(doc.getName()+" "+doc.getVariableName() + " = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");

         if(StorageType.CMS.equals(section.getDocument().getParentModule().getStorageType())){
         //  appendString("if("+doc.getVariableName()+" instanceof LockableObject){ ");
          // appendIncreasedStatement("LockableObject lockable = (LockableObject)" + doc.getVariableName());
		  //This is SomeHow related  to Document Updation! SO next method should be invoked!
           appendStatement("canUpdate" + doc.getMultiple() +"("+doc.getVariableName()+", req)" );
		   //autoUnlocking!
		   appendStatement("check" + doc.getMultiple() + "("+doc.getVariableName()+", req)");
          // appendString("}");
        }

		appendString("if ("+quote("top")+".equalsIgnoreCase(direction))");
		appendIncreasedStatement("moveTop("+doc.getVariableName()+", position)");
		appendString("if ("+quote("up")+".equalsIgnoreCase(direction))");
		appendIncreasedStatement("moveUp("+doc.getVariableName()+", position)");
		appendString("if ("+quote("down")+".equalsIgnoreCase(direction))");
		appendIncreasedStatement("moveDown("+doc.getVariableName()+", position)");
		appendString("if ("+quote("bottom")+".equalsIgnoreCase(direction))");
		appendIncreasedStatement("moveBottom("+doc.getVariableName()+", position)");
		

		if (methodName==null)
			appendStatement("return "+getSuperCall());
		else
			appendStatement("return "+CMSMappingsConfiguratorGenerator.getContainerPath(doc, container, CMSMappingsConfiguratorGenerator.ACTION_SHOW)+"(mapping, af, req, res)");
		closeBlockNEW();
		emptyline();
		
		String moveMethodParameter = doc.getName()+" "+doc.getVariableName()+", int position";
		
		appendString("private void moveUp("+moveMethodParameter+") throws ASGRuntimeException {");
		increaseIdent();
		appendString("if (position==0) ");
		appendIncreasedStatement("return");
		appendStatement(doc.getVariableName()+"."+DataFacadeGenerator.getContainerEntrySwapperName(container)+"(position, position-1)");
		appendStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		closeBlockNEW();
		emptyline();
		
		appendString("private void moveTop("+moveMethodParameter+") throws ASGRuntimeException {");
		increaseIdent();
		appendStatement(generic.toJavaType()+" targetList = "+doc.getVariableName()+".get"+container.getAccesserName()+"()");
		appendStatement(sourceProperty.getContainedProperty().toJavaType()+" toSwap = targetList.remove(position)");
		appendStatement("targetList.add(0, toSwap)");
		appendStatement(doc.getVariableName()+".set"+container.getAccesserName()+"(targetList)"); 
		appendStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		closeBlockNEW();
		emptyline();

		appendString("private void moveDown("+moveMethodParameter+") throws ASGRuntimeException {");
		increaseIdent();
		appendString("if (position<"+doc.getVariableName()+"."+DataFacadeGenerator.getContainerSizeGetterName(container)+"()-1){");
		increaseIdent();
		
		appendStatement(doc.getVariableName()+"."+DataFacadeGenerator.getContainerEntrySwapperName(container)+"(position, position+1)");
		appendStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		closeBlockNEW();
		closeBlockNEW();
		emptyline();

		appendString("private void moveBottom("+moveMethodParameter+") throws ASGRuntimeException {");
		increaseIdent();
		appendStatement(generic.toJavaType()+" targetList = "+doc.getVariableName()+".get"+container.getAccesserName()+"()");
		appendStatement(sourceProperty.getContainedProperty().toJavaType()+" toSwap = targetList.remove(position)");
		appendStatement("targetList.add(toSwap)");
		appendStatement(doc.getVariableName()+".set"+container.getAccesserName()+"(targetList)"); 
		appendStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		closeBlockNEW();
	}

	/**
	 * Generates the show action for a container. This would be a list or table.
	 * @param section
	 * @param container
	 * @return
	 */
	private GeneratedClass generateContainerShowAction(MetaModuleSection section, MetaContainerProperty container){
		if (container instanceof MetaTableProperty)
			return generateTableShowAction(section, (MetaTableProperty)container);

		if (container instanceof MetaListProperty)
			return generateListShowAction(section, (MetaListProperty)container);
			
		throw new RuntimeException("Unsupported container type: "+container.getClass().getName());
	}

	private GeneratedClass generateListShowAction(MetaModuleSection section, MetaListProperty list){
		if (USE_MULTIOP_ACTIONS)
			return null;

		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);

		MetaDocument doc = section.getDocument();

		clazz.setPackageName(getPackage(section.getModule()));

		clazz.addImport("java.util.List");
		clazz.addImport("java.util.ArrayList");
		addStandardActionImports(clazz);
		clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
		clazz.addImport(ModuleBeanGenerator.getContainerEntryFormImport(doc, list));
		if (list.getContainedProperty().isLinked()){
			clazz.addImport(ModuleBeanGenerator.getContainerQuickAddFormImport(doc, list));
			MetaLink link = (MetaLink)list.getContainedProperty();
			
			String tDocName = link.getTargetDocumentName(); 
			MetaModule targetModule = link.getLinkTarget().indexOf('.')== -1 ?
					doc.getParentModule() : GeneratorDataRegistry.getInstance().getModule(link.getTargetModuleName());
			MetaDocument targetDocument = targetModule.getDocumentByName(tDocName);
			clazz.addImport(DataFacadeGenerator.getDocumentImport(targetDocument));
			clazz.addImport(DataFacadeGenerator.getSortTypeImport(targetDocument));
			clazz.addImport("net.anotheria.anodoc.data.NoSuchDocumentException");
			

		}

		clazz.setName(getContainerShowActionName(doc, list));
		clazz.setParent(getBaseActionName(section));

		startClassBody();
	
		generateListShowActionMethod(section, list, null);
		
		return clazz;
	}	

	private void generateListShowActionMethod(MetaModuleSection section, MetaListProperty list, String methodName){
		appendGenerationPoint("generateListShowActionMethod");
		MetaDocument doc = section.getDocument();

		appendString( getExecuteDeclaration(methodName));
		increaseIdent();
		appendStatement("String id = getStringParameter(req, \"ownerId\")");
		appendStatement(doc.getName()+" "+doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");
		appendStatement("addBeanToRequest(req, \"ownerId\", id)");
		if(StorageType.CMS.equals(doc.getParentModule().getStorageType())){
			//autoUnlocking!
		    appendStatement("check" + doc.getMultiple() + "("+doc.getVariableName()+", req)");
		}
		emptyline();
		
		appendStatement(ModuleBeanGenerator.getContainerEntryFormName(list)+" form = new "+ModuleBeanGenerator.getContainerEntryFormName(list)+"() ");
		appendStatement("form.setPosition(-1)"); //hmm?
		appendStatement("form.setOwnerId("+doc.getVariableName()+".getId())");	
		appendStatement("addBeanToRequest(req, "+quote(CMSMappingsConfiguratorGenerator.getContainerEntryFormName(doc, list))+", form)");
		emptyline();
		
		if (list.getContainedProperty().isLinked()){
			appendStatement(ModuleBeanGenerator.getContainerQuickAddFormName(list)+" quickAddForm = new "+ModuleBeanGenerator.getContainerQuickAddFormName(list)+"() ");
			appendStatement("quickAddForm.setOwnerId("+doc.getVariableName()+".getId())");	
			appendStatement("addBeanToRequest(req, "+quote(CMSMappingsConfiguratorGenerator.getContainerQuickAddFormName(doc, list))+", quickAddForm)");
			emptyline();
		}
		
		if (list.getContainedProperty().isLinked()){
			//generate list collection
			MetaLink link = (MetaLink)list.getContainedProperty();
			emptyline();
			appendString( "//link "+link.getName()+" to "+link.getLinkTarget());
			MetaModule targetModule = link.getLinkTarget().indexOf('.')== -1 ?
					doc.getParentModule() : GeneratorDataRegistry.getInstance().getModule(link.getTargetModuleName());
			MetaDocument targetDocument = targetModule.getDocumentByName(link.getTargetDocumentName());
			String listName = targetDocument.getMultiple().toLowerCase();
			String sortType = "new "+DataFacadeGenerator.getSortTypeName(targetDocument);
			sortType += "("+DataFacadeGenerator.getSortTypeName(targetDocument)+".SORT_BY_NAME)";
			appendStatement("List<"+targetDocument.getName()+"> "+listName+" = "+getServiceGetterCall(targetModule)+".get"+targetDocument.getMultiple()+"("+sortType+")");
			appendStatement("List<LabelValueBean> "+listName+"Values = new ArrayList<LabelValueBean>("+listName+".size())");
			appendString( "for (int i=0; i<"+listName+".size(); i++){");
			increaseIdent();
			appendStatement(DataFacadeGenerator.getDocumentImport(targetDocument)+" "+targetDocument.getTemporaryVariableName()+" = ("+DataFacadeGenerator.getDocumentImport(targetDocument)+") "+listName+".get(i)");
			appendStatement("LabelValueBean bean = new LabelValueBean("+targetDocument.getTemporaryVariableName()+".getId(), "+targetDocument.getTemporaryVariableName()+".getName()+\" [\"+"+targetDocument.getTemporaryVariableName()+".getId()+\"]\" )");
			appendStatement(listName+"Values.add(bean)");
			closeBlockNEW();
			appendStatement("addBeanToRequest(req, "+quote(link.getName().toLowerCase()+"ValuesCollection")+", "+listName+"Values"+")");
		}
		
		if(list.getContainedProperty() instanceof MetaEnumerationProperty){
			MetaEnumerationProperty enumeration = (MetaEnumerationProperty)list.getContainedProperty();
			EnumerationType type = (EnumerationType )GeneratorDataRegistry.getInstance().getType(((MetaEnumerationProperty) list.getContainedProperty()).getEnumeration());
			emptyline();
			String arrName = type.getName()+"_values";
//		    String listName = arrName+"List";
		    String listName = enumeration.getName().toLowerCase() + "ValuesCollection";
		    appendString("//enumeration "+type.getName());
		    appendStatement(EnumTypeGenerator.getEnumClassName(type) + "[] " + arrName + " = "+ EnumTypeGenerator.getEnumClassName(type) +".values()");
		    appendStatement("List<LabelValueBean> "+listName+" = new ArrayList<LabelValueBean>("+arrName+".length)");
		    appendString("for ("+EnumTypeGenerator.getEnumClassName(type)+" element : " + arrName + ") {");
		    increaseIdent();
		
		    appendStatement("LabelValueBean bean = new LabelValueBean(\"\"+" + "element.getValue(), element.name())");
		    appendStatement(listName+".add(bean)");
		    closeBlockNEW();
		    appendStatement("addBeanToRequest(req, "+quote(listName)+", " + listName +")");
		}
		
		appendString( "// generate list ...");
		MetaModule targetModule = null;
		MetaDocument targetDocument = null;
		
		//ok this is a hack, but its a fast hack to display names for links
		if (list.getContainedProperty().isLinked()){
			//generate list collection
			MetaLink link = (MetaLink)list.getContainedProperty();
			targetModule = link.getLinkTarget().indexOf('.')== -1 ?
					doc.getParentModule() : GeneratorDataRegistry.getInstance().getModule(link.getTargetModuleName());
			targetDocument = targetModule.getDocumentByName(link.getTargetDocumentName());
		}		
		
		
		appendStatement("int size = "+doc.getVariableName()+"."+DataFacadeGenerator.getContainerSizeGetterName(list)+"()");
		appendStatement("List<"+ModuleBeanGenerator.getContainerEntryFormName(list)+"> beans = new ArrayList<"+ModuleBeanGenerator.getContainerEntryFormName(list)+">(size)");
		//appendStatement("List elements = "+doc.getVariableName()+".get"+list.getAccesserName()+"()");
		
		
		appendString( "for (int i=0; i<size; i++){");
		increaseIdent();
		appendStatement(list.getContainedProperty().toJavaType() + " value = "+doc.getVariableName()+"."+DataFacadeGenerator.getListElementGetterName(list)+"(i)");
		appendStatement(ModuleBeanGenerator.getContainerEntryFormName(list)+" bean = new "+ModuleBeanGenerator.getContainerEntryFormName(list)+"()");
		appendStatement("bean.setOwnerId("+doc.getVariableName()+".getId())");
		appendStatement("bean.setPosition(i)");
		appendStatement("bean."+list.getContainedProperty().toSetter()+"(value)");
		if (list.getContainedProperty().isLinked()){
			appendString( "try{");
			increaseIdent();
			appendStatement(targetDocument.getName()+" "+targetDocument.getTemporaryVariableName()+" = "+getServiceGetterCall(targetModule)+".get"+targetDocument.getName()+"(value)");
			//THIS is the hack
			appendStatement("bean.setDescription("+targetDocument.getTemporaryVariableName()+".getName())");
			decreaseIdent();
			appendString( "}catch(NoSuchDocumentException e){");
			appendIncreasedStatement("bean.setDescription(\"*** DELETED ***\")");
			appendString( "}");
		}
		if(list.getContainedProperty() instanceof MetaEnumerationProperty){
			EnumerationType type = (EnumerationType )GeneratorDataRegistry.getInstance().getType(((MetaEnumerationProperty) list.getContainedProperty()).getEnumeration());
			appendStatement("bean.setDescription("+EnumTypeGenerator.getEnumClassName(type)+".getConstantByValue(value).name())");
		}
		appendStatement("beans.add(bean)");
		closeBlockNEW();		
		appendStatement("addBeanToRequest(req, "+quote("elements")+", beans)");
//*/		
		appendStatement("return mapping.findForward(", quote("success"), ")");
		closeBlockNEW(); 
	}
	
	/**
	 * Generates show table action.
	 * @param section
	 * @param table
	 * @return
	 */
	private GeneratedClass generateTableShowAction(MetaModuleSection section, MetaTableProperty table){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		MetaDocument doc = section.getDocument();

		clazz.setPackageName(getPackage(section.getModule()));
	    
		clazz.addImport("java.util.List");
		clazz.addImport("java.util.ArrayList");
		addStandardActionImports(clazz);
		clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
		clazz.addImport(ModuleBeanGenerator.getContainerEntryFormImport(doc, table));

		clazz.setName(getContainerShowActionName(doc, table));
		clazz.setParent(getBaseActionName(section));
		
		startClassBody();
		appendString(getExecuteDeclaration());
		increaseIdent();
		appendStatement("String id = getStringParameter(req, PARAM_ID)");
		appendStatement(doc.getName()+" "+doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id);");
		if(StorageType.CMS.equals(doc.getParentModule().getStorageType())){
			//autoUnlocking!
		    appendStatement("check" + doc.getMultiple() + "("+doc.getVariableName()+", req)");
		}
		emptyline();
		
		appendStatement(ModuleBeanGenerator.getContainerEntryFormName(table)+" form = new "+ModuleBeanGenerator.getContainerEntryFormName(table)+"() ");
		appendStatement("form.setPosition(\"-1\")");
		appendStatement("form.setOwnerId("+doc.getVariableName()+".getId())");	
		appendStatement("addBeanToRequest(req, "+quote(CMSMappingsConfiguratorGenerator.getContainerEntryFormName(doc, table))+", form)");
		emptyline();
		
		appendString("// generate table...");
		appendStatement("List beans = new ArrayList()");
		appendStatement("List rows  = "+doc.getVariableName()+"."+DataFacadeGenerator.getTableGetterName(table)+"()");
		appendString("for (int i=0; i<rows.size(); i++){");
		increaseIdent();
		appendStatement("List row = (List) rows.get(i)");
		appendStatement(ModuleBeanGenerator.getContainerEntryFormName(table)+" bean = new "+ModuleBeanGenerator.getContainerEntryFormName(table)+"()");
		appendStatement("bean.setOwnerId("+doc.getVariableName()+".getId())");
		appendStatement("bean.setPosition(\"\"+i)");
		List<MetaProperty> columns = table.getColumns();
		for (int i=0; i<columns.size(); i++){
			MetaProperty p = columns.get(i);
			String setter = "bean.set"+StringUtils.capitalize(table.extractSubName(p));
			setter += "((String)row.get("+i+"))";
			appendStatement(setter);
		}
		appendStatement("beans.add(bean)");
		closeBlockNEW();		
		appendStatement("addBeanToRequest(req, "+quote("rows")+", beans)");
		
		appendStatement("return mapping.findForward("+quote("success")+")");
		closeBlockNEW();		
		
		return clazz;
	}
	
	public String getFormActionName(MetaForm form){
		if (form.getAction().equals("sendMail"))
			return getSendMailFormActionName(form);
		throw new RuntimeException("Unsupported action type: "+form.getAction());
	}
	
	private String getSendMailFormActionName(MetaForm form){
		return "Send"+StringUtils.capitalize(form.getId())+"FormAction"; 
	}
	
	public GeneratedClass generateFormAction(MetaForm form){
		if (form.getAction().equals("sendMail"))
			return generateSendMailFormAction(form);
		throw new RuntimeException("Unsupported action type: "+form.getAction());
	}
	
	private GeneratedClass generateSendMailFormAction(MetaForm form){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);

		clazz.setPackageName(getPackage());
		addStandardActionImports(clazz);
		clazz.addImport(ModuleBeanGenerator.getFormBeanImport(form));
		clazz.addImport("net.anotheria.communication.data.HtmlMailMessage");
		clazz.addImport("net.anotheria.communication.service.IMessagingService");
		clazz.addImport("net.anotheria.communication.service.MessagingServiceFactory");

		clazz.setName(getFormActionName(form));
		clazz.setParent(BaseActionGenerator.getBaseActionName());

		startClassBody();
		appendStatement("private IMessagingService service = MessagingServiceFactory.getMessagingService()"); 
		emptyline();
		List<String> targets = form.getTargets();
		appendString("public static String[] MAIL_TARGETS = {");
		for (int i=0; i<targets.size(); i++){
			appendIncreasedString(quote(targets.get(i))+",");
		}
		appendStatement("}");
		emptyline();
	    
	    
		appendString(getExecuteDeclaration());
		increaseIdent();
	

		appendStatement(ModuleBeanGenerator.getFormBeanName(form)+" form = ("+ModuleBeanGenerator.getFormBeanName(form)+") af");	
		//create message.
		appendString("//create message");
		appendStatement("String message = "+quote(""));
		appendStatement("String htmlMessage = "+quote(""));
		emptyline();

		appendStatement("String emptyHtmlLine = "+quote(""));
		appendStatement("emptyHtmlLine += "+quote("<tr>"));
		appendStatement("emptyHtmlLine += "+quote("\\t<td colspan=\\\"2\\\">"));
		appendStatement("emptyHtmlLine += "+quote("\\t\\t&nbsp;"));
		appendStatement("emptyHtmlLine  += "+quote("\\t</td>"));
		appendStatement("emptyHtmlLine  += "+quote("</tr>"));
		emptyline();
		
		appendStatement("htmlMessage += "+quote("<table border=\\\"0\\\">"));
		
		List<MetaFormField> elements = form.getElements();
		for (int i=0; i<elements.size(); i++){
			appendStatement("htmlMessage += "+quote("\\n"));

			MetaFormField element = (MetaFormField)elements.get(i);
			
			if (element.isSingle()){
				
				MetaFormSingleField field = (MetaFormSingleField)element;
				
				appendStatement("htmlMessage += "+quote("<tr>"));
				appendStatement("htmlMessage += "+quote("\\t<td width=\\\"1\\\">"));
				appendStatement("htmlMessage += "+quote("\\t\\t"+(field.isSpacer() ? "&nbsp;" : ""+(i+1))));
				appendStatement("htmlMessage += "+quote("\\t</td>"));
				appendStatement("htmlMessage += "+quote("\\t<td>"));
				appendStatement("htmlMessage += \"\\t\\t\"+getDefaultResources().getMessage("+quote(field.getTitle())+")");
				appendStatement("htmlMessage += "+quote("\\t</td>"));
				appendStatement("htmlMessage += "+quote("</tr>"));
				emptyline();

				if (field.isSpacer())
					continue;
				
				appendStatement("htmlMessage += "+quote("<tr>"));
				appendStatement("htmlMessage += "+quote("\\t<td colspan=\\\"2\\\">"));
				String value = "String value"+i+" = "; 
				if (field.getType().equals("boolean")){
					value += "form.get"+StringUtils.capitalize(element.getName())+"() ? "+quote("Yes")+" : "+quote("No");
				}else{
					value += "form.get"+StringUtils.capitalize(element.getName())+"()"; 
				}
				appendStatement(value);
				appendStatement("htmlMessage += \"\\t\\t\"+value"+i+"+"+quote("&nbsp;"));
				appendStatement("htmlMessage += "+quote("\\t</td>"));
				appendStatement("htmlMessage += "+quote("</tr>"));
				emptyline();

				appendStatement("htmlMessage += emptyHtmlLine");
				emptyline();

				
//				String title = element.getTitle();
				appendStatement("message += "+quote(element.getName()+" - "));
				appendStatement("message += getDefaultResources().getMessage("+quote(field.getTitle())+")+"+quote(":\\n"));
				appendStatement("message += value"+i+"+"+quote("\\n"));

				emptyline();
			}
			
			if (element.isComplex()){
				MetaFormTableField table = (MetaFormTableField)element;
				appendStatement("htmlMessage += "+quote("<!-- including table element "+table.getName()+" -->\\n"));
				appendStatement("htmlMessage += "+quote("<tr>"));
				appendStatement("htmlMessage += "+quote("\\t<td colspan=\\\"3\\\">"));
				emptyline();
				//start subtable...
				
				appendStatement("htmlMessage += "+quote("\\n"));
				appendString("//Writing inner table: "+table.getName());
				appendStatement("htmlMessage += "+quote("<table width=\\\"100%\\\">"));
				
				//generate headers.
				List<MetaFormTableColumn> columns = table.getColumns();
				appendStatement("htmlMessage += "+quote("\\n"));
				appendStatement("htmlMessage += "+quote("<tr>"));
				for (int c=0; c<columns.size(); c++){
					MetaFormTableColumn col = columns.get(c);
					MetaFormTableHeader header = col.getHeader();
					appendStatement("htmlMessage += "+quote("\\n"));
					appendStatement("htmlMessage += "+quote("\\t<th width=\\\""+header.getWidth()+"\\\">"));
					appendStatement("htmlMessage += getDefaultResources().getMessage("+quote(header.getKey())+")");
					appendStatement("htmlMessage += "+quote("\\t</th>"));
															
				}
				appendStatement("htmlMessage += "+quote("</tr>"));
				appendStatement("htmlMessage += "+quote("\\n"));

				//generate data lines.
				for (int r=0; r<table.getRows(); r++){
					appendStatement("htmlMessage += "+quote("<tr>"));
					appendStatement("htmlMessage += "+quote("\\n"));
					for (int c=0; c<columns.size(); c++){
						MetaFormTableColumn col = (MetaFormTableColumn)columns.get(c);
						MetaFormTableHeader header = col.getHeader();
						appendStatement("htmlMessage += "+quote("\\t<td width=\\\""+header.getWidth()+"\\\">"));
						appendStatement("htmlMessage += form.get"+StringUtils.capitalize(table.getVariableName(r,c))+"()");
						appendStatement("htmlMessage += "+quote("\\t</td>"));
					}
					appendStatement("htmlMessage += "+quote("</tr>\\n"));
					
				}
				//end subtable
				appendStatement("htmlMessage += "+quote("</table>"));
				appendStatement("htmlMessage += "+quote("\\t</td>"));
				appendStatement("htmlMessage += "+quote("</tr>"));
			}
		}
		
		appendStatement("htmlMessage += "+quote("</table>"));

		emptyline();
		appendStatement("HtmlMailMessage mail = new HtmlMailMessage()");
		appendStatement("mail.setMessage(message)");
		appendStatement("mail.setHtmlContent(htmlMessage)");
		appendStatement("mail.setPlainTextContent(message)");
		appendStatement("mail.setSubject("+quote("WebSiteForm Submit: "+StringUtils.capitalize(form.getId()))+")");
		appendStatement("mail.setSender(\"\\\"WebForm\\\"<support@anotheria.net>\")");
			
		emptyline();
		appendString("//sending mail to "+targets.size()+" target(s)");
		appendString("for (int i=0; i<MAIL_TARGETS.length; i++){");
		increaseIdent();
		appendString("try{");
		increaseIdent();
		appendStatement("mail.setRecipient(MAIL_TARGETS[i])");	
		appendStatement("service.sendMessage(mail)");
		decreaseIdent();
		appendString("}catch(Exception e){");
		increaseIdent();
		appendStatement("e.printStackTrace()");
		closeBlockNEW();
		closeBlockNEW();
		emptyline();		
		
		appendStatement("return mapping.findForward(\"success\")");
		closeBlockNEW();
		emptyline();

		return clazz;
	}

    /**
	 * A helper generator object created for each generated document.
	 */
	class EnumerationPropertyGenerator{
		/**
		 * List of already generated properties (to avoid duplicated).
		 */
	    private List<String> generatedProperties;
	    /**
	     * Currently generated document.
	     */
	    private MetaDocument doc;
	    
	    EnumerationPropertyGenerator(MetaDocument aDoc){
	        generatedProperties = new ArrayList<String>();
	        doc = aDoc;
	    }
	    
	    public void generateEnumerationPropertyHandling(MetaEnumerationProperty mep, boolean editMode){
	    	appendGenerationPoint("generateEnumerationPropertyHandling");
	    	EnumerationType type = (EnumerationType )GeneratorDataRegistry.getInstance().getType(mep.getEnumeration());
			emptyline();
			String arrName = type.getName()+"_values";
		    String listName = arrName+"List";
			if (generatedProperties.indexOf(arrName)==-1){
			    appendString("//enumeration "+type.getName());
				appendStatement(EnumTypeGenerator.getEnumClassName(type) + "[] " + arrName + " = "+ EnumTypeGenerator.getEnumClassName(type) +".values()");
				appendStatement("List<LabelValueBean> "+listName+"Values"+" = new ArrayList<LabelValueBean>("+arrName+".length)");
				appendString("for ("+EnumTypeGenerator.getEnumClassName(type)+" element : " + arrName + ") {");
				increaseIdent();

				appendStatement("LabelValueBean bean = new LabelValueBean(\"\"+" + "element.getValue(), element.name())");
				appendStatement(listName+"Values.add(bean)");

			    closeBlockNEW();
			    generatedProperties.add(arrName);
			}else{
				appendString("//enumeration "+type.getName()+" already prepared.");
			}
			appendStatement("form."+mep.toBeanSetter()+"Collection("+listName + "Values)");
			if (editMode){
				openTry();
				appendStatement("form."+mep.toBeanSetter()+"CurrentValue("+EnumTypeGenerator.getEnumClassName(type)+".getConstantByValue("+doc.getVariableName()+"."+mep.toGetter()+"()).name())");
				appendCatch(ConstantNotFoundException.class);
				closeBlock("try");
			}
	    }
	}
	
	private String getShowActionRedirect(MetaDocument doc){
	    return quote(CMSMappingsConfiguratorGenerator.getPath(doc, CMSMappingsConfiguratorGenerator.ACTION_SHOW)+"?ts=")+"+System.currentTimeMillis()";
	}
	private String getEditActionRedirect(MetaDocument doc){
	    return quote(CMSMappingsConfiguratorGenerator.getPath(doc, CMSMappingsConfiguratorGenerator.ACTION_EDIT)+"?ts=")+"+System.currentTimeMillis()";
	}
}
