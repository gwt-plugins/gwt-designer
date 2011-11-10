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

import java.net.Socket;

/**
 * Thread class for handle client's connection
 * 
 * @author mitin_aa
 * @coverage gwt.http
 */
public class HttpClient extends Thread implements IHttpConstants {
  private final Socket m_socket;
  private final HttpServer m_server;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param group
   *          A group for which all client handling threads will belong
   * @param name
   *          A thread name
   * @param server
   *          A http server object. All client's requests will be delegated to it.
   * @param socket
   *          A socket provided by accept() call of server socket
   */
  public HttpClient(ThreadGroup group, String name, HttpServer server, Socket socket) {
    super(group, name);
    m_server = server;
    m_socket = socket;
  }

  /**
   * main thread method for handling client requests
   */
  @Override
  public void run() {
    try {
      // setup socket timeout for to avoid hanging thread on blocked sockets
      m_socket.setSoTimeout(30000);
      // prepare responce
      HttpResponse response = new HttpResponse(m_socket.getOutputStream());
      {
        // create the request, will wait for data in ctor
        HttpRequest request = new HttpRequest(m_socket.getInputStream());
        // delegate request handling
        if (!m_server.handleRequest(request, response)) {
          // request cant be processed
          response.sendError(HTTP_NOT_FOUND);
        }
      }
      response.flush();
    } catch (Throwable e) {
      // We ignore exceptions (such as for example "Connection reset") because browser can close
      // connection in any time. In reality we know one case when this happens:
      // 1. we add Tree to fetch its properties;
      // 2. Tree contains Image's, so browser opens connection for images;
      // 3. we finish with properties and remove Tree;
      // 4. browser detects that images are not required and closes connection.
    } finally {
      // always close the socket
      try {
        m_socket.close();
      } catch (Throwable e) {
      }
    }
  }
}