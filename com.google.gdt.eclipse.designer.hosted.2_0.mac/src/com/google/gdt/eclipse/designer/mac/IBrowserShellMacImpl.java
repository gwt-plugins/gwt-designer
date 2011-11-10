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
package com.google.gdt.eclipse.designer.mac;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
/**
 * Abstract layer for MacOSX native WebKit window support.
 * 
 * @author mitin_aa
 */
public interface IBrowserShellMacImpl {
	////////////////////////////////////////////////////////////////////////////
	//
	// Visual data  methods
	//
	////////////////////////////////////////////////////////////////////////////
	long create(Object callback);
	void release(long handle);
	void setVisible(long handle, boolean visible);
	void setUrl(long handle, String url);
	void setBounds(long handle, Rectangle bounds);
	Rectangle getBounds(long handle);
	Rectangle computeTrim(long handle, Rectangle trim);
	Image createBrowserScreenshot(long handle) throws Exception;
	void showAsPreview(long handle);
}
