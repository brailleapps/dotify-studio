package application.prefs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.daisy.braille.api.embosser.EmbosserProperties;
import org.daisy.braille.api.embosser.PrintPage.Shape;
import org.daisy.braille.api.paper.Length;
import org.daisy.braille.api.paper.SheetPaperFormat;

import application.l10n.Messages;

class OptionNiceNames {
	private final Set<NiceName> printModeNN;
	private final Map<String, NiceName> shapeNN;
	private final Set<NiceName> orientationNN;
	private final Set<NiceName> lengthNN;
	private final Set<NiceName> zfoldingNN;
	private final Set<NiceName> alignNN;


	OptionNiceNames() {
		printModeNN = new HashSet<>();
		printModeNN.add(new NiceName(EmbosserProperties.PrintMode.REGULAR.toString(),
				Messages.OPTION_VALUE_REGULAR_PRINT_MODE.localize()));
		printModeNN.add(new NiceName(EmbosserProperties.PrintMode.MAGAZINE.toString(),
				Messages.OPTION_VALUE_MAGAZINE_PRINT_MODE.localize()));

    	shapeNN = new HashMap<>();
    	addToShape(new NiceName(Shape.LANDSCAPE.toString(), Messages.OPTION_VALUE_LANDSCAPE.localize()));
    	addToShape(new NiceName(Shape.PORTRAIT.toString(), Messages.OPTION_VALUE_PORTRAIT.localize()));
    	addToShape(new NiceName(Shape.SQUARE.toString(), Messages.OPTION_VALUE_SQUARE.localize()));
    	
    	orientationNN = new HashSet<>();
    	orientationNN.add(new NiceName(SheetPaperFormat.Orientation.DEFAULT.toString(), Messages.OPTION_VALUE_DEFAULT_ORIENTATION.localize()));
    	orientationNN.add(new NiceName(SheetPaperFormat.Orientation.REVERSED.toString(), Messages.OPTION_VALUE_REVERSED_ORIENTATION.localize()));
    	
    	zfoldingNN = new HashSet<>();
    	zfoldingNN.add(new NiceName("OFF", "Off"));
    	zfoldingNN.add(new NiceName("ON", "On"));

    	lengthNN = new HashSet<>();
    	lengthNN.add(new NiceName(Length.UnitsOfLength.INCH.toString(), "inch"));
    	lengthNN.add(new NiceName(Length.UnitsOfLength.CENTIMETER.toString(), "cm"));
    	lengthNN.add(new NiceName(Length.UnitsOfLength.MILLIMETER.toString(), "mm"));

    	alignNN = new HashSet<>();
    	alignNN.add(new NiceName("center_inner", Messages.OPTION_VALUE_PAGE_ALIGNMENT_CENTER.localize()));
    	alignNN.add(new NiceName("inner", Messages.OPTION_VALUE_PAGE_ALIGNMENT_INNER.localize()));
    	alignNN.add(new NiceName("outer", Messages.OPTION_VALUE_PAGE_ALIGNMENT_OUTER.localize()));
    	alignNN.add(new NiceName("left", Messages.OPTION_VALUE_PAGE_ALIGNMENT_LEFT.localize()));
    	alignNN.add(new NiceName("right", Messages.OPTION_VALUE_PAGE_ALIGNMENT_RIGHT.localize()));
	}
	
	private void addToShape(NiceName nn) {
		shapeNN.put(nn.getKey(), nn);
	}


	public Set<NiceName> getPrintModeNN() {
		return printModeNN;
	}


	public Map<String, NiceName> getShapeNN() {
		return shapeNN;
	}


	public Set<NiceName> getOrientationNN() {
		return orientationNN;
	}


	public Set<NiceName> getLengthNN() {
		return lengthNN;
	}


	public Set<NiceName> getZfoldingNN() {
		return zfoldingNN;
	}


	public Set<NiceName> getAlignNN() {
		return alignNN;
	}

}
