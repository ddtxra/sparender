package com.sparender;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SparenderUtilsTest {
	
	@Test
	public void testGetBaseUrl(){
		
		String baseUrl1 = SparenderUtils.getBaseUrl("https://karibou.ch");
		String baseUrl2 = SparenderUtils.getBaseUrl("https://karibou.ch/boum");

		assertEquals(baseUrl1, baseUrl2);
		assertEquals(baseUrl1, "https://karibou.ch");

	}

}
