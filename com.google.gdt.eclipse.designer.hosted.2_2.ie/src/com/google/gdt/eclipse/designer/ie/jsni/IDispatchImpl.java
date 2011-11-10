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
package com.google.gdt.eclipse.designer.ie.jsni;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.swt.internal.ole.win32.COM;
import org.eclipse.swt.internal.ole.win32.COMObject;
import org.eclipse.swt.internal.ole.win32.DISPPARAMS;
import org.eclipse.swt.internal.ole.win32.GUID;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.Variant;

import com.google.gdt.eclipse.designer.ie.util.Utils;
import com.google.gwt.dev.shell.designtime.DispatchIdOracle;
import com.google.gwt.dev.shell.designtime.MethodAdaptor;
import com.google.gwt.dev.shell.designtime.ModuleSpace;

/**
 * Basic IDispatch implementation for use by {@link com.google.gwt.shell.ie.IDispatchProxy} and
 * {@link com.google.gwt.shell.ie.IDispatchStatic}.
 */
public abstract class IDispatchImpl extends COMObject {
  /**
   * An exception for wrapping bad HR's.
   */
  public static class HResultException extends Exception {
    private final int hr;
    private final String source;

    /**
     * Constructs a standard bad HR exception.
     */
    public HResultException(int hr) {
      super(Integer.toString(hr));
      this.hr = hr;
      source = "Java";
    }

    /**
     * Constructs a DISP_E_EXCEPTION bad HR.
     */
    public HResultException(String message) {
      super(message);
      hr = COM.DISP_E_EXCEPTION;
      source = "Java";
    }

    /**
     * Constructs a DISP_E_EXCEPTION bad HR.
     */
    public HResultException(Throwable e) {
      super(getStackTraceAsString(e), e);
      hr = COM.DISP_E_EXCEPTION;
      source = "Java";
    }

    /**
     * If the HR is DISP_E_EXCEPTION, this method will fill in the EXCEPINFO structure. Otherwise,
     * it does nothing.
     */
    public void fillExcepInfo(int pExcepInfo) {
      if (hr == COM.DISP_E_EXCEPTION) {
        String desc = getMessage();
        // 0: wCode (size = 2)
        // 4: bstrSource (size = 4)
        // 8: bstrDescription (size = 4)
        // 28: scode (size = 4)
        //
        OS.MoveMemory(pExcepInfo + 0, new short[]{(short) hr}, 2);
        if (source != null && source.length() != 0) {
          int bstrSource = SwtOleGlue.sysAllocString(source);
          OS.MoveMemory(pExcepInfo + 4, new int[]{bstrSource}, 4);
        }
        if (desc != null && desc.length() != 0) {
          int bstrDesc = SwtOleGlue.sysAllocString(desc);
          OS.MoveMemory(pExcepInfo + 8, new int[]{bstrDesc}, 4);
        }
        OS.MoveMemory(pExcepInfo + 28, new int[]{0}, 4);
      }
    }

    /**
     * Gets the HR.
     */
    public int getHResult() {
      return hr;
    }
  }

  // This one isn't defined in SWT for some reason.
  protected static final int DISP_E_UNKNOWNNAME = 0x80020006;

  protected static Variant callMethod(ClassLoader cl,
      DispatchIdOracle ora,
      Object jthis,
      Variant[] params,
      MethodAdaptor method) throws InstantiationException, InvocationTargetException,
      HResultException {
    // TODO: make sure we have enough args! It's okay if there are too many.
    Object[] javaParams =
        SwtOleGlue.convertVariantsToObjects(
          cl,
          method.getParameterTypes(),
          params,
          "Calling method '" + method.getName() + "'");
    Object result = null;
    try {
      try {
        result = method.invoke(jthis, javaParams);
      } catch (IllegalAccessException e) {
        // should never, ever happen
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    } catch (NullPointerException e) {
      /*
       * The JavaScript expected the method to be static, having forgotten an
       * instance reference (most often "this.").
       */
      StringBuffer sb = new StringBuffer();
      sb.append("Instance method '");
      sb.append(method.getName());
      sb.append("' needed a qualifying instance ");
      sb.append("(did you forget to prefix the call with 'this.'?)");
      throw new HResultException(sb.toString());
    } finally {
      for (int i = 0; i < javaParams.length; i++) {
        if (javaParams[i] instanceof OleAutomation) {
          OleAutomation tmp = (OleAutomation) javaParams[i];
          tmp.dispose();
        }
      }
    }
    return SwtOleGlue.convertObjectToVariant(cl, ora, method.getReturnType(), result);
  }

  protected int refCount;

  public IDispatchImpl() {
    super(new int[]{2, 0, 0, 1, 3, 5, 8});
  }

  // CHECKSTYLE_OFF
  public int AddRef() {
    return ++refCount;
  }

  // CHECKSTYLE_ON
  @Override
  public int method0(int[] args) {
    return QueryInterface(args[0], args[1]);
  }

  @Override
  public int method1(int[] args) {
    return AddRef();
  }

  // method3 GetTypeInfoCount - not implemented
  // method4 GetTypeInfo - not implemented
  @Override
  public int method2(int[] args) {
    return Release();
  }

  @Override
  public int method5(int[] args) {
    return GetIDsOfNames(args[0], args[1], args[2], args[3], args[4]);
  }

  @Override
  public int method6(int[] args) {
    return Invoke(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7]);
  }

