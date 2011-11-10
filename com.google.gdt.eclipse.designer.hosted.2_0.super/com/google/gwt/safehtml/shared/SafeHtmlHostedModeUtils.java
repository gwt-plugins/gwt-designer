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
package com.google.gwt.safehtml.shared;

import com.google.gwt.core.client.GwtScriptOnly;

// This is the super-source peer of this class.
@GwtScriptOnly
public class SafeHtmlHostedModeUtils {

  // Unused in super-source; only defined to avoid compiler warnings
  public static final String FORCE_CHECK_COMPLETE_HTML = null;
  
  public static void maybeCheckCompleteHtml(String html) {
    // This check is a noop in web mode.
  }
  
  // Unused in super-source; only defined to avoid compiler warnings
  public static void setForceCheckCompleteHtml(boolean check) { }
  static void setForceCheckCompleteHtmlFromProperty() { }
}
