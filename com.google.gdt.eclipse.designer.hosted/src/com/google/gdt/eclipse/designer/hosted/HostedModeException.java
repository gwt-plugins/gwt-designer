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

/**
 * Exception thrown when something goes wrong while GWT start up.
 * 
 * @author mitin_aa
 */
public class HostedModeException extends RuntimeException {
  // Error codes constants
  // Common codes
  /**
   * @deprecated
   */
  @Deprecated
  public static final int MODULE_LOADING_ERROR = 4102;
  public static final int GWT_INIT_TIMEOUT = 4103;
  public static final int UNSUPPORTED_OS = 4104;
  public static final int NATIVE_LIBS_LOADING_ERROR = 4107;
  public static final int MODULE_LOADING_ERROR2 = 4108;
  public static final int NO_DEV_LIB = 4109;
  // Linux specific
  public static final int LINUX_WRONG_MOZILLA_VER = 4110;
  public static final int LINUX_BROWSER_ERROR = 4111;
  public static final int LINUX_HOSTED_MODE_INIT_ERROR = 4112;
  public static final int LINUX_GENERAL_INIT_ERROR = 4113;
  // Mac OS X specific
  public static final int OSX_BROWSER_ERROR = 4120;
  public static final int OSX_UNKNOWN_BROWSER_ERROR = 4121;
  public static final int OSX_BROWSER_INIT_ERROR = 4122;
  // Windows specific
  public static final int WIN32_NO_WINDOWS_64 = 4130;
  // fields
  private final int m_code;
  private final String[] m_parameters;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public HostedModeException(int code) {
    this(code, (Throwable) null);
  }

  public HostedModeException(int code, Throwable e) {
    this(code, e, new String[0]);
  }

  public HostedModeException(int code, String[] parameters) {
    super();
    m_code = code;
    m_parameters = parameters;
  }

  public HostedModeException(int code, Throwable e, String[] parameters) {
    super(e);
    m_code = code;
    m_parameters = parameters;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public int getCode() {
    return m_code;
  }

  public String[] getParameters() {
    return m_parameters;
  }
}
