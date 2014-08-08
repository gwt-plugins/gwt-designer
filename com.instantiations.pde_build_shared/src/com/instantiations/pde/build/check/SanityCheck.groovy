package com.instantiations.pde.build.check

import com.instantiations.pde.build.util.BuildProperties
import com.instantiations.pde.build.util.BuildUtilimport org.apache.tools.ant.BuildException
/**
 * Sanity check the source code for common problems
 * such as files missing from build.properties, missing execution environment, etc.
 */
public class SanityCheck extends BuildUtil
{
	// Must be initialized before calling any methods
	File sourceDir;			// The workspace containing the feature and plugin projects

	// Used internally
	private int errorCount;
	
	//========================================================================
	// Main entry points
	
	/**
	 * Check for common problems in the primary feature and referenced plugins.
	 */
	public final void checkSource() {
		check('Source Sanity Check') {
			checkSourceImpl();
		}
	}
	
	/**
	 * Subclasses should extend or override to provide additional behavior
	 */
	protected void checkSourceImpl() {
		checkJobName();
		checkBuildProjName();
	}
	
	/**
	 * Check for common problems in the result files
	 */
	public final void checkResult() {
		check('Result Sanity Check') {
			checkResultImpl();
		}
	 }
	
	/**
	 * Subclasses should extend or override to provide additional behavior
	 */
	protected void checkResultImpl() {
		// Nothing at this time... see subclasses
	}

	/**
	 * Called by entry point methods such as checkSource(...), checkResult(...), etc
	 * to perform a sanity check.
	 */
	public void check(String name, Closure closure) {
		errorCount = 0;
		println('Starting ' + name);
		closure();
		if (warningCount > 0)
			println(name + ' has ' + warningCount + ' warnings.')
		if (errorCount > 0)
			throw new BuildException(name + ' Failed with ' + errorCount + ' errors.');
		println(name + ' Complete');
	}

	//==========================================================================
	// Utility Methods
	
	/**
	 * Assert that the job name is the same as the product name
	 */
	protected void checkJobName() {
		String basedir = ant.project.properties['basedir'];
		File jobDir = new File(basedir).parentFile.parentFile;
		if (!new File(jobDir, 'config.xml').exists())
			return;
		if (jobDir.name != prop.productName)
			fail('The job name "' + jobDir.name + '" must be the same as the product.name "' + prop.productName + '" in the product.properties file.');
	}
	
	/**
	 * Assert that the name of the build project ends with "_build"
	 */
	protected void checkBuildProjName() {
		File buildProjDir = new File('.').canonicalFile;
		if (!buildProjDir.name.endsWith('_build')) {
			throw new BuildException('Expected the name of the build project to end with "_build"\n   ' 
				+ buildProjDir.name);
		}
	}
	
	/**
	 * Assert that the specified node is defined
	 */
	protected boolean assertNodeDefined(NodeList node, String nodeName) {
		if (node != null && node.size() > 0 && node[0] != null)
			return true;
		fail('Missing ' + nodeName);
		return false;
	}
	
	/**
	 * Assert that the specified node has at least one non-whitespace text associated with it
	 */
	protected boolean assertNodeTextDefined(NodeList node, String nodeName) {
		if (node != null && node.text().trim().length() > 0)
			return true;
		fail('Missing ' + nodeName + ' text');
		return false;
	}
	
