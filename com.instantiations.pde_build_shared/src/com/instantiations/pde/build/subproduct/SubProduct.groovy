package com.instantiations.pde.build.subproduct

import com.instantiations.pde.build.preprocessor.PluginProjectPreprocessor
import groovy.xml.MarkupBuilderimport com.instantiations.pde.build.util.ProductDownloaderimport com.instantiations.pde.build.util.OemVersionimport com.instantiations.pde.build.preprocessor.BundleManifestReaderimport com.instantiations.pde.build.util.BuildProperties
import com.instantiations.pde.build.util.BuildUtilimport org.apache.tools.ant.BuildException
/**
 * Represents a deployable portion of a product.
 * 
 * Do not instantiate directly, but rather
 * call SubProductManager#getSubProduct(...) to retrieve an existing subproduct
 * or SubProductManager#newSubProduct(...) to when building a new subproduct
 */
public class SubProduct extends BuildUtil
{
	// Must be initialized by the caller
	String name;
	OemVersion targetVersion;
	ProductDownloader productCache;
	
	// Used internally to cache information
	String id;
	String fullVersion;
	Collection<String> includedFeatures;
	Collection<String> includedPlugins;
	Collection<String> requiredFeatures;
	Collection<String> requiredPlugins;
	Map<String, String> pluginToFeature;
	
	// =========================================================
	// Accessors
	
	public String getId() {
		checkInit();
		return id;
	}
	
	public String getFullVersion() {
		checkInit();
		return fullVersion;
	}
	
	public Collection<String> getIncludedFeatures() {
		checkInit();
		return includedFeatures;
	}
	
	public Collection<String> getIncludedPlugins() {
		checkInit();
		return includedPlugins;
	}
	
	public Collection<String> getRequiredFeatures() {
		checkInit();
		return requiredFeatures;
	}
	
	public Collection<String> getRequiredPlugins() {
		checkInit();
		return requiredPlugins;
	}
	
	public Map<String, String> getPluginToFeature() {
		checkInit();
		return pluginToFeature;
	}
	
	public String getContainingFeature(String pluginId) {
		checkInit();
		return pluginToFeature.get(pluginId);
	}
	
	public boolean zipExists() {
		return productCache.downloadFile(name, targetVersion, null, false).exists();
	}
	
	/**
	 * Scan the specified feature and extract the information
	 */
	public void scanFeature(Node feature) {
		SubProductZipReader reader = new SubProductZipReader();
		reader.scanFeatureNode(feature);
		initFields();
		if (id == null) {
			id          = feature.'@id';
			fullVersion = feature.'@version';
		}
		includedFeatures.addAll(reader.includedFeatures);
		includedPlugins.addAll(reader.includedPlugins);
		requiredFeatures.addAll(reader.requiredFeatures);
		requiredPlugins.addAll(reader.requiredPlugins);
		pluginToFeature.putAll(reader.pluginToFeature);
	}
	
	/**
	 * Scan the specified plugin and extract the information
	 */
	public SubProductZipReader scanPlugin(File pluginDir) {
		SubProductZipReader reader = new SubProductZipReader();
		reader.scanPluginDir(pluginDir);
		initFields();
		if (id == null) {
			id          = reader.id;
			fullVersion = reader.fullVersion;
		}
		includedPlugins.addAll(reader.includedPlugins);
		requiredPlugins.addAll(reader.requiredPlugins);
		return reader;
	}
	
	/**
	 * Scan the given zip file and extract the information
	 */
	public void scanZip (File zipFile) {
		SubProductZipReader reader = new SubProductZipReader(zipFile: zipFile, prop:prop); 
		initFields(); 
		if (id == null) { 
			id = reader.id; fullVersion = reader.fullVersion; 
		}
		includedFeatures.addAll(reader.includedFeatures); 
		includedPlugins.addAll(reader.includedPlugins); 
		requiredFeatures.addAll(reader.requiredFeatures); 
		requiredPlugins.addAll(reader.requiredPlugins); 
		pluginToFeature.putAll(reader.pluginToFeature);  
	}
	/**
	 * Initialize the subproduct to contain javadoc
	 */
	public void setJavaDoc() {
		initFields();
		// Nothing else to do at this time
	}
	
	// =========================================================
	// Internal

	private checkFirstCall() {
		if (includedFeatures != null)
			throw new RuntimeException('load() or scanFeature() has already been called for ' + name + ' subproduct');
	}

	private checkInit() {
		if (includedFeatures == null)
			throw new RuntimeException('Must call either load() or scanFeature()/scanPlugin() before accessing ' + name + ' subproduct');
	}
	
	private void initFields() {
		if (includedFeatures != null)
			return;
		includedFeatures = new HashSet<String>();
		includedPlugins = new HashSet<String>();
		requiredFeatures = new HashSet<String>();
		requiredPlugins = new HashSet<String>();
		pluginToFeature = new HashMap<String, String>();
	}
	
	/**
	 * Load and cache the information from disk.
	 * Do not call this directly, but rather call SubProductManager#getSubProduct(...)
	 */
	void load() {
		checkFirstCall();
		loadInfoFile(getInfoFile(true));
	}
	
	private void loadInfoFile(File infoFile) {
		initFields();
		
		Node info = new XmlParser().parse(infoFile);
		
		// If this is a "rev 1" file then it is missing the pluginToFeature information
		if (info.'@rev' != '3' && info.'@rev' != '2')
			throw new RuntimeException('Invalid ' + infoFile.name + ' for ' + name + '\n   ' + infoFile.path);
		
		id          = info.'@id';
		fullVersion = info.'@fullVersion';
		
		for (Node parent : info.includedFeatures)
			for (Node child : parent.feature)
				includedFeatures.add(child.'@id');
		
		for (Node parent : info.includedPlugins) {
			for (Node child : parent.plugin) {
				includedPlugins.add(child.'@id');
				pluginToFeature.put(child.'@id', child.'@feature');
			}
		}
		
		for (Node parent : info.requiredFeatures)
			for (Node child : parent.feature)
				requiredFeatures.add(child.'@id');
		
		for (Node parent : info.requiredPlugins)
			for (Node child : parent.plugin)
				requiredPlugins.add(child.'@id');
	}

	private void save() {
		save(getInfoFile(false));
	}

	private void save(File infoFile) {
		infoFile.parentFile.mkdirs();
		infoFile.withWriter { writer ->
			MarkupBuilder builder = new MarkupBuilder(writer);
			builder.doubleQuotes = true;
			builder.subproduct(name: name, id: id, fullVersion: fullVersion, rev: '3') {
				description 'Dependency information for the ' + name + ' subproduct'
				
				includedFeatures {
					for (String featureId : new TreeSet<String>(includedFeatures))
						feature(id: featureId)
				}
				
				includedPlugins {
					for (String pluginId : new TreeSet<String>(includedPlugins)) {
						String featureId = pluginToFeature.get(pluginId);
						if (featureId != null && featureId.length() > 0)
							plugin(id: pluginId, feature: featureId)
						else
							plugin(id: pluginId)
					}
				}
				
				requiredFeatures {
					for (String featureId : new TreeSet<String>(requiredFeatures))
						feature(id: featureId)
				}
				
				requiredPlugins {
					for (String pluginId : new TreeSet<String>(requiredPlugins))
						plugin(id: pluginId)
				}
			}
		}
	}
	
	private File getInfoFile(boolean mustExist) {
		return productCache.downloadFile(name, targetVersion, 'subproduct.xml', mustExist);
	}
}
