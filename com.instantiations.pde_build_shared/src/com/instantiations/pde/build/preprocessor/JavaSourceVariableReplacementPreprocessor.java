package com.instantiations.pde.build.preprocessor;

import java.io.File;
import java.io.IOException;

import com.instantiations.pde.build.util.BuildProperties;
import com.instantiations.pde.build.util.Version;

/**
 * Specialized subclass for performing variable replacement on specifically named *.java files
 */
public class JavaSourceVariableReplacementPreprocessor extends VariableReplacementPreprocessor
{
	public JavaSourceVariableReplacementPreprocessor(Version eclipseTargetVersion, BuildProperties prop) {
		super(eclipseTargetVersion, prop);
	}

	/**
	 * Override superclass to process only *.java files
	 */
	public boolean process(File file) throws IOException {
		if (file.getName().endsWith("ProductInfo.java"))
			return super.process(file);
		return false;
	}
}
