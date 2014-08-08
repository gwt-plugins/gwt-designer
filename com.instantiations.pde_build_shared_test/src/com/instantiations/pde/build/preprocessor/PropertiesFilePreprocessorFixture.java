package com.instantiations.pde.build.preprocessor;

import java.io.File;
import java.io.IOException;

import test.util.FileUtil;

import com.instantiations.pde.build.util.BuildProperties;
import com.instantiations.pde.build.util.OemVersion;
import com.instantiations.pde.build.util.Version;

/**
 * A fixture for testing {@link PropertiesFilePreprocessor}
 */
public class PropertiesFilePreprocessorFixture
{
	private static final File dataDir = new File("testdata/preprocessor/properties");
	private static final File originalFile = new File(dataDir, "original/plugin.properties");

	private final String oemName;
	private final Version targetVersion;
	private final PropertiesFilePreprocessor processor;

	public PropertiesFilePreprocessorFixture(String oemName, Version targetVersion) {
		this.oemName = oemName;
		this.targetVersion = targetVersion;
		BuildProperties prop = null;
		try {
			prop = new BuildProperties();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		prop.set("preprocessor.ignore.variables", "none");
		this.processor = new PropertiesFilePreprocessor(new OemVersion(oemName, targetVersion), prop);
	}

	public String process() throws IOException {
		File propFile = originalFile.getCanonicalFile();
		String result = processor.process0(propFile);
		if (result == null)
			result = FileUtil.readFile(propFile);
		return result;
	}

	public String getExpected() throws IOException {
		return readFile(oemName, targetVersion);
	}

	private String readFile(String oemName, Version version) throws IOException {
		return FileUtil.readFile(new File(dataDir.getCanonicalFile(), "expected/plugin-" + oemName + "-" + version + ".properties"));
	}
}
