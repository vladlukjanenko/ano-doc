package net.anotheria.asg.generator.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.forms.meta.MetaForm;
import net.anotheria.asg.generator.forms.meta.MetaFormField;
import net.anotheria.asg.generator.forms.meta.MetaFormSingleField;
import net.anotheria.asg.generator.forms.meta.MetaFormTableColumn;
import net.anotheria.asg.generator.forms.meta.MetaFormTableField;
import net.anotheria.asg.generator.forms.meta.MetaFormTableHeader;
import net.anotheria.asg.generator.meta.MetaContainerProperty;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaEnumerationProperty;
import net.anotheria.asg.generator.meta.MetaGenericListProperty;
import net.anotheria.asg.generator.meta.MetaGenericProperty;
import net.anotheria.asg.generator.meta.MetaLink;
import net.anotheria.asg.generator.meta.MetaListProperty;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.MetaProperty;
import net.anotheria.asg.generator.meta.MetaTableProperty;
import net.anotheria.asg.generator.model.DataFacadeGenerator;
import net.anotheria.asg.generator.model.ServiceGenerator;
import net.anotheria.asg.generator.types.EnumerationGenerator;
import net.anotheria.asg.generator.types.meta.EnumerationType;
import net.anotheria.asg.generator.util.DirectLink;
import net.anotheria.asg.generator.view.meta.MetaDecorator;
import net.anotheria.asg.generator.view.meta.MetaDialog;
import net.anotheria.asg.generator.view.meta.MetaFieldElement;
import net.anotheria.asg.generator.view.meta.MetaFilter;
import net.anotheria.asg.generator.view.meta.MetaModuleSection;
import net.anotheria.asg.generator.view.meta.MetaView;
import net.anotheria.asg.generator.view.meta.MetaViewElement;
import net.anotheria.asg.generator.view.meta.MultilingualFieldElement;
import net.anotheria.util.ExecutionTimer;
import net.anotheria.util.StringUtils;

/**
 * This generator generate module-based actions like delete, create, edit, new, update, show and so on.
 * @author another
 */
public class ModuleActionsGenerator extends AbstractGenerator implements IGenerator {
    
    private MetaView view;
    private Context context;
    
    static final boolean USE_MULTIOP_ACTIONS = true;
    
    public ModuleActionsGenerator(MetaView aView){
        view = aView;
    }
	
	/* (non-Javadoc)
	 * @see net.anotheria.anodoc.generator.IGenerator#generate(net.anotheria.anodoc.generator.IGenerateable, net.anotheria.anodoc.generator.Context)
	 */
	public List<FileEntry> generate(IGenerateable g, Context context) {
		 List<FileEntry> files = new ArrayList<FileEntry>();
		
		this.context = context;
		
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
					MetaProperty pp = (MetaProperty)doc.getProperties().get(p);
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
	
	public static String getBaseActionName(MetaModuleSection section){
	    return "Base"+getActionSuffix(section);
	}
	
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
	
	private GeneratedClass generateMultiOpAction(MetaModuleSection section){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		MetaDocument doc = section.getDocument();

	    clazz.setPackageName(getPackage(section.getModule()));
	    clazz.addImport("net.anotheria.util.NumberUtils");
		addStandardActionImports(clazz);
		clazz.addImport(DataFacadeGenerator.getDocumentFactoryImport(context, doc));
		clazz.addImport(DataFacadeGenerator.getDocumentImport(context, doc));

		clazz.setName(getMultiOpActionName(section));
		clazz.setParent(getBaseActionName(section));

		startClassBody();
		appendString( getExecuteDeclaration(null));
	    increaseIdent();
	    appendStatement("String path = stripPath(mapping.getPath())");
	    //MOVE THIS TO MULTIOP WITHOUT DIALOG
	    writePathResolveForMultiOpAction(doc,StrutsConfigGenerator.ACTION_VERSIONINFO);
	    
	    appendStatement("throw new IllegalArgumentException("+quote("Unknown path: ")+"+path)");
	    append(closeBlock());
	    appendEmptyline();
	    
		
	    generateVersionInfoActionMethod(section, StrutsConfigGenerator.getPath(doc, StrutsConfigGenerator.ACTION_VERSIONINFO));
		return clazz;
	}
	
	private GeneratedClass generateMultiOpDialogAction(MetaModuleSection section){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		MetaDocument doc = section.getDocument();
		MetaDialog dialog = section.getDialogs().get(0);
		
		clazz.setName(getMultiOpDialogActionName(section));
		clazz.setParent(getBaseActionName(section));
		clazz.setPackageName(getPackage(section.getModule()));
	    
		
	    //write imports...
	    clazz.addImport("net.anotheria.util.NumberUtils");
	    addStandardActionImports(clazz);
		
	    clazz.addImport(DataFacadeGenerator.getDocumentFactoryImport(context, doc));
	    clazz.addImport(DataFacadeGenerator.getDocumentImport(context, doc));
	    clazz.addImport(ModuleBeanGenerator.getDialogBeanImport(context, dialog, doc));
		if (GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported() && doc.isMultilingual())
			clazz.addImport("net.anotheria.asg.data.MultilingualObject");

	    
		List<MetaViewElement> elements = createMultilingualList(dialog.getElements(), doc, context);
		for (int i=0; i<elements.size(); i++){
			MetaViewElement elem = elements.get(i);
			if (elem instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)elem;
				MetaProperty p = doc.getField(field.getName());
				if (p.getType().equals("image")){
					clazz.addImport("net.anotheria.webutils.filehandling.actions.FileStorage");
					clazz.addImport("net.anotheria.webutils.filehandling.beans.TemporaryFileHolder");
					break;
				}
			}
		}
	    appendEmptyline();

	    startClassBody();
		appendString( getExecuteDeclaration(null));
	    increaseIdent();
	    appendStatement("String path = stripPath(mapping.getPath())");
	    writePathResolveForMultiOpAction(doc,StrutsConfigGenerator.ACTION_DELETE);
	    writePathResolveForMultiOpAction(doc,StrutsConfigGenerator.ACTION_DUPLICATE);
	    writePathResolveForMultiOpAction(doc,StrutsConfigGenerator.ACTION_UPDATE);
	    if (GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported() && doc.isMultilingual()){
	    	writePathResolveForMultiOpAction(doc, StrutsConfigGenerator.ACTION_COPY_LANG);
	    	writePathResolveForMultiOpAction(doc, StrutsConfigGenerator.ACTION_SWITCH_MULTILANGUAGE_INSTANCE);
	    }
	    
	    appendStatement("throw new IllegalArgumentException("+quote("Unknown path: ")+"+path)");
	    append(closeBlock());
	    appendEmptyline();
	    
		
	    generateDeleteActionMethod(section, StrutsConfigGenerator.getPath(doc, StrutsConfigGenerator.ACTION_DELETE));
	    appendEmptyline();
	    generateDuplicateActionMethod(section, StrutsConfigGenerator.getPath(doc, StrutsConfigGenerator.ACTION_DUPLICATE));
	    appendEmptyline();
	    generateUpdateActionMethod(section, StrutsConfigGenerator.getPath(doc, StrutsConfigGenerator.ACTION_UPDATE));
	    appendEmptyline();
	    if (GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported() && doc.isMultilingual()){
	    	generateLanguageCopyActionMethod(section, StrutsConfigGenerator.getPath(doc, StrutsConfigGenerator.ACTION_COPY_LANG));
	    	appendEmptyline();
	    	generateSwitchMultilingualityActionMethod(section, StrutsConfigGenerator.getPath(doc, StrutsConfigGenerator.ACTION_SWITCH_MULTILANGUAGE_INSTANCE));
	    	appendEmptyline();
	    }
	    
	    return clazz;
	}
	
	private void writePathResolveForMultiOpAction(MetaDocument doc,String action){
		String path = StrutsConfigGenerator.getPath(doc, action);
		appendString("if (path.equals("+quote(path)+"))");
		appendIncreasedStatement("return "+path+"(mapping, af, req, res)");
	}

	private void writePathResolveForContainerMultiOpAction(MetaDocument doc, MetaContainerProperty container, String action){
		String path = StrutsConfigGenerator.getContainerPath(doc, container, action);
		appendString("if (path.equals("+quote(path)+"))");
		appendIncreasedStatement("return "+path+"(mapping, af, req, res)");
	}
	
	private GeneratedClass generateShowAction(MetaModuleSection section){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz)
		;
	    MetaDocument doc = section.getDocument();
		List<MetaViewElement> elements = section.getElements();
	    
	    boolean containsComparable = section.containsComparable();
	    
	    clazz.setPackageName(getPackage(section.getModule()));

	    clazz.addImport("java.util.List");
	    clazz.addImport("java.util.ArrayList");
	    clazz.addImport("net.anotheria.asg.util.decorators.IAttributeDecorator");
	    clazz.addImport("net.anotheria.asg.util.filter.DocumentFilter");
	    clazz.addImport("net.anotheria.util.NumberUtils");
	    addStandardActionImports(clazz);
	    clazz.addImport(DataFacadeGenerator.getDocumentImport(context, doc));
	    clazz.addImport(ModuleBeanGenerator.getListItemBeanImport(context, doc));
		
		clazz.addImport("net.anotheria.util.slicer.Slicer");
		clazz.addImport("net.anotheria.util.slicer.Slice");
		clazz.addImport("net.anotheria.util.slicer.Segment");
		clazz.addImport("net.anotheria.asg.util.bean.PagingLink");
		
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
					clazz.addImport(EnumerationGenerator.getUtilsImport(type));
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
	    
	    boolean containsDecorators = neededDecorators.size() >0;
	    
