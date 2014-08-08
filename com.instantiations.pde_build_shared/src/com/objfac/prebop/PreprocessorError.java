/*
 * Copyright (c) 2004 Object Factory Inc. All rights reserved.
 * This file is made available under the Common Public License (CPL) 1.0
 * (see http://www.opensource.org/licenses/cpl.php).
 * Every copy, modified or not, must retain the above copyright
 * and license notices.
 */
package com.objfac.prebop;

/**
 * @author Bob Foster
 */
public class PreprocessorError extends Exception {

	private FileLocator locator;
	
	/**
	 * @param message
	 */
	public PreprocessorError(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public PreprocessorError(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public PreprocessorError(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
	
	public PreprocessorError(String message, FileLocator locator) {
		super(message);
		this.locator = locator;
	}

	public FileLocator getLocator() {
		return locator;
	}

	public void setLocator(FileLocator locator) {
		this.locator = locator;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(super.toString());
		FileLocator locator = getLocator();
		if (locator != null) {
			sb.append(System.getProperty("line.separator"));
			sb.append(String.format("in file %s%nat line %d", 
					locator.getFileName(), 
					locator.getLineNumber()));
		}
		return sb.toString();
	}

}
