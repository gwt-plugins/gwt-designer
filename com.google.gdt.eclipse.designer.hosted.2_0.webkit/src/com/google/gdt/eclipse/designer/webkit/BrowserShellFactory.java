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
package com.google.gdt.eclipse.designer.webkit;

import com.google.gdt.eclipse.designer.hosted.IBrowserShell;
import com.google.gdt.eclipse.designer.hosted.IBrowserShellFactory;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.swt.SWT;


/**
 * Implementation for {@link IBrowserShellFactory} for WebKit-based renderer on every platform (if available).
 * 
 * @author mitin_aa
 */
public class BrowserShellFactory implements IBrowserShellFactory {
	public IBrowserShell create() throws Exception {
		boolean is64bit = SystemUtils.OS_ARCH.indexOf("64") != -1;
		if (SystemUtils.OS_NAME.startsWith("Windows")) {
      boolean isXP = SystemUtils.OS_NAME.indexOf("XP") != -1 || SystemUtils.OS_NAME.indexOf("2003") != -1;
			// forcibly use WebKit for 64-bit Windows and not use in Windows XP due to bug in cairo.
			boolean useWebKitWin = (useWebKit() | is64bit) & !isXP;
			if (!useWebKitWin) {
				// early exit to prevent possible long deployment operation
				return null;
			}
			WebKitSupportWin32.deployIfNeededAndLoad();
			if (WebKitSupportWin32.isAvailable()) {
				return is64bit
						? new BrowserShellWebKit<Long>(BrowserShellWebKitImplWin32.newImpl64())
						: new BrowserShellWebKit<Integer>(BrowserShellWebKitImplWin32.newImpl32());
			}
		} else if (SystemUtils.OS_NAME.startsWith("Linux")) {
			if (BrowserShellWebKitImplLinux.isAvailable() && useWebKit()) {
				return is64bit
						? new BrowserShellWebKit<Long>(BrowserShellWebKitImplLinux.newImpl64())
						: new BrowserShellWebKit<Integer>(BrowserShellWebKitImplLinux.newImpl32());
			}
		} else if (SystemUtils.OS_NAME.startsWith("Mac")) {
			if ("cocoa".equals(SWT.getPlatform())) {
				// cocoa
				return is64bit
						? new BrowserShellWebKit<Long>(BrowserShellWebKitImplMacCocoa.newImpl64())
						: new BrowserShellWebKit<Integer>(BrowserShellWebKitImplMacCocoa.newImpl32());
			} else {
				return new BrowserShellWebKit<Integer>(new BrowserShellWebKitImplMacCarbon());
			}
		}
		// not available/not supported.
		return null;
	}
	private boolean useWebKit() {
		return Boolean.parseBoolean(System.getProperty("__wbp.gwt.useWebKit"));
	}
}
