package net.anotheria.asg.generator.model.db;

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
import net.anotheria.asg.generator.meta.MetaProperty;
import net.anotheria.asg.generator.model.DataFacadeGenerator;
import net.anotheria.util.StringUtils;


public class PersistenceServiceDAOGenerator extends AbstractGenerator implements IGenerator{
	
	private Context context;
	
	public List<FileEntry> generate(IGenerateable gmodule, Context context){
		
		MetaModule mod = (MetaModule)gmodule;
		
		this.context = context;
		
		List<FileEntry> ret = new ArrayList<FileEntry>();
		
		List<MetaDocument> documents = mod.getDocuments();
		for (MetaDocument d: documents){
			ret.add(new FileEntry(FileEntry.package2path(getPackageName()), getExceptionName(d), generateException(d)));
			ret.add(new FileEntry(FileEntry.package2path(getPackageName()), getNoItemExceptionName(d), generateNoItemException(d)));
			ret.add(new FileEntry(FileEntry.package2path(getPackageName()), getDAOName(d), generateDAO(d)));
			ret.add(new FileEntry(FileEntry.package2path(getPackageName()), getRowMapperName(d), generateRowMapper(d)));
		}
//		ret.add(new FileEntry(FileEntry.package2path(packageName), getFactoryName(mod), generateFactory(mod)));
	//	ret.add(new FileEntry(FileEntry.package2path(packageName), getImplementationName(mod), generateImplementation(mod)));
		
		return ret;
	}
	
	private String getPackageName(){
		return context.getPackageName()+".service.persistence";
	}
	
	private String generateException(MetaDocument doc){
		String ret = "";
		
	    ret += CommentGenerator.generateJavaTypeComment(getExceptionName(doc));
	    
	    ret += writeStatement("package "+getPackageName());
	    ret += emptyline();
	    ret += writeImport("net.anotheria.db.dao.DAOException");
	    ret += emptyline();

	    ret += writeString("public class "+getExceptionName(doc)+ " extends DAOException{");
		increaseIdent();
		ret += emptyline();
		ret += writeString("public "+getExceptionName(doc)+"(String message){");
		ret += writeIncreasedStatement("super(message)");
		ret += writeString("}");
		
		ret += emptyline();
		ret += writeString("public "+getExceptionName(doc)+"(){");
		ret += writeIncreasedStatement("super()");
		ret += writeString("}");

		ret += closeBlock();
		return ret; 
	}
	
	private String generateNoItemException(MetaDocument doc){
		String ret = "";
		
	    ret += CommentGenerator.generateJavaTypeComment(getNoItemExceptionName(doc));
	    
	    ret += writeStatement("package "+getPackageName());
	    ret += emptyline();

	    ret += writeString("public class "+ getNoItemExceptionName(doc)+ " extends "+getExceptionName(doc)+"{");
		increaseIdent();
		ret += emptyline();
		ret += writeString("public "+getNoItemExceptionName(doc)+"(String id){");
		ret += writeIncreasedStatement("super("+quote("No item found for id: ")+"+id)");
		ret += writeString("}");
		
		ret += writeString("public "+getNoItemExceptionName(doc)+"(long id){");
		ret += writeIncreasedStatement("this("+quote("")+"+id)");
		ret += writeString("}");

		ret += closeBlock();
		return ret; 
	}
	
	private String getAttributeConst(MetaProperty p){
		return "ATT_NAME_"+p.getName().toUpperCase();
	}

	private String getAttributeName(MetaProperty p){
		return p.getName().toLowerCase();
	}

