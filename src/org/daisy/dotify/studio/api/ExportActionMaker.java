package org.daisy.dotify.studio.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.daisy.streamline.api.media.FileDetails;

/**
 * Provides a maker for export actions.
 * @author Joel Håkansson
 */
public class ExportActionMaker {
	private final List<ExportActionProvider> providers;

	private ExportActionMaker() {
		List<ExportActionProvider> tmp = new ArrayList<>();
		for (ExportActionProvider p : ServiceLoader.load(ExportActionProvider.class)) {
			tmp.add(p);
		}
		this.providers = Collections.unmodifiableList(tmp);
	}

	/**
	 * Creates a new instance.
	 * @return a new instance
	 */
	public static ExportActionMaker newInstance() {
		return new ExportActionMaker();
	}

	/**
	 * Lists all export actions available for the specified format.
	 * @param format the format to export
	 * @return a list of export actions for the format
	 */
	public List<ExportActionDescription> listActions(FileDetails format) {
		return providers.stream()
				.filter(p->p.supportsFormat(format))
				.flatMap(v->v.listActions().stream())
				.collect(Collectors.toList());
	}

	/**
	 * Creates a new instance of the specified export action.
	 * @param id the export action identifier
	 * @return a new instance
	 */
	public Optional<ExportAction> newExportAction(String id) {
		return providers.stream()
				.filter(p->p.supportsAction(id))
				.findFirst()
				.flatMap(p->p.newExportAction(id));
	}

}
