package com.instantiations.pde.build

import com.instantiations.pde.build.util.BuildProperties
import com.instantiations.pde.build.util.BuildUtil
import com.instantiations.pde.build.util.CleanupUtil
import com.instantiations.pde.build.util.FileDownloader
import com.instantiations.pde.build.util.ProductDownloader
import com.instantiations.pde.build.util.ProjectSetFile
import com.instantiations.pde.build.util.TimedResult
import com.instantiations.pde.build.util.Version
import org.apache.tools.ant.BuildException
import groovy.text.SimpleTemplateEngineimport com.instantiations.pde.build.util.OemVersion
import com.instantiations.pde.build.check.SanityCheckimport groovy.xml.MarkupBuilder

import org.tmatesoft.svn.core.SVNCommitInfo
import org.tmatesoft.svn.core.SVNDepth
import org.tmatesoft.svn.core.SVNErrorMessage
import org.tmatesoft.svn.core.SVNException
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory
import org.tmatesoft.svn.core.wc.SVNClientManager
import org.tmatesoft.svn.core.wc.SVNRevision
import org.tmatesoft.svn.core.wc.SVNCommitClient
import org.tmatesoft.svn.core.wc.SVNWCClient
/**
 * Behavior shared by RcpBuild and FeatureBuild.
 */
public class AbstractBuild extends BuildUtil
{
	protected final FileDownloader    fileCache;
	protected final ProductDownloader productCache;
	protected final File              sourceDir  = new File("..").getCanonicalFile();
	protected final List              buildTimes = new ArrayList();
	protected final Collection        referencedProjNames = new TreeSet();	// Names of source projects that are used in the build process
	protected SanityCheck sanityChecker = null;

	private def gb = 1024 * 1024 * 1024;
	
	// Used internally to track temp space usage
	long tempSizeBefore;
	long tempSizeAfter;
	
	public AbstractBuild() {
		prop = new BuildProperties();
		fileCache = new FileDownloader(prop: prop);
		productCache = new ProductDownloader(prop: prop);
	}

	/**
	 * This is the main entry point
	 */
	public void build() {
		timed("Build Time (total)") {
	
			// Echo ant Properties
			printAntProperties('Ant Properties at start')
			
			// Read properties
			readBuildProperties();
			prop.echoAll();

			referencedProjNames.add(new File(".").canonicalFile.name);

			timed("Pre-build cleanup") {
				preBuildCleanup();
				copyProductProperties();
				writeWorkspaceProjectSet();
			}
			
			// Sanity check the source files
			sanityCheckSource();

			// Perform the actual build
			println("===== Build ===============================");
			buildImpl();
			
			// Perform additional tasks and cleanup
			finishBuild();
			sanityCheckResult();
			postBuildCleanup();
		}
		
		// Print some build statistics
		println("===== Report ===============================");
		report();
		println("Build Complete");
	}
	
	/**
	 * Print all ant properties to the screen
	 */
	void printAntProperties(String title) {
		def antProperties = ant.project.properties;
		def propertiesList = [' '];
		
		propertiesList.clear();
		antProperties.each { entry -> propertiesList += entry.key + ' = ' + entry.value }
		propertiesList.sort { i1, i2 -> i1 <=> i2 }
		
		println title;
		propertiesList.each { println "  $it" }
	}
	
	/**
	 * Called by build() to initialize and read the build properties.
	 * Subclasses can override to add/modify/replace properties.
	 */
	protected void readBuildProperties() {
		prop.read();
	}
	
	/**
	 * Subclasses may override to provide a sanity checker with additional behavior
	 * Specifically, the Feature and Plugin manifest editors remove all comments
	 * (and thus preprocessor statements) from the feature.xml and plugin.xml files
	 * so subclasses should sanity check their own files to ensure this has not happened.
	 */
	protected SanityCheck createSanityChecker() {
		return null;
	}
	
	/**
	 * Subclasses may extend to check that source files contain expected content,
	 * but it is recommended to override #createSanityChecker to provide
	 * a different instance rather than overriding this method.
	 */
	protected void sanityCheckSource() {
		
		sanityChecker = createSanityChecker();
		if (sanityChecker != null) {
			sanityChecker.setSourceDir(sourceDir);
			sanityChecker.setProp(prop);
			sanityChecker.checkSource();
		}
	}

	/**
	 * Subclasses may extend to check that result files contain expected content,
	 * but it is recommended to override #createSanityChecker to provide
	 * a different instance rather than overriding this method.
	 */
	protected void sanityCheckResult() {
		if (sanityChecker != null)
			sanityChecker.checkResult();
	}
	
