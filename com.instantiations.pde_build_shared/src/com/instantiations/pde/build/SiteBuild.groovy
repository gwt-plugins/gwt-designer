package com.instantiations.pde.build

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.instantiations.pde.build.util.BuildProperties;
import com.instantiations.pde.build.preprocessor.SiteXmlPreprocessor;
import org.apache.tools.ant.BuildException
import sun.misc.PerformanceLogger.TimeData;

import com.instantiations.pde.build.external.EclipseRunner
import com.instantiations.pde.build.external.SignJars;

import groovy.text.XmlTemplateEngineimport groovy.text.Templateimport groovy.text.SimpleTemplateEngineimport groovy.xml.MarkupBuilder;

import com.instantiations.pde.build.util.FileDownloader
import com.instantiations.pde.build.util.Versionimport com.instantiations.pde.build.util.OemVersion
import com.instantiations.pde.build.preprocessor.SiteXmlBuildimport com.instantiations.pde.build.preprocessor.VariableReplacementPreprocessor/**
 * Builder of update sites
 */
public class SiteBuild extends AbstractEclipseBuild
{
	// Initialize these properties when object is instantiated
	OemVersion targetVersion;	// The version of Eclipse against which the source will be built
	File eclipseTargetDir;		// The "eclipse" directory used to run the p2 metadata generation process
	File siteProjDir;			// The project directory containing update site files (e.g. site.xml) or null if none
	File siteDir;				// The directory into which the update site is generated
								// Assume that the PDE Build Process has already copied the output to this directory
	
	// Used internally
	private int errorCount;
	private File signJarLogFile;
	private Collection<String> hiddenFeatures = new HashSet<String>();
	
	public OemVersion getTargetVersion() {
		return targetVersion;
	}
	
	/**
	 * Add the specified feature to a list of features which are part of the update site
	 * but not explicitly listed in the site.xml file and should not be visible to the user 
	 */
	public void hideFeature(String featureId) {
		hiddenFeatures.add(featureId);
	}
	
	/**
	 * The main entry point to build an update site 
	 * and launch an external process to generate "p2" metadata
	 * 
	 * @return true if an external process was launched,
	 * 		or false if the update process site is complete
	 */
	public boolean buildSite() {
		copySiteProject();
		
		// Unzip and preprocess features and plugins
		// ASSUME the that the PDE Build process or the caller has copied the build output to the siteDir
		SiteXmlBuild siteXmlBuild = new SiteXmlBuild(targetVersion: targetVersion, hiddenFeatures: hiddenFeatures, prop: prop);
		SiteXmlPreprocessor siteXmlProc = new SiteXmlPreprocessor(targetVersion.version, prop);
		errorCount = 0;
		processAllFeatures(siteXmlBuild, siteXmlProc);
		if (errorCount > 0)
			throw new BuildException('Sanity Check Failed with ' + errorCount + ' errors.');
		
		// Preprocess site project content in its output location
		File siteXmlFile = new File(siteDir, 'site.xml');
		if (!siteXmlFile.exists())
			siteXmlBuild.generateSiteXml(siteXmlFile);
		siteXmlProc.process(siteXmlFile);
		VariableReplacementPreprocessor varPreprocessor = new VariableReplacementPreprocessor(targetVersion.version, prop);
		File indexHtmlFile = new File(siteDir, 'index.html');
		if (indexHtmlFile.exists())
			varPreprocessor.process(indexHtmlFile);
		new File(siteDir, 'web').eachFile {	file ->
			varPreprocessor.process(file); }
		
		// Optimize for "p2" if Eclipse 3.4 or greater
		if (targetVersion.version < Version.V_3_4)
			return false;
		
		// Ensure that only one build is launched
		if (runner != null)
			throw new IllegalStateException("Generate p2 metadata process has already been launched");
		
		//sign the Jars
		if (prop.shouldSignJars()) {
			signJars(siteDir.canonicalFile);
		}
		
		// Launch an external process to generate p2 metadata
	    runner = new EclipseRunner(targetVersion + " P2 Metadata Generation", getPublisherEclipse());
		runner.setApplication("org.eclipse.equinox.p2.publisher.UpdateSitePublisher");
		runner.setCmdLine([
		    "-source",                 siteDir.canonicalPath,
		    "-metadataRepository",     "file:" + siteDir.canonicalPath,
		    "-metadataRepositoryName", prop.productTitle + " Update Site",
		    "-artifactRepository",     "file:" + siteDir.canonicalPath,
		    "-artifactRepositoryName", prop.productTitle + " Artifacts",
		    "-compress",
		    "-publishArtifacts",
		]);
		runner.setWorkspace(new File(eclipseTargetDir.parentFile, "workspace-p2gen"));
		runner.setWorkingDir(eclipseTargetDir.parentFile);
		runner.setLogFile(new File(eclipseTargetDir.parentFile, "p2gen-build.log"));
		runner.launch();
		return true;
	}
	
