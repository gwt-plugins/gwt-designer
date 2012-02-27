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
package com.google.gdt.eclipse.designer.core.model.widgets;

import com.google.gdt.eclipse.designer.core.model.widgets.cell.CellWidgetTests;
import com.google.gdt.eclipse.designer.core.model.widgets.generic.GenericTests;
import com.google.gdt.eclipse.designer.core.model.widgets.grid.HTMLTableTests;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests widgets models.
 * 
 * @author scheglov_ke
 */
public class WidgetTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("gwt.model.widgets");
    // TODO(scheglov) 20120130 Disabled because of memory leaks.
    //suite.addTest(createSingleSuite(ClassLoaderTest.class));
    //suite.addTest(createSingleSuite(VersionsTest.class));
    //
    suite.addTest(createSingleSuite(WidgetTest.class));
    suite.addTest(createSingleSuite(WidgetTopBoundsSupportTest.class));
    suite.addTest(createSingleSuite(GwtStateTest.class));
    suite.addTest(createSingleSuite(CssSupportTest.class));
    // XXX
    suite.addTest(createSingleSuite(JavaScriptObjectTest.class));
    suite.addTest(createSingleSuite(UIObjectUtilsTest.class));
    suite.addTest(createSingleSuite(UIObjectSizeSupportTest.class));
    suite.addTest(createSingleSuite(RootPanelTopBoundsSupportTest.class));
    suite.addTest(createSingleSuite(GwtLiveManagerTest.class));
    suite.addTest(createSingleSuite(CoordinatesTest.class));
    suite.addTest(createSingleSuite(FactoryTest.class));
    suite.addTest(createSingleSuite(UIObjectSelectionEditPolicyTest.class));
    suite.addTest(createSingleSuite(AbsolutePanelTest.class));
    suite.addTest(createSingleSuite(AbsolutePanelGefTest.class));
    suite.addTest(createSingleSuite(VisualInheritanceTest.class));
    suite.addTest(createSingleSuite(ImageBundleTest.class));
    suite.addTest(createSingleSuite(CustomButtonTest.class));
    suite.addTest(createSingleSuite(ListBoxTest.class));
    suite.addTest(createSingleSuite(TreeTest.class));
    suite.addTest(createSingleSuite(TreeGefTest.class));
    suite.addTest(createSingleSuite(CaptionPanelTest.class));
    suite.addTest(createSingleSuite(DatePickerTest.class));
    suite.addTest(createSingleSuite(DateBoxTest.class));
    suite.addTest(createSingleSuite(PanelTest.class));
    suite.addTest(createSingleSuite(HistoryTest.class));
    suite.addTest(createSingleSuite(HyperlinkTest.class));
    suite.addTest(createSingleSuite(CompositeTest.class));
    suite.addTest(GenericTests.suite());
    suite.addTest(createSingleSuite(FlowPanelTest.class));
    suite.addTest(createSingleSuite(DockPanelTest.class));
    suite.addTest(createSingleSuite(DockPanelGefTest.class));
    suite.addTest(createSingleSuite(HorizontalPanelTest.class));
    suite.addTest(createSingleSuite(VerticalPanelTest.class));
    suite.addTest(createSingleSuite(DeckPanelTest.class));
    suite.addTest(createSingleSuite(DisclosurePanelTest.class));
    suite.addTest(createSingleSuite(HTMLPanelTest.class));
    suite.addTest(createSingleSuite(StackPanelTest.class));
    suite.addTest(createSingleSuite(StackPanelGefTest.class));
    suite.addTest(createSingleSuite(SimplePanelTest.class));
    suite.addTest(createSingleSuite(HorizontalSplitPanelTest.class));
    suite.addTest(createSingleSuite(HorizontalSplitPanelGefTest.class));
    suite.addTest(createSingleSuite(VerticalSplitPanelTest.class));
    suite.addTest(createSingleSuite(VerticalSplitPanelGefTest.class));
    suite.addTest(createSingleSuite(TabPanelTest.class));
    suite.addTest(createSingleSuite(TabPanelGefTest.class));
    suite.addTest(createSingleSuite(LazyPanelTest.class));
    suite.addTest(createSingleSuite(MenuBarTest.class));
    suite.addTest(createSingleSuite(MenuBarGefTest.class));
    suite.addTest(HTMLTableTests.suite());
    // GWT 2.0
    suite.addTest(createSingleSuite(RootLayoutPanelTest.class));
    suite.addTest(createSingleSuite(RootLayoutPanelGefTest.class));
    suite.addTest(createSingleSuite(DockLayoutPanelTest.class));
    suite.addTest(createSingleSuite(DockLayoutPanelGefTest.class));
    suite.addTest(createSingleSuite(SplitLayoutPanelTest.class));
    suite.addTest(createSingleSuite(StackLayoutPanelTest.class));
    suite.addTest(createSingleSuite(StackLayoutPanelGefTest.class));
    suite.addTest(createSingleSuite(TabLayoutPanelTest.class));
    suite.addTest(createSingleSuite(TabLayoutPanelGefTest.class));
    suite.addTest(createSingleSuite(ResizeCompositeTest.class));
    // popup
    suite.addTest(createSingleSuite(PopupPanelTest.class));
    suite.addTest(createSingleSuite(DialogBoxTest.class));
    // Google
    suite.addTest(createSingleSuite(MapWidgetTest.class));
    // GWT 2.1
    suite.addTest(createSingleSuite(ValueBoxTest.class));
    suite.addTest(createSingleSuite(ValuePickerTest.class));
    suite.addTest(createSingleSuite(ValueListBoxTest.class));
    suite.addTest(createSingleSuite(ValueLabelTest.class));
    suite.addTest(createSingleSuite(DateLabelTest.class));
    suite.addTest(createSingleSuite(NumberLabelTest.class));
    suite.addTest(CellWidgetTests.suite());
    // wait for memory profiler
    /*suite.addTestSuite(DisposeStateJavaTest.class);
    suite.addTestSuite(WaitForMemoryProfilerTest.class);*/
    return suite;
  }
}
