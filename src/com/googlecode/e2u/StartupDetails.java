package com.googlecode.e2u;

import java.io.File;
import java.util.Objects;

public class StartupDetails {
	private final Mode mode;
	private final File file;
	private final boolean display;
	private final boolean log;
	public enum Mode {
		UNDEFINED,
		SETUP,
		EMBOSS,
		OPEN,
		VIEW;
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
		} else if (builder.mode!=Mode.SETUP) {
			Objects.requireNonNull(builder.file);
		}
		this.mode = builder.mode;
		this.file = builder.file;
		this.display = builder.display;
		this.log = builder.log;
	}

	public static StartupDetails parse(String[] args) {
		if (args.length==0) {
			return new StartupDetails.Builder().build();
			/*
			page = "";
			content = new MainPage(null);*/
		} else if (args.length==1 && args[0].equalsIgnoreCase("-setup")) {
			return new StartupDetails.Builder().mode(Mode.SETUP).build();
			/*
			page = "index.html?method=setup";
			content = new MainPage(null);
			*/
		} else if (args.length==2 && args[0].equalsIgnoreCase("-emboss")) {
			return new StartupDetails.Builder().mode(Mode.EMBOSS)
				.file(new File(args[1])).build();
			/*
			content = new MainPage(new File(args[1]));
			page = "index.html?method=do";*/
		} else if (args.length==2 && args[0].equalsIgnoreCase("-open")) {
			return new StartupDetails.Builder().mode(Mode.OPEN)
				.file(new File(args[1]))
				.build();
			/*
			content = new MainPage(new File(args[1]));
			page = "view.html";*/
		} else if (args.length==2 && (args[0].equalsIgnoreCase("-view") || args[0].equalsIgnoreCase("-print"))) {
			return new StartupDetails.Builder().mode(Mode.VIEW).file(new File(args[1])).build();
			/*
			content = new MainPage(new File(args[1]));
			page = "";*/
		} else {
			return null;
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
