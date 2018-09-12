package application.ui.prefs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.daisy.dotify.api.factory.FactoryProperties;

import application.common.FactoryPropertiesAdapter;
import application.common.Settings;
import application.common.Settings.Keys;
import javafx.concurrent.Task;

class FactoryPropertiesScanner extends Task<List<FactoryPropertiesAdapter>> {
	private final String currentIdentifier;
	private final ListFactoryProperties listMethod;
	private FactoryPropertiesAdapter currentValue;
	
	public FactoryPropertiesScanner(ListFactoryProperties listMethod, Keys prefsKey) {
		this.listMethod = listMethod;
		this.currentIdentifier = Settings.getSettings().getString(prefsKey, "");
	}
	
	FactoryPropertiesAdapter getCurrentValue() {
		return currentValue;
	}

	@Override
	protected List<FactoryPropertiesAdapter> call() throws Exception {
		List<FactoryPropertiesAdapter> tc = new ArrayList<>();
		for (FactoryProperties p : listMethod.list()) {
			FactoryPropertiesAdapter ap = new FactoryPropertiesAdapter(p);
			tc.add(ap);
			if (p.getIdentifier().equals(currentIdentifier)) {
				currentValue = ap; 
			}
		}
		Collections.sort(tc);
		return tc;
	}
	
}