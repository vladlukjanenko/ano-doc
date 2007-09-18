package net.anotheria.asg.generator.model;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;

public class AbstractDataObjectGenerator extends AbstractGenerator{
	protected String getPackageName(MetaDocument doc){
		return GeneratorDataRegistry.getInstance().getContext().getDataPackageName(doc);
	}
	
	public static String getPackageName(Context context, MetaDocument doc){
		return context.getPackageName(doc);
	}

	public static String getPackageName(Context context, MetaModule module){
		return context.getPackageName(module);
	}
}
