package com.instantiations.pde.build.preprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.junit.Test;

import test.util.FileUtil;

import com.instantiations.pde.build.util.BuildProperties;
import com.instantiations.pde.build.util.OemVersion;
import com.instantiations.pde.build.util.Version;
import com.objfac.prebop.PreprocessorError;

import static org.junit.Assert.*;

public class PluginProjectPreprocessorTest
{
	private static final File tempDir = new File(System.getProperty("java.io.tmpdir"),
		"test/" + PluginProjectPreprocessorTest.class.getName());

	@Test
	public void testPluginProjectPreprocessor() throws Exception {
		FileUtil.deleteFiles(tempDir);
		File dataDir = new File("testdata/preprocessor/plugin");
		File originalDir = new File(dataDir, "original");
		File expectedDir = new File(dataDir, "expected");

		TreeSet<String> names = new TreeSet<String>(Arrays.asList(originalDir.list()));
		names.remove(".svn");
		List<String> versionStrings = new ArrayList<String>(Arrays.asList(expectedDir.list()));
		versionStrings.remove(".svn");
		TreeSet<String> oemNames = new TreeSet<String>();
		for (Iterator<String> verStrIter = versionStrings.iterator(); verStrIter.hasNext();) {
			String verStr = verStrIter.next();
			if (!Character.isDigit(verStr.charAt(0))) {
				verStrIter.remove();
				oemNames.add(verStr);
			}
		}
		testPluginProjectPreprocessor(originalDir, names, null, versionStrings);
		for (String oemName : oemNames) {
			File oemDir = new File(expectedDir, oemName);
			versionStrings = new ArrayList<String>(Arrays.asList(oemDir.list()));
			versionStrings.remove(".svn");
			testPluginProjectPreprocessor(originalDir, names, oemName, versionStrings);
		}
	}

	private void testPluginProjectPreprocessor(File originalDir, TreeSet<String> names, String oemName, List<String> versionStrings)
		throws IOException, PreprocessorError
	{
		BuildProperties prop = new BuildProperties();
		prop.set("key.1", "this is value #1");
		prop.set("key.2", "another value");
		prop.set("very.log.key", "v3");
		prop.set("preprocessor.ignore.variables", "none");
		TreeSet<Version> versions = new TreeSet<Version>();
		for (String string : versionStrings)
			versions.add(new Version(string));

		for (String projName : names) {
			for (Version eclipseTargetVersion : versions) {
				File projDir = FileUtil.copyFiles(new File(originalDir, projName), new File(tempDir, oemName + "/"
					+ eclipseTargetVersion + "/" + projName));
				PluginProjectPreprocessor processor = new PluginProjectPreprocessor(new OemVersion(oemName, eclipseTargetVersion), "2.6.7.abc",
					prop);
				processor.processManifest(projDir);
				processor.processPluginProperties(projDir);
				assertEquals("JavaSE-1.6", processor.getExecutionEnvironment());
				processor.processBuildProperties(projDir);
				File srcDir = new File(projDir, "src");
				if (srcDir.exists())
					processor.processSource(srcDir);
				File expectedDir = new File("testdata/preprocessor/plugin/expected/"
					+ (oemName != null ? oemName + "/" : "") + eclipseTargetVersion + "/" + projName);
				compareFiles(expectedDir, projDir);
			}
		}
	}

	private void compareFiles(File expected, File actual) throws IOException {
		if (expected.getName().equals(".svn"))
			return;
		if (expected.isDirectory()) {
			if (!actual.isDirectory())
				throw new RuntimeException("Expected a directory: " + actual.getCanonicalPath());
			TreeSet<String> expectedNames = new TreeSet<String>(Arrays.asList(expected.list()));
			for (String name : expectedNames)
				compareFiles(new File(expected, name), new File(actual, name));
			TreeSet<String> actualNames = new TreeSet<String>(Arrays.asList(actual.list()));
			for (String name : actualNames)
				if (!expectedNames.contains(name))
					throw new RuntimeException("Did not expected: " + actual.getCanonicalPath() + "/" + name);
		}
		else {
			if (!actual.isFile())
				throw new RuntimeException("Expected a file: " + actual.getCanonicalPath());
			assertEquals("Compare file content: " + actual.getCanonicalPath(), readFile(expected), readFile(actual));
		}
	}

	protected String readFile(File file) throws IOException {
		StringWriter stringWriter = new StringWriter(4096);
		FileReader fileReader = new FileReader(file);
		try {
			PrintWriter writer = new PrintWriter(stringWriter);
			LineNumberReader reader = new LineNumberReader(new BufferedReader(fileReader));
			while (true) {
				String line = reader.readLine();
				if (line == null)
					break;
				// Skip the comment lines at the beginning of the build.properties file
				if (line.startsWith("#") && file.getName().equals("build.properties"))
					continue;
				writer.println(line);
			}
		}
		finally {
			fileReader.close();
		}
		return stringWriter.toString();
	}
}