package com.instantiations.pde.build.util

import com.instantiations.pde.build.util.BuildProperties
import org.apache.tools.ant.BuildExceptionimport org.apache.tools.ant.util.JavaEnvUtils
/**
 * Manages download and caching of file that we do not build ourselves
 * such as the Eclipse SDK
 */
public class FileDownloader extends BuildUtil
{
	/**
	 * Download the file associated with the specified key.
	 * The key is composed of the specified identifier
	 * (e.g. "eclipse-sdk", "delta-pack", feature id, ...),
	 * and eclipse version (e.g. "3.4", "3.3", "3.2", ...),
	 * along with (optionally) os, ws, and arch.
	 * The search is made for the most specific key first,
	 * which includes the os, ws, and arch
	 * then for the more general without those suffixes.
	 */
	public File download(String id, Version version) {
		return download(id, version.toString());
	}
	public File download(String id, String version) {
		String url = prop.getWithTrailingSlash(id + "." + version + ".url");
		String fileName = prop.getPlatformSpecific(id + "." + version, ".zip");
		return downloadFile(url + fileName, fileName);
	}
	
	/**
	 * Download the artifacts.jar file from the update site
	 * associated with the specified key.
	 * The key is composed of the specified identifier (e.g. "gpe", ...),
	 * and eclipse version (e.g. "3.4", "3.3", "3.2", ...).
	 * For example, if id = "gpe" and version = "3.6"
	 * then there must be a build property defined
	 * gpe.3.6.url = http://dl.google.com/eclipse/plugin/3.6
	 * 
	 * @param siteId the identifier for the build property defining the URL for the update site
	 * @param eclipseVersion the Eclipse version of the update site
	 * @return the artifacts.xml file
	 */
	public File downloadUpdateSiteArtifactsJar(String siteId, Version eclipseVersion) {
		String siteRelPath = 'updatesite/' + siteId + '/' + eclipseVersion + '/';
		String url = prop.getWithTrailingSlash(siteId + "." + eclipseVersion + ".url");
		return downloadFile(url + 'artifacts.jar', siteRelPath + 'artifacts.jar', true);
	}
	
	/**
	 * Download the specified feature jar from the update site
	 * associated with the specified key.
	 * The key is composed of the specified identifier (e.g. "gpe", ...),
	 * and eclipse version (e.g. "3.4", "3.3", "3.2", ...).
	 * For example, if id = "gpe" and version = "3.6"
	 * then there must be a build property defined
	 * gpe.3.6.url = http://dl.google.com/eclipse/plugin/3.6
	 * 
	 * @param siteId the identifier for the build property defining the URL for the update site
	 * @param eclipseVersion the Eclipse version of the update site
	 * @param featureId the feature identifier
	 * @param featureVersion the feature version
	 */
	public File downloadUpdateSiteFeatureJar(String siteId, Version eclipseVersion, String featureId, String featureVersion) {
		String siteRelPath = 'updatesite/' + siteId + '/' + eclipseVersion + '/';
		String featureRelUrl = 'features/' + featureId + '_' + featureVersion + '.jar';
		String featureUrl = prop.getWithTrailingSlash(siteId + '.' + eclipseVersion + '.url') + featureRelUrl;
		return downloadFile(featureUrl, siteRelPath + featureRelUrl);
	}
	
	/**
	 * Download the specified plugin jar from the update site
	 * associated with the specified key.
	 * The key is composed of the specified identifier (e.g. "gpe", ...),
	 * and eclipse version (e.g. "3.4", "3.3", "3.2", ...).
	 * For example, if id = "gpe" and version = "3.6"
	 * then there must be a build property defined
	 * gpe.3.6.url = http://dl.google.com/eclipse/plugin/3.6
	 *
	 * @param siteId the identifier for the build property defining the URL for the update site
	 * @param eclipseVersion the Eclipse version of the update site
	 * @param pluginId the plugin identifier
	 * @param pluginVersion the plugin version
	 */
	public File downloadUpdateSitePluginJar(String siteId, Version eclipseVersion, String pluginId, String pluginVersion) {
		String siteRelPath = 'updatesite/' + siteId + '/' + eclipseVersion + '/';
		String pluginRelUrl = 'plugins/' + pluginId + '_' + pluginVersion + '.jar';
		String pluginUrl = prop.getWithTrailingSlash(siteId + '.' + eclipseVersion + '.url') + pluginRelUrl;
		return downloadFile(pluginUrl, siteRelPath + pluginRelUrl);
	}
	
//	/**
//	 * Download the specified feature directory from the update site
//	 * associated with the specified key.
//	 * The key is composed of the specified identifier (e.g. "gpe", ...),
//	 * and eclipse version (e.g. "3.4", "3.3", "3.2", ...).
//	 * For example, if id = "gpe" and version = "3.6"
//	 * then there must be a build property defined
//	 * gpe.3.6.url = http://dl.google.com/eclipse/plugin/3.6
//	 * 
//	 * @param siteId the identifier for the build property defining the URL for the update site
//	 * @param eclipseVersion the Eclipse version of the update site
//	 * @return the downloaded feature directory
//	 */
//	public File downloadUpdateSiteFeatureDir(String siteId, Version eclipseVersion, String featureId, String featureVersion) {
//		String siteRelPath = 'updatesite/' + siteId + '/' + eclipseVersion + '/';
//		String featureRelUrl = 'features/' + featureId + '_' + featureVersion + '/';
//		String featureRelPath = siteRelPath + featureRelUrl;
//		File featureDir = new File(prop.getWithTrailingSlash('build.downloads') + featureRelPath);
//		System.out.println('>>> does local feature dir end with slash??\n   ' + featureDir.name);
//		if (featureDir.exists())
//			return featureDir;
//		ant.mkdir(dir:featureDir);
//		
//		String featureUrl = prop.getWithTrailingSlash(siteId + '.' + eclipseVersion + '.url') + featureRelUrl;
//		downloadFile(featureUrl + 'feature.xml',        featureRelPath + 'feature.xml');
//		downloadFile(featureUrl + 'feature.properties', featureRelPath + 'feature.properties', false, false);
//		
//		return featureDir;
//	}
	