	/**
	 * Return a version of Eclipse that org.eclipse.equinox.p2.publisher.UpdateSitePublisher can be run in.
	 * If this is eclipse 3.4 then unzip a version of Eclipse 3.7 to run the UpdateSitePublisher in.
	 * @return A version of eclipse that will run org.eclipse.equinox.p2.publisher.UpdateSitePublisher
	 */
	File getPublisherEclipse() {
		File ret = eclipseTargetDir;
		if (targetVersion.equals(OemVersion.V_3_4)) {
			File eclipse37Dir = new File(prop.productTemp, targetVersion.toString() + "/eclipse3.7");
			FileDownloader fileCache = new FileDownloader(prop: prop);
			unzip(fileCache.download("eclipse-sdk", Version.V_3_7), eclipse37Dir);
			ret = new File(eclipse37Dir, 'eclipse');
		}
		return ret;
	}
	/**
	 * sign the jars in the update directory
	 */
	protected signJars(File updateDir) {
		try {
			SignJars sj = new SignJars(updateDir, prop.get('product.sign.jar.host'), new Integer(prop.get('product.sign.jar.port')));
			signJarLogFile = new File('/var/tmp/jarSigner/' + targetVersion.version.toString() + '/signJar.log');
			println("creating $signJarLogFile");
			ant.mkdir(dir: signJarLogFile.parent);
			signJarLogFile.createNewFile();
			ant.chmod(file: signJarLogFile, perm: "ugo+w");
			sj.signJars(signJarLogFile);
			File stageDir = new File(prop.productTemp, targetVersion.version.toString() + '/build/scripts');
			ant.mkdir(dir: stageDir);
			println("moving $signJarLogFile to $stageDir");
			ant.move(file: signJarLogFile, todir: stageDir)
		}
		catch (Exception e) {
			println('failed to sign Jars');
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw); 
			e.printStackTrace(pw);
			println(sw.toString());
			warn(sprintf('failed to sign jars for %s version %s', prop.productName, targetVersion.version.toString()),
							sw.toString());
			try {
				pw.close();
				sw.close();
			}
			catch (IOException ioe) {
				//intentionally ignored
			}
		}
	}
	
	/**
	 * Copy the update site template to the site project
	 * then overlay the specific update site project content if there is one.
	 */
	protected void copySiteProject() {
		ant.mkdir(dir:siteDir);
		File sourceDir = new File('..').canonicalFile;
		
		// Copy the update site template
		File projDir = new File(prop.buildCommonHome, 'update-site');
		if (!projDir.exists())
			projDir = new File(sourceDir, 'com.instantiations.pde_site_template');
		if (!projDir.exists())
			throw new BuildException('Missing update site template project');
		copySiteProject(projDir);
		
		// Copy the specific update site project if there is one
		if (siteProjDir != null && !siteProjDir.exists())
			throw new BuildException('Could not locate specified update site project ' 
				+ siteProjDir.name + '\n   ' + siteProjDir.canonicalPath
				+ '\n   Leave siteProjDir unspecified (null) to automatically locate update site project relative to build project');
		if (siteProjDir == null) {
			siteProjDir = new File(sourceDir, prop.productId + '_site');
			if (!siteProjDir.exists()) {
				String buildProjName = new File('.').canonicalFile.name;
				if (buildProjName.endsWith('_build')) {
					String siteProjName = buildProjName.substring(0, buildProjName.length() - 6) + '_site';
					siteProjDir = new File(sourceDir, siteProjName);
					if (!siteProjDir.exists())
						siteProjDir = null;
				}
			}
		}
		if (siteProjDir != null)
			copySiteProject(siteProjDir);

		// Copy the product specific update site files (if any) from the build project
		projDir = new File('site');
		if (projDir.exists())
			copySiteProject(projDir);
		
		// Read the installation instructions from the install.html file
		// for inclusion in the index.html and/or site.xsl files then cleanup that file
		
//		File installFile = new File(siteDir, 'install.html');
//		prop.set('site.install.instructions', readHtmlBody(installFile));
//		installFile.delete();
		
		File installFile = new File(siteDir, 'install-notes.html');
		String notes = readHtmlBody(installFile);
		prop.set('site.install.notes', notes);
		installFile.delete();
	}
	
	/**
	 * Copy the update site files from the specified project directories
	 * to the update site directory being built
	 */
	protected void copySiteProject(File projDir) {
		println('Copying update site files from ' + projDir.name 
			+ ' project\n   ' + projDir.canonicalPath);
		
		// Copy files to the site directory
		
		ant.copy(todir: siteDir, overwrite: true) {
			fileset(dir: projDir) {
				include(name: 'images/**/*');
				include(name: 'web/**/*');
				include(name: 'index.html');
				include(name: 'site.xml');
				include(name: 'associateSite*.xml');
				include(name: 'install*.html');
			}
		}
		
		// Copy target specific files to the site directory
		
		File subDir = new File(projDir, targetVersion.version.toString());
		if (subDir.exists()) {
			ant.copy(todir: siteDir, overwrite: true) {
				fileset(dir: subDir);
			}
		}
	}
	
	/**
	 * Answer the lines between <body> and </body> or null if none
	 */
	protected String readHtmlBody(File file) {
		if (!file.exists())
			return null;
		file.withReader { reader ->
			while (true) {
				String line = reader.readLine();
				if (line == null)
					return null;
				if (line.trim() == '<body>')
					break;
			}
			StringWriter stringWriter = new StringWriter(1000);
			PrintWriter printWriter = new PrintWriter(stringWriter);
			while (true) {
				String line = reader.readLine();
				if (line == null)
					throw new BuildException('Failed to find </body> in ' + file.canonicalPath);
				if (line.indexOf('</body>') >= 0)
					return stringWriter.toString();
				printWriter.println(line);
			}
		}
	}
	
	/**
	 * Process all features and plugins
	 */
	protected void processAllFeatures(SiteXmlBuild siteXmlBuild, SiteXmlPreprocessor siteXmlProc) {
		for (File featureDir : new File(siteDir, "features").listFiles()) {
			processFeature(siteXmlBuild, siteXmlProc, featureDir); 
		}
	}
	
	/**
	 * Process the specified feature
	 */
	protected void processFeature(SiteXmlBuild siteXmlBuilder, SiteXmlPreprocessor siteXmlProc, File featureDir) {
		Node feature = new XmlParser().parse(new File(featureDir, "feature.xml"));
		File featurePropFile = new File(featureDir, "feature.properties");
		if (featurePropFile.exists()) {
			Properties featureProp = new Properties();
			featurePropFile.withReader { reader ->
				featureProp.load(reader);
			}
			siteXmlBuilder.setCategory(feature.'@id', featureProp.get('siteCategory'));
		}
		siteXmlProc.setVersion(feature.'@id', feature.'@version');
		for (plugin in feature.plugin) {
			File pluginDir = new File(siteDir, "plugins/" + plugin.'@id' + "_" + plugin.'@version');
			boolean isDir = processPlugin(pluginDir);
		}
		ant.zip(
			destfile: new File(featureDir.getParent(), feature.'@id' + "_" + feature.'@version' + ".jar"),
			basedir: featureDir);
		ant.delete(dir:featureDir);
	}
	
	/**
	 * Process the specified plugin by jar'ing it as necessary
	 * @return true if the plugin is a directory based plugin and was jar'd
	 */
	protected void processPlugin(File pluginDir) {
		if (pluginDir.exists()) {
			ant.zip(
				destfile: new File(pluginDir.getPath() + ".jar"),
				basedir: pluginDir);
			ant.delete(dir: pluginDir);
		}
	}
	
	/**
	 * Override superclass implementation because there is not
	 * any generation of p2 metadata and thus no runner
	 * for Eclipse earlier than 3.4
	 */
	protected boolean waitForRunner() {
		return targetVersion.version < Version.V_3_4 || super.waitForRunner();
	}

	/**
	 * Wait for the external PDE Build process to complete and gather the build logs.
	 * Must call collect logs before calling zipSite
	 */
	public void collectLogs() {
		// There are no logs for Eclipse 3.3 and earlier
		if (targetVersion.version >= Version.V_3_4) {
			basicCollectLogs(new File(prop.productArtifacts, targetVersion + "/p2-logs"));
			if (signJarLogFile != null && signJarLogFile.exists()) {
				File signJarLogDir = new File(prop.productArtifacts, targetVersion + "/signJars");
				ant.mkdir(dir: signJarLogDir);
				ant.copy(file: signJarLogFile, todir: signJarLogDir);
			}
		}
	}
	
	/**
	 * Answer the update site zip file name
	 */
	public String getSiteZipFileName() {
		String targetName = targetVersion.oemName;
		if (targetName == null)
			targetName = 'Eclipse';
		return prop.productName + '_v' + prop.productVersion + '_UpdateSite_for_' + targetName + targetVersion.version + '.zip';
	}
	
	/**
	 * Generate a zip file containing the update site so that a customer can download
	 * and install the update site locally.
	 */
	public void zipSite() {
		File siteZip = new File(prop.productOut, 'update/' + getSiteZipFileName());
		ant.mkdir(dir: siteZip.parent);
		ant.zip(destfile: siteZip, basedir:  siteDir);
		createChecksum(siteZip);

	}
	
	/**
	 * Record a sanity check failure
	 */
	protected void fail(String errorMessage) {
		errorCount++;
		println('ERROR ' + errorCount + ': ' + errorMessage);
	}
}
