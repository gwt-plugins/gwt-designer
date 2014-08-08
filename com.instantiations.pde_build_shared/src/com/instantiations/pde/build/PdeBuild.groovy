package com.instantiations.pde.build

import org.apache.tools.ant.BuildException
import groovy.xml.MarkupBuilderimport groovy.text.SimpleTemplateEngine
import com.instantiations.pde.build.util.BuildPropertiesimport com.instantiations.pde.build.util.RcpBuildUtil;
import com.instantiations.pde.build.util.Version
import com.instantiations.pde.build.preprocessor.FeatureProjectPreprocessor
import com.instantiations.pde.build.preprocessor.ProductFilePreprocessor
import com.instantiations.pde.build.preprocessor.PluginProjectPreprocessor
import com.instantiations.pde.build.subproduct.SubProduct;
import com.instantiations.pde.build.subproduct.SubProductManager;
import com.instantiations.pde.build.util.FileDownloader
import com.instantiations.pde.build.util.ProductDownloader;import org.apache.tools.ant.taskdefs.WaitForimport com.instantiations.pde.build.external.EclipseRunnerimport java.nio.channels.IllegalSelectorExceptionimport com.objfac.prebop.Preprocessorimport com.instantiations.pde.build.preprocessor.PluginProjectPreprocessorimport com.instantiations.pde.build.util.OemVersionimport com.instantiations.pde.build.preprocessor.BundleManifestReader
import java.util.List;
import java.util.regex.Patternimport com.instantiations.pde.build.subproduct.SubProductZipReader
import java.io.File;
import java.lang.Systemimport com.instantiations.pde.build.util.FeatureVersionZipReaderimport com.instantiations.pde.build.preprocessor.FeatureXmlImportPreprocessor/**
 * Wrapper for the PDE build process.
 * There is typically one instance of this class used in an RCP build
 * but potentially many instances of this class used when building a feature
 * that is to be delivered against multiple versions of Eclipse.
 * 
 * Under the specified "pdeTemp", several directories are created
 * 		pdeTemp/runtime = the eclipse used to execute the PDE Build
 * 		pdeTemp/target  = the eclipse against which features and plugins are built
 * 		pdeTemp/build   = the features and plugins to be built
 */
public class PdeBuild extends AbstractEclipseBuild
{
	// Initialize these properties when object is instantiated
	Version eclipseRuntimeVersion;	// The version of Eclipse used to execute the PDE build scripts
	OemVersion targetVersion;		// The Oem name and the version of Eclipse against which the source will be built
	File              sourceDir;	// The directory containing source projects to be built
	FileDownloader    fileCache;
	ProductDownloader productCache;
	// Only necessary if you want to track referenced projects
	Collection<String> referencedProjNames;	// Names of source projects that are used in the build process
	// Optional... Initialized automatically if not already initialized
	File pdeTemp;					// The directory in which the PDE Build takes place
	// Set true if target should be unzipped immediately rather than as part of PDE Build Process
	boolean unzipTargetNow = false;	// true = unzip immediately, false = unzip as part of PDE Build Process
	// Initialized as necessary
	SubProductManager subproductManager;
	SubProduct subproduct;
	Collection<String> requiredSubProductNames;
	FeatureVersionZipReader featureVersions;

	// Initialized as necessary and consumed in launchBuild
	StringBuffer preBuildTasks;
	StringBuffer postBuildTasks;
	
	// Initialized in the launchBuild method
	File buildOut;					// Contains build zips and logs
	File productFile;				// the location of the product File for RCP builds
	
	// Used in scanLineFromLogInError
	protected static final Pattern CANNOT_RESOLVE_JOBS_PATTERN = Pattern.compile('^\\s*\\[javac\\]\\s+\\S+(jobs|Job|WorkspaceJob)\\s+cannot be resolved to a type');

	/**
	 * Initialize the pdeTemp directory if it is not already initialized.
	 * This is automatically called as needed.
	 */
	public void initTemp() {
		if (pdeTemp == null) {
			pdeTemp = new File(prop.productTemp, targetVersion.toString());
			buildOut = new File(pdeTemp, 'build/eclipse/' + prop.get('pde.buildLabel'));

			addPreBuildTask('<echo>ant.home = ${ant.home}</echo>');
			addPreBuildTask('<echo>java.home = ${java.home}</echo>');
			addPreBuildTask('<echo>javacSource = ${javacSource}</echo>');
			addPreBuildTask('<echo>javacTarget = ${javacTarget}</echo>');
			addPreBuildTask('<echo>bootclasspath = ${bootclasspath}</echo>\n');
			addPreBuildTask('<echo>baseos = ${baseos}</echo>');
			addPreBuildTask('<echo>basews = ${basews}</echo>');
			addPreBuildTask('<echo>basearch = ${basearch}</echo>\n');
			addPreBuildTask('<echo>os = ${os}</echo>');
			addPreBuildTask('<echo>ws = ${ws}</echo>');
			addPreBuildTask('<echo>arch = ${arch}</echo>\n');
			
			// Extract the generated zip if necessary
			if (prop.isTrue('pde.groupConfigurations')) {
				File pdeZip = new File(buildOut, prop.productId + '-' + prop.get('pde.buildId') + '-group.group.group.zip');
				addPostBuildTask('<mkdir dir="' + getFeatureOutputDir().parentFile + '" />')
				addPostBuildTask('<unzip src="' + pdeZip + '"');
				addPostBuildTask('      dest="' + getFeatureOutputDir().parentFile + '" />\n');
			}
		}
		if (subproductManager == null) {
			subproductManager = new SubProductManager(
				targetVersion:	targetVersion,
				productCache:	productCache,
				prop:			prop);
			subproduct = subproductManager.newSubProduct(prop.productName);
			requiredSubProductNames = new HashSet<String>();
			featureVersions = new FeatureVersionZipReader();
		}
	}
	
	/**
	 * Delete the temporary directory
	 */
	public void deletePdeTemp() {
		initTemp();
		ant.delete(dir:pdeTemp);
	}
	
	/**
	 * Expand the *.zip, *.tar, or *.tar.gz file
	 * into the PDE Runtime directory 
	 */
	public void unzipRuntime(File file) {
		initTemp();
		unzip(file, new File(pdeTemp, "runtime"));
	}
	
