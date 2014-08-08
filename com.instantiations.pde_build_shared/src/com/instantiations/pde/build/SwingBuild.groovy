package com.instantiations.pde.build;

import java.io.File;

import com.instantiations.pde.build.util.Version;
import java.util.Collection;

/**
 * Compile and assemble a Swing applications
 */
class SwingBuild extends AbstractBuild
{
	/**
	 * Called by build after reading properties, prebuild cleanup and sanity check
	 * to perform the actual build
	 */
	protected void buildImpl() {
		File projSrcDir = new File(sourceDir, prop.productId + '/src');
		File tempBinDir = new File(prop.productTemp, 'bin');
		File jarFile = productCache.localFile(null, 'swing/' + prop.productName + '.jar');
		
		compileSource(projSrcDir, tempBinDir);
		assembleJar(projSrcDir, tempBinDir, jarFile);
		ant.copy(file: jarFile, todir: prop.productOut);
	}
	
	/**
	 * Compile the source files
	 */
	protected void compileSource(File srcDir, File binDir) {
		
		// Build the classpath
		
		Collection<File> classpathFiles = new ArrayList<File>(10);
		for (String subproductName : prop.getList('product.swing.subproducts')) {
			File jarFile = productCache.downloadFile(subproductName, null, 'swing/' + subproductName + '.jar');
			classpathFiles.add(jarFile);
		}
		Collection<String> pluginIds = prop.getList('product.swing.plugins');
		if (pluginIds.size() > 0) {
			File eSdkZip = fileCache.download('eclipse-sdk', Version.V_3_5);
			unzip(eSdkZip, prop.productTemp) {
				patternset {
					for (String eachId : pluginIds) {
						include(name: 'eclipse/plugins/' + eachId + '_*.jar');
						include(name: 'eclipse/plugins/' + eachId + '_*/**');
					}
				}
			}
			for (File plugin : new File(prop.productTemp, 'eclipse/plugins').listFiles()) {
				if (plugin.isFile())
					classpathFiles.add(plugin);
				else {
					for (File jarFile : plugin.listFiles())
						if (jarFile.isFile() && jarFile.name.endsWith('.jar'))
							classpathFiles.add(jarFile);
				}
			}
		}
		
		// Compile the source
		
		ant.mkdir(dir: binDir);
		ant.javac(
			srcdir:		srcDir,
			destdir:	binDir,
			debug:		true,
			source:		'1.4',
			target:		'1.4',
			fork:		true,
			verbose:	true
		) {
			// generic JDK classpath... nothing special
			classpath {
				for (File jarFile : classpathFiles) {
					pathelement(location: jarFile);
				}
			}
		}
	}
	
	/**
	 * Assemble the specified jar file
	 */
	protected void assembleJar(File srcDir, File binDir, File jarFile) {
		ant.mkdir(dir: jarFile.parentFile);
		ant.jar(
			destfile:	jarFile,
			duplicate:	'fail'
		) {
			manifest {
				attribute(name: 'Main-Class', value: prop.productMainClass);
				attribute(name: 'Created-By', value: prop.productProvider);
			}
			fileset(dir: binDir);
			fileset(dir: srcDir) {
				exclude(name: '**/.svn');
				exclude(name: '**/*.class');
				exclude(name: '**/*.java');
				exclude(name: '**/Thumbs.db');
			}
		}
	}
}
