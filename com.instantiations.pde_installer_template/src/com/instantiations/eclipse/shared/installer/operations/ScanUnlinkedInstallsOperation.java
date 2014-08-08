package com.instantiations.eclipse.shared.installer.operations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import org.eclipse.core.runtime.IStatus;

import com.instantiations.eclipse.shared.installer.BaseProductInstaller;
import com.instantiations.installer.core.InstallOptions;
import com.instantiations.installer.core.model.Context;
import com.instantiations.installer.core.model.InstallOperation;
import com.instantiations.installer.core.model.Installer;
import com.instantiations.installer.internal.core.InstallLog;
import com.instantiations.installer.internal.core.InstallLogEntry;

/**
 * Search the specified directory for installations that are not currently linked to any
 * Eclipse installation, and place that information into the install options.
 * <p>
 * Copyright (c) 2006, Instantiations, Inc.<br>
 * All Rights Reserved
 * 
 * @author Dan Rubel
 */
public class ScanUnlinkedInstallsOperation extends InstallOperation
{
	public static final String OPTION_UNLINKED_INSTALL_DIRS = "unlinkedInstallDirs";
	private final File rootDir;

	/**
	 * Construct a new instance to find unlinked installations
	 * 
	 * @param rootDir the root directory from which to scan
	 */
	public ScanUnlinkedInstallsOperation(File rootDir) {
		this.rootDir = rootDir;
	}

	/**
	 * Search for unlinked installations
	 */
	protected IStatus run(Context installer) {
		InstallOptions options = installer.getOptions();
		if (options.isVerbose())
			System.out.println("Scanning for unlinked installations");
		Collection result = new TreeSet();
		try {
			findUnlinkedDirs(rootDir, installer.getOptions(), result);
		}
		catch (ScanFailedException e) {
			System.out.println("Abort scan for unlinked installations because exception occurred");
			e.printStackTrace();
			result.clear();
		}
		StringBuffer buf = new StringBuffer(result.size() * 40);
		for (Iterator iter = result.iterator(); iter.hasNext();) {
			File installDir = (File) iter.next();
			if (buf.length() > 0)
				buf.append(",");
			buf.append(installDir.getPath());
		}
		options.set(OPTION_UNLINKED_INSTALL_DIRS, buf.toString());
		return super.run(installer);
	}

	public String toString() {
		return "Scanning for unlinked installations...";
	}

	// //////////////////////////////////////////////////////////////////////////
	//
	// Internal
	//
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * Find unlinked installation directories that have the specified parent
	 * 
	 * @param dir the directory to scan
	 * @param result a collection into which unlinked directories are placed.
	 */
	private static void findUnlinkedDirs(File dir, InstallOptions options, Collection result)
		throws ScanFailedException
	{
		if (dir == null || !dir.isDirectory())
			return;
		dir = dir.getAbsoluteFile();
		File[] children = dir.listFiles();
		boolean verbose = options.isVerbose();

		// [author=Dan] Apparently there is an issue with Java 5 reuse of file handles
		// and performing an explicit garbage collect here seems to prevent intermittent
		// "java.io.IOException: The handle is invalid." exceptions that occur when
		// reading existing log files. For more detail, see:
		// http://issues.apache.org/jira/browse/LUCENE-669
		System.gc();

		// Build a list of all Eclipse installations
		Collection eclipseDirs = new HashSet(20);
		if (verbose)
			System.out.println("Scanning " + children.length + " product installations...");
		for (int i = 0; i < children.length; i++) {
			File child = children[i];
			if (verbose)
				System.out.println("  " + child.getPath());
			findEclipseDirs(child, eclipseDirs);
		}
		String[] eclipsePaths = options.getStrings(BaseProductInstaller.OPTION_ALL_ECLIPSES_PATH_LIST);
		if (verbose)
			System.out.println("Including " + eclipsePaths.length
				+ " previously identified Eclipse installations in scan");
		for (int i = 0; i < eclipsePaths.length; i++) {
			String eachPath = eclipsePaths[i];
			if (verbose)
				System.out.println("  " + eachPath);
			eclipseDirs.add(new File(eachPath));
		}
		eclipsePaths = options.getStrings(InstallOptions.OPTION_ECLIPSE_PATH_LIST);
		if (verbose)
			System.out.println("Including " + eclipsePaths.length
				+ " previously selected Eclipse installations in scan");
		for (int i = 0; i < eclipsePaths.length; i++) {
			String eachPath = eclipsePaths[i];
			if (verbose)
				System.out.println("  " + eachPath);
			eclipseDirs.add(new File(eachPath));
		}
		if (eclipseDirs.size() == 0)
			throw new ScanFailedException("Failed to find any Eclipse installations to scan for links", null);

		// Build a list of linked installation directories
		if (verbose)
			System.out.println("Scanning " + eclipseDirs.size() + " Eclipse installations...");
		Collection linkedInstallDirs = new HashSet(20);
		for (Iterator iter = eclipseDirs.iterator(); iter.hasNext();) {
			File eclipseDir = (File) iter.next();
			if (verbose)
				System.out.println("  " + eclipseDir.getPath());
			findLinkedInstallDirs(eclipseDir, linkedInstallDirs);
		}

		// Determine which install directories are not linked
		for (int i = 0; i < children.length; i++) {
			File child = children[i];
			if (isUnlinkedInstallDir(child, linkedInstallDirs))
				result.add(child);
		}
	}

