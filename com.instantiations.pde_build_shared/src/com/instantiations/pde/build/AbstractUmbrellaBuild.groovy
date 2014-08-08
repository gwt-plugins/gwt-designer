package com.instantiations.pde.build

import java.util.Map;

import com.instantiations.pde.build.subproduct.SubProductManager;
import com.instantiations.pde.build.subproduct.SubProductZipReader
import com.instantiations.pde.build.util.OemVersion;
import com.instantiations.pde.build.util.Version
import org.apache.tools.ant.BuildExceptionimport com.instantiations.pde.build.check.SanityCheckimport com.instantiations.pde.build.check.SanityCheckUmbrella/**
 * Builder for products such as WindowTesterPro and RCPDeveloper
 * which do not build any features or plugins themselves
 * but are collections of features and plugins from other builds. 
 */
public class AbstractUmbrellaBuild extends AbstractBuild
{
	Collection<String> missingSubProducts = new HashSet<String>();
	
	/**
	 * Subclasses may override to provide a sanity checker with additional behavior
	 * Specifically, the Feature and Plugin manifest editors remove all comments
	 * (and thus preprocessor statements) from the feature.xml and plugin.xml files
	 * so subclasses should sanity check their own files to ensure this has not happened.
	 */
	protected SanityCheck createSanityChecker() {
		return new SanityCheckUmbrella();
	}
	
	/**
	  * Called by build() to perform that actual work of
	  * creating the RCP Developer update sites, zip files, and installer
	  */
	public void buildImpl() {
		ant.mkdir(dir: prop.productOut);
		Map<OemVersion, Collection<String>> subProductsMap = getSubProductsMap();

		boolean shouldBuildInstaller = false;
		for (OemVersion targetVersion : prop.targetVersions) {
			Collection<String> subproducts = subProductsMap.get(targetVersion);
			
			if (targetVersion.version >= Version.V_3_4)
				buildUpdateSite(targetVersion, subproducts);
			else
				shouldBuildInstaller = true;
			buildZip(targetVersion, subproducts);
		}

		if (shouldBuildInstaller)
			buildInstaller(subProductsMap);
	}
	
	/**
	 * Answer a map of target version to list of subproduct names
	 */
	protected Map<OemVersion, Collection<String>> getSubProductsMap() {
		Map<OemVersion, Collection<String>> result = new HashMap<OemVersion, Collection<String>>();
		
		// Cycle through all the targets, 
		// building a list of subproducts to be deployed for that target
		for (OemVersion targetVersion : prop.targetVersions) {
			result.put(targetVersion, getSubProducts(targetVersion));
		}
		
		// Allow subproducts that are non-existant for one version of eclipse
		// and exist for another version of Eclipse
		// but fail the build if a subproduct is not found
		// for any version of Eclipse
		for (OemVersion targetVersion : prop.targetVersions) {
			for (String name : result.get(targetVersion)) {
				missingSubProducts.remove(name);
			}
		}
		failIfMissingSubProducts();
		
		return result;
	}
	
	/**
	 * Throw a build failure if the missingSubProducts field contains any subproduct names.
	 * Subclasses may override or extend or manipulate the content of the missingSubProducts field. 
	 */
	protected void failIfMissingSubProducts() {
		if (missingSubProducts.size() > 0) {
			throw new BuildException('The following subproducts cannot be found for any version of Eclipse'
				+ '\n   ' + missingSubProducts
				+ '\n   If the subproduct does not exist at http://download.instantiations.com/subproducts/'
				+ '\n   then click the "Build Now" button for that subproduct\'s build job.'
				+ '\n   Also check that build job\'s configuration to ensure that it is directly or indirectly dependent'
				+ '\n   upon the BuildCommon build job. For the complete build job hierarchy, see '
				+ '\n   https://hudson.instantiations.com/artifacts/BuildReport/continuous/latest/BuildJobHierarchyReport.html');
		}
	}
	
	/**
	 * Answer the subproducts for the specified target version of Eclipse
	 */
	protected Collection<String> getSubProducts(OemVersion targetVersion, Map<String, String> overrideMap = null) {
		
		SubProductManager subproductManager = new SubProductManager(
			targetVersion:	targetVersion,
			productCache:	productCache,
			prop:			prop);
		Collection<String> subproducts = new HashSet<String>();
		for (String name : getSpecifiedSubProductNames(targetVersion)) {
			try {
				subproductManager.getSubProduct(name);
				subproducts.add(name);
				subproducts.addAll(subproductManager.getAllDependencies(name, overrideMap));
			} catch (BuildException e) {
				warn("Could not find subproduct $name for $targetVersion\n   " + e);
				missingSubProducts.add(name);
			}
		}
		
		println('SubProducts for ' + targetVersion);
		for (String name : new TreeSet<String>(subproducts))
			println('   ' + name);
		
		return subproducts;
	}
	
