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
package com.google.gdt.eclipse.designer.smartgwt.model.widgets;

import com.google.gdt.eclipse.designer.smart.model.CanvasInfo;
import com.google.gdt.eclipse.designer.smartgwt.model.SmartGwtModelTest;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for <code>com.smartgwt.client.widgets.Slider</code> widget.
 * 
 * @author scheglov_ke
 */
public class SliderTest extends SmartGwtModelTest {
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
  public void test_vertical() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    {",
            "      Slider slider = new Slider();",
            "      addChild(slider);",
            "    }",
            "  }",
            "}");
    canvas.refresh();
    CanvasInfo slider = getJavaInfoByName("slider");
    assertThat(slider.getBounds().height).isGreaterThan(200);
  }

  public void test_horizontal() throws Exception {
    CanvasInfo canvas =
        parseJavaInfo(
            "public class Test extends Canvas {",
            "  public Test() {",
            "    {",
            "      Slider slider = new Slider();",
            "      slider.setVertical(false);",
            "      addChild(slider);",
            "    }",
            "  }",
            "}");
    canvas.refresh();
    CanvasInfo slider = getJavaInfoByName("slider");
    assertThat(slider.getBounds().width).isGreaterThan(200);
  }
}