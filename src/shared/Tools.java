package shared;

import org.daisy.braille.api.paper.Length;

/**
 * Provides tools common to both e2u and application packages.
 * @author Joel Håkansson
 *
 */
public class Tools {

	private Tools() {
		// no instances
	}
	
	/**
	 * Returns a length for the specified string inputs.
	 * @param valStr the value
	 * @param unitStr the unit
	 * @return returns the length
	 */
	public static Length parseLength(String valStr, String unitStr) {
		Length.UnitsOfLength unit = Length.UnitsOfLength.valueOf(unitStr);
		double val = Double.parseDouble(valStr);
		switch (unit) {
			case CENTIMETER:
				return Length.newCentimeterValue(val);
			case MILLIMETER:
				return Length.newMillimeterValue(val);
			case INCH:
				return Length.newInchValue(val);
			default:
				throw new RuntimeException();
		}
	}

	
}
