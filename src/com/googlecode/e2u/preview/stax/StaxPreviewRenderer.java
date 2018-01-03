package com.googlecode.e2u.preview.stax;

import java.io.File;
import java.util.List;

import org.daisy.braille.utils.pef.PEFBook;
import org.daisy.streamline.api.validity.ValidationReport;

import javafx.concurrent.Task;

class StaxPreviewRenderer {
	private final StaxPreviewParser parser;

	StaxPreviewRenderer(PEFBook book, ValidationReport report) {
		this.parser = new StaxPreviewParser(book, report);
		Task<Void> t = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				parser.staxParse();
				return null;
			}
		};
        Thread th = new Thread(t);
        th.setDaemon(true);
        th.start();
	}
	
	void abort() {
		parser.abort();
	}

	File getFile(int v) throws InterruptedException {
		if (v<1 || v>parser.getBook().getVolumes()) {
			throw new IndexOutOfBoundsException();
		}
		try {
			List<File> volumes = parser.getVolumes(); 
			while (volumes.size()<v) {
				Thread.sleep(100);
			}
			return volumes.get(v-1);
		} catch (InterruptedException e) {
			throw e;
		}
	}
}
