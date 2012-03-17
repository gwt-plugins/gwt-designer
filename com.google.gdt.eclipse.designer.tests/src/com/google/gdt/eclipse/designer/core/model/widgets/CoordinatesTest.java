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
import com.google.gdt.eclipse.designer.model.widgets.panels.ComplexPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;

import org.apache.commons.lang.StringUtils;

/**
 * Test absolute/parent/etc coordinates.
 * 
 * @author scheglov_ke
 */
public class CoordinatesTest extends GwtModelTest {
  protected boolean strict = true;
  /**
   * Default BODY border left.
   */
  private static final int BBL = 0;
  /**
   * Default BODY border top.
   */
  private static final int BBT = 0;
  /**
   * Default BODY margin.
   */
  private static final int BM = 8;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    dontUseSharedGWTState();
  }

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
  public void test_emptyRootPanel() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    assertHierarchy("{RootPanel.get()} {local-unique: rootPanel} {/RootPanel.get()/}");
    frame.refresh();
    // default bounds
    {
      assertEquals(new Rectangle(0, 0, 450, 300), frame.getBounds());
      assertEquals(new Rectangle(0, 0, 450, 300), frame.getModelBounds());
    }
    // check GWT_TopBoundsSupport, set new size
    {
      TopBoundsSupport topBoundsSupport = frame.getTopBoundsSupport();
      topBoundsSupport.setSize(500, 400);
      frame.refresh();
      assertEquals(new Rectangle(0, 0, 500, 400), frame.getBounds());
      assertEquals(new Rectangle(0, 0, 500, 400), frame.getModelBounds());
    }
  }

  /**
   * IE6/IE7 has default <code>(2,2,2,2)</code> borders.
   */
  public void test_defaultBorderIE() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      rootPanel.add(button, 10, 20);",
            "      button.setPixelSize(100, 30);",
            "    }",
            "  }",
            "}");
    frame.refresh();
    // check bounds for Button
    {
      WidgetInfo button = frame.getChildrenWidgets().get(0);
      assertEquals(new Rectangle(10, 20, 100, 30), button.getBounds());
      assertEquals(new Rectangle(10, 20, 100, 30), button.getModelBounds());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests for different variants of RootPanel styles and Button styles
  // Here is order of values in test method names:
  //   1) margin  (left, top, right, bottom);
  //   2) border  (left, top, right, bottom);
  //   3) padding (left, top, right, bottom);
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class StyleSet {
    private final String m_margin;
    private final String m_border;
    private final String m_padding;

    public StyleSet(String margin, String border, String padding) {
      m_margin = margin;
      m_border = border;
      m_padding = padding;
    }

    public StyleSet(String border, String margin) {
      this(border, margin, "0px");
    }
  }

  private void check_Button_on_RootPanel(StyleSet pStyle,
      StyleSet bStyle,
      String locationCode,
      Rectangle buttonModelBounds,
      Rectangle buttonParentBounds) throws Exception {
    // set CSS
    {
      setFileContentSrc(
          "test/public/Module.css",
          getSourceDQ(
              "body {",
              getSidesStyle(pStyle.m_margin, "margin"),
              getSidesStyle(pStyle.m_border, "border"),
              getSidesStyle(pStyle.m_padding, "padding"),
              "}",
              ".gwt-Button {",
              getSidesStyle(bStyle.m_margin, "margin"),
              getSidesStyle(bStyle.m_border, "border"),
              "}"));
      waitForAutoBuild();
      //System.out.println(getFileContentSrc("test/public/Module.css"));
    }
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      Button button = new Button();",
            "      button.setSize('100px', '50px');",
            "      rootPanel.add(button" + locationCode + ");",
            "    }",
            "  }",
            "}");
    frame.refresh();
    // check "button" coordinates
    WidgetInfo button = frame.getChildrenWidgets().get(0);
    assertEquals(buttonModelBounds, button.getModelBounds());
    assertEquals(buttonParentBounds, button.getBounds());
  }

  private void check_Button_on_innerPanel(StyleSet pStyle,
      StyleSet iStyle,
      StyleSet bStyle,
      String locationCode,
      Rectangle innerModelBounds,
      Rectangle innerParentBounds,
      Rectangle buttonModelBounds,
      Rectangle buttonParentBounds) throws Exception {
    // set CSS
    {
      String pMargin = getSidesStyle(pStyle.m_margin, "margin");
      String pBorder = getSidesStyle(pStyle.m_border, "border");
      String pPadding = getSidesStyle(pStyle.m_padding, "padding");
      String iMargin = getSidesStyle(iStyle.m_margin, "margin");
      String iBorder = getSidesStyle(iStyle.m_border, "border");
      String iPadding = getSidesStyle(iStyle.m_padding, "padding");
      String bMargin = getSidesStyle(bStyle.m_margin, "margin");
      String bBorder = getSidesStyle(bStyle.m_border, "border");
      setFileContentSrc(
          "test/public/Module.css",
          getSourceDQ(
              "body {",
              pMargin,
              pBorder,
              pPadding,
              "}",
              ".innerPanel {",
              iMargin,
              iBorder,
              iPadding,
              "}",
              ".gwt-Button {",
              bMargin,
              bBorder,
              "}"));
      //System.out.println(getFileContentSrc("test/public/Module.css"));
    }
    // parse
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "    {",
            "      AbsolutePanel innerPanel = new AbsolutePanel();",
            "      innerPanel.setStyleName('innerPanel');",
            "      innerPanel.setSize('200px', '200px');",
            "      rootPanel.add(innerPanel, 100, 100);",
            "      {",
            "        Button button = new Button();",
            "        button.setSize('100px', '50px');",
            "        innerPanel.add(button" + locationCode + ");",
            "      }",
            "    }",
            "  }",
            "}");
    //System.out.println(m_lastEditor.getSource());
    frame.refresh();
    // check "innerPanel" coordinates
    ComplexPanelInfo innerPanel = (ComplexPanelInfo) frame.getChildrenWidgets().get(0);
    assertEquals(innerModelBounds, innerPanel.getModelBounds());
    assertEquals(innerParentBounds, innerPanel.getBounds());
    // check "button" coordinates
    {
      WidgetInfo button = innerPanel.getChildrenWidgets().get(0);
      assertEquals(buttonModelBounds, button.getModelBounds());
      assertEquals(buttonParentBounds, button.getBounds());
    }
  }

  /**
   * For string like <code>"left 10px, top 20 px"</code> returns
   * 
   * <pre>
	 * 		border-left 10px;
	 * 		border-top 20px;
	 * </pre>
   */
  private static String getSidesStyle(String stylesString, String styleName) {
    String styles = "";
    String[] stylesParts = StringUtils.split(stylesString, ',');
    for (String stylePart : stylesParts) {
      stylePart = stylePart.trim();
      styles += "\t";
      if (stylePart.startsWith("top")
          || stylePart.startsWith("right")
          || stylePart.startsWith("bottom")
          || stylePart.startsWith("left")) {
        String side = StringUtils.substringBefore(stylePart, " ");
        String withoutSide = StringUtils.substringAfter(stylePart, " ");
        styles += styleName + "-" + side + ": " + withoutSide;
      } else {
        styles += styleName + ": " + stylePart;
      }
      styles += ";\n";
    }
    styles = StringUtils.chomp(styles, "\n");
    return styles;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Default Button location
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_defaultLocation___R_0p_0p_0p___B_0p_0p() throws Exception {
    Rectangle buttonModelBounds = new Rectangle(BBL + 0, BBT, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(BBL + 0, BBT, 100, 50);
    check_Button_on_RootPanel(
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid"),
        "",
        buttonModelBounds,
        buttonParentBounds);
  }

  public void test_defaultLocation___R_10p20p_0p_0p___B_0p_0p() throws Exception {
    Rectangle buttonModelBounds = new Rectangle(BBL + 10, BBT + 20, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(BBL + 10, BBT + 20, 100, 50);
    check_Button_on_RootPanel(
        new StyleSet("left 10px, top 20px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid"),
        "",
        buttonModelBounds,
        buttonParentBounds);
  }

  /**
   * "right" and "bottom" margins for parent don't change bounds for Button.
   */
  public void test_defaultLocation___R_10p20p1p2p_0p_0p___B_0p_0p() throws Exception {
    Rectangle buttonModelBounds = new Rectangle(BBL + 10, BBT + 20, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(BBL + 10, BBT + 20, 100, 50);
    check_Button_on_RootPanel(
        new StyleSet("left 10px, top 20px, right 1px, bottom 2px",
            "left 0px solid, top 0px solid",
            "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid"),
        "",
        buttonModelBounds,
        buttonParentBounds);
  }

  public void test_defaultLocation___R_0p_10p20p_0p___B_0p_0p() throws Exception {
    Rectangle buttonModelBounds = new Rectangle(BBL + 10, BBT + 20, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(BBL + 10, BBT + 20, 100, 50);
    check_Button_on_RootPanel(
        new StyleSet("left 0px, top 0px", "left 10px solid, top 20px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid"),
        "",
        buttonModelBounds,
        buttonParentBounds);
  }

  public void test_defaultLocation___R_e_10p20p_0p___B_0p_0p() throws Exception {
    Rectangle buttonModelBounds = new Rectangle(BBL + 10 + BM, BBT + 20 + BM, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(BBL + 10 + BM, BBT + 20 + BM, 100, 50);
    check_Button_on_RootPanel(
        new StyleSet("", "left 10px solid, top 20px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid"),
        "",
        buttonModelBounds,
        buttonParentBounds);
  }

  public void test_defaultLocation___R_0p_0p_10p20p___B_0p_0p() throws Exception {
    Rectangle buttonModelBounds = new Rectangle(BBL + 10, BBT + 20, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(BBL + 10, BBT + 20, 100, 50);
    check_Button_on_RootPanel(
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid", "left 10px, top 20px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid"),
        "",
        buttonModelBounds,
        buttonParentBounds);
  }

  public void test_defaultLocation___R_0p_0p_0p___B_10p20p_0p() throws Exception {
    Rectangle buttonModelBounds = new Rectangle(BBL + 0, BBT + 0, 110, 70);
    Rectangle buttonParentBounds = new Rectangle(BBL + 0, BBT + 0, 110, 70);
    check_Button_on_RootPanel(
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 10px, top 20px", "left 0px solid, top 0px solid"),
        "",
        buttonModelBounds,
        buttonParentBounds);
  }

  public void test_defaultLocation___R_0p_0p_0p___B_10p20p1p2p_0p() throws Exception {
    Rectangle buttonModelBounds = new Rectangle(BBL + 0, BBT + 0, 111, 72);
    Rectangle buttonParentBounds = new Rectangle(BBL + 0, BBT + 0, 111, 72);
    check_Button_on_RootPanel(new StyleSet("left 0px, top 0px",
        "left 0px solid, top 0px solid",
        "left 0px, top 0px"), new StyleSet("left 10px, top 20px, right 1px, bottom 2px",
        "left 0px solid, top 0px solid"), "", buttonModelBounds, buttonParentBounds);
  }

  public void test_defaultLocation___R_10p20p_0p_0p___B_1p2p_0p() throws Exception {
    Rectangle buttonModelBounds = new Rectangle(BBL + 10, BBT + 20, 101, 52);
    Rectangle buttonParentBounds = new Rectangle(BBL + 10, BBT + 20, 101, 52);
    check_Button_on_RootPanel(
        new StyleSet("left 10px, top 20px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 1px, top 2px", "left 0px solid, top 0px solid"),
        "",
        buttonModelBounds,
        buttonParentBounds);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // (5,3) as Button location
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_location_5p3p___R_0p_0p_0p___B_0p_0p() throws Exception {
    Rectangle buttonModelBounds = new Rectangle(5, 3, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(5, 3, 100, 50);
    check_Button_on_RootPanel(
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid"),
        ", 5, 3",
        buttonModelBounds,
        buttonParentBounds);
  }

  public void test_location_5p3p___R_10p20p_0p_0p___B_0p_0p() throws Exception {
    Rectangle buttonModelBounds = new Rectangle(5, 3, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(5, 3, 100, 50);
    check_Button_on_RootPanel(
        new StyleSet("left 10px, top 20px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid"),
        ", 5, 3",
        buttonModelBounds,
        buttonParentBounds);
  }

  public void test_location_5p3p___R_0p_10p20p_0p___B_0p_0p() throws Exception {
    Rectangle buttonModelBounds = new Rectangle(5, 3, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(5, 3, 100, 50);
    check_Button_on_RootPanel(
        new StyleSet("left 0px, top 0px", "left 10px solid, top 20px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid"),
        ", 5, 3",
        buttonModelBounds,
        buttonParentBounds);
  }

  public void test_location_5p3p___R_0p_0p_10p20p___B_0p_0p() throws Exception {
    Rectangle buttonModelBounds = new Rectangle(5, 3, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(5, 3, 100, 50);
    check_Button_on_RootPanel(
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid", "left 10px, top 20px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid"),
        ", 5, 3",
        buttonModelBounds,
        buttonParentBounds);
  }

  public void test_defaultLocation___R_0p_0p_0p___B_0p_10p20p() throws Exception {
    Rectangle buttonModelBounds = new Rectangle(BBL + 0, BBT + 0, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(BBL + 0, BBT + 0, 100, 50);
    check_Button_on_RootPanel(
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 10px solid, top 20px solid"),
        "",
        buttonModelBounds,
        buttonParentBounds);
  }

  public void test_location_5p3p___R_0p_0p_0p___B_10p20p_0p() throws Exception {
    Rectangle buttonModelBounds = new Rectangle(5, 3, 110, 70);
    Rectangle buttonParentBounds = new Rectangle(5, 3, 110, 70);
    check_Button_on_RootPanel(
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 10px, top 20px", "left 0px solid, top 0px solid"),
        ", 5, 3",
        buttonModelBounds,
        buttonParentBounds);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // With inner AbsolutePanel, default Button location
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_defaultLocation___R_0p_0p_0p___I_0p_0p_0p___B_0p_0p() throws Exception {
    Rectangle innerModelBounds = new Rectangle(100, 100, 200, 200);
    Rectangle innerParentBounds = new Rectangle(100, 100, 200, 200);
    Rectangle buttonModelBounds = new Rectangle(0, 0, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(0, 0, 100, 50);
    check_Button_on_innerPanel(
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid"),
        "",
        innerModelBounds,
        innerParentBounds,
        buttonModelBounds,
        buttonParentBounds);
  }

  public void test_defaultLocation___R_0p_0p_0p___I_10p20p_0p_0p___B_0p_0p() throws Exception {
    Rectangle innerModelBounds = new Rectangle(100, 100, 210, 220);
    Rectangle innerParentBounds = new Rectangle(100, 100, 210, 220);
    Rectangle buttonModelBounds = new Rectangle(0, 0, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(10, 20, 100, 50);
    check_Button_on_innerPanel(
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 10px, top 20px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid"),
        "",
        innerModelBounds,
        innerParentBounds,
        buttonModelBounds,
        buttonParentBounds);
  }

  /**
   * Note, that in "strict" mode border is included into bounds.
   */
  public void test_defaultLocation___R_0p_0p_0p___I_0p_10p20p_0p___B_0p_0p() throws Exception {
    int deltaW = strict ? 10 : 0;
    int deltaH = strict ? 20 : 0;
    Rectangle innerModelBounds = new Rectangle(100, 100, 200 + deltaW, 200 + deltaH);
    Rectangle innerParentBounds = new Rectangle(100, 100, 200 + deltaW, 200 + deltaH);
    Rectangle buttonModelBounds = new Rectangle(0, 0, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(10, 20, 100, 50);
    check_Button_on_innerPanel(
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 10px solid, top 20px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid"),
        "",
        innerModelBounds,
        innerParentBounds,
        buttonModelBounds,
        buttonParentBounds);
  }

  public void test_defaultLocation___R_0p_0p_0p___I_0p_0p_10p20p___B_0p_0p() throws Exception {
    int deltaW = strict ? 10 : 0;
    int deltaH = strict ? 20 : 0;
    Rectangle innerModelBounds = new Rectangle(100, 100, 200 + deltaW, 200 + deltaH);
    Rectangle innerParentBounds = new Rectangle(100, 100, 200 + deltaW, 200 + deltaH);
    Rectangle buttonModelBounds = new Rectangle(10, 20, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(10, 20, 100, 50);
    check_Button_on_innerPanel(
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid", "left 10px, top 20px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid"),
        "",
        innerModelBounds,
        innerParentBounds,
        buttonModelBounds,
        buttonParentBounds);
  }

  public void test_defaultLocation___R_10p20_0p_0p___I_0p_0p_0p___B_0p_0p() throws Exception {
    Rectangle innerModelBounds = new Rectangle(100, 100, 200, 200);
    Rectangle innerParentBounds = new Rectangle(100, 100, 200, 200);
    Rectangle buttonModelBounds = new Rectangle(0, 0, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(0, 0, 100, 50);
    check_Button_on_innerPanel(
        new StyleSet("left 10px, top 20px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid"),
        "",
        innerModelBounds,
        innerParentBounds,
        buttonModelBounds,
        buttonParentBounds);
  }

  public void test_defaultLocation___R_0p_10p20p_0p___I_0p_0p_0p___B_0p_0p() throws Exception {
    Rectangle innerModelBounds = new Rectangle(100, 100, 200, 200);
    Rectangle innerParentBounds = new Rectangle(100, 100, 200, 200);
    Rectangle buttonModelBounds = new Rectangle(0, 0, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(0, 0, 100, 50);
    check_Button_on_innerPanel(
        new StyleSet("left 0px, top 0px", "left 10px solid, top 20px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid"),
        "",
        innerModelBounds,
        innerParentBounds,
        buttonModelBounds,
        buttonParentBounds);
  }

  public void test_defaultLocation___R_0p_0p_10p20p___I_0p_0p_0p___B_0p_0p() throws Exception {
    Rectangle innerModelBounds = new Rectangle(100, 100, 200, 200);
    Rectangle innerParentBounds = new Rectangle(100, 100, 200, 200);
    Rectangle buttonModelBounds = new Rectangle(0, 0, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(0, 0, 100, 50);
    check_Button_on_innerPanel(
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid", "left 10px, top 20px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid"),
        "",
        innerModelBounds,
        innerParentBounds,
        buttonModelBounds,
        buttonParentBounds);
  }

  public void test_defaultLocation___R_0p_10p20p_0p___I_1p2p_0p_0p___B_0p_0p() throws Exception {
    Rectangle innerModelBounds = new Rectangle(100, 100, 201, 202);
    Rectangle innerParentBounds = new Rectangle(100, 100, 201, 202);
    Rectangle buttonModelBounds = new Rectangle(0, 0, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(1, 2, 100, 50);
    check_Button_on_innerPanel(
        new StyleSet("left 0px, top 0px", "left 10px solid, top 20px solid", "left 0px, top 0px"),
        new StyleSet("left 1px, top 2px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid"),
        "",
        innerModelBounds,
        innerParentBounds,
        buttonModelBounds,
        buttonParentBounds);
  }

  public void test_defaultLocation___R_0p_10p20p_0p___I_0p0p_1p2p_0p___B_0p_0p() throws Exception {
    int deltaW = strict ? 1 : 0;
    int deltaH = strict ? 2 : 0;
    Rectangle innerModelBounds = new Rectangle(100, 100, 200 + deltaW, 200 + deltaH);
    Rectangle innerParentBounds = new Rectangle(100, 100, 200 + deltaW, 200 + deltaH);
    Rectangle buttonModelBounds = new Rectangle(0, 0, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(1, 2, 100, 50);
    check_Button_on_innerPanel(
        new StyleSet("left 0px, top 0px", "left 10px solid, top 20px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 1px solid, top 2px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid"),
        "",
        innerModelBounds,
        innerParentBounds,
        buttonModelBounds,
        buttonParentBounds);
  }

  public void test_defaultLocation___R_0p_10p20p_0p___I_0p_0p_1p2p___B_0p_0p() throws Exception {
    int deltaW = strict ? 1 : 0;
    int deltaH = strict ? 2 : 0;
    Rectangle innerModelBounds = new Rectangle(100, 100, 200 + deltaW, 200 + deltaH);
    Rectangle innerParentBounds = new Rectangle(100, 100, 200 + deltaW, 200 + deltaH);
    Rectangle buttonModelBounds = new Rectangle(1, 2, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(1, 2, 100, 50);
    check_Button_on_innerPanel(
        new StyleSet("left 0px, top 0px", "left 10px solid, top 20px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid", "left 1px, top 2px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid"),
        "",
        innerModelBounds,
        innerParentBounds,
        buttonModelBounds,
        buttonParentBounds);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // With inner AbsolutePanel, (5,3) as Button location
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_location_5p3p___R_0p_0p_0p___I_0p_0p_0p___B_0p_0p() throws Exception {
    Rectangle innerModelBounds = new Rectangle(100, 100, 200, 200);
    Rectangle innerParentBounds = new Rectangle(100, 100, 200, 200);
    Rectangle buttonModelBounds = new Rectangle(5, 3, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(5, 3, 100, 50);
    check_Button_on_innerPanel(
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid"),
        ", 5, 3",
        innerModelBounds,
        innerParentBounds,
        buttonModelBounds,
        buttonParentBounds);
  }

  public void test_location_5p3p___R_0p_0p_0p___I_10p20p_0p_0p___B_0p_0p() throws Exception {
    Rectangle innerModelBounds = new Rectangle(100, 100, 210, 220);
    Rectangle innerParentBounds = new Rectangle(100, 100, 210, 220);
    Rectangle buttonModelBounds = new Rectangle(5, 3, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(15, 23, 100, 50);
    check_Button_on_innerPanel(
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 10px, top 20px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid"),
        ", 5, 3",
        innerModelBounds,
        innerParentBounds,
        buttonModelBounds,
        buttonParentBounds);
  }

  public void test_location_5p3p___R_0p_10p20p_0p___I_0p_0p_0p___B_0p_0p() throws Exception {
    int deltaW = strict ? 10 : 0;
    int deltaH = strict ? 20 : 0;
    Rectangle innerModelBounds = new Rectangle(100, 100, 200 + deltaW, 200 + deltaH);
    Rectangle innerParentBounds = new Rectangle(100, 100, 200 + deltaW, 200 + deltaH);
    Rectangle buttonModelBounds = new Rectangle(5, 3, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(15, 23, 100, 50);
    check_Button_on_innerPanel(
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 10px solid, top 20px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid"),
        ", 5, 3",
        innerModelBounds,
        innerParentBounds,
        buttonModelBounds,
        buttonParentBounds);
  }

  public void test_location_5p3p___R_0p_0p_10p20p___I_0p_0p_0p___B_0p_0p() throws Exception {
    int deltaW = strict ? 10 : 0;
    int deltaH = strict ? 20 : 0;
    Rectangle innerModelBounds = new Rectangle(100, 100, 200 + deltaW, 200 + deltaH);
    Rectangle innerParentBounds = new Rectangle(100, 100, 200 + deltaW, 200 + deltaH);
    Rectangle buttonModelBounds = new Rectangle(5, 3, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(5, 3, 100, 50);
    check_Button_on_innerPanel(
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid", "left 10px, top 20px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid"),
        ", 5, 3",
        innerModelBounds,
        innerParentBounds,
        buttonModelBounds,
        buttonParentBounds);
  }

  public void test_location_5p3p___R_10p20p_0p_0p___I_0p_0p_0p___B_0p_0p() throws Exception {
    Rectangle innerModelBounds = new Rectangle(100, 100, 200, 200);
    Rectangle innerParentBounds = new Rectangle(100, 100, 200, 200);
    Rectangle buttonModelBounds = new Rectangle(5, 3, 100, 50);
    Rectangle buttonParentBounds = new Rectangle(5, 3, 100, 50);
    check_Button_on_innerPanel(
        new StyleSet("left 10px, top 20px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid", "left 0px, top 0px"),
        new StyleSet("left 0px, top 0px", "left 0px solid, top 0px solid"),
        ", 5, 3",
        innerModelBounds,
        innerParentBounds,
        buttonModelBounds,
        buttonParentBounds);
  }
}