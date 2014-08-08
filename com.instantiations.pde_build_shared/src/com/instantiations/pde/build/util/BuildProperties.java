package com.instantiations.pde.build.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.tools.ant.BuildException;

/**
 * A collection of all properties for building a product. This class reads the following
 * files where properties in files earlier in the list take precedence over properties in
 * files later in the list:
 * 
 * <pre>
 * ./build-settings/product.properties
 * </pre>
 */
public class BuildProperties
{
	private static final String BUILD_BRANCH_KEY = "build.branch";

	/**
	 * The build properties accessed via {@link #get(String)}
	 */
	private final Properties properties;

	/**
	 * The build property files that were read
	 */
	private final ArrayList<File> filesRead = new ArrayList<File>(5);

	/**
	 * The names of subproducts whose product.properties were read
	 */
	private final Collection<String> subproductsRead = new HashSet<String>();

	/**
	 * The loader used to access product.properties files from other builds or
	 * <code>null</code> if those other product.properties files cannot be accessed.
	 */
	private ProductPropertiesLoader productPropertiesLoader = null;

	public BuildProperties() throws IOException {
		properties = new Properties(System.getProperties());

		String home = properties.getProperty("BuildCommonHome");
		if (home == null) {
			home = new File("../com.instantiations.pde_build_shared").getCanonicalPath();
			properties.setProperty("BuildCommonHome", home);
		}
		
		String data = properties.getProperty("BuildCommonData");
		if (data == null) {
			data = new File("../com.instantiations.pde_build_data_instantiations").getCanonicalPath();
			properties.setProperty("BuildCommonData", data);

		}
		System.out.println("BuildCommonHome is " + home);
		System.out.println("BuildCommonData is " + data);
		
		File homeFile = new File(home);
		File dataFile = new File(data);
		
		if (!homeFile.exists()) {
			throw new BuildPropertiesException("could not find " + homeFile.getCanonicalPath() + 
												".\n  make sure BuildCommonHome points to com.instantiations.pde_build_shared \n" +
												" or you have project com.instantiations.pde_build_shared loaded in your workspace");
		}

		if (!dataFile.exists()) {
			throw new BuildPropertiesException("could not find " + dataFile.getCanonicalPath() + 
												".\n  make sure BuildCommonData points to com.instantiations.pde_build_data_instantiations on the build machine \n" +
												" or you have project com.instantiations.pde_build_data_instantiations loaded in your workspace");
		}

		String osName = properties.getProperty("os.name");
		String osArch = properties.getProperty("os.arch");
		if (osArch.equalsIgnoreCase("i386")) {
			osArch = "x86";
		}
		if (osName.startsWith("Windows")) {
			properties.setProperty("build.os", "win32");
			properties.setProperty("build.ws", "win32");
			properties.setProperty("build.arch", osArch);
		}
		else if (osName.startsWith("Linux")) {
			properties.setProperty("build.os", "linux");
			properties.setProperty("build.ws", "gtk");
			properties.setProperty("build.arch", osArch);
		}
		else if (osName.equals("Mac OS X")) {
			properties.setProperty("build.os", "macosx");
			properties.setProperty("build.ws", "macosx");
			properties.setProperty("build.arch", osArch);
		}
		else {
			throw new BuildPropertiesException(
				"\nModify " + getClass() + " initialization"
					+ "\nto dynamically define"
					+ "\n   build.os, build.ws, and build.arch"
					+ "\nfor"
					+ "\n   os.name = " + osName
					+ "\n   os.arch = " + properties.getProperty("os.arch")
					+ "\nand add eclipse-sdk* key/value pairs to "
					+ "\n   " + home + "/build-settings-global/eclipse-platform.properties"
					+ "\nStack Trace");
		}
		properties.setProperty("build.num", new SimpleDateFormat("yyyyMMddHHmm").format(new Date()));
		properties.setProperty("build.date", new SimpleDateFormat("yyyy.MM.dd").format(new Date()));
		properties.setProperty("build.year", new SimpleDateFormat("yyyy").format(new Date()));
		properties.setProperty("build.month", new SimpleDateFormat("MM").format(new Date()));
		properties.setProperty("build.day", new SimpleDateFormat("dd").format(new Date()));
		//		System.out.println("*build.os = " + properties.getProperty("build.os")); 
		//		System.out.println("*build.ws = " + properties.getProperty("build.ws"));
		//		System.out.println("*build.arch = " + properties.getProperty("build.arch"));
		//		System.out.println("*build.num = " + properties.getProperty("build.num"));
	}

