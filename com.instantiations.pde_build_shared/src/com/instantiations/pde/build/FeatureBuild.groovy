package com.instantiations.pde.build

import com.instantiations.pde.build.check.SanityCheck
import com.instantiations.pde.build.check.SanityCheckFeature
import com.instantiations.pde.build.subproduct.SubProductZipReader
import com.instantiations.pde.build.usage.UsageInfoScanner
import com.instantiations.pde.build.util.BuildProperties
import com.instantiations.pde.build.util.FileDownloader
import com.instantiations.pde.build.util.Version
import com.instantiations.pde.build.util.OemVersionimport org.apache.tools.ant.BuildException/**
 * Build a Feature
 */
public class FeatureBuild extends AbstractProductBuild
{
	List pendingSiteBuilders = new ArrayList();
	Map requiredSubProductNamesMap = new HashMap<OemVersion, Collection<String>>();
	
	/**
	 * Extend superclass information 
	 * to pull information out of the feature.xml file to be built.
	 */
	protected void readBuildProperties() {
		super.readBuildProperties();
		
		// Assert that the build version is defined in the product.properties or the feature.xml, but not both
		File projDir = new File(sourceDir, prop.productId + '_feature');
		Node feature = new XmlParser().parse(new File(projDir, 'feature.xml'));
		if (prop.isDefined('product.version') && feature.'@version' != '0.0.0')
			warn('product.version is defined in product.properties'
				+ '\n   and the version specified in ' + feature.'@id' + ' feature.xml is not "0.0.0"'
				+ '\n   Either remove product.version from the product.properties file'
				+ '\n   or set version = "0.0.0" in ' + feature.'@id' + ' feature.xml')
		
		readFeatureProperties(projDir, prop);
	}
	
	/**
	 * Read properties from the feature.xml and feature.properties files
	 * in the specified project directory
	 */
	public static void readFeatureProperties(File projDir, BuildProperties prop) {
		
		if (!projDir.exists())
			throw new BuildException('Cannot find feature project: ' + projDir.name
				+ '\n   ' + projDir.canonicalPath);
		Node feature = new XmlParser().parse(new File(projDir, 'feature.xml'));
		
		File featurePropFile = new File(projDir, 'feature.properties');
		Properties featureProp = null;
		if (featurePropFile.exists()) {
			featureProp = new Properties();
			featurePropFile.withReader { reader -> featureProp.load(reader); }
		}
		
		if (!prop.isDefined('product.id'))
			prop.set('product.id', feature.'@id');
		if (!prop.isDefined('product.version'))
			prop.set('product.version', feature.'@version');
		if (!prop.isDefined('product.title'))
			prop.set('product.title', getTranslatedText(feature.'@label', featureProp));

		if (!prop.isDefined('product.license'))
			prop.set('product.license', feature.license.text());
		if (!prop.isDefined('product.license.url')) {
			String[] urls = feature.license.'@url';
			if (urls != null && urls.length > 0) {
				String url = urls[0];
				if (url != null)
					prop.set('product.license.url', url.trim());
			}
		}
		if (!prop.isDefined('product.description'))
			prop.set('product.description', getTranslatedText(feature.description.text(), featureProp));
		if (!prop.isDefined('product.description.url')) {
			String[] urls = feature.description.'@url';
			if (urls != null && urls.length > 0) {
				String url = urls[0];
				if (url != null)
					prop.set('product.description.url', url.trim());
			}
		}
		if (!prop.isDefined('product.copyright'))
			prop.set('product.copyright', feature.copyright.text());
		if (!prop.isDefined('product.copyright.url')) {
			String[] urls = feature.copyright.'@url';
			if (urls != null && urls.length > 0) {
				String url = urls[0];
				if (url != null)
					prop.set('product.copyright.url', url.trim());
			}
		}
	}
	
	/**
	 * Answer the text or if the text begins with '%' then use the text
	 * as a key to find the translated text in the properties
	 */
	public static String getTranslatedText(String text, Properties translation) {
		if (text == null || text.length() == 0 || text[0] != '%')
			return text;
		if (translation == null)
			throw new BuildException('Expected to find translation for "' + text 
				+ '" but property file was not found.\n   May be missing feature.properties file or some other properties file');
		String value = translation.getProperty(text.substring(1));
		if (value == null)
			throw new BuildException('Expected to find translation for "' + text 
				+ '" but property file did not contain a value for that key');
		return value;
	}
	
