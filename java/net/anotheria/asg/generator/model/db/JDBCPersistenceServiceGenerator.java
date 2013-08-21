package net.anotheria.asg.generator.model.db;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.CommentGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratedClass;
import net.anotheria.asg.generator.GenerationOptions;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.TypeOfClass;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.model.DataFacadeGenerator;

import java.util.ArrayList;
import java.util.List;


public class JDBCPersistenceServiceGenerator extends AbstractGenerator implements IGenerator{
	
	public List<FileEntry> generate(IGenerateable gmodule){
		
		MetaModule mod = (MetaModule)gmodule;
		
		List<FileEntry> ret = new ArrayList<FileEntry>();
		
		List<GeneratedClass> itemNotFoundExceptions = generateItemNotFoundExceptions(mod);
		for (GeneratedClass anExceptionClass : itemNotFoundExceptions)
			ret.add(new FileEntry(anExceptionClass));
		ret.add(new FileEntry(generateException(mod)));
		ret.add(new FileEntry(generateInterface(mod)));
		ret.add(new FileEntry(generateFactory(mod)));
		ret.add(new FileEntry(generateImplementation(mod)));
		
		return ret;
	}
	
	private String getPackageName(MetaModule m){
		return getPackageName(GeneratorDataRegistry.getInstance().getContext(), m);
	}
	
