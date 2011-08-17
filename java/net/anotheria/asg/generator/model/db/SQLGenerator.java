package net.anotheria.asg.generator.model.db;

import net.anotheria.asg.generator.*;
import net.anotheria.asg.generator.meta.*;

import java.util.ArrayList;
import java.util.List;

public class SQLGenerator extends AbstractGenerator implements IGenerator{
	
	public List<FileEntry> generate(List<MetaModule>  modules){
		ArrayList<FileEntry> ret = new ArrayList<FileEntry>();
		ArrayList<MetaDocument> documents = new ArrayList<MetaDocument>();
		for (MetaModule m : modules){
			if (m.getStorageType().equals(StorageType.DB)){
				ret.addAll(generate(m));
				documents.addAll(m.getDocuments());
			}
		}

		if (documents.size()>0)
			ret.addAll(generateAllScripts(documents));
		
		return ret;
	}
	
	private List<FileEntry> generateAllScripts(List<MetaDocument> documents){
		List<FileEntry> entries = new ArrayList<FileEntry>();
		
		GeneratedSQLFile allCreate = new GeneratedSQLFile("create_all");
		GeneratedSQLFile allDelete = new GeneratedSQLFile("delete_all");

		MetaProperty daoCreated = new MetaProperty("dao_created", MetaProperty.Type.LONG);
	    MetaProperty daoUpdated = new MetaProperty("dao_updated", MetaProperty.Type.LONG);

		String tableNames = "";
		for (MetaDocument doc : documents){
			GenerationJobManager.getCurrentJob().setBuilder(allCreate.getBody());
			generateSQLCreate(doc, daoCreated, daoUpdated);
			emptyline();
			
			if (tableNames.length()>0)
				tableNames += ",";
			tableNames += getSQLTableName(doc);
		
			GenerationJobManager.getCurrentJob().setBuilder(allDelete.getBody());
			generateSQLDelete(doc);
			emptyline();
		}
		
		
		GenerationJobManager.getCurrentJob().setBuilder(allCreate.getBody());
		appendString("GRANT ALL ON "+tableNames+" TO "+GeneratorDataRegistry.getInstance().getContext().getOwner()+" ; ");
		
		entries.add(new FileEntry(allCreate));
		entries.add(new FileEntry(allDelete));
		return entries;
	}
	
	public List<FileEntry> generate(IGenerateable gmodule){
		
		MetaModule mod = (MetaModule)gmodule;
		
		List<FileEntry> ret = new ArrayList<FileEntry>();
		
		List<MetaDocument> documents = mod.getDocuments();
		for (MetaDocument d: documents){
			ret.add(new FileEntry(generateDocumentCreate(d)));
		}
		
		return ret;
	}
	
	public String getCreateScriptName(MetaDocument doc){
		return "create_"+doc.getParentModule().getName().toLowerCase()+"_"+doc.getName().toLowerCase();
	}
	
	private GeneratedSQLFile generateDocumentCreate(MetaDocument doc){
		GeneratedSQLFile file = new GeneratedSQLFile(getCreateScriptName(doc));
		startNewJob(file);
		
		
	    MetaProperty daoCreated = new MetaProperty("dao_created", MetaProperty.Type.LONG);
	    MetaProperty daoUpdated = new MetaProperty("dao_updated", MetaProperty.Type.LONG);

		generateSQLCreate(doc, daoCreated, daoUpdated);
		
		return file;
	}
	
	private void generateSQLDelete(MetaDocument doc){
		appendString("DROP TABLE "+getSQLTableName(doc)+";");
	}
	
	private void generateSQLCreate(MetaDocument doc, MetaProperty... additionalProps){
		appendString("CREATE TABLE "+getSQLTableName(doc)+"(");
		increaseIdent();
		appendString("id int8 PRIMARY KEY,");
		for (int i=0; i<doc.getProperties().size(); i++){
			appendString(getSQLPropertyDefinition(doc.getProperties().get(i))+",");
		}
		for (int i=0; i<doc.getLinks().size(); i++){
			appendString(getSQLPropertyDefinition(doc.getLinks().get(i))+",");
		}
		for (int i=0; i<additionalProps.length-1; i++)
			appendString(getSQLPropertyDefinition(additionalProps[i])+",");
		appendString(getSQLPropertyDefinition(additionalProps[additionalProps.length-1]));
		
		decreaseIdent();
		appendString(");");
	}
	
	private String getSQLPropertyDefinition(MetaProperty p){
		return getAttributeName(p)+" "+getSQLPropertyType(p);
	}
	
	/**
	 * This method maps MetaProperties Types to SQL DataTypes.
	 * @param p
	 * @return
	 */
	private String getSQLPropertyType(MetaProperty p){
		
		switch (p.getType()) {
		case STRING:
			return "varchar";
		case TEXT:
			return "varchar";
		case LONG:
			return "int8";
		case INT:
			return "int";
		case DOUBLE:
			return "double precision";
		case FLOAT:
			return "float4";
		case BOOLEAN:
			return "boolean";
		case LIST:
			return getSQLPropertyType(((MetaListProperty)p).getContainedProperty()) + "[]";
		default:
			return "UNKNOWN!";
		}
	}

	private String getSQLTableName(MetaDocument doc){
		return doc.getName().toLowerCase();
	}
	
	private String getAttributeName(MetaProperty p){
		return p.getName().toLowerCase();
	}

}
