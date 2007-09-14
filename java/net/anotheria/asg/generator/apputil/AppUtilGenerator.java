package net.anotheria.asg.generator.apputil;

import java.util.ArrayList;
import java.util.List;

import net.anotheria.asg.generator.AbstractAnoDocGenerator;
import net.anotheria.asg.generator.Context;
import net.anotheria.asg.generator.FileEntry;

public class AppUtilGenerator extends AbstractAnoDocGenerator{
	
	private Context context;
	
	public AppUtilGenerator(Context aContext){
		context = aContext;
	}
	
	public void generate(){
		List<FileEntry> files = new ArrayList<FileEntry>();
		files.addAll(new CallContextGenerator().generate(null, context));
		
		writeFiles(files);
	}
}