	public BuildProperties(BuildProperties defaults) {
		properties = new Properties(defaults.properties);
	}

	//====================================================================================
	// Accessors

	/**
	 * Set the property value
	 * 
	 * @param key the key (not <code>null</code>, not empty, no leading or trailing
	 *            spaces)
	 * @param value (not <code>null</code>)
	 */
	public void set(String key, String value) {
		if (key == null || key.length() == 0 || key.trim().length() != key.length())
			throw new BuildPropertiesException("Invalid key: " + key);
		if (value == null)
			throw new BuildPropertiesException("Invalid value: " + value + " for key: " + key);
		properties.setProperty(key, value);
	}

	/**
	 * Answer true if the receiver contains the specified key.
	 */
	public boolean isDefined(String key) {
		return properties.containsKey(key);
	}

	/**
	 * Answer true if the receiver contains the specified key and the value for that key
	 * is "true".
	 */
	public boolean isTrue(String key) {
		return properties.containsKey(key) && get(key).equals("true");
	}

	/**
	 * Answer the value for the specified key as a list. The associated string value is
	 * split into elements at each comma and leading and trailing whitespace is trimmed
	 * off each element.
	 * 
	 * @param key the key
	 * @return a list of elements
	 */
	public List<String> getList(String key) {
		return getList(key, ',');
	}

	/**
	 * Answer the value for the specified key as a list. The associated string value is
	 * split into elements at each specified separator character and leading and trailing
	 * whitespace is trimmed off each element.
	 * 
	 * @param key the key
	 * @return a list of elements
	 */
	public List<String> getList(String key, char separator) {
		String value = get(key);
		List<String> result = new ArrayList<String>(4);
		if (value.length() == 0)
			return result;
		int start = 0;
		while (true) {
			int index = value.indexOf(separator, start);
			if (index == -1)
				break;
			result.add(value.substring(start, index).trim());
			start = index + 1;
		}
		result.add(value.substring(start).trim());
		return result;
	}
	
	/**
	 * Answer the value associated with the specified key with any embedded property
	 * references (e.g. "${key}") replaced by the associated value
	 * and append a trailing slash '/' if the string does not end with a slash '/'.
	 * 
	 * @param key the key
	 * @return the value (not <code>null</code>)
	 * @throws BuildException if the key or any embedded key does not have a value.
	 */
	public String getWithTrailingSlash(String key) {
		String value = get(key);
		return value.endsWith("/") ? value : value + "/";
	}

	/**
	 * Answer the value associated with the specified key with any embedded property
	 * references (e.g. "${key}") replaced by the associated value.
	 * 
	 * @param key the key
	 * @return the value (not <code>null</code>)
	 * @throws BuildException if the key or any embedded key does not have a value.
	 */
	public String get(String key) {
		return get0(key, 0, true);
	}

