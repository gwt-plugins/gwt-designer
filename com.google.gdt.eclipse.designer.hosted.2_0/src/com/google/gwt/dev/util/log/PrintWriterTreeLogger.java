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
package com.google.gwt.dev.util.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

/**
 * Tree logger that logs to a print writer.
 */
public final class PrintWriterTreeLogger extends AbstractTreeLogger {

  private final String indent;

  private final PrintWriter out;
  
  private final Object mutex = new Object();

  public PrintWriterTreeLogger() {
    this(new PrintWriter(System.out, true));
  }

  public PrintWriterTreeLogger(PrintWriter out) {
    this(out, "");
  }
  
  public PrintWriterTreeLogger(File logFile) throws IOException {
    boolean existing = logFile.exists();
    this.out = new PrintWriter(new FileWriter(logFile, true), true);
    this.indent = "";
    if (existing) {
      out.println();  // blank line to mark relaunch
    }
  }

  protected PrintWriterTreeLogger(PrintWriter out, String indent) {
    this.out = out;
    this.indent = indent;
  }

  @Override
  protected AbstractTreeLogger doBranch() {
    return new PrintWriterTreeLogger(out, indent + "   ");
  }

  @Override
  protected void doCommitBranch(AbstractTreeLogger childBeingCommitted,
      Type type, String msg, Throwable caught, HelpInfo helpInfo) {
    doLog(childBeingCommitted.getBranchedIndex(), type, msg, caught, helpInfo);
  }

  @Override
  protected void doLog(int indexOfLogEntryWithinParentLogger, Type type,
      String msg, Throwable caught, HelpInfo helpInfo) {
    synchronized (mutex) { // ensure thread interleaving...
      out.print(indent);
      if (type.needsAttention()) {
        out.print("[");
        out.print(type.getLabel());
        out.print("] ");
      }

      out.println(msg);
      if (helpInfo != null) {
        URL url = helpInfo.getURL();
        if (url != null) {
          out.print(indent);
          out.println("For additional info see: " + url.toString());
        }
      }
      if (caught != null) {
        caught.printStackTrace(out);
      }
    }
  }
}
