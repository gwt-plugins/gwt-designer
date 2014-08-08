package com.instantiations.pde.build.check

import com.instantiations.pde.build.PdeBuild
import com.instantiations.pde.build.util.BuildPropertiesimport org.apache.tools.ant.BuildExceptionimport com.instantiations.pde.build.preprocessor.BundleManifestReaderimport java.util.regex.Pattern
import java.util.zip.ZipFileimport java.util.zip.ZipEntry/**
 * Sanity check the source code for common problems
 * such as files missing from build.properties, missing execution environment, etc.
 */
public class SanityCheckFeature extends SanityCheckProject
{
	protected static final Pattern ANT_VARIABLE_PATTERN = Pattern.compile('@[A-Za-z0-9]@');
	protected static final Pattern INVALID_PREPROCESSOR_STMT_PATTERN = Pattern.compile('\\$codepro\\.preprocessor\\.(if|elseif)\\s+version\\s+><');
	protected static final Pattern ACTIVATOR_PATTERN = Pattern.compile('extends\\s*(Plugin|AbstractUIPlugin)');
	protected static final Pattern SINGLETON_PATTERN = Pattern.compile(';\\s*singleton\\s*:=\\s*true')

	// Used internally
	protected List executionEnvironments;
	protected Collection<String> missingProjNames = [];
	protected File activatorJavaFile = null;
	
	/**
	 * Check for common problems in the primary feature and referenced plugins.
	 */
	protected void checkSourceImpl() {
		super.checkSourceImpl();
		checkBuildProperties();
		checkFeatureProjName();
		checkExecutionEnvironments();
		checkFeaturesAndPlugins();

//		assertNoStrayBuildPropertiesOutputProperties();
//		assertMustHaveBuildPropertiesDotInBinIncludesProperty();

//		assertOptionsFileContainsOptionsExpressedInCode();
//		assertOptionsFileIncludedInBuildProperties();
//		assertAllBuildXmlFilesRemoved();
//		
//		checkAllOldPluginXmlForPreprocessorStatements();
//		obfuscateCommonCorePlugin();
//		copyUnobfuscatedPluginsToArtifactDir();
//		removeBuildBundleXmlFilesAfterAddingObfuscation();

		// Write missing projects to log
		if (!missingProjNames.isEmpty())
			writeMissingProjectsLog(missingProjNames);
	}
	
	/**
	 * Assert common build properties
	 */
	protected void checkBuildProperties() {
		if (prop.isTrue('debug.pde') && prop.isHeadlessBuild())
			fail('Cannot set "debug.pde = true" in build properties on build server.');
	}
	
	/**
	 * Assert that the name of the build project ends with "_build"
	 */
	protected void checkBuildProjName() {
		super.checkBuildProjName();
		File buildProjDir = new File('.').canonicalFile;
		if (!buildProjDir.name == prop.productId + '_build') {
			throw new BuildException('Expected the name of the build project to be ' + prop.productId + '_build, but found\n   ' 
				+ buildProjDir.path);
		}
	}
	
	/**
	 * Assert that the name of the feature project ends with "_feature"
	 */
	protected void checkFeatureProjName() {
		File featureProjDir = new File('../' + prop.productId + '_feature').canonicalFile;
		if (!featureProjDir.exists()) {
			throw new BuildException('Expected feature project\n   ' 
				+ featureProjDir.path);
		}
	}
	
	/**
	 * Check the execution environments defined in pde-build.properties
	 * and cache the list for later use when checking plugin manifests
	 */
	protected void checkExecutionEnvironments() {
		inContext('in BuildProperties') {
			executionEnvironments = new ArrayList();
			prop.keys.sort().each { key ->
				if (key.startsWith('pde.') && key.endsWith('.jdk.version')) {
					String env = key.substring(4, key.length() - 12);
					if (!prop.isDefined('pde.' + env))
						fail(key + ' is defined, but missing pde.' + env);
					else
						executionEnvironments.add(env);
				}
			}
		}
	}
	
	/**
	 * Check the primary product feature and all referenced plugins
	 */
	protected void checkFeaturesAndPlugins() {
		checkFeaturesAndPlugins(prop.productId);
	}
	
	/**
	 * Check the specified feature and all referenced plugins
	 */
	protected void checkFeaturesAndPlugins(String featureId) {
		File srcProj = new File(sourceDir, featureId + "_feature");
		if (!srcProj.exists()) {
			missingProjNames.add(srcProj.name);
			fail('Missing feature source project: ' + srcProj.canonicalPath);
			return;
		}
		File featureXmlFile = new File(srcProj, "feature.xml");
		Node feature = new XmlParser().parse(featureXmlFile);
		checkFeature(srcProj, feature);
		for (child in feature.includes)
			checkFeaturesAndPlugins(child.'@id');
		for (plugin in feature.plugin)
			checkPlugin(
				new File(sourceDir, plugin.'@id'), 
				plugin.'@unpack' == 'false',
				plugin.'@fragment' == 'true');
	}
	
	/**
	 * Check just the specified feature
	 */
	protected void checkFeature(File srcProj, Node feature) {
		println('Checking feature ' + srcProj.name);
		inContext('in feature ' + srcProj.name) {
			if (hasJavaNature(srcProj))
				fail('Feature should not have java nature');
			checkFeatureXml(srcProj, feature);
			checkFeatureProperties(srcProj, feature);
			checkFeatureBuildProperties(srcProj, feature);
		}
	}
	
