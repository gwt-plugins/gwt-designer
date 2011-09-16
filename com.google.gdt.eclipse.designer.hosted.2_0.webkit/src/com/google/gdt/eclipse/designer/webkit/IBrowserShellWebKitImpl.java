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

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Abstract layer for platform-dependent implementation of WebKit-driven BrowserShell.
 * 
 * @author mitin_aa
 */
public interface IBrowserShellWebKitImpl {
	void create(Object callback);
	void dispose();
	boolean isDisposed();
	void setUrl(String url);
	void prepare();
	void showAsPreview();
	Image makeShot() throws Exception;
	Rectangle computeTrim(int x, int y, int width, int height);
	void setBounds(int x, int y, int width, int height);
	Rectangle getBounds();
}
