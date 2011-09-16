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
package com.google.gdt.eclipse.designer.core.model.widgets;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.module.ModuleElement;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.support.CssSupport;
import com.google.gdt.eclipse.designer.util.DefaultModuleProvider;
import com.google.gdt.eclipse.designer.util.DefaultModuleProvider.ModuleModification;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import org.eclipse.core.resources.IFile;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

/**
 * Tests for {@link CssSupport}.
 * 
 * @author scheglov_ke
 */
public class CssSupportTest extends GwtModelTest {
  @DisposeProjectAfter
  public void test_XXX() throws Exception {
    dontUseSharedGWTState();
    setFileContent(
        "war/Module.css",
        getSourceDQ(
            "/* filler filler filler filler filler */",
            "/* filler filler filler filler filler */",
            "/* filler filler filler filler filler */",
            ".myPanel {",
            "  background: ivory;",
            "}",
            ".test {",
            "  width: 500px;",
            "}",
            ""));
    waitForAutoBuild();
    // add sub/My.css into module
    {
      ModuleDescription module = Utils.getModule(m_javaProject, "test.Module");
      DefaultModuleProvider.modify(module, new ModuleModification() {
        public void modify(ModuleElement moduleElement) throws Exception {
          moduleElement.addStylesheetElement("sub/My.css");
        }
      });
    }
    // parse
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    setStyleName('myPanel');",
        "    {",
        "      Button button = new Button();",
        "      button.setStyleName('test');",
        "      add(button);",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    //
    CssSupport cssSupport = button.getState().getCssSupport();
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
  @DisposeProjectAfter
  public void test_getters() throws Exception {
    dontUseSharedGWTState();
    setFileContent(
        "war/Module.css",
        getSourceDQ(
            "/* filler filler filler filler filler */",
            "/* filler filler filler filler filler */",
            "/* filler filler filler filler filler */"));
    setFileContent(
        "src/test/public/sub/My.css",
        getSourceDQ(
            "/* filler filler filler filler filler */",
            "/* filler filler filler filler filler */",
            "/* filler filler filler filler filler */"));
    waitForAutoBuild();
    // add sub/My.css into module
    {
      ModuleDescription module = Utils.getModule(m_javaProject, "test.Module");
      DefaultModuleProvider.modify(module, new ModuleModification() {
        public void modify(ModuleElement moduleElement) throws Exception {
          moduleElement.addStylesheetElement("sub/My.css");
        }
      });
    }
    // parse
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      button.setStyleName('test');",
        "      add(button);",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    //
    CssSupport cssSupport = button.getState().getCssSupport();
    {
      List<String> resources = cssSupport.getResources();
      assertThat(resources).containsExactly("sub/My.css", "Module.css");
    }
    {
      List<IFile> files = cssSupport.getFiles();
      assertThat(files).containsExactly(
          getFile("src/test/public/sub/My.css"),
          getFile("war/Module.css"));
    }
  }

  @DisposeProjectAfter
  public void test_reparseOnCss() throws Exception {
    dontUseSharedGWTState();
    parseJavaInfo(
        "public class Test extends FlowPanel {",
        "  public Test() {",
        "    {",
        "      Button button = new Button();",
        "      button.setStyleName('test');",
        "      add(button);",
        "    }",
        "  }",
        "}");
    refresh();
    WidgetInfo button = getJavaInfoByName("button");
    assertThat(button.getBounds().width).isLessThan(200);
    // initially not modified
    assertFalse(button.getState().isModified());
    // update CSS
    setFileContent(
        "war/Module.css",
        getSourceDQ(
            "/* filler filler filler filler filler */",
            "/* filler filler filler filler filler */",
            "/* filler filler filler filler filler */",
            ".test {",
            "  width: 500px;",
            "}"));
    waitForAutoBuild();
    // now CSS modified
    assertTrue(button.getState().isModified());
    // do refresh, CSS applied
    refresh();
    assertThat(button.getBounds().width).isEqualTo(500);
  }
  // XXX
}