	/**
	 * Check the feature.xml file
	 */
	protected void checkFeatureXml(File srcProj, Node feature) {
		inContext('in feature.xml of ' + srcProj.name) {
			if (prop.get('product.version').startsWith('0.0.0')) {
				Properties prodProp = new Properties();
				new File(srcProj, 'build-settings/product.properties').withReader { reader -> prodProp.load(reader); }
				if (prodProp.get('product.version') != null)
					fail('Invalid product.version ' + prop.get('product.version') + ' defined in product.properties');
				else
					fail('Invalid version ' + prop.get('product.version') + ' defined in feature.xml');
			}
			if (assertNodeDefined(feature.description, 'description')) {
				assertNodeTextDefined(feature.description, 'description');
				assertURLNodeValid(feature.description.'@url', 'description url attribute');
			}
			if (assertNodeDefined(feature.license, 'license')) {
				if (assertNodeTextDefined(feature.license, 'license')) {
					String text = feature.license.text();
					if (text.indexOf('@') > 0)
						fail('License contains Ant variables that will not be replaced at build time\n      ' 
							+ (text.length() < 200 ? text : text.substring(0, 200) + '...'));
					if (text.indexOf(prop.buildYear) > 0)
						fail('License year ' + prop.buildYear + ' should be replaced by ${build_year}');
					if (text.indexOf('' + (Integer.parseInt(prop.buildYear) - 1)) > 0)
						fail('License year ' + (Integer.parseInt(prop.buildYear) - 1) + ' should be replaced by ${build_year}');
				}
				assertURLNodeValid(feature.license.'@url', 'license url attribute');
			}
			if (assertNodeDefined(feature.copyright, 'copyright')) {
				if (assertNodeTextDefined(feature.copyright, 'copyright')) {
					String text = feature.copyright.text();
					if (text.indexOf('@') > 0)
						fail('Copyright contains Ant variables that will not be replaced at build time\n      ' + text);
					if (text.indexOf(prop.buildYear) > 0)
						fail('Copyright year ' + prop.buildYear + ' should be replaced by ${build_year}');
					if (text.indexOf('' + (Integer.parseInt(prop.buildYear) - 1)) > 0)
						fail('Copyright year ' + (Integer.parseInt(prop.buildYear) - 1) + ' should be replaced by ${build_year}');
				}
				assertURLNodeValid(feature.copyright.'@url', 'copyright url attribute');
			}
		}
		new SanityCheckPreprocessor(
			check:		this, 
			sourceDir:	sourceDir, 
			prop:		prop
		).assertStatementsNotRemoved(new File(srcProj, "feature.xml"));
	}
	
	/**
	 * Check the feature.properties file
	 */
	protected checkFeatureProperties(File srcProj, Node feature) {
		File featurePropFile = new File(srcProj, 'feature.properties');
		if (!featurePropFile.exists()) {
			fail('Missing feature.properties file');
			return;
		}
		Properties featureProp = new Properties();
		featurePropFile.withReader { reader -> featureProp.load(reader); }
		checkFeatureProperties(srcProj, feature, featureProp);
	}
	
	/**
	 * Check the feature.properties file
	 */
	protected checkFeatureProperties(File srcProj, Node feature, Properties featureProp) {
		if (feature.'@label' != '%featureName')
			fail('Attribute "label" should have value "%featureName" in feature.xml');
		assertPropertyExists('feature.properties', featureProp, 'featureName');
		if (feature.'@provider-name' != '%providerName')
			fail('Attribute "provider-name" should have value "%providerName" in feature.xml');
		assertPropertyHasValue('feature.properties', featureProp, 'providerName', '${product.provider}');
		assertPropertyExists('feature.properties', featureProp, 'siteCategory', 
			'The siteCategory is used when categorizing dynamically appended required features to the site.xml file for an update site');
	}

	/**
	 * Check the feature's build.properties file
	 */
	protected checkFeatureBuildProperties(File srcProj, Node feature) {
		File buildPropFile = new File(srcProj, 'build.properties');
		if (!buildPropFile.exists()) {
			fail('Missing build.properties file');
			return;
		}
		Properties buildProp = new Properties();
		buildPropFile.withReader { reader -> buildProp.load(reader); }
		checkFeatureBuildProperties(srcProj, feature, buildProp);
	}
	
	/**
	 * Check the feature's build.properties file
	 */
	protected checkFeatureBuildProperties(File srcProj, Node feature, Properties buildProp) {
		checkBinIncludesBuildProperty(srcProj, null, buildProp, false, false);
	}
	
	/**
	 * Check the specified plugin's manifest and plugin properties files
	 */
	protected void checkPlugin(File srcProj, boolean isJar, boolean isFragment) {
		File manifestFile = getManifestFile(srcProj);
		if (manifestFile == null)
			return;
		String projType = isFragment ? 'fragment' : 'plugin';
		println('Checking ' + projType + ' ' + srcProj.name);
		inContext('in ' + projType + ' ' + srcProj.name) {
			BundleManifestReader manifest = new BundleManifestReader();
			manifest.process(manifestFile);
			checkPluginManifest(manifest, isJar, isFragment);
			if (isFragment) {
				checkFragmentXmlFile(new File(srcProj, 'fragment.xml'));
				checkFragmentProperties(srcProj, manifest);
			}
			else {
				checkPluginXmlFile(new File(srcProj, 'plugin.xml'));
				checkPluginProperties(srcProj, manifest);
			}
			checkPluginClasspath(srcProj, manifest);
			checkPluginBuildProperties(srcProj, manifest, isJar, isFragment);
			checkPluginProductDocZip(srcProj);
			checkAboutFiles(srcProj);

			// activatorJavaFile is used to return a value 
			// detected during scanning the plugin's source code
			activatorJavaFile = null;
			checkJavaSourceFolders(manifest, srcProj);
			
			checkActivator(manifest, activatorJavaFile);
		}
	}
	
