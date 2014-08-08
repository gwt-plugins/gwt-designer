package com.instantiations.pde.build.subproduct;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.TreeSet;

import org.junit.Test;

import static org.junit.Assert.*;

import com.instantiations.pde.build.util.BuildProperties;
import com.instantiations.pde.build.util.Version;

public class SubProductZipReaderTest
{
	private BuildProperties prop;

	@Test
	public void testReadAllZips() throws Exception {
		prop = new BuildProperties();
		prop.read();
		File subproductsDir = new File(prop.get("actual.build.subproducts"));
		if (!subproductsDir.exists())
			fail("actual.build.subproducts property does not point to a valid directory:\n   "
				+ subproductsDir.getCanonicalPath());
		for (String childName : new TreeSet<String>(Arrays.asList(subproductsDir.list()))) {
			File child = new File(subproductsDir, childName);
			if (child.isDirectory())
				testReadAllZips(child.getName(), child);
		}
		System.out.println("Test Complete");
	}

	private void testReadAllZips(String subProductName, File dir) throws IOException {
		for (String childName : new TreeSet<String>(Arrays.asList(dir.list()))) {
			File child = new File(dir, childName);
			if (child.isDirectory()) {
				testReadAllZips(subProductName, child);
			}
			else if (child.getName().endsWith(".zip")) {
				SubProductZipReader reader = testReadZip(subProductName, child);
				if (subProductName.equals("Shared"))
					assertSharedSubProduct(reader, child.getParentFile().getName());
			}
		}
	}

	private SubProductZipReader testReadZip(String subProductName, File productZipFile) throws IOException {
		System.out.println(productZipFile.getCanonicalPath());
		SubProductZipReader reader = new SubProductZipReader();
		reader.setZipFile(productZipFile);
		reader.setProp(prop);
		System.out.println("   ID = " + reader.getId());
		System.out.println("   Full Version = " + reader.getFullVersion());
		System.out.println("   Included Features");
		for (String featureId : reader.getIncludedFeatures()) {
			System.out.println("      " + featureId);
			assertTrue(featureId.indexOf('"') == -1);
		}
		System.out.println("   Included Plugins");
		for (String pluginId : reader.getIncludedPlugins()) {
			String featureId = reader.getPluginToFeature().get(pluginId);
			System.out.print("      " + pluginId + "  ");
			for (int i = pluginId.length(); i < 50; i++)
				System.out.print('.');
			System.out.println(" in feature " + featureId);
			assertTrue(pluginId.indexOf('"') == -1);
			assertNotNull(featureId);
		}
		System.out.println("   Required Features");
		for (String featureId : reader.getRequiredFeatures()) {
			System.out.println("      " + featureId);
			assertTrue(featureId.indexOf('"') == -1);
		}
		System.out.println("   Required Plugins");
		for (String pluginId : reader.getRequiredPlugins()) {
			System.out.println("      " + pluginId);
			assertTrue(pluginId.indexOf('"') == -1);
		}
		return reader;
	}

	private void assertSharedSubProduct(SubProductZipReader reader, String targetVersion) {
		assertEquals("com.instantiations.eclipse.shared", reader.getId());
		Version fullVersion = new Version(reader.getFullVersion());
		if (fullVersion.compareTo(new Version(5, 5, 0)) < 0)
			fail("Expected version >= 5.5.0, but found " + fullVersion.toStringBase());
		if (fullVersion.getQualifier().compareTo("200901010000") < 0)
			fail("Expected version build number > 200901010000, but found " + fullVersion.getQualifier());
		assertEquals(1, reader.getIncludedFeatures().size());
		if (!reader.getIncludedFeatures().contains("com.instantiations.eclipse.shared"))
			fail("Expected included features to contain com.instantiations.eclipse.shared");
		assertEquals(targetVersion.equals("2.1") ? 6 : 7, reader.getIncludedPlugins().size());
		if (!reader.getIncludedPlugins().contains("com.instantiations.common.help"))
			fail("Expected included plugins to contain com.instantiations.common.help");
		assertEquals(0, reader.getRequiredFeatures().size());
		assertTrue(reader.getRequiredPlugins().size() >= 3);
	}
}
