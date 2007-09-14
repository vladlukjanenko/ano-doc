package net.anotheria.asg.generator.model.docs;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.CommentGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class FactoryGenerator extends AbstractGenerator implements IGenerator{
	
	
	public List<FileEntry> generate(IGenerateable gmodule, Context context){
		
		MetaModule mod = (MetaModule)gmodule;
		
		List<FileEntry> ret = new ArrayList<FileEntry>();
		
		ret.add(generateFactory(mod, context));
		
		return ret;
	}
	
	public static String getModuleFactoryName(MetaModule module){
		return module.getFactoryClassName();
	}
	
	private FileEntry generateFactory(MetaModule module, Context context){
		String ret = "";
		
		String packageName = context.getPackageName()+".data";
	
		ret += CommentGenerator.generateJavaTypeComment(getModuleFactoryName(module), "The Factory for the "+module.getName()+" objects.");

		ret += writeStatement("package "+packageName);
		ret += emptyline();
		
		
		ret += writeImport("net.anotheria.anodoc.data.Module");
		ret += writeImport("net.anotheria.anodoc.data.Document");
//		ret += writeImport("net.anotheria.anodoc.data.IDHolder");
		ret += writeImport("net.anotheria.anodoc.data.DataHolder");
		ret += writeImport("net.anotheria.anodoc.service.AbstractModuleFactory");

						
		ret += emptyline();
		ret += writeString("public class "+module.getFactoryClassName()+ " extends AbstractModuleFactory{");
		increaseIdent();
		ret += emptyline();
		ret += generateModuleCreator(module);
		ret += emptyline();
		ret += generateSecondLevelDocumentCreator(module);
		ret += emptyline();
		ret += closeBlock();
		
		
		return new FileEntry(FileEntry.package2path(packageName), module.getFactoryClassName(), ret);
	}
	
	private String generateModuleCreator(MetaModule module){
		String ret = "";
		ret += writeString("public Module recreateModule(String ownerId, String copyId) {");
		increaseIdent();
		ret += writeStatement("return new Module"+module.getName()+"()");
		ret += closeBlock();
		return ret;	
		
	}
	
	private String generateSecondLevelDocumentCreator(MetaModule module){
		String ret = "";
		ret += writeString("public Document createDocument(String id, DataHolder context) {");
		increaseIdent();
		List<MetaDocument> docs = module.getDocuments();
		for (int i=0; i<docs.size(); i++){
			MetaDocument doc = docs.get(i);
			ret += writeString("if (context.getId().equals("+module.getModuleClassName()+"."+doc.getListName()+"))");
			increaseIdent();
			ret += writeStatement("return new "+DocumentGenerator.getDocumentName(doc)+"(id)");
			decreaseIdent();
		}
		
		ret += writeStatement("throw new RuntimeException(\"Unexpected document in list:\"+context.getId())");
		ret += closeBlock();
		return ret;
		
	}
}
