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
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link NumberLabelInfo}.
 * 
 * @author scheglov_ke
 */
public class NumberLabelTest extends UiBinderModelTest {
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
        "    <g:NumberLabel wbp:name='numberLabel'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo numberLabel = getObjectByName("numberLabel");
    // has some text
    assertEquals("NumberLabel", getValueLabelText(numberLabel));
    // not empty bounds
    {
      Rectangle bounds = numberLabel.getBounds();
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // Format
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_formatProperty_getValue() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:NumberLabel wbp:name='numberLabel'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo numberLabel = getObjectByName("numberLabel");
    // prepare property
    Property property = numberLabel.getPropertyByTitle("format");
    assertNotNull(property);
    assertSame(PropertyCategory.PREFERRED, property.getCategory());
    // no value yet
    assertFalse(property.isModified());
    assertNull(property.getValue());
    assertEquals(null, getPropertyText(property));
    // "format" attribute
    {
      property.setValue(Property.UNKNOWN_VALUE);
      numberLabel.setAttribute("format", "{myFormat}");
      assertTrue(property.isModified());
      assertEquals("{myFormat}", property.getValue());
    }
    // "predefinedFormat" attribute
    {
      property.setValue(Property.UNKNOWN_VALUE);
      numberLabel.setAttribute("predefinedFormat", "CURRENCY");
      assertTrue(property.isModified());
      // no currency
      assertEquals("CURRENCY", property.getValue());
      // with currency
      numberLabel.setAttribute("currencyCode", "RUB");
      assertEquals("CURRENCY RUB", property.getValue());
    }
    // "customFormat" attribute
    {
      property.setValue(Property.UNKNOWN_VALUE);
      numberLabel.setAttribute("customFormat", "0.00");
      assertTrue(property.isModified());
      // no currency
      assertEquals("0.00", property.getValue());
      // with currency
      numberLabel.setAttribute("currencyCode", "USD");
      assertEquals("0.00 USD", property.getValue());
    }
  }

  public void test_formatProperty_setValue_clear() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:NumberLabel wbp:name='numberLabel'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo numberLabel = getObjectByName("numberLabel");
    Property property = numberLabel.getPropertyByTitle("format");
    // no value yet
    assertFalse(property.isModified());
    assertNull(property.getValue());
    assertEquals(null, getPropertyText(property));
    // set raw "format" attributes
    numberLabel.setAttribute("format", "a");
    numberLabel.setAttribute("predefinedFormat", "b");
    numberLabel.setAttribute("customFormat", "c");
    numberLabel.setAttribute("currencyData", "d");
    numberLabel.setAttribute("currencyCode", "e");
    // has some format
    assertLabelSource(" format='a' predefinedFormat='b' customFormat='c' currencyData='d' currencyCode='e'");
    assertTrue(property.isModified());
    // clear
    property.setValue(Property.UNKNOWN_VALUE);
    assertFalse(property.isModified());
    assertNull(property.getValue());
  }

  public void test_formatProperty_setValue() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:NumberLabel wbp:name='numberLabel'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo numberLabel = getObjectByName("numberLabel");
    Property property = numberLabel.getPropertyByTitle("format");
    // no currency
    property.setValue(new String[]{"predefinedFormat", "SCIENTIFIC", null});
    assertLabelSource(" predefinedFormat='SCIENTIFIC'");
    // with currency
    property.setValue(new String[]{"customFormat", "0.00", "RUB"});
    assertLabelSource(" customFormat='0.00' currencyCode='RUB'");
  }

  public void test_formatProperty_dialog() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:NumberLabel wbp:name='numberLabel' customFormat='0.00' currencyCode='RUB'/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
    refresh();
    WidgetInfo numberLabel = getObjectByName("numberLabel");
    final Property property = numberLabel.getPropertyByTitle("format");
    // abstract class for NumberFormat dialog testing
    abstract class NumberLabelUIRunnable implements UIRunnable {
      Button predefinedButton;
      Button customButton;
      Button currencyButton;
      Combo predefinedCombo;
      Text customText;
      Combo currencyCombo;

      @Override
      public final void run(UiContext context) throws Exception {
        // prepare widgets
        predefinedButton = context.getButtonByText("Predefined format:");
        customButton = context.getButtonByText("Custom format:");
        currencyButton = context.getButtonByText("Currency code:");
        predefinedCombo = context.getControlAfter(predefinedButton);
        customText = context.getControlAfter(customButton);
        currencyCombo = context.getControlAfter(currencyButton);
        // run
        runEx(context);
      }

      abstract void runEx(UiContext context) throws Exception;
    }
    // set SCIENTIFIC
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        openPropertyDialog(property);
      }
    }, new NumberLabelUIRunnable() {
      @Override
      public void runEx(UiContext context) throws Exception {
        context.useShell("format");
        // initial state
        {
          assertEquals(false, predefinedButton.getSelection());
          assertEquals(true, customButton.getSelection());
          assertEquals(true, currencyButton.getSelection());
          //
          assertEquals(false, predefinedCombo.isEnabled());
          assertEquals(true, customText.isEnabled());
          assertEquals(true, currencyButton.isEnabled());
          assertEquals(true, currencyCombo.isEnabled());
          //
          assertEquals("DECIMAL", predefinedCombo.getText());
          assertEquals("0.00", customText.getText());
          assertEquals("RUB", currencyCombo.getText());
        }
        // set new state
        context.selectButton(predefinedButton);
        predefinedCombo.setText("SCIENTIFIC");
        {
          assertEquals(true, predefinedCombo.isEnabled());
          assertEquals(false, customText.isEnabled());
          assertEquals(false, currencyButton.isEnabled());
          assertEquals(false, currencyCombo.isEnabled());
          //
          assertEquals("SCIENTIFIC", predefinedCombo.getText());
        }
        // commit
        context.clickButton("OK");
      }
    });
    assertLabelSource(" predefinedFormat='SCIENTIFIC'");
    // set custom + currency
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        openPropertyDialog(property);
      }
    }, new NumberLabelUIRunnable() {
      @Override
      public void runEx(UiContext context) throws Exception {
        context.useShell("format");
        // initial state
        {
          assertEquals(true, predefinedButton.getSelection());
          assertEquals(false, customButton.getSelection());
          assertEquals(false, currencyButton.getSelection());
          //
          assertEquals(true, predefinedCombo.isEnabled());
          assertEquals(false, customText.isEnabled());
          assertEquals(false, currencyButton.isEnabled());
          assertEquals(false, currencyCombo.isEnabled());
          //
          assertEquals("SCIENTIFIC", predefinedCombo.getText());
          assertEquals("0.0", customText.getText());
          assertEquals(0, currencyCombo.getSelectionIndex());
        }
        // set new state
        context.selectButton(customButton);
        context.selectButton(currencyButton);
        customText.setText("#.##");
        currencyCombo.setText("USD");
        {
          assertEquals(false, predefinedCombo.isEnabled());
          assertEquals(true, customText.isEnabled());
          assertEquals(true, currencyButton.isEnabled());
          assertEquals(true, currencyButton.getSelection());
          assertEquals(true, currencyCombo.isEnabled());
          //
          assertEquals("USD", currencyCombo.getText());
        }
        // commit
        context.clickButton("OK");
      }
    });
    assertLabelSource(" customFormat='#.##' currencyCode='USD'");
  }

  private void assertLabelSource(String expected) {
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<ui:UiBinder>",
        "  <g:FlowPanel>",
        "    <g:NumberLabel wbp:name='numberLabel'" + expected + "/>",
        "  </g:FlowPanel>",
        "</ui:UiBinder>");
  }
}