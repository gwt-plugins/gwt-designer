package com.instantiations.pde.build.preprocessor

import com.instantiations.pde.build.util.BuildUtil;
import groovy.xml.MarkupBuilderimport com.instantiations.pde.build.util.OemVersion
/**
 * Build the site.xml file based upon the features and plugins being built
 */
public class SiteXmlBuild extends BuildUtil
{
	// Initialized by the caller
	OemVersion targetVersion;
	Collection<String> hiddenFeatures;
	
	// A map of feature identifier to category name/label
	// Populated by calls to addFeature
	Map<String, String> categories = new HashMap<String, String>();
	
	/**
	 * Set the category for a specified feature
	 * and include that feature in the site.xml file
	 */
	public void setCategory(String featureId, String categoryNameAndLabel) {
		String value = categoryNameAndLabel;
		if (value == null || value.trim().length() == 0)
			value = 'Unspecified';
		categories.put(featureId, value);
	}
	
	/**
	 * Generate the site.xml file
	 */
	public void generateSiteXml(File siteXmlFile) {
		
		// Collect the categories and features to be added
		
		Collection<String> categoriesToAdd = new TreeSet<String>();
		Collection<String> featuresToAdd = new TreeSet<String>();
		for (String featureId : new TreeSet(categories.keySet())) {
			if (!hiddenFeatures.contains(featureId)) {
				featuresToAdd.add(featureId);
				categoriesToAdd.add(categories.get(featureId));
			}
		}
		
		// Write the site.xml file
		
		siteXmlFile.withWriter { writer ->
			MarkupBuilder builder = new MarkupBuilder(writer);
			builder.doubleQuotes = true;
			builder.site {
				description '${product_description}'
				for (String categoryNameAndLabel : categoriesToAdd) {
					'category-def'(name: categoryNameAndLabel, label: categoryNameAndLabel) {
					}
				}
				for (String featureId : featuresToAdd) {
					feature(url: 'features/' + featureId + '_0.0.0.jar', id: featureId, version: '0.0.0') {
						category(name: categories.get(featureId));
					}
				}
			}
			
		}
	}
}