	private GeneratedClass generateException(MetaModule module){

		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getInterfaceName(module), this));
	    
	    clazz.setPackageName(getPackageName(module));
	    clazz.addImport("java.sql.SQLException");
	    clazz.addImport("net.anotheria.db.dao.DAOException");
	    
	    clazz.setName(getExceptionName(module));
	    clazz.setParent("Exception");

	    startClassBody();
	    appendString("public "+getExceptionName(module)+"(String message){");
		appendIncreasedStatement("super(message)");
		appendString("}");
		
		appendString("public "+getExceptionName(module)+"(SQLException e){");
		appendIncreasedStatement("super("+quote("Undelying DB Error: ")+"+e.getMessage())");
		appendString("}");

		appendString("public "+getExceptionName(module)+"(DAOException e){");
		appendIncreasedStatement("super("+quote("Undelying DAO Error: ")+"+e.getMessage())");
		appendString("}");
		
		return clazz;
		
	}
	
	public static final String getItemNotFoundExceptionImport(Context c, MetaDocument doc, MetaModule m){
		return getPackageName(c, m)+"."+getItemNotFoundExceptionName(doc,m);
	}
	
	public static final String getItemNotFoundExceptionName(MetaDocument doc, MetaModule module){
		return doc.getName()+"NotFoundIn"+getExceptionName(module);
	}
	
	private List<GeneratedClass> generateItemNotFoundExceptions(MetaModule module){

		ArrayList<GeneratedClass> ret = new ArrayList<GeneratedClass>();
		
		for (MetaDocument doc : module.getDocuments()){
		
			GeneratedClass clazz = new GeneratedClass();
			startNewJob(clazz);
			
			clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getInterfaceName(module), this));
		    
		    clazz.setPackageName(getPackageName(module));
		    
		    clazz.setName(getItemNotFoundExceptionName(doc, module));
		    clazz.setParent(getExceptionName(module));
	
		    startClassBody();
		    appendString("public "+getItemNotFoundExceptionName(doc, module)+"(String id){");
			appendIncreasedStatement("super("+quote("No item with id ")+"+id+"+quote(" found.")+")");
			appendString("}");
			ret.add(clazz);
		}
		
		return ret;
		
	}

	private GeneratedClass generateInterface(MetaModule module){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
	    
		appendGenerationPoint("generateInterface");
		
	    clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getInterfaceName(module), this));
 
	    clazz.setPackageName(getPackageName(module));
	    clazz.addImport("java.util.List");
	    clazz.addImport(("net.anotheria.anodoc.query2.QueryProperty"));
	    clazz.addImport("net.anotheria.util.slicer.Segment");
	    
	    List<MetaDocument> docs = module.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = docs.get(i);
	        clazz.addImport((DataFacadeGenerator.getDocumentImport(doc)));
	    }
	    
	    clazz.setName(getInterfaceName(module));
	    clazz.setType(TypeOfClass.INTERFACE);
	    startClassBody();
	    
	    String throwsClause = " throws "+getExceptionName(module);
	    
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = docs.get(i);
	        String listDecl = "List<"+doc.getName()+">";
	        appendComment("Returns all "+doc.getMultiple()+" objects stored.");
	        appendStatement("public "+listDecl+" get"+doc.getMultiple()+"()"+throwsClause);
	        emptyline();
	        appendComment("Deletes a "+doc.getName()+" object by id.");
	        appendStatement("public void delete"+doc.getName()+"(String id)"+throwsClause);
	        emptyline();
	        appendComment("Deletes multiple "+doc.getName()+" object.");
	        appendStatement("public void delete"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause);
	        emptyline();
	        appendComment("Returns the "+doc.getName()+" object with the specified id.");
	        appendStatement("public "+doc.getName()+" get"+doc.getName()+"(String id)"+throwsClause+", "+getItemNotFoundExceptionName(doc, module));
	        emptyline();
	        appendComment("Imports a new "+doc.getName()+" object.\nReturns the imported version.");
	        appendStatement("public "+doc.getName()+" import"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause);
	        emptyline();
            appendComment("Imports multiple new "+doc.getName()+" objects.\nReturns the imported versions.");
	        appendStatement("public "+listDecl+" import"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause);
	        emptyline();
	        appendComment("Creates a new "+doc.getName()+" object.\nReturns the created version.");
	        appendStatement("public "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause);
	        emptyline();
	        appendComment("Creates multiple new "+doc.getName()+" objects.\nReturns the created versions.");
	        appendStatement("public "+listDecl+" create"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause);
	        emptyline();
	        appendComment("Updates a "+doc.getName()+" object.\nReturns the updated version.");
	        appendStatement("public "+doc.getName()+" update"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause);
	        emptyline();
	        appendComment("Updates multiple "+doc.getName()+" object.\nReturns the updated versions.");
	        appendStatement("public "+listDecl+" update"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause);
	        emptyline();
	        appendComment("Returns all "+doc.getName()+" objects which match the given property or properties.");
	        appendStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty"+"(QueryProperty... properties)"+throwsClause);
	        emptyline();
	        
	        // get elements COUNT
			appendComment("Returns all " + doc.getMultiple() + " count.");
			appendStatement("public int get" + doc.getMultiple() + "Count()" + throwsClause);
			emptyline();
			// end get elements COUNT

			// get elements Segment
			appendComment("Returns " + doc.getMultiple() + " objects segment.");
			appendStatement("public " + listDecl + " get" + doc.getMultiple() + "(Segment aSegment)" + throwsClause);
			emptyline();
			// end get elements Segment

			// get elements Segment with FILTER
			appendComment("Returns " + doc.getName() + " objects segment which match the given property or properties.");
			appendStatement("public " + listDecl + " get" + doc.getMultiple() + "ByProperty"
					+ "(Segment aSegment, QueryProperty... properties)" + throwsClause);
			emptyline();
			// end get elements Segment with FILTER
	    }
	    
	    return clazz;
	}
	
	private String getDAOVariableName(MetaDocument doc){
		return doc.getVariableName()+"DAO";
	}
	
	private GeneratedClass generateImplementation(MetaModule module){

		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		appendGenerationPoint("generateImplementation");
		
		clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getImplementationName(module),"The implementation of the "+getInterfaceName(module)+"."));

	    clazz.setPackageName(getPackageName(module));
	    
	    clazz.addImport("java.util.List");
	    clazz.addImport("java.util.ArrayList");
	    clazz.addImport("java.util.Arrays");
	    clazz.addImport("net.anotheria.util.sorter.SortType");
	    clazz.addImport("net.anotheria.util.Date");
	    clazz.addImport("net.anotheria.util.slicer.Segment");
	    clazz.addImport("net.anotheria.anodoc.query2.QueryProperty");
		clazz.addImport("org.slf4j.MarkerFactory");
	    List<MetaDocument> docs = module.getDocuments();
	    for (MetaDocument doc : docs){
	    	clazz.addImport(DataFacadeGenerator.getDocumentImport(doc));
	    }
	    clazz.addImport("net.anotheria.db.service.BasePersistenceServiceJDBCImpl");
	    clazz.addImport("net.anotheria.db.dao.DAOException");
	    clazz.addImport("java.sql.Connection");
	    clazz.addImport("java.sql.SQLException");
	    emptyline();
	    
	    clazz.setName(getImplementationName(module));
	    clazz.setParent("BasePersistenceServiceJDBCImpl");
	    clazz.addInterface(getInterfaceName(module));

	    startClassBody();
	    appendStatement("private static "+getImplementationName(module)+" instance");
	    emptyline();
	    
	    appendCommentLine("DAO Objects for data access.");
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = docs.get(i);
	        appendStatement("private "+PersistenceServiceDAOGenerator.getDAOName(doc)+" "+getDAOVariableName(doc));
	    }
	    emptyline();
	    
	    String jdbcConfigName = "";
	    if (module.getModuleOptions()!= null)
	    	if (module.getModuleOptions().get(GenerationOptions.JDBCCONFIG) != null )
	    		jdbcConfigName = module.getModuleOptions().get(GenerationOptions.JDBCCONFIG).getValue();

	    if (jdbcConfigName.length() > 0)
	    	jdbcConfigName = "\"" + jdbcConfigName + "\"";//surround with "" if it is not an empty string
	    appendString("private "+getImplementationName(module)+"(){");
	    increaseIdent();
    	appendStatement("super("+jdbcConfigName+")");
	    closeBlockNEW();
	    emptyline();
	    
	    appendString("static final "+getImplementationName(module)+" getInstance(){");
	    increaseIdent();
	    appendString("if (instance==null){");
	    increaseIdent();
	    appendStatement("instance = new "+getImplementationName(module)+"()");
	    closeBlockNEW();
	    appendStatement("return instance");
	    closeBlockNEW();
	    emptyline();
	    
	    appendString("public void init(){");
	    increaseIdent();
	    appendStatement("super.init()");
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = docs.get(i);
	        appendStatement(getDAOVariableName(doc) +" = new "+PersistenceServiceDAOGenerator.getDAOName(doc)+"("+jdbcConfigName+")");
	    }
	    appendStatement("String currentDAO = null");
        openTry();
	    appendStatement("Connection c = getConnection()");
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = docs.get(i);
	        appendStatement("log.info(\"Initializing DAO for "+doc.getName()+"\" )");
	        appendStatement("currentDAO = "+quote(doc.getName()));
	        appendStatement(getDAOVariableName(doc)+".init(c)");
	    }
	
	    decreaseIdent();
	    appendString("}catch(DAOException e){");
		appendIncreasedStatement("log.error(MarkerFactory.getMarker(\"FATAL\"), \"init failed (dao:\"+currentDAO+\") \",e )");
		//appendIncreasedStatement("throw new RuntimeException(\"init failed (dao:\"+currentDAO+\") cause: \"+e.getMessage())"));
	    appendString("}catch(SQLException e){");
		appendIncreasedStatement("log.error(MarkerFactory.getMarker(\"FATAL\"), \"init failed (sql) \",e )");
		//appendIncreasedStatement("throw new RuntimeException(\"init failed (sql) cause: \"+e.getMessage())"));
	    appendString("}catch(Exception e){");
	    appendIncreasedStatement("System.out.println(e.getMessage()+\" \"+e.getClass())");
		appendIncreasedStatement("log.error(MarkerFactory.getMarker(\"FATAL\"), \"init failed (e) \",e )");
		//appendIncreasedStatement("throw new RuntimeException(\"init failed (sql) cause: \"+e.getMessage())"));
		appendString("}");
	    closeBlockNEW();
	    emptyline();
	    
	    
	    String throwsClause = " throws "+getExceptionName(module);
	    String callLog = null;
	    
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = docs.get(i);
	        String listDecl = "List<"+doc.getName()+">";

	        callLog = "\"Call get"+doc.getMultiple()+"() \"";
	        appendComment("Returns all "+doc.getMultiple()+" objects stored.");
	        openFun("public "+listDecl+" get"+doc.getMultiple()+"()"+throwsClause);
	        generateMethodStart(callLog);
	        appendStatement("return "+getDAOVariableName(doc)+".get"+doc.getMultiple()+"(c)");
	        generateMethodEnd(module, callLog);
	        closeBlockNEW();
	        emptyline();

	        callLog = "\"Call delete"+doc.getName()+"(\"+id+\") \"";
	        appendComment("Deletes a "+doc.getName()+" object by id.");
	        openFun("public void delete"+doc.getName()+"(String id)"+throwsClause);
	        generateMethodStart(callLog);
	        appendStatement(getDAOVariableName(doc)+".delete"+doc.getName()+"(c, id)");
	        generateMethodEnd(module, callLog);
	        closeBlockNEW();
	        emptyline();

	        callLog = "\"Call delete"+doc.getMultiple()+"(\"+list+\") \"";
	        appendComment("Deletes multiple  "+doc.getName()+" objects.");
	        openFun("public void delete"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause);
	        generateMethodStart(callLog);
	        appendStatement(getDAOVariableName(doc)+".delete"+doc.getMultiple()+"(c, list)");
	        appendStatement("return");
	        generateMethodEnd(module, callLog);
	        closeBlockNEW();
	        emptyline();
	        
	        callLog = "\"Call get"+doc.getName()+"(\"+id+\") \"";
	        appendComment("Returns the "+doc.getName()+" object with the specified id.");
	        openFun("public "+doc.getName()+" get"+doc.getName()+"(String id)"+throwsClause);
	        generateMethodStart(callLog);
	        appendStatement("return "+getDAOVariableName(doc)+".get"+doc.getName()+"(c, id)");
			decreaseIdent();
			appendString("}catch("+PersistenceServiceDAOGenerator.getNoItemExceptionName(doc)+" e){");
			appendIncreasedStatement("throw new "+getItemNotFoundExceptionName(doc, module)+"(id)");
	        increaseIdent();
	        generateMethodEnd(module, callLog);
	        closeBlockNEW();
	        emptyline();

	        callLog = "\"Call import"+doc.getName()+"(\"+"+doc.getVariableName()+"+\") \"";
	        appendComment("Imports a new "+doc.getName()+" object.\nReturns the imported version.");
	        openFun("public "+doc.getName()+" import"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause);
	        generateMethodStart(callLog);
	        appendStatement("return "+getDAOVariableName(doc)+".import"+doc.getName()+"(c, "+doc.getVariableName()+")");
	        generateMethodEnd(module, callLog);
	        closeBlockNEW();
	        emptyline();

            callLog = "\"Call import"+doc.getMultiple()+"(\"+list+\") \"";
	        appendComment("Imports multiple new "+doc.getName()+" objects.\nReturns the imported versions.");
	        openFun("public "+listDecl+" import"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause);
	        generateMethodStart(callLog);
	        appendStatement("return "+getDAOVariableName(doc)+".import"+doc.getMultiple()+"(c, list)");
	        generateMethodEnd(module, callLog);
	        closeBlockNEW();
	        emptyline();

	        
	        callLog = "\"Call create"+doc.getName()+"(\"+"+doc.getVariableName()+"+\") \"";
	        appendComment("Creates a new "+doc.getName()+" object.\nReturns the created version.");
	        openFun("public "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause);
	        generateMethodStart(callLog);
	        appendStatement("return "+getDAOVariableName(doc)+".create"+doc.getName()+"(c, "+doc.getVariableName()+")");
	        generateMethodEnd(module, callLog);
	        closeBlockNEW();
	        emptyline();
	        
	        callLog = "\"Call create"+doc.getMultiple()+"(\"+list+\") \"";
	        appendComment("Creates multiple new "+doc.getName()+" objects.\nReturns the created versions.");
	        openFun("public "+listDecl+" create"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause);
	        generateMethodStart(callLog);
	        appendStatement("return "+getDAOVariableName(doc)+".create"+doc.getMultiple()+"(c, list)");
	        generateMethodEnd(module, callLog);
	        closeBlockNEW();
	        emptyline();
	        

	        callLog = "\"Call update"+doc.getName()+"(\"+"+doc.getVariableName()+"+\") \"";
	        appendComment("Updates a "+doc.getName()+" object.\nReturns the updated version.");
	        openFun("public "+doc.getName()+" update"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause);
	        generateMethodStart(callLog);
	        appendStatement("return "+getDAOVariableName(doc)+".update"+doc.getName()+"(c, "+doc.getVariableName()+")");
	        generateMethodEnd(module, callLog);
	        closeBlockNEW();
	        emptyline();
	        
	        callLog = "\"Call update"+doc.getMultiple()+"(\"+list+\") \"";
	        appendComment("Updates multiple  "+doc.getName()+" objects.\nReturns the updated versions.");
	        openFun("public "+listDecl+" update"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause);
	        generateMethodStart(callLog);
	        appendStatement("return "+getDAOVariableName(doc)+".update"+doc.getMultiple()+"(c, list)");
	        generateMethodEnd(module, callLog);
	        closeBlockNEW();
	        emptyline();

	        callLog = "\"Call get"+doc.getMultiple()+"ByProperty(\"+properties+\") \"";
	        appendComment("Returns all "+doc.getName()+" objects which match the given property.");
	        openFun("public "+listDecl+" get"+doc.getMultiple()+"ByProperty"+"(QueryProperty... properties)"+throwsClause);
	        generateMethodStart(callLog);
	        appendStatement("return "+getDAOVariableName(doc)+".get"+doc.getMultiple()+"ByProperty(c, Arrays.asList(properties))");
	        generateMethodEnd(module, callLog);
	        closeBlockNEW();
	        emptyline();
	        
	        // get elements COUNT
			callLog = "\"Call get" + doc.getMultiple() + "Count() \"";
			appendComment("Returns " + doc.getMultiple() + " objects count.");
			openFun("public int get" + doc.getMultiple() + "Count()" + throwsClause);
			generateMethodStart(callLog);
			appendStatement("return " + getDAOVariableName(doc) + ".get" + doc.getMultiple() + "Count(c)");
			generateMethodEnd(module, callLog);
			closeBlockNEW();
			emptyline();
			// end get elements COUNT
	        
			// get elements Segment
			callLog = "\"Call get" + doc.getMultiple() + "(\" + aSegment + \") \"";
			appendComment("Returns " + doc.getMultiple() + " objects segment.");
			openFun("public " + listDecl + " get" + doc.getMultiple() + "(Segment aSegment)" + throwsClause);
			generateMethodStart(callLog);
			appendStatement("return " + getDAOVariableName(doc) + ".get" + doc.getMultiple() + "(c, aSegment)");
			generateMethodEnd(module, callLog);
			closeBlockNEW();
			emptyline();
			// end get elements Segment

			// get elements Segment with FILTER
			callLog = "\"Call get" + doc.getMultiple() + "ByProperty(\" + aSegment + \",\" + aProperties + \") \"";
			appendComment("Returns " + doc.getName() + " objects segment which match the given property.");
			openFun("public " + listDecl + " get" + doc.getMultiple() + "ByProperty" + "(Segment aSegment, QueryProperty... aProperties)"
					+ throwsClause);
			generateMethodStart(callLog);
			appendStatement("return " + getDAOVariableName(doc) + ".get" + doc.getMultiple()
					+ "ByProperty(c, aSegment, Arrays.asList(aProperties))");
			generateMethodEnd(module, callLog);
			closeBlockNEW();
			emptyline();
			// end get elements Segment with FILTER
	        