	/**
	 * Read the "install.log" file for the specified product installation to locate
	 * Eclipse installations
	 * 
	 * @param installDir the product installation directory
	 * @param result a collection into which Eclipse installation directories are placed.
	 */
	private static void findEclipseDirs(File installDir, Collection result) throws ScanFailedException {
		InstallLog log = readLog(installDir);
		if (log == null)
			return;

		// Extract Eclipse installations from the log file

		File eclipseDir;
		for (Iterator iter = log.getEntries().iterator(); iter.hasNext();) {
			InstallLogEntry entry = (InstallLogEntry) iter.next();
			switch (entry.getOperationCode()) {

				case InstallLog.LINK_CREATED_ENTRY :
					int tokenIndex = entry.getArgument().indexOf(InstallLogEntry.LINK_ENTRY_TOKEN);
					if (tokenIndex == -1)
						break;
					File linkFile = new File(entry.getArgument().substring(0, tokenIndex));
					File linksDir = linkFile.getParentFile();
					if (linksDir == null)
						break;
					eclipseDir = linksDir.getParentFile();
					if (eclipseDir == null || !eclipseDir.isDirectory())
						break;
					result.add(eclipseDir.getAbsoluteFile());
					break;

				case InstallLog.ECLIPSE_CONFIG_CLEANED_ENTRY :
					eclipseDir = new File(entry.getArgument());
					if (!eclipseDir.isDirectory())
						break;
					result.add(eclipseDir.getAbsoluteFile());
					break;
			}
		}
	}

