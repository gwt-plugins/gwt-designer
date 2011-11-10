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
package com.google.gdt.eclipse.designer.core.model.widgets.grid;

import com.google.gdt.eclipse.designer.model.widgets.panels.grid.HTMLTableInfo;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests {@link HTMLTableInfo} and its successors.
 * 
 * @author scheglov_ke
 */
public class HTMLTableTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("gwt.model.widgets.grid");
    suite.addTest(createSingleSuite(HTMLTableTest.class));
    suite.addTest(createSingleSuite(HTMLTableGridInfoTest.class));
    suite.addTest(createSingleSuite(HTMLTableConstraintsTest.class));
    suite.addTest(createSingleSuite(GridTest.class));
    suite.addTest(createSingleSuite(FlexTableTest.class));
    suite.addTest(createSingleSuite(DimensionColumnTest.class));
    suite.addTest(createSingleSuite(DimensionRowTest.class));
    suite.addTest(createSingleSuite(CellFormatterExpressionAccessorTest.class));
    suite.addTest(createSingleSuite(HTMLTableCellTest.class));
    suite.addTest(createSingleSuite(HTMLTableGefTest.class));
    suite.addTest(createSingleSuite(FlexTableGefTest.class));
    return suite;
  }
}
