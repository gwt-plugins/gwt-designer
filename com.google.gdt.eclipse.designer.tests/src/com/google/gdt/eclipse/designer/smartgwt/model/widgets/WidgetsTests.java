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
package com.google.gdt.eclipse.designer.smartgwt.model.widgets;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * SmartGWT widgets tests.
 * 
 * @author scheglov_ke
 */
public class WidgetsTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("gwt.SmartGWT.model.widgets");
    suite.addTest(createSingleSuite(VersionTest.class));
    suite.addTest(createSingleSuite(CanvasTest.class));
    suite.addTest(createSingleSuite(SliderTest.class));
    suite.addTest(createSingleSuite(HLayoutTest.class));
    suite.addTest(createSingleSuite(VLayoutTest.class));
    suite.addTest(createSingleSuite(TileLayoutTest.class));
    suite.addTest(createSingleSuite(ListGridTest.class));
    suite.addTest(createSingleSuite(DetailViewerTest.class));
    suite.addTest(createSingleSuite(TileGridTest.class));
    suite.addTest(createSingleSuite(ColumnTreeTest.class));
    suite.addTest(createSingleSuite(TreeGridTest.class));
    suite.addTest(createSingleSuite(SectionStackTest.class));
    suite.addTest(createSingleSuite(DynamicFormTest.class));
    suite.addTest(createSingleSuite(FormItemTest.class));
    suite.addTest(createSingleSuite(TabSetTest.class));
    suite.addTest(createSingleSuite(ToolStripTest.class));
    suite.addTest(createSingleSuite(MenuTest.class));
    suite.addTest(createSingleSuite(MenuButtonTest.class));
    suite.addTest(createSingleSuite(MenuBarTest.class));
    suite.addTest(createSingleSuite(WindowTest.class));
    suite.addTest(createSingleSuite(FilterBuilderTest.class));
    suite.addTest(createSingleSuite(DataSourceTest.class));
    // GEF
    suite.addTest(createSingleSuite(ListGridGefTest.class));
    return suite;
  }
}
