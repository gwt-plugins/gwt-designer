/*
 * Copyright (c) 2003 Object Factory Inc. All rights reserved.
 */
package com.objfac.prebop.ant;

/**
 * @author Bob Foster
 */
public class Filetype {

	private String outextension;
	private String outextensions;
	private String extensions;
	private String commentbegin;
	private String commentend;
	private String name;
	
	public Filetype() {
		super();
	}

	public String getCommentbegin() {
		return commentbegin;
	}

	public String getCommentend() {
		return commentend;
	}

	public String getExtensions() {
		return extensions;
	}

	public void setCommentbegin(String string) {
		commentbegin = string;
	}

	public void setCommentend(String string) {
		commentend = string;
	}

	public void setExtensions(String string) {
		extensions = string;
	}

	public String getName() {
		return name;
	}

	public void setName(String string) {
		name = string;
	}
	
	public void setOutextensions(String string) {
		outextensions = string;
	}
	
	public String getOutextensions() {
		return outextensions;
	}

	public String getOutextension() {
		return outextension;
	}

	public Filetype setOutextension(String string) {
		outextension = string;
		return this;
	}
	
	public Filetype copy() {
		Filetype copy = new Filetype();
		copy.setCommentbegin(commentbegin);
		copy.setCommentend(commentend);
		copy.setExtensions(extensions);
		copy.setName(name);
		copy.setOutextension(outextension);
		copy.setOutextensions(outextensions);
		return copy;
	}

}
