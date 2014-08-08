package com.instantiations.subproducts;

import com.instantiations.eclipse.shared.installer.EclipseInstallUtils;
import com.instantiations.eclipse.shared.installer.SubProduct;
import com.instantiations.installer.core.InstallOptions;
import com.instantiations.installer.core.eclipse.EclipseInstallation;
import com.instantiations.installer.internal.core.IProductVersion;

/**
 * PDE SubProduct definition.
 * This is a Groovy template, so it has Groovy variables that are replaced at build time.
 */
public class PDESubProduct extends SubProduct
{
	public PDESubProduct() {
		super("PDE", "${product_version}", "${product_version}.${build_num}", "${product_id}");
	}

	public boolean canLinkTo(EclipseInstallation eclipse, InstallOptions options) {
		IProductVersion eclipseVersion = eclipse.getEclipseVersion();

		// If this is Eclipse 3.3 or 3.4 and does not have PDE (e.g. Java distro)
		// then install and link our own PDE plugins
		
		if (eclipseVersion.getMajor() == 3) {
			if (eclipseVersion.getMinor() == 3 || eclipseVersion.getMinor() == 4) {
				boolean pdePluginsFound = EclipseInstallUtils.containsPDEPlugins(eclipse, options);
				if (options.isVerbose())
					System.out.println("found PDE plugins (" + pdePluginsFound + ")");
				return !pdePluginsFound;
			}
		}

		return false;
	}
}
