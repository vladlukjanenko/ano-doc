package net.anotheria.asg.generator.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
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
import net.anotheria.asg.generator.view.meta.MetaViewElement;
import net.anotheria.asg.generator.view.meta.MetaView;
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
		files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getBaseActionName(section), generateBaseAction(section)));
		files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getShowActionName(section), generateShowAction(section)));
		files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getSearchActionName(section), generateSearchAction(section)));
		files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getDeleteActionName(section), generateDeleteAction(section)));
		files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getDuplicateActionName(section), generateDuplicateAction(section)));
		files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getVersionInfoActionName(section), generateVersionInfoAction(section)));
		
		timer.stopExecution(section.getModule().getName()+"-view");
		try{
			if (section.getDialogs().size()>0){
				//works only if the section has a dialog.
				timer.startExecution(section.getModule().getName()+"-dialog-base");
				files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getUpdateActionName(section), generateUpdateAction(section)));
				files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getEditActionName(section), generateEditAction(section)));
				files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getNewActionName(section), generateNewAction(section)));
				timer.stopExecution(section.getModule().getName()+"-dialog-base");

				MetaDocument doc = section.getDocument();
				
				timer.startExecution(section.getModule().getName()+"-dialog-copylang");
				if (GeneratorDataRegistry.hasLanguageCopyMethods(doc)){
					files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getLanguageCopyActionName(section), generateLanguageCopyAction(section)));
					files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getSwitchMultilingualityActionName(section), generateSwitchMultilingualityAction(section)));
				}
				timer.stopExecution(section.getModule().getName()+"-dialog-copylang");
				
				timer.startExecution(section.getModule().getName()+"-dialog-container");
				for (int p=0; p<doc.getProperties().size(); p++){
					MetaProperty pp = (MetaProperty)doc.getProperties().get(p);
					//System.out.println("checking "+pp+" "+(pp instanceof MetaContainerProperty));
					if (pp instanceof MetaContainerProperty){
					//	System.out.println("generating "+pp);
						files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getContainerShowActionName(doc, (MetaContainerProperty)pp), generateContainerShowAction(section, (MetaContainerProperty)pp)));
						files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getContainerAddEntryActionName(doc, (MetaContainerProperty)pp), generateContainerAddRowAction(section, (MetaContainerProperty)pp)));
						files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getContainerQuickAddActionName(doc, (MetaContainerProperty)pp), generateContainerQuickAddAction(section, (MetaContainerProperty)pp)));
						files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getContainerDeleteEntryActionName(doc, (MetaContainerProperty)pp), generateContainerDeleteEntryAction(section, (MetaContainerProperty)pp)));

						files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getContainerMoveEntryActionName(doc, (MetaContainerProperty)pp), generateContainerMoveEntryAction(section, (MetaContainerProperty)pp)));
