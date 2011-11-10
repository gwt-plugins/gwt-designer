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
package com.google.gdt.eclipse.designer.uibinder.wizards;

import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.StructuredSelection;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests for {@link PopupPanelWizard}.
 * 
 * @author scheglov_ke
 */
public class PopupPanelWizardTest extends UiBinderWizardTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  @DisposeProjectAfter
  public void test_0() throws Exception {
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        TestUtils.runWizard(new PopupPanelWizard(), new StructuredSelection(m_packageFragment));
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("New UiBinder PopupPanel");
        context.getTextByLabel("Name:").setText("MyPopupPanel");
        context.clickButton("Finish");
      }
    });
    // Java
    {
      String content = getFileContentSrc("test/client/MyPopupPanel.java");
      assertThat(content).contains("public class MyPopupPanel extends PopupPanel {");
      assertThat(content).contains("setWidget(binder.createAndBindUi(this));");
    }
    // ui.xml
    {
      IFile file = getFileSrc("test/client/MyPopupPanel.ui.xml");
      String content = getFileContent(file);
      assertThat(content).contains("<ui:UiBinder");
      assertThat(content).contains("<g:FlowPanel");
      assertThat(content).contains("encoding=").contains("UTF-8");
      assertEquals("UTF-8", file.getCharset(false));
    }
  }
}