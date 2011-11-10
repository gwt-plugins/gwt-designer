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
import com.google.gdt.eclipse.designer.model.widgets.ImageBundlePrototypeDescription;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.ObjectUtils;

/**
 * Implementation of {@link EntryInfo} that is contributed to palette for each
 * <code>AbstractImagePrototype</code> method in <code>ImageBundle</code>, so that it can be used to
 * drop new <code>Image</code> widget.
 * 
 * @author scheglov_ke
 * @coverage gwt.editor.palette
 */
public final class ImageBundleUseEntryInfo extends ToolEntryInfo {
  private static final Image ICON = Activator.getImage("info/ImageBundle/Image.gif");
  private final ImageBundlePrototypeDescription m_prototype;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ImageBundleUseEntryInfo(ImageBundlePrototypeDescription prototype) {
    m_prototype = prototype;
    setId(ObjectUtils.identityToString(m_prototype));
    setName(m_prototype.getMethod().getName());
    {
      String bundleClassName =
          m_prototype.getBundle().getDescription().getComponentClass().getName();
      setDescription("Allows to create and drop new instance of Image widget for method "
          + CodeUtils.getShortClass(bundleClassName)
          + "."
          + m_prototype.getMethod().getName()
          + "()");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EntryInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Image getIcon() {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<Image>() {
      public Image runObject() throws Exception {
        return m_prototype.getIcon();
      }
    }, ICON);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ToolEntryInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Tool createTool() throws Exception {
    ICreationFactory factory = new ICreationFactory() {
      private JavaInfo m_javaInfo;

      public void activate() throws Exception {
        m_javaInfo = m_prototype.createImageWidget();
        m_javaInfo.putArbitraryValue(JavaInfo.FLAG_MANUAL_COMPONENT, Boolean.TRUE);
      }

      public Object getNewObject() {
        return m_javaInfo;
      }
    };
    // return tool
    return new CreationTool(factory);
  }
}
