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
import net.anotheria.util.NumberUtils;

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
		
		long s1 = System.currentTimeMillis();
		
		String dataContent = XMLPreprocessor.loadFile(new File(BASE_DIR+"etc/def/datadef.xml"));
		String contextContent = XMLPreprocessor.loadFile(new File(BASE_DIR+"etc/def/context.xml"));
		String viewContent = null;
		//System.out.println("VIEW GENERATOR TURNED OFF");

		long s2 = System.currentTimeMillis();
		///*
		try{
			viewContent = XMLPreprocessor.loadFile(new File(BASE_DIR+"etc/def/editview_def.xml"));
		}catch(IOException ignored){}
		//*/
		XMLContextParser contextParser = new XMLContextParser(contextContent);
		Context c = contextParser.parseContext();
		GeneratorDataRegistry.getInstance().setContext(c);
		
		System.out.println("Context parameters: "+c.getContextParameters());
		long s3 = System.currentTimeMillis();
		
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

		long s4 = System.currentTimeMillis();
		try{
			String decoratorsContent = XMLPreprocessor.loadFile(new File(BASE_DIR+"etc/def/decorators-def.xml"));
			XMLDecoratorsParser dParser = new XMLDecoratorsParser(decoratorsContent);
			List<MetaDecorator> decorators = dParser.parseDecorators();
			GeneratorDataRegistry.getInstance().addDecorators(decorators);
			//System.out.println(decorators); 
		}catch(Exception e){}

		long s5 = System.currentTimeMillis();
		try{
			String filtersContent = XMLPreprocessor.loadFile(new File(BASE_DIR+"etc/def/filters-def.xml"));
			XMLFiltersParser dParser = new XMLFiltersParser(filtersContent);
			List<MetaFilter> filters = dParser.parseFilters();
			//System.out.println("parsed filters: "+filters);
			GeneratorDataRegistry.getInstance().addFilters(filters);
			//System.out.println(decorators); 
		}catch(Exception e){}

		long s6 = System.currentTimeMillis();
		XMLDataParser dataParser = new XMLDataParser(dataContent);
		List<MetaModule> modules = dataParser.parseModules();
		GeneratorDataRegistry.getInstance().addModules(modules);
		
		AppUtilGenerator utilGen = new AppUtilGenerator(c);
		utilGen.generate(modules);

		long s7 = System.currentTimeMillis();
		
		DataGenerator g = new DataGenerator(c);
		g.generate("java", modules);

		long s8 = System.currentTimeMillis();
		
		
		if(viewContent!=null){
			XMLViewParser viewParser = new XMLViewParser(viewContent);
			List<MetaView> views = viewParser.parseViews();
		//	System.out.println("Parsed views: "+views);
			ViewGenerator v = new ViewGenerator();
			v.generate("java", views);
		}
		
		long s9 = System.currentTimeMillis();
		
		
		try{
			String formContent = XMLPreprocessor.loadFile(new File(BASE_DIR+"etc/def/forms_def.xml"));
			XMLFormParser formParser = new XMLFormParser(formContent);
			List<MetaForm> forms = formParser.parseForms();
			FormsGenerator fg = new FormsGenerator();
			fg.generate("java", forms);
			//System.out.println(forms); 
		}catch(Exception e){}
		
		long s10 = System.currentTimeMillis();

		System.out.println("DONE.");
		printTime("Total ", s10, s1);
		///*
		printTime("Till s2", s2, s1);
		printTime("Till s3", s3, s1);
		printTime("Till s4", s4, s1);
		printTime("Till s5", s5, s1);
		printTime("Till s6", s6, s1);
		printTime("Till s7", s7, s1);
		printTime("Till s8", s8, s1);
		printTime("Till s9", s9, s1);
		printTime("Till s10", s10, s1);
		
		printTime("s2", s2, s1);
		printTime("s3", s3, s2);
		printTime("s4", s4, s3);
		printTime("s5", s5, s4);
		printTime("s6", s6, s5);
		printTime("s7", s7, s6);
		printTime("s8", s8, s7);
		printTime("s9", s9, s8);
		printTime("s10", s10, s9);
		// */
	}
	
	private static void printTime(String name, long end, long start){
		System.out.println(name+": "+NumberUtils.getDotedNumber(end-start));
	}
	
	public static final String getVersionString(){
		return "1.3.2";
	}
	
	public static final String getProductString(){
		return "AnoSiteGenerator (ASG)";
	}
}
