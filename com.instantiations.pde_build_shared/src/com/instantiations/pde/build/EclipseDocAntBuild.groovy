/**
 * 
 */
package com.instantiations.pde.build

import groovy.lang.Closure;
import groovy.util.AntBuilder;
import groovy.xml.MarkupBuilder;import java.io.StringWriter;import java.util.Map;
import java.util.LinkedHashMap;
import com.instantiations.pde.build.subproduct.SubProductManager;import com.instantiations.pde.build.util.FileDownloader;import com.instantiations.pde.build.util.ProductDownloader;import com.instantiations.pde.build.util.BuildProperties;import com.instantiations.pde.build.util.OemVersion;
import com.instantiations.pde.build.external.EclipseRunner;import org.apache.tools.ant.BuildException;
import com.instantiations.pde.build.util.BuildPropertiesExceptionimport com.instantiations.pde.build.util.Versionimport javax.print.DocPrintJob
/**
 * @author markr
 *
 */
public class EclipseDocAntBuild extends AbstractEclipseBuild {
	public static final String ECLIPSE_VERSION = '3.4';
	
	// Initialize these properties when object is instantiated
	FileDownloader    fileCache;    // download files as needed
	ProductDownloader productCache; // download products as needed
	BuildProperties	  prop;         // properties for this build
	AntBuilder		  ant;			// antBuilder to do ant calls with
	Map				  projects;		// the projects to build the doc for

	protected File docTemp;
	protected File docOut;
	protected File docRuntime;
	protected File docDropins;
	protected File docBuildScript;
	protected File docWorkspace;
	protected File docSource;
	protected File eclipseHome;
	List docDropinsJars = [];
	protected MarkupBuilder builder;
	protected Writer writer;
	protected Map docProjects = [:];
	protected Map commonHelp = 
		['com.instantiations.common.help': ['com.instantiations.common.help/html': null,
	                                        'com.instantiations.common.help/images': null,
	                                        'com.instantiations.common.help/stylesheets': ['include': '**/*.css']]];
	
	/**
	 * constructor to take a map and populate the attributes for this class
	 */	
	public EclipseDocAntBuild (LinkedHashMap initData) {
		 fileCache = initData.'fileCache';
		 productCache = initData.'productCache';
		 prop = initData.'prop';
		 ant = initData.'ant';
		 projects = initData.'projects';
	 }
	/**
	 * Initialize the markup builder and all needed attributes for build
	 */
	public void init() {
		 if (builder == null) {
			 writer = new StringWriter();
			 builder = new MarkupBuilder(writer);
			 builder.setDoubleQuotes(true);
		 }
	 }
	/**
	 * Initialize the docBuildTemp directory if it is not already initialized.
	 * This is automatically called as needed.
	 */
	public void initTemp() {
		init();
		if (docTemp == null) {
			docTemp = new File(prop.productTemp, ECLIPSE_VERSION);
			docOut = new File(docTemp, 'build');
			docRuntime = new File(docTemp, 'runtime');
			docWorkspace = new File(docRuntime, 'workspace');
			docBuildScript = new File(docOut, "build.xml");
			eclipseHome = new File(docRuntime, "eclipse").canonicalFile;
			ant.mkdir(dir: docOut);
			ant.mkdir(dir: docRuntime);
		}
	}
	
	/**
	 * Expand the *.zip, *.tar, or *.tar.gz file
	 * into the PDE Runtime directory 
	 */
	public void unzipRuntime() {
		initTemp();
		docDropins = new File(docRuntime, 'eclipse/dropins');
		unzipFileIntoRuntime('eclipse-sdk', ECLIPSE_VERSION);
		//Shared is needed for the doc build to run
		unzipSubProductIntoRuntimeDropins('Shared');
	}
	
	/**
	 * unzip a subproduct archive into the runtime
	 */
	public void unzipSubProductIntoRuntimeDropins(String subproduct, OemVersion version = OemVersion.V_3_4) {
		unzip(productCache.downloadFile(subproduct, version), docDropins);
	}	
	
	/**
	 * unzip an Eclipse style archive into the runtime
	 * @param file
	 */
	public void unzipFileIntoRuntime(String file, String version = ECLIPSE_VERSION) {
		unzip(fileCache.download(file, version), docRuntime);
	}	
	