	/**
	 * assert that a node does not exist or if it exists it contains google
	 */
	protected boolean assertURLNodeValid(NodeList node, String nodeName) {
		if (node == null || node.size() == 0 || node[0] == null)
			return true;

		String data = node[0];
		if (data.contains('google'))
			return true;

		// once all code is converted to google this can be removed
		if (data.contains('nstantiations')) {
			warn(nodeName + ' attribute is present but SHOULD contain google RATHER THAN instantiations', 
				'This warning will be changed to a build error on 9/8/2010.  The current value is ' + data);
			return true;
		}
		
		// warn for now, but change to error later
		if (data.contains('example')) {
			warn(nodeName + ' attribute is present but contains "example" and should be fixed', 
				'This warning will be changed to a build error on 9/8/2010.  The current value is ' + data);
			return true;
		}
		
		// rim and hostbridge should go away as we leave these projects
		if (data.contains('rim') || data.contains('hostbridge'))
			return true;
		
		// Eric and Dan's book project
		if (data.contains('qualityeclipse'))
			return true;
		
		warn(nodeName + ' attribute is present but does not contain google or instantiations',
			"The current value is $data");
		return false;
	}
	/**
	 * Assert that the plugin property exists and has non-empty value
	 * Return the value of the property found
	 */
	protected String assertPropertyExists(String fileName, Properties properties, String key, String instructions = null) {
		String value = properties.get(key);
		if (value == null || value.trim().length() == 0)
			fail('Missing property "' + key + '" in ' + fileName, instructions);
		return value;
	}
	
	/**
	 * Assert that the plugin property does not exist
	 */
	protected void assertPropertyDoesNotExist(String fileName, Properties properties, String key, String instructions = null) {
		if (properties.containsKey(key))
			fail('Property "' + key + '" in ' + fileName + ' should be removed.', instructions);
	}
	
	/**
	 * Assert that the plugin property exists and has the specified value
	 */
	protected void assertPropertyHasValue(String fileName, Properties properties, String key, String expectedValue, String instructions = null) {
		assertPropertyHasValue(fileName, properties, key, [expectedValue], instructions);
	}
	
	/**
	 * Assert that the plugin property exists and has the specified value
	 */
	protected void assertPropertyHasValue(String fileName, Properties properties, String key, Collection expectedValues, String instructions = null) {
		String value = assertPropertyExists(fileName, properties, key, instructions);
		if (expectedValues != null && !expectedValues.contains(value)) {
			String msg = 'The value for property "' + key + '" should be ' + (
				expectedValues.size() == 1 ? '"' + expectedValues[0] + '"' : 'one of ' + expectedValues
			) + ' in ' + fileName;
			warn(msg, instructions);
		}
	}

	/**
	 * Assert that no value contains the specified string
	 */
	protected void assertNoValueContaining(String fileName, Properties properties, String text, String instructions = null) {
		for (String key : properties.keySet()) {
			String value = properties.get(key);
			if (value.indexOf(text) >= 0)
				fail(fileName + ' contains ' + key + ' = ' + value);
		}
	}
	
	/**
	 * Verify that the specified file contains the specified text
	 */
	protected void assertFileContains(File file, String text) {
		if (!fileContains(file, text))
			fail('Expected text "' + text + '" in ' + file.name + ' file');
	}
	
	/**
	 * Answer true if the specified file contains the specified text
	 */
	protected boolean fileContains(File file, String text) {
		boolean found = false;
		file.withReader { reader ->
			while (true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				if (line.contains(text)) {
					found = true;
					break;
				}
			}
		}
		return found;
	}
	
	/**
	 * Determine if the specified project has the java nature
	 */
	protected boolean hasJavaNature(File srcProj) {
		Node proj = new XmlParser().parse(new File(srcProj, '.project'));
		return proj.buildSpec.buildCommand.find {
			it.name.text() == 'org.eclipse.jdt.core.javabuilder' } != null;
	}
	
	/**
	 * Record a sanity check failure
	 */
	public void fail(String errorMessage) {
		//errorCount++;
		//println('ERROR ' + errorCount + ': ' + errorMessage);
		//context.reverseEach { println('   ' + it); }
		warn(errorMessage);
	}
	
	/**
	 * Record a sanity check failure
	 */
	 public void fail(String errorMessage, String instructions) {
		fail(errorMessage);
		if (instructions != null)
			println('   ' + instructions);
	}
}