	private String get0(String key, int depth, boolean failOnUnresolved) {
		String value = getRaw0(key, failOnUnresolved);
		if (value == null)
			return null;
		try {
			int start = 0;
			while (true) {
				start = value.indexOf("${", start);
				if (start == -1)
					break;
				int end = value.indexOf('}', start);
				if (end == -1)
					throw new BuildPropertiesException("Failed to find closing '}' for embedded key");
				String subKey = value.substring(start + 2, end);
				if (depth > 5)
					throw new BuildPropertiesException("Possible recursive property declaration: depth=" + depth);
				String subValue = get0(subKey, depth + 1, failOnUnresolved);
				if (subValue != null)
					value = value.substring(0, start) + subValue + value.substring(end + 1);
				else
					start = end + 1;
			}
		}
		catch (BuildPropertiesException e) {
			StringWriter stringWriter = new StringWriter(300);
			PrintWriter writer = new PrintWriter(stringWriter);
			writer.print("Failed to resolve '");
			writer.print(key);
			writer.print("'");
			if (depth == 0) {
				writer.println();
				writer.println("Look for the cause in a nested exception, "
					+ "then make the adjustment in one of the following files:");
				writer.println("(Files earlier in the list take precedence over files later in the list)");
				for (File file : filesRead) {
					writer.print("  ");
					writer.print(file.getPath());
					if (!file.exists())
						writer.print(" [attempted to read but does not exist]");
					writer.println();
				}
				writer.print("Stack Trace:");
			}
			throw new BuildPropertiesException(stringWriter.toString(), e);
		}
		return value;
	}

	/**
	 * Answer the raw value associated with the specified key with any embedded property
	 * references (e.g. "${key}") unchanged.
	 * 
	 * @param key the key
	 * @return the value (not <code>null</code>)
	 * @throws BuildProperties if the key does not have a value.
	 */
	public String getRaw(String key) {
		return getRaw0(key, true);
	}

	private String getRaw0(String key, boolean failOnUnresolved) {

		// Support references to properties defined in product.properties of other subproducts
		// in the format <subproduct>/<key> as in WindowTesterRuntime/product.version
		int index = key.indexOf('/');
		if (index > 0)
			loadSubProductProperties(key.substring(0, index));

		String value = properties.getProperty(key);
		if (value == null) {

			// Support alternate key format so that ${build_num},
			// which can be used in a Groovy template, is equivalent to ${build.num}
			value = properties.getProperty(key.replace('_', '.'));

			if (value == null) {
				if (failOnUnresolved) {
					String message = getMissingKeyMessage(key);
					if (message == null)
						message = "Failed to find property '" + key + "'";
					throw new BuildPropertiesException(message);
				}
				else
					return null;
			}
		}
		return value.trim();
	}

	/**
	 * Set the loader used to access properties defined in the product.properties file of
	 * other builds.
	 * 
	 * @param productPropertiesLoader the loader or <code>null</code> if none
	 */
	public void setProductPropertiesLoader(ProductPropertiesLoader productPropertiesLoader) {
		this.productPropertiesLoader = productPropertiesLoader;
	}

	/**
	 * Read the subproduct's product.properties file into the receiver if they have not
	 * already been loaded. When loaded into the receiver, all keys will be prefixed with
	 * the subproduct name and a slash (e.g. WindowTesterRuntime/product.version)
	 * 
	 * @param subproductName the name of the subproduct whose product.properties file
	 *            should be loaded into the receiver
	 */
	private void loadSubProductProperties(String subproductName) {
		if (productPropertiesLoader == null || subproductsRead.contains(subproductName))
			return;
		subproductsRead.add(subproductName);
		Properties productProperties = productPropertiesLoader.getProductProperties(subproductName);
		for (Map.Entry<Object, Object> entry : productProperties.entrySet())
			properties.put(subproductName + "/" + entry.getKey(), entry.getValue());
	}

	/**
	 * Answer the message associated with a specific missing key
	 * 
	 * @param key the missing key
	 * @return the message or <code>null</code> if none
	 */
	private String getMissingKeyMessage(String key) {
		String path = "/" + getClass().getName().replace('.', '/') + "-" + key + ".txt";
		StringWriter writer = new StringWriter(1024);
		InputStream stream = getClass().getResourceAsStream(path);
		if (stream == null)
			return null;
		try {
			Reader reader = new BufferedReader(new InputStreamReader(stream));
			char[] buf = new char[1024];
			while (true) {
				int count = reader.read(buf);
				if (count == -1)
					break;
				writer.write(buf, 0, count);
			}
		}
		catch (IOException e) {
			System.err.println("Failed to read message file: " + path);
			return null;
		}
		finally {
			try {
				stream.close();
			}
			catch (IOException e) {
				System.err.println("Failed to close message file: " + path);
			}
		}
		return writer.toString();
	}

