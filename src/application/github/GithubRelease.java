package application.github;

import com.grack.nanojson.JsonObject;

import javax.swing.text.html.Option;
import java.util.Optional;

public class GithubRelease {
	private static final String NAME = "name";
	private static final String HTML_URL = "html_url";
	private static final String ASSETS = "assets";
	private final JsonObject release;

	GithubRelease(JsonObject release) {
		this.release = release;
	}

	public Optional<String> getName() {
		return Optional.of(release.getString(NAME));
	}

	public Optional<String> getHtmlUrl() {
		return Optional.of(release.getString(HTML_URL));
	}

	public Optional<GithubAsset> getAssetForPlatform(String platform) {
		return release.getArray(ASSETS).stream()
				.filter(v->v instanceof JsonObject)
				.map(v->(JsonObject)v)
				.filter(v->v.getString(NAME).endsWith(platform))
				.findFirst()
				.map(v->new GithubAsset(v));
	}
}
