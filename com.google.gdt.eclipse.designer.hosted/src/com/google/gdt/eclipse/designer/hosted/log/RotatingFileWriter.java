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
package com.google.gdt.eclipse.designer.hosted.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * {@link Writer} writes into some {@link File} and performs rotation based on file size.
 * 
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage gwtHosted
 */
public final class RotatingFileWriter extends Writer {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance access
  //
  ////////////////////////////////////////////////////////////////////////////
  private static Map/*<String,RotatingFileWriter>*/m_writers = new HashMap();

  public static Writer getInstance(String fileName, int maxSize, int maxBackupCount)
      throws Exception {
    RotatingFileWriter writer = (RotatingFileWriter) m_writers.get(fileName);
    if (writer == null) {
      writer = new RotatingFileWriter(fileName, maxSize, maxBackupCount);
      m_writers.put(fileName, writer);
    }
    writer.m_refCount++;
    return writer;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final String m_fileName;
  private final int m_maxSize;
  private final int m_maxCount;
  private Writer m_output;
  private long m_currentLogLength;
  private long m_refCount;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private RotatingFileWriter(String fileName, int maxSize, int maxBackupCount) throws Exception {
    m_fileName = fileName;
    m_maxSize = maxSize;
    m_maxCount = maxBackupCount;
    // prepare file to write
    {
      File outFile = new File(fileName);
      m_output = new BufferedWriter(new FileWriter(outFile, true));
      m_currentLogLength = outFile.length();
    }
    // schedule periodical flush
    {
      final Timer timer = new Timer(true);
      timer.scheduleAtFixedRate(new TimerTask() {
        @Override
        public void run() {
          // check if closed
          if (m_output == null) {
            timer.cancel();
            return;
          }
          // flush safely
          try {
            m_output.flush();
          } catch (Throwable e) {
          }
        }
      }, 1000, 100);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Writer
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public synchronized void write(char[] cbuf, int off, int len) throws IOException {
    m_output.write(cbuf, off, len);
    m_currentLogLength += len;
    if (m_currentLogLength >= m_maxSize * 1024 * 1024) {
      rotate();
      m_currentLogLength = 0;
    }
  }

  @Override
  public synchronized void close() throws IOException {
    m_refCount--;
    if (m_refCount == 0) {
      m_output.close();
      m_output = null;
      m_writers.remove(m_fileName);
    }
  }

  @Override
  public synchronized void flush() throws IOException {
    m_output.flush();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rotation
  //
  ////////////////////////////////////////////////////////////////////////////
  private void rotate() throws IOException {
    // rotate name_0, name_1, etc files
    for (int i = m_maxCount - 1; i >= 0; i--) {
      File fileFrom = new File(m_fileName + "_" + (i - 1));
      File fileTo = new File(m_fileName + "_" + i);
      if (fileFrom.exists()) {
        if (fileTo.exists()) {
          fileTo.delete();
        }
        fileFrom.renameTo(fileTo);
      }
    }
    // rotate "name" (without any suffix) into name_0
    {
      m_output.close();
      File file = new File(m_fileName);
      File fileTo = new File(m_fileName + "_0");
      file.renameTo(fileTo);
      m_output = new BufferedWriter(new FileWriter(m_fileName));
    }
  }
}
