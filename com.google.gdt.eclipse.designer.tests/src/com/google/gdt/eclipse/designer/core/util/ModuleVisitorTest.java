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
package com.google.gdt.eclipse.designer.core.util;

import com.google.gdt.eclipse.designer.IExceptionConstants;
import com.google.gdt.eclipse.designer.core.GTestUtils;
import com.google.gdt.eclipse.designer.model.module.ModuleElement;
import com.google.gdt.eclipse.designer.util.DefaultModuleDescription;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.ModuleVisitor;

import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import org.eclipse.core.resources.IFile;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Test for {@link ModuleVisitor}.
 * 
 * @author scheglov_ke
 */
public class ModuleVisitorTest extends AbstractJavaTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    if (m_testProject == null) {
      do_projectCreate();
      GTestUtils.configure(m_testProject);
      GTestUtils.createModule(m_testProject, "test.Module");
      waitForAutoBuild();
    }
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
  /**
   * Test for using default {@link ModuleVisitor} implementation. No any action expected.
   */
  public void test_defaultFlow() throws Exception {
    ModuleDescription moduleDescription = getModuleDescription();
    ModuleVisitor.accept(moduleDescription, new ModuleVisitor() {
    });
  }

  /**
   * Test that we visit all required modules, source and public folders.
   */
  public void test_normalFlow() throws Exception {
    ModuleDescription moduleDescription = getModuleDescription();
    final StringBuilder buffer = new StringBuilder();
    ModuleVisitor.accept(moduleDescription, new ModuleVisitor() {
      @Override
      public boolean visitModule(ModuleElement module) {
        String moduleName = module.getName();
        if ("test.Module".equals(moduleName)) {
          buffer.append("|+project module|");
        }
        if ("com.google.gwt.user.User".equals(moduleName)) {
          buffer.append("|+std User module|");
        }
        if ("com.google.gwt.core.Core".equals(moduleName)) {
          buffer.append("|+std Core module|");
        }
        return true;
      }

      @Override
      public void endVisitModule(ModuleElement module) {
        String moduleName = module.getName();
        if ("test.Module".equals(moduleName)) {
          buffer.append("|-project module|");
        }
        if ("com.google.gwt.user.User".equals(moduleName)) {
          buffer.append("|-std User module|");
        }
        if ("com.google.gwt.core.Core".equals(moduleName)) {
          buffer.append("|-std Core module|");
        }
      }

      @Override
      public void visitSourcePackage(ModuleElement module, String packageName, boolean superSource)
          throws Exception {
        String packageSignature = module.getName() + " " + packageName + " " + superSource;
        if ("test.Module test.client false".equals(packageSignature)) {
          buffer.append("|project module source|");
        }
        if ("com.google.gwt.user.User com.google.gwt.user.client false".equals(packageSignature)) {
          buffer.append("|std Core module source|");
        }
        if ("com.google.gwt.emul.Emulation com.google.gwt.emul true".equals(packageSignature)) {
          buffer.append("|std Emulation super-source|");
        }
      }

      @Override
      public void visitPublicPackage(ModuleElement module, String packageName) throws Exception {
        String packageSignature = module.getName() + " " + packageName;
        if ("test.Module test.public".equals(packageSignature)) {
          buffer.append("|project module public|");
        }
        if ("com.google.gwt.user.User com.google.gwt.user.public".equals(packageSignature)) {
          buffer.append("|std Core module public|");
        }
      }
    });
    String output = buffer.toString();
    // enter modules
    assertThat(output).contains("|+project module|");
    assertThat(output).contains("|+std User module|");
    assertThat(output).contains("|+std Core module|");
    // leave modules
    assertThat(output).contains("|-project module|");
    assertThat(output).contains("|-std User module|");
    assertThat(output).contains("|-std Core module|");
    // source packages
    assertThat(output).contains("|project module source|");
    assertThat(output).contains("|std Core module source|");
    assertThat(output).contains("|std Emulation super-source|");
    // public packages
    assertThat(output).contains("|project module public|");
    assertThat(output).contains("|std Core module public|");
  }

  public void test_dontEnterModule() throws Exception {
    ModuleDescription moduleDescription = getModuleDescription();
    ModuleVisitor.accept(moduleDescription, new ModuleVisitor() {
      @Override
      public boolean visitModule(ModuleElement module) {
        if ("test.Module".equals(module.getName())) {
          return false;
        }
        return true;
      }

      @Override
      public void endVisitModule(ModuleElement module) {
        assertFalse("test.Module".equals(module.getName()));
      }
    });
  }

  @DisposeProjectAfter
  public void test_invalidInherits_noSuchName() throws Exception {
    setFileContent(
        getModuleFile(),
        getSourceDQ(
            "<!-- filler filler filler filler filler -->",
            "<module>",
            "  <inherits name='no.such.Module'/>",
            "</module>"));
    ModuleDescription moduleDescription = getModuleDescription();
    waitForAutoBuild();
    // try to visit, exception expected
    try {
      ModuleVisitor.accept(moduleDescription, new ModuleVisitor() {
        @Override
        public boolean visitModule(ModuleElement module) {
          assertFalse("no.such.Module".equals(module.getName()));
          return true;
        }
      });
      fail();
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.NO_MODULE, e.getCode());
    }
  }

  @DisposeProjectAfter
  public void test_invalidInherits_noNameAttribute() throws Exception {
    setFileContent(
        getModuleFile(),
        getSourceDQ(
            "<!-- filler filler filler filler filler -->",
            "<!-- filler filler filler filler filler -->",
            "<module>",
            "  <inherits/>",
            "</module>"));
    ModuleDescription moduleDescription = getModuleDescription();
    waitForAutoBuild();
    // try to visit, exception expected
    try {
      ModuleVisitor.accept(moduleDescription, new ModuleVisitor() {
      });
      fail();
    } catch (DesignerException e) {
      assertEquals(IExceptionConstants.INHERITS_NO_NAME, e.getCode());
    }
  }

  /**
   * Sometimes Eclipse is configured to not copy XML and HTML files into output folder (bin). So, we
   * should be able to visit without these files in output folder.
   * <p>
   * See 40193: Can not find GWT module http://fogbugz.instantiations.com/fogbugz/default.php?40193
   */
  @DisposeProjectAfter
  public void test_withoutModuleFile_inClassOutputFolder() throws Exception {
    // delete module files from output
    {
      IFile moduleFile_inOutput = m_testProject.getProject().getFile("bin/test/Module.gwt.xml");
      assertTrue(moduleFile_inOutput.exists());
      moduleFile_inOutput.delete(true, null);
    }
    // try to visit, no exceptions
    ModuleDescription moduleDescription = getModuleDescription();
    ModuleVisitor.accept(moduleDescription, new ModuleVisitor() {
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private IFile getModuleFile() throws Exception {
    return getFileSrc("test/Module.gwt.xml");
  }

  private ModuleDescription getModuleDescription() throws Exception {
    IFile moduleFile = getModuleFile();
    return new DefaultModuleDescription(moduleFile);
  }
}