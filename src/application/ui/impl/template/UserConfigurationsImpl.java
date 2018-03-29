package application.ui.impl.template;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.daisy.streamline.api.config.ConfigurationDetails;
import org.daisy.streamline.api.config.ConfigurationsProviderException;
import org.daisy.streamline.api.config.ExclusiveAccess;
import org.daisy.streamline.api.config.UserConfigurationsCollection;
import org.daisy.streamline.api.config.UserConfigurationsProvider;

public class UserConfigurationsImpl implements UserConfigurationsProvider {

	@Override
	public Set<ConfigurationDetails> getConfigurationDetails() {
		return getInstance().getConfigurationDetails();
	}

	@Override
	public Map<String, Object> getConfiguration(String key) throws ConfigurationsProviderException {
		return getInstance().getConfiguration(key);
	}

	@Override
	public Optional<String> addConfiguration(String niceName, String description, Map<String, Object> config) {
		return getInstance().addConfiguration(niceName, description, config);
	}

	@Override
	public boolean removeConfiguration(String identifier) {
		return getInstance().removeConfiguration(identifier);
	}

	@Override
	public boolean containsConfiguration(String identifier) {
		return getInstance().containsConfiguration(identifier);
	}
	
	private enum InstanceManager {
		GET(configureCollection());
		private final UserConfigurationsCollection collection;
		private InstanceManager(UserConfigurationsCollection collection) {
			this.collection = collection;
		}
		
		private static UserConfigurationsCollection configureCollection() {
			File configDir = getConfigDir();
			configDir.mkdirs();
			ExclusiveAccess lock = new ExclusiveAccessImpl(new File(configDir, "lock"));
			return new UserConfigurationsCollection(configDir, lock);
		}
		
		private static File getConfigDir() {
			String userHome = System.getProperty("user.home");
			// Note that modifying this path will effectively clear all existing user templates
			return new File(new File(new File(userHome, ".dotify"), "data"), "config");
		}
	}

	/**
	 * Gets the instance.
	 * @return returns the instance
	 */
	private static UserConfigurationsCollection getInstance() {
		return InstanceManager.GET.collection;
	}

}
