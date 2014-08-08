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
public class FileLocator {

	private String fileName;
	private int lineNumber;

	public FileLocator(String fileName, int lineNumber) {
		this.fileName = fileName;
		this.lineNumber = lineNumber;
	}
	
	public String getFileName() {
		return fileName;
	}

	public int getLineNumber() {
		return lineNumber;
	}

}