		if (containsComparable){
			clazz.addImport(ModuleBeanGenerator.getListItemBeanSortTypeImport(context, doc));
			clazz.addImport("net.anotheria.util.sorter.Sorter");
			clazz.addImport("net.anotheria.util.sorter.QuickSorter");

			appendStatement("private Sorter<", ModuleBeanGenerator.getListItemBeanName(doc), "> sorter");
			appendEmptyline();
		}
		
		if (containsDecorators){
			for (int i=0; i<elements.size();i++){
				MetaViewElement element = (MetaViewElement)elements.get(i);
				if (element.getDecorator()!=null){
					appendStatement("private IAttributeDecorator "+getDecoratorVariableName(element));
				}
			}
			appendEmptyline();
		}
		
		if (section.getFilters().size()>0){
			for (MetaFilter f : section.getFilters()){
				appendStatement("private DocumentFilter "+getFilterVariableName(f));
			}
			appendEmptyline();
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
			appendIncreasedStatement("log.fatal(\"Couldn't instantiate decorator:\", e)");
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
			appendIncreasedStatement("log.fatal(\"Couldn't instantiate filter:\", e)");
			appendString( "}");
		}
	    append(closeBlock());
		

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
			    append(closeBlock());
		    	appendStatement("req.setAttribute("+quote("currentFilterParameter"+i)+", "+filterParameterName+")");
		    	appendEmptyline();
	    	}
	    }
	    
	    //check if its sortable.
		if (containsComparable){
			String sortType = ModuleBeanGenerator.getListItemBeanSortTypeName(doc);
			appendStatement("int sortMethod = "+sortType+".SORT_BY_DEFAULT");
			appendStatement("boolean sortOrder = "+sortType+".ASC");
			appendStatement("boolean sortParamSet = false");
			appendEmptyline();
			appendString( "try{");
			appendIncreasedStatement("sortMethod = getIntParameter(req, PARAM_SORT_TYPE)");
			appendIncreasedStatement("sortParamSet = true");
			appendString( "}catch(Exception ignored){}");
			appendEmptyline	();    
			appendString( "try{");
			appendIncreasedStatement("String sortMethodName = getStringParameter(req, PARAM_SORT_TYPE_NAME)");
			appendIncreasedStatement("sortMethod = "+sortType+".name2method(sortMethodName)");
			appendIncreasedStatement("sortParamSet = true");
			appendString( "}catch(Exception ignored){}");
			appendEmptyline	    ();
			appendString( "try{");
			increaseIdent();
			appendString( "sortOrder = getStringParameter(req, PARAM_SORT_ORDER).equals("+quote(ViewConstants.VALUE_SORT_ORDER_ASC)+") ? ");
			appendIncreasedStatement(""+sortType+".ASC : "+sortType+".DESC");
			decreaseIdent();
			appendString( "}catch(Exception ignored){}");
			appendEmptyline();
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
		    append(closeBlock());
			appendStatement("req.setAttribute("+quote("currentSortCode")+", sortType.getMethodAndOrderCode())");
			appendEmptyline();
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
		    append(closeBlock());
	    }else{
		    appendStatement("List<"+doc.getName()+"> "+listName+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getMultiple()+"()");
	    }

		appendStatement("List<"+ModuleBeanGenerator.getListItemBeanName(doc)+"> beans = new ArrayList<"+ModuleBeanGenerator.getListItemBeanName(doc)+">("+listName+".size())");
		appendString("for ("+doc.getName()+" "+doc.getVariableName()+" : "+listName+"){");
		increaseIdent();
		appendStatement(ModuleBeanGenerator.getListItemBeanName(doc)+" bean = "+getMakeBeanFunctionName(ModuleBeanGenerator.getListItemBeanName(doc))+"("+doc.getVariableName()+")");
		appendStatement("beans.add(bean)");
	    append(closeBlock());
	    appendEmptyline();
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
	    appendEmptyline();
	    
	    appendCommentLine("prepare paging links");
	    appendStatement("ArrayList<PagingLink> pagingLinks = new ArrayList<PagingLink>()");
		appendStatement("pagingLinks.add(new PagingLink(slice.isFirstSlice() ? null : \"1\", \"|<<\"))");
		appendStatement("pagingLinks.add(new PagingLink(slice.hasPrevSlice() ? \"\"+(slice.getCurrentSlice()-1) : null, \"<<\"))");
		
		appendString( "for (int i=1; i<slice.getCurrentSlice(); i++){");
		increaseIdent();
		appendString( "if (slice.getCurrentSlice()-i<=7)");
		appendIncreasedStatement("pagingLinks.add(new PagingLink(\"\"+i,\"\"+i))");
	    append(closeBlock());
		
		appendStatement("pagingLinks.add(new PagingLink(null, \"Page \"+(slice.getCurrentSlice()+\" of \"+slice.getTotalNumberOfSlices())))");
		
		appendString( "for (int i=slice.getCurrentSlice()+1; i<=slice.getTotalNumberOfSlices(); i++){");
		increaseIdent();
		appendString( "if (i-slice.getCurrentSlice()<=7)");
		appendIncreasedStatement("pagingLinks.add(new PagingLink(\"\"+i,\"\"+i))");
	    append(closeBlock());
		
		
		appendStatement("pagingLinks.add(new PagingLink(slice.hasNextSlice() ?  \"\"+(slice.getCurrentSlice()+1) : null, \">>\"))");
		appendStatement("pagingLinks.add(new PagingLink(slice.isLastPage() ? null : \"\"+slice.getTotalNumberOfSlices(), \">>|\"))");
	    appendCommentLine(" paging links end");
	    
	    appendStatement("req.setAttribute("+quote("paginglinks")+", pagingLinks)");
	    appendStatement("req.setAttribute("+quote("currentpage")+", pageNumber)");
	    appendStatement("req.setAttribute("+quote("currentItemsOnPage")+", itemsOnPage)");
	    appendStatement("req.getSession().setAttribute("+quote("currentItemsOnPage")+", itemsOnPage)");
	    appendStatement("req.setAttribute("+quote("PagingSelector")+", ITEMS_ON_PAGE_SELECTOR)");
	    appendEmptyline();
	    //paging end
	    
	    
	    
	    appendStatement("addBeanToRequest(req, "+quote(listName)+", beans)");
	    
	    //add filters
	    for (MetaFilter f : section.getFilters()){
	    	appendStatement("addBeanToRequest(req, ", quote(getFilterVariableName(f)), ", ", getFilterVariableName(f), ".getTriggerer(\"\"))");
	    }
	    
	    appendStatement("return mapping.findForward(\"success\")");
	    append(closeBlock());
	    appendEmptyline();
	    
	    
	    // BEAN creation function
	    appendString( "protected "+ModuleBeanGenerator.getListItemBeanName(doc)+" "+getMakeBeanFunctionName(ModuleBeanGenerator.getListItemBeanName(doc))+"("+doc.getName()+" "+doc.getVariableName()+"){");
	    increaseIdent();
	    appendStatement(ModuleBeanGenerator.getListItemBeanName(doc)+" bean = new "+ModuleBeanGenerator.getListItemBeanName(doc)+"()");
	    //set the properties.
	    //this is a hack...
	    appendStatement("bean.setPlainId("+doc.getVariableName()+".getId())");

		elements = createMultilingualList(elements, doc, context);
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
					String value = "";
					if (p instanceof MetaEnumerationProperty){
						MetaEnumerationProperty mep = (MetaEnumerationProperty)p;
						EnumerationType type = (EnumerationType)GeneratorDataRegistry.getInstance().getType(mep.getEnumeration());
						value = EnumerationGenerator.getUtilsClassName(type)+".getName("+doc.getVariableName()+".get"+p.getAccesserName()+"())"; 
					}else {
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
					}
					appendStatement("bean."+p.toBeanSetter(lang)+"("+value+")");