/*	        
	        appendComment("Returns all "+doc.getName()+" objects which match given properties."));
	        appendStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperties"+"(List<QueryProperty> properties)"+throwsClause));
	        emptyline();
*/	        
	    }
	    

	    return clazz;
	}
	
	private void generateMethodStart(String callLog){
		appendStatement("log.debug("+callLog+")");
		appendStatement("Connection c = null");
		openTry();
		appendStatement("c = getConnection()");
	}
	
	private void generateMethodEnd(MetaModule mod, String callLog){
		decreaseIdent();
		appendString("}catch(SQLException e){");
		appendIncreasedStatement("log.error(\""+callLog.substring(callLog.indexOf(' ')+1)+",e)");
		appendIncreasedStatement("throw new "+getExceptionName(mod)+"(e)");
		appendString("}catch(DAOException e){");
		appendIncreasedStatement("throw new "+getExceptionName(mod)+"(e)");
		appendString("}finally{");
		appendIncreasedStatement("release(c)");
		appendString("}");
	}

	/**
	 * Generates a factory for the implementation.
	 * @param module
	 * @return
	 */
	private GeneratedClass generateFactory(MetaModule module){
		GeneratedClass clazz = new GeneratedClass();
		startNewJob(clazz);
		
		appendGenerationPoint("generateFactory");
		
		clazz.setTypeComment(CommentGenerator.generateJavaTypeComment(getFactoryName(module),"The factory for the "+getInterfaceName(module)+" implementation."));

	    clazz.setPackageName(getPackageName(module));
	    clazz.setName(getFactoryName(module));
	    
	    startClassBody();
	    appendString("public static "+getInterfaceName(module)+" create"+getServiceName(module)+"(){");
	    increaseIdent();
	    appendString("return "+getImplementationName(module)+".getInstance();");
	    closeBlockNEW();
	    
	    return clazz;
	}

	
	public static final String getExceptionName(MetaModule m){
		return getServiceName(m)+"Exception";
	}
	
	public static String getInterfaceName(MetaModule m){
	    return "I"+getServiceName(m);
	}
	
	public static String getServiceName(MetaModule m){
	    return m.getName()+"PersistenceService";
	}

	public static String getFactoryName(MetaModule m){
	    return getServiceName(m)+"Factory";
	}
	
	public static String getImplementationName(MetaModule m){
	    return getServiceName(m)+"Impl";
	}
	
	public static String getPackageName(Context context, MetaModule module){
		return context.getPackageName(module)+".service.persistence";
	}
	
	public static final String getExceptionImport(Context c, MetaModule m){
		return getPackageName(c, m)+"."+getExceptionName(m);
	}

	public static final String getFactoryImport(Context c, MetaModule m){
		return getPackageName(c, m)+"."+getFactoryName(m);
	}

	public static final String getInterfaceImport(Context c, MetaModule m){
		return getPackageName(c, m)+"."+getInterfaceName(m);
	}
}
