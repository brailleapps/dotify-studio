package application.common;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ObservableValue;

/**
 * Provides a way to keep bindings from being garbage collected without declaring lots of instance variables.
 * Use {@link #add(ObservableValue)} to store the binding while adding it to the bound object.
 * Use {@link #clear()} to remove all expired bindings before adding new ones. For example:
 *  
 * <pre>{@code
 * 
 *  BindingStore bindings = new BindingStore();
 *  ...
 *  bindings.clear();
 *  variable.bind(bindings.add(a.and(b)));
 * 
 * }</pre>

 * @author Joel Håkansson
 */
public class BindingStore {
	private final List<ObservableValue<?>> bindings;
	
	public BindingStore() {
		this.bindings = new ArrayList<>();
	}
	
	/**
	 * Adds the observable to the store.
	 * @param v the observable
	 * @return the observable
	 */
	public <T extends ObservableValue<?>> T add(T v) {
		bindings.add(v);
		return v;
	}
	
	/**
	 * Removes all stored elements.
	 */
	public void clear() {
		bindings.clear();
	}

}