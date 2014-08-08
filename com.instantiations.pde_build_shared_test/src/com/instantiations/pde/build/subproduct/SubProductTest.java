package com.instantiations.pde.build.subproduct;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.junit.Test;

import test.util.FileUtil;

import com.instantiations.pde.build.util.BuildProperties;
import com.instantiations.pde.build.util.OemVersion;
import com.instantiations.pde.build.util.ProductDownloader;
import com.instantiations.pde.build.util.Version;

import static org.junit.Assert.*;

public class SubProductTest
{
	private static BuildProperties prop = null;
	private static ProductDownloader productCache = null;
	
	private static BuildProperties getBuildProp() throws IOException {
		if (prop != null)
			return prop;
		prop = new BuildProperties();
		prop.read();
		if (!prop.get("build.root").endsWith("test"))
			fail("To prevent inadvertantly deleting important files, build.root must end with 'test'");
		return prop;
	}
	
	private static ProductDownloader getProductCache() throws IOException {
		if (productCache != null)
			return productCache;
		productCache = new ProductDownloader();
		productCache.setProp(getBuildProp());
		FileUtil.deleteFiles(new File(prop.get("build.subproducts")));
		FileUtil.copyFiles(new File("testdata/subproducts"), new File(prop.get("build.subproducts")));
		return productCache;
	}
	
	public static SubProduct newSubProduct(String name, OemVersion targetVersion) throws IOException {
		SubProduct subproduct = new SubProduct();
		subproduct.setName(name);
		subproduct.setTargetVersion(targetVersion);
		subproduct.setProductCache(getProductCache());
		subproduct.setProp(getBuildProp());
		return subproduct;
	}
	
	@Test
	public void testReadZipFile1() throws IOException {
		assertSubProductTest1(newSubProduct("Test1", OemVersion.V_3_4));
	}
	
	@Test
	public void testReadInfoFile1() throws IOException {
		assertSubProductTest1(newSubProduct("Test1", OemVersion.V_3_4));
	}

	private void assertSubProductTest1(SubProduct subproduct) {
		assertEquals("test1", subproduct.getId());
		assertEquals("1.0.0.200903101318", subproduct.getFullVersion());
		
		Collection<String> includedFeatures = subproduct.getIncludedFeatures();
		assertEquals(1, includedFeatures.size());
		assertTrue(includedFeatures.contains("test1"));
		
		Collection<String> includedPlugins = subproduct.getIncludedPlugins();
		assertEquals(1, includedPlugins.size());
		assertTrue(includedPlugins.contains("test1"));
		assertEquals("test1", subproduct.getContainingFeature("test1"));
		
		Collection<String> requiredFeatures = subproduct.getRequiredFeatures();
		assertEquals(2, requiredFeatures.size());
		assertTrue(requiredFeatures.contains("com.instantiations.eclipse.shared"));
		assertTrue(requiredFeatures.contains("test7"));
		
		Collection<String> requiredPlugins = subproduct.getRequiredPlugins();
		assertEquals(3, requiredPlugins.size());
		assertTrue(requiredPlugins.contains("com.instantiations.assist.eclipse.analysis.ant"));
		assertTrue(requiredPlugins.contains("org.eclipse.ui"));
		assertTrue(requiredPlugins.contains("org.eclipse.core.runtime"));
	}
	
	@Test
	public void testReadZipFile2() throws IOException {
		assertSubProductTest2(newSubProduct("Test2", new OemVersion("CodeGear", Version.V_3_2)));
	}
	
	@Test
	public void testReadInfoFile2() throws IOException {
		assertSubProductTest2(newSubProduct("Test2", new OemVersion("CodeGear", Version.V_3_2)));
	}

	private void assertSubProductTest2(SubProduct subproduct) {
		assertEquals("test2", subproduct.getId());
		assertEquals("1.0.0.200903191213", subproduct.getFullVersion());
		assertEquals("test2", subproduct.getContainingFeature("test2"));
	}
}
