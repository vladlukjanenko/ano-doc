package net.anotheria.asg.generator.model.db;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.CommentGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.model.AbstractServiceGenerator;
import net.anotheria.asg.generator.model.DataFacadeGenerator;

/**
 * Generates a DB-Backed implementation of a module interface and the according factory.
 * @author another
 *
 */
public class JDBCBasedServiceGenerator extends AbstractServiceGenerator implements IGenerator{
	
	private Context context;
	
	public List<FileEntry> generate(IGenerateable gmodule, Context context){
		
		MetaModule mod = (MetaModule)gmodule;
		
		this.context = context;
		String packageName = context.getPackageName(mod)+".service";
		
		List<FileEntry> ret = new ArrayList<FileEntry>();
		
		ret.add(new FileEntry(FileEntry.package2path(packageName), getFactoryName(mod), generateFactory(mod)));
		ret.add(new FileEntry(FileEntry.package2path(packageName), getImplementationName(mod), generateImplementation(mod)));
		
		return ret;
	}
	
	/**
	 * Generates the implementation
	 * @param module the metamodule to generate
	 * @return
	 */
	private String generateImplementation(MetaModule module){
	    StringBuilder ret = new StringBuilder(5000);

		ret.append(CommentGenerator.generateJavaTypeComment(getImplementationName(module),"The implementation of the "+getInterfaceName(module)+"."));

	    ret.append(writeStatement("package "+getPackageName(module)));
	    ret.append(emptyline());
	    ret.append(writeImport("java.util.List"));
	    //ret.append(writeImport("java.util.ArrayList"));
	    ret.append(writeImport("net.anotheria.util.sorter.SortType"));
	    ret.append(writeImport("net.anotheria.util.sorter.StaticQuickSorter"));
		//ret.append(writeImport("net.anotheria.util.Date"));
	    ret.append(writeImport(context.getServicePackageName(MetaModule.SHARED)+".BasicService"));
	    ret.append(emptyline());
	    ret.append(writeImport(JDBCPersistenceServiceGenerator.getInterfaceImport(context, module)));
	    ret.append(writeImport(JDBCPersistenceServiceGenerator.getFactoryImport(context, module)));
	    ret.append(writeImport(JDBCPersistenceServiceGenerator.getExceptionImport(context, module)));

	    List<MetaDocument> docs = module.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        ret.append(writeImport(DataFacadeGenerator.getDocumentImport(context, doc)));
	        ret.append(writeImport(DataFacadeGenerator.getXMLHelperImport(context, doc)));
	    }
	    ret.append(emptyline());
	    ret.append(writeImport("net.anotheria.asg.util.listener.IServiceListener"));
	    ret.append(writeImport("net.anotheria.anodoc.query2.DocumentQuery"));
	    ret.append(writeImport("net.anotheria.anodoc.query2.QueryResult"));
	    ret.append(writeImport("net.anotheria.anodoc.query2.QueryResultEntry"));
	    ret.append(writeImport("net.anotheria.anodoc.query2.QueryProperty"));
	    
	    ret.append(emptyline());
	    ret.append(writeImport("net.anotheria.util.xml.XMLNode"));
	    ret.append(writeImport("net.anotheria.util.xml.XMLAttribute"));
	    ret.append(emptyline());
	    
	    ret.append(writeString("public class "+getImplementationName(module)+" extends BasicService implements "+getInterfaceName(module)+" {"));
	    increaseIdent();
	    ret.append(writeStatement("private static "+getImplementationName(module)+" instance"));
	    ret.append(emptyline());
	    
	    ret.append(writeStatement("private "+JDBCPersistenceServiceGenerator.getInterfaceName(module)+" pService"));
	    
	    ret.append(writeString("private "+getImplementationName(module)+"(){"));
	    increaseIdent();
	    ret.append(writeStatement("pService = "+JDBCPersistenceServiceGenerator.getFactoryName(module)+".create"+JDBCPersistenceServiceGenerator.getServiceName(module)+"()"));
	    if (module.getListeners().size()>0){
	    	for (int i=0; i<module.getListeners().size(); i++){
	    		String listClassName = (String)module.getListeners().get(i);
	    		ret.append(writeStatement("addServiceListener(new "+listClassName+"())"));
	    	}
	    }
	    ret.append(closeBlock());
	    ret.append(emptyline());
	    
	    ret.append(writeString("static final "+getImplementationName(module)+" getInstance(){"));
	    increaseIdent();
	    ret.append(writeString("if (instance==null){"));
	    increaseIdent();
	    ret.append(writeStatement("instance = new "+getImplementationName(module)+"()"));
	    ret.append(closeBlock());
	    ret.append(writeStatement("return instance"));
	    ret.append(closeBlock());
	    ret.append(emptyline());
	    

