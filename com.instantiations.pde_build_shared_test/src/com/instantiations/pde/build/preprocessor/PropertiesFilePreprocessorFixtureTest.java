package com.instantiations.pde.build.preprocessor;

import org.junit.Test;

import com.instantiations.pde.build.util.Version;

import static org.junit.Assert.*;

public class PropertiesFilePreprocessorFixtureTest extends LineBasedPreprocessorTest
{
	@Test
	public void test_null_30() throws Exception {
		PropertiesFilePreprocessorFixture fixture = new PropertiesFilePreprocessorFixture(null, Version.V_3_0);
		assertEquals(fixture.getExpected(), fixture.process());
	}

	@Test
	public void test_null_31() throws Exception {
		PropertiesFilePreprocessorFixture fixture = new PropertiesFilePreprocessorFixture(null, Version.V_3_1);
		assertEquals(fixture.getExpected(), fixture.process());
	}

	@Test
	public void test_null_32() throws Exception {
		PropertiesFilePreprocessorFixture fixture = new PropertiesFilePreprocessorFixture(null, Version.V_3_2);
		assertEquals(fixture.getExpected(), fixture.process());
	}

	@Test
	public void test_null_33() throws Exception {
		PropertiesFilePreprocessorFixture fixture = new PropertiesFilePreprocessorFixture(null, Version.V_3_3);
		assertEquals(fixture.getExpected(), fixture.process());
	}

	@Test
	public void test_null_34() throws Exception {
		PropertiesFilePreprocessorFixture fixture = new PropertiesFilePreprocessorFixture(null, Version.V_3_4);
		assertEquals(fixture.getExpected(), fixture.process());
	}

	@Test
	public void test_null_35() throws Exception {
		PropertiesFilePreprocessorFixture fixture = new PropertiesFilePreprocessorFixture(null, Version.V_3_5);
		assertEquals(fixture.getExpected(), fixture.process());
	}

	@Test
	public void test_CodeGear_32() throws Exception {
		PropertiesFilePreprocessorFixture fixture = new PropertiesFilePreprocessorFixture("CodeGear", Version.V_3_2);
		assertEquals(fixture.getExpected(), fixture.process());
	}

	@Test
	public void test_CodeGear_33() throws Exception {
		PropertiesFilePreprocessorFixture fixture = new PropertiesFilePreprocessorFixture("CodeGear", Version.V_3_3);
		assertEquals(fixture.getExpected(), fixture.process());
	}

	@Test
	public void test_CodeGear_34() throws Exception {
		PropertiesFilePreprocessorFixture fixture = new PropertiesFilePreprocessorFixture("CodeGear", Version.V_3_4);
		assertEquals(fixture.getExpected(), fixture.process());
	}
}