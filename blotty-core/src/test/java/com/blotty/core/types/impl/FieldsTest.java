package com.blotty.core.types.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.blotty.core.types.FieldType;

class FieldsTest {

	@Test
	void testBooleanField() {
		assertEquals(FieldType.BOOLEAN_TYPE, BooleanField.TRUE.getType());
		assertFalse(BooleanField.TRUE.isNull());

		assertSame(BooleanField.TRUE, BooleanField.of(true));
		assertSame(BooleanField.FALSE, BooleanField.of(false));

		assertSame(BooleanField.TRUE, BooleanField.of(1));
		assertSame(BooleanField.FALSE, BooleanField.of(0));

		assertSame(BooleanField.TRUE, BooleanField.of("true"));
		assertSame(BooleanField.TRUE, BooleanField.of("True"));
		assertSame(BooleanField.TRUE, BooleanField.of("yes"));
		assertSame(BooleanField.TRUE, BooleanField.of("Y"));
		assertSame(BooleanField.TRUE, BooleanField.of("ok"));
		assertSame(BooleanField.TRUE, BooleanField.of("1"));
		assertSame(BooleanField.FALSE, BooleanField.of("false"));
		assertSame(BooleanField.FALSE, BooleanField.of("no"));

		assertEquals("true", BooleanField.TRUE.toString());
		assertEquals("false", BooleanField.FALSE.toString());

		assertEquals(BooleanField.TRUE, BooleanField.of(true));
		assertNotEquals(BooleanField.TRUE, BooleanField.FALSE);

		assertTrue(BooleanField.FALSE.compareTo(BooleanField.TRUE) < 0);
		assertTrue(BooleanField.TRUE.compareTo(BooleanField.FALSE) > 0);
		assertEquals(0, BooleanField.TRUE.compareTo(BooleanField.TRUE));
		assertTrue(BooleanField.TRUE.compareGeneric(BooleanField.NULL) > 0);
	}

	@Test
	void testIntegerField() {
		assertEquals(FieldType.INTEGER_TYPE, IntegerField.ZERO.getType());
		assertFalse(IntegerField.ZERO.isNull());

		assertSame(IntegerField.ZERO, IntegerField.of(0));
		assertSame(IntegerField.ONE, IntegerField.of(1));
		assertSame(IntegerField.TEN, IntegerField.of(10));
		assertSame(IntegerField.ONE_HUNDRED, IntegerField.of(100));
		assertSame(IntegerField.ONE_THOUSAND, IntegerField.of(1000));

		assertEquals(IntegerField.of(42), IntegerField.of("42"));
		assertEquals("42", IntegerField.of(42).toString());

		// Test comparison and overflow safety
		IntegerField min = IntegerField.of(Integer.MIN_VALUE);
		IntegerField one = IntegerField.of(1);
		assertTrue(min.compareTo(one) < 0);
		assertTrue(one.compareTo(min) > 0);
		assertEquals(0, one.compareTo(IntegerField.of(1)));
		assertTrue(one.compareGeneric(IntegerField.NULL) > 0);
	}

	@Test
	void testLongField() {
		assertEquals(FieldType.LONG_TYPE, LongField.ZERO.getType());
		assertFalse(LongField.ZERO.isNull());

		assertSame(LongField.ZERO, LongField.of(0L));
		assertSame(LongField.ONE, LongField.of(1L));
		assertSame(LongField.TEN, LongField.of(10L));
		assertSame(LongField.ONE_HUNDRED, LongField.of(100L));
		assertSame(LongField.ONE_THOUSAND, LongField.of(1000L));

		assertEquals(LongField.of(42L), LongField.of("42"));

		// Test values exceeding 2^32 to verify cache check doesn't truncate to zero
		long largeVal = 4_294_967_296L; // 2^32
		LongField large = LongField.of(largeVal);
		assertNotEquals(LongField.ZERO, large);
		assertEquals(String.valueOf(largeVal), large.toString());

		// Test comparison across large spans
		assertTrue(LongField.of(3_000_000_000L).compareTo(LongField.of(0L)) > 0);
		assertTrue(LongField.of(0L).compareTo(LongField.of(3_000_000_000L)) < 0);
		assertTrue(LongField.of(1L).compareGeneric(LongField.NULL) > 0);
	}

	@Test
	void testDoubleField() {
		assertEquals(FieldType.DOUBLE_TYPE, DoubleField.ZERO.getType());
		assertFalse(DoubleField.ZERO.isNull());

		assertSame(DoubleField.ZERO, DoubleField.of(0.0));
		assertSame(DoubleField.ONE, DoubleField.of(1.0));

		assertEquals(DoubleField.of(3.14), DoubleField.of("3.14"));

		// Test fractional comparison (must not truncate to 0)
		DoubleField d1 = DoubleField.of(1.2);
		DoubleField d2 = DoubleField.of(1.8);
		assertTrue(d1.compareTo(d2) < 0);
		assertTrue(d2.compareTo(d1) > 0);
		assertEquals(0, d1.compareTo(DoubleField.of(1.2)));
		assertTrue(d1.compareGeneric(DoubleField.NULL) > 0);
	}

	@Test
	void testFloatField() {
		assertEquals(FieldType.FLOAT_TYPE, FloatField.ZERO.getType());
		assertFalse(FloatField.ZERO.isNull());

		assertSame(FloatField.ZERO, FloatField.of(0.0f));
		assertSame(FloatField.ONE, FloatField.of(1.0f));

		assertEquals(FloatField.of(2.5f), FloatField.of("2.5"));

		// Test fractional comparison (must not truncate to 0)
		FloatField f1 = FloatField.of(1.2f);
		FloatField f2 = FloatField.of(1.8f);
		assertTrue(f1.compareTo(f2) < 0);
		assertTrue(f2.compareTo(f1) > 0);
		assertEquals(0, f1.compareTo(FloatField.of(1.2f)));
		assertTrue(f1.compareGeneric(FloatField.NULL) > 0);
	}

	@Test
	void testShortField() {
		assertEquals(FieldType.SHORT_TYPE, ShortField.ZERO.getType());
		assertFalse(ShortField.ZERO.isNull());

		assertSame(ShortField.ZERO, ShortField.of((short) 0));
		assertSame(ShortField.ONE, ShortField.of((short) 1));

		assertEquals(ShortField.of((short) 15), ShortField.of("15"));
		assertTrue(ShortField.of((short) 10).compareTo(ShortField.of((short) 20)) < 0);
		assertTrue(ShortField.of((short) 20).compareTo(ShortField.of((short) 10)) > 0);
		assertTrue(ShortField.of((short) 1).compareGeneric(ShortField.NULL) > 0);
	}

	@Test
	void testStringField() {
		StringField s1 = StringField.of("Hello World");
		StringField s2 = StringField.of("Hello");
		StringField s3 = StringField.of("Hello World");

		assertEquals(FieldType.STRING_TYPE, s1.getType());
		assertFalse(s1.isNull());
		assertEquals("Hello World", s1.toString());
		assertEquals(s1, s3);
		assertEquals(s1.hashCode(), s3.hashCode());
		assertNotEquals(s1, s2);

		// Test contains
		assertTrue(s1.contains(s2)); // "Hello World" contains "Hello"
		assertFalse(s2.contains(s1)); // "Hello" does NOT contain "Hello World"

		// Test compareTo
		assertTrue(s2.compareTo(s1) < 0);
		assertTrue(s1.compareTo(s2) > 0);
		assertEquals(0, s1.compareTo(s3));
	}
}
