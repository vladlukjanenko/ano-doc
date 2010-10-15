package net.anotheria.asg.generator.model;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GeneratorDataRegistry;
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
	
	
	public List<FileEntry> generate(IGenerateable gmodule){
		
		MetaModule mod = (MetaModule)gmodule;
		
		List<FileEntry> ret = new ArrayList<FileEntry>();
		
		if (mod.getStorageType()==StorageType.CMS)
			ret.add(new FileEntry(generateModule(mod)));
		for (int i=0; i<mod.getDocuments().size();i++){
			DataFacadeGenerator facadeGen = new DataFacadeGenerator();
			ret.addAll(facadeGen.generate(mod.getDocuments().get(i)));
			//System.out.println("Generating module: "+mod);

			if (mod.getStorageType()==StorageType.CMS){
				DocumentGenerator dg = new DocumentGenerator();
				ret.addAll(dg.generate(mod.getDocuments().get(i)));
			}
			
			if (mod.getStorageType()==StorageType.DB){
				VOGenerator vog = new VOGenerator();
				ret.addAll(vog.generate(mod.getDocuments().get(i)));
			}

			if (mod.getStorageType()==StorageType.FEDERATION){
				FederationVOGenerator vog = new FederationVOGenerator();
				ret.addAll(vog.generate(mod.getDocuments().get(i)));
			}
		
		}
			
		
		return ret;
	}
	
	private GeneratedClass generateModule(MetaModule module){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setPackageName(GeneratorDataRegistry.getInstance().getContext().getPackageName(module)+".data");
	
		clazz.addImport("net.anotheria.anodoc.data.Module");
		clazz.addImport("net.anotheria.anodoc.data.DocumentList");
		//appendImport("net.anotheria.anodoc.data.Document");
		clazz.addImport("net.anotheria.anodoc.data.IDHolder");
		clazz.addImport("net.anotheria.anodoc.data.NoSuchDocumentListException");
		clazz.addImport("java.util.List");
						
		emptyline();
		clazz.setName("Module"+module.getName());
		clazz.setParent("Module");

		startClassBody();
		generateConstants(module);
		emptyline();
		generateConstructor(module);
		emptyline();
		
		List<MetaDocument> documents = module.getDocuments();
		for (int i=0; i<documents.size(); i++){
			MetaDocument doc = documents.get(i);
			//do document type related
			generateDocumentRelatedCode(doc);
			emptyline();
		}
		return clazz;
	}
	
	private void generateConstructor(MetaModule module){
		appendString("public "+module.getModuleClassName()+"(){");
		increaseIdent();
		appendString("super(MODULE_ID);");
		append(closeBlock());
	}

	private String generateConstants(MetaModule module){
		String ret = "";
		
		//first generate module id.
		appendStatement("public static final String MODULE_ID = "+quote(module.getId()));
		emptyline();
		List<MetaDocument> documents = module.getDocuments();
		for (int i=0; i<documents.size(); i++){
			MetaDocument d = documents.get(i);
			String listName = "public static final String ";
			listName += d.getListName();
			listName += " = "+quote(d.getListConstantValue());
			appendStatement(listName);
			
			String idHolderName = "public static final String ";
			idHolderName += d.getIdHolderName();
			idHolderName += " = IDHolder.DOC_ID_HOLDER_PRE+"+quote(d.getName().toLowerCase());
			appendStatement(idHolderName);
			
			
		} 
		
		return ret;
	}
	
	private void generateDocumentRelatedCode(MetaDocument doc){
		appendString("@SuppressWarnings(\"unchecked\")");
		appendString("private DocumentList<"+DocumentGenerator.getDocumentName(doc)+"> _get"+doc.getMultiple()+"(){");
		increaseIdent();
		appendString("try{");
		increaseIdent();
		appendStatement("return getList("+doc.getListName()+")");
		decreaseIdent();
		appendString("}catch(NoSuchDocumentListException e){");
		increaseIdent();
		appendStatement("return new DocumentList<"+DocumentGenerator.getDocumentName(doc)+">("+doc.getListName()+")");
		append(closeBlock());
		append(closeBlock());
		emptyline();
		
		appendString("private void _update"+doc.getMultiple()+"(DocumentList<"+DocumentGenerator.getDocumentName(doc)+"> list){");
		increaseIdent();
		appendString("putList(list);");
		append(closeBlock());
		emptyline();

		appendString("public List<"+DocumentGenerator.getDocumentName(doc)+"> get"+doc.getMultiple()+"(){");
		increaseIdent();
		appendString("return _get"+doc.getMultiple()+"().getList();");
		append(closeBlock());
		emptyline();

		appendString("public "+DocumentGenerator.getDocumentName(doc)+" get"+doc.getName()+"(String id){");
		increaseIdent();
		appendStatement("return _get"+doc.getMultiple()+"().getDocumentById(id)");
		append(closeBlock());
		emptyline();
		
		appendString("public void update"+doc.getName()+"("+DocumentGenerator.getDocumentName(doc)+" "+doc.getVariableName()+"){");
		increaseIdent();
		appendString("DocumentList<"+DocumentGenerator.getDocumentName(doc)+"> "+doc.getMultiple().toLowerCase()+" = _get"+doc.getMultiple()+"();");
		appendString(doc.getMultiple().toLowerCase()+".removeDocumentById("+doc.getVariableName()+".getId());");
		appendStatement(doc.getVariableName()+".setLastUpdateNow()");
		appendStatement(doc.getVariableName()+".setCallContextAuthor()");
		appendString(doc.getMultiple().toLowerCase()+".addDocument("+doc.getVariableName()+");");
		appendString("_update"+doc.getMultiple()+"("+doc.getMultiple().toLowerCase()+");");
		append(closeBlock());
		emptyline();
		
		appendString("public void delete"+doc.getName()+"(String id){");
		increaseIdent();
		appendStatement("DocumentList<"+DocumentGenerator.getDocumentName(doc)+"> entries = _get"+doc.getMultiple()+"()");
		appendStatement("entries.removeDocumentById(id)");
		appendStatement("_update"+doc.getMultiple()+"(entries)");
		append(closeBlock());
		emptyline();
		
		appendString("public "+DocumentGenerator.getDocumentName(doc)+" create"+doc.getName()+"("+DocumentGenerator.getDocumentName(doc)+" "+doc.getVariableName()+" ){");
		increaseIdent();
		appendStatement("IDHolder idh = _getIdHolder("+doc.getIdHolderName()+")");
		appendStatement("int id = idh.getNextIdInt()");
		appendStatement(doc.getVariableName()+".renameTo(\"\"+id)");
		appendStatement("putDocument(idh)");
		emptyline();
		appendStatement("DocumentList<"+DocumentGenerator.getDocumentName(doc)+"> entries = _get"+doc.getMultiple()+"()");
		appendStatement(doc.getVariableName()+".setLastUpdateNow()");
		appendStatement(doc.getVariableName()+".setCallContextAuthor()");
		appendStatement("entries.addDocument("+doc.getVariableName()+")");
		appendStatement("_update"+doc.getMultiple()+"(entries)");
		appendStatement("return "+doc.getVariableName());
		append(closeBlock());
		emptyline();

		appendString("public "+DocumentGenerator.getDocumentName(doc)+" import"+doc.getName()+"("+DocumentGenerator.getDocumentName(doc)+" "+doc.getVariableName()+" ){");
		increaseIdent();
		appendStatement("IDHolder idh = _getIdHolder("+doc.getIdHolderName()+")");
		appendStatement("idh.adjustTill("+doc.getVariableName()+".getId())");
		appendStatement("putDocument(idh)");
		emptyline();
		appendStatement("DocumentList<"+DocumentGenerator.getDocumentName(doc)+"> entries = _get"+doc.getMultiple()+"()");
		appendStatement(doc.getVariableName()+".setLastUpdateNow()");
		appendStatement(doc.getVariableName()+".setCallContextAuthor()");
		appendStatement("entries.addDocument("+doc.getVariableName()+")");
		appendStatement("_update"+doc.getMultiple()+"(entries)");
		appendStatement("return "+doc.getVariableName());
		append(closeBlock());
		emptyline();
	}
}
