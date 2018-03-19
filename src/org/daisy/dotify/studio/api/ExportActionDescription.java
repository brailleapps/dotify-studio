package org.daisy.dotify.studio.api;

/**
 * Provides a description of an export action.
 * @author Joel Håkansson
 */
public final class ExportActionDescription {
	private final String name;
	private final String identifier;
	
	/**
	 * Provides a builder for {@link ExportActionDescription}s.
	 */
	public static class Builder {
		private final String identifier;
		private String name = "";

		/**
		 * Creates a new builder with the specified identifier
		 * @param identifier the identifier
		 */
		public Builder(String identifier) {
			this.identifier = identifier;
		}

		/**
		 * Sets the name for this descriptor
		 * @param value the name
		 * @return this builder
		 */
		public Builder name(String value) {
			this.name = value;
			return this;
		}

		/**
		 * Builds the description using the current state of the builder.
		 * @return returns a new {@link ExportActionDescription}
		 */
		public ExportActionDescription build() {
			return new ExportActionDescription(this);
		}
	}
	
	private ExportActionDescription(Builder builder) {
		this.name = builder.name;
		this.identifier = builder.identifier;
	}

	/**
	 * Gets the name of the export action.
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the identifier for the export action
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

}
