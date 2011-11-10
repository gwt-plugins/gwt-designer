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
package com.google.gdt.eclipse.designer.uibinder.model;

import com.google.gdt.eclipse.designer.uibinder.model.property.PropertyTests;
import com.google.gdt.eclipse.designer.uibinder.model.util.UtilTests;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.WidgetTests;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * GWT UiBinder model tests.
 * 
 * @author scheglov_ke
 */
public class ModelTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("gwt.UiBinder.model");
    suite.addTest(createSingleSuite(UiBinderTagResolverTest.class));
    suite.addTest(createSingleSuite(UiBinderStaticFieldSupportTest.class));
    suite.addTest(UtilTests.suite());
    suite.addTest(WidgetTests.suite());
    suite.addTest(PropertyTests.suite());
    return suite;
  }
}