	/**
	 * Throw a build exception indicating that some expected source projects cannot be found.
	 * Subclasses may override to make some source projects optional.
	 */
	protected void throwMissingProjectsException() {
		String message = "Cannot find project(s) in " + sourceDir.canonicalPath;
		for (String name : getMissingProjNames()) {
			message += "\n  " + name; }
		message += "\nStack Trace";
		throw new BuildException(message);
	}
	
	/**
	 * Called by build after reading properties, prebuild cleanup and sanity check.
	 * Subclasses should override to perform the actual build.
	 */
	protected void buildImpl() {
	}
	
	/**
	 * Subclasses may override to perform additional build operations
	 */
	protected void finishBuild() {
	}
	
	/**
	 * Cleanup performed after the build properties have been read
	 * but before the sanity check and build.
	 * Subclasses may extend to include additional cleanup.
	 */
	protected void preBuildCleanup() {
		new CleanupUtil(prop: prop).cleanupTemp();
		File temp = prop.buildTemp;
		tempSizeBefore = temp.getTotalSpace() - temp.getFreeSpace();
	}

	/**
	 * Copy the product.properties file to the subproducts directory
	 */
	protected void copyProductProperties() {
		File srcFile = prop.productPropertiesFile;
		File dstFile = productCache.localFile(null, 'product.properties');
		dstFile.parentFile.mkdirs();
		ant.copy(file: srcFile, tofile: dstFile);
	}
	
	/**
	 * Cleanup performed after the build has occurred.
	 * Subclasses may extend to include additional cleanup.
	 */
	protected void postBuildCleanup() {
		println("===== Cleanup ===============================");
		println('ant.home = ' + ant.project.properties.'ant.home');

		File temp = prop.buildTemp;
		tempSizeAfter = temp.getTotalSpace() - temp.getFreeSpace();
		
		timed("Cleanup") {
			copyAndCheckInBuildJobConfig();
			writeBuildStatsFile();
			writeMissingProjectsLog();
			writeUnusedProjectsLog();
			createBuildDateFile();
			productCache.finalizeSubproductsDir();
			createSymlinks(); }
		timed("Cleanup old builds") {
			new CleanupUtil(prop: prop).cleanupOldBuilds(); }
	}
	
	/**
	 * Copy the Hudson build job's config.xml file into the build project's "hudson" directory
	 * and then check in that file if it has changed.
	 * 
	 * Once working, this method change will be folded back into AbstractBuild
	 */
	protected void copyAndCheckInBuildJobConfig() {
		
		if (!prop.isHudsonBuild()) {
			// If this is a local build then don't bother
			// checking the Hudson build job config file
			return;
		}
		
		// Read the build job configuration
		
		File realConfigFile = new File('../../config.xml').canonicalFile;
		if (!realConfigFile.exists())
			return;
		println('Hudson build job configuration file:\n   ' + realConfigFile.path);
		String realConfig = realConfigFile.getText();
		
		// Read the duplicate configuration
		
		File hudsonDir = new File('hudson').canonicalFile;
		boolean hudsonDirExists = hudsonDir.exists();
		File duplicateConfigFile = new File(hudsonDir, realConfigFile.name);
		boolean duplicateConfigExists = duplicateConfigFile.exists();
		String duplicateConfig = duplicateConfigExists ? duplicateConfigFile.getText() : '';
		if (duplicateConfig == realConfig) {
			println('Hudson build job configuration file is up to date - no need to commit a new version');
			return;
		}
		
		if (!prop.shouldCommitBuildJob()) {
			// If this is a local build or a test hudson build system
			// then do NOT commit any hudson build job configuration files
			return;
		}
		
		// Copy the build job configuration to the duplicate file
		
		duplicateConfigFile.parentFile.mkdirs();
		duplicateConfigFile.setText(realConfig);

		// Initialize SVNKit
		
		try {
			DAVRepositoryFactory.setup();
		} 
		catch (SVNException e) {
			println('SVN initialization failed');
			e.printStackTrace();
			return;
		}
		SVNClientManager clientManager = SVNClientManager.newInstance(null);
		
		// Add the file to the working copy if it is a new copy
		
		if (!duplicateConfigExists) {
			SVNWCClient wcClient = clientManager.getWCClient();
			File path = hudsonDirExists ? duplicateConfigFile : hudsonDir;
			try {
				wcClient.doAdd(path, false, false, false, SVNDepth.INFINITY, false, false, false);
			} 
			catch ( SVNException e ) {
				println('SVN add exception');
				e.printStackTrace();
				return;
			}
		}
		
		// Commit the change to SVN
		
		SVNCommitClient commitClient = clientManager.getCommitClient();
		File[] paths = hudsonDirExists ? [duplicateConfigFile] : [hudsonDir];
		SVNCommitInfo info;
		try {
			info = commitClient.doCommit(paths, false, 'Auto commit hudson build job config.xml', null, null, false, false, SVNDepth.INFINITY);
		} 
		catch ( SVNException e ) {
			println('SVN commit exception');
			e.printStackTrace();
			return;
		}
		
		SVNErrorMessage err = info.getErrorMessage();
		if (err != null) {
			println('SVN commit failed')
			println(err);
			return;
		}
		
		println('Committed Hudson build job configuration file:\n   ' + duplicateConfigFile.path);
	}
	
