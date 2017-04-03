package application.prefs;

import org.daisy.braille.api.factory.FactoryProperties;

class FactoryPropertiesAdapter extends NiceName implements Comparable<FactoryPropertiesAdapter> {
	private final FactoryProperties p;
	FactoryPropertiesAdapter(FactoryProperties p) {
		super(p.getIdentifier(), p.getDisplayName(), p.getDescription());
		this.p = p;
	}
	
	FactoryProperties getProperties() {
		return p;
	}
	
	@Override
	public int compareTo(FactoryPropertiesAdapter o) {
		return p.getDisplayName().compareTo(o.p.getDisplayName());
	}

}