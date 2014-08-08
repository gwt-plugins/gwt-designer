package com.instantiations.pde.build.preprocessor;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.instantiations.pde.build.util.Version;

public class FeatureXmlImportPreprocessor extends LineBasedPreprocessor
{
	private static final Pattern FEATURE_PATTERN = Pattern.compile("feature\\s*=\\s*\"[A-Za-z0-9_\\.]+\"");
	private static final Pattern VERSION_PATTERN = Pattern.compile("version\\s*=\\s*\"0\\.0\\.0(\\.qualifier)?\"");
	private final Map<String, String> featureVersions;

	protected FeatureXmlImportPreprocessor(Version eclipseTargetVersion, Map<String, String> featureVersions) {
		super(eclipseTargetVersion);
		this.featureVersions = featureVersions;
		lineProcessors.add(new ImportVersionLineProcessor());
	}
	
	/**
	 * Process all feature manifest files 
	 */
	public void processAllFeatureManifests(File featuresDir) throws IOException {
		for (File dir : featuresDir.listFiles()) {
			File file = new File(dir, "feature.xml");
			if (file.exists())
				process(file);
		}
	}

	private final class ImportVersionLineProcessor
		implements LineProcessor
	{
		public void reset() {
		}
		
		public String process(String line) {
			if (!line.trim().startsWith("<import"))
				return line;
			Matcher matcher = FEATURE_PATTERN.matcher(line);
			if (!matcher.find())
				return line;
			int start = line.indexOf('"', matcher.start());
			if (start == -1)
				return line;
			int end = line.indexOf('"', start + 1);
			if (end == -1)
				return line;
			String featureId = line.substring(start + 1, end);
			String version = featureVersions.get(featureId);
			if (version == null)
				return line;
			matcher = VERSION_PATTERN.matcher(line);
			if (!matcher.find())
				return line;
			return matcher.replaceAll("version=\"" + version + "\"");
		}
	}
}
