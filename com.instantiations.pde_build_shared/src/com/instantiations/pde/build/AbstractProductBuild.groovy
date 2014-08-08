package com.instantiations.pde.build

import java.util.List;

import com.instantiations.pde.build.analysis.CodeAnalysis
import com.instantiations.pde.build.util.BuildProperties
import com.instantiations.pde.build.util.Version
import com.instantiations.pde.build.util.OemVersion
import com.instantiations.pde.build.check.SanityCheck

/**
 * Behavior shared by RcpBuild and FeatureBuild.
 */
public class AbstractProductBuild extends AbstractBuild
{
	/**
	 * Extend superclass information 
	 * to pull product properties from other builds.
	 */
	protected void readBuildProperties() {
		super.readBuildProperties();
		prop.setProductPropertiesLoader(productCache);
	}
	
	/**
	 * Called by build after reading properties, prebuild cleanup and sanity check
	 * to perform the actual build
	 */
	protected void buildImpl() {
		
		List pendingPdeBuilders = new ArrayList();
		List completedPdeBuilders = new ArrayList();
		int buildThreadMax = Integer.valueOf(prop.get('build.thread.max'));
		if (prop.isTrue('debug.pde'))
			buildThreadMax = 1;

		// Launch one external PDE Build process per target Eclipse
		boolean skippedTarget31 = false;
		for (OemVersion targetVersion in prop.targetVersions) {
			
			// Need to use a different runtime for older builds
			Version eclipseRuntimeVersion = getEclipseRuntimeVersion(targetVersion);
			
			// Cannot build Eclipse 3.1 with Eclipse 3.4, 
			// so assume that this product is being built for Eclipse 3.0 and copy 3.0 to 3.1
			if (targetVersion.version == Version.V_3_1 && eclipseRuntimeVersion != Version.V_3_1) {
				println('>>>');
				println('>>> Skipping build for Eclipse 3.1');
				println('>>> so that build for Eclipse 3.0 can be copied to 3.1 later');
				println('>>>');
				skippedTarget31 = true;
				continue;
			}
			
			// Initialize the PDE Build process
			PdeBuild builder = createPdeBuild(eclipseRuntimeVersion, targetVersion);
			builder.deletePdeTemp();
			timed(targetVersion + " Install Runtime E-" + eclipseRuntimeVersion) {
				installRuntimeEclipse(builder, eclipseRuntimeVersion); }
			timed(targetVersion + " Install Target ") {
				installTargetEclipse(builder); }
			timed(targetVersion + " Copy Source Projects") {
				copySourceProjects(builder);
				convertTextileToHtml(builder); }
			timed(targetVersion + " Install Target Extra") {
				installTargetExtras(builder); }
			
			// Spawns a PDE Build process and returns before build is complete
			timed(targetVersion + " Launch PDE Build") {
				launchPdeBuild(builder); }
			
			// Cache the builder so that the output can be gathered later
			pendingPdeBuilders.add(builder);
			
			// Launch PDE Build processes in parallel, up to the maximum specified
			if (pendingPdeBuilders.size() >= buildThreadMax) {
				builder = pendingPdeBuilders.remove(0);
				timed(builder.targetVersion + " Wait for external PDE Build Process (debugging)") {
					builder.collectLogs(); }
				completedPdeBuilders.add(builder);
			}
		}
		
		// Gather information for the usage profiler report
		generateUsageInfo();
		
		// Wait for each PDE Build process to complete
		while (pendingPdeBuilders.size() > 0) {
			PdeBuild builder = pendingPdeBuilders.remove(0);
			timed(builder.targetVersion + " Wait for external PDE Build Process (debugging)") {
				builder.collectLogs(); }
			completedPdeBuilders.add(builder);
		}

		// Make the assumption that if Eclipse 3.1 is specified then Eclipse 3.0 is also specified
		// and copy Eclipse 3.0 to 3.1 now that Eclipse 3.0 has been built
		if (skippedTarget31) {
			
			// Copy the subproduct file
			println('>>>');
			println('>>> Copy Eclipse 3.0 subproduct to Eclipse 3.1 subproduct');
			println('>>> since Eclipse 3.1 build was skipped.');
			println('>>>');
			File dir31 = productCache.localFile(OemVersion.V_3_1).parentFile;
			if (!dir31.exists()) {
				File dir30 = productCache.localFile(OemVersion.V_3_0).parentFile;
				ant.copy(todir: dir31) {
					fileset(dir: dir30);
				}
			}
			
			// In the future, if we need Eclipse 3.1 output files
			// add code here to copy Eclipse 3.0 output files to 3.1
			// ...
		}
		
		// Gather the output from the PDE Build process
		while (completedPdeBuilders.size() > 0) {
			PdeBuild builder = completedPdeBuilders.remove(0);
			gatherPdeOutput(builder);
		}
		
		// Launch code analysis (audit, metrics, ...) as external process
		CodeAnalysis analysis = createCodeAnalysis();
		if (analysis != null) {
			timed('Launch Code Analysis') {
				launchCodeAnalysis(analysis);
			}
		}
		
		// Wait for the code analysis result
		if (analysis != null) {
			timed('Wait for external Code Analysis') {
				analysis.collectLogs();
				analysis.collectInputOutput();
				analysis.collectResults();
			}
		}
	}
	
