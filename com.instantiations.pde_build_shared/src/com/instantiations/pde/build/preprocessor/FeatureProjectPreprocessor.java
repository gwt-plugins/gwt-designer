package com.instantiations.pde.build.preprocessor;

import java.io.File;
import java.io.IOException;

import com.instantiations.pde.build.util.BuildProperties;
import com.instantiations.pde.build.util.OemVersion;
import com.objfac.prebop.PreprocessorError;

/**
 * Preprocess feature source projects from one version of Eclipse to another.
 */
public class FeatureProjectPreprocessor
{
	private final OemVersion targetVersion;
	private final String featureVersion;
	private final BuildProperties prop;

	/**
	 * Construct a new instance to preprocess a plugin source project
	 * 
	 * @param targetVersion the version of Eclipse against which the source will be
	 *            compiled (e.g. "3.3", "OEM-NAME/3.2")
	 * @param featureVersion the version used to replace "0.0.0" and "0.0.0.qualifier"
	 * @param prop the build properties
	 */
	public FeatureProjectPreprocessor(OemVersion targetVersion, String featureVersion, BuildProperties prop) {
		this.targetVersion = targetVersion;
		this.featureVersion = featureVersion;
		this.prop = prop;
	}

	public void processFeatureManifest(File dstProj) throws IOException, PreprocessorError {

		// Process the feature.xml file
		File featureXmlFile = new File(dstProj, "feature.xml");
		FeatureXmlPreprocessor featureProcessor = new FeatureXmlPreprocessor(targetVersion.getVersion(),
			featureVersion, prop);
		featureProcessor.process(featureXmlFile);
		new OldPreprocessor(targetVersion.getVersion()).process(featureXmlFile);
		new PrebopPreprocessor(targetVersion.getOemName(), targetVersion.getVersion()).preprocessFile(featureXmlFile);

		// Process the feature.properties file if it exists
		File featurePropFile = new File(dstProj, "feature.properties");
		if (featurePropFile.exists()) {
			PropertiesFilePreprocessor propProcessor = new PropertiesFilePreprocessor(targetVersion, prop);
			propProcessor.process(featurePropFile);
		}
	}

	public void processProductFile(File productFile) throws IOException {
		ProductFilePreprocessor processor = new ProductFilePreprocessor(targetVersion.getVersion(), featureVersion, prop);
		processor.process(productFile);
	}
}
