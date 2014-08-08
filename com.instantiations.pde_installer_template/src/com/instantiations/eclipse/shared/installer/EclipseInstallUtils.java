package com.instantiations.eclipse.shared.installer;

import java.io.File;

import com.instantiations.installer.core.InstallOptions;
import com.instantiations.installer.core.eclipse.EclipseInstallation;

/**
 * Utilities used to determine if particular Eclipse installations are actually larger
 * Eclipse based products such as WebSphere Application Developer 5.1 or Rational
 * Application Developer 6.0
 * <p>
 * Copyright (c) 2006, Instantiations, Inc.<br>
 * All Rights Reserved
 * 
 * @author Dan Rubel
 */
public class EclipseInstallUtils
{
	private static final String dirSep = File.separator;
	/**
	 * Determine if the specified Eclipse installation contains the specified plugins
	 * 
	 * @param eclipse the Eclipse installation (not <code>null</code>)
	 * @param relPath relative path from the Eclipse installation
	 * @param pluginIds an array of plugin identifiers (not <code>null</code>, contains
	 *            no <code>null</code>s)
	 * @param options TODO
	 * @return <code>true</code> if the Eclipse installation contains all of the
	 *         specified plugins, else <code>false</code>
	 */
	public static boolean containsAllPlugins(EclipseInstallation eclipse, String relPath, 
												String[] pluginIds, InstallOptions options) {
		boolean ret = false;
		if (eclipse == null || pluginIds == null) {
			ret = false;
		}
		else {
			ret = containsAllPlugins(eclipse.getEclipseDir(), relPath, pluginIds, options);
		}
		return ret;
	}
		
	/**
	 * Determine if the specified Eclipse installation contains the specified plugins
	 * 
	 * @param eclipse the Eclipse installation if relPath is not null or 
	 * the plugins Directory if relPath is null(not <code>null</code>)
	 * @param relPath relative path from the Eclipse installation
	 * @param pluginIds an array of plugin identifiers (not <code>null</code>, contains
	 *            no <code>null</code>s)
	 * @param options the options for this install
	 * @return <code>true</code> if the Eclipse installation contains all of the
	 *         specified plugins, else <code>false</code>
	 */
	private static boolean containsAllPlugins(File installDir, String relPath, 
												String[] pluginIds, InstallOptions options) {
		
		boolean ret = true;
		boolean verbose = false;
		if (options != null) {
			verbose = options.isVerbose();
		}
		if (installDir == null || pluginIds == null) {
			ret = false;
		} else {
			boolean[] found = new boolean[pluginIds.length];
			StringBuffer path = new StringBuffer();
			if (relPath != null) {
				path.append(installDir).append(dirSep);
				if (relPath != null && relPath.length() > 0) {
					path.append(relPath).append(dirSep);
				}
				path.append("plugins");
			} 
			else {
				path.append(installDir);
			}
			File pluginDir = new File(path.toString());
			if (verbose) {
				System.out.println("checking directory " + path.toString());
			}
			String[] elements = pluginDir.list();
			for (int i = 0; i < pluginIds.length; i++) {
				if (verbose) {
					System.out.println("plugin (" + i + ") - " + pluginIds[i]);
				}
			}
			for (int i = 0; elements != null && i < elements.length; i++) {
				String element = elements[i];
				if (verbose) {
					System.out.println("Checking element: " + element);
				}
				int index = elementStartWith(element, pluginIds);
				if (index >= 0) {
					if (verbose) {
						System.out.println("found plugin " + pluginIds[index]);
					}
					found[index] = true;
				}
			}
			
			for (int i = 0; i < found.length; i++) {
				if (!found[i]) {
					ret = false;
				}
			}
		}
		
		return ret;
	}

	/**
	 * Determine if the specified Eclipse installation contains the GEF plugins
	 * TODO add searches in link files and in platform.xml
	 * 
	 * @param eclipse the Eclipse installation (not <code>null</code>)
	 * @param options TODO
	 * @return <code>true</code> if the Eclipse installation contains all of the
	 *         GEF plugins, else <code>false</code>
	 */
	public static boolean containsGEFPlugins(EclipseInstallation eclipse, InstallOptions options) {
		return containsAllPlugins(eclipse, options, "GEF", new String[]{
			"org.eclipse.draw2d", "org.eclipse.gef"
		});
	}

	/**
	 * Determine if the specified Eclipse installation contains the PDE plugins
	 * 
	 * @param eclipse the Eclipse installation (not <code>null</code>)
	 * @param relPath relative path from the Eclipse installation
	 * @param pluginIds an array of plugin identifiers (not <code>null</code>, contains
	 *            no <code>null</code>s)
	 * @param options TODO
	 * @return <code>true</code> if the Eclipse installation contains all of the
	 *         specified plugins, else <code>false</code>
	 */
	public static boolean containsPDEPlugins(EclipseInstallation eclipse, InstallOptions options) {
		return containsAllPlugins(eclipse, options, "PDE", new String[]{
			"org.eclipse.pde.core", "org.eclipse.pde"
		});
	}

	/**
	 * Determine if the specified Eclipse installation contains the GEF plugins
	 * TODO add searches in link files and in platform.xml
	 * 
	 * @param eclipse the Eclipse installation (not <code>null</code>)
	 * @param options TODO
	 * @param groupName the internal name for the group of plugins (e.g. "PDE", "GEF", ...)
	 * @param pluginIds an array of plugin identifiers (not null and contains no nulls)
	 * @return <code>true</code> if the Eclipse installation contains all of the
	 *         specified plugins, else <code>false</code>
	 */
	private static boolean containsAllPlugins(EclipseInstallation eclipse, InstallOptions options, String groupName, String[] pluginIds)
	{
		boolean ret = true;
		boolean verbose = false;
		if (options != null) {
			verbose = options.isVerbose();
		}
		if (eclipse == null) {
			ret = false;
		} else {
			if (verbose) {
				System.out.println("searching " + eclipse.getPluginsDir() + " for " + groupName + " Plugins");
			}
			ret = containsAllPlugins(eclipse.getPluginsDir(), null, pluginIds, options);
			if (verbose) {
				if (ret) {
					System.out.print("found");
				}
				else {
					System.out.print("did not find");
				}
				System.out.println(" " + groupName + " plugins");
			}
		}
		
		return ret;
	}

	/**
	 * @param element the element to compare with
	 * @param ids the list of id's to do the compare against
	 * @return
	 */
	private static int elementStartWith(String element, String[] ids) {
		int ret = -1;
		StringBuffer buf = new StringBuffer(80);
		for (int i = 0; i < ids.length; i++) {
			String id = ids[i];
			if (id != null) {
				buf.setLength(0);
				buf.append(id).append("_");
				
				if (element.startsWith(buf.toString())) {
					ret = i;
					break;
				}
			}
		}
		return ret;
	}
}