	/**
	 * copy the source files into the runtime eclipse workspace
	 */
	public void copySource(File sourceIn) {
		initTemp();
		docSource = sourceIn;
		ant.mkdir(dir: docWorkspace);
		println()
		if (docSource == null || !docSource.exists()) {
			throw new BuildException("the source for the doc build is not set");
		}
		ant.copy(todir: docWorkspace) {
			fileset(dir: docSource) {
				exclude(name: '.metadata/**');
			}
		}
	}
	
	/**
	 * create the ant script to run the documentation generation
	 */
	public void createScript(String primaryPluginName) {
		File docPrimaryPlugin = new File(docWorkspace, primaryPluginName);
		def buildScript = builder.project(name: prop.productName + 'Build', default: 'docs_all') {
			'property'(name: 'src.home', value: docWorkspace.canonicalFile);
			'property'(name: 'eclipse.home', value: eclipseHome);
			'property'(name: 'recorder.file', value: new File(docOut, 'buildDocumnetation.log'));
			'property'(name: 'recorder.level', value: 'verbose');
			record(name: '${recorder.name}', loglevel: '${recorder.level}');
			'property'(file: 'build.properties');
			
			target(name: 'read_build_properties') {
				echo(message: 'ant.home = ${ant.home}');
				echo(message: 'java.home = ${java.home}');
				echo(message: 'javacSource = ${javacSource}');
				echo(message: 'javacTarget = ${javacTarget}');
				echo(message: 'bootclasspath = ${bootclasspath}');
				echo();
				echo(message: 'baseos = ${baseos}');
				echo(message: 'basews = ${basews}');
				echo(message: 'basearch = ${basearch}');
				echo();
				echo(message: 'os = ${os}');
				echo(message: 'ws = ${ws}');
				echo(message: 'arch = ${arch}');
				echo();
			}
			target(name: 'setup_workspace', depends: 'read_build_properties') {
				'eclipsetools.getClasspathVariable'(variable: 'ECLIPSE_HOME',
                    								property: 'classpath.var.@{varName}',
                    								failonnotfound: true);
				'eclipsetools.importProject'(location: '..');
				echo(message: 'build.temp          = ${build.temp}');
			}
			target(name: 'docs_all', depends: 'setup_workspace') {
		        build_documentation(primary: primaryPluginName) {
		        	projects() {
		        		docProjects.clear();
		        		docProjects.putAll(commonHelp);
		        		docProjects.putAll(projects);
						for (String projectName in docProjects.keySet()) {
							println ("processing $projectName");
							Map filesets = docProjects[projectName];
							project(name: projectName) {
								if (filesets != null) {
									for(String filesetDir in filesets.keySet()) {
										Map incexc = filesets[filesetDir];
										fileset(dir: "${docWorkspace.canonicalPath}/$filesetDir") {
											if (incexc != null) {
												for (String key in incexc.keySet()) {
													String name = incexc[key];
													switch (key) {
													case 'include':
														include(name: name);
														break;
												
													case 'exclude':
														exclude(name: name);
														break;
												
													default:
														throw new BuildException("value of key must be include or exclude");
														break;
													}
												}
											}
											exclude(name: '**/Thumbs.db');
										}
									}
								}
							}
						}
						for (String project in commonHelp.keySet()) {
							println("project :$project");
							Map y = commonHelp[project];
							for (String filesetDir in y.keySet()) {
								println(" filesetDir: $filesetDir");
								Map z = y[filesetDir];
								if (z != null) {
									for (String incexc in z.keySet()) {
										String pattern = z[incexc];
										println("  incexc $incexc : $pattern");
									}
								}
							}
						}
		        	}
				}
			}
			macrodef(name: 'build_documentation') {
				attribute(name: 'primary');
				attribute(name: 'url', default: 'http://www.instantiations.com/docgeneration/template_default.html');
				attribute(name: 'templates.location', default: '..');
				attribute(name: 'template.name', default: '${doc.template.file}');
				attribute(name: 'doc.title.prefix', default: '${doc.title.prefix}');
				attribute(name: 'doc.title.postfix', default: '${doc.title.postfix}');
				attribute(name: 'doc.out', default: new File(prop.productOut, 'docs').canonicalPath);
				attribute(name: 'template', default: '${build.temp}/@{template.name}');
				element(name: 'projects', implicit: false, optional: false);
				element(name: 'alternates', implicit: false, optional: true);
				sequential() {
					echo(level: 'warning', message: 'build_documentation');
					echo(message: 'Building Documentation for project @{primary}', level: 'info');
					echo(message: 'URL = @{url}', level: 'info');
					echo(message: 'templates.location = @{templates.location}',  level: 'verbose');
					echo(message: 'template.name = @{template.name}', level: 'verbose');
					echo(message: 'doc.title.prefix = @{doc.title.prefix}', level: 'verbose');
					echo(message: 'doc.title.postfix = @{doc.title.postfix}', level: 'verbose');
					echo(message: 'template = @{template}', level: 'verbose');
					echo(message: 'basedir = ${basedir}');
					get(src: '@{url}', dest: '@{template}', verbose: true);

					'eclipsetools.generateDocumentation'(primary: '@{primary}',
					                                    destinationDirectory: '@{doc.out}',
					                                    templateFile: '@{template}',
					                                    titlePrefix: '@{doc.title.prefix}',
					                                    titlePostfix: '@{doc.title.postfix}') {
						projects();
						alternates();
					}
				}
			}
		}
		PrintWriter pw = null;
		try {
			println("writing Ant script to $docBuildScript");
			pw = docBuildScript.newPrintWriter();
			pw.println(writer.toString());
		} 
		finally {
			if (pw != null) {
				pw.close();
			}
		}
		println (writer.toString());
//		ant.fail("done");
	}
		
