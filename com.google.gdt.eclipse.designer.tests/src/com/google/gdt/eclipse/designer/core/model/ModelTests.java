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
