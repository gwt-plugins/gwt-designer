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
package com.google.gdt.eclipse.designer.gxt.model.widgets;

import com.google.gdt.eclipse.designer.gxt.model.widgets.form.FormTests;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Ext-GWT widgets tests.
 * 
 * @author scheglov_ke
 */
public class WidgetsTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("gwt.GXT.model.widgets");
    suite.addTest(createSingleSuite(ComponentTest.class));
    suite.addTest(createSingleSuite(UntypedEventsTest.class));
    suite.addTest(createSingleSuite(HtmlContainerTest.class));
    suite.addTest(createSingleSuite(ComboBoxTest.class));
    suite.addTest(createSingleSuite(TableTest.class));
    suite.addTest(createSingleSuite(TreeTest.class));
    suite.addTest(createSingleSuite(TreeTableTest.class));
    suite.addTest(createSingleSuite(GridTest.class));
    suite.addTest(createSingleSuite(EditorGridTest.class));
    suite.addTest(createSingleSuite(TreeGridTest.class));
    suite.addTest(createSingleSuite(EditorTreeGridTest.class));
    suite.addTest(createSingleSuite(ListViewTest.class));
    // LayoutContainer and panels
    suite.addTest(createSingleSuite(LayoutContainerTest.class));
    suite.addTest(createSingleSuite(HorizontalPanelTest.class));
    suite.addTest(createSingleSuite(VerticalPanelTest.class));
    suite.addTest(createSingleSuite(PortalTest.class));
    suite.addTest(createSingleSuite(TabPanelTest.class));
    suite.addTest(createSingleSuite(MenuTest.class));
    suite.addTest(createSingleSuite(MenuGefTest.class));
    suite.addTest(createSingleSuite(ToolBarTest.class));
    suite.addTest(createSingleSuite(CompositeTest.class));
    suite.addTest(createSingleSuite(ContentPanelTest.class));
    suite.addTest(createSingleSuite(ContentPanelGefTest.class));
    suite.addTest(createSingleSuite(ButtonGroupTest.class));
    suite.addTest(createSingleSuite(DialogTest.class));
    suite.addTest(createSingleSuite(ViewportTest.class));
    // form
    suite.addTest(FormTests.suite());
    return suite;
  }
}
