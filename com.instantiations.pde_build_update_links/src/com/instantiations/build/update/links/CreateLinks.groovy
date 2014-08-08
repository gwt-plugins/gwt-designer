/**
 * 
 */
package com.instantiations.build.update.links

import java.util.Arrays
import groovy.lang.Closureimport java.lang.IllegalArgumentExceptionimport java.lang.StringBufferimport java.lang.System
/**
 * @author markr
 *
 */
public class CreateLinks{

	List products = new ArrayList()
	File base = new File('/opt/download/out')
	String target = 'integration'
	
	public static void main(String[] args) {
		CreateLinks create = new CreateLinks();
		create.processArgs(args)
		create.collectProducts()
		create.createLinks()
		create.cleanup()
		println create.toString()
	}
	
	CreateLinks () {
		super()
	}
	
	void processArgs(String[] argsIn) {
		List args = Arrays.asList(argsIn)
		Boolean hasArg = false
		Closure closure = null

		for (String arg in args) {
			if (arg.startsWith('--')) {
				String key = arg.substring(2)
				switch (key) {
					case 'base':
						println "processing argument $key"
						closure = { setBase(it) }
						hasArg = true;
						break;
				
					case 'target':
						println "processing argument $key"
						closure = { setTarget(it) }
						hasArg = true;
						break;
					
					default:
						println("argument $arg is unknown, exiting")
						System.exit(1);
						break;
				}
			}
			else {
				if (hasArg) {
					closure.call(arg)
					hasArg = false
				}
				else {
					println "unknown value $arg"
				}
			}
		}
	}
	
	void collectProducts() {
		base.eachDir { File dir ->
			println "processing directory $dir"
			File updateDir = new File(dir, "$target/latest/update")
			if (updateDir.exists()) {
				println("found update directory $updateDir")
				updateDir.eachDir { File versionDir ->
					if (versionDir.getName().startsWith('E-')) {
						println "found version directory $versionDir"
					}
				}
			}
		}
		
	}
	
	void createLinks() {
		
	}
	
	void cleanup() {
		
	}
	
	void setBase(String baseIn) {
		this.base = new File(baseIn)
		if (!this.base.exists()) {
			throw new IllegalArgumentException("base($baseIn) must exist")
		}
		else if (!this.base.isDirectory()) {
			throw new IllegalArgumentException("base ($baseIn) must be a directory")
		}
		else if (!this.base.canRead()) {
			throw new IllegalArgumentException("base ($baseIn) must be readable")
		}
	}
	
	void settarget(String targetIn) {
		this.target = targetIn
	}
	String toString() {
		StringBuffer buf = new StringBuffer(80)
		buf.append("base: $base versions [")
		for(Product product in products) {
			buf.append(product.toString());
		}
		buf.append(']')
	}
}
