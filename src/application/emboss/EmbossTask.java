package application.emboss;

import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import org.daisy.braille.api.embosser.Embosser;
import org.daisy.braille.api.embosser.EmbosserWriter;
import org.daisy.braille.pef.PEFConverterFacade;
import org.daisy.braille.pef.PEFHandler;
import org.daisy.braille.pef.PEFHandler.Alignment;
import org.daisy.braille.pef.PrinterDevice;
import org.daisy.braille.pef.Range;

import javafx.concurrent.Task;
import shared.Configuration;

class EmbossTask extends Task<Void> {
	private static final Logger logger = Logger.getLogger(EmbossTask.class.getCanonicalName());
	private final URL url;
	private final String deviceName;
	private final String align;
	private final Range range;
	private final int copies;
	private final Configuration conf;
	
	EmbossTask(URL url, String deviceName, String align, Range range, int copies, Configuration conf) {
		this.url = url;
		this.deviceName = deviceName;
		this.align = align;
		this.range = range;
		this.copies = copies;
		this.conf = conf;
	}

	@Override
	protected Void call() throws Exception {
		//TODO: include range (requires release of pef-tools v2.3.0)
		logger.info("Embossing " + (copies>1?copies + " copies ":"") + "on " + deviceName + " with alignment " + align);
		for (int i=0; i<copies; i++) {
			try (InputStream iss = url.openStream()) {
				//TODO: don't recreate objects for each copy unless necessary
				Embosser emb = conf.getConfiguredEmbosser();
				PrinterDevice bd = new PrinterDevice(deviceName, false);
				EmbosserWriter writer = emb.newEmbosserWriter(bd);
				
				PEFHandler.Builder phb = new PEFHandler.Builder(writer).
											range(range).
											offset(0);
				if (conf.supportsAligning()) {
			        Alignment alignment = Alignment.CENTER_INNER;
			        try {
			        	alignment = Alignment.valueOf(align.toUpperCase());
			        } catch (IllegalArgumentException e) {
			        	e.printStackTrace();
			        }
					phb.align(alignment);
				}
				new PEFConverterFacade(conf.getEmbosserCatalog()).parsePefFile(iss, phb.build());
			}
		}
		return null;
	}
}