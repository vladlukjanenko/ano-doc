package net.anotheria.asg.generator.model;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.StorageType;
import net.anotheria.asg.generator.model.db.VOGenerator;
import net.anotheria.asg.generator.model.docs.DocumentGenerator;
import net.anotheria.asg.generator.model.federation.FederationVOGenerator;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public class ModuleGenerator extends AbstractGenerator implements IGenerator{
	
	
	public List<FileEntry> generate(IGenerateable gmodule, Context context){
		
		MetaModule mod = (MetaModule)gmodule;
		
		List<FileEntry> ret = new ArrayList<FileEntry>();
		
		if (mod.getStorageType()==StorageType.CMS)
			ret.add(generateModule(mod, context));
		for (int i=0; i<mod.getDocuments().size();i++){
			DataFacadeGenerator facadeGen = new DataFacadeGenerator();
			ret.addAll(facadeGen.generate(mod.getDocuments().get(i), context));
			//System.out.println("Generating module: "+mod);

			if (mod.getStorageType()==StorageType.CMS){
				DocumentGenerator dg = new DocumentGenerator();
				ret.addAll(dg.generate(mod.getDocuments().get(i), context));
			}
			
			if (mod.getStorageType()==StorageType.DB){
				VOGenerator vog = new VOGenerator();
				ret.addAll(vog.generate(mod.getDocuments().get(i), context));
			}

			if (mod.getStorageType()==StorageType.FEDERATION){
				FederationVOGenerator vog = new FederationVOGenerator();
				ret.addAll(vog.generate(mod.getDocuments().get(i), context));
			}
		
		}
			
		
		return ret;
	}
	
	private FileEntry generateModule(MetaModule module, Context context){
		String ret = "";
		
		String packageName = context.getPackageName(module)+".data";
	
		ret += writeStatement("package "+packageName);
		ret += emptyline();
		
		ret += writeImport("net.anotheria.anodoc.data.Module");
		ret += writeImport("net.anotheria.anodoc.data.DocumentList");
		//ret += writeImport("net.anotheria.anodoc.data.Document");
		ret += writeImport("net.anotheria.anodoc.data.IDHolder");
		ret += emptyline();
		ret += writeImport("net.anotheria.anodoc.data.NoSuchDocumentListException");
		ret += emptyline();
		ret += writeImport("java.util.List");
						
		ret += emptyline();
		ret += writeString("public class Module"+module.getName()+" extends Module{");
		increaseIdent();
		ret += emptyline();
		
		ret += generateConstants(module);
		ret += emptyline();
		ret += generateConstructor(module);
		ret += emptyline();
		
		List documents = module.getDocuments();
		for (int i=0; i<documents.size(); i++){
			MetaDocument doc = (MetaDocument)documents.get(i);
			//do document type related
			ret += generateDocumentRelatedCode(doc);
			ret += emptyline();
		}
		
		ret += closeBlock();
		
		return new FileEntry(FileEntry.package2path(packageName), module.getModuleClassName(), ret);
	}
	
	private String generateConstructor(MetaModule module){
		String ret = "";
		ret += writeString("public "+module.getModuleClassName()+"(){");
		increaseIdent();
		ret += writeString("super(MODULE_ID);");
		ret += closeBlock();
		return ret;
	}

	private String generateConstants(MetaModule module){
		String ret = "";
		
		//first generate module id.
		ret += writeStatement("public static final String MODULE_ID = "+quote(module.getId()));
		ret += emptyline();
		List<MetaDocument> documents = module.getDocuments();
		for (int i=0; i<documents.size(); i++){
			MetaDocument d = documents.get(i);
			String listName = "public static final String ";
			listName += d.getListName();
			listName += " = "+quote(d.getListConstantValue());
			ret += writeStatement(listName);
			
			String idHolderName = "public static final String ";
			idHolderName += d.getIdHolderName();
			idHolderName += " = IDHolder.DOC_ID_HOLDER_PRE+"+quote(d.getName().toLowerCase());
			ret += writeStatement(idHolderName);
			
			
		} 
		
		return ret;
	}
	
	private String generateDocumentRelatedCode(MetaDocument doc){
		String ret = "";
		
		ret += writeString("@SuppressWarnings(\"unchecked\")");
		ret += writeString("private DocumentList<"+DocumentGenerator.getDocumentName(doc)+"> _get"+doc.getMultiple()+"(){");
		increaseIdent();
		ret += writeString("try{");
		increaseIdent();
		ret += writeStatement("return getList("+doc.getListName()+")");
		decreaseIdent();
		ret += writeString("}catch(NoSuchDocumentListException e){");
		increaseIdent();
		ret += writeStatement("return new DocumentList<"+DocumentGenerator.getDocumentName(doc)+">("+doc.getListName()+")");
		ret += closeBlock();
		ret += closeBlock();
		ret += emptyline();
		
		ret += writeString("private void _update"+doc.getMultiple()+"(DocumentList<"+DocumentGenerator.getDocumentName(doc)+"> list){");
		increaseIdent();
		ret += writeString("putList(list);");
		ret += closeBlock();
		ret += emptyline();

		ret += writeString("public List<"+DocumentGenerator.getDocumentName(doc)+"> get"+doc.getMultiple()+"(){");
		increaseIdent();
		ret += writeString("return _get"+doc.getMultiple()+"().getList();");
		ret += closeBlock();
		ret += emptyline();

		ret += writeString("public "+DocumentGenerator.getDocumentName(doc)+" get"+doc.getName()+"(String id){");
		increaseIdent();
		ret += writeStatement("return _get"+doc.getMultiple()+"().getDocumentById(id)");
		ret += closeBlock();
		ret += emptyline();
		
		ret += writeString("public void update"+doc.getName()+"("+DocumentGenerator.getDocumentName(doc)+" "+doc.getVariableName()+"){");
		increaseIdent();
		ret += writeString("DocumentList<"+DocumentGenerator.getDocumentName(doc)+"> "+doc.getMultiple().toLowerCase()+" = _get"+doc.getMultiple()+"();");
		ret += writeString(doc.getMultiple().toLowerCase()+".removeDocumentById("+doc.getVariableName()+".getId());");
		ret += writeStatement(doc.getVariableName()+".setLastUpdateNow()");
		ret += writeString(doc.getMultiple().toLowerCase()+".addDocument("+doc.getVariableName()+");");
		ret += writeString("_update"+doc.getMultiple()+"("+doc.getMultiple().toLowerCase()+");");
		ret += closeBlock();
		ret += emptyline();
		
		ret += writeString("public void delete"+doc.getName()+"(String id){");
		increaseIdent();
		ret += writeStatement("DocumentList<"+DocumentGenerator.getDocumentName(doc)+"> entries = _get"+doc.getMultiple()+"()");
		ret += writeStatement("entries.removeDocumentById(id)");
		ret += writeStatement("_update"+doc.getMultiple()+"(entries)");
		ret += closeBlock();
		ret += emptyline();
		
		ret += writeString("public "+DocumentGenerator.getDocumentName(doc)+" create"+doc.getName()+"("+DocumentGenerator.getDocumentName(doc)+" "+doc.getVariableName()+" ){");
		increaseIdent();
		ret += writeStatement("IDHolder idh = _getIdHolder("+doc.getIdHolderName()+")");
		ret += writeStatement("int id = idh.getNextIdInt()");
		ret += writeStatement(doc.getVariableName()+".renameTo(\"\"+id)");
		ret += writeStatement("putDocument(idh)");
		ret += emptyline();
		ret += writeStatement("DocumentList<"+DocumentGenerator.getDocumentName(doc)+"> entries = _get"+doc.getMultiple()+"()");
		ret += writeStatement(doc.getVariableName()+".setLastUpdateNow()");
		ret += writeStatement("entries.addDocument("+doc.getVariableName()+")");
		ret += writeStatement("_update"+doc.getMultiple()+"(entries)");
		ret += writeStatement("return "+doc.getVariableName());
		ret += closeBlock();
		ret += emptyline();


		return ret;

			
		
	}
	
	
	
}