	/**
	 * Read the log file
	 * 
	 * @param installDir the installation directory containing the log file
	 * @return the install log or <code>null</code> if it does not exist or could not be
	 *         read
	 */
	private static InstallLog readLog(File installDir) throws ScanFailedException {
		// Open the log file

		File logFile = new File(installDir, "install.log");
		if (!logFile.exists())
			return null;
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(logFile));
		}
		catch (FileNotFoundException e) {
			throw new ScanFailedException("Failed to open log file: " + logFile.getPath(), e);
		}

		// Read the log file

		InstallLog log = new InstallLog();
		try {
			log.read(reader);
		}
		catch (IOException e) {
			throw new ScanFailedException("IOException reading log file: " + logFile.getPath(), e);
		}
		finally {
			try {
				reader.close();
			}
			catch (IOException e) {
				System.out.println("Failed to close log file reader: " + logFile.getPath());
				e.printStackTrace();
			}
		}
		return log;
	}

	/**
	 * Scan the "dropins" and "links" directory of the specified installation directory and add all
	 * linked install directories to the result collection.
	 * 
	 * @param eclipseDir the Eclipse directory (not <code>null</code>)
	 * @param result the collection to which linked install directories are added
	 */
	private static void findLinkedInstallDirs(File eclipseDir, Collection result) throws ScanFailedException {
		findLinkedInstallDirs0(new File(eclipseDir, "dropins"), result);
		findLinkedInstallDirs0(new File(eclipseDir, "links"), result);
	}

	private static void findLinkedInstallDirs0(File linksDir, Collection result) throws ScanFailedException {
		if (!linksDir.isDirectory())
			return;
		File[] children = linksDir.listFiles();
		for (int i = 0; i < children.length; i++) {
			File child = children[i];
			if (!child.isFile() || !child.getName().endsWith(".link"))
				continue;
			LineNumberReader reader;
			try {
				reader = new LineNumberReader(new BufferedReader(new FileReader(child)));
			}
			catch (FileNotFoundException e) {
				throw new ScanFailedException("Failed to open link file: " + child.getPath(), e);
			}
			String line;
			try {
				line = reader.readLine();
			}
			catch (IOException e) {
				throw new ScanFailedException("Failed to read link file: " + child.getPath(), e);
			}
			finally {
				try {
					reader.close();
				}
				catch (IOException e) {
					System.out.println("Failed to close link file: " + child.getPath());
					e.printStackTrace();
				}
			}
			if (line == null || !line.startsWith("path="))
				continue;
			File installDir = new File(line.substring(5));
			if (installDir.getName().startsWith("E-"))
				installDir = installDir.getParentFile();
			result.add(installDir);
		}
	}

	/**
	 * Determine if the specified installation directory is indeed unlinked
	 * 
	 * @param installDir the installation directory
	 * @param linkedInstallDirs the linked installation directories
	 * @return <code>true</code> if installDir is unlinked
	 */
	private static boolean isUnlinkedInstallDir(File installDir, Collection linkedInstallDirs)
		throws ScanFailedException
	{
		if (installDir == null || !installDir.isDirectory() || linkedInstallDirs.contains(installDir))
			return false;

		// Check for the existing of some known file or directory
		// so that random non-product-install directories are not included in the result

		boolean logFound = false;
		boolean uninstallerFound = false;
		File[] children = installDir.listFiles();
		for (int i = 0; i < children.length; i++) {
			File child = children[i];
			String childName = child.getName();
			if (child.isDirectory()) {
				if (childName.length() == 5 && childName.startsWith("E-"))
					return true;
				if (childName.equals("eclipse")) {

					// Ignore standalone products
					if (new File(child, ".eclipseproduct").exists())
						return false;
					File[] grandChildren = child.listFiles();
					for (int j = 0; j < grandChildren.length; j++) {
						File grandChild = grandChildren[j];
						if (grandChild.getName().endsWith(".exe"))
							return false;
					}

					return true;
				}
			}
			else {

				// Ignore standalone products
				if (childName.equals(".eclipseproduct"))
					return false;
				if (childName.endsWith(".exe"))
					return false;

				if (childName.equals("install.log"))
					logFound = true;
				if (childName.equals("uninstall.jar"))
					uninstallerFound = true;
			}
		}
		if (!logFound || !uninstallerFound)
			return uninstallerFound;

		// If a log was found, but no "eclipse" or "E-#.#" subdirectories
		// then check to see if this is a umbrella install such as WindowTesterPro
		// by checking if any of its subproducts are linked

		InstallLog log = readLog(installDir);
		if (log == null)
			return false;
		for (Iterator iter = log.getEntries().iterator(); iter.hasNext();) {
			InstallLogEntry entry = (InstallLogEntry) iter.next();
			switch (entry.getOperationCode()) {
				case InstallLog.LINK_CREATED_ENTRY :
					int tokenIndex = entry.getArgument().indexOf(InstallLogEntry.LINK_ENTRY_TOKEN);
					if (tokenIndex == -1)
						break;
					File subInstallDir = new File(entry.getArgument().substring(tokenIndex + 1));
					if (subInstallDir.getName().startsWith("E-"))
						subInstallDir = subInstallDir.getParentFile();
					String subInstallName = subInstallDir.getName();
					if (subInstallName.startsWith("Shared_v") || subInstallName.startsWith("CodeProCore_v"))
						break;
					if (linkedInstallDirs.contains(subInstallDir))
						return false;
					break;
			}
		}
		return true;
	}

	// //////////////////////////////////////////////////////////////////////////
	//
	// Internal classes
	//
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * Exception used internally to wrapper an IOException and indicate that the scan
	 * should not proceed.
	 */
	private static final class ScanFailedException extends Exception
	{
		private final Throwable cause;

		public ScanFailedException(String message, Throwable cause) {
			super(message);
			this.cause = cause;
		}

		public void printStackTrace() {
			super.printStackTrace();
			if (cause != null) {
				System.err.println("--- Nested Exception ---");
				cause.printStackTrace();
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	//
	// Main method to generate a list of local unlinked installations for testing purposes
	//
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * Test to scan the Instantiations directory for unlinked directories
	 */
	public static void main(String[] args) {
		InstallOptions options = new InstallOptions();
		options.setVerbose(true);
		Installer installer = new Installer(options) {
			protected Class getRequiredAdapterType() {
				return null;
			}
		};
		
		File rootDir = new File(args.length > 0 ? args[0] : "/Program Files/Instantiations");

		ScanUnlinkedInstallsOperation op = new ScanUnlinkedInstallsOperation(rootDir);
		long startTime = System.currentTimeMillis();
		op.run(installer);
		long elapseTime = System.currentTimeMillis() - startTime;

		String[] paths = options.getStrings(OPTION_UNLINKED_INSTALL_DIRS);
		System.out.println(paths.length + " unlinked install directories found in " + elapseTime + " milliseconds");
		for (int i = 0; i < paths.length; i++)
			System.out.println("  " + trimmedPath(paths[i], rootDir));
	}

	private static String trimmedPath(File file, File commonDir) {
		return trimmedPath(file.getPath(), commonDir);
	}

	private static String trimmedPath(String path, File commonDir) {
		String prefix = commonDir.getPath();
		if (path.startsWith(prefix))
			path = path.substring(prefix.length());
		return path;
	}
}
