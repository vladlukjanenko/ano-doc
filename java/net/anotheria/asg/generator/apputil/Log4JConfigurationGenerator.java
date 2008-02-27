package net.anotheria.asg.generator.apputil;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.AbstractGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;
import net.anotheria.asg.generator.IGenerateable;
import net.anotheria.asg.generator.IGenerator;
import net.anotheria.util.StringUtils;

public class Log4JConfigurationGenerator extends AbstractGenerator implements IGenerator {

	private static final String categories[] = new String[]{
		"debug","info","warn","error", "fatal"
	};
	
	public static final String GENERATED_PACKAGE = "net.anotheria.anosite.gen";
	
	public List<FileEntry> generate(IGenerateable g, Context context) {
		List<FileEntry> files = new ArrayList<FileEntry>();
		
		String fileContent = generateContent(context);
		
		FileEntry entry = new FileEntry("/etc/appdata", "log4j", fileContent);
		entry.setType(".xml");
		files.add(entry);
	
		return files;
	}
	
	private String generateContent(Context context){
	
		StringBuilder ret = new StringBuilder(5000);
		
		appendString(ret, "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		appendString(ret, "<!DOCTYPE log4j:configuration SYSTEM \"log4j.dtd\">");
		
		appendString(ret, "<log4j:configuration xmlns:log4j='http://jakarta.apache.org/log4j/'>");
		increaseIdent();
		emptyline(ret);
		
		appendString(ret , "<!-- File Appenders for generated code -->");
		emptyline(ret);
		
		for (String cat : categories){
		  	appendString(ret, "<!-- File Appender for ", cat, " and higher -->");
		  	appendString(ret, "<appender name=", quote(StringUtils.capitalize(cat) + "GenAppender"), " class="+quote("org.apache.log4j.RollingFileAppender"),">");
		  	increaseIdent();
		  	appendString(ret, "<param name=", quote("File"), " value=", quote("logs/"+context.getApplicationName()+"-gen-"+cat+".log"), " />");
		  	appendString(ret, "<param name=", quote("Threshold"), " value=", quote(cat.toUpperCase()), " />");
		  	appendString(ret, "<param name=", quote("MaxFileSize"), " value=",quote("100MB"), " />  ");     
		  	appendString(ret, "<param name=", quote("MaxBackupIndex"), " value=", quote(5), " />");
		  	appendString(ret, "<layout class=", quote("org.apache.log4j.PatternLayout"), ">");
		  	increaseIdent();
		  	appendString(ret, "<param name=", quote("ConversionPattern"), " value=", quote("%r %d{ISO8601} %-5p %c - %m%n"), "/>");
		  	decreaseIdent();
		  	appendString(ret, "</layout>");
			decreaseIdent();
		  	appendString(ret, "</appender>");
		}
		emptyline(ret);
		
		appendString(ret, "<!-- File Appenders for written code -->");
		emptyline(ret);

		for (String cat : categories){
		  	appendString(ret, "<!-- File Appender for ", cat, " and higher -->");
		  	appendString(ret, "<appender name=", quote(StringUtils.capitalize(cat) + "Appender"), " class="+quote("org.apache.log4j.RollingFileAppender"),">");
		  	increaseIdent();
		  	appendString(ret, "<param name=", quote("File"), " value=", quote("logs/"+context.getApplicationName()+"-"+cat+".log"), " />");
		  	appendString(ret, "<param name=", quote("Threshold"), " value=", quote(cat.toUpperCase()), " />");
		  	appendString(ret, "<param name=", quote("MaxFileSize"), " value=",quote("100MB"), " />  ");     
		  	appendString(ret, "<param name=", quote("MaxBackupIndex"), " value=", quote(5), " />");
		  	appendString(ret, "<layout class=", quote("org.apache.log4j.PatternLayout"), ">");
		  	increaseIdent();
		  	appendString(ret, "<param name=", quote("ConversionPattern"), " value=", quote("%r %d{ISO8601} %-5p %c - %m%n"), "/>");
		  	decreaseIdent();
		  	appendString(ret, "</layout>");
			decreaseIdent();
		  	appendString(ret, "</appender>");
		}
		emptyline(ret);
		
		appendString(ret, "<!-- Console appender -->");
		appendString(ret, "<appender name="+quote("ConsoleAppender"), " class=", quote("org.apache.log4j.ConsoleAppender"),">");
		increaseIdent();
		appendString(ret, "<param name="+quote("Threshold"), " value="+quote("WARN"), " />");
		appendString(ret, "<layout class="+quote("org.apache.log4j.PatternLayout"), ">");
		increaseIdent();
		appendString(ret, "<param name="+quote("ConversionPattern")," value=", quote("%r %d{ISO8601} %-5p %c - %m%n"), "/>");
		decreaseIdent();
		appendString(ret, "</layout>");
		decreaseIdent();
		appendString(ret, "</appender>");
		emptyline(ret);

		appendString(ret, "<logger name=", quote(context.getPackageName()), " additivity=", quote("false"), ">");
		increaseIdent(); 
		appendString(ret, "<level value=", quote("INFO"), "/>");
		for (String cat : categories)
			appendString(ret, "<appender-ref ref=",quote(StringUtils.capitalize(cat)+"GenAppender"), "/>");
		decreaseIdent();
		appendString(ret, "</logger>");
		emptyline(ret);
		
		appendString(ret, "<logger name=", quote("net.anotheria."+context.getApplicationName().toLowerCase()), " additivity=", quote("false"), ">");
		increaseIdent(); 
		appendString(ret, "<level value=", quote("INFO"), "/>");
		for (String cat : categories)
			appendString(ret, "<appender-ref ref=",quote(StringUtils.capitalize(cat)+"Appender"), "/>");
		decreaseIdent();
		appendString(ret, "</logger>");
		emptyline(ret);

		decreaseIdent();
		appendString(ret, "</log4j:configuration>");
		return ret.toString();
	}

	
}
