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

import org.eclipse.wb.internal.core.DesignerPlugin;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Simple localhost TCP server managing incoming connections.
 * 
 * @author mitin_aa
 */
public class TcpServer extends Thread {
  private static final String ADDRESS = "127.0.0.1";
  protected final ThreadGroup m_threadGroup;
  protected int m_threadCounter;
  private boolean m_started;
  private int m_port;
  private boolean m_quit;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TcpServer(String name) {
    super();
    m_threadGroup = new ThreadGroup(name + " group");
    start();
  }

  public String getTCPAddress() {
    return ADDRESS + ":" + m_port;
  }

  @Override
  public void run() {
    try {
      try {
        // create the server socket
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(ADDRESS, 0));
        serverSocket.setSoTimeout(5000);
        // notify waiting start() method about thread start
        synchronized (this) {
          m_started = true;
          notify();
        }
        // fetch local port on which server socket bounded to
        m_port = serverSocket.getLocalPort();
        try {
          while (isWorking()) {
            try {
              Socket clientSocket = serverSocket.accept();
              // after possibly long waiting
              if (!isWorking()) {
                clientSocket.close();
                break;
              }
              // handle client
              clientConnected(clientSocket);
            } catch (java.net.SocketTimeoutException e) {
            }
          }
        } finally {
          serverSocket.close();
          serverSocket = null;
        }
      } catch (Throwable e) {
        DesignerPlugin.log(e);
      }
    } finally {
      // interrupt all threads
      m_threadGroup.interrupt();
    }
  }

  private boolean isWorking() {
    return !m_quit || !isInterrupted();
  }

  @Override
  public synchronized void start() {
    super.start();
    synchronized (this) {
      // thread may be already started, so just wait may cause caller thread to block
      if (!m_started) {
        try {
          wait();
        } catch (InterruptedException e) {
        }
      }
    }
  }

  public synchronized void shutdown() {
    m_quit = true;
    interrupt();
    try {
      join();
    } catch (InterruptedException e) {
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handle incoming connections
  //
  ////////////////////////////////////////////////////////////////////////////
  protected void clientConnected(Socket socket) {
  }
}