	/**
	 * create an ant value to pass to the ant script
	 */
	private String antValue(String key, int index, String prefix='') {
		String value = null;
		try {
			value = prop.get(key + '.' + index);
		} catch (BuildPropertiesException e) {
			value = prop.get(key);
		}
		return prefix + value;
	}
	
	protected void setSource(File sourceIn) {
		this.docSource = sourceIn;
	}
	
	protected File getSource() {
		return this.docSource;
	}
	/**
	 * Spawns a documentation Build process
	 * and returns before the process has completed
	 */
	public void launch() {
		initTemp();
		
		// Ensure that only one build is launched
		if (runner != null)
			throw new IllegalStateException("Build has already been launched");
		
		// The com.instantiations.pde_build_debug EarlyStartup class assumes 
		// the relative locations of eclipseHome, workspace, and builder
		// so any changes here must be reflected there as well.
		
		File buildDirectory = docOut;
		//   buildOut       = initialized in initTemp
		File builder        = new File(docTemp, "build");
		     runner 		= new EclipseRunner("Documentation Build", eclipseHome);

		// Copy any top level custom scripts and build properties to this directory
		builder.mkdirs();
		
		// Build the Ant property file that is loaded by the PDE Build process
		Properties docProp = new Properties();
		docProp.setProperty('src.home', 	docSource.canonicalPath);
		docProp.setProperty('eclipse.home',	eclipseHome.canonicalPath);
		docProp.setProperty('doc.template.file', prop.get('doc.template.file'));
		docProp.setProperty('doc.alt.template.file', prop.get('doc.alt.template.file'));
		docProp.setProperty('doc.title.prefix', prop.get('doc.title.prefix'));
		docProp.setProperty('doc.title.postfix', prop.get('doc.title.postfix'));
		docProp.setProperty('build.temp', prop.productTemp.canonicalPath);
		new File(builder, "build.properties").withOutputStream() {
			docProp.save(it, "Properties for the Documentation Generation Build process") }
		
		runner.setApplication("org.eclipse.ant.core.antRunner");
		runner.setCmdLine([
	   		//'-Dosgi.arch=x86',
			'-verbose',
			'-noinput'
		]);
		
		// Use the JDK specified by the build property "pde.java.version"
		runner.setJavaExe(fileCache.getJavaExe(new Version('1.6')));

		// Launch the external PDE Build process
		runner.setWorkspace(docWorkspace);
		runner.setWorkingDir(builder);
		runner.setLogFile(new File(builder, "documentationGeneration-build.log"));
		runner.launch();
	}
	
	/**
	 * Wait for the external PDE Build process to complete and gather the build logs.
	 * Must call collect logs before calling collectRcpOutput or collectFeatureOutput
	 */
	public void collectLogs() {
		basicCollectLogs(new File(prop.productArtifacts, "/pde-logs"));
	}
	
	/**
	 * answer the location of the runtime workspace
	 */
	public File getDocWorkspace() {
		return docWorkspace;
	}
	
	/**
	 * answer the location the files usd as source to the documentation generation
	 */
	public File getDocSource() {
		return docSource;
	}
	
	/**
	 * answer the location ofthe eclipse home
	 */
	public File getEclipseHome() {
		return eclipseHome;
	}
}
