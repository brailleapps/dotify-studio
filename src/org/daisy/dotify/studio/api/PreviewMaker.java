package org.daisy.dotify.studio.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.daisy.streamline.api.details.FormatDetails;
import org.daisy.streamline.api.media.FileDetails;

public class PreviewMaker {
	private final List<PreviewProvider> providers;
	
	public PreviewMaker() {
		List<PreviewProvider> tmp = new ArrayList<>();
		for (PreviewProvider p : ServiceLoader.load(PreviewProvider.class)) {
			tmp.add(p);
		}
		this.providers = Collections.unmodifiableList(tmp);
	}
	
	public static PreviewMaker newInstance() {
		return new PreviewMaker();
	}

	public boolean supportsFormat(FileDetails format) {
		return providers.stream()
				.anyMatch(p->p.supportsFormat(format));
	}
	
	public List<FormatDetails> listDetails() {
		return providers.stream()
				.flatMap(p->p.listDetails().stream())
				.collect(Collectors.toList());
	}
	
	public Optional<OpenableEditor> newPreview(FileDetails format) {
		return providers.stream()
				.filter(p->p.supportsFormat(format))
				.findFirst()
				.map(p->p.newPreview(format));
	}
	
}
