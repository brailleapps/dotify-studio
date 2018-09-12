package application.ui.impl.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.daisy.dotify.api.embosser.Embosser;
import org.daisy.dotify.api.embosser.EmbosserCatalog;
import org.daisy.dotify.api.embosser.EmbosserCatalogService;
import org.daisy.dotify.api.embosser.EmbosserFeatures;
import org.daisy.dotify.api.embosser.EmbosserWriter;
import org.daisy.braille.utils.pef.PEFFileSplitter;
import org.daisy.braille.utils.pef.PEFHandler;
import org.daisy.streamline.api.validity.Validator;
import org.daisy.streamline.api.validity.ValidatorFactoryMaker;
import org.xml.sax.SAXException;

import application.ui.preview.FileDetailsCatalog;

final class PefExportActions {
	
	private PefExportActions() {
		throw new AssertionError("No instances.");
	}

	static void toText(File source, File target, String table) throws IOException {
		//TODO: sync this with the embossing code and its settings
		OutputStream os = new FileOutputStream(target);
		EmbosserCatalogService ef = EmbosserCatalog.newInstance();
		Embosser emb = ef.newEmbosser("org_daisy.GenericEmbosserProvider.EmbosserType.NONE");
		if (table!=null) {
			emb.setFeature(EmbosserFeatures.TABLE, table);
		}
		EmbosserWriter embosser = emb.newEmbosserWriter(os);
		PEFHandler ph = new PEFHandler.Builder(embosser).build();
		FileInputStream is = new FileInputStream(source);
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		SAXParser sp;
		try {
			sp = spf.newSAXParser();
			sp.parse(is, ph);
		} catch (ParserConfigurationException | SAXException e) {
			throw new IOException("Failed to export", e);
		}
	}
	
	static boolean split(File source, File target) {
		Validator v = ValidatorFactoryMaker.newInstance().newValidator(FileDetailsCatalog.PEF_FORMAT.getMediaType());
		PEFFileSplitter splitter = new PEFFileSplitter(f->v.validate(f).isValid());
		return splitter.split(source, target);
	}
}
