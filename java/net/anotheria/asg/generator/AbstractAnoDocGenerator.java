package net.anotheria.asg.generator;

import java.util.Iterator;
import java.util.List;

import net.anotheria.asg.generator.util.FileWriter;


/**
 * TODO please remined another to comment this class
 * @author another
 */
public class AbstractAnoDocGenerator {
	protected void runGenerator(IGenerator generator, IGenerateable target, Context context, List<FileEntry> results){
		List<FileEntry> tmp = generator.generate(target, context);
		for (Iterator<FileEntry> it = tmp.iterator(); it.hasNext(); )
			results.add(it.next());
		
	}
	
	protected void writeFiles(List<FileEntry> entries){
		for (int i=0; i<entries.size(); i++){
			FileEntry e = (FileEntry)entries.get(i);
			FileWriter.writeFile(e.getPath(), e.getName()+e.getType(), e.getContent());
		}
		
	}
}
