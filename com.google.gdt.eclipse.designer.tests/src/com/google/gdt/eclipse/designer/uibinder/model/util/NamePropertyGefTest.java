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
package com.google.gdt.eclipse.designer.uibinder.model.util;

import com.google.gdt.eclipse.designer.uibinder.gef.UiBinderGefTest;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.ComplexPanelInfo;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;

import org.eclipse.jdt.ui.refactoring.RenameSupport;

/**
 * Test for {@link NameProperty} in GEF.
 * 
 * @author scheglov_ke
 */
public class NamePropertyGefTest extends UiBinderGefTest {
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
  // Canvas
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When we change existing name of "@UiField", we use {@link RenameSupport} for this. So, our
   * refactoring participant is used. And it updates <code>*.ui.xml</code> template. But when editor
   * is active (and it is active if we change name using {@link PropertyTable}), then we see change
   * of underlaying document and reparse. This is slow and even causes exception.
   * <p>
   * So, we should prevent reparse.
   */
  public void test_renameUsingProperty() throws Exception {
    setFileContentSrc(
        "test/client/Test.java",
        getJavaSource(
            "public class Test extends Composite {",
            "  interface Binder extends UiBinder<Widget, Test> {}",
            "  private static final Binder binder = GWT.create(Binder.class);",
            "  @UiField FlowPanel oldName;",
            "  public Test() {",
            "    initWidget(binder.createAndBindUi(this));",
            "  }",
            "}"));
    ComplexPanelInfo panel =
        openEditor(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel ui:field='oldName'/>",
            "</ui:UiBinder>");
    // initially we know root
    assertSame(panel, m_lastObject);
    //
    Property property = panel.getPropertyByTitle("UiField");
    property.setValue("newName");
    // still same root
    assertSame(panel, m_lastObject);
    // ...and expected content
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel ui:field='newName'/>",
        "</ui:UiBinder>");
    assertJava(
        "public class Test extends Composite {",
        "  interface Binder extends UiBinder<Widget, Test> {}",
        "  private static final Binder binder = GWT.create(Binder.class);",
        "  @UiField FlowPanel newName;",
        "  public Test() {",
        "    initWidget(binder.createAndBindUi(this));",
        "  }",
        "}");
  }
}