	/**
	 * Expand the *.zip, *.tar, or *.tar.gz file
	 * into the PDE Target directory 
	 * Typically, subproducts would be unzipped using
	 * unzipTargetExtra instead.
	 */
	public void unzipTarget(File file) {
		featureVersions.readZip(file);
		unzipIntoPdeTemp(file, 'target');
	}
	
	/**
	 * Expand the *.zip, *.tar, or *.tar.gz file
	 * into the PDE Target Eclipse directory 
	 */
	public void unzipTargetExtra(File file) {
		featureVersions.readZip(file);
		unzipIntoPdeTemp(file, 'target/eclipse');
	}
	
	/**
	 * Extract an Ant jar file out of the specified subproduct's plugin
	 * into the "extra-classpath" directory of a plugin about to be built.
	 */
	public void unzipTargetExtraClasspath(String subproductName, String path, String pluginId) {
		File zipFile = productCache.downloadFile(subproductName, targetVersion);
		
		// Assert that the destination plugin already exists
		File pluginDir = new File(pdeTemp, 'build/eclipse/plugins/' + pluginId);
		if (!pluginDir.exists())
			throw new BuildException(
				'Destination plugin does not exist: ' + pluginId 
				+ '\n      ' + pluginDir.canonicalPath);
		
		// Extract the file
		File extraClasspathDir = new File(pluginDir, '/extra-classpath');
		extraClasspathDir.mkdirs();
		ant.unzip(src: zipFile, dest: extraClasspathDir) {
			patternset {
				include(name: path);
			}
			mapper(type: 'flatten');
		}
		
		// Assert that the file was indeed extracted
		int indexOfLastSlash = path.lastIndexOf('/');
		String fileName = indexOfLastSlash > -1 ? path.substring(indexOfLastSlash + 1) : path;
		File jarFile = new File(extraClasspathDir, fileName);
		if (!jarFile.exists())
			throw new BuildException(
				'Failed to extract ' + fileName + ' from ' + subproductName + '/' + path 
				+ '\n      ' + jarFile.canonicalPath);
	}
	
	/**
	 * Answer the target "eclipse" directory
	 */
	public File getEclipseTargetDir() {
		initTemp();
		return new File(pdeTemp, "target/eclipse");
	}
	
	/**
	 * Expand the *.zip, *.tar, or *.tar.gz file
	 * into the PDE Target directory 
	 */
	public void unzipIntoPdeTemp(File file, String relPath) {
		
		// Either unzip immediately ...
		if (unzipTargetNow) {
			unzipIntoPdeTempNow(file, relPath);
		}

		// ... or as part of the PDE Build Process
		else {
			File dir = new File(pdeTemp, relPath);
			addPreBuildTask('<mkdir  dir=\"' + dir.canonicalPath + '\"/>');
			if (file.getName().endsWith(".zip"))
				addPreBuildTask('<unzip dest=\"' + dir.canonicalPath + '\"');
			else if (file.getName().endsWith(".tar.gz") || file.getName().endsWith(".tgz"))
				addPreBuildTask('<untar dest=\"' + dir.canonicalPath + '\"' + ' compression=\"gzip\"');
			else if (file.getName().endsWith(".tar"))
				addPreBuildTask('<untar dest=\"' + dir.canonicalPath + '\"');
			else {
				throw new BuildException(
					"Cannot expand file: " + file.getName()
					+ "\n   Modify " + getClass().getName() + " to support this file type"
					+ "\nStack Trace");
			}
			addPreBuildTask('        src=\"' + file.canonicalPath + '\"/>\n');
		}
	}
	
	/**
	 * Expand the *.zip, *.tar, or *.tar.gz file
	 * into the PDE Target directory immediately rather than part of the PDE Build Process
	 */
	public void unzipIntoPdeTempNow(File file, String relPath) {
		initTemp();
		unzip(file, new File(pdeTemp, relPath));
	}
	
	/**
	 * Append ant code to the task buffer
	 */
	public void addPreBuildTask(String line) {
		if (preBuildTasks == null) {
			preBuildTasks = new StringBuffer(4000);
			preBuildTasks.append('-->\n\n');
		}
		preBuildTasks.append('\t\t');
		preBuildTasks.append(line);
		preBuildTasks.append('\n');
	}
	
	/**
	 * Append ant code to the task buffer
	 */
	public void addPostBuildTask(String line) {
		if (postBuildTasks == null) {
			postBuildTasks = new StringBuffer(4000);
			postBuildTasks.append('-->\n\n');
		}
		postBuildTasks.append('\t\t');
		postBuildTasks.append(line);
		postBuildTasks.append('\n');
	}
	
	/**
	 * Copy and preprocess the feature and any plugins contained by the feature
	 * from the sourceDir to the build/eclipse/features and plugins directory.
	 * Download (if necessary) and unzip any subproducts required by this feature.
	 * The feature project must reside in the source directory and have the name 
	 * "[id]_feature" where "[id]" is the specified feature identifier.
	 */
	public boolean copyFeatureBasedProduct(String id) {
		if (!copyFeature(id))
			return false;
		convertProductFile(true);
		return true;
	}
	
	/**
	 * Copy and preprocess the feature and any plugins contained by the feature
	 * from the sourceDir to the build/eclipse/features and plugins directory.
	 * Download (if necessary) and unzip any subproducts required by this feature.
	 * The feature project must reside in the source directory and have the name 
	 * "[id]_feature" where "[id]" is the specified feature identifier.
	 */
	public boolean copyFeature(String id) {
		initTemp();
		File srcProj = new File(sourceDir, id + "_feature");
		File dstProj = new File(pdeTemp, "build/eclipse/features/" + id);
		if (referencedProjNames != null)
			referencedProjNames.add(srcProj.name);
		if (!srcProj.exists())
			return false;

		// Copy the feature project
		ant.copy(todir: dstProj) {
			fileset(dir: srcProj)
		}
		convertOemFiles(dstProj);

		// Preprocess the feature
		FeatureProjectPreprocessor processor = new FeatureProjectPreprocessor(
			targetVersion,
			prop.productVersion + "." + prop.getBuildQualifier(targetVersion),
			prop
		);
		processor.processFeatureManifest(dstProj);
		File productFile = new File(dstProj, prop.productName + ".product");
		if (productFile.exists())
			processor.processProductFile(productFile);
		
		// Read the feature manifest
		boolean result = true;
		Node feature = new XmlParser().parse(new File(dstProj, "feature.xml"));
		
		// Recursively copy any included features
		for (child in feature.includes) {
			if (!copyFeature(child.'@id'))
				result = false;
		}
		
		// Scan the feature manifest for dependency information
		subproduct.scanFeature(feature);
		
		// Copy included plugin projects
		Collection<String> requiredPlugins = new HashSet<String>();
		for (plugin in feature.plugin) {
			boolean isJar = plugin.'@unpack' == 'false';
			if (!copyPlugin(plugin.'@id', isJar, requiredPlugins))
				result = false;
		}
		
		// Check references in feature manifests for Eclipse 3.4 and greater
		if (targetVersion.version >= Version.V_3_4)
			checkReferencedFeatures(feature, requiredPlugins);
		
		return result;
	}
	
