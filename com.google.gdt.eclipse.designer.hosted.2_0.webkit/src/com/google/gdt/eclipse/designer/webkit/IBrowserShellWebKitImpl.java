/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
