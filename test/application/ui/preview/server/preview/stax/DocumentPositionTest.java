package application.ui.preview.server.preview.stax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class DocumentPositionTest {

	@Test
	public void testIsBefore_01() {
		DocumentPosition lc1 = DocumentPosition.with(1, 2);
		DocumentPosition other = DocumentPosition.with(1, 3);
		assertTrue(lc1.isBefore(other));
	}
	
	@Test
	public void testIsBefore_02() {
		DocumentPosition lc1 = DocumentPosition.with(1, 2);
		DocumentPosition other = DocumentPosition.with(2, 1);
		assertTrue(lc1.isBefore(other));
	}

	@Test
	public void testIsBefore_03() {
		DocumentPosition lc1 = DocumentPosition.with(2, 1);
		DocumentPosition other = DocumentPosition.with(1, 2);
		assertFalse(lc1.isBefore(other));
	}
	
	@Test
	public void testIsBefore_04() {
		DocumentPosition lc1 = DocumentPosition.with(2, 1);
		DocumentPosition other = DocumentPosition.with(1, 2);
		assertFalse(lc1.isBefore(other));
	}
	
	@Test
	public void testIsBefore_05() {
		DocumentPosition lc1 = DocumentPosition.with(2, 2);
		DocumentPosition other = DocumentPosition.with(2, 1);
		assertFalse(lc1.isBefore(other));
	}
	
	@Test
	public void testIsBefore_06() {
		DocumentPosition lc1 = DocumentPosition.with(2, 2);
		DocumentPosition other = DocumentPosition.with(2, 2);
		assertFalse(lc1.isBefore(other));
	}
	
	@Test
	public void testSort() {
		List<DocumentPosition> lc = new ArrayList<>();
		DocumentPosition lc3 = DocumentPosition.with(3, 1);
		DocumentPosition lc2 = DocumentPosition.with(2, 6); 
		DocumentPosition lc1 = DocumentPosition.with(2, 1); 
		lc.add(lc3);
		lc.add(lc2);
		lc.add(lc1);
		Collections.sort(lc);
		assertEquals(lc1, lc.get(0));
		assertEquals(lc2, lc.get(1));
		assertEquals(lc3, lc.get(2));
	}

}
