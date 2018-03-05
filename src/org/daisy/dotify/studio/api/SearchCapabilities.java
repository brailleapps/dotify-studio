package org.daisy.dotify.studio.api;

/**
 * Provides capabilities for a searchable implementation.
 * 
 * @author Joel Håkansson
 */
public final class SearchCapabilities {
	/**
	 * Defines no capabilities.
	 */
	public static final SearchCapabilities NONE = new SearchCapabilities.Builder().build();
	private final boolean wrapping;
	private final boolean matchCase;
	private final boolean direction;
	private final boolean find;
	private final boolean replace;

	/**
	 * Provides a builder for search capabilities.
	 */
	public static class Builder {
		private boolean wrapping = false;
		private boolean matchCase = false;
		private boolean direction = false;
		private boolean find = false;
		private boolean replace = false;

		/**
		 * Creates a new empty builder.
		 */
		public Builder() {
		}

		/**
		 * Sets the wrapping capability of this builder. When true, the implementation
		 * has the capability to support text wrapping as specified by {@link SearchOptions}.
		 * @param value the value
		 * @return this builder
		 */
		public Builder wrap(boolean value) {
			this.wrapping = value;
			return this;
		}
		
		/**
		 * Sets the case matching capability of this builder. When true, the implementation
		 * has the capability to support case matching as specified by {@link SearchOptions}.
		 * @param value the value
		 * @return this builder
		 */
		public Builder matchCase(boolean value) {
			this.matchCase = value;
			return this;
		}
		
		/**
		 * Sets the search direction capability of this builder. When true, the implementation
		 * has the capability to support search direction as specified by {@link SearchOptions}.
		 * @param value the value
		 * @return this builder
		 */
		public Builder direction(boolean value) {
			this.direction = value;
			return this;
		}
		
		/**
		 * Sets the find capability of this builder. When true, the implementation
		 * has the capability to support find as specified by {@link Searchable}.
		 * @param value the value
		 * @return this builder
		 */
		public Builder find(boolean value) {
			this.find = value;
			return this;
		}

		/**
		 * Sets the replace capability of this builder. When true, the implementation
		 * has the capability to support replace as specified by {@link Searchable}.
		 * @param value the value
		 * @return this builder
		 */
		public Builder replace(boolean value) {
			this.replace = value;
			return this;
		}
		
		/**
		 * Creates a new instance using the current state of the builder.
		 * @return a new {@link SearchCapabilities} instance
		 */
		public SearchCapabilities build() {
			return new SearchCapabilities(this);
		}
	}
	
	private SearchCapabilities(Builder builder) {
		this.wrapping = builder.wrapping;
		this.matchCase = builder.matchCase;
		this.direction = builder.direction;
		this.find = builder.find;
		this.replace = builder.replace;
	}

	/**
	 * Returns true if the wrapping properties can be configured, false otherwise. 
	 * @return true if the wrapping properties can be configured, false otherwise
	 */
	public boolean supportsWrapping() {
		return wrapping;
	}

	/**
	 * Returns true if case matching property can be configured, false otherwise.
	 * @return true if case matching property can be configured, false otherwise
	 */
	public boolean supportsCaseMatching() {
		return matchCase;
	}

	/**
	 * Returns true if the search direction can be configured, false otherwise.
	 * @return true if the search direction can be configured, false otherwise
	 */
	public boolean supportsSearchDirection() {
		return direction;
	}
	
	/**
	 * Returns true if the find operation is supported, false otherwise.
	 * @return true if the find operation is supported, false otherwise
	 */
	public boolean supportsFind() {
		return find;
	}
	
	/**
	 * Returns true if the replace operation is supported, false otherwise.
	 * @return true if the replace operation is supported, false otherwise
	 */
	public boolean supportsReplace() {
		return replace;
	}

}