  // CHECKSTYLE_OFF
  public int QueryInterface(int riid, int ppvObject) {
    if (riid == 0 || ppvObject == 0) {
      return COM.E_NOINTERFACE;
    }
    GUID guid = new GUID();
    COM.MoveMemory(guid, riid, GUID.sizeof);
    if (COM.IsEqualGUID(guid, COM.IIDIUnknown)) {
      OS.MoveMemory(ppvObject, new int[]{getAddress()}, 4);
      AddRef();
      return COM.S_OK;
    }
    if (COM.IsEqualGUID(guid, COM.IIDIDispatch)) {
      OS.MoveMemory(ppvObject, new int[]{getAddress()}, 4);
      AddRef();
      return COM.S_OK;
    }
    OS.MoveMemory(ppvObject, new int[]{0}, 4);
    return COM.E_NOINTERFACE;
  }

  public int Release() {
    if (--refCount == 0) {
      dispose();
    }
    return refCount;
  }

  // CHECKSTYLE_ON
  /**
   * Override this method to implement GetIDsOfNames().
   */
  protected abstract void getIDsOfNames(String[] names, int[] ids) throws HResultException;

  /**
   * Override this method to implement Invoke().
   */
  protected abstract Variant invoke(int dispId, int flags, Variant[] params)
      throws HResultException, InstantiationException, InvocationTargetException;

  private Variant[] extractVariantArrayFromDispParamsPtr(int pDispParams) {
    DISPPARAMS dispParams = new DISPPARAMS();
    COM.MoveMemory(dispParams, pDispParams, DISPPARAMS.sizeof);
    Variant[] variants = new Variant[dispParams.cArgs];
    // Reverse the order as we pull the variants in.
    for (int i = 0, n = dispParams.cArgs; i < n; ++i) {
      int varArgAddr = dispParams.rgvarg + Variant.sizeof * i;
      variants[n - i - 1] = Utils.win32_new(varArgAddr);
    }
    return variants;
  }

  // CHECKSTYLE_OFF
  @SuppressWarnings("unused")
  private final int GetIDsOfNames(int riid, int rgszNames, int cNames, int lcid, int rgDispId) {
    try {
      if (cNames < 1) {
        return COM.E_INVALIDARG;
      }
      // Extract the requested names and build an answer array init'ed with -1.
      //
      String[] names = SwtOleGlue.extractStringArrayFromOleCharPtrPtr(rgszNames, cNames);
      int[] ids = new int[names.length];
      Arrays.fill(ids, -1);
      getIDsOfNames(names, ids);
      OS.MoveMemory(rgDispId, ids, ids.length * 4);
    } catch (HResultException e) {
      return e.getHResult();
    } catch (Throwable e) {
      e.printStackTrace();
      return COM.E_FAIL;
    }
    return COM.S_OK;
  }

  @SuppressWarnings("unused")
  private int Invoke(int dispIdMember,
      int riid,
      int lcid,
      int dwFlags,
      int pDispParams,
      int pVarResult,
      int pExcepInfo,
      int pArgErr) {
    HResultException ex = null;
    Variant[] vArgs = null;
    Variant result = null;
    try {
      vArgs = extractVariantArrayFromDispParamsPtr(pDispParams);
      result = invoke(dispIdMember, dwFlags, vArgs);
      if (pVarResult != 0) {
        Utils.win32_copy(pVarResult, result);
      }
    } catch (HResultException e) {
      // Log to the console for detailed examination.
      //
      e.printStackTrace();
      ex = e;
    } catch (InvocationTargetException e) {
      // If we get here, it means an exception is being thrown from
      // Java back into JavaScript
      Throwable t = e.getTargetException();
      ex = new HResultException(t);
      ModuleSpace.setThrownJavaException(t);
    } catch (Exception e) {
      // Log to the console for detailed examination.
      //
      e.printStackTrace();
      ex = new HResultException(e);
    } finally {
      // We allocated variants for all arguments, so we must dispose them all.
      //
      for (int i = 0; i < vArgs.length; ++i) {
        if (vArgs[i] != null) {
          vArgs[i].dispose();
        }
      }
      if (result != null) {
        result.dispose();
      }
    }
    if (ex != null) {
      // Set up an exception for IE to throw.
      //
      ex.fillExcepInfo(pExcepInfo);
      return ex.getHResult();
    }
    return COM.S_OK;
  }

  // CHECKSTYLE_ON
  private static String getStackTraceAsString(Throwable e) {
    // Show the exception info for anything other than "UnableToComplete".
    if (e == null || e.getClass().getName().equals("UnableToCompleteException")) {
      return null;
    }
    // For each cause, print the requested number of entries of its stack
    // trace, being careful to avoid getting stuck in an infinite loop.
    //
    StringBuffer message = new StringBuffer();
    Throwable currentCause = e;
    String causedBy = "";
    HashSet<Throwable> seenCauses = new HashSet<Throwable>();
    while (currentCause != null && !seenCauses.contains(currentCause)) {
      seenCauses.add(currentCause);
      message.append(causedBy);
      causedBy = "\nCaused by: "; // after 1st, all say "caused by"
      message.append(currentCause.getClass().getName());
      message.append(": " + currentCause.getMessage());
      StackTraceElement[] stackElems = currentCause.getStackTrace();
      if (stackElems != null) {
        for (int i = 0; i < stackElems.length; ++i) {
          message.append("\n\tat ");
          message.append(stackElems[i].toString());
        }
      }
      currentCause = currentCause.getCause();
    }
    return message.toString();
  }
}