	/**
	 * Answer a collection of keys defined in the receiver
	 */
	public Collection<?> getKeys() {
		return properties.keySet();
	}

	/**
	 * Echo all property key/value pairs to System.out The values displayed are resolved
	 * to the extent that they can be at this point in time.
	 */
	public void echoAll() {
		TreeSet<String> keySet = new TreeSet<String>(properties.stringPropertyNames());
		for (String key : keySet) {
			String value = get0(key, 0, false);
			if (value == null)
				value = "${" + key + "}";
			System.out.println("  " + key + " = " + value);
		}
	}

	/**
	 * Find all properties with keys starting with "pde." and return a {@link Properties}
	 * object containing all those key/value pairs with the leading "pde." stripped off
	 * each key. All values in the resulting object are resolved.
	 * 
	 * @return the properties (not <code>null</code>)
	 */
	public Properties getPdeProperties() {
		Properties result = new Properties();
		for (String key : properties.stringPropertyNames())
			if (key.startsWith("pde."))
				result.setProperty(key.substring(4), get(key));
		return result;
	}

	//	public Properties getSiteProperties() {
	//		Properties result = new Properties();
	//		for (String key : properties.stringPropertyNames())
	//			if (key.startsWith("product.") || key.startsWith("build."))
	//				result.setProperty(key.replace('.', '_'), get(key));
	//		return result;
	//	}

	public Properties getInstallerProperties() {
		Properties result = new Properties();
		for (String key : properties.stringPropertyNames())
			if (key.startsWith("product.") || key.startsWith("build.") || key.startsWith("installer."))
				result.setProperty(key.replace('.', '_'), get(key));
		return result;
	}

	/**
	 * Generate a new map containing keys and values from the original map. Each line
	 * ending in each value is replaced with the specified sequence of characters.
	 * 
	 * @param original the original map
	 * @param lineEnd the new line ending
	 * @return the new map
	 */
	public static Map<String, String> newLineEnds(Map<String, String> original, String lineEnd) {
		Map<String, String> result = new HashMap<String, String>();
		for (Map.Entry<String, String> entry : original.entrySet()) {
			String value = entry.getValue();
			LineNumberReader reader = new LineNumberReader(new StringReader(value));
			StringBuffer buf = new StringBuffer(value.length() + 40);
			while (true) {
				String line;
				try {
					line = reader.readLine();
				}
				catch (IOException e) {
					// Should never happen because we are reading a string
					throw new RuntimeException(e);
				}
				if (line == null)
					break;
				if (buf.length() > 0)
					buf.append(lineEnd);
				buf.append(line);
			}
			result.put(entry.getKey(), buf.toString());
		}
		return result;
	}

	/**
	 * Convenience method that converts the value returned by {@link #get(String)} into a
	 * version.
	 * 
	 * @param key the key
	 * @return the value converted to a version
	 */
	public Version getVersion(String key) {
		return new Version(get(key));
	}

	/**
	 * Convenience method that converts the values returned by {@link #getList(String)}
	 * into versions.
	 * 
	 * @param key the key
	 * @return the values converted to versions (not <code>null</code>
	 */
	public List<OemVersion> getOemVersionList(String key) {
		List<String> values = getList(key);
		ArrayList<OemVersion> result = new ArrayList<OemVersion>(values.size());
		for (String eachValue : values)
			result.add(new OemVersion(eachValue));
		return result;
	}

