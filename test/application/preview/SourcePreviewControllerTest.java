package application.preview;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SourcePreviewControllerTest {

	@Test
	public void testXmlPattern_01() {
		assertTrue(SourcePreviewController.XML_PATTERN.matcher("application/xml").matches());
	}

	@Test
	public void testXmlPattern_02() {
		assertTrue(SourcePreviewController.XML_PATTERN.matcher("application/x-dtbook+xml").matches());
	}
	
	@Test
	public void testXmlPattern_03() {
		assertFalse(SourcePreviewController.XML_PATTERN.matcher("text/plain").matches());
	}
}
