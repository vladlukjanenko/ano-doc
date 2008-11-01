package net.anotheria.asg.generator.model;

import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.MetaProperty;

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
	
	
	/**
	 * Generates getFootprint method 
	 * @param doc 
	 * @return
	 */
	protected String generateGetFootprintMethod(MetaDocument doc){
		String ret = "";
		ret += writeString("public String getFootprint(){");
		increaseIdent();
		ret += writeStatement("StringBuilder footprint = new StringBuilder()");

		ret += generatePropertyListFootprint(doc.getProperties());
		ret += generatePropertyListFootprint(doc.getLinks());
		
		ret += writeStatement("return MD5Util.getMD5Hash(footprint)");
		ret += closeBlock();
		return ret;
		
	}
	
	protected String generatePropertyListFootprint(List<MetaProperty> properties){
		String ret = "";
		
		Context c = GeneratorDataRegistry.getInstance().getContext();

		for (MetaProperty p : properties){
			if (c.areLanguagesSupported() && p.isMultilingual()){
				for (String l : c.getLanguages())
					ret += writeStatement("footprint.append(get"+p.getAccesserName(l)+"())");
			}else{
				ret += writeStatement("footprint.append(get"+p.getAccesserName()+"())");
			}
		}
		
		return ret;
	}

}