	private String generateRowMapper(MetaDocument doc){
	    String ret = "";
	    
	    ret += CommentGenerator.generateJavaTypeComment(getRowMapperName(doc));
	    
	    ret += writeStatement("package "+getPackageName());
	    ret += emptyline();
	    
	    ret += writeImport("java.sql.ResultSet");
	    ret += writeImport("java.sql.SQLException");
	    ret += emptyline();

	    ret += writeImport("net.anotheria.db.dao.RowMapper");
	    ret += writeImport("net.anotheria.db.dao.RowMapperException");
	    ret += emptyline();
	    ret += writeImport("org.apache.log4j.Logger");
	    ret += emptyline();
	    
        ret += writeImport(DataFacadeGenerator.getDocumentImport(context, doc));
        ret += writeImport(VOGenerator.getDocumentImport(context, doc));
	    ret += emptyline();

	    ret += writeString("public class "+getRowMapperName(doc)+" extends RowMapper<"+doc.getName()+">{");
	    increaseIdent();
	    ret += emptyline();
		ret += writeStatement("private static Logger log = Logger.getLogger("+getDAOName(doc)+".class)");
	    ret += emptyline();

	    
	    ret += openFun("public "+doc.getName()+" map(ResultSet row) throws RowMapperException");

	    ret += openTry();
	    
	    ret += writeStatement("long id = row.getLong(1)");
	    ret += writeStatement(doc.getName()+" ret = new "+VOGenerator.getDocumentImplName(doc)+"(\"\"+id)");
	    
	    for (int i=0; i<doc.getProperties().size(); i++){
	    	ret += generateProperty2DBMapping(doc.getProperties().get(i), i+2);
	    }
	    
	    ret += generateProperty2DBMappingPrivate(doc, new MetaProperty(VOGenerator.DAO_CREATED, "long"), doc.getProperties().size()+2);
	    ret += generateProperty2DBMappingPrivate(doc, new MetaProperty(VOGenerator.DAO_UPDATED, "long"), doc.getProperties().size()+3);
	    
	    ret += writeStatement("return ret");
	    decreaseIdent();
	    ret += writeString("}catch(SQLException e){");
	    ret += writeIncreasedStatement("log.error(\"map\", e)");
	    ret += writeIncreasedStatement("throw new RowMapperException(e)");
	    ret += writeString("}");
	    
	    ret += closeBlock();

		ret += closeBlock();
	    
	    return ret;
	}
	
	private String generateProperty2DBMapping(MetaProperty p, int position){
		String call = "";
		
		call += "ret.set";
		call += p.getAccesserName();
		call += "(";
		call += "row.";
		call += p.toPropertyGetter();
		call += "("+position+"))";
		
		
		return writeStatement(call);
	}

	private String generateProperty2DBMappingPrivate(MetaDocument doc, MetaProperty p, int position){
		String call = "";
		
		call += "(("+VOGenerator.getDocumentImplName(doc)+")ret).set";
		call += p.getAccesserName();
		call += "(";
		call += "row.";
		call += p.toPropertyGetter();
		call += "("+position+"))";
		
		
		return writeStatement(call);
	}

	private String generateDB2PropertyMapping(String variableName, MetaProperty p, int position){
		String call = "";
		
		call += "ps.";
		call += p.toPropertySetter();
		call += "("+position+", ";
		call += variableName+".";
		call += p.toGetter();
		call += "())";
		
		
		return writeStatement(call);
	}
	
	private String generateDB2PropertyCallMapping(String variableName, MetaProperty p, String position){
		String call = "";
		
		call += "ps.";
		call += p.toPropertySetter();
		call += "("+position+", ";
		call += "("+p.toJavaObjectType()+")" + variableName;
		call += ")";
		
		
		return call;
	}

