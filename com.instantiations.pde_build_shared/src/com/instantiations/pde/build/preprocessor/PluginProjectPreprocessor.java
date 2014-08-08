package com.instantiations.pde.build.preprocessor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import com.instantiations.pde.build.util.BuildProperties;
import com.instantiations.pde.build.util.OemVersion;
import com.instantiations.pde.build.util.Version;
import com.objfac.prebop.PreprocessorError;

/**
 * Preprocess plugin source projects from one version of Eclipse to another.
 */
public class PluginProjectPreprocessor
{
	private final String pluginVersion;

	private boolean manifestProcessed = false;
	private String executionEnvironment = null;
	private final BuildProperties prop;

	private final OemVersion targetVersion;

	/**
	 * Construct a new instance to preprocess a plugin source project
	 * @param targetVersion TODO
	 * @param pluginVersion the version used to replace "0.0.0" and "0.0.0.qualifier"
	 * @param prop TODO
	 */
	public PluginProjectPreprocessor(OemVersion targetVersion, String pluginVersion, BuildProperties prop) {
		this.targetVersion = targetVersion;
		this.pluginVersion = pluginVersion;
		this.prop = prop;
	}

	/**
	 * Answer the detected execution environment (e.g. "J2SE-1.5") or null if not defined
	 */
	public String getExecutionEnvironment() {
		if (!manifestProcessed)
			throw new IllegalStateException("must call processManifest first");
		return executionEnvironment;
	}

	/**
	 * Process the MANIFEST.MF and plugin.xml or fragment.xml files
	 * 
	 * @param projDir the plugin project directory
	 */
	public void processManifest(File projDir) throws IOException, PreprocessorError {

		// Process the bundle manifest.mf file
		File manifestFile = new File(projDir, "META-INF/MANIFEST.MF");
		BackportBundleManifestPreprocessor manifestPreprocessor = new BackportBundleManifestPreprocessor(targetVersion.getVersion(),
			pluginVersion);
		manifestPreprocessor.process(manifestFile);
		executionEnvironment = manifestPreprocessor.getExecutionEnvironment();
		manifestProcessed = true;

		// Process the plugin.xml or fragment.xml file
		File xmlFile = new File(projDir,
				manifestPreprocessor.isFragment() ? "fragment.xml"
						: "plugin.xml");
		PluginXmlPreprocessor pluginXmlPreprocessor = null;
		if (xmlFile.exists()) {
			pluginXmlPreprocessor = new PluginXmlPreprocessor(targetVersion.getVersion(), prop);
			pluginXmlPreprocessor.process(xmlFile);
			new OldPreprocessor(targetVersion.getVersion()).process(xmlFile);
			new PrebopPreprocessor(targetVersion.getOemName(), targetVersion.getVersion()).preprocessFile(xmlFile);
		}

		// If backporting to Eclipse 3.1, then merge META-INF/MANIFEST.MF into plugin.xml or fragment.xml.
		// In addition, if an Eclipse 3.3 or earlier plugin declares one or more Ant tasks,
		// then merge META-INF/MANIFEST.MF into plugin.xml or Eclipse will not see the Ant tasks.
		if (targetVersion.getVersion().compareTo(Version.V_3_1) <= 0
			|| (pluginXmlPreprocessor != null && pluginXmlPreprocessor.declaresAntTasks() && targetVersion.getVersion()
				.compareTo(Version.V_3_3) <= 0)) {
			new BundleManifestToXmlPreprocessor().process(manifestFile, xmlFile, targetVersion.getVersion());
			ensureBuildPropertyListHasElement(projDir, "bin.includes", xmlFile.getName());
			manifestFile.delete();
		}
	}

	/**
	 * Process the plugin.properties file
	 * 
	 * @param projDir the plugin project directory
	 */
	public void processPluginProperties(File projDir) throws IOException {
		File pluginPropFile = new File(projDir, "plugin.properties");
		if (pluginPropFile.exists()) {
			new PropertiesFilePreprocessor(targetVersion, prop).process(pluginPropFile);
		}
	}

	/**
	 * Process the fragment.properties file
	 * 
	 * @param projDir the plugin project directory
	 */
	public void processFragmentProperties(File projDir) throws IOException {
		File fragmentPropFile = new File(projDir, "fragment.properties");
		if (fragmentPropFile.exists()) {
			new PropertiesFilePreprocessor(targetVersion, prop).process(fragmentPropFile);
		}
	}

	/**
	 * Modify the build.properties if necessary 
	 * to include the specified value in the specified property list
	 * @param key the key
	 * @param listElem the element to appear in the comma separated value associated with the key
	 */
	private void ensureBuildPropertyListHasElement(File projDir, String key, String listElem) throws IOException {
		Properties buildProp = readBuildProperties(projDir);
		
		// Ensure that "bin.includes" contains "plugin.xml"
		String value = buildProp.getProperty(key);
		if (value == null)
			value = "";
		String[] values = value.split(",");
		boolean found = false;
		for (String eachValue : values) {
			if (eachValue.equals(listElem)) {
				found = true;
				break;
			}
		}
		if (!found)
			buildProp.setProperty(key, value + "," + listElem);

		writeBuildProperties(projDir, buildProp);
	}

