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
package com.google.gdt.eclipse.designer.uibinder.model.widgets;

import com.google.gdt.eclipse.designer.uibinder.model.widgets.cell.CellWidgetTests;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * GWT UiBinder model tests.
 * 
 * @author scheglov_ke
 */
public class WidgetTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("gwt.UiBinder.model.widgets");
    // general UiBinder support
    suite.addTest(createSingleSuite(WidgetTest.class));
    suite.addTest(createSingleSuite(UIObjectSizeSupportTest.class));
    suite.addTest(createSingleSuite(WidgetTopBoundsSupportTest.class));
    suite.addTest(createSingleSuite(GwtLiveManagerTest.class));
    suite.addTest(createSingleSuite(SizeManagementTest.class));
    // IsWidget
    suite.addTest(createSingleSuite(IsWidgetTest.class));
    suite.addTest(createSingleSuite(IsWidgetGefTest.class));
    // specific widgets
    suite.addTest(createSingleSuite(DateBoxTest.class));
    suite.addTest(createSingleSuite(HTMLTest.class));
    suite.addTest(createSingleSuite(FlowPanelTest.class));
    suite.addTest(createSingleSuite(FlowPanelGefTest.class));
    suite.addTest(createSingleSuite(HorizontalPanelTest.class));
    suite.addTest(createSingleSuite(VerticalPanelTest.class));
    suite.addTest(createSingleSuite(CellPanelTest.class));
    suite.addTest(createSingleSuite(HTMLPanelTest.class));
    suite.addTest(createSingleSuite(DockPanelTest.class));
    suite.addTest(createSingleSuite(DockPanelGefTest.class));
    suite.addTest(createSingleSuite(DeckPanelTest.class));
    suite.addTest(createSingleSuite(StackPanelTest.class));
    suite.addTest(createSingleSuite(StackPanelGefTest.class));
    suite.addTest(createSingleSuite(StackLayoutPanelTest.class));
    suite.addTest(createSingleSuite(StackLayoutPanelGefTest.class));
    suite.addTest(createSingleSuite(SimplePanelTest.class));
    suite.addTest(createSingleSuite(CaptionPanelTest.class));
    suite.addTest(createSingleSuite(DisclosurePanelTest.class));
    suite.addTest(createSingleSuite(DisclosurePanelGefTest.class));
    suite.addTest(createSingleSuite(AbsolutePanelTest.class));
    suite.addTest(createSingleSuite(AbsolutePanelGefTest.class));
    suite.addTest(createSingleSuite(TreeTest.class));
    suite.addTest(createSingleSuite(TreeGefTest.class));
    suite.addTest(createSingleSuite(DockLayoutPanelTest.class));
    suite.addTest(createSingleSuite(DockLayoutPanelGefTest.class));
    suite.addTest(createSingleSuite(SplitLayoutPanelTest.class));
    suite.addTest(createSingleSuite(TabPanelTest.class));
    suite.addTest(createSingleSuite(TabPanelGefTest.class));
    suite.addTest(createSingleSuite(TabLayoutPanelTest.class));
    suite.addTest(createSingleSuite(TabLayoutPanelGefTest.class));
    suite.addTest(createSingleSuite(LayoutPanelTest.class));
    suite.addTest(createSingleSuite(LayoutPanelGefTest.class));
    suite.addTest(createSingleSuite(MenuBarTest.class));
    suite.addTest(createSingleSuite(MenuBarGefTest.class));
    suite.addTest(createSingleSuite(GridTest.class));
    suite.addTest(createSingleSuite(GridGefTest.class));
    suite.addTest(createSingleSuite(DateLabelTest.class));
    suite.addTest(createSingleSuite(NumberLabelTest.class));
    // Cell widgets
    suite.addTest(CellWidgetTests.suite());
    return suite;
  }
}