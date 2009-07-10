package net.anotheria.asg.generator;

/**
 * Mostly currently executed generation job.
 * @author lrosenberg
 */
public class GenerationJob {
	/**
	 * The string builder with file content.
	 */
	private StringBuilder builder;
	/**
	 * Currently generated artefact.
	 */
	private GeneratedArtefact artefact;
	/**
	 * Creates and starts a new job.
	 */
	public GenerationJob(){
		reset();
	}
	
	public GenerationJob(StringBuilder aBuilder){
		builder = aBuilder;
		artefact = null;
	}
	/**
	 * Sets the generation of the given artefact as the current job.
	 * @param anArtefact
	 */
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