	/**
	 * Answer the subproducts specified in the product.properties file.
	 * Subclasses may extend or override this method to adjust the list based upon the target version.
	 */
	protected Collection<String> getSpecifiedSubProductNames(OemVersion targetVersion) {
		return prop.getList('product.subproducts');
	}

	/**
	 * Build the updatesite for the specified OEM including builds in the specified range
	 * where oemName is null to build the instantiations installer.
	 * To prevent an update site from being built, either override this method
	 * or set "product.site = false" in product.properties
	 */
	protected void buildUpdateSite(OemVersion targetVersion, Collection<String> subproducts) {
		if (!prop.isTrue('product.site'))
			return;

		// Temporary directory where all update sites are built
		File updateTemp = new File(prop.productTemp, 'update');
		
		// Find the site project
		File buildProj = new File('.').canonicalFile;
		if (!buildProj.name.endsWith('_build'))
			throw new BuildException('Expected build project name to end with "_build"\n   ' + buildProj.path);
		referencedProjNames.add(buildProj.name);
		File siteProj = new File(buildProj.parentFile, buildProj.name.substring(0, buildProj.name.length() - 6) + '_site');
		referencedProjNames.add(siteProj.name);
		if (!siteProj.exists())
			siteProj = null;
		
		 // Install Eclipse if necessary
		File eclipseTargetDir = new File(prop.productTemp, targetVersion + '/target/eclipse');
		timed(targetVersion + ' Install Target ') {
			unzip(fileCache.download('eclipse-sdk', targetVersion.version), eclipseTargetDir.parentFile); }
		
		// Setup the update site builder
		File siteDir = new File(updateTemp, targetVersion.toString()).canonicalFile;
		SiteBuild siteBuilder = new SiteBuild(
			targetVersion: targetVersion,
			eclipseTargetDir: eclipseTargetDir,
			siteProjDir: siteProj,
			siteDir: siteDir,
			prop: prop);
		ant.mkdir(dir: siteDir);
		
		// Unzip subproducts and their dependencies into temp
		unzipUpdateSiteSubProducts(siteBuilder, targetVersion, subproducts, siteDir);
		
		// Build the update site
		timed(targetVersion + ' Build update site') {
			siteBuilder.buildSite();
			siteBuilder.collectLogs();
		}
		timed(targetVersion + ' Zip update site') {
			siteBuilder.zipSite();
		}
		
		// Move all generated update sites to their final location
		timed('Move update sites from temp to output') {
			ant.move(file: updateTemp, todir: prop.productOut); 
		}
	}
	
	/**
	 * Download and unzip subproducts into temp to build an update site.
	 * Subclasses may extend or override this method as necessary
	 * but it is recommended to extend or override the getSubProductFiles(...) method instead
	 */
	protected void unzipUpdateSiteSubProducts(SiteBuild siteBuilder, OemVersion targetVersion, Collection<String> subproducts, File siteDir) {
		unzipUpdateSiteSubProducts(targetVersion, subproducts, siteDir);
	}
	
	/**
	 * Download and unzip subproducts into temp to build an update site.
	 * Subclasses may extend or override this method as necessary
	 * but it is recommended to extend or override the getSubProductFiles(...) method instead
	 */
	protected void unzipUpdateSiteSubProducts(OemVersion targetVersion, Collection<String> subproducts, File siteDir) {
		Map<String, File> files = getSubProductFiles(targetVersion, subproducts);

		// Do not include PDE or GEF in an update site...
		files.remove('PDE');
		files.remove('GEF');
		
		for (String subproductName : new TreeSet(files.keySet())) {
			timed(targetVersion + ' Unzip ' + subproductName) {
				unzip(files.get(subproductName), siteDir); } }
	}
	
	/**
	 * Answer a mapping of subproduct name to subproduct.
	 * Subclasses may extend or override this method to adjust the return value
	 */
	protected Map<String, File> getSubProductFiles(OemVersion targetVersion, Collection<String> subproducts) {
		Map<String, File> files = new HashMap<String, File>();
		timed(targetVersion + ' Retrieving subproduct files') {
			for (String subproductName : subproducts) {
				files.put(subproductName, productCache.downloadFile(subproductName, targetVersion)); } }
		return files;
	}

	/**
	 * Build the zip file for the specified version.
	 * To prevent an zip file from being built, either override this method
	 * or set "product.zip = false" in product.properties
	 */
	protected void buildZip(OemVersion targetVersion, Collection<String> subproducts) {
		if (!prop.isTrue('product.zip'))
			return;
		
		// Name of the zip file to be generated
		String targetName = targetVersion.oemName;
		if (targetName == null)
			targetName = 'Eclipse';
		String zipFileName = prop.productName + '_v' + prop.productVersion + '_for_' + targetName + targetVersion.version + '.zip';
		
		// Generate temporary .eclipseextension file for inclusion in the zip
		File zipTemp = new File(prop.productTemp, 'zip/' + targetVersion);
		ant.mkdir(dir: zipTemp);
		new File(zipTemp, '.eclipseextension').write(
			'id=' + prop.productId
			+ '\nname=' + prop.productName
			+ '\nversion=' + prop.productVersion + '_' + prop.getBuildQualifier(targetVersion));
		
		// Zip the subproducts into a single file
		zipSubProducts(targetVersion, subproducts, zipTemp, zipFileName);
	}
	
