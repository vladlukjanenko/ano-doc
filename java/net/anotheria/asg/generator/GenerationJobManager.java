package net.anotheria.asg.generator;

public class GenerationJobManager {
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

	public static void startNewJob(GeneratedClass clazz){
		currentJob.set(new GenerationJob(clazz.getBody()));
	}
}
