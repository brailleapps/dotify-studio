package application.ui.tools;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import application.ui.tools.CodePointHelper.Mode;

public class CodePointHelperTest {

	@Test
	public void testParse() {
		String actual = CodePointHelper.parse("32 | test\n33 | comment", Mode.DECIMAL);
		assertEquals(" !", actual);
	}
}
