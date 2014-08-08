/**
 * 
 */
package com.instantiations.build.update.links;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author markr
 *
 */
public class Product {
	private String name;
	private File location;
	private List<String> versions = new ArrayList<String>();
	
	Product () {
		super();
	}
	Product (File directory) {
		this();
		setLocation(directory);
		process();
	}
	
	void process() {
		
	}
	
	public File getlocation() {
		return this.location;
	}
	
	void setLocation(File locationIn) {
		this.location =  locationIn;
	}
	
	public List<String> getVersions() {
		return this.versions;
	}
	
	String getName() {
		return this.name;
	}
	
	void setName(String nameIn) {
		this.name = nameIn;
	}

}
