package com.instantiations.subproducts;

import com.instantiations.eclipse.shared.installer.SubProduct;

/**
 * A generic SubProduct definition.
 * This is a Groovy template, so it has Groovy variables that are replaced at build time.
 */
public class ${product_name}SubProduct extends SubProduct
{
	public ${product_name}SubProduct() {
		super("${product_name}", "${product_version}", "${product_version}.${build_num}", "${product_id}");
	}
}
