package net.anotheria.asg.generator;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.meta.MetaDocument;
import net.anotheria.asg.generator.meta.MetaProperty;
import net.anotheria.asg.generator.view.meta.MetaFieldElement;
import net.anotheria.asg.generator.view.meta.MetaViewElement;
import net.anotheria.asg.generator.view.meta.MultilingualFieldElement;
import net.anotheria.util.StringUtils;


/**
 * TODO please remined another to comment this class.
 * @author another
 */
public class AbstractGenerator{

	/**
	 * Quotes a string with double quotes &quot;.
	 * @param s
	 * @return
	 */
	protected String quote(String s){
		return "\""+s+"\"";
	}

	/**
	 * Quotes a string with double quotes &quot;.
	 * @param s
	 * @return
	 */
	protected String quote(StringBuilder s){
		return "\""+s.toString()+"\"";
	}
	
	/**
	 * Quotes the string representation of the integer parameter with double quotes &quot;.
	 * @param a
	 * @return
	 */
	protected String quote(int a){
		return quote(""+a);
	}

	/**
	 * Returns a line with increased ident and the parameter string.
	 * @param s
	 * @return
	 */
	protected String writeIncreasedString(String s){
		increaseIdent();
		String ret = writeString(s);
		decreaseIdent();
		return ret;
	}
	
	/**
	 * Adds all string parameters after each other to the current target StringBuilder with an increased ident.
	 * @param strings
	 */
	protected void appendIncreasedString(String... strings){
		appendIncreasedString(getCurrentJobContent(), strings);
	}
		
	/**
	 * Adds all string parameters after each other to the given target StringBuilder with an increased ident.
	 * @param target
	 * @param strings
	 */
	protected void appendIncreasedString(StringBuilder target, String... strings){
		increaseIdent();
		appendString(target, strings);
		decreaseIdent();
	}

	protected String writeIncreasedStatement(String s){
		return writeIncreasedString(s+";");
	}
	
	protected void appendIncreasedStatement(String... strings){
		appendIncreasedStatement(getCurrentJobContent(), strings);
	}
	
	protected void appendIncreasedStatement(StringBuilder target, String... strings){
		increaseIdent();
		appendStatement(target, strings);
		decreaseIdent();
	}

	/**
	 * Constant for line break.
	 */
	public static final String CRLF = "\n";
	
	/**
	 * Current ident.
	 */
	protected int ident = 0;

	/**
	 * Writes a string in a new line with ident and linefeed.
	 * @param s string to write.
	 * @return
	 * @deprecated use appendString instead
	 */
	protected String writeString(String s){
		String ret = getIdent();
		ret += s;
		ret += CRLF;
		return ret; 
	}
	
	protected void appendString(String... strings){
		appendString(getCurrentJobContent(), strings);
	}

	protected void appendString(StringBuilder target, String... strings){
		appendIdent(target);
		for (String s : strings)
			target.append(s);
		target.append(CRLF);
	}

	//later replace with openTry
	protected void openTry(){
		appendString("try {");
		increaseIdent();
	}

	protected void appendCatch(Class<? extends Throwable> exceptionClazz){
		((GeneratedClass)getCurrentJob()).addImport(exceptionClazz);
		appendCatch(exceptionClazz.getName());
	}
	
	protected void appendCatch(String exceptionName){
		decreaseIdent();
		appendString("} catch (", exceptionName, " e) {");
		increaseIdent();
	}
	
	protected void openFun(String s){
		if (!s.endsWith("{"))
			s+=" {";
		appendString(s);
		increaseIdent();
	}
	
	protected void appendNullCheck(String aArgName, String aExceptionMessage){
		((GeneratedClass)getCurrentJob()).addImport(IllegalArgumentException.class);
		appendString("if(" + aArgName + " == null)");
		increaseIdent();
		appendString("throw new IllegalArgumentException(\"" + aExceptionMessage + "\");");
		decreaseIdent();		
	}

	
	/**
	 * Writes a statement (';' at the end of the line).
	 * @param s statement to write.
	 * @return
	 * @deprecated use appendStatement instead
	 */
	protected String writeStatement(String s){
		String ret = getIdent();
		ret += s;
		ret += ";";
		ret += CRLF;
		return ret; 
	}

	protected void append(String... strings){
		StringBuilder target = getCurrentJobContent();
		for (String s: strings)
			target.append(s);
	}
	
	protected void appendStatement(String... strings){
		appendStatement(getCurrentJobContent(), strings);
	}
	
	protected void appendStatement(StringBuilder target, String... strings){
		appendIdent(target);
		for (String s : strings)
			target.append(s);
		target.append(';');
		target.append(CRLF);
	}

	
	private void appendIdent(StringBuilder target){
		for (int i=0; i<ident; i++)
			target.append('\t');
	}
	
	/**
	 * Returns current ident as string.
	 * @return a string with "\t"s.
	 */
	private String getIdent(){
		StringBuilder ret = new StringBuilder();
		for (int i=0; i<ident; i++)
			ret.append("\t");
		return ret.toString();
	}
	
	/**
	 * increases current ident.
	 */
	protected void increaseIdent(){
		ident++;
	}
	
	/**
	 * decreases current ident.
	 */
	protected void  decreaseIdent(){
		ident--;
		if (ident<0)
			ident = 0;
	}
	
	protected void resetIdent(){
	    ident = 0;
	}
	
	/**
	 * Returns an empty line.
	 * @deprecated use emptyline.
	 */
	public static String writeEmptyline(){
		return CRLF;
	}

	/**
	 * Appends an empty line.
	 */
	public static void emptyline(){
		getCurrentJobContent().append(CRLF);
	}
	
