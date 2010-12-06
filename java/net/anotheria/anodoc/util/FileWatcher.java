package net.anotheria.anodoc.util;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class for watching file existence. If file not exists - FileWatcher creates it and fire onChange event.
 *
 * @author dsilenko
 */
public abstract class FileWatcher {

	/**
	 * Path for file to watch.
	 */
	private String filePath;
	/**
	 * Period time in milliseconds between successive task executions.
	 */
	private long period;
	/**
	 * Timer for watching.
	 */
	private Timer timer;

	/**
	 * @param aFilePath path for file to watch.
	 * @param aPeriod delay for starting and period time in milliseconds between successive task executions.
	 */
	public FileWatcher(String aFilePath, long aPeriod) {
		filePath = aFilePath;
		period = aPeriod;
	}

	/**
	 * Starting file watching task.
	 */
	public void start(){
		stop();
		timer = new Timer("FileWatcher("+filePath+")", true);
		timer.schedule(new FileWatcherTask(filePath), period, period);

	}

	/**
	 * Stopping file watching task.
	 */
	public void stop(){
		if (timer!=null){
			timer.cancel();
			timer.purge();
			timer=null;
		}
	}

	/**
	 * Event when file was not exist.
	 */
	protected abstract void onChange();

	private class FileWatcherTask extends TimerTask {
		private File file;

		public FileWatcherTask(String aFilePath) {
			this.file = new File(aFilePath);
		}

		@Override
		public final void run() {
			//System.out.println("check file("+file.getAbsolutePath()+")");

			if (!file.exists()){
				try {
					file.getParentFile().mkdirs();
					file.createNewFile();
					onChange();
				} catch (IOException e) {
					System.out.println("FileWatcherTask run " + e);
				}
			}

		}
	}

}
