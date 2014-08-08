package com.instantiations.pde.build

import com.instantiations.pde.build.subproduct.SubProductZipReader
import com.instantiations.pde.build.util.BuildProperties;
import com.instantiations.pde.build.util.BuildUtil
import com.instantiations.pde.build.util.ProductDownloader;
import com.instantiations.pde.build.util.Version;
import groovy.text.Templateimport groovy.text.SimpleTemplateEngineimport com.instantiations.pde.build.util.OemVersion
/**
 * Builder of installers
 */
public class InstallerBuild extends BuildUtil
{
	// Initialize these properties when object is instantiated
	ProductDownloader productCache;
	File installerProjDir;			// The project directory containing the product specific installer files (e.g. META-INF/INSTALLER.MF)
	File templateProjDir;			// The project directory containing the generic installer files (e.g. META-INF/INSTALLER.MF)
	Version targetVersionLow;		// Inclusive (e.g. greather than or equal to 3.1)
	Version targetVersionHigh;		// Exclusive (e.g. less than 3.4)
	
	// Optional... Initialized automatically to contain only the primary project if not already initialized
	Collection<String> allSubproductNames  // The names of all subproducts to be installed
	
	// Optional... Initialized automatically if not already initialized
	File installerTemp;				// Temporary directory used when compiling and assembling the installer
	
	protected void initTemp() {
		if (installerTemp == null)
			installerTemp = new File(prop.productTemp, '/installer');
		if (allSubproductNames == null)
			allSubproductNames = new HashSet<String>([ prop.productName ]);
	}
	
	/**
	 * Answer the directory containing bits to be installed
	 */
	private File getInstallImageDir() {
		initTemp();
		return new File(installerTemp, 'install-image');
	}
	
	/**
	 * Expand the RCPInstaller *.zip file for use during installer compilation and assembly
	 */
	 public void unzipRCPInstaller(File installerZip) {
		initTemp();
		File libTemp = new File(installerTemp, 'libTemp');
		ant.mkdir(dir: libTemp);
		ant.copy(todir: libTemp) {
			zipfileset(src: installerZip) {
				include(name: 'plugins/com.instantiations.installer_*/lib/**');
				include(name: 'plugins/com.instantiations.installer_*/template/**');
				include(name: 'plugins/com.instantiations.installer_*/installerTools.jar');
				exclude(name: '**/src/**');
				exclude(name: '**/src.zip');
			}
		}
		File installerPlugin = new File(libTemp, 'plugins').listFiles()
			.find { dir -> dir.name.startsWith('com.instantiations.installer_'); }
		ant.move(todir: installerTemp) {
			fileset(dir: installerPlugin);
		}
		ant.delete(dir: libTemp);
	}
	
	/**
	 * Unzip the SubProduct into the install image for assembly and delivery
	 */
	public void unzipInstallImage(File productZip, String productId, String productName, String productVersion, Version eclipseVersion) {
		initTemp();
		allSubproductNames.add(productName);
		File dest = new File(getInstallImageDir(), productName + '/E-' + eclipseVersion + '/eclipse');
		dest.mkdirs();
		ant.copy(todir: dest) {
			zipfileset(src: productZip);
		}
		new File(dest, '.eclipseextension').write(
			'id=' + productId
			+ '\nname=' + productName
			+ '\nversion=' + productVersion);
	}
	
	/**
	 * Move the SubProduct into the install image for assembly and delivery
	 */
	public void moveInstallImage(File productDir, String productId, String productName, String productVersion, Version eclipseVersion) {
		initTemp();
		allSubproductNames.add(productName);
		File dest = new File(getInstallImageDir(), productName + '/E-' + eclipseVersion + '/eclipse');
		dest.mkdirs();
		ant.move(todir: dest) {
			fileset(dir: productDir);
		}
		new File(dest, '.eclipseextension').write(
			'id=' + productId
			+ '\nname=' + productName
			+ '\nversion=' + productVersion);
	}
	
	/**
	 * The main entry point to build an installer
	 */
	public void buildInstaller() {
		generateSubProduct();
		copyInstallerSource();
		compileInstaller();
		scanForCommon();
		assembleInstaller();
	}
	
	/**
	 * Answer the relative path of the SubProduct file for the specified product
	 */
	private String getSubproductJavaName(String prodName) {
		return 'com/instantiations/subproducts/' + prodName + 'SubProduct.java';
	}

	/**
	 * Generate a subclass of the installer SubProduct class that can be used in this
	 * installer and any installers that include this product.
	 */
	public void generateSubProduct() {
		generateSubProduct(
			prop.productName, 
			prop.productId,
			prop.productVersion, 
			prop.buildNum,
			productCache.localFile(null, 'installer/src/' + getSubproductJavaName(prop.productName)));
	}
	
