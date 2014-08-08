/**
 * 
 */
package com.instantiations.pde.build.util;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import test.util.CopyFileFilter;
import test.util.FileUtil;

/**
 * @author markr
 *
 */
public class RcpBuildUtilTest {
	private static final CopyFileFilter ALT_FILE_FILTER = new CopyFileFilter() {
		public boolean shouldCopyFile(File file) {
			Boolean ret = file.getName().contains("-3.5");
			return ret;
		}
	};
	private static final CopyFileFilter NORMAL_FILE_FILTER = new CopyFileFilter() {
		public boolean shouldCopyFile(File file) {
			Boolean ret = !file.getName().contains("-3.5");
			return ret;
		}
	};

	private static File tmpDir;
	private static BuildProperties prop;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		File systemTmp = null;
		String systemTempName = System.getProperty("test.tmp.dir");
		if (systemTempName != null) {
			systemTmp = new File(systemTempName, "tests"); 
		}
		else {
			systemTmp = new File(System.getProperty("java.io.tmpdir"), "tests");
		}
		
		tmpDir = new File(systemTmp, RcpBuildUtilTest.class.getName());
		if (tmpDir.exists()) {
			FileUtil.deleteFiles(tmpDir);
		}
		System.out.println("creating directory " + tmpDir);
		tmpDir.mkdirs();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		prop = new BuildProperties();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		prop = null;
	}

	/**
	 * Test method for {@link com.instantiations.pde.build.util.RcpBuildUtil#locateProductFile(com.instantiations.pde.build.util.BuildProperties, java.io.File, com.instantiations.pde.build.util.Version)}.
	 * @throws IOException 
	 */
	@Test
	public void testLocateProductFile() throws IOException {
		File testDir = new File(tmpDir, "testLocateProductFile");
		File copyDest = new File(testDir, "com.foo.plugin");
		copyDest.mkdirs();
		FileUtil.copyFiles(new File("testdata/product"), copyDest, NORMAL_FILE_FILTER);
		prop.set("product.rcp.file", "plugins/com.foo.plugin/ProductTest");
		File productFile = RcpBuildUtil.locateProductFile(prop, testDir, Version.V_3_5);
		Assert.assertEquals("ProductTest.product", productFile.getName());
		FileUtil.copyFiles(new File("testdata/product"), copyDest, ALT_FILE_FILTER);
		prop.set("product.rcp.file", "plugins/com.foo.plugin/ProductTest");
		productFile = RcpBuildUtil.locateProductFile(prop, testDir, Version.V_3_5);
		Assert.assertEquals("ProductTest-3.5.product", productFile.getName());
	}

	/**
	 * Test method for {@link com.instantiations.pde.build.util.RcpBuildUtil#productFile(com.instantiations.pde.build.util.BuildProperties, java.io.File, java.lang.Boolean)}.
	 * @throws IOException 
	 */
	@Test
	public void testProductFile() throws IOException {
		File testDir = new File(tmpDir, "testProductFile");
		testDir.mkdirs();
		FileUtil.copyFiles(new File("testdata/product"), new File(testDir, "com.foo.plugin"));
		prop.set("product.rcp.file", "plugins/com.foo.plugin/ProductTest");
		File productFile = RcpBuildUtil.productFile(prop, testDir, true);
		Assert.assertNotNull(productFile);
		Assert.assertEquals("ProductTest.product", productFile.getName());
		Assert.assertFalse("plugins should not be in the name", productFile.toString().contains("plugins"));
		productFile = RcpBuildUtil.productFile(prop, testDir, false);
		Assert.assertNotNull(productFile);
		Assert.assertEquals("ProductTest.product", productFile.getName());
		Assert.assertTrue("plugins should be in the name", productFile.toString().contains("plugins"));
		prop.set("product.rcp.file", "features/com.foo.feature_feature/ProductTest");
		productFile = RcpBuildUtil.productFile(prop, testDir, true);
		Assert.assertNotNull(productFile);
		Assert.assertEquals("ProductTest.product", productFile.getName());
		Assert.assertFalse("features should not be in the name", productFile.toString().contains("features"));
		Assert.assertTrue("_feature should be in the name", productFile.toString().contains("_feature"));
		Assert.assertEquals("com.foo.feature_feature", productFile.getParentFile().getName());
		productFile = RcpBuildUtil.productFile(prop, testDir, false);
		Assert.assertNotNull(productFile);
		Assert.assertEquals("ProductTest.product", productFile.getName());
		Assert.assertTrue("feature should be in the name", productFile.toString().contains("features"));
		Assert.assertFalse("_feature should be in the name", productFile.toString().contains("_feature"));
		Assert.assertEquals("com.foo.feature", productFile.getParentFile().getName());
	}

	/**
	 * Test method for {@link com.instantiations.pde.build.util.RcpBuildUtil#alternateProductFile(com.instantiations.pde.build.util.BuildProperties, java.io.File, com.instantiations.pde.build.util.Version, java.lang.Boolean)}.
	 * @throws IOException 
	 */
	@Test
	public void testAlternateProductFile() throws IOException {
		File testDir = new File(tmpDir, "testAlternateProductFile");
		testDir.mkdirs();
		FileUtil.copyFiles(new File("testdata/product"), new File(testDir, "com.foo.plugin"));
		prop.set("product.rcp.file", "plugins/com.foo.plugin/ProductTest");
		File productFile = RcpBuildUtil.alternateProductFile(prop, testDir, Version.V_3_5, true);
		Assert.assertNotNull(productFile);
		Assert.assertEquals("ProductTest-3.5.product", productFile.getName());
		Assert.assertFalse("plugins should not be in the name", productFile.toString().contains("plugins"));
		productFile = RcpBuildUtil.alternateProductFile(prop, testDir, Version.V_3_5, false);
		Assert.assertNotNull(productFile);
		Assert.assertEquals("ProductTest-3.5.product", productFile.getName());
		Assert.assertTrue("plugins should be in the name", productFile.toString().contains("plugins"));
		prop.set("product.rcp.file", "features/com.foo.feature_feature/ProductTest");
		productFile = RcpBuildUtil.alternateProductFile(prop, testDir, Version.V_3_5, true);
		Assert.assertNotNull(productFile);
		Assert.assertEquals("ProductTest-3.5.product", productFile.getName());
		Assert.assertFalse("features should not be in the name", productFile.toString().contains("features"));
		Assert.assertTrue("_feature should be in the name", productFile.toString().contains("_feature"));
		Assert.assertEquals("com.foo.feature_feature", productFile.getParentFile().getName());
		productFile = RcpBuildUtil.alternateProductFile(prop, testDir, Version.V_3_5, false);
		Assert.assertNotNull(productFile);
		Assert.assertEquals("ProductTest-3.5.product", productFile.getName());
		Assert.assertTrue("feature should be in the name", productFile.toString().contains("features"));
		Assert.assertFalse("_feature should be in the name", productFile.toString().contains("_feature"));
		Assert.assertEquals("com.foo.feature", productFile.getParentFile().getName());
	}

}