	/**
	 * Search the build properties for the specified keys in the specified order. Answer
	 * the value for the first defined key or throw an exception if none found: keys in
	 * this order:
	 * <ol>
	 * <li>prefix + "." + get("build.os") + "." + get("build.ws") + "." +
	 * get("build.arch") + suffix</li>
	 * <li>prefix + "." + get("build.os") + "." + get("build.ws") + suffix</li>
	 * <li>prefix + "." + get("build.os") + suffix</li>
	 * <li>prefix + suffix</li>
	 * </ol>
	 */
	public String getPlatformSpecific(String prefix, String suffix) {
		String key;
		Collection<String> keys = new ArrayList<String>(4);

		key = prefix + "." + get("build.os") + "." + get("build.ws") + "." + get("build.arch") + suffix;
		if (isDefined(key))
			return get(key);
		keys.add(key);

		key = prefix + "." + get("build.os") + "." + get("build.ws") + suffix;
		if (isDefined(key))
			return get(key);
		keys.add(key);

		key = prefix + "." + get("build.os") + suffix;
		if (isDefined(key))
			return get(key);
		keys.add(key);

		key = prefix + suffix;
		if (isDefined(key))
			return get(key);
		keys.add(key);

		StringBuffer buf = new StringBuffer(300);
		for (Iterator<String> iter = keys.iterator(); iter.hasNext();) {
			if (buf.length() == 0)
				buf.append("Failed to resolve '");
			else
				buf.append("              and '");
			buf.append((String) iter.next());
			buf.append("'\n");
		}
		throw new BuildPropertiesException(buf.toString());
	}

	/**
	 * Replace all ${key} expressions with their value as defined in the receiver
	 * 
	 * @param text the original text
	 * @return the text with all ${key} replaced with their value
	 * @throws BuildPropertiesException if any key or embedded key does not have a value
	 */
	public String resolve(String text, Version eclipseTargetVersion) {
		return resolve(text, eclipseTargetVersion, new ArrayList<String>());
	}

	/**
	 * Replace all ${key} expressions with their value as defined in the receiver
	 * 
	 * @param text the original text
	 * @return the text with all ${key} replaced with their value
	 * @throws BuildPropertiesException if any key or embedded key does not have a value
	 */
	public String resolve(String text, Version eclipseTargetVersion, Collection<String> ignoredKeys) {
		String result = text;
		int end = 0;
		while (true) {
			int start = result.indexOf("${", end);
			if (start == -1)
				break;
			end = result.indexOf('}', start);
			if (end == -1)
				break;
			String key = result.substring(start + 2, end);
			if (ignoredKeys.contains(key))
				continue;
			if (!isDefined(key))
				key = key.replace('_', '.');
			String value;
			if (key.equals("eclipse.version"))
				value = eclipseTargetVersion.toString();
			else
				value = get(key);
			result = result.substring(0, start) + value + result.substring(end + 1);
			end = start;
		}
		return result;
	}

	//====================================================================================
	// Accessors for commonly referenced build elements

	public File getBuildCommonHome() {
		return new File(get("BuildCommonHome"));
	}

	public File getBuildCommonData() {
		return new File(get("BuildCommonData"));
	}

	public boolean isWin32() {
		return get("build.os").equals("win32");
	}

	public boolean isLinux() {
		return get("build.os").equals("linux");
	}

	public boolean shouldCreateSymLinks() {
		return isLinux() && isTrue("build.symlinks");
	}

	public boolean shouldSignJars() {
		return isTrue("product.master.sign.jars") && isTrue("product.sign.jars");
	}

	public boolean isLocalBuild() {
		return "local".equals(get("build.server"));
	}

	public boolean isHudsonBuild() {
		return "hudson".equals(get("build.server"));
	}

	public boolean shouldCommitBuildJob() {
		return isTrue("build.commit.build.job");
	}

	public boolean isHeadlessBuild() {
		return isTrue("build.headless");
	}

	// ===== Build

	public String getBuildNum() {
		return get("build.num");
	}

