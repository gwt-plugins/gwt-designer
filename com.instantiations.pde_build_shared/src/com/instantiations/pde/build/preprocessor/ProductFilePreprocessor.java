package com.instantiations.pde.build.preprocessor;

import com.instantiations.pde.build.util.BuildProperties;
import com.instantiations.pde.build.util.Version;

/**
 * A preprocessor for *.product files
 */
public class ProductFilePreprocessor extends VersionXmlPreprocessor
{
	public ProductFilePreprocessor(Version eclipseTargetVersion, String version, BuildProperties prop) {
		super(eclipseTargetVersion, version, prop);
	}
}
