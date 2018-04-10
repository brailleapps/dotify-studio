package application.ui.preview;

/**
 * The purpose of this class is to provide an object that can be synchronized on
 * when the data variable is null or modified.
 * @author Joel Håkansson
 */
class Wrapper<T> {
	private T value = null;

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

}
