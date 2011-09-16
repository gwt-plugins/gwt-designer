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
package com.google.gdt.eclipse.designer.uibinder.model.widgets.cell;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * GWT UiBinder model tests.
 * 
 * @author scheglov_ke
 */
public class CellWidgetTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("gwt.UiBinder.model.widgets.cell");
    // 2.2
    suite.addTest(createSingleSuite(ValuePickerTest.class));
    suite.addTest(createSingleSuite(CellTableTest.class));
    suite.addTest(createSingleSuite(CellListTest.class));
    suite.addTest(createSingleSuite(CellTreeTest.class));
    suite.addTest(createSingleSuite(CellBrowserTest.class));
    suite.addTest(createSingleSuite(SimplePagerTest.class));
    suite.addTest(createSingleSuite(PageSizePagerTest.class));
    // 2.3
    return suite;
  }
}