	/**
	 * Check the required features against the list of features referenced by this feature
	 * and warn if required features are missing from the feature.xml
	 */
	protected void checkReferencedFeatures(Node feature, Collection<String> requiredPlugins) {
		Collection<String> requiredFeatures = subproductManager.getFeaturesContainingPlugins(requiredPlugins);
		requiredFeatures.remove(feature.'@id');
		Collection<String> missing = new HashSet<String>(requiredFeatures);
		Collection<String> plugins = new HashSet<String>();
		
		for (Node req : feature.'requires') {
			for (Node imp : req.'import') {
				String id = imp.'@feature';
				if (id != null && id.length() > 0) {
					
					// If a feature is referenced but not required, then it is unnecessary
					if (!requiredFeatures.contains(id)) {
						warn('In the ' + targetVersion + ' build, feature ' + feature.@id + ' has '
							+ '\n      an unnecessary reference to feature ' + id);
					}
					
					// If a feature is referenced a 2nd time, then it is a duplicate
					else if (!missing.remove(id)) {
						warn('In the ' + targetVersion + ' build, feature ' + feature.@id + ' has '
							+ '\n      a duplicate reference to feature ' + id);
					}
					
					// References to Eclipse and other 3rd party plugins should specify the least common version, not "0.0.0"
					else if (id.startsWith('org.eclipse.')) {
						if (imp.'@version'.startsWith('0.0.0'))
							warn('In the ' + targetVersion + ' build, feature ' + feature.@id + ' references feature ' + id + ' with version "0.0.0".'
								+ '\n   It should NOT specify "0.0.0" but rather the least common version for all builds of this feature, as in'
								+ '\n      <import feature="' + id + '" version="3.2.0" match="greaterOrEqual"/>');
					}
					
					// References to our own plugins should specify "0.0.0" so that they can be replaced at build time
					else {
						if (imp.'@version' != '0.0.0')
							warn('In the ' + targetVersion + ' build, feature ' + feature.@id + ' references feature ' + id + ' with version "' + imp.'@version' + '".'
								+ '\n   Instead, it should specify version "0.0.0", so that the version will be replaced at built time, as in'
								+ '\n      <import feature="' + id + '" version="0.0.0" match="greaterOrEqual"/>');
					}
				}
				id = imp.'@plugin';
				if (id != null && id.length() > 0) {
					plugins.add(id);
				}
			}
		}
		
		if (missing.size() > 0) {
			String msg = 'In the ' + targetVersion + ' build, feature ' + feature.@id + ' is missing references to required features:';
			for (String id : new TreeSet<String>(missing))
				msg += '\n   ' + id
			warn(msg, 'Add required features built by us with "0.0.0" so that the version will be replaced at built time, as in'
				+ '\n      <import feature="com.instantiations.eclipse.shared" version="0.0.0" match="greaterOrEqual"/>'
				+ '\n   and add required Eclipse and other 3rd party features with the lowest common version, as in'
				+ '\n      <import feature="org.eclipse.gef" version="3.2.0" match="greaterOrEqual"/>');
		}
		
		if (plugins.size() > 0) {
			String msg = 'In the ' + targetVersion + ' build, feature ' + feature.@id + ' unnecessarily references the following plugins:';
			for (String id : new TreeSet<String>(plugins))
				msg += '\n   ' + id
			warn(msg);
		}
	}
	
	/**
	 * Answer the build "eclipse" directory 
	 * that contains the source to be built
	 */
	public File getEclipseBuildDir() {
		initTemp();
		return new File(pdeTemp, "build/eclipse");
	}
	
	/**
	 * Copy and preprocess the plugin from the sourceDir to the build/eclipse/plugins directory.
	 * The plugin project must reside in the source directory
	 * and have a name of "[id]" where "[id]" is the specified plugin identifier.
	 */
	public boolean copyPluginBasedProduct(String id, boolean isJar, Collection<String> requiredPlugins) {
		if (!copyPlugin(id, isJar, requiredPlugins))
			return false;
		convertProductFile(false);
		return true;
	}
	
