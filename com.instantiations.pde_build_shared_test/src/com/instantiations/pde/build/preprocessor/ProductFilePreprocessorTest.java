package com.instantiations.pde.build.preprocessor;

import java.io.File;

import org.junit.Test;

import com.instantiations.pde.build.util.BuildProperties;
import com.instantiations.pde.build.util.Version;

public class ProductFilePreprocessorTest extends LineBasedPreprocessorTest
{
	@Test
	public void testProcessFile() throws Exception {
		BuildProperties prop = new BuildProperties();
		prop.set("preprocessor.ignore.variables", "none");
		processFiles(new ProductFilePreprocessor(new Version("3.4"), "x.y.z.aaa", prop), new File("testdata/preprocessor/product"));
	}
}