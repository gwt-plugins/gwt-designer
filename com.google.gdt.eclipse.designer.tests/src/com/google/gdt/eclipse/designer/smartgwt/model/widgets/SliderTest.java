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