	/**
	 * Answer the MANIFEST.MF file or null if it does not exist
	 */
	protected File getManifestFile(File srcProj) {
		if (!srcProj.exists()) {
			missingProjNames.add(srcProj.name);
			fail('Missing plugin source project: ' + srcProj.canonicalPath);
			return null;
		}
		File metaInfDir = srcProj.listFiles().find { it.name == 'META-INF' };
		if (metaInfDir == null) {
			metaInfDir = srcProj.listFiles().find { it.name.equalsIgnoreCase('META-INF') };
			if (metaInfDir == null)
				fail('Missing plugin META-INF/MANIFEST.MF file in ' + srcProj.canonicalPath);
			else
				fail('Plugin META-INF directory name is not all uppercase: ' + metaInfDir.canonicalPath);
			return null;
		}
		File manifestFile = metaInfDir.listFiles().find { it.name == 'MANIFEST.MF' };
		if (manifestFile == null) {
			manifestFile = metaInfDir.listFiles().find { it.name.equalsIgnoreCase('MANIFEST.MF') };
			if (manifestFile == null)
				fail('Missing plugin META-INF/MANIFEST.MF file in ' + srcProj.canonicalPath);
			else
				fail('Plugin META-INF/MANIFEST.MF file name is not all uppercase: ' + manifestFile.canonicalPath);
			return null;
		}
		return manifestFile;
	}
	
	/**
	 * Validate the content of the plugin's MANIFEST.MF files
	 */
	protected void checkPluginManifest(BundleManifestReader manifest, boolean isJar, boolean isFragment) {
		assertManifestValue(manifest, 'Bundle-SymbolicName', SINGLETON_PATTERN,
			'Check the "This plugin/fragment is a singleton" checkbox on the Overview page in the plugin/fragment manifest editor');
		String fragmentHost = manifest.get('Fragment-Host');
		if (isFragment) {
			if (fragmentHost == null)
				fail('feature.xml specifies fragment="true" for ' + manifest.getId() 
					+ '\n   but MANIFEST.MF does not specify "Fragment-Host: <plugin-id>"');
			else if (fragmentHost.indexOf('bundle-version') != -1)
				warn('MANIFEST.MF should not specify "bundle-version" in "Fragment-Host: ' + fragmentHost + '"');
			assertManifestValue(manifest, 'Bundle-Localization', 'fragment');
		}
		else {
			if (fragmentHost != null)
				fail('feature.xml does not specify fragment="true" for ' + manifest.getId() 
					+ '\n   yet MANIFEST.MF specifies "Fragment-Host: ' + fragmentHost + '"');
			assertManifestValue(manifest, 'Bundle-ActivationPolicy', 'lazy',
					'Check the "Activate this plugin/fragment when one of its classes is loaded" checkbox on the Overview page in the plugin/fragment manifest editor');
			assertManifestValue(manifest, 'Bundle-Localization', 'plugin');
		}
		if (!prop.getList('sanitycheck.version.overrides').contains(manifest.getId())) {
			assertManifestValue(manifest, 'Bundle-Version', '0.0.0',
				'Set the Bundle-Version to 0.0.0 or add the plugin identifier'
				+ '\n   to the sanitycheck.version.overrides property in the product.properties file.');
		}
		
		assertManifestValue(manifest, 'Bundle-RequiredExecutionEnvironment', executionEnvironments, 
			'Set the execution environment on the overview page of the plugin manifest editor'
			+ '\n   and click the "Update Classpath Settings" on that page.');
		Collection bundleClasspath = manifest.getList('Bundle-ClassPath', null);
		if (isJar) {
			if (bundleClasspath != null) {
				boolean found = false;
				for (String entry : bundleClasspath)
					if (entry.trim() == '.')
						found = true;
				if (!found)
					fail('Jar\'d plugins should have "." on their Bundle-ClassPath or no Bundle-ClassPath defined at all', 
						'Modify the Classpath on the Runtime page of the plugin manifest editor');
			}
		}
		if (manifest.activator == 'org.eclipse.core.internal.compatibility.PluginActivator')
			fail('Compatibility Activator Specificed: ' + manifest.activator,
				'Refactor the old-style activator: ' + manifest.get('Plugin-Class')
				+ '\n   and modify the MANIFEST.MF to replace org.eclipse.core.internal.compatibility.PluginActivator'
				+ '\n   with ' + manifest.get('Plugin-Class'));
		else if (manifest.get('Plugin-Class') != null)
			fail('Manifest still contains unnecessary reference to plugin activator'
				+ '\n   Plugin-Class: ' + manifest.get('Plugin-Class'));
		
		// Find duplicate bundles in the required bundles list in the MANIFEST.MF
		
		Collection<String> requiredPlugins = manifest.requiredPlugins;
		if (requiredPlugins.size() != new TreeSet(requiredPlugins).size()) {
			Collection<String> normalized = new TreeSet(requiredPlugins);
			Collection<String> delta = new ArrayList(requiredPlugins.size());
			for (String id : requiredPlugins)
				if (!normalized.remove(id))
					delta.add(id);
			StringBuffer buf = new StringBuffer(200);
			buf.append(delta.size() + ' duplicate bundle identifiers in Require-Bundle list');
			for (String id : delta)
				buf.append('\n      ' + id);
			fail(buf.toString());
		}
		
		for (String requiredId : requiredPlugins)
			if (requiredId.indexOf('compatibility') != -1)
				fail('Require-Bundle list contains compatibility bundle: ' + requiredId);
		for (String item : manifest.getList('Require-Bundle')) {
			if (item.indexOf(';bundle-version="') != -1)
				fail('Found required bundle with version specified: ' + item);
			if (item.indexOf(';resolution:=optional') != -1)
				warn('Found required bundle with optional resolution: ' + item);
		}
	}
	
	/**
	 * Validate the content of the fragments's fragment.xml file
	 */
	protected void checkFragmentXmlFile(File fragmentXmlFile) {
		if (!fragmentXmlFile.exists())
			return;
		fragmentXmlFile.withReader { reader ->
			String line;
			line = reader.readLine().trim();
			line = reader.readLine().trim();
			// Warn for now...
			if (line != '<?eclipse version="3.2"?>')
				fail('Expected fragment.xml to have 2nd line = <?eclipse version="3.2"?>');
		}
	}
	
