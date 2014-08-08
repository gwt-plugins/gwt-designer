/**
 * 
 */
package com.instantiations.pde.build

import com.instantiations.pde.build.AbstractBuild;
import com.instantiations.pde.build.util.BuildPropertiesimport com.instantiations.pde.build.util.OemVersionimport com.instantiations.pde.build.util.BuildPropertiesException
import com.instantiations.pde.build.subproduct.SubProductManagerimport com.instantiations.pde.build.subproduct.SubProduct
import java.util.Mapimport java.lang.StringBuffer/**
 * @author markr
 *
 */
public abstract class JavaDocBuild extends AbstractBuild {
	private OemVersion oemVersion = OemVersion.V_3_4;
	private File docTemp;
	private File docOut;
	private File docSrc;
	private File docSubproducts;
	private String javadocClasspath;
	
	
	/* (non-Javadoc)
	 * @see com.instantiations.pde.build.AbstractBuild#AbstractBuild()
	 */
	public JavaDocBuild(){
		
	}
	
	public void build() {
		timed('Build Time (total)') {
			timed('Initialize') {
				init();
				preBuildCleanup();
				initTemp();
			}
			timed('Unzip Subproducts') {
				unzipSubproducts();
			}
			timed ('Create JavaDoc') {
				createJavadoc();
			}
			timed('Collect Results') {
				collectResults();
			}
		}
		println('JavaDoc generated for ' + prop.productName);
		// Print some build statistics
		println("===== Report ===============================");
		println("Build times in seconds");
		int totalTime = 0; // sum of all recorded times including total build time
		int lastTime = 0; // the total build time
		buildTimes.each() {
			totalTime += it.deltaTime;
			lastTime = it.deltaTime;
			printf("  %9d %s%n", it.deltaTime, it.name );
		}
		printf("  %9d %s%n", (2 * lastTime - totalTime), "Unaccounted time");
		println("Build Complete");
}
	
	/**
	 * Initialize the markup builder and all needed attributes for build
	 */
	protected void init() {
		// Echo ant Properties
		printAntProperties('Ant Properties at start')
		
		// Read properties
		readBuildProperties();
		prop.echoAll();

	 }
	/**
	 * Initialize the docBuildTemp directory if it is not already initialized.
	 * This is automatically called as needed.
	 */
	public void initTemp() {
		init();
		if (docTemp == null) {
			docTemp = prop.productTemp;
			docOut = new File(docTemp, 'plugins/' + prop.productId);
			docSubproducts = new File(docTemp, 'classpath');
			ant.mkdir(dir: docOut);
			ant.mkdir(dir: docSubproducts);
		}
	}
	
	/**
	 * unzip the required subproducts
	 */
	protected unzipSubproducts() {
		List <String> subproducts = getSubproducts();
		for (String subproduct in subproducts) {
			unzip(productCache.downloadFile(subproduct, oemVersion), docSubproducts);
		}
		File plugins = new File(docSubproducts, 'plugins')
		def scanner = ant.fileScanner {
			fileset (dir: plugins) {
				include(name: '*.jar');
				include(name: '*/**');
				include(name: '${java.home}/lib/rt.jar');
			}
		}
		StringBuffer path = new StringBuffer();
		for (File f in scanner) {
			path.append(f).append(File.pathSeparatorChar);
		}
		javadocClasspath = path.toString();
	}
	
	/**
	 * use ant to create the javadoc
	 */
	protected createJavadoc() {
		String overview = null;
		String stylesheet = null;
		Collection<String> messages = [];
		
		int javadocCount = prop.get('javadoc.count').toString().toInteger();
		for (int i=1; i <= javadocCount; i++) {
			try {
				overview = '../' + getValue('javadoc.overview', i);
			}
			catch (BuildPropertiesException e) {
				// intentionally ingnoed
			}
			try {
				stylesheet = '../' + getValue('javadoc.stylesheet', i);
			}
			catch (BuildPropertiesException e) {
				//intentionally ignored
			}
			File out = new File(docOut, getValue('javadoc.out', i));
			def options = [destdir: out];
			if (stylesheet != null) options['stylesheetfile'] = stylesheet;
			if (overview != null) options['overview'] = overview;
			options.putAll(javadocOptions());
			
			messages += "Creating Java Doc in $out";
			String cp = javadocClasspath;
			ant.javadoc(options) {
				classpath() {
					pathelement(path: cp);
				}
				List filesets = prop.getList('javadoc.filesets.' + i);
				for (String filesetref in filesets) {
					String value = '../' + prop.get('javadoc.filesets.dir.' + filesetref);
					'fileset'(dir: value) {
						try {
							String data = prop.get('javadoc.filesets.dir.' + filesetref + '.include');
							if (data != null) {
								String[] includes = data.trim().split(',');
								for (String inc in includes) {
									'include'(name: inc.trim());
								}
							}
						} 
						catch (BuildPropertiesException e) {
							// intentionally ignored
						}
						
						try {
							String data = prop.get('javadoc.filesets.dir.' + filesetref + '.exclude');
							if (data != null) {
								String[] excludes = data.trim().split(',');
								for (String exc in excludes) {
									'exclude'(name: exc.trim());
								}
							}
						}
						catch (BuildPropertiesException e) {
							//intentionally ignored
						}
					}
				}
			}
		}
		messages.each { println it };
	}
	
	/**
	 * read a property.  first try key.index if that fails then try
	 * key.
	 * <p>e.g. given key foo and index 1.  first try to find foo.1 
	 * if that fails then try foo
	 * @param key the base key for the value
	 * @param index the value to add to the key
	 * @prarm prefix to add to the value
	 */
	private String getValue(String key, int index, String prefix='') {
		String value = null;
		try {
			value = prop.get(key + '.' + index);
		} catch (BuildPropertiesException e) {
			value = prop.get(key);
		}
		return prefix + value;
	}
	 
	/**
	 * collect the results of the java doc generation
	 */
	protected void collectResults() {
		// zip the javadoc produced
		File zipFile = new File(prop.productTemp, 'subproducts/' + oemVersion.version.toString() + '/' + prop.productName + '.zip'); 
		ant.mkdir(dir: zipFile.parent);
		File javaDocLoc = prop.productTemp;
		ant.zip(destfile: zipFile) {
			fileset(dir: javaDocLoc) {
				include(name: 'plugins/**');
			}
		}
		SubProductManager subproductManager = new SubProductManager(
				targetVersion:	oemVersion,
				productCache:	productCache,
				prop:			prop);
		SubProduct subp = subproductManager.newSubProduct(prop.productName);
		subp.setJavaDoc();
		subproductManager.addSubProduct(subp);
		File subproductDir = new File(prop.get('build.subproducts'), prop.productName + '/' + oemVersion.version.toString());
		ant.copy(todir: subproductDir) {
			fileset(dir: zipFile.parent, includes: '*.zip');
		}

	}
	
	/**
	 * answer a map of options to pass to javadoc
	 */
	 protected Map javadocOptions() {
		return [:];
	}

	/**
	 * answer a list of subproducts needed to build the javadoc
	 */
	 public abstract List<String> getSubproducts();
}
