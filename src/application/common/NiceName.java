package application.common;

public class NiceName {
	private final String key;
	private final String displayName;
	private final String description;

	public NiceName(String key, String displayName) {
		this(key, displayName, null);
	}

	public NiceName(String key, String displayName, String description) {
		this.key = key;
		this.displayName = displayName;
		this.description = description;
	}

	public String getKey() {
		return key;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return displayName;
	}

}
