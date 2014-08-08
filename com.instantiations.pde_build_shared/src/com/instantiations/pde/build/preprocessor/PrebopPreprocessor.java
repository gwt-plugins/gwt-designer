package com.instantiations.pde.build.preprocessor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import com.instantiations.pde.build.util.Version;
import com.objfac.prebop.Preprocessor;
import com.objfac.prebop.PreprocessorError;
import com.objfac.prebop.ant.Filetype;

/**
 * Wrapper for the actual prebop preprocessor
 */
public class PrebopPreprocessor
{
	private static final HashSet<String> NO_EXCLUDES = new HashSet<String>();
	private static final HashMap<String, Filetype> TYPE_MAP = new HashMap<String, Filetype>();
	static {
		Filetype jtype = new Filetype();
		jtype.setName("Java");
		jtype.setExtensions("java");
		jtype.setOutextension("java");
		jtype.setCommentbegin("/*");
		jtype.setCommentend("*/");
		TYPE_MAP.put("java", jtype);
			
		Filetype xtype = new Filetype();
		xtype.setName("XML");
		xtype.setExtensions("xml");
		xtype.setOutextension("xml");
		xtype.setCommentbegin("<!--");
		xtype.setCommentend("-->");
		TYPE_MAP.put("xml", xtype);
		
		Filetype htmltype = new Filetype();
		htmltype.setName("HTML");
		htmltype.setExtensions("html");
		htmltype.setOutextension("html");
		htmltype.setCommentbegin("<!--");
		htmltype.setCommentend("-->");
		TYPE_MAP.put("html", htmltype);
		
		Filetype htmtype = new Filetype();
		htmtype.setName("HTM");
		htmtype.setExtensions("htm");
		htmtype.setOutextension("htm");
		htmtype.setCommentbegin("<!--");
		htmtype.setCommentend("-->");
		TYPE_MAP.put("htm", htmtype);
		
	}
	
	private final HashMap<String, String> vars;

	public PrebopPreprocessor(String oemName, Version eclipseTargetVersion) {
		this.vars = new HashMap<String, String>();
		this.vars.put("oem.name", oemName != null ? oemName : "none");
		this.vars.put("eclipse.version", eclipseTargetVersion.toString());
	}
	
	public void preprocess(File dir) throws PreprocessorError {
		new Preprocessor().preprocess(
			dir,			// in
			dir,			// out
			vars,			// expression variables
			NO_EXCLUDES,	// exclude
			TYPE_MAP,		// typeMap,
			false,			// replace 
			true			// merge
		);
	}
	
	public void preprocessFile(File file) throws PreprocessorError, IOException {
		new Preprocessor().preprocessFile(
			file,			// in
			file,			// out
			vars,			// expression variables
			NO_EXCLUDES,	// exclude
			TYPE_MAP,		// typeMap,
			false,			// replace 
			true			// merge
		);
	}
}
