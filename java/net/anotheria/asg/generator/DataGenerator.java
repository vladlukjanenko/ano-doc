package net.anotheria.asg.generator;

import java.util.ArrayList;
import java.util.List;


import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.StorageType;
import net.anotheria.asg.generator.model.ModuleGenerator;
import net.anotheria.asg.generator.model.ServiceGenerator;
import net.anotheria.asg.generator.model.db.SQLGenerator;
import net.anotheria.asg.generator.model.docs.ModuleFactoryGenerator;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class DataGenerator extends AbstractAnoDocGenerator{
	
	private Context context;
	
	public DataGenerator(Context aContext){
		context = aContext;
	}
	
	public void generate(String path, List<MetaModule> modules){
		List<FileEntry> todo = new ArrayList<FileEntry>();
		todo.addAll(new ConfiguratorGenerator().generate(modules, context));
		todo.addAll(new BasicServiceGenerator().generate(modules, context));
		todo.addAll(new SQLGenerator().generate(modules, context));
		for (int i=0; i<modules.size(); i++){
			MetaModule m = modules.get(i);
			if (m.getStorageType()==StorageType.CMS){
				runGenerator(new ModuleFactoryGenerator(), m, context, todo);
			}
			runGenerator(new ModuleGenerator(), m, context, todo);
			runGenerator(new ServiceGenerator(), m, context, todo);
			
			//System.out.println(todo);	
		}
		writeFiles(todo);
	}
	
}
