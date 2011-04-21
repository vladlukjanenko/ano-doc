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
	 * @return 
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
	 * @param modules a list of modules to add.
	 */
	public void addModules(List<MetaModule> modules){
		for (MetaModule m: modules)
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
	 * @param views the views to add.
	 */
	public void addViews(List<MetaView> views){
		for (MetaView v: views)
			addView(v);
	}
	
	/**
	 * 
	 * @param link
	 * @return
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
	 * @return
	 */
	public MetaModule getModule(String name){
		return modules.get(name);
	}
	
	public Collection<MetaModule> getModules(){
		return modules.values();
	}
	
	public MetaView getView(String name){
		return views.get(name);
	}
	/**
	 * @return
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * @param context
	 */
	public void setContext(Context context) {
		this.context = context;
		if (context.getOptions()!=null)
			options = context.getOptions();
	}
	
	public void addType(DataType type){
		types.put(type.getName(), type);
		
	}
	
	public void addTypes(List<DataType> types){
		for (Iterator<DataType> it = types.iterator(); it.hasNext();){
			addType(it.next());
		}
	}

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
	 * @param decorators a list with decorators to add.
	 */
	public void addDecorators(List<MetaDecorator> someDecorators){
		for (int i=0; i<someDecorators.size();i++){
			addDecorator(someDecorators.get(i));
		}
	}
	
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
	
	public void addFilter(MetaFilter filter){
		filters.put(filter.getName(), filter);
	}

	public void addFilters(List<MetaFilter> filters){
		for (int i=0; i<filters.size();i++){
			addFilter(filters.get(i));
		}
	}
	
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
	
	public void addValidator(MetaValidator validator) {
		validators.put(validator.getName(), validator);
	}
	
	public void addValidators(List<MetaValidator> validators) {
		for (MetaValidator validator : validators)
			addValidator(validator);
	}
	
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
	public static final boolean hasLanguageCopyMethods(MetaDocument doc){
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
