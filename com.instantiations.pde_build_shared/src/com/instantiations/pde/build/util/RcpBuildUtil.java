package com.instantiations.pde.build.util;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;

public class RcpBuildUtil {

	private static final String FEATURES = "features";
	private static final String PLUGINS = "plugins";
	private static final String UNDER_FEATURE = "_feature";

	/**
	 * find the product file for this RCP
	 * @param prop properties for this build
	 * @param basePath the path to base the search from
	 * @param version the version of Eclipse this product file is for
	 * @return product file for the RCP build
	 */
	public static File locateProductFile(BuildProperties prop, File basePath, Version version) {
		File productFile = productFile(prop, basePath, true);
		File productFileAlt = alternateProductFile(prop, basePath, version, true);
		if (!productFile.exists() && !productFileAlt.exists()) {
			String message = String.format("Could not find product file.%n  product.rcp.file = %s%nTried the following Files:%n  %s%n  %s%n",
											prop.get("product.rcp.file"),
											productFile,
											productFileAlt);
			throw new BuildException(message);
		}
		if (productFile.exists()) {
			return productFile;
		}
		else {
			return productFileAlt;
		}
	}
	
	/**
	 * answer the name of the product file for this RCP build
	 * @param prop properties for this build
	 * @param basePath the path to base the search from
	 * @param source flag <code>TRUE</code> if a source directory is being searched 
	 * <code>FALSE</code> if a build directory is being searched
	 * @return the product file for the RCP build
	 */
	public static File productFile(BuildProperties prop, File basePath, Boolean source) {
		String fileName = null;
		String path = getPath(prop.get("product.rcp.file"), source);
		File productFile;
		try {
			fileName = path + ".product";
			productFile = new File(basePath, fileName).getCanonicalFile();
		} catch (IOException e) {
			productFile = new File(basePath, fileName).getAbsoluteFile();
		}
		return productFile;
	}

	/**
	 * answer the alternate product file for the build
	 * @param prop properties for this build
	 * @param basePath the path to base the search from
	 * @param version the version of Eclipse this product file is for
	 * @param source flag <code>TRUE</code> if a source directory is being searched 
	 * <code>FALSE</code> if a build directory is being searched
	 * @return the alternate product File for the build
	 */
	public static File alternateProductFile(BuildProperties prop, File basePath, Version version, Boolean source) {
		String fileName = null;
		String path = getPath(prop.get("product.rcp.file"), source);
		File productFileAlt;
		try {
			fileName = String.format("%s-%s.product", path, version.toString()); 
			productFileAlt = new File(basePath, fileName).getCanonicalFile();
		} catch (IOException e) {
			productFileAlt = new File(basePath, fileName).getAbsoluteFile();
		}
		return productFileAlt;
	}

	/**
	 * Answer the path converted my removing plugins/ or features/.  This method will convert the path to the
	 * product file for the case when it is located in the source directories. 
	 * @param pathIn the current path
	 * @param source flag <code>TRUE</code> if a source directory is being searched 
	 * <code>FALSE</code> if a build directory is being searched
	 * @return the correct path
	 */
	private static String getPath(String pathIn, Boolean source) {
		String path = pathIn;
		Boolean feature = false;
		Integer index = 0;
		index = path.indexOf(FEATURES);
		if (index < 0) {
			index = path.indexOf(PLUGINS);
			index += PLUGINS.length() + 1;
		}
		else {
			index += FEATURES.length() + 1;
			feature = true;
		}
		if (source) {
			if (index > 0) {
				path = path.substring(index);
			}
		}
		else if (feature) {
			Integer pos = path.lastIndexOf(UNDER_FEATURE);
			if (pos >= 0) {
				path = path.substring(0, pos) + path.substring(pos + UNDER_FEATURE.length());
			}
		}
		return path;
	}

}
