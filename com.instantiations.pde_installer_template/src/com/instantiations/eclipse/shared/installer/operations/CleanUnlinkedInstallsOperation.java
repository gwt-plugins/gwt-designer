package com.instantiations.eclipse.shared.installer.operations;

import java.io.File;

import com.instantiations.installer.core.InstallOptions;
import com.instantiations.installer.core.model.Context;
import com.instantiations.installer.core.model.InstallOperation;
import com.instantiations.installer.internal.core.operations.CleanDirectoryOperation;
import com.instantiations.installer.internal.core.operations.IBackupOptions;

/**
 * Search the specified directory for installations that are not currently linked to any
 * Eclipse installation.
 * <p>
 * Copyright (c) 2006, Instantiations, Inc.<br>
 * All Rights Reserved
 * 
 * @author Dan Rubel
 */
public class CleanUnlinkedInstallsOperation extends InstallOperation
{
	private final Context installer;

	/**
	 * Construct a new instance to find unlinked installations
	 * 
	 * @param rootDir the root directory from which to scan
	 */
	public CleanUnlinkedInstallsOperation(Context installer) {
		this.installer = installer;
	}

	/**
	 * Queue the installations to be deleted
	 */
	protected void prepare() throws Exception {
		InstallOptions options = installer.getOptions();
		boolean verbose = options.isVerbose();
		if (verbose)
			System.out.println("Preparing to delete old unused installations:");
		createCleanDirectoryOperation(installer.getOptions().getStrings(
			ScanUnlinkedInstallsOperation.OPTION_UNLINKED_INSTALL_DIRS), verbose, options);
		createCleanDirectoryOperation(installer.getOptions().getStrings(
			ScanUnusedInstallsOperation.OPTION_UNUSED_INSTALL_DIRS), verbose, options);
	}

	private void createCleanDirectoryOperation(String[] unlinkedInstallPaths, boolean verbose, InstallOptions options) {
		for (int i = 0; i < unlinkedInstallPaths.length; i++) {
			String installPath = unlinkedInstallPaths[i];
			if (verbose)
				System.out.println("Preparing to delete: " + installPath);
			add(new CleanDirectoryOperation(options, new File(installPath), null, false,
				IBackupOptions.OPTION_NO_BACKUP));
		}
	}

	public String toString() {
		return "Deleting unlinked installations...";
	}
}
