package com.googlecode.e2u;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.daisy.braille.embosser.Embosser;
import org.daisy.braille.embosser.EmbosserWriter;
import org.daisy.braille.facade.PEFConverterFacade;
import org.daisy.braille.pef.PEFHandler;
import org.daisy.braille.pef.PEFHandler.Alignment;
import org.daisy.braille.pef.Range;
import org.daisy.braille.pef.PEFGenerator;
import org.daisy.printing.PrinterDevice;

import com.googlecode.ajui.AComponent;
import com.googlecode.ajui.AContainer;
import com.googlecode.ajui.ALabel;
import com.googlecode.ajui.ALink;
import com.googlecode.ajui.AParagraph;
import com.googlecode.ajui.APre;
import com.googlecode.ajui.Context;
import com.googlecode.ajui.XHTMLTagger;
import com.googlecode.ajui.XMLTagger;
import com.googlecode.e2u.Settings.Keys;
import com.googlecode.e2u.l10n.L10nKeys;
import com.googlecode.e2u.l10n.Messages;

public class MainPage extends BasePage implements AListener {
	//201x.m.d
	public final static String VERSION = "2.0 dev";
	public final static String BUILD = "2011-09-13";
	final static int MAX_COPIES = 99;
	final static String ENCODING = "utf-8";
	
	final static String KEY_TOP_BAR = "top-bar";
	private final static Settings settings;
	public final static String TARGET = "/index.html";

	private BookViewController bookController;

	private MenuSystem embossMenu;
	private MenuSystem openMenu;
	private MenuSystem setupMenu;
	
	private final AContainer aboutView;
	private final SettingsView settingsView;
	private final AContainer previewSettingsView;
	private final AContainer paperView;
	private final FindView findView;
	private final AContainer fileChooser;
	
	private final ComponentRegistry registry;

	private static boolean closing = false;
    
    static {
        HashMap<String, String> def = new HashMap<String, String>();
        def.put(Settings.Keys.align.toString(), "center_inner");
        settings = new Settings("/EasyEmbossingUtility"+VERSION, def);
    }
    /*
    public static InputStream getInputStreamForBook() {
    	if (bookFile!=null) {
    		try {
				return new FileInputStream(bookFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
    	}
    	return WebUI.getResourceStream(target)Stream("/book.pef");
    }*/
    /*
    private URI getURIForBook() {
    	if (bookReader.getResult().getBookFile()!=null) {
			return bookReader.getResult().getBookFile().toURI();
    	}
    	try {
    		return ClassLoader.getSystemResource("www/book.pef").toURI();
    	} catch (URISyntaxException e) {
    		e.printStackTrace();
    		return null;
    	}
    }*/
    

    public MainPage(File f) {
    	buildMenu();
    	bookController = new BookViewController(f, settings, embossMenu);
    	//changeHappened = false;
    	registry = new ComponentRegistry();

    	aboutView = new AboutView();
    	settingsView = new SettingsView(settings, setupMenu);
    	previewSettingsView = new PreviewSettingsView(settings, setupMenu);
    	findView = new FindView(settings, openMenu, registry);
    	findView.setIdentifier("fileChooser");
    	registry.register(findView);
    	paperView = new PaperView(setupMenu, settingsView);
    	File libPath = null;
    	try {
	    	libPath = new File(settings.getString(Settings.Keys.libraryPath));
			if (!libPath.isDirectory()) {
				libPath = null;
			}
    	} catch (NullPointerException e) {
    		// value was not set
    	}
    	fileChooser = new AFileChooser(libPath, openMenu);


    }

    public void buildMenu() {
    	MenuSystem topMenu = new MenuSystem("method")
			.setDivider(" | ")
			.addMenuItem(new MenuItem("start", "Start"));
    	
    	embossMenu = new MenuSystem("method", topMenu)
    		.setDivider(" | ")
    		.addMenuItem("emboss", Messages.getString(L10nKeys.MENU_MAIN))
    		.addMenuItem("meta", Messages.getString(L10nKeys.MENU_ABOUT_BOOK));
		openMenu = new MenuSystem("method")
			.setDivider(" | ")
			.addMenuItem("find", Messages.getString(L10nKeys.FIND_IN_LIBRARY))
			.addMenuItem("choose", Messages.getString(L10nKeys.BROWSE_FILE_SYSTEM));
		setupMenu = new MenuSystem("method")
			.setDivider(" | ")
			.addMenuItem("setup", Messages.getString(L10nKeys.EMBOSS_VIEW))
			.addMenuItem("setup-preview", Messages.getString(L10nKeys.PREVIEW_VIEW))
			.addMenuItem("paper", Messages.getString(L10nKeys.PAPER_VIEW));
    }
    