	private String generateDAO(MetaDocument doc){
	    String ret = "";
	    
	    ret += CommentGenerator.generateJavaTypeComment(getDAOName(doc));

	    
	    
	    ret += writeStatement("package "+getPackageName());
	    ret += emptyline();
	    ret += writeImport("java.util.List");
	    ret += writeImport("java.util.ArrayList");
	    ret += writeImport("java.util.concurrent.atomic.AtomicLong");
	    ret += emptyline();
	    
	    
        ret += writeImport(DataFacadeGenerator.getDocumentImport(context, doc));
        ret += writeImport(VOGenerator.getDocumentImport(context, doc));
        ret += writeImport("net.anotheria.db.dao.DAO");
        ret += writeImport("net.anotheria.db.dao.DAOException");
        ret += writeImport("net.anotheria.db.dao.DAOSQLException");
        ret += writeImport("net.anotheria.db.dao.RowMapper");
	    ret += emptyline(); 
	    ret += writeImport("net.anotheria.anodoc.query2.QueryProperty");
	    ret += emptyline();

	    ret += writeImport("java.sql.Connection");
	    ret += writeImport("java.sql.PreparedStatement");
	    ret += writeImport("java.sql.ResultSet");
	    ret += writeImport("java.sql.SQLException");
	    ret += writeImport("java.sql.Statement");
	    ret += emptyline();
	    
	    ret += writeImport("org.apache.log4j.Logger");
	    ret += emptyline();

	    ret += writeString("public class "+getDAOName(doc)+" implements DAO{");
	    increaseIdent();
	    ret += emptyline();
		ret += writeStatement("private static Logger log = Logger.getLogger("+getDAOName(doc)+".class)");
	    
	    //first define constants.
	    String constDecl = "public static final String ";
	    ret += writeStatement(constDecl+"TABNAME = "+quote(getSQLTableName(doc)));
	    ret += emptyline();
	    MetaProperty id = new MetaProperty("id", "string");
	    MetaProperty dao_created = new MetaProperty("dao_created", "long");
	    MetaProperty dao_updated = new MetaProperty("dao_updated", "long");
	    List<MetaProperty> properties = doc.getProperties();
	    ret += writeStatement(constDecl+getAttributeConst(id)+" = "+quote(getAttributeName(id)));
	    for (MetaProperty p : properties){
		    ret += writeStatement(constDecl+getAttributeConst(p)+" \t = "+quote(getAttributeName(p)));
	    }
	    
	    ret += emptyline();
	    //create sql staments
	    //SQL-CREATE
	    String sqlCreate = quote("INSERT INTO ")+" + TABNAME +"+quote(" (");
	    sqlCreate += "+"+getAttributeConst(id);
	    for (MetaProperty p : properties){
	    	sqlCreate += "+"+quote(", ")+"+"+getAttributeConst(p);
	    }
	    sqlCreate += "+"+quote(", ")+"+"+getAttributeConst(dao_created);
	    String sqlCreateEnd = ") VALUES (";
	    //+2 because of created flag and id.
	    for (int i=0; i<properties.size()+2; i++){
	    	sqlCreateEnd+="?";
	    	if (i<properties.size()+1)
	    		sqlCreateEnd+=",";
	    }
	    sqlCreateEnd += ")";
	    sqlCreate += "+"+quote(sqlCreateEnd);
	    ret += writeStatement(constDecl+" SQL_CREATE \t\t= "+sqlCreate);
	    
	    //SQL-UPDATE
	    String sqlUpdate = quote("UPDATE ")+" + TABNAME +"+quote(" SET ");
	    for (MetaProperty p : properties){
	    	sqlUpdate += " + "+getAttributeConst(p)+" + "+quote(" = ?, ");
	    }
    	sqlUpdate += " + "+getAttributeConst(dao_updated)+" + "+quote(" = ?");
	    sqlUpdate += " + "+quote(" WHERE ")+" + TABNAME +"+quote(".")+" + "+getAttributeConst(id)+" + "+quote(" = ?");

    	ret += writeStatement(constDecl+" SQL_UPDATE \t\t= "+sqlUpdate);
	    
	    //SQL-DELETE
	    String sqlDelete = quote("DELETE FROM ")+" + TABNAME +";
	    sqlDelete += quote(" WHERE ")+" + TABNAME +"+quote(".")+" + "+getAttributeConst(id)+" + "+quote(" = ?");
	    ret += writeStatement(constDecl + " SQL_DELETE \t\t= "+sqlDelete);

	    //SQL-READ-ONE
	    String sqlReadOne = quote("SELECT * FROM ")+" + TABNAME +";
	    sqlReadOne += quote(" WHERE ")+" + TABNAME +"+quote(".")+" + "+getAttributeConst(id)+" + "+quote(" = ?");
	    ret += writeStatement(constDecl + " SQL_READ_ONE \t= "+sqlReadOne);

	    //SQL-READ-ALL
	    String sqlReadAll = quote("SELECT * FROM ")+" + TABNAME ";
	    ret += writeStatement(constDecl + " SQL_READ_ALL \t= "+sqlReadAll);
	    
	    //SQL-READ-ALL
	    String sqlReadAllByProperty = quote("SELECT * FROM ")+" + TABNAME +"+quote(" WHERE ");
	    ret += writeStatement(constDecl + " SQL_READ_ALL_BY_PROPERTY \t= "+sqlReadAllByProperty);

	    ret += emptyline();
	    ret += writeStatement("private AtomicLong lastId = new AtomicLong()");
	    ret += writeStatement("private static final long START_ID = 0");
	    ret += emptyline();
	    ret += writeStatement("private RowMapper<"+doc.getName()+"> rowMapper = new "+doc.getName()+"RowMapper()");
	    
	    ret += emptyline();
	    //create impl
	    
	    
	    String throwsClause = " throws DAOException";
	    String callLog = "";
	    
        //get all XYZ method
        callLog = quote("get"+doc.getMultiple()+"(")+"+con+"+quote(")");
        ret += writeComment("Returns all "+doc.getMultiple()+" objects stored.");
        ret += openFun("public List<"+doc.getName()+">"+" get"+doc.getMultiple()+"(Connection con)"+throwsClause);
        ret += generateFunctionStart("SQL_READ_ALL", callLog, true);
        ret += writeStatement("ResultSet result = ps.executeQuery()");
        ret += writeStatement("ArrayList<"+doc.getName()+"> ret = new ArrayList<"+doc.getName()+">()");
        ret += writeString("while(result.next())");
		ret += writeIncreasedStatement("ret.add(rowMapper.map(result))");
		ret += writeStatement("return  ret");
        ret += generateFunctionEnd(callLog, true);
        ret += closeBlock();
        ret += emptyline();
        
        ret += writeComment("Deletes a "+doc.getName()+" object by id.");
        callLog = quote("delete"+doc.getName()+"(")+"+con+"+quote(", ")+"+id+"+quote(")");
        
        ret += openFun("public void delete"+doc.getName()+"(Connection con, String id)"+throwsClause);
        ret += generateFunctionStart("SQL_DELETE", callLog, true);
        ret += writeStatement("ps.setLong(1, Long.parseLong(id))");
        ret += writeStatement("int rows = ps.executeUpdate()");
        ret += writeString("if (rows!=1 && rows!=0){");
        increaseIdent();
        ret += writeStatement("log.warn(\"Deleted more than one row of "+doc.getName()+": \"+id)");
		ret += closeBlock();
		ret += generateFunctionEnd(callLog, true);
        ret += closeBlock();
        ret += emptyline();
        
        //getXYZ method
        callLog = quote("get"+doc.getName()+"(")+"+con+"+quote(", ")+"+id+"+quote(")");
        ret += writeComment("Returns the "+doc.getName()+" object with the specified id.");
        ret += openFun("public "+doc.getName()+" get"+doc.getName()+"(Connection con, String id)"+throwsClause);
        ret += generateFunctionStart("SQL_READ_ONE", callLog, true);
        ret += writeStatement("ps.setLong(1, Long.parseLong(id))");
        ret += writeStatement("ResultSet result = ps.executeQuery()");
        ret += writeString("if (!result.next())");
        ret += writeIncreasedStatement("throw new "+getNoItemExceptionName(doc)+"(id)");
		ret += writeStatement("return rowMapper.map(result)");
        ret += generateFunctionEnd(callLog, true);
        ret += closeBlock();
        ret += emptyline();
        
        int ii = 0;
        
        //createXYZ method
        callLog = quote("create"+doc.getName()+"(")+"+con+"+quote(", ")+"+"+doc.getVariableName()+"+"+quote(")");
        ret += writeComment("Creates a new "+doc.getName()+" object.\nReturns the created version.");
        ret += openFun("public "+doc.getName()+" create"+doc.getName()+"(Connection con, "+doc.getName()+" "+doc.getVariableName()+")"+throwsClause);
        ret += generateFunctionStart("SQL_CREATE", callLog, true);
        ret += writeStatement("long nextId = lastId.incrementAndGet()");
        ret += writeStatement("ps.setLong(1, nextId)");
        for (int i=0; i<doc.getProperties().size(); i++){
        	ret += generateDB2PropertyMapping(doc.getVariableName(), doc.getProperties().get(i), i+2);
        	ii = i +2;
        }
        ret += writeCommentLine("set create timestamp");
        ret += writeStatement("ps.setLong("+(ii+1)+", System.currentTimeMillis())");

        ret += writeStatement("int rows = ps.executeUpdate()");
        ret += writeString("if (rows!=1)");
        ret += writeIncreasedStatement("throw new DAOException(\"Create failed, updated rows: \"+rows)");
        
        String copyResVarName = "new"+StringUtils.capitalize(doc.getVariableName());
        String createCopyCall = VOGenerator.getDocumentImplName(doc)+" "+copyResVarName + " = new "+VOGenerator.getDocumentImplName(doc);
        createCopyCall += "(\"\"+nextId)";
        ret += writeStatement(createCopyCall);
        ret += writeStatement(copyResVarName+".copyAttributesFrom("+doc.getVariableName()+")");
        
        ret += writeStatement("return "+copyResVarName);
        ret += generateFunctionEnd(callLog, true);
        
        ret += closeBlock();
        ret += emptyline();

        
        //updateXYZ method
        callLog = quote("update"+doc.getName()+"(")+"+con+"+quote(", ")+"+"+doc.getVariableName()+"+"+quote(")");
        ret += writeComment("Updates a "+doc.getName()+" object.\nReturns the updated version.");
        ret += openFun("public "+doc.getName()+" update"+doc.getName()+"(Connection con, "+doc.getName()+" "+doc.getVariableName()+")"+throwsClause);
        ret += generateFunctionStart("SQL_UPDATE", callLog, true);

        for (int i=0; i<doc.getProperties().size(); i++){
        	ret += generateDB2PropertyMapping(doc.getVariableName(), doc.getProperties().get(i), i+1);
        	ii = i+1;
        }
        ret += writeCommentLine("set update timestamp");
        ret += writeStatement("ps.setLong("+(ii+1)+", System.currentTimeMillis())");
        ret += writeCommentLine("set id for the where clause");
        ret += writeStatement("ps.setLong("+(ii+2)+", Long.parseLong("+doc.getVariableName()+".getId()))");

        ret += writeStatement("int rows = ps.executeUpdate()");
        ret += writeString("if (rows!=1)");
        ret += writeIncreasedStatement("throw new DAOException(\"Update failed, updated rows: \"+rows)");

        ret += writeStatement("return "+doc.getVariableName());
        ret += generateFunctionEnd(callLog, true);
        ret += closeBlock();
        ret += emptyline();
        
        
        //get all XYZ byProperty method
        callLog = quote("get"+doc.getMultiple()+"ByProperty(")+"+con+"+quote(",")+"+ properties+"+quote(")");
        ret += writeComment("Returns all "+doc.getMultiple()+" objects stored which matches given properties.");
        ret += openFun("public List<"+doc.getName()+">"+" get"+doc.getMultiple()+"ByProperty(Connection con, List<QueryProperty> properties)"+throwsClause);
        //ret += generateFunctionStart("SQL_READ_ALL_BY_PROPERTY", callLog, true);
		ret += writeStatement("PreparedStatement ps = null");
		ret += openTry();
		//TODO Caching fuer generierte SQL Statements
        ret += writeCommentLine("//enable caching of statements one day");
        ret += writeStatement("String SQL = SQL_READ_ALL_BY_PROPERTY");
        ret += writeStatement("String whereClause = "+quote(""));
        ret += writeString("for (QueryProperty p : properties){");
        increaseIdent();
        ret += writeString("if (whereClause.length()>0)");
        ret += writeIncreasedStatement("whereClause += "+quote(" AND "));
        ret += writeStatement("whereClause += p.getName()+"+quote("=?"));
        ret +=closeBlock();
        ret += writeStatement("SQL += whereClause");
        //ret += writeStatement("System.out.println(SQL)");
        ret += writeStatement("ps = con.prepareStatement(SQL)");
        //set properties
        ret += writeString("for (int i=0; i<properties.size(); i++){");
        increaseIdent();
        ret += writeStatement("setProperty(i+1, ps, properties.get(i))");
        ret +=closeBlock();
        
        
        ret += writeStatement("ResultSet result = ps.executeQuery()");
        ret += writeStatement("ArrayList<"+doc.getName()+"> ret = new ArrayList<"+doc.getName()+">()");
        ret += writeString("while(result.next())");
		ret += writeIncreasedStatement("ret.add(rowMapper.map(result))");
		ret += writeStatement("return  ret");
        ret += generateFunctionEnd(callLog, true);
        ret += closeBlock();
        ret += emptyline();
        
        //setProperty
        ret += openFun("private void setProperty(int position, PreparedStatement ps, QueryProperty property) throws SQLException");
        for (MetaProperty p : properties){
        	ret += writeString("if ("+getAttributeConst(p)+".equals(property.getName())){");
        	increaseIdent();
        	ret += writeStatement(generateDB2PropertyCallMapping("property.getValue()", p, "position"));
        	ret += writeStatement("return");
        	ret += closeBlock();
        }
    	ret += writeString("if ("+getAttributeConst(id)+".equals(property.getName())){");
    	increaseIdent();
    	ret += writeStatement(generateDB2PropertyCallMapping("property.getValue()", id, "position"));
    	ret += writeStatement("return");
    	ret += closeBlock();
    	ret += writeString("if ("+quote(dao_created.getName())+".equals(property.getName())){");
    	increaseIdent();
    	ret += writeStatement(generateDB2PropertyCallMapping("property.getValue()", dao_created, "position"));
    	ret += writeStatement("return");
    	ret += closeBlock();
    	ret += writeString("if ("+quote(dao_updated.getName())+".equals(property.getName())){");
    	increaseIdent();
    	ret += writeStatement(generateDB2PropertyCallMapping("property.getValue()", dao_updated, "position"));
    	ret += writeStatement("return");
    	ret += closeBlock();

        ret += closeBlock(); //end setProperty
        
       //special functions
//	        ret += writeComment("Returns all "+doc.getName()+" objects, where property with given name equals object.");
//	        ret += writeStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value)");
//	        ret += emptyline();
//			ret += writeComment("Returns all "+doc.getName()+" objects, where property with given name equals object, sorted");
//			ret += writeStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value, SortType sortType)");
//			ret += emptyline();
//			ret += writeComment("Executes a query");
//			ret += writeStatement("public QueryResult executeQueryOn"+doc.getMultiple()+"(DocumentQuery query)");
//			ret += emptyline();
	    
	    
//		ret += writeComment("creates an xml element with all contained data.");
//		ret += writeStatement("public Element exportToXML()");
//		ret += emptyline();
        
        ret += writeString("/* ---------- SQL --------- ");
        ret += generateSQLCreate(doc, dao_created, dao_updated);
        ret += writeString("   ---------- SQL --------- */");
        
        ret += openFun("public void createStructure(Connection connection) "+throwsClause);
        ret += writeCommentLine("not implemented");
        ret += closeBlock();
        ret += emptyline();
        		
        ret += writeString("/* ---------- SQL --------- ");
        ret += generateSQLDelete(doc);
        ret += writeString("   ---------- SQL --------- */");
        ret += openFun("public void deleteStructure(Connection connection) "+throwsClause);
        ret += writeCommentLine("not implemented");
        ret += closeBlock();
        ret += emptyline();
        
        ret += openFun("protected void finish(Statement st)");
        ret += closeBlock();

        //init() method
        ret += openFun("public void init(Connection con) "+throwsClause);
        ret += writeStatement("log.debug(\"Called: init(\"+con+\")\")");
        ret += writeStatement("Statement st = null");
        ret += openTry();
        ret += writeStatement("con.setAutoCommit(true)");
        ret += writeStatement("st = con.createStatement()");
        ret += writeStatement("st.execute(\"SELECT MAX(\"+"+getAttributeConst(id)+"+\") FROM \"+TABNAME)");
        ret += writeStatement("ResultSet set = st.getResultSet()");
    	ret += writeStatement("long maxId = 0");
    	ret += writeString("if (set.next())");
    	ret += writeIncreasedStatement("maxId = set.getLong(1)");
    	ret += writeStatement("lastId = new AtomicLong(maxId == 0 ? START_ID : maxId)");
    	ret += writeStatement("log.info(\"lastId is \"+lastId)");
    	ret += writeStatement("set.close()");
    	ret += writeStatement("st.close()");
    	ret += generateFunctionEnd(quote("init(")+"+con+"+quote(")"), false);
    	ret += closeBlock();
    	
	    ret += closeBlock();
	    return ret;
	}
	
