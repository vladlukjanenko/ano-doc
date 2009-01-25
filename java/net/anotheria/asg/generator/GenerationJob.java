package net.anotheria.asg.generator;

public class GenerationJob {
	private StringBuilder builder;
	
	public GenerationJob(){
		reset();
	}
	
	public GenerationJob(StringBuilder aBuilder){
		builder = aBuilder;
	}

	public StringBuilder getStringBuilder(){
		return builder;
	}
	
	public void reset(){
		builder = new StringBuilder(5000);
	}
}
