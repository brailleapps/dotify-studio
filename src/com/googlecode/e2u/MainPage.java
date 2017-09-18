package com.googlecode.e2u;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.braille.utils.api.embosser.Embosser;
import org.daisy.braille.utils.api.embosser.EmbosserWriter;
import org.daisy.braille.utils.pef.PEFConverterFacade;
import org.daisy.braille.utils.pef.PEFGenerator;
import org.daisy.braille.utils.pef.PEFHandler;
import org.daisy.braille.utils.pef.PEFHandler.Alignment;
import org.daisy.braille.utils.pef.PrinterDevice;
import org.daisy.braille.utils.pef.Range;

import com.googlecode.ajui.AContainer;
import com.googlecode.ajui.ALabel;
import com.googlecode.ajui.ALink;
import com.googlecode.ajui.AParagraph;
import com.googlecode.ajui.APre;
import com.googlecode.ajui.Context;
import com.googlecode.ajui.XHTMLTagger;
import com.googlecode.e2u.BookReader.BookReaderResult;
import com.googlecode.e2u.l10n.L10nKeys;
import com.googlecode.e2u.l10n.Messages;

import shared.BuildInfo;
import shared.Configuration;
import shared.Settings;
import shared.Settings.Keys;

public class MainPage extends BasePage implements AListener {
	private static final Logger logger = Logger.getLogger(MainPage.class.getCanonicalName());
	//201x.m.d

	final static int MAX_COPIES = 99;
	public final static String ENCODING = "utf-8";

	private final static Settings settings;
	public final static String TARGET = "/index.html";

	private BookViewController bookController;

	
	private static boolean closing = false;
    
    static {
        settings = Settings.getSettings();
    }

    public MainPage(File f) {
    	buildMenu();
    	bookController = new BookViewController(f, settings);
    	
    }

    public void buildMenu() {
    }
    
    public Optional<URI> getBookURI() {
    	if (bookController!=null) {
    		return Optional.of(bookController.getBookURI());
    	} else {
    		return Optional.<URI>empty();
    	}
    }
    
    public Optional<BookReaderResult> getBookReaderResult() {
    	if (bookController!=null) {
    		return Optional.ofNullable(bookController.getBookReaderResult());
    	} else {
    		return Optional.<BookReaderResult>empty();
    	}
    }
    
    private Reader previewReader(String key, Context context) {
    	String volume = context.getArgs().get("volume");
		int v = 1;
		try {
			v = Integer.parseInt(volume);
		} catch (NumberFormatException e) {
			
		}
		if (v<1) {v=1;}
    	return bookController.getPreviewView().getReader(v);
    }
    
