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
package com.google.gdt.eclipse.designer.common;

import com.google.gdt.eclipse.designer.Activator;

import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

/**
 * GWT plugin constants.
 * 
 * @author scheglov_ke
 * @coverage gwt.common
 */
public interface Constants {
  String PLUGIN_ID = Activator.PLUGIN_ID;
  ////////////////////////////////////////////////////////////////////////////
  //
  // GWT Home
  //
  ////////////////////////////////////////////////////////////////////////////
  String GWT_DOWNLOAD_URL = "http://code.google.com/webtoolkit/download.html";
  String P_GWT_LOCATION = "P_GWT_LOCATION";
  String GWT_HOME_CPE = "GWT_HOME";
  ////////////////////////////////////////////////////////////////////////////
  //
  // GWT 1.6
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Specifies the name of "web" folder for GWT 1.6
   */
  String P_WEB_FOLDER = "P_GWT_WEB_FOLDER";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Troubleshooting
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Timeout for GWT hosted mode to initialize, sec. Default is 60 sec.
   */
  String P_GWT_HOSTED_INIT_TIME = "P_GWT_HOSTED_INIT_TIME";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Builder
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When <code>true</code>, builder will automatically generate "Async" parts of
   * <code>RemoteService</code> 's.
   */
  String P_BUILDER_GENERATE_ASYNC = "P_BUILDER_GENERATE_ASYNC";
  /**
   * When <code>true</code>, builder will check that <code>Composite</code> should have default
   * constructor.
   */
  String P_BUILDER_COMPOSITE_DEFAULT_CONSTRUCTOR = "P_BUILDER_COMPOSITE_DEFAULT_CONSTRUCTOR";
  /**
   * When <code>true</code>, builder will check that used classes belong to "client" classpath.
   */
  String P_BUILDER_CHECK_CLIENT_CLASSPATH = "P_BUILDER_CHECK_CLIENT_CLASSPATH";
  ////////////////////////////////////////////////////////////////////////////
  //
  // CSS Units
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Fetching CSS units may be very time-consuming on large GUI. This option allows to
   * enable/disable it.
   */
  String P_ENABLE_CSS_UNITS_CONVERSION = "P_ENABLE_CSS_UNITS_CONVERSION";
  ////////////////////////////////////////////////////////////////////////////
  //
  // CSS editing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Specifies if name of color should be used (if exists) or its hex value.
   */
  String P_CSS_USE_NAMED_COLORS = "P_CSS_USE_NAMED_COLORS";
  ////////////////////////////////////////////////////////////////////////////
  //
  // GWT classes
  //
  ////////////////////////////////////////////////////////////////////////////
  String CLASS_GWT = "com.google.gwt.core.client.GWT";
  String CLASS_CONSTANTS = "com.google.gwt.i18n.client.Constants";
  String CLASS_CONSTANTS_WITH_LOOKUP = "com.google.gwt.i18n.client.ConstantsWithLookup";
  String CLASS_MESSAGES = "com.google.gwt.i18n.client.Messages";
  String CLASS_IMAGE_BUNDLE = "com.google.gwt.user.client.ui.ImageBundle";
  String CLASS_ENTRY_POINT = "com.google.gwt.core.client.EntryPoint";
  String CLASS_REMOTE_SERVICE = "com.google.gwt.user.client.rpc.RemoteService";
  String CLASS_REMOTE_SERVICE_IMPL = "com.google.gwt.user.server.rpc.RemoteServiceServlet";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Project structure
  //
  ////////////////////////////////////////////////////////////////////////////
  String BUILDER_ID = "com.google.gdt.eclipse.designer.GWTBuilder";
  String NATURE_ID = "com.google.gdt.eclipse.designer.GWTNature";
  String GPE_NATURE_ID = "com.google.gwt.eclipse.core.gwtNature";
  String GWT_XML_EXT = ".gwt.xml";
  String PUBLIC_FOLDER = "public";
  String SERVER_PACKAGE = "server";
  String CLIENT_PACKAGE = "client";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Testing
  //
  ////////////////////////////////////////////////////////////////////////////
  String P_GWT_TESTS_SOURCE_FOLDER = "P_GWT_TESTS_SOURCE_FOLDER";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Launch
  //
  ////////////////////////////////////////////////////////////////////////////
  String LAUNCH_TYPE_ID_SHELL = "com.google.gdt.eclipse.designer.gwtLaunchConfigurationType";
  String LAUNCH_TYPE_ID_COMPILER =
      "com.google.gdt.eclipse.designer.gwtCompilerLaunchConfigurationType";
  String LAUNCH_TYPE_ID_JUNIT = "com.google.gdt.eclipse.designer.junitGwtLaunchConfigurationType";
  String GWT_DEV_MODE_CLASS = "com.google.gwt.dev.DevMode";
  String GWT_COMPILER_CLASS = "com.google.gwt.dev.Compiler";
  String LAUNCH_ATTR_PROJECT = IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME;
  String LAUNCH_ATTR_URL = PLUGIN_ID + ".launchAttrURL";
  String LAUNCH_ATTR_MODULE = PLUGIN_ID + ".launchAttrModule";
  String LAUNCH_ATTR_MODULE_HTML = PLUGIN_ID + ".launchAttrModuleHtml";
  String LAUNCH_ATTR_PARAMETERS = PLUGIN_ID + ".launchAttrParameters";
  String LAUNCH_ATTR_PORT = PLUGIN_ID + ".launchAttrPort";
  String LAUNCH_ATTR_NO_SERVER = PLUGIN_ID + ".launchAttrNoServer";
  String LAUNCH_ATTR_WHITE_LIST = PLUGIN_ID + ".launchAttrWhiteList";
  String LAUNCH_ATTR_BLACK_LIST = PLUGIN_ID + ".launchAttrBlackList";
  String LAUNCH_ATTR_LOG_LEVEL = PLUGIN_ID + ".launchAttrLogLevel";
  String LAUNCH_ATTR_DIR_GEN = PLUGIN_ID + ".launchAttrDirGen";
  String LAUNCH_ATTR_DIR_WAR = PLUGIN_ID + ".launchAttrDirOut";
  String LAUNCH_ATTR_STYLE = PLUGIN_ID + ".launchAttrStyle";
  ////////////////////////////////////////////////////////////////////////////
  //
  // WebKit
  //
  ////////////////////////////////////////////////////////////////////////////
  String P_GWT_USE_WEBKIT = "com.google.gdt.eclipse.designer.useWebKit";
}
