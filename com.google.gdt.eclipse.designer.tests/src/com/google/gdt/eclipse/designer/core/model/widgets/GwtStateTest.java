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

import com.google.gdt.eclipse.designer.IExceptionConstants;
import com.google.gdt.eclipse.designer.core.GTestUtils;
import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;
import com.google.gdt.eclipse.designer.util.ModuleDescription;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import org.eclipse.core.resources.IFile;

/**
 * Some tests for {@link GwtState}.
 * 
 * @author scheglov_ke
 */
public class GwtStateTest extends GwtModelTest {
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
  public void test_noModuleFile() throws Exception {
    ModuleDescription moduleDescription = null;
    try {
      new GwtState(null, moduleDescription);
      fail();
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.NO_MODULE_FILE, e.getCode());
    }
  }

  @DisposeProjectAfter
  public void test_noRequiredModule() throws Exception {
    IFile moduleFile = getTestModuleFile();
    ModuleDescription moduleDescription = getTestModuleDescription();
    setFileContent(
        moduleFile,
        getSourceDQ(
            "<!-- filler filler filler filler filler -->",
            "<!-- filler filler filler filler filler -->",
            "<module>",
            "  <inherits name='no.such.Module'/>",
            "</module>"));
    GwtState state = new GwtState(null, moduleDescription);
    // initialize() will fail, but dispose() should not
    try {
      state.initialize();
    } catch (Throwable e) {
      state.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getValuePx()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getValuePx() throws Exception {
    assertEquals(0, GwtState.getValuePx(null));
    assertEquals(0, GwtState.getValuePx("25"));
    assertEquals(25, GwtState.getValuePx("25px"));
    assertEquals(5, GwtState.getValuePx("5.6333px"));
    assertEquals(0, GwtState.getValuePx("-bad-px"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isStrictMode()
  //
  ////////////////////////////////////////////////////////////////////////////
  @DisposeProjectAfter
  public void test_isStrictMode_false() throws Exception {
    setFileContent("war/Module.html", "<html/>");
    check_isStrictMode(false);
  }

  @DisposeProjectAfter
  public void test_isStrictMode_defaultTrue() throws Exception {
    getFile("war/Module.html").delete(true, null);
    check_isStrictMode(true);
  }

  @DisposeProjectAfter
  public void test_isStrictMode_true() throws Exception {
    setFileContent("war/Module.html", getSourceDQ("<!doctype html>", "<html/>"));
    check_isStrictMode(true);
  }

  @DisposeProjectAfter
  public void test_isStrictMode_true2() throws Exception {
    setFileContent(
        "war/Module.html",
        getSourceDQ("<!doctype html PUBLIC '-//W3C//DTD HTML 4.01//EN'>", "<html/>"));
    check_isStrictMode(true);
  }

  private void check_isStrictMode(boolean expected) throws Exception {
    dontUseSharedGWTState();
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    assertEquals(expected, frame.getState().isStrictMode());
    // versions map
    assertEquals(expected, m_lastState.getVersions().get("gwt_isStrictMode"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Default "locale" property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * To prevent generation of "default" locale (so reduce number of permutations) users override
   * default value for property "locale" to some specific value, usually "en". So, we can not use
   * "default" and should analyze modules to get default "locale".
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47884
   */
  @DisposeProjectAfter
  public void test_overrideDefaultLocale_doOverride() throws Exception {
    IFile moduleFile = getFileSrc("test/Module.gwt.xml");
    setFileContent(
        moduleFile,
        getSourceDQ(
            "<!-- filler filler filler filler filler -->",
            "<module>",
            "  <inherits name='com.google.gwt.user.User'/>",
            "  <inherits name='com.google.gwt.i18n.I18N'/>",
            "  <extend-property name='locale' values='en,es'/>",
            "  <set-property name='locale' value='en,es'/>",
            "  <set-property-fallback name='locale' value='en'/>",
            "</module>"));
    // prepare for parsing
    waitForAutoBuild();
    dontUseSharedGWTState();
    // parsing should be successful
    parseJavaInfo(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "  }",
        "}");
  }

  /**
   * Same as {@link #test_overrideDefaultLocale_doOverride()}, but override not in module itself,
   * but in required "library" module.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47884
   */
  @DisposeProjectAfter
  public void test_overrideDefaultLocale_doOverride_inLibraryModule() throws Exception {
    // prepare module "the.Library"
    {
      IFile libraryModuleFile = GTestUtils.createModule(m_testProject, "the.Library");
      setFileContent(
          libraryModuleFile,
          getSourceDQ(
              "<!-- filler filler filler filler filler -->",
              "<module>",
              "  <inherits name='com.google.gwt.i18n.I18N'/>",
              "  <extend-property name='locale' values='en,es'/>",
              "  <set-property name='locale' value='en,es'/>",
              "  <set-property-fallback name='locale' value='en'/>",
              "</module>"));
    }
    // configure module
    IFile moduleFile = getFileSrc("test/Module.gwt.xml");
    setFileContent(
        moduleFile,
        getSourceDQ(
            "<!-- filler filler filler filler filler -->",
            "<module>",
            "  <inherits name='com.google.gwt.user.User'/>",
            "  <inherits name='the.Library'/>",
            "</module>"));
    // prepare for parsing
    waitForAutoBuild();
    dontUseSharedGWTState();
    // parsing should be successful
    parseJavaInfo(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Coordinates
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link GwtState#getAbsoluteBounds(Object)}
   */
  public void test_getAbsoluteBounds_null() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    GwtState state = frame.getState();
    //
    Rectangle bounds = state.getAbsoluteBounds(null);
    assertEquals(new Rectangle(), bounds);
  }

  /**
   * Test for {@link GwtState#getModelBounds(Object)}
   */
  public void test_getModelBounds_null() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    GwtState state = frame.getState();
    //
    Rectangle bounds = state.getModelBounds(null);
    assertEquals(new Rectangle(), bounds);
  }
}