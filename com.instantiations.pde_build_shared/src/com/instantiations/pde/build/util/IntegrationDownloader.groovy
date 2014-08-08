package com.instantiations.pde.build.util

import org.apache.tools.ant.BuildException
import java.util.Map;
import java.net.UnknownHostException;
import java.util.regex.Pattern
import java.util.regex.Matcherimport com.instantiations.pde.build.util.URLUtil
import com.instantiations.pde.build.util.BuildUtil;/**
 * Manages download and caching of integration files that we build
 * such as WindowBuilderPro, SWTDesigner, and WindowTesterPro
 * which are then consumed as part of a more encompassing build
 */
public class IntegrationDownloader extends BuildUtil
{
	private static final long MILLIS_PER_DAY = 1000 * 60 * 60 * 24;
	
	/**
	 * Answer the local file for the specified pattern or null if it does not exist.
	 * If multiple files match the same pattern, then answer the file that would sort
	 * later in the list alphabetically.
	 */
	public File localFile(String prodName, Pattern fileNamePattern) {
		File dir = localDir(prodName);
		String[] childNames = dir.list();
		if (childNames == null)
			return null;
		String result = null;
		for (String name : childNames)
			if (fileNamePattern.matcher(name).find() && (result == null || result < name))
				result = name;
		if (result == null)
			return null;
		return new File(dir, result);
	}
	private File localDir(String prodName) {
		return new File(prop.buildOut, prodName + '/integration/latest');
	}
	
	/**
	 * Answer the remote file for the specified pattern or null if it does not exist.
	 * If multiple files match the same pattern, then answer the file that would sort
	 * later in the list alphabetically.
	 */
	public URL remoteFile(String prodName, Pattern fileNamePattern) {
		String[] childNames = getDownloadableFileNames(prodName);
		if (childNames == null)
			return null;
		String result = null;
		for (String name : childNames)
			if (fileNamePattern.matcher(name).find() && (result == null || result < name))
				result = name;
		if (result == null)
			return null;
		return new URL(remoteDirSpec(prodName) + '/' + result);
	}
	private String remoteDirSpec(String prodName) {
		return 'http://download.instantiations.com/' + prodName + '/integration/latest';
	}
	private Collection<String> getDownloadableFileNames(String prodName) {
		return URLUtil.getChildFileNames(new URL(remoteDirSpec(prodName)));
	}
	
	/**
	 * Download the product associated with the specified product name.
	 * @param prodName a product.name (e.g. "WindowBuilderPro", "SWTDesigner", "WindowTesterPro", ...)
	 * @param fileName the name of the file to download
	 */
	public File downloadFile(String prodName, Pattern fileNamePattern) {
		
		// Check if local file exists and is fairly recent
		
		File localFile = localFile(prodName, fileNamePattern);
		if (isLocalOnly()) {
			if (localFile == null || !localFile.exists())
				throw new BuildException("Missing product file: " + fileNamePattern
					+ '\n   ' + localDir(prodName));
			return localFile;
		}
		if (localFile != null && localFile.exists()) {
			long ageDays = (System.currentTimeMillis() - localFile.lastModified()) / MILLIS_PER_DAY;
			if (ageDays < maxAgeDays())
				return localFile;
		}
		
		// Download file, and if that fails, then use local copy if it exists
		
		URL remoteFileURL;
		try {
			remoteFileURL = remoteFile(prodName, fileNamePattern);
			if (remoteFileURL == null)
				return downloadFailed(remoteDirSpec(prodName), fileNamePattern, localFile, null);
		}
		catch (Exception e) {
			return downloadFailed(remoteDirSpec(prodName), fileNamePattern, localFile, e);
		}
		String downloadName = remoteFileURL.path.substring(remoteFileURL.path.lastIndexOf('/') + 1);
		File downloadFile = new File(localDir(prodName), downloadName);
		downloadFile.parentFile.mkdirs();
		try {
			ant.get(src: remoteFileURL, dest: downloadFile, usetimestamp: true);
			return downloadFile; 
		}
		catch (Exception e) {
			return downloadFailed(remoteFileURL.toExternalForm(), fileNamePattern, localFile, e);
		}
	}
	private File downloadFailed(String remoteSpec, Pattern fileNamePattern, File localFile, Exception e) {
		String errMsg;
		boolean localExists = localFile != null && localFile.exists();
		if (localExists)
			errMsg = localFile.name + ' is older than ' + maxAgeDays() + ' days, but failed to download newer version';
		else
			errMsg = 'Could not download ' + fileNamePattern + ' or find locally cached file';
		println('WARNING! ' + errMsg
			+ '\n   ' + remoteSpec
			+ '\n   ' + (localFile != null ? localFile.canonicalPath : 'localFile is null')
			+ '\n   ' + (e != null ? e.message : 'exception is null'));
		if (e != null)
			e.printStackTrace();
		if (localExists)
			return localFile;
		throw new BuildException(errMsg);
	}
	
	/**
	 * Answer true if only local files are used and no files are downloaded
	 */
	public boolean isLocalOnly() {
		return maxAgeDays() < 0;
	}
	private long maxAgeDays() {
		return Long.valueOf(prop.get('build.subproducts.age.max'));
	}

	//======================================================================
	// TEST
	
//	public static void main(String[] args) {
//		Pattern fileNamePattern = Pattern.compile('[\\w].zip');
//		BuildProperties prop = new BuildProperties();
//		prop.readDefaults();
//		IntegrationDownloader integrationCache = new IntegrationDownloader(prop: prop);
//		
//		println('======================================================');
//		Collection<String> fileNames = integrationCache.getDownloadableFileNames('WindowTesterPro');
//		for (String name : fileNames)
//			println('   ' + name);
//		println('======================================================');
//		fileNames = integrationCache.getDownloadableFileNames('D2RCP');
//		for (String name : fileNames)
//			println('   ' + name);
//		println('======================================================');
//		println(integrationCache.localDir('D2RCP'));
//		println(integrationCache.localFile('D2RCP', fileNamePattern));
//		println('======================================================');
//		println(integrationCache.remoteDirSpec('D2RCP'));
//		println(integrationCache.remoteFile('D2RCP', fileNamePattern));
//
//		println('======================================================');
//		try {
//			integrationCache.downloadFailed(integrationCache.remoteDirSpec('D2RCP'), fileNamePattern, null, null);
//			throw new RuntimeException('Expected BuildException');
//		}
//		catch (BuildException e) {
//			/* expected exception */
//		}
//
//		println('======================================================');
//		File file = integrationCache.downloadFile('D2RCP', fileNamePattern);
//		println('Downloaded ' + file);
//		
//		println('Test Complete');
//	}
}
