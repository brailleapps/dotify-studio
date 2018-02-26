package application.ui.prefs;

import java.util.Collection;

import org.daisy.braille.utils.api.factory.FactoryProperties;

@FunctionalInterface
interface ListFactoryProperties {
	Collection<? extends FactoryProperties> list();
}