package com.instantiations.eclipse.shared.installer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;

import com.instantiations.eclipse.shared.installer.operations.CleanUnlinkedInstallsOperation;
import com.instantiations.eclipse.shared.installer.operations.ScanUnlinkedInstallsOperation;
import com.instantiations.eclipse.shared.installer.operations.ScanUnusedInstallsOperation;
import com.instantiations.installer.core.IPlatform;
import com.instantiations.installer.core.InstallOptions;
import com.instantiations.installer.core.InstallerFactory;
import com.instantiations.installer.core.Platform;
import com.instantiations.installer.core.eclipse.EclipseInstallation;
import com.instantiations.installer.core.eclipse.EclipseLocator;
import com.instantiations.installer.core.eclipse.EclipseVersion;
import com.instantiations.installer.core.model.Context;
import com.instantiations.installer.core.model.InstallOperation;
import com.instantiations.installer.core.model.InstallStep;
import com.instantiations.installer.core.model.Installer;
import com.instantiations.installer.core.operations.CleanAllEclipseConfigurations;
import com.instantiations.installer.core.operations.CleanEmptyDirectoryOperation;
import com.instantiations.installer.core.operations.CleanRegistryOperation;
import com.instantiations.installer.core.operations.CleanupRegisteredProductsOperation;
import com.instantiations.installer.core.operations.CreateUninstallerOperation;
import com.instantiations.installer.core.operations.ExtractDirectoryOperation;
import com.instantiations.installer.core.operations.LinkProductOperation;
import com.instantiations.installer.core.operations.RegisterProductOperation;
import com.instantiations.installer.core.operations.RemoveAllEmbeddedInstallsOperation;
import com.instantiations.installer.core.operations.UninstallOperation;
import com.instantiations.installer.core.steps.ChoiceStep;
import com.instantiations.installer.core.steps.ChooseEclipseStep;
import com.instantiations.installer.core.steps.ChooseLocationStep;
import com.instantiations.installer.core.steps.PromptUserStep;
import com.instantiations.installer.core.steps.RemoveAllEmbeddedInstallsStep;
import com.instantiations.installer.core.steps.RunOperationsStep;
import com.instantiations.installer.core.steps.ScrollablePromptUserStep;
import com.instantiations.installer.core.steps.ShowWarningsStep;
import com.instantiations.installer.internal.core.IProductInstallation;
import com.instantiations.installer.internal.core.IProductLocatorListener;
import com.instantiations.installer.internal.core.IProductVersion;

/**
 * The abstract superclass for all Instantiations product installers.
 * <p>
 * Copyright (c) 2006, Instantiations, Inc.<br>
 * All Rights Reserved
 * 
 * @author Dan Rubel
 */
public abstract class BaseProductInstaller
{
	protected static final String OPTION_DELETE_UNUSED_INSTALLATIONS = "deleteUnusedInstallations";
	protected static final String OPTION_CLEAN_CONFIG = "CleanConfig";
	public static final String OPTION_DELETE_EMBEDDED = RemoveAllEmbeddedInstallsStep.OPTION_DELETE_EMBEDDED;
	public static final String OPTION_ALL_ECLIPSES_PATH_LIST = "AllEclipsesPathList";

	/**
	 * The name of the directory in the installer that contains the code to be installed.
	 */
	public static final String INSTALL_IMAGE = "install-image";

	/**
	 * The installer (not <code>null</code>)
	 */
	protected Installer installer;

	/**
	 * The installation options (not <code>null</code>)
	 */
	protected InstallOptions options;
	/**
	 * the collection of subproducts not to display in the installer UI
	 */
	private ArrayList hiddenSubproducts = new ArrayList();

	/**
	 * Run the installer
	 * 
	 * @param args command line arguments
	 */
	protected void run(String[] args) throws Exception {
		options = new InstallOptions(getClass(), "install.properties");
		options.set("InstallDir", new File(Platform.getVarString("ProgramFiles"), options.getString("InstallDirName")).getAbsolutePath());
		options.setUninstall(isUninstaller());
		options.setAllowOverwrite(true);
		options.setNoOverwriteWarningDuringInstall(true);
		options.setBoolean(ChooseLocationStep.OPTION_SUPPRESS_ALREADY_INSTALLED_WARNING, true);

		/*
		 * TODO [author=Dan] No backup until these cases are fixed<br>
		 * https://developer.instantiations.com/fogbugz/default.php?7494
		 * https://developer.instantiations.com/fogbugz/default.php?7495
		 * https://developer.instantiations.com/fogbugz/default.php?7581
		 */
		options.setNobackup(true);

		initInstallDir();
		
		// Do not show PDE or GEF 
		SubProduct[] subProducts = getSubProductsToInstall();
		for (int i = 0; i < subProducts.length; i++) {
			SubProduct each = subProducts[i];
			String name = each.getName();
			if (name.equals("PDE") || name.equals("GEF"))
				hideSubproduct(each);
		}
		
		options.parseCommandOptions(args);
		installer = InstallerFactory.createInstaller(options);
		if (options.isInstall())
			createInstall();
		else
			createUninstall();
		installer.run();
	}

	/**
	 * Read the prior installation location from the registry and set the InstallDir
	 * variable based upon that location.
	 * 
	 * @return the new value that InstallDir was set to or <code>null</code> if no
	 *         change was made
	 */
	protected String initInstallDir() {
		IPlatform platform = Platform.getPlatform();
		if (platform == null)
			return null;
		String[] productLocations;
		try {
			productLocations = platform.readPreviousProductInstallLocations(options);
		}
		catch (Exception e) {
			if (options.isVerbose())
				e.printStackTrace();
			return null;
		}
		if (productLocations == null || productLocations.length == 0)
			return null;
		String installDir = productLocations[productLocations.length - 1];
		if (options.isVerbose())
			System.out.println("Setting InstallDir to prior installation: " + installDir);
		options.set(InstallOptions.OPTION_INSTALL_DIR, installDir);
		return installDir;
	}

	// //////////////////////////////////////////////////////////////////////////
	//
	// Install
	//
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * Create the steps necessary to install the product.
	 */
	protected void createInstall() {
		installer.setTitle(options.getString("InstallerTitle"));
		welcomeStep();
		licenseStep();
		installDirStep();
		productDescriptionStep();
		chooseProductStep();
		chooseEclipseStep();
		shouldUseUpdateSiteStep();
		checkEclipseRunning();
		cleanConfigStep();
		removeEmbeddedInstallStep();
		verifyInstallStep();
		installCodeStep();
		checkUnlinkedInstallsStep();
		cleanUnlinkedInstallsStep();
		installResultStep();
		installCompleteStep();
	}

