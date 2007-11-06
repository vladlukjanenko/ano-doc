package net.anotheria.asg.generator;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.types.meta.DataType;
import net.anotheria.asg.generator.view.meta.MetaDecorator;
import net.anotheria.asg.generator.view.meta.MetaFilter;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class GeneratorDataRegistry {
	private static GeneratorDataRegistry instance;
	
	private Hashtable<String,MetaModule> modules;
	private Context context;
	private Hashtable<String,DataType> types;
	private Hashtable<String,MetaDecorator> decorators;
	private Hashtable<String, MetaFilter> filters;
	
	
	private GeneratorDataRegistry(){
		modules = new Hashtable<String,MetaModule>();
		types   = new Hashtable<String,DataType>();
		decorators = new Hashtable<String,MetaDecorator>();
		filters = new Hashtable<String, MetaFilter>();
	}
	
	public static synchronized  GeneratorDataRegistry getInstance(){
		if (instance == null)
			instance = new GeneratorDataRegistry();
		return instance;
	}
	
	public void addModule(MetaModule m){
		modules.put(m.getName(), m);
	}
	
	public void addModules(List<MetaModule> modules){
		for (int i=0; i<modules.size(); i++)
			addModule((MetaModule)modules.get(i));
	}
	
	public MetaDocument resolveLink(String link){
		int dotIndex = link.indexOf('.');
		String targetModuleName = link.substring(0,dotIndex);
		String targetDocumentName = link.substring(dotIndex+1);
		MetaModule mod = getModule(targetModuleName);
		MetaDocument targetDocument = mod.getDocumentByName(targetDocumentName);
		return targetDocument;
	}
	
	public MetaModule getModule(String name){
		return (MetaModule) modules.get(name);
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
		if (ret == null)
			throw new RuntimeException("No such type: "+name);
		return ret;
	}
	
	public void addDecorator(MetaDecorator decorator){
		decorators.put(decorator.getName(), decorator);
	}
	
	public void addDecorators(List<MetaDecorator> decorators){
		for (int i=0; i<decorators.size();i++){
			addDecorator((MetaDecorator)decorators.get(i));
		}
	}
	
	public MetaDecorator getDecorator(String name){
		return (MetaDecorator)decorators.get(name);
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
			addFilter((MetaFilter)filters.get(i));
		}
	}
	
	public MetaFilter getFilter(String name){
		return (MetaFilter)filters.get(name);
	}

	public MetaFilter createFilter(String name, String fieldName){
		MetaFilter blueprint = getFilter(name);
		if (blueprint==null)
			throw new RuntimeException("No such filter: "+name);
		MetaFilter ret = (MetaFilter)blueprint.clone();
		ret.setFieldName(fieldName);
		return ret;
	}


}
