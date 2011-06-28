package net.anotheria.asg.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaLink;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.types.meta.DataType;
import net.anotheria.asg.generator.util.DirectLink;
import net.anotheria.asg.generator.view.meta.MetaDecorator;
import net.anotheria.asg.generator.view.meta.MetaFilter;
import net.anotheria.asg.generator.view.meta.MetaModuleSection;
import net.anotheria.asg.generator.view.meta.MetaSection;
import net.anotheria.asg.generator.view.meta.MetaValidator;
import net.anotheria.asg.generator.view.meta.MetaView;

/**
 * Used by the generator to store the parsed xml data during the generation.
 * @author lrosenberg
 */
public final class GeneratorDataRegistry {

	/**
	 * Instance of GeneratorDataRegistry.
	 */
	private static GeneratorDataRegistry instance  = new GeneratorDataRegistry();
	
	/**
	 * The storage for the parsed modules.
	 */
	private Map<String,MetaModule> modules;
	/**
	 * The storage for views.
	 */
	private Map<String,MetaView> views;
	/**
	 * The context.
	 */
	private Context context;
	/**
	 * Parsed data types.
	 */
	private Map<String,DataType> types;
	/**
	 * Parsed decorators.
	 */
	private Map<String,MetaDecorator> decorators;
	/**
	 * Parsed filters.
	 */
	private Map<String, MetaFilter> filters;
	/**
	 * Parsed validators. 
	 */
	private Map<String, MetaValidator> validators;
	
	/**
	 * Default generation options. Used if no other options are specified.
	 */
	private GenerationOptions defaultOptions = createDefaultGenerationOptions();
	/**
	 * Current generation options.
	 */
	private GenerationOptions options;
	/**
	 * Creates a new GeneratorDataRegistry.
	 */
	private GeneratorDataRegistry(){
		modules = new HashMap<String,MetaModule>();
		views = new HashMap<String, MetaView>();
		types   = new HashMap<String,DataType>();
		decorators = new HashMap<String,MetaDecorator>();
		filters = new HashMap<String, MetaFilter>();
		validators = new HashMap<String, MetaValidator>();
		options = defaultOptions;
	}
	
	/**
	 * Returns the singleton instance of the GeneratorDataRegistry.
	 * @return instance of the GeneratorDataRegistry
	 */
	public static GeneratorDataRegistry getInstance(){
		return instance;
	}
	
	/**
	 * Adds a new parsed module to the storage.
	 * @param m the module to add.
	 */
	public void addModule(MetaModule m){
		modules.put(m.getName(), m);
	}
	
	/**
	 * Adds some modules to the storage.
	 * @param aModules a list of modules to add.
	 */
	public void addModules(List<MetaModule> aModules){
		for (MetaModule m: aModules)
			addModule(m);
	}
	/**
	 * Adds a view to the storage.
	 * @param v the view to add.
	 */
	public void addView(MetaView v){
		views.put(v.getName(), v);
	}
	
	/**
	 * Adds some view to the storage.
	 * @param aViews the views to add.
	 */
	public void addViews(List<MetaView> aViews){
		for (MetaView v: aViews)
			addView(v);
	}
	
	/**
	 * 
	 * @param link link for resolving document
	 * @return resolved document
	 */
	public MetaDocument resolveLink(String link){
		int dotIndex = link.indexOf('.');
		String targetModuleName = link.substring(0,dotIndex);
		String targetDocumentName = link.substring(dotIndex+1);
		MetaModule mod = getModule(targetModuleName);
		MetaDocument targetDocument = mod.getDocumentByName(targetDocumentName);
		return targetDocument;
	}
	
	public List<DirectLink> findLinksToDocument(MetaDocument target){
		ArrayList<DirectLink> ret = new ArrayList<DirectLink>();
		for (MetaModule module : modules.values()){
			for (MetaDocument document : module.getDocuments()){
				List<MetaLink> links = document.getLinksToDocument(target);
				for (MetaLink l : links){
					ret.add(new DirectLink(module, document, l));
				}
			}
		}
		return ret;
	}
	
	public MetaSection findViewSection(MetaDocument document){
		for (MetaView module : views.values()){
			for (MetaSection section : module.getSections())
				if(section instanceof MetaModuleSection && document.equals(((MetaModuleSection)section).getDocument()))
					return section;
		}
		return null;
	}

	/**
	 * Returns the module from the storage.
	 * @param name the name of the module.
	 * @return module
	 */
	public MetaModule getModule(String name){
		return modules.get(name);
	}

	/**
	 * Returns module from storage.
	 * @return modules
	 */
	public Collection<MetaModule> getModules(){
		return modules.values();
	}

