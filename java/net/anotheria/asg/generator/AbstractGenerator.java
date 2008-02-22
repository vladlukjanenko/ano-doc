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
 * TODO please remined another to comment this class
 * @author another
 */
public class AbstractGenerator{

	protected String quote(String s){
		return "\""+s+"\"";
	}
	
	protected String quote(StringBuilder s){
		return "\""+s.toString()+"\"";
	}

	protected String quote(int a){
		return quote(""+a);
	}

	protected String writeIncreasedString(String s){
		increaseIdent();
		String ret = writeString(s);
		decreaseIdent();
		return ret;
	}
	
	protected String writeIncreasedStatement(String s){
		return writeIncreasedString(s+";");
	}
	
	public static final String CRLF = "\n";
	
	/**
	 * Current ident.
	 */
	protected int ident = 0;

	/**
	 * Writes a string in a new line with ident and linefeed.
	 * @param s string to write.
	 * @return
	 */
	protected String writeString(String s){
		String ret = getIdent();
		ret += s;
		ret += CRLF;
		return ret; 
	}
	
	protected String openTry(){
		String ret = writeString("try{");
		increaseIdent();
		return ret;
	}

	protected String openFun(String s){
		if (!s.endsWith("{"))
			s+=" {";
		String ret = writeString(s);
		increaseIdent();
		return ret;
	}

	
	/**
	 * Writes a statement (';' at the end of the line)
	 * @param s statement to write.
	 * @return
	 */
	protected String writeStatement(String s){
		String ret = getIdent();
		ret += s;
		ret += ";";
		ret += CRLF;
		return ret; 
	}

	/**
	 * Returns current ident as string.
	 * @return a string with "\t"s.
	 */
	private String getIdent(){
		String ret = "";
		for (int i=0; i<ident; i++)
			ret += "\t";
		return ret;
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
	 */
	protected static String emptyline(){
		return CRLF;
	}
	
	protected static void emptyline(StringBuilder b){
		b.append(CRLF);
	}

	protected String writeImport(String imp){
		return writeString("import "+imp+";");
	}

	protected String writeImport(String packagename, String classname){
		return writeString("import "+packagename+"."+classname+";");
	}

	protected void closeBlock(StringBuilder b){
		decreaseIdent();
		b.append(writeString("}"));
	}
	
	protected String closeBlock(){
		decreaseIdent();
		String ret = writeString("}");
		return ret;
	}

	protected String writeMark(int markNumber){
		String ret = "/* ***** MARK ";
		ret += markNumber;
		ret += ", Generator: "+this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
    	ret += " ***** */";
		return emptyline()+writeString(ret)+emptyline();
	}

	protected String writeCommentLine(String commentline){
		String tokens[] = StringUtils.tokenize(commentline, '\n');
		if (tokens.length!=1)
			return writeComment(commentline);
		String ret = writeString("// "+commentline);
    	return ret;
	}
	
	protected String writeComment(String commentline){
	    String tokens[] = StringUtils.tokenize(commentline, '\n');
	    String ret = "";
	    
	    ret += writeString("/**");
	    for (int i=0; i<tokens.length; i++){
	       ret += writeString(" * "+tokens[i]); 
	    }
	    ret += writeString(" */");
	    return ret;
	}

	protected static List<MetaViewElement> createMultilingualList(List<MetaViewElement> source, MetaDocument doc, Context context){
		List<MetaViewElement> ret = new ArrayList<MetaViewElement>();
		for (MetaViewElement e : source){
			if (e instanceof MetaFieldElement){
				MetaProperty p = doc.getField(e.getName());
				if (p==null){
					System.out.println("Can't find property for filed "+e.getName()+", skipped");
					continue;
				}
				if (!p.isMultilingual() || !context.areLanguagesSupported()){
					ret.add(e);
				}else{
					for (String l : context.getLanguages())
						ret.add(new MultilingualFieldElement(l,(MetaFieldElement)e));
				}
			}else{
				ret.add(e);
			}
		}

		return ret;
		
		
	}
	
	protected String getElementLanguage(MetaViewElement element){
		return element instanceof MultilingualFieldElement ? ((MultilingualFieldElement)element).getLanguage() : null;
	}

}
