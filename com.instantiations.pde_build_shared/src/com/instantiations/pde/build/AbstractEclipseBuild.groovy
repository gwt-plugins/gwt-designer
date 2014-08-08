package com.instantiations.pde.build

import com.instantiations.pde.build.util.BuildUtil
import com.instantiations.pde.build.external.EclipseRunner
import org.apache.tools.ant.BuildExceptionimport java.util.regex.Pattern
/**
 * Behavior shared by PdeBuild and SiteBuild.
 */
public class AbstractEclipseBuild extends BuildUtil
{
	// Initialized in the launchBuild method
	protected EclipseRunner runner;
	
	// Set in collectLogs() and checked in various other methods
	protected boolean logsCollected = false;
	
	// Used in scanLineFromLogInError
	protected static final Pattern COMPILATION_ERROR_START_PATTERN = Pattern.compile('^\\s*\\[javac\\]\\s+\\d+\\.\\s*ERROR');
	protected static final Pattern COMPILATION_ERROR_END_PATTERN = Pattern.compile('^\\s*\\[javac\\]\\s+(---|\\[)');
	protected Collection<String> compilationErrors = null;
	protected boolean processingCompilationError = false;
	
	public void setProp(def theProp) {
		setPropImpl(theProp);
	}
	
	/**
	 * Wait for the runner to complete.
	 * Subclasses may override if there is no runner.
	 * Return true if successful, else false
	 */
	protected boolean waitForRunner() {
		if (runner == null)
			throw new IllegalStateException("External process has not yet been launched");
		return runner.waitForResult();
	}
	
	/**
	 * Called by collectLogs() to wait for the external process to complete and gather the logs.
	 * Must call collectLogs() before calling other collect methods.
	 */
	protected void basicCollectLogs(File logDir) {
		if (logsCollected)
			throw new IllegalStateException('collectLogs() already called');
		logsCollected = true;
		boolean success = waitForRunner();
		basicCopyLogs(logDir);
		if (!success) {
			basicCopyTempInError(logDir);
			printLogsInError();
			externalBuildFailed(logDir);
		}
	}
	
	/**
	 * Called by basicCollectLogs(...) if the external build process has failed.
	 * Default behavior is to throw a build exception.
	 * Subclasses may extend or override as necessary.
	 */
	protected void externalBuildFailed(File logDir) {
		throw new BuildException(runner.getName() + ' Failed'
			+ '\n>>> See logs above and in ' + logDir.canonicalPath);
	}
	
	/**
	 * If there is an error, then this method is called by basicCollectLogs()
	 * to copy temporary files to the artifacts directory for later diagnosis.
	 * Subclasses may extend or override to print appropriate logs.
	 */
	protected void basicCopyTempInError(File logDir) {
		
	}
	
	/**
	 * If there is an error, then this method is called by basicCollectLogs()
	 * to print relavent logs for easier debugging.
	 * Subclasses may extend or override to print appropriate logs.
	 */
	protected void printLogsInError() {
		File logFile = runner.getLogFile();
		if (logFile != null && logFile.exists()) {
			println('=== BEGIN Log file: ' + logFile.canonicalPath);
			if (!prop.isLocalBuild())
				println('See full logs: ' + prop.productArtifactUrlSpec);
			
			// Echo the last 500 or so lines from the log file
			int lineCount = 0;
			ArrayList<String> lastLines = new ArrayList<String>(1000);
			logFile.withReader { reader ->
				String line;
				while (true) {
					line = reader.readLine();
					if (line == null)
						break;
					lineCount++;
					lastLines.add(line);
					if (scanLineFromLogInError(line, lastLines)) {
						lastLines.add('\t... log truncated');
						break;
					}
					if (lastLines.size() > 500)
						lastLines.remove(0);
				}
			}
			if (lineCount > lastLines.size())
				println('\t... ' + (lineCount - lastLines.size()) + ' more');
			for (String line : lastLines) {
				println(line);
			}
			
			println('=== END Log file: ' + logFile.canonicalPath);
			if (!prop.isLocalBuild())
				println('See full logs: ' + prop.productArtifactUrlSpec);
		}
	}
	
	/**
	 * When an error occurs, this method is called by printLogsInError to scan
	 * each line in the log looking for specific patterns that might better indicate
	 * the cause of the build failure.
	 * @return true if this is the last line that should be echoed
	 */
	protected boolean scanLineFromLogInError(String line, ArrayList<String> lastLines) {
		if (line.endsWith('Compile failed; see the compiler error output for details.')) {
			if (compilationErrors != null) {
				lastLines.add('>>>>>>>> BEGIN Compilation Errors');
				lastLines.addAll(compilationErrors);
				lastLines.add('>>>>>>>> END Compilation Errors');
			}
			return true;
		}
		if (processingCompilationError) {
			if (COMPILATION_ERROR_END_PATTERN.matcher(line).find()) {
				processingCompilationError = false;
				compilationErrors.add('    [javac] \t----------');
			}
			else {
				compilationErrors.add(line);
			}
		}
		else {
			if (COMPILATION_ERROR_START_PATTERN.matcher(line).find()) {
				if (compilationErrors == null)
					compilationErrors = new ArrayList(1000);
				compilationErrors.add(line);
				processingCompilationError = true;
			}
		}
		return false;
	}

	/**
	 * Called by basicCollectLogs() to copy the logs.
	 * Subclasses may extend to collect additional logs.
	 */
	protected void basicCopyLogs(File logDir) {
		logDir.mkdirs();
		if (runner == null)
			return;
		if (runner.getConfiguration().exists())
			ant.copy(todir: logDir) {
				fileset(dir: runner.getConfiguration()) {
					include(name: "*.log"); } }
		if (runner.getWorkspaceLog().exists())
			ant.copy(file: runner.getWorkspaceLog(), todir: logDir);
		if (runner.getLogFile().exists())
			ant.copy(file: runner.getLogFile(), todir: logDir);
	}
}