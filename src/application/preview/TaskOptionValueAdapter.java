package application.preview;

import org.daisy.dotify.api.tasks.TaskOptionValue;

/**
 * @author Joel Håkansson
 *
 */
public class TaskOptionValueAdapter {
	private final TaskOptionValue value;

	public TaskOptionValueAdapter(TaskOptionValue value) {
		this.value = value;
	}
	
	public TaskOptionValue getValue() {
		return value;
	}

	@Override
	public String toString() {
		return value.getName();
	}

}
