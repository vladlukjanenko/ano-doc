package net.anotheria.asg.generator;

public class GenerationJobManager {

	private GenerationJobManager() {
	}

	/**
	 * Currently executed job.
	 */
	private static ThreadLocal<GenerationJob> currentJob = new ThreadLocal<GenerationJob>(){
		@Override
		protected synchronized GenerationJob initialValue(){
			return new GenerationJob();
		}
	};
	
	public static GenerationJob getCurrentJob(){
		return currentJob.get();
	}
	
	public static void startNewJob(){
		currentJob.set(new GenerationJob());
	}

	public static void startNewJob(GeneratedArtefact artefact){
		currentJob.set(new GenerationJob(artefact));
	}
}
