package net.anotheria.asg.generator.parser;

import java.io.File;
import java.io.IOException;
import java.util.List;


import net.anotheria.util.IOUtils;
import net.anotheria.util.StringUtils;

/**
 * This class preprocess an xml file. Its mainly used to put together a splitted xml file.
 * @author another
 *
 */
public class XMLPreprocessor {
	
	public static final String INCLUDE_COMMAND = "@include:";
	
	public static void main(String a[]) throws IOException{
		String file = loadFile(new File("../ano-site/etc/def/datadef.xml"));
		System.out.println("file: "+file);
	}
	
	/**
	 * Loads a file from the disk. Include marks are replaced with the content of the included files.
	 * @param f
	 * @return
	 * @throws IOException
	 */
	public static String loadFile(File f) throws IOException{
		if (!f.exists())
			throw new IOException("File doesn't exists: "+f.getAbsolutePath());
		String content = IOUtils.readFileAtOnceAsString(f.getAbsolutePath());
		
		List<String> tags = StringUtils.extractTags(content, '<', '>');
		for (String t : tags){
			if (!t.startsWith("<!--"))
				continue;
			int indexOfIncludeCommand = t.indexOf(INCLUDE_COMMAND);
			if (indexOfIncludeCommand==-1)
				continue;
			int space = t.indexOf(' ', indexOfIncludeCommand);
			String includeTarget = t.substring(indexOfIncludeCommand+INCLUDE_COMMAND.length(), space);
			File toInclude = new File(f.getParentFile()+File.separator+includeTarget);
			if (!toInclude.exists())
				throw new IOException("File to include doesn't exists: "+toInclude.getAbsolutePath()+" included from "+f.getAbsolutePath());
			String includeContent = loadFile(toInclude);
			content = StringUtils.replaceOnce(content, t, includeContent);
		}
		
		return content;
	}
}
