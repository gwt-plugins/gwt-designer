package com.instantiations.pde.build.util

import org.apache.tools.ant.BuildException
import java.util.Map;
import java.io.File;
import java.net.UnknownHostException;
/**
 * Manages download and caching of SubProduct files that we build
 * such as Shared, CodeProCore, D2SWT, etc.
 * which are then consumed as part of a more encompassing build
 */
public class ProductDownloader extends BuildUtil
	implements ProductPropertiesLoader
{
	private static final long MILLIS_PER_DAY = 1000 * 60 * 60 * 24;
	
	/**
	 * Answer the relative path from the ${build.subproducts} directory
	 * to the locally cached file
	 */
	private String getLocalFilePath(String prodName, OemVersion oemVersion, String fileName) {
		String path;
		
		if (oemVersion != null)
			path = oemVersion + '/';
		else
			path = '';
		
		if (fileName != null)
			path += fileName;
		else
			path += prodName + ".zip";

		return path;
	}
	
	/**
	 * Answer the local file for the current build, which may or may not exist.
	 * Use #downloadFile to return a product file that always exists.
	 */
	public File localFile(OemVersion oemVersion, String fileName = null) {
		String path = getLocalFilePath(prop.productName, oemVersion, fileName);
		return new File(prop.productTemp, 'subproducts/' + path);
	}
	
	/**
	 * Download the product associated with the specified product name.
	 * @param prodName a product.name (e.g. "Shared", "RCPInstaller", ...)
	 * @param oemVersion the eclipse version (e.g. "3.4", "3.3", "OEM-NAME/3.2", ...).
	 * @param mustExist indicates whether an exception is throw if the file does not exist locally
	 * 				and could not be downloaded.
	 */
	public File downloadFile(String prodName, OemVersion oemVersion, String fileName = null, boolean mustExist = true, boolean alwaysDownload = false) {
		
		// If requesting a file for the product currently being built
		// then that file can be found in the temporary subproducts directory
		if (prop.productName.equals(prodName)) {
			File localFile = localFile(oemVersion, fileName);
			if (!localFile.exists() && mustExist)
				throw new BuildException('Expected subproducts file to have been already generated\n   ' + localFile.canonicalPath);
			return localFile;
		}
		
		// Otherwise, if the local file does not exist, then download it
		String path = getLocalFilePath(prodName, oemVersion, fileName);
		File localFile = new File(prop.get('build.subproducts'), prodName + '/' + path);
		if (isLocalOnly()) {
			if (mustExist && !localFile.exists())
				throw new BuildException("Missing subproduct: " + localFile.name
					+ '\n   ' + localFile.canonicalPath);
			return localFile;
		}
		if (localFile.exists() && !alwaysDownload) {
			long ageDays = (System.currentTimeMillis() - localFile.lastModified()) / MILLIS_PER_DAY;
			if (ageDays < maxAgeDays())
				return localFile;
		}
		String url = prop.get('build.download.url');
		String fileUrl = url + prodName + '/' + path;
		ant.mkdir(dir: localFile.parentFile);
		
		// If download fails, use local copy if it exists
		try {
			ant.get(src: fileUrl, dest: localFile, usetimestamp: true);
		}
		catch (Exception e) {
			String errMsg;
			if (localFile.exists())
				errMsg = ' is older than ' + maxAgeDays() + ' days, but failed to download newer version';
			else
				errMsg = ' is not cached locally and could not be downloaded';
			println('WARNING! ' + localFile.name + errMsg
				+ '\n   ' + fileUrl
				+ '\n   ' + localFile.canonicalPath
				+ '\n   ' + e.message);
			if (mustExist && !localFile.exists())
				throw new BuildException('Failed to download ' + localFile.name + ' and local file does not exist');
		}
		
		return localFile; 
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
	
	/**
	 * Because the files in the "subproducts" directory are in turn consumed by other builds, 
	 * we do not want the content of this directory to change except after a successful build. 
	 * To accomplish this, any files generated during the build process that are destine for this directory
	 * are generated into the ${build.temp}/${product.name}/subproducts directory instead. 
	 * At the end of the build process, this method is called during cleanup to move the content 
	 * of the temporary subproducts directory to ${build.subproducts}/${product.name}
	 */
	public void finalizeSubproductsDir() {
		File subproductsTempDir = new File(prop.productTemp, 'subproducts');
		File subproductsFinalDir = new File(prop.get('build.subproducts'), prop.productName);
		if (subproductsTempDir.exists()) {
			
			// Copy the subproducts files into the artifacts
//			ant.copy(todir: new File(prop.productArtifacts, 'subproducts')) {
//				fileset(dir: subproductsTempDir);
//			}
			
			// Move the subproducts files into the final location
			ant.delete(dir: subproductsFinalDir);
			ant.mkdir(dir: subproductsFinalDir.parentFile);
			ant.move(file: subproductsTempDir, tofile: subproductsFinalDir);
		}
	}
	
	//====================================================
	// ProductPropertiesLoader
	
	public Properties getProductProperties(String prodName) {
		File productPropertiesFile = downloadFile(prodName, null, 'product.properties');
		Properties productProperties = new Properties();
		productPropertiesFile.withInputStream{ stream ->
			productProperties.load(stream); }
		return productProperties;
	}
	
	public File downloadCodeProServer(String url, File destFile) {
		// download file only of it is newer on the server 
		try {
			ant.get(src: url, dest: destFile, usetimestamp: true);
		} catch (Exception e) {
			throw new BuildException('Failed to download ' + url + ' and local file ' + destFile + ' does not exist');
		}
		return destFile;
	}
}
