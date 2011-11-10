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
package com.google.gwt.dev.util;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.dev.util.msg.Message0;
import com.google.gwt.dev.util.msg.Message1String;

/**
 * Static messages.
 */
public class Messages {

  public static final Message0 HINT_CHECK_CLASSPATH_SOURCE_ENTRIES = new Message0(
      TreeLogger.ERROR,
      "Hint: Check that your classpath includes all required source roots");

  public static final Message0 HINT_CHECK_INHERIT_CORE = new Message0(
      TreeLogger.ERROR,
      "Hint: Check that your module inherits 'com.google.gwt.core.Core' either directly or indirectly (most often by inheriting module 'com.google.gwt.user.User')");
  public static final Message0 HINT_CHECK_INHERIT_USER = new Message0(
      TreeLogger.ERROR,
      "Hint: Check that your module inherits 'com.google.gwt.user.User' either directly or indirectly");
  public static final Message0 HINT_CHECK_MODULE_INHERITANCE = new Message0(
      TreeLogger.ERROR,
      "Hint: Check the inheritance chain from your module; it may not be inheriting a required module or a module may not be adding its source path entries properly");
  public static final Message0 HINT_CHECK_MODULE_NONCLIENT_SOURCE_DECL = new Message0(
      TreeLogger.ERROR,
      "Hint: Your source appears not to live underneath a subpackage called 'client'; no problem, but you'll need to use the <source> directive in your module to make it accessible");
  public static final Message1String HINT_CHECK_TYPENAME = new Message1String(
      TreeLogger.ERROR,
      "Hint: Check that the type name '$0' is really what you meant");
  public static final Message0 HINT_PRIOR_COMPILER_ERRORS = new Message0(
      TreeLogger.ERROR,
      "Hint: Previous compiler errors may have made this type unavailable");

  private Messages() {
  }
}
