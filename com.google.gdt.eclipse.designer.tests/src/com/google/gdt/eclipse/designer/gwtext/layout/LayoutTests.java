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
package com.google.gdt.eclipse.designer.gwtext.layout;

import com.google.gdt.eclipse.designer.gwtext.model.layout.LayoutInfo;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for {@link LayoutInfo} and its subclasses.
 * 
 * @author scheglov_ke
 */
public class LayoutTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("gwt.GWTExt.layout");
    suite.addTestSuite(LayoutTest.class);
    suite.addTestSuite(RowLayoutTest.class);
    suite.addTestSuite(ColumnLayoutTest.class);
    suite.addTestSuite(FlowLayoutsGefTest.class);
    suite.addTestSuite(BorderLayoutTest.class);
    suite.addTestSuite(AnchorLayoutTest.class);
    suite.addTestSuite(AbsoluteLayoutTest.class);
    suite.addTestSuite(FormLayoutTest.class);
    suite.addTestSuite(FitLayoutTest.class);
    suite.addTestSuite(AccordionLayoutTest.class);
    suite.addTestSuite(CardLayoutTest.class);
    suite.addTestSuite(TableLayoutTest.class);
    suite.addTestSuite(HorizontalLayoutTest.class);
    suite.addTestSuite(VerticalLayoutTest.class);
    return suite;
  }
}
