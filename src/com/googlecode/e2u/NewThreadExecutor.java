package com.googlecode.e2u;

import java.util.concurrent.Executor;

public class NewThreadExecutor implements Executor {

	@Override
	public void execute(Runnable r) {
		new Thread(r).start();
	}

}
