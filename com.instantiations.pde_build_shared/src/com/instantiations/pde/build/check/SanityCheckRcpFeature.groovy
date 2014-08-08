/**
 * 
 */
package com.instantiations.pde.build.check

import org.apache.tools.ant.BuildException 

/**
 * @author markr
 *
 */
class SanityCheckRcpFeature extends SanityCheckFeature {
	@Override
	/**
	 * Check for common problems.
	 * @see SanityCheck.checkSourceImpl()
	 */
	protected void checkSourceImpl() {
		super.checkSourceImpl();
		checkPrimaryFeatureExists();
	}


	/**
	 * sanity check to see if the primary product exists
	 */
	protected void checkPrimaryFeatureExists() {
		File primaryFeature = new File(sourceDir, prop.productId + '_feature');
		
		if (!primaryFeature.exists()) {
			throw new BuildException('could not find the primary feature ' + primaryFeature.canonicalPath + 'for the RCP')
		}
	}

}
