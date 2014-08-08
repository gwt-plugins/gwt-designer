package com.instantiations.pde.build.check;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Verify the obfuscation of the specified jar file by searching each class file there in
 * for the specified string. In our case, we search the com.instantiations.common.core
 * plugin jar for the string "com.instantiations.common.internal" because all internal
 * classes in that plugin should be obfuscated. If a match is found, then throw a build
 * exception because it probably means that the obfuscator has improperly converted a
 * class file.
 * <p>
 * Copyright (c) 2006, 2009, Instantiations, Inc.<br>
 * All Rights Reserved
 * 
 * @author Dan Rubel
 */
public class SanityCheckObfuscation
{
	private ZipFile zipFile;
	private int errorCount;

	/**
	 * Scan the specified source directory and jar file to find classes with "internal" in
	 * their package name that have not been obfuscated
	 * 
	 * @return <code>true</code> if at least once class was found that should have been
	 *         obfuscated.
	 */
	public boolean find(File srcDir, File jarFile) throws IOException {
		errorCount = 0;
		try {
			zipFile = new ZipFile(jarFile);
		}
		catch (IOException e) {
			System.out.println("Failed to open jar file" + "\n   " + jarFile.getCanonicalPath() + "\n   "
				+ e.toString());
			errorCount++;
			zipFile = null;
		}
		if (zipFile != null) {
			try {
				scan(srcDir, null);
				scanForObfuscatedClasses();
			}
			finally {
				zipFile.close();
			}
		}
		return errorCount > 0;
	}

	/**
	 * Recursively scan the specified source directory
	 */
	private void scan(File srcDir, String path) {
		for (String childName : srcDir.list()) {
			if (childName.equalsIgnoreCase(".svn")) {
				continue;
			}
			File child = new File(srcDir, childName);
			String childPath = childName + "/";
			if (path != null)
				childPath = path + childPath;
			if (child.isDirectory()) {
				scan(child, childPath);
				continue;
			}
			if (childName.endsWith(".java") && childPath.indexOf("/internal/") > 0) {
				String entryPath = childPath.substring(0, childPath.length() - 6) + ".class";
				if (zipFile.getEntry(entryPath) != null) {
					if (errorCount == 0)
						System.out.println("Found unobfuscated entries in " + zipFile.getName());
					fail("   " + entryPath);
				}
			}
		}
	}

	/**
	 * Scan the zip file for an obfuscated class.
	 * Report an error if no obfuscated classes are found.
	 */
	private void scanForObfuscatedClasses() {
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (entry.getName().endsWith("A/A/A.class"))
				return;
		}
		fail("Failed to find any obfuscated classes");
	}

	private void fail(String errMsg) {
		errorCount++;
		System.out.println(errMsg);
	}
}
