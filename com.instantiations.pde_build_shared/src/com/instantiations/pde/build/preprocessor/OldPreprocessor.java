package com.instantiations.pde.build.preprocessor;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import com.instantiations.pde.build.util.Version;

/**
 * Line based preprocessor for converting old style preprocessor statements into new
 * "prebop" style preprocessor statements.
 */
public class OldPreprocessor extends LineBasedPreprocessor
{
	private static final Pattern OLD_IF_PATTERN = Pattern.compile("codepro\\.preprocessor\\.if version");
	private static final Pattern OLD_ELSEIF_PATTERN = Pattern.compile("codepro\\.preprocessor\\.elseif version");
	private static final Pattern OLD_ENDIF_PATTERN = Pattern.compile("codepro\\.preprocessor\\.endif\\s*");

	protected OldPreprocessor(Version eclipseTargetVersion) {
		super(eclipseTargetVersion);
		lineProcessors.add(new LineReplacement(OLD_IF_PATTERN, "if eclipse.version"));
		lineProcessors.add(new LineReplacement(OLD_ELSEIF_PATTERN, "elseif eclipse.version"));
		lineProcessors.add(new LineReplacement(OLD_ENDIF_PATTERN, "endif"));
	}

	/**
	 * Override superclass to process only *.java files
	 */
	public boolean process(File file) throws IOException {
		String name = file.getName();
		if (name.endsWith(".java") || name.equals("feature.xml") || name.equals("plugin.xml") || name.equals("fragment.xml"))
			return super.process(file);
		return false;
	}
}
