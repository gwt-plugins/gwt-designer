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
package com.google.gdt.eclipse.designer.gwtext;

import com.google.gdt.eclipse.designer.gwtext.layout.LayoutTests;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All GWT-Ext tests.
 * 
 * @author sablin_aa
 */
public class GwtExtTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("gwt.GWTExt");
    suite.addTestSuite(ConfigureGwtExtOperationTest.class);
    suite.addTestSuite(ComponentTest.class);
    suite.addTestSuite(BoxComponentTest.class);
    suite.addTestSuite(CycleButtonTest.class);
    suite.addTestSuite(ContainerTest.class);
    suite.addTest(LayoutTests.suite());
    suite.addTestSuite(PanelTest.class);
    suite.addTestSuite(TreePanelTest.class);
    suite.addTestSuite(FocusPanelTest.class);
    suite.addTestSuite(MapPanelTest.class);
    //
    suite.addTestSuite(FormPanelTest.class);
    suite.addTestSuite(GridPanelTest.class);
    suite.addTestSuite(PaddedPanelTest.class);
    suite.addTestSuite(HTMLPanelTest.class);
    suite.addTestSuite(TabPanelTest.class);
    suite.addTestSuite(MultiFieldPanelTest.class);
    suite.addTestSuite(WindowTest.class);
    suite.addTestSuite(PortalTest.class);
    //
    suite.addTestSuite(ToolbarTest.class);
    suite.addTestSuite(PagingToolbarTest.class);
    return suite;
  }
}
