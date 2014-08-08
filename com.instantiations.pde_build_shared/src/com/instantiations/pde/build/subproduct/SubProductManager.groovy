package com.instantiations.pde.build.subproduct

import java.util.Map;

import com.instantiations.pde.build.util.BuildUtil
import com.instantiations.pde.build.util.OemVersion
import com.instantiations.pde.build.util.ProductDownloaderimport groovy.xml.MarkupBuilder
import org.apache.tools.ant.BuildException
/**
 * A manager of all subproducts for a given target version
 */
public class SubProductManager extends BuildUtil
{
	// Must be initialized by the caller
	OemVersion targetVersion;
	ProductDownloader productCache;

	// The parent manager or null if not initialized
	// The root parent manager points to itself
	SubProductManager parent = null;
	String branchName;
	
	// Cached information
	Map<String, SubProduct> subproducts = new HashMap<String, SubProduct>();
	Map<String, String> featureToSubProductName = null;
	Map<String, String> pluginToSubProductName = null;
	Map<String, String> pluginToFeature = null;
	
	/**
	 * Perform a recursive search for all subproducts
	 * upon which the specified subproduct is dependent
	 */
	public Collection<String> getAllDependencies(String subproductName, Map<String, String> overrideMap = null) {
		load();
		Collection<String> result = new HashSet<String>();
		Collection<String> todo = new ArrayList<String>();
		Collection<String> allFeatureIds = new HashSet<String>();
		Collection<String> allPluginIds = new HashSet<String>();
		todo.add(subproductName);
		while (todo.size() > 0) {
			String name = todo.remove(0);
			if (result.contains(name))
				continue;
			if (overrideMap != null && overrideMap.containsKey(name)) {
				String override = overrideMap.get(name);
				if (override == null)
					continue;
				name = override;
			}
			result.add(name);
			
			SubProduct subproduct = getSubProduct(name);
			for (String featureId : subproduct.includedFeatures) {
				if (allFeatureIds.contains(featureId))
					failDuplicate('Feature ' + featureId, name, result);
				allFeatureIds.add(featureId);
			}
			for (String pluginId : subproduct.includedPlugins) {
				if (allPluginIds.contains(pluginId))
					failDuplicate('Plugin ' + pluginId, name, result)
				allPluginIds.add(pluginId);
			}
			
			todo.addAll(getDependencies(name));
		}
		result.remove(subproductName);
		return result;
	}
	
	/**
	 * Search for all subproducts upon which the specified subproduct is directly dependent
	 */
	public Collection<String> getDependencies(String subproductName) {
		load();
		Collection<String> result = new HashSet<String>();
		SubProduct subproduct = getSubProduct(subproductName);
		for (String featureId : subproduct.requiredFeatures) {
			String required = getNameOfSubProductContainingFeature(featureId);
			if (required != null)
				result.add(required);
		}
		for (String pluginId : subproduct.requiredPlugins) {
			String required = getNameOfSubProductContainingPlugin(pluginId);
			if (required != null)
				result.add(required);
		}
		return result;
	}
	
	/**
	 * Given a collection of required plugins, 
	 * return a collection of features containing those plugins 
	 */
	public Collection<String> getFeaturesContainingPlugins(Collection<String> plugins) {
		load();
		Collection<String> result = parent == this ? new HashSet<String>() : parent.getFeaturesContainingPlugins(plugins);
		for (String plugin : plugins) {
			String feature = pluginToFeature.get(plugin);
			if (feature != null)
				result.add(feature);
		}
		return result;
	}
	
	/**
	 * Answer the name of the subproduct containing the specified feature
	 * or null if unknown
	 */
	public String getNameOfSubProductContainingFeature(String featureId) {
		load();
		String name = featureToSubProductName.get(featureId);
		if (name != null || parent == this)
			return name;
		return parent.getNameOfSubProductContainingFeature(featureId);
	}
	
	/**
	 * Answer the subproduct containing the specified plugin
	 */
	public String getNameOfSubProductContainingPlugin(String pluginId) {
		load();
		String name = pluginToSubProductName.get(pluginId);
		if (name != null || parent == this)
			return name;
		return parent.getNameOfSubProductContainingPlugin(pluginId);
	}
	
	/**
	 * Answer the SubProduct with the given name
	 */
	public SubProduct getSubProduct(String name) {
		SubProduct subproduct = subproducts.get(name);
		if (subproduct == null) {
			subproduct = newSubProduct(name);
			subproduct.load();
			subproducts.put(name, subproduct);
		}
		return subproduct;
	}
	
	/**
	 * Answer a new SubProduct with the given name which is NOT cached by the receiver.
	 * After calling this method, call subproduct.scanFeature and subproduct.scanPlugin
	 * as part of building the subproduct, then call addSubProduct(...) to cache and save the info.
	 */
	public SubProduct newSubProduct(String name) {
		return new SubProduct(
			name:			name, 
			targetVersion:	targetVersion,
			productCache:	productCache,
			prop:			prop);
	}
	
