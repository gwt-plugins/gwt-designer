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
package com.google.gdt.eclipse.designer.support.http;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author mitin_aa
 * @coverage gwt.http
 */
public class HttpResponse implements IHttpConstants {
  private final OutputStream m_outputStream;
  private final Map<String, String> m_headers = Maps.newTreeMap();
  private final List<IResponseData> m_sendQueue = Lists.newArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public HttpResponse(OutputStream output) {
    m_outputStream = output;
    //		m_headers.put("date", m_dateFormat.format(new Date()));
    //m_headers.put("server", "GWT Designer Internal HTTP Server");
    //		m_headers.put("connection", "close");
    //		m_headers.put("content-type", "text/html; charset=iso-8859-1");
    //m_headers.put("cache-control", "no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
    // commented, let a browser to cache files (images seems to be displayed in time, and css-files are fetched as local files, so we may use the cache) 		
    //	m_headers.put("cache-control", "no-store, no-cache, must-revalidate");
    //	m_headers.put("pragma", "no-cache");
    //	m_headers.put("expires", "0");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Headers
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setHeader(String key, String value) {
    m_headers.put(key.toLowerCase(), value);
  }

  public void removeHeader(String key) {
    m_headers.remove(key.toLowerCase());
  }

  public String getHeader(String key, String defaultValue) {
    key = key.toLowerCase();
    if (!m_headers.containsKey(key)) {
      return defaultValue;
    }
    return m_headers.get(key);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Output
  //
  ////////////////////////////////////////////////////////////////////////////
  public void print(byte[] bytes) {
    m_sendQueue.add(new ByteResponseData(bytes));
  }

  public void print(String data) {
    m_sendQueue.add(new StringResponseData(data));
  }

  public void println(String data) {
    m_sendQueue.add(new StringResponseData(data));
    m_sendQueue.add(new StringResponseData("\r\n"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Flush
  //
  ////////////////////////////////////////////////////////////////////////////
  public void flush() throws Exception {
    prepareHeaders();
    for (IResponseData element : m_sendQueue) {
      m_outputStream.write(element.getBytes());
    }
    m_outputStream.flush();
    m_headers.clear();
    m_sendQueue.clear();
  }

  private void prepareHeaders() {
    if (m_headers.isEmpty()) {
      return;
    }
    StringBuffer sb = new StringBuffer();
    sb.append("HTTP/1.1 200 OK");
    sb.append("\r\n");
    // send all headers
    for (Map.Entry<String, String> entry : m_headers.entrySet()) {
      sb.append(entry.getKey() + ": ");
      sb.append(entry.getValue());
      sb.append("\r\n");
    }
    sb.append("\r\n");
    m_sendQueue.add(0, new StringResponseData(sb.toString()));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Error
  //
  ////////////////////////////////////////////////////////////////////////////
  public void sendError(int code) throws Exception {
    String errorString;
    switch (code) {
      case HTTP_MOVED_PERMANENTLY :
        errorString = "301 Moved Permanently";
        break;
      case HTTP_BAD_REQUEST :
        errorString = "400 Bad Request";
        break;
      case HTTP_NOT_FOUND :
        errorString = "404 Not Found";
        break;
      case HTTP_BAD_METHOD :
        errorString = "405 Bad Method";
        break;
      case HTTP_LENGTH_REQUIRED :
        errorString = "405 Bad Method";
        break;
      default :
        errorString = "500 Internal Server Error";
        break;
    }
    print("<html><head><title>" + errorString + "</title></head>");
    print("<h1>" + errorString + "</h1>");
    print("</body></html>");
    flush();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IResponseElement
  //
  ////////////////////////////////////////////////////////////////////////////
  private static interface IResponseData {
    byte[] getBytes();
  }
  private final class StringResponseData implements IResponseData {
    private final String m_data;

    private StringResponseData(String data) {
      m_data = data;
    }

    public byte[] getBytes() {
      return m_data.getBytes();
    }
  }
  private final class ByteResponseData implements IResponseData {
    private final byte[] m_data;

    private ByteResponseData(byte[] bytes) {
      m_data = bytes;
    }

    public byte[] getBytes() {
      return m_data;
    }
  }
}