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
package com.google.gdt.eclipse.designer.gxt.model.widgets.form;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for GXT "form" package.
 * 
 * @author scheglov_ke
 */
public class FormTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("gwt.GXT.model.widgets.form");
    suite.addTestSuite(FormPanelTest.class);
    suite.addTestSuite(SliderFieldTest.class);
    suite.addTestSuite(MultiFieldTest.class);
    suite.addTestSuite(RadioGroupTest.class);
    suite.addTestSuite(FieldSetTest.class);
    suite.addTestSuite(ListFieldTest.class);
    suite.addTestSuite(DualListFieldTest.class);
    return suite;
  }
}