	/**
	 * Add the specified SubProduct to the receiver
	 */
	public void addSubProduct(SubProduct subproduct) {
		load();
		String name = subproduct.name

		// Cache the new subproduct
		if (subproducts.get(name) != null)
			throw new RuntimeException('SubProduct ' + name + ' already added to the SubProductManager' +
				'\n   Do not call getSubProduct(...) and pass the result to addSubProduct(...)');
		subproducts.put(name, subproduct);
		
		// Update the feature map
		Iterator iter = featureToSubProductName.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = iter.next();
			if (entry.value == name)
				iter.remove();
		}
		for (String featureId : subproduct.includedFeatures)
			featureToSubProductName.put(featureId, name);
		
		// Update the plugin map
		iter = pluginToSubProductName.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = iter.next();
			if (entry.value == name)
				iter.remove();
		}
		for (String pluginId : subproduct.includedPlugins)
			pluginToSubProductName.put(pluginId, name);
			
		// Add to the plugins to features map
		pluginToFeature.putAll(subproduct.pluginToFeature);
		
		// Save the changes
		subproduct.save();
		save();
	}
	
	// =========================================================
	// Internal
	
	private void failDuplicate(String item, String subproductName, Collection<String> otherSubProductNames) {
		StringBuffer buf = new StringBuffer(1000);
		buf.append(item);
		buf.append(' is part of multiple subproducts');
		buf.append('\n   ' + subproductName + ' subproduct');
		buf.append('\n   and one of ');
		for (String name : new TreeSet<String>(otherSubProductNames)) {
			if (name != subproductName) {
				buf.append(name);
				buf.append(', ');
			}
		}
		throw new BuildException(buf.toString());
	}
	 
	/**
	 * Load and cache the feature/plugin to subproduct maps
	 * if they have not already been loaded
	 */
	private void load() {
		if (featureToSubProductName != null)
			return;

		// Initialize the parent 
		if (parent == null) {
			branchName = prop.getBranchName();

			// If we are building a branch then the parent is the root manager
			if (branchName != null) {
				parent = new SubProductManager(targetVersion: targetVersion, productCache: productCache);
				parent.parent = parent;
			}
			// Otherwise this is the root manager
			else {
				parent = this;
			}
		}
		
		featureToSubProductName = new HashMap<String, String>();
		pluginToSubProductName = new HashMap<String, String>();
		pluginToFeature = new HashMap<String, String>();
		
		// Load the cached information from file if it exists
		File infoFile = getInfoFile();
		if (infoFile.exists()) {
			loadInfoFile(infoFile);
			return;
		}
		
		// If this is a local build, then the file SHOULD exist
		if (!productCache.isLocalOnly() && branchName == null)
			throw new BuildException("Failed to find subproduct file:\n   " + infoFile.canonicalPath);

		// Fresh restart, no dependency info, must rebuild each project to populate this map
		println('*********************************************************');
		println('* No ' + infoFile.name + ' found:');
		println('*    ' + infoFile.path);
		if (branchName != null)
			println('* for branch ' + branchName);
		println('* Dependency info will be added as a result of building each subproduct');
		println('*********************************************************');
	}
	
	private void loadInfoFile(File infoFile) {
		Node root = new XmlParser().parse(infoFile);
		
		// If this is a "rev 1" file then it is missing the pluginToFeature information
		if (root.'@rev' != '3' && root.'@rev' != '2')
			throw new RuntimeException('Invalid ' + infoFile.name + '\n   ' + infoFile.path);

		for (Node features : root.features)
			for (Node feature : features.feature)
				featureToSubProductName.put(feature.'@id', feature.'@subproduct');
		for (Node plugins : root.plugins) {
			for (Node plugin : plugins.plugin) {
				pluginToSubProductName.put(plugin.'@id', plugin.'@subproduct');
				pluginToFeature.put(plugin.'@id', plugin.'@feature');
			}
		}
	}
	
	/**
	 * Save the cached feature/plugin to subproduct maps
	 * if they have been loaded
	 */
	private void save() {
		File file = getInfoFile();
		file.parentFile.mkdirs();
		file.withWriter { writer ->
			MarkupBuilder builder = new MarkupBuilder(writer);
			builder.doubleQuotes = true;
			builder.subproducts(rev: '3') {
				target(version: targetVersion);
				if (branchName == null)
					description 'Dependency information for subproducts in trunk';
				else
					description 'Dependency information for subproducts in the ' + branchName + ' branch';
				features {
					for (String id : new TreeSet<String>(featureToSubProductName.keySet())) {
						feature(id: id, subproduct: featureToSubProductName.get(id));
					}
				}
				plugins {
					for (String id : new TreeSet<String>(pluginToSubProductName.keySet())) {
						String featureId = pluginToFeature.get(id);
						if (featureId != null && featureId.length() > 0)
							plugin(id: id, subproduct: pluginToSubProductName.get(id), feature: featureId);
						else
							plugin(id: id, subproduct: pluginToSubProductName.get(id));
					}
				}
			}
		}
	}
	 
	private File getInfoFile() {
		String fileName = 'subproducts';
		if (branchName != null)
			fileName += '-' + branchName;
		fileName += '.xml';
		return productCache.downloadFile('BuildCommon', targetVersion, fileName, false);
	}
}