	public String getBuildQualifier(OemVersion targetVersion) {
		Version version = targetVersion.getVersion();
		if (version.compareTo(Version.V_3_4) < 0)
			return getBuildNum();
		return "r" + version.getMajor() + version.getMinor() + "x" + getBuildNum();
	}

	public String getBuildYear() {
		return get("build.year");
	}

	public File getBuildOut() throws IOException {
		return new File(get("build.out")).getCanonicalFile();
	}

	public File getBuildArtifacts() throws IOException {
		return new File(get("build.artifacts")).getCanonicalFile();
	}

	public File getBuildTemp() throws IOException {
		return new File(get("build.temp")).getCanonicalFile();
	}

	/**
	 * Answer the branch name if this is a branch build or <code>null</code> if this is a
	 * trunk build
	 */
	public String getBranchName() {
		if (!isDefined(BUILD_BRANCH_KEY))
			return null;
		final String branchName = get(BUILD_BRANCH_KEY);
		if (branchName.equals(""))
			throw new RuntimeException(BUILD_BRANCH_KEY + " cannot be an empty string");
		if (branchName.equalsIgnoreCase("true") || branchName.equalsIgnoreCase("false"))
			throw new RuntimeException(BUILD_BRANCH_KEY + " should be the name of the branch, not true/false");
		return branchName;
	}

	// ===== Product

	public String getProductName() {
		return get("product.name");
	}

	public String getProductTitle() {
		return get("product.title");
	}

	public String getProductId() {
		return get("product.id");
	}

	public String getProductVersion() {
		return getVersion("product.version").toStringBase();
	}

	public String getProductProvider() {
		return get("product.provider");
	}

	public String getProductMainClass() {
		return get("product.main.class");
	}

	public File getProductOut() throws IOException {
		return new File(getBuildOut(), getProductName() + "/continuous/v" + getProductVersion() + "_" + getBuildNum());
	}

	public File getProductArtifacts() throws IOException {
		return new File(getBuildArtifacts(), getProductName() + "/continuous/v" + getProductVersion() + "_"
			+ getBuildNum());
	}
	public String getServerUrl() {
	  return get("build.server.url");
	}
	
	public String getProductArtifactUrlSpec() throws IOException {
		return getProductArtifacts().getPath().replaceFirst(getBuildArtifacts().getPath(),
			 getServerUrl() + "/artifacts");
	}

	public File getWarningsLog() throws IOException {
		return new File(getProductArtifacts(), "warnings.log");
	}

	public File getProductTemp() throws IOException {
		return new File(getBuildTemp(), getProductName());
	}

	// ===== Eclipse

	public List<OemVersion> getTargetVersions() {
		return getOemVersionList("target.versions");
	}

	/**
	 * If the <b>product.swing.zip</b> property is <code>true</code>, then the subproduct's
	 * class files from this version of Eclipse are assembled into subproduct Swing jars.
	 */
	public OemVersion getSwingTargetVersion() {
		return OemVersion.V_3_5;
	}

	//====================================================================================
	// Reading property files

	/**
	 * Read the build properties
	 */
	public void read() throws IOException {
		readDefaults();
		readProductProperties(new File("."));
	}

	/**
	 * Read the default build properties
	 */
	public void readDefaults() {
		String username = System.getProperty("builder.name");
		if (username == null || username.length() == 0)
			username = System.getProperty("user.name");
		String globalSettingsDirPath = getBuildCommonData().getPath() + "/build-settings-global/";

		read(globalSettingsDirPath + "eclipse-platform.properties");
		read(globalSettingsDirPath + "pde-build.properties");

		read(globalSettingsDirPath + "default-user.properties");
		readIfExists("build-settings/default-user.properties");

		readIfExists(globalSettingsDirPath + username + ".properties");
		readIfExists("build-settings/" + username + ".properties");
	}

