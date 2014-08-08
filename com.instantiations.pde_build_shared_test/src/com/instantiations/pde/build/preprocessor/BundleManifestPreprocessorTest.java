package com.instantiations.pde.build.preprocessor;

import java.io.File;

import org.junit.Test;

import com.instantiations.pde.build.util.Version;

public class BundleManifestPreprocessorTest extends LineBasedPreprocessorTest
{
	@Test
	public void testProcessFileFor34() throws Exception {
		processFiles(new BundleManifestPreprocessor(Version.V_3_4, "x.y.z.aaa"), new File("testdata/preprocessor/manifest"), Version.V_3_4);
	}
	@Test
	public void testProcessFileFor33() throws Exception {
		processFiles(new BundleManifestPreprocessor(Version.V_3_3, "x.y.z.aaa"), new File("testdata/preprocessor/manifest"), Version.V_3_3);
	}
	@Test
	public void testProcessFileFor30() throws Exception {
		processFiles(new BundleManifestPreprocessor(Version.V_3_0, "x.y.z.aaa"), new File("testdata/preprocessor/manifest"), Version.V_3_0);
	}
	@Test
	public void testProcessFileFor21() throws Exception {
		processFiles(new BundleManifestPreprocessor(Version.V_2_1, "x.y.z.aaa"), new File("testdata/preprocessor/manifest"), Version.V_2_1);
	}
}