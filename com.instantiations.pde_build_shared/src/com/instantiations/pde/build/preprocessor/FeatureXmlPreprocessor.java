package com.instantiations.pde.build.preprocessor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.instantiations.pde.build.util.BuildProperties;
import com.instantiations.pde.build.util.Version;

/**
 * A preprocessor for feature.xml files
 */
public class FeatureXmlPreprocessor extends VersionXmlPreprocessor
{
	// Ensure that the feature version is replaced even if it is not "0.0.0" or "0.0.0.qualifier"
	private static final Pattern FEATURE_VERSION_PATTERN = Pattern.compile("version\\s*=\\s*\"\\w+\\.\\w+\\.\\w+(\\.\\w+)?\"");
	private static final Pattern UNPACK_PATTERN = Pattern.compile("unpack\\s*=\\s*\"false\"");
	
	public FeatureXmlPreprocessor(Version eclipseTargetVersion, String featureVersion, BuildProperties prop) {
		super(eclipseTargetVersion, featureVersion, prop);
		lineProcessors.add(new LineReplacementOnce(FEATURE_VERSION_PATTERN, "version=\"" + featureVersion + "\""));
		if (eclipseTargetVersion.compareTo(Version.V_3_1) <= 0)
			lineProcessors.add(new LineReplacement(UNPACK_PATTERN, "unpack=\"true\""));
	}

	/**
	 * Replace a source pattern with the replacement text
	 */
	protected class LineReplacementOnce
		implements LineProcessor
	{
		private final Pattern pattern;
		private final String replacement;
		private boolean replaced;

		public LineReplacementOnce(Pattern pattern, String replacement) {
			this.pattern = pattern;
			this.replacement = replacement;
		}

		public void reset() {
			replaced = false;
		}

		public String process(String line) {
			if (replaced)
				return line;
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				line = matcher.replaceAll(replacement);
				replaced = true;
			}
			return line;
		}
	}
}
