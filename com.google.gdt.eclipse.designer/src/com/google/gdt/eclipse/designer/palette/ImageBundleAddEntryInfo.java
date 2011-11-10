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
package com.google.gdt.eclipse.designer.palette;

import com.google.gdt.eclipse.designer.Activator;
import com.google.gdt.eclipse.designer.model.widgets.ImageBundleContainerInfo;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.SubtypesScope;
import org.eclipse.wb.internal.core.utils.jdt.ui.JdtUiUtils;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.swt.graphics.Image;

/**
 * {@link EntryInfo} that allows user select <code>ImageBundle</code> and add its instance to the
 * form.
 * 
 * @author scheglov_ke
 * @coverage gwt.editor.palette
 */
public final class ImageBundleAddEntryInfo extends EntryInfo {
  private static final Image ICON = Activator.getImage("info/ImageBundle/bundle_add.png");

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ImageBundleAddEntryInfo() {
    setName("Add...");
    setDescription("Adds ImageBundle instance to this form.");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EntryInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Image getIcon() {
    return ICON;
  }

  @Override
  public boolean activate(boolean reload) {
    ExecutionUtils.run(m_rootJavaInfo, new RunnableEx() {
      public void run() throws Exception {
        activateEx();
      }
    });
    return false;
  }

  /**
   * Implementation of {@link #activate(boolean)} that can throw exception.
   */
  private void activateEx() throws Exception {
    IType bundleInterface = m_javaProject.findType("com.google.gwt.user.client.ui.ImageBundle");
    IType bundleType =
        JdtUiUtils.selectType(
            DesignerPlugin.getShell(),
            new SubtypesScope(bundleInterface),
            IJavaElementSearchConstants.CONSIDER_INTERFACES);
    if (bundleType != null) {
      String bundleClassName = bundleType.getFullyQualifiedName();
      ImageBundleContainerInfo.add(m_rootJavaInfo, bundleClassName);
    }
  }
}
