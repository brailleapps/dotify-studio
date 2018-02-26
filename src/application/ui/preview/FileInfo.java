package application.ui.preview;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

class FileInfo {
	private final File file;
	private final boolean xml;
	private final boolean bom;
	private final Charset charset;
	
	static class Builder {
		private File file;
		private boolean xml = false;
		private boolean bom = false;
		private Charset charset = StandardCharsets.UTF_8;
		
		Builder(File f) {
			this.file = f;
		}
		
		Builder(FileInfo template) {
			this.file = template.file;
			this.xml = template.xml;
			this.bom = template.bom;
			this.charset = template.charset;
		}
		
		Builder file(File value) {
			this.file = value;
			return this;
		}
		
		Builder xml(boolean value) {
			this.xml = value;
			return this;
		}
		
		Builder bom(boolean value) {
			this.bom = value;
			return this;
		}
		
		Builder charset(Charset value) {
			this.charset = value;
			return this;
		}
		
		FileInfo build() {
			return new FileInfo(this);
		}
	}
	
	private FileInfo(Builder builder) {
		this.file = builder.file;
		this.xml = builder.xml;
		this.bom = builder.bom;
		this.charset = builder.charset;
	}
	
	public static FileInfo.Builder with(FileInfo template) {
		return new FileInfo.Builder(template);
	}

	public File getFile() {
		return file;
	}

	public boolean isXml() {
		return xml;
	}

	public boolean hasBom() {
		return bom;
	}
	
	public Charset getCharset() {
		return charset;
	}

}