	/**
	 * @deprecated  
	 * @param b
	 */
	protected static void emptyline(StringBuilder b){
		b.append(CRLF);
	}

	/**
	 * @deprecated use emptyline instead
	 */
	protected static void appendEmptyline(){
		emptyline(getCurrentJobContent());
	}

	/**
	 * @deprecated use clazz.addImport(imp) instead
	 */
	protected String writeImport(String imp){
		return writeString("import "+imp+";");
	}

	/**
	 * @deprecated use clazz.addImport(imp) instead
	 */
	protected void appendImport(String imp){
		appendString(getCurrentJobContent(), "import ", imp, ";");
	}

	/**
	 * @deprecated use clazz.addImport(imp) instead
	 */
	protected void appendImport(StringBuilder target, String imp){
		appendString(target, "import ", imp, ";");
	}

	/**
	 * @deprecated use clazz.addImport(imp) instead
	 */
	protected String writeImport(String packagename, String classname){
		return writeString("import "+packagename+"."+classname+";");
	}

	protected void closeBlock(StringBuilder b){
		decreaseIdent();
		b.append(writeString("}"));
	}
	
	/**
	 * @deprecated use closeBlock(String) or closeBlockNEW instead
	 */
	@Deprecated
	protected String closeBlock(){
		decreaseIdent();
		String ret = writeString("}");
		return ret;
	}

	protected void closeBlockNEW(){
		decreaseIdent();
		appendString("}");
	}

	/**
	 * Generates ident decreasing and block closing. Appends message just after block
	 * @param message comment message to block closing. Usually block/method name.
	 */
	protected void closeBlock(String message){
		decreaseIdent();
		appendString("} //"+message);
	}

	protected void appendMark(int markNumber){
		
//		String ret = "/* ***** MARK ";
//		ret += markNumber;
//		ret += ", Generator: "+this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
 //   	ret += " ***** */";
//		return emptyline()+writeString(ret)+emptyline();
	}

	/**
	 * @deprecated 
	 * @param commentline
	 * @return
	 */
	protected String writeCommentLine(String commentline){
		String[] tokens = StringUtils.tokenize(commentline, '\n');
		if (tokens.length!=1)
			return writeComment(commentline);
		String ret = writeString("// "+commentline);
    	return ret;
	}
	
	protected String writeComment(String commentline){
	    String[] tokens = StringUtils.tokenize(commentline, '\n');
	    String ret = "";
	    
	    ret += writeString("/**");
	    for (int i=0; i<tokens.length; i++){
	       ret += writeString(" * "+tokens[i]); 
	    }
	    ret += writeString(" */");
	    return ret;
	}

	protected void appendCommentLine(String commentline){
		appendCommentLine(getCurrentJobContent(), commentline);
	}
	
	protected void appendGenerationPoint(String point){
		appendCommentLine("Generated by: " + getClass() + "." + point);
		emptyline();
	}
	
	protected void appendCommentLine(StringBuilder target, String commentline){
		String[] tokens = StringUtils.tokenize(commentline, '\n');
		if (tokens.length!=1)
			appendComment(target, commentline);
		else
			appendString(target, "// ",commentline);
	}

	protected void appendComment(String commentline){
		appendComment(getCurrentJobContent(), commentline);
	}
	
	protected void appendComment(StringBuilder target, String commentline){
	    String[] tokens = StringUtils.tokenize(commentline, '\n');
	    
	    
	    appendString(target, "/**");
	    for (int i=0; i<tokens.length; i++){
	    	appendString(target, " * "+tokens[i]); 
	    }
	    appendString(target, " */");
	}

	protected static List<MetaViewElement> createMultilingualList(List<MetaViewElement> source, MetaDocument doc){
		List<MetaViewElement> ret = new ArrayList<MetaViewElement>();
		for (MetaViewElement e : source){
			if (e instanceof MetaFieldElement){
				MetaProperty p = doc.getField(e.getName());
				if (p==null){
					System.out.println("Can't find property for filed "+e.getName()+", skipped");
					continue;
				}
				if (!p.isMultilingual() || !GeneratorDataRegistry.getInstance().getContext().areLanguagesSupported()){
					ret.add(e);
				}else{
					for (String l : GeneratorDataRegistry.getInstance().getContext().getLanguages())
						ret.add(new MultilingualFieldElement(l,(MetaFieldElement)e));
				}
			}else{
				ret.add(e);
			}
		}

		return ret;
		
		
	}
	
	/**
	 * Returns the language of the selected multilingual element or null if the element is not multilingual.
	 * @param element
	 * @return
	 */
	protected String getElementLanguage(MetaViewElement element){
		return element instanceof MultilingualFieldElement ? ((MultilingualFieldElement)element).getLanguage() : null;
	}
	
	///////// NEW GENERATION INTERFACE ///////////
	/**
	 * Starts new job. Sets the parameter artefact as generated artefact.
	 * @param clazz
	 */
	public final void startNewJob(GeneratedArtefact clazz){
		GenerationJobManager.startNewJob(clazz);
	}

	/**
	 * @return the content of the currently active job
	 */
	public static final StringBuilder getCurrentJobContent(){
		return GenerationJobManager.getCurrentJob().getStringBuilder();
	}
	
	/**
	 * @return the artefact currently being generated
	 */
	public static final GeneratedArtefact getCurrentJob(){
		return GenerationJobManager.getCurrentJob().getArtefact();
	}

	/**
	 * Starts the body of a class. Resets the ident.
	 */
	protected void startClassBody(){
		ident = 1;
	}
}