/*						files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getContainerMoveUpEntryActionName(doc, (MetaContainerProperty)pp), generateContainerMoveUpEntryAction(section, (MetaContainerProperty)pp)));
						files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getContainerMoveDownEntryActionName(doc, (MetaContainerProperty)pp), generateContainerMoveDownEntryAction(section, (MetaContainerProperty)pp)));
						files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getContainerMoveTopEntryActionName(doc, (MetaContainerProperty)pp), generateContainerMoveTopEntryAction(section, (MetaContainerProperty)pp)));
						files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getContainerMoveBottomEntryActionName(doc, (MetaContainerProperty)pp), generateContainerMoveBottomEntryAction(section, (MetaContainerProperty)pp)));
*/
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
			files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getShowQueryActionName(section), generateShowQueryAction(section)));
			files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getExecuteQueryActionName(section), generateExecuteQueryAction(section)));
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

	
	private String generateShowAction(MetaModuleSection section){
	    StringBuilder ret = new StringBuilder(5000);
	    MetaDocument doc = section.getDocument();
		List<MetaViewElement> elements = section.getElements();
	    
	    boolean containsComparable = section.containsComparable();
	    appendStatement(ret, "package "+getPackage(section.getModule()));
	    ret.append(emptyline());
	    
	    //write imports...
	    ret.append(writeImport("java.util.List"));
	    ret.append(writeImport("java.util.ArrayList"));
	    ret.append(writeImport("net.anotheria.asg.util.decorators.IAttributeDecorator"));
	    ret.append(writeImport("net.anotheria.asg.util.filter.DocumentFilter"));
	    ret.append(writeImport("net.anotheria.util.NumberUtils"));
	    ret.append(getStandardActionImports());
	    ret.append(writeImport(DataFacadeGenerator.getDocumentImport(context, doc)));
	    ret.append(writeImport(ModuleBeanGenerator.getListItemBeanImport(context, doc)));
		ret.append(emptyline());
		if (containsComparable){
			ret.append(writeImport(ModuleBeanGenerator.getListItemBeanSortTypeImport(context, doc)));
			ret.append(writeImport("net.anotheria.util.sorter.Sorter"));
			ret.append(writeImport("net.anotheria.util.sorter.QuickSorter"));
			ret.append(emptyline());
		}
		
		ret.append(writeImport("net.anotheria.util.slicer.Slicer"));
		ret.append(writeImport("net.anotheria.util.slicer.Slice"));
		ret.append(writeImport("net.anotheria.util.slicer.Segment"));
		ret.append(writeImport("net.anotheria.asg.util.bean.PagingLink"));
		ret.append(emptyline());
		
		//check if we have to property definition files.
		//check if we have decorators
		HashMap<String,MetaEnumerationProperty> importedEnumerations = new HashMap<String,MetaEnumerationProperty>();
		List<MetaDecorator> neededDecorators = new ArrayList<MetaDecorator>();
		
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			if (element instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)element;
				MetaProperty p = doc.getField(field.getName());
				if (p instanceof MetaEnumerationProperty){
					MetaEnumerationProperty enumeration = (MetaEnumerationProperty)p;
					if (importedEnumerations.get(enumeration.getName())==null){
						EnumerationType type = (EnumerationType)GeneratorDataRegistry.getInstance().getType(enumeration.getEnumeration());
						ret.append(writeImport(EnumerationGenerator.getUtilsImport(type)));
						importedEnumerations.put(enumeration.getName(), enumeration);
					}
				}
				
				MetaDecorator d = field.getDecorator();
				if (d!=null){
					 if (neededDecorators.indexOf(d)==-1)
					 	neededDecorators.add(d);
				}
			}
		}
		
	    appendString(ret, "public class "+getShowActionName(section)+" extends "+getBaseActionName(section)+" {");
	    increaseIdent();
	    ret.append(emptyline());
	    
	    //generate session attributes constants
	    appendStatement(ret, "public static final String SA_SORT_TYPE = SA_SORT_TYPE_PREFIX+", quote(doc.getName()));
	    appendStatement(ret, "public static final String SA_FILTER = SA_FILTER_PREFIX+", quote(doc.getName()));
	    appendStatement(ret, "private static final List<String> ITEMS_ON_PAGE_SELECTOR = java.util.Arrays.asList(new String[]{\"5\",\"10\",\"20\",\"25\",\"50\",\"100\",\"500\",\"1000\"})");
	    
	    boolean containsDecorators = neededDecorators.size() >0;
	    
		if (containsComparable){
			appendStatement(ret, "private Sorter<", ModuleBeanGenerator.getListItemBeanName(doc), "> sorter");
			ret.append(emptyline());
		}
		
		if (containsDecorators){
			for (int i=0; i<elements.size();i++){
				MetaViewElement element = (MetaViewElement)elements.get(i);
				if (element.getDecorator()!=null){
					appendStatement(ret, "private IAttributeDecorator "+getDecoratorVariableName(element));
				}
			}
			ret.append(emptyline());
		}
		
		if (section.getFilters().size()>0){
			for (MetaFilter f : section.getFilters()){
				appendStatement(ret, "private DocumentFilter "+getFilterVariableName(f));
			}
			ret.append(emptyline());
		}
			
		
		appendString(ret, "public "+getShowActionName(section)+"(){");
		increaseIdent();
		appendStatement(ret, "super()");
		if (containsComparable)
			appendStatement(ret, "sorter = new QuickSorter<"+ModuleBeanGenerator.getListItemBeanName(doc)+">()");
		if (containsDecorators){
			appendString(ret, "try{ ");
			increaseIdent();
			for (int i=0; i<elements.size();i++){
				MetaViewElement element = elements.get(i);
				if (element.getDecorator()!=null){
					appendStatement(ret, getDecoratorVariableName(element)+" = (IAttributeDecorator)Class.forName("+quote(element.getDecorator().getClassName())+").newInstance()");
				}
			}
			decreaseIdent();
			appendString(ret, "} catch(Exception e){");
			appendIncreasedStatement(ret, "log.fatal(\"Couldn't instantiate decorator:\", e)");
			appendString(ret, "}");
		}
	    //add filters
		if (section.getFilters().size()>0){
			appendString(ret, "try{ ");
			increaseIdent();
			for (MetaFilter f : section.getFilters()){
				appendStatement(ret, getFilterVariableName(f), " = (DocumentFilter) Class.forName(", quote(f.getClassName()), ").newInstance()");
			}
			decreaseIdent();
			appendString(ret, "} catch(Exception e){");
			ret.append(writeIncreasedStatement("log.fatal(\"Couldn't instantiate filter:\", e)"));
			appendString(ret, "}");
		}
		closeBlock(ret);
		

	    appendString(ret, getExecuteDeclaration());
	    increaseIdent();
	    
	    if (section.getFilters().size()>0){
	    	for (int i=0 ; i<section.getFilters().size(); i++){
	    		MetaFilter f = section.getFilters().get(i);
	    		String filterParameterName = "filterParameter"+i;
		    	//hacky, only one filter at time allowed. otherwise, we must submit the filter name.
		    	appendStatement(ret, "String filterParameter"+i+" = "+quote(""));
		    	appendString(ret, "try{ ");
		    	appendIncreasedStatement(ret, filterParameterName+" = getStringParameter(req, "+quote("pFilter"+i)+")");
		    	appendIncreasedStatement(ret, "addBeanToSession(req, SA_FILTER+"+quote(i)+", "+filterParameterName+")");
		    	appendString(ret, "}catch(Exception ignored){");
		    	increaseIdent();
		    	ret.append(writeCommentLine("no filter parameter given, tring to check in the session."));
		    	appendStatement(ret, filterParameterName+" = (String)getBeanFromSession(req, SA_FILTER+"+quote(i)+")");
		    	appendString(ret, "if ("+filterParameterName+"==null)");
		    	ret.append(writeIncreasedStatement(filterParameterName+" = "+quote("")));
		    	closeBlock(ret);
		    	appendStatement(ret, "req.setAttribute("+quote("currentFilterParameter"+i)+", "+filterParameterName+")");
		    	ret.append(emptyline());
	    	}
	    }
	    
	    //check if its sortable.
		if (containsComparable){
			String sortType = ModuleBeanGenerator.getListItemBeanSortTypeName(doc);
			appendStatement(ret, "int sortMethod = "+sortType+".SORT_BY_DEFAULT");
			appendStatement(ret, "boolean sortOrder = "+sortType+".ASC");
			appendStatement(ret, "boolean sortParamSet = false");
			ret.append(emptyline());
			appendString(ret, "try{");
			appendIncreasedStatement(ret, "sortMethod = getIntParameter(req, PARAM_SORT_TYPE)");
			appendIncreasedStatement(ret, "sortParamSet = true");
			appendString(ret, "}catch(Exception ignored){}");
			ret.append(emptyline());	    
			appendString(ret, "try{");
			ret.append(writeIncreasedStatement("String sortMethodName = getStringParameter(req, PARAM_SORT_TYPE_NAME)"));
			ret.append(writeIncreasedStatement("sortMethod = "+sortType+".name2method(sortMethodName)"));
			ret.append(writeIncreasedStatement("sortParamSet = true"));
			appendString(ret, "}catch(Exception ignored){}");
			ret.append(emptyline());	    
			appendString(ret, "try{");
			increaseIdent();
			appendString(ret, "sortOrder = getStringParameter(req, PARAM_SORT_ORDER).equals("+quote(ViewConstants.VALUE_SORT_ORDER_ASC)+") ? ");
			ret.append(writeIncreasedStatement(""+sortType+".ASC : "+sortType+".DESC"));
			decreaseIdent();
			appendString(ret, "}catch(Exception ignored){}");
			ret.append(emptyline());
			appendStatement(ret, ModuleBeanGenerator.getListItemBeanSortTypeName(doc)+" sortType = null");
			appendString(ret, "if (sortParamSet){");
			increaseIdent();
			appendStatement(ret, "sortType = new "+ModuleBeanGenerator.getListItemBeanSortTypeName(doc)+"(sortMethod, sortOrder)");
			appendStatement(ret, "addBeanToSession(req, SA_SORT_TYPE, sortType)");
			decreaseIdent();
			appendString(ret, "}else{");
			increaseIdent();
			appendStatement(ret, "sortType = ("+ModuleBeanGenerator.getListItemBeanSortTypeName(doc)+")getBeanFromSession(req, SA_SORT_TYPE)");
			appendString(ret, "if (sortType==null)");
			ret.append(writeIncreasedStatement("sortType = new "+ModuleBeanGenerator.getListItemBeanSortTypeName(doc)+"(sortMethod, sortOrder)"));
			closeBlock(ret);
			appendStatement(ret, "req.setAttribute("+quote("currentSortCode")+", sortType.getMethodAndOrderCode())");
			ret.append(emptyline());
		}
	    
	    String listName = doc.getMultiple().toLowerCase();
	    if (section.getFilters().size()>0){
		    String unfilteredListName = "_unfiltered_"+listName;
		    //change this if more than one filter can be triggered at once.
		    appendStatement(ret, "List<"+doc.getName()+"> "+unfilteredListName+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getMultiple()+"()");
		    appendStatement(ret, "List<"+doc.getName()+"> "+listName+" = new ArrayList<"+doc.getName()+">()");
		    appendString(ret, "for (int i=0; i<"+unfilteredListName+".size(); i++){");
		    increaseIdent();
		    appendStatement(ret, "boolean mayPass = true");
		    for (int i=0; i<section.getFilters().size(); i++){
			    MetaFilter activeFilter = section.getFilters().get(i);
			    String filterVarName = getFilterVariableName(activeFilter);
			    appendStatement(ret, "mayPass = mayPass && ("+filterVarName+".mayPass("+unfilteredListName+".get(i), "+quote(activeFilter.getFieldName())+", filterParameter"+i+"))");
		    	
		    }
		    appendString(ret, "if (mayPass)");
		    ret.append(writeIncreasedStatement(listName+".add("+unfilteredListName+".get(i))"));
		    closeBlock(ret);
	    }else{
		    appendStatement(ret, "List<"+doc.getName()+"> "+listName+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getMultiple()+"()");
	    }

		appendStatement(ret, "List<"+ModuleBeanGenerator.getListItemBeanName(doc)+"> beans = new ArrayList<"+ModuleBeanGenerator.getListItemBeanName(doc)+">("+listName+".size())");
		appendString(ret, "for ("+doc.getName()+" "+doc.getVariableName()+" : "+listName+"){");
		increaseIdent();
		appendStatement(ret, ModuleBeanGenerator.getListItemBeanName(doc)+" bean = "+getMakeBeanFunctionName(ModuleBeanGenerator.getListItemBeanName(doc))+"("+doc.getVariableName()+")");
		appendStatement(ret, "beans.add(bean)");
		closeBlock(ret);
	    ret.append(emptyline());
	    if (containsComparable){
	    	appendStatement(ret, "beans = sorter.sort(beans, sortType)");
	    }

	    //paging start
	    ret.append(writeCommentLine("paging"));
	    appendStatement(ret, "int pageNumber = 1"); 
	    appendString(ret, "try{");
	    ret.append(writeIncreasedStatement("pageNumber = Integer.parseInt(req.getParameter("+quote("pageNumber")+"))"));
	    appendString(ret, "}catch(Exception ignored){}");
	    appendStatement(ret, "Integer lastItemsOnPage = (Integer)req.getSession().getAttribute(\"currentItemsOnPage\")");
	    appendStatement(ret, "int itemsOnPage = lastItemsOnPage == null ? 20 : lastItemsOnPage"); 
	    appendString(ret, "try{");
	    appendIncreasedStatement(ret, "itemsOnPage = Integer.parseInt(req.getParameter("+quote("itemsOnPage")+"))");
	    appendString(ret, "}catch(Exception ignored){}");
	    appendStatement(ret, "Slice<"+ModuleBeanGenerator.getListItemBeanName(doc)+"> slice = Slicer.slice(new Segment(pageNumber, itemsOnPage), beans)");
	    appendStatement(ret, "beans = slice.getSliceData()");
	    ret.append(emptyline());
	    
	    ret.append(writeCommentLine("prepare paging links"));
	    appendStatement(ret, "ArrayList<PagingLink> pagingLinks = new ArrayList<PagingLink>()");
		appendStatement(ret, "pagingLinks.add(new PagingLink(slice.isFirstSlice() ? null : \"1\", \"|<<\"))");
		appendStatement(ret, "pagingLinks.add(new PagingLink(slice.hasPrevSlice() ? \"\"+(slice.getCurrentSlice()-1) : null, \"<<\"))");
		
		appendString(ret, "for (int i=1; i<slice.getCurrentSlice(); i++){");
		increaseIdent();
		appendString(ret, "if (slice.getCurrentSlice()-i<=7)");
		ret.append(writeIncreasedStatement("pagingLinks.add(new PagingLink(\"\"+i,\"\"+i))"));
		closeBlock(ret);
		
		appendStatement(ret, "pagingLinks.add(new PagingLink(null, \"Page \"+(slice.getCurrentSlice()+\" of \"+slice.getTotalNumberOfSlices())))");
		
		appendString(ret, "for (int i=slice.getCurrentSlice()+1; i<=slice.getTotalNumberOfSlices(); i++){");
		increaseIdent();
		appendString(ret, "if (i-slice.getCurrentSlice()<=7)");
		ret.append(writeIncreasedStatement("pagingLinks.add(new PagingLink(\"\"+i,\"\"+i))"));
		closeBlock(ret);
		
		
		appendStatement(ret, "pagingLinks.add(new PagingLink(slice.hasNextSlice() ?  \"\"+(slice.getCurrentSlice()+1) : null, \">>\"))");
		appendStatement(ret, "pagingLinks.add(new PagingLink(slice.isLastPage() ? null : \"\"+slice.getTotalNumberOfSlices(), \">>|\"))");
	    ret.append(writeCommentLine(" paging links end"));
	    
	    appendStatement(ret, "req.setAttribute("+quote("paginglinks")+", pagingLinks)");
	    appendStatement(ret, "req.setAttribute("+quote("currentpage")+", pageNumber)");
	    appendStatement(ret, "req.setAttribute("+quote("currentItemsOnPage")+", itemsOnPage)");
	    appendStatement(ret, "req.getSession().setAttribute("+quote("currentItemsOnPage")+", itemsOnPage)");
	    appendStatement(ret, "req.setAttribute("+quote("PagingSelector")+", ITEMS_ON_PAGE_SELECTOR)");
	    ret.append(emptyline());
	    //paging end
	    
	    
	    
	    appendStatement(ret, "addBeanToRequest(req, "+quote(listName)+", beans)");
	    
	    //add filters
	    for (MetaFilter f : section.getFilters()){
	    	appendStatement(ret, "addBeanToRequest(req, ", quote(getFilterVariableName(f)), ", ", getFilterVariableName(f), ".getTriggerer(\"\"))");
	    }
	    
	    appendStatement(ret, "return mapping.findForward(\"success\")");
	    closeBlock(ret);
	    ret.append(emptyline());
	    
	    
	    // BEAN creation function
	    appendString(ret, "protected "+ModuleBeanGenerator.getListItemBeanName(doc)+" "+getMakeBeanFunctionName(ModuleBeanGenerator.getListItemBeanName(doc))+"("+doc.getName()+" "+doc.getVariableName()+"){");
	    increaseIdent();
	    appendStatement(ret, ModuleBeanGenerator.getListItemBeanName(doc)+" bean = new "+ModuleBeanGenerator.getListItemBeanName(doc)+"()");
	    //set the properties.
	    //this is a hack...
	    appendStatement(ret, "bean.setPlainId("+doc.getVariableName()+".getId())");

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
//						appendStatement(ret, "bean."+tmp.toBeanSetter(lang)+"("+value+")");
//						MetaDecorator d = element.getDecorator();
//						value = getDecoratorVariableName(element)+".decorate("+doc.getVariableName()+", "+quote(p.getName())+", "+quote(d.getRule())+")";
//					}
//					appendStatement(ret, "bean."+p.toBeanSetter(lang)+"("+value+")");
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

							appendStatement(ret, "bean."+tmp.toBeanSetter()+"("+value+")");
							MetaDecorator d = element.getDecorator();
							value = getDecoratorVariableName(element)+".decorate("+doc.getVariableName()+", "+quote(p.getName()+(lang==null?"":"_"+lang))+", "+quote(d.getRule())+")";
						}
					}
					appendStatement(ret, "bean."+p.toBeanSetter(lang)+"("+value+")");
//				}
			}
		}
		
		appendStatement(ret, "bean.setDocumentLastUpdateTimestamp(NumberUtils.makeISO8601TimestampString("+doc.getVariableName()+".getLastUpdateTimestamp()))");
	    
	    appendStatement(ret, "return bean");
	    closeBlock(ret);
	    ret.append(emptyline());
	    
	    
	    closeBlock(ret);
	    return ret.toString();
	}
	
	///////////////////////////////////////////////////
	////////              SEARCH             //////////
	///////////////////////////////////////////////////
	private String generateSearchAction(MetaModuleSection section){
	    String ret = "";
	    MetaDocument doc = section.getDocument();
		List<MetaViewElement> elements = section.getElements();
	    
	    ret += writeStatement("package "+getPackage(section.getModule()));
	    ret += emptyline();
	    
	    //write imports...
	    ret += writeImport("java.util.List");
	    ret += writeImport("java.util.ArrayList");
	    ret += getStandardActionImports();
	    //Removed 2 unneeded imports
	    //ret += writeImport(DataFacadeGenerator.getDocumentImport(context, doc));
	    //ret += writeImport(ModuleBeanGenerator.getListItemBeanImport(context, doc));
		ret += emptyline();
		ret += writeImport("net.anotheria.anodoc.query2.DocumentQuery");
		ret += writeImport("net.anotheria.anodoc.query2.string.ContainsStringQuery");
		ret += writeImport("net.anotheria.anodoc.query2.QueryResult");
		ret += writeImport("net.anotheria.anodoc.query2.QueryResultEntry");
		ret += writeImport("net.anotheria.anodoc.query2.ResultEntryBean");
		

		
		//check if we have to property definition files.
		HashMap<String,MetaEnumerationProperty> importedEnumerations = new HashMap<String,MetaEnumerationProperty>();
		
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			if (element instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)element;
				MetaProperty p = doc.getField(field.getName());
				if (p instanceof MetaEnumerationProperty){
					MetaEnumerationProperty enumeration = (MetaEnumerationProperty)p;
					if (importedEnumerations.get(enumeration.getName())==null){
						EnumerationType type = (EnumerationType)GeneratorDataRegistry.getInstance().getType(enumeration.getEnumeration());
						ret += writeImport(EnumerationGenerator.getUtilsImport(type));
						importedEnumerations.put(enumeration.getName(), enumeration);
					}
				}
				
			}
		}
		
		
	    ret += writeString("public class "+getSearchActionName(section)+" extends "+getBaseActionName(section)+" {");
	    increaseIdent();
	    ret += emptyline();
	    
		ret += writeString("public "+getSearchActionName(section)+"(){");
		increaseIdent();
		ret += writeStatement("super()");
		ret += closeBlock();
		

	    ret += writeString(getExecuteDeclaration());
	    increaseIdent();
	    
