package com.instantiations.pde.build.util

import java.io.File;

import com.instantiations.pde.build.util.BuildProperties
import org.apache.tools.ant.BuildException
/**
 * Build utilities that can be used via inheritence or composition
 */
public class BuildUtil
{
	protected static final AntBuilder ant = new AntBuilder();
	
	// Warnings (and Errors)
	protected static int warningCount = 0;
	protected final List<String> context = new ArrayList<String>();

	// Initialize these properties when object is instantiated
	BuildProperties prop;			// The build properties
	
	public void setPropImpl(BuildProperties theProp) {
		this.prop = theProp;
	}
	
	//======================================================================
	// Warnings (and Errors)
	
	/**
	 * Append the specified contextual information to any failures that occur
	 * while executing the specified closure
	 */
	protected void inContext(String text, Closure closure) {
		context.add(text);
		closure();
		context.pop();
	}
	
	/**
	 * Record a sanity check warning
	 */
	 public void warn(String warningMessage) {
		warningCount++;
		println('WARNING ' + warningCount + ': ' + warningMessage);
		context.reverseEach { println('   ' + it); }
		
		StringBuffer buf = new StringBuffer(1000);
		buf.append('\n');
		buf.append('WARNING ' + warningCount + ': ' + warningMessage + '\n');
		context.reverseEach { buf.append(it + '\n'); }
		
		prop.warningsLog.parentFile.mkdirs();
		prop.warningsLog.append(buf.toString());
	}
	
	/**
	 * Record a sanity check failure
	 */
	 public void warn(String warningMessage, String instructions) {
		warn(warningMessage);
		if (instructions != null) {
			println('   ' + instructions);
			prop.warningsLog.append('   ' + instructions + '\n');
		}
	}
	
	//======================================================================
	// Utilities
	
	/**
	 * Delete the specified directory or symlink.
	 */
	protected deleteDirOrSymlink(File dirToDelete) {
		boolean deleted = false;
		if (prop.isLinux()) {
			try {
				ant.symlink(action: 'delete', link: dirToDelete.path); // NOT canonical path
				deleted = true;
			}
			catch (BuildException e) {
				System.out.println('Failed to delete symlink ' + dirToDelete.path 
					+ '\n  thus assuming this is a directory and deleting it.'
					+ '\n  Exception: ' + e);
			}
		}
//		if (!deleted)
			ant.delete(dir: dirToDelete, failonerror: false);
	}
	
	/**
	 * Expand the *.zip, *.tar, or *.tar.gz file
	 * into the specified directory 
	 * closure  - is ant sub tasks to be passed to the ant.unzip, ant.untar commands
	 * for example if you have a eclipse zip file and you only want a small portion of it use this command
	 * <pre><code>
	 * 		unzip(eclipseSdkZip, new File(prop.productTemp, 'WindowTesterRunner/runner')) {
	 *		patternset {
	 *			include(name: 'eclipse/plugins/org.apache.ant_*');
	 *		}
	 *	}
	 * </code></pre>
	 * the closure passed will start at the { at the end of the unzip parameters
	 */
	public static void unzip(File file, File dir, Closure closureIn = null) {
		Closure closure = {}
		// if there was a closure passed in use it otherwise use the empty closure; 
		if (closureIn != null) {
			closure = closureIn;
		}
		ant.mkdir(dir:dir);
		if (file.getName().endsWith(".zip") || file.getName().endsWith(".jar"))
			ant.unzip(src:file, dest:dir, closure);
		else if (file.getName().endsWith(".tar.gz") || file.getName().endsWith(".tgz"))
			ant.untar(src:file, dest:dir, compression: 'gzip', closure)
		else if (file.getName().endsWith(".tar"))
			ant.untar(src:file, dest:dir, compression: 'none', closure)
		else
			throw new BuildException(
				"Cannot expand file: " + file.getName()
				+ "\n   Modify " + BuildUtil.class.getName() + "#unzip(...) to support this file type"
				+ "\nStack Trace");
	}
	
	/**
	 * Write the specified collection to the missing projects log
	 */
	protected void writeMissingProjectsLog(Collection<String> missingProjNames) {
		if (missingProjNames.size() == 0)
			return;
		File logFile = new File(prop.productArtifacts, "missing-projects.log");
		if (logFile.exists() && !logFile.delete())
			throw new BuildException("Could not delete artifact " + logFile.canonicalPath);
		ant.mkdir(dir: logFile.parentFile);
		logFile.withWriter { writer ->
			for (String name : missingProjNames) {
				writer.writeLine(name); } }
	}

	public void createChecksum(File targetFile) {
		if (prop.isTrue('build.create.checksum')) {
			if (!targetFile.exists()) {
				warn("Trying to create a checksum on $targetFile but it does not exist");
			}
			else {
				ant.checksum(file: targetFile, todir: targetFile.parent, format: 'MD5SUM', algorithm: 'MD5');
			}
		}
	}
}
