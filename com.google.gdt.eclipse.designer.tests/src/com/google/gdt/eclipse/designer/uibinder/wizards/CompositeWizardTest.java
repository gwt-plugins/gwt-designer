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

import com.google.gdt.eclipse.designer.core.GTestUtils;

import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Button;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests for {@link CompositeWizard}.
 * 
 * @author scheglov_ke
 */
public class CompositeWizardTest extends UiBinderWizardTest {
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
  /**
   * We should check that GWT includes patches for UiBinder support in GWT Designer.
   */
  @DisposeProjectAfter
  public void test_wrongVersion() throws Exception {
    configureForGWT_version(GTestUtils.getLocation_20());
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        TestUtils.runWizard(new CompositeWizard(), new StructuredSelection(m_packageFragment));
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("New UiBinder Composite");
        // "Finish" button is not enabled
        {
          Button finishButton = context.getButtonByText("Finish");
          assertFalse(finishButton.isEnabled());
        }
        // cancel
        context.clickButton("Cancel");
      }
    });
  }

  @DisposeProjectAfter
  public void test_0() throws Exception {
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        TestUtils.runWizard(new CompositeWizard(), new StructuredSelection(m_packageFragment));
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("New UiBinder Composite");
        context.getTextByLabel("Name:").setText("MyComposite");
        context.clickButton("Finish");
      }
    });
    // Java
    {
      String content = getFileContentSrc("test/client/MyComposite.java");
      assertThat(content).contains("public class MyComposite extends Composite {");
      assertThat(content).contains("initWidget(binder.createAndBindUi(this));");
    }
    // ui.xml
    {
      IFile file = getFileSrc("test/client/MyComposite.ui.xml");
      String content = getFileContent(file);
      assertThat(content).contains("<ui:UiBinder");
      assertThat(content).contains("<g:FlowPanel");
      assertThat(content).contains("encoding=").contains("UTF-8");
      assertEquals("UTF-8", file.getCharset(false));
    }
  }
}