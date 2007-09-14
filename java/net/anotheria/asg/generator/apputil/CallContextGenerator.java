package net.anotheria.asg.generator.apputil;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.util.StringUtils;

public class CallContextGenerator extends AbstractGenerator implements IGenerator {

	private Context context;
	
	public List<FileEntry> generate(IGenerateable g, Context context) {
		this.context = context;
		
		List<FileEntry> ret = new ArrayList<FileEntry>();
		ret.add(generateCallContextFactory());
		ret.add(generateCallContext());
		return ret;
	}

	private FileEntry generateCallContext(){
		String ret = "";
		ret += writeStatement("package "+getPackageName());
		ret += emptyline();
		
		ret += writeImport("net.anotheria.anodoc.util.context.CallContext");
		ret += emptyline(); 
		
		String className = getCallContextName(context);
		String classDecl = "public class "+className+" extends CallContext{";
		ret += writeString(classDecl);
		ret += emptyline();
		increaseIdent();
		
		ret += writeString("public String getDefaultLanguage(){");
		increaseIdent();
		ret += writeStatement("return "+quote(context.getDefaultLanguage()));
		ret += closeBlock();
		
		ret += closeBlock();
		
		return new FileEntry(FileEntry.package2path(getPackageName()), className , ret);
		
		
	}
	
	private FileEntry generateCallContextFactory(){
		String ret = "";
		
		ret += writeStatement("package "+getPackageName());
		ret += emptyline();
		
		ret += writeImport("net.anotheria.anodoc.util.context.CallContextFactory");
		ret += writeImport("net.anotheria.anodoc.util.context.CallContext");
		ret += emptyline();
		
		String className = getFactoryName(context);
		String classDecl = "public class "+className+" implements CallContextFactory{";
		ret += writeString(classDecl);
		ret += emptyline();
		increaseIdent();
		
		ret += writeString("public CallContext createContext(){");
		increaseIdent();
		ret += writeStatement("return new "+getPreName()+"CallContext()");
		ret += closeBlock();
		
		ret += closeBlock();
		
		return new FileEntry(FileEntry.package2path(getPackageName()), className , ret);
	}
	
	private static String getPreName(Context context){
		return StringUtils.capitalize(context.getApplicationName());
	}

	private String getPreName(){
		return getPreName(context);
	}

	private String getPackageName(){
		return getPackageName(context);
	}
	
	private static String getPackageName(Context context){
		return context.getPackageName()+".util";
	}

	public static final String getFullFactoryName(Context c){
		return getPackageName(c)+"."+getFactoryName(c);
	}
	
	public static String getFactoryName(Context c){
		return getPreName(c)+"CallContextFactory";
	}
	public static String getCallContextName(Context c){
		return getPreName(c)+"CallContext";
	}

}
