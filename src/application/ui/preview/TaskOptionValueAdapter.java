package application.ui.preview;

import org.daisy.streamline.api.option.UserOptionValue;

/**
 * @author Joel Håkansson
 *
 */
public class TaskOptionValueAdapter {
	private final UserOptionValue value;

	/**
	 * Creates a new task option value adapter.
	 * @param value the task option value
	 */
	public TaskOptionValueAdapter(UserOptionValue value) {
		this.value = value;
	}
	
	/**
	 * Gets the task option value.
	 * @return returns the task option value
	 */
	public UserOptionValue getValue() {
		return value;
	}

	@Override
	public String toString() {
		return value.getDisplayName();
	}

}
