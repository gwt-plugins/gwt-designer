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
package com.google.gdt.eclipse.designer.hosted.tdt.log;

import java.io.File;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import com.google.gdt.eclipse.designer.hosted.ILogSupport;
import com.google.gdt.eclipse.designer.hosted.log.RotatingFileWriter;
import com.google.gdt.eclipse.designer.hosted.tdt.HostedModeSupport;

/**
 * Implementation of {@link ILogSupport} for GWT.
 * 
 * @author mitin_aa
 * @coverage gwtHosted
 */
public final class LogSupport implements ILogSupport {
  private static final String ERROR_TYPE_LABEL = "[ERROR]";
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");
  private final PrintWriter writer;
  private final Object logger;
  private String m_errors = "";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LogSupport(int type, Object hmsImpl, IJavaProject javaProject) throws Exception {
    if ("true".equalsIgnoreCase(System.getProperty(WBP_TESTING_TIME))) {
      this.logger =
          ReflectionUtils.invokeMethod(hmsImpl, "createLogger(java.io.PrintWriter,int)", null, type);
      this.writer = null;
    } else {
      // prepare directory to write log files to
      String logDir = HostedModeSupport.getTemporaryDirectoryName(javaProject);
      // prepare logger
      Writer rotatingWriter =
          RotatingFileWriter.getInstance(logDir + File.separator + ".gwt-log", 10, 3);
      this.writer = new PrintWriter(new FilterWriter(rotatingWriter) {
        private String buffer = new String();

        @Override
        public void write(String message, int off, int len) throws IOException {
          super.write(message, off, len);
          int eolIndex = message.indexOf(LINE_SEPARATOR);
          if (eolIndex != -1) {
            buffer += message.substring(0, eolIndex);
            if (buffer.indexOf(ERROR_TYPE_LABEL) != -1) {
              m_errors += buffer + LINE_SEPARATOR;
            }
            buffer = message.substring(eolIndex + 1);
          } else {
            buffer += message;
          }
        }
      });
      this.logger =
          ReflectionUtils.invokeMethod(
            hmsImpl,
            "createLogger(java.io.PrintWriter,int)",
            writer,
            type);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ILogSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link TreeLogger} instance.
   */
  public Object getLogger() {
    return logger;
  }

  public void setLogLevel(int logLevel) {
    // Not used, subject to remove.
  }

  public String getErrorMessages() {
    if (StringUtils.isEmpty(m_errors)) {
      return "<none>";
    }
    return m_errors;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dispose
  //
  ////////////////////////////////////////////////////////////////////////////
  public void dispose() {
    if (writer != null) {
      writer.close();
    }
  }
}
