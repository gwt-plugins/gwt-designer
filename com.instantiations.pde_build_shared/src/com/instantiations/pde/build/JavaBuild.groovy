package com.instantiations.pde.build

import com.instantiations.pde.build.preprocessor.PluginProjectPreprocessor
import com.instantiations.pde.build.util.BuildUtil
import com.instantiations.pde.build.util.Version;import org.apache.tools.ant.BuildException
import java.util.zip.ZipFileimport java.util.zip.ZipEntryimport com.instantiations.pde.build.util.OemVersion/**
 * Compile and assemble a JAR file
 */
public class JavaBuild extends BuildUtil
{
	// Initialize these properties when object is instantiated
	Version eclipseTargetVersion;			// The version of Eclipse against which the source will be built
	File sourceDir;							// The directory containing source projects to be built
	// Only necessary if you want to track referenced projects
	Collection referencedProjNames;			// Names of source projects that are used in the build process
	// Only necessary if classpath has references to plugins
	File targetHome;						// "eclipse" target directory used to resolve classpath entries
	// Only necessary if classpath has references to classpath variables other than ECLIPSE*HOME
	Map classpathVars;
	// Optional... Initialized automatically if not already initialized
	File javaTemp;							// The directory in which the Java Build takes place

	// May override these properties as desired
	boolean debug	= true; // Generate debug information in the compiled classes
	String vSource	= "1.3"; // The java source JVM level (e.g. 1.4, 1.5, ...)
	String vTarget	= "1.3"; // The JVM level for the compiled code (e.g. 1.4, 1.5, ...)
	
	// Initialized by the prepareProj method
	File projDir;				// the source project directory
	File tempSrcDir;			// the temporary source directory containing *.java files that are preprocessed and ready to be compiled
	File tempBinDir;			// the temporary directory containing *.class files that were compiled
	Collection classpathElems;	// The directories and JAR files on the classpath
	
	/**
	 * Preprocess the project source and prepare the project to be compiled.
	 */
	private void prepareProj(String projName, File srcZip) {
		if (referencedProjNames != null)
			referencedProjNames.add(projName);
		if (javaTemp == null)
			javaTemp = new File(prop.productTemp, eclipseTargetVersion + "/javabuild");

		println("Compiling Project " + projName);
		projDir = new File(sourceDir, projName);
		tempSrcDir = new File(javaTemp, projName + "/src");
		tempBinDir = new File(javaTemp, projName + "/bin");
		
		// Traverse the classpath file
		// to copy and preprocess the source files
		// and build the classpath used during compilation
		classpathElems = new ArrayList();
		Node classpathNode = new XmlParser().parse(new File(projDir, ".classpath"));
		for (entry in classpathNode.classpathentry) {
			if (entry.'@kind' == 'src') {
				if (entry.'@combineaccessrules' == 'false') {
					classpathElems.add(new File(javaTemp, entry.'@path'.substring(1) + "/bin"))
				}
				else {
					ant.copy(todir: tempSrcDir) {
						fileset(dir: new File(projDir, entry.'@path'));
					}
				}
			}
			else if (entry.'@kind' == 'var') {
				String path = entry.'@path';
				String var = path.substring(0, path.indexOf('/'));
				File elem = null;
				File elemRoot = null;
				if (classpathVars != null)
					elemRoot = classpathVars.get(var);
				if (elemRoot == null && var.startsWith('ECLIPSE') && var.endsWith("HOME"))
					elemRoot = targetHome;
				if (elemRoot != null) {
					path = path.substring(var.length() + 1);
					if (path.startsWith('plugins')) {
						path = path.substring(path.indexOf('/') + 1);
						String pluginId = path.substring(0, path.indexOf('_'));
						String prefix = pluginId + '_';
						String name = new File(elemRoot, "plugins").list().find { it.startsWith(prefix) };
						if (name == null)
							throw new BuildException("Could not find plugin " + pluginId 
								+ "\n   in " + elemRoot.canonicalPath);
						if (path.indexOf('/') == -1)
							elem = new File(elemRoot, "plugins/" + name);
						else
							elem = new File(elemRoot, "plugins/" + name + path.substring(path.indexOf('/')));
					}
					else
						elem = new File(elemRoot, path);
				}
				if (elem != null)
					classpathElems.add(elem);
				else
					throw new BuildException("Cannot translate classpath element: " + entry.'@path');
			}
			else if (entry.'@kind' == 'lib') {
				classpathElems.add(new File(projDir, entry.'@path'));
			}
		}
		println("Resolved Classpath:");
		for (File file in classpathElems)
			println("  " + file.canonicalPath);
		
		// Preprocess the source
		PluginProjectPreprocessor processor = new PluginProjectPreprocessor(
			new OemVersion(null, eclipseTargetVersion),
			prop.productVersion + "." + prop.getBuildQualifier(new OemVersion(null, eclipseTargetVersion)),
			prop
		);
		processor.processSource(tempSrcDir);
		
		// Zip the source files
		if (srcZip != null) {
			ant.mkdir(dir: srcZip.parentFile);
			ant.zip(
				destfile:	srcZip, 
				update:		true, 
				duplicate:	"fail") {
				fileset(dir: tempSrcDir);
			}
		}
	}
	
