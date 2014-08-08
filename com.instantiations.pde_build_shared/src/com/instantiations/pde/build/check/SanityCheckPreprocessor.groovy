package com.instantiations.pde.build.check

import com.instantiations.pde.build.util.BuildProperties
import com.instantiations.pde.build.util.BuildUtilimport groovy.xml.StreamingMarkupBuilderimport groovy.xml.MarkupBuilderimport java.util.regex.Pattern
/**
 * Sanity check that preprocessor statements exist in specified files.
 * The history for preprocessor statement existance is in
 * ${build.artifacts}/${product.name}/preprocessor-history.xml
 */
public class SanityCheckPreprocessor extends BuildUtil
{
	private static final Pattern PREPROCESSOR_STATEMENT_PATTERN = Pattern.compile('\\$(if|elseif|else|endif)');

	// Must be initialized by caller
	SanityCheck check;			// The receiver calls check.fail(...) as necessary
	File sourceDir;				// The workspace directory containing the projects to be built
	BuildProperties prop;		// The build properties
	
	// Used internally
	File historyFile;			// File storing the preprocessor history
	Map history;				// Mapping of workspace path to # of preprocessor statements
	
	/**
	 * Assert that if the file had preprocessor statements before,
	 * that it still has preprocessor statements now.
	 */
	public void assertStatementsNotRemoved(File file, boolean failOnError = true) {
		int actualCount = getActualStatementCount(file);
		int historicalCount = getHistoricalStatementCount(file);
		if (actualCount == 0 && historicalCount > 0) {
			String errMsg = file.name + ' previously had ' + historicalCount + ' preprocessor statements, but now has none';
			if (failOnError)
				check.fail(errMsg);
			else
				check.warn(errMsg)
		}
		if (actualCount > 0)
			setHistoricalStatementCount(file, actualCount);
	}

	/**
	 * Answer the actual number of preprocessor statements
	 */
	protected int getActualStatementCount(File file) {
		int count = 0;
		file.withReader { reader ->
			while (true) {
				String line = reader.readLine();
				if (line == null)
					break;
				if (PREPROCESSOR_STATEMENT_PATTERN.matcher(line).find())
					count++;
			}
		}
		return count;
	}
	
	/**
	 * Answer the historical number of preprocessor statements
	 */
	protected int getHistoricalStatementCount(File file) {
		loadHistory();
		String path = getWorkspacePath(file);
		String value = history.get(path);
		if (value == null)
			return 0;
		return Integer.parseInt(value);
	}
	
	/**
	 * Set the historical number of preprocessor statements
	 */
	protected void setHistoricalStatementCount(File file, int count) {
		String path = getWorkspacePath(file);
		history.put(path, Integer.toString(count));
		storeHistory();
	}
	
	/**
	 * Answer the path of the file relative to the workspace
	 */
	protected String getWorkspacePath(File file) {
		String workspacePath = sourceDir.canonicalPath;
		String fullPath = file.canonicalPath;
		if (!fullPath.startsWith(workspacePath))
			check.fail('File not in workspace:\n   ' + fullPath + '\n   ' + workspacePath);
		String path = fullPath.substring(workspacePath.length());
		if (path.startsWith(File.separator))
			path = path.substring(1);
		return path;
	}
	
	/**
	 * Read the history if not already loaded
	 */
	 protected void loadHistory() {
		if (history != null)
			return;
		historyFile = new File(sourceDir, 'preprocessor-history.xml');

		// Move the old history file to its new home
		File oldHistoryFile = new File(prop.buildArtifacts, prop.productName + '/preprocessor-history.xml');
		if (oldHistoryFile.exists()) {
			if (!historyFile.exists()) {
				ant.move(file: oldHistoryFile, tofile: historyFile);
			}
			oldHistoryFile.delete();
		}
		
		history = new HashMap();
		if (!historyFile.exists())
			return;
		new XmlParser().parse(historyFile).children().each{
			history.put(it.'@path', it.'@statementCount');
		}
	}
	
	/**
	 * Write the history file
	 */
	 protected void storeHistory() {
		historyFile.parentFile.mkdirs();
		historyFile.withWriter { writer ->
			new MarkupBuilder(writer).preprocessorHistory(product: prop.productName) {
				new TreeSet(history.keySet()).each { path ->
					preprocessedFile(path: path, statementCount: history.get(path));
				}
			}
		}
	}
}
