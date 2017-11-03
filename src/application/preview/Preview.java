package application.preview;

import java.net.URI;
import java.util.Optional;

public interface Preview {
	
	/**
	 * Returns true if this preview can save (it has a location)
	 */
	public boolean canSave();
	
	/**
	 * Saves the file to the current location.
	 * @throws IllegalStateException if no location has been set
	 */
	public void save();

	/**
	 * Reload from disk
	 */
	public void reload();

	/**
	 * Gets the url for the book in the view.
	 * @return returns the url
	 */
	public String getURL();
	
	/**
	 * Shows the emboss dialog.
	 */
	public void showEmbossDialog();
	
	/**
	 * Gets the uri for the book
	 * @return returns the uri
	 */
	public Optional<URI> getBookURI();
	
	/**
	 * Informs the controller that it should close.
	 */
	public void closing();

}