	private String generateSQLDelete(MetaDocument doc){
		return writeString("DROP TABLE "+getSQLTableName(doc));
	}
	
	private String generateSQLCreate(MetaDocument doc, MetaProperty... additionalProps){
		String ret = "";
		ret += writeString("CREATE TABLE "+getSQLTableName(doc)+"(");
		ret += writeString("id int8 PRIMARY KEY,");
		for (int i=0; i<doc.getProperties().size(); i++){
			ret += writeString(getSQLPropertyDefinition(doc.getProperties().get(i))+",");
		}
		for (int i=0; i<additionalProps.length-1; i++)
			ret += writeString(getSQLPropertyDefinition(additionalProps[i])+",");
		ret += writeString(getSQLPropertyDefinition(additionalProps[additionalProps.length-1]));
		
		ret += writeString(")");
		return ret;

	}
	
	private String getSQLPropertyDefinition(MetaProperty p){
		return getAttributeName(p)+" "+getSQLPropertyType(p);
	}
	
	private String getSQLPropertyType(MetaProperty p){
		if (p.getType().equals("string"))
			return "varchar";
		if (p.getType().equals("long"))
			return "int8";
		if (p.getType().equals("int"))
			return "int";
		if (p.getType().equals("double"))
			return "double precision";
		if (p.getType().equals("boolean"))
			return "boolean";
		return "UNKNOWN!";
	}
/*	iid integer PRIMARY KEY,
    secret char(10) NOT NULL,
    pb_card char(16),
    pb_pin char(4)
	*/
	private String getSQLTableName(MetaDocument doc){
		return doc.getName().toLowerCase();
	}
	