	/**
	 * Adjust the build.properties for older versions of Eclipse.
	 */
	public void processBuildProperties(File projDir) throws IOException {

		// This adjustment only applies to Eclipse 3.1 and earlier
		if (targetVersion.getVersion().compareTo(Version.V_3_1) > 0)
			return;

		// Read the build.properties
		Properties buildProp = readBuildProperties(projDir);
		if (buildProp.isEmpty())
			return;

		String key;
		String value;

		// Replace jar'd plugin with directory based plugin
		key = "source..";
		value = buildProp.getProperty(key, null);
		if (value != null) {

			// Compile source to 'bin.jar'
			buildProp.remove("source..");
			buildProp.remove("output..");
			buildProp.setProperty("source.bin.jar", value);
			buildProp.setProperty("output.bin.jar", "bin/");
			buildProp.setProperty("jars.compile.order", "bin.jar");

			// Replace '.' with 'bin.jar'
			boolean replaced = false;
			key = "bin.includes";
			value = buildProp.getProperty(key, null);
			if (value == null)
				throw new RuntimeException("Expected " + key + " in " + projDir.getCanonicalPath() + "/build.properties");
			String[] values = value.split(",");
			StringBuffer buf = new StringBuffer(value.length() + 20);
			for (int i = 0; i < values.length; i++) {
				value = values[i].trim();
				if (value.length() == 0)
					continue;
				if (value.equals(".")) {
					value = "bin.jar";
					replaced = true;
				}
				if (buf.length() > 0)
					buf.append(",");
				buf.append(value);
			}
			if (!replaced) {
				if (buf.length() > 0)
					buf.append(",");
				buf.append("bin.jar");
			}
			buildProp.setProperty(key, buf.toString());
		}

		// Adjust the jars.extra.classpath
		key = "jars.extra.classpath";
		value = buildProp.getProperty(key, null);
		if (value != null) {
			String[] values = value.split(",");
			StringBuffer buf = new StringBuffer(value.length() + 20);
			for (int i = 0; i < values.length; i++) {
				value = values[i].trim();
				if (value.length() == 0)
					continue;
				if (!value.startsWith("file:"))
					value = "file:" + new File(projDir, value).getCanonicalPath().replace('\\', '/');
				if (buf.length() > 0)
					buf.append(",");
				buf.append(value);
			}
			buildProp.setProperty(key, buf.toString());
		}

		// Save the adjusted build.properties file
		writeBuildProperties(projDir, buildProp);
	}

	/**
	 * Read the "build.properties" file
	 * @param projDir the project directory containing the build.properties file 
	 * @return the properties read from the build.properties file 
	 * 		or an empty properties object if the build.properties file does not exist.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private Properties readBuildProperties(File projDir) throws FileNotFoundException, IOException {
		Properties buildProp = new Properties();

		File file = new File(projDir, "build.properties");
		if (!file.exists())
			return buildProp;
		
		FileReader in = new FileReader(file);
		try {
			buildProp.load(new BufferedReader(in));
		}
		finally {
			in.close();
		}
		return buildProp;
	}

	/**
	 * Write the properties to the "build.properties" file
	 * @param projDir the project directory into which the build.properties file will be written
	 * @param buildProp the build properties to be written
	 */
	private void writeBuildProperties(File projDir, Properties buildProp) throws IOException {
		File file = new File(projDir, "build.properties");
		file.delete();
		FileWriter out = new FileWriter(file);
		try {
			buildProp.store(new BufferedWriter(out), "Modified for " + targetVersion.getVersion());
		}
		finally {
			out.close();
		}
	}
	
	/**
	 * Process the about.mappings file if it exists
	 */
	public void processAboutMappings(File projDir) throws IOException {
		File aboutMappingsFile = new File(projDir, "about.mappings");
		if (!aboutMappingsFile.exists())
			return;
		VariableReplacementPreprocessor varProc = new VariableReplacementPreprocessor(
				targetVersion.getVersion(), prop);
		varProc.process(aboutMappingsFile);
	}

	/**
	 * Process the about.properties file if it exists
	 */
	public void processAboutProperties(File projDir) throws PreprocessorError, IOException {
		File aboutPropertiesFile = new File(projDir, "about.properties");
		if (!aboutPropertiesFile.exists())
			return;
		VariableReplacementPreprocessor varProc = new VariableReplacementPreprocessor(
				targetVersion.getVersion(), prop);
		varProc.process(aboutPropertiesFile);
		new PropertiesFilePreprocessor(targetVersion, prop).process(aboutPropertiesFile);
	}

	/**
	 * Process the about.html file if it exists
	 */
	public void processAboutHtml(File projDir) throws PreprocessorError, IOException {
		File aboutHtmlFile = new File(projDir, "about.html");
		if (!aboutHtmlFile.exists())
			return;
		VariableReplacementPreprocessor varProc = new VariableReplacementPreprocessor(
				targetVersion.getVersion(), prop);
		varProc.process(aboutHtmlFile);
		new PrebopPreprocessor(targetVersion.getOemName(), targetVersion.getVersion()).preprocessFile(aboutHtmlFile);
	}

	/**
	 * Process the plugin java source directory
	 * 
	 * @param srcDir the directory containing *.java files
	 */
	public void processSource(File srcDir) throws PreprocessorError, IOException {
		JavaSourceVariableReplacementPreprocessor varProc = new JavaSourceVariableReplacementPreprocessor(targetVersion.getVersion(), prop);
		varProc.processAll(srcDir);
		OldPreprocessor oldPreprocessor = new OldPreprocessor(targetVersion.getVersion());
		oldPreprocessor.processAll(srcDir);
		new PrebopPreprocessor(targetVersion.getOemName(), targetVersion.getVersion()).preprocess(srcDir);
	}
}
