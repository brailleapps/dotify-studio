package org.daisy.dotify.studio.api;

/**
 * Defines search options.
 * @author Joel Håkansson
 */
public final class SearchOptions {
	/**
	 * Provides a default set of options.
	 */
	public static final SearchOptions DEFAULT = new SearchOptions.Builder().build();
	private final boolean wrapAround;
	private final boolean matchCase;
	private final boolean reverse;

	/**
	 * Creates a builder for search options.
	 */
	public static class Builder {
		private boolean wrapAround = true;
		private boolean matchCase = false;
		private boolean reverse = false;

		/**
		 * Creates a new empty builder.
		 */
		public Builder() {
			
		}

		/**
		 * Sets the wrap around option. When true, the search will continue from the beginning
		 * when the end of the document is reached.
		 * @param value the value
		 * @return this builder
		 */
		public Builder wrapAround(boolean value) {
			this.wrapAround = value;
			return this;
		}

		/**
		 * Sets the case matching option. When true, only text with matching case will be found.
		 * @param value the value
		 * @return this builder
		 */
		public Builder matchCase(boolean value) {
			this.matchCase = value;
			return this;
		}
		
		/**
		 * Sets the option to reverse the search. When true, the search is performed backwards
		 * through the document.
		 * @param value the value
		 * @return this builder
		 */
		public Builder reverseSearch(boolean value) {
			this.reverse = value;
			return this;
		}

		/**
		 * Creates a new {@link SearchOptions} instance.
		 * @return a new instance
		 */
		public SearchOptions build() {
			return new SearchOptions(this);
		}
	}
	
	private SearchOptions(Builder builder) {
		this.wrapAround = builder.wrapAround;
		this.matchCase = builder.matchCase;
		this.reverse = builder.reverse;
	}

	/**
	 * Returns true if the search should wrap around when the end of the document is reached, false otherwise.
	 * @return true if the search should wrap around when the end of the document is reached, false otherwise
	 */
	public boolean shouldWrapAround() {
		return wrapAround;
	}

	/**
	 * Returns true if the found text must have the same case as the specified text, false otherwise.
	 * @return true if the found text must have the same case as the specified text, false otherwise
	 */
	public boolean shouldMatchCase() {
		return matchCase;
	}

	/**
	 * Returns true if the text should be performed backwards rather than forwards, false otherwise.
	 * @return true if the text should be performed backwards rather than forwards, false otherwise
	 */
	public boolean shouldReverseSearch() {
		return reverse;
	}
}
