package application.ui.prefs;

import java.io.IOException;

import application.l10n.Messages;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;

public class DriverDetails extends FlowPane {
	@FXML private Label eightDotLabel;
	@FXML private Label volumesLabel;
	@FXML private Label rowSpacingLabel;
	private final boolean eightDot;
	private final boolean volumes;
	private final boolean rowSpacing;
	private final Image no;
	private Image yes;

	DriverDetails(boolean eightDot, boolean volumes, boolean rowSpacing) {
		this.eightDot = eightDot;
		this.volumes = volumes;
		this.rowSpacing = rowSpacing;
		this.no = new Image(this.getClass().getResourceAsStream("resource-files/no.png"));
		this.yes = new Image(this.getClass().getResourceAsStream("resource-files/yes.png"));
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("DriverDetails.fxml"), Messages.getBundle());
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	@FXML void initialize() {
		eightDotLabel.setGraphic(new ImageView(eightDot?yes:no));
		eightDotLabel.setTooltip(new Tooltip(eightDot?Messages.TOOLTIP_8_DOT_SUPPORT.localize():Messages.TOOLTIP_6_DOT_ONLY.localize()));
		volumesLabel.setGraphic(new ImageView(volumes?yes:no));
		volumesLabel.setTooltip(new Tooltip(volumes?Messages.TOOLTIP_VOLUME_SUPPORT.localize():Messages.TOOLTIP_NO_VOLUME_SUPPORT.localize()));
		rowSpacingLabel.setGraphic(new ImageView(rowSpacing?yes:no));
		rowSpacingLabel.setTooltip(new Tooltip(rowSpacing?Messages.TOOLTIP_ACCURATE_LINE_SPACING.localize():Messages.TOOLTIP_SIMPLE_LINE_SPACING.localize()));
	}

}