	/**
	 * Called by build after reading properties, prebuild cleanup and sanity check to perform the actual build.
	 * Extended here to call zipSwingClasses().
	 */
	protected void buildImpl() {
		super.buildImpl();
		if (prop.isTrue('product.swing.zip'))
			zipSwingClasses();
	}
	
	/**
	 * Subclasses may override to provide a sanity checker with additional behavior
	 * Specifically, the Feature and Plugin manifest editors remove all comments
	 * (and thus preprocessor statements) from the feature.xml and plugin.xml files
	 * so subclasses should sanity check their own files to ensure this has not happened.
	 */
	protected SanityCheck createSanityChecker() {
		return new SanityCheckFeature();
	}
	
	/**
	 * Answer a collection of Ant patterns for files to be deleted 
	 * after the PDE Build Process has completed.
	 * For example, to delete a particular file in a plugin,
	 * 		['plugins/my.plugin.id/mydir/myfile.txt']
	 * or to delete an entire feature
	 * 		['features/my.feature.id_**']
	 */
	protected Collection<String> getPostPdeBuildDeletions() {
		return [];
	}
	
	/**
	 * Called by build() to launch the specified PDE Build Process.
	 * Subclasses can override to perform pre/post processing.
	 */
	protected void launchPdeBuild(PdeBuild builder) {
	    if (prop.isTrue('build.only.32.bit')) {
	        builder.delete64BitPluginsBeforeLaunch();
			warn(prop.productName + ' has code that is 32 bit and SHOULD be in a fragment.'
				+ '\n   in build for Eclipse ' + builder.targetVersion
				+ '\n   To work around this issue, we delete the 64 bit plugins from the Eclipse target before the PDE Build.')
	    }
		builder.generateTargetProperties();
		builder.deleteAfterBuild(getPostPdeBuildDeletions());
		builder.zipFeatureOutputAfterBuild();
		preBuildUpdateSite(builder);
		builder.launchFeatureBuild();
	}
	
	/**
	 * Called after the PDE builds have been launched and before they have completed
	 * to generate the UsageInfo.xml file consumed by the user profiler reporter.
	 */
	public void generateUsageInfo() {
		UsageInfoScanner scanner = new UsageInfoScanner(prop: prop);
		scanner.scanWorkspace(sourceDir);
	}
	
	/**
	 * Called by gatherResults() to wait for the external PDE Build process gather the build deployables.
	 * May be overridden or extended by subclasses.
	 */
	protected void gatherPdeOutput(PdeBuild builder) {
		if (sanityChecker != null)
			sanityChecker.checkPDEBuildResult(builder);
		File featureZip = productCache.localFile(builder.targetVersion);
		buildUpdateSite(builder, featureZip); 
		requiredSubProductNamesMap.put(builder.targetVersion, builder.requiredSubProductNames);
	}
	
	/**
	 * Called by launchPdeBuild before launching the PDE Build Process
	 * so that additional steps for generating the update site can be added to the PDE Build Process script.
	 * To prevent an update site from being built, either override this method and buildUpdateSite(...)
	 * or set "product.site = false" in product.properties
	 */
	protected void preBuildUpdateSite(PdeBuild pdeBuilder) {
		
		// Only build update sites for Eclipse 3.4 and greater
		if (prop.isTrue('product.site') && pdeBuilder.targetVersion.version >= Version.V_3_4) {
			File siteDir = new File(prop.productOut, "update/" + pdeBuilder.targetVersion);
			ant.mkdir(dir: siteDir);
			pdeBuilder.copyFeatureOutputAfterBuild(siteDir);
		}
	}
	
	/**
	 * Called by gatherPdeOutput() once the PDE Build process is complete and the deployable zip files collected
	 * so that the update site can be built from the zip file content and the ${product.id}_site project.
	 * To prevent an update site from being built, either override this method and preBuildUpdateSite(...)
	 * or set "product.site = false" in product.properties
	 */
	protected void buildUpdateSite(PdeBuild pdeBuilder, File featureZip) {
		
		// Build the update site
		File siteDir = new File(prop.productOut, "update/" + pdeBuilder.targetVersion);
		if (prop.isTrue('product.site') && siteDir.exists()) {
			SiteBuild siteBuilder = new SiteBuild(
				targetVersion:			pdeBuilder.targetVersion,
				eclipseTargetDir:		pdeBuilder.getEclipseTargetDir(),
				siteDir:				siteDir,
				prop:					prop);
			timed(pdeBuilder.targetVersion + " Build Update Site") {
				installUpdateSiteExtras(pdeBuilder, siteBuilder);
				siteBuilder.buildSite();
			}
			int buildThreadMax = Integer.valueOf(prop.get('build.thread.max'));
			pendingSiteBuilders.add(siteBuilder);
			if (pendingSiteBuilders.size() >= buildThreadMax)
				finishBuild(pendingSiteBuilders.remove(0));
		}
	}
	
