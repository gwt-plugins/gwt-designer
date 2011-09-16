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
package com.google.gdt.eclipse.designer.gxt.model.layout;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.internal.core.model.generation.statement.AbstractInsideStatementGenerator;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGenerator;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.Block;

/**
 * {@link StatementGenerator} for virtual {@link LayoutDataInfo}.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model.layout
 */
public final class VirtualLayoutDataStatementGenerator extends AbstractInsideStatementGenerator {
  public static final StatementGenerator INSTANCE = new VirtualLayoutDataStatementGenerator();

  ////////////////////////////////////////////////////////////////////////////
  //
  // StatementGenerator
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void add(JavaInfo child, StatementTarget target, Association association) throws Exception {
    // prepare block
    Block block = (Block) child.getEditor().addStatement(ImmutableList.of("{", "}"), target);
    // add statements in block
    target = new StatementTarget(block, true);
    add(child, target, null, association);
  }
}