	/**
	 * Create a step display an introduct to this installation process and briefly
	 * describing what is to be installed.
	 * 
	 * @return the installation step created
	 */
	protected PromptUserStep welcomeStep() {
		PromptUserStep step = new PromptUserStep(installer);
		initStep(step, "Welcome");
		step.setText(options.getString("WelcomeText"));
		installer.add(step);
		return step;
	}

	/**
	 * Create a step displaying the EULA (End User License Agreement) for this product.
	 * 
	 * @return the installation step created
	 */
	protected ChoiceStep licenseStep() {
		ChoiceStep step = new ChoiceStep(installer);
		initStep(step, "License");
		step.setChoiceText(options.getString(InstallOptions.OPTION_LICENSE_AGREEMENT));
		step.setAcceptText("I accept the agreement");
		step.setDeclineText("I do not accept the agreement");
		step.setContinueOnDecline(false);
		step.setDefaultChoice(false);
		installer.add(step);
		return step;
	}

	/**
	 * Create a step prompting the user for the root location where the product is to be
	 * installed.
	 * 
	 * @return the installation step created
	 */
	protected ChooseLocationStep installDirStep() {
		ChooseLocationStep step = new ChooseLocationStep(installer);
		initStep(step, "InstallDir");
		step.setDetails(createInstallDirDetails(options.getString("InstallDirDetails")));
		installer.add(step);
		return step;
	}

