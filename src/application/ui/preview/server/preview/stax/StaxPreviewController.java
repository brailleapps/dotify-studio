package application.ui.preview.server.preview.stax;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import org.daisy.dotify.studio.api.DocumentPosition;

import application.common.Settings;
import application.common.Settings.Keys;

public class StaxPreviewController {
	private final BookReader r;
	private final Settings settings;
	private StaxPreviewRenderer renderer;
	private String brailleFont, textFont, charset;
	private long lastUpdated;

	/**
	 * Creates a new stax preview controller.
	 * @param r the book reader
	 * @param settings the settings
	 * 
	 */
	public StaxPreviewController(final BookReader r, Settings settings) {
		this.settings = settings;
		this.r = r;
		update(false);
		brailleFont = settings.getString(Keys.brailleFont);
		textFont = settings.getString(Keys.textFont);
		charset = settings.getString(Keys.charset);
	}
	
	private void update(boolean force) {
		synchronized (r) {
			if (!force && lastUpdated+10000>System.currentTimeMillis()) {
				return;
			}
			lastUpdated = System.currentTimeMillis();			
		}
		BookReaderResult brr = r.getResult();
		if (renderer!=null) {
			// abort rendering and delete files
			renderer.abort();
		}
		// set up new renderer
		renderer = new StaxPreviewRenderer(brr.getBook(), brr.getValidationReport());
	}
	
	private boolean settingsChanged() {
		String brailleFont = settings.getString(Keys.brailleFont);
		String textFont = settings.getString(Keys.textFont);
		String charset = settings.getString(Keys.charset);
		boolean changed = 
			(this.brailleFont!=null && !this.brailleFont.equals(brailleFont)) ||
			(this.textFont!=null && !this.textFont.equals(textFont)) ||
			(this.charset!=null && !this.charset.equals(charset));
		this.brailleFont = brailleFont;
		this.textFont = textFont;
		this.charset = charset;
		return changed;
	}

	public Reader getReader(int vol) {
		try {
			boolean fileChanged = false;
			synchronized(r) {
				fileChanged = lastUpdated<r.getFile().lastModified();
			}
			if (settingsChanged() || fileChanged) {
				update(fileChanged);
			}
			return new InputStreamReader(new FileInputStream(renderer.getFile(vol)), "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			return new StringReader("Failed to read");
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return new StringReader("Failed to read");
		}
	}
	
	public int getVolumeForPosition(DocumentPosition p) {
		return renderer.getVolumeForPosition(p);
	}

}