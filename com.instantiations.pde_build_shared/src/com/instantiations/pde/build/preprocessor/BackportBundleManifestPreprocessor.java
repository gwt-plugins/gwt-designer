package com.instantiations.pde.build.preprocessor;

import java.io.PrintWriter;
import java.util.regex.Pattern;

import com.instantiations.pde.build.util.Version;

public class BackportBundleManifestPreprocessor extends BundleManifestPreprocessor
{
	// Eclipse 3.4
	private static final Pattern BUNDLE_ACTIVATIONPOLICY_PATTERN = Pattern
		.compile("Bundle\\-ActivationPolicy:\\s*lazy");
	private static final Pattern SINGLETON_PATTERN = Pattern.compile(";\\s*singleton:=true");

	// Eclipse 3.3
	private static final Pattern ECLIPSE_LAZYSTART_PATTERN = Pattern.compile("Eclipse\\-LazyStart:\\s*true");

	// Eclipse 3.1
	private static final Pattern ECLIPSE_AUTOSTART_PATTERN = Pattern.compile("Eclipse\\-AutoStart:\\s*true");
	private static final Pattern BUNDLE_MANIFESTVERSION_PATTERN = Pattern.compile("Bundle\\-ManifestVersion:\\s*2");

	// Eclipse 3.0
	private static final Pattern BUNDLE_CLASSPATH_PATTERN = Pattern.compile("Bundle\\-ClassPath:");
	private static final String BUNDLE_CLASSPATH_DEFAULT = "Bundle-ClassPath: bin.jar";

	// Plugin Version replacement
	private static final Pattern VERSION_PATTERN = Pattern.compile("Bundle\\-Version:\\s*0\\.0\\.0(\\.qualifier)?");

	// Flag indicating whether Bundle-ClassPath should be added
	private boolean addDefaultBundleClasspath = false;

	/**
	 * Construct a new instance.
	 * 
	 * @param eclipseTargetVersion the version of Eclipse against which the source will be
	 *            compiled (e.g. "3.3")
	 * @param pluginVersion the version used to replace "0.0.0" and "0.0.0.qualifier"
	 */
	public BackportBundleManifestPreprocessor(Version eclipseTargetVersion, String pluginVersion) {
		super(eclipseTargetVersion);
		
		lineProcessors.add(new LineReplacement(VERSION_PATTERN, "Bundle-Version: " + pluginVersion));
		lineProcessors.add(new DetectExecutionEnvironment());
		lineProcessors.add(new DetectFragmentHost());
	
		RequireBundleModifier requireBundleModifier = new RequireBundleModifier();
		lineProcessors.add(requireBundleModifier);
	
		if (backportTo(Version.V_3_3)) {
			lineProcessors.add(new LineReplacement(BUNDLE_ACTIVATIONPOLICY_PATTERN, "Eclipse-LazyStart: true"));
			lineProcessors.add(new RemoveDuplicateLines(ECLIPSE_LAZYSTART_PATTERN));
		}
		if (backportTo(Version.V_3_1)) {
			lineProcessors.add(new LineReplacement(ECLIPSE_LAZYSTART_PATTERN, "Eclipse-AutoStart: true"));
			lineProcessors.add(new RemoveDuplicateLines(ECLIPSE_AUTOSTART_PATTERN));
			lineProcessors.add(new LineReplacement(SINGLETON_PATTERN, ""));
			addDefaultBundleClasspath = true;
			lineProcessors.add(new DetectBundleClasspath());
		}
		if (backportTo(Version.V_3_0)) {
			requireBundleModifier.addRule(new RemoveBundleRule("org.eclipse.ui.intro.universal"));
			/*
			 * Jobs exist in Eclipse 3.0, but as part of org.eclipse.core.runtime
			 * Don't add this rule if backporting to Eclipse 2.1, because it will conflict with the Eclipse 2.1 rule ...
			 * 
			 *		requireBundleModifier.addRule(new ReplaceBundleRule(
			 *			new String[] { "org.eclipse.core.jobs" },
			 *			"com.instantiations.eclipse.jobs"));
			 *
			 * ... that appears below in the Eclipse 2.1 backporting section
			 */
			// 
			if (!backportTo(Version.V_2_1)) {
				requireBundleModifier.addRule(new ReplaceBundleRule(new String[]{
					"org.eclipse.core.jobs"
				}, "org.eclipse.core.runtime"));
			}
		}
		if (backportTo(Version.V_2_1)) {
			lineProcessors.add(new RemoveLines(BUNDLE_MANIFESTVERSION_PATTERN));
			requireBundleModifier.addRule(new AddBundleRule("org.apache.xerces"));
			requireBundleModifier.addRule(new ReplaceBundleRule(new String[]{
				"org.eclipse.core.jobs"
			}, "com.instantiations.eclipse.jobs"));
			requireBundleModifier.addRule(new ReplaceBundleRule(new String[]{
				"org.eclipse.jface.text", "org.eclipse.text", "org.eclipse.ui.console", "org.eclipse.ui.editors",
				"org.eclipse.ui.forms", "org.eclipse.ui.ide", "org.eclipse.ui.intro", "org.eclipse.ui.intro.universal",
				"org.eclipse.ui.views", "org.eclipse.ui.workbench", "org.eclipse.ui.workbench.texteditor",
			}, "org.eclipse.ui"));
			requireBundleModifier.addRule(new RemoveBundleRule("org.eclipse.core.runtime.compatibility"));
			requireBundleModifier.addRule(new RemoveBundleRule("org.eclipse.help.base"));
			requireBundleModifier.addRule(new RemoveBundleRule("org.eclipse.osgi"));
			requireBundleModifier.addRule(new RemoveBundleRule("org.eclipse.update.configurator"));
			requireBundleModifier.addRule(new RemoveBundleRule("com.instantiations.eclipse.analysis.indexer"));
		}
	}

	protected void endProcess(PrintWriter writer) {

		// Eclipse 3.1 and earlier must be directory based plugins
		if (addDefaultBundleClasspath)
			writer.println(BUNDLE_CLASSPATH_DEFAULT);

		super.endProcess(writer);
	}

	private class DetectBundleClasspath
		implements LineProcessor
	{
		public void reset() {
		}

		public String process(String line) {
			if (BUNDLE_CLASSPATH_PATTERN.matcher(line).find())
				addDefaultBundleClasspath = false;
			return line;
		}
	}
}