	/**
	 * Validate the content of the plugin's plugin.xml file
	 */
	protected void checkPluginXmlFile(File pluginXmlFile) {
		if (!pluginXmlFile.exists())
			return;
		pluginXmlFile.withReader { reader ->
			String line;
			line = reader.readLine().trim();
			line = reader.readLine().trim();
			if (line != '<?eclipse version="3.2"?>')
				fail('Expected plugin.xml to have 2nd line = <?eclipse version="3.2"?>');
		}
		new SanityCheckPreprocessor(
			check:		this, 
			sourceDir:	pluginXmlFile.parentFile, 
			prop:		prop
		).assertStatementsNotRemoved(pluginXmlFile, false);
	}
	
	/**
	 * Check the fragment manifest and fragment properties 
	 */
	protected void checkFragmentProperties(File srcProj, BundleManifestReader manifest) {
		File fragmentPropFile = new File(srcProj, 'fragment.properties');
		if (!fragmentPropFile.exists()) {
			fail('Missing fragment.properties');
			return;
		}
		Properties fragmentProp = new Properties();
		fragmentPropFile.withReader { reader -> fragmentProp.load(reader); }
		checkFragmentProperties(srcProj, manifest, fragmentProp);
	}
	
	/**
	 * Check the plugin manifest and plugin properties 
	 */
	protected void checkPluginProperties(File srcProj, BundleManifestReader manifest) {
		File pluginPropFile = new File(srcProj, 'plugin.properties');
		if (!pluginPropFile.exists()) {
			fail('Missing plugin.properties');
			return;
		}
		Properties pluginProp = new Properties();
		pluginPropFile.withReader { reader -> pluginProp.load(reader); }
		checkPluginProperties(srcProj, manifest, pluginProp);
	}
	
	/**
	 * Check the fragment manifest and fragment properties 
	 */
	protected void checkFragmentProperties(File srcProj, BundleManifestReader manifest, Properties fragmentProp) {
		assertManifestValue(manifest, 'Bundle-Name', '%fragmentName');
		assertPropertyExists('fragment.properties', fragmentProp, 'fragmentName');
		assertManifestValue(manifest, 'Bundle-Vendor', '%providerName');
		assertPropertyHasValue('fragment.properties', fragmentProp, 'providerName', '${product.provider}');
		assertPropertyDoesNotExist('fragment.properties', fragmentProp, 'build');
		assertNoValueContaining('fragment.properties', fragmentProp, '@BuildNum@');
	}
	
	/**
	 * Check the plugin manifest and plugin properties 
	 */
	protected void checkPluginProperties(File srcProj, BundleManifestReader manifest, Properties pluginProp) {
		assertManifestValue(manifest, 'Bundle-Name', '%pluginName');
		assertPropertyExists('plugin.properties', pluginProp, 'pluginName');
		assertManifestValue(manifest, 'Bundle-Vendor', '%providerName');
		if (!prop.getList('sanitycheck.provider.overrides').contains(manifest.getId())) {
			assertPropertyHasValue('plugin.properties', pluginProp, 'providerName', '${product.provider}');
		}
		assertPropertyDoesNotExist('plugin.properties', pluginProp, 'build');
		assertNoValueContaining('plugin.properties', pluginProp, '@BuildNum@');
	}
	
	/**
	 * Check the plugin classpath 
	 */
	protected void checkPluginClasspath(File srcProj, BundleManifestReader manifest) {
		File classpathFile = new File(srcProj, '.classpath');
		if (!classpathFile.exists())
			return;
		Node classpath = new XmlParser().parse(classpathFile);
		checkPluginClasspath(srcProj, manifest, classpath);
	}
	
	/**
	 * Check the plugin classpath 
	 */
	protected void checkPluginClasspath(File srcProj, BundleManifestReader manifest, Node classpath) {
		String jrePath = null;
		for (entry in classpath.classpathentry) {
			if (entry.'@kind' == 'con' && entry.'@path'.startsWith('org.eclipse.jdt.launching.JRE_CONTAINER')) {
				jrePath = entry.'@path';
				break;
			}
		}
		String execEnv = manifest.get('Bundle-RequiredExecutionEnvironment');
		if (jrePath == null)
			fail('Missing classpath entry org.eclipse.jdt.launching.JRE_CONTAINER');
		else if (execEnv != null && !jrePath.endsWith(execEnv))
			fail('JRE container specified in classpath file is not ' + execEnv,
				'Set the execution environment on the overview page of the plugin manifest editor'
				+ '\n   and click the "Update Classpath Settings" on that page.');
	}
	
	/**
	 * Check the plugin's build.properties file
	 */
	protected void checkPluginBuildProperties(File srcProj, BundleManifestReader manifest, boolean isJar, boolean isFragment) {
		File buildPropFile = new File(srcProj, 'build.properties');
		if (!buildPropFile.exists()) {
			fail('Missing build.properties file');
			return;
		}
		Properties buildProp = new Properties();
		buildPropFile.withReader { reader -> buildProp.load(reader); }
		checkPluginBuildProperties(srcProj, manifest, buildProp, isJar, isFragment);
	}
	
	/**
	 * Check the plugin's build.properties file
	 */
	protected void checkPluginBuildProperties(File srcProj, BundleManifestReader manifest, Properties buildProp, boolean isJar, boolean isFragment) {
		checkCompileOrderBuildProperty(srcProj, manifest, buildProp, isJar, isFragment);
		checkBinIncludesBuildProperty(srcProj, manifest, buildProp, isJar, isFragment);
	}
	