	/**
	 * Download the necessary subproducts and generate the zip file for the specified version.
	 * Subclasses may extend or override this method as necessary
	 * but it is recommended to extend or override the getSubProductFiles(...) method instead
	 */
	protected void zipSubProducts(OemVersion targetVersion, Collection<String> subproducts, File zipTemp, String zipFileName, String subproductZipPrefix = '') {
		// patterns of elements of the zipTemp directory to mark as executable in the zip file
		List execPatterns = ['**/*.bash', '**/*.sh', '**/bin/ant', '**/bin/antRun'];
		Map<String, File> files = getSubProductFiles(targetVersion, subproducts);
		Collection<File> zipFiles = files.values();
		File newZipFile = new File(prop.productOut, zipFileName);
		timed(targetVersion + ' Zip ' + zipFileName) {
			ant.zip(destfile: newZipFile, duplicate: 'fail') {
				for (File file : zipFiles) {
					zipfileset(src: file, prefix: subproductZipPrefix) {
						exclude(name: '.eclipseextension');
						exclude(name: 'file.txt');
					}
				}
				zipfileset(dir: zipTemp) {
					for (String pattern in execPatterns) {
						exclude(name: pattern);
					}
				}
				
				zipfileset(dir: zipTemp, filemode: '755') {
					for (String pattern in execPatterns) {
						include(name: pattern);
					}
				}
			}
			createChecksum(newZipFile);
		}
	}
	
	/**
	 * Build the installer for the specified OEM including builds in the specified range
	 * where oemName is null to build the instantiations installer.
	 * To prevent an installer from being built, either override this method
	 * or set "product.installer = false" in product.properties
	 */
	protected void buildInstaller(Map<OemVersion, Collection<String>> subProductsMap) {
		if (!prop.isTrue('product.installer'))
			return;
		 
		Collection<OemVersion> targetVersions = prop.targetVersions.findAll { it.version < Version.V_3_4 };
		
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
		
		// Build the installer
		Collection<String> allSubproductNames = new HashSet<String>();
		for (OemVersion targetVersion : prop.targetVersions)
			allSubproductNames.addAll(subProductsMap.get(targetVersion));
		allSubproductNames.add(prop.productName);
		InstallerBuild installerBuilder = new InstallerBuild(
			installerProjDir:		installerProjDir,
			templateProjDir:		templateProjDir,
			targetVersionLow:		targetVersions.collect{it.version}.min(),
			targetVersionHigh:		targetVersions.collect{it.version}.max().incrementMinor(),
			productCache:			productCache,
			allSubproductNames:		allSubproductNames,
			prop:					prop);
		timed("Assemble install-image") {
			installerBuilder.unzipRCPInstaller(productCache.downloadFile("RCPInstaller", new OemVersion(null, Version.V_3_1)));
			unzipInstallerSubProducts(targetVersions, subProductsMap, installerBuilder);
		}
		timed("Build Installer") {
			installerBuilder.buildInstaller(); }
	}
	
	/**
	 * Download and unzip subproducts into temp to build an installer.
	 * Subclasses may extend or override this method as necessary
	 * but it is recommended to extend or override the getSubProductFiles(...) 
	 * and downloadOrGenerateSubProduct(...) methods instead
	 */
	protected void unzipInstallerSubProducts(Collection<OemVersion> targetVersions, Map<OemVersion, Collection<String>> subProductsMap, InstallerBuild installerBuilder) {
		for (OemVersion targetVersion : targetVersions) {
			Collection<String> subproducts = subProductsMap.get(targetVersion);
			Map<String, File> files = getSubProductFiles(targetVersion, subproducts);
			for (String subproductName : new TreeSet(files.keySet())) {
				try {
					// Until all subproducts are built in Hudson, 
					// [Name]SubProduct files must be generated
					SubProductZipReader reader = downloadOrGenerateSubProduct(subproductName, targetVersion, installerBuilder);
					
					installerBuilder.unzipInstallImage(
						files.get(subproductName), 
						reader.id, 
						subproductName, 
						reader.fullVersion, 
						targetVersion.version);
				} catch (BuildException e) {
					warn("Could not download subproduct $subproductName for $targetVersion\n   " + e);
				}
			}
		}
	}
	
	/**
	 * Answer a reader for extracting id and version information from the subproduct.
	 * Until all subproducts are built in Hudson, [Name]SubProduct files must be generated.
	 */
	protected SubProductZipReader downloadOrGenerateSubProduct(String subproductName, OemVersion targetVersion, InstallerBuild installerBuilder) {
		return installerBuilder.downloadOrGenerateSubProduct(subproductName, targetVersion);
	}
}