	    String throwsClause = " throws "+getExceptionName(module)+" ";
	    
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        String listDecl = "List<"+doc.getName()+">";
	        
	        ret.append(writeString("public "+listDecl+" get"+doc.getMultiple()+"()"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(openTry());
	        ret.append(writeStatement("return pService.get"+doc.getMultiple()+"()"));
	        decreaseIdent();
	        ret.append(writeString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){"));
	        ret.append(writeIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())"));
	        ret.append(writeString("}"));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
			ret.append(writeString("public "+listDecl+" get"+doc.getMultiple()+"(SortType sortType)"+throwsClause+"{"));
			increaseIdent();
			ret.append(writeStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"(), sortType)"));
			ret.append(closeBlock());
			ret.append(emptyline());

	        ret.append(writeString("public void delete"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement("delete"+doc.getName()+"("+doc.getVariableName()+".getId())"));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
	        ret.append(writeString("public void delete"+doc.getName()+"(String id)"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(openTry());
	        ret.append(writeStatement("pService.delete"+doc.getName()+"(id)"));
	        decreaseIdent();
	        ret.append(writeString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){"));
	        ret.append(writeIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())"));
	        ret.append(writeString("}"));
//	        TODO: fix service delete listening
//	        ret.append(writeString("if (hasServiceListeners()){"));
//	        increaseIdent();
//	        ret.append(writeStatement("List<IServiceListener> myListeners = getServiceListeners()"));
//	        ret.append(writeString("for (int i=0; i<myListeners.size(); i++)"));
//	        ret.append(writeIncreasedStatement("myListeners.get(i).documentDeleted(oldVersion, "+doc.getVariableName()+")"));
//	        ret.append(closeBlock());
	        ret.append(closeBlock());
	        ret.append(emptyline());

	        ret.append(writeComment("Deletes multiple "+doc.getName()+" objects."));
	        ret.append(writeString("public void delete"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(openTry());
	        ret.append(writeStatement("pService.delete"+doc.getMultiple()+"(list)"));
	        decreaseIdent();
	        ret.append(writeString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){"));
	        ret.append(writeIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())"));
	        ret.append(writeString("}"));
	        ret.append(writeString("if (hasServiceListeners()){"));
	        increaseIdent();
	        ret.append(writeStatement("List<IServiceListener> myListeners = getServiceListeners()"));
	        ret.append(writeString("for (int i=0; i<myListeners.size(); i++)"));
	        increaseIdent();
	        ret.append(writeString("for (int t = 0; t<list.size(); t++){"));
	        ret.append(writeIncreasedStatement("myListeners.get(i).documentDeleted(list.get(t))"));
	        ret.append(writeString("}"));
	        decreaseIdent();
	        ret.append(closeBlock());	
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
	        ret.append(writeString("public "+doc.getName()+" get"+doc.getName()+"(String id)"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(openTry());
	        ret.append(writeStatement("return pService.get"+doc.getName()+"(id)"));
	        decreaseIdent();
	        ret.append(writeString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){"));
	        ret.append(writeIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())"));
	        ret.append(writeString("}"));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
	        
	        ret.append(writeString("public "+doc.getName()+" import"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(openTry());
	        ret.append(writeStatement(doc.getVariableName() + " = pService.import"+doc.getName()+"("+doc.getVariableName()+")"));
	        decreaseIdent();
	        ret.append(writeString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){"));
	        ret.append(writeIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())"));
	        ret.append(writeString("}"));
	        ret.append(writeStatement("return "+doc.getVariableName()));
	        ret.append(closeBlock());
	        ret.append(emptyline());

	        
	        ret.append(writeString("public "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(openTry());
	        ret.append(writeStatement(doc.getVariableName() + " = pService.create"+doc.getName()+"("+doc.getVariableName()+")"));
	        decreaseIdent();
	        ret.append(writeString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){"));
	        ret.append(writeIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())"));
	        ret.append(writeString("}"));
	        ret.append(writeString("if (hasServiceListeners()){"));
	        increaseIdent();
	        ret.append(writeStatement("List<IServiceListener> myListeners = getServiceListeners()"));
	        ret.append(writeString("for (int i=0; i<myListeners.size(); i++)"));
	        ret.append(writeIncreasedStatement("myListeners.get(i).documentCreated("+doc.getVariableName()+")"));
	        ret.append(closeBlock());	
	        ret.append(writeStatement("return "+doc.getVariableName()));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
	        //
	        ret.append(writeComment("Creates multiple new "+doc.getName()+" objects.\nReturns the created versions."));
	        ret.append(writeString("public "+listDecl+" create"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement(listDecl+" ret = null"));
	        ret.append(openTry());
	        ret.append(writeStatement("ret = pService.create"+doc.getMultiple()+"(list)"));
	        decreaseIdent();
	        ret.append(writeString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){"));
	        ret.append(writeIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())"));
	        ret.append(writeString("}"));
	        ret.append(writeString("if (hasServiceListeners()){"));
	        increaseIdent();
	        ret.append(writeStatement("List<IServiceListener> myListeners = getServiceListeners()"));
	        ret.append(writeString("for (int i=0; i<myListeners.size(); i++)"));
	        increaseIdent();
	        ret.append(writeString("for ("+doc.getName()+" "+doc.getVariableName()+" : ret)"));
	        ret.append(writeIncreasedStatement("myListeners.get(i).documentCreated("+doc.getVariableName()+")"));
	        decreaseIdent();
	        ret.append(closeBlock());	
	        ret.append(writeStatement("return ret"));
	        ret.append(closeBlock());
	        ret.append(emptyline());

	        ret.append(writeComment("Updates multiple new "+doc.getName()+" objects.\nReturns the updated versions."));
	        ret.append(writeString("public "+listDecl+" update"+doc.getMultiple()+"("+listDecl+" list)"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement(listDecl+" ret = null"));
	        ret.append(openTry());
	        ret.append(writeStatement("ret = pService.update"+doc.getMultiple()+"(list)"));
	        decreaseIdent();
	        ret.append(writeString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){"));
	        ret.append(writeIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())"));
	        ret.append(writeString("}"));
	        ret.append(writeString("if (hasServiceListeners()){"));
	        increaseIdent();
	        ret.append(writeStatement("List<IServiceListener> myListeners = getServiceListeners()"));
	        ret.append(writeString("for (int i=0; i<myListeners.size(); i++)"));
	        increaseIdent();
	        ret.append(writeString("for (int t = 0; t<ret.size(); t++){"));
	        ret.append(writeIncreasedStatement("myListeners.get(i).documentUpdated(list.get(t), ret.get(t))"));
	        ret.append(writeString("}"));
	        decreaseIdent();
	        ret.append(closeBlock());	
	        ret.append(writeStatement("return ret"));
	        ret.append(closeBlock());
	        ret.append(emptyline());

	        
	        ret.append(writeString("public "+doc.getName()+" update"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+")"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement(doc.getName()+" oldVersion = null"));
	        ret.append(openTry());
	        ret.append(writeString("if (hasServiceListeners())"));
	        ret.append(writeIncreasedStatement("oldVersion = pService.get"+doc.getName()+"("+doc.getVariableName()+".getId())"));
	        
	        ret.append(writeStatement(doc.getVariableName()+" = pService.update"+doc.getName()+"("+doc.getVariableName()+")"));
	        decreaseIdent();
	        ret.append(writeString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){"));
	        ret.append(writeIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())"));
	        ret.append(writeString("}"));
	        
	        
	        ret.append(writeString("if (hasServiceListeners()){"));
	        increaseIdent();
	        ret.append(writeStatement("List<IServiceListener> myListeners = getServiceListeners()"));
	        ret.append(writeString("for (int i=0; i<myListeners.size(); i++)"));
	        ret.append(writeIncreasedStatement("myListeners.get(i).documentUpdated(oldVersion, "+doc.getVariableName()+")"));
	        ret.append(closeBlock());
	        
	        ret.append(writeStatement("return "+doc.getVariableName()));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
	        ret.append(writeString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value)"+throwsClause+"{"));
	        increaseIdent();
			ret.append(writeStatement("QueryProperty p = new QueryProperty(propertyName, value)"));
			ret.append(writeString("try{"));
			ret.append(writeIncreasedStatement("return pService.get"+doc.getMultiple()+"ByProperty(p)"));
			ret.append(writeString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){"));
			ret.append(writeIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())"));
			ret.append(writeString("}"));
			
/*
	        ret.append(writeStatement(listDecl+" all"+doc.getMultiple()+" = get"+doc.getMultiple()+"()"));
	        ret.append(writeStatement(listDecl+" ret = new ArrayList<"+doc.getName()+">()"));
	        ret.append(writeString("for (int i=0; i<all"+doc.getMultiple()+".size(); i++){"));
	        increaseIdent();
	        ret.append(writeStatement(doc.getName()+" "+doc.getVariableName()+" = all"+doc.getMultiple()+".get(i)"));
	        ret.append(writeString("try{"));
	        increaseIdent();
	        ret.append(writeStatement("Property property = (("+DocumentGenerator.getDocumentName(doc)+")"+doc.getVariableName()+").getProperty(propertyName)"));
	        ret.append(writeStatement("if (property.getValue()==null && value==null){"));
	        ret.append(writeIncreasedStatement("ret.add("+doc.getVariableName()+")"));
	        ret.append(writeString("}else{"));
	        increaseIdent();
	        ret.append(writeString("if (value!=null && property.getValue().equals(value))"));
	        ret.append(writeIncreasedStatement("ret.add("+doc.getVariableName()+")"));
	        ret.append(closeBlock());
	        decreaseIdent();
			ret.append(writeString("}catch(NoSuchPropertyException nspe){"));
			increaseIdent();
			ret.append(writeString("if (value==null)"));
			ret.append(writeIncreasedStatement("ret.add("+doc.getVariableName()+")"));
			decreaseIdent();
	        ret.append(writeString("}catch(Exception ignored){}"));
	        
	        ret.append(closeBlock());
	        ret.append(writeString("return ret;"));
	*/      ret.append(closeBlock());
	        ret.append(emptyline());
	        
			ret.append(writeString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value, SortType sortType)"+throwsClause+"{"));
			increaseIdent();
			ret.append(writeStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"ByProperty(propertyName, value), sortType)"));
			ret.append(closeBlock());
			
			ret.append(writeComment("Executes a query on "+doc.getMultiple()));
			ret.append(writeString("public QueryResult executeQueryOn"+doc.getMultiple()+"(DocumentQuery query)"+throwsClause+"{"));
			increaseIdent();
			ret.append(writeStatement(listDecl+" all"+doc.getMultiple()+" = get"+doc.getMultiple()+"()"));
			ret.append(writeStatement("QueryResult result = new QueryResult()"));
			ret.append(writeString("for (int i=0; i<all"+doc.getMultiple()+".size(); i++){"));
			increaseIdent();
			ret.append(writeStatement("List<QueryResultEntry> partialResult = query.match(all"+doc.getMultiple()+".get(i))"));
			ret.append(writeStatement("result.add(partialResult)"));
			ret.append(closeBlock());
			
			ret.append(writeStatement("return result"));
			ret.append(closeBlock());
			ret.append(emptyline());

			ret.append(writeComment("Returns all "+doc.getName()+" objects, where property matches."));
	        ret.append(writeStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(QueryProperty... property)"+throwsClause+"{"));
	        increaseIdent();
			ret.append(writeString("try{"));
			ret.append(writeIncreasedStatement("return pService.get"+doc.getMultiple()+"ByProperty(property)"));
			ret.append(writeString("}catch("+JDBCPersistenceServiceGenerator.getExceptionName(module)+" e){"));
			ret.append(writeIncreasedStatement("throw new "+getExceptionName(module)+"(\"Persistence failed: \"+e.getMessage())"));
			ret.append(writeString("}"));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
			ret.append(writeComment("Returns all "+doc.getName()+" objects, where property matches, sorted"));
			ret.append(writeStatement("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(SortType sortType, QueryProperty... property)"+throwsClause+"{"));
	        increaseIdent();
	        ret.append(writeStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"ByProperty(property), sortType)"));
	        ret.append(closeBlock());
			ret.append(emptyline());
			
	    }
	    
	    //generate export function
	    ret.append(emptyline());
	    for (MetaDocument d : docs){
	    	ret.append(writeString("public XMLNode export"+d.getMultiple()+"ToXML(){"));
	    	increaseIdent();
	    	ret.append(writeStatement("XMLNode ret = new XMLNode("+quote(d.getMultiple())+")"));
	    	
	    	ret.append(writeString("try{"));
	    	increaseIdent();
	    	ret.append(writeStatement("List<"+d.getName()+"> list = get"+d.getMultiple()+"()"));
	    	ret.append(writeStatement("ret.addAttribute(new XMLAttribute("+quote("count")+", list.size()))"));
	    	ret.append(writeString("for ("+d.getName()+" object : list)"));
	    	ret.append(writeIncreasedStatement("ret.addChildNode("+DataFacadeGenerator.getXMLHelperName(d)+".toXML(object))"));
	    	ret.append(writeStatement("return ret"));
	    	ret.append(closeBlock());
	    	ret.append(writeStatement("catch("+getExceptionName(module)+" e){"));
	    	increaseIdent();
	    	ret.append(writeStatement("throw new RuntimeException("+quote("export"+d.getMultiple()+"ToXML() failure: ")+" + e.getStackTrace())"));
	    	ret.append(closeBlock());
	    	ret.append(closeBlock());
	    	ret.append(emptyline());
	    }
	    

	    ret.append(writeString("public XMLNode exportToXML(){"));
	    increaseIdent();
	    ret.append(writeStatement("XMLNode ret = new XMLNode("+quote(module.getName())+")"));
	    ret.append(emptyline());
	    for (MetaDocument d : docs){
	    	ret.append(writeStatement("ret.addChildNode(export"+d.getMultiple()+"ToXML())"));
	    }
	    ret.append(emptyline());
	    ret.append(writeStatement("return ret"));
	    ret.append(closeBlock());
	    
	    
	    ret.append(closeBlock());
	    return ret.toString();
	}
		
}