	/**
	 * Called by buildUpdateSite() to install additional elements such as GEF or Shared
	 * to be included in the update site. Subclasses may extend or override.
	 */
	protected void installUpdateSiteExtras(PdeBuild pdeBuilder, SiteBuild siteBuilder) {
		for (String required : pdeBuilder.requiredSubProductNames) {
			
			// Do not include PDE or GEF in an update site...
			if (required == 'PDE' || required == 'GEF')
				continue;
			
			unzip(productCache.downloadFile(required, pdeBuilder.targetVersion), siteBuilder.siteDir);
		}
	}
	
	/**
	 * Create a subproducts zip file containing just classes without the plugin specific files
	 * for use by WindowTesterRunner. This should be called after the Eclipse 3.5 build is complete
	 */
	protected void zipSwingClasses() {
		File srcZip = productCache.localFile(prop.getSwingTargetVersion());
		File zipTemp = new File(prop.productTemp, 'zip/swing');
		File swingJar = productCache.localFile(null, 'swing/' + prop.productName + '.jar');
		
		// if this is a local build and the prop.getSwingTargetVersion() version of eclipse has not been built
		// then the swing version of the plugin can not be built
		if(prop.isLocalBuild() && !srcZip.exists()) {
			warn('Can not build ' + prop.productName + 
					'.jar. This is a local build and there is no build of Eclipse ' + 
					prop.getSwingTargetVersion() + 
					'.  Since this is the case then no ' + swingJar + ' will be built.');
			return;
		}
		ant.mkdir(dir: zipTemp)
		
		// Unzip the plugins
		ant.unzip(src: srcZip, dest: zipTemp) {
			patternset {
				include(name: 'plugins/*.jar');
				include(name: 'plugins/*/*.jar');
			}
		}
		
		// Zip the swing jar
		
		ant.mkdir(dir: swingJar.parent);
		ant.zip(destfile: swingJar, duplicate: 'fail') {
			for (File plugin : new File(zipTemp, 'plugins').listFiles()) {
				if (plugin.isFile()) {
					zipfileset(src: plugin) {
						include(name: 'abbot/**');
						include(name: 'com/**');
						include(name: 'junit/**');
					}
				}
				else {
					for (File jarFile : plugin.listFiles()) {
						if (jarFile.isFile() && jarFile.name.endsWith('.jar')) {
							zipfileset(src: jarFile) {
								include(name: 'abbot/**');
								include(name: 'com/**');
								include(name: 'junit/**');
							}
						}
					}
				}
			}
		}
	}
		
	/**
	 * Wait for any site builders that launched external processes
	 */
	protected void finishBuild() {
		while (pendingSiteBuilders.size() > 0)
			finishBuild(pendingSiteBuilders.remove(0));
		buildInstallers();
	}
	
	/**
	 * Wait for the specified site builder and zip the update site
	 */
	protected void finishBuild(SiteBuild siteBuilder) {
		timed(siteBuilder.getTargetVersion() + " Wait for external Generate P2 Metadata Process") {
			siteBuilder.collectLogs(); }
		timed(siteBuilder.getTargetVersion() + " Zip update site") {
			siteBuilder.zipSite(); }
	}
	
	/**
	 * Called by build() once the PDE Build process is complete and the deployable zip files collected
	 * so that the installer can be built from the zip file content and the ${product.id}_installer project.
	 * To prevent an installer from being built, either override this method
	 * or set "product.installer = false" in product.properties
	 */
	protected void buildInstallers() {
		 if (prop.isTrue('product.installer'))
			 buildInstaller(null);
		 else
			 buildSubProduct();
	}
	
	/**
	 * Build the installer for the specified OEM
	 * where oemName is null to build the instantiations installer
	 */
	protected void buildInstaller(String oemName) {
		 buildInstaller(oemName, Version.V_3_3, Version.V_2_1);
	}
	