	/**
	 * Compile the java source files in the project
	 * as part of the PDE Build Process after the PDE build has occurred.
	 */
	private void compileProjAfterBuild(PdeBuild pdeBuilder, String projName, File srcZip) {
		prepareProj(projName, srcZip);
		
		// Compile the source files
		pdeBuilder.addPostBuildTask('<mkdir dir=\"' + tempBinDir.canonicalPath + '\"/>');
		pdeBuilder.addPostBuildTask('<javac');
		pdeBuilder.addPostBuildTask('\tsrcdir=\"' + tempSrcDir.canonicalPath + '\"');
		pdeBuilder.addPostBuildTask('\tdestdir=\"' + tempBinDir.canonicalPath + '\"');
		pdeBuilder.addPostBuildTask('\tdebug=\"' + debug + '\"');
		pdeBuilder.addPostBuildTask('\tsource=\"' + vSource + '\"');
		pdeBuilder.addPostBuildTask('\ttarget=\"' + vTarget + '\"');
		pdeBuilder.addPostBuildTask('\tfork=\"true\"');
		pdeBuilder.addPostBuildTask('\tverbose=\"true\">');
		if (classpathElems.size() > 0) {
			pdeBuilder.addPostBuildTask('\t<classpath>');
			for (File file in classpathElems)
				pdeBuilder.addPostBuildTask('\t\t<pathelement location=\"' + file.canonicalPath + '\"/>');
			pdeBuilder.addPostBuildTask('\t</classpath>');
		}
		pdeBuilder.addPostBuildTask('</javac>');
		pdeBuilder.addPostBuildTask('<copy todir=\"' + tempBinDir.canonicalPath + '\">');
		pdeBuilder.addPostBuildTask('\t<fileset dir=\"' + tempSrcDir.canonicalPath + '\">');
		pdeBuilder.addPostBuildTask('\t\t<exclude name=\"**/*.class\"/>');
		pdeBuilder.addPostBuildTask('\t\t<exclude name=\"**/*.java\"/>');
		pdeBuilder.addPostBuildTask('\t\t<exclude name=\"**/Thumbs.db\"/>');
		pdeBuilder.addPostBuildTask('\t</fileset>');
		pdeBuilder.addPostBuildTask('</copy>');
	}
		
	/**
	 * Compile the java source files in the project.
	 */
	private void compileProj(String projName, File srcZip) {
		prepareProj(projName, srcZip);
		
		// Compile the source files
		ant.mkdir(dir: tempBinDir);
		ant.javac(
			srcdir:		tempSrcDir,
			destdir:	tempBinDir,
			debug:		debug,
			source:		vSource,
			target:		vTarget,
			fork:		true,
			verbose:	true) {
			if (classpathElems.size() > 0) {
				classpath() {
					for (File file in classpathElems)
						pathelement(location: file.canonicalPath)
				}
			}
		}
		ant.copy(todir: tempBinDir) {
			fileset(dir: tempSrcDir) {
				exclude(name: "**/*.class");
				exclude(name: "**/*.java");
				exclude(name: "**/Thumbs.db");
			}
		}
	}
	