	/**
	 * If the SubProduct class does not exist, first try to download it
	 * and failing that, then generate it. This exists as a stop-gap measure
	 * because not all products are yet built on Hudson 
	 * and thus do not have associated SubProduct source files.
	 */
	public SubProductZipReader downloadOrGenerateSubProduct(String name, OemVersion targetVersion) {
		SubProductZipReader reader = new SubProductZipReader(
			zipFile: productCache.downloadFile(name, targetVersion), 
			prop: prop);
		 File localFile = productCache.downloadFile(name, null, 'installer/src/' + getSubproductJavaName(name), false);
		 if (!localFile.exists()) {

			// Scan the SubProduct zip file for information
			String fullVersion = reader.fullVersion;
			String id = reader.id;
			String version = fullVersion.substring(0, fullVersion.lastIndexOf('.'));
			String buildNum = fullVersion.substring(fullVersion.lastIndexOf('.') + 1);
			
			// Generate the subproduct
			generateSubProduct(name, id, version, buildNum, localFile);
		}
		return reader;
	}

	/**
	 * Generate a subclass of the installer Subproduct class that can be used in this
	 * installer and any installers that include this product.
	 */
	private void generateSubProduct(String name, String id, String version, String buildNum, File output) {
		
		// Search for the subproduct source file for this build in the following places
		// * the product installer project's src folder 
		// * the product installer project's templates folder 
		// * the build project's installer folder
		// * Finally use the installer template project's generic template
		String javaName = getSubproductJavaName(name);
		File javaFile = new File(installerProjDir, 'src/' + javaName);
		if (!javaFile.exists())
			javaFile = new File(installerProjDir, 'templates/' + javaName);
		if (!javaFile.exists())
			javaFile = new File('installer/src/' + javaName);

		// Until all subproducts are built in Hudson, 
		// some [Name]SubProduct files will be located in the installer template project
		if (!javaFile.exists())
			javaFile = new File(templateProjDir, 'src/' + javaName);
		if (!javaFile.exists())
			javaFile = new File(templateProjDir, 'templates/' + javaName);
		
		if (!javaFile.exists())
			javaFile = new File(templateProjDir, 'templates/com/instantiations/subproducts/GenericSubProduct.java');

		// Generate the subproduct file
		Template template;
		javaFile.withReader { reader ->
			template = new SimpleTemplateEngine().createTemplate(reader); }
		ant.mkdir(dir:output.parentFile);
		Properties installProp = prop.getInstallerProperties();
		
		installProp.setProperty('product_name', name);
		installProp.setProperty('product_id', id);
		installProp.setProperty('product_version', version);
		installProp.setProperty('build_num', buildNum);
		
		output.withWriter { writer ->
			template.make(installProp).writeTo(writer); }
	}
	