	/**
	 * Check that the product.doc.zip is valid for this plugin
	 */
	protected void checkPluginProductDocZip(File srcProj) {
		Collection docZipIncludes = prop.getList('product.doc.zip');
		for (String path : docZipIncludes) {
			File file = new File(srcProj, path);
			if (file.isFile() && path.endsWith('/'))
				fail('"' + path + '" in product.doc.zip specifies a file and should NOT end with "/"');
			if (file.isDirectory() && !path.endsWith('/'))
				fail('"' + path + '" in product.doc.zip specifies a directory and MUST end with "/"');
		}
	}
	
	/**
	 * Check to see if there is an about.properties file and a matching about.mappings file
	 */
	protected void checkAboutFiles(File srcProj) {
		File aboutPropFile = new File(srcProj, 'about.properties');
		File aboutMappingsFile = new File(srcProj, 'about.mappings');
		if (aboutPropFile.exists()) {
			if (!aboutMappingsFile.exists())
				fail('Found ' + aboutPropFile.name + ' but missing ' + aboutMappingsFile.name);
		}
		if (aboutMappingsFile.exists()) {
			if (!aboutPropFile.exists())
				fail('Found ' + aboutMappingsFile.name + ' but missing ' + aboutPropFile.name);
			if (fileContains(aboutMappingsFile, '@BUILD.DATE@'))
				fail('Replace @BUILD.DATE@ with ${build.date} in about.mappings');
		}
	}
	
	/**
	 * Search the project's source folders
	 * to see if any *.java files contain @BuildNum@, etc
	 */
	protected void checkJavaSourceFolders(BundleManifestReader manifest, File srcProj) {
		File buildPropFile = new File(srcProj, 'build.properties');
		if (!buildPropFile.exists())
			return;
		Properties buildProp = new Properties();
		buildPropFile.withReader { reader -> buildProp.load(reader); }
		for (String key : buildProp.keySet()) {
			if (key.startsWith('source.')){
				Collection srcFolders = buildProp.get(key).toString().split(",");
				for (String folder in srcFolders) {
					if (folder.endsWith('/')) {
						folder = folder.substring(0, folder.length() - 1); 
					}
					checkJavaSourceFolder(manifest, new File(srcProj, folder));
				}
			}
		}
	}
	
	/**
	 * Recursively search the specified source folder
	 * to see if any *.java files contain @BuildNum@, etc
	 */
	protected void checkJavaSourceFolder(BundleManifestReader manifest, File dir) {
		dir.eachDir { checkJavaSourceFolder(manifest, it); }
		dir.eachFile { if (it.name.endsWith('.java')) checkJavaSourceFile(manifest, it); }
	}
	
	/**
	 * Recursively search the specified source folder
	 * to see if any *.java files contain @BuildNum@, etc
	 */
	protected void checkJavaSourceFile(BundleManifestReader manifest, File javaFile) {
		javaFile.withReader { reader ->
			while (true) {
				String line = reader.readLine();
				if (line == null)
					break;
				if (ANT_VARIABLE_PATTERN.matcher(line).find()) {
					fail('The following line contains an Ant Variable in the form @varname@ that will not be replaced at build time'
							+ '\n   ' + line + '\n   in ' + javaFile.canonicalPath);
				}
				if (INVALID_PREPROCESSOR_STMT_PATTERN.matcher(line).find()) {
					fail('Invalid Preprocessor Statement: \n      ' + line + '\n      in ' + javaFile.canonicalPath);
				}
				if (ACTIVATOR_PATTERN.matcher(line).find()) {
					String activator = manifest.activator;
					if (activator == null) {
						activatorJavaFile = javaFile;
						fail('Expected activator ' + javaFile.canonicalPath + '\n   but activator is unspecified');
					}
					else if (activator.endsWith('.' + javaFile.name.substring(0, javaFile.name.length() - 5))) {
						activatorJavaFile = javaFile;
					}
					else if (activatorJavaFile == null) {
						activatorJavaFile = javaFile;
					}
				}
			}
		}
	}
	
	/**
	 * Check the activator and activator source file if any
	 */
	protected void checkActivator(BundleManifestReader manifest, File javaFile) {
		String activator = manifest.activator;
		if (javaFile != null) {
			checkActivatorSourceFile(javaFile);
			if (!activator.endsWith('.' + javaFile.name.substring(0, javaFile.name.length() - 5)))
				fail('Expected activator ' + javaFile.canonicalPath + '\n   but found ' + activator);
		}
		else {
			if (activator != null)
				warn('No source file found for activator ' + activator);
		}
	}
	
