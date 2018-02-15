package application.github;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.Optional;

/**
 * @author Joel Håkansson
 */
public class GithubReleases {
    private static final String PUBLISHED_AT = "published_at";
    private final JsonArray releases;

    private GithubReleases(JsonArray releases) {
		this.releases = releases;
	}

	public static GithubReleases from(InputStream is) throws JsonParserException {
		JsonArray arr = JsonParser.array().from(is);
    	return new GithubReleases(arr);
	}

	public static GithubReleases from(URL url) throws IOException, JsonParserException {
    	return from(url.openStream());
	}

    public Optional<GithubRelease> getLatest() throws IOException, JsonParserException {
        return releases.stream()
                .filter(v->v instanceof JsonObject)
                .map(v->(JsonObject)v)
                .sorted((o1, o2) ->
                        Instant.parse(o2.getString(PUBLISHED_AT))
                                .compareTo(Instant.parse(o1.getString(PUBLISHED_AT))))
                .findFirst().map(v->new GithubRelease(v));
    }
}
