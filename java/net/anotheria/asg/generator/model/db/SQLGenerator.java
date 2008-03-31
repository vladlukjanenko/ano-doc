package net.anotheria.asg.generator.model.db;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaListProperty;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.meta.MetaProperty;
import net.anotheria.asg.generator.meta.StorageType;

public class SQLGenerator extends AbstractGenerator implements IGenerator{
	
	private Context context;
	
	public List<FileEntry> generate(List<MetaModule>  modules, Context context){
		ArrayList<FileEntry> ret = new ArrayList<FileEntry>();
		ArrayList<MetaDocument> documents = new ArrayList<MetaDocument>();
		for (MetaModule m : modules){
			if (m.getStorageType().equals(StorageType.DB)){
				ret.addAll(generate(m, context));
				documents.addAll(m.getDocuments());
			}
		}

		if (documents.size()>0)
			ret.addAll(generateAllScripts(documents));
		
		return ret;
	}
	
	private List<FileEntry> generateAllScripts(List<MetaDocument> documents){
		List<FileEntry> entries = new ArrayList<FileEntry>();

		String allCreate = "";
		String allDelete = "";
		
		MetaProperty dao_created = new MetaProperty("dao_created", "long");
	    MetaProperty dao_updated = new MetaProperty("dao_updated", "long");

		String tableNames = "";
		for (MetaDocument doc : documents){
			allCreate += generateSQLCreate(doc, dao_created, dao_updated);
			allCreate += emptyline();
			
			if (tableNames.length()>0)
				tableNames += ",";
			tableNames += getSQLTableName(doc);
		
			allDelete += generateSQLDelete(doc);
			allDelete += emptyline();
		}
		
		allCreate += writeString("GRANT ALL ON "+tableNames+" TO "+GeneratorDataRegistry.getInstance().getContext().getOwner()+" ; ");
		
		entries.add(new FileEntry("sql", "create_all", allCreate, ".sql"));
		entries.add(new FileEntry("sql", "delete_all", allDelete, ".sql"));
		
		

		
		return entries;
	}
	
	public List<FileEntry> generate(IGenerateable gmodule, Context context){
		
		MetaModule mod = (MetaModule)gmodule;
		
		this.context = context;
		
		List<FileEntry> ret = new ArrayList<FileEntry>();
		
		List<MetaDocument> documents = mod.getDocuments();
		for (MetaDocument d: documents){
			ret.add(new FileEntry("sql", getCreateScriptName(d), generateDocumentCreate(d), ".sql"));
		}
		
		return ret;
	}
	
	public String getCreateScriptName(MetaDocument doc){
		return "create_"+doc.getParentModule().getName().toLowerCase()+"_"+doc.getName().toLowerCase();
	}
	
	private String generateDocumentCreate(MetaDocument doc){
	    MetaProperty dao_created = new MetaProperty("dao_created", "long");
	    MetaProperty dao_updated = new MetaProperty("dao_updated", "long");

		return generateSQLCreate(doc, dao_created, dao_updated);
	}
	
	private String generateSQLDelete(MetaDocument doc){
		return writeString("DROP TABLE "+getSQLTableName(doc)+";");
	}
	
	private String generateSQLCreate(MetaDocument doc, MetaProperty... additionalProps){
		String ret = "";
		ret += writeString("CREATE TABLE "+getSQLTableName(doc)+"(");
		increaseIdent();
		ret += writeString("id int8 PRIMARY KEY,");
		for (int i=0; i<doc.getProperties().size(); i++){
			ret += writeString(getSQLPropertyDefinition(doc.getProperties().get(i))+",");
		}
		for (int i=0; i<doc.getLinks().size(); i++){
			ret += writeString(getSQLPropertyDefinition(doc.getLinks().get(i))+",");
		}
		for (int i=0; i<additionalProps.length-1; i++)
			ret += writeString(getSQLPropertyDefinition(additionalProps[i])+",");
		ret += writeString(getSQLPropertyDefinition(additionalProps[additionalProps.length-1]));
		
		decreaseIdent();
		ret += writeString(");");
		return ret;

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
		if (p.getType().equals("string"))
			return "varchar";
		if (p.getType().equals("text"))
			return "varchar";
		if (p.getType().equals("long"))
			return "int8";
		if (p.getType().equals("int"))
			return "int";
		if (p.getType().equals("double"))
			return "double precision";
		if (p.getType().equals("float"))
			return "float4";
		if (p.getType().equals("boolean"))
			return "boolean";
		if (p instanceof MetaListProperty)
			return getSQLPropertyType(((MetaListProperty)p).getContainedProperty()) + "[]";
		return "UNKNOWN!";
	}

	private String getSQLTableName(MetaDocument doc){
		return doc.getName().toLowerCase();
	}
	
	private String getAttributeName(MetaProperty p){
		return p.getName().toLowerCase();
	}

}