	/**
	 * Download the specified file from the specified URL
	 * if it has not already been downloaded and cached
	 * 
	 * @param url the URL specifying the file to be downloaded
	 * @param filename the name or relative path of the file into which the bits will be placed
	 * @param alwaysDownload true if the file should always be downloaded regardless of whether it is already cached
	 * 		(default value = false)
	 * @param mustExist true if this method should throw an exception if the file cannot be downloaded and is not already cached 
	 * 		(default value = true)
	 * @return the downloaded file
	 */
	private File downloadFile(String url, String filename, boolean alwaysDownload = false, boolean mustExist = true) {
		File cacheFile = new File(prop.get("build.cache"), filename);
		if (!cacheFile.exists() || alwaysDownload) {
			ant.mkdir(dir:cacheFile.parentFile);
			File downloadFile = new File(prop.get("build.downloads"), filename);
			boolean exists = downloadFile.exists();
			if (!exists || alwaysDownload) {
				ant.mkdir(dir:downloadFile.parentFile);
				try {
					ant.get(src: url, dest: downloadFile);
				}
				catch (Exception e) {
					String errMsg = 'Failed to download ' + filename + ' and local file does not exist';
					println((mustExist ? 'WARNING! ' : 'Note: ') 
						+ filename + ' ' + errMsg
						+ '\n   ' + url
						+ '\n   ' + e.message);
					if (mustExist && !exists)
						throw new BuildException(errMsg);
				}
			}
			if (!cacheFile.exists()) {
				println("Copying " + filename);
				ant.copy(file:downloadFile, tofile:cacheFile);
			}
		}
		else
			println("File already downloaded and cached: " + cacheFile.canonicalPath);
		return cacheFile;
	}
	
	/**
	 * Remove the specified file from the cache but not from the download
	 * if the cache directory is different than the download
	 */
	public clearCache(String filename) {
		if (!prop.get("build.cache").equals(prop.get("build.downloads")))
			new File(prop.get("build.cache"), filename).delete();
	 }
	
	/**
	 * Download and unzip the JDK (if not already installed)
	 * and return the java executable (e.g. java.exe)
	 */
	public File getJavaExe(Version jdkVersion) {
		File javaExe = new File(prop.getPlatformSpecific('jdk.' + jdkVersion, '.exe'));
		if (!javaExe.exists()) {
			File javaHome = getJavaHome(jdkVersion);
			if (!javaExe.exists())
				throw new RuntimeException('Found JDK ' + jdkVersion + ' at ' + javaHome.canonicalPath 
					+ '\n  but could not find ' + javaExe.canonicalPath);
		}
		return javaExe;
	}
	
	/**
	 * Download and unzip the JDK (if not already installed)
	 * and return the java home directory
	 */
	public File getJavaHome(Version jdkVersion) {
		File javaHome = new File(prop.get('jdk.' + jdkVersion + '.home'));
		if (!javaHome.exists()) {
			String javaUrl = prop.getWithTrailingSlash('jdk.' + jdkVersion + '.url');
			String javaZipName = prop.getPlatformSpecific('jdk.' + jdkVersion, '.zip');
			File javaZip = downloadFile(javaUrl + javaZipName, javaZipName);
			unzip(javaZip, javaHome);
			clearCache(javaZipName);
			// On Linux, make sure that the permissions are properly set
			if (prop.isLinux()) {
				File javaExe = new File(prop.getPlatformSpecific('jdk.' + jdkVersion, '.exe'));
				String javaName = javaExe.name;
				ant.chmod(perm: 'ug+rx', file: new File(javaHome, 'bin/' + javaName));
				ant.chmod(perm: 'ug+rx', file: javaExe);
				ant.chmod(perm: 'ug+rx', file: new File(javaHome, 'bin/javac'));
				ant.chmod(perm: 'ug+rx', file: new File(javaHome, 'bin/javadoc'));
				ant.chmod(perm: 'ug+rx', file: new File(javaHome, 'bin/jar'));
				ant.chmod(perm: 'ug+rx', file: new File(javaHome, 'bin/jarsigner'));
			}
		}
		return javaHome;
	}
}
