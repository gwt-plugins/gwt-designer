package test.util

import com.instantiations.pde.build.util.BuildProperties;
import com.instantiations.pde.build.util.FileDownloader;
import com.instantiations.pde.build.util.Versionimport org.apache.tools.ant.BuildException;
/**
 * Unzip a clean Eclipse environment
 */
public class InstallEclipse
{
	private final AntBuilder		ant = new AntBuilder();
	private final BuildProperties	prop = new BuildProperties();
	private final FileDownloader	fileCache = new FileDownloader(prop: prop);
	
	public static void main(String[] args) {
		println('=== Arguments:');
		for (int i = 0; i < args.length; i++)
			println('   args[' + i + '] = ' + args[i]);
		println('');
		
		new InstallEclipse().install(args);
	}

	public void install(String[] args) {
		
		println('=== Read build properties');
		prop.read();
		println('');
		
		if (args.length == 0) {
			println('=== Select a clean installation (or exit):');
			for (String item : prop.getList('clean.installs'))
				println('  ' + item);
			println('   exit');
			String line = new InputStreamReader(System.in).readLine().trim();
			if (line.length() == 0 || line == 'exit')
				return;
			args = prop.getList('clean.install.' + line);
			println('');
		}
	
		String  eclipseVersion;	// The version of Eclipse to be installed
		File    installDir;		// Directory into which Eclipse will be installed
		boolean unknownArg = false;
		boolean badInstallDir = false;
		boolean badTempDir = false;
		int index = 0;
		
		while (index < args.length) {
			switch (args[index]) {
					
				case '-eclipse':
					index++;
					if (index == args.length) {
						println('Expected Eclipse version ("3.5" or "3.4" or ...) after -eclipse argument');
						break;
					}
					eclipseVersion = args[index];
					index++;
					break;
				
				case '-installDir':
					index++;
					if (index == args.length) {
						println('Expected path argument after -installDir argument');
						break;
					}
					installDir = new File(args[index]).canonicalFile;
					badInstallDir = installDir.exists() && !new File(installDir, 'clean-install-marker.txt').exists();
					badTempDir = new File(installDir.parentFile, 'eclipse').exists();
					index++;
					break;
					
				default:
					println('Unknown argument ' + args[index]);
					unknownArg = true;
					index++;
					break;
			}
		}
		
		
		// Display help if missing arguments
		
		if (eclipseVersion == null || installDir == null || unknownArg || badInstallDir || badTempDir) {
			if (eclipseVersion == null) {
				println('Missing -eclipse argumens');
			}
			if (installDir == null) {
				println('Missing -installDir argumens');
			}
			if (badInstallDir) {
				println('The specified installation directory is not a clean install test directory'
					+ '\n   ' + installDir.canonicalPath
					+ '\nPlease manually delete the directory or specify a different directory');
			}
			if (badTempDir) {
				println('The temporary directory into which Eclipse will be unzipped already exists'
					+ '\n   ' + installDir.parentFile.canonicalPath + '/eclipse'
					+ '\nPlease manually delete the directory or specify a different installation parent directory')
			}
			println('');
			println('Arguments:');
			println('   -eclipse eclipseVersion');
			println('      the version of Eclipse to be installed ("3.5" or "3.4" or ...)');
			println('   -installDir path');
			println('      The directory into which eclipse will be installed');
			println('');
			println('Example:');
			println('   -eclipse 3.4 -installDir /Work/eclipse-341-clean');
			println('');
			throw new IllegalArgumentException();
		}
		
		installEclipse(eclipseVersion, installDir);
		println('Install Complete');
	}

	public void installEclipse(String eclipseVersion, File installDir) {
		prop.echoAll();
		
		// Unzip eclipse into temporary location
		ant.mkdir(dir: installDir.parentFile);
		ant.unzip(
			src: fileCache.download('eclipse-sdk', eclipseVersion), 
			dest: installDir.parentFile);
		
		// Rename eclipse directory
		if (!installDir.name.equals('eclipse')) {
			ant.delete(dir: installDir);
			ant.move(
				file: new File(installDir.parentFile, 'eclipse'),
				tofile: installDir);
		}
		
		// Zip the configuration and p2 directories
		ant.zip(destfile: new File(installDir, 'config-bak.zip')) {
			fileset(dir: installDir) {
				include(name: 'configuration/**');
				include(name: 'p2/**');
			}
		}
		
		// Create the clean install marker file
		new File(installDir, 'clean-install-marker.txt').withWriter { writer ->
			writer.println('This file serves as a marker so that the InstallEclipse class ' 
				+ '\nknows it can delete this directory and install a new Eclipse in this directory.');
		}
	}
}
