package com.instantiations.pde.build.usage

import com.instantiations.pde.build.util.BuildProperties
import com.instantiations.pde.build.util.BuildUtil

/**
 * Scan a workspace (sourceDir) for plugins and create/update a UsageInfo.xml file
 * containing information for the Hudson usage profiler report build job.
 */public class UsageInfoScanner extends BuildUtil
{
	/**
	 * Scan the sourceDir looking for plugins from which to extract usage information
	 */
	public void scanWorkspace(File sourceDir) {
		File usageInfoFile = new File(prop.buildArtifacts, prop.productName + '/UsageInfo.xml');
		println('Scanning ' + prop.productName + ' and building ' + usageInfoFile.name);
		println('   ' + usageInfoFile.path);
		
		// If the build.group property is not defined
		// then delete the UsageInfo.xml file and exit
		
		if (!prop.isDefined('build.group')) {
			usageInfoFile.delete();
			return;
		}
		
		// Read the UsageInfo.xml file if that file exists
		
		Node usageInfo;
		try {
			usageInfo = new XmlParser().parse(usageInfoFile);
		}
		catch (Exception e) {
			println('Exception reading ' + usageInfoFile.name);
			println('   ' + usageInfoFile.path);
			println('   ' + e.getMessage());
			usageInfo = new Node(null, 'usageInfo');
		}
		
		// The file should have exactly one buildGroup
		
		Node buildGroup;
		if (usageInfo.buildGroup.size() > 0)
			buildGroup = usageInfo.buildGroup[0];
		else
			buildGroup = usageInfo.appendNode('buildGroup');
		buildGroup.@name = prop.get('build.group');

		// Scan the workspace
		
		for (File projDir : sourceDir.listFiles())
			scanProject(buildGroup, projDir);

		// Write the results
		ant.mkdir(dir: usageInfoFile.parent);
		usageInfoFile.withPrintWriter { writer ->
			new XmlNodePrinter(writer, '\t').print(usageInfo);
		}
	}
	
	/**
	 * Scan the specified project directory and extract plugin information
	 */
	protected void scanProject(Node buildGroup, File projDir) {
		
		// Load the plugin.xml and plugin.properties files
		
		File pluginXmlFile = new File(projDir, 'plugin.xml');
		if (!pluginXmlFile.exists())
			return;
		Node plugin = new XmlParser().parse(pluginXmlFile);

		File pluginPropFile = new File(projDir, 'plugin.properties');
		Properties pluginProp = new Properties();
		if (pluginPropFile.exists())
			pluginProp.load(new FileInputStream(pluginPropFile));
		
		// Scan the plugin.xml file content
		
		for (Node extRec : plugin.extension) {
			scanExtension(buildGroup, extRec, pluginProp, 'command',     'org.eclipse.ui.commands' );
			scanExtension(buildGroup, extRec, pluginProp, 'view',        'org.eclipse.ui.views');
			scanExtension(buildGroup, extRec, pluginProp, 'editor',      'org.eclipse.ui.editors');
			scanExtension(buildGroup, extRec, pluginProp, 'perspective', 'org.eclipse.ui.perspectives');
		}
	}
	
	/**
	 * Scan the plugin.xml file content for the specified extension
	 * and add that information to the build group if it does not already exist
	 */
	protected void scanExtension(Node buildGroup, Node extRec, Properties pluginProp, String extPointName, String extPointId) {
		if (extRec.@point != extPointId)
			return;
		for (Node subRec : extRec."$extPointName") {
			String id = subRec.@id;
			String name = subRec.@name;
			if (name.startsWith("%"))
				name = pluginProp[name.substring(1)];
			addOrUpdateBuildGroupItem(buildGroup, extPointName, id, name);
		}
	}
	
	/**
	 * Add the item to the build group or update an existing item if it already exists
	 */
	protected void addOrUpdateBuildGroupItem(Node buildGroup, String extPointName, String id, String name) {
		for (Node item : buildGroup.item) {
			if (item.@type == extPointName && item.id.text().trim() == id) {
				item.name[0].setValue(name);
				return;
			}
		}
		Node item = buildGroup.appendNode('item', ['type': extPointName]);
		item.appendNode('id', id);
		item.appendNode('name', name);
	}
	
	//==========================================================
	// TEST
	
//	public static void main(String[] args) {
//		BuildProperties prop = new BuildProperties();
//		prop.readDefaults();
//		prop.set('product.name', 'Shared');
//		prop.set('build.group', 'Infrastructure');
//		prop.echoAll();
//		
//		UsageInfoScanner scanner = new UsageInfoScanner(prop: prop);
//		scanner.scanWorkspace(new File('..').canonicalFile);
//		println('Test Complete');
//	}
}