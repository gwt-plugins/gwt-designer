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
package com.google.gdt.eclipse.designer.gxt.model.layout;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for GXT {@link LayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class LayoutTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("gwt.GXT.model.layout");
    suite.addTest(createSingleSuite(LayoutTest.class));
    suite.addTest(createSingleSuite(LayoutDataTest.class));
    suite.addTest(createSingleSuite(FlowLayoutTest.class));
    suite.addTest(createSingleSuite(RowLayoutTest.class));
    suite.addTest(createSingleSuite(FillLayoutTest.class));
    suite.addTest(createSingleSuite(ColumnLayoutTest.class));
    suite.addTest(createSingleSuite(FitLayoutTest.class));
    suite.addTest(createSingleSuite(CardLayoutTest.class));
    suite.addTest(createSingleSuite(AccordionLayoutTest.class));
    suite.addTest(createSingleSuite(HBoxLayoutTest.class));
    suite.addTest(createSingleSuite(VBoxLayoutTest.class));
    suite.addTest(createSingleSuite(CenterLayoutTest.class));
    suite.addTest(createSingleSuite(BorderLayoutTest.class));
    suite.addTest(createSingleSuite(BorderLayoutGefTest.class));
    suite.addTest(createSingleSuite(AnchorLayoutTest.class));
    suite.addTest(createSingleSuite(AbsoluteLayoutTest.class));
    suite.addTest(createSingleSuite(AbsoluteLayoutGefTest.class));
    suite.addTest(createSingleSuite(FormLayoutTest.class));
    suite.addTest(createSingleSuite(TableLayoutTest.class));
    suite.addTest(createSingleSuite(TableLayoutGefTest.class));
    suite.addTest(createSingleSuite(TableRowLayoutTest.class));
    return suite;
  }
}
