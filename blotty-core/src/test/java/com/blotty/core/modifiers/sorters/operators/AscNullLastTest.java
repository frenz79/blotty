package com.blotty.core.modifiers.sorters.operators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.blotty.core.types.impl.IntegerField;

class AscNullLastTest {

	@Test
	void testCompareBothNull() {
		AscNullLast op = new AscNullLast();
		assertEquals(0, op.compare(IntegerField.NULL, IntegerField.NULL));
	}

	@Test
	void testCompareFirstNull() {
		AscNullLast op = new AscNullLast();
		assertEquals(1, op.compare(IntegerField.NULL, IntegerField.of(5)));
	}

	@Test
	void testCompareSecondNull() {
		AscNullLast op = new AscNullLast();
		assertEquals(-1, op.compare(IntegerField.of(5), IntegerField.NULL));
	}

	@Test
	void testCompareNonNullValues() {
		AscNullLast op = new AscNullLast();
		assertTrue(op.compare(IntegerField.of(3), IntegerField.of(5)) < 0);
		assertTrue(op.compare(IntegerField.of(5), IntegerField.of(3)) > 0);
		assertEquals(0, op.compare(IntegerField.of(5), IntegerField.of(5)));
	}

	@Test
	void testToString() {
		assertEquals("AscNullLast", new AscNullLast().toString());
	}
}