	private String generateFunctionStart(String SQL_STATEMENT, String callLog, boolean usePreparedSt){
		String ret = "";
		if (usePreparedSt){
			ret += writeStatement("PreparedStatement ps = null");
			ret += openTry();
			ret += writeStatement("con.setAutoCommit(true)");
			ret += writeStatement("ps = con.prepareStatement("+SQL_STATEMENT+")");
		}else{
			ret += writeStatement("Statement st = null");
			ret += openTry();
			ret += writeStatement("con.setAutoCommit(true)");
			//ret += writeStatement("ps = con.prepareStatement("+SQL_STATEMENT+")");
		}
		return ret;
	}
	
	private String generateFunctionEnd(String callLog, boolean usePreparedSt){
		String ret = "";
		decreaseIdent();
		ret += writeString("}catch(SQLException e){");
		increaseIdent();
		ret += writeStatement("log.error("+callLog+", e)");
		ret += writeStatement("throw new DAOSQLException(e)");
		decreaseIdent();
		ret += writeString("}finally{");
		increaseIdent();
		ret += writeStatement("finish("+(usePreparedSt ? "ps": "st")+")");
		ret += closeBlock();
		return ret;
	}
	
	
	private String getModuleGetterMethod(MetaModule module){
	    return "_get"+module.getModuleClassName();	    
	}
	
