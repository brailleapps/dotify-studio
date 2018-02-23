package com.googlecode.e2u;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

public final class StartupDetails {
	private final File file;
	private final boolean display;
	private final boolean log;

	public static class Builder {
		private final File file;
		private boolean display = true;
		private boolean log = true;
		
		public Builder(File f) {
			this.file = Objects.requireNonNull(f);
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
		if (args.length==2 && args[0].equalsIgnoreCase("-open")) {
			return Optional.of(new StartupDetails.Builder(new File(args[1])).build());
		} else {
			return Optional.empty();
		}
	}
	
	public static StartupDetails open(File f) {
		return new StartupDetails.Builder(f).build();
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
