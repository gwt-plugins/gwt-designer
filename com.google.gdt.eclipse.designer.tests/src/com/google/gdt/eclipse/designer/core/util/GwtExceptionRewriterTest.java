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

import com.google.gdt.eclipse.designer.GwtExceptionRewriter;
import com.google.gdt.eclipse.designer.IExceptionConstants;
import com.google.gdt.eclipse.designer.core.model.widgets.WidgetTest;
import com.google.gdt.eclipse.designer.hosted.HostedModeException;

import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

/**
 * Test for {@link GwtExceptionRewriter}.
 * 
 * @see {@link WidgetTest}
 * 
 * @author scheglov_ke
 * @author mitin_aa
 */
public class GwtExceptionRewriterTest extends DesignerTestCase {
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
  public void test_RuntimeException_withCause() throws Exception {
    Throwable e = new Exception();
    Throwable wrapped = new RuntimeException(e);
    Throwable result = GwtExceptionRewriter.INSTANCE.rewrite(wrapped);
    assertSame(e, result);
  }

  public void test_RuntimeException_withoutCause() throws Exception {
    Throwable e = new RuntimeException();
    Throwable result = GwtExceptionRewriter.INSTANCE.rewrite(e);
    assertSame(e, result);
  }

  public void test_genericException() throws Exception {
    Throwable e = new Exception();
    Throwable result = GwtExceptionRewriter.INSTANCE.rewrite(e);
    assertSame(e, result);
  }

  public void test_ExceptionInInitializerError() throws Exception {
    Throwable e = new Exception();
    Throwable wrapped = new ExceptionInInitializerError(e);
    Throwable result = GwtExceptionRewriter.INSTANCE.rewrite(wrapped);
    assertSame(e, result);
  }

  public void test_HostedModeException() throws Exception {
    int code = 123;
    Exception nested = new Exception();
    String[] parameters = new String[]{"a", "b", "c"};
    Throwable e = new HostedModeException(code, nested, parameters);
    DesignerException result = (DesignerException) GwtExceptionRewriter.INSTANCE.rewrite(e);
    assertEquals(code, result.getCode());
    assertSame(nested, result.getCause());
    assertSame(parameters, result.getParameters());
  }

  /**
   * GWT 2.2 breaks binary compatibility.
   */
  public void test_IncompatibleClassChangeError_JClassType() throws Exception {
    Throwable e =
        new IncompatibleClassChangeError("Found interface com.google.gwt.core.ext.typeinfo.JClassType, but class was expected");
    DesignerException result = (DesignerException) GwtExceptionRewriter.INSTANCE.rewrite(e);
    assertEquals(IExceptionConstants.BINARY_INCOMPAT_GWT22, result.getCode());
  }

  /**
   * We often see {@link NullPointerException} in
   * <code>com.google.gwt.dev.javac.CompiledClass</code>, so we need special message.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.asp?47895
   */
  public void test_NPE_CompiledClass() throws Exception {
    String key = "wbp.GWT_ExceptionRewriter.simulate.CompiledClass";
    try {
      System.setProperty(key, "true");
      Throwable e = new NullPointerException();
      DesignerException result = (DesignerException) GwtExceptionRewriter.INSTANCE.rewrite(e);
      assertEquals(IExceptionConstants.NPE_IN_COMPILED_CLASS, result.getCode());
    } finally {
      System.clearProperty(key);
    }
  }
}