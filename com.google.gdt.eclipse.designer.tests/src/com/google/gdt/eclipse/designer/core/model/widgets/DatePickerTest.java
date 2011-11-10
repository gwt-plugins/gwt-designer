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
package com.google.gdt.eclipse.designer.core.model.widgets;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.tests.designer.core.model.property.EventsPropertyTest;

/**
 * Test for <code>DatePicker</code>.
 * 
 * @author scheglov_ke
 */
public class DatePickerTest extends GwtModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * <code>DatePicker</code> uses parameterized <code>ValueChangeHandler</code> with parameterized
   * (again!) <code>ValueChangeEvent</code>. We test that we can use such listener.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?41941
   */
  public void test_valueChanged() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "import com.google.gwt.event.logical.shared.*;",
            "import com.google.gwt.user.datepicker.client.DatePicker;",
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      DatePicker datePicker = new DatePicker();",
            "      rootPanel.add(datePicker);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    WidgetInfo datePicker = frame.getChildrenWidgets().get(0);
    //
    EventsPropertyTest.ensureListenerMethod(datePicker, "valueChange", "onValueChange");
    assertEditor(
        "import com.google.gwt.event.logical.shared.*;",
        "import com.google.gwt.user.datepicker.client.DatePicker;",
        "import java.util.Date;",
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      DatePicker datePicker = new DatePicker();",
        "      datePicker.addValueChangeHandler(new ValueChangeHandler<Date>() {",
        "        public void onValueChange(ValueChangeEvent<Date> event) {",
        "        }",
        "      });",
        "      rootPanel.add(datePicker);",
        "    }",
        "  }",
        "}");
  }
}