package com.instantiations.pde.build.subproduct

import java.util.zip.ZipFile
import java.util.zip.ZipEntryimport com.instantiations.pde.build.util.BuildUtil
import com.instantiations.pde.build.preprocessor.BundleManifestReader/**
 * Extracts feature and plugin information from product zip files
 */
public class SubProductZipReader extends BuildUtil
{
	// Must be initialized by the caller
	File zipFile;		// The zip file to be read
	
	// Used internally to cache information
	String id;
	String fullVersion;
	Collection<String> includedFeatures;
	Collection<String> includedPlugins;
	Collection<String> requiredFeatures;
	Collection<String> requiredPlugins;
	Map<String, String> pluginToFeature;
	
	// Used when scanning jar'd plugins in zip file
	File tempDir;
	
	// =========================================================
	// Accessors
	
	public String getId() {
		scanZipFile();
		return id;
	}
	
	public String getFullVersion() {
		scanZipFile();
		return fullVersion;
	}
	
	public Collection<String> getIncludedFeatures() {
		scanZipFile();
		return includedFeatures;
	}
	
	public Collection<String> getIncludedPlugins() {
		scanZipFile();
		return includedPlugins;
	}
	
	public Collection<String> getRequiredFeatures() {
		scanZipFile();
		return requiredFeatures;
	}
	
	public Collection<String> getRequiredPlugins() {
		scanZipFile();
		return requiredPlugins;
	}
	
	public Map<String, String> getPluginToFeature() {
		scanZipFile();
		return pluginToFeature;
	}
	
	/**
	 * This is called externally by SubProduct to extract information
	 * from the content of a feature.xml file
	 */
	public void scanFeatureNode(Node feature) {
		initFields();
		String featureId = feature.'@id';
		if (featureId != null) {
			includedFeatures.add(featureId);
			if (id == null) {
				id = featureId;
				fullVersion = feature.'@version';
			}
		}
		for (Node requires : feature.requires) {
			for (Node imp : requires.'import') {
				featureId = imp.'@feature';
				if (featureId != null && featureId.length() > 0)
					requiredFeatures.add(featureId);
				String pluginId = imp.'@plugin';
				if (pluginId != null && pluginId.length() > 0)
					requiredPlugins.add(pluginId);
			}
		}
		for (Node plugin : feature.plugin)
			pluginToFeature.put(plugin.'@id', id);
	}
	
	/**
	 * Scan the plugin directory for information in the MANIFEST.MF
	 * and plugin.xml files
	 */
	public void scanPluginDir(File pluginDir) {
		 initFields();
		 File manifestFile = new File(pluginDir, 'META-INF/MANIFEST.MF');
		 if (manifestFile.exists()) {
				InputStream stream = new FileInputStream(manifestFile);
				try {
					scanPluginManifest(stream);
				}
				finally {
					stream.close();
				}
		 }
		 else {
			File xmlFile = new File(pluginDir, 'plugin.xml');
			if (!xmlFile.exists())
				xmlFile = new File(pluginDir, 'fragment.xml');
			InputStream stream = new FileInputStream(xmlFile);
			try {
				scanPluginXml(stream);
			}
			finally {
				stream.close();
			}
		 }
	}
	
	// =========================================================
	// Internal
	
	private void initFields() {
		if (includedFeatures != null)
			return;
		includedFeatures = new HashSet<String>();
		includedPlugins = new HashSet<String>();
		requiredFeatures = new HashSet<String>();
		requiredPlugins = new HashSet<String>();
		pluginToFeature = new HashMap<String, String>();
	}
	
	private void scanZipFile() {
		if (includedFeatures != null)
			return;
		tempDir = new File(prop.productTemp, 'SubProductZipReader');
		ant.mkdir(dir: tempDir);
		initFields();
		ZipFile zip = new ZipFile(zipFile);
		try {
			scanZipFile(zip);
		}
		finally {
			zip.close();
		}
		requiredFeatures.removeAll(includedFeatures);
		requiredPlugins.removeAll(includedPlugins);
	}
	
