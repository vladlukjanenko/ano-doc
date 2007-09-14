package net.anotheria.asg.generator;

import java.util.List;

/**
 * TODO please remined another to comment this class
 * @author another
 */
public interface IGenerator {
	public List<FileEntry> generate(IGenerateable g, Context context);	
}
