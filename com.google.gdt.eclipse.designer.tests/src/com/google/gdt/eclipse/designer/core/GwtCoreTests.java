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
package com.google.gdt.eclipse.designer.core;

import com.google.gdt.eclipse.designer.core.builders.BuilderTests;
import com.google.gdt.eclipse.designer.core.common.CommonTests;
import com.google.gdt.eclipse.designer.core.description.DescriptionTests;
import com.google.gdt.eclipse.designer.core.model.ModelTests;
import com.google.gdt.eclipse.designer.core.nls.NlsTests;
import com.google.gdt.eclipse.designer.core.refactoring.RefactoringTests;
import com.google.gdt.eclipse.designer.core.util.UtilTests;
import com.google.gdt.eclipse.designer.core.wizards.WizardsTests;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * GWT core tests.
 * 
 * @author scheglov_ke
 */
public class GwtCoreTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("gwt");
    suite.addTest(UtilTests.suite());
    suite.addTest(ModelTests.suite());
    suite.addTest(DescriptionTests.suite());
    suite.addTest(NlsTests.suite());
    suite.addTest(CommonTests.suite());
    suite.addTest(WizardsTests.suite());
    suite.addTest(BuilderTests.suite());
    suite.addTest(RefactoringTests.suite());
    return suite;
  }
}
