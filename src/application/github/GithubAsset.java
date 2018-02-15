package application.github;

import com.grack.nanojson.JsonObject;

import java.util.Optional;

public class GithubAsset {
	private static final String DOWNLOAD_URL = "browser_download_url";
	private final JsonObject asset;

	GithubAsset(JsonObject asset) {
		this.asset = asset;
	}

	public Optional<String> getDownloadUrl() {
		return Optional.of(asset.getString(DOWNLOAD_URL));
	}
}
