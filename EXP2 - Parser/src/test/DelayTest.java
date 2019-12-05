package test;
import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import parser.Delay;

class DelayTest {

	@Test
	void testNormalDelay() {
		assertEquals(3l,Delay.getDelay("191212", "1245", "191212", "1248"));
		assertEquals(-3l,Delay.getDelay("191212", "1248", "191212", "1245"));
	}

	@Test
	void testHourDelay() {
		assertEquals(17l,Delay.getDelay("191212", "1245", "191212", "1302"));
		assertEquals(-17l,Delay.getDelay("191212", "1302", "191212", "1245"));
	}

	@Test
	void testDayDelay() {
		assertEquals(19l,Delay.getDelay("191212", "2345", "191213", "0004"));
		assertEquals(-19l,Delay.getDelay("191213", "0004", "191212", "2345"));
	}

	@Test
	void testMonthDelay() {
		assertEquals(19l,Delay.getDelay("191130", "2345", "191201", "0004"));
		assertEquals(-19l,Delay.getDelay("191201", "0004", "191130", "2345"));
	}

	@Test
	void testYearDelay() {
		assertEquals(19l,Delay.getDelay("191231", "2345", "200101", "0004"));
		assertEquals(-19l,Delay.getDelay("200101", "0004", "191231", "2345"));
	}

	@Test
	void testCenturyDelay() {
		assertEquals(19l,Delay.getDelay("991231", "2345", "000101", "0004"));
		assertEquals(-19l,Delay.getDelay("000101", "0004", "991231", "2345"));
	}

	@Test
	void testLeapYear() {
		assertEquals(19l+60*24,Delay.getDelay("200228", "2345", "200301", "0004"));
		assertEquals(-19l-60*24,Delay.getDelay("200301", "0004", "200228", "2345"));
	}

}