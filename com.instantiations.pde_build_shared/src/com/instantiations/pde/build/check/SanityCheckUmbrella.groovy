package com.instantiations.pde.build.check

import com.instantiations.pde.build.PdeBuild
import com.instantiations.pde.build.util.BuildProperties
import org.apache.tools.ant.BuildException
import com.instantiations.pde.build.preprocessor.BundleManifestReader
import java.util.regex.Pattern
import java.util.zip.ZipFile
import java.util.zip.ZipEntry

/**
 * Sanity checker for Umbrella builds
 */
public class SanityCheckUmbrella extends SanityCheck
{
	/**
	 * Check for common problems before the build
	 */
	protected void checkSourceImpl() {
		super.checkSourceImpl();
		checkBuildProperties();
	}
	
	/**
	 * Assert common build properties
	 */
	protected void checkBuildProperties() {
		checkDebugPdeProperty();
		checkSubProducts();
	}
	
	/**
	 * Check that the 'debug.pde' build property is not set on the server
	 */
	protected void checkDebugPdeProperty() {
		if (prop.isTrue('debug.pde') && prop.isHeadlessBuild())
			fail('Cannot set "debug.pde = true" in build properties on build server.');
	}
	
	/**
	 * Check the subproducts to be included in the build
	 */
	protected void checkSubProducts() {
		checkSubProducts(prop.getList('product.subproducts'));
	}
	
	/**
	 * Check the subproducts to be included in the build
	 */
	protected void checkSubProducts(Collection<String> subproducts) {
		if (subproducts == null || subproducts.isEmpty()) {
			fail('product.subproducts is undefined');
			return;
		}
		Collection<String> visited = new HashSet<String>();
		Collection<String> duplicate = new HashSet<String>();
		for (String name: new TreeSet(subproducts)) {
			if (!visited.contains(name)) {
				visited.add(name);
				checkSubProduct(name);
			}
			else {
				duplicate.add(name);
			}
		}
		if (duplicate.size() > 0) {
			fail('Duplicate subproducts specified in product.subproducts in product.properties: ' 
					+ new TreeSet<String>(duplicate));
		}
	}
	
	/**
	 * Check that the specified subproduct is a valid
	 */
	protected void checkSubProduct(String subproduct) {
		
		// This check does not make sense except on a build machine
		if (!prop.isHudsonBuild())
			return;
		
		// Check that the config.xml file exists for this subproduct
		// indicating that it is being built by Hudson
		File configFile = new File('../../../' + subproduct + '/config.xml');
		if (!configFile.exists()) {
			String errMsg = (
				'Subproduct "' + subproduct + '" specified in product.subproducts list in product.properties file is not built by Hudson'
				+ '\n   ' + configFile.path + '\n   ' + configFile.canonicalPath);
			
			// These products have not yet been moved to Hudson
			// Allow them to "pass" the sanity check for the next month
//			if (new GregorianCalendar().before(new GregorianCalendar(2009, 7, 16))) {
//				if (['D2RCP', 'HelpComposer', 'D2Core', 'D2SWT'
//				    ].contains(subproduct)) {
//					warn(errMsg)
//					return;
//				}
//			}
			
			fail(errMsg);
		}
	}
	
	//========================================================
	// Result Sanity Checking
	
	/**
	 * Check for common problems after the build
	 */
	protected void checkResultImpl() {
		super.checkResultImpl();
	}
}
