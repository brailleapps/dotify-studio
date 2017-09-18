package shared;

import org.daisy.braille.utils.api.factory.FactoryProperties;

public class FactoryPropertiesAdapter extends NiceName implements Comparable<FactoryPropertiesAdapter> {
	private final FactoryProperties p;
	public FactoryPropertiesAdapter(FactoryProperties p) {
		super(p.getIdentifier(), p.getDisplayName(), p.getDescription());
		this.p = p;
	}
	
	public FactoryProperties getProperties() {
		return p;
	}
	
	@Override
	public int compareTo(FactoryPropertiesAdapter o) {
		return p.getDisplayName().compareTo(o.p.getDisplayName());
	}

}