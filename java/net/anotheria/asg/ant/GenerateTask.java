package net.anotheria.asg.ant;

import java.io.File;

import net.anotheria.asg.generator.Generator;
import net.anotheria.asg.generator.util.FileWriter;

import org.apache.tools.ant.Task;

public class GenerateTask extends Task {
	
	private String outputDir;
	private String baseDir;
	
	public void execute(){
		
		try{
			log("Starting generation with "+Generator.getProductString()+" into: "+outputDir+" from: "+baseDir);
			FileWriter.setBaseDir(outputDir);
			Generator.setBaseDir(getBaseDir()+File.separatorChar);
			Generator.generate();
			log("Generation complete");
		}catch(Exception e){
			log("Error: "+e.getMessage());
		}
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	public String getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}
}