	/**
	 * Return view from storage.
	 * @param name name of view
	 * @return view
	 */
	public MetaView getView(String name){
		return views.get(name);
	}

	/**
	 * @return the context
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * Sets the context.
	 * @param context context to set
	 */
	public void setContext(Context context) {
		this.context = context;
		if (context.getOptions()!=null)
			options = context.getOptions();
	}

	/**
	 * Adds parsed data type.
	 * @param type type to add
	 */
	public void addType(DataType type){
		types.put(type.getName(), type);
		
	}

	/**
	 * Adds parsed data types.
	 * @param aTypes types to add
	 */
	public void addTypes(List<DataType> aTypes){
		for (Iterator<DataType> it = aTypes.iterator(); it.hasNext();){
			addType(it.next());
		}
	}

	/**
	 * Returns parsed data type by name.
	 * @param name name of type
	 * @return type
	 */
	public DataType getType(String name){
		
		DataType ret = types.get(name);
		if (ret == null){
			RuntimeException e = new RuntimeException("No such type: "+name);
			e.fillInStackTrace();
			e.printStackTrace();
			throw e;
		}
		return ret;
	}
	
	/**
	 * Adds the decorator to the storage.
	 * @param decorator the decorator to add.
	 */
	public void addDecorator(MetaDecorator decorator){
		decorators.put(decorator.getName(), decorator);
	}
	
	/**
	 * Adds some decorators to the storage.
	 * @param someDecorators a list with decorators to add.
	 */
	public void addDecorators(List<MetaDecorator> someDecorators){
		for (int i=0; i<someDecorators.size();i++){
			addDecorator(someDecorators.get(i));
		}
	}

	/**
	 * Returns parsed decorator by name.
	 * @param name name of decorator
	 * @return decorator
	 */
	public MetaDecorator getDecorator(String name){
		return decorators.get(name);
	}

	public MetaDecorator createDecorator(String name, String rule){
		MetaDecorator blueprint = getDecorator(name);
		if (blueprint==null)
			throw new RuntimeException("No such decorator: "+name);
		MetaDecorator ret = (MetaDecorator)blueprint.clone();
		ret.setRule(rule);
		return ret;
	}

	/**
	 * Adds parsed filter.
	 * @param filter filter to add
	 */
	public void addFilter(MetaFilter filter){
		filters.put(filter.getName(), filter);
	}

	/**
	 * Adds parsed filters.
	 * @param aFilters filters to add
	 */
	public void addFilters(List<MetaFilter> aFilters){
		for (int i=0; i<aFilters.size();i++){
			addFilter(aFilters.get(i));
		}
	}

	/**
	 * returns parsed filter by name.
	 * @param name name of filter
	 * @return filter
	 */
	public MetaFilter getFilter(String name){
		return filters.get(name);
	}

	public MetaFilter createFilter(String name, String fieldName){
		MetaFilter blueprint = getFilter(name);
		if (blueprint==null)
			throw new RuntimeException("No such filter: "+name);
		MetaFilter ret = (MetaFilter)blueprint.clone();
		ret.setFieldName(fieldName);
		return ret;
	}

	/**
	 * Adds parsed validator.
	 * @param validator validator to add
	 */
	public void addValidator(MetaValidator validator) {
		validators.put(validator.getName(), validator);
	}
	
	/**
	 * Adds parsed validators.
	 * @param aValidators validators to add
	 */
	public void addValidators(List<MetaValidator> aValidators) {
		for (MetaValidator validator : aValidators)
			addValidator(validator);
	}
	
	/**
	 * Returns parsed validator by name.
	 * @param name validator name
	 * @return validator
	 */
	public MetaValidator getValidator(String name) {
		MetaValidator result = validators.get(name);
		if (result == null) {
			throw new RuntimeException("No such validator: " + name);
		}
		return result;
	}
	
	/**
	 * Returns true if the document should have methods for language copying.
	 * @param doc the document to check.
	 * @return true if support for languages is enabled and the document has multilingual attributes.
	 */
	public static boolean hasLanguageCopyMethods(MetaDocument doc){
		if (!getInstance().getContext().areLanguagesSupported())
			return false;
		return doc.isMultilingual();
	}

	private static GenerationOptions createDefaultGenerationOptions(){
		GenerationOptions ret = new GenerationOptions();
		
		ret.set("rmi", "false");
		ret.set("inmemory", "false");
		
		
		return ret;
	}

	public GenerationOptions getOptions() {
		return options;
	}

	public void setOptions(GenerationOptions options) {
		this.options = options;
	}

}