	private void scanZipFile(ZipFile zip) {
		Enumeration<? extends ZipEntry> entries = zip.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (entry.name.startsWith('plugins/'))
				scanPluginEntry(zip, entry);
			else if (entry.name.startsWith('features/'))
				scanFeatureEntry(zip, entry);
		}
	}
	
	private void scanFeatureEntry(ZipFile zip, ZipEntry entry) {
		String path = entry.name.substring(9);
		if (path.length() == 0)
			return;
		path = path.substring(path.indexOf('/') + 1);
		if (path.length() == 0)
			return;
		if (path.equals("feature.xml"))
			scanFeatureXml(zip, entry);
	}
	
	private void scanFeatureXml(ZipFile zip, ZipEntry entry) {
		InputStream stream = zip.getInputStream(entry);
		try {
			scanFeatureNode(new XmlParser().parse(stream));
		}
		finally {
			stream.close();
		}
	}
	
	private void scanPluginEntry(ZipFile zip, ZipEntry entry) {
		String path = entry.name.substring(8);
		if (path.length() == 0)
			return;
		int index = path.indexOf('/');
		if (index > 0)
			path = path.substring(0, index);
		if (path.endsWith('.jar'))
			scanPluginJar(zip, entry);
		else
			scanPluginDirEntry(zip, entry);
	}
	
	private void scanPluginJar(ZipFile zip, ZipEntry entry) {
		// Extract the jar'd plugin to a temp location so that we can read it
		InputStream input = zip.getInputStream(entry);
		try {
			File tempFile = new File(tempDir, 'plugin.jar');
			try {
				tempFile.withOutputStream { output ->
					byte[] buf = new byte[1024];
					while (true) {
						int count = input.read(buf);
						if (count == -1)
							break;
						output.write(buf, 0, count);
					}
				}
				ZipFile pluginZip = new ZipFile(tempFile);
				try {
					scanPluginJar(pluginZip);
				}
				finally {
					pluginZip.close();
				}
			}
			finally {
				tempFile.delete();
			}
		}
		finally {
			input.close();
		}
	}
	
	private void scanPluginJar(ZipFile zip) {
		ZipEntry entry = zip.getEntry("META-INF/MANIFEST.MF");
		if (entry != null)
			scanPluginManifest(zip, entry);
		else {
			entry = zip.getEntry("plugin.xml");
			if (entry == null)
				entry = zip.getEntry("fragment.xml");
			if (entry != null)
				scanPluginXml(zip, entry);
		}
	}
	
	private void scanPluginDirEntry(ZipFile zip, ZipEntry entry) {
		String path = entry.name;
		int index = path.indexOf('/');
		if (index == -1)
			return;
		path = path.substring(index + 1);
		index = path.indexOf('/');
		if (index == -1)
			return;
		path = path.substring(index + 1);
		if (path.equals("META-INF/MANIFEST.MF"))
			scanPluginManifest(zip, entry);
		if (path.equals("plugin.xml") || path.equals("fragment.xml"))
			scanPluginXml(zip, entry);
	}
	
	private void scanPluginManifest(ZipFile zip, ZipEntry entry) {
		InputStream stream = zip.getInputStream(entry);
		try {
			scanPluginManifest(stream);
		}
		finally {
			stream.close();
		}
	}
	
	private void scanPluginManifest(InputStream stream) {
		BundleManifestReader reader = new BundleManifestReader();
		reader.process(new BufferedReader(new InputStreamReader(stream)));
		includedPlugins.add(reader.id);
		requiredPlugins.addAll(reader.requiredPlugins);
	}
	
	private void scanPluginXml(ZipFile zip, ZipEntry entry) {
		InputStream stream = zip.getInputStream(entry);
		try {
			scanPluginXml(stream);
		}
		finally {
			stream.close();
		}
	}
	
	private void scanPluginXml(InputStream stream) {
		Node plugin = new XmlParser().parse(stream);
		String pluginId = plugin.'@id';
		if (pluginId == null || pluginId.length() == 0)
			return;
		includedPlugins.add(pluginId);
		for (Node requires : plugin.requires) {
			for (Node imp : requires.'import') {
				requiredPlugins.add(imp.'@plugin');
			}
		}
		// Also handle fragments
		String hostId = plugin.'@plugin-id';
		if (hostId != null && hostId.length() > 0)
			requiredPlugins.add(hostId);
	}
}
