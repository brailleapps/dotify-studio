package com.googlecode.e2u;

import java.util.ArrayList;
import java.util.Collection;

import org.daisy.braille.embosser.Embosser;
import org.daisy.braille.embosser.EmbosserCatalog;
import org.daisy.braille.embosser.EmbosserFeatures;
import org.daisy.braille.embosser.EmbosserProperties.PrintMode;
import org.daisy.braille.table.TableCatalog;
import org.daisy.braille.tools.Length;
import org.daisy.factory.FactoryProperties;
import org.daisy.paper.PageFormat;
import org.daisy.paper.Paper;
import org.daisy.paper.PaperCatalog;
import org.daisy.paper.PrintPage;
import org.daisy.paper.PrintPage.Shape;
import org.daisy.paper.RollPaperFormat;
import org.daisy.paper.SheetPaperFormat;
import org.daisy.paper.TractorPaperFormat;

import com.googlecode.e2u.Settings.Keys;


public class Configuration {
	enum ErrorCode {
		NOT_SET,
		INCOMPLETE,
		INVALID
	}
	private final Settings settings;
	private final PaperCatalog paperCatalog;
	private EmbosserCatalog embosserCatalog;
	private TableCatalog tableCatalog;
	private Embosser em;
	private Collection<FactoryProperties> supportedTables;
	private ArrayList<Paper> supportedPapers; 
	private PageFormat pageFormat;
	private final Paper p;
	
	private boolean supportsPrintModeSelect = false;
	private boolean supportsOrientation = false;
	private boolean supportsZFolding = false;
	private boolean supportsAligning = false;
	private boolean isRollPaper = false;
	private boolean settingsOK = false;
	private int width = 0;
	private int height = 0;
	private Length pWidth = Length.newMillimeterValue(0);
	private Length pHeight = Length.newMillimeterValue(0);
	private Shape s = null;
	private ErrorCode errorCode = ErrorCode.NOT_SET;
	
	
	private Configuration(Settings settings) {
		this.settings = settings;
		this.paperCatalog = PaperCatalog.newInstance();
		this.embosserCatalog = EmbosserCatalog.newInstance();
		this.tableCatalog = TableCatalog.newInstance();
		
    	//String device = settings.getString(Keys.device);
    	String embosser = settings.getString(Keys.embosser);
    	String printMode = settings.getString(Keys.printMode);
    	String paper = settings.getString(Keys.paper);
    	String lengthValue = settings.getString(Keys.cutLengthValue);
    	String lengthUnit = settings.getString(Keys.cutLengthUnit);
    	//String align = settings.getString(Keys.align);
    	String table = settings.getString(Keys.table);

    	String orientation = settings.getString(Keys.orientation, "DEFAULT");
    	String zFolding = settings.getString(Keys.zFolding);
		errorCode = ErrorCode.INCOMPLETE;
		if ((em = embosserCatalog.get(embosser))==null) {
			supportedTables = new ArrayList<FactoryProperties>();
			supportedPapers = new ArrayList<Paper>();
		} else {
			try {
				em.setFeature(EmbosserFeatures.TABLE, table);
			} catch (IllegalArgumentException e) {

			}
			supportsPrintModeSelect = em.supportsPrintMode(PrintMode.REGULAR) && em.supportsPrintMode(PrintMode.MAGAZINE);
			if (supportsPrintModeSelect) {
				em.setFeature(EmbosserFeatures.SADDLE_STITCH, PrintMode.MAGAZINE.toString().equals(printMode));
			}
			supportedTables = tableCatalog.list(em.getTableFilter());
	    	supportedPapers = new ArrayList<Paper>();
			for (Paper p : paperCatalog.list()) {
				if (em.supportsPaper(p)) {
					supportedPapers.add(p);
				}
			}
			supportsAligning = em.supportsAligning();

		}

    	pageFormat = null;
    	if (paper!=null && !"".equals(paper)) {
    		p = paperCatalog.get(paper);
    		if (p!=null) {
	        	switch (p.getType()) {
	        		case SHEET:
	        			pageFormat = new SheetPaperFormat(p.asSheetPaper(), getOrientation(orientation));
	        			isRollPaper = false;
	        			break;
	        		case TRACTOR:
	        			pageFormat = new TractorPaperFormat(p.asTractorPaper());
	        			isRollPaper = false;
	        			break;
	        		case ROLL:
	        			isRollPaper = true;
	        			Length.UnitsOfLength units;
	        			try {
	        				units = Length.UnitsOfLength.valueOf(lengthUnit);
	        			} catch (Exception e) {
	        				break;
	        			}
	        			double val;
	        			try {
	        				val = Double.parseDouble(lengthValue);
	        			} catch (NumberFormatException e) {
	        				val = 0;
	        			}
	        			Length l;
	        			switch (units) {
	        				case MILLIMETER:
	        					l = Length.newMillimeterValue(val);
	        					break;
	        				case CENTIMETER:
	        					l = Length.newCentimeterValue(val);
	        					break;
	        				case INCH:
	        					l = Length.newInchValue(val);
	        					break;
	        				default:
	        					throw new RuntimeException("Coding error");
	        			}
	        			pageFormat = new RollPaperFormat(p.asRollPaper(), l);
	        			break;
	        		default:
	        			throw new RuntimeException("coding error");
	        	}
    		}
	    	if (pageFormat!=null) {
	    		PrintPage pp = em.getPrintPage(pageFormat);
	    		pWidth = Length.newMillimeterValue(Math.round(pp.getWidth()));
	    		pHeight = Length.newMillimeterValue(Math.round(pp.getHeight()));
	    		s = pp.getShape();
	    		if (pageFormat.getPageFormatType()==PageFormat.Type.SHEET && !s.equals(Shape.SQUARE)) {
	    			supportsOrientation = true;
	    		} else if (pageFormat.getPageFormatType()==PageFormat.Type.TRACTOR && em.supportsZFolding()) {
	    			em.setFeature(EmbosserFeatures.Z_FOLDING, "ON".equals(zFolding));
	    			supportsZFolding = true;
	    	    }
				width = em.getMaxWidth(pageFormat);
				height = em.getMaxHeight(pageFormat);
				try {
					em.setFeature(EmbosserFeatures.PAGE_FORMAT, pageFormat);
					settingsOK = true;
				} catch (IllegalArgumentException e) {
					errorCode = ErrorCode.INVALID;
					e.printStackTrace();
				}
	    	}
    	} else {
    		p = null;
    	}

	}