	/**
	 * Cache temp space usage information 
	 */
	protected void writeBuildStatsFile() {
		String productName = prop.productName;
		File statsFile = new File(prop.buildArtifacts, 'buildStats.xml');
		if (!statsFile.exists()) {
			statsFile.withWriter { writer ->
				MarkupBuilder builder = new MarkupBuilder(writer);
				builder.doubleQuotes = true;
				builder.stats {
				}
			}
		}
		Node stats = new XmlParser().parse(statsFile);
		Node build = stats.build.find { it.@name == productName };
		if (build != null) {
			build.@tempSizeBefore = tempSizeBefore;
			build.@tempSizeAfter = tempSizeAfter;
		}
		else {
			stats.appendNode('build', [tempSizeAfter: tempSizeAfter, tempSizeBefore: tempSizeBefore, name: productName]);
		}
		statsFile.withPrintWriter { writer ->
			new XmlNodePrinter(writer).print(stats);
		}
	}
	
	/**
	 * Answer project that were referenced but not found in the source directory
	 */
	protected Collection getMissingProjNames() {
		Collection result = new TreeSet();
		for (String name : referencedProjNames) {
			if (!new File(sourceDir, name).exists())
				result.add(name); }
		return result;
	}
	
	/**
	 * Write project that were referenced but not found in the source directory
	 * to a missing-projects.log file.
	 */
	protected void writeMissingProjectsLog() {
		writeMissingProjectsLog(getMissingProjNames());
	}

	/**
	 * Write the list of projects in the source directory
	 * but not used during the build to a unused-projects.log file
	 */
	protected void writeUnusedProjectsLog() {
		Collection unused = new TreeSet();
		sourceDir.eachDir() { projDir ->
			if (!referencedProjNames.contains(projDir.name))
				unused.add(projDir.name); }
		ant.mkdir(dir: prop.productArtifacts);
		new File(prop.productArtifacts, "unused-projects.log").withWriter { writer ->
			for (String name : unused) {
				if (name.equals(".metadata"))
					continue;
				if (name.equals("External Plug-in Libraries"))
					continue;
				writer.writeLine(name); } }
	}
	
	/**
	 * Read the workspace and create a project set in the workspace
	 */
	protected void writeWorkspaceProjectSet() {
		ProjectSetFile psf = new ProjectSetFile(sourceDir);
		for (File child : sourceDir.listFiles())
			if (child.isDirectory())
				psf.addProjName(child.name);
		psf.write(new File(sourceDir, prop.productName + ".psf"));
	}
	
	/**
	 * Create the "build-date.html" file to indicate a successful build
	 */
	protected void createBuildDateFile() {
		new File(prop.buildCommonHome, "templates/out/build-date.html").withReader { reader ->
			ant.mkdir(dir: prop.productOut);
			new File(prop.productOut, "build-date.html").withWriter { writer ->
				String now = prop.buildNum;
				new SimpleTemplateEngine().createTemplate(reader).make(
					buildDate: now.substring(0, 4) + "." + now.substring(4, 6) + "." + now.substring(6, 8)
				).writeTo(writer);
			}
		}
		new File(prop.productOut, "buildnum.txt").setText(prop.buildNum.toString());
	}
	
