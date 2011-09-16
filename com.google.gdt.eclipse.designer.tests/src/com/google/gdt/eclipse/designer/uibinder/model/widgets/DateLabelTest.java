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
package com.google.gdt.eclipse.designer.uibinder.model.widgets;

import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;

import org.eclipse.wb.draw2d.geometry.Rectangle;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link DateLabelInfo}.
 * 
 * @author scheglov_ke
 */
public class DateLabelTest extends UiBinderModelTest {
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
   * We should show some text to have some bounds.
   */
  public void test_parse() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:DateLabel wbp:name='dateLabel'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo dateLabel = getObjectByName("dateLabel");
    // has some text
    assertEquals("12/31/2010", getValueLabelText(dateLabel));
    // not empty bounds
    {
      Rectangle bounds = dateLabel.getBounds();
      assertThat(bounds.width).isGreaterThan(75);
      assertThat(bounds.height).isGreaterThan(15);
    }
  }

  /**
   * @return the inner text of given <code>ValueLabel</code> element.
   */
  private static String getValueLabelText(WidgetInfo valueLabel) throws Exception {
    Object element = valueLabel.getDOMElement();
    return valueLabel.getDOM().getInnerText(element);
  }
}