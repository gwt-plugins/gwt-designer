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
package com.google.gdt.eclipse.designer.uibinder.editor;

import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderContext;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.errors.report2.IErrorReport;
import org.eclipse.wb.internal.core.editor.errors.report2.IReportEntriesProvider;
import org.eclipse.wb.internal.core.editor.errors.report2.StringFileReportEntry;
import org.eclipse.wb.internal.core.xml.model.utils.GlobalStateXml;

import org.eclipse.jdt.core.IType;

import java.util.zip.ZipOutputStream;

/**
 * Adds UiBinder-related error report entries.
 * 
 * @author mitin_aa
 * @coverage GWT.UiBinder.editor
 */
public final class UiBinderReportEntriesProvider implements IReportEntriesProvider {
  public void addEntries(final IErrorReport report) {
    // Report entry allowing to include the corresponding java source file of the currently editing
    // UiBinder xml
    try {
      UiBinderContext context = (UiBinderContext) GlobalStateXml.getEditorContext();
      if (context == null) {
        // not applicable
        return;
      }
      IType formType = context.getFormType();
      String name = formType.getTypeQualifiedName() + ".java";
      String source = formType.getCompilationUnit().getSource();
      report.addEntry(new StringFileReportEntry(name, source) {
        @Override
        public void write(ZipOutputStream zipStream) throws Exception {
          if (report.hasSourceFile()) {
            // don't write java code if the user unchecked sending the editor contents
            super.write(zipStream);
          }
        }
      });
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }
}