	/**
	 * Check to see if the activator as currently implemented requires the compatibility bundle
	 */
	protected void checkActivatorSourceFile(File javaFile) {
		final String shortClassName = javaFile.name.substring(0, javaFile.name.length() - 5);
		final Pattern PREPROC_IF_E21_PATTERN = Pattern.compile('if\\s+(eclipse.version|version)\\s+(< 3.0|<= 2.1|== 2.1)');
		final Pattern PREPROC_ANY_PATTERN = Pattern.compile('(if|else|endif)');
		final Pattern OLD_CONSTRUCTOR_PATTERN = Pattern.compile('public\\s*' + shortClassName + '\\s*\\(\\s*[A-Za-z]+');
		final Pattern NEW_CONSTRUCTOR_PATTERN = Pattern.compile('public\\s*' + shortClassName + '\\s*\\(\\s*\\)');
		final Pattern OLD_START_PATTERN = Pattern.compile('public\\s*void\\s*startup\\s*\\(');
		final Pattern NEW_START_PATTERN = Pattern.compile('public\\s*void\\s*start\\s*\\(');
		final Pattern OLD_STOP_PATTERN = Pattern.compile('public\\s*void\\s*shutdown\\s*\\(');
		final Pattern NEW_STOP_PATTERN = Pattern.compile('public\\s*void\\s*stop\\s*\\(');
		
		boolean oldStart = false;
		boolean newStart = false;
		boolean oldStop = false;
		boolean newStop = false;
		boolean in_preproc_e21 = false;

		String errMsg = '';
		javaFile.withReader { reader ->
			while (true) {
				String line = reader.readLine();
				if (line == null)
					break;
				if (PREPROC_IF_E21_PATTERN.matcher(line).find())
					in_preproc_e21 = true;
				else if (PREPROC_ANY_PATTERN.matcher(line).find())
					in_preproc_e21 = false;
				
				if (OLD_CONSTRUCTOR_PATTERN.matcher(line).find() && !in_preproc_e21)
					errMsg += 'Expected old style activator in constructor to be inside Eclipse 2.1 preprocessor statements\n   ';
				if (NEW_CONSTRUCTOR_PATTERN.matcher(line).find())
					errMsg += 'Remove no argument constructor from activator and move functionality into start(...) method\n   ';
				
				if (OLD_START_PATTERN.matcher(line).find())
					oldStart = true;
				if (NEW_START_PATTERN.matcher(line).find())
					newStart = true;
				
				if (OLD_STOP_PATTERN.matcher(line).find())
					oldStop = true;
				if (NEW_STOP_PATTERN.matcher(line).find())
					newStop = true;
			}
		}
		if (oldStart && !newStart)
			errMsg += 'Found old style startup(...) in activator but missing new style start(...)\n   ';
		if (oldStop && !newStop)
			errMsg += 'Found old style shutdown(...) in activator but missing new style stop(...)\n   ';
		
		// TODO: change this back to a fail(...) when warnings are cleaned up
		if (errMsg.length() > 0)
			warn(errMsg + 'in ' + javaFile.canonicalPath,
				'For preprocessor code supporting Eclipse 2.1 and 3.x style activators, see "Activators for Eclipse 2.1 and 3.x"\n   in https://hudson.instantiations.com/artifacts/BuildReport/continuous/latest/docs/preprocessing.html');
	}
	
	/**
	 * Check the compilation related properties as compared with the Bundle-ClassPath
	 */
	protected void checkCompileOrderBuildProperty(File srcProj, BundleManifestReader manifest, Properties buildProp, boolean isJar, boolean isFragment) {
		if (!hasJavaNature(srcProj))
			return;
		String compileOrderValue = buildProp.getProperty('jars.compile.order');
//		if (compileOrderValue == null) {
//			fail('jars.compile.order is undefined build.properties file',
//				'Modify the "Runtime Information" on the Build page in the plugin manifest editor');
//			return;
//		}
		Collection compileOrder;
		if (compileOrderValue == null || compileOrderValue.trim().length() == 0) {
			String srcDot = buildProp.getProperty('source..');
			String outDot = buildProp.getProperty('output..');
			if (srcDot != null && srcDot.trim().length() > 0 && outDot != null && outDot.trim().length() > 0)
				compileOrder = [ '.' ];
			else
				compileOrder = [ ];
		}
		else {
			compileOrder = compileOrderValue.split(',').collect { it.trim() };
		}
		Collection bundleClasspath = manifest.getList('Bundle-ClassPath', isJar ? ['.'] : [ ]);
		for (String entry : bundleClasspath) {
			if (!new File(srcProj, entry).exists() && compileOrder.find { it == entry } == null)
				fail('jars.compile.order is missing "' + entry + '", which is in the Bundle-ClassPath but does not already exist in ' + srcProj.name,
					'Modify the "Runtime Information" on the Build page in the plugin manifest editor'
					+ '\nor check ' + entry + ' into the source code management system (CVS, SVN, ...)');
		}
		for (String entry : compileOrder) {
			if (buildProp.getProperty('source.' + entry) == null)
				fail('Either missing "source.' + entry + '" in build.properties'
					+ '\n   or the jars.compile.order contains "' + entry + '" when it should not',
   					'Modify the "Runtime Information" on the Build page in the plugin manifest editor');
			if (buildProp.getProperty('output.' + entry) == null)
				fail('Missing "output.' + entry + '" in build.properties',
   					'Modify the "Runtime Information" on the Build page in the plugin manifest editor');
		}
		for (String key : buildProp.keySet()) {
			if (key.startsWith('source.') && !compileOrder.contains(key.substring(7)))
				fail('Unnecessary key in build.properties: ' + key);
			if (key.startsWith('output.') && !compileOrder.contains(key.substring(7)))
				fail('Unnecessary key in build.properties: ' + key);
		}
	}
	
