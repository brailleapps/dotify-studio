package org.daisy.dotify.studio.api;

import java.io.File;
import java.util.function.Consumer;

public interface OpenableEditor extends Editor {

	public Consumer<File> open(File f);
}
