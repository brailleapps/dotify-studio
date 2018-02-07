package application.ui.template;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.daisy.streamline.api.config.ConfigurationDetails;
import org.daisy.streamline.api.config.ConfigurationsCatalogService;
import org.daisy.streamline.api.config.ConfigurationsProviderException;

import application.l10n.Messages;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import shared.Singleton;

/**
 * Provides a controller for a template view.
 * @author Joel Håkansson
 *
 */
public class TemplateController {
	private static final Logger logger = Logger.getLogger(TemplateController.class.getCanonicalName());
	@FXML private VBox templates;
	@FXML private Label title;
	private String selected = null;
	
	/**
	 * Creates a new template controller.
	 */
	public TemplateController() {
		
	}
	
	private boolean isEmpty() {
		return getConfigurationsCatalog().getConfigurationDetails().isEmpty();
	}

	@FXML void initialize() {
		if (!isEmpty()) {
			List<ConfigurationDetails> sortedDetails = getConfigurationsCatalog().getConfigurationDetails().stream()
					.sorted((o1, o2) -> {
						return o1.getKey().compareTo(o2.getKey());
					})
					.collect(Collectors.toList());
			for (ConfigurationDetails conf : sortedDetails) {
				boolean removable = getConfigurationsCatalog().isRemovable(conf.getKey());
				ConfigurationItem item = new ConfigurationItem(conf.getNiceName(), conf.getDescription(), removable);
				item.setApplyAction(ev -> {
					selected = conf.getKey();
					((Stage)templates.getScene().getWindow()).close();
				});
				if (removable) {
					item.setRemoveAction(ev -> {
						getConfigurationsCatalog().removeConfiguration(conf.getKey());
						templates.getChildren().remove(item);
					});
				}
				addItem(item);
			}
			ConfigurationItem item = new ConfigurationItem(Messages.LABEL_NONE.localize(), "", false);
			item.setApplyAction(ev -> {
				selected = null;
				closeWindow();
			});
			addItem(item);
		}
	}

	private ConfigurationsCatalogService getConfigurationsCatalog() {
		return Singleton.getInstance().getConfigurationsCatalog();
	}
	
	void closeWindow() {
		((Stage)templates.getScene().getWindow()).close();
	}
	
	void setHeading(String value) {
		title.setText(value);
	}

	boolean hasTemplates() {
		return !isEmpty();
	}
	
	Map<String, Object> getSelectedConfiguration() {
		if (selected==null) {
			return Collections.emptyMap();
		} else {
			try {
				return getConfigurationsCatalog().getConfiguration(selected);
			} catch (ConfigurationsProviderException e) {
				logger.log(Level.WARNING, "Failed to load configuration with key: " + selected, e);
				return Collections.emptyMap();
			}
		}
	}

	private void addItem(Node item) {
		VBox.setMargin(item, new Insets(0, 0, 10, 0));
		templates.getChildren().add(item);
	}
}
