package net.anotheria.asg.generator.view.action;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.util.StringUtils;

public class AbstractActionGenerator extends AbstractGenerator{

	/**
	 * Returns the base action name for the application.
	 * @param context
	 * @return
	 */
	protected static String getBaseActionName(){
		return "Base"+StringUtils.capitalize(GeneratorDataRegistry.getInstance().getContext().getApplicationName())+"Action";
	}
	
	
	protected static String getSharedActionPackageName(){
		return GeneratorDataRegistry.getInstance().getContext().getPackageName(MetaModule.SHARED)+".action";
	}
	
	protected static String getBaseActionClassName(){
		return getSharedActionPackageName() + "." + getBaseActionName();
	}
	
}
