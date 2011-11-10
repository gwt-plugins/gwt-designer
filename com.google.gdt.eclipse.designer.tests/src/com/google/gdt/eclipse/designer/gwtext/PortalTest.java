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
package com.google.gdt.eclipse.designer.gwtext;

import com.google.gdt.eclipse.designer.gwtext.model.widgets.ContainerInfo;

/**
 * Tests for <code>Portal</code>.
 * 
 * @author scheglov_ke
 */
public class PortalTest extends GwtExtModelTest {
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
  public void test_parse() throws Exception {
    ContainerInfo panel =
        parseJavaInfo(
            "public class Test extends Portal {",
            "  public Test() {",
            "    PortalColumn firstCol = new PortalColumn();",
            "    firstCol.setPaddings(10, 10, 0, 10);",
            "    {",
            "      Portlet portlet = new Portlet('My portlet', 'Some html');",
            "      firstCol.add(portlet);;",
            "    }",
            "    add(firstCol, new ColumnLayoutData(.33));",
            "  }",
            "}");
    panel.refresh();
    //
    assertHierarchy(
        "{this: com.gwtext.client.widgets.portal.Portal} {this} {/add(firstCol, new ColumnLayoutData(.33))/}",
        "  {implicit-layout: com.gwtext.client.widgets.layout.ColumnLayout} {implicit-layout} {}",
        "  {new: com.gwtext.client.widgets.portal.PortalColumn} {local-unique: firstCol} {/new PortalColumn()/ /firstCol.setPaddings(10, 10, 0, 10)/ /firstCol.add(portlet)/ /add(firstCol, new ColumnLayoutData(.33))/}",
        "    {new: com.gwtext.client.widgets.portal.Portlet} {local-unique: portlet} {/new Portlet('My portlet', 'Some html')/ /firstCol.add(portlet)/}",
        "      {implicit-layout: default} {implicit-layout} {}",
        "    {new: com.gwtext.client.widgets.layout.ColumnLayoutData} {empty} {/add(firstCol, new ColumnLayoutData(.33))/}");
  }
}