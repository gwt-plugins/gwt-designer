package com.instantiations.pde.build.util

/**
 * Read and write project set files (*.psf)
 */
public class ProjectSetFile
{
	String comment;
	private final File sourceDir;
	private final Collection projUrls = new TreeSet();
	
	public ProjectSetFile(File sourceDir) {
		this.sourceDir = sourceDir;
	}
	
	/**
	 * Add the project names to the project set file
	 */
	public void addProjNames(Collection names) {
		for (String projName : names)
			addProjName(projName);
	}

	/**
	 * Add the project name to the project set file
	 */
	public void addProjName(String projName) {
		File svnFile = new File(sourceDir, projName + "/.svn/entries");
		if (!svnFile.exists())
			return;
		svnFile.withReader() { reader ->
			while (true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				if (line.startsWith("http")) {
					projUrls.add(line);
					break;
				}
			}
		}
	}
	
	/**
	 * Add the project URL to the project set file
	 */
	public void addProjUrl(String url) {
		projUrls.add(url);
	}
	
	/**
	 * Read the project set file
	 */
	public void read(File file) {
		file.withReader { reader ->
			while (true) {
				String line = reader.readLine();
				if (line == null)
					return;
				line = line.trim();
				if (line.startsWith('<project reference="0.9.3,')) {
					int start = line.indexOf(',') + 1;
					int end = line.indexOf(',', start);
					projUrls.add(line.substring(start, end).trim());
				}
			}
		}
	}
	
	/**
	 * Write the project set to a file
	 */
	public void write(File file) {
		file.withWriter { writer ->
			writer.writeLine('<?xml version="1.0" encoding="UTF-8"?>');
			if (comment != null && comment.length() > 0)
				writer.writeLine('<!-- ' + comment + ' -->');
			writer.writeLine('<psf version="2.0">');
			writer.writeLine('  <provider id="org.tigris.subversion.subclipse.core.svnnature">');
			for (String eachUrl : projUrls) {
				String projName = eachUrl.substring(eachUrl.lastIndexOf('/') + 1);
				writer.write('    <project reference="0.9.3,');
				writer.write(eachUrl);
				writer.write(',');
				writer.write(projName);
				writer.writeLine('"/>');
			}
			writer.writeLine('  </provider>');
			writer.writeLine('</psf>');
		}
	}
}
