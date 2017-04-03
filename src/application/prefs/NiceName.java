package application.prefs;

class NiceName {
	private final String key;
	private final String displayName;
	private final String description;

	NiceName(String key, String displayName) {
		this(key, displayName, null);
	}

	NiceName(String key, String displayName, String description) {
		this.key = key;
		this.displayName = displayName;
		this.description = description;
	}

	String getKey() {
		return key;
	}

	String getDisplayName() {
		return displayName;
	}
	
	String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return displayName;
	}

}
