package application.github;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class GithubReleasesTest {

	@Test
	public void testParseReleases() throws Exception {
		Optional<GithubRelease> latest = GithubReleases
				.from(this.getClass().getResourceAsStream("resource-files/github.json"))
				.getLatest();
		GithubRelease release = latest.orElseThrow(RuntimeException::new);
		assertEquals("v0.6.1", release.getName().orElse(""));
		assertEquals("https://github.com/brailleapps/dotify-studio/releases/tag/releases/v0.6.1", release.getHtmlUrl().orElse(""));
		assertEquals("https://github.com/brailleapps/dotify-studio/releases/download/releases/v0.6.1/dotify-studio-0.6.1.rpm",
				release.getAssetForPlatform("rpm")
						.flatMap(v->v.getDownloadUrl())
						.orElseThrow(RuntimeException::new));
	}

	/*
	@Test
	public void testURL() throws Exception {
		GithubReleases.from(new URL("https://api.github.com/repos/brailleapps/dotify-studio/releases"));
	}*/
}