	/**
	 * Copy and preprocess the plugin from the sourceDir to the build/eclipse/plugins directory.
	 * The plugin project must reside in the source directory
	 * and have a name of "[id]" where "[id]" is the specified plugin identifier.
	 */
	public boolean copyPlugin(String id, boolean isJar, Collection<String> requiredPlugins) {
		initTemp();
		File srcProj = new File(sourceDir, id);
		File dstProj = new File(pdeTemp, "build/eclipse/plugins/" + id);
		
		if (referencedProjNames != null)
			referencedProjNames.add(srcProj.name);
		if (!srcProj.exists())
			return false;
		// Copy the plugin project
		ant.copy(todir: dstProj) {
			fileset(dir: srcProj) {
				exclude(name: 'bin*/**');
			}
		}

		convertOemFiles(dstProj);
		// Preprocess the plugin project
		PluginProjectPreprocessor processor = new PluginProjectPreprocessor(
			targetVersion,
			prop.productVersion + "." + prop.getBuildQualifier(targetVersion),
			prop
		);
		processor.processManifest(dstProj);
		processor.processPluginProperties(dstProj);
		processor.processFragmentProperties(dstProj)
		processor.processBuildProperties(dstProj);
		processor.processAboutMappings(dstProj);
		processor.processAboutProperties(dstProj);
		processor.processAboutHtml(dstProj);
		File classpathFile = new File(dstProj, ".classpath");
		if (classpathFile.exists()) {
			Node classpath = new XmlParser().parse(classpathFile);
			for (entry in classpath.classpathentry)
				if (entry.'@kind' == 'src' && entry.'@combineaccessrules' != 'false')
					processor.processSource(new File(dstProj, entry.'@path'));
		}
		
		// Scan the feature manifest for dependency information
		SubProductZipReader reader = subproduct.scanPlugin(dstProj);
		requiredPlugins.addAll(reader.requiredPlugins);
		
		// Ensure that the appropriate JDK is downloaded and available
		String executionEnvironment = processor.getExecutionEnvironment();
		if (executionEnvironment != null) {
			Version jdkVersion = prop.getVersion('pde.' + executionEnvironment + '.jdk.version');
			fileCache.getJavaHome(jdkVersion);
		}
		
		// Old code that copies and creates bundles after the PDE Build process completes
//		Properties buildProp = new Properties();
//		new File(dstProj, 'build.properties').withReader { reader -> buildProp.load(reader); }
//		if (buildProp.get('build.bundle.dir') == 'true') {
//			BundleManifestReader manifest = new BundleManifestReader();
//			manifest.process(new File(dstProj, 'META-INF/MANIFEST.MF'));
//			File bundleOut = new File(getFeatureOutputDir(), 'plugins/' + id + '_' + manifest.version);
//			addPostBuildTask('<!-- The PDE Build Process does not seem to generate bundle ' + id + ' -->');
//			addPostBuildTask('<!-- so we build the bundle after the PDE Build Process completes -->');
//			addPostBuildTask('<mkdir dir="' + bundleOut.canonicalPath + '"/>');
//			addPostBuildTask('<copy todir="' + bundleOut.canonicalPath + '">');
//			addPostBuildTask('\t<fileset dir="' + dstProj.canonicalPath + '">');
//			for (String path : buildProp.get('bin.includes').split(','))
//				addPostBuildTask('\t\t<include name="' + (path.endsWith('/') ? path + '**' : path) + '"/>');
//			addPostBuildTask('\t</fileset>');
//			addPostBuildTask('</copy>\n');
//		}

		// If this is a directory based plugin, then zip up and remove all directories 
		// specified by the "product.doc.zip" build property into a doc.zip file 
		if (!isJar) {
			File buildPropFile = new File(dstProj, 'build.properties');		
			Properties buildProp = new Properties();
			buildPropFile.withReader{ rr -> 
				buildProp.load(rr); 
			};
			String data = buildProp.'bin.includes';
			List binIncludes = [];
			if (data != null) {
				String[] binInclArray = data.trim().split(',');
				for (String item in binInclArray) {
					binIncludes.add(item.trim());
				}
			}
			List docDirs = prop.getList('product.doc.zip');
			
			List <File> dirsToZip = [];
			for (String docDirName in docDirs) {
				if (binIncludes.contains(docDirName)) {
					File docDir = new File(dstProj, docDirName);
					if(docDir.exists()) {
						dirsToZip += docDir;
						binIncludes.remove(docDirName);
					}
				}
			}
			
			if (!dirsToZip.isEmpty()) {
				File docZipFile = new File(dstProj, 'doc.zip');
				
				ant.zip(destfile: docZipFile) {
					for(File dir in dirsToZip) {
						zipfileset(dir: dir.parent) {
							include(name: dir.name + '/**')
						}
					}
				}
				ant.delete(includeemptydirs: true) {
					for(File dir in dirsToZip) {
						fileset(dir: dir, defaultexcludes: false);
					}
				}
				binIncludes += 'doc.zip';
				data = '';
				binIncludes.every {item -> data += "${item},"};
				buildProp.putAt('bin.includes', data.substring(0,data.length()-1 ));
				buildPropFile.withWriter{ rw -> 
					buildProp.store(rw, "build properties file for plugin.  modified to remove the documentation directories and add them to doc.zip"); 
				};
			}
		}
		
		return true;
	}

	/**
	 * convert files that start with OemName-* to *
	 */
	protected void convertOemFiles(File srcDir) {
		// only process if there is an oem name
		if (targetVersion.getOemName() != null) {
			List<File> files = [];
			
			// Build collection of files to be checked
			files.addAll(srcDir.listFiles().collect { it });
			List<File> contents = new File(srcDir, 'META-INF').listFiles();
			if (contents != null) {
				files.addAll(contents);
			}
			
			// Find files starting with OEM name followed by a dash
			List<File> oemFiles = [];
			for (File file in files) {
				println ("processing file = $file");
				if (!file.isDirectory()) {
					if (file.name.startsWith(targetVersion.getOemName() + '-')) {
						oemFiles.add(file);
					}
				}
			}
			
			for (File oemFile in oemFiles) {
				String fileName = oemFile.name.substring(targetVersion.getOemName().length() + 1);
				File newFile = new File(oemFile.parentFile, fileName);
				ant.copy(file: oemFile.canonicalPath, toFile: newFile.canonicalPath, overwrite: true);
			}
		}
	}

	/**
	 * Convert and validate the *.product file
	 */
	protected void convertProductFile(boolean useFeatures) {
		
		productFile = RcpBuildUtil.productFile(prop, getEclipseBuildDir(), false);
		File altProdFile = RcpBuildUtil.alternateProductFile(prop, getEclipseBuildDir(), targetVersion.getVersion(), false);
		if (altProdFile.exists()) {
			printf('copying %s%n     to %s%n', altProdFile.getCanonicalPath(), productFile.getCanonicalPath());
			ant.copy(file: altProdFile, tofile: productFile, overwrite: true);
		}
		else if (!productFile.exists())
			throw new RuntimeException('Failed to find product file defined by ${product.rcp.file} build property:\n\t' +
										'property values:\n\t\t' +  
										'product.rcp.file    = ' + prop.get('product.rcp.file') + '\n\t' +
										'files Searched were\n\t\tbase product file name: ' + productFile.path + '\n\t\t' +
										'             altername: ' + altProdFile.path);
	}

	/**
	 * Unzip the required SubProducts
	 */
	public void unzipRequiredSubProducts(def overrideMap = null) {
		subproductManager.addSubProduct(subproduct);
		Collection<String> allSubProducts = 
			new TreeSet<String>(subproductManager.getAllDependencies(subproduct.name, overrideMap));
		println('Required SubProducts as determined by the subproduct manager');
		for (String required : allSubProducts)
			println('   ' + required);
		for (String required : allSubProducts)
			unzipSubProduct(required);
	}
	
