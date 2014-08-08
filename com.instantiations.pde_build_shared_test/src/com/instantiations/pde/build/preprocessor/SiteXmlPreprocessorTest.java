package com.instantiations.pde.build.preprocessor;

import java.io.File;

import org.junit.Test;

import com.instantiations.pde.build.util.BuildProperties;
import com.instantiations.pde.build.util.Version;

public class SiteXmlPreprocessorTest extends LineBasedPreprocessorTest
{
	@Test
	public void testProcessFile() throws Exception {
		BuildProperties prop = new BuildProperties();
		prop.set("preprocessor.ignore.variables", "none");
		SiteXmlPreprocessor processor = new SiteXmlPreprocessor(Version.V_3_4, prop);
		processor.setVersion("com.qualityeclipse.book", "x.y.z.aaa");
		processFiles(processor, new File("testdata/preprocessor/site"));
	}

	@Test
	public void testProcessFile2() throws Exception {
		BuildProperties prop = new BuildProperties();
		prop.set("preprocessor.ignore.variables", "none");
		SiteXmlPreprocessor processor = new SiteXmlPreprocessor(Version.V_3_4, prop);
		processor.setVersion("com.qualityeclipse.book", "1.2.3.456");
		processor.setVersion("com.qualityeclipse.book2", "3.8.0.200809110900");
		processFiles(processor, new File("testdata/preprocessor/site2"));
	}
}