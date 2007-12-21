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
		
		files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getBaseActionName(section), generateBaseAction(section)));
		files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getShowActionName(section), generateShowAction(section)));
		files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getSearchActionName(section), generateSearchAction(section)));
		files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getDeleteActionName(section), generateDeleteAction(section)));
		files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getDuplicateActionName(section), generateDuplicateAction(section)));
		files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getVersionInfoActionName(section), generateVersionInfoAction(section)));
		
		try{
			if (section.getDialogs().size()>0){
			
			//works only if the section has a dialog.
			files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getUpdateActionName(section), generateUpdateAction(section)));
			files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getEditActionName(section), generateEditAction(section)));
			files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getNewActionName(section), generateNewAction(section)));
			MetaDocument doc = section.getDocument();
			
			if (GeneratorDataRegistry.hasLanguageCopyMethods(doc)){
				files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getLanguageCopyActionName(section), generateLanguageCopyAction(section)));
			}
			
			for (int p=0; p<doc.getProperties().size(); p++){
				MetaProperty pp = (MetaProperty)doc.getProperties().get(p);
				//System.out.println("checking "+pp+" "+(pp instanceof MetaContainerProperty));
				if (pp instanceof MetaContainerProperty){
				//	System.out.println("generating "+pp);
					files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getContainerShowActionName(doc, (MetaContainerProperty)pp), generateContainerShowAction(section, (MetaContainerProperty)pp)));
					files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getContainerAddEntryActionName(doc, (MetaContainerProperty)pp), generateContainerAddRowAction(section, (MetaContainerProperty)pp)));
					files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getContainerQuickAddActionName(doc, (MetaContainerProperty)pp), generateContainerQuickAddAction(section, (MetaContainerProperty)pp)));
					files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getContainerDeleteEntryActionName(doc, (MetaContainerProperty)pp), generateContainerDeleteEntryAction(section, (MetaContainerProperty)pp)));
					files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getContainerMoveUpEntryActionName(doc, (MetaContainerProperty)pp), generateContainerMoveUpEntryAction(section, (MetaContainerProperty)pp)));
					files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getContainerMoveDownEntryActionName(doc, (MetaContainerProperty)pp), generateContainerMoveDownEntryAction(section, (MetaContainerProperty)pp)));
					files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getContainerMoveTopEntryActionName(doc, (MetaContainerProperty)pp), generateContainerMoveTopEntryAction(section, (MetaContainerProperty)pp)));
					files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getContainerMoveBottomEntryActionName(doc, (MetaContainerProperty)pp), generateContainerMoveBottomEntryAction(section, (MetaContainerProperty)pp)));
				}
			}
		}

		}catch(Exception ignored){
			ignored.printStackTrace();
		}
		
		MetaDocument targetDocument = section.getDocument();
		List<MetaProperty> links = targetDocument.getLinks();
		if (links.size()>0){
			files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getShowQueryActionName(section), generateShowQueryAction(section)));
			files.add(new FileEntry(FileEntry.package2path(getPackage(section.getModule())), getExecuteQueryActionName(section), generateExecuteQueryAction(section)));
		}
		
			
		
				
		
		
		
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
	    String ret = "";
	    MetaDocument doc = section.getDocument();
		List<MetaViewElement> elements = section.getElements();
	    
	    boolean containsComparable = section.containsComparable();
	    ret += writeStatement("package "+getPackage(section.getModule()));
	    ret += emptyline();
	    
	    //write imports...
	    ret += writeImport("java.util.List");
	    ret += writeImport("java.util.ArrayList");
	    ret += writeImport("net.anotheria.asg.util.decorators.IAttributeDecorator");
	    ret += writeImport("net.anotheria.asg.util.filter.DocumentFilter");
	    ret += writeImport("net.anotheria.util.NumberUtils");
	    ret += getStandardActionImports();
	    ret += writeImport(DataFacadeGenerator.getDocumentImport(context, doc));
	    ret += writeImport(ModuleBeanGenerator.getListItemBeanImport(context, doc));
		ret += emptyline();
		if (containsComparable){
			ret += writeImport(ModuleBeanGenerator.getListItemBeanSortTypeImport(context, doc));
			ret += writeImport("net.anotheria.util.sorter.Sorter");
			ret += writeImport("net.anotheria.util.sorter.QuickSorter");
			ret += emptyline();
		}
		
		ret += writeImport("net.anotheria.util.slicer.Slicer");
		ret += writeImport("net.anotheria.util.slicer.Slice");
		ret += writeImport("net.anotheria.util.slicer.Segment");
		ret += writeImport("net.anotheria.asg.util.bean.PagingLink");
		ret += emptyline();
		
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
						ret += writeImport(EnumerationGenerator.getUtilsImport(type));
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
		
	    ret += writeString("public class "+getShowActionName(section)+" extends "+getBaseActionName(section)+" {");
	    increaseIdent();
	    ret += emptyline();
	    
	    //generate session attributes constants
	    ret += writeStatement("public static final String SA_SORT_TYPE = SA_SORT_TYPE_PREFIX+"+quote(doc.getName()));
	    ret += writeStatement("public static final String SA_FILTER = SA_FILTER_PREFIX+"+quote(doc.getName()));
	    ret += writeStatement("private static final List<String> ITEMS_ON_PAGE_SELECTOR = java.util.Arrays.asList(new String[]{\"5\",\"10\",\"20\",\"25\",\"50\",\"100\",\"500\",\"1000\"})");
	    
	    boolean containsDecorators = neededDecorators.size() >0;
	    
		if (containsComparable){
			ret += writeStatement("private Sorter<"+ModuleBeanGenerator.getListItemBeanName(doc)+"> sorter");
			ret += emptyline();
		}
		
		if (containsDecorators){
			for (int i=0; i<elements.size();i++){
				MetaViewElement element = (MetaViewElement)elements.get(i);
				if (element.getDecorator()!=null){
					ret += writeStatement("private IAttributeDecorator "+getDecoratorVariableName(element));
				}
			}
			ret += emptyline();
		}
		
		if (section.getFilters().size()>0){
			for (MetaFilter f : section.getFilters()){
				ret += writeStatement("private DocumentFilter "+getFilterVariableName(f));
			}
			ret += emptyline();
		}
			
		
		ret += writeString("public "+getShowActionName(section)+"(){");
		increaseIdent();
		ret += writeStatement("super()");
		if (containsComparable)
			ret += writeStatement("sorter = new QuickSorter<"+ModuleBeanGenerator.getListItemBeanName(doc)+">()");
		if (containsDecorators){
			ret += writeString("try{ ");
			increaseIdent();
			for (int i=0; i<elements.size();i++){
				MetaViewElement element = elements.get(i);
				if (element.getDecorator()!=null){
					ret += writeStatement(getDecoratorVariableName(element)+" = (IAttributeDecorator)Class.forName("+quote(element.getDecorator().getClassName())+").newInstance()");
				}
			}
			decreaseIdent();
			ret += writeString("} catch(Exception e){");
			ret += writeIncreasedStatement("log.fatal(\"Couldn't instantiate decorator:\", e)");
			ret += writeString("}");
		}
	    //add filters
		if (section.getFilters().size()>0){
			ret += writeString("try{ ");
			increaseIdent();
			for (MetaFilter f : section.getFilters()){
				ret += writeStatement(getFilterVariableName(f)+" = (DocumentFilter) Class.forName("+quote(f.getClassName())+").newInstance()");
			}
			decreaseIdent();
			ret += writeString("} catch(Exception e){");
			ret += writeIncreasedStatement("log.fatal(\"Couldn't instantiate filter:\", e)");
			ret += writeString("}");
		}
		ret += closeBlock();
		

	    ret += writeString(getExecuteDeclaration());
	    increaseIdent();
	    
	    if (section.getFilters().size()>0){
	    	for (int i=0 ; i<section.getFilters().size(); i++){
	    		MetaFilter f = section.getFilters().get(i);
	    		String filterParameterName = "filterParameter"+i;
		    	//hacky, only one filter at time allowed. otherwise, we must submit the filter name.
		    	ret += writeStatement("String filterParameter"+i+" = "+quote(""));
		    	ret += writeString("try{ ");
		    	ret += writeIncreasedStatement(filterParameterName+" = getStringParameter(req, "+quote("pFilter"+i)+")");
		    	ret += writeIncreasedStatement("addBeanToSession(req, SA_FILTER+"+quote(i)+", "+filterParameterName+")");
		    	ret += writeString("}catch(Exception ignored){");
		    	increaseIdent();
		    	ret += writeCommentLine("no filter parameter given, tring to check in the session.");
		    	ret += writeStatement(filterParameterName+" = (String)getBeanFromSession(req, SA_FILTER+"+quote(i)+")");
		    	ret += writeString("if ("+filterParameterName+"==null)");
		    	ret += writeIncreasedStatement(filterParameterName+" = "+quote(""));
		    	ret += closeBlock();
		    	ret += writeStatement("req.setAttribute("+quote("currentFilterParameter"+i)+", "+filterParameterName+")");
		    	ret += emptyline();
	    	}
	    }
	    
	    //check if its sortable.
		if (containsComparable){
			String sortType = ModuleBeanGenerator.getListItemBeanSortTypeName(doc);
			ret += writeStatement("int sortMethod = "+sortType+".SORT_BY_DEFAULT");
			ret += writeStatement("boolean sortOrder = "+sortType+".ASC");
			ret += writeStatement("boolean sortParamSet = false");
			ret += emptyline();
			ret += writeString("try{");
			ret += writeIncreasedStatement("sortMethod = getIntParameter(req, PARAM_SORT_TYPE)");
			ret += writeIncreasedStatement("sortParamSet = true");
			ret += writeString("}catch(Exception ignored){}");
			ret += emptyline();	    
			ret += writeString("try{");
			ret += writeIncreasedStatement("String sortMethodName = getStringParameter(req, PARAM_SORT_TYPE_NAME)");
			ret += writeIncreasedStatement("sortMethod = "+sortType+".name2method(sortMethodName)");
			ret += writeIncreasedStatement("sortParamSet = true");
			ret += writeString("}catch(Exception ignored){}");
			ret += emptyline();	    
			ret += writeString("try{");
			increaseIdent();
			ret += writeString("sortOrder = getStringParameter(req, PARAM_SORT_ORDER).equals("+quote(ViewConstants.VALUE_SORT_ORDER_ASC)+") ? ");
			ret += writeIncreasedStatement(""+sortType+".ASC : "+sortType+".DESC");
			decreaseIdent();
			ret += writeString("}catch(Exception ignored){}");
			ret += emptyline();
			ret += writeStatement(ModuleBeanGenerator.getListItemBeanSortTypeName(doc)+" sortType = null");
			ret += writeString("if (sortParamSet){");
			increaseIdent();
			ret += writeStatement("sortType = new "+ModuleBeanGenerator.getListItemBeanSortTypeName(doc)+"(sortMethod, sortOrder)");
			ret += writeStatement("addBeanToSession(req, SA_SORT_TYPE, sortType)");
			decreaseIdent();
			ret += writeString("}else{");
			increaseIdent();
			ret += writeStatement("sortType = ("+ModuleBeanGenerator.getListItemBeanSortTypeName(doc)+")getBeanFromSession(req, SA_SORT_TYPE)");
			ret += writeString("if (sortType==null)");
			ret += writeIncreasedStatement("sortType = new "+ModuleBeanGenerator.getListItemBeanSortTypeName(doc)+"(sortMethod, sortOrder)");
			ret += closeBlock();
			ret += writeStatement("req.setAttribute("+quote("currentSortCode")+", sortType.getMethodAndOrderCode())");
			ret += emptyline();
		}
	    
	    String listName = doc.getMultiple().toLowerCase();
	    if (section.getFilters().size()>0){
		    String unfilteredListName = "_unfiltered_"+listName;
		    //change this if more than one filter can be triggered at once.
		    ret += writeStatement("List<"+doc.getName()+"> "+unfilteredListName+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getMultiple()+"()");
		    ret += writeStatement("List<"+doc.getName()+"> "+listName+" = new ArrayList<"+doc.getName()+">()");
		    ret += writeString("for (int i=0; i<"+unfilteredListName+".size(); i++){");
		    increaseIdent();
		    ret += writeStatement("boolean mayPass = true");
		    for (int i=0; i<section.getFilters().size(); i++){
			    MetaFilter activeFilter = section.getFilters().get(i);
			    String filterVarName = getFilterVariableName(activeFilter);
			    ret += writeStatement("mayPass = mayPass && ("+filterVarName+".mayPass("+unfilteredListName+".get(i), "+quote(activeFilter.getFieldName())+", filterParameter"+i+"))");
		    	
		    }
		    ret += writeString("if (mayPass)");
		    ret += writeIncreasedStatement(listName+".add("+unfilteredListName+".get(i))");
		    ret += closeBlock();
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
	    if (containsComparable){
	    	ret += writeStatement("beans = sorter.sort(beans, sortType)");
	    }

	    //paging start
	    ret += writeCommentLine("paging");
	    ret += writeStatement("int pageNumber = 1"); 
	    ret += writeString("try{");
	    ret += writeIncreasedStatement("pageNumber = Integer.parseInt(req.getParameter("+quote("pageNumber")+"))");
	    ret += writeString("}catch(Exception ignored){}");
	    ret += writeStatement("Integer lastItemsOnPage = (Integer)req.getSession().getAttribute(\"currentItemsOnPage\")");
	    ret += writeStatement("int itemsOnPage = lastItemsOnPage == null ? 20 : lastItemsOnPage"); 
	    ret += writeString("try{");
	    ret += writeIncreasedStatement("itemsOnPage = Integer.parseInt(req.getParameter("+quote("itemsOnPage")+"))");
	    ret += writeString("}catch(Exception ignored){}");
	    ret += writeStatement("Slice<"+ModuleBeanGenerator.getListItemBeanName(doc)+"> slice = Slicer.slice(new Segment(pageNumber, itemsOnPage), beans)");
	    ret += writeStatement("beans = slice.getSliceData()");
	    ret += emptyline();
	    
	    ret += writeCommentLine("prepare paging links");
	    ret += writeStatement("ArrayList<PagingLink> pagingLinks = new ArrayList<PagingLink>()");
		ret += writeStatement("pagingLinks.add(new PagingLink(slice.isFirstSlice() ? null : \"1\", \"|<<\"))");
		ret += writeStatement("pagingLinks.add(new PagingLink(slice.hasPrevSlice() ? \"\"+(slice.getCurrentSlice()-1) : null, \"<<\"))");
		
		ret += writeString("for (int i=1; i<slice.getCurrentSlice(); i++){");
		increaseIdent();
		ret += writeString("if (slice.getCurrentSlice()-i<=7)");
		ret += writeIncreasedStatement("pagingLinks.add(new PagingLink(\"\"+i,\"\"+i))");
		ret += closeBlock();
		
		ret += writeStatement("pagingLinks.add(new PagingLink(null, \"Page \"+(slice.getCurrentSlice()+\" of \"+slice.getTotalNumberOfSlices())))");
		
		ret += writeString("for (int i=slice.getCurrentSlice()+1; i<=slice.getTotalNumberOfSlices(); i++){");
		increaseIdent();
		ret += writeString("if (i-slice.getCurrentSlice()<=7)");
		ret += writeIncreasedStatement("pagingLinks.add(new PagingLink(\"\"+i,\"\"+i))");
		ret += closeBlock();
		
		
		ret += writeStatement("pagingLinks.add(new PagingLink(slice.hasNextSlice() ?  \"\"+(slice.getCurrentSlice()+1) : null, \">>\"))");
		ret += writeStatement("pagingLinks.add(new PagingLink(slice.isLastPage() ? null : \"\"+slice.getTotalNumberOfSlices(), \">>|\"))");
	    ret += writeCommentLine(" paging links end");
	    
	    ret += writeStatement("req.setAttribute("+quote("paginglinks")+", pagingLinks)");
	    ret += writeStatement("req.setAttribute("+quote("currentpage")+", pageNumber)");
	    ret += writeStatement("req.setAttribute("+quote("currentItemsOnPage")+", itemsOnPage)");
	    ret += writeStatement("req.getSession().setAttribute("+quote("currentItemsOnPage")+", itemsOnPage)");
	    ret += writeStatement("req.setAttribute("+quote("PagingSelector")+", ITEMS_ON_PAGE_SELECTOR)");
	    ret += emptyline();
	    //paging end
	    
	    
	    
	    ret += writeStatement("addBeanToRequest(req, "+quote(listName)+", beans)");
	    
	    //add filters
	    for (MetaFilter f : section.getFilters()){
	    	ret += writeStatement("addBeanToRequest(req, "+quote(getFilterVariableName(f))+", "+getFilterVariableName(f)+".getTriggerer(\"\"))");
	    }
	    
	    ret += writeStatement("return mapping.findForward(\"success\")");
	    ret += closeBlock();
	    ret += emptyline();
	    
	    
	    // BEAN creation function
	    ret += writeString("protected "+ModuleBeanGenerator.getListItemBeanName(doc)+" "+getMakeBeanFunctionName(ModuleBeanGenerator.getListItemBeanName(doc))+"("+doc.getName()+" "+doc.getVariableName()+"){");
	    increaseIdent();
	    ret += writeStatement(ModuleBeanGenerator.getListItemBeanName(doc)+" bean = new "+ModuleBeanGenerator.getListItemBeanName(doc)+"()");
	    //set the properties.
	    //this is a hack...
	    ret += writeStatement("bean.setPlainId("+doc.getVariableName()+".getId())");

		elements = createMultilingualList(elements, doc, context);
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			if (element instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement) element;
				String lang = null;
				if (field instanceof MultilingualFieldElement)
					lang = ((MultilingualFieldElement)field).getLanguage();
				MetaProperty p = doc.getField(field.getName());
				if (p instanceof MetaContainerProperty){
					String value = "";
					value = doc.getVariableName()+"."+DataFacadeGenerator.getContainerSizeGetterName((MetaContainerProperty)p, lang)+"()";
					if (element.getDecorator()!=null){
						//if decorated, save original value for sorting and replace with decorated value
						MetaProperty tmp ;
						if (element instanceof MultilingualFieldElement)
							tmp = new MetaProperty(p.getName("ForSorting", ((MultilingualFieldElement)element).getLanguage()), p.getType());
						else
							tmp = new MetaProperty(p.getName()+"ForSorting", p.getType());
							
						ret += writeStatement("bean."+tmp.toBeanSetter(lang)+"("+value+")");
						MetaDecorator d = element.getDecorator();
						value = getDecoratorVariableName(element)+".decorate("+doc.getVariableName()+", "+quote(p.getName())+", "+quote(d.getRule())+")";
					}
					ret += writeStatement("bean."+p.toBeanSetter(lang)+"("+value+")");
				}else{
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

							ret += writeStatement("bean."+tmp.toBeanSetter()+"("+value+")");
							MetaDecorator d = element.getDecorator();
							value = getDecoratorVariableName(element)+".decorate("+doc.getVariableName()+", "+quote(p.getName()+(lang==null?"":"_"+lang))+", "+quote(d.getRule())+")";
						}
					}
					ret += writeStatement("bean."+p.toBeanSetter(lang)+"("+value+")");
				}
			}
		}
		
		ret += writeStatement("bean.setDocumentLastUpdateTimestamp(NumberUtils.makeISO8601TimestampString("+doc.getVariableName()+".getLastUpdateTimestamp()))");
	    
	    ret += writeStatement("return bean");
	    ret += closeBlock();
	    ret += emptyline();
	    
	    
	    ret += closeBlock();
	    return ret;
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
			String targetModuleName = lt.substring(0,dotIndex);
			String targetDocumentName = lt.substring(dotIndex+1);
			MetaModule mod = GeneratorDataRegistry.getInstance().getModule(targetModuleName);
			MetaDocument targetDocument = mod.getDocumentByName(targetDocumentName);
			
			ret += writeImport(DataFacadeGenerator.getDocumentImport(context, targetDocument));
			
			linkTargets.add(lt);
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
		String ret = "";
		MetaDocument doc = section.getDocument();
		ret += writeStatement("package "+getPackage(section.getModule()));
		ret += emptyline();

		//write imports...
		ret += getStandardActionImports();
	    ret += writeImport(DataFacadeGenerator.getDocumentImport(context, doc));
	    ret += writeImport("net.anotheria.util.NumberUtils");
	    ret += emptyline();

		ret += writeString("public class "+getVersionInfoActionName(section)+" extends "+getBaseActionName(section)+" {");
		increaseIdent();
		ret += emptyline();
	    
		ret += writeString(getExecuteDeclaration());
		increaseIdent();
	
		ret += writeStatement("String id = getStringParameter(req, PARAM_ID)");
		ret += writeStatement(doc.getName()+" "+doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id);");
		ret += writeStatement("long timestamp = "+doc.getVariableName()+".getLastUpdateTimestamp()");
		ret += writeStatement("String lastUpdateDate = NumberUtils.makeDigitalDateStringLong(timestamp)");
		ret += writeStatement("lastUpdateDate += \" \"+NumberUtils.makeTimeString(timestamp)");

		try{
			doc.getField("name");
			ret += writeStatement("req.setAttribute("+quote("documentName")+", "+doc.getVariableName()+".getName())");
		}catch(Exception ignored){
			ret += writeStatement("req.setAttribute("+quote("documentName")+", \"Id:\"+"+doc.getVariableName()+".getId())");
		}
		ret += writeStatement("req.setAttribute("+quote("documentType")+", "+doc.getVariableName()+".getClass())");
		ret += writeStatement("req.setAttribute("+quote("lastUpdate")+", lastUpdateDate)");
		
		ret += writeStatement("return mapping.findForward("+quote("success")+")");
	    
		ret += closeBlock();
		ret += closeBlock();
		return ret;
	}
	
	private String generateUpdateAction(MetaModuleSection section){
		String ret = "";
		MetaDocument doc = section.getDocument();
		MetaDialog dialog = section.getDialogs().get(0);
		ret += writeStatement("package "+getPackage(section.getModule()));
		ret += emptyline();

		//write imports...
		ret += getStandardActionImports();
		ret += writeImport(ModuleBeanGenerator.getDialogBeanImport(context, dialog, doc));
	    ret += writeImport(DataFacadeGenerator.getDocumentImport(context, doc));
	    ret += writeImport(DataFacadeGenerator.getDocumentFactoryImport(context, doc));
	    ret += emptyline();
	    
		List<MetaViewElement> elements = createMultilingualList(dialog.getElements(), doc, context);
		for (int i=0; i<elements.size(); i++){
			MetaViewElement elem = elements.get(i);
			if (elem instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)elem;
				MetaProperty p = doc.getField(field.getName());
				if (p.getType().equals("image")){
					ret += writeImport("net.anotheria.webutils.filehandling.actions.FileStorage");
					ret += writeImport("net.anotheria.webutils.filehandling.beans.TemporaryFileHolder");
					break;
				}
			}
		}

	    
		ret += writeString("public class "+getUpdateActionName(section)+" extends "+getBaseActionName(section)+" {");
		increaseIdent();
		ret += emptyline();
	    
		ret += writeString(getExecuteDeclaration());
		increaseIdent();
	
		ret += writeStatement(ModuleBeanGenerator.getDialogBeanName(dialog, doc)+" form = ("+ModuleBeanGenerator.getDialogBeanName(dialog, doc)+") af");
		//check if we have a form submission at all.
		ret += writeString("if (!form.isFormSubmittedFlag())");
		ret += writeIncreasedStatement("throw new RuntimeException(\"Request broken!\")");
		//if update, then first get the target object.
		ret += writeStatement("boolean create = false");
		ret += writeStatement(doc.getName()+" "+doc.getVariableName()+" = null");
		ret += writeString("if (form.getId()!=null && form.getId().length()>0){");	
		ret += writeIncreasedString(doc.getVariableName()+" = ("+doc.getName()+")"+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(form.getId()).clone();");
		ret += writeString("}else{");
		increaseIdent();
		ret += writeString(doc.getVariableName()+" = "+DataFacadeGenerator.getDocumentFactoryName(doc)+".create"+doc.getName()+"();");
		ret += writeString("create = true;");
		ret += closeBlock();
		ret += emptyline();
		
		ret += writeStatement("String nextAction = req.getParameter("+quote("nextAction")+")");
		ret += writeString("if (nextAction == null || nextAction.length() == 0)");
		ret += writeIncreasedStatement("nextAction = \"close\"");
		ret += emptyline();
		
		//set fields
		for (int i=0; i<elements.size(); i++){
			MetaViewElement elem = elements.get(i);
			if (elem instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)elem;
				String lang = getElementLanguage(field);
				//System.out.println("checking field:"+field);
				if (field.isReadonly()){
					ret += writeString("//skipped "+field.getName()+" because it's readonly.");
				}else{
					MetaProperty p = doc.getField(field.getName());
					//handle images.
					if (p.getType().equals("image")){
						//will work only with one image.
						ret += writeString("//handle image");
						ret += writeStatement("TemporaryFileHolder holder = FileStorage.getTemporaryFile(req)");
						ret += writeString("if (holder!=null && holder.getData()!=null){");
						increaseIdent();
						ret += writeStatement("FileStorage.storeFilePermanently(req, holder.getFileName())");
						ret += writeStatement(doc.getVariableName()+"."+p.toSetter()+"(holder.getFileName())");
						ret += writeStatement("FileStorage.removeTemporaryFile(req)");
						ret += closeBlock();
						continue;
					}
					if (! (p instanceof MetaContainerProperty)){
						String propertyCopy = "";
						propertyCopy += doc.getVariableName()+"."+p.toSetter(lang)+"(";
						propertyCopy += "form."+p.toBeanGetter(lang)+"())";
						ret += writeStatement(propertyCopy);
					}else{
						ret += writeString("// skipped container "+p.getName());
					}
					
				}
			}
		}
		
		ret += emptyline();
		ret += writeStatement(doc.getName()+" updatedCopy = null");
		
		ret += writeString("if (create){");
		//ret += writeIncreasedStatement("System.out.println(\"creating\")");
		ret += writeIncreasedStatement("updatedCopy = "+getServiceGetterCall(section.getModule())+".create"+doc.getName()+"("+doc.getVariableName()+")");
		ret += writeString("}else{");
		ret += writeIncreasedStatement("updatedCopy = "+getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		//ret += writeIncreasedStatement("System.out.println(\"updating\")");
		ret += writeString("}");
		ret += writeString("if (nextAction.equalsIgnoreCase("+quote("stay")+"))");
	    ret += writeIncreasedStatement("res.sendRedirect("+getEditActionRedirect(doc)+"+"+quote("&pId=")+"+updatedCopy.getId())");
		ret += writeString("else");
	    ret += writeIncreasedStatement("res.sendRedirect("+getShowActionRedirect(doc)+")");
	    ret += writeStatement("return null");
		ret += closeBlock();
		ret += emptyline();
	    
		ret += closeBlock();
		return ret;
	}

	private String generateLanguageCopyAction(MetaModuleSection section){
		String ret = "";
		MetaDocument doc = section.getDocument();
		ret += writeStatement("package "+getPackage(section.getModule()));
		ret += emptyline();

		//write imports...
		ret += getStandardActionImports();
	    ret += writeImport(DataFacadeGenerator.getDocumentImport(context, doc));
	    ret += emptyline();
	    
	    ret += writeComment("This class copies multilingual contents from one language to another in a given document");
		ret += writeString("public class "+getLanguageCopyActionName(section)+" extends "+getBaseActionName(section)+" {");
		increaseIdent();
		ret += emptyline();
	    
		ret += writeString(getExecuteDeclaration());
		increaseIdent();
		
		ret += writeStatement("String sourceLanguage = req.getParameter("+quote("pSrcLang")+")");
		ret += writeString("if (sourceLanguage==null || sourceLanguage.length()==0)");
		ret += writeIncreasedStatement("throw new RuntimeException("+quote("No source language")+")");
		ret += emptyline();

		ret += writeStatement("String destLanguage = req.getParameter("+quote("pDestLang")+")");
		ret += writeString("if (destLanguage==null || destLanguage.length()==0)");
		ret += writeIncreasedStatement("throw new RuntimeException("+quote("No destination language")+")");
		ret += emptyline();

		ret += writeStatement("String id = getStringParameter(req, PARAM_ID)");
		ret += writeStatement(doc.getName()+" "+doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");
		ret += writeStatement(doc.getVariableName()+"."+DataFacadeGenerator.getCopyMethodName()+"(sourceLanguage, destLanguage)");
		ret += writeStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
	    ret += writeStatement("res.sendRedirect("+getEditActionRedirect(doc)+"+"+quote("&pId=")+"+id)");
		
	    ret += writeStatement("return null");
		ret += closeBlock(); //end doExecute
		ret += closeBlock(); // end class
		return ret;
	}

	private String generateEditAction(MetaModuleSection section){
		String ret = "";
		MetaDocument doc = section.getDocument();
		MetaDialog dialog = section.getDialogs().get(0);
		List<MetaViewElement> elements = createMultilingualList(dialog.getElements(), doc, context);
		
		EnumerationPropertyGenerator enumProGenerator = new EnumerationPropertyGenerator(doc);

		ret += writeStatement("package "+getPackage(section.getModule()));
		ret += emptyline();

		//write imports...
		ret += getStandardActionImports();
		ret += writeImport(ModuleBeanGenerator.getDialogBeanImport(context, dialog, doc));
		ret += writeImport(DataFacadeGenerator.getDocumentImport(context, doc));
		ret += writeImport("net.anotheria.asg.util.helper.cmsview.CMSViewHelperUtil");
		ret += emptyline();
		
		boolean listImported = false;
		
		//check if we have to import list.
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			if (element instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)element;
				MetaProperty p = doc.getField(field.getName());
				if (p.isLinked() || p instanceof MetaEnumerationProperty){
					ret += writeImport("java.util.List");
					ret += writeImport("java.util.ArrayList");
					ret += writeImport("net.anotheria.webutils.bean.LabelValueBean");
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
					    ret += writeImport(customImport);
					    customImports.add(customImport);
					}
				}
			}
		}
		
	    List<DirectLink> backlinks = GeneratorDataRegistry.getInstance().findLinksToDocument(doc);
	    if (backlinks.size()>0){
	    	ret += writeImport("net.anotheria.anodoc.query2.QueryProperty");
	    	ret += writeImport("net.anotheria.asg.util.bean.LinkToMeBean");
	    	if (!listImported){
	    		listImported=true;
				ret += writeImport("java.util.List");
				ret += writeImport("java.util.ArrayList");
	    	}
	    	for (DirectLink l : backlinks){
	    		String imp = DataFacadeGenerator.getDocumentImport(context, l.getDocument());
	    		if (customImports.indexOf(imp)==-1){
	    			ret += writeImport(imp);
	    			customImports.add(imp);
	    		}
	    	}
	    }
		
		ret += emptyline();

	    
		ret += writeString("public class "+getEditActionName(section)+" extends "+getShowActionName(section)+" {");
		increaseIdent();
		ret += emptyline();
	    
		ret += writeString(getExecuteDeclaration());
		increaseIdent();
	
		ret += writeStatement("String id = getStringParameter(req, PARAM_ID)");
		ret += writeStatement(ModuleBeanGenerator.getDialogBeanName(dialog, doc)+" form = new "+ModuleBeanGenerator.getDialogBeanName(dialog, doc)+"() ");	

		ret += writeStatement(doc.getName()+" "+doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id);");
		
		//set field
		for (int i=0; i<elements.size(); i++){
			MetaViewElement elem = elements.get(i);
			//System.out.println("checking elem:"+elem);
			if (elem instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)elem;
				MetaProperty p = doc.getField(field.getName());
				if (p instanceof MetaContainerProperty){
					ret += writeString("// "+p.getName()+" is a table, storing size only");
					String lang = getElementLanguage(elem);
					ret += writeStatement("form."+p.toBeanSetter(lang)+"("+doc.getVariableName()+"."+DataFacadeGenerator.getContainerSizeGetterName((MetaContainerProperty)p, lang)+"())");
				}else{
					String lang = getElementLanguage(elem);
					String propertyCopy = "";
					propertyCopy += "form."+p.toBeanSetter(lang)+"(";
					propertyCopy += doc.getVariableName()+"."+p.toGetter(lang)+"())";
					ret += writeStatement(propertyCopy);
				}
			}
		}
		
		ret +=emptyline();
		
		Set<String> linkTargets = new HashSet<String>();
		
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			if (element instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)element;
				MetaProperty p = doc.getField(field.getName());
				if (p.isLinked()){
					MetaLink link = (MetaLink)p;
					MetaModule targetModule = GeneratorDataRegistry.getInstance().getModule(StringUtils.tokenize(link.getLinkTarget(), '.')[0]);
					if (targetModule == null){
						throw new RuntimeException("Can't resolve link: "+p+" in document "+doc.getName()+" and dialog "+dialog.getName());
					}
					String tDocName = StringUtils.tokenize(link.getLinkTarget(), '.')[1]; 
					MetaDocument targetDocument = targetModule.getDocumentByName(tDocName);
					String listName = targetDocument.getMultiple().toLowerCase();
					ret += emptyline();

					if (linkTargets.contains(link.getLinkTarget())){
						ret += writeString("//reusing collection for "+link.getName()+" to "+link.getLinkTarget()+".");
					}else{
					
						ret += writeString("//link "+link.getName()+" to "+link.getLinkTarget());
						ret += writeString("//to lazy to include List in the imports.");
						ret += writeStatement("List<"+DataFacadeGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), targetDocument)+"> "+listName+" = "+getServiceGetterCall(targetModule)+".get"+targetDocument.getMultiple()+"()");
						ret += writeStatement("List<LabelValueBean> "+listName+"Values = new ArrayList<LabelValueBean>("+listName+".size()+1)");
						ret += writeStatement(listName+"Values.add(new LabelValueBean("+quote("")+", \"-----\"))");
						ret += writeString("for ("+(DataFacadeGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), targetDocument))+" "+targetDocument.getVariableName()+" : "+listName+"){");
						increaseIdent();
						ret += writeStatement("LabelValueBean bean = new LabelValueBean("+targetDocument.getVariableName()+".getId(), "+targetDocument.getVariableName()+".getName() )");
						ret += writeStatement(listName+"Values.add(bean)");
						ret += closeBlock();

					}

					String lang = getElementLanguage(element);
					ret += writeStatement("form."+p.toBeanSetter()+"Collection"+(lang==null ? "":lang)+"("+listName+"Values"+")");
					
					ret += writeString("try{");
					increaseIdent();
					String getter = getServiceGetterCall(targetModule)+".get"+targetDocument.getName()+"("+doc.getVariableName()+"."+p.toGetter()+"()).getName()";
					ret += writeStatement("form."+p.toBeanSetter()+"CurrentValue"+(lang==null ? "":lang)+"("+getter+")");
					decreaseIdent();
					ret +=  writeString("}catch(Exception e){");
					ret += writeIncreasedStatement("form."+p.toBeanSetter()+"CurrentValue"+(lang==null ? "":lang)+"("+quote("none")+")");
					ret += writeString("}");
					linkTargets.add(link.getLinkTarget());
					
				}
				
				if (p instanceof MetaEnumerationProperty){
				    ret += enumProGenerator.generateEnumerationPropertyHandling((MetaEnumerationProperty)p, true);
				}
			}
		}
		
		
		ret += writeStatement("addBeanToRequest(req, "+quote(StrutsConfigGenerator.getDialogFormName(dialog, doc))+" , form)");
		ret += writeStatement("addBeanToRequest(req, "+quote("save.label.prefix")+", "+quote("Update")+")");
		
		//add field descriptions ...
		ret += writeStatement("String fieldDescription = null");
		for (MetaProperty p : doc.getProperties()){
			ret += writeStatement("fieldDescription = CMSViewHelperUtil.getFieldExplanation("+quote(doc.getParentModule().getName()+"."+doc.getName())+ ", "+doc.getVariableName()+", "+quote(p.getName())+")");
			ret += writeString("if (fieldDescription!=null && fieldDescription.length()>0)");
			ret += writeIncreasedStatement("req.setAttribute("+quote("description."+p.getName())+", fieldDescription)");
		}
	
	    if (backlinks.size()>0){
	    	ret += emptyline();
	    	ret += writeCommentLine("Generating back link handling...");
	    	ret += writeStatement("List<LinkToMeBean> linksToMe = findLinksToCurrentDocument("+doc.getVariableName()+".getId())");
	    	ret += writeString("if (linksToMe.size()>0)");
	    	ret += writeIncreasedStatement("req.setAttribute("+quote("linksToMe")+", linksToMe)");
	    }

		
		ret += writeStatement("return mapping.findForward(\"success\")");
		ret += closeBlock();
		ret += emptyline();
		
	    //backlinks
		if (backlinks.size()>0){
			ret += writeString("private List<LinkToMeBean> findLinksToCurrentDocument(String documentId){");
			increaseIdent();
			ret += writeStatement("List<LinkToMeBean> ret = new ArrayList<LinkToMeBean>()");
			for (DirectLink l : backlinks){
				if (l.getProperty().isMultilingual()){
					for (String lang : context.getLanguages()){
						ret += writeStatement("ret.addAll(findLinkToCurrentDocumentIn"+l.getModule().getName()+l.getDocument().getName()+StringUtils.capitalize(l.getProperty().getName(lang))+"(documentId))");
					}
				}else{
					ret += writeStatement("ret.addAll(findLinkToCurrentDocumentIn"+l.getModule().getName()+l.getDocument().getName()+StringUtils.capitalize(l.getProperty().getName())+"(documentId))");	
				}
			}
			ret += writeStatement("return ret");
			ret += closeBlock();
			
			for (DirectLink l : backlinks){
				if (l.getProperty().isMultilingual()){
					for (String lang : context.getLanguages()){
						ret += writeString("private List<LinkToMeBean> findLinkToCurrentDocumentIn"+l.getModule().getName()+l.getDocument().getName()+StringUtils.capitalize(l.getProperty().getName(lang))+"(String documentId){");
						increaseIdent();
						ret += writeStatement("List<LinkToMeBean> ret = new ArrayList<LinkToMeBean>()");
						ret += writeStatement("QueryProperty p = new QueryProperty("+l.getDocument().getName()+"."+l.getProperty().toNameConstant(lang)+", documentId)");
						//ret += writeStatement("List<"+l.getDocument().getName()+"> list = "+getServiceGetterCall(l.getModule())+".get"+l.getDocument().getMultiple()+"ByProperty(p)");
						ret += writeCommentLine("temporarly - replacy with query property");
						ret += writeStatement("List<"+l.getDocument().getName()+"> list = "+getServiceGetterCall(l.getModule())+".get"+l.getDocument().getMultiple()+"ByProperty(p.getName(), p.getValue())");
						ret += writeString("for ("+l.getDocument().getName() +" doc : list ){");
						increaseIdent();
						ret += writeStatement("ret.add(new LinkToMeBean(doc, "+quote(l.getProperty().getName())+"))");
						ret += closeBlock();
						ret += writeStatement("return ret");
						ret += closeBlock();
					}
				}else{
					ret += writeString("private List<LinkToMeBean> findLinkToCurrentDocumentIn"+l.getModule().getName()+l.getDocument().getName()+StringUtils.capitalize(l.getProperty().getName())+"(String documentId){");
					increaseIdent();
					ret += writeStatement("List<LinkToMeBean> ret = new ArrayList<LinkToMeBean>()");
					ret += writeStatement("QueryProperty p = new QueryProperty("+l.getDocument().getName()+"."+l.getProperty().toNameConstant()+", documentId)");
					//ret += writeStatement("List<"+l.getDocument().getName()+"> list = "+getServiceGetterCall(l.getModule())+".get"+l.getDocument().getMultiple()+"ByProperty(p)");
					ret += writeCommentLine("temporarly - replacy with query property");
					ret += writeStatement("List<"+l.getDocument().getName()+"> list = "+getServiceGetterCall(l.getModule())+".get"+l.getDocument().getMultiple()+"ByProperty(p.getName(), p.getValue())");
					ret += writeString("for ("+l.getDocument().getName() +" doc : list ){");
					increaseIdent();
					ret += writeStatement("ret.add(new LinkToMeBean(doc, "+quote(l.getProperty().getName())+"))");
					ret += closeBlock();
					ret += writeStatement("return ret");
					ret += closeBlock();
				}
			}
		}
		
	    
		ret += closeBlock();
		return ret;
	}

	private String generateDeleteAction(MetaModuleSection section){
	    String ret = "";
	    MetaDocument doc = section.getDocument();
	    ret += writeStatement("package "+getPackage(section.getModule()));
	    ret += emptyline();

	    //write imports...
	    ret += getStandardActionImports();
	    
	    ret += writeString("public class "+getDeleteActionName(section)+" extends "+getBaseActionName(section)+" {");
	    increaseIdent();
	    ret += emptyline();
	    
	    ret += writeString(getExecuteDeclaration());
	    increaseIdent();
	    ret += writeStatement("String id = getStringParameter(req, PARAM_ID)");
	    ret += writeStatement(getServiceGetterCall(section.getModule())+".delete"+doc.getName()+"(id)");
	    ret += writeStatement("res.sendRedirect("+getShowActionRedirect(doc)+")");
	    ret += writeStatement("return null");
	    ret += closeBlock();
	    ret += emptyline();
	    
	    ret += closeBlock();
	    return ret;
	}

	private String generateDuplicateAction(MetaModuleSection section){
		String ret = "";
		MetaDocument doc = section.getDocument();
		ret += writeStatement("package "+getPackage(section.getModule()));
		ret += emptyline();

		//write imports...
		ret += getStandardActionImports();
		ret += writeImport(DataFacadeGenerator.getDocumentImport(context, doc));
		ret += writeImport(DataFacadeGenerator.getDocumentFactoryImport(context, doc));
	    
		ret += writeString("public class "+getDuplicateActionName(section)+" extends "+getBaseActionName(section)+" {");
		increaseIdent();
		ret += emptyline();
	    
		ret += writeString(getExecuteDeclaration());
		increaseIdent();
		ret += writeStatement("String id = getStringParameter(req, PARAM_ID)");
		ret += writeStatement(doc.getName()+" "+doc.getVariableName()+"Src = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id);");
		ret += writeStatement(doc.getName()+" "+doc.getVariableName()+"Dest = "+DataFacadeGenerator.getDocumentFactoryName(doc)+".create"+doc.getName()+"("+doc.getVariableName()+"Src)");

		ret += writeStatement(getServiceGetterCall(section.getModule())+".create"+doc.getName()+"("+doc.getVariableName()+"Dest"+")");
	    ret += writeStatement("res.sendRedirect("+getShowActionRedirect(doc)+")");
	    ret += writeStatement("return null");
		ret += closeBlock();
		ret += emptyline();
	    
		ret += closeBlock();
		return ret;
	}

	private String generateNewAction(MetaModuleSection section){
		String ret = "";
		MetaDocument doc = section.getDocument();
		MetaDialog dialog = section.getDialogs().get(0);
		//List<MetaViewElement> elements = dialog.getElements();
		List<MetaViewElement> elements = createMultilingualList(dialog.getElements(), doc, context);
		
		EnumerationPropertyGenerator enumPropGen = new EnumerationPropertyGenerator(doc);
		
		ret += writeStatement("package "+getPackage(section.getModule()));
		ret += emptyline();

		//write imports...
		ret += getStandardActionImports();
		ret += writeImport(ModuleBeanGenerator.getDialogBeanImport(context, dialog, doc));
		ret += emptyline();

		//check if we have to import list.
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = elements.get(i);
			if (element instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)element;
				MetaProperty p = doc.getField(field.getName());
				if (p.isLinked() || p instanceof MetaEnumerationProperty){
					ret += writeImport("java.util.List");
					ret += writeImport("java.util.ArrayList");
					ret += writeImport("net.anotheria.webutils.bean.LabelValueBean");
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
						ret += writeImport(EnumerationGenerator.getUtilsImport(type));
						//System.out.println("Adding enumeration import: "+mep.getType()+", "+mep+", "+mep.getName());
						importedEnumerations.put(mep.getName(), mep);
					}
				}
			}
		}
		
		ret += emptyline();
	    
		ret += writeString("public class "+getNewActionName(section)+" extends "+getShowActionName(section)+" {");
		increaseIdent();
		ret += emptyline();
	    
		ret += writeString(getExecuteDeclaration());
		increaseIdent();
	
		ret += writeStatement(ModuleBeanGenerator.getDialogBeanName(dialog, doc)+" form = new "+ModuleBeanGenerator.getDialogBeanName(dialog, doc)+"() ");	
		ret += writeStatement("form.setId("+quote("")+")");
		
		Set<String> linkTargets = new HashSet<String>();
		
		for (int i=0; i<elements.size(); i++){
			MetaViewElement element = (MetaViewElement)elements.get(i);
			if (element instanceof MetaFieldElement){
				MetaFieldElement field = (MetaFieldElement)element;
				MetaProperty p = doc.getField(field.getName());
				if (p.isLinked()){
					MetaLink link = (MetaLink)p;

					MetaModule targetModule = GeneratorDataRegistry.getInstance().getModule(StringUtils.tokenize(link.getLinkTarget(), '.')[0]);
					String tDocName = StringUtils.tokenize(link.getLinkTarget(), '.')[1]; 
					MetaDocument targetDocument = targetModule.getDocumentByName(tDocName);
					String listName = targetDocument.getMultiple().toLowerCase();
					ret += emptyline();
					
					if (linkTargets.contains(link.getLinkTarget())){
						ret += writeString("//link "+link.getName()+" to "+link.getLinkTarget()+" reuses collection.");
					}else{
						ret += writeString("//link "+link.getName()+" to "+link.getLinkTarget());
						ret += writeStatement("List<"+DataFacadeGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), targetDocument)+"> "+listName+" = "+getServiceGetterCall(targetModule)+".get"+targetDocument.getMultiple()+"()");
						ret += writeStatement("List<LabelValueBean> "+listName+"Values = new ArrayList<LabelValueBean>("+listName+".size()+1)");
						ret += writeStatement(listName+"Values.add(new LabelValueBean("+quote("")+", \"-----\"))");
						ret += writeString("for ("+(DataFacadeGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), targetDocument))+" "+targetDocument.getVariableName()+" : "+listName+"){");
						increaseIdent();
						
						ret += writeStatement("LabelValueBean bean = new LabelValueBean("+targetDocument.getVariableName()+".getId(), "+targetDocument.getVariableName()+".getName() )");
						ret += writeStatement(listName+"Values.add(bean)");
						ret += closeBlock();
					}
					
					String lang = getElementLanguage(element);
					ret += writeStatement("form."+p.toBeanSetter()+"Collection"+(lang==null ? "" : lang)+"("+listName+"Values"+")");
					linkTargets.add(link.getLinkTarget());
				}//...end if (p.isLinked())

				if (p instanceof MetaEnumerationProperty){
				    ret += enumPropGen.generateEnumerationPropertyHandling((MetaEnumerationProperty)p, false);
				}
				
			}
		}

		ret += emptyline();
		ret += writeStatement("addBeanToRequest(req, "+quote(StrutsConfigGenerator.getDialogFormName(dialog, doc))+" , form)");
		ret += writeStatement("addBeanToRequest(req, "+quote("save.label.prefix")+", "+quote("Create")+")");

		ret += writeStatement("return mapping.findForward(\"success\")");
		ret += closeBlock();
		ret += emptyline();
	    
		ret += closeBlock();
		return ret;
	}

	private String generateBaseAction(MetaModuleSection section){
	    String ret = "";

	    //MetaDocument doc = section.getDocument();
//	    MetaModule mod = section.getModule();
	    
	    ret += writeStatement("package "+getPackage(section.getModule()));
	    ret += emptyline();
	    ret += writeImport(context.getPackageName(MetaModule.SHARED)+".action."+BaseViewActionGenerator.getViewActionName(view));
	    ret += emptyline();
	    
	    ret += writeString("public abstract class "+getBaseActionName(section)+" extends "+BaseViewActionGenerator.getViewActionName(view)+" {");
	    increaseIdent();
	    ret += emptyline();

	    //generate getTitle
	    ret += writeString("public String getTitle(){");
	    increaseIdent();
	    ret += writeStatement("return "+quote(section.getTitle()));
	    ret += closeBlock();
	    ret += emptyline();
	    
	    
	    ret += closeBlock();
	    return ret;
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
		if (container instanceof MetaListProperty)
			return generateListQuickAddAction(section, (MetaListProperty)container);
		return "";
		//throw new RuntimeException("Unsupported container type: "+container.getClass().getName());
	}

	private String generateListAddRowAction(MetaModuleSection section, MetaListProperty list){
		String ret = "";

		MetaDocument doc = section.getDocument();

		ret += writeStatement("package "+getPackage(section.getModule()));
		ret += emptyline();
	    
		//write imports...
		ret += getStandardActionImports();
		ret += writeImport(DataFacadeGenerator.getDocumentImport(context, doc));
		ret += writeImport(ModuleBeanGenerator.getContainerEntryFormImport(doc, list));
		ret += emptyline();
		
		ret += writeString("public class "+getContainerAddEntryActionName(doc, list)+" extends "+getContainerShowActionName(doc, list)+"{");	
		increaseIdent();
		ret += emptyline();
		ret += writeString(getExecuteDeclaration());
		increaseIdent();
		ret += writeStatement(ModuleBeanGenerator.getContainerEntryFormName(list)+" form = ("+ModuleBeanGenerator.getContainerEntryFormName(list)+") af");
		ret += writeStatement("String id = form.getOwnerId()");
		ret += writeStatement(doc.getName()+" "+doc.getVariableName());
		ret += writeStatement(doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");
		
		String call = "";
		MetaProperty p = list.getContainedProperty();
		String getter = "form."+p.toBeanGetter()+"()";
		call += getter;
		ret += writeStatement(doc.getVariableName()+"."+DataFacadeGenerator.getContainerEntryAdderName(list)+"("+call+")");
		ret += writeStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		ret += writeStatement("return "+getSuperCall());
		ret += closeBlock();
		ret += closeBlock();
		
		return ret;


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

	private String generateContainerMoveUpEntryAction(MetaModuleSection section, MetaContainerProperty container){
		if (!(container instanceof MetaListProperty)){
			//TODO decomment
			//System.out.println("WARN moveUp only supported by lists, "+container+" is not a list");
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
		
		ret += writeString("public class "+getContainerMoveUpEntryActionName(doc, container)+" extends "+getContainerShowActionName(doc, container)+"{");	
		increaseIdent();
		ret += emptyline();
		ret += writeString(getExecuteDeclaration());
		increaseIdent();
		ret += writeStatement("String id = getStringParameter(req, PARAM_ID)");
		ret += writeStatement("int position = getIntParameter(req, "+quote("pPosition")+")");
		ret += writeString("if (position!=0){");
		increaseIdent();
		ret += writeStatement(doc.getName()+" "+doc.getVariableName());
		ret += writeStatement(doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");
		
		ret += writeStatement(doc.getVariableName()+"."+DataFacadeGenerator.getContainerEntrySwapperName(container)+"(position, position-1)");
		ret += writeStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		ret += closeBlock();
		ret += writeStatement("return "+getSuperCall());
		ret += closeBlock();
		ret += closeBlock();
		
		return ret;
	}

	/**
	 * Generates a move to the top of the interal list action for a container. Currently only MetaListProperty is container which supports 
	 * it.
	 * @param section
	 * @param container
	 * @return
	 */
	private String generateContainerMoveTopEntryAction(MetaModuleSection section, MetaContainerProperty container){
		if (!(container instanceof MetaListProperty)){
			//TODO re-comment
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
		
		ret += writeString("public class "+getContainerMoveTopEntryActionName(doc, container)+" extends "+getContainerShowActionName(doc, container)+"{");	
		increaseIdent();
		ret += emptyline();
		ret += writeString(getExecuteDeclaration());
		increaseIdent();
		ret += writeStatement("String id = getStringParameter(req, PARAM_ID)");
		ret += writeStatement("int position = getIntParameter(req, "+quote("pPosition")+")");
		ret += writeString("if (position!=0){");
		increaseIdent();
		ret += writeStatement(doc.getName()+" "+doc.getVariableName());
		ret += writeStatement(doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id)");

		ret += writeStatement(generic.toJavaType()+" targetList = "+doc.getVariableName()+".get"+container.getAccesserName()+"()");
		ret += writeStatement(sourceProperty.getContainedProperty().toJavaType()+" toSwap = targetList.remove(position)");
		ret += writeStatement("targetList.add(0, toSwap)");
		ret += writeStatement(doc.getVariableName()+".set"+container.getAccesserName()+"(targetList)"); 
		ret += writeStatement(getServiceGetterCall(section.getModule())+".update"+doc.getName()+"("+doc.getVariableName()+")");
		ret += closeBlock();
		ret += writeStatement("return "+getSuperCall());
		ret += closeBlock();
		ret += closeBlock();
		
		return ret;
	}

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
	
	private String generateContainerShowAction(MetaModuleSection section, MetaContainerProperty container){
		if (container instanceof MetaTableProperty)
			return generateTableShowAction(section, (MetaTableProperty)container);

		if (container instanceof MetaListProperty)
			return generateListShowAction(section, (MetaListProperty)container);
			
		throw new RuntimeException("Unsupported container type: "+container.getClass().getName());
	}

	private String generateListShowAction(MetaModuleSection section, MetaListProperty list){
		String ret = "";


		MetaDocument doc = section.getDocument();

		ret += writeStatement("package "+getPackage(section.getModule()));
		ret += emptyline();
	    
		//write imports...
		ret += writeImport("java.util.List");
		ret += writeImport("java.util.ArrayList");
		ret += getStandardActionImports();
		ret += writeImport(DataFacadeGenerator.getDocumentImport(context, doc));
		ret += writeImport(ModuleBeanGenerator.getContainerEntryFormImport(doc, list));
		if (list.getContainedProperty().isLinked()){
			ret += writeImport(ModuleBeanGenerator.getContainerQuickAddFormImport(doc, list));
			MetaLink link = (MetaLink)list.getContainedProperty();
			String tDocName = StringUtils.tokenize(link.getLinkTarget(), '.')[1]; 
			MetaModule targetModule = GeneratorDataRegistry.getInstance().getModule(StringUtils.tokenize(link.getLinkTarget(), '.')[0]);
			MetaDocument targetDocument = targetModule.getDocumentByName(tDocName);
			ret += writeImport(DataFacadeGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), targetDocument));
			ret += writeImport(DataFacadeGenerator.getSortTypeImport(targetDocument));
			ret += writeImport("net.anotheria.anodoc.data.NoSuchDocumentException");

		}
		ret += emptyline();

		ret += writeString("public class "+getContainerShowActionName(doc, list)+" extends "+getBaseActionName(section)+" {");
		increaseIdent();
		ret += emptyline();
		ret += writeString(getExecuteDeclaration());
		increaseIdent();
		ret += writeStatement("String id = getStringParameter(req, PARAM_ID)");
		ret += writeStatement(doc.getName()+" "+doc.getVariableName()+" = "+getServiceGetterCall(section.getModule())+".get"+doc.getName()+"(id);");
		ret += emptyline();
		
		ret += writeStatement(ModuleBeanGenerator.getContainerEntryFormName(list)+" form = new "+ModuleBeanGenerator.getContainerEntryFormName(list)+"() ");
		ret += writeStatement("form.setPosition(-1)"); //hmm?
		ret += writeStatement("form.setOwnerId("+doc.getVariableName()+".getId())");	
		ret += writeStatement("addBeanToRequest(req, "+quote(StrutsConfigGenerator.getContainerEntryFormName(doc, list))+", form)");
		ret += emptyline();
		
		if (list.getContainedProperty().isLinked()){
			ret += writeStatement(ModuleBeanGenerator.getContainerQuickAddFormName(list)+" quickAddForm = new "+ModuleBeanGenerator.getContainerQuickAddFormName(list)+"() ");
			ret += writeStatement("quickAddForm.setOwnerId("+doc.getVariableName()+".getId())");	
			ret += writeStatement("addBeanToRequest(req, "+quote(StrutsConfigGenerator.getContainerQuickAddFormName(doc, list))+", quickAddForm)");
			ret += emptyline();
		}

		if (list.getContainedProperty().isLinked()){
			//generate list collection
			MetaLink link = (MetaLink)list.getContainedProperty();
			ret += emptyline();
			ret += writeString("//link "+link.getName()+" to "+link.getLinkTarget());
			MetaModule targetModule = GeneratorDataRegistry.getInstance().getModule(StringUtils.tokenize(link.getLinkTarget(), '.')[0]);
			String tDocName = StringUtils.tokenize(link.getLinkTarget(), '.')[1]; 
			MetaDocument targetDocument = targetModule.getDocumentByName(tDocName);
			String listName = targetDocument.getMultiple().toLowerCase();
			String sortType = "new "+DataFacadeGenerator.getSortTypeName(targetDocument);
			sortType += "("+DataFacadeGenerator.getSortTypeName(targetDocument)+".SORT_BY_NAME)";
			ret += writeStatement("List<"+targetDocument.getName()+"> "+listName+" = "+getServiceGetterCall(targetModule)+".get"+targetDocument.getMultiple()+"("+sortType+")");
			ret += writeStatement("List<net.anotheria.webutils.bean.LabelValueBean> "+listName+"Values = new ArrayList<net.anotheria.webutils.bean.LabelValueBean>("+listName+".size())");
			ret += writeString("for (int i=0; i<"+listName+".size(); i++){");
			increaseIdent();
			ret += writeStatement(DataFacadeGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), targetDocument)+" "+targetDocument.getTemporaryVariableName()+" = ("+DataFacadeGenerator.getDocumentImport(GeneratorDataRegistry.getInstance().getContext(), targetDocument)+") "+listName+".get(i)");
			ret += writeStatement("net.anotheria.webutils.bean.LabelValueBean bean = new net.anotheria.webutils.bean.LabelValueBean("+targetDocument.getTemporaryVariableName()+".getId(), "+targetDocument.getTemporaryVariableName()+".getName()+\" [\"+"+targetDocument.getTemporaryVariableName()+".getId()+\"]\" )");
			ret += writeStatement(listName+"Values.add(bean)");
			ret += closeBlock();
			ret += writeStatement("addBeanToRequest(req, "+quote(listName+"Values")+", "+listName+"Values"+")");
			ret += writeStatement("form."+list.getContainedProperty().toBeanSetter()+"Collection("+listName+"Values"+")");
			
		}
		
		ret += writeString("// generate list ...");
		MetaModule targetModule = null;
		MetaDocument targetDocument = null;
		
		//ok this is a hack, but its a fast hack to display names for links
		if (list.getContainedProperty().isLinked()){
			//generate list collection
			MetaLink link = (MetaLink)list.getContainedProperty();
			targetModule = GeneratorDataRegistry.getInstance().getModule(StringUtils.tokenize(link.getLinkTarget(), '.')[0]);
			String tDocName = StringUtils.tokenize(link.getLinkTarget(), '.')[1]; 
			targetDocument = targetModule.getDocumentByName(tDocName);
		}		
		
		
		ret += writeStatement("int size = "+doc.getVariableName()+"."+DataFacadeGenerator.getContainerSizeGetterName(list)+"()");
		ret += writeStatement("List<"+ModuleBeanGenerator.getContainerEntryFormName(list)+"> beans = new ArrayList<"+ModuleBeanGenerator.getContainerEntryFormName(list)+">(size)");
		//ret += writeStatement("List elements = "+doc.getVariableName()+".get"+list.getAccesserName()+"()");
		
		
		ret += writeString("for (int i=0; i<size; i++){");
		increaseIdent();
		ret += writeStatement(list.getContainedProperty().toJavaType() + " value = "+doc.getVariableName()+"."+DataFacadeGenerator.getListElementGetterName(list)+"(i)");
		ret += writeStatement(ModuleBeanGenerator.getContainerEntryFormName(list)+" bean = new "+ModuleBeanGenerator.getContainerEntryFormName(list)+"()");
		ret += writeStatement("bean.setOwnerId("+doc.getVariableName()+".getId())");
		ret += writeStatement("bean.setPosition(i)");
		ret += writeStatement("bean."+list.getContainedProperty().toSetter()+"(value)");
		if (list.getContainedProperty().isLinked()){
			ret += writeString("try{");
			increaseIdent();
			ret += writeStatement(targetDocument.getName()+" "+targetDocument.getTemporaryVariableName()+" = "+getServiceGetterCall(targetModule)+".get"+targetDocument.getName()+"(value)");
			//THIS is the hack
			ret += writeStatement("bean.setDescription("+targetDocument.getTemporaryVariableName()+".getName())");
			decreaseIdent();
			ret += writeString("}catch(NoSuchDocumentException e){");
			ret += writeIncreasedStatement("bean.setDescription(\"*** DELETED ***\")");
			ret += writeString("}");
		}
		ret += writeStatement("beans.add(bean)");
		ret += closeBlock();		
		ret += writeStatement("addBeanToRequest(req, "+quote("elements")+", beans)");
//*/		
		ret += writeStatement("return mapping.findForward("+quote("success")+")");
		ret += closeBlock();
		ret += closeBlock();
		
		return ret;
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
