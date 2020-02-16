/**
 * 
 */
package application.ui;

/**
 * @author Joel Håkansson
 *
 */
public class EditorNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5387198189070523684L;

	/**
	 * 
	 */
	public EditorNotFoundException() {
	}

	/**
	 * @param message
	 */
	public EditorNotFoundException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public EditorNotFoundException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public EditorNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public EditorNotFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
