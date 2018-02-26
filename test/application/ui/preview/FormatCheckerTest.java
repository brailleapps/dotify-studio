package application.ui.preview;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class FormatCheckerTest {

	@Test
	public void testXmlPattern_01() {
		assertTrue(FormatChecker.XML_PATTERN.matcher("application/xml").matches());
	}

	@Test
	public void testXmlPattern_02() {
		assertTrue(FormatChecker.XML_PATTERN.matcher("application/x-dtbook+xml").matches());
	}
	
	@Test
	public void testXmlPattern_03() {
		assertFalse(FormatChecker.XML_PATTERN.matcher("text/plain").matches());
	}
}
