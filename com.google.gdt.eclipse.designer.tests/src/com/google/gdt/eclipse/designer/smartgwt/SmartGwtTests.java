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
package com.google.gdt.eclipse.designer.smartgwt;

import com.google.gdt.eclipse.designer.smartgwt.model.widgets.WidgetsTests;

import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.StrValue;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All SmartGWT tests.
 * 
 * @author scheglov_ke
 */
public class SmartGwtTests extends DesignerSuiteTests {
  public static final String LOCATION = Expectations.get(
      "C:/Work/GWT/smartgwt-2.4/",
      new StrValue("flanker-desktop", "/home/flanker/Work/GWT/smartgwt-2.5/"),
      new StrValue("mitin-aa", "/Users/mitin_aa/gwt/smartgwt-2.4/"));
  public static final String LOCATION_OLD = Expectations.get(
      "C:/Work/GWT/smartgwt-2.1/",
      new StrValue("flanker-desktop", "/home/flanker/Work/GWT/smartgwt-2.1/"),
      new StrValue("mitin-aa", "/Users/mitin_aa/gwt/smartgwt-2.1/"));

  public static Test suite() {
    TestSuite suite = new TestSuite("gwt.SmartGWT");
    suite.addTestSuite(ConfigureSmartGwtOperationTest.class);
    suite.addTestSuite(SmartGwtPropertyTesterTest.class);
    suite.addTest(WidgetsTests.suite());
    return suite;
  }
}
