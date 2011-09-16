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
package com.google.gdt.eclipse.designer.core.refactoring;

import com.google.gdt.eclipse.designer.refactoring.EntryPointMoveParticipant;
import com.google.gdt.eclipse.designer.refactoring.EntryPointRenameParticipant;

import org.eclipse.wb.tests.designer.core.RefactoringTestUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;

import java.io.ByteArrayInputStream;

/**
 * Test <code>EntryPoint</code> related refactorings.
 * 
 * @author scheglov_ke
 */
public class EntryPointRefactoringTest extends AbstractRefactoringTest {
  private IType m_entryPointType;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m_entryPointType = m_testProject.getJavaProject().findType("test.client.Module");
  }

  @Override
  protected void tearDown() throws Exception {
    m_entryPointType = null;
    super.tearDown();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_delete() throws Exception {
    // do delete
    RefactoringTestUtils.deleteType(m_entryPointType);
    waitForAutoBuild();
    // checks after refactoring
    assertEquals(getDoubleQuotes2(new String[]{
        "<module>",
        "  <inherits name='com.google.gwt.user.User'/>",
        "</module>"}), getFileContentSrc("/test/Module.gwt.xml"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rename
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link EntryPointRenameParticipant}.<br>
   * Rename some class that is not an <code>EntryPoint</code>.
   */
  public void test_renameNotEntryPoint() throws Exception {
    IType typeToRename = createEmptyTestType();
    // rename - no change expected
    RefactoringTestUtils.renameType(typeToRename, "Test2");
    assertEquals(getDoubleQuotes2(new String[]{
        "<module>",
        "  <inherits name='com.google.gwt.user.User'/>",
        "  <entry-point class='test.client.Module'/>",
        "</module>"}), getFileContentSrc("/test/Module.gwt.xml"));
  }

  /**
   * Test for {@link EntryPointRenameParticipant}.
   */
  public void test_renameEntryPoint() throws Exception {
    RefactoringTestUtils.renameType(m_entryPointType, "MyModule");
    assertEquals(getDoubleQuotes2(new String[]{
        "<module>",
        "  <inherits name='com.google.gwt.user.User'/>",
        "  <entry-point class='test.client.MyModule'/>",
        "</module>"}), getFileContentSrc("/test/Module.gwt.xml"));
  }

  /**
   * Test for {@link EntryPointRenameParticipant}.
   */
  public void test_renameEntryPoint_notInModule() throws Exception {
    // set new Module.gwt.xml, that does not reference type test.client.Module
    String moduleContent =
        getDoubleQuotes2(new String[]{
            "<module>",
            "  <inherits name='com.google.gwt.user.User'/>",
            "</module>"});
    getFileSrc("/test/Module.gwt.xml").setContents(
        new ByteArrayInputStream(moduleContent.getBytes()),
        true,
        false,
        null);
    // rename - no change expected
    RefactoringTestUtils.renameType(m_entryPointType, "MyModule");
    assertEquals(moduleContent, getFileContentSrc("/test/Module.gwt.xml"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Move
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link EntryPointRenameParticipant}.<br>
   * Move some class that is not an <code>EntryPoint</code>.
   */
  public void test_moveNotEntryPoint() throws Exception {
    IFile moduleFile = getFileSrc("test/Module.gwt.xml");
    IType typeToMove = createEmptyTestType();
    String moduleFileContent = getFileContent(moduleFile);
    // move - no change expected
    IPackageFragment newPackage = m_testProject.getPackage("test.client.points");
    RefactoringTestUtils.moveType(typeToMove, newPackage);
    assertEquals(moduleFileContent, getFileContent(moduleFile));
  }

  /**
   * Test for {@link EntryPointMoveParticipant}.
   */
  public void test_moveEntryPoint() throws Exception {
    IPackageFragment newPackage = m_testProject.getPackage("test.client.points");
    RefactoringTestUtils.moveType(m_entryPointType, newPackage);
    assertEquals(getDoubleQuotes2(new String[]{
        "<module>",
        "  <inherits name='com.google.gwt.user.User'/>",
        "  <entry-point class='test.client.points.Module'/>",
        "</module>"}), getFileContentSrc("/test/Module.gwt.xml"));
  }
}