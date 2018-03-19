package org.daisy.dotify.studio.api;

import java.util.Map;

import org.daisy.streamline.api.media.FileDetails;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;

/**
 * Provides a wrapper class for {@link FileDetails} which uses {@link Property} subclasses
 * for its properties instead of regular strings and maps. The purpose is to allow
 * bindings for these properties.
 * 
 * @author Joel Håkansson
 */
public final class FileDetailsProperty implements FileDetails {
	private SimpleStringProperty formatName = new SimpleStringProperty();
	private SimpleStringProperty extension = new SimpleStringProperty();
	private SimpleStringProperty mediaType = new SimpleStringProperty();
	private SimpleMapProperty<String, Object> properties = new SimpleMapProperty<String, Object>();
	
	/**
	 * Creates a new empty instance
	 */
	public FileDetailsProperty() {
	}
	
	/**
	 * Creates a new instance with the specified details.
	 * @param details the details
	 */
	public FileDetailsProperty(FileDetails details) {
		setFileDetails(details);
	}
	
	/**
	 * Sets all properties of this instance using the specified details 
	 * template.
	 * @param template the template
	 */
	public void setFileDetails(FileDetails template) {
		formatName.set(template.getFormatName());
		extension.set(template.getExtension());
		mediaType.set(template.getMediaType());
		properties.set(FXCollections.observableMap(template.getProperties()));
	}

	@Override
	public String getFormatName() {
		return formatName.getValue();
	}
	
	/**
	 * Gets the format name as a property. See {@link #getFormatName()}.
	 * @return the format name property
	 */
	public SimpleStringProperty formatNameProperty() {
		return formatName;
	}

	@Override
	public String getExtension() {
		return extension.getValue();
	}
	
	/**
	 * Gets the extension as a property. See {@link #getExtension()}.
	 * @return the extension property
	 */
	public SimpleStringProperty extensionProperty() {
		return extension;
	}

	@Override
	public String getMediaType() {
		return mediaType.getValue();
	}
	
	/**
	 * Gets the media type as a property. See {@link #getMediaType()}.
	 * @return the media type property
	 */
	public SimpleStringProperty mediaTypeProperty() {
		return mediaType;
	}

	@Override
	public Map<String, Object> getProperties() {
		return properties.getValue();
	}
	
	/**
	 * Gets the file details properties as a property. See {@link #getProperties()}.
	 * @return the properties as a property
	 */
	public SimpleMapProperty<String, Object> propertiesProperty() {
		return properties;
	}

}