/*	    
	    String listName = doc.getMultiple().toLowerCase();
	    if (section.getFilters().size()>0){
		    String unfilteredListName = "_unfiltered_"+listName;
		    //change this if more than one filter can be triggered at once.
		    MetaFilter activeFilter = section.getFilters().get(0);
		    String filterVarName = getFilterVariableName(activeFilter);
		    ret += writeStatement("List<"+doc.getName()+"> "+unfilteredListName+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getMultiple()+"()");
		    ret += writeStatement("List<"+doc.getName()+"> "+listName+" = new ArrayList<"+doc.getName()+">()");
		    ret += writeString("for (int i=0; i<"+unfilteredListName+".size(); i++)");
		    ret += writeIncreasedString("if ("+filterVarName+".mayPass("+unfilteredListName+".get(i), "+quote(activeFilter.getFieldName())+", filterParameter))");
		    ret += writeIncreasedStatement("\t"+listName+".add("+unfilteredListName+".get(i))");
	    }else{
		    ret += writeStatement("List<"+doc.getName()+"> "+listName+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getMultiple()+"()");
	    }
		ret += writeStatement("List<"+ModuleBeanGenerator.getListItemBeanName(doc)+"> beans = new ArrayList<"+ModuleBeanGenerator.getListItemBeanName(doc)+">("+listName+".size())");
		ret += writeString("for ("+doc.getName()+" "+doc.getVariableName()+" : "+listName+"){");
		increaseIdent();
		ret += writeStatement(ModuleBeanGenerator.getListItemBeanName(doc)+" bean = "+getMakeBeanFunctionName(ModuleBeanGenerator.getListItemBeanName(doc))+"("+doc.getVariableName()+")");
		ret += writeStatement("beans.add(bean)");
		ret += closeBlock();
	    ret += emptyline();
	    ret += writeStatement("addBeanToRequest(req, "+quote(listName)+", beans)");
	
*/
	    
	    ret += writeStatement("String criteria = req.getParameter("+quote("criteria")+")");
	    //ret += writeStatement("System.out.println("+quote("Criteria: ")+" + criteria)");
	    ret += writeStatement("DocumentQuery query = new ContainsStringQuery(criteria)");
	    ret += writeStatement("QueryResult result = "+getServiceGetterCall(section.getModule())+".executeQueryOn"+doc.getMultiple()+"(query)");
	    //ret += writeStatement("System.out.println("+quote("Result: ")+" + result)");
	    ret += writeString("if (result.getEntries().size()==0){");
	    ret += writeIncreasedStatement("req.setAttribute("+quote("srMessage")+", "+quote("Nothing found.")+")");
	    ret += writeString("}else{");
	    increaseIdent();
	    ret += writeStatement("List<ResultEntryBean> beans = new ArrayList<ResultEntryBean>(result.getEntries().size())");
	    ret += writeString("for (int i=0; i<result.getEntries().size(); i++){");
	    increaseIdent();
	    ret += writeStatement("QueryResultEntry entry = result.getEntries().get(i)");
	    ret += writeStatement("ResultEntryBean bean = new ResultEntryBean()");
	    ret += writeStatement("bean.setEditLink("+quote(StrutsConfigGenerator.getPath(doc, StrutsConfigGenerator.ACTION_EDIT)+"?pId=")+"+entry.getMatchedDocument().getId()+"+quote("&ts=")+"+System.currentTimeMillis())");
	    ret += writeStatement("bean.setDocumentId(entry.getMatchedDocument().getId())");
	    ret += writeStatement("bean.setPropertyName(entry.getMatchedProperty().getId())");
	    ret += writeStatement("bean.setInfo(entry.getInfo().toHtml())");
	    ret += writeStatement("beans.add(bean)");
	    ret += closeBlock();
	    ret += writeStatement("req.setAttribute("+quote("result")+", beans)");
	    ret += closeBlock();
	    
	    ret += writeStatement("return mapping.findForward(\"success\")");
	    ret += closeBlock();
	    ret += emptyline();
	    ret += closeBlock();
	    return ret;
	}

	private String generateShowQueryAction(MetaModuleSection section){
		String ret = "";
		MetaDocument doc = section.getDocument();
	    
		ret += writeStatement("package "+getPackage(section.getModule()));
		ret += emptyline();
		ret += getStandardActionImports();
		ret += emptyline();
		ret += writeImport("net.anotheria.webutils.bean.LabelValueBean");
		ret += writeImport("java.util.List");
		ret += writeImport("java.util.ArrayList");
		ret += writeImport("java.util.Iterator");
		ret += emptyline();
	
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
			
				ret += writeImport(DataFacadeGenerator.getDocumentImport(context, targetDocument));
			
				linkTargets.add(lt);
			}else{
				//WARN implement relative linking.
			}
		}
		ret += emptyline();

		ret += writeString("public class "+getShowQueryActionName(section)+" extends "+getBaseActionName(section)+" {");
		increaseIdent();
		ret += emptyline();
		
		ret += writeString(getExecuteDeclaration());
		increaseIdent();
		ret += emptyline();
		
		//ret += writeStatement("Iterator it");
		
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
			
				ret += writeStatement("List<"+targetDocument.getName()+"> "+targetDocument.getMultiple().toLowerCase()+" = "+getServiceGetterCall(mod)+".get"+targetDocument.getMultiple()+"()");
				ret += writeStatement("List<LabelValueBean> "+targetDocument.getMultiple().toLowerCase()+"Beans = new ArrayList<LabelValueBean>("+targetDocument.getMultiple().toLowerCase()+".size())");
				ret += writeString("for(Iterator<"+targetDocument.getName()+"> it="+targetDocument.getMultiple().toLowerCase()+".iterator(); it.hasNext(); ){");
				increaseIdent();
				ret += writeStatement(targetDocument.getName()+" "+targetDocument.getVariableName()+" = ("+targetDocument.getName()+") it.next()");
				String beanCreationCall = targetDocument.getMultiple().toLowerCase()+"Beans";
				beanCreationCall+=".add(";
				beanCreationCall+="new LabelValueBean(";
				beanCreationCall+=targetDocument.getVariableName()+".getId(), ";
				beanCreationCall+=targetDocument.getVariableName()+".getName()))";
				ret += writeStatement(beanCreationCall);
				ret += closeBlock();
				ret += writeStatement("addBeanToRequest(req, "+quote(targetDocument.getMultiple().toLowerCase())+", "+targetDocument.getMultiple().toLowerCase()+"Beans)"); 
				ret += emptyline();
				
				linkTargets.add(lt);
			}
			
		}
		
		
		ret += writeStatement("return mapping.findForward(\"success\")");
		ret += closeBlock();
		
		
		
		ret += closeBlock();
		

		return ret;
	}

	private String generateExecuteQueryAction(MetaModuleSection section){
		String ret = "";
		MetaDocument doc = section.getDocument();
		//List<MetaViewElement> elements = section.getElements();
	    
		ret += writeStatement("package "+getPackage(section.getModule()));
		ret += emptyline();
		ret += getStandardActionImports();
		ret += writeImport("java.util.List");
		ret += writeImport("java.util.ArrayList");
		ret += emptyline();
	    ret += writeImport(DataFacadeGenerator.getDocumentImport(context, doc));
	    ret += writeImport(ModuleBeanGenerator.getListItemBeanImport(context, doc));
		ret += emptyline();

		ret += writeString("public class "+getExecuteQueryActionName(section)+" extends "+getShowActionName(section)+" {");
		increaseIdent();
		ret += emptyline();

		ret += writeString(getExecuteDeclaration());
		increaseIdent();
		ret += emptyline();
		ret += writeStatement("String property = req.getParameter("+quote("property")+")");
		ret += writeStatement("String criteria = req.getParameter("+quote("criteria")+")");
		ret += writeString("if( criteria!=null && criteria.length()==0)");
		ret += writeIncreasedStatement("criteria = null;");
		//ret += writeStatement("System.out.println(property+\"=\"+criteria)");
		
		String listName = doc.getMultiple().toLowerCase();
		ret += writeStatement("List<"+doc.getName()+"> "+listName+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getMultiple()+"ByProperty(property, criteria)");
		//ret += writeStatement("System.out.println(\"result: \"+"+listName+")");

		ret += writeStatement("List<"+ModuleBeanGenerator.getListItemBeanName(doc)+"> beans = new ArrayList<"+ModuleBeanGenerator.getListItemBeanName(doc)+">("+listName+".size())");
		ret += writeString("for (int i=0; i<"+listName+".size(); i++){");
		increaseIdent();
		ret += writeStatement("beans.add("+getMakeBeanFunctionName(ModuleBeanGenerator.getListItemBeanName(doc))+"(("+doc.getName()+")"+listName+".get(i)))");
		ret += closeBlock();
		ret += emptyline();
		ret += writeStatement("addBeanToRequest(req, "+quote(listName)+", beans)");
		
		
		ret += writeStatement("return mapping.findForward(\"success\")");
		ret +=closeBlock();

		ret += closeBlock();
		

		return ret;
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
	
	private String generateVersionInfoAction(MetaModuleSection section){
		StringBuilder ret = new StringBuilder();
		MetaDocument doc = section.getDocument();
		appendStatement(ret, "package ", getPackage(section.getModule()));
		emptyline(ret);

		//write imports...
		appendStandardActionImports(ret);
	    appendImport(ret, DataFacadeGenerator.getDocumentImport(context, doc));
	    appendImport(ret, "net.anotheria.util.NumberUtils");
	    emptyline(ret);

		appendString(ret, "public class ",getVersionInfoActionName(section)," extends ",getBaseActionName(section)," {");
		increaseIdent();
		emptyline(ret);
	    
		appendString(ret, getExecuteDeclaration());
		increaseIdent();
	
		appendStatement(ret, "String id = getStringParameter(req, PARAM_ID)");
		appendStatement(ret, doc.getName()+" "+doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id);");
		appendStatement(ret, "long timestamp = "+doc.getVariableName()+".getLastUpdateTimestamp()");
		appendStatement(ret, "String lastUpdateDate = NumberUtils.makeDigitalDateStringLong(timestamp)");
		appendStatement(ret, "lastUpdateDate += \" \"+NumberUtils.makeTimeString(timestamp)");

		try{
			doc.getField("name");
			appendStatement(ret, "req.setAttribute("+quote("documentName")+", "+doc.getVariableName()+".getName())");
		}catch(Exception ignored){
			appendStatement(ret, "req.setAttribute("+quote("documentName")+", \"Id:\"+"+doc.getVariableName()+".getId())");
		}
		appendStatement(ret, "req.setAttribute(",quote("documentType"),", ",doc.getVariableName(),".getClass())");
		appendStatement(ret, "req.setAttribute(",quote("lastUpdate"),", lastUpdateDate)");
		
		appendStatement(ret, "return mapping.findForward("+quote("success")+")");
	    
		closeBlock(ret);
		closeBlock(ret);
		return ret.toString();
	}
	
	private String generateUpdateAction(MetaModuleSection section){
		StringBuilder ret = new StringBuilder(5000);
		MetaDocument doc = section.getDocument();
		MetaDialog dialog = section.getDialogs().get(0);
		appendStatement(ret, "package "+getPackage(section.getModule()));
		emptyline(ret);

		//write imports...
		appendStandardActionImports(ret);
		appendImport(ret, ModuleBeanGenerator.getDialogBeanImport(context, dialog, doc));
	    appendImport(ret, DataFacadeGenerator.getDocumentImport(context, doc));
	    appendImport(ret, DataFacadeGenerator.getDocumentFactoryImport(context, doc));
	    emptyline(ret);
	    
		List<MetaViewElement> elements = createMultilingualList(dialog.getElements(), doc, context);
		for (int i=0; i<elements.size(); i++){
			MetaViewElement elem = elements.get(i);
			if (elem instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)elem;
				MetaProperty p = doc.getField(field.getName());
				if (p.getType().equals("image")){
					appendImport(ret, "net.anotheria.webutils.filehandling.actions.FileStorage");
					appendImport(ret, "net.anotheria.webutils.filehandling.beans.TemporaryFileHolder");
					break;
				}
			}
		}

	    
		appendString(ret, "public class "+getUpdateActionName(section)+" extends "+getBaseActionName(section)+" {");
		increaseIdent();
		emptyline(ret);
	    
		appendString(ret, getExecuteDeclaration());
		increaseIdent();
	
		appendStatement(ret, ModuleBeanGenerator.getDialogBeanName(dialog, doc)+" form = ("+ModuleBeanGenerator.getDialogBeanName(dialog, doc)+") af");
		//check if we have a form submission at all.
		appendString(ret, "if (!form.isFormSubmittedFlag())");
		appendIncreasedStatement(ret, "throw new RuntimeException(\"Request broken!\")");
		//if update, then first get the target object.
		appendStatement(ret, "boolean create = false");
		appendStatement(ret, doc.getName()+" "+doc.getVariableName()+" = null");
		appendString(ret, "if (form.getId()!=null && form.getId().length()>0){");	
		appendIncreasedString(ret, doc.getVariableName()+" = ("+doc.getName()+")"+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(form.getId()).clone();");
		appendString(ret, "}else{");
		increaseIdent();
		appendString(ret, doc.getVariableName()+" = "+DataFacadeGenerator.getDocumentFactoryName(doc)+".create"+doc.getName()+"();");
		appendString(ret, "create = true;");
		closeBlock(ret);
		emptyline(ret);
		
		appendStatement(ret, "String nextAction = req.getParameter("+quote("nextAction")+")");
		appendString(ret, "if (nextAction == null || nextAction.length() == 0)");
		appendIncreasedStatement(ret, "nextAction = \"close\"");
		emptyline(ret);
		
		//set fields
		for (int i=0; i<elements.size(); i++){
			MetaViewElement elem = elements.get(i);
			if (elem instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)elem;
				String lang = getElementLanguage(field);
				//System.out.println(ret, "checking field:"+field);
				if (field.isReadonly()){
					appendString(ret, "//skipped "+field.getName()+" because it's readonly.");
				}else{
					MetaProperty p = doc.getField(field.getName());
					//handle images.
					if (p.getType().equals("image")){
						//will work only with one image.
						appendString(ret, "//handle image");
						appendStatement(ret, "TemporaryFileHolder holder = FileStorage.getTemporaryFile(req)");
						appendString(ret, "if (holder!=null && holder.getData()!=null){");
						increaseIdent();
						appendStatement(ret, "FileStorage.storeFilePermanently(req, holder.getFileName())");
						appendStatement(ret, doc.getVariableName()+"."+p.toSetter()+"(holder.getFileName())");
						appendStatement(ret, "FileStorage.removeTemporaryFile(req)");
						closeBlock(ret);
						continue;
					}
					if (! (p instanceof MetaContainerProperty)){
						String propertyCopy = "";
						propertyCopy += doc.getVariableName()+"."+p.toSetter(lang)+"(";
						propertyCopy += "form."+p.toBeanGetter(lang)+"())";
						appendStatement(ret, propertyCopy);
					}else{
						appendString(ret, "// skipped container "+p.getName());
					}
					
				}
			}
		}
		
		emptyline(ret);
		appendStatement(ret, doc.getName(), " updatedCopy = null");
		
		appendString(ret, "if (create){");
		//appendIncreasedStatement(ret, "System.out.println(\"creating\")");
		appendIncreasedStatement(ret, "updatedCopy = "+getServiceGetterCall(section.getModule())+".create"+doc.getName()+"("+doc.getVariableName()+")");
		appendString(ret, "}else{");
		appendIncreasedStatement(ret, "updatedCopy = "+getServiceGetterCall(section.getModule())+".update"+doc.getName()+"( "+doc.getVariableName()+")");
		//appendIncreasedStatement(ret, "System.out.println(\"updating\")");
		appendString(ret, "}");
		appendString(ret, "if (nextAction.equalsIgnoreCase("+quote("stay")+"))");
	    appendIncreasedStatement(ret, "res.sendRedirect("+getEditActionRedirect(doc)+"+"+quote("&pId=")+"+updatedCopy.getId())");
		appendString(ret, "else");
	    appendIncreasedStatement(ret, "res.sendRedirect("+getShowActionRedirect(doc)+")");
	    appendStatement(ret, "return null");
		closeBlock(ret);
		emptyline(ret);
	    
		closeBlock(ret);
		return ret.toString();
	}

	private String generateSwitchMultilingualityAction(MetaModuleSection section){
		StringBuilder ret = new StringBuilder(4000);
		MetaDocument doc = section.getDocument();
		appendStatement(ret, "package "+getPackage(section.getModule()));
		emptyline(ret);

		//write imports...
		appendStandardActionImports(ret);
	    appendImport(ret, DataFacadeGenerator.getDocumentImport(context, doc));
	    appendImport(ret, "net.anotheria.asg.data.MultilingualObject");
	    emptyline(ret);
	    
	    appendComment(ret, "This class enables or disables support for multiple languages for a particular document.");
		appendString(ret, "public class "+getSwitchMultilingualityActionName(section)+" extends "+getBaseActionName(section)+" {");
		increaseIdent();
		emptyline(ret);
	    
		appendString(ret, getExecuteDeclaration());
		increaseIdent();
		
		appendStatement(ret, "String id = getStringParameter(req, PARAM_ID)");
		appendStatement(ret, "String value = getStringParameter(req, "+quote("value")+")");

		appendStatement(ret, doc.getName()+" "+doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");
		appendStatement(ret, "((MultilingualObject)"+doc.getVariableName()+").setMultilingualDisabledInstance(Boolean.valueOf(value))");
		appendStatement(ret, getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
	    appendStatement(ret, "res.sendRedirect("+getEditActionRedirect(doc)+"+"+quote("&pId=")+"+id)");
		
	    appendStatement(ret, "return null");
		closeBlock(ret); //end doExecute
		closeBlock(ret); // end class
		return ret.toString();
	}

	
	private String generateLanguageCopyAction(MetaModuleSection section){
		StringBuilder ret = new StringBuilder(4000);
		MetaDocument doc = section.getDocument();
		appendStatement(ret, "package "+getPackage(section.getModule()));
		emptyline(ret);

		//write imports...
		appendStandardActionImports(ret);
	    appendImport(ret, DataFacadeGenerator.getDocumentImport(context, doc));
	    emptyline(ret);
	    
	    appendComment(ret, "This class copies multilingual contents from one language to another in a given document");
		appendString(ret, "public class "+getLanguageCopyActionName(section)+" extends "+getBaseActionName(section)+" {");
		increaseIdent();
		emptyline(ret);
	    
		appendString(ret, getExecuteDeclaration());
		increaseIdent();
		
		appendStatement(ret, "String sourceLanguage = req.getParameter("+quote("pSrcLang")+")");
		appendString(ret, "if (sourceLanguage==null || sourceLanguage.length()==0)");
		appendIncreasedStatement(ret, "throw new RuntimeException("+quote("No source language")+")");
		emptyline(ret);

		appendStatement(ret, "String destLanguage = req.getParameter("+quote("pDestLang")+")");
		appendString(ret, "if (destLanguage==null || destLanguage.length()==0)");
		appendIncreasedStatement(ret, "throw new RuntimeException("+quote("No destination language")+")");
		emptyline(ret);

		appendStatement(ret, "String id = getStringParameter(req, PARAM_ID)");
		appendStatement(ret, doc.getName()+" "+doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");
		appendStatement(ret, doc.getVariableName()+"."+DataFacadeGenerator.getCopyMethodName()+"(sourceLanguage, destLanguage)");
		appendStatement(ret, getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
	    appendStatement(ret, "res.sendRedirect("+getEditActionRedirect(doc)+"+"+quote("&pId=")+"+id)");
		
	    appendStatement(ret, "return null");
		closeBlock(ret); //end doExecute
		closeBlock(ret); // end class
		return ret.toString();
	}

	private String generateEditAction(MetaModuleSection section){
		StringBuilder ret = new StringBuilder(5000);
		MetaDocument doc = section.getDocument();
		MetaDialog dialog = section.getDialogs().get(0);
		List<MetaViewElement> elements = createMultilingualList(dialog.getElements(), doc, context);
		
		EnumerationPropertyGenerator enumProGenerator = new EnumerationPropertyGenerator(doc);

		appendStatement(ret, "package ", getPackage(section.getModule()));
		emptyline(ret);

		//write imports...
		appendStandardActionImports(ret);
		appendImport(ret, ModuleBeanGenerator.getDialogBeanImport(context, dialog, doc));
		appendImport(ret, DataFacadeGenerator.getDocumentImport(context, doc));
		appendImport(ret, "net.anotheria.asg.util.helper.cmsview.CMSViewHelperUtil");
		if (doc.isMultilingual())
			appendImport(ret, "net.anotheria.asg.data.MultilingualObject");
		emptyline(ret);
		
		boolean listImported = false;
		
		//check if we have to import list.
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			if (element instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)element;
				MetaProperty p = doc.getField(field.getName());
				if (p.isLinked() || p instanceof MetaEnumerationProperty){
					appendImport(ret, "java.util.List");
					appendImport(ret, "java.util.ArrayList");
					appendImport(ret, "net.anotheria.webutils.bean.LabelValueBean");
					listImported = true;
					break;
				}
			}
		}
		
		List<String> customImports = new ArrayList<String>();
		//check if we have to property definition files.
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			if (element instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)element;
				MetaProperty p = doc.getField(field.getName());
				if (p instanceof MetaEnumerationProperty){
					MetaEnumerationProperty mep = (MetaEnumerationProperty)p;
					EnumerationType type = (EnumerationType)GeneratorDataRegistry.getInstance().getType(mep.getEnumeration());
					String customImport = EnumerationGenerator.getUtilsImport(type);
					if (customImports.indexOf(customImport)==-1){
					    appendImport(ret, customImport);
					    customImports.add(customImport);
					}
				}
			}
		}
		
	    List<DirectLink> backlinks = GeneratorDataRegistry.getInstance().findLinksToDocument(doc);
	    if (backlinks.size()>0){
	    	appendImport(ret, "net.anotheria.anodoc.query2.QueryProperty");
	    	appendImport(ret, "net.anotheria.asg.util.bean.LinkToMeBean");
	    	if (!listImported){
	    		listImported=true;
				appendImport(ret, "java.util.List");
				appendImport(ret, "java.util.ArrayList");
	    	}
	    	for (DirectLink l : backlinks){
	    		String imp = DataFacadeGenerator.getDocumentImport(context, l.getDocument());
	    		if (customImports.indexOf(imp)==-1){
	    			appendImport(ret, imp);
	    			customImports.add(imp);
	    		}
	    	}
	    }
		
		emptyline(ret);

	    
		appendString(ret, "public class "+getEditActionName(section)+" extends "+getShowActionName(section)+" {");
		increaseIdent();
		emptyline(ret);
	    
		appendString(ret, getExecuteDeclaration());
		increaseIdent();
	
		appendStatement(ret, "String id = getStringParameter(req, PARAM_ID)");
		appendStatement(ret, ModuleBeanGenerator.getDialogBeanName(dialog, doc), " form = new ", ModuleBeanGenerator.getDialogBeanName(dialog, doc), "() ");	

		appendStatement(ret, doc.getName()," ",doc.getVariableName()," = ",getServiceGetterCall(section.getModule()),".get",doc.getName(),"(id);");
		
		//set field
		for (int i=0; i<elements.size(); i++){
			MetaViewElement elem = elements.get(i);
			//System.out.println("checking elem:"+elem);
			if (elem instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)elem;
				MetaProperty p = doc.getField(field.getName());
				if (p instanceof MetaContainerProperty){
					appendString(ret, "// "+p.getName()+" is a table, storing size only");
					String lang = getElementLanguage(elem);
					appendStatement(ret, "form."+p.toBeanSetter(lang)+"("+doc.getVariableName()+"."+DataFacadeGenerator.getContainerSizeGetterName((MetaContainerProperty)p, lang)+"())");
				}else{
					String lang = getElementLanguage(elem);
					String propertyCopy = "";
					propertyCopy += "form."+p.toBeanSetter(lang)+"(";
					propertyCopy += doc.getVariableName()+"."+p.toGetter(lang)+"())";
					appendStatement(ret, propertyCopy);
				}
			}
		}
		
		if (doc.isMultilingual()){
			MetaProperty p = doc.getField(ModuleBeanGenerator.FIELD_ML_DISABLED);
			String propertyCopy = "form."+p.toBeanSetter()+"(((MultilingualObject)"+doc.getVariableName()+").isMultilingualDisabledInstance())";
			appendStatement(ret, propertyCopy);
		}
		
		emptyline(ret);
		
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
					emptyline(ret);

					if (linkTargets.contains(link.getLinkTarget())){
						appendString(ret, "//reusing collection for "+link.getName()+" to "+link.getLinkTarget()+".");
					}else{
					
						appendString(ret, "//link "+link.getName()+" to "+link.getLinkTarget());
						appendString(ret, "//to lazy to include List in the imports.");
						appendStatement(ret, "List<"+DataFacadeGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), targetDocument)+"> "+listName+" = "+getServiceGetterCall(targetModule)+".get"+targetDocument.getMultiple()+"()");
						appendStatement(ret, "List<LabelValueBean> "+listName+"Values = new ArrayList<LabelValueBean>("+listName+".size()+1)");
						appendStatement(ret, listName+"Values.add(new LabelValueBean("+quote("")+", \"-----\"))");
						appendString(ret, "for ("+(DataFacadeGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), targetDocument))+" "+targetDocument.getVariableName()+" : "+listName+"){");
						increaseIdent();
						appendStatement(ret, "LabelValueBean bean = new LabelValueBean("+targetDocument.getVariableName()+".getId(), "+targetDocument.getVariableName()+".getName() )");
						appendStatement(ret, listName,"Values.add(bean)");
						closeBlock(ret);

					}

					String lang = getElementLanguage(element);
					appendStatement(ret, "form."+p.toBeanSetter()+"Collection"+(lang==null ? "":lang)+"("+listName+"Values"+")");
					
					appendString(ret, "try{");
					increaseIdent();
					String getter = getServiceGetterCall(targetModule)+".get"+targetDocument.getName()+"("+doc.getVariableName()+"."+p.toGetter()+"()).getName()";
					appendStatement(ret, "form."+p.toBeanSetter()+"CurrentValue"+(lang==null ? "":lang)+"("+getter+")");
					decreaseIdent();
					appendString(ret, "}catch(Exception e){");
					appendIncreasedStatement(ret, "form."+p.toBeanSetter()+"CurrentValue"+(lang==null ? "":lang)+"("+quote("none")+")");
					appendString(ret, "}");
					linkTargets.add(link.getLinkTarget());
					
				}
				
				if (p instanceof MetaEnumerationProperty){
				    ret.append(enumProGenerator.generateEnumerationPropertyHandling((MetaEnumerationProperty)p, true));
				}
			}
		}
		
		
		appendStatement(ret, "addBeanToRequest(req, "+quote(StrutsConfigGenerator.getDialogFormName(dialog, doc))+" , form)");
		appendStatement(ret, "addBeanToRequest(req, "+quote("save.label.prefix")+", "+quote("Update")+")");
		
		//add field descriptions ...
		appendStatement(ret, "String fieldDescription = null");
		for (MetaProperty p : doc.getProperties()){
			appendStatement(ret, "fieldDescription = CMSViewHelperUtil.getFieldExplanation("+quote(doc.getParentModule().getName()+"."+doc.getName())+ ", "+doc.getVariableName()+", "+quote(p.getName())+")");
			appendString(ret, "if (fieldDescription!=null && fieldDescription.length()>0)");
			appendIncreasedStatement(ret, "req.setAttribute("+quote("description."+p.getName())+", fieldDescription)");
		}
	
	    if (backlinks.size()>0){
	    	emptyline(ret);
	    	appendCommentLine(ret, "Generating back link handling...");
	    	appendStatement(ret, "List<LinkToMeBean> linksToMe = findLinksToCurrentDocument("+doc.getVariableName()+".getId())");
	    	appendString(ret, "if (linksToMe.size()>0)");
	    	appendIncreasedStatement(ret, "req.setAttribute("+quote("linksToMe")+", linksToMe)");
	    }

		
		appendStatement(ret, "return mapping.findForward(\"success\")");
		closeBlock(ret);
		emptyline(ret);
		
	    //backlinks
		if (backlinks.size()>0){
			appendString(ret, "private List<LinkToMeBean> findLinksToCurrentDocument(String documentId){");
			increaseIdent();
			appendStatement(ret, "List<LinkToMeBean> ret = new ArrayList<LinkToMeBean>()");
			for (DirectLink l : backlinks){
				appendString(ret, "try{");
				String methodName = "";
				if (l.getProperty().isMultilingual()){
					for (String lang : context.getLanguages()){
						methodName = "findLinkToCurrentDocumentIn"+l.getModule().getName()+l.getDocument().getName()+StringUtils.capitalize(l.getProperty().getName(lang));
						appendIncreasedStatement(ret, "ret.addAll("+methodName+"(documentId))");
					} 
				}else{
					methodName = "findLinkToCurrentDocumentIn"+l.getModule().getName()+l.getDocument().getName()+StringUtils.capitalize(l.getProperty().getName());
					appendIncreasedStatement(ret, "ret.addAll("+methodName+"(documentId))");	
				}
				appendString(ret, "}catch(Exception ignored){");
				appendIncreasedStatement(ret, "log.warn(\""+methodName+"(\"+documentId+\")\", ignored)");
				appendString(ret, "}");
			}
			appendStatement(ret, "return ret");
			closeBlock(ret);
			
			for (DirectLink l : backlinks){
				if (l.getProperty().isMultilingual()){
					for (String lang : context.getLanguages()){
						appendString(ret, "private List<LinkToMeBean> findLinkToCurrentDocumentIn"+l.getModule().getName()+l.getDocument().getName()+StringUtils.capitalize(l.getProperty().getName(lang))+"(String documentId) throws "+ServiceGenerator.getExceptionImport(context,l.getModule())+"{");
						increaseIdent();
						appendStatement(ret, "List<LinkToMeBean> ret = new ArrayList<LinkToMeBean>()");
						appendStatement(ret, "QueryProperty p = new QueryProperty("+l.getDocument().getName()+"."+l.getProperty().toNameConstant(lang)+", documentId)");
						//appendStatement("List<"+l.getDocument().getName()+"> list = "+getServiceGetterCall(l.getModule())+".get"+l.getDocument().getMultiple()+"ByProperty(p)");
						appendCommentLine(ret, "temporarly - replacy with query property");
						appendStatement(ret, "List<"+l.getDocument().getName()+"> list = "+getServiceGetterCall(l.getModule())+".get"+l.getDocument().getMultiple()+"ByProperty(p.getName(), p.getValue())");
						appendString(ret, "for ("+l.getDocument().getName() +" doc : list ){");
						increaseIdent();
						appendStatement(ret, "ret.add(new LinkToMeBean(doc, "+quote(l.getProperty().getName())+"))");
						closeBlock(ret);
						appendStatement(ret, "return ret");
						closeBlock(ret);
					}
				}else{
					appendString(ret, "private List<LinkToMeBean> findLinkToCurrentDocumentIn"+l.getModule().getName()+l.getDocument().getName()+StringUtils.capitalize(l.getProperty().getName())+"(String documentId) throws "+ServiceGenerator.getExceptionImport(context, l.getModule())+"{");
					increaseIdent();
					appendStatement(ret, "List<LinkToMeBean> ret = new ArrayList<LinkToMeBean>()");
					appendStatement(ret, "QueryProperty p = new QueryProperty("+l.getDocument().getName()+"."+l.getProperty().toNameConstant()+", documentId)");
					//appendStatement("List<"+l.getDocument().getName()+"> list = "+getServiceGetterCall(l.getModule())+".get"+l.getDocument().getMultiple()+"ByProperty(p)");
					appendCommentLine(ret, "temporarly - replacy with query property");
					appendStatement(ret, "List<"+l.getDocument().getName()+"> list = "+getServiceGetterCall(l.getModule())+".get"+l.getDocument().getMultiple()+"ByProperty(p.getName(), p.getValue())");
					appendString(ret, "for ("+l.getDocument().getName() +" doc : list ){");
					increaseIdent();
					appendStatement(ret, "ret.add(new LinkToMeBean(doc, "+quote(l.getProperty().getName())+"))");
					closeBlock(ret);
					appendStatement(ret, "return ret");
					closeBlock(ret);
				}
			}
		}
		
	    
		closeBlock(ret);
		return ret.toString();
	}

	private String generateDeleteAction(MetaModuleSection section){
	    StringBuilder ret = new StringBuilder(1000);
	    MetaDocument doc = section.getDocument();
	    appendStatement(ret, "package "+getPackage(section.getModule()));
	    emptyline(ret);

	    //write imports...
	    appendStandardActionImports(ret);
	    
	    appendString(ret, "public class "+getDeleteActionName(section)+" extends "+getBaseActionName(section)+" {");
	    increaseIdent();
	    emptyline(ret);
	    
	    appendString(ret, getExecuteDeclaration());
	    increaseIdent();
	    appendStatement(ret, "String id = getStringParameter(req, PARAM_ID)");
	    appendStatement(ret, getServiceGetterCall(section.getModule())+".delete"+doc.getName()+"(id)");
	    appendStatement(ret, "res.sendRedirect("+getShowActionRedirect(doc)+")");
	    appendStatement(ret, "return null");
	    closeBlock(ret);
	    emptyline(ret);
	    
	    closeBlock(ret);
	    return ret.toString();
	}

	private String generateDuplicateAction(MetaModuleSection section){
		StringBuilder ret = new StringBuilder(1000);
		MetaDocument doc = section.getDocument();
		appendStatement(ret, "package "+getPackage(section.getModule()));
		emptyline(ret);

		//write imports...
		appendStandardActionImports(ret);
		appendImport(ret, DataFacadeGenerator.getDocumentImport(context, doc));
		appendImport(ret, DataFacadeGenerator.getDocumentFactoryImport(context, doc));
	    
		appendString(ret, "public class "+getDuplicateActionName(section)+" extends "+getBaseActionName(section)+" {");
		increaseIdent();
		emptyline(ret);
	    
		appendString(ret, getExecuteDeclaration());
		increaseIdent();
		appendStatement(ret, "String id = getStringParameter(req, PARAM_ID)");
		appendStatement(ret, doc.getName()+" "+doc.getVariableName()+"Src = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id);");
		appendStatement(ret, doc.getName()+" "+doc.getVariableName()+"Dest = "+DataFacadeGenerator.getDocumentFactoryName(doc)+".create"+doc.getName()+"("+doc.getVariableName()+"Src)");

		appendStatement(ret, doc.getName()+" "+doc.getVariableName()+"Created = "+getServiceGetterCall(section.getModule())+".create"+doc.getName()+"("+doc.getVariableName()+"Dest"+")");
	    appendStatement(ret, "res.sendRedirect("+getEditActionRedirect(doc)+"+"+quote("&")+"+PARAM_ID+"+quote("=")+"+"+doc.getVariableName()+"Created.getId()"+")");
	    appendStatement(ret, "return null");
		closeBlock(ret);
		emptyline(ret);
	    
		closeBlock(ret);
		return ret.toString();
	}

	private String generateNewAction(MetaModuleSection section){
		StringBuilder ret = new StringBuilder(2500);
		MetaDocument doc = section.getDocument();
		MetaDialog dialog = section.getDialogs().get(0);
		//List<MetaViewElement> elements = dialog.getElements();
		List<MetaViewElement> elements = createMultilingualList(dialog.getElements(), doc, context);
		
		EnumerationPropertyGenerator enumPropGen = new EnumerationPropertyGenerator(doc);
		
		appendStatement(ret, "package "+getPackage(section.getModule()));
		emptyline(ret);

		//write imports...
		appendStandardActionImports(ret);
		appendImport(ret, ModuleBeanGenerator.getDialogBeanImport(context, dialog, doc));
		emptyline(ret);

		//check if we have to import list.
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			if (element instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)element;
				MetaProperty p = doc.getField(field.getName());
				if (p.isLinked() || p instanceof MetaEnumerationProperty){
					appendImport(ret, "java.util.List");
					appendImport(ret, "java.util.ArrayList");
					appendImport(ret, "net.anotheria.webutils.bean.LabelValueBean");
					break;
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
					if (importedEnumerations.get(mep.getName())==null){
						EnumerationType type = (EnumerationType)GeneratorDataRegistry.getInstance().getType(mep.getEnumeration());
						appendImport(ret, EnumerationGenerator.getUtilsImport(type));
						//System.out.println("Adding enumeration import: "+mep.getType()+", "+mep+", "+mep.getName());
						importedEnumerations.put(mep.getName(), mep);
					}
				}
			}
		}
		
		emptyline(ret);
	    
		appendString(ret, "public class "+getNewActionName(section)+" extends "+getShowActionName(section)+" {");
		increaseIdent();
		emptyline(ret);
	    
		appendString(ret, getExecuteDeclaration());
		increaseIdent();
	
		appendStatement(ret, ModuleBeanGenerator.getDialogBeanName(dialog, doc)+" form = new "+ModuleBeanGenerator.getDialogBeanName(dialog, doc)+"() ");	
		appendStatement(ret, "form.setId("+quote("")+")");
		
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
					emptyline(ret);
					
					if (linkTargets.contains(link.getLinkTarget())){
						appendString(ret, "//link "+link.getName()+" to "+link.getLinkTarget()+" reuses collection.");
					}else{
						appendString(ret, "//link "+link.getName()+" to "+link.getLinkTarget());
						appendStatement(ret, "List<"+DataFacadeGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), targetDocument)+"> "+listName+" = "+getServiceGetterCall(targetModule)+".get"+targetDocument.getMultiple()+"()");
						appendStatement(ret, "List<LabelValueBean> "+listName+"Values = new ArrayList<LabelValueBean>("+listName+".size()+1)");
						appendStatement(ret, listName+"Values.add(new LabelValueBean("+quote("")+", \"-----\"))");
						appendString(ret, "for ("+(DataFacadeGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), targetDocument))+" "+targetDocument.getVariableName()+" : "+listName+"){");
						increaseIdent();
						
						appendStatement(ret, "LabelValueBean bean = new LabelValueBean("+targetDocument.getVariableName()+".getId(), "+targetDocument.getVariableName()+".getName() )");
						appendStatement(ret, listName+"Values.add(bean)");
						closeBlock(ret);
					}
					
					String lang = getElementLanguage(element);
					appendStatement(ret, "form."+p.toBeanSetter()+"Collection"+(lang==null ? "" : lang)+"("+listName+"Values"+")");
					linkTargets.add(link.getLinkTarget());
				}//...end if (p.isLinked())

				if (p instanceof MetaEnumerationProperty){
				    ret.append(enumPropGen.generateEnumerationPropertyHandling((MetaEnumerationProperty)p, false));
				}
				
			}
		}

		emptyline(ret);
		appendStatement(ret, "addBeanToRequest(req, "+quote(StrutsConfigGenerator.getDialogFormName(dialog, doc))+" , form)");
		appendStatement(ret, "addBeanToRequest(req, "+quote("save.label.prefix")+", "+quote("Create")+")");

		appendStatement(ret, "return mapping.findForward(\"success\")");
		closeBlock(ret);
		emptyline(ret);
	    
		closeBlock(ret);
		return ret.toString();
	}

	private String generateBaseAction(MetaModuleSection section){
	    StringBuilder ret = new StringBuilder(1000);

	    //MetaDocument doc = section.getDocument();
//	    MetaModule mod = section.getModule();
	    
	    appendStatement(ret, "package "+getPackage(section.getModule()));
	    emptyline(ret);
	    appendImport(ret, context.getPackageName(MetaModule.SHARED)+".action."+BaseViewActionGenerator.getViewActionName(view));
	    emptyline(ret);
	    
	    appendString(ret, "public abstract class "+getBaseActionName(section)+" extends "+BaseViewActionGenerator.getViewActionName(view)+" {");
	    increaseIdent();
	    emptyline(ret);

	    //generate getTitle
	    appendString(ret, "public String getTitle(){");
	    increaseIdent();
	    appendStatement(ret, "return "+quote(section.getTitle()));
	    closeBlock(ret);
	    emptyline(ret);
	    
	    
	    closeBlock(ret);
	    return ret.toString();
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
	    String ret = "";
	    ret += "public ActionForward anoDocExecute(";
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

	/*
	public static String getContainerMoveUpEntryActionName(MetaDocument doc, MetaContainerProperty property){
		return "MoveUp"+doc.getMultiple()+StringUtils.capitalize(property.getName())+getContainerNameAddy(property)+"Action";
	}
	
	public static String getContainerMoveTopEntryActionName(MetaDocument doc, MetaContainerProperty property){
		return "MoveTop"+doc.getMultiple()+StringUtils.capitalize(property.getName())+getContainerNameAddy(property)+"Action";
	}

	public static String getContainerMoveDownEntryActionName(MetaDocument doc, MetaContainerProperty property){
		return "MoveDown"+doc.getMultiple()+StringUtils.capitalize(property.getName())+getContainerNameAddy(property)+"Action";
	}

	public static String getContainerMoveBottomEntryActionName(MetaDocument doc, MetaContainerProperty property){
		return "MoveBottom"+doc.getMultiple()+StringUtils.capitalize(property.getName())+getContainerNameAddy(property)+"Action";
	}
*/
	private static String getContainerNameAddy(MetaContainerProperty p){
		return p.getContainerEntryName();
	}
	
	private String generateContainerAddRowAction(MetaModuleSection section, MetaContainerProperty container){
		if (container instanceof MetaTableProperty)
			return generateTableAddRowAction(section, (MetaTableProperty)container);

		if (container instanceof MetaListProperty)
			return generateListAddRowAction(section, (MetaListProperty)container);
			
		throw new RuntimeException("Unsupported container type: "+container.getClass().getName());
	}
	
	private String generateContainerQuickAddAction(MetaModuleSection section, MetaContainerProperty container){
		if (container instanceof MetaListProperty && ((MetaListProperty)container).getContainedProperty().isLinked())
			return generateListQuickAddAction(section, (MetaListProperty)container);
		return "";
		//throw new RuntimeException("Unsupported container type: "+container.getClass().getName());
	}

	private String generateListAddRowAction(MetaModuleSection section, MetaListProperty list){
		StringBuilder ret = new StringBuilder(1000);

		MetaDocument doc = section.getDocument();

		appendStatement(ret, "package ", getPackage(section.getModule()));
		emptyline(ret);
	    
		//write imports...
		appendStandardActionImports(ret);
		appendImport(ret, DataFacadeGenerator.getDocumentImport(context, doc));
		appendImport(ret, ModuleBeanGenerator.getContainerEntryFormImport(doc, list));
		emptyline(ret);
		
		appendString(ret, "public class "+getContainerAddEntryActionName(doc, list)+" extends "+getContainerShowActionName(doc, list)+"{");	
		increaseIdent();
		emptyline(ret);
		appendString(ret, getExecuteDeclaration());
		increaseIdent();
		appendStatement(ret, ModuleBeanGenerator.getContainerEntryFormName(list)+" form = ("+ModuleBeanGenerator.getContainerEntryFormName(list)+") af");
		appendStatement(ret, "String id = form.getOwnerId()");
		appendStatement(ret, doc.getName()+" "+doc.getVariableName());
		appendStatement(ret, doc.getVariableName()," = ",getServiceGetterCall(section.getModule()),".get",doc.getName(),"(id)");
		
		String call = "";
		MetaProperty p = list.getContainedProperty();
		String getter = "form."+p.toBeanGetter()+"()";
		call += getter;
		appendStatement(ret, doc.getVariableName()+"."+DataFacadeGenerator.getContainerEntryAdderName(list)+"("+call+")");
		appendStatement(ret, getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		appendStatement(ret, "return "+getSuperCall());
		closeBlock(ret);
		closeBlock(ret);
		
		return ret.toString();


	}
	
	private String generateListQuickAddAction(MetaModuleSection section, MetaListProperty list){
		String ret = "";

		MetaDocument doc = section.getDocument();

		ret += writeStatement("package "+getPackage(section.getModule()));
		ret += emptyline();
	    
		//write imports...
		ret += getStandardActionImports();
		ret += writeImport(DataFacadeGenerator.getDocumentImport(context, doc));
		ret += writeImport(ModuleBeanGenerator.getContainerQuickAddFormImport(doc, list));
		ret += emptyline();
		ret += writeImport("net.anotheria.util.StringUtils");
		
		ret += writeString("public class "+getContainerQuickAddActionName(doc, list)+" extends "+getContainerShowActionName(doc, list)+"{");	
		increaseIdent();
		ret += emptyline();
		ret += writeString(getExecuteDeclaration());
		increaseIdent();
		ret += writeStatement(ModuleBeanGenerator.getContainerQuickAddFormName(list)+" form = ("+ModuleBeanGenerator.getContainerQuickAddFormName(list)+") af");
		ret += writeStatement("String id = form.getOwnerId()");
		ret += writeStatement(doc.getName()+" "+doc.getVariableName());
		ret += writeStatement(doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");
		ret += writeStatement("String paramIdsToAdd = form.getQuickAddIds()");

		ret += emptyline();
		ret += writeStatement("String idParameters[] = StringUtils.tokenize(paramIdsToAdd, ',')");
		ret += writeString("for (String anIdParam : idParameters){");
		increaseIdent();
		
		ret += writeString("String ids[] = StringUtils.tokenize(anIdParam, '-');");
		ret += writeString("for (int i=Integer.parseInt(ids[0]); i<=Integer.parseInt(ids[ids.length-1]); i++){");
		increaseIdent();
		ret += writeStatement(doc.getVariableName()+"."+DataFacadeGenerator.getContainerEntryAdderName(list)+"("+quote("")+"+i)");
		ret += closeBlock();
		
		ret += closeBlock();
		String call = "";
		MetaProperty p = list.getContainedProperty();
		String getter = "form."+p.toBeanGetter()+"()";
		call += getter;
		ret += writeStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		ret += writeStatement("return "+getSuperCall());
		ret += closeBlock();
		ret += closeBlock();
		
		return ret;


	}

	private String generateTableAddRowAction(MetaModuleSection section, MetaTableProperty table){
		String ret = "";
		
		MetaDocument doc = section.getDocument();

		ret += writeStatement("package "+getPackage(section.getModule()));
		ret += emptyline();
	    
		//write imports...
		ret += getStandardActionImports();
		ret += writeImport(DataFacadeGenerator.getDocumentImport(context, doc));
		ret += writeImport(ModuleBeanGenerator.getContainerEntryFormImport(doc, table));
		ret += emptyline();
		
		ret += writeString("public class "+getContainerAddEntryActionName(doc, table)+" extends "+getContainerShowActionName(doc, table)+"{");	
		increaseIdent();
		ret += emptyline();
		ret += writeString(getExecuteDeclaration());
		increaseIdent();
		ret += writeStatement(ModuleBeanGenerator.getContainerEntryFormName(table)+" form = ("+ModuleBeanGenerator.getContainerEntryFormName(table)+") af");
		ret += writeStatement("String id = form.getOwnerId()");
		ret += writeStatement(doc.getName()+" "+doc.getVariableName());
		ret += writeStatement(doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");
		
		String call = "";
		List<MetaProperty> columns = table.getColumns();
		for (int i =0; i<columns.size(); i++){
		    MetaProperty p = columns.get(i);
		    String getter = "form.get"+StringUtils.capitalize(table.extractSubName(p))+"()";
		    call += getter;
		    if (i<columns.size()-1)
		        call += ", ";
		}
		ret += writeStatement(doc.getVariableName()+"."+DataFacadeGenerator.getContainerEntryAdderName(table)+"("+call+")");
		ret += writeStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		ret += writeStatement("return "+getSuperCall());
		ret += closeBlock();
		ret += closeBlock();
		
		return ret;
	}

	private String generateContainerDeleteEntryAction(MetaModuleSection section, MetaContainerProperty container){		
		String ret = "";
		 
		MetaDocument doc = section.getDocument();

		ret += writeStatement("package "+getPackage(section.getModule()));
		ret += emptyline();
	    
		//write imports...
		ret += getStandardActionImports();
		ret += writeImport(DataFacadeGenerator.getDocumentImport(context, doc));
		ret += emptyline();
		
		ret += writeString("public class "+getContainerDeleteEntryActionName(doc, container)+" extends "+getContainerShowActionName(doc, container)+"{");	
		increaseIdent();
		ret += emptyline();
		ret += writeString(getExecuteDeclaration());
		increaseIdent();
		ret += writeStatement("String id = getStringParameter(req, PARAM_ID)");
		ret += writeStatement("int position = getIntParameter(req, "+quote("pPosition")+")"); 
		ret += writeStatement(doc.getName()+" "+doc.getVariableName());
		ret += writeStatement(doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");
		
		ret += writeStatement(doc.getVariableName()+"."+DataFacadeGenerator.getContainerEntryDeleterName(container)+"(position)");
		ret += writeStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		ret += writeStatement("return "+getSuperCall());
		ret += closeBlock();
		ret += closeBlock();
		
		return ret;
	}

	private String generateContainerMoveEntryAction(MetaModuleSection section, MetaContainerProperty container){
		if (!(container instanceof MetaListProperty)){
			//TODO decomment
			//System.out.println("WARN moveUp only supported by lists, "+container+" is not a list");
			return null;
		}
			
		MetaListProperty sourceProperty = (MetaListProperty)container;
		MetaGenericProperty generic = new MetaGenericListProperty(sourceProperty.getName(), sourceProperty.getContainedProperty());
		
		
		String ret = "";
		 
		MetaDocument doc = section.getDocument();

		ret += writeStatement("package "+getPackage(section.getModule()));
		ret += emptyline();
	    
		//write imports...
		ret += getStandardActionImports();
		ret += writeImport(DataFacadeGenerator.getDocumentImport(context, doc));
		ret += writeImport("net.anotheria.asg.exception.ASGRuntimeException");
		ret += writeImport("java.util.List");
		ret += emptyline();
		
		ret += writeString("public class "+getContainerMoveEntryActionName(doc, container)+" extends "+getContainerShowActionName(doc, container)+"{");	
		increaseIdent();
		ret += emptyline();
		ret += writeString(getExecuteDeclaration());
		increaseIdent();
		ret += writeStatement("String id = getStringParameter(req, PARAM_ID)");
		ret += writeStatement("int position = getIntParameter(req, "+quote("pPosition")+")");
		ret += writeStatement("String direction = getStringParameter(req, "+quote("dir")+")");

		ret += writeStatement(doc.getName()+" "+doc.getVariableName() + " = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");

		ret += writeString("if ("+quote("top")+".equalsIgnoreCase(direction))");
		ret += writeIncreasedStatement("moveTop("+doc.getVariableName()+", position)");
		ret += writeString("if ("+quote("up")+".equalsIgnoreCase(direction))");
		ret += writeIncreasedStatement("moveUp("+doc.getVariableName()+", position)");
		ret += writeString("if ("+quote("down")+".equalsIgnoreCase(direction))");
		ret += writeIncreasedStatement("moveDown("+doc.getVariableName()+", position)");
		ret += writeString("if ("+quote("bottom")+".equalsIgnoreCase(direction))");
		ret += writeIncreasedStatement("moveBottom("+doc.getVariableName()+", position)");
		

		ret += writeStatement("return "+getSuperCall());
		ret += closeBlock();
		ret += emptyline();
		
		String moveMethodParameter = doc.getName()+" "+doc.getVariableName()+", int position";
		
		ret += writeString("private void moveUp("+moveMethodParameter+") throws ASGRuntimeException {");
		increaseIdent();
		ret += writeString("if (position==0) ");
		ret += writeIncreasedStatement("return");
		ret += writeStatement(doc.getVariableName()+"."+DataFacadeGenerator.getContainerEntrySwapperName(container)+"(position, position-1)");
		ret += writeStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		ret += closeBlock();
		ret += emptyline();
		
		ret += writeString("private void moveTop("+moveMethodParameter+") throws ASGRuntimeException {");
		increaseIdent();
		ret += writeStatement(generic.toJavaType()+" targetList = "+doc.getVariableName()+".get"+container.getAccesserName()+"()");
		ret += writeStatement(sourceProperty.getContainedProperty().toJavaType()+" toSwap = targetList.remove(position)");
		ret += writeStatement("targetList.add(0, toSwap)");
		ret += writeStatement(doc.getVariableName()+".set"+container.getAccesserName()+"(targetList)"); 
		ret += writeStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		ret += closeBlock();
		ret += emptyline();

		ret += writeString("private void moveDown("+moveMethodParameter+") throws ASGRuntimeException {");
		increaseIdent();
		ret += writeString("if (position<"+doc.getVariableName()+"."+DataFacadeGenerator.getContainerSizeGetterName(container)+"()-1){");
		increaseIdent();
		
		ret += writeStatement(doc.getVariableName()+"."+DataFacadeGenerator.getContainerEntrySwapperName(container)+"(position, position+1)");
		ret += writeStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		ret += closeBlock();
		ret += closeBlock();
		ret += emptyline();

		ret += writeString("private void moveBottom("+moveMethodParameter+") throws ASGRuntimeException {");
		increaseIdent();
		ret += writeStatement(generic.toJavaType()+" targetList = "+doc.getVariableName()+".get"+container.getAccesserName()+"()");
		ret += writeStatement(sourceProperty.getContainedProperty().toJavaType()+" toSwap = targetList.remove(position)");
		ret += writeStatement("targetList.add(toSwap)");
		ret += writeStatement(doc.getVariableName()+".set"+container.getAccesserName()+"(targetList)"); 
		ret += writeStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		ret += closeBlock();
		ret += emptyline();

		ret += closeBlock();
		
		return ret;
	}

	/*
	private String generateContainerMoveBottomEntryAction(MetaModuleSection section, MetaContainerProperty container){
		if (!(container instanceof MetaListProperty)){
			//System.out.println("WARN moveTop only supported by lists, "+container+" is not a list");
			return null;
		}
		
		MetaListProperty sourceProperty = (MetaListProperty)container;
		MetaGenericProperty generic = new MetaGenericListProperty(sourceProperty.getName(), sourceProperty.getContainedProperty());
		
		String ret = "";
		 
		MetaDocument doc = section.getDocument();
		

		ret += writeStatement("package "+getPackage(section.getModule()));
		ret += emptyline();
	    
		//write imports...
		ret += getStandardActionImports();
		ret += writeImport(DataFacadeGenerator.getDocumentImport(context, doc));
		ret += writeImport("java.util.List");
		ret += emptyline();
		
		ret += writeString("public class "+getContainerMoveBottomEntryActionName(doc, container)+" extends "+getContainerShowActionName(doc, container)+"{");	
		increaseIdent();
		ret += emptyline();
		ret += writeString(getExecuteDeclaration());
		increaseIdent();
		ret += writeStatement("String id = getStringParameter(req, PARAM_ID)");
		ret += writeStatement("int position = getIntParameter(req, "+quote("pPosition")+")");
		ret += writeStatement(doc.getName()+" "+doc.getVariableName());
		ret += writeStatement(doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");

		
		ret += writeStatement(generic.toJavaType()+" targetList = "+doc.getVariableName()+".get"+container.getAccesserName()+"()");
		ret += writeStatement(sourceProperty.getContainedProperty().toJavaType()+" toSwap = targetList.remove(position)");
		ret += writeStatement("targetList.add(toSwap)");
		ret += writeStatement(doc.getVariableName()+".set"+container.getAccesserName()+"(targetList)"); 
		ret += writeStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		ret += writeStatement("return "+getSuperCall());
		ret += closeBlock();
		ret += closeBlock();
		
		return ret;
	}
	*/

	/*
	private String generateContainerMoveDownEntryAction(MetaModuleSection section, MetaContainerProperty container){		
		if (!(container instanceof MetaListProperty)){
			//System.out.println("WARN moveDown only supported by lists, "+container+" is not a list");
			return null;
		}

		String ret = "";
		 
		MetaDocument doc = section.getDocument();

		ret += writeStatement("package "+getPackage(section.getModule()));
		ret += emptyline();
	    
		//write imports...
		ret += getStandardActionImports();
		ret += writeImport(DataFacadeGenerator.getDocumentImport(context, doc));
		ret += emptyline();
		
		ret += writeString("public class "+getContainerMoveDownEntryActionName(doc, container)+" extends "+getContainerShowActionName(doc, container)+"{");	
		increaseIdent();
		ret += emptyline();
		ret += writeString(getExecuteDeclaration());
		increaseIdent();
		ret += writeStatement("String id = getStringParameter(req, PARAM_ID)");
		ret += writeStatement("int position = getIntParameter(req, "+quote("pPosition")+")");
		ret += writeStatement(doc.getName()+" "+doc.getVariableName());
		ret += writeStatement(doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");
		ret += writeString("if (position<"+doc.getVariableName()+"."+DataFacadeGenerator.getContainerSizeGetterName(container)+"()-1){");
		increaseIdent();
		
		ret += writeStatement(doc.getVariableName()+"."+DataFacadeGenerator.getContainerEntrySwapperName(container)+"(position, position+1)");
		ret += writeStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		ret += closeBlock();
		ret += writeStatement("return "+getSuperCall());
		ret += closeBlock();
		ret += closeBlock();
		
		return ret;
	}
	*/
	
	private String generateContainerShowAction(MetaModuleSection section, MetaContainerProperty container){
		if (container instanceof MetaTableProperty)
			return generateTableShowAction(section, (MetaTableProperty)container);

		if (container instanceof MetaListProperty)
			return generateListShowAction(section, (MetaListProperty)container);
			
		throw new RuntimeException("Unsupported container type: "+container.getClass().getName());
	}

	private String generateListShowAction(MetaModuleSection section, MetaListProperty list){
		StringBuilder ret = new StringBuilder(5000);


		MetaDocument doc = section.getDocument();

		appendStatement(ret, "package "+getPackage(section.getModule()));
		emptyline(ret);
	    
		//write imports...
		appendImport(ret, "java.util.List");
		appendImport(ret, "java.util.ArrayList");
		appendStandardActionImports(ret);
		appendImport(ret, DataFacadeGenerator.getDocumentImport(context, doc));
		appendImport(ret, ModuleBeanGenerator.getContainerEntryFormImport(doc, list));
		if (list.getContainedProperty().isLinked()){
			appendImport(ret, ModuleBeanGenerator.getContainerQuickAddFormImport(doc, list));
			MetaLink link = (MetaLink)list.getContainedProperty();
			
			String tDocName = link.getTargetDocumentName(); 
			MetaModule targetModule = link.getLinkTarget().indexOf('.')== -1 ?
					doc.getParentModule() : GeneratorDataRegistry.getInstance().getModule(link.getTargetModuleName());
			MetaDocument targetDocument = targetModule.getDocumentByName(tDocName);
			appendImport(ret, DataFacadeGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), targetDocument));
			appendImport(ret, DataFacadeGenerator.getSortTypeImport(targetDocument));
			appendImport(ret, "net.anotheria.anodoc.data.NoSuchDocumentException");

		}
		emptyline(ret);

		appendString(ret, "public class "+getContainerShowActionName(doc, list)+" extends "+getBaseActionName(section)+" {");
		increaseIdent();
		emptyline(ret);
		appendString(ret, getExecuteDeclaration());
		increaseIdent();
		appendStatement(ret, "String id = getStringParameter(req, PARAM_ID)");
		appendStatement(ret, doc.getName()+" "+doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id);");
		emptyline(ret);
		
		appendStatement(ret, ModuleBeanGenerator.getContainerEntryFormName(list)+" form = new "+ModuleBeanGenerator.getContainerEntryFormName(list)+"() ");
		appendStatement(ret, "form.setPosition(-1)"); //hmm?
		appendStatement(ret, "form.setOwnerId("+doc.getVariableName()+".getId())");	
		appendStatement(ret, "addBeanToRequest(req, "+quote(StrutsConfigGenerator.getContainerEntryFormName(doc, list))+", form)");
		emptyline(ret);
		
		if (list.getContainedProperty().isLinked()){
			appendStatement(ret, ModuleBeanGenerator.getContainerQuickAddFormName(list)+" quickAddForm = new "+ModuleBeanGenerator.getContainerQuickAddFormName(list)+"() ");
			appendStatement(ret, "quickAddForm.setOwnerId("+doc.getVariableName()+".getId())");	
			appendStatement(ret, "addBeanToRequest(req, "+quote(StrutsConfigGenerator.getContainerQuickAddFormName(doc, list))+", quickAddForm)");
			emptyline(ret);
		}

		if (list.getContainedProperty().isLinked()){
			//generate list collection
			MetaLink link = (MetaLink)list.getContainedProperty();
			emptyline(ret);
			appendString(ret, "//link "+link.getName()+" to "+link.getLinkTarget());
			MetaModule targetModule = link.getLinkTarget().indexOf('.')== -1 ?
					doc.getParentModule() : GeneratorDataRegistry.getInstance().getModule(link.getTargetModuleName());
			MetaDocument targetDocument = targetModule.getDocumentByName(link.getTargetDocumentName());
			String listName = targetDocument.getMultiple().toLowerCase();
			String sortType = "new "+DataFacadeGenerator.getSortTypeName(targetDocument);
			sortType += "("+DataFacadeGenerator.getSortTypeName(targetDocument)+".SORT_BY_NAME)";
			appendStatement(ret, "List<"+targetDocument.getName()+"> "+listName+" = "+getServiceGetterCall(targetModule)+".get"+targetDocument.getMultiple()+"("+sortType+")");
			appendStatement(ret, "List<net.anotheria.webutils.bean.LabelValueBean> "+listName+"Values = new ArrayList<net.anotheria.webutils.bean.LabelValueBean>("+listName+".size())");
			appendString(ret, "for (int i=0; i<"+listName+".size(); i++){");
			increaseIdent();
			appendStatement(ret, DataFacadeGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), targetDocument)+" "+targetDocument.getTemporaryVariableName()+" = ("+DataFacadeGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), targetDocument)+") "+listName+".get(i)");
			appendStatement(ret, "net.anotheria.webutils.bean.LabelValueBean bean = new net.anotheria.webutils.bean.LabelValueBean("+targetDocument.getTemporaryVariableName()+".getId(), "+targetDocument.getTemporaryVariableName()+".getName()+\" [\"+"+targetDocument.getTemporaryVariableName()+".getId()+\"]\" )");
			appendStatement(ret, listName+"Values.add(bean)");
			closeBlock(ret);
			appendStatement(ret, "addBeanToRequest(req, "+quote(listName+"Values")+", "+listName+"Values"+")");
			appendStatement(ret, "form."+list.getContainedProperty().toBeanSetter()+"Collection("+listName+"Values"+")");
			
		}
		
		appendString(ret, "// generate list ...");
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
		
		
		appendStatement(ret, "int size = "+doc.getVariableName()+"."+DataFacadeGenerator.getContainerSizeGetterName(list)+"()");
		appendStatement(ret, "List<"+ModuleBeanGenerator.getContainerEntryFormName(list)+"> beans = new ArrayList<"+ModuleBeanGenerator.getContainerEntryFormName(list)+">(size)");
		//appendStatement(ret, "List elements = "+doc.getVariableName()+".get"+list.getAccesserName()+"()");
		
		
		appendString(ret, "for (int i=0; i<size; i++){");
		increaseIdent();
		appendStatement(ret, list.getContainedProperty().toJavaType() + " value = "+doc.getVariableName()+"."+DataFacadeGenerator.getListElementGetterName(list)+"(i)");
		appendStatement(ret, ModuleBeanGenerator.getContainerEntryFormName(list)+" bean = new "+ModuleBeanGenerator.getContainerEntryFormName(list)+"()");
		appendStatement(ret, "bean.setOwnerId("+doc.getVariableName()+".getId())");
		appendStatement(ret, "bean.setPosition(i)");
		appendStatement(ret, "bean."+list.getContainedProperty().toSetter()+"(value)");
		if (list.getContainedProperty().isLinked()){
			appendString(ret, "try{");
			increaseIdent();
			appendStatement(ret, targetDocument.getName()+" "+targetDocument.getTemporaryVariableName()+" = "+getServiceGetterCall(targetModule)+".get"+targetDocument.getName()+"(value)");
			//THIS is the hack
			appendStatement(ret, "bean.setDescription("+targetDocument.getTemporaryVariableName()+".getName())");
			decreaseIdent();
			appendString(ret, "}catch(NoSuchDocumentException e){");
			appendIncreasedStatement(ret, "bean.setDescription(\"*** DELETED ***\")");
			appendString(ret, "}");
		}
		appendStatement(ret, "beans.add(bean)");
		closeBlock(ret);		
		appendStatement(ret, "addBeanToRequest(req, "+quote("elements")+", beans)");