//				}
			}
		}
		
		appendStatement("bean.setDocumentLastUpdateTimestamp(NumberUtils.makeISO8601TimestampString("+doc.getVariableName()+".getLastUpdateTimestamp()))");
	    
	    appendStatement("return bean");
	    append(closeBlock());
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
					clazz.addImport(EnumerationGenerator.getUtilsImport(type));
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
	    append(closeBlock());
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
	    appendStatement("bean.setEditLink("+quote(StrutsConfigGenerator.getPath(doc, StrutsConfigGenerator.ACTION_EDIT)+"?pId=")+"+entry.getMatchedDocument().getId()+"+quote("&ts=")+"+System.currentTimeMillis())");
	    appendStatement("bean.setDocumentId(entry.getMatchedDocument().getId())");
	    appendStatement("bean.setPropertyName(entry.getMatchedProperty().getId())");
	    appendStatement("bean.setInfo(entry.getInfo().toHtml())");
	    appendStatement("beans.add(bean)");
	    append(closeBlock());
	    appendStatement("req.setAttribute("+quote("result")+", beans)");
	    append(closeBlock());
	    
	    appendStatement("return mapping.findForward(\"success\")");
	    append(closeBlock());
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
			
				clazz.addImport(DataFacadeGenerator.getDocumentImport(context, targetDocument));
			
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
		appendEmptyline();
		
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
				append(closeBlock());
				appendStatement("addBeanToRequest(req, "+quote(targetDocument.getMultiple().toLowerCase())+", "+targetDocument.getMultiple().toLowerCase()+"Beans)"); 
				appendEmptyline();
				
				linkTargets.add(lt);
			}
			
		}
		
		
		appendStatement("return mapping.findForward(\"success\")");
		append(closeBlock());
		return clazz;
	}

	private GeneratedClass generateExecuteQueryAction(MetaModuleSection section){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		MetaDocument doc = section.getDocument();
		//List<MetaViewElement> elements = section.getElements();
	    
		clazz.setPackageName(getPackage(section.getModule()));
		addStandardActionImports(clazz);
		clazz.addImport("java.util.List");
		clazz.addImport("java.util.ArrayList");
		clazz.addImport(DataFacadeGenerator.getDocumentImport(context, doc));
		clazz.addImport(ModuleBeanGenerator.getListItemBeanImport(context, doc));
		
		clazz.setName(getExecuteQueryActionName(section));
		clazz.setParent(getShowActionName(section));

		startClassBody();
		appendString(getExecuteDeclaration());
		increaseIdent();
		appendEmptyline();
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
		append(closeBlock());
		appendEmptyline();
		appendStatement("addBeanToRequest(req, "+quote(listName)+", beans)");
		
		
		appendStatement("return mapping.findForward(\"success\")");
		append(closeBlock());

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

	    clazz.addImport(DataFacadeGenerator.getDocumentImport(context, doc));
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
	    
		append(closeBlock()); 
	}
	
	private GeneratedClass generateUpdateAction(MetaModuleSection section){
		if (USE_MULTIOP_ACTIONS)
			return null;
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		MetaDocument doc = section.getDocument();
		MetaDialog dialog = section.getDialogs().get(0);
		clazz.setPackageName(getPackage(section.getModule()));
		addStandardActionImports(clazz);
		clazz.addImport(ModuleBeanGenerator.getDialogBeanImport(context, dialog, doc));
		clazz.addImport(DataFacadeGenerator.getDocumentImport(context, doc));
		clazz.addImport(DataFacadeGenerator.getDocumentFactoryImport(context, doc));
	    
		List<MetaViewElement> elements = createMultilingualList(dialog.getElements(), doc, context);
		for (int i=0; i<elements.size(); i++){
			MetaViewElement elem = elements.get(i);
			if (elem instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)elem;
				MetaProperty p = doc.getField(field.getName());
				if (p.getType().equals("image")){
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

	private void generateUpdateActionMethod(MetaModuleSection section, String methodName){
		MetaDocument doc = section.getDocument();
		MetaDialog dialog = section.getDialogs().get(0);
		List<MetaViewElement> elements = createMultilingualList(dialog.getElements(), doc, context);

		appendString( getExecuteDeclaration(methodName));
		increaseIdent();
	
		appendStatement(ModuleBeanGenerator.getDialogBeanName(dialog, doc)+" form = ("+ModuleBeanGenerator.getDialogBeanName(dialog, doc)+") af");
		//check if we have a form submission at all.
		appendString( "if (!form.isFormSubmittedFlag())");
		appendIncreasedStatement("throw new RuntimeException(\"Request broken!\")");
		//if update, then first get the target object.
		appendStatement("boolean create = false");
		appendStatement(doc.getName()+" "+doc.getVariableName()+" = null");
		appendString( "if (form.getId()!=null && form.getId().length()>0){");	
		appendIncreasedString(doc.getVariableName()+" = ("+doc.getName()+")"+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(form.getId()).clone();");
		appendString( "}else{");
		increaseIdent();
		appendString( doc.getVariableName()+" = "+DataFacadeGenerator.getDocumentFactoryName(doc)+".create"+doc.getName()+"();");
		appendString( "create = true;");
		append(closeBlock()); ;
		appendEmptyline();
		
		appendStatement("String nextAction = req.getParameter("+quote("nextAction")+")");
		appendString( "if (nextAction == null || nextAction.length() == 0)");
		appendIncreasedStatement("nextAction = \"close\"");
		appendEmptyline();
		
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
					if (p.getType().equals("image")){
						//will work only with one image.
						appendString( "//handle image");
						appendStatement("TemporaryFileHolder holder = FileStorage.getTemporaryFile(req)");
						appendString( "if (holder!=null && holder.getData()!=null){");
						increaseIdent();
						appendStatement("FileStorage.storeFilePermanently(req, holder.getFileName())");
						appendStatement(doc.getVariableName()+"."+p.toSetter()+"(holder.getFileName())");
						appendStatement("FileStorage.removeTemporaryFile(req)");
						append(closeBlock()); ;
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
		
		appendEmptyline();
		appendStatement(doc.getName(), " updatedCopy = null");
		
		appendString( "if (create){");
		//appendIncreasedStatement("System.out.println(\"creating\")");
		appendIncreasedStatement("updatedCopy = "+getServiceGetterCall(section.getModule())+".create"+doc.getName()+"("+doc.getVariableName()+")");
		appendString( "}else{");
		appendIncreasedStatement("updatedCopy = "+getServiceGetterCall(section.getModule())+".update"+doc.getName()+"( "+doc.getVariableName()+")");
		//appendIncreasedStatement("System.out.println(\"updating\")");
		appendString( "}");
		appendString( "if (nextAction.equalsIgnoreCase("+quote("stay")+"))");
	    appendIncreasedStatement("res.sendRedirect("+getEditActionRedirect(doc)+"+"+quote("&pId=")+"+updatedCopy.getId())");
		appendString( "else");
	    appendIncreasedStatement("res.sendRedirect("+getShowActionRedirect(doc)+")");
	    appendStatement("return null");
		append(closeBlock()); ;
	}
	
	private GeneratedClass generateSwitchMultilingualityAction(MetaModuleSection section){
		if (USE_MULTIOP_ACTIONS)
			return null;
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		MetaDocument doc = section.getDocument();
		clazz.setPackageName(getPackage(section.getModule()));

		//write imports...
		addStandardActionImports(clazz);
	    clazz.addImport(DataFacadeGenerator.getDocumentImport(context, doc));
	    clazz.addImport("net.anotheria.asg.data.MultilingualObject");

	    clazz.setTypeComment("This class enables or disables support for multiple languages for a particular document.");
		clazz.setName(getSwitchMultilingualityActionName(section));
		clazz.setParent(getBaseActionName(section));

		startClassBody();
		generateSwitchMultilingualityActionMethod(section, null);
		return clazz;
	}
	
	private void generateSwitchMultilingualityActionMethod(MetaModuleSection section, String methodName){
	    
		MetaDocument doc = section.getDocument();
	    
		appendString( getExecuteDeclaration(methodName));
		increaseIdent();
		
		appendStatement("String id = getStringParameter(req, PARAM_ID)");
		appendStatement("String value = getStringParameter(req, "+quote("value")+")");

		appendStatement(doc.getName()+" "+doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");
		appendStatement("((MultilingualObject)"+doc.getVariableName()+").setMultilingualDisabledInstance(Boolean.valueOf(value))");
		appendStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
	    appendStatement("res.sendRedirect("+getEditActionRedirect(doc)+"+"+quote("&pId=")+"+id)");
		
	    appendStatement("return null");
		append(closeBlock()); //end doExecute
	}

	
	private GeneratedClass generateLanguageCopyAction(MetaModuleSection section){
		if (USE_MULTIOP_ACTIONS)
			return null;
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		MetaDocument doc = section.getDocument();
		clazz.setPackageName(getPackage(section.getModule()));

		//write imports...
		addStandardActionImports(clazz);
	    clazz.addImport(DataFacadeGenerator.getDocumentImport(context, doc));
	    
	    clazz.setTypeComment("This class copies multilingual contents from one language to another in a given document");
		clazz.setName(getLanguageCopyActionName(section));
		clazz.setParent(getBaseActionName(section));

		startClassBody();
		generateLanguageCopyActionMethod(section, null);
		
		return clazz;
	}
	
	private void generateLanguageCopyActionMethod(MetaModuleSection section, String methodName){
	    
		MetaDocument doc = section.getDocument();
		appendString( getExecuteDeclaration(methodName));
		increaseIdent();
		
		appendStatement("String sourceLanguage = req.getParameter("+quote("pSrcLang")+")");
		appendString( "if (sourceLanguage==null || sourceLanguage.length()==0)");
		appendIncreasedStatement("throw new RuntimeException("+quote("No source language")+")");
		appendEmptyline();

		appendStatement("String destLanguage = req.getParameter("+quote("pDestLang")+")");
		appendString( "if (destLanguage==null || destLanguage.length()==0)");
		appendIncreasedStatement("throw new RuntimeException("+quote("No destination language")+")");
		appendEmptyline();

		appendStatement("String id = getStringParameter(req, PARAM_ID)");
		appendStatement(doc.getName()+" "+doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");
		appendStatement(doc.getVariableName()+"."+DataFacadeGenerator.getCopyMethodName()+"(sourceLanguage, destLanguage)");
		appendStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
	    appendStatement("res.sendRedirect("+getEditActionRedirect(doc)+"+"+quote("&pId=")+"+id)");
		
	    appendStatement("return null");
		append(closeBlock()); ; //end doExecute
	}

	private GeneratedClass generateEditAction(MetaModuleSection section){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		MetaDocument doc = section.getDocument();
		MetaDialog dialog = section.getDialogs().get(0);
		List<MetaViewElement> elements = createMultilingualList(dialog.getElements(), doc, context);
		
		clazz.setPackageName(getPackage(section.getModule()));
		addStandardActionImports(clazz);
		clazz.addImport(ModuleBeanGenerator.getDialogBeanImport(context, dialog, doc));
		clazz.addImport(DataFacadeGenerator.getDocumentImport(context, doc));
		clazz.addImport("net.anotheria.asg.util.helper.cmsview.CMSViewHelperUtil");
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
					clazz.addImport(EnumerationGenerator.getUtilsImport(type));
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
    			clazz.addImport(DataFacadeGenerator.getDocumentImport(context, l.getDocument()));
	    	}
	    }
		
		startClassBody();
		
		clazz.setName(getEditActionName(section));
		clazz.setParent(getShowActionName(section));

		startClassBody();
		generateEditActionMethod(section, "anoDocExecute");

		return clazz;

	}

	private void generateEditActionMethod(MetaModuleSection section, String methodname){

		MetaDocument doc = section.getDocument();
		MetaDialog dialog = section.getDialogs().get(0);
		List<MetaViewElement> elements = createMultilingualList(dialog.getElements(), doc, context);
		EnumerationPropertyGenerator enumProGenerator = new EnumerationPropertyGenerator(doc);
	    List<DirectLink> backlinks = GeneratorDataRegistry.getInstance().findLinksToDocument(doc);

		
		appendString( getExecuteDeclaration(methodname));
		increaseIdent();
	
		appendStatement("String id = getStringParameter(req, PARAM_ID)");
		appendStatement(ModuleBeanGenerator.getDialogBeanName(dialog, doc), " form = new ", ModuleBeanGenerator.getDialogBeanName(dialog, doc), "() ");	

		appendStatement(doc.getName()," ",doc.getVariableName()," = ",getServiceGetterCall(section.getModule()),".get",doc.getName(),"(id);");
		
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
		
		appendEmptyline();
		
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
					appendEmptyline();
					
					if (linkTargets.contains(link.getLinkTarget())){
						appendString( "//reusing collection for "+link.getName()+" to "+link.getLinkTarget()+".");
					}else{
					
						appendString( "//link "+link.getName()+" to "+link.getLinkTarget());
						appendString( "//to lazy to include List in the imports.");
						appendStatement("List<"+DataFacadeGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), targetDocument)+"> "+listName+" = "+getServiceGetterCall(targetModule)+".get"+targetDocument.getMultiple()+"()");
						appendStatement("List<LabelValueBean> "+listName+"Values = new ArrayList<LabelValueBean>("+listName+".size()+1)");
						appendStatement(listName+"Values.add(new LabelValueBean("+quote("")+", \"-----\"))");
						appendString( "for ("+(DataFacadeGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), targetDocument))+" "+targetDocument.getVariableName()+" : "+listName+"){");
						increaseIdent();
						appendStatement("LabelValueBean bean = new LabelValueBean("+targetDocument.getVariableName()+".getId(), "+targetDocument.getVariableName()+".getName() )");
						appendStatement(listName,"Values.add(bean)");
						append(closeBlock()); ;

					}

					String lang = getElementLanguage(element);
					appendStatement("form."+p.toBeanSetter()+"Collection"+(lang==null ? "":lang)+"("+listName+"Values"+")");
					
					appendString( "try{");
					increaseIdent();
					String getter = getServiceGetterCall(targetModule)+".get"+targetDocument.getName()+"("+doc.getVariableName()+"."+p.toGetter()+"()).getName()";
					appendStatement("form."+p.toBeanSetter()+"CurrentValue"+(lang==null ? "":lang)+"("+getter+")");
					decreaseIdent();
					appendString( "}catch(Exception e){");
					appendIncreasedStatement("form."+p.toBeanSetter()+"CurrentValue"+(lang==null ? "":lang)+"("+quote("none")+")");
					appendString( "}");
					linkTargets.add(link.getLinkTarget());
					
				}
				
				if (p instanceof MetaEnumerationProperty){
				    append(enumProGenerator.generateEnumerationPropertyHandling((MetaEnumerationProperty)p, true));
				}
			}
		}
		
		
		appendStatement("addBeanToRequest(req, "+quote("objectId")+" , "+doc.getVariableName()+".getId())");
		appendStatement("addBeanToRequest(req, "+quote(StrutsConfigGenerator.getDialogFormName(dialog, doc))+" , form)");
		appendStatement("addBeanToRequest(req, "+quote("objectInfoString")+" , "+doc.getVariableName()+".getObjectInfo().toString())");
		appendStatement("addBeanToRequest(req, "+quote("save.label.prefix")+", "+quote("Update")+")");
		
		//add field descriptions ...
		appendStatement("String fieldDescription = null");
		for (MetaProperty p : doc.getProperties()){
			appendStatement("fieldDescription = CMSViewHelperUtil.getFieldExplanation("+quote(doc.getParentModule().getName()+"."+doc.getName())+ ", "+doc.getVariableName()+", "+quote(p.getName())+")");
			appendString( "if (fieldDescription!=null && fieldDescription.length()>0)");
			appendIncreasedStatement("req.setAttribute("+quote("description."+p.getName())+", fieldDescription)");
		}
	
	    if (backlinks.size()>0){
			appendEmptyline();
			appendCommentLine("Generating back link handling...");
	    	appendStatement("List<LinkToMeBean> linksToMe = findLinksToCurrentDocument("+doc.getVariableName()+".getId())");
	    	appendString( "if (linksToMe.size()>0)");
	    	appendIncreasedStatement("req.setAttribute("+quote("linksToMe")+", linksToMe)");
	    }

		
		appendStatement("return mapping.findForward(\"success\")");
		append(closeBlock()); 
		appendEmptyline();
		
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
				appendIncreasedStatement("log.warn(\""+methodName+"(\"+documentId+\")\", ignored)");
				appendString( "}");
			}
			appendStatement("return ret");
			append(closeBlock()); ;
			
			for (DirectLink l : backlinks){
				if (l.getProperty().isMultilingual()){
					for (String lang : context.getLanguages()){
						appendString( "private List<LinkToMeBean> findLinkToCurrentDocumentIn"+l.getModule().getName()+l.getDocument().getName()+StringUtils.capitalize(l.getProperty().getName(lang))+"(String documentId) throws "+ServiceGenerator.getExceptionImport(context,l.getModule())+"{");
						increaseIdent();
						appendStatement("List<LinkToMeBean> ret = new ArrayList<LinkToMeBean>()");
						appendStatement("QueryProperty p = new QueryProperty("+l.getDocument().getName()+"."+l.getProperty().toNameConstant(lang)+", documentId)");
						//appendStatement("List<"+l.getDocument().getName()+"> list = "+getServiceGetterCall(l.getModule())+".get"+l.getDocument().getMultiple()+"ByProperty(p)");
						appendCommentLine("temporarly - replacy with query property");
						appendStatement("List<"+l.getDocument().getName()+"> list = "+getServiceGetterCall(l.getModule())+".get"+l.getDocument().getMultiple()+"ByProperty(p.getName(), p.getValue())");
						appendString( "for ("+l.getDocument().getName() +" doc : list ){");
						increaseIdent();
						appendStatement("ret.add(new LinkToMeBean(doc, "+quote(l.getProperty().getName())+"))");
						append(closeBlock()); ;
						appendStatement("return ret");
						append(closeBlock()); ;
					}
				}else{
					appendString( "private List<LinkToMeBean> findLinkToCurrentDocumentIn"+l.getModule().getName()+l.getDocument().getName()+StringUtils.capitalize(l.getProperty().getName())+"(String documentId) throws "+ServiceGenerator.getExceptionImport(context, l.getModule())+"{");
					increaseIdent();
					appendStatement("List<LinkToMeBean> ret = new ArrayList<LinkToMeBean>()");
					appendStatement("QueryProperty p = new QueryProperty("+l.getDocument().getName()+"."+l.getProperty().toNameConstant()+", documentId)");
					//appendStatement("List<"+l.getDocument().getName()+"> list = "+getServiceGetterCall(l.getModule())+".get"+l.getDocument().getMultiple()+"ByProperty(p)");
					appendCommentLine("temporarly - replacy with query property");
					appendStatement("List<"+l.getDocument().getName()+"> list = "+getServiceGetterCall(l.getModule())+".get"+l.getDocument().getMultiple()+"ByProperty(p.getName(), p.getValue())");
					appendString( "for ("+l.getDocument().getName() +" doc : list ){");
					increaseIdent();
					appendStatement("ret.add(new LinkToMeBean(doc, "+quote(l.getProperty().getName())+"))");
					append(closeBlock());
					appendStatement("return ret");
					append(closeBlock());
				}
			}
		}
	}

	private GeneratedClass generateDeleteAction(MetaModuleSection section){
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
	    appendStatement("String id = getStringParameter(req, PARAM_ID)");
	    appendStatement(getServiceGetterCall(section.getModule())+".delete"+doc.getName()+"(id)");
	    appendStatement("res.sendRedirect("+getShowActionRedirect(doc)+")");
	    appendStatement("return null");
	    append(closeBlock());
	    
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
		clazz.addImport(DataFacadeGenerator.getDocumentFactoryImport(context, doc));
		clazz.addImport(DataFacadeGenerator.getDocumentImport(context, doc));
		
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
		appendStatement(doc.getName()+" "+doc.getVariableName()+"Src = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id);");
		appendStatement(doc.getName()+" "+doc.getVariableName()+"Dest = "+DataFacadeGenerator.getDocumentFactoryName(doc)+".create"+doc.getName()+"("+doc.getVariableName()+"Src)");

		appendStatement(doc.getName()+" "+doc.getVariableName()+"Created = "+getServiceGetterCall(section.getModule())+".create"+doc.getName()+"("+doc.getVariableName()+"Dest"+")");
	    appendStatement("res.sendRedirect("+getEditActionRedirect(doc)+"+"+quote("&")+"+PARAM_ID+"+quote("=")+"+"+doc.getVariableName()+"Created.getId()"+")");
	    appendStatement("return null");
	    append(closeBlock());
	}

	private GeneratedClass generateNewAction(MetaModuleSection section){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		
		MetaDocument doc = section.getDocument();
		MetaDialog dialog = section.getDialogs().get(0);
		//List<MetaViewElement> elements = dialog.getElements();
		List<MetaViewElement> elements = createMultilingualList(dialog.getElements(), doc, context);
		
		EnumerationPropertyGenerator enumPropGen = new EnumerationPropertyGenerator(doc);
		
		clazz.setPackageName(getPackage(section.getModule()));
	    
		//write imports...
		addStandardActionImports(clazz);
		clazz.addImport(ModuleBeanGenerator.getDialogBeanImport(context, dialog, doc));
	    
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
				}
			}
		}

		//check if we have to property definition files.
		HashMap<String, MetaEnumerationProperty> importedEnumerations = new HashMap<String, MetaEnumerationProperty>();
		
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = (MetaViewElement)elements.get(i);
			if (element instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)element;
				MetaProperty p = doc.getField(field.getName());
				if (p instanceof MetaEnumerationProperty){
					MetaEnumerationProperty mep = (MetaEnumerationProperty)p;
					EnumerationType type = (EnumerationType)GeneratorDataRegistry.getInstance().getType(mep.getEnumeration());
					clazz.addImport(EnumerationGenerator.getUtilsImport(type));
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
		
		Set<String> linkTargets = new HashSet<String>();
		
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = (MetaViewElement)elements.get(i);
			if (element instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)element;
				MetaProperty p = doc.getField(field.getName());
				if (p.isLinked()){
					MetaLink link = (MetaLink)p;

					MetaModule targetModule = link.isRelative() ? 
							doc.getParentModule() : 
							GeneratorDataRegistry.getInstance().getModule(link.getTargetModuleName());
					String tDocName = link.getTargetDocumentName(); 
					MetaDocument targetDocument = targetModule.getDocumentByName(tDocName);
					String listName = targetDocument.getMultiple().toLowerCase();
					appendEmptyline();
					
					if (linkTargets.contains(link.getLinkTarget())){
						appendString( "//link "+link.getName()+" to "+link.getLinkTarget()+" reuses collection.");
					}else{
						appendString( "//link "+link.getName()+" to "+link.getLinkTarget());
						appendStatement("List<"+DataFacadeGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), targetDocument)+"> "+listName+" = "+getServiceGetterCall(targetModule)+".get"+targetDocument.getMultiple()+"()");
						appendStatement("List<LabelValueBean> "+listName+"Values = new ArrayList<LabelValueBean>("+listName+".size()+1)");
						appendStatement(listName+"Values.add(new LabelValueBean("+quote("")+", \"-----\"))");
						appendString( "for ("+(DataFacadeGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), targetDocument))+" "+targetDocument.getVariableName()+" : "+listName+"){");
						increaseIdent();
						
						appendStatement("LabelValueBean bean = new LabelValueBean("+targetDocument.getVariableName()+".getId(), "+targetDocument.getVariableName()+".getName() )");
						appendStatement(listName+"Values.add(bean)");
						append(closeBlock());
					}
					
					String lang = getElementLanguage(element);
					appendStatement("form."+p.toBeanSetter()+"Collection"+(lang==null ? "" : lang)+"("+listName+"Values"+")");
					linkTargets.add(link.getLinkTarget());
				}//...end if (p.isLinked())

				if (p instanceof MetaEnumerationProperty){
					append(enumPropGen.generateEnumerationPropertyHandling((MetaEnumerationProperty)p, false));
				}
				
			}
		}

		appendEmptyline();
		appendStatement("addBeanToRequest(req, "+quote(StrutsConfigGenerator.getDialogFormName(dialog, doc))+" , form)");
		appendStatement("addBeanToRequest(req, "+quote("save.label.prefix")+", "+quote("Create")+")");
		appendStatement("addBeanToRequest(req, "+quote("objectInfoString")+" , "+quote("none")+")");


		appendStatement("return mapping.findForward(\"success\")");
		append(closeBlock());

		return clazz;
	}

	private GeneratedClass generateBaseAction(MetaModuleSection section){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);

	    clazz.setPackageName(getPackage(section.getModule()));
	    clazz.setAbstractClass(true);
	    
	    
	    appendEmptyline();
	    clazz.addImport(context.getPackageName(MetaModule.SHARED)+".action."+BaseViewActionGenerator.getViewActionName(view));
	    appendEmptyline();
	    
	    clazz.setName(getBaseActionName(section));
	    clazz.setParent(BaseViewActionGenerator.getViewActionName(view));

	    startClassBody();
	    //generate getTitle
	    appendString( "public String getTitle(){");
	    increaseIdent();
	    appendStatement("return "+quote(section.getTitle()));
	    append(closeBlock());
	    
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
	
	public static String getPackage(Context context, MetaDocument doc){
	    return context.getPackageName(doc)+".action";
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
	
	private String getExecuteDeclaration(String methodName){
	    String ret = "";
	    ret += "public ActionForward "+(methodName == null ? "anoDocExecute" : methodName ) + "(";
		ret += "ActionMapping mapping, ";
		ret += "ActionForm af, ";
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
	 * @deprecated use addStandardActionImports instead 
	 */
	@Deprecated 
	private void appendStandardActionImports(){
		appendStandardActionImports(getCurrentJobContent());
	}

	private void addStandardActionImports(GeneratedClass clazz){
	    clazz.addImport("javax.servlet.http.HttpServletRequest");
	    clazz.addImport("javax.servlet.http.HttpServletResponse");
	    clazz.addImport("org.apache.struts.action.ActionForm");
	    clazz.addImport("org.apache.struts.action.ActionForward");
	    clazz.addImport("org.apache.struts.action.ActionMapping");
	}
	
	private void appendStandardActionImports(StringBuilder target){
	    appendImport(target, "javax.servlet.http.HttpServletRequest");
	    appendImport(target, "javax.servlet.http.HttpServletResponse");
	    appendImport(target, "org.apache.struts.action.ActionForm");
	    appendImport(target, "org.apache.struts.action.ActionForward");
	    appendImport(target, "org.apache.struts.action.ActionMapping");
		emptyline(target);
		//TODO change this, its probably no need to store shared actions under action
		//ret += writeImport(GeneratorDataRegistry.getInstance().getContext().getTopPackageName()+".action.*");
		//ret += emptyline();
	}

	private String getStandardActionImports(){
	    String ret = "";
		ret += writeImport("javax.servlet.http.HttpServletRequest");
		ret += writeImport("javax.servlet.http.HttpServletResponse");
		ret += writeImport("org.apache.struts.action.ActionForm");
		ret += writeImport("org.apache.struts.action.ActionForward");
		ret += writeImport("org.apache.struts.action.ActionMapping");
		ret += emptyline();
		//TODO change this, its probably no need to store shared actions under action
		//ret += writeImport(GeneratorDataRegistry.getInstance().getContext().getTopPackageName()+".action.*");
		//ret += emptyline();
		return ret;
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
		
		MetaDocument doc = section.getDocument();

	    clazz.setPackageName(getPackage(section.getModule()));

	    //write imports...
		clazz.addImport("java.util.List");
		clazz.addImport("java.util.ArrayList");
		addStandardActionImports(clazz);
		clazz.addImport(DataFacadeGenerator.getDocumentFactoryImport(context, doc));
		clazz.addImport(DataFacadeGenerator.getDocumentImport(context, doc));
		clazz.addImport(ModuleBeanGenerator.getContainerEntryFormImport(doc, containerProperty));
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
				clazz.addImport(DataFacadeGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), targetDocument));
				clazz.addImport(DataFacadeGenerator.getSortTypeImport(targetDocument));
				clazz.addImport("net.anotheria.webutils.bean.LabelValueBean");
				clazz.addImport("net.anotheria.anodoc.data.NoSuchDocumentException");
				clazz.addImport("net.anotheria.util.StringUtils");
			}
			if(containedProperty instanceof MetaEnumerationProperty){
				clazz.addImport("net.anotheria.webutils.bean.LabelValueBean");
				EnumerationType type = (EnumerationType)GeneratorDataRegistry.getInstance().getType(((MetaEnumerationProperty) containedProperty).getEnumeration());
				clazz.addImport(EnumerationGenerator.getUtilsImport(type));
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
			writePathResolveForContainerMultiOpAction(doc, containerProperty, StrutsConfigGenerator.ACTION_SHOW);
			writePathResolveForContainerMultiOpAction(doc, containerProperty, StrutsConfigGenerator.ACTION_ADD);
			writePathResolveForContainerMultiOpAction(doc, containerProperty, StrutsConfigGenerator.ACTION_DELETE);
			writePathResolveForContainerMultiOpAction(doc, containerProperty, StrutsConfigGenerator.ACTION_MOVE);

			if (((MetaListProperty)containerProperty).getContainedProperty().isLinked()){
				writePathResolveForContainerMultiOpAction(doc, containerProperty, StrutsConfigGenerator.ACTION_QUICK_ADD);
			}
		}
	    
	    appendStatement("throw new IllegalArgumentException("+quote("Unknown path: ")+"+path)");
	    append(closeBlock());
	    appendEmptyline();
	    
		
		if (containerProperty instanceof MetaListProperty ){
			MetaListProperty list = (MetaListProperty)containerProperty;
			generateListShowActionMethod(section, list, StrutsConfigGenerator.getContainerPath(doc, containerProperty, StrutsConfigGenerator.ACTION_SHOW));
		    appendEmptyline();
			generateContainerDeleteEntryActionMethod(section, list, StrutsConfigGenerator.getContainerPath(doc, containerProperty, StrutsConfigGenerator.ACTION_DELETE));
		    appendEmptyline();
		    generateContainerMoveEntryActionMethod(section, list, StrutsConfigGenerator.getContainerPath(doc, containerProperty, StrutsConfigGenerator.ACTION_MOVE));
		    appendEmptyline();
			generateListAddRowActionMethod(section, list, StrutsConfigGenerator.getContainerPath(doc, containerProperty, StrutsConfigGenerator.ACTION_ADD));
		    appendEmptyline();
		    
		    if (list.getContainedProperty().isLinked()){
		    	generateListQuickAddActionMethod(section, list, StrutsConfigGenerator.getContainerPath(doc, containerProperty, StrutsConfigGenerator.ACTION_QUICK_ADD));
		    	appendEmptyline();
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
		if (USE_MULTIOP_ACTIONS)
			return null;
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);

		MetaDocument doc = section.getDocument(); 

		clazz.setPackageName(getPackage(section.getModule()));
		addStandardActionImports(clazz);
		clazz.addImport(DataFacadeGenerator.getDocumentImport(context, doc));
		clazz.addImport(ModuleBeanGenerator.getContainerEntryFormImport(doc, list));
		
		clazz.setName(getContainerAddEntryActionName(doc, list));
		clazz.setParent(getContainerShowActionName(doc, list));	

		generateListAddRowActionMethod(section, list, null);
		
		return clazz;
	}
	
	private void generateListAddRowActionMethod(MetaModuleSection section, MetaListProperty list, String methodName){
		MetaDocument doc = section.getDocument();
		appendString( getExecuteDeclaration(methodName));
		increaseIdent();
		appendStatement(ModuleBeanGenerator.getContainerEntryFormName(list)+" form = ("+ModuleBeanGenerator.getContainerEntryFormName(list)+") af");
		appendStatement("String id = form.getOwnerId()");
		appendStatement(doc.getName()+" "+doc.getVariableName());
		appendStatement(doc.getVariableName()," = ",getServiceGetterCall(section.getModule()),".get",doc.getName(),"(id)");
		
		String call = "";
		MetaProperty p = list.getContainedProperty();
		String getter = "form."+p.toBeanGetter()+"()";
		call += getter;
		appendStatement(doc.getVariableName()+"."+DataFacadeGenerator.getContainerEntryAdderName(list)+"("+call+")");
		appendStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		if (methodName==null)
			appendStatement("return "+getSuperCall());
		else
			appendStatement("return "+StrutsConfigGenerator.getContainerPath(doc, list, StrutsConfigGenerator.ACTION_SHOW)+"(mapping, af, req, res)");
		append(closeBlock());
	}
	
	private GeneratedClass generateListQuickAddAction(MetaModuleSection section, MetaListProperty list){
		if (USE_MULTIOP_ACTIONS)
			return null;

		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);

		MetaDocument doc = section.getDocument();

		clazz.setPackageName(getPackage(section.getModule()));
		addStandardActionImports(clazz);
		clazz.addImport(DataFacadeGenerator.getDocumentImport(context, doc));
		clazz.addImport(ModuleBeanGenerator.getContainerQuickAddFormImport(doc, list));
		clazz.addImport("net.anotheria.util.StringUtils");
		
		clazz.setName(getContainerQuickAddActionName(doc, list));
		clazz.setParent(getContainerShowActionName(doc, list));	

		startClassBody();
		generateListQuickAddActionMethod(section, list, null);
		
		return clazz;
	}

	private void generateListQuickAddActionMethod(MetaModuleSection section, MetaListProperty list, String methodName){

		MetaDocument doc = section.getDocument();

		appendString(getExecuteDeclaration(methodName));
		increaseIdent();
		appendStatement(ModuleBeanGenerator.getContainerQuickAddFormName(list)+" form = ("+ModuleBeanGenerator.getContainerQuickAddFormName(list)+") af");
		appendStatement("String id = form.getOwnerId()");
		appendStatement(doc.getName()+" "+doc.getVariableName());
		appendStatement(doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");
		appendStatement("String paramIdsToAdd = form.getQuickAddIds()");

		appendEmptyline();
		appendStatement("String idParameters[] = StringUtils.tokenize(paramIdsToAdd, ',')");
		appendString("for (String anIdParam : idParameters){");
		increaseIdent();
		
		appendString("String ids[] = StringUtils.tokenize(anIdParam, '-');");
		appendString("for (int i=Integer.parseInt(ids[0]); i<=Integer.parseInt(ids[ids.length-1]); i++){");
		increaseIdent();
		appendStatement(doc.getVariableName()+"."+DataFacadeGenerator.getContainerEntryAdderName(list)+"("+quote("")+"+i)");
		append(closeBlock());
		
		append(closeBlock());
		String call = "";
		MetaProperty p = list.getContainedProperty();
		String getter = "form."+p.toBeanGetter()+"()";
		call += getter;
		appendStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		if (methodName==null)
			appendStatement("return "+getSuperCall());
		else
			appendStatement("return "+StrutsConfigGenerator.getContainerPath(doc, list, StrutsConfigGenerator.ACTION_SHOW)+"(mapping, af, req, res)");
		append(closeBlock());
		
	}

	private GeneratedClass generateTableAddRowAction(MetaModuleSection section, MetaTableProperty table){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		MetaDocument doc = section.getDocument();
		clazz.setPackageName(getPackage(section.getModule()));
	    
		//write imports...
		addStandardActionImports(clazz);
		clazz.addImport(DataFacadeGenerator.getDocumentImport(context, doc));
		clazz.addImport(ModuleBeanGenerator.getContainerEntryFormImport(doc, table));
		
		clazz.setName(getContainerAddEntryActionName(doc, table));
		clazz.setParent(getContainerShowActionName(doc, table));	

		startClassBody();
		appendString(getExecuteDeclaration());
		increaseIdent();
		appendStatement(ModuleBeanGenerator.getContainerEntryFormName(table)+" form = ("+ModuleBeanGenerator.getContainerEntryFormName(table)+") af");
		appendStatement("String id = form.getOwnerId()");
		appendStatement(doc.getName()+" "+doc.getVariableName());
		appendStatement(doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");
		
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
		append(closeBlock());
		return clazz;
	}

	private GeneratedClass generateContainerDeleteEntryAction(MetaModuleSection section, MetaContainerProperty container){
		if (USE_MULTIOP_ACTIONS)
			return null;

		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		 
		MetaDocument doc = section.getDocument();

		clazz.setPackageName(getPackage(section.getModule()));
		addStandardActionImports(clazz);
		clazz.addImport(DataFacadeGenerator.getDocumentImport(context, doc));
		
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
		appendStatement("String id = getStringParameter(req, PARAM_ID)");
		appendStatement("int position = getIntParameter(req, "+quote("pPosition")+")"); 
		appendStatement(doc.getName()+" "+doc.getVariableName());
		appendStatement(doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");
		
		appendStatement(doc.getVariableName()+"."+DataFacadeGenerator.getContainerEntryDeleterName(container)+"(position)");
		appendStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		if (methodName==null)
			appendStatement("return "+getSuperCall());
		else
			appendStatement("return "+StrutsConfigGenerator.getContainerPath(doc, container, StrutsConfigGenerator.ACTION_SHOW)+"(mapping, af, req, res)");
		append(closeBlock());
	}

	private GeneratedClass generateContainerMoveEntryAction(MetaModuleSection section, MetaContainerProperty container){
		if (!(container instanceof MetaListProperty)){
			//TODO decomment
			//System.out.println("WARN moveUp only supported by lists, "+container+" is not a list");
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
		clazz.addImport(DataFacadeGenerator.getDocumentImport(context, doc));
		clazz.addImport("net.anotheria.asg.exception.ASGRuntimeException");
		clazz.addImport("java.util.List");
		
		clazz.setName(getContainerMoveEntryActionName(doc, container));
		clazz.setParent(getContainerShowActionName(doc, container));	

		startClassBody();
		generateContainerMoveEntryActionMethod(section, container, null);
		
		return clazz;

		
	}
	
	private void generateContainerMoveEntryActionMethod(MetaModuleSection section, MetaContainerProperty container, String methodName){
		MetaDocument doc = section.getDocument();
		MetaListProperty sourceProperty = (MetaListProperty)container;
		MetaGenericProperty generic = new MetaGenericListProperty(sourceProperty.getName(), sourceProperty.getContainedProperty());

		appendString(getExecuteDeclaration(methodName));
		increaseIdent();
		appendStatement("String id = getStringParameter(req, PARAM_ID)");
		appendStatement("int position = getIntParameter(req, "+quote("pPosition")+")");
		appendStatement("String direction = getStringParameter(req, "+quote("dir")+")");

		appendStatement(doc.getName()+" "+doc.getVariableName() + " = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");

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
			appendStatement("return "+StrutsConfigGenerator.getContainerPath(doc, container, StrutsConfigGenerator.ACTION_SHOW)+"(mapping, af, req, res)");
		append(closeBlock());
		appendEmptyline();
		
		String moveMethodParameter = doc.getName()+" "+doc.getVariableName()+", int position";
		
		appendString("private void moveUp("+moveMethodParameter+") throws ASGRuntimeException {");
		increaseIdent();
		appendString("if (position==0) ");
		appendIncreasedStatement("return");
		appendStatement(doc.getVariableName()+"."+DataFacadeGenerator.getContainerEntrySwapperName(container)+"(position, position-1)");
		appendStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		append(closeBlock());
		appendEmptyline();
		
		appendString("private void moveTop("+moveMethodParameter+") throws ASGRuntimeException {");
		increaseIdent();
		appendStatement(generic.toJavaType()+" targetList = "+doc.getVariableName()+".get"+container.getAccesserName()+"()");
		appendStatement(sourceProperty.getContainedProperty().toJavaType()+" toSwap = targetList.remove(position)");
		appendStatement("targetList.add(0, toSwap)");
		appendStatement(doc.getVariableName()+".set"+container.getAccesserName()+"(targetList)"); 
		appendStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		append(closeBlock());
		appendEmptyline();

		appendString("private void moveDown("+moveMethodParameter+") throws ASGRuntimeException {");
		increaseIdent();
		appendString("if (position<"+doc.getVariableName()+"."+DataFacadeGenerator.getContainerSizeGetterName(container)+"()-1){");
		increaseIdent();
		
		appendStatement(doc.getVariableName()+"."+DataFacadeGenerator.getContainerEntrySwapperName(container)+"(position, position+1)");
		appendStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		append(closeBlock());
		append(closeBlock());
		appendEmptyline();

		appendString("private void moveBottom("+moveMethodParameter+") throws ASGRuntimeException {");
		increaseIdent();
		appendStatement(generic.toJavaType()+" targetList = "+doc.getVariableName()+".get"+container.getAccesserName()+"()");
		appendStatement(sourceProperty.getContainedProperty().toJavaType()+" toSwap = targetList.remove(position)");
		appendStatement("targetList.add(toSwap)");
		appendStatement(doc.getVariableName()+".set"+container.getAccesserName()+"(targetList)"); 
		appendStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		append(closeBlock());
	}

	
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
		clazz.addImport(DataFacadeGenerator.getDocumentImport(context, doc));
		clazz.addImport(ModuleBeanGenerator.getContainerEntryFormImport(doc, list));
		if (list.getContainedProperty().isLinked()){
			clazz.addImport(ModuleBeanGenerator.getContainerQuickAddFormImport(doc, list));
			MetaLink link = (MetaLink)list.getContainedProperty();
			
			String tDocName = link.getTargetDocumentName(); 
			MetaModule targetModule = link.getLinkTarget().indexOf('.')== -1 ?
					doc.getParentModule() : GeneratorDataRegistry.getInstance().getModule(link.getTargetModuleName());
			MetaDocument targetDocument = targetModule.getDocumentByName(tDocName);
			clazz.addImport(DataFacadeGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), targetDocument));
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
		MetaDocument doc = section.getDocument();

		appendString( getExecuteDeclaration(methodName));
		increaseIdent();
		appendStatement("String id = getStringParameter(req, PARAM_ID)");
		appendStatement(doc.getName()+" "+doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id);");
		appendEmptyline();
		
		appendStatement(ModuleBeanGenerator.getContainerEntryFormName(list)+" form = new "+ModuleBeanGenerator.getContainerEntryFormName(list)+"() ");
		appendStatement("form.setPosition(-1)"); //hmm?
		appendStatement("form.setOwnerId("+doc.getVariableName()+".getId())");	
		appendStatement("addBeanToRequest(req, "+quote(StrutsConfigGenerator.getContainerEntryFormName(doc, list))+", form)");
		appendEmptyline();
		
		if (list.getContainedProperty().isLinked()){
			appendStatement(ModuleBeanGenerator.getContainerQuickAddFormName(list)+" quickAddForm = new "+ModuleBeanGenerator.getContainerQuickAddFormName(list)+"() ");
			appendStatement("quickAddForm.setOwnerId("+doc.getVariableName()+".getId())");	
			appendStatement("addBeanToRequest(req, "+quote(StrutsConfigGenerator.getContainerQuickAddFormName(doc, list))+", quickAddForm)");
			appendEmptyline();
		}

		if (list.getContainedProperty().isLinked()){
			//generate list collection
			MetaLink link = (MetaLink)list.getContainedProperty();
			appendEmptyline();
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
			appendStatement(DataFacadeGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), targetDocument)+" "+targetDocument.getTemporaryVariableName()+" = ("+DataFacadeGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), targetDocument)+") "+listName+".get(i)");
			appendStatement("LabelValueBean bean = new LabelValueBean("+targetDocument.getTemporaryVariableName()+".getId(), "+targetDocument.getTemporaryVariableName()+".getName()+\" [\"+"+targetDocument.getTemporaryVariableName()+".getId()+\"]\" )");
			appendStatement(listName+"Values.add(bean)");
			append(closeBlock());
			appendStatement("addBeanToRequest(req, "+quote(listName+"Values")+", "+listName+"Values"+")");
			appendStatement("form."+list.getContainedProperty().toBeanSetter()+"Collection("+listName+"Values"+")");
			
		}
		
		if(list.getContainedProperty() instanceof MetaEnumerationProperty){
			EnumerationType type = (EnumerationType )GeneratorDataRegistry.getInstance().getType(((MetaEnumerationProperty) list.getContainedProperty()).getEnumeration());
			appendEmptyline();
			String arrName = type.getName()+"_values";
		    String listName = arrName+"List";
		    appendString("//enumeration "+type.getName());
		    appendStatement("int[] "+arrName+" = " +EnumerationGenerator.getUtilsClassName(type)+"."+type.getName().toUpperCase()+"_VALUES");
		    appendStatement("List<LabelValueBean> "+listName+"Values"+" = new ArrayList<LabelValueBean>("+arrName+".length)");
		    appendString("for (int i=0; i<"+arrName+".length; i++){");
		    increaseIdent();
		
		    appendStatement("LabelValueBean bean = new LabelValueBean(\"\"+"+arrName+"[i], "+EnumerationGenerator.getUtilsClassName(type)+".getName("+arrName+"[i]))");
		    appendStatement(listName+"Values"+".add(bean)");
		    append(closeBlock());
		    appendStatement("form."+list.getContainedProperty().toBeanSetter()+"Collection("+listName+"Values"+")");
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
			appendStatement("bean.setDescription("+EnumerationGenerator.getUtilsClassName(type)+".getName(value))");
		}
		appendStatement("beans.add(bean)");
		append(closeBlock()); ;		
		appendStatement("addBeanToRequest(req, "+quote("elements")+", beans)");
