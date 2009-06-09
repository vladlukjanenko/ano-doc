package net.anotheria.asg.generator;

public class GenerationJob {
	private StringBuilder builder;
	private GeneratedArtefact artefact;
	
	public GenerationJob(){
		reset();
	}
	
	public GenerationJob(StringBuilder aBuilder){
		builder = aBuilder;
		artefact = null;
	}

	public GenerationJob(GeneratedArtefact anArtefact){
		artefact = anArtefact;
		builder = artefact.getBody();
	}

	public StringBuilder getStringBuilder(){
		return builder;
	}
	
	public GeneratedArtefact getArtefact(){
		return artefact;
	}
	
	public void reset(){
		builder = new StringBuilder(5000);
		artefact = null;
	}
	
	public void setBuilder(StringBuilder target){
		builder = target;
	}
}
