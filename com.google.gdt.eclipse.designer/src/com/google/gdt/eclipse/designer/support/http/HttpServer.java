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

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.EnvironmentUtils;

import java.util.Map;

/**
 * Simple HTTP server.
 * 
 * @author mitin_aa
 * @coverage gwt.http
 */
public class HttpServer extends TcpServer {
  private final Map<String, IResourceProvider> m_providers = Maps.newTreeMap();
  private static HttpServer m_instance;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static synchronized HttpServer getInstance() {
    if (m_instance == null) {
      m_instance = new HttpServer();
    }
    return m_instance;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private HttpServer() {
    super("http-server");
    if (EnvironmentUtils.IS_LINUX) {
      System.out.println("GWT http-server started at " + getTCPAddress());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Request handling
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void clientConnected(java.net.Socket socket) {
    new HttpClient(m_threadGroup, "http-server thread-" + m_threadCounter++, this, socket).start();
  }

  boolean handleRequest(HttpRequest request, HttpResponse response) throws Exception {
    String url = request.getUrl();
    IResourceProvider provider = getProvider(url);
    if (provider == null) {
      return false;
    }
    //
    byte[] resource = provider.getResource(url);
    if (resource == null) {
      return false;
    }
    //
    response.setHeader("content-type", MimeTypes.getMimeType(url));
    response.setHeader("content-length", Integer.toString(resource.length));
    response.print(resource);
    return true;
  }

  private IResourceProvider getProvider(String url) {
    synchronized (m_providers) {
      for (Map.Entry<String, IResourceProvider> entry : m_providers.entrySet()) {
        String prefix = entry.getKey();
        if (url.startsWith(prefix)) {
          IResourceProvider provider = entry.getValue();
          return provider;
        }
      }
    }
    return null;
  }

  public void addResourceProvider(String prefix, IResourceProvider handler) {
    synchronized (m_providers) {
      m_providers.put(prefix, handler);
    }
  }

  public synchronized void removeResourceProvider(String prefix) {
    synchronized (m_providers) {
      m_providers.remove(prefix);
    }
  }
}