	/**
	 * Build the installer for the specified OEM including builds in the specified range
	 * where oemName is null to build the instantiations installer
	 */
	protected void buildInstaller(String oemName, Version high, Version low) {
		List targetVersions = prop.targetVersions.findAll{
			it.oemName == oemName && (high >= it.version && it.version >= low) };
		buildInstaller(oemName, targetVersions);
	}
	
	/**
	 * Create an installer builder
	 */
	protected InstallerBuild createInstallerBuild(String oemName, List targetVersions) {
		
		// Find the product specific installer project (if it exists)
		String installerProjName = prop.productId + "_installer";
		File installerProjDir = new File(sourceDir, installerProjName);
		referencedProjNames.add(installerProjName);
		
		// Find the product installer template project
		File templateProjDir = new File(prop.buildCommonHome, 'installer');
		if (!templateProjDir.exists()) {
			String templateProjName = "com.instantiations.pde_installer_template";
			templateProjDir = new File(sourceDir, templateProjName);
			// referencedProjNames.add(templateProjName);
			if (!templateProjDir.exists())
				return;
		}
		
		Version targetVersionLow = null;
		Version targetVersionHigh = null;
		if (targetVersions != null) {
			targetVersionLow = targetVersions.collect{it.version}.min();
			targetVersionHigh = targetVersions.collect{it.version}.max().incrementMinor();
		}
		
		return new InstallerBuild(
			installerProjDir:		installerProjDir,
			templateProjDir:		templateProjDir,
			targetVersionLow:		targetVersionLow,
			targetVersionHigh:		targetVersionHigh,
			productCache:			productCache,
			prop:					prop);
	}
	
	/**
	 * Build the installer for the specified OEM and versions
	 * where oemName is null to build the instantiations installer
	 */
	protected void buildInstaller(String oemName, List targetVersions) {
		
		// Only build if there is something to build
		if (targetVersions == null || targetVersions.empty)
			return;

		// Build the installer
		InstallerBuild installerBuilder = createInstallerBuild(oemName, targetVersions);
		timed("Assemble install-image") {
			installerBuilder.unzipRCPInstaller(productCache.downloadFile("RCPInstaller", new OemVersion(null, Version.V_3_1)));
			for (OemVersion targetVersion in targetVersions) {
				installerBuilder.unzipInstallImage(
					productCache.downloadFile(prop.productName, targetVersion), 
					prop.productId, 
					prop.productName, 
					prop.productVersion + '_' + prop.getBuildQualifier(targetVersion), 
					targetVersion.version);
				installInstallerExtras(installerBuilder, targetVersion)
			}
		}
		timed("Build Installer") {
			installerBuilder.buildInstaller(); }
	}

	/**
	 * Called by buildInstaller() to install additional elements such as GEF or Shared
	 * to be included in the installer. Subclasses may extend or override.
	 */
	protected void installInstallerExtras(InstallerBuild installerBuilder, OemVersion targetVersion) {
		Collection<String> requiredSubProductNames = requiredSubProductNamesMap.get(targetVersion);
		if (requiredSubProductNames != null) {
			for (String required : requiredSubProductNames) {
				
				// Until all subproducts are built in Hudson, 
				// [Name]SubProduct files must be generated
				SubProductZipReader reader = installerBuilder.downloadOrGenerateSubProduct(required, targetVersion);
				
				installerBuilder.unzipInstallImage(
					productCache.downloadFile(required, targetVersion), 
					reader.id, 
					required, 
					reader.fullVersion, 
					targetVersion.version);
			}
		}
	}
	
	/**
	 * If the property "product.installer" == "false" then buildInstallers() calls this method
	 * to build the SubProduct source file so that other builds can consume this subproduct as part of an installer
	 */
	public void buildSubProduct() {
		InstallerBuild installerBuilder = createInstallerBuild(null, null);
		installerBuilder.generateSubProduct();
	}

	/**
	 * Print build information to standard out
	 */
	protected void report() {
		reportRequiredSubProducts();
		super.report();
	}
	
	protected void reportRequiredSubProducts() {
		Collection<String> allNames = new HashSet();
		for (Collection<String> someNames : requiredSubProductNamesMap.values())
			allNames.addAll(someNames);
		allNames = new TreeSet<String>(allNames);
		println('Required SubProducts for ' + prop.targetVersions)
		for (String required : allNames)
			println('   ' + required);
	}
}
