package com.instantiations.pde.build.preprocessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import test.util.FileUtil;

import com.instantiations.pde.build.util.Version;

/**
 * Common methods shared by {@link FeatureXmlPreprocessorTest} and
 * {@link BundleManifestPreprocessorTest}
 */
public class LineBasedPreprocessorTest
{
	protected void processFiles(LineBasedPreprocessor processor, File dataDir) throws IOException {
		assertTrue("Cannot find test data: " + dataDir.getCanonicalPath(), dataDir.exists());
		File originalDir = new File(dataDir, "original");
		File expectedDir = new File(dataDir, "expected");
		String[] allNames = originalDir.list();
		for (int i = 0; i < allNames.length; i++) {
			String name = allNames[i];
			if (name.equalsIgnoreCase("cvs") || name.equalsIgnoreCase(".svn"))
				continue;
			processFile(processor, name, originalDir, expectedDir);
		}
	}

	protected void processFiles(LineBasedPreprocessor processor, File dataDir, Version eclipseTargetVersion)
		throws IOException
	{
		assertTrue("Cannot find test data: " + dataDir.getCanonicalPath(), dataDir.exists());
		File originalDir = new File(dataDir, "original");
		File expectedDir = new File(dataDir, "expected/" + eclipseTargetVersion);
		String[] allNames = originalDir.list();
		for (int i = 0; i < allNames.length; i++) {
			String name = allNames[i];
			if (name.equalsIgnoreCase("cvs") || name.equalsIgnoreCase(".svn"))
				continue;
			processFile(processor, name, originalDir, expectedDir);
		}
	}

	protected void processFile(LineBasedPreprocessor processor, String name, File originalDir, File expectedDir) throws IOException {
			File originalFile = new File(originalDir, name);
			String actual = processFile(processor, originalFile);
			String expected = FileUtil.readFile(new File(expectedDir, name));
			// result of null indicates no change
			if (actual == null)
				actual = FileUtil.readFile(originalFile);
			assertEquals("Unexpected result for " + name, expected, actual);
	}

	protected String processFile(LineBasedPreprocessor processor, File originalFile) throws IOException {
		return processor.process0(originalFile);
	}
}
