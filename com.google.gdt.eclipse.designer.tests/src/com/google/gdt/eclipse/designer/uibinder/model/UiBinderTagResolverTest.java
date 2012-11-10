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
package com.google.gdt.eclipse.designer.uibinder.model;

import com.google.gdt.eclipse.designer.core.GTestUtils;
import com.google.gdt.eclipse.designer.uibinder.model.util.UiBinderTagResolver;

import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectResolveTag;

/**
 * Test for {@link UiBinderTagResolver}.
 * 
 * @author scheglov_ke
 */
public class UiBinderTagResolverTest extends UiBinderModelTest {
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
  public void test_standardWidget_user_client_ui() throws Exception {
    String[] lines =
        {
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<ui:UiBinder>",
            "  <g:FlowPanel/>",
            "</ui:UiBinder>"};
    parse(lines);
    //
    String componentClassName = "com.google.gwt.user.client.ui.Button";
    String expectedNamespace = "g";
    String expectedTag = "Button";
    assertNamespaceTag(componentClassName, expectedNamespace, expectedTag, lines);
  }

  /**
   * New Cell based widgets should have good namespace.
   */
  public void test_standardWidget_widget_client() throws Exception {
    configureForGWT_version(GTestUtils.getLocation_25());
    dontUseSharedGWTState();
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel/>",
        "</ui:UiBinder>");
    //
    String componentClassName = "com.google.gwt.widget.client.TextButton";
    String expectedNamespace = "w";
    String expectedTag = "TextButton";
    assertNamespaceTag(componentClassName, expectedNamespace, expectedTag, new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder xmlns:w='urn:import:com.google.gwt.widget.client'>",
        "  <g:FlowPanel/>",
        "</ui:UiBinder>"});
  }

  public void test_customWidget() throws Exception {
    dontUseSharedGWTState();
    setFileContentSrc(
        "test/client/sub/MyPanel.java",
        getSource(
            "package test.client.sub;",
            "import com.google.gwt.user.client.ui.*;",
            "public class MyPanel extends FlowPanel {",
            "  public MyPanel() {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel/>",
        "</ui:UiBinder>");
    //
    String componentClassName = "test.client.sub.MyPanel";
    String expectedNamespace = "p1";
    String expectedTag = "MyPanel";
    assertNamespaceTag(componentClassName, expectedNamespace, expectedTag, new String[]{
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder xmlns:p1='urn:import:test.client.sub'>",
        "  <g:FlowPanel/>",
        "</ui:UiBinder>"});
  }

  /**
   * Asserts that adding {@link XmlObjectInfo} with given name produces expected namespace and tag.
   */
  private void assertNamespaceTag(String componentClassName,
      String expectedNamespace,
      String expectedTag,
      String[] expectedLines) throws Exception {
    XmlObjectInfo object = createObject(componentClassName);
    Class<?> componentClass = object.getDescription().getComponentClass();
    //
    String[] namespaceArray = new String[1];
    String[] tagArray = new String[1];
    object.getBroadcast(XmlObjectResolveTag.class).invoke(
        object,
        componentClass,
        namespaceArray,
        tagArray);
    String namespace = namespaceArray[0];
    String tag = tagArray[0];
    //
    assertEquals(expectedNamespace, namespace);
    assertEquals(expectedTag, tag);
    assertXML(expectedLines);
  }
}