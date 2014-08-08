package com.instantiations.pde.build.analysis

import java.io.File;
import java.util.regex.Pattern
import groovy.xml.MarkupBuilder
import org.apache.tools.ant.BuildException;import com.instantiations.pde.build.AbstractEclipseBuild
import com.instantiations.pde.build.external.EclipseRunnerimport com.instantiations.pde.build.server.CodeProServerBuild;
import com.instantiations.pde.build.util.BuildPropertiesException;import com.instantiations.pde.build.util.FileDownloader
import com.instantiations.pde.build.util.IntegrationDownloader
import com.instantiations.pde.build.util.OemVersion;
import com.instantiations.pde.build.util.ProductDownloader;
import com.instantiations.pde.build.util.Version

/**
 * Launch the CodePro Server to perform code analysis (audit, metrics, ...).
 */
public class CodeAnalysis extends AbstractEclipseBuild {
	// The directory containing Eclipse projects to be analyzed
	File			sourceDir;
	FileDownloader	fileCache;
	
	// Initialized and used internally
	File serverDir;
	File analysisDir;
	File outputDir;
	
	/**
	 * Answer true if the necessary build properties are defined
	 * for a code analysis to be performed.
	 */
	public boolean canPerformAnalysis() {
		File featureDir = new File(sourceDir, prop.productId + '_feature');
		return prop.isTrue('build.analysis') && prop.isTrue('build.group') && featureDir.exists();
	}
	
	/**
	 * Download CodePro Server if necessary, 
	 * write the XML file used to communicate intent, then launch as an external process.
	 * This method returns before the analysis is complete.
	 * Call #collectLogs() to wait for the external process to complete and collect the results.
	 */
	public void launchAnalysis() {
		serverDir = new File(prop.productTemp, 'server');
		analysisDir = new File(prop.get('build.analysis'));
		outputDir = new File(prop.productArtifacts, 'analysis');
		File datastoreDir = new File(prop.get('build.analysis.datastore'));
		File dashboardDir = new File(prop.get('build.analysis.dashboard'));
		
		File resultsDir = new File(prop.get('build.analysis'), 'results');
		ant.delete(dir:resultsDir);
			
		serverDir.mkdirs();
		analysisDir.mkdirs();
		outputDir.mkdirs();
		datastoreDir.mkdirs();
		dashboardDir.mkdirs();		

		File productTempLocation = new File(prop.productTemp, prop.get('build.analysis.target')).getCanonicalFile();
		File eclipseDir = new File(productTempLocation,  'build/eclipse').getCanonicalFile();
		File workspaceDir = new File(analysisDir, 'workspace').getCanonicalFile();
		if (!eclipseDir.exists()) {
			throw new BuildException("trying to audit code in $eclipseDir but it does not exist");
		}
		
		ant.copy(todir: workspaceDir) {
			fileset(dir: new File(eclipseDir, 'features').getCanonicalPath()) {
				include(name: '**');
			}
		}
		
		workspaceDir.eachDir { dir ->
			println("moving $dir to ${dir}_feature");
			ant.move(file: dir.getCanonicalPath(), toFile: dir.getCanonicalPath() + '_feature', verbose: true);
		}
		ant.copy(todir: workspaceDir) {
			fileset(dir: new File(eclipseDir, 'plugins').getCanonicalPath());
		}

		downloadAndInstall();
		CodeProServerBuild.createInput(prop);
		launchEclipse();
	}
	
