package com.instantiations.pde.build.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.instantiations.pde.build.util.BuildProperties;


/**
 * The class <code>BuildPropertiesFactory</code> implements static methods that return
 * instances of the class <code>{@link BuildProperties}</code>.
 * 
 * @generatedBy CodePro at 12/9/08 2:37 PM
 * @author danrubel
 * @version $Revision: 1.0 $
 */
public class BuildPropertiesFactory
{
	/**
	 * Prevent creation of instances of this class.
	 * 
	 * @generatedBy CodePro at 12/9/08 2:37 PM
	 */
	private BuildPropertiesFactory() {
	}

	/**
	 * Create an instance of the class <code>{@link BuildProperties}</code>.
	 * 
	 * @generatedBy CodePro at 12/9/08 2:37 PM
	 */
	public static BuildProperties createBuildProperties() throws IOException {
		BuildProperties prop = new BuildProperties();
		String filename = BuildPropertiesFactory.class.getName().replace('.', '/') + "-test.properties";  
			// "com/instantiations/build/BuildProptiesFactory-test.properties";
		InputStream stream = BuildProperties.class.getClassLoader().getResourceAsStream(filename);
		if (stream == null)
			throw new FileNotFoundException("Failed to find " + filename);
		prop.readStream(stream);
		return prop;
	}
}