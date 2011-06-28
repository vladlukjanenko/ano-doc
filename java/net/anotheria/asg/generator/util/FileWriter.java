package net.anotheria.asg.generator.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import net.anotheria.util.IOUtils;
import net.anotheria.util.StringUtils;

/**
 * Utility for writing code to files.
 * @author lrosenberg.
 *
 */
public class FileWriter {

	private FileWriter() {
	}

	private static String BASE_DIR;
	
	public static final String DEF_BASE_DIR = ".";
	
	static{
		BASE_DIR = DEF_BASE_DIR;
	}
	
	public static final void writeFile(String path, String fileName, String content){
		writeFile(path, fileName, content, false);
	}

	public static final void writeFile(String path, String fileName, String content, boolean override){
		if (content==null || content.length()==0){
			//System.out.println("IGNORE emptyfile "+fileName );
			return;
		}
		if (path==null)
			path = "";
		if (path.length()>0 && !path.endsWith("/"))
			path += "/";
			
		File fDir = new File(BASE_DIR+"/"+path);
		fDir.mkdirs();
		File f = new File(BASE_DIR+"/"+path+fileName);
		if (f.exists() && !override){
			FileInputStream fIn = null;
			try{
				fIn = new FileInputStream(f);
				byte[] d = new byte[fIn.available()];
				fIn.read(d);
				if (content.equals(new String(d))){
					//System.out.println("Skipping "+f);
					return;					
				}
					
			}catch(IOException e){
			}finally{
				IOUtils.closeIgnoringException(fIn);
			}
		}
		System.out.println("writing "+f);
		FileOutputStream fOut = null;
		try{
		
			fOut = new FileOutputStream(f);
			fOut.write(content.getBytes());
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			IOUtils.closeIgnoringException(fOut);
		}
	}
	
	public static final void writeJavaFile(String packageName, String className, String content){
		String[] tokens = StringUtils.tokenize(packageName, '.');
		String path = "";
		for (int i=0; i<tokens.length; i++){
			path += tokens[i];
			if (i< tokens.length-1)
				path += "/";
		}
		writeFile(path, className+".java", content, false);
	}
	
	public static void setBaseDir(String aBaseDir){
		BASE_DIR = aBaseDir;
	}
	
	public static void main(String []a){
		
	}
}
