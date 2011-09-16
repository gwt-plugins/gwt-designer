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
package com.google.gdt.eclipse.designer;

import com.google.gdt.eclipse.designer.core.GwtCoreTests;
import com.google.gdt.eclipse.designer.core.model.DisposeStateJavaTest;
import com.google.gdt.eclipse.designer.gpe.GwtGpeTests;
import com.google.gdt.eclipse.designer.gwtext.GwtExtTests;
import com.google.gdt.eclipse.designer.gxt.ExtGwtTests;
import com.google.gdt.eclipse.designer.mobile.GwtMobileTests;
import com.google.gdt.eclipse.designer.smartgwt.SmartGwtTests;
import com.google.gdt.eclipse.designer.uibinder.UiBinderTests;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All GWT tests.
 * 
 * @author scheglov_ke
 */
public class GwtTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("gwt");
    // GWT Core
    suite.addTest(GwtCoreTests.suite());
    suite.addTestSuite(DisposeStateJavaTest.class);
    // GWT GPE
    suite.addTest(GwtGpeTests.suite());
    suite.addTestSuite(DisposeStateJavaTest.class);
    // GWTExt
    suite.addTest(GwtExtTests.suite());
    suite.addTestSuite(DisposeStateJavaTest.class);
    // ExtGWT
    suite.addTest(ExtGwtTests.suite());
    suite.addTestSuite(DisposeStateJavaTest.class);
    // SmartGWT
    suite.addTest(SmartGwtTests.suite());
    suite.addTestSuite(DisposeStateJavaTest.class);
    // mobile
    suite.addTest(GwtMobileTests.suite());
    suite.addTestSuite(DisposeStateJavaTest.class);
    // UiBinder
    suite.addTest(UiBinderTests.suite());
    // for memory profiler
    //suite.addTestSuite(WaitForMemoryProfilerTest.class);
    return suite;
  }
}
