/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
