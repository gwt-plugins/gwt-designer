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
package com.google.gdt.eclipse.designer.core.model;

import com.google.gdt.eclipse.designer.core.model.module.ModuleModelTests;
import com.google.gdt.eclipse.designer.core.model.property.PropertyTests;
import com.google.gdt.eclipse.designer.core.model.web.WebModelTests;
import com.google.gdt.eclipse.designer.core.model.widgets.WidgetTests;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * GWT model tests.
 * 
 * @author scheglov_ke
 */
public class ModelTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("gwt.model");
    suite.addTest(ModuleModelTests.suite());
    suite.addTest(WebModelTests.suite());
    suite.addTest(WidgetTests.suite());
    suite.addTest(PropertyTests.suite());
    return suite;
  }
}
