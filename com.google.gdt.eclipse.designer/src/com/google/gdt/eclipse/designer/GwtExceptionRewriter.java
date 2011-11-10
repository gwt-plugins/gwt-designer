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
package com.google.gdt.eclipse.designer;

import com.google.gdt.eclipse.designer.hosted.HostedModeException;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;

import org.eclipse.wb.core.editor.errors.IExceptionRewriter;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * {@link IExceptionRewriter} for GWT exceptions.
 * 
 * @author scheglov_ke
 * @coverage gwt
 */
public class GwtExceptionRewriter implements IExceptionRewriter {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IExceptionRewriter INSTANCE = new GwtExceptionRewriter();

  private GwtExceptionRewriter() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IExceptionRewriter
  //
  ////////////////////////////////////////////////////////////////////////////
  public Throwable rewrite(Throwable e) {
    // unwrap
    while (e.getClass() == RuntimeException.class
        || e.getClass() == InvocationTargetException.class) {
      Throwable cause = e.getCause();
      if (cause != null) {
        e = cause;
      } else {
        break;
      }
    }
    // unwrap
    if (e instanceof ExceptionInInitializerError) {
      e = e.getCause();
    }
    // UnableToCompleteException
    if ("com.google.gwt.core.ext.UnableToCompleteException".equals(e.getClass().getName())) {
      String messages = GwtState.getLoggerErrorMessages();
      return new DesignerException(HostedModeException.MODULE_LOADING_ERROR2,
          e,
          new String[]{messages});
    }
    // HostedModeException
    if (e instanceof HostedModeException) {
      HostedModeException hme = (HostedModeException) e;
      return new DesignerException(hme.getCode(), e.getCause(), hme.getParameters());
    }
    // NPE in com.google.gwt.dev.javac.CompiledClass.<init>
    if (e instanceof NullPointerException) {
      StackTraceElement element = e.getStackTrace()[0];
      if (element.getClassName().equals("com.google.gwt.dev.javac.CompiledClass")
          && element.getMethodName().equals("<init>")
          || System.getProperty("wbp.GWT_ExceptionRewriter.simulate.CompiledClass") != null) {
        return new DesignerException(IExceptionConstants.NPE_IN_COMPILED_CLASS, e);
      }
    }
    // IncompatibleClassChangeError for GWT 2.2
    {
      Throwable rootException = DesignerExceptionUtils.getRootCause(e);
      if (rootException instanceof IncompatibleClassChangeError) {
        String message = rootException.getMessage();
        if (message != null && message.contains("com.google.gwt.core.ext.typeinfo.JClassType")) {
          return new DesignerException(IExceptionConstants.BINARY_INCOMPAT_GWT22, e);
        }
      }
    }
    // use as is
    return e;
  }
}
