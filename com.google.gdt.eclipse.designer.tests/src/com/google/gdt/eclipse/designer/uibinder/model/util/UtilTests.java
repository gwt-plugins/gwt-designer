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
package com.google.gdt.eclipse.designer.uibinder.model.util;

import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for UiBinder properties and {@link PropertyEditor}s.
 * 
 * @author scheglov_ke
 */
public class UtilTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("gwt.UiBinder.model.property");
    suite.addTest(createSingleSuite(ImageUrlPropertyEditorTest.class));
    suite.addTest(createSingleSuite(NameSupportTest.class));
    suite.addTest(createSingleSuite(NamePropertyTest.class));
    suite.addTest(createSingleSuite(NamePropertyGefTest.class));
    suite.addTest(createSingleSuite(EventsHandlersSupportTest.class));
    suite.addTest(createSingleSuite(UiConstructorTest.class));
    suite.addTest(createSingleSuite(UiChildTest.class));
    suite.addTest(createSingleSuite(UiChildGefTest.class));
    suite.addTest(createSingleSuite(TemplateChangedRootProcessorTest.class));
    suite.addTest(createSingleSuite(MorphingSupportTest.class));
    return suite;
  }
}