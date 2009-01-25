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
	protected void generateGetFootprintMethod(MetaDocument doc){
		appendString("public String getFootprint(){");
		increaseIdent();
		appendStatement("StringBuilder footprint = new StringBuilder()");

		generatePropertyListFootprint(doc.getProperties());
		generatePropertyListFootprint(doc.getLinks());
		
		appendStatement("return MD5Util.getMD5Hash(footprint)");
		append(closeBlock());
	}
	
	protected void generatePropertyListFootprint(List<MetaProperty> properties){
		Context c = GeneratorDataRegistry.getInstance().getContext();

		for (MetaProperty p : properties){
			if (c.areLanguagesSupported() && p.isMultilingual()){
				for (String l : c.getLanguages())
					appendStatement("footprint.append(get"+p.getAccesserName(l)+"())");
			}else{
				appendStatement("footprint.append(get"+p.getAccesserName()+"())");
			}
		}
	}

}
