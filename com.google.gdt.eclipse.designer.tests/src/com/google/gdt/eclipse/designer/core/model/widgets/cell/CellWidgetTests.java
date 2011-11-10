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
package com.google.gdt.eclipse.designer.core.model.widgets.cell;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests "cell" widgets.
 * 
 * @author scheglov_ke
 */
public class CellWidgetTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("gwt.model.widgets.cell");
    // 2.2
    suite.addTest(createSingleSuite(CellTreeTest.class));
    suite.addTest(createSingleSuite(CellBrowserTest.class));
    suite.addTest(createSingleSuite(CellListTest.class));
    suite.addTest(createSingleSuite(CellTableTest.class));
    suite.addTest(createSingleSuite(ColumnTest.class));
    suite.addTest(createSingleSuite(ColumnGefTest.class));
    // 2.3
    suite.addTest(createSingleSuite(TextButtonTest.class));
    return suite;
  }
}
