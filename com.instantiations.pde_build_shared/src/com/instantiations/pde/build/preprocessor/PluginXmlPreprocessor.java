package com.instantiations.pde.build.preprocessor;

import java.util.regex.Pattern;

import com.instantiations.pde.build.preprocessor.LineBasedPreprocessor.LineProcessor;
import com.instantiations.pde.build.util.BuildProperties;
import com.instantiations.pde.build.util.Version;

/**
 * A preprocessor for plugin.xml files
 */
public class PluginXmlPreprocessor extends VariableReplacementPreprocessor
{
	private static final Pattern ECLIPSE_VERSION_32_PATTERN = Pattern.compile("<\\?eclipse version=\"3.2\"\\?>");
	private static final Pattern ECLIPSE_VERSION_30_PATTERN = Pattern.compile("<\\?eclipse version=\"3.0\"\\?>");
	private static final String ECLIPSE_VERSION_30 = "<?eclipse version=\"3.0\"?>";

	private static final Pattern ANT_EXTRA_CLASSPATH_PATTERN = Pattern
		.compile("<extension\\s+point\\s*=\\s*\"org.eclipse.ant.core.extraClasspathEntries\"\\s*>");

	private boolean processedFile = false;
	private boolean declaresAntTasks = false;

	public PluginXmlPreprocessor(Version eclipseTargetVersion, BuildProperties prop) {
		super(eclipseTargetVersion, prop);
		if (backportTo(Version.V_3_1)) {
			lineProcessors.add(new LineReplacement(ECLIPSE_VERSION_32_PATTERN, ECLIPSE_VERSION_30));
		}
		if (backportTo(Version.V_2_1)) {
			lineProcessors.add(new RemoveLines(ECLIPSE_VERSION_30_PATTERN));
		}
		lineProcessors.add(new DetectAntTask());
	}

	/**
	 * Determine if the plugin.xml declares an Ant library with Ant tasks
	 */
	public boolean declaresAntTasks() {
		if (!processedFile)
			throw new IllegalStateException("Must process plugin.xml file before calling this method");
		return declaresAntTasks;
	}

	private class DetectAntTask
		implements LineProcessor
	{
		public DetectAntTask() {
		}

		public void reset() {
		}

		public String process(String line) {
			processedFile = true;
			if (ANT_EXTRA_CLASSPATH_PATTERN.matcher(line).find())
				declaresAntTasks = true;
			return line;
		}
	}
}
