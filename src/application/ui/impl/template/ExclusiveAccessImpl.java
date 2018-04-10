package application.ui.impl.template;

import java.io.File;

import org.daisy.dotify.common.io.InterProcessLock;
import org.daisy.dotify.common.io.LockException;
import org.daisy.streamline.api.config.ExclusiveAccess;
import org.daisy.streamline.api.config.ExclusiveAccessException;

class ExclusiveAccessImpl implements ExclusiveAccess {
	private final InterProcessLock lock;
	
	public ExclusiveAccessImpl(File lockFile) {
		this.lock = new InterProcessLock(lockFile);
	}

	@Override
	public boolean acquire() {
		try {
			return lock.lock();
		} catch (LockException e) {
			throw new ExclusiveAccessException(e);
		}
	}

	@Override
	public void release() {
		lock.unlock();
	}

}
