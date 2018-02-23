package com.googlecode.e2u;

import java.io.File;
import java.util.Optional;

public class StartupDetails {
	private final Mode mode;
	private final File file;
	private final boolean display;
	private final boolean log;
	public enum Mode {
		UNDEFINED,
		OPEN;
	}
	
	public static class Builder {
		private Mode mode = Mode.UNDEFINED;
		private File file = null;
		private boolean display = true;
		private boolean log = true;
		
		public Builder() {
		}
		
		Builder(StartupDetails template) {
			this.mode = template.mode;
			this.file = template.file;
			this.display = template.display;
			this.log = template.log;
		}
		
		public Builder mode(Mode value) {
			this.mode = value;
			return this;
		}
		
		public Builder file(File f) {
			this.file = f;
			return this;
		}
		
		public Builder log(boolean value) {
			this.log = value;
			return this;
		}
		
		public Builder display(boolean value) {
			this.display = value;
			return this;
		}

		public StartupDetails build() {
			return new StartupDetails(this);
		}
	}
	
	private StartupDetails(Builder builder) {
		if (builder.mode==Mode.UNDEFINED) {
			if (builder.file!=null) {
				throw new IllegalArgumentException("Illegal combination: " + builder.mode + " / " + builder.file);
			}
		}
		this.mode = builder.mode;
		this.file = builder.file;
		this.display = builder.display;
		this.log = builder.log;
	}

	/**
	 * Parses the supplied string array for startup commands.
	 * @param args the arguments
	 * @return the startup details
	 */
	public static Optional<StartupDetails> parse(String[] args) {
		if (args.length==0) {
			return Optional.of(new StartupDetails.Builder().build());
		} else if (args.length==2 && args[0].equalsIgnoreCase("-open")) {
			return Optional.of(new StartupDetails.Builder().mode(Mode.OPEN)
				.file(new File(args[1]))
				.build());
		} else {
			return Optional.empty();
		}
	}
	
	public static StartupDetails open(File f) {
		return new StartupDetails.Builder().mode(Mode.OPEN).file(f).build();
	}
	
	
	public static Builder with(StartupDetails template) {
		return new StartupDetails.Builder(template);
	}

	public Mode getMode() {
		return mode;
	}

	public File getFile() {
		return file;
	}
	
	public boolean shouldLog() {
		return log;
	}
	
	public boolean shouldDisplay() {
		return display;
	}

}
