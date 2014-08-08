package com.instantiations.subproducts;

import com.instantiations.eclipse.shared.installer.EclipseInstallUtils;
import com.instantiations.eclipse.shared.installer.SubProduct;
import com.instantiations.installer.core.InstallOptions;
import com.instantiations.installer.core.eclipse.EclipseInstallation;
import com.instantiations.installer.internal.core.IProductVersion;

/**
 * GEF SubProduct definition.
 * This is a Groovy template, so it has Groovy variables that are replaced at build time.
 */
public class GEFSubProduct extends SubProduct
{
	public GEFSubProduct() {
		super("GEF", "${product_version}", "${product_version}.${build_num}", "${product_id}");
	}

	public boolean canLinkTo(EclipseInstallation eclipse, InstallOptions options) {
		
		IProductVersion v = eclipse.getEclipseVersion();
		if (v.getMajor() != 3 || v.getMinor() < 2 ) {
			return false;
		}
		boolean gefPluginsFound = EclipseInstallUtils.containsGEFPlugins(eclipse, options);
		if (options.isVerbose()) {
			System.out.println("found GEF plugins (" + gefPluginsFound + ")");
		}
		// pde is installed for 3.3 that does not have the PDE loaded
		return !gefPluginsFound;
	}
}