	/**
	 * Create a "latest" symlink on Linux
	 * and delete the "latest" directory on Windows
	 * 
	 * If this method fails to create a symlink, make sure that the ant-nodeps.jar 
	 * is on your Groovy or Eclipse or launch configuration classpath
	 * (can be found in your <plugins>/org.apache.ant_<version>/lib directory)
	 */
	protected void createSymlinks() {
		File outLinkFile = new File(prop.productOut.parentFile, "latest");
		File artifactsLinkFile = new File(prop.productArtifacts.parentFile, "latest");
		
		// This causes symlink contents to be deleted... do not use
		//ant.delete(dir:outLinkFile);
		//ant.delete(dir:artifactsLinkFile);
		
		if (prop.shouldCreateSymLinks()) {
			try {
				ant.symlink(
					action:		"single",
					link:		outLinkFile,
					resource:	prop.productOut,
					overwrite:	true);
				ant.symlink(
					action:		"single",
					link:		artifactsLinkFile,
					resource:	prop.productArtifacts,
					overwrite:	true);
			}
			catch (Exception e) {
				if (e.getMessage().indexOf('class org.apache.tools.ant.taskdefs.optional.unix.Symlink was not found') != -1) {
					System.err.println('\n\nError: Failed to find Ant Symlink class'
						+ '\n  Possible solutions:'
						+ '\n    1) The existing symlink file may be corrupt/locked/bad. Delete it and try again'
						+ '\n    -- OR --'
						+ '\n    2) Make sure that the ant-nodeps.jar is on your Groovy or Eclipse or launch configuration classpath.'
						+ '\n       This jar can be found in your <plugins>/org.apache.ant_<version>/lib directory.'
						+ '\n    --OR--'
						+ '\n    3) Set build.symlinks=false in your <username>.properties file\n');
				}
				throw e;
			}
		}
	}
	
	/**
	 * Print build information to standard out
	 */
	protected void report() {
		reportWarnings();
		reportBuildTimes();
		reportTempUsage();
	}
	
	protected void reportWarnings() {
		println('Warnings');
		println('      ' + warningCount + ' warnings');
		println('      ' + prop.warningsLog.canonicalPath);
	}
	
	protected void reportBuildTimes() {
		println("Build times in seconds");
		int totalTime = 0; // sum of all recorded times including total build time
		int lastTime = 0; // the total build time
		buildTimes.each() {
			totalTime += it.deltaTime;
			lastTime = it.deltaTime;
			printf("  %9d ", it.deltaTime); println it.name; }
		printf("  %9d ", (2 * lastTime - totalTime)); println "Unaccounted time";
	}
	
	protected void reportTempUsage() {
		long totalSize = prop.buildTemp.getTotalSpace();
		int percentUsage = tempSizeAfter * 100 / totalSize;
		println('Temp Space Usage in Gig')
		// println("tempSizeBefore = $tempSizeBefore");
		// println("tempSizeAfter = $tempSizeAfter");
		// code added to stop an exception thrown when the disk spaces used is very small
		if (tempSizeBefore < (gb * 0.1)) {
			tempSizeBefore = 0l;
		}
		if (tempSizeAfter < (gb * 0.1)) {
			tempSizeAfter = 0l;
		}
		printf("  %11.1f ", totalSize / gb); println 'Total Size';
		printf("  %11.1f ", tempSizeBefore / gb); println 'Size before build';
		printf("  %11.1f ", tempSizeAfter / gb); println 'Size after build';
		def diff = (tempSizeAfter - tempSizeBefore) / gb ;
		//code addedto stop an exception when the build takes up 
		// less thatn .1 GB.
		def limit = 0.1;
		if (diff < limit) {
			printf("  %11s ", '< .1 GB'); 
		} 
		else {
			printf("  %11.1f ", diff);
		}
		println 'Space used during build';
		printf("  %9d ", percentUsage); println '% Space used after build';
	}
	
	/**
	 * Time the specified worker operation and append that information 
	 * to the list of build timings
	 */
	protected void timed(String name, Closure worker) {
		long startTime = System.currentTimeMillis();
		worker();
		buildTimes.add(
			new TimedResult(
				name: name,
				startTime: startTime / 1000,
				deltaTime: (System.currentTimeMillis() - startTime) / 1000));
	}
	
	//================================================================================
	// Testing
	
//	public static void main(String[] args) {
//		AbstractBuild build = new AbstractBuild();
//		build.prop.readDefaults();
//		build.prop.set('product.name', 'Roger');
//		build.prop.echoAll();
//		build.tempSizeBefore = 50;
//		build.tempSizeAfter = 60;
//		build.writeBuildStatsFile();
//		println('Test Complete');
//	}
}