//*/		
		appendStatement("return mapping.findForward(", quote("success"), ")");
		append(closeBlock()); 
	}
	
	private GeneratedClass generateTableShowAction(MetaModuleSection section, MetaTableProperty table){
		
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		MetaDocument doc = section.getDocument();

		clazz.setPackageName(getPackage(section.getModule()));
	    
		clazz.addImport("java.util.List");
		clazz.addImport("java.util.ArrayList");
		addStandardActionImports(clazz);
		clazz.addImport(DataFacadeGenerator.getDocumentImport(context, doc));
		clazz.addImport(ModuleBeanGenerator.getContainerEntryFormImport(doc, table));

		clazz.setName(getContainerShowActionName(doc, table));
		clazz.setParent(getBaseActionName(section));
		
		startClassBody();
		appendString(getExecuteDeclaration());
		increaseIdent();
		appendStatement("String id = getStringParameter(req, PARAM_ID)");
		appendStatement(doc.getName()+" "+doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id);");
		appendEmptyline();
		
		appendStatement(ModuleBeanGenerator.getContainerEntryFormName(table)+" form = new "+ModuleBeanGenerator.getContainerEntryFormName(table)+"() ");
		appendStatement("form.setPosition(\"-1\")");
		appendStatement("form.setOwnerId("+doc.getVariableName()+".getId())");	
		appendStatement("addBeanToRequest(req, "+quote(StrutsConfigGenerator.getContainerEntryFormName(doc, table))+", form)");
		appendEmptyline();
		
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
		append(closeBlock());		
		appendStatement("addBeanToRequest(req, "+quote("rows")+", beans)");
		
		appendStatement("return mapping.findForward("+quote("success")+")");
		append(closeBlock());		
		
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
	
	public String generateFormAction(MetaForm form){
		if (form.getAction().equals("sendMail"))
			return generateSendMailFormAction(form);
		throw new RuntimeException("Unsupported action type: "+form.getAction());
	}
	
	private String generateSendMailFormAction(MetaForm form){
		String ret = "";

		ret += writeStatement("package "+getPackage());
		
		ret += emptyline();



		//write imports...
		ret += getStandardActionImports();
		ret += writeImport(ModuleBeanGenerator.getFormBeanImport(form));
		ret += emptyline();
		ret += writeImport("net.anotheria.communication.data.HtmlMailMessage");
		ret += writeImport("net.anotheria.communication.service.IMessagingService");
		ret += writeImport("net.anotheria.communication.service.MessagingServiceFactory");

		ret += emptyline();
	    
		ret += writeString("public class "+getFormActionName(form)+" extends "+BaseActionGenerator.getBaseActionName(GeneratorDataRegistry.getInstance().getContext())+" {");
		increaseIdent();
		ret += emptyline();
		ret += writeStatement("private IMessagingService service = MessagingServiceFactory.getMessagingService()"); 
		ret += emptyline();
		List<String> targets = form.getTargets();
		ret += writeString("public static String[] MAIL_TARGETS = {");
		for (int i=0; i<targets.size(); i++){
			ret += writeIncreasedString(quote((String)targets.get(i))+",");
		}
		ret += writeStatement("}");
		ret += emptyline();
	    
	    
		ret += writeString(getExecuteDeclaration());
		increaseIdent();
	

		ret += writeStatement(ModuleBeanGenerator.getFormBeanName(form)+" form = ("+ModuleBeanGenerator.getFormBeanName(form)+") af");	
		//create message.
		ret += writeString("//create message");
		ret += writeStatement("String message = "+quote(""));
		ret += writeStatement("String htmlMessage = "+quote(""));
		ret += emptyline();

		ret += writeStatement("String emptyHtmlLine = "+quote(""));
		ret += writeStatement("emptyHtmlLine += "+quote("<tr>"));
		ret += writeStatement("emptyHtmlLine += "+quote("\\t<td colspan=\\\"2\\\">"));
		ret += writeStatement("emptyHtmlLine += "+quote("\\t\\t&nbsp;"));
		ret += writeStatement("emptyHtmlLine  += "+quote("\\t</td>"));
		ret += writeStatement("emptyHtmlLine  += "+quote("</tr>"));
		ret += emptyline();
		
		ret += writeStatement("htmlMessage += "+quote("<table border=\\\"0\\\">"));
		
		List<MetaFormField> elements = form.getElements();
		for (int i=0; i<elements.size(); i++){
			ret += writeStatement("htmlMessage += "+quote("\\n"));

			MetaFormField element = (MetaFormField)elements.get(i);
			
			if (element.isSingle()){
				
				MetaFormSingleField field = (MetaFormSingleField)element;
				
				ret += writeStatement("htmlMessage += "+quote("<tr>"));
				ret += writeStatement("htmlMessage += "+quote("\\t<td width=\\\"1\\\">"));
				ret += writeStatement("htmlMessage += "+quote("\\t\\t"+(field.isSpacer() ? "&nbsp;" : ""+(i+1))));
				ret += writeStatement("htmlMessage += "+quote("\\t</td>"));
				ret += writeStatement("htmlMessage += "+quote("\\t<td>"));
				ret += writeStatement("htmlMessage += \"\\t\\t\"+getDefaultResources().getMessage("+quote(field.getTitle())+")");
				ret += writeStatement("htmlMessage += "+quote("\\t</td>"));
				ret += writeStatement("htmlMessage += "+quote("</tr>"));
				ret += emptyline();

				if (field.isSpacer())
					continue;
				
				ret += writeStatement("htmlMessage += "+quote("<tr>"));
				ret += writeStatement("htmlMessage += "+quote("\\t<td colspan=\\\"2\\\">"));
				String value = "String value"+i+" = "; 
				if (field.getType().equals("boolean")){
					value += "form.get"+StringUtils.capitalize(element.getName())+"() ? "+quote("Yes")+" : "+quote("No");
				}else{
					value += "form.get"+StringUtils.capitalize(element.getName())+"()"; 
				}
				ret += writeStatement(value);
				ret += writeStatement("htmlMessage += \"\\t\\t\"+value"+i+"+"+quote("&nbsp;"));
				ret += writeStatement("htmlMessage += "+quote("\\t</td>"));
				ret += writeStatement("htmlMessage += "+quote("</tr>"));
				ret += emptyline();

				ret += writeStatement("htmlMessage += emptyHtmlLine");
				ret += emptyline();

				
//				String title = element.getTitle();
				ret += writeStatement("message += "+quote(element.getName()+" - "));
				ret += writeStatement("message += getDefaultResources().getMessage("+quote(field.getTitle())+")+"+quote(":\\n"));
				ret += writeStatement("message += value"+i+"+"+quote("\\n"));

				ret += emptyline();
				
			}
			
			if (element.isComplex()){
				MetaFormTableField table = (MetaFormTableField)element;
				ret += writeStatement("htmlMessage += "+quote("<!-- including table element "+table.getName()+" -->\\n"));
				ret += writeStatement("htmlMessage += "+quote("<tr>"));
				ret += writeStatement("htmlMessage += "+quote("\\t<td colspan=\\\"3\\\">"));
				ret += emptyline();
				//start subtable...
				
				ret += writeStatement("htmlMessage += "+quote("\\n"));
				ret += writeString("//Writing inner table: "+table.getName());
				ret += writeStatement("htmlMessage += "+quote("<table width=\\\"100%\\\">"));
				
				//generate headers.
				List<MetaFormTableColumn> columns = table.getColumns();
				ret += writeStatement("htmlMessage += "+quote("\\n"));
				ret += writeStatement("htmlMessage += "+quote("<tr>"));
				for (int c=0; c<columns.size(); c++){
					MetaFormTableColumn col = columns.get(c);
					MetaFormTableHeader header = col.getHeader();
					ret += writeStatement("htmlMessage += "+quote("\\n"));
					ret += writeStatement("htmlMessage += "+quote("\\t<th width=\\\""+header.getWidth()+"\\\">"));
					ret += writeStatement("htmlMessage += getDefaultResources().getMessage("+quote(header.getKey())+")");
					ret += writeStatement("htmlMessage += "+quote("\\t</th>"));
															
				}
				ret += writeStatement("htmlMessage += "+quote("</tr>"));
				ret += writeStatement("htmlMessage += "+quote("\\n"));

				//generate data lines.
				for (int r=0; r<table.getRows(); r++){
					ret += writeStatement("htmlMessage += "+quote("<tr>"));
					ret += writeStatement("htmlMessage += "+quote("\\n"));
					for (int c=0; c<columns.size(); c++){
						MetaFormTableColumn col = (MetaFormTableColumn)columns.get(c);
						MetaFormTableHeader header = col.getHeader();
						ret += writeStatement("htmlMessage += "+quote("\\t<td width=\\\""+header.getWidth()+"\\\">"));
						ret += writeStatement("htmlMessage += form.get"+StringUtils.capitalize(table.getVariableName(r,c))+"()");
						ret += writeStatement("htmlMessage += "+quote("\\t</td>"));
					}
					ret += writeStatement("htmlMessage += "+quote("</tr>\\n"));
					
				}
				
				

				//end subtable
				ret += writeStatement("htmlMessage += "+quote("</table>"));
				ret += writeStatement("htmlMessage += "+quote("\\t</td>"));
				ret += writeStatement("htmlMessage += "+quote("</tr>"));
				
				
			}


		}
		
		ret += writeStatement("htmlMessage += "+quote("</table>"));

		ret += emptyline();
		ret += writeStatement("HtmlMailMessage mail = new HtmlMailMessage()");
		ret += writeStatement("mail.setMessage(message)");
		ret += writeStatement("mail.setHtmlContent(htmlMessage)");
		ret += writeStatement("mail.setPlainTextContent(message)");
		ret += writeStatement("mail.setSubject("+quote("WebSiteForm Submit: "+StringUtils.capitalize(form.getId()))+")");
		ret += writeStatement("mail.setSender(\"\\\"WebForm\\\"<support@anotheria.net>\")");
			
		ret += emptyline();
		ret += writeString("//sending mail to "+targets.size()+" target(s)");
		ret += writeString("for (int i=0; i<MAIL_TARGETS.length; i++){");
		increaseIdent();
		ret += writeString("try{");
		increaseIdent();
		ret += writeStatement("mail.setRecipient(MAIL_TARGETS[i])");	
		ret += writeStatement("service.sendMessage(mail)");
		decreaseIdent();
		ret += writeString("}catch(Exception e){");
		increaseIdent();
		ret += writeStatement("e.printStackTrace()");
		ret += closeBlock();
		ret += closeBlock();
		ret += emptyline();		
		
		ret += writeStatement("return mapping.findForward(\"success\")");
		ret += closeBlock();
		ret += emptyline();

//*/	    
		ret += closeBlock();
		return ret;
	}
	
	class EnumerationPropertyGenerator{
	    private List<String> generatedProperties;
	    MetaDocument doc;
	    
	    EnumerationPropertyGenerator(MetaDocument doc){
	        generatedProperties = new ArrayList<String>();
	        this.doc = doc;
	    }
	    
	    public String generateEnumerationPropertyHandling(MetaEnumerationProperty mep, boolean editMode){
	        String ret = "";
	        EnumerationType type = (EnumerationType )GeneratorDataRegistry.getInstance().getType(mep.getEnumeration());
			ret += emptyline();
			String arrName = type.getName()+"_values";
		    String listName = arrName+"List";
			if (generatedProperties.indexOf(arrName)==-1){
			    ret += writeString("//enumeration "+type.getName());
			    ret += writeStatement("int[] "+arrName+" = " +EnumerationGenerator.getUtilsClassName(type)+"."+type.getName().toUpperCase()+"_VALUES");
			    ret += writeStatement("List<LabelValueBean> "+listName+" = new ArrayList<LabelValueBean>("+arrName+".length)");
			    ret += writeString("for (int i=0; i<"+arrName+".length; i++){");
			    increaseIdent();
			
			    ret += writeStatement("LabelValueBean bean = new LabelValueBean(\"\"+"+arrName+"[i], "+EnumerationGenerator.getUtilsClassName(type)+".getName("+arrName+"[i]))");
			    ret += writeStatement(listName+".add(bean)");
			    ret += closeBlock();
			    generatedProperties.add(arrName);
			}else{
			    ret += writeString("//enumeration "+type.getName()+" already prepared.");
			}
			ret += writeStatement("form."+mep.toBeanSetter()+"Collection("+listName+")");
			if (editMode)
			    ret += writeStatement("form."+mep.toBeanSetter()+"CurrentValue("+EnumerationGenerator.getUtilsClassName(type)+".getName("+doc.getVariableName()+"."+mep.toGetter()+"()))");
	        return ret;
	    }
	}
	
	private String getShowActionRedirect(MetaDocument doc){
	    return quote(StrutsConfigGenerator.getPath(doc, StrutsConfigGenerator.ACTION_SHOW)+"?ts=")+"+System.currentTimeMillis()";
	}
	private String getEditActionRedirect(MetaDocument doc){
	    return quote(StrutsConfigGenerator.getPath(doc, StrutsConfigGenerator.ACTION_EDIT)+"?ts=")+"+System.currentTimeMillis()";
	}
	
}
