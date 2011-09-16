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
package com.google.gdt.eclipse.designer.core.model;

import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;
import com.google.gdt.eclipse.designer.parser.ParseFactory;

import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

/**
 * Disposes {@link GwtState} for Java.
 * 
 * @author scheglov_ke
 */
public class DisposeStateJavaTest extends DesignerTestCase {
  public void test_doDispose() throws Exception {
    ParseFactory.disposeSharedGWTState();
  }
}