	/**
	 * Read the product.properties file from either the build project (name must end with
	 * "_build") or the feature project (sibling to build project, except name ending with
	 * "_feature" rather than "_build").
	 * 
	 * @param projDir The build project directory.
	 * @throws BuildException if file exists in both places or neither place.
	 */
	public void readProductProperties(File projDir) throws IOException {
		readPropertyFile(getProductPropertiesFile(projDir));
	}

	/**
	 * Answer the product.properties file from either the build project (name must end
	 * with "_build") or the feature project (sibling to build project, except name ending
	 * with "_feature" rather than "_build").
	 * 
	 * @throws BuildException if file exists in both places or neither place.
	 */
	public File getProductPropertiesFile() throws IOException {
		return getProductPropertiesFile(new File("."));
	}

	/**
	 * Answer the product.properties file from either the build project (name must end
	 * with "_build") or the feature project (sibling to build project, except name ending
	 * with "_feature" rather than "_build").
	 * 
	 * @param projDir The build project directory.
	 * @throws BuildException if file exists in both places or neither place.
	 */
	public File getProductPropertiesFile(File projDir) throws IOException {
		File buildProjDir = projDir.getCanonicalFile();
		String buildProjName = buildProjDir.getName();
		File buildProjPropFile = new File(buildProjDir, "build-settings/product.properties");

		String featureProjName = buildProjName.substring(0, buildProjName.length() - 6) + "_feature";
		File featureProjDir = new File(buildProjDir, "../" + featureProjName).getCanonicalFile();
		File featureProjPropFile = new File(featureProjDir, "/build-settings/product.properties");

		if (buildProjPropFile.exists()) {
			if (featureProjPropFile.exists())
				throw new BuildException("product.properties file found in two places:\n   "
					+ buildProjPropFile.getPath() + "\n   " + featureProjPropFile.getPath());
			return buildProjPropFile;
		}
		if (featureProjPropFile.exists()) {
			return featureProjPropFile;
		}
		throw new BuildException("Cannot find product.properties file in either of these two places:\n   "
			+ buildProjPropFile.getPath() + "\n   " + featureProjPropFile.getPath());
	}

	public void readIfExists(String path) {
		File file;
		try {
			file = new File(path).getCanonicalFile();
		}
		catch (IOException e) {
			throw new BuildPropertiesException("Failed to resolve property file " + path, e);
		}
		if (file.exists())
			readPropertyFile(file);
		else {
			filesRead.add(0, file);
			System.out.println("Did not find property file " + file.getAbsolutePath());
		}
	}

	public void read(String path) {
		File file;
		try {
			file = new File(path).getCanonicalFile();
		}
		catch (IOException e) {
			throw new BuildPropertiesException("Failed to resolve property file " + path, e);
		}
		readPropertyFile(file);
	}

	public void readPropertyFile(File file) {
		filesRead.add(0, file);
		System.out.println("Reading property file " + file.getAbsolutePath());
		Reader reader;
		try {
			reader = new FileReader(file);
		}
		catch (FileNotFoundException e) {
			throw new BuildPropertiesException("Failed to find property file " + file.getAbsolutePath(), e);
		}
		try {
			properties.load(reader);
		}
		catch (IOException e) {
			throw new BuildPropertiesException("Failed to read property file " + file.getAbsolutePath(), e);
		}
		finally {
			try {
				reader.close();
			}
			catch (IOException e) {
				throw new BuildPropertiesException("Failed to close property file " + file.getAbsolutePath(), e);
			}
		}
	}
	
	/**
	 * Read properties from the specified URL
	 */
	public void readFromUrl(URL url) {
		try {
			InputStream stream = url.openStream();
			try {
				properties.load(stream);
			}
			finally {
				stream.close();
			}
		} catch (IOException e) {
			throw new BuildPropertiesException("Failed to read property file from " + url);
		}
	}

	/**
	 * FOR TESTING PURPOSES ONLY
	 */
	void readStream(InputStream stream) throws IOException {
		properties.load(stream);
	}
}
