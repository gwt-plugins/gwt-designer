package com.instantiations.pde.build.util;

import java.util.Properties;

/**
 * An interface used by {@link BuildProperties} to load product.properties files for
 * products other than the product being built.
 */
public interface ProductPropertiesLoader
{
	/**
	 * Answer the product properties for the specified subproduct as loaded from that
	 * subproduct's product.properties file.
	 * 
	 * @param subproductName the name of the subproduct
	 * @return the properties (not <code>null</code>)
	 */
	Properties getProductProperties(String subproductName);
}
