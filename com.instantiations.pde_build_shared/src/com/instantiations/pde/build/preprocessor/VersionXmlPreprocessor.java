package com.instantiations.pde.build.preprocessor;

import java.util.regex.Pattern;

import com.instantiations.pde.build.util.BuildProperties;
import com.instantiations.pde.build.util.Version;

/**
 * A preprocessor for XML files for replacing "0.0.0" and "0.0.0.qualifier" 
 */
public class VersionXmlPreprocessor extends VariableReplacementPreprocessor
{
	private static final Pattern VERSION_PATTERN = Pattern.compile("version\\s*=\\s*\"0\\.0\\.0(\\.qualifier)?\"");

	/**
	 * Construct a new instance.
	 * @param version the version used to replace "0.0.0" and "0.0.0.qualifier"
	 * @param prop the build properties
	 */
	public VersionXmlPreprocessor(Version eclipseTargetVersion, String version, BuildProperties prop) {
		super(eclipseTargetVersion, prop);
		lineProcessors.add(new NonImportLineReplacement(VERSION_PATTERN, "version=\"" + version + "\""));
	}

	/**
	 * Replace version="0.0.0" except in import declarations
	 */
	private final class NonImportLineReplacement extends LineReplacement {
		private NonImportLineReplacement(Pattern pattern, String replacement) {
			super(pattern, replacement);
		}
		public String process(String line) {
			if (line.trim().startsWith("<import"))
				return line;
			return super.process(line);
		}
	}
}
