package com.instantiations.pde.build.util;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Read the versions of the features in the zip file
 */
public class FeatureVersionZipReader
{
	/**
	 * Map feature identifier to feature version string
	 */
	private Map<String, String> featureVersionMap = new HashMap<String, String>();
	
	/**
	 * Scan the zip file for feature directories 
	 * and fromt them, extract the feature identifier and version 
	 */
	public void readZip(File file) throws IOException {
		if (!file.getName().endsWith(".zip")) {
			System.out.println("Cannot extract feature version information from non *.zip files\n   " 
				+ file.getCanonicalPath());
			return;
		}
		ZipFile zip = new ZipFile(file);
		try {
			Enumeration<? extends ZipEntry> enumeration = zip.entries();
			while (enumeration.hasMoreElements())
				readZipEntry(enumeration.nextElement());
		}
		finally {
			zip.close();
		}
	}

	/**
	 * Determine if this entry is for the feature manifest
	 * and if so, extract the feature identifier and version from the name
	 */
	private void readZipEntry(ZipEntry entry) {
		String name = entry.getName();
		int end;
		if (name.endsWith("/feature.xml"))
			end = name.length() - 12;
		else if (name.endsWith("/META-INF/MANIFEST.MF") && name.indexOf("features/") != -1)
			end = name.length() - 21;
		else
			return;
		int mid = name.lastIndexOf('.', end - 1);
		if (mid == -1)
			return;
		mid = name.lastIndexOf('_', mid);
		if (mid == -1)
			return;
		int start = name.lastIndexOf('/', end - 1);
		if (start == -1 || mid < start)
			return;
		String id = name.substring(start + 1, mid);
		String version = name.substring(mid + 1, end);
		featureVersionMap.put(id, version);
	}
	
	/**
	 * After calling {@link #readZip(File)} one or more times, call this method to
	 * return a map (not <code>null</code>) of feature identifiers to feature version strings
	 */
	public Map<String, String> getFeatureVersions() {
		return featureVersionMap;
	}
}
