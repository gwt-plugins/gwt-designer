package com.instantiations.pde.build.preprocessor;

import java.io.File;

import org.junit.Test;

import com.instantiations.pde.build.util.BuildProperties;
import com.instantiations.pde.build.util.Version;

public class FeatureXmlPreprocessorTest extends LineBasedPreprocessorTest
{
	@Test
	public void testProcessFile() throws Exception {
		BuildProperties prop = new BuildProperties();
		prop.set("build.year", "2009");
		prop.set("preprocessor.ignore.variables", "none");
		processFiles(new FeatureXmlPreprocessor(Version.V_3_4, "x.y.z.aaa", prop), new File(
			"testdata/preprocessor/feature"), Version.V_3_4);
		processFiles(new FeatureXmlPreprocessor(Version.V_3_1, "x.y.z.aaa", prop), new File(
			"testdata/preprocessor/feature"), Version.V_3_1);
	}
}