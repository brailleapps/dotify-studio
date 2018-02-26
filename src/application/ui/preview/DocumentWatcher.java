package application.ui.preview;

import java.io.File;
import java.util.logging.Logger;

/**
 * Provides a runnable that performs an action whenever
 * the specified file's modified date is updated to a later
 * date.
 * @author Joel Håkansson
 *
 */
abstract class DocumentWatcher implements Runnable {
	private static final Logger logger = Logger.getLogger(DocumentWatcher.class.getCanonicalName());
	protected final File file;
	private final long pollTime;
	private long modified;

	/**
	 * Creates a new document watcher for the specified file.
	 * @param input the file to watch
	 */
	DocumentWatcher(File input) {
		this(input, 1000);
	}

	/**
	 * Creates a new document watcher for the specified file and 
	 * poll time.
	 * @param input the file to watch
	 * @param pollTime the time to sleep between polling, in milliseconds
	 */
	DocumentWatcher(File input, long pollTime) {
		this.file = input;
		this.pollTime = pollTime;
		this.modified = input.lastModified();
	}
	
	/**
	 * Returns true if the watcher should stay alive. Unless overridden,
	 * the file should be monitored as long as it exists.
	 * @return true if the watcher should stay alive, false otherwise
	 */
	boolean shouldMonitor() {
		return true;
	}

	/**
	 * Returns true if the watcher should perform its action at this time.
	 * Unless overridden, the action should be performed if the file's modified
	 * date is more recent than what it was last time the action was performed.
	 * @return true if the watcher should perform its action, false otherwise
	 */
	boolean shouldPerformAction() {
		return file.exists() && modified<file.lastModified();
	}
	
	/**
	 * Performs the action.
	 */
	abstract void performAction();

	@Override
	public void run() { 
		while (shouldMonitor()) {
			if (shouldPerformAction()) {
				modified = file.lastModified();
				logger.fine("Updating " + file.getAbsolutePath());
				performAction();
				logger.info("Waiting for changes in " + file);
			}
			try {
				Thread.sleep(pollTime);
			} catch (InterruptedException e) {
			}
		}
		logger.info("Removing watcher on " + file);
	}
}