	@Override
	public Reader getContent(String key, Context context) throws IOException {
		if ("book".equals(key)) {
	    	return new InputStreamReader(bookController.getBookURI().toURL().openStream(), bookController.getBook().getInputEncoding());
		} else if ("preview-new".equals(key)) {
			return previewReader(key, context);
		} else {
			// settings ok?
			Map<String, String> args = context.getArgs();
			String device = settings.getString(Keys.device); //$NON-NLS-1$
			String align = settings.getString(Keys.align);

			// open new book
			String open = args.get("open");
			if (open !=null) {
				open = URLDecoder.decode(open, ENCODING);
				File f = new File(open);
				if (f.exists()) {
					if (logger.isLoggable(Level.FINE)) {
						logger.fine("open book" + f);
					}
					bookController.close();
					bookController = new BookViewController(f, settings);
				}
			}
	        Configuration conf = Configuration.getConfiguration(settings);
			if ("test".equals(args.get("method")) && conf.settingOK()) {
		        File temp = File.createTempFile("generated-", ".pef");
				temp.deleteOnExit();
		        Map<String,String> keys = new HashMap<>();
		        keys.put(PEFGenerator.KEY_COLS, String.valueOf(conf.getMaxWidth()));
		        keys.put(PEFGenerator.KEY_ROWS, String.valueOf(conf.getMaxHeight()));
		        keys.put(PEFGenerator.KEY_DUPLEX, String.valueOf(true));
		        keys.put(PEFGenerator.KEY_EIGHT_DOT, String.valueOf(false));
		        PEFGenerator generator = new PEFGenerator(keys);
		        try {
		            generator.generateTestPages(temp);
		        } catch (Exception e) {
		        }
		        String encURL = URLEncoder.encode(temp.getAbsolutePath(), MainPage.ENCODING);
		        AContainer div = new AContainer();
			    AParagraph p = new AParagraph();
	            ALink a = new ALink("index.html?open="+encURL);                    
	            ALabel label = new ALabel(Messages.getString(L10nKeys.OPEN_TEST_DOCUMENT));
	            a.add(label);
	            p.add(a);
			    div.add(p);
	            return new StringReader(buildHTML(div.getHTML(context), Messages.getString(L10nKeys.TEST_SETUP), true));
	        } else if ("meta".equals(args.get("method"))) {  //$NON-NLS-1$ //$NON-NLS-2$
				return new StringReader(buildHTML(renderView(context, bookController.getAboutBookView()), Messages.getString(L10nKeys.ABOUT_THE_BOOK), true));
			} else if (!bookController.bookIsValid()) {
				return new StringReader(buildHTML(renderView(context, bookController.getValidationView()), Messages.getString(L10nKeys.VALIDATION), false));
			} else if (device!=null && conf.settingOK() && align!=null && "do".equals(args.get("method"))) { //$NON-NLS-1$ //$NON-NLS-2$
				context.log("Settings ok! " + device + " : " + align); //$NON-NLS-1$

		        int pMin;
		        int pMax;
		        int copies;
		        pMin = parseInt((args.get("pagesFrom")+""), 1);
		        pMax = parseInt((args.get("pagesTo")+""), bookController.getBook().getPages());
		        copies = parseInt((args.get("copies")+""), 1);
		        if (pMin<1) {
		        	pMin=1;
		        } else if (pMin>bookController.getBook().getPages()) {
		        	pMin=bookController.getBook().getPages();
		        }
		        if (pMax<pMin) {
		        	pMax = pMin;
		        } else if (pMax>bookController.getBook().getPages()) {
		        	pMax = bookController.getBook().getPages();
		        }
		        if (copies<1) {
		        	copies=1;
		        } else if (copies>MAX_COPIES) {
		        	copies=1;
		        }
				try {
					for (int i=0; i<copies; i++) {
						try (InputStream iss = bookController.getBookURI().toURL().openStream()){
							; //$NON-NLS-1$
							Embosser emb = conf.getConfiguredEmbosser();
							PrinterDevice bd = new PrinterDevice(URLDecoder.decode(device, ENCODING), false);
							EmbosserWriter writer = emb.newEmbosserWriter(bd);
							
							PEFHandler.Builder phb = new PEFHandler.Builder(writer).
														range(Range.parseRange(pMin+"-"+pMax)).
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
			    	AContainer okDiv = new AContainer();
			    	{
			    		AParagraph p = new AParagraph();
			    		p.add(new ALabel(Messages.getString(L10nKeys.FILE_EMBOSSED_OK)));
			    		okDiv.add(p);
			    	}
			    	{
			    		AParagraph p = new AParagraph();
			    		p.add(mailtoDebug());
			    		okDiv.add(p);
			    	}
					return new StringReader(buildHTML(okDiv.getHTML(context), Messages.getString(L10nKeys.FILE_EMBOSSED), false)); //$NON-NLS-1$
				} catch (Exception e) {
					context.log("Exception in parser"  + e.getMessage()); //$NON-NLS-1$
					e.printStackTrace();
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					sw.close();
					AContainer errorDiv = new AContainer();
					{
						AParagraph p = new AParagraph();
						p.add(new ALabel(Messages.getString(L10nKeys.EMBOSSING_FAILED)));
						errorDiv.add(p);
					}
					{
						AParagraph p = new AParagraph();
						p.add(mailtoDebug(e.getCause().toString()));
						errorDiv.add(p);
					}
					AContainer preDiv = new AContainer();
					preDiv.setClass("overflow");
					APre pre = new APre();
					pre.add(new ALabel(sw.toString()));
					preDiv.add(pre);
					errorDiv.add(preDiv);
					return new StringReader(buildHTML(errorDiv.getHTML(context), Messages.getString(L10nKeys.ERROR), false)); //$NON-NLS-1$
				}
			} else {
				return previewReader(key, context);
			}
		}
	}

	private String mailtoEncode(String s) {
		String noencode = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.!~*'()";
		String hexits = "0123456789ABCDEF";
		StringBuilder ret = new StringBuilder();
		s = s.replaceAll("\\x0D\\x0A", "\\x0A").replaceAll("\\x0D", "\\x0A");
		for (char c : s.toCharArray()) {
			if (c==0x0A) {
				ret.append("%0D%0A");
			} else if (noencode.contains("" + c)) {
				ret.append(c);
			} else {
				ret.append("%");
				ret.append(hexits.charAt((c>>4) & 0x0F));
				ret.append(hexits.charAt(c & 0x0F));
			}
		}
		//http://shadow2531.com/opera/testcases/mailto/modern_mailto_uri_scheme.html#encoding
		return ret.toString();
	}

	private ALink mailtoDebug() {
		return mailtoDebug(null);
	}

	private ALink mailtoDebug(String errorMsg) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n\nBook: ");
		try {
			sb.append(bookController.getBook().toString());
		} catch (Exception e) {
			sb.append(e.toString());
		}
		sb.append("\nSettings: ");
		try {
			sb.append(settings.toString());
		} catch (Exception e) {
			sb.append(e.toString());
		}
		boolean hasErrors = false;
		if (errorMsg!=null && !"".equals(errorMsg)) {
			hasErrors = true;
			sb.append("\nError message: ");
			sb.append(errorMsg.replaceAll("\\s", " "));
		}
		ALink a = new ALink("mailto:?subject="+mailtoEncode("Easy Embossing Utility Feedback " + (hasErrors?"(failure)":"(success)")) +
    			"&body="+mailtoEncode("Application: " + this.toString() + sb.toString()));
    	a.add(new ALabel(Messages.getString(L10nKeys.SEND_FEEDBACK)));
    	return a;
	}
	
	private int parseInt(String intVal, int def) {
        try {
        	return Integer.parseInt(intVal);
        } catch (NumberFormatException e) {
        	return def;
        }
	}

    private XHTMLTagger renderView(Context context, AContainer subview) {
    	return subview.getHTML(context);
    }

	@Override
	public void close() {
		closing = true;
		bookController.close();
	}
	@Override
	public synchronized void changeHappened(Object o) {
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Easy Embossing Utility " + BuildInfo.VERSION + ", " + BuildInfo.BUILD;
	}

	@Override
	protected Map<String, String> getBodyAttributes() {
		HashMap<String, String> bodyAtts = new HashMap<>();
		bodyAtts.put("onload", "get('ping.xml?updates=true'+getUpdateString())");
		bodyAtts.put("class", "ui");
		return bodyAtts;
	}

	@Override
	protected List<String> getStylePaths() {
		List<String> styles = new ArrayList<>();
		styles.add("styles/default/base.css");
		styles.add("styles/default/theme.css");
		styles.add("styles/default/layout.css");
		styles.add("styles/default/state.css");
		return styles;
	}

	@Override
	protected List<String> getScriptPaths() {
		List<String> scripts = new ArrayList<>();
		scripts.add("script/index.js");
		return scripts;
	}
	
}
