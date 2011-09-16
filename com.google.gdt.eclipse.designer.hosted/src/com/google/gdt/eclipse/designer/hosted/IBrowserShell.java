/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gdt.eclipse.designer.hosted;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Common interface for platform-dependent browser shells.
 * 
 * @author mitin_aa
 * @coverage gwtHosted
 */
public interface IBrowserShell {
	/**
	 * Dispose browser shell. This will close shell window and dispose all resources associated.
	 */
	void dispose();
	/**
	 * @return true if shell is already disposed.
	 */
	boolean isDisposed();
	/**
	 * Loads an give url and waits for browser to done loading and rendering if possible. During waiting for
	 * given timeout it runs message loop which should be implemented as runnable.
	 * 
	 * @param url
	 *            the url to load.
	 * @param moduleName
	 *            the module name which should be loaded.
	 * @param timeout
	 *            the load waiting timeout in msec.
	 * @param messageProcessor
	 *            the {@link Runnable} implementing messages loop (display.readAndDispatch)
	 */
	void setUrl(String url, String moduleName, int timeout, Runnable messageProcessor) throws Exception;
	/**
	 * Set the visibility state of shell window
	 */
	void setVisible(boolean visible);
	/**
	 * Given a desired <em>client area</em> for the receiver (as described by the arguments), returns the
	 * bounding rectangle which would be required to produce that client area.
	 */
	Rectangle computeTrim(int x, int y, int width, int height);
	/**
	 * Set the desired bounds of the shell window.
	 */
	void setBounds(Rectangle bounds);
	/**
	 * Set the desired size of the shell window
	 */
	void setSize(int width, int height);
	/**
	 * Set the desired location of the shell window
	 */
	void setLocation(int x, int y);
	/**
	 * Run browser shell as preview test window (pressing "Test" button in Designer)
	 */
	void showAsPreview();
	/**
	 * Prepares browser shell to taking a screenshot, i.e., set location, visibility state and so on
	 */
	void prepare();
	/**
	 * Platform-dependent screenshot method.
	 */
	Image createBrowserScreenshot() throws Exception;
	/**
	 * @return the string representing currently used browser. Allowed values are: ie6, gecko, gecko1_8,
	 *         safari, opera.
	 */
	String getUserAgentString();
}
