package org.daisy.dotify.studio.api;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.daisy.streamline.api.details.FormatDetails;
import org.daisy.streamline.api.details.FormatDetailsProvider;
import org.daisy.streamline.api.details.FormatDetailsProviderService;
import org.daisy.streamline.api.media.FileDetails;
import org.daisy.streamline.api.media.FormatIdentifier;

public interface PreviewProvider {
	static FormatDetailsProviderService detailsProvider = FormatDetailsProvider.newInstance();
	
	public boolean supportsFormat(FileDetails format);
	
	public List<FormatDetails> listDetails();
	
	public OpenableEditor newPreview(FileDetails format);
	
    static List<FormatDetails> getDetails(String ... exts) {
    	return Arrays.asList(exts)
    		.stream()
    		.map(id->detailsProvider.getDetails(FormatIdentifier.with(id)))
    		.filter(v->v.isPresent())
    		.map(v->v.get())
    		.collect(Collectors.toList());
    }

}