	/**
	 * Check the bin.includes build property
	 */
	protected void checkBinIncludesBuildProperty(File srcProj, BundleManifestReader manifest, Properties buildProp, boolean isJar, boolean isFragment) {
		String binIncludesValue = buildProp.getProperty('bin.includes');
		if (binIncludesValue == null || binIncludesValue.trim().length() == 0) {
			fail('Missing bin.includes property in build.properties file');
			return;
		}
		Collection binIncludes = binIncludesValue.split(',').collect { it.trim(); };
		if (hasJavaNature(srcProj) && manifest != null) {
			Collection bundleClasspath = manifest.getList('Bundle-ClassPath', isJar ? ['.'] : [ ]);
			if (bundleClasspath != ['.'] || buildProp.getProperty('source..') != null) {
				for (String entry : bundleClasspath) {
					if (!entry.startsWith('lib/') && !binIncludes.contains(entry)) {
						String msg = 'Missing "' + entry + '" in bin.includes property in build.properties file';
						if (entry != '.')
							msg += '\nor move "' + entry + '" into the plugin "lib" directory';
						fail(msg);
					}
				}
			}
		}
		// Check for common file names
		Collection commonFileNames = [
            '.options',
		    'feature.xml', 'feature.properties', 
		    'plugin.xml', 'plugin.properties', 
		    'fragment.xml', 'fragment.properties'
		];
		commonFileNames.addAll(srcProj.list().findAll { it.startsWith('toc') && it.endsWith('.xml')});
		commonFileNames.addAll(srcProj.list().findAll { it.startsWith('about.') });
		for (String name : commonFileNames) {
			File file = new File(srcProj, name);
			if (file.exists() && !binIncludes.contains(name))
				fail('Missing "' + name + '" in bin.includes property in build.properties file');
		}
		// Check for common directory names
		for (String name : ['META-INF/', 'doc/', 'examples/', 'html/', 'icons/', 'images/', 'stylesheets/']) {
			File file = new File(srcProj, name);
			if (file.exists() && !binIncludes.contains(name))
				fail('Missing "' + name + '" in bin.includes property in build.properties file');
		}
		// Check for common files that should NOT be included
		commonFileNames = ['build.properties'];
		commonFileNames.addAll(srcProj.list().findAll { it.startsWith('build') && it.endsWith('.xml') });
		for (String name : commonFileNames) {
			if (binIncludes.contains(name))
				fail('"' + name + '" should NOT be in bin.includes in build.properties file');
		}
		// Check that specified directories end with "/" and specified files do not
		for (String path : binIncludes) {
			if (path == '.')
				continue;
			File file = new File(srcProj, path);
			if (file.isFile() && path.endsWith('/'))
				fail('"' + path + '" in bin.includes in build.properties file specifies a file and should NOT end with "/"');
			if (file.isDirectory() && !path.endsWith('/'))
				fail('"' + path + '" in bin.includes in build.properties file specifies a directory and MUST end with "/"');
		}
	}
	
	/**
	 * Assert that the MANIFEST.MF has the specified key/value pair
	 * 
	 * @return the actual value or null
	 */
	protected String assertManifestValue(BundleManifestReader manifest, String manifestKey, String expectedValue = null, String instructions = null) {
		String manifestValue = manifest.get(manifestKey);
		if (manifestValue == null) {
			fail('Missing attribute "' + manifestKey + '" in MANIFEST.MF', instructions);
		}
		else if (expectedValue != null && expectedValue != manifestValue) {
			fail('Attribute "' + manifestKey + '" should have value "' + expectedValue + '" in MANIFEST.MF', instructions);
		}
		return manifestValue;
	}
	
	/**
	 * Assert that the MANIFEST.MF has the specified key
	 * and that the value contains the specified pattern
	 * 
	 * @return the actual value or null
	 */
	protected String assertManifestValue(BundleManifestReader manifest, String manifestKey, Pattern expectedPattern, String instructions = null) {
		String manifestValue = manifest.get(manifestKey);
		if (manifestValue == null) {
			fail('Missing attribute "' + manifestKey + '" in MANIFEST.MF', instructions);
		}
		else if (!expectedPattern.matcher(manifestValue).find()) {
			fail('Attribute "' + manifestKey + '" should have contain "' + expectedPattern.toString() + '" in MANIFEST.MF', instructions);
		}
		return manifestValue;
	}
	
	/**
	 * Assert that the MANIFEST.MF has the specified key
	 * and that the value is one of a specific set of values
	 * 
	 * @return the actual value or null
	 */
	protected String assertManifestValue(BundleManifestReader manifest, String manifestKey, Collection validValues, String instructions = null) {
		String manifestValue = manifest.get(manifestKey);
		if (manifestValue == null) {
			fail('Missing attribute "' + manifestKey + '" in MANIFEST.MF', instructions);
		}
		else if (!validValues.contains(manifestValue)) {
			fail('Attribute "' + manifestKey + '" has an invalid value "' + manifestValue + '" in MANIFEST.MF'
				+ '\n   valid values are ' + validValues, instructions);
		}
		return manifestValue;
	}
	
	//==============================================================
	// Post PDE Build sanity check
	
	/**
	 * Check for common problems in the PDE Build output
	 */
	protected void checkPDEBuildResult(PdeBuild builder) {
		check('PDE Build Sanity Check') {
			inContext('   in ' + builder.targetVersion) {
				checkGeneratedFeaturesAndPlugins(builder.getFeatureOutputDir());
			}
		}
	}
	
	/**
	 * Check the generated features and plugins for common problems
	 */
	protected void checkGeneratedFeaturesAndPlugins(File eclipseDir) {
		for (File featureDir : new File(eclipseDir, "features").listFiles()) {
			inContext('   in feature ' + featureDir.name) {
				checkGeneratedFeature(featureDir); 
			}
		}
	}
	
	/**
	 * Check the generated feature and referenced plugins for common problems
	 */
	protected void checkGeneratedFeature(File featureDir) {
		Node feature = new XmlParser().parse(new File(featureDir, "feature.xml"));
		for (plugin in feature.plugin) {
			File pluginDir = new File(featureDir, "../../plugins/" + plugin.'@id' + "_" + plugin.'@version').canonicalFile;
			boolean unpack = plugin.'@unpack' != "false";
			inContext('   in plugin ' + pluginDir.name) {
				checkGeneratedPlugin(pluginDir, unpack);
			}
		}
	}
	
	/**
	 * Check the generated plugin for common problems
	 */
	protected void checkGeneratedPlugin(File pluginDir, boolean unpack) {
		 File pluginJar = new File(pluginDir.path + '.jar');
		 boolean isDir = pluginDir.exists();
		 boolean isJar = pluginJar.exists();
		 if (isDir) {
			 if (!unpack)
				 fail('Either make plugin ' + pluginDir.name + ' a jar or remove unpack="false" from feature.xml');
			 scanFilesInDir(pluginDir);
		 }
		 else if (isJar) {
			 if (unpack)
				 fail('Either make plugin ' + pluginDir.name + ' a directory or add unpack="false" to feature.xml');
			 scanFilesInZip(pluginJar)
		 }
		 else {
			 fail('Plugin not generated: ' + pluginDir.name,
				 'For some reason, the PDE Build process does not build bundles that contain artifacts but no files to compile.'
				 + '\n   A common reason for this is the case where a plugin is only activated for specific platforms'
				 + '\n   but those platforms are not all specified in the pde.configs attribute of the parent feature\'s product.properties file.'
				 + '\n   If necessary specify "pde.configs" and "pde.groupConfigurations" in the bundle\'s build.properties file'
				 + '\n   similar to what\'s done in com.instantiations.profiler_feature/build-settings/product.properties'
				 + '\n   https://hudson.instantiations.com/hudson/job/Profiler/ws/com.instantiations.profiler_feature/build-settings/product.properties/*view*/');
		 }
	}
	
