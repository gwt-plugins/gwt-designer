package com.instantiations.pde.build.subproduct;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;

import org.apache.tools.ant.BuildException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.util.CopyFileFilter;
import test.util.FileUtil;

import com.instantiations.pde.build.util.BuildProperties;
import com.instantiations.pde.build.util.OemVersion;
import com.instantiations.pde.build.util.ProductDownloader;

import static org.junit.Assert.*;

public class SubProductManagerTest
{
	private static BuildProperties prop = null;
	private static ProductDownloader productCache = null;
	private static SubProductManager manager = null;
	private static String cachedSubproductsAgeMaxValue = null;

	
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
		FileUtil.copyFiles(new File(prop.get("actual.build.subproducts")), new File(prop.get("build.subproducts")),
			new CopyFileFilter() {
				public boolean shouldCopyFile(File file) {
					return file.getPath().contains("3.4") && file.getName().endsWith(".zip");
				}
			});
		return productCache;
	}

	private SubProductManager getManager() throws Exception {
		if (manager != null)
			return manager;
		manager = new SubProductManager();
		manager.setTargetVersion(OemVersion.V_3_4);
		manager.setProductCache(getProductCache());
		manager.setProp(getBuildProp());
		return manager;
	}

	@Test
	public void testCantFindDependencies() throws Exception {
		try {
			getManager().getAllDependencies("does-not-exist");
			fail("Exception should have been thrown");
		}
		catch (BuildException e) {
			// This test should throw a build exception
		}
	}
	
	@Before
	public void setupBuildProperties() throws Exception {
		cachedSubproductsAgeMaxValue = getBuildProp().get("build.subproducts.age.max");
		getBuildProp().set("build.subproducts.age.max", "-1");
	}
	
	@After
	public void tearDownBuildProperties() throws Exception {
		getBuildProp().set("build.subproducts.age.max", cachedSubproductsAgeMaxValue);
	}

	@Test
	public void testSharedDependencies() throws Exception {
		Collection<String> dependencies = getManager().getAllDependencies("Shared");
		assertEquals(0, dependencies.size());
	}

	@Test
	public void testCodeProCoreDependencies() throws Exception {
		Collection<String> dependencies = getManager().getAllDependencies("CodeProCore");
		assertEquals(1, dependencies.size());
		assertTrue(dependencies.contains("Shared"));
	}

	@Test
	public void testCodeCoverageDependencies() throws Exception {
		Collection<String> dependencies = getManager().getAllDependencies("CodeCoverage");
		assertEquals(2, dependencies.size());
		assertTrue(dependencies.contains("Shared"));
		assertTrue(dependencies.contains("CodeProCore"));
	}

	@Test
	public void testListAllDependencies() throws Exception {
		
		// Cycle through once building and caching information
		
		System.out.println(">>> Starting testListAllDependencies() scan and cache");
		Map<String, Collection<String>> expected = new HashMap<String, Collection<String>>();
		File root = new File(prop.get("build.subproducts"));
		for (String name : new TreeSet<String>(Arrays.asList(root.list()))) {
			File child = new File(root, name);
			if (!new File(child, "3.4/" + name + ".zip").exists())
				continue;
			System.out.println(name + " Dependencies");
			Collection<String> dependencies = getManager().getAllDependencies(name);
			for (String depName : dependencies)
				System.out.println("   " + depName);
			expected.put(name, dependencies);
		}
		
		// Cycle through a 2nd time with a new manager using the cached information
		
		System.out.println(">>> Starting testListAllDependencies() reuse cached");
		File rootFile = new File(root, "BuildCommon/3.4/subproducts.xml");
		long rootLastModified = rootFile.lastModified();
		assertTrue(rootLastModified > 0);
		File sharedFile = new File(root, "Shared/3.4/subproduct.xml");
		long sharedLastModified = sharedFile.lastModified();
		assertTrue(sharedLastModified > 0);
		File coreFile = new File(root, "CodeProCore/3.4/subproduct.xml");
		long coreLastModified = coreFile.lastModified();
		assertTrue(coreLastModified > 0);
		
		manager = null;
		for (String name : new TreeSet<String>(Arrays.asList(root.list()))) {
			File child = new File(root, name);
			if (!new File(child, "3.4/" + name + ".zip").exists())
				continue;
			Collection<String> expectedDependencies = expected.get(name);
			Collection<String> actualDependencies = getManager().getAllDependencies(name);
			if (expectedDependencies.size() != actualDependencies.size() || !actualDependencies.containsAll(expectedDependencies)) {
				System.out.println("Expected " + name + " Dependencies");
				for (String depName : expectedDependencies)
					System.out.println("   " + depName);
				System.out.println("Actual " + name + " Dependencies");
				for (String depName : actualDependencies)
					System.out.println("   " + depName);
				fail("Expected and actual dependencies don't match for " + name);
			}
		}

		assertEquals(rootLastModified, rootFile.lastModified());
		assertEquals(sharedLastModified, sharedFile.lastModified());
		assertEquals(coreLastModified, coreFile.lastModified());
	}

	@Test
	public void testEmptyFeaturesContainingPlugins1() throws Exception {
		Collection<String> features = new HashSet<String>();
		Collection<String> plugins = getManager().getFeaturesContainingPlugins(features);
		assertEquals(0, plugins.size());
	}

	@Test
	public void testEmptyFeaturesContainingPlugins2() throws Exception {
		Collection<String> features = new HashSet<String>();
		features.add("does.not.exist");
		Collection<String> plugins = getManager().getFeaturesContainingPlugins(features);
		assertEquals(0, plugins.size());
	}

	@Test
	public void testSharedFeaturesContainingPlugins1() throws Exception {
		Collection<String> features = new HashSet<String>();
		features.add("com.instantiations.common.ui");
		Collection<String> plugins = getManager().getFeaturesContainingPlugins(features);
		assertEquals(1, plugins.size());
		assertTrue(plugins.contains("com.instantiations.eclipse.shared"));
	}

	@Test
	public void testSharedFeaturesContainingPlugins2() throws Exception {
		Collection<String> features = new HashSet<String>();
		features.add("com.instantiations.common.ui");
		features.add("com.instantiations.common.core");
		Collection<String> plugins = getManager().getFeaturesContainingPlugins(features);
		assertEquals(1, plugins.size());
		assertTrue(plugins.contains("com.instantiations.eclipse.shared"));
	}

	@Test
	public void testSharedFeaturesContainingPlugins3() throws Exception {
		Collection<String> features = new HashSet<String>();
		features.add("com.instantiations.common.ui");
		features.add("does.not.exist");
		features.add("com.instantiations.common.core");
		Collection<String> plugins = getManager().getFeaturesContainingPlugins(features);
		assertEquals(1, plugins.size());
		assertTrue(plugins.contains("com.instantiations.eclipse.shared"));
	}

	@Test
	public void testSharedAndCoverageFeaturesContainingPlugins() throws Exception {
		Collection<String> features = new HashSet<String>();
		features.add("com.instantiations.common.ui");
		features.add("does.not.exist");
		features.add("com.instantiations.assist.eclipse.coverage.ui");
		features.add("com.instantiations.common.core");
		Collection<String> plugins = getManager().getFeaturesContainingPlugins(features);
		assertEquals(2, plugins.size());
		assertTrue(plugins.contains("com.instantiations.eclipse.shared"));
		assertTrue(plugins.contains("com.instantiations.assist.eclipse.coverage"));
	}
}
