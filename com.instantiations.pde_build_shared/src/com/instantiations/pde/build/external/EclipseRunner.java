package com.instantiations.pde.build.external;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A specialized {@link ProcessRunner} for launching and monitoring Eclipse applications.
 */
public class EclipseRunner extends ProcessRunner
{
	private final File eclipseHome;
	private File javaExe;
	private File workspace;
	private String appId;
	private ArrayList<String> eclipseArgs;

	/**
	 * Construct a new instance to launch and monitor an external Eclipse application
	 * 
	 * @param name the name used when printing information about the external process in
	 *            this process
	 * @param eclipseHome the "eclipse" directory containing the application to be
	 *            executed
	 */
	public EclipseRunner(String name, File eclipseHome) {
		super(name);
		this.eclipseHome = eclipseHome;
	}
	
	/**
	 * Set the java executable that will be used to run Eclipse
	 * (e.g. ${jdk.1.3.home}\jre\bin\java.exe)
	 */
	public void setJavaExe(File exe) {
		javaExe = exe;
	}

	/**
	 * Set the identifier of the application to be run. If not set and eclipseHome is the
	 * Eclipse SDK, then the Eclipse IDE will launch.
	 */
	public void setApplication(String appId) {
		this.appId = appId;
	}

	/**
	 * Set the Eclipse workspace directory
	 */
	public void setWorkspace(File workspace) {
		this.workspace = workspace;
	}

	/**
	 * Override the superclass implementation so that Eclipse specific command line
	 * information can be added at the time of launch
	 */
	public void setCmdLine(ArrayList<String> eclipseArgs) {
		this.eclipseArgs = eclipseArgs;
	}
	
	/**
	 * Answer the Eclipse home directory named "eclipse"
	 */
	public File getEclipseHome() {
		return eclipseHome;
	}
	
	/**
	 * Answer the Eclipse configuration directory 
	 */
	public File getConfiguration() {
		return new File(eclipseHome, "configuration");
	}
	
	/**
	 * Answer the workspace directory
	 */
	public File getWorkspace() {
		return workspace;
	}
	
	/**
	 * Answer the workspace log file
	 */
	public File getWorkspaceLog() {
		return new File(workspace, ".metadata/.log");
	}

	/* (non-Javadoc)
	 * @see com.instantiations.pde.build.external.ProcessRunner#launch()
	 */
	public void launch() throws IOException {

		// Find the Eclipse "launcher" or "startup" jar
		File launcher = findPlugin(eclipseHome, "org.eclipse.equinox.launcher");
		if (launcher == null)
			launcher = new File(eclipseHome, "startup.jar");
		if (!launcher.exists())
			throw new IllegalStateException("Cannot find Eclipse launcher in " + getPath(eclipseHome));

		// Build the command line
		ArrayList<String> newCmdLine = new ArrayList<String>(14);
		newCmdLine.add(javaExe != null ? javaExe.getCanonicalPath() : "java");
		newCmdLine.add("-cp");
		newCmdLine.add(getPath(launcher));
		newCmdLine.add("org.eclipse.core.launcher.Main");
		newCmdLine.add("-data");
		newCmdLine.add(getPath(workspace));
		if (appId != null) {
			newCmdLine.add("-application");
			newCmdLine.add(appId);
		}
		if (eclipseArgs != null) {
			newCmdLine.addAll(eclipseArgs);
		}
		newCmdLine.add("-vmargs");
		newCmdLine.add("-Xms64M");
		newCmdLine.add("-Xmx256M");
		super.setCmdLine(newCmdLine);

		// Launch the Eclipse application
		super.launch();
	}

	//=========================================================================================
	// Utilities
	//=========================================================================================

	/**
	 * Answer the canonical path if possible, otherwise the absolute path
	 */
	private static String getPath(File file) {
		try {
			return file.getCanonicalPath();
		}
		catch (IOException e) {
			return file.getAbsolutePath();
		}
	}

	/**
	 * Search the specified Eclipse instance for the plugin directory or jar that has the
	 * specified plugin identifier.
	 * 
	 * @param eclipseHome the "eclipse" directory
	 * @param pluginId the plugin identifier
	 * @return the plugin directory or jar or <code>null</code> if none found
	 */
	public static File findPlugin(File eclipseHome, String pluginId) {
		String prefix = pluginId + "_";
		String[] pluginNames = new File(eclipseHome, "plugins").list();
		for (String name : pluginNames)
			if (name.startsWith(prefix))
				return new File(eclipseHome, "plugins/" + name);
		return null;
	}

	/**
	 * Search the specified Eclipse instance for the plugin directory or jar that has the
	 * specified plugin identifier.
	 * 
	 * @param pluginId the plugin identifier
	 * @return the plugin directory or jar or <code>null</code> if none found
	 */
	public File findPlugin(String pluginId) {
		return findPlugin(eclipseHome, pluginId);
	}
}
