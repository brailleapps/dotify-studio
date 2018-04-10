package application.common;

import org.daisy.streamline.api.config.ConfigurationsCatalog;
import org.daisy.streamline.api.config.ConfigurationsCatalogService;

public enum Singleton {
	INSTANCE;
	public static Singleton getInstance() {
		return Singleton.INSTANCE;
	}
	
	private ConfigurationsCatalogService configsCatalog;
	public synchronized ConfigurationsCatalogService getConfigurationsCatalog() {
		if (configsCatalog==null) {
			configsCatalog = ConfigurationsCatalog.newInstance();
		}
		return configsCatalog;
	}
}