	private SheetPaperFormat.Orientation getOrientation(String orientation) {
		try {
			return SheetPaperFormat.Orientation.valueOf(orientation);
		} catch (Exception e) {
			System.err.println(orientation);
			e.printStackTrace();
			return SheetPaperFormat.Orientation.DEFAULT;
		}
	}
	
	public EmbosserCatalog getEmbosserCatalog() {
		return embosserCatalog;
	}

	public Settings getSettings() {
		return settings;
	}
	
	public Collection<FactoryProperties> getSupportedTables() {
		return supportedTables;
	}
	
	public Collection<Paper> getSupportedPapers() {
		return supportedPapers;
	}
	
	public boolean supportsOrientation() {
		return supportsOrientation;
	}
	
	public boolean supportsZFolding() {
		return supportsZFolding;
	}
	
	public boolean supportsAligning() {
		return supportsAligning;
	}
	
	public boolean supportsBothPrintModes() {
		return supportsPrintModeSelect;
	}
	
	public boolean isRollPaper() {
		return isRollPaper;
	}
	
	public boolean settingOK() {
		return settingsOK;
	}
	
	public ErrorCode getErrorCode() {
		return errorCode;
	}
	
	public int getMaxWidth() {
		return width;
	}
	
	public int getMaxHeight() {
		return height;
	}
	
	public Shape getShape() {
		return s;
	}
	
	public Length getPaperWidth() {
		return pWidth;
	}
	
	public Length getPaperHeight() {
		return pHeight;
	}
	
	public String getPaperName() {
		return p.getDisplayName();
	}
	
	public Embosser getConfiguredEmbosser() {
		return em;
	}
	
	public static Configuration getConfiguration(Settings settings) {
		return new Configuration(settings);
	}

}
