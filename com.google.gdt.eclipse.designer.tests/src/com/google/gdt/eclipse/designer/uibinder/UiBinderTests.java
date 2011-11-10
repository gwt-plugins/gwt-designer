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
package com.google.gdt.eclipse.designer.uibinder;

import com.google.gdt.eclipse.designer.core.model.DisposeStateJavaTest;
import com.google.gdt.eclipse.designer.uibinder.gef.GefTests;
import com.google.gdt.eclipse.designer.uibinder.model.ModelTests;
import com.google.gdt.eclipse.designer.uibinder.wizards.WizardTests;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All GWT UiBinder tests.
 * 
 * @author scheglov_ke
 */
public class UiBinderTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("gwt.UiBinder");
    suite.addTest(createSingleSuite(DisposeStateJavaTest.class));
    {
      suite.addTest(createSingleSuite(ActivatorTest.class));
      suite.addTest(ModelTests.suite());
      suite.addTest(GefTests.suite());
      suite.addTest(WizardTests.suite());
    }
    suite.addTest(createSingleSuite(DisposeStateXmlTest.class));
    //suite.addTestSuite(WaitForMemoryProfilerTest.class);
    return suite;
  }
}
