package com.instantiations.eclipse.shared.installer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.PluginVersionIdentifier;

import com.instantiations.installer.core.InstallOptions;
import com.instantiations.installer.core.eclipse.EclipseInstallation;
import com.instantiations.installer.core.eclipse.EclipseVersion;
import com.instantiations.installer.internal.core.IProductVersion;

/**
 * A sub product to be installed as part of a larger product.
 * <p>
 * Copyright (c) 2006, Instantiations, Inc.<br>
 * All Rights Reserved
 * 
 * @author Dan Rubel
 */
public class SubProduct
{
	static final ZipEntry[] EMPTY_ZIP_ENTRY = new ZipEntry[0];
	
	private String name;
	private String version;
	private String fullVersion;
	private String linkId;

	public SubProduct(String name, String version, String fullVersion, String linkId) {
		this.name = name;
		this.version = version;
		this.fullVersion = fullVersion;
		this.linkId = linkId;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public String getFullVersion() {
		return fullVersion;
	}

	public String getLinkId() {
		return linkId;
	}

	/**
	 * Answer the install entries for the specified version of Eclipse
	 * 
	 * @param image the install file containing code to be installed
	 * @param eclipseTarget the version of Eclipse for which install entries are desired
	 * @return an array (not null) of zero or more entries to be installed
	 */
	public ZipEntry[] getEntries(ZipFile image, IProductVersion eclipseTarget) {
		
		// Check for the install entry specific to the specified version of Eclipse
		ZipEntry entry = image.getEntry(BaseProductInstaller.INSTALL_IMAGE + "/" + getName() + "/E-" + (eclipseTarget.getMajor() + "." + eclipseTarget.getMinor()));
		if (entry == null)
			return EMPTY_ZIP_ENTRY;
		
		// Check for the install entry common to all versions of Eclipse
		ZipEntry commonEntry = image.getEntry(BaseProductInstaller.INSTALL_IMAGE + "/" + getName() + "/E-common");
		if (commonEntry == null)
			return new ZipEntry[] { entry };

		return new ZipEntry[] { entry, commonEntry };
	}

	public String getInstallDirName() {
		return getName() + "_v" + getFullVersion();
	}

	/**
	 * Determine if this sub product can be linked to the specified eclipse installation.
	 * 
	 * @param eclipse the Eclipse installation (not <code>null</code>)
	 * @param options the options object so options can be interrogated
	 * @return <code>true</code> if the sub product can be linked to the specified
	 *         eclipse installation, else <code>false</code>
	 */
	public boolean canLinkTo(EclipseInstallation eclipse, InstallOptions options) {
		return true;
	}

	/**
	 * Determine if this sub product can be linked to the specified eclipse installation
	 * and what version should be linked
	 * 
	 * @param image the install file containing code to be installed
	 * @param eclipse the Eclipse installation (not <code>null</code>)
	 * @param eclipseTarget the intended eclipse target (e.g. 3.2, 3.3, ...)
	 * @param options the options object so options can be interrogated
	 * @return <code>true</code> if the sub product can be linked to the specified eclipse
	 *         installation, else <code>false</code>
	 */
	public IProductVersion adjustEclipseTarget(ZipFile image, EclipseInstallation eclipse, IProductVersion eclipseTarget, InstallOptions options) {

		// Check if this product can be linked
		if (!canLinkTo(eclipse, options))
			return null;
		
		// Check if the entries exist for the specified version of Eclipse
		if (getEntries(image, eclipseTarget).length > 0)
			return eclipseTarget;

		// TODO [author=Dan] For now, link > 3.4 code into 3.4 Eclipse installations
		// until we successfully compile all products against > 3.5 and include that code in the installer
		if (eclipseTarget.getMajor() > 3  || (eclipseTarget.getMajor() == 3 && eclipseTarget.getMinor() > 4)) {
			EclipseVersion adjustedEclipseTarget = new EclipseVersion(3, 4, 0, null);
			if (getEntries(image, adjustedEclipseTarget).length > 0) {
				if (options.isVerbose())
					System.out.println("  adjusting " + this + " targetDir from E-" + eclipseTarget.getMajor() + "."
						+ eclipseTarget.getMinor() + " to E-" + adjustedEclipseTarget.getMajor() + "."
						+ adjustedEclipseTarget.getMinor());
				return adjustedEclipseTarget;
			}
		}
		
		// Nothing to be installed
		return null;
	}

	public String toString() {
		return "SubProduct(" + name + "," + version + "," + linkId + ")";
	}

	/**
	 * Determine if the specified "new version" is newer or equal to the "version"
	 * embedded in the path in the specified link file.
	 * @param linkFile the file to be read containing an embedded version in the path
	 * 
	 * @return <code>true</code> if the link file does not exist or the version embedded
	 *         in the path in the link file is older than the specified version
	 */
	public boolean isNewerOrEqual(File linkFile) {
		/*
		 * Read the Shared link file for specified Eclipse installation to determine
		 * currently linked version of Shared (if there is one).
		 */
		if (!linkFile.exists()) {
			System.out.println("Installing " + getName() + " because no link file: " + linkFile.getPath());
			return true;
		}
		Properties props = new Properties();
		FileInputStream in;
		try {
			in = new FileInputStream(linkFile);
		}
		catch (FileNotFoundException e) {
			return true;
		}
		try {
			props.load(in);
		}
		catch (IOException e) {
			System.out.println("Failed to read link file: " + linkFile.getPath());
			e.printStackTrace();
			return true;
		}
		finally {
			try {
				in.close();
			}
			catch (IOException e) {
				System.out.println("Failed to close link file: " + linkFile.getPath());
				e.printStackTrace();
			}
		}
		/*
		 * If the currently linked version of Shared is newer that what is being installed
		 * then don't install and link Shared
		 */
		String path = props.getProperty("path");
		if (path == null) {
			System.out.println("Link file exists but contains no path: " + linkFile.getPath());
			return true;
		}
		String prefix = getName() + "_v";
		int start = path.indexOf(prefix);
		if (start == -1) {
			System.out.println("Failed to find '" + prefix + "' in '" + path + "': " + linkFile.getPath());
			return true;
		}
		int end = path.indexOf('/', start);
		if (end == -1) {
			System.out.println("Failed to find '/' after " + prefix + " in '" + path + "': " + linkFile.getPath());
			return true;
		}
		String oldVerStr = path.substring(start + prefix.length(), end);
		System.out.println("Found " + getName() + " version " + oldVerStr + " already installed");
		String newVerStr = getFullVersion();
		System.out.println("Installer has " + getName() + " version " + newVerStr);
		if (newVerStr == null || newVerStr.equals(oldVerStr))
			return true;
		PluginVersionIdentifier oldVer = new PluginVersionIdentifier(oldVerStr);
		PluginVersionIdentifier newVer = new PluginVersionIdentifier(newVerStr);
		if (newVer.isGreaterOrEqualTo(oldVer))
			return true;
		System.out.println("Not installing/linking " + getName() + " because current version is newer.");
		return false;
	}

}
