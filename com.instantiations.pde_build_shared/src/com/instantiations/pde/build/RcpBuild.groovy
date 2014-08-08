package com.instantiations.pde.build

import java.io.File;

import org.apache.tools.ant.BuildException;

import com.instantiations.pde.build.check.SanityCheck;
import com.instantiations.pde.build.check.SanityCheckRcp 
import com.instantiations.pde.build.subproduct.SubProductManager;
import com.instantiations.pde.build.util.BuildProperties
import com.instantiations.pde.build.util.OemVersion;
import com.instantiations.pde.build.util.RcpBuildUtil;
import com.instantiations.pde.build.util.Version/**
 * Build an RCP
 */
public class RcpBuild extends AbstractProductBuild
{
	boolean useFeatures;
	java.util.HashMap<String, String> requiredFeatures = [:];

	/**
	 * Subclasses may override to provide a sanity checker with additional behavior
	 * Specifically, the Feature and Plugin manifest editors remove all comments
	 * (and thus preprocessor statements) from the feature.xml and plugin.xml files
	 * so subclasses should sanity check their own files to ensure this has not happened.
	 */
	protected SanityCheck createSanityChecker() {
		return new SanityCheckRcp();
	}
	
	/**
	 * Called by build() to initialize and read the build properties.
	 * Subclasses can override to add/modify/replace properties.
	 */
	protected void readBuildProperties() {
		super.readBuildProperties();
		
		// Verify that the PDE config property is defined in product.properties
		// by accessing it here rather than later in the PDE Build Process
		prop.get("pde.configs");
		
	}
	
	/**
	 * load the *.product file and determine if it is feature or plug-in based
	 */
	protected void readProductFile(OemVersion targetVersion) {
		File productFile = RcpBuildUtil.locateProductFile(prop, sourceDir, targetVersion.getVersion());
		Node prod = new XmlParser().parse(productFile);
		if (prod.'@useFeatures' == 'true') {
			useFeatures = true;
			prop.set('pde.topLevelElementType', 'feature');
		}
		else {
			useFeatures = false;
			prop.set('pde.topLevelElementType', 'plugin');
		}
		if (useFeatures) {
			NodeList features = prod.'features';
			for (Node feature in features[0]) {
				printf('id: %s%nversion: %s%n', feature.'@id', feature.'@version');
				requiredFeatures.put(feature.'@id', feature.'@version');
			}
		}
	}
	
	/**
	 * If this product is a plugin-only based product (no features)
	 * then just copy plugin projects
	 */
	protected void copySourceProjects(PdeBuild builder) {
		boolean success;
		readProductFile(builder.targetVersion);
		if (useFeatures) {
			if (!builder.copyFeatureBasedProduct(prop.productId)) {
				SubProductManager manager = builder.subproductManager;
				String subProductName = manager.getNameOfSubProductContainingFeature(id);
				if (subProductName != null) {
					println("found subproduct $subProductName for $id");
				}
				success = false;
			}
		}
		else {
			Collection<String> requiredPlugins = new HashSet<String>();
			success = builder.copyPluginBasedProduct(prop.productId, true, requiredPlugins);
		}
		if (!success) {
			writeMissingProjectsLog();
			if (getMissingProjNames().size() > 0)
				throwMissingProjectsException();
		}
	}
	
	/**
	 * Called by build() to launch the specified PDE Build Process.
	 * Subclasses can override to perform pre/post processing.
	 */
	protected void launchPdeBuild(PdeBuild builder) {
		builder.launchRcpBuild();
	}
	
	/**
	 * Called by gatherResults() to wait for the external PDE Build process and gather the build deployables.
	 * May be overridden or extended by subclasses.
	 */
	protected void gatherPdeOutput(PdeBuild pdeBuilder) {
		timed('E-' + pdeBuilder.targetVersion + ' Gather RCP Zip') {
			prop.productOut.mkdirs();
			String vText = '_v' + prop.productVersion;
			if (prop.getTargetVersions().size() > 1) {
				OemVersion v = pdeBuilder.targetVersion;
				vText += '_for_' + (v.oemName ?: 'Eclipse') + v.version;
			}
			pdeBuilder.collectRcpOutput(prop.productOut, vText);
		}
	}
}
