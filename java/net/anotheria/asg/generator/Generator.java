package net.anotheria.asg.generator;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.anotheria.asg.generator.apputil.AppUtilGenerator;
import net.anotheria.asg.generator.forms.FormsGenerator;
import net.anotheria.asg.generator.forms.meta.MetaForm;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.parser.XMLContextParser;
import net.anotheria.asg.generator.parser.XMLDataParser;
import net.anotheria.asg.generator.parser.XMLDecoratorsParser;
import net.anotheria.asg.generator.parser.XMLFiltersParser;
import net.anotheria.asg.generator.parser.XMLFormParser;
import net.anotheria.asg.generator.parser.XMLPreprocessor;
import net.anotheria.asg.generator.parser.XMLTypesParser;
import net.anotheria.asg.generator.parser.XMLViewParser;
import net.anotheria.asg.generator.types.TypesGenerator;
import net.anotheria.asg.generator.types.meta.DataType;
import net.anotheria.asg.generator.view.ViewGenerator;
import net.anotheria.asg.generator.view.meta.MetaDecorator;
import net.anotheria.asg.generator.view.meta.MetaFilter;
import net.anotheria.asg.generator.view.meta.MetaView;

/**
 * This is the main class which runs all other generators in order to produce the code.
 * @author another
 */
public class Generator {
	
	private static String BASE_DIR = "";
	
	public static void setBaseDir(String dir){
		BASE_DIR = dir;
	}
	
	public static String getBaseDir(){
		return BASE_DIR;
	}
	
	public static void generate() throws Exception{
		String dataContent = XMLPreprocessor.loadFile(new File(BASE_DIR+"etc/def/datadef.xml"));
		String contextContent = XMLPreprocessor.loadFile(new File(BASE_DIR+"etc/def/context.xml"));
		String viewContent = null;
		try{
			viewContent = XMLPreprocessor.loadFile(new File(BASE_DIR+"etc/def/editview_def.xml"));
		}catch(IOException ignored){}

		XMLContextParser contextParser = new XMLContextParser(contextContent);
		Context c = contextParser.parseContext();
		GeneratorDataRegistry.getInstance().setContext(c);
		
		System.out.println("Context parameters: "+c.getContextParameters());
		
		/*System.out.println("Context ist multilanguage enabled: "+c.areLanguagesSupported());
		if (c.areLanguagesSupported()){
			System.out.println("Supported "+c.getLanguages()+", default: "+c.getDefaultLanguage());
		}*/
		
		try{
			String typesContent = XMLPreprocessor.loadFile(new File(BASE_DIR+"etc/def/datatypes.xml"));
			XMLTypesParser typesParser = new XMLTypesParser(typesContent);
			List<DataType> types = typesParser.parseTypes();
			TypesGenerator tg = new TypesGenerator();
			tg.generate("java", types);
			GeneratorDataRegistry.getInstance().addTypes(types);
			//System.out.println(types); 
		}catch(Exception e){}

		try{
			String decoratorsContent = XMLPreprocessor.loadFile(new File(BASE_DIR+"etc/def/decorators-def.xml"));
			XMLDecoratorsParser dParser = new XMLDecoratorsParser(decoratorsContent);
			List<MetaDecorator> decorators = dParser.parseDecorators();
			GeneratorDataRegistry.getInstance().addDecorators(decorators);
			//System.out.println(decorators); 
		}catch(Exception e){}

		try{
			String filtersContent = XMLPreprocessor.loadFile(new File(BASE_DIR+"etc/def/filters-def.xml"));
			XMLFiltersParser dParser = new XMLFiltersParser(filtersContent);
			List<MetaFilter> filters = dParser.parseFilters();
			//System.out.println("parsed filters: "+filters);
			GeneratorDataRegistry.getInstance().addFilters(filters);
			//System.out.println(decorators); 
		}catch(Exception e){}

		XMLDataParser dataParser = new XMLDataParser(dataContent);
		List<MetaModule> modules = dataParser.parseModules();
		GeneratorDataRegistry.getInstance().addModules(modules);
		
		AppUtilGenerator utilGen = new AppUtilGenerator(c);
		utilGen.generate(modules);
		
		DataGenerator g = new DataGenerator(c);
		g.generate("java", modules);

		if(viewContent!=null){
			XMLViewParser viewParser = new XMLViewParser(viewContent);
			List<MetaView> views = viewParser.parseViews();
		//	System.out.println("Parsed views: "+views);
			ViewGenerator v = new ViewGenerator();
			v.generate("java", views);
		}
		
		try{
			String formContent = XMLPreprocessor.loadFile(new File(BASE_DIR+"etc/def/forms_def.xml"));
			XMLFormParser formParser = new XMLFormParser(formContent);
			List<MetaForm> forms = formParser.parseForms();
			FormsGenerator fg = new FormsGenerator();
			fg.generate("java", forms);
			//System.out.println(forms); 
		}catch(Exception e){}
		
		System.out.println("DONE.");
		
	}
	
	public static final String getVersionString(){
		return "1.3.2";
	}
	
	public static final String getProductString(){
		return "AnoSiteGenerator (ASG)";
	}
}
