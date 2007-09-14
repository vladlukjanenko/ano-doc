package net.anotheria.asg.generator;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.apputil.CallContextGenerator;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.StorageType;
import net.anotheria.asg.generator.model.ServiceGenerator;
import net.anotheria.util.StringUtils;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class ConfiguratorGenerator extends AbstractGenerator{
	
	public static String getConfiguratorClassName(){
		return "AnoDocConfigurator";
	}

	public static String getExporterClassName(Context context){
		return StringUtils.capitalize(context.getApplicationName())+"XMLExporter";
	}
	
	public List<FileEntry> generate(List<MetaModule> modules, Context context){
		List<FileEntry> entries = new ArrayList<FileEntry>();
		
		entries.add(generateConfigurator(modules, context));
		entries.add(generateExporter(modules, context));
		
		return entries;
		
	}
	
	private FileEntry generateConfigurator(List<MetaModule> modules, Context context){
		String ret = "";
		
		ret += writeStatement("package "+context.getPackageName());


		ret += writeImport("net.anotheria.anodoc.service.IModuleFactory");
		ret += writeImport("net.anotheria.anodoc.service.IModuleService");
		ret += writeImport("net.anotheria.anodoc.service.ModuleServiceFactory");
		ret += writeImport("net.anotheria.anodoc.util.CommonHashtableModuleStorage");
		ret += emptyline();
		ret += writeImport(context.getPackageName()+".data.*");
		
		ret += emptyline();
		ret += writeString("public class "+getConfiguratorClassName()+"{");
		increaseIdent();
		ret += emptyline();

		ret += writeString("private static void addCommonStorage(String moduleId, IModuleService service, IModuleFactory factory, String storageDirConfigKey){");
		increaseIdent();
		ret += writeString("service.attachModuleFactory(moduleId, factory );");
		ret += writeString("if (storageDirConfigKey==null)");
		ret += writeIncreasedString("service.attachModuleStorage(moduleId, new CommonHashtableModuleStorage(moduleId+\".dat\", factory));");
		ret += writeString("else");
		ret += writeIncreasedString("service.attachModuleStorage(moduleId, new CommonHashtableModuleStorage(moduleId+\".dat\", factory, storageDirConfigKey));");
		ret += closeBlock();
		ret += emptyline();
		
		ret += writeStatement("private static boolean configured");
		ret += emptyline();
		ret += writeString("public static void configure(){");
		increaseIdent();
		ret += writeString("if (configured)");
		increaseIdent();
		ret += writeString("return;");
		decreaseIdent();
		ret += writeString("configured = true;");
		ret += writeString("net.anotheria.anodoc.util.context.ContextManager.setFactory(new "+CallContextGenerator.getFullFactoryName(context)+"());");
		
		ret += writeStatement("IModuleService service = ModuleServiceFactory.createModuleService()");
		
		for (int i=0; i<modules.size(); i++){
			MetaModule m = modules.get(i);
			if (m.getStorageType()==StorageType.CMS){
				String call = "addCommonStorage(";
				call += m.getModuleClassName()+".MODULE_ID";
				call += ", ";
				call += "service";
				call += ", ";
				call += "new "+m.getFactoryClassName()+"()";
				if (m.getStorageKey()!=null)
					call += ", "+quote(m.getStorageKey());
				else
					call += ", null";
				call +=")";
				ret += writeStatement(call);
			}
			  
		}
		ret += closeBlock();
		ret += closeBlock();
			
		return new FileEntry(FileEntry.package2path(context.getPackageName()), getConfiguratorClassName(),ret);
		
	}
	
	private FileEntry generateExporter(List<MetaModule> modules, Context context){
		String ret = "";
		
		ret += writeStatement("package "+context.getPackageName());
		
		/*ret += writeImport("net.anotheria.anodoc.service.IModuleFactory");
		ret += writeImport("net.anotheria.anodoc.service.IModuleService");
		ret += writeImport("net.anotheria.anodoc.service.ModuleServiceFactory");
		ret += writeImport("net.anotheria.anodoc.util.CommonHashtableModuleStorage");
		ret += emptyline();
		ret += writeImport(context.getPackageName()+".data.*");
		*/
		
		ret += writeImport("java.util.List");
		ret += writeImport("java.util.ArrayList");
		ret += writeImport("java.io.File");
		ret += writeImport("java.io.FileOutputStream");
		ret += writeImport("java.io.OutputStream");
		ret += writeImport("java.io.IOException");
		ret += emptyline();
		ret += writeImport("org.jdom.Element");
		ret += writeImport("org.jdom.Attribute");
		ret += writeImport("org.jdom.Document");
		ret += writeImport("org.jdom.output.XMLOutputter");
		ret += emptyline();
		ret += writeImport("net.anotheria.util.Date");
		ret += writeImport("org.apache.log4j.BasicConfigurator");
		ret += emptyline();
		for (MetaModule m : modules){
			ret += writeImport(context.getPackageName()+".service"+"."+ServiceGenerator.getFactoryName(m));
		}
		
		ret += emptyline();
		ret += writeString("public class "+getExporterClassName(context)+"{");
		increaseIdent();
		ret += emptyline();
		
		ret += writeString("public "+getExporterClassName(context)+"(){");
		increaseIdent();
		ret += writeStatement(getConfiguratorClassName()+".configure()");
		ret += closeBlock();
		ret += emptyline();
		
		
		ret += writeComment("Create an XML Document (jdom) with data from all modules.");
		ret += writeString("public Document createCompleteXMLExport(){");
		increaseIdent();
		ret += writeStatement("ArrayList<Element> elements = new ArrayList<Element>()");
		for (MetaModule m : modules){
			ret += writeStatement("elements.add("+ServiceGenerator.getFactoryName(m)+".create"+ServiceGenerator.getServiceName(m)+"().exportToXML())");
		}
		ret += writeStatement("return createExport(elements)");
		ret += closeBlock();
		ret += emptyline();

		ret += writeComment("Write XML data from all modules into given stream.");
		ret += writeString("public void writeCompleteXMLExportToStream(OutputStream target) throws IOException{");
		increaseIdent();
		ret += writeStatement("new XMLOutputter().output(createCompleteXMLExport(), target)");
		ret += closeBlock();
		ret += emptyline();
		
		ret += writeComment("Write XML data from all modules into given file.");
		ret += writeString("public void writeCompleteXMLExportToFile(File target) throws IOException{");
		increaseIdent();
		ret += writeStatement("writeToFile(createCompleteXMLExport(), target)");
		ret += closeBlock();
		ret += emptyline();

		
		
		//create export methods for all modules.
		for (MetaModule m : modules){
			ret += writeComment("Create an XML Document (jdom) from "+m.getName()+" data for export.");
			ret += writeString("public Document create"+m.getName()+"XMLExport(){");
			increaseIdent();
			ret += writeStatement("ArrayList<Element> elements = new ArrayList<Element>(1)");
			ret += writeStatement("elements.add("+ServiceGenerator.getFactoryName(m)+".create"+ServiceGenerator.getServiceName(m)+"().exportToXML())");
			ret += writeStatement("return createExport(elements)");
			ret += closeBlock();
			ret += emptyline();

			ret += writeComment("Write "+m.getName()+" as XML into given stream.");
			ret += writeString("public void write"+m.getName()+"XMLExportToStream(OutputStream target) throws IOException{");
			increaseIdent();
			ret += writeStatement("new XMLOutputter().output(create"+m.getName()+"XMLExport(), target)");
			ret += closeBlock();
			ret += emptyline();
			
			ret += writeComment("Write "+m.getName()+" as XML into given file.");
			ret += writeString("public void write"+m.getName()+"XMLExportToFile(File target) throws IOException{");
			increaseIdent();
			ret += writeStatement("writeToFile(create"+m.getName()+"XMLExport(), target)");
			ret += closeBlock();
			ret += emptyline();
		}
		
		//private methods
		ret += writeString("private void writeToFile(Document doc, File target) throws IOException{");
		increaseIdent();
		ret += writeStatement("FileOutputStream fOut = null");
		ret += writeString("try{");
		increaseIdent();
		ret += writeStatement("fOut = new FileOutputStream(target)");
		ret += writeStatement("XMLOutputter outputter = new XMLOutputter()");
		ret += writeStatement("outputter.output(doc, fOut)");
		ret += writeStatement("fOut.flush()");
		ret += writeStatement("fOut.close()");
		decreaseIdent();
		ret += writeStatement("}catch(IOException e){");
		increaseIdent();
		ret += writeString("if (fOut!=null){");
		increaseIdent();
		ret += writeString("try{");
		ret += writeIncreasedStatement("fOut.close()");
		ret += writeString("}catch(IOException ignored){}");
		ret += closeBlock();
		ret += writeStatement("throw e");
		ret += closeBlock();
		ret += closeBlock();
		ret += emptyline();

		ret += writeString("private Document createExport(List<Element> elements){");
		increaseIdent();
		ret += writeStatement("Document doc = new Document()");
		ret += writeStatement("Element root = new Element("+quote("export")+")");
		ret += writeStatement("root.setAttribute(new Attribute("+quote("timestamp")+", \"\"+System.currentTimeMillis()))");
		ret += writeStatement("root.setAttribute(new Attribute("+quote("date")+", Date.currentDate().toString()))");
		ret += writeStatement("doc.setRootElement(root)");
		ret += writeStatement("System.out.println(\"Warning, xml export disabled\")");
		ret += writeStatement("//root.setChildren(elements)");
		ret += writeStatement("return doc");
		ret += closeBlock();
		ret += emptyline();
		
		ret += writeString("public static void main(String[] a) throws IOException{");
		increaseIdent();
		ret += writeStatement("BasicConfigurator.configure()");
		ret += writeStatement("new "+getExporterClassName(context)+"().writeCompleteXMLExportToFile(new File("+quote(context.getApplicationName()+"_export.xml")+"))");
		ret += closeBlock();

		
		ret += closeBlock();
		

		return new FileEntry(FileEntry.package2path(context.getPackageName()), getExporterClassName(context),ret);
		
	}

}
