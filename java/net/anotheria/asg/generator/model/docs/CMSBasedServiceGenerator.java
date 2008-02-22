package net.anotheria.asg.generator.model.docs;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.CommentGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.GeneratorDataRegistry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaModule;
import net.anotheria.asg.generator.model.AbstractServiceGenerator;
import net.anotheria.asg.generator.model.DataFacadeGenerator;

public class CMSBasedServiceGenerator extends AbstractServiceGenerator implements IGenerator{
	
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
	
	private String generateImplementation(MetaModule module){
	    StringBuilder ret = new StringBuilder(5000);

		ret.append(CommentGenerator.generateJavaTypeComment(getImplementationName(module),"The implementation of the "+getInterfaceName(module)+"."));

		ret.append(writeStatement("package "+getPackageName(module)));
	    ret.append(emptyline());
	    ret.append(writeImport("java.util.List"));
	    ret.append(writeImport("java.util.ArrayList"));
	    ret.append(writeImport("net.anotheria.anodoc.data.Property"));
	    ret.append(writeImport("net.anotheria.anodoc.data.NoSuchPropertyException"));
		ret.append(writeImport("net.anotheria.util.sorter.SortType"));
		ret.append(writeImport("net.anotheria.util.sorter.StaticQuickSorter"));
		//ret.append(writeImport("net.anotheria.util.Date"));
	    ret.append(writeImport(context.getPackageName(module)+".data."+ module.getModuleClassName()));
	    ret.append(writeImport(context.getServicePackageName(MetaModule.SHARED)+".BasicCMSService"));
	    List<MetaDocument> docs = module.getDocuments();
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        ret.append(writeImport(DataFacadeGenerator.getDocumentImport(context, doc)));
	        ret.append(writeImport(DataFacadeGenerator.getXMLHelperImport(context, doc)));
	        ret.append(writeImport(DocumentGenerator.getDocumentImport(context, doc)));
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
	    
	    ret.append(writeString("public class "+getImplementationName(module)+" extends BasicCMSService implements "+getInterfaceName(module)+" {"));
	    increaseIdent();
	    ret.append(writeStatement("private static "+getImplementationName(module)+" instance"));
	    ret.append(emptyline());
	    
	    ret.append(writeString("private "+getImplementationName(module)+"(){"));
	    increaseIdent();
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
	    
	    //generate module handling.
	    ret.append(writeString("private "+module.getModuleClassName()+" "+getModuleGetterCall(module)+"{"));
	    increaseIdent();
	    ret.append(writeStatement("return ("+module.getModuleClassName()+") getModule("+module.getModuleClassName()+".MODULE_ID)"));
	    ret.append(closeBlock());
	    ret.append(emptyline());
	    
	    boolean containsAnyMultilingualDocs = false;
	    
	    for (int i=0; i<docs.size(); i++){
	        MetaDocument doc = (MetaDocument)docs.get(i);
	        String listDecl = "List<"+doc.getName()+">";
	        
	        ret.append(writeString("public "+listDecl+" get"+doc.getMultiple()+"(){"));
	        increaseIdent();
	        ret.append(writeStatement("List "+doc.getMultiple().toLowerCase()+" = "+getModuleGetterCall(module)+".get"+doc.getMultiple()+"()"));
	        ret.append(writeStatement("return "+doc.getMultiple().toLowerCase()));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
			ret.append(writeString("public "+listDecl+" get"+doc.getMultiple()+"(SortType sortType){"));
			increaseIdent();
			ret.append(writeStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"(), sortType)"));
			ret.append(closeBlock());
			ret.append(emptyline());

	        ret.append(writeString("public void delete"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+"){"));
	        increaseIdent();
	        ret.append(writeStatement("delete"+doc.getName()+"("+doc.getVariableName()+".getId())"));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
	        ret.append(writeString("public void delete"+doc.getName()+"(String id){"));
	        increaseIdent();
	        ret.append(writeStatement(module.getModuleClassName()+" module = "+getModuleGetterCall(module)));
	        ret.append(writeStatement("module.delete"+doc.getName()+"(id)"));
	        ret.append(writeStatement("updateModule(module)"));
	        ret.append(closeBlock());
	        ret.append(emptyline());

	        ret.append(writeString("public "+doc.getName()+" get"+doc.getName()+"(String id){"));
	        increaseIdent();
	        ret.append(writeStatement("return "+getModuleGetterCall(module)+".get"+doc.getName()+"(id)"));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
	        //create
	        ret.append(writeString("public "+doc.getName()+" create"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+"){"));
	        increaseIdent();
	        ret.append(writeStatement(module.getModuleClassName()+" module = "+getModuleGetterCall(module)));
	        ret.append(writeStatement("module.create"+doc.getName()+"(("+DocumentGenerator.getDocumentName(doc)+")"+doc.getVariableName()+")"));
	        ret.append(writeStatement("updateModule(module)"));
	        
	        ret.append(writeString("if (hasServiceListeners()){"));
	        increaseIdent();
	        ret.append(writeStatement("List<IServiceListener> myListeners = getServiceListeners()"));
	        ret.append(writeString("for (int i=0; i<myListeners.size(); i++)"));
	        ret.append(writeIncreasedStatement("myListeners.get(i).documentCreated("+doc.getVariableName()+")"));
	        ret.append(closeBlock());	
	        
	        
	        ret.append(writeStatement("return "+doc.getVariableName()));
	        ret.append(closeBlock());
	        ret.append(emptyline());

	        
	        
	        //create multiple
	        ret.append(writeComment("Creates multiple new "+doc.getName()+" objects.\nReturns the created versions."));
	        ret.append(writeStatement("public "+listDecl+" create"+doc.getMultiple()+"("+listDecl+" list){"));
	        increaseIdent();
	        ret.append(writeStatement(module.getModuleClassName()+" module = "+getModuleGetterCall(module)));
	        ret.append(writeStatement(listDecl+" ret = new ArrayList<"+doc.getName()+">()"));
	        ret.append(writeString("for ("+doc.getName()+" "+doc.getVariableName()+" : list){"));
	        increaseIdent();
	        ret.append(writeStatement(doc.getName()+" created = module.create"+doc.getName()+"(("+DocumentGenerator.getDocumentName(doc)+")"+doc.getVariableName()+")"));
	        ret.append(writeStatement("ret.add(created)"));
	        ret.append(closeBlock());
	        
	        ret.append(writeStatement("updateModule(module)"));
	        
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
	        
	        
	        ret.append(writeString("public "+doc.getName()+" update"+doc.getName()+"("+doc.getName()+" "+doc.getVariableName()+"){"));
	        increaseIdent();
	        ret.append(writeStatement(doc.getName()+" oldVersion = null"));
	        ret.append(writeStatement(module.getModuleClassName()+" module = "+getModuleGetterCall(module)));
	        
	        ret.append(writeString("if (hasServiceListeners())"));
	        ret.append(writeIncreasedStatement("oldVersion = module.get"+doc.getName()+"("+doc.getVariableName()+".getId())"));
	        
	        ret.append(writeStatement("module.update"+doc.getName()+"(("+DocumentGenerator.getDocumentName(doc)+")"+doc.getVariableName()+")"));
	        ret.append(writeStatement("updateModule(module)"));
	        
	        ret.append(writeString("if (hasServiceListeners()){"));
	        increaseIdent();
	        ret.append(writeStatement("List<IServiceListener> myListeners = getServiceListeners()"));
	        ret.append(writeString("for (int i=0; i<myListeners.size(); i++)"));
	        ret.append(writeIncreasedStatement("myListeners.get(i).documentUpdated(oldVersion, "+doc.getVariableName()+")"));
	        ret.append(closeBlock());
	        
	        ret.append(writeStatement("return "+doc.getVariableName()));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        

	        //updatemultiple
	        ret.append(writeString("public "+listDecl+" update"+doc.getMultiple()+"("+listDecl+" list){"));
	        increaseIdent();
	        ret.append(writeStatement(listDecl+" oldList = null;"));
	        ret.append(writeString("if (hasServiceListeners())"));
	        ret.append(writeIncreasedStatement("oldList = new ArrayList<"+doc.getName()+">(list.size())"));
	        
	        ret.append(writeStatement(module.getModuleClassName()+" module = "+getModuleGetterCall(module)));

	        ret.append(writeString("for ("+doc.getName()+" "+doc.getVariableName()+" : list){"));
	        increaseIdent();
	        ret.append(writeString("if (hasServiceListeners())"));
	        ret.append(writeIncreasedStatement("oldList.add(module.get"+doc.getName()+"("+doc.getVariableName()+".getId()))"));
	        ret.append(writeStatement("module.update"+doc.getName()+"(("+DocumentGenerator.getDocumentName(doc)+")"+doc.getVariableName()+")"));
	        ret.append(closeBlock());
	        ret.append(writeStatement("updateModule(module)"));
	        
	        ret.append(writeString("if (hasServiceListeners()){"));
	        increaseIdent();
	        ret.append(writeStatement("List<IServiceListener> myListeners = getServiceListeners()"));
	        ret.append(writeString("for (int i=0; i<myListeners.size(); i++)"));
	        increaseIdent();
	        ret.append(writeString("for (int t=0; t<list.size(); t++)"));
	        ret.append(writeIncreasedStatement("myListeners.get(i).documentUpdated(oldList.get(i), list.get(i))"));
	        decreaseIdent();
	        ret.append(closeBlock());
	        
	        ret.append(writeStatement("return list"));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
	        
	        
	        ret.append(writeString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value){"));
	        increaseIdent();
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
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
			ret.append(writeString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(String propertyName, Object value, SortType sortType){"));
			increaseIdent();
			ret.append(writeStatement("return StaticQuickSorter.sort(get"+doc.getMultiple()+"ByProperty(propertyName, value), sortType)"));
			ret.append(closeBlock());
			
			ret.append(writeComment("Executes a query on "+doc.getMultiple()));
			ret.append(writeString("public QueryResult executeQueryOn"+doc.getMultiple()+"(DocumentQuery query){"));
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
	        ret.append(writeString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(QueryProperty... property){"));
	        increaseIdent();
	        ret.append(writeStatement("throw new RuntimeException(\"Not yet implemented\")"));
	        ret.append(closeBlock());
	        ret.append(emptyline());
	        
			ret.append(writeComment("Returns all "+doc.getName()+" objects, where property matches, sorted"));
			ret.append(writeString("public "+listDecl+" get"+doc.getMultiple()+"ByProperty(SortType sortType, QueryProperty... property){"));
	        increaseIdent();
	        ret.append(writeStatement("throw new RuntimeException(\"Not yet implemented\")"));
	        ret.append(closeBlock());
			ret.append(emptyline());
			
			if (GeneratorDataRegistry.hasLanguageCopyMethods(doc)){
				containsAnyMultilingualDocs = true;
				ret.append(writeCommentLine("This method is not very fast, since it makes an update (eff. save) after each doc."));
				ret.append(writeString("public void copyMultilingualAttributesInAll"+doc.getMultiple()+"(String sourceLanguage, String targetLanguage){"));
				increaseIdent();
				ret.append(writeStatement("List<"+doc.getName()+"> allDocumentsSrc = get"+doc.getMultiple()+"()"));
				ret.append(writeStatement("List<"+doc.getName()+"> allDocuments = new ArrayList<"+doc.getName()+">(allDocumentsSrc.size())"));
				ret.append(writeStatement("allDocuments.addAll(allDocumentsSrc)"));
				ret.append(writeString("for ("+doc.getName()+" document : allDocuments){"));
				increaseIdent();
				ret.append(writeStatement("document.copyLANG2LANG(sourceLanguage, targetLanguage)"));
				ret.append(writeStatement("update"+doc.getName()+"(document)"));
				ret.append(closeBlock());
				ret.append(closeBlock());
				ret.append(emptyline());
			
			}
			
			
	    }
	    
	    if (containsAnyMultilingualDocs){
			ret.append(writeComment("Copies all multilingual fields from sourceLanguage to targetLanguage in all data objects (documents, vo) which are part of this module and managed by this service"));
			ret.append(writeString("public void copyMultilingualAttributesInAllObjects(String sourceLanguage, String targetLanguage){"));
			increaseIdent();
			for (MetaDocument doc : docs){
				if (GeneratorDataRegistry.hasLanguageCopyMethods(doc))
					ret.append(writeStatement("copyMultilingualAttributesInAll"+doc.getMultiple()+"(sourceLanguage, targetLanguage)"));
			}
			ret.append(closeBlock());
			ret.append(emptyline());
	    }

	    
	    //generate export function
	    ret.append(emptyline());
	    for (MetaDocument d : docs){
	    	ret.append(writeStatement("public XMLNode export"+d.getMultiple()+"ToXML(){"));
	    	increaseIdent();
	    	ret.append(writeStatement("XMLNode ret = new XMLNode("+quote(d.getMultiple())+")"));
	    	ret.append(writeStatement("List<"+d.getName()+"> list = get"+d.getMultiple()+"()"));
	    	ret.append(writeStatement("ret.addAttribute(new XMLAttribute("+quote("count")+", list.size()))"));
	    	ret.append(writeString("for ("+d.getName()+" object : list)"));
	    	ret.append(writeIncreasedStatement("ret.addChildNode("+DataFacadeGenerator.getXMLHelperName(d)+".toXML(object))"));
	    	ret.append(writeStatement("return ret"));
	    	
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
	
	private String getModuleGetterMethod(MetaModule module){
	    return "_get"+module.getModuleClassName();	    
	}
	
	private String getModuleGetterCall(MetaModule module){
	    return getModuleGetterMethod(module)+"()";
	}
	
}
