package application.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMaker;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryMaker;

/**
 * Provides a list of supported locales as a lazy-loaded thread-safe singleton,
 * without the need for explicit synchronization.
 * 
 * @author Joel Håkansson
 */
public enum SupportedLocales {
	/**
	 * Gets the instance. Using the enum value directly outside of the enum
	 * is not necessary or desired.
	 * 
	 * @see #list()
	 */
	INSTANCE;
	private final List<Locale> locales;

	/**
	 * The contents of this list is somewhat arbitrary. A locale on this list 
	 * isn't necessarily fully supported by all parts of the system. Conversely,
	 * a locale that's missing from the list may be supported by some parts of
	 * the system.
	 */
	private SupportedLocales() {
		// Get locales with hyphenator 
		Set<Locale> loc = HyphenatorFactoryMaker.newInstance().listLocales().stream()
			.map(v->Locale.forLanguageTag(v))
			.distinct()
			.collect(Collectors.toCollection(HashSet::new));
		// Add locales with translator
		BrailleTranslatorFactoryMaker.newInstance().listSpecifications().stream()
			.map(v->Locale.forLanguageTag(v.getLocale()))
			.distinct()
			.forEach(v->loc.add(v));
		Set<Locale> all = Arrays.asList(Locale.getAvailableLocales()).stream().collect(Collectors.toSet());
		// Subtract any that's not supported by java
		loc.retainAll(all);
		locales = loc.stream()
				.sorted((o1, o2)->o1.getDisplayName().compareTo(o2.getDisplayName()))
				.collect(Collectors.toList());
	}
	
	/**
	 * Gets a list of locales that are supported. Currently this means that the locale must 
	 * support hyphenation or braille translation <em>and</em> the locale must be supported
	 * by the JVM.
	 * 
	 * @return a list of locales
	 */
	public static List<Locale> list() {
		return INSTANCE.locales;
	}
}
