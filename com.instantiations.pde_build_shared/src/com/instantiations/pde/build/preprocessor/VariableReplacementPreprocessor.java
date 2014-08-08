package com.instantiations.pde.build.preprocessor;

import java.util.Collection;
import java.util.regex.Pattern;

import com.instantiations.pde.build.util.BuildProperties;
import com.instantiations.pde.build.util.BuildPropertiesException;
import com.instantiations.pde.build.util.Version;

/**
 * A line based preprocessor for replacing variables of the form ${varname} with their
 * build time values. For example, both ${build.year} and ${build_year} are replaced by
 * the actual year in which the build is occurring.
 */
public class VariableReplacementPreprocessor extends LineBasedPreprocessor
{
	private static final Pattern ECLIPSE_VERSION_PATTERN = Pattern.compile("\\$\\{eclipse(_|\\.)version\\}");

	protected final BuildProperties prop;

	public VariableReplacementPreprocessor(Version eclipseTargetVersion, BuildProperties prop) {
		super(eclipseTargetVersion);
		this.prop = prop;
		lineProcessors.add(new LineReplacement(ECLIPSE_VERSION_PATTERN, eclipseTargetVersion.toString()));
		lineProcessors.add(new VariableReplacement());
	}

	private class VariableReplacement
		implements LineProcessor
	{
		public void reset() {
		}

		public String process(String line) {
			Collection<String> ignoredKeys = prop
					.getList("preprocessor.ignore.variables");
			try {
				return prop.resolve(line, eclipseTargetVersion, ignoredKeys);
			} catch (BuildPropertiesException e) {
				throw new BuildPropertiesException(
						e.getMessage()
								+ "\n   used in "
								+ getCurrentFile().getPath()
								+ "\n   You can suppress replacement of this variable by adding this variable name"
								+ " to the preprocessor.ignore.variables property defined in the product.properties file.",
						e);
			}
		}
	}
}