	/**
	 * Unzip the specified SubProduct
	 */
	public void unzipSubProduct(String name) {
		if (!requiredSubProductNames.contains(name)) {
			println('Unzipping Subproduct ' + name);
			unzipTargetExtra(productCache.downloadFile(name, targetVersion));
			requiredSubProductNames.add(name);
		}
	}
	
	/**
	 * Generate "target.properties" files in all source plugins
	 * and adjust the build.properties to include the new files
	 */
	public void generateTargetProperties() {
		File tmpFile = new File(pdeTemp, 'target.properties');
		tmpFile.write('eclipse.version=' + targetVersion.version);
		Collection featuresAndPlugins = new ArrayList(20);
		featuresAndPlugins.addAll(Arrays.asList(new File(pdeTemp, 'build/eclipse/features').listFiles()));
		List plugins = new File(pdeTemp, 'build/eclipse/plugins').listFiles();
		if (plugins != null) {  // if this is null then the feature being built has no plug-ins so this step can be safely skipped
			featuresAndPlugins.addAll(Arrays.asList(new File(pdeTemp, 'build/eclipse/plugins').listFiles()));
		}
		for (File child : featuresAndPlugins) {
			ant.copy(file: tmpFile, todir: child);
			File buildPropFile = new File(child, 'build.properties');
			Properties buildProp = new Properties();
			buildPropFile.withReader { reader -> buildProp.load(reader); }
			String includes = buildProp.get('bin.includes');
			if (includes == null || includes.trim().length() == 0)
				includes = tmpFile.name;
			else
				includes += ',' + tmpFile.name;
			buildProp.put('bin.includes', includes);
			buildPropFile.withWriter { writer -> buildProp.store(writer, 'Modified to include ' + tmpFile.name); }
		}
		tmpFile.delete();
	}
	
	/**
	 * When an error occurs, this method is called by printLogsInError to scan
	 * each line in the log looking for specific patterns that might better indicate
	 * the cause of the build failure.
	 * @return true if this is the last line that should be echoed
	 */
	protected boolean scanLineFromLogInError(String line, ArrayList<String> lastLines) {
		if (line.indexOf('Unable to find plug-in: ') != -1) {
			lastLines.add('   It may be that the plugin listed above is present');
			lastLines.add('   and instead some prerequisite of that plugin is missing.');
			lastLines.add('   Start by checking that all prerequisite subproducts are in the following list:');
			lastLines.add('   ' + requiredSubProductNames);
			return true;
		}
		if (processingCompilationError) {
			if (CANNOT_RESOLVE_JOBS_PATTERN.matcher(line).find()) {
				compilationErrors.add('    [javac] \tIf this plugin using the Eclipse Jobs API and is compiled against Eclipse 2.1');
				compilationErrors.add('    [javac] \tthen you must explicitly list "org.eclipse.core.jobs" as a required bundle in the MANIFEST.MF');
				compilationErrors.add('    [javac] \tso that the build system will replace "org.eclipse.core.jobs" with "com.instantiations.eclipse.jobs" when building against Eclipse 2.1.');
			}
		}
		return super.scanLineFromLogInError(line, lastLines);
	}
	
	/**
	 * Answer the "eclipse" directory containing the generated "features" and "plugins" directory
	 */
	public File getFeatureOutputDir() {
		return new File(pdeTemp, "build/eclipse/tmp/eclipse");
	}
	
	/**
	 * Answer the name of the feature output file
	 */
	public String getFeatureOutputZipFileName() {
		String targetName = targetVersion.oemName;
		if (targetName == null)
			targetName = 'Eclipse';
		return prop.productName + '_v' + prop.productVersion + '_for_' + targetName + targetVersion.version + '.zip';
	}
	
	/**
	 * Delete the following files after the PDE Build Process is complete.
	 * This method takes a collection of Ant patterns specifying the files to be deleted.
	 * For example, to delete a particular file in a plugin,
	 * 		['plugins/my.plugin.id/mydir/myfile.txt']
	 * or to delete an entire feature
	 * 		['features/my.feature.id_**']
	 */
	public void deleteAfterBuild(Collection<String> patterns) {
		if (patterns == null || patterns.empty)
			return;
		addPostBuildTask('<delete includeEmptyDirs=\"true\">');
		addPostBuildTask('\t<fileset dir=\"' + getFeatureOutputDir().canonicalPath + '\">');
		for (String eachPattern : patterns)
			addPostBuildTask('\t\t<include name=\"' + eachPattern + '\"/>');
		addPostBuildTask('\t</fileset>');
		addPostBuildTask('</delete>\n');
	}
	
	/**
	 * Zip the Feature build output after the PDE Build Process is complete.
	 */
	public void zipFeatureOutputAfterBuild() {
		
		// Zip the build output without any dependencies and without the "eclipse" directory prefix
		File featureZip = productCache.localFile(targetVersion);
		addPostBuildTask('<mkdir    dir=\"' + featureZip.parentFile.canonicalPath + '\"/>');
		addPostBuildTask('<zip destfile=\"' + featureZip.canonicalPath + '\"');
		addPostBuildTask('      basedir=\"' + getFeatureOutputDir().canonicalPath + '\"/>\n');
		
		// Zip the feature along with its dependencies
		// Suppress the generation of the product zip file by specifying "product.zip = false" in the product.properties file
		if (prop.isTrue('product.zip')) {
			File productZip = new File(prop.productOut, getFeatureOutputZipFileName());
			addPostBuildTask('<mkdir    dir=\"' + productZip.parentFile.canonicalPath + '\"/>');
			addPostBuildTask('<zip destfile=\"' + productZip.canonicalPath + '\">');
			addPostBuildTask('\t<zipfileset src=\"' + featureZip.canonicalPath + '\"/>');
			for (String required : requiredSubProductNames) {
				// add all subproducts except PDE to the zip file
				if (!required.equals('PDE') && !required.equals('GEF')) {
					addPostBuildTask('\t<zipfileset src=\"' + productCache.downloadFile(required, targetVersion).canonicalPath + '\"/>');
				}
			}
			addPostBuildTask('</zip>\n');
			if (prop.isTrue('build.create.checksum')) {
				addPostBuildTask('<checksum file=\"' + productZip.canonicalPath + 
									'\" todir=\"' + productZip.parentFile.canonicalPath +
									'\" format=\"MD5SUM\" algorithm=\"MD5\" />')
			}	
		}
	}
	
