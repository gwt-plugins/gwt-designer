package com.instantiations.pde.build.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;


/**
 * The class <code>BuildPropertiesTest</code> contains tests for the class <code>{@link BuildProperties}</code>.
 *
 * @generatedBy CodePro at 12/9/08 2:41 PM
 * @author danrubel
 * @version $Revision: 1.0 $
 */
public class BuildPropertiesTest
{
	/**
	 * An instance of the class being tested.
	 *
	 * @see BuildProperties
	 *
	 * @generatedBy CodePro at 12/9/08 2:41 PM
	 */
	private BuildProperties fixtureCreateBuildProperties;

	/**
	 * Return an instance of the class being tested.
	 *
	 * @return an instance of the class being tested
	 *
	 * @see BuildProperties
	 *
	 * @generatedBy CodePro at 12/9/08 2:41 PM
	 */
	public BuildProperties getFixtureCreateBuildProperties()
		throws Exception {
		if (fixtureCreateBuildProperties == null) {
			fixtureCreateBuildProperties = BuildPropertiesFactory.createBuildProperties();
		}
		return fixtureCreateBuildProperties;
	}

	/**
	 * Run the String get(String) method test.
	 *
	 * @generatedBy CodePro at 12/9/08 2:41 PM
	 */
	@Test
	public void testGetMissing_1()
		throws Exception {
		BuildProperties fixture = getFixtureCreateBuildProperties();
		String key = "";
		try {
			fixture.get(key);
			fail("Should have thrown an exception");
		}
		catch (BuildPropertiesException e) {
			// success... fall through
		}
	}

	/**
	 * Run the String get(String) method test.
	 *
	 * @generatedBy CodePro at 12/9/08 2:41 PM
	 */
	@Test
	public void testGetMissing_2()
		throws Exception {
		BuildProperties fixture = getFixtureCreateBuildProperties();
		String key = "does-not-exist";
		try {
			fixture.get(key);
			fail("Should have thrown an exception");
		}
		catch (BuildPropertiesException e) {
			// success... fall through
		}
	}

	/**
	 * Run the String get(String) method test.
	 *
	 * @generatedBy CodePro at 12/9/08 2:41 PM
	 */
	@Test
	public void testGet_1()
		throws Exception {
		BuildProperties fixture = getFixtureCreateBuildProperties();
		String key = "my-key";
		String result = fixture.get(key);
		// add additional test code here
		assertEquals("my-value", result);
	}

	/**
	 * Run the String get(String) method test.
	 *
	 * @generatedBy CodePro at 12/9/08 2:41 PM
	 */
	@Test
	public void testGetRaw_1()
		throws Exception {
		BuildProperties fixture = getFixtureCreateBuildProperties();
		String key = "my-key";
		String result = fixture.getRaw(key);
		// add additional test code here
		assertEquals("my-value", result);
	}

	/**
	 * Run the String get(String) method test.
	 *
	 * @generatedBy CodePro at 12/9/08 2:41 PM
	 */
	@Test
	public void testGet_2()
		throws Exception {
		BuildProperties fixture = getFixtureCreateBuildProperties();
		String key = "my-key2";
		String result = fixture.get(key);
		// add additional test code here
		assertEquals("composed-my-valuex30", result);
	}

	@Test
	public void testGet_3() throws Exception {
		BuildProperties buildProperties = getFixtureCreateBuildProperties();
		String key = "deep";
		String result = buildProperties.get(key);
		assertEquals("composed-my-valuex30andmy-value", result);
	}

	@Test
	public void testGetInvalid_1() throws Exception {
		BuildProperties buildProperties = getFixtureCreateBuildProperties();
		String key = "not-right";
		try {
			buildProperties.get(key);
			fail("Should have thrown an exception");
		}
		catch (BuildPropertiesException e) {
			// success... fall through
		}
	}

	@Test
	public void testGetRecursive_1() throws Exception {
		BuildProperties buildProperties = getFixtureCreateBuildProperties();
		String key = "my-recursive";
		try {
			buildProperties.get(key);
			fail("Should have thrown an exception");
		}
		catch (BuildPropertiesException e) {
			// success... fall through
		}
	}

	/**
	 * Run the String get(String) method test.
	 *
	 * @generatedBy CodePro at 12/9/08 2:41 PM
	 */
	@Test
	public void testGetRaw_2()
		throws Exception {
		BuildProperties fixture = getFixtureCreateBuildProperties();
		String key = "my-key2";
		String result = fixture.getRaw(key);
		// add additional test code here
		assertEquals("composed-${my-key}x30", result);
	}

	@Test
	public void testGetRaw_3() throws Exception {
		BuildProperties buildProperties = getFixtureCreateBuildProperties();
		String key = "deep";
		String result = buildProperties.getRaw(key);
		assertEquals("${my-key2}and${my-key}", result);
	}

	@Test
	public void testGetRaw_4() throws Exception {
		BuildProperties buildProperties = getFixtureCreateBuildProperties();
		String key = "not-right";
		String result = buildProperties.getRaw(key);
		assertEquals("invalid-composed-${my-key", result);
	}

	@Test
	public void testGetList_1() throws Exception {
		BuildProperties buildProperties = getFixtureCreateBuildProperties();
		String key = "my-list-1";
		List<String> result = buildProperties.getList(key);
		assertNotNull(result);
		assertEquals(0, result.size());
	}

	@Test
	public void testGetList_2() throws Exception {
		BuildProperties buildProperties = getFixtureCreateBuildProperties();
		String key = "my-list-2";
		List<String> result = buildProperties.getList(key);
		assertEquals(Arrays.asList("6"), result);
	}

	@Test
	public void testGetList_3() throws Exception {
		BuildProperties buildProperties = getFixtureCreateBuildProperties();
		String key = "my-list-3";
		List<String> result = buildProperties.getList(key);
		assertEquals(Arrays.asList("8", "3"), result);
	}

	@Test
	public void testGetList_4() throws Exception {
		BuildProperties buildProperties = getFixtureCreateBuildProperties();
		String key = "my-list-4";
		List<String> result = buildProperties.getList(key);
		assertEquals(Arrays.asList("3", "2", "4"), result);
	}

	@Test
	public void testResolve_1() throws Exception {
		BuildProperties buildProperties = getFixtureCreateBuildProperties();
		String text = "foo";
		String result = buildProperties.resolve(text, Version.V_3_4, new ArrayList<String>());
		assertEquals("foo", result);
	}

	@Test
	public void testResolve_2() throws Exception {
		BuildProperties fixture = getFixtureCreateBuildProperties();
		String text = "${my-key}";
		String result = fixture.resolve(text, Version.V_3_4, new ArrayList<String>());
		assertEquals("my-value", result);
	}

	@Test
	public void testResolve_3() throws Exception {
		BuildProperties fixture = getFixtureCreateBuildProperties();
		String text = "A ${my-key}${my-key2} and more";
		String result = fixture.resolve(text, Version.V_3_4, new ArrayList<String>());
		assertEquals("A my-valuecomposed-my-valuex30 and more", result);
	}
}