//*/		
		appendStatement(ret, "return mapping.findForward(", quote("success"), ")");
		closeBlock(ret);
		closeBlock(ret);
		
		return ret.toString();
	}	

	private String generateTableShowAction(MetaModuleSection section, MetaTableProperty table){
		String ret = "";
		
		MetaDocument doc = section.getDocument();

		ret += writeStatement("package "+getPackage(section.getModule()));
		ret += emptyline();
	    
		//write imports...
		ret += writeImport("java.util.List");
		ret += writeImport("java.util.ArrayList");
		ret += getStandardActionImports();
		ret += writeImport(DataFacadeGenerator.getDocumentImport(context, doc));
		ret += writeImport(ModuleBeanGenerator.getContainerEntryFormImport(doc, table));
		ret += emptyline();

		ret += writeString("public class "+getContainerShowActionName(doc, table)+" extends "+getBaseActionName(section)+" {");
		increaseIdent();
		ret += emptyline();
		ret += writeString(getExecuteDeclaration());
		increaseIdent();
		ret += writeStatement("String id = getStringParameter(req, PARAM_ID)");
		ret += writeStatement(doc.getName()+" "+doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id);");
		ret += emptyline();
		
		ret += writeStatement(ModuleBeanGenerator.getContainerEntryFormName(table)+" form = new "+ModuleBeanGenerator.getContainerEntryFormName(table)+"() ");
		ret += writeStatement("form.setPosition(\"-1\")");
		ret += writeStatement("form.setOwnerId("+doc.getVariableName()+".getId())");	
		ret += writeStatement("addBeanToRequest(req, "+quote(StrutsConfigGenerator.getContainerEntryFormName(doc, table))+", form)");
		ret += emptyline();
		
		ret += writeString("// generate table...");
		ret += writeStatement("List beans = new ArrayList()");
		ret += writeStatement("List rows  = "+doc.getVariableName()+"."+DataFacadeGenerator.getTableGetterName(table)+"()");
		ret += writeString("for (int i=0; i<rows.size(); i++){");
		increaseIdent();
		ret += writeStatement("List row = (List) rows.get(i)");
		ret += writeStatement(ModuleBeanGenerator.getContainerEntryFormName(table)+" bean = new "+ModuleBeanGenerator.getContainerEntryFormName(table)+"()");
		ret += writeStatement("bean.setOwnerId("+doc.getVariableName()+".getId())");
		ret += writeStatement("bean.setPosition(\"\"+i)");
		List<MetaProperty> columns = table.getColumns();
		for (int i=0; i<columns.size(); i++){
			MetaProperty p = columns.get(i);
			String setter = "bean.set"+StringUtils.capitalize(table.extractSubName(p));
			setter += "((String)row.get("+i+"))";
			ret += writeStatement(setter);
		}
		ret += writeStatement("beans.add(bean)");
		ret += closeBlock();		
		ret += writeStatement("addBeanToRequest(req, "+quote("rows")+", beans)");
		
		ret += writeStatement("return mapping.findForward("+quote("success")+")");
		ret += closeBlock();
		ret += closeBlock();
		
		return ret;
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