	/**
	 * Compile the java source file in the project
	 * and then copy into the specified directory
	 * as part of the PDE Build Process after the PDE build has occurred.
	 */
	public void compileToDirAfterBuild(PdeBuild pdeBuilder, String projName, File binDir, File srcZip = null) {
		pdeBuilder.addPostBuildTask('<!-- Compile ' + projName + ' to directory -->');
		compileProjAfterBuild(pdeBuilder, projName, srcZip);
		pdeBuilder.addPostBuildTask('<mkdir dir=\"' + binDir.canonicalPath + '\"/>');
		pdeBuilder.addPostBuildTask('<copy todir=\"' + binDir.canonicalPath + '\">');
		pdeBuilder.addPostBuildTask('\t<fileset dir=\"' + tempBinDir.canonicalPath + '\"/>');
		pdeBuilder.addPostBuildTask('</copy>\n');
	}
	
	/**
	 * Compile the java source file in the project
	 * and then copy into the specified directory
	 */
	public void compileToDir(String projName, File binDir, File srcZip = null) {
		compileProj(projName, srcZip);
		ant.mkdir(dir: binDir);
		ant.copy(todir: binDir) {
			fileset(dir: tempBinDir);
		}
	}
	
	/**
	 * Compile the java source files in the project
	 * and then copy into the specified jar file
	 * as part of the PDE Build Process after the PDE build has occurred.
	 */
	public void compileToJarAfterBuild(PdeBuild pdeBuilder, String projName, File jarFile, File srcZip = null) {
		pdeBuilder.addPostBuildTask('<!-- Compile ' + projName + ' to jar -->');
		compileProjAfterBuild(pdeBuilder, projName, srcZip);
		pdeBuilder.addPostBuildTask('<mkdir dir=\"' + jarFile.parentFile.canonicalPath + '\"/>');
		pdeBuilder.addPostBuildTask('<zip');
		pdeBuilder.addPostBuildTask('\tbasedir=\"' + tempBinDir.canonicalPath + '\"');
		pdeBuilder.addPostBuildTask('\tdestfile=\"' + jarFile.canonicalPath + '\"');
		pdeBuilder.addPostBuildTask('\tupdate=\"true\"');
		pdeBuilder.addPostBuildTask('\tduplicate=\"fail\"/>\n');
	}
	
	/**
	 * Compile the java source files in the project
	 * and then copy into the specified jar file
	 */
	public void compileToJar(String projName, File jarFile, File srcZip = null) {
		compileProj(projName, srcZip);
		ant.mkdir(dir: jarFile.parentFile);
		ant.zip(
			basedir:	tempBinDir, 
			destfile:	jarFile, 
			update:		true, 
			duplicate:	"fail");
	}
	
	/**
	 * Copy a plugin or part of a plugin
	 * as part of the PDE Build Process after the PDE build has occurred.
	 */
	public void copyPluginAfterBuild(PdeBuild pdeBuilder, String pluginId, File dest, String path = '**') {
		pdeBuilder.addPostBuildTask('<!-- Copy plugin ' + pluginId + ' ' + path + ' -->');
		String prefix = pluginId + '_';
		String name = new File(targetHome, "plugins").list().find { it.startsWith(prefix) };
		if (name == null)
			throw new BuildException("Could not find plugin " + pluginId 
				+ "\n   in " + targetHome.canonicalPath);
		File plugin = new File(targetHome, "plugins/" + name);
		if (plugin.isFile() || name.endsWith('.jar')) {
			pdeBuilder.addPostBuildTask('<copy file=\"' + plugin.canonicalPath + '\" todir=\"' + dest.canonicalPath + '\"/>');
		}
		else {
			pdeBuilder.addPostBuildTask('<copy todir=\"' + dest.canonicalPath + '\">');
			pdeBuilder.addPostBuildTask('\t<fileset dir=\"' + plugin.canonicalPath + '\">');
			pdeBuilder.addPostBuildTask('\t\t<include name=\"' + path + '\"/>');
			pdeBuilder.addPostBuildTask('\t</fileset>');
			pdeBuilder.addPostBuildTask('</copy>\n');
		}
	}
	
	/**
	 * Copy a plugin or part of a plugin
	 */
	public void copyPlugin(String pluginId, File dest, String path = '**') {
		String prefix = pluginId + '_';
		String name = new File(targetHome, "plugins").list().find { it.startsWith(prefix) };
		if (name == null)
			throw new BuildException("Could not find plugin " + pluginId + " in " + targetHome.canonicalPath);
		File plugin = new File(targetHome, "plugins/" + name);
		if (plugin.isFile()) {
			ant.copy(file: plugin, todir: dest);
		}
		else {
			ant.copy(todir: dest) {
				fileset(dir: plugin) {
					include(name: path);
				}
			}
		}
	}
}
