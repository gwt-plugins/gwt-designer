package com.instantiations.eclipse.shared.installer.operations;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;

import com.instantiations.installer.core.InstallOptions;
import com.instantiations.installer.core.model.Context;
import com.instantiations.installer.core.model.InstallOperation;

/**
 * Scan the install directory for directories similar to the specified top level product
 * directory that should be removed, and place that information into the install options.
 * <p>
 * Copyright (c) 2008, Instantiations, Inc.<br>
 * All Rights Reserved
 * 
 * @author Dan Rubel
 */
public class ScanUnusedInstallsOperation extends InstallOperation
{
	public static final String OPTION_UNUSED_INSTALL_DIRS = "unusedInstallDirs";
	private final File rootDir;
	private final String prefix;
	private final String exclude;

	/**
	 * Construct a new instance to find directories in the specified root directory that
	 * have the same prefix but a different suffix.
	 * 
	 * @param rootDir the root directory from which to scan
	 * @param prefix the directory name prefix
	 * @param exclude the directory name to exclude from the results
	 */
	public ScanUnusedInstallsOperation(File rootDir, String prefix, String exclude) {
		this.rootDir = rootDir;
		this.prefix = prefix;
		this.exclude = exclude;
	}

	/**
	 * Search for unused directories
	 */
	protected IStatus run(Context installer) {
		InstallOptions options = installer.getOptions();
		boolean verbose = options.isVerbose();
		if (verbose)
			System.out.println("Scanning for unused directories");
		Collection result = findDirsMatching(verbose);
		StringBuffer buf = new StringBuffer(result.size() * 40);
		for (Iterator iter = result.iterator(); iter.hasNext();) {
			File installDir = (File) iter.next();
			if (buf.length() > 0)
				buf.append(",");
			buf.append(installDir.getPath());
		}
		options.set(OPTION_UNUSED_INSTALL_DIRS, buf.toString());
		return super.run(installer);
	}

	public String toString() {
		return "Scanning for unused directories...";
	}

	// //////////////////////////////////////////////////////////////////////////
	//
	// Internal
	//
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * Scan the {@link #rootDir} for files (directories) with the specified
	 * {@link #prefix} but not matching {@link #exclude}
	 * 
	 * @param verbose <code>true</code> if verbose output should be generated
	 * @return a list of file (directory) names
	 * @post $result != null
	 */
	public List findDirsMatching(boolean verbose) {
		List ret = new ArrayList();
		File[] wrapperDirs = rootDir.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String file) {
				return file.trim().startsWith(prefix);
			}

		});
		if (verbose) {
			System.out.println("found " + wrapperDirs.length + " directories to process");
		}
		for (int i = 0; i < wrapperDirs.length; i++) {
			File dir = wrapperDirs[i];
			if (dir.getName().equals(exclude)) {
				if (verbose) {
					System.out.println(" * excluding " + dir);
				}
			}
			else {
				if (verbose) {
					System.out.println("   including " + dir);
				}
				ret.add(dir);
			}
		}
		return ret;
	}
}