	//=========================================================================
	// PDE Build
	
	/**
	 * This is overridden by subclasses (specifically FeatureBuild)
	 * to generate the UsageInfo.xml file consumed by the user profiler reporter.
	 */
	public void generateUsageInfo() {
		// Overriden as necessary
	}
	
	/**
	 * Answer the runtime version of Eclipse to use for the given target version of Eclipse.
	 * By default, Eclipse 3.4 is used to build all versions except Eclipse 3.1
	 * and Eclipse 3.1 is not built, but rather copied from Eclipse 3.0.
	 */
	public Version getEclipseRuntimeVersion(OemVersion targetVersion) {
		return Version.V_3_4;
	}
	
	/**
	 * Called by build() to create and initialize the PDE Builder
	 * for the specified version of Eclipse.
	 */
	protected PdeBuild createPdeBuild(Version eclipseRuntimeVersion, OemVersion targetVersion) {
		PdeBuild pdeBuilder;
		if (targetVersion.oemName != null)
			pdeBuilder = Class.forName('com.instantiations.pde.build.oem.' + targetVersion.oemName + 'PdeBuild').newInstance();
		else
			pdeBuilder = new PdeBuild();
		pdeBuilder.setEclipseRuntimeVersion(eclipseRuntimeVersion);
		pdeBuilder.setTargetVersion(targetVersion);
		pdeBuilder.setReferencedProjNames(referencedProjNames);
		pdeBuilder.setSourceDir(sourceDir);
		pdeBuilder.setFileCache(fileCache);
		pdeBuilder.setProductCache(productCache);
		pdeBuilder.setProp(prop);
		return pdeBuilder;
	}

	/**
	 * Called by build() to install the Runtime Eclipse
	 * used to execute the PDE Build process.
	 */
	protected void installRuntimeEclipse(PdeBuild builder, Version eclipseVersion) {
		builder.unzipRuntime(fileCache.download("eclipse-sdk", eclipseVersion));
	}

	/**
	 * Called by build() to install the Target Eclipse
	 * against which the product is built.
	 */
	protected void installTargetEclipse(PdeBuild builder) {
		List<String> targetEclipses = prop.getList('product.target.eclipse');
		for (String target in targetEclipses) {
			File archive = fileCache.download(target, builder.targetVersion.version);
//			// Delta packs for Eclipse 2.1 and 3.0 do not have "eclipse/" in their folder structure
//			// so unzip by calling a different method.
			if (target.equals('delta-pack') && builder.targetVersion.version <= Version.V_3_0) {
				builder.unzipTargetExtra(archive);
			}
			else {
				builder.unzipTarget(archive);
			}
		}
	}

	/**
	 * Called by build() to install additional elements such as GEF or Shared
	 * against which the product is built.
	 */
	protected void installTargetExtras(PdeBuild builder) {
		builder.unzipRequiredSubProducts();
	}

	/**
	 * Called by build() to copy and preprocess the feature and plugin projects
	 * into the temporary PDE Build structure so that it can be built.
	 */
	protected void copySourceProjects(PdeBuild builder) {
		if (!builder.copyFeature(prop.productId)) {
			writeMissingProjectsLog();
			if (getMissingProjNames().size() > 0)
				throwMissingProjectsException();
		}
	}

	/**
	 * Find textile files and convert those files into html files
	 */
	protected void convertTextileToHtml(PdeBuild builder) {
		builder.convertTextileToHtml();
	}
	
	//=========================================================================
	// Code Analysis
	
	/**
	 * Answer a new object for performing code analysis (audit, metrics, ...)
	 * or null if a code analysis should not be performed.
	 */
	protected CodeAnalysis createCodeAnalysis() {
		CodeAnalysis analysis = new CodeAnalysis(
			prop:		prop,
			sourceDir:	sourceDir,
			fileCache:	fileCache);
		return analysis.canPerformAnalysis() ? analysis : null;
	}
	
	/**
	 * Launch the code analysis
	 */
	protected void launchCodeAnalysis(CodeAnalysis analysis) {
		analysis.launchAnalysis();
	}
}