	/**
	 * Create a description of what is to be installed by appending a list of subproducts
	 * to be installed to the given template text.
	 * 
	 * @param templateText the template text (not <code>null</code>)
	 * @return the installation directory details of what is to be installed
	 */
	protected String createInstallDirDetails(String templateText) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		SubProduct[] subProducts = getSubProductsToInstall();
		for (int i = 0; i < subProducts.length; i++) {
			SubProduct each = subProducts[i];
			// Exclude PDE from list shown to user
			if (hiddenSubproducts.contains(each))
				continue;
			writer.print("\n   * ");
			writer.print(each.getInstallDirName());
		}
		return templateText + stringWriter.toString();
	}

	/**
	 * Create a step describing the products that can be installed and how they relate to
	 * one another.
	 * 
	 * @return the installation step created
	 */
	protected void productDescriptionStep() {
		// TODO [author=Dan] implement product description step
		// PromptUserStep step = new PromptUserStep(installer);
		// initStep(step, "ProductDescription");
		// step.setText(options.getString("ProductDescriptionText"));
		// installer.add(step);
		// return step;
	}

	/**
	 * Create a step so that the user can select which product will be installed
	 */
	protected void chooseProductStep() {
		// TODO [author=Dan] implement chooseProductStep
	}

	/**
	 * Create a step so that the user can select one or more existing Eclipse
	 * installations to which this product will be linked.
	 * 
	 * @return the install step (not <code>null</code>)
	 */
	protected ChooseEclipseStep chooseEclipseStep() {
		ChooseEclipseStep step = new ChooseEclipseStep(installer);
		initStep(step, "LinkToEclipse");
		step.setLocator(new EclipseLocator(options) {
			protected EclipseInstallation[] findLinkedUnsorted() {
				File rootInstallDir = new File(options.getString(InstallOptions.OPTION_INSTALL_DIR));
				return findLinkedEclipseInstallations(rootInstallDir);
			}
			public void scan(final IProductLocatorListener listener) {
				super.scan(new IProductLocatorListener() {
					private StringBuffer allEclipsePaths = new StringBuffer(2000);
					public void scanning(File location) {
						listener.scanning(location);
					}
					public boolean isCanceled() {
						return listener.isCanceled();
					}
					public void found(IProductInstallation installation) {
						if (isAppropriateEclipseInstallation(installation, options)) {
							IProductVersion version = installation.getProductVersion();
							// 3.1 is no longer supported so skip it
							if (!(version.getMajor() == 3 && version.getMinor() == 1)) {
								listener.found(installation);
								if (allEclipsePaths.length() > 0)
									allEclipsePaths.append(',');
								allEclipsePaths.append(installation.getProductDir().getAbsolutePath());
								options.set(OPTION_ALL_ECLIPSES_PATH_LIST, allEclipsePaths.toString());
							}

						}
					}
				});
			}
		});
		installer.add(step);
		return step;
	}
	
	/**
	 * If the installer wishes to only show specific Eclipse installations (e.g. only show
	 * IBM RAD installations) then override this method. By default, this calls {@link
	 * #filterEclipseLocations(IProductInstallation[], InstallOptions)} for backward
	 * compatibility.
	 * 
	 * @param installation an Eclispse installation location (not <code>null</code>)
	 * @return <code>true</code> if the installation should be included as one that the
	 * 	user can link to this product, else <code>false</code>
	 */
	protected boolean isAppropriateEclipseInstallation(IProductInstallation installation, InstallOptions options) {
		return filterEclipseLocations(new IProductInstallation[] {installation}, options).length > 0;
	}

	/**
	 * DEPRECATED: Use {@link #isAppropriateEclipseInstallation(IProductInstallation,
	 * InstallOptions)} instead. If the installer wishes to only show specific Eclipse
	 * installations (e.g. only show IBM RAD installations) then override this method. By
	 * default, this returns the array of Eclipse installations passed into this method
	 * indicating that all "found" installations should be shown to the user.
	 * 
	 * @param installations an array of Eclipse installation locations (not
	 * 		<code>null</code> contains no <code>null</code>s)
	 * @return a filtered array of Eclispse installation locations (not <code>null</code>
	 * 	contains no <code>null</code>s)
	 * @deprecated use {@link #isAppropriateEclipseInstallation(IProductInstallation, InstallOptions)}
	 */
	protected IProductInstallation[] filterEclipseLocations(IProductInstallation[] installations, InstallOptions options)
	{
		return installations;
	}

	/**
	 * Given the specified root installation directory, find the linked Eclipse
	 * installations by finding and scanning an existing log file if possible.
	 * 
	 * @param rootInstallDir the root installation directory (not <code>null</code>)
	 * @return an array of linked Eclipse installations (not <code>null</code>,
	 *         contains no <code>null</code>s)
	 */
	protected EclipseInstallation[] findLinkedEclipseInstallations(File rootInstallDir) {
		String installSubDirName = getPrimaryProduct().getInstallDirName();
		File logFile = getLogFile(rootInstallDir, installSubDirName);

		// If log file does not already exist, then search for older log file
		// from which to extract a list of linked Eclipse installations

		if (!logFile.exists()) {
			String[] subDirNames = rootInstallDir.list();
			if (subDirNames != null) {
				Arrays.sort(subDirNames);

				// First search for older log files of same product

				for (int i = subDirNames.length; --i >= 0;) {
					installSubDirName = subDirNames[i];
					if (!installSubDirName.startsWith(getPrimaryProduct().getName()))
						continue;
					logFile = getLogFile(rootInstallDir, installSubDirName);
					if (logFile.exists())
						break;
				}

				// Then search for older log files of related products

				if (!logFile.exists()) {
					for (int i = subDirNames.length; --i >= 0;) {
						installSubDirName = subDirNames[i];
						logFile = getLogFile(rootInstallDir, installSubDirName);
						if (logFile.exists())
							break;
					}
				}
			}
		}
		return EclipseInstallation.findLinked(logFile);
	}

	/**
	 * Answer the log file contained in the specified subdirectory in the specified root
	 * directory
	 * 
	 * @param rootInstallDir the root directory (not <code>null</code>)
	 * @param installSubDirName the sub directory name (not <code>null</code>)
	 * @return the log file (not <code>null</code>, but may not exist)
	 */
	private File getLogFile(File rootInstallDir, String installSubDirName) {
		return new File(new File(rootInstallDir, installSubDirName), CreateUninstallerOperation.INSTALL_LOG);
	}

	/**
	 * Create a step to warn the user if any selected Eclipse installations are
	 * Eclipse 3.4 or greater, in which case they should use an update site.
	 */
	protected void shouldUseUpdateSiteStep() {
		final ScrollablePromptUserStep step = new ScrollablePromptUserStep(installer) {

			private EclipseInstallation[] newerEclipses;

			public boolean canExecute() {
				IProductVersion V34 = new EclipseVersion(3, 4, 0, null);
				List newer = new ArrayList();
				String[] eclipsePaths = options.getStrings(InstallOptions.OPTION_ECLIPSE_PATH_LIST);
				for (int i = 0; i < eclipsePaths.length; i++) {
					EclipseInstallation eclipse = new EclipseInstallation(new File(eclipsePaths[i]));
					if (eclipse.getEclipseVersion().compareTo(V34) >= 0)
						newer.add(eclipse);
				}
				newerEclipses = new EclipseInstallation[newer.size()];
				newer.toArray(newerEclipses);
				return newerEclipses.length > 0;
			}

			public void aboutToStep() {
				StringBuffer text = new StringBuffer();
				for (int i = 0; i < newerEclipses.length; i++) {
					EclipseInstallation each = newerEclipses[i];
					text.append(each.getEclipseDir().getAbsolutePath());
					text.append(" ");
					text.append(each.getDescription());
					text.append("\n");
				}
				setText(text.toString());
			}
		};
		initStep(step, "ShouldUseUpdateSite");
		installer.add(step);
	}

	/**
	 * Create a step to warn the user if any selected Eclipse installations have
	 * workspaces that are currently being used.
	 * 
	 * @return the step (not <code>null</code>)
	 */
	protected ScrollablePromptUserStep checkEclipseRunning() {
		final ScrollablePromptUserStep step = new ScrollablePromptUserStep(installer) {

			private EclipseInstallation[] runningEclipses;

			public boolean canExecute() {
				List running = new ArrayList();
				String[] eclipsePaths = options.getStrings(InstallOptions.OPTION_ECLIPSE_PATH_LIST);
				for (int i = 0; i < eclipsePaths.length; i++) {
					EclipseInstallation eclipse = new EclipseInstallation(new File(eclipsePaths[i]));
					if (eclipse.isRunning())
						running.add(eclipse);
				}
				runningEclipses = new EclipseInstallation[running.size()];
				running.toArray(runningEclipses);
				return runningEclipses.length > 0;
			}

			public void aboutToStep() {
				StringBuffer text = new StringBuffer();
				for (int i = 0; i < runningEclipses.length; i++) {
					EclipseInstallation each = runningEclipses[i];
					text.append(each.getEclipseDir().getAbsolutePath());
					text.append(" ");
					text.append(each.getDescription());
					text.append("\n");
				}
				setText(text.toString());
			}
		};
		initStep(step, "EclipseRunning");
		installer.add(step);
		return step;
	}

	/**
	 * Create a step to delete specific files and directories cached the configuration
	 * directory so that the next time that Eclipse is started, it will properly recache
	 * information about the product's bundles.
	 * 
	 * @return the install step (not <code>null</code>)
	 */
	protected ChoiceStep cleanConfigStep() {
		ChoiceStep step = new ChoiceStep(installer);
		initStep(step, OPTION_CLEAN_CONFIG);
		step.setChoiceText(options.getString("CleanConfigText"));
		step.setAcceptText(options.getString("CleanConfigYes"));
		step.setDeclineText(options.getString("CleanConfigNo"));
		step.setOptionName(OPTION_CLEAN_CONFIG);
		step.setDefaultChoice(options.getBoolean(OPTION_CLEAN_CONFIG));
		installer.add(step);
		return step;
	}

	/**
	 * Create a step detecting the an embedded installation of the product and prompting
	 * the user to remove that embedded installation.
	 * 
	 * @return the step (not <code>null</code>)
	 */
	protected RemoveAllEmbeddedInstallsStep removeEmbeddedInstallStep() {
		readEmbeddedFeatureAndPluginList();

		if (options.isVerbose()) {
			System.out.println("--- Embedded features and plugins to be removed");
			System.out.println(RemoveAllEmbeddedInstallsStep.OPTION_EMBEDDED_FEATURES + " = "
				+ options.getString(RemoveAllEmbeddedInstallsStep.OPTION_EMBEDDED_FEATURES));
			System.out.println();
			System.out.println(RemoveAllEmbeddedInstallsStep.OPTION_EMBEDDED_PLUGINS + " = "
				+ options.getString(RemoveAllEmbeddedInstallsStep.OPTION_EMBEDDED_PLUGINS));
			System.out.println();
		}

		RemoveAllEmbeddedInstallsStep step = new RemoveAllEmbeddedInstallsStep(installer);
		installer.add(step);
		return step;
	}

	/**
	 * Read the lists of embedded features and plugins from the features.properties and
	 * plugins.properties files respectively. These files are dynamically generated at
	 * build time and placed into the same directory as the BaseProductInstaller class.
	 * Subclasses can override this method to get the list of features from elsewhere or
	 * augment this method to add additional features and/or plugins.
	 */
	protected void readEmbeddedFeatureAndPluginList() {
		options.set(RemoveAllEmbeddedInstallsStep.OPTION_EMBEDDED_FEATURES, readSinglePropertyFile("features"));
		options.set(RemoveAllEmbeddedInstallsStep.OPTION_EMBEDDED_PLUGINS, readSinglePropertyFile("plugins"));
	}

	/**
	 * Read a single property from a file of the same name.
	 * 
	 * @param key the property key
	 * @return the property value or empty string if not defined (not <code>null</code>)
	 */
	protected String readSinglePropertyFile(String key) {
		InputStream input = BaseProductInstaller.class.getResourceAsStream(key + ".properties");
		if (input == null)
			return "";
		Properties temp = new Properties();
		try {
			temp.load(input);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				input.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return temp.getProperty(key, "");
	}

	/**
	 * Create a step prompting the user to verify the installation selections before the
	 * code is actually installed. This calls {@link #createInstallDescription()} to
	 * generate a description of what is to be installed and where. Once the user has
	 * verified the installation, the user cannot click the Back button to modify
	 * installation options.
	 * 
	 * @return the installation step created
	 */
	protected PromptUserStep verifyInstallStep() {
		final PromptUserStep step = new PromptUserStep(installer) {
			public void aboutToStep() {
				setText(createInstallDescription());
			}
		};
		initStep(step, "VerifyInstall");
		installer.add(step);
		return step;
	}

	/**
	 * Create a description of what is to be installed and where.
	 * 
	 * @return the description (not <code>null</code>, not empty)
	 */
	protected String createInstallDescription() {
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);

		// Product being installed
		writer.println("Product:");
		writer.println("   " + options.getString(InstallOptions.OPTION_PRODUCT_NAME));

		// Subproduct install locations
		File rootInstallDir = new File(options.getString(InstallOptions.OPTION_INSTALL_DIR));
		SubProduct[] subProducts = getSubProductsToInstall();
		writer.println("");
		writer.println("Locations:");
		for (int i = 0; i < subProducts.length; i++) {
			SubProduct each = subProducts[i];
			// Exclude PDE from list shown to user
			if (hiddenSubproducts.contains(each))
				continue;
			File installSubDir = new File(rootInstallDir, each.getInstallDirName());
			writer.println("   " + installSubDir.getAbsolutePath());
		}

		// Linked Eclipse installations
		String[] eclipsePaths = InstallOptions.pathListToArray(options
			.getString(InstallOptions.OPTION_ECLIPSE_PATH_LIST));
		if (eclipsePaths != null && eclipsePaths.length > 0) {
			writer.println("");
			writer.println("Linked to:");
			for (int i = 0; i < eclipsePaths.length; i++)
				writer.println("   " + eclipsePaths[i]);
		}

		// Additional installation options
		boolean cleanConfig = options.getBoolean(OPTION_CLEAN_CONFIG);
		boolean deleteEmbedded = options.getBoolean(OPTION_DELETE_EMBEDDED);
		if (cleanConfig || deleteEmbedded) {
			writer.println("");
			writer.println("Additional install options:");
			if (deleteEmbedded)
				writer.println("   Delete embedded installations");
			if (cleanConfig)
				writer.println("   Clean Eclipse configuration directories");
		}

		return stringWriter.toString();
	}

	/**
	 * Create a step to install the actual code. This calls
	 * {@link #createInstallOperations(RunOperationsStep)} to create the operations
	 * necessary to install the code.
	 * 
	 * @return the installation step created
	 */
	protected RunOperationsStep installCodeStep() {
		final RunOperationsStep step = new RunOperationsStep(installer) {
			public void aboutToStep() {
				try {
					createInstallOperations(this);
				}
				catch (IOException e) {
					showException(e);
				}
			}
		};
		initStep(step, "InstallCode");
		installer.add(step);
		return step;
	}

	/**
	 * Create the install operations based upon the user's choices.
	 * 
	 * @param step the step about to be performed
	 */
	protected void createInstallOperations(RunOperationsStep step) throws IOException {

		// get the primary product
		SubProduct primary = getPrimaryProduct();
		// Get subproducts to be installed
		SubProduct[] subProducts = getSubProductsToInstall();

		// Assemble install operations
		String selfPath = System.getProperty(Context.INSTALLER_JAR_PROPERTY);

		// The file containing code to be delivered
		ZipFile image = new ZipFile(selfPath);

		// The directory into which the product will be installed
		File installDir = new File(options.getString(InstallOptions.OPTION_INSTALL_DIR));

		// Cleanup any other Windows registry entries pointing to this product
		step.add(new CleanupRegisteredProductsOperation(CreateUninstallerOperation.UNINSTALL_JAR));

		// For each selected Eclipse installation, determine which code to install
		// whether it is already installed, and link the Eclipse installation to that code

		String[] eclipsePathList = InstallOptions.pathListToArray(options
			.getString(InstallOptions.OPTION_ECLIPSE_PATH_LIST));
		Collection installs = new HashSet(20);
		for (int i = 0; i < eclipsePathList.length; i++) {

			// For a given Eclipse installation, determine the code to be installed

			EclipseInstallation eclipse = new EclipseInstallation(new File(eclipsePathList[i]));
			IProductVersion eclipseTarget = adjustEclipseTarget(eclipse.getEclipseVersion());

			if (options.isVerbose()) {
				System.out.println("eclipse");
				System.out.println(" found eclipse version = " + eclipse.getEclipseVersion());
				System.out.println(" found product version = " + eclipse.getProductVersion());
				System.out.println(" mapped version = " + eclipseTarget.getMajor() + "." + eclipseTarget.getMinor()
					+ "." + eclipseTarget.getService());
				System.out.println("processing " + eclipsePathList[i]);
				System.out.println("  installDir = " + installDir);
				System.out.println("  eclipse found version = ");
				System.out.println("  targetDir = E-" + (eclipseTarget.getMajor() + "." + eclipseTarget.getMinor()));
				System.out.println("  selfpath = " + selfPath);
				System.out.println("  image = " + image);
			}

			for (int j = 0; j < subProducts.length; j++) {
				SubProduct each = subProducts[j];
				if (options.isVerbose()) {
					System.out.println("processing " + each);
					System.out.println("  each.instdir = " + each.getInstallDirName());
				}

				// Check to see if subproduct can be linked to this eclipse installation

				// Allow each subproduct to determine whether they can link 
				// to this eclipse installation and what version should be linked
				IProductVersion eachEclipseTarget = each.adjustEclipseTarget(image, eclipse, eclipseTarget, options);

				if (eachEclipseTarget == null) {
					if (options.isVerbose())
						System.out.println("  cannot link " + each + " to " + eclipsePathList[i]);
					continue;
				}

				if (options.isVerbose()) {
					System.out.println("can link to " + eclipse.getProductDir().getAbsolutePath());
				}

				String eachEclipseTargetString = eachEclipseTarget.getMajor() + "." + eachEclipseTarget.getMinor();

				String relInstallPath = each.getInstallDirName() + File.separator + "E-" + eachEclipseTargetString;
				File installSubDir = new File(installDir, relInstallPath);

				// Install the sub product if not already installed

				if (!installs.contains(relInstallPath)) {
					installs.add(relInstallPath);
					if (options.isVerbose()) {
						System.out.println("searching for " + BaseProductInstaller.INSTALL_IMAGE + "/" + each.getName()
							+ "/E-" + eachEclipseTargetString);
						System.out.println(BaseProductInstaller.INSTALL_IMAGE + "/" + each.getName() + "/E-common");
					}

					ZipEntry[] entries = each.getEntries(image, eachEclipseTarget);
					if (entries == null) {
						throw new FileNotFoundException("Nothing found searching for " + 
														BaseProductInstaller.INSTALL_IMAGE + "/" + 
														each.getName()+
														"/E-" + eachEclipseTargetString + " and " +
														BaseProductInstaller.INSTALL_IMAGE + "/" + 
														each.getName() + "/E-common");
					}
					if (options.isVerbose()) {
						if (entries == null) {
							System.out.println("ZipEntries is null");
						}
						else {
							System.out.println("ZipEntries size " + entries.length);
						}
					}
					for (int k = 0; k < entries.length; k++) {
						if (options.isVerbose()) {
							if (entries[k] == null) {
								System.out.println("processing ZipEntry[ " + k + "] is null");
							}
							else {
								System.out.println("ZipEntry [" + k + "] = " + entries[k].getName());
							}
						}
						if (entries[k] == null) {
							throw new FileNotFoundException("one of the following is not found " + 
															BaseProductInstaller.INSTALL_IMAGE + "/" + 
															each.getName()+
															"/E-" + eachEclipseTargetString + " or " +
															BaseProductInstaller.INSTALL_IMAGE + "/" + 
															each.getName() + "/E-common");
						}
						step.add(new ExtractDirectoryOperation(image, entries[k], installSubDir));
					}
				}

				// Link the eclipse installation to the installed code

				step.add(new LinkProductOperation(options, new String[]{
					eclipse.getProductDir().getAbsolutePath()
				}, each.getLinkId(), installSubDir));

				// If CLeanup CruiseControl Style link files
				if (each.getName().equalsIgnoreCase("CodeProCore")) {
					if (options.isVerbose()) {
						System.out.println("Removing ");
						System.out.println(" com.instantiations.assist.eclipse.link");
						System.out.println(" com.instantiations.codepro.core.link");
					}
					createDeleteOldLinkOperation(step, eclipse, "com.instantiations.assist.eclipse.link");
					createDeleteOldLinkOperation(step, eclipse, "com.instantiations.codepro.core.link");
				}
				if (each.getName().equalsIgnoreCase("CodeProPlusPak")) {
					if (options.isVerbose()) {
						System.out.println("Removing ");
						System.out.println(" com.instantiations.codepro.pluspak.feature.link");
					}
					createDeleteOldLinkOperation(step, eclipse, "com.instantiations.codepro.pluspak.feature.link");
				}
				if (each.getName().equalsIgnoreCase("CodeProComm")) {
					if (options.isVerbose()) {
						System.out.println("Removing ");
						System.out.println(" com.instantiations.assist.eclipse.analytix.link");
					}
					createDeleteOldLinkOperation(step, eclipse, "com.instantiations.assist.eclipse.analytix.link");
				}
				if (each.getName().equalsIgnoreCase("UnitTester")) {
					if (options.isVerbose()) {
						System.out.println("Removing ");
						System.out.println(" com.instantiations.unittester.link");
					}
					createDeleteOldLinkOperation(step, eclipse, "com.instantiations.unittester.link");
				}
				if (each.getName().equalsIgnoreCase("CodeCoverage")) {
					if (options.isVerbose()) {
						System.out.println("Removing ");
						System.out.println(" com.instantiations.eclipse.coverage.link");
					}
					createDeleteOldLinkOperation(step, eclipse, "com.instantiations.eclipse.coverage.link");
				}
				if (each.getName().equalsIgnoreCase("AppAnalysis")) {
					if (options.isVerbose()) {
						System.out.println("Removing ");
						System.out.println(" com.instantiations.eclipse.analysis.appanalysis.link");
					}
					createDeleteOldLinkOperation(step, eclipse, "com.instantiations.eclipse.analysis.appanalysis.link");
				}
				if (each.getName().equalsIgnoreCase("Analysis")) {
					if (options.isVerbose()) {
						System.out.println("Removing ");
						System.out.println(" com.instantiations.assist.eclipse.analysis.link");
					}
					createDeleteOldLinkOperation(step, eclipse, "com.instantiations.assist.eclipse.analysis.link");
				}
				if (each.getName().equalsIgnoreCase("CodeProAnt")) {
					if (options.isVerbose()) {
						System.out.println("Removing ");
						System.out.println(" com.instantiations.codepro.ant.link");
					}
					createDeleteOldLinkOperation(step, eclipse, "com.instantiations.codepro.ant.link");
				}
				if (each.getName().equalsIgnoreCase("Shared")) {
					if (options.isVerbose()) {
						System.out.println("Removing ");
						System.out.println(" com.instantiations.shared.link");
					}
					createDeleteOldLinkOperation(step, eclipse, "com.instantiations.shared.link");
				}
				if (each.getName().equalsIgnoreCase("WindowTesterRuntime")) {
					if (options.isVerbose()) {
						System.out.println("Removing ");
						System.out.println(" com.instantiations.windowtester.link");
					}
					createDeleteOldLinkOperation(step, eclipse, "com.instantiations.windowtester.link");
				}
			}
		}

		if (options.getBoolean(OPTION_DELETE_EMBEDDED))
			step.add(new RemoveAllEmbeddedInstallsOperation(options));
		if (options.getBoolean(OPTION_CLEAN_CONFIG))
			step.add(new CleanAllEclipseConfigurations(options));

		String primaryProductDirName = primary.getInstallDirName();
		File primaryProductDir = new File(installDir, primaryProductDirName);

		// Dynamically set the Uninstall comment to the installation location before registering the product
		options.set("UninstallComments", options.getString(InstallOptions.OPTION_INSTALL_DIR));
//		step.add(new RegisterProductOperation(options, primaryProductDirName + File.separator
//			+ CreateUninstallerOperation.UNINSTALL_JAR));
		
		// Create the uninstaller *after* registering the product so that creation of registry keys gets logged
		step.add(new CreateUninstallerOperation(primaryProductDir, CreateUninstallerOperation.UNINSTALL_JAR,
			INSTALL_IMAGE));

		if (isWrapperProduct(primary, subProducts)) {
			String prefix = primary.getName() + "_v" + primary.getVersion();
			String exclude = primary.getName() + "_v" + primary.getFullVersion();
			step.add(new ScanUnusedInstallsOperation(installDir, prefix, exclude));
		}
		step.add(new ScanUnlinkedInstallsOperation(installDir));
		step.add(new CleanRegistryOperation(options, primaryProductDir.getAbsolutePath()));
	}

	/**
	 * Determine if the primary product is a wrapper product containing only an
	 * installation log.
	 * 
	 * @param primary the primary product
	 * @param subProducts the subproducts
	 * @return <code>true</code> if the primary product is a wrapper product
	 */
	public static boolean isWrapperProduct(SubProduct primary, SubProduct[] subProducts) {
		/*
		 * If the primary product is in the list of sub-products with content to be installed,
		 * then the primary product is NOT a wrapper product
		 */
		for (int i = 0; i < subProducts.length; i++)
			if (subProducts[i] == primary)
				return false;
		return true;
	}

	/**
	 * Given the version of an Eclipse installation (e.g. "3.0", "3.1", etc) answer the
	 * version of Eclipse for which this product is compiled that is most compatible with
	 * Eclipse installation, or <code>null</code> if this product is not compatible with
	 * that Eclipse installation.
	 * 
	 * @param IProductVersion the specified version of an Eclipse installation
	 * @return the version of Eclipse for which this product is compiled that is most
	 *         compatible with the specified Eclipse installation version
	 */
	protected IProductVersion adjustEclipseTarget(IProductVersion version) {
		// TODO [author=Dan] issue a warning about unsupport installation
		if (version.getMajor() > 3 || version.getMinor() > 4) {
			return new EclipseVersion(3, 4, 0, null);
		}
		return new EclipseVersion(version.getMajor(), version.getMinor(), 0, null);
	}

	/**
	 * An optional "cleanup" confirmation step that appears only if older unused
	 * installations are detected.
	 * 
	 * @return the install step (not <code>null</code>)
	 */
	protected ChoiceStep checkUnlinkedInstallsStep() {
		final ChoiceStep step = new ChoiceStep(installer) {
			public boolean canBack() {
				return false;
			}

			public boolean canExecute() {
				String[] unlinkedInstallPaths = installer.getOptions().getStrings(
					ScanUnlinkedInstallsOperation.OPTION_UNLINKED_INSTALL_DIRS);
				String[] unusedInstallPaths = installer.getOptions().getStrings(
					ScanUnusedInstallsOperation.OPTION_UNUSED_INSTALL_DIRS);
				return (unlinkedInstallPaths.length > 0 || unusedInstallPaths.length > 0)
					&& options.getBoolean(OPTION_CLEAN_CONFIG);
			}

			public void aboutToStep() {
				super.aboutToStep();
				StringWriter stringWriter = new StringWriter();
				PrintWriter writer = new PrintWriter(stringWriter);
				String[] unlinkedInstallPaths = installer.getOptions().getStrings(
					ScanUnlinkedInstallsOperation.OPTION_UNLINKED_INSTALL_DIRS);
				String[] unusedInstallPaths = installer.getOptions().getStrings(
					ScanUnusedInstallsOperation.OPTION_UNUSED_INSTALL_DIRS);
				for (int i = 0; i < unlinkedInstallPaths.length; i++)
					writer.println(unlinkedInstallPaths[i]);
				for (int i = 0; i < unusedInstallPaths.length; i++)
					writer.println(unusedInstallPaths[i]);
				setChoiceText(stringWriter.toString());
			}
		};
		step.setTitle("Delete old installations");
		step.setDescription("The following installations appear to be unused."
			+ "\nDo you want to delete these installations?");
		step.setChoiceText("Scanning for unused installations...");
		step.setAcceptText("Yes, delete the installations listed above");
		step.setDeclineText("No, do NOT delete any installations");
		step.setDefaultChoice(false);
		step.setOptionName(OPTION_DELETE_UNUSED_INSTALLATIONS);
		installer.add(step);
		return step;
	}

	/**
	 * An optional "cleanup" step that appears only if older unused installations are
	 * detected and the user has chosen in the prior step to clean them up.
	 * 
	 * @return the install step (not <code>null</code>)
	 */
	protected InstallStep cleanUnlinkedInstallsStep() {
		final RunOperationsStep step = new RunOperationsStep(installer) {
			public boolean canExecute() {
				return options.getBoolean(OPTION_DELETE_UNUSED_INSTALLATIONS)
					&& options.getBoolean(OPTION_CLEAN_CONFIG);
			}

			public boolean canRollback() {
				return false;
			}

			public void aboutToStep() {
				File installDir = new File(options.getString(InstallOptions.OPTION_INSTALL_DIR));
				String primaryProductDirName = getPrimaryProduct().getInstallDirName();
				File primaryProductDir = new File(installDir, primaryProductDirName);

				add(new CleanUnlinkedInstallsOperation(installer));
				add(new CleanRegistryOperation(options, primaryProductDir.getAbsolutePath()));
			}
		};
		step.setTitle("Delete old installations");
		step.setDescription("Deleting old installations... this may take a few minutes...");
		installer.add(step);
		return step;
	}

	/**
	 * An optional "warning" step that appears only if there were non-critical problems
	 * during the installation that need to be brought to the user's attention.
	 * 
	 * @return the install step (not <code>null</code>)
	 */
	protected ShowWarningsStep installResultStep() {
		final ShowWarningsStep step = new ShowWarningsStep(installer) {
			public boolean canBack() {
				return false;
			}
		};
		step.setTitle("Warning");
		step.setDescription("Installation program failed to perform the following operations:");
		installer.add(step);
		return step;
	}

	/**
	 * Create a step indicating to the user that installation is complete
	 * 
	 * @return the installation step created
	 */
	protected PromptUserStep installCompleteStep() {
		final PromptUserStep step = new PromptUserStep(installer) {
			public boolean canBack() {
				return false;
			}
		};
		initStep(step, "InstallComplete");
		step.setText(options.getString("InstallCompleteText"));
		installer.add(step);
		return step;
	}

	/**
	 * Initialize common step properties from the install options.
	 * 
	 * @param step the step to be initialized
	 * @param adjustEclipseTarget(stepName) the name of the step used as a prefix for the
	 *            install options key
	 */
	protected void initStep(InstallStep step, String stepName) {
		step.setTitle(options.getString(stepName + "Title"));
		step.setDescription(options.getString(stepName + "Description"));
	}

	// //////////////////////////////////////////////////////////////////////////
	//
	// Uninstall
	//
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * Create the steps to uninstall the product
	 */
	protected void createUninstall() {
		installer.setTitle(options.getString(InstallOptions.OPTION_PRODUCT_NAME) + " Uninstaller");

		// Get the directory where the INSTALLER_JAR is.
		String selfPath = System.getProperty(Context.INSTALLER_JAR_PROPERTY);

		// Assume that it is the same as the installation directory.
		File self = new File(selfPath);
		options.set(InstallOptions.OPTION_INSTALL_DIR, self.getParent());

		// Retrieve the linked Eclipse installations from the LOG file
		File logFile = new File(self.getParentFile(), CreateUninstallerOperation.INSTALL_LOG);

		if (logFile.exists()) {
			verifyUninstallStep();
			uninstallCodeStep();
			uninstallCompletStep();
		}
		else {
			alreadyUninstalledStep();
		}
	}

	/**
	 * Create a step that prompts the user to verify the uninstall. This calls
	 * {@link #createUninstallDescription(File, SubProduct[], EclipseInstallation[])} to
	 * generate a description of what is to be uninstalled and what other programs will be
	 * affected. Once the user has verified the uninstall, the user cannot click the Back
	 * button to modify uninstall options.
	 * 
	 * @return the installation step created
	 */
	protected PromptUserStep verifyUninstallStep() {
		final PromptUserStep step = new PromptUserStep(installer) {
			public void aboutToStep() {

				// Get the directory where the INSTALLER_JAR is.
				String selfPath = System.getProperty(Context.INSTALLER_JAR_PROPERTY);

				// Assume that it is the same as the installation directory.
				File self = new File(selfPath);
				options.set(InstallOptions.OPTION_INSTALL_DIR, self.getParent());

				// Retrieve the linked Eclipse installations from the LOG file
				File logFile = new File(self.getParentFile(), CreateUninstallerOperation.INSTALL_LOG);
				EclipseInstallation[] linkedInstallations = EclipseInstallation.findLinked(logFile);

				// The directory from which the product will be uninstalled
				File installDir = new File(options.getString(InstallOptions.OPTION_INSTALL_DIR));

				// Get subproducts to be uninstalled
				SubProduct[] subProducts = getSubProductsToInstall();

				setText(createUninstallDescription(installDir, subProducts, linkedInstallations));
			}
		};
		step.setTitle("Verify uninstall");
		step
			.setDescription("This wizard will uninstall " + options.getString(InstallOptions.OPTION_PRODUCT_NAME) + ".");
		installer.add(step);
		return step;
	}

	/**
	 * Create a description of what is to be uninstalled and what other programs will be
	 * affected.
	 * 
	 * @param installDir The directory from which products will be uninstalled
	 * @param subProducts An array of sub products to be uninstalled
	 * @param linkedInstallations An array of Eclipse installations linked to this
	 *            installation (not <code>null</code>, contains no <code>null</code>s)
	 * @return the description (not <code>null</code>, not empty)
	 */
	protected String createUninstallDescription(File installDir, SubProduct[] subProducts, EclipseInstallation[] linkedInstallations)
	{
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		writer.print("Do you wish to uninstall ");
		writer.print(options.getString(InstallOptions.OPTION_PRODUCT_NAME));
		writer.println("?");

		// Append a list of subproducts to be uninstalled
		writer.println();
		writer.println("Location:");
		for (int i = 0; i < subProducts.length; i++) {
			SubProduct each = subProducts[i];
			File installSubDir = new File(installDir, each.getInstallDirName());
			writer.print("   ");
			writer.println(installSubDir.getAbsolutePath());
		}

		// Append a list of linked Eclipse installations
		if (linkedInstallations != null && linkedInstallations.length > 0) {
			writer.println();
			writer.println("Unlink from:");
			for (int i = 0; i < linkedInstallations.length; i++) {
				writer.print("   ");
				writer.println(linkedInstallations[i].getEclipseDir().getAbsolutePath());
			}
		}
		return stringWriter.toString();
	}

	/**
	 * Create a step that uninstalls the code and unlinks it from any existing Eclipse
	 * installations.
	 * 
	 * @return the installation step created
	 */
	protected RunOperationsStep uninstallCodeStep() {
		final RunOperationsStep step = new RunOperationsStep(installer) {
			public void aboutToStep() {
				createUninstallOperations(this);
			}
		};
		step.setDescription("Uninstalling...");
		installer.add(step);
		return step;
	}

	/**
	 * Create the uninstall operations based upon the user's choices
	 * 
	 * @param step the step about to be performed
	 */
	protected void createUninstallOperations(RunOperationsStep step) {
		// Assemble uninstall operations
		String selfPath = System.getProperty(Context.INSTALLER_JAR_PROPERTY);
		File self = new File(selfPath);
		File productDir = self.getParentFile();
		File logFile = new File(productDir, CreateUninstallerOperation.INSTALL_LOG);
		File cleanDir = productDir.getParentFile();
		if (cleanDir == null || !cleanDir.getName().equalsIgnoreCase("Instantiations"))
			cleanDir = productDir;

		step.add(new UninstallOperation(logFile));
		step.add(new CleanupRegisteredProductsOperation(CreateUninstallerOperation.UNINSTALL_JAR));
		step.add(new CleanEmptyDirectoryOperation(options, cleanDir));
		step.add(new CleanRegistryOperation(options, logFile.getParent()));
	}

	/**
	 * Create a step that notifies the user that the uninstallation is complete or that it
	 * will be once the OS reboots.
	 * 
	 * @return the installation step created
	 */
	protected PromptUserStep uninstallCompletStep() {
		final PromptUserStep step = new PromptUserStep(installer) {
			public void aboutToStep() {
				String[] filesRemovedOnReboot = options.getStrings(UninstallOperation.OPTION_FILES_REMOVED_ON_REBOOT);
				StringBuffer buf = new StringBuffer(200 + filesRemovedOnReboot.length * 40);
				if (filesRemovedOnReboot.length > 0) {
					buf.append("The following files will be removed when you reboot:\n");
					for (int i = 0; i < filesRemovedOnReboot.length; i++) {
						buf.append("\t");
						buf.append(filesRemovedOnReboot[i]);
						buf.append("\n");
					}
					buf.append("\n");
				}
				buf.append("Click Finish to close this wizard.");
				setText(buf.toString());
			}
		};
		step.setDescription("Uninstall complete.");
		installer.add(step);
		return step;
	}

	/**
	 * Create a step to inform the user that the product has already been uninstalled
	 * 
	 * @return the created step (not <code>null</code>)
	 */
	protected PromptUserStep alreadyUninstalledStep() {
		final PromptUserStep step = new PromptUserStep(installer);
		step.setTitle("Already uninstalled");

		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		writer.print(options.getString(InstallOptions.OPTION_PRODUCT_NAME));
		writer.print(" has already been uninstalled.");

		step.setDescription(stringWriter.toString());

		writer.println();
		writer.print("The remaining files should be removed when the machine is rebooted.");

		step.setText(stringWriter.toString());
		installer.add(step);
		return step;
	}

	// //////////////////////////////////////////////////////////////////////////
	//
	// Utility
	//
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * Determine if the installer should default to "uninstall" mode by checking for the
	 * existance of the "install-image" directory containing code to be installed. If that
	 * directory does not exist, then this is the "uninstaller" generated by the
	 * CreateUninstallerOperation.
	 * 
	 * @return <code>true</code> if uninstall operation should be performed, else
	 *         <code>false</code>
	 */
	protected boolean isUninstaller() {
		String selfPath = System.getProperty(Context.INSTALLER_JAR_PROPERTY);
		if (selfPath == null) {
			return false;
		}

		try {
			ZipFile image = new ZipFile(selfPath);
			ZipEntry installImage = image.getEntry(INSTALL_IMAGE);
			if (installImage == null) {
				return true;
			}
		}
		catch (IOException ex) {
			return false;
		}
		return false;
	}

	/**
	 * Answer the primary sub product
	 * 
	 * @return the sub product (not <code>null</code>)
	 */
	protected abstract SubProduct getPrimaryProduct();

	/**
	 * Answer the sub products to be installed
	 * 
	 * @return an array of sub products (not <code>null</code>, contains no
	 *         <code>null</code>s), must include the product returned by
	 *         <code>getPrimaryPruduct()</code>.
	 */
	protected abstract SubProduct[] getSubProductsToInstall();

	/**
	 * Check for old link files pointing to older installations that would interfere with
	 * the operation of the version about to be installed.
	 */
	protected void createDeleteOldLinkOperations(final RunOperationsStep step, String linkFileName) {
		String[] eclipsePathList = InstallOptions.pathListToArray(options
			.getString(InstallOptions.OPTION_ECLIPSE_PATH_LIST));
		for (int i = 0; i < eclipsePathList.length; i++) {
			final EclipseInstallation eclipse = new EclipseInstallation(new File(eclipsePathList[i]));
			createDeleteOldLinkOperation(step, eclipse, linkFileName);
		}
	}

	/**
	 * Check for old link file pointing to older installations that would interfere with
	 * the operation of the version about to be installed.
	 */
	protected void createDeleteOldLinkOperation(final RunOperationsStep step, final EclipseInstallation eclipse, String linkFileName)
	{
		final String eclipseDir = eclipse.getProductDir().getAbsolutePath();
		final File linkFile = new File(new File(eclipseDir, "links"), linkFileName);

		if (options.isVerbose())
			System.out.println("Checking for " + linkFile.getAbsolutePath());
		if (!linkFile.exists())
			return;

		step.add(new InstallOperation() {
			public String toString() {
				return "Deleting " + linkFile.getAbsolutePath();
			}

			protected IStatus run(Context installer) {
				if (options.isVerbose())
					System.out.println(toString());
				if (!linkFile.delete())
					return createStatus(step, "Failed to delete: " + linkFile.getAbsolutePath());
				return Status.OK_STATUS;
			}
		});
	}

	/**
	 * Create a error status object with the specified message
	 * 
	 * @param step the install operation step generating the error (not <code>null</code>)
	 * @param message the error message (not <code>null</code>)
	 * @return the error status object (not <code>null</code>)
	 */
	protected Status createStatus(RunOperationsStep step, String message) {
		String title = step.getOptions().getString("InstallerTitle");
		return new Status(IStatus.ERROR, title, 0, message, null);
	}

	/**
	 * Open a dialog showing the user the problem, and print the stack trace to standard
	 * out or standard error.
	 * 
	 * @param e the exception that occurred
	 */
	protected static void showException(Throwable e) {

		// First do the simplest thing
		e.printStackTrace();

		// Next generate a log file if possible
		String selfPath = System.getProperty(Context.INSTALLER_JAR_PROPERTY);
		if (selfPath != null) {
			File logFile = new File(selfPath + ".log");
			Writer logWriter = null;
			try {
				logWriter = new BufferedWriter(new FileWriter(logFile));
				e.printStackTrace(new PrintWriter(logWriter));
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}
			finally {
				try {
					if (logWriter != null)
						logWriter.close();
				}
				catch (IOException e2) {
					e2.printStackTrace();
				}
			}
		}

		// Finally attempt to notify the user
		// but this may not work if the UI is not properly initialized
		MessageDialog.openError(null, "Install Problem", "Installation Problem: " + e.toString());
	}

	/**
	 * Add a subproduct to the list of subproducts to skip when displaying the subproducts
	 * on the Install Directory and Verify Install pages
	 * 
	 * @param subproduct a subproduct to add to the list
	 */
	public void hideSubproduct(SubProduct subproduct) {
		hiddenSubproducts.add(subproduct);
	}
}