	private String getModuleGetterCall(MetaModule module){
	    return getModuleGetterMethod(module)+"()";
	}
	
/*
	private String generateFactory(MetaModule module){
	    String ret = "";

		ret += CommentGenerator.generateJavaTypeComment(getFactoryName(module),"The factory for the "+getInterfaceName(module)+" implementation.");

	    ret += writeStatement("package "+getPackageName());
	    ret += emptyline();
	    
	    ret += writeString("public class "+getFactoryName(module)+"{");
	    increaseIdent();

	    ret += writeString("public static "+getInterfaceName(module)+" create"+getServiceName(module)+"(){");
	    increaseIdent();
	    ret += writeString("return "+getImplementationName(module)+".getInstance();");
	    ret += closeBlock();
	    
	    ret += closeBlock();
	    return ret;
	}
*/
	
	public static final String getExceptionName(MetaDocument doc){
		return getDAOName(doc)+"Exception";
	}
	
	public static final String getNoItemExceptionName(MetaDocument doc){
		return getDAOName(doc)+"NoItemForIdFoundException";
	}

	/*	public static String getInterfaceName(MetaModule m){
	    return "I"+getServiceName(m);
	}
	*/
	public static String getDAOName(MetaDocument doc){
	    return doc.getName()+"DAO";
	}

	public static String getRowMapperName(MetaDocument doc){
	    return doc.getName()+"RowMapper";
	}

	public static String getPackageName(Context context){
	    return context.getPackageName()+".service";
	}
}
