package application.ui.tools;

class CodePointHelper {
	enum Style {
		XML,
		COMMA,
		LINE
	}
	enum Mode {
		HEX,
		DECIMAL
	}
	enum Input {
		NAME, CODE
	}

	/**
	 * Formats a number as a zero padded hex string of a specified length.
	 * @param i the number to format
	 * @param len the length of the resulting string
	 * @return returns a string of the specified length
	 */
	static String toHexString(int i, int len) {
		return padStr(Integer.toHexString(i), len, '0');
	}
	
	/**
	 * Parses a string as a sequence of code point entities and returns the
	 * corresponding characters. For example:
	 * "0065,0066,0067" will be return "ABC". 
	 * @param str the string to parse
	 * @param m the parser mode
	 * @return returns a string 
	 * @throws IllegalArgumentException if the string could not be parsed
	 */
   static String parse(String str, Mode m) {
    	if (str==null || str.equals("")) {
    		return "";
    	}
    	String[]strs;
    	Style st = (str.contains("&")?Style.XML:(str.contains("|")?Style.LINE:Style.COMMA));
    	switch (st) {
    		case XML:
    			strs = str.split(";");
    			break;
    		case LINE:
    			strs = str.split("[\\r\\n]+");
    			break;
    		case COMMA: default:
    			strs = str.split(",");
    			break;
    	}
    	StringBuffer ret = new StringBuffer();
    	for (String s : strs) {
    		s = s.trim();
    		switch (st) {
    			case XML:
    				switch (m) {
	    				case DECIMAL:
	        				if (s.startsWith("&#")) {
	        					s = s.substring(2);
	        				} else {
	        					throw new IllegalArgumentException("Cannot parse string");
	        				}
	    					break;
	    				case HEX: default:
	        				if (s.startsWith("&#x")) {
	        					s = s.substring(3);
	        				} else {
	        					throw new IllegalArgumentException("Cannot parse string");
	        				}
	    					break;
    				}
    				break;
    			case LINE:
    				int x = s.indexOf('|');
    				if (x>-1) {
    					s = s.substring(0, x).trim();
    				}
    				break;
    			case COMMA: default:
    				break;
    		}
    		switch (m) {
    		case DECIMAL:
    			ret.append((char)Integer.parseInt(s, 10));
    			break;
    		case HEX: default:
    			ret.append((char)Integer.parseInt(s, 16));
    			break;
    		}
    	}
    	return ret.toString();
    }
    
   /**
    * Formats a string as code point entities. 
    * @param str the string to format
    * @param s the formatting style
    * @param m the formatting mode
    * @return returns a string containing a sequence of code point entities
    */
	static String format(String str, Style s, Mode m) {
		StringBuffer sb = new StringBuffer();
		
		for (int i=0; i<str.length(); i++) {
			if (s==Style.XML) {
				switch (m) {
					case DECIMAL:
						sb.append("&#");
						break;
					case HEX: default:
						sb.append("&#x");
						break;
				}
			}
			int cp = str.codePointAt(i);
			switch (m) {
				case DECIMAL:
					sb.append(Integer.toString(cp));
					break;
				case HEX: default:
					sb.append(CodePointHelper.toHexString(cp, 4));
					break;
			}
			switch (s) {
				case XML:
					sb.append(";");
					break;
				case LINE:
					sb.append(" | ");
					sb.append(Character.getName(cp));
					sb.append('\n');
					break;
				case COMMA:
					if (i<str.length()-1) {
						sb.append(", ");
					}
					break;
			}
		}
		return sb.toString();
	}

	private static String padStr(String in, int len, char padding) {
		StringBuilder sb = new StringBuilder();
		for (int i=in.length(); i<len; i++) {
			sb.append(padding);
		}
		sb.append(in);
		return sb.toString();
	}
	
}