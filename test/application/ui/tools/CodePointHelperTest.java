package application.ui.tools;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import application.ui.tools.CodePointHelper.Mode;
import application.ui.tools.CodePointHelper.Style;

public class CodePointHelperTest {
	
	@Test
	public void testParse_01() {
		String input = "0065,0066,0067";
		String expected =  "ABC";
		String actual = CodePointHelper.parse(input, Mode.DECIMAL);
		org.junit.Assert.assertEquals(expected, actual);
		
	}
	
	@Test
	public void testParse_02() {
		String actual = CodePointHelper.parse("32 | test\n33 | comment", Mode.DECIMAL);
		assertEquals(" !", actual);
	}
	
	@Test
	public void testFormat() {
		String input = "ABC";
		String expected = "65, 66, 67";
		String actual = CodePointHelper.format(input, Style.COMMA, Mode.DECIMAL);
		org.junit.Assert.assertEquals(expected, actual);
	}
}
