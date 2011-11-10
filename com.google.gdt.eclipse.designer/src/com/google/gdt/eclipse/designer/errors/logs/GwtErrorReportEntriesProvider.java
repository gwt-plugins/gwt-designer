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
package com.google.gdt.eclipse.designer.errors.logs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.editor.errors.report2.FileListReportEntry;
import org.eclipse.wb.internal.core.editor.errors.report2.IErrorReport;
import org.eclipse.wb.internal.core.editor.errors.report2.IReportEntriesProvider;

import org.eclipse.core.resources.IProject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

/**
 * Provider for GWT log files.
 * 
 * @author mitin_aa
 * @coverage gwt.logs
 */
public class GwtErrorReportEntriesProvider implements IReportEntriesProvider {
  public void addEntries(final IErrorReport report) {
    FileListReportEntry reportEntry = new FileListReportEntry() {
      @Override
      protected String getPrefix() {
        return "gwt-logs/";
      }

      @Override
      protected List<File> getFiles() {
        IProject project = report.getProject();
        if (project == null) {
          return ImmutableList.of();
        }
        if (Utils.isGWTProject(project)) {
          String logDir = project.getLocation().toOSString() + File.separator + ".gwt";
          File logsPathAsFile = new File(logDir);
          if (!logsPathAsFile.exists()) {
            return ImmutableList.of();
          }
          File[] logFiles = logsPathAsFile.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
              return name.startsWith(".gwt-log");
            }
          });
          return Lists.newArrayList(logFiles);
        }
        return ImmutableList.of();
      }
    };
    report.addEntry(reportEntry);
  }
}
