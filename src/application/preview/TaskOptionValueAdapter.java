package application.preview;

import org.daisy.dotify.api.tasks.TaskOptionValue;

/**
 * @author Joel Håkansson
 *
 */
public class TaskOptionValueAdapter {
	private final TaskOptionValue value;

	/**
	 * Creates a new task option value adapter.
	 * @param value the task option value
	 */
	public TaskOptionValueAdapter(TaskOptionValue value) {
		this.value = value;
	}
	
	/**
	 * Gets the task option value.
	 * @return returns the task option value
	 */
	public TaskOptionValue getValue() {
		return value;
	}

	@Override
	public String toString() {
		return value.getName();
	}

}