	/**
	 * Scan the directory for *.java files
	 */
	protected void scanFilesInDir(File dir) {
		inContext('   in ' + dir.name) {
			Collection sourcePathsFound = new TreeSet();
			scanFilesInDir0(dir, sourcePathsFound);
			warnSourceFound(sourcePathsFound);
		}
	}
	protected void scanFilesInDir0(File dir, Collection sourcePathsFound) {
		for (File child : dir.listFiles()) {
			if (child.isDirectory())
				scanFilesInDir0(child, sourcePathsFound);
			else if (child.name.endsWith('.jar') || child.name.endsWith('.zip'))
				scanFilesInZip(child);
			else
				checkFile(child.canonicalPath, sourcePathsFound);
		}
	}
	
	/**
	 * Scan the zip file for *.java files
	 */
	protected void scanFilesInZip(File file) {
		inContext('   in ' + file.name) {
			Collection sourcePathsFound = new TreeSet();
			ZipFile zip = new ZipFile(file);
			try {
				for (ZipEntry entry : zip.entries()) {
					if (entry.name.endsWith('.jar') || entry.name.endsWith('.zip')) {
						File exFile = new File(prop.productTemp, 'sanityCheck/scanForJava/' + entry.name);
						try {
							extractFile(zip, entry, exFile);
							scanFilesInZip(exFile);
						}
						finally {
							exFile.delete();
						}
					}
					else
						checkFile(entry.name, sourcePathsFound);
				}
			}
			finally {
				zip.close();
			}
			warnSourceFound(sourcePathsFound);
		}
	}
	
	/**
	 * Extract the file from the zip into the product temp space
	 */
	protected File extractFile(ZipFile zip, ZipEntry entry, File file) {
		file.parentFile.mkdirs();
		InputStream src = zip.getInputStream(entry);
		try {
			OutputStream dst = new FileOutputStream(file);
			try {
				byte[] buf = new byte[4096];
				while (true) {
					int count = src.read(buf);
					if (count == -1)
						break;
					dst.write(buf, 0, count);
				}
			}
			finally {
				dst.close();
			}
		}
		finally {
			src.close();
		}
	}
	
	/**
	 * Warn that source code was found
	 */
	protected checkFile(String path, Collection sourcePathsFound) {
		if (path == null || path.length() == 0)
			return;
		path = path.replace('\\', '/');
		
		// Check for invalid files
		if (path.endsWith('Thumbs.db')) {
			warn('Unnecessary file: ' + path);
			return;
		}
		
		// Check for allowed java source code
		if (path.endsWith('.java')) {
			for (String srcPrefix : prop.getList('product.include.source')) {
				if (srcPrefix.length() == 0)
					continue;
				if (srcPrefix == '*' || path.indexOf(srcPrefix) != -1)
					return;
			}
			if (sourcePathsFound == null)
				sourcePathsFound = new TreeSet(20);
			sourcePathsFound.add(path);
		}
	}
	
	/**
	 * If source was found then append to the warning log and echo to the console
	 */
	protected warnSourceFound(Collection sourcePathsFound) {
		if (sourcePathsFound == null || sourcePathsFound.size() == 0)
			return;
		StringBuffer buf = new StringBuffer(1000);
		buf.append('Found ' + sourcePathsFound.size() + ' source files');
		int count = 0;
		for (String path : sourcePathsFound) {
			count++;
			if (count > 25) {
				buf.append('\n   ...');
				break;
			}
			buf.append('\n   ');
			buf.append(path);
		}
		warn(buf.toString(), 
			'Remove the source from the product or add a comma separated list of wildcard expressions'
			+ '\n   to product.properties indicating source that may be included.'
			+ '\n   For example:'
			+ '\n      product.include.source = com/instantiations/example/, com/designer/example/'
			+ '\n   or for tests where it does not matter what source is included:'
			+ '\n      product.include.source = *');
		sourcePathsFound = null;
	}
	
	//===========================================================
	// Test
	
	public static void main(String[] args) {
		File tempDir = new File(System.getProperty("java.io.tmpdir"), 'test/' + SanityCheckFeature.class.getName());
		SanityCheckFeature sanityCheck = new SanityCheckFeature();
		BuildProperties prop = new BuildProperties();
		prop.set('build.temp', tempDir.parent);
		prop.set('product.name', tempDir.name);
		prop.set('product.version', '1.2.3');
		prop.set('build.artifacts', 'E:/build/artifacts');
		prop.set('product.include.source', 'com.instantiations.example.*');
		sanityCheck.setProp(prop);
		sanityCheck.check('test Scan for Java files') {
			File fileToScan = new File('E:/build/out/CodeProCore/continuous/v6.1.0_200905052135/CodeProCore_v6.1.0_for_Eclipse3.4.zip');
			sanityCheck.scanFilesInZip(fileToScan);
			println('=======================================================')
			fileToScan = new File('E:/build/out/CodeProCore/continuous/v6.1.0_200905052135/CodeProCore_v6.1.0_for_Eclipse3.4');
			sanityCheck.scanFilesInDir(fileToScan);
		}
		println('Scan Complete');
	}
}
