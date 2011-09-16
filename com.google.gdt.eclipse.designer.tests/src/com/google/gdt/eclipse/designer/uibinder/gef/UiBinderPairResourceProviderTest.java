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
package com.google.gdt.eclipse.designer.uibinder.gef;

import com.google.gdt.eclipse.designer.core.GTestUtils;
import com.google.gdt.eclipse.designer.uibinder.editor.UiBinderPairResourceProvider;
import com.google.gdt.eclipse.designer.uibinder.model.UiBinderModelTest;

import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import org.eclipse.core.resources.IFile;

/**
 * Test for {@link UiBinderPairResourceProvider}.
 * 
 * @author scheglov_ke
 */
public class UiBinderPairResourceProviderTest extends UiBinderModelTest {
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
  public void test_unknownExtension() throws Exception {
    IFile file = setFileContentSrc("test/Test.foo", "");
    assertEquals(null, getPair(file));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ui.xml -> Java
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_toJava_sameName() throws Exception {
    IFile javaFile = getFileSrc("test/client/Test.java");
    IFile uiFile = setFileContentSrc("test/client/Test.ui.xml", getSource(""));
    //
    assertEquals(javaFile, getPair(uiFile));
  }

  public void test_toJava_no() throws Exception {
    IFile uiFile = setFileContentSrc("test/client/Test2.ui.xml", getSource(""));
    //
    assertEquals(null, getPair(uiFile));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Java -> ui.xml
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_toUI_sameName() throws Exception {
    IFile javaFile = getFileSrc("test/client/Test.java");
    IFile uiFile = setFileContentSrc("test/client/Test.ui.xml", getSource(""));
    //
    assertEquals(uiFile, getPair(javaFile));
  }

  public void test_toUI_no() throws Exception {
    IFile javaFile = setFileContentSrc("test/client/Test.java", "");
    //
    assertEquals(null, getPair(javaFile));
  }

  @DisposeProjectAfter
  public void test_toUI_maven() throws Exception {
    GTestUtils.configureMavenProject();
    // prepare files
    IFile javaFile =
        setFileContent(
            "src/main/java/test/client/Test.java",
            getJavaSource(
                "public class Test extends Composite {",
                "  interface Binder extends UiBinder<Widget, Test> {}",
                "  private static final Binder binder = GWT.create(Binder.class);",
                "  public Test() {",
                "    initWidget(binder.createAndBindUi(this));",
                "  }",
                "}"));
    IFile uiFile =
        setFileContent(
            "src/main/resources/test/client/Test.ui.xml",
            getTestSource(
                "// filler filler filler filler filler",
                "// filler filler filler filler filler",
                "<ui:UiBinder>",
                "  <g:FlowPanel/>",
                "</ui:UiBinder>"));
    // do check
    assertEquals(uiFile, getPair(javaFile));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static IFile getPair(IFile file) {
    return UiBinderPairResourceProvider.INSTANCE.getPair(file);
  }
}