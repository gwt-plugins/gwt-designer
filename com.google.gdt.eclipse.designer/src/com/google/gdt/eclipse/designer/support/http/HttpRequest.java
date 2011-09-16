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
package com.google.gdt.eclipse.designer.support.http;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * Class implementing very basics of HTTP requests
 * 
 * @author mitin_aa
 * @coverage gwt.http
 */
public class HttpRequest implements IHttpConstants {
  private String m_url;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public HttpRequest(InputStream inputStream) throws Exception {
    BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream));
    // first read and parse the first line, with waiting for data
    String line = inputReader.readLine();
    int result = parseRequest(line);
    if (result != HTTP_OK) {
      throw new IllegalArgumentException("" + result);
    }
  }

  /**
   * Parses request from browser.
   * 
   * @param requestLine
   */
  private int parseRequest(String requestLine) {
    // parse the line into components, check if there are enough components
    String[] items = StringUtils.split(requestLine, " ");
    if (items.length < 2) {
      return HTTP_BAD_REQUEST;
    }
    if (!items[0].toUpperCase(Locale.ENGLISH).equals("GET")) {
      return HTTP_BAD_METHOD;
    }
    m_url = items[1];
    return HTTP_OK;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getUrl() {
    return m_url;
  }
}
