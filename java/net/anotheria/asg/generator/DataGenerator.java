package net.anotheria.asg.generator;

import java.util.ArrayList;
import java.util.List;


import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.StorageType;
import net.anotheria.asg.generator.model.ModuleGenerator;
import net.anotheria.asg.generator.model.ServiceGenerator;
import net.anotheria.asg.generator.model.db.SQLGenerator;
import net.anotheria.asg.generator.model.docs.ModuleFactoryGenerator;
import net.anotheria.util.ExecutionTimer;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class DataGenerator extends AbstractAnoDocGenerator{
	
	public void generate(String path, List<MetaModule> modules){
		Context context = GeneratorDataRegistry.getInstance().getContext();
		List<FileEntry> todo = new ArrayList<FileEntry>();
		ExecutionTimer timer = new ExecutionTimer("DataGenerator");
		timer.startExecution("config");
		todo.addAll(new ConfiguratorGenerator().generate(modules));
		timer.stopExecution("config");
		timer.startExecution("basic service");
		todo.addAll(new BasicServiceGenerator().generate(modules));
		timer.stopExecution("basic service");
		timer.startExecution("sql");
		todo.addAll(new SQLGenerator().generate(modules));
		timer.stopExecution("sql");
		timer.startExecution("modules");
		for (int i=0; i<modules.size(); i++){
			MetaModule m = modules.get(i);
			timer.startExecution(m.getName());
			if (m.getStorageType()==StorageType.CMS){
				runGenerator(new ModuleFactoryGenerator(), m, context, todo);
			}
			timer.startExecution(m.getName()+"-ModuleGen");
			runGenerator(new ModuleGenerator(), m, context, todo);
			timer.stopExecution(m.getName()+"-ModuleGen");
			timer.startExecution(m.getName()+"-ServiceGen");
			runGenerator(new ServiceGenerator(), m, context, todo);
			timer.stopExecution(m.getName()+"-ServiceGen");
			timer.stopExecution(m.getName());
			//System.out.println(todo);	
		}
		timer.stopExecution("modules");
		//timer.startExecution("write");
		writeFiles(todo);
		//timer.stopExecution("write");
		//timer.printExecutionTimesOrderedByCreation();
	}
	
}
