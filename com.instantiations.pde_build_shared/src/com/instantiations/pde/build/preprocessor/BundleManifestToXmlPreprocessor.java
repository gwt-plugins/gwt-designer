package com.instantiations.pde.build.preprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.List;
import java.util.regex.Pattern;

import com.instantiations.pde.build.util.Version;

/**
 * Merge the META-INF/MANIFEST.MF file into the plugin.xml file.
 */
public class BundleManifestToXmlPreprocessor
{
	private static final Pattern VISIBILITY_REEXPORT_PATTERN = Pattern
		.compile(";\\s*visibility\\s*:=\\s*reexport");

	private static final Pattern RESOLUTION_OPTIONAL_PATTERN = Pattern
		.compile(";\\s*resolution\\s*:=\\s*optional");

	/**
	 * Merge content from the META-INF/MANIFEST.MF file into the plugin.xml file.
	 * 
	 * @param manifestFile the META-INF/MANIFEST.MF file (must exist)
	 * @param xmlFile the plugin.xml or fragment.xml file (may not exist)
	 * @param version 
	 */
	public void process(File manifestFile, File xmlFile, Version targetVersion) throws IOException {
		BundleManifestReader manifest = new BundleManifestReader();
		manifest.process(manifestFile);
		String rootTag = manifest.isFragment() ? "fragment" : "plugin";

		String content;
		if (xmlFile.exists())
			content = readFile(xmlFile);
		else {
			StringBuffer buf = new StringBuffer(200);
			buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			if (targetVersion.compareTo(Version.V_3_2) >= 0)
				buf.append("\n<?eclipse version=\"3.2\"?>");
			else if (targetVersion.compareTo(Version.V_3_0) >= 0)
				buf.append("\n<?eclipse version=\"3.0\"?>");
			buf.append("\n<" + rootTag + ">");
			buf.append("\n</" + rootTag + ">");
			content = buf.toString();
		}

		String replacement = buildReplacement(manifest);
		content = content.replace("<" + rootTag + ">", replacement);

		writeFile(xmlFile, content);
		if (!manifestFile.delete())
			throw new IOException("Failed to delete " + manifestFile.getCanonicalPath());
		File metaInfDir = manifestFile.getParentFile();
		if (metaInfDir.list().length == 0 && !metaInfDir.delete())
			throw new IOException("Failed to delete " + metaInfDir.getCanonicalPath());
	}

	private String readFile(File pluginFile) throws IOException {
		StringWriter writer = new StringWriter(4096);
		char[] cbuf = new char[4096];
		Reader reader = new BufferedReader(new FileReader(pluginFile));
		try {
			while (true) {
				int count = reader.read(cbuf);
				if (count == -1)
					break;
				writer.write(cbuf, 0, count);
			}
		}
		finally {
			reader.close();
		}
		return writer.toString();
	}

	private String buildReplacement(BundleManifestReader manifest) {
		String value;
		StringWriter writer = new StringWriter(400);

		if (manifest.isFragment())
			writer.append("<fragment");
		else
			writer.append("<plugin");
		writer.append("\n   id=\"" + manifest.getId() + "\"");
		writer.append("\n   name=\"" + manifest.get("Bundle-Name") + "\"");
		writer.append("\n   version=\"" + manifest.get("Bundle-Version") + "\"");
		value = manifest.get("Bundle-Vendor");
		if (value != null)
			writer.append("\n   provider-name=\"" + value + "\"");
		value = manifest.get("Bundle-Activator");
		if (value != null)
			writer.append("\n   class=\"" + value + "\"");
		value = manifest.get("Fragment-Host");
		if (value != null) {
			int index = value.indexOf(';');
			String hostId = value;
			String hostVersion = "1.0.0";
			if (index > 0) {
				hostId = value.substring(0, index).trim();
				index = value.indexOf("bundle-version", index);
				if (index > 0) {
					index = value.indexOf('"', index) + 1;
					hostVersion = value.substring(index, value.indexOf('"', index));
				}
			}
			writer.append("\n   plugin-id=\"" + hostId + "\"");
			writer.append("\n   plugin-version=\"" + hostVersion + "\"");
			writer.append("\n   match=\"greaterOrEqual\"");
		}
		writer.append(">\n");

		List<String> classpath = manifest.getList("Bundle-ClassPath");
		if (classpath != null && classpath.size() > 0) {
			writer.append("\n   <runtime>");
			for (String entry : classpath) {
				String libName = entry;
				int end = libName.indexOf(';');
				if (end != -1)
					libName = libName.substring(0, end);
				writer.append("\n      <library name=\"" + libName + "\">");
				writer.append("\n         <export name=\"*\"/>");
				writer.append("\n      </library>");
			}
			writer.append("\n   </runtime>");
		}

		List<String> requires = manifest.getList("Require-Bundle");
		if (requires != null && requires.size() > 0) {
			writer.append("\n   <requires>");
			for (String entry : requires) {
				String id = entry;
				int end = id.indexOf(';');
				if (end != -1)
					id = id.substring(0, end);
				appendImportPlugin(writer, id, VISIBILITY_REEXPORT_PATTERN.matcher(entry).find(),
					RESOLUTION_OPTIONAL_PATTERN.matcher(entry).find());
			}
			writer.append("\n   </requires>");
		}

		return writer.toString();
	}

	private void appendImportPlugin(StringWriter writer, String id, boolean reexport, boolean optional) {
		writer.append("\n      <import plugin=\"" + id + "\"");
		if (reexport)
			writer.append(" export=\"true\"");
		if (optional)
			writer.append(" optional=\"true\"");
		writer.append("/>");
	}

	private void writeFile(File pluginFile, String content) throws IOException {
		if (pluginFile.exists())
			pluginFile.delete();
		FileWriter writer = new FileWriter(pluginFile);
		try {
			writer.write(content);
		}
		finally {
			writer.close();
		}
	}
}
