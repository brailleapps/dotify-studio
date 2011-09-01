package com.googlecode.e2u;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.print.PrintService;

import org.daisy.braille.embosser.Embosser;
import org.daisy.braille.embosser.EmbosserCatalog;
import org.daisy.braille.embosser.EmbosserProperties;
import org.daisy.braille.tools.Length;
import org.daisy.paper.PrintPage.Shape;

import com.googlecode.ajui.ALabel;
import com.googlecode.ajui.AParagraph;
import com.googlecode.ajui.Context;
import com.googlecode.ajui.XHTMLTagger;
import com.googlecode.e2u.Settings.Keys;
import com.googlecode.e2u.l10n.L10nKeys;
import com.googlecode.e2u.l10n.Messages;

public class SettingsView extends AbstractSettingsView implements AListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = -3082172015930709465L;
	private final static String setupTarget = "setup";
	private final static Map<String, String> printModeNN;
	private final static Map<String, String> shapeNN;
	private final static Map<String, String> orientationNN;
	public final static Map<String, String> lengthNN;
	private final static Map<String, String> zfoldingNN;
	private final static Map<String, String> alignNN;
	private final static PrinterLookup printers;
	static {
		printers = PrinterLookup.getInstance();

		printModeNN = new LinkedHashMap<String, String>();
		printModeNN.put(EmbosserProperties.PrintMode.REGULAR.toString(),
				Messages.getString(L10nKeys.REGULAR_PRINT_MODE));
		printModeNN.put(EmbosserProperties.PrintMode.MAGAZINE.toString(),
				Messages.getString(L10nKeys.MAGAZINE_PRINT_MODE));

    	shapeNN = new HashMap<String, String>();
    	shapeNN.put(Shape.LANDSCAPE.toString(), Messages.getString(L10nKeys.LANDSCAPE));
    	shapeNN.put(Shape.PORTRAIT.toString(), Messages.getString(L10nKeys.PORTRAIT));
    	shapeNN.put(Shape.SQUARE.toString(), Messages.getString(L10nKeys.SQUARE));
    	
    	orientationNN = new LinkedHashMap<String, String>();
    	orientationNN.put("DEFAULT", "Default");
    	orientationNN.put("REVERSED", "Reversed");
    	
    	zfoldingNN = new LinkedHashMap<String, String>();
    	zfoldingNN.put("OFF", "Off");
    	zfoldingNN.put("ON", "On");

    	lengthNN = new LinkedHashMap<String, String>();
    	lengthNN.put(Length.UnitsOfLength.INCH.toString(), "inch");
    	lengthNN.put(Length.UnitsOfLength.CENTIMETER.toString(), "cm");
    	lengthNN.put(Length.UnitsOfLength.MILLIMETER.toString(), "mm");

    	alignNN = new LinkedHashMap<String, String>();
    	alignNN.put("center_inner", Messages.getString(L10nKeys.PAGE_ALIGNMENT_CENTER));
    	alignNN.put("inner", Messages.getString(L10nKeys.PAGE_ALIGNMENT_INNER));
    	alignNN.put("outer", Messages.getString(L10nKeys.PAGE_ALIGNMENT_OUTER));
    	alignNN.put("left", Messages.getString(L10nKeys.PAGE_ALIGNMENT_LEFT));
    	alignNN.put("right", Messages.getString(L10nKeys.PAGE_ALIGNMENT_RIGHT));

	}
	
	private final Settings settings;

	private final SelectComponent printModeSelect;
	private final LengthSelectComponent lengthSelect;
	private final SelectComponent orientationSelect;
	private final SelectComponent zFoldingSelect;
	private final SelectComponent alignSelect;
	private final SelectComponent embosserSelect;
	private SelectComponent tableSelect;
	private SelectComponent deviceSelect;
	private SelectComponent paperSelect;
	
	private Configuration conf;
	private MenuSystem menu;
	
	//private String device, embosser, paper, align, table, orientation, zFolding;
	
	public SettingsView(Settings settings, MenuSystem menu) {
		this.settings = settings;
		this.menu = menu;

		printModeSelect = new SelectComponent(printModeNN, "print-mode", Messages.getString(L10nKeys.PRINT_MODE), false, setupTarget);
		lengthSelect = new LengthSelectComponent(lengthNN, "length-unit", "length-value", Messages.getString(L10nKeys.CUT_LENGTH), true, setupTarget);
    	orientationSelect = new SelectComponent(orientationNN, "orientation", Messages.getString(L10nKeys.ORIENTATION), false, setupTarget);
    	zFoldingSelect = new SelectComponent(zfoldingNN, "zfolding", Messages.getString(L10nKeys.Z_FOLDING), false, setupTarget);

    	{
	    	Collection<Embosser> embossers = EmbosserCatalog.newInstance().list();
	    	embosserSelect = new SelectComponent(embossers, "embosser", null, Messages.getString(L10nKeys.EMBOSSER), true, setupTarget);
    	}
    	
    	
    	alignSelect = new SelectComponent(alignNN, "align", Messages.getString(L10nKeys.PAGE_ALIGNMENT), true, setupTarget);
		setClass("group");
		conf = Configuration.getConfiguration(settings);
	}
	
	@Override
	public XHTMLTagger getHTML(Context context) {
		//update settings
    	String device = settings.getSetPref(Keys.device, context.getArgs().get("device"));
    	String embosser = settings.getSetPref(Keys.embosser, context.getArgs().get("embosser"));
    	String printMode = settings.getSetPref(Keys.printMode, context.getArgs().get("print-mode"));
    	String paper = settings.getSetPref(Keys.paper, context.getArgs().get("paper"));
    	String lengthValue = settings.getSetPref(Keys.cutLengthValue, context.getArgs().get("length-value"));
    	String lengthUnit = settings.getSetPref(Keys.cutLengthUnit, context.getArgs().get("length-unit"));
    	String align = settings.getSetPref(Keys.align, context.getArgs().get("align"));
    	String table = settings.getSetPref(Keys.table, context.getArgs().get("table"));

    	String orientation = settings.getSetPref(Keys.orientation, context.getArgs().get("orientation"), "DEFAULT");
    	String zFolding = settings.getSetPref(Keys.zFolding, context.getArgs().get("zfolding"), "OFF");

    	if (conf==null || context.getArgs().size()>1) {
    		conf = Configuration.getConfiguration(settings);
    	}

    	//update view
    	clear();
    	if (menu!=null) {
    		add(menu);
    	}
		if (deviceSelect==null) {
	    	Map<String, String> printerNames = new LinkedHashMap<String, String>();
	    	for (PrintService s : printers.getPrinters()) {
	    		printerNames.put(s.getName(), s.getName());
	    	}
	    	deviceSelect = new SelectComponent(printerNames, "device", Messages.getString(L10nKeys.DEVICE), true, setupTarget);
		}
    	select(deviceSelect, device);
    	add(deviceSelect);
    	if (!"".equals(device)) {
    		select(embosserSelect, embosser);
    		add(embosserSelect);
    		if (conf.supportsBothPrintModes()) {
    			select(printModeSelect, printMode);
    			add(printModeSelect);
    		}
			if (conf.getSupportedTables().size()>1) {
    			tableSelect = new SelectComponent(conf.getSupportedTables(), "table", null, Messages.getString(L10nKeys.EMBOSSER_TABLE), true, setupTarget);
    	    	select(tableSelect, table);
    			add(tableSelect);
    		}
			paperSelect = new SelectComponent(conf.getSupportedPapers(), "paper", null, Messages.getString(L10nKeys.PAPERSIZE), true, setupTarget);
    		select(paperSelect, paper);
    		add(paperSelect);
    		if (conf.isRollPaper()) {
    			lengthSelect.setValue(lengthValue);
    			select(lengthSelect, lengthUnit);
    			add(lengthSelect);
    		}
    		if (conf.supportsOrientation()) {
    			select(orientationSelect, orientation);
    			add(orientationSelect);
    		}

    		{
    			AParagraph p = new AParagraph();
    			p.setClass("desc");
    			if (conf.settingOK()) {
	    			p.add(new ALabel(MessageFormat.format((conf.getShape()==null?"":shapeNN.get(conf.getShape().name())) + 
	    					", " + Messages.getString(L10nKeys.PAPER_DIM), 
	    					conf.getPaperWidth(), conf.getPaperHeight(), conf.getMaxWidth(), conf.getMaxHeight())));
    			}
    			add(p);
    			//tagger.start("p").attr("class", "desc").text(MessageFormat.format(s.name().toLowerCase() + ": " + Messages.getString(L10nKeys.PAPER_DIM_CELLS), em.getMaxWidth(pf), em.getMaxHeight(pf))).end();
    		}
    		if (conf.supportsZFolding()) {
    			select(zFoldingSelect, zFolding);
    			add(zFoldingSelect);
    		}
    		if (conf.supportsAligning()) {
        		select(alignSelect, align);
        		add(alignSelect);    			
    		}
    	}

		return super.getHTML(context);
	}


	public Configuration getConfiguration() {
		return conf;
	}

	@Override
	public void changeHappened(Object o) {
		conf = Configuration.getConfiguration(settings);
		update();
	}

}
