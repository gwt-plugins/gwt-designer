/**
 * 
 */
package com.instantiations.pde.build

import org.apache.tools.ant.BuildExceptionimport java.util.Map/**
 * @author markr
 *
 */
public abstract class DocBuild extends AbstractBuild {
	private static final String DOC_PRIMARY_ID = 'doc.primary.id'
	EclipseDocAntBuild builder;
	
	/* (non-Javadoc)
	 * @see com.instantiations.pde.build.AbstractBuild#AbstractBuild()
	 */
	public DocBuild(){
		
	}
	
	public void buildImpl() {
		// add two common properties the the global propety store
		prop.putAt('doc.template.file', 'template_default.html');
		prop.putAt('doc.alt.template.file', 'template_empty.html');

		builder = createBuilder();
		builder.initTemp();
		//Copy the sources to the runtime workspace
		timed('Copy Source'){
			builder.copySource(new File('.').canonicalFile.parentFile);
			postCopy(builder);
		}
		// unzip the runtime
		timed('Unzip Runtime') {
			builder.unzipRuntime();
			postRuntime(builder);
		}
		// create the ant script to run the documentation generation
		timed('Create Script') {
			builder.createScript(getPrimaryPluginName());
		}
		//run the documentation generation
		timed('Run Document Generation') {
			builder.launch();
			builder.collectLogs()
		}
	}
	/**
	 * create a new instance of the doc builder
	 * subclasses can override this method to use a different DocBuilder
	 */
	protected EclipseDocAntBuild createBuilder() {
		// create the builder to generate the documentation
		return new EclipseDocAntBuild(prop: prop,
										 fileCache: fileCache,
										 productCache: productCache,
										 ant: ant,
										 projects: documentationProjects());

	} 
	/**
	 * answer the plugin name that will be used as the base for the documentation 
	 * generation
	 */
	protected String getPrimaryPluginName() {
		String primaryPluginName = prop.productId;
		if (prop.isDefined(DOC_PRIMARY_ID)) {
			primaryPluginName = prop.get(DOC_PRIMARY_ID);
		}
		if (primaryPluginName == null) {
			throw new BuildException('could not find product.id ');
		}
		return primaryPluginName;
	}
	
	/**
	 * a multi level map that tells the documentation generation which 
	 *  project, optional fileset(s) and optional includes/excludes 
	 */
	abstract protected Map documentationProjects();
	
	/**
	 * Actions to do after the copy happens.  Subclasses should override 
	 * this method if they want to do any processing after the copy happens.
	 */
	protected void postCopy(EclipseDocAntBuild builder) {
		return;
	}
	 
	/**
	 * Actions to do after the install of the runtime happens.  Subclasses should override 
	 * this method if they want to do any processing after the copy happens.  This is 
	 * normally done if the documentation generation would need extra plugins installed 
	 */
	protected void postRuntime(EclipseDocAntBuild builder) {
		return;
	}
}