	/**
	 * Copy the datastore to the artifacts directory.
	 */
	public void collectResults() {
		//File datastore = new File(prop.get('build.analysis'), 'results');
		File datastore = new File(prop.get('build.analysis.datastore'));
		
		try {
			if (datastore.exists()) {
				ant.mkdir(dir: outputDir);
				ant.copy(todir: outputDir) {
					fileset(dir: datastore);
				}
			}
			else {
				warn "directory $datastore does not exist can't copy the results from there";
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	* Copy the input and output files used by the server to the artifacts directory.
	*/
       public void collectInputOutput() {
	       File input = new File(prop.get('build.analysis'), (prop.getProductName()+'-input.xml'));
	       File output = new File(prop.get('build.analysis'), (prop.getProductName()+'-output.xml'));
	       
	       try {
		       ant.copy(todir: outputDir) {
			       fileset(file: input);
		       }
		       ant.copy(todir: outputDir) {
			       fileset(file: output);
		       }
	       } catch(Exception e) {
		       e.printStackTrace();
	       }
       }
	
	/**
	 * Download and install CodePro Server 
	 */
	protected void downloadAndInstall() {
		//IntegrationDownloader integrationCache = new IntegrationDownloader(prop: prop);
		//File serverZip = integrationCache.downloadFile('CodeProServer', Pattern.compile('CodeProServer_v\\d+\\.\\d+\\.\\d+\\.zip'));
		
		if(!serverDir.exists())
			ant.mkdir(dir: serverDir);
		
		//BJS: ProductCache here instead of IntegrationCache per Mark.
		ProductDownloader productCache = new ProductDownloader();
		productCache.setProp prop;
		
		File cpsZip = new File(prop.get('build.downloads'), 'CodeProServer_v6.5.0.zip');
		File serverZip = productCache.downloadCodeProServer("http://darkdown.instantiations.com/out/CodeProServerAnt/continuous/latest/CodeProServer_v6.5.0.zip", cpsZip);
		
		unzip(serverZip, serverDir);
	}
	
	/**
	 * Launch CodePro Server external process to perform the analysis
	 */
	protected void launchEclipse() {
		
		// Ensure that only one build is launched
		if (runner != null)
			throw new IllegalStateException("Build has already been launched");

		File eclipseHome = new File(serverDir, 'CodeProServer/eclipse');
		File workspace   = new File(analysisDir, 'workspace');
		
		runner = new EclipseRunner('Code Analysis', eclipseHome);
		runner.setWorkingDir(analysisDir);
		runner.setJavaExe(fileCache.getJavaExe(new Version('1.6')));
		runner.setApplication('com.instantiations.eclipse.server.app');
		runner.setWorkspace(workspace);
		runner.setCmdLine([
			'-in', new File(prop.get('build.analysis'), (prop.getProductName()+'-input.xml')).canonicalPath,
			'-out', new File(prop.get('build.analysis'), (prop.getProductName()+'-output.xml')).canonicalPath,
			'-debug',
			'-consoleLog',
			'-verbose',
			'-noinput'
		]);
		runner.setLogFile(new File(analysisDir, "analysis.log"));
		createOptionsFile(analysisDir);
		runner.launch();
	}
	
	protected void createOptionsFile(File workingDir) {
		try {
			List options = prop.getList('build.analysis.target.options');
			if (!options.isEmpty()) {
				File optionsFile = new File(workingDir, '.options');
				optionsFile.withPrintWriter { writer ->
					for (String option in options) {
						writer.println(option + '=true');
					}
				}
			}
		}
		catch (BuildPropertiesException e) {
			// this property is not defined so ignore this method
		}
	}

	/**
	 * Wait for the runner to complete.
	 * Override superclass to only wait for a finite length of time for the analysis to complete
	 */
	protected boolean waitForRunner() {
		if (runner == null)
			throw new IllegalStateException("External process has not yet been launched");
		// Wait for a maximum 60 may take time to download and run.
		return runner.waitForResult(60 * 60000);
	}
	
	/**
	 * Wait for the external code analysis process to complete
	 * and collect the logs from that process
	 */
	public void collectLogs() {
		basicCollectLogs(new File(outputDir, 'logs'));
	}
	
	/**
	 * If there is an error, then this method is called by basicCollectLogs()
	 * to copy the Code Analysis temp directory to artifacts for later diagnosis
	 */
	protected void basicCopyTempInError(File logDir) {
		
		// Copy temp space of failed code analysis to special location in CodeProServerAnt artifacts
		
		File dir = new File(prop.buildArtifacts, 'CodeProServerAnt/failures/' + System.currentTimeMillis());
		dir.mkdirs();
		ant.copy(todir: dir) {
			fileset(dir: analysisDir);
		}
	}
	
	/**
	 * Called by basicCollectLogs(...) if the external build process has failed.
	 * Override default behavior to ignore the code analysis failure
	 */
	protected void externalBuildFailed(File logDir) {
		// ignored
	}
}
