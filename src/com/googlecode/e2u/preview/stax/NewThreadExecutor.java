package com.googlecode.e2u.preview.stax;

import java.util.concurrent.Executor;

public class NewThreadExecutor implements Executor {

	@Override
	public void execute(Runnable r) {
		new Thread(r).start();
	}

}
