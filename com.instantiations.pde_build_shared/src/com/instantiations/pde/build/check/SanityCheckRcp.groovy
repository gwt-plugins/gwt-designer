
/**
 * 
 */
package com.instantiations.pde.build.check

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.tools.ant.BuildException 

import com.instantiations.pde.build.util.OemVersion;
import com.instantiations.pde.build.util.RcpBuildUtil;
import com.instantiations.pde.build.util.Version;

/**
 * @author markr
 *
 */
class SanityCheckRcp extends SanityCheck {
	Boolean useFeatures = false;
	SanityCheck sanityChecker = null;
	
	protected void createSanityChecker() {
		if (sanityChecker == null) {
			readProductFile();
			if (useFeatures) {
				sanityChecker = new SanityCheckRcpFeature();
			}
			else {
				sanityChecker = new SanityCheckRcpPlugin();
			}
			sanityChecker.setSourceDir(sourceDir);
			sanityChecker.setProp(prop);
			sanityChecker.checkSource();
		}
	}
	
	@Override
	/**
	 * Check for common problems.  
	 * @see SanityCheck.checkResultImpl()
	 */
	protected void checkResultImpl() {
		super.checkResultImpl();
	}

	@Override
	/**
	 * Check for common problems.
	 * @see SanityCheck.checkSourceImpl()
	 */
	protected void checkSourceImpl() {
		createSanityChecker()
		checkProductFile();
		sanityChecker.checkSourceImpl();
	}

	/**
	 * load the *.product file and determine if it is feature or plug-in based
	 * @param targetVersion the version of Eclipse used to determine the product file or 
	 * null indicating use the first version found in property target.versions
	 */
	protected void readProductFile(OemVersion targetVersionIn = null) {
		OemVersion targetVersion = null;
		if (targetVersionIn == null) {
			List<OemVersion> targetVersions = prop.targetVersions;
			targetVersion = targetVersions.get(0);
		}
		else {
			targetVersion = targetVersionIn;
		}
		File productFile = RcpBuildUtil.locateProductFile(prop, sourceDir, targetVersion.getVersion());
		Node prod = new XmlParser().parse(productFile);
		if (prod.'@useFeatures' == 'true') {
			useFeatures = true;
		}
	}

	/**
	 * a sanity check to make sure all of the eclipse specific product Files have the savme value for 
	 * attribute useFeatures
	 */
	protected void checkProductFile() {
		Boolean useFeatureSaved = useFeatures;
		List<OemVersion> targetVersions = prop.targetVersions;
		// this is an invalid version of Eclipse and will force the generic product file (if one exists) to be loaded 
//		targetVersions.add(new OemVersion(null, Version.V_1_3));
		for (OemVersion oemVersion in targetVersions) {
			readProductFile(oemVersion);
			if (useFeatureSaved != useFeatures) {
				File productFile = RcpBuildUtil.locateProjectFile(prop, sourceDir, oemVersion.getVersion());
				fail('In the product file ' + productFile + ' the value of useFeatures differs from the others',
						'please lok at the value of useFeatures in ' + productFile + ' and make sure it is the same as the rest');	
			}
		}
		try {
			// look for a 
			OemVersion oemVersion = new OemVersion(null, Version.V_1_3);
			readProductFile(oemVersion);
			if (useFeatureSaved != useFeatures) {
				File productFile = RcpBuildUtil.locateProjectFile(prop, sourceDir, oemVersion.getVersion());
				fail('In the product file ' + productFile + ' the value of useFeatures differs from the others',
						'please lok at the value of useFeatures in ' + productFile + ' and make sure it is the same as the rest');	
			}
		}
		catch (BuildException e) {
			// this is ignored because 1,3 is an invalid version of eclipse so it will return the base product file if one exists
			//  if this exception gets thrown then it means there is no base product file
		}
	}
}