    private List<AComponent> getUpdateComponent(AComponent a, Date since) {
    	ArrayList<AComponent> ret = new ArrayList<AComponent>();
    	if (a.hasIdentifer() && a.hasUpdates(since)) {
    		ret.add(a);
    		return ret;
    	} else {
    		if (a.getChildren()==null) {
    			return ret;
    		}
			for (AComponent c : a.getChildren()) {
				ret.addAll(getUpdateComponent(c, since));
			}
    	}
		return ret;
    }
    
	@Override
	public Reader getContent(String key, Context context) throws IOException {
		if ("status".equals(key)) {
			XMLTagger xtag = new XMLTagger();
			String component = context.getArgs().get("component");
			boolean updates = context.getArgs().get("updates")!=null;
			AComponent a = null;
			if (component!=null && !"".equals(component)) {
				a = registry.getComponent(component);
			}
			Date d = new Date(); //FIXME: use correct date
			for (int i=0; i<20; i++) {
				if (closing==true || !updates) {
					break;
				}
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					break;
				}
				if (a!=null) {
					List<AComponent> list = getUpdateComponent(a, d);
					if (list.size()==0) {
						continue;
					}
					for (AComponent a2 : list) {
						xtag.start("update")
						.attr("id", a2.getIdentifier());
						for (AComponent c : a2.getChildren()) {
							xtag.insert(c.getHTML(context));
						}
						xtag.end();
					}
					break;
				}
			}
			System.err.println(xtag.getResult());
			return new StringReader(xtag.getResult());
		} else if ("book".equals(key)) {
	    	return new InputStreamReader(bookController.getBookURI().toURL().openStream(), bookController.getBook().getInputEncoding());
		} else if ("preview-new".equals(key)) {
			String volume = context.getArgs().get("volume");
			int v = 1;
			try {
				v = Integer.parseInt(volume);
			} catch (NumberFormatException e) {
				
			}
			if (v<1) {v=1;}
	    	return bookController.getPreviewView().getReader(v);
		} else if ("preview".equals(key)) {
			Map<String, String> params = PreviewController.buildParamsFromContext(context, settings);
			String style = context.getArgs().get("style");
			if (style==null || "".equals(style)) {
				style = "pef2xhtml.xsl";
			}
			if (params.containsKey("uri")) {
				StringBuilder sb = new StringBuilder();
				sb.append("<?xml-stylesheet type=\"text/xsl\" href=\"");
				sb.append(style);
				sb.append("\"?>");
				XMLTagger xt = new XMLTagger();
				xt.start("settings");
				for (String k : params.keySet()) {
					xt.start("param").attr("name", k).attr("value", params.get(k)).end();
				}
				xt.end();
				sb.append(xt.getResult());
				return new StringReader(sb.toString());
			} else {
				return new StringReader("Missing required argument(s)");
			}
		} else if (KEY_TOP_BAR.equals(key)) {
			return new StringReader(
					new XHTMLTagger()
						.start("div").attr("id", "top-bar")
							.start("p")
								.start("a").attr("href", "index.html?method=about").attr("title", Messages.getString(L10nKeys.TOOLTIP_HELP)).text("?").end()
								.start("span").attr("id", "software-title").text("Easy Embossing Utility").end()
								.start("span").attr("id", "status")
									.start("a").attr("id", "connected").attr("href", "close.html").attr("title", Messages.getString(L10nKeys.TOOLTIP_CLOSE))
										.start("img").attr("src", "images/green.gif").attr("alt", "connected").end()
									.end()
									.start("img").attr("id", "notConnected").attr("src", "images/red.gif").attr("alt", "not connected").end()
								.end()
							.end()
						.end()
				.getResult());
		} else {
			return super.getContent(key, context);
		}
	}

	public String getContentString(String key, Context context) throws IOException {
		// settings ok?
		HashMap<String, String> args = context.getArgs();
		String device = settings.getString(Keys.device); //$NON-NLS-1$
		String align = settings.getString(Keys.align);

		// open new book
		String open = args.get("open");
		if (open !=null && !KEY_TITLE.equals(key)) {
			open = URLDecoder.decode(open, ENCODING);
			File f = new File(open);
			if (f.exists()) {
				System.err.println("open book" + f);
				bookController.close();
				bookController = new BookViewController(f, settings, embossMenu);
			}
		}
		if ("choose".equals(args.get("method"))) {
				if (KEY_TITLE.equals(key)) {
					return Messages.getString(L10nKeys.OPEN);
				}
				return buildHTML(renderView(context, fileChooser), Messages.getString(L10nKeys.OPEN), true);
		} else if ("find".equals(args.get("method"))) {
			if (KEY_TITLE.equals(key)) {
				return Messages.getString(L10nKeys.OPEN);
			}
			return buildHTML(renderView(context, findView), Messages.getString(L10nKeys.OPEN), true);
		} else if (!bookController.bookIsValid()) {
			if (KEY_TITLE.equals(key)) {
				return Messages.getString(L10nKeys.VALIDATION);
			}
			return buildHTML(renderView(context, bookController.getValidationView()), Messages.getString(L10nKeys.VALIDATION), false);
		} else if ("setup".equals(args.get("method"))) { //$NON-NLS-1$ //$NON-NLS-2$
			if (KEY_TITLE.equals(key)) {
				return Messages.getString(L10nKeys.SETTINGS);
			}
			return buildHTML(renderView(context, settingsView), Messages.getString(L10nKeys.SETTINGS), true); //$NON-NLS-1$
		} else if ("setup-preview".equals(args.get("method"))) { //$NON-NLS-1$ //$NON-NLS-2$
			if (KEY_TITLE.equals(key)) {
				return Messages.getString(L10nKeys.SETTINGS);
			}
			return buildHTML(renderView(context, previewSettingsView), Messages.getString(L10nKeys.SETTINGS), true); //$NON-NLS-1$
		} else if ("paper".equals(args.get("method"))) {
			if (KEY_TITLE.equals(key)) {
				return Messages.getString(L10nKeys.SETTINGS);
			}
			return buildHTML(renderView(context, paperView), Messages.getString(L10nKeys.SETTINGS), true);
		}
		else if ("meta".equals(args.get("method"))) {  //$NON-NLS-1$ //$NON-NLS-2$
			if (KEY_TITLE.equals(key)) {
				return Messages.getString(L10nKeys.ABOUT_THE_BOOK);
			}
			return buildHTML(renderView(context, bookController.getAboutBookView()), Messages.getString(L10nKeys.ABOUT_THE_BOOK), true);
		} else if ("about".equals(args.get("method"))) {
			if (KEY_TITLE.equals(key)) {
				return Messages.getString(L10nKeys.ABOUT_THE_SOFTWARE);
			}
			return buildHTML(aboutView.getHTML(context).getResult(), Messages.getString(L10nKeys.ABOUT_THE_SOFTWARE), true);
		} else if ("test".equals(args.get("method")) && settingsView.getConfiguration().settingOK()) {
                    File temp = File.createTempFile("generated-", ".pef");
					temp.deleteOnExit();
                    Map<String,String> keys = new HashMap<String,String>();
                    keys.put(PEFGenerator.KEY_COLS, String.valueOf(settingsView.getConfiguration().getMaxWidth()));
                    keys.put(PEFGenerator.KEY_ROWS, String.valueOf(settingsView.getConfiguration().getMaxHeight()));
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
                    return buildHTML(div.getHTML(context).getResult(), "Test setup", true);
                } else if (device!=null && settingsView.getConfiguration().settingOK() && align!=null) {
			if ("do".equals(args.get("method"))) { //$NON-NLS-1$ //$NON-NLS-2$
				
				if (KEY_TITLE.equals(key)) {
					return "Transformer result";
				}
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
						InputStream iss = null;
						try {
							iss = bookController.getBookURI().toURL().openStream(); //$NON-NLS-1$
							Embosser emb = settingsView.getConfiguration().getConfiguredEmbosser();
							PrinterDevice bd = new PrinterDevice(URLDecoder.decode(device, ENCODING), false);
							EmbosserWriter writer = emb.newEmbosserWriter(bd);
							
							PEFHandler.Builder phb = new PEFHandler.Builder(writer).
														range(Range.parseRange(pMin+"-"+pMax)).
														offset(0);
							if (settingsView.getConfiguration().supportsAligning()) {
						        Alignment alignment = Alignment.CENTER_INNER;
						        try {
						        	alignment = Alignment.valueOf(align.toUpperCase());
						        } catch (IllegalArgumentException e) {
						        	e.printStackTrace();
						        }
								phb.align(alignment);
							}
							PEFConverterFacade.parsePefFile(iss, phb.build());
						} finally {
							if (iss!=null) {
								iss.close();
							}
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
					return buildHTML(okDiv.getHTML(context).getResult(), Messages.getString(L10nKeys.FILE_EMBOSSED), false); //$NON-NLS-1$
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
					return buildHTML(errorDiv.getHTML(context).getResult(), Messages.getString(L10nKeys.ERROR), false); //$NON-NLS-1$
				}
			} else {
				if (KEY_TITLE.equals(key)) {
					return Messages.getString(L10nKeys.EMBOSS_PEF);
				}
				return buildHTML(embossHTML(context), Messages.getString(L10nKeys.EMBOSS_PEF), true); //$NON-NLS-1$
			}
		} else {
			if (KEY_TITLE.equals(key)) {
				return Messages.getString(L10nKeys.SETTINGS);
			}
			String notice = "";
			if ("emboss".equals(args.get("method")) ||
                            "test".equals(args.get("method"))) {
				notice = "<p class=\"warning\">" + Messages.getString(L10nKeys.COMPLETE_SETUP) + "</p>";
			}
			return buildHTML(notice + renderView(context, settingsView), Messages.getString(L10nKeys.SETTINGS), true);
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
		}/*
		sb.append("\nEmbosser: ");
		try {
			sb.append(settingsView.getConfiguration().getConfiguredEmbosser().toString());
		} catch (Exception e) {
			sb.append(e.toString());
		}*/
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
	/*
	private String findHTML(Context context) throws IOException {
		XHTMLTagger tagger = new XHTMLTagger();
		tagger.insert(openMenu.getHTML(context));

		return tagger.getResult();
	}
*/
    private String renderView(Context context, AContainer subview) {
    	return subview.getHTML(context).getResult();
    }



    private String embossHTML(Context context) {
    	StringBuffer sb = new StringBuffer();
    	sb.append(embossMenu.getHTML(context).getResult());
    	Iterable<String> data;
    	data = bookController.getBook().getTitle();
    	if (data==null || !data.iterator().hasNext()) {
    		sb.append(new XHTMLTagger().tag("p",  Messages.getString(L10nKeys.UNKNOWN_TITLE)).getResult());
    	} else {
    		for (String s: data) {
    			sb.append(new XHTMLTagger().tag("h2", s).getResult());
    			//insertElement(sb, "h2", s); //$NON-NLS-1$
    		}
    	}
    	data = bookController.getBook().getAuthors();
    	if (data==null || !data.iterator().hasNext()) {
    		sb.append(new XHTMLTagger().tag("p", Messages.getString(L10nKeys.UNKNOWN_AUTHOR)).getResult());
    	} else {
    		sb.append("<p>");
    		String delimiter = "";
    		for (String s: data) {
    			sb.append(delimiter + s);
    			delimiter = ", "; //$NON-NLS-1$
    		}
    		sb.append("</p>");
    	}
    	sb.append(
    			new XHTMLTagger().tag("p", MessageFormat.format(Messages.getString(L10nKeys.FILE_DIMENSIONS), bookController.getBook().getMaxWidth(), bookController.getBook().getMaxHeight())).getResult()
    		);
    	
    	//Paper paper = settings.getPaper();
    	//Embosser emb = settings.getEmbosser();

    	sb.append("<form action=\""); //$NON-NLS-1$
    	sb.append(MainPage.TARGET);
    	sb.append("\" method=\"get\">\n"); //$NON-NLS-1$
    	
    	sb.append("<div class=\"group\">");
    	sb.append(
    			new XHTMLTagger().tag("p", showOptions(context)).getResult()
    			//tag("p", showOptions(context))
    		); //$NON-NLS-1$
    	if ("options".equals(context.getArgs().get("show"))) { //$NON-NLS-1$ //$NON-NLS-2$
    		
    		if (bookController.getBook().getVolumes()>1) {
				sb.append("<p>");
				sb.append(Messages.getString(L10nKeys.EMBOSS_VOLUME));
		    	for (int i=1; i<=bookController.getBook().getVolumes(); i++) {
		    		sb.append("<label><input type=\"radio\" name=\"embossSelection\" value=\"");
		    		sb.append(i);
		    		sb.append("\" onclick=\"var from = document.getElementById('pagesFrom');var to = document.getElementById('pagesTo');from.value=");
		    		sb.append(bookController.getBook().getFirstPage(i));
		    		sb.append(";to.value=");
		    		sb.append(bookController.getBook().getLastPage(i)+";\" ></input>");
		    		sb.append(i);
		    		sb.append(" </label>");
		    	}
		    	sb.append("</p>");
    		}
    		sb.append("<p>");
    		sb.append(MessageFormat.format(
    				Messages.getString(L10nKeys.EMBOSS_PAGES_FROM_TO),
    				"<input type=\"radio\" name=\"embossSelection\" value=\"pages\" id=\"embossPagesRadio\" checked=\"checked\"></input>",
    				"<input type=\"text\" name=\"pagesFrom\" id=\"pagesFrom\" maxlength=\"5\" size=\"3\" value=\"1\" onfocus=\"document.getElementById('embossPagesRadio').checked='checked'\"/>",
    				"<input type=\"text\" name=\"pagesTo\" id=\"pagesTo\" maxlength=\"5\" size=\"3\" value=\"" + bookController.getBook().getPages() + "\" onfocus=\"document.getElementById('embossPagesRadio').checked='checked'\"/>")); //$NON-NLS-1$
			sb.append("</p>"); //$NON-NLS-1$
			sb.append("<p>");
			sb.append(Messages.getString(L10nKeys.EMBOSS_COPIES) + " <input type=\"text\" name=\"copies\" id=\"copies\" maxlength=\"2\" size=\"3\" value=\"1\" /> " + MessageFormat.format(Messages.getString(L10nKeys.EMBOSS_COPIES_MAX), MAX_COPIES));
			sb.append("</p>");
    	}
    	
    	sb.append("</div>");
    	//PageFormat pf = settingsView.getPageFormat();
    	Configuration conf = settingsView.getConfiguration();
    	if (bookController.getBook().containsEightDot()) {
    		sb.append("<p>");
    		sb.append(Messages.getString(L10nKeys.EIGHT_DOT_NOT_SUPPORTED));
    		sb.append("</p>");
    	} else if (!conf.settingOK()) {
    		sb.append(
    				new XHTMLTagger().start("p").attr("class", "warning").text("Unknown paper").end().getResult()
    				//tag("p", " class=\"warning\"", "Unknown paper")
    				
    		);
    		settings.resetKey(Keys.paper);
    	} else if (conf.getMaxWidth()<bookController.getBook().getMaxWidth()) {
    		String papername = conf.getPaperName();
			sb.append(
					new XHTMLTagger().start("p").attr("class", "warning")
							.text(MessageFormat.format(Messages.getString(L10nKeys.CURRENT_PAPER_TOO_NARROW), papername))
						.end().getResult()
			);
		} else if (conf.getMaxHeight()<bookController.getBook().getMaxHeight()) {
    		String papername = conf.getPaperName();
			sb.append(
					new XHTMLTagger().start("p").attr("class", "warning").text(MessageFormat.format(Messages.getString(L10nKeys.CURRENT_PAPER_TOO_SHORT), papername)).end().getResult()
			);
		} else {
	    	sb.append("<p><input type=\"hidden\" name=\"method\" value=\"do\" /><input type=\"submit\" value=\""); //$NON-NLS-1$
	    	sb.append(Messages.getString(L10nKeys.BUTTON_EMBOSS));
	    	sb.append("\" /></p>\n"); //$NON-NLS-1$
    	}
    	sb.append("</form>\n"); //$NON-NLS-1$
    	return sb.toString();
    }
   

	
    private XHTMLTagger showOptions(Context context) {
    	if ("options".equals(context.getArgs().get("show"))) { //$NON-NLS-1$ //$NON-NLS-2$
    		return new XHTMLTagger().start("a").attr("href", TARGET + "?method=emboss")
    		.text(Messages.getString(L10nKeys.EMBOSS_RESET_OPTIONS)).end();
    	} else {
    		return new XHTMLTagger().start("a").attr("href", TARGET + "?method=emboss&show=options")
    		.text(Messages.getString(L10nKeys.SHOW_OPTIONS)).end();
    	}
    }

	@Override
	public void close() {
		closing = true;
		findView.close();
		bookController.close();
	}
	@Override
	public synchronized void changeHappened(Object o) {/*
		if (bs.equals(o)) {
			//changeHappened = true;
			scanningInProgressLabel.setText("");
			componentsToUpdate.put(scanningInProgress.getIdentifier(), scanningInProgress);
		}*/
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Easy Embossing Utility " + VERSION + ", " + BUILD;
	}
	
}