	/**
	 * Copy and preprocess the installer source code from
	 * the installer template project, the product specific installer project,
	 * and the necessary SubProduct source files.
	 */
	protected void copyInstallerSource() {
		
		// Copy installer template project excluding the SubProduct template
		ant.copy(todir: new File(installerTemp, 'installer-proj')) {
			fileset(dir: templateProjDir) {
				exclude(name: 'src/com/instantiations/subproducts/**');
				exclude(name: 'templates/**');
				exclude(name: 'bin/**');
			}
		}
		ant.copy(todir: new File(installerTemp, 'installer-proj/src'), overwrite: true) {
			fileset(dir: new File(templateProjDir, 'templates')) {
				include(name: 'com/instantiations/installer/*');
			}
		}
		
		// Copy the product specific installer project (if it exists)
		if (installerProjDir.exists()) {
			println('Copying additional installer source from ' + installerProjDir.canonicalPath);
			ant.copy(todir: new File(installerTemp, 'installer-proj'), overwrite: true) {
				fileset(dir: installerProjDir) {
					exclude(name: 'src/com/instantiations/subproducts/**');
					exclude(name: 'templates/**');
					exclude(name: 'bin/**');
				}
			}
			if (new File(installerProjDir, 'templates/com/instantiations/installer').exists()) {
				ant.copy(todir: new File(installerTemp, 'installer-proj/src'), overwrite: true) {
					fileset(dir: new File(installerProjDir, 'templates')) {
						include(name: 'com/instantiations/installer/*');
					}
				}
			}
		}
		
		// Copy the product specific installer files (if any) from the build project
		File productSpecificInstallerSourceDir = new File ('installer').canonicalFile;
		if (productSpecificInstallerSourceDir.exists()) {
			println('Copying additional installer source from ' + productSpecificInstallerSourceDir.canonicalPath);
			ant.copy(todir: new File(installerTemp, 'installer-proj'), overwrite: true) {
				fileset(dir: productSpecificInstallerSourceDir) {
					exclude(name: 'src/com/instantiations/subproducts/**');
					exclude(name: 'templates/**');
					exclude(name: 'bin/**');
				}
			}
		}
		
		// Copy the generated SubProduct source files
		for (String prodName : allSubproductNames) {
			productCache.downloadFile(prodName, null, 'installer/src/' + getSubproductJavaName(prodName));
			File srcDir = productCache.downloadFile(prodName, null, 'installer/src');
			ant.copy(todir: new File(installerTemp, 'installer-proj/src'), overwrite: true) {
				fileset(dir: srcDir);
			}
		}
		
		// Preprocess the source files in the com.instantiations.installer package only
		Map installProp = prop.getInstallerProperties();
		installProp.put('installer_target_versions_low', targetVersionLow.toString());
		installProp.put('installer_target_versions_high', targetVersionHigh.toString());

		StringBuffer buf = new StringBuffer(1000);
		buf.append('PRIMARY_PRODUCT')
		for (String subproductName : new TreeSet<String>(allSubproductNames)) {
			if (subproductName != prop.productName) {
				buf.append(',\n		new com.instantiations.subproducts.');
				buf.append(subproductName);
				buf.append('SubProduct()');
			}
		}
		installProp.put('all_subproducts', buf.toString());
		
		Map installPropForPropFile = BuildProperties.newLineEnds(installProp, '\\n\\\n');
		new File(installerTemp, 'installer-proj/src/com/instantiations/installer').eachFileRecurse() { file ->
			println('preprocessing ' + file.canonicalPath);
			Template template;
			file.withReader { reader ->
				template = new SimpleTemplateEngine().createTemplate(reader);
			}
			file.withWriter { writer ->
				Map p = installProp;
				if (file.getName().endsWith('.properties'))
					p = installPropForPropFile;
				template.make(p).writeTo(writer);
			}
		}
	}
	
	/**
	 * Build the installer source for this SubProduct
	 */
	protected compileInstaller() {
		JavaBuild javaBuilder = new JavaBuild(
			eclipseTargetVersion:	Version.V_3_1,
			sourceDir:				installerTemp,
			classpathVars:			[RCP_INSTALLER_LIB: new File(installerTemp, 'lib')],
			prop:					prop);
		javaBuilder.compileToDir('installer-proj', new File(installerTemp, 'bin'));
	}
	
	/**
	 * Scan the install-image for common files
	 */
	protected scanForCommon() {
		// TODO:
	}
	
	/**
	 * Assemble the pieces into a single installer file
	 */
	protected assembleInstaller() {
		File installerJar;
		
		// Assemble the Win32 installer
		installerJar = new File(installerTemp, 'install.jar');
		ant.jar(destfile: installerJar) {
			manifest {
				attribute(name: 'Main-Class', value: 'com.instantiations.installer.launcher.Main');
			}
			metainf(dir: new File(installerTemp, 'installer-proj/META-INF'));
			fileset(dir: new File(installerTemp, 'lib')) {
				include(name: 'com/**/*.*');
				include(name: 'swt-installer/**/*.*');
				include(name: 'win32/**/*.*');
				include(name: 'win32-x86/**/*.*');
			}
			zipfileset(dir: new File(installerTemp, 'bin'), prefix: 'swt-installer');
			zipfileset(dir: getInstallImageDir(), prefix: 'install-image');
		}
		
		// Build the native installer
		File installerExe = new File(prop.productOut, prop.productName + '_v' + prop.productVersion + '_win32_x86.exe').canonicalFile;
		ant.java(
			classname:		'com.instantiations.installer.tools.packager.NativePackager',
			failonerror:	true,
			output:			new File(installerTemp, 'native-win32.log')) {
			arg(value: installerExe.canonicalPath);
			arg(value: new File(installerTemp, 'template/win32/x86/install.exe').canonicalPath);
			arg(value: '-x' + installerJar.canonicalPath);
			arg(value: '-x' + new File(installerTemp, 'installer-proj/icons/splash.bmp').canonicalPath);
			classpath {
				pathelement(location: new File(installerTemp, 'installerTools.jar'));
			}
		}

		createChecksum(installerExe);
		// Move and rename the install.jar
		ant.move(
			file: installerJar, 
			tofile: new File(prop.productOut, prop.productName + '_v' + prop.productVersion + '_win32_x86.jar'));
	}
}
