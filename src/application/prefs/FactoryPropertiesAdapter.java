package application.prefs;

import org.daisy.braille.api.factory.FactoryProperties;

class FactoryPropertiesAdapter implements Comparable<FactoryPropertiesAdapter> {
	private final FactoryProperties p;
	FactoryPropertiesAdapter(FactoryProperties p) {
		this.p = p;
	}
	
	FactoryProperties getProperties() {
		return p;
	}
	
	@Override
	public int compareTo(FactoryPropertiesAdapter o) {
		return p.getDisplayName().compareTo(o.p.getDisplayName());
	}
	
	@Override
	public String toString() {
		return p.getDisplayName();
	}

}