	/**
	 * Copy the Feature build output after the PDE Build Process is complete
	 */
	public void copyFeatureOutputAfterBuild(File dir) {
		addPostBuildTask('<mkdir  dir=\"' + dir.canonicalPath + '\"/>');
		addPostBuildTask('<copy todir=\"' + dir.canonicalPath + '\">');
		addPostBuildTask('\t<fileset dir=\"' + getFeatureOutputDir().canonicalPath + '\"/>');
		addPostBuildTask('</copy>\n');
	}
	
	/**
	 * Update the version #s in a feature import
	 */
	public void updateFeatureImports() {
		FeatureXmlImportPreprocessor preprocessor = new FeatureXmlImportPreprocessor(targetVersion.version, featureVersions.getFeatureVersions());
		preprocessor.processAllFeatureManifests(new File(pdeTemp, 'build/eclipse/features'));
	}
	
	/**
	 * Spawns a PDE Build process to build and RCP application
	 * and returns before the process has completed
	 */
	public void launchRcpBuild() {
		launchBuild("build-product.xml", true);
	}
	
	/**
	 * Spawns a PDE Build process to build a feature and associated plugins
	 * and returns before the process has completed
	 */
	public void launchFeatureBuild() {
		
		// Update imported feature references right before launch
		updateFeatureImports();
		
		launchBuild("build-feature.xml", false);
	}

	/**
	 * Spawns a PDE Build process
	 * and returns before the process has completed
	 */
	private void launchBuild(String templateBuildFilename, boolean zipOutput) {
		initTemp();
		
		// Ensure that only one build is launched
		if (runner != null)
			throw new IllegalStateException("Build has already been launched");
		
		// Add a sanity check to ensure files were unzipped properly into the Eclipse target
		addPreBuildTask('<!-- Sanity check that files were unzipped properly into the target Eclipse -->');
		addPreBuildTask('<available file="' + getEclipseTargetDir().parent + '/plugins" property="unzip-target-failed"/>');
		addPreBuildTask('<fail if="unzip-target-failed">');
		addPreBuildTask('Failed to properly unzip one or more files into the target Eclipse directory, ');
		addPreBuildTask('possibly because unzipTarget(...) was called instead of unzipTargetExtra(...) or vise-versa.');
		addPreBuildTask('</fail>\n');
		
		// The com.instantiations.pde_build_debug EarlyStartup class assumes 
		// the relative locations of eclipseHome, workspace, and builder
		// so any changes here must be reflected there as well.
		File eclipseHome    = new File(pdeTemp, "runtime/eclipse");
		File workspace      = new File(pdeTemp, "runtime/workspace");
		File base           = new File(pdeTemp, "target");
		File buildDirectory = new File(pdeTemp, "build/eclipse");
		//   buildOut       = initialized in initTemp
		File builder        = new File(pdeTemp, "build/scripts");
		     runner 		= new EclipseRunner(targetVersion + " PDE Build", eclipseHome);
		File pdeHome        = runner.findPlugin("org.eclipse.pde.build");

		// Sanity check
		if (pdeHome == null)
			throw new BuildException("Cannot find org.eclipse.pde.build plugin in " + eclipseHome.canonicalPath);

		// Copy any top level custom scripts and build properties to this directory
		builder.mkdirs();
		
		// Build the Ant property file that is loaded by the PDE Build process
		BuildProperties pdeProp = new BuildProperties(prop);
		pdeProp.set('pde.eclipse.pdebuild.home',	pdeHome.canonicalPath);
		pdeProp.set('pde.base',                     base.canonicalPath);
		pdeProp.set('pde.buildDirectory',           buildDirectory.canonicalPath);
		pdeProp.set('pde.builder',					builder.canonicalPath);
		pdeProp.set('pde.buildingOSGi',				targetVersion.version >= Version.V_3_0 ? 'true' : 'false');
		if (!zipOutput)
			pdeProp.set('pde.archivesFormat', '*,*,*-folder');
		if (targetVersion.version <= Version.V_2_1 && prop.isTrue('product.compile.e21.with.jdk13')) {
			// Ensure that JDK 1.3 is available
			fileCache.getJavaHome(Version.V_1_3);
			pdeProp.set('pde.bootclasspath', prop.getPlatformSpecific('jdk.1.3', '.classpath'));
			pdeProp.set('pde.javacSource',	'1.3');
			pdeProp.set('pde.javacTarget',	'1.1');
		}
		if (productFile != null) {
			pdeProp.set('pde.product', productFile.canonicalPath);
		}
		new File(builder, "build.properties").withOutputStream() {
			pdeProp.getPdeProperties().save(it, "Properties for the PDE Build process") }
		
		List<String> repos = prop.getList ('product.p2.update.sites');
		if (repos.size() > 0) {
			installP2UpdateSiteZips(repos);
		}

		// End build task generation
		if (preBuildTasks != null)
			preBuildTasks.append('\t\t<!--');
		else
			preBuildTasks = new StringBuffer();
		if (postBuildTasks != null)
			postBuildTasks.append('\t\t<!--');
		else
			postBuildTasks = new StringBuffer();

		// Copy the main build.xml template
		new File(prop.buildCommonHome, "templates/pde/" + templateBuildFilename).withReader { reader ->
			new File(builder, "build.xml").withWriter { writer ->
				new SimpleTemplateEngine().createTemplate(reader).make(
					pdePlugin:		pdeHome.canonicalPath,
					preBuildTasks:	preBuildTasks.toString(),
					postBuildTasks:	postBuildTasks.toString()
				).writeTo(writer);
			}
		}
		
		// Eclipse 3.1 requires customTarget.xml and genericTargets.xml
		if (eclipseRuntimeVersion <= Version.V_3_1) {
			File srcFile = new File('templates/pde/3.1/customTargets.xml');
			if (!srcFile.exists())
				srcFile = new File(prop.buildCommonHome, 'templates/pde/3.1/customTargets.xml');
			File dstFile = new File(builder, srcFile.name);
			if (!dstFile.exists()) {
				ant.copy(file: srcFile, tofile: dstFile) {
					filterset {
						filter(token: 'PRODUCT-ID',			value: prop.productId);
						filter(token: 'PRODUCT-VERSION',	value: prop.productVersion);
						filter(token: 'BUILD-NUM',			value: prop.getBuildQualifier(targetVersion));
					}
				}
			}
			srcFile = new File(prop.buildCommonHome, 'templates/pde/3.1/genericTargets.xml');
			dstFile = new File(builder, srcFile.name);
			if (!dstFile.exists()) {
				ant.copy(file: srcFile, tofile: dstFile);
			}
		}
		
		// If debugging the PDE Build process
		// then include a special plugin to initialize the PDE Build workspace for debugging purposes. 
		if (prop.isTrue("debug.pde")) {
			ant.copy(todir: new File(eclipseHome, "plugins")) {
				fileset(dir: new File(prop.buildCommonHome, "debug"));
			}
		}
		
		// Otherwise provide normal headless execution
		else {
			runner.setApplication("org.eclipse.ant.core.antRunner");
			runner.setCmdLine([
		   		//'-Dosgi.arch=x86',
   				'-Dbuilder='     + builder.canonicalPath,
				'-verbose',
				'-noinput'
			]);
		}
		
		// Use the JDK specified by the build property "pde.java.version"
		runner.setJavaExe(fileCache.getJavaExe(prop.getVersion('pde.java.version')));
		//runner.setEnvironmentVar('JAVA_HOME', fileCache.getJavaHome(prop.getVersion('pde.java.version')).canonicalPath);

		// Launch the external PDE Build process
		runner.setWorkspace(workspace);
		runner.setWorkingDir(builder);
		runner.setLogFile(new File(builder, "pde-build.log"));
		runner.launch();
	}

	/**
	 * install code that is in P2 update site format using director
     * this only works for eclipse 3.5 currently
	 */
	protected void installP2UpdateSiteZips(List<String> repos) {
		File targetEclipse = new File(pdeTemp, 'target/eclipse');
		File targetEclipseExe = null;
		if (prop.isLinux()) {
			targetEclipseExe = new File(targetEclipse, 'eclipse');
		}
		else {
			targetEclipseExe = new File(targetEclipse, 'eclipse.exe');
		}
		File targetPlugins = new File(targetEclipse, 'plugins');
		String repoLoc = null;
		StringBuffer ius = new StringBuffer('');
		for (String repoData in repos) {
			String repoName = null;
			List<String> data = repoData.split(':');
			Boolean first = true;
			File archive = null;
			for(String s in data) {
				if (first) {
					archive = fileCache.download(s, targetVersion.getVersion().toString()).canonicalFile;
					repoName = archive.name;
					first = false;
				}
				else {
					ius.append(s).append('.feature.group,')
				}
			}
			ius.setLength(ius.length() - 1);
			File tmpDir = new File(pdeTemp, 'P2_repos/' + repoName);
			repoLoc = tmpDir.toURI().toURL().toString();
			
			File directorLog = new File(pdeTemp, 'build/scripts/director-' + targetVersion.getVersion().toString() + '.log');
			String resultProperty = 'director.result.' + targetVersion.getVersion().toString();
			
			unzipIntoPdeTemp(archive, 'P2_repos/' + repoName);
			addPreBuildTask('<echo message="using           ' + targetEclipseExe + '" />');
			addPreBuildTask('<echo message="installing   to ' + targetEclipse + '" />');
			addPreBuildTask('<echo message="installing from ' + repoLoc + '" />');
			addPreBuildTask('<chmod perm="a+x" file="' + targetEclipseExe + '" />');
			addPreBuildTask('<exec executable="' + targetEclipseExe + '"');
			addPreBuildTask('\t\toutput="' + directorLog.canonicalPath + '"');
			addPreBuildTask('\t\tfailonerror="false"');
			addPreBuildTask('\t\tresultproperty="' + resultProperty + '"');
			addPreBuildTask('\t\ttaskname="Install-' + targetVersion.getVersion().toString() + '">');
			addPreBuildTask('\t<arg value="-consolelog" />');
			addPreBuildTask('\t<arg value="-debug" />');
			addPreBuildTask('\t<arg value="-application" />');
			addPreBuildTask('\t<arg value="org.eclipse.equinox.p2.director" />');
			addPreBuildTask('\t<arg value="-repository" />');
			addPreBuildTask('\t<arg value="' + repoLoc + '" />');
			addPreBuildTask('\t<arg value="-installIU" />');
			addPreBuildTask('\t<arg value="' + ius + '" />');
			addPreBuildTask('\t<arg value="-destination" />');
			addPreBuildTask('\t<arg value="' + targetEclipse + '" />');
			addPreBuildTask('\t<arg value="-profile" />');
			addPreBuildTask('\t<arg value="SDKProfile" />');
			addPreBuildTask('</exec>');
			addPreBuildTask('');
			addPreBuildTask('<echo message="error property ' + resultProperty + '" />');
			addPreBuildTask('<echo message="call to install ' + repoName + ' returned ${' + resultProperty + '}" />');
			addPreBuildTask('<echo message="log file is ' + directorLog + '" />');
			addPreBuildTask('<fail message="could not install ' + repoName + '">');
			addPreBuildTask('\t<condition>');
			addPreBuildTask('\t\t<not>');
			addPreBuildTask('\t\t\t<equals arg1="0" arg2="${' + resultProperty + '}" />');
			addPreBuildTask('\t\t</not>');
			addPreBuildTask('\t</condition>');
			addPreBuildTask('\t</fail>');
		}
	}
	/**
	 * Wait for the external PDE Build process to complete and gather the build logs.
	 * Must call collect logs before calling collectRcpOutput or collectFeatureOutput
	 */
	public void collectLogs() {
		basicCollectLogs(new File(prop.productArtifacts, targetVersion + "/pde-logs"));
	}
	
	/**
	 * If there is an error, then this method is called by basicCollectLogs()
	 * to copy the PDE Build Process temp directory to artifacts for later diagnosis
	 */
	protected void basicCopyTempInError(File logDir) {
		if (prop.isTrue('build.on.error.copy.temp')) {
			File dir = new File(prop.productArtifacts, targetVersion + "/pde-temp");
			dir.mkdirs();
			ant.copy(todir: dir) {
				fileset(dir: pdeTemp);
			}
		}
	}

	/**
	 * Called by basicCollectLogs() to copy the logs.
	 * Extend superclass implementation to collection additional logs.
	 */
	protected void basicCopyLogs(File logDir) {
		super.basicCopyLogs(logDir);
		if (buildOut.exists())
			ant.copy(todir: logDir) {
				fileset(dir: buildOut) {
					include(name: "compilelogs/**/*"); } }
	}
	
	/**
	 * Gather the RCP build output
	 * Must call collectLogs() before calling this method
	 */
	public void collectRcpOutput(File dir, String versionSuffix = null) {
		if (!logsCollected)
			throw new IllegalStateException('Must call collectLogs() first');
		
		// If versionSuffix is specified, then inject text into the file name
		// otherwise append a default suffix that includes the product version
		String vText = versionSuffix;
		if (vText == null)
			vText = '_v' + prop.productVersion;
		 
		// Copy and rename the RCP files for each platform
		dir.mkdirs();
		ant.copy(todir: dir) {
			fileset(dir: buildOut) {
				include(name: '*.zip');
			}
			globmapper(
				from: prop.get('pde.buildId') + '-*', 
				to:   prop.productName + vText + '_*');
		}
	}

	public void delete64BitPluginsBeforeLaunch() {
		// Remove the 64 bit plugins.  This is needed for plugins that do work with low level UI functions.
		// Delete the 64 bit plugins to prevent them from appearing on the classpath during compilation
		addPreBuildTask('<!-- Delete 64 bit SWT plugins so that they will not appear on the classpath -->');
		addPreBuildTask('<echo message="****** deleting the 64 bit plugins *****************" />');
		addPreBuildTask('<delete>');
		addPreBuildTask('\t<fileset dir="' + eclipseTargetDir.canonicalPath + '/plugins">');
		addPreBuildTask('\t\t<include name="org.eclipse.swt.win32.win32.x86_64_*.jar" />');
		addPreBuildTask('\t\t<include name="org.eclipse.swt.win32.win32.x86_64.source*.jar" />');
		addPreBuildTask('\t\t<include name="org.eclipse.swt.gtk.linux.x86_64_*.jar" />');
		addPreBuildTask('\t\t<include name="org.eclipse.swt.gtk.linux.x86_64.source*.jar" />');
		addPreBuildTask('\t\t<include name="org.eclipse.swt.motif.hpux.ia64_32_*.jar" />');
		addPreBuildTask('\t\t<include name="org.eclipse.swt.motif.hpux.ia64_32.source*.jar" />');
		addPreBuildTask('\t\t<include name="org.eclipse.swt.gtk.linux.s390_*.jar" />');
		addPreBuildTask('\t\t<include name="org.eclipse.swt.gtk.linux.s390.source*.jar" />');
		addPreBuildTask('\t\t<include name="org.eclipse.swt.gtk.linux.s390x_*.jar" />');
		addPreBuildTask('\t\t<include name="org.eclipse.swt.gtk.linux.s390x.source*.jar" />');
		addPreBuildTask('\t\t<include name="org.eclipse.swt.gtk.linux.ppc_*.jar" />');
		addPreBuildTask('\t\t<include name="org.eclipse.swt.gtk.linux.ppc.source*.jar" />');
		addPreBuildTask('\t\t<include name="org.eclipse.swt.gtk.linux.ppc64_*.jar" />');
		addPreBuildTask('\t\t<include name="org.eclipse.swt.gtk.linux.ppc64.source*.jar" />');
		addPreBuildTask('\t\t<include name="org.eclipse.swt.motif.linux.x86.source*.jar" />');
		addPreBuildTask('\t\t<include name="org.eclipse.swt.motif.linux.x86_*.jar" />');
		addPreBuildTask('\t\t<include name="org.eclipse.swt.cocoa.macosx.x86_64.source*.jar" />');
		addPreBuildTask('\t\t<include name="org.eclipse.swt.cocoa.macosx.x86_64_*.jar" />');
		
		/* test */

		addPreBuildTask('\t</fileset>');
		addPreBuildTask('</delete>\n');
	}
	
	/**
	 * Find textile files and convert those files into html files
	 */
	public void convertTextileToHtml() {
		
		// Define the textile Ant task
		
		addPreBuildTask('<!-- Define the textile Ant task -->');
		addPreBuildTask('<path id="textile.path">');
		addPreBuildTask('\t<fileset dir="' + prop.buildCommonHome.canonicalPath + '/lib" includes="*.jar"/>');
		addPreBuildTask('</path>');
		addPreBuildTask('<taskdef classpathref="textile.path" resource="net/java/textilej/util/anttask/tasks.properties"/>\n');

		// Run the textile Ant task on any textile files
		
		addPreBuildTask('<!-- Convert textile files to html -->');
		addPreBuildTask('<textile-to-html>');
		addPreBuildTask('\t<fileset dir="' + pdeTemp.canonicalPath + '/build/eclipse">');
		addPreBuildTask('\t\t<include name="**/*.textile"/>');
		addPreBuildTask('\t</fileset>');
		addPreBuildTask('</textile-to-html>\n');
		
		// Remove the textile files that were processed

		addPreBuildTask('<!-- Delete the textile files -->');
		addPreBuildTask('<delete>');
		addPreBuildTask('\t<fileset dir="' + pdeTemp.canonicalPath + '/build/eclipse">');
		addPreBuildTask('\t\t<include name="**/*.textile"/>');
		addPreBuildTask('\t</fileset>');
		addPreBuildTask('</delete>\n');
	}
	
	//==========================================================
	// TEST
	
//	public static void main(String[] args) {
//		ArrayList<String> lastLines = new ArrayList<String>();
//		PdeBuild builder = new PdeBuild();
//		builder.compilationErrors = new ArrayList<String>(100);
//		println('------------------------------');
//		builder.scanLineFromLogInError('foo', lastLines);
//		println(builder.compilationErrors);
//		println('------------------------------');
//		builder.scanLineFromLogInError('[javac] org.eclipse.core.runtime.jobs cannot be resolved to a type', lastLines);
//		println(builder.compilationErrors);
//		println('------------------------------');
//	}
}
