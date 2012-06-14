package net.anotheria.asg.generator;

import net.anotheria.asg.generator.apputil.AppUtilGenerator;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.parser.*;
import net.anotheria.asg.generator.types.TypesGenerator;
import net.anotheria.asg.generator.types.meta.DataType;
import net.anotheria.asg.generator.util.IncludedDocuments;
import net.anotheria.asg.generator.validation.XMLAgainstXSDValidation;
import net.anotheria.asg.generator.view.ViewGenerator;
import net.anotheria.asg.generator.view.meta.MetaDecorator;
import net.anotheria.asg.generator.view.meta.MetaFilter;
import net.anotheria.asg.generator.view.meta.MetaValidator;
import net.anotheria.asg.generator.view.meta.MetaView;
import net.anotheria.util.NumberUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
        IncludedDocuments includedDocuments = new IncludedDocuments();

        String dataContent = XMLPreprocessor.loadFile(new File(BASE_DIR+"etc/def/datadef.xml"),includedDocuments);
        // validating datadef.xml
        validateXML("datadef",dataContent,includedDocuments);

        String viewContent = null;
        //System.out.println("VIEW GENERATOR TURNED OFF");

        long s2 = System.currentTimeMillis();
        ///*
        try{
            viewContent = XMLPreprocessor.loadFile(new File(BASE_DIR+"etc/def/editview_def.xml"),includedDocuments);
        }catch(IOException ignored){
            ignored.printStackTrace();
        }
        if (viewContent != null){
            //validating editview_def.xml
            validateXML("editview_def",viewContent,includedDocuments);

        }



        String contextContent = XMLPreprocessor.loadFile(new File(BASE_DIR+"etc/def/context.xml"),null);

        //validating context.xml
        validateXML("context",contextContent,null);

        Context c = XMLContextParser.parseContext(contextContent);
        GeneratorDataRegistry.getInstance().setContext(c);

        long s3 = System.currentTimeMillis();

        try{
            String typesContent = XMLPreprocessor.loadFile(new File(BASE_DIR+"etc/def/datatypes.xml"),null);
            // validating datatypes.xml
            validateXML("datatypes",typesContent,null);
            List<DataType> types = XMLTypesParser.parseTypes(typesContent);
            TypesGenerator tg = new TypesGenerator();
            tg.generate("java", types);
            GeneratorDataRegistry.getInstance().addTypes(types);
            //System.out.println(types);
        }catch(Exception e){}

        long s4 = System.currentTimeMillis();
        try{
            String decoratorsContent = XMLPreprocessor.loadFile(new File(BASE_DIR+"etc/def/decorators-def.xml"),null);
            // validating decorators-def.xml
            validateXML("decorators-def",decoratorsContent,null);
            List<MetaDecorator> decorators = XMLDecoratorsParser.parseDecorators(decoratorsContent);
            GeneratorDataRegistry.getInstance().addDecorators(decorators);
            //System.out.println(decorators);
        }catch(Exception e){}

        long s5 = System.currentTimeMillis();
        try{
            String filtersContent = XMLPreprocessor.loadFile(new File(BASE_DIR+"etc/def/filters-def.xml"),null);
            // validating filters-def.xml
            validateXML("filters-def",filtersContent,null);
            List<MetaFilter> filters = XMLFiltersParser.parseFilters(filtersContent);
            //System.out.println("parsed filters: "+filters);
            GeneratorDataRegistry.getInstance().addFilters(filters);
            //System.out.println(filters);
        }catch(Exception e){}

        long s6 = System.currentTimeMillis();
        try{
            String validatorsContent = XMLPreprocessor.loadFile(new File(BASE_DIR+"etc/def/validators-def.xml"),null);
            // validating validators-def.xml
            validateXML("validators-def",validatorsContent,null);
            List<MetaValidator> filters = XMLValidatorsParser.parseValidators(validatorsContent);
            GeneratorDataRegistry.getInstance().addValidators(filters);
        }catch(Exception e){}

        long s7 = System.currentTimeMillis();

        List<MetaModule> modules = XMLDataParser.parseModules(dataContent);
        GeneratorDataRegistry.getInstance().addModules(modules);

        AppUtilGenerator utilGen = new AppUtilGenerator(c);
        utilGen.generate(modules);

        long s8 = System.currentTimeMillis();

        DataGenerator g = new DataGenerator();
        g.generate("java", modules);

        long s9 = System.currentTimeMillis();


        if(viewContent!=null){

            List<MetaView> views = XMLViewParser.parseViews(viewContent);
            GeneratorDataRegistry.getInstance().addViews(views);
            //	System.out.println("Parsed views: "+views);
            ViewGenerator v = new ViewGenerator();
            v.generate("java", views);
        }else{
            System.out.println("VIEW_CONTENT = NULL");
        }

        System.out.println("DONE.");
        printTime("Total ", s9, s1);

//		if (false){
//			printTime("Till s2", s2, s1);
//			printTime("Till s3", s3, s1);
//			printTime("Till s4", s4, s1);
//			printTime("Till s5", s5, s1);
//			printTime("Till s6", s6, s1);
//			printTime("Till s7", s7, s1);
//			printTime("Till s8", s8, s1);
//			printTime("Till s9", s9, s1);
//			
//			printTime("s2", s2, s1);
//			printTime("s3", s3, s2);
//			printTime("s4", s4, s3);
//			printTime("s5", s5, s4);
//			printTime("s6", s6, s5);
//			printTime("s7", s7, s6);
//			printTime("s8", s8, s7);
//			printTime("s9", s9, s8);
//		}
        // */
    }

    private static void printTime(String name, long end, long start){
        System.out.println(name+": "+NumberUtils.getDotedNumber(end-start));
    }

    public static String getVersionString(){
        return "1.3.3";
    }

    public static String getProductString(){
        return "AnoSiteGenerator (ASG)";
    }

    private static void validateXML(String fileName, String fileContent, IncludedDocuments includedDocuments){

        InputStream inputStream = Generator.class.getResourceAsStream("/schema/"+fileName+".xsd");

        if (inputStream != null){
            XMLAgainstXSDValidation.validateAgainstXSDSchema(fileName+".xml",fileContent, inputStream, includedDocuments);
        } else {
            throw new RuntimeException("File /schema/"+fileName+".xsd doesn't exist.");
        }
    }
}
