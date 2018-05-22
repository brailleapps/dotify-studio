package application.ui.validation;

import java.util.Optional;

import org.daisy.streamline.api.validity.ValidatorMessage;

import javafx.scene.image.Image;

class ValidatorMessageAdapter {
	private final Optional<ValidatorMessage> validatorMessage;
	private final Optional<Image> image;
	private final String text;

	ValidatorMessageAdapter(String text, Image image) {
		this(text, image, null);
	}

	ValidatorMessageAdapter(String text, Image image, ValidatorMessage msg) {
		this.text = text;
		this.image = Optional.ofNullable(image);
		this.validatorMessage = Optional.ofNullable(msg);
	}

	Optional<ValidatorMessage> getValidatorMessage() {
		return validatorMessage;
	}

	Optional<Image> getImage() {
		return image;
	}

	String getText() {
		return text;
	}

}