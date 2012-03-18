package com.google.gdt.eclipse.designer.ie.jsni;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.swt.internal.ole.win32.COM;
import org.eclipse.swt.internal.ole.win32.DISPPARAMS;
import org.eclipse.swt.internal.ole.win32.EXCEPINFO;
import org.eclipse.swt.internal.ole.win32.GUID;
import org.eclipse.swt.internal.ole.win32.IDispatch;
import org.eclipse.swt.internal.ole.win32.VARIANT;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.Variant;

/**
 * This is extracted implementation from {@link OleAutomation} to fix bug in {@link Variant}. It
 * handles {@link COM#VT_BSTR} <code>null</code> as {@link COM#VT_EMPTY}, but actually should handle
 * it as an empty {@link String}.
 * <p>
 * http://blogs.msdn.com/b/ericlippert/archive/2003/09/12/52976.aspx
 * 
 * @author scheglov_ke
 */
public class OleAutomationFix {
  /**
   * @see OleAutomation#invoke(int, Variant[])
   */
  public static Variant invoke(OleAutomation automation, int dispIdMember, Variant[] rgvarg) {
    try {
      Variant pVarResult = new Variant();
      int result = invoke(automation, dispIdMember, COM.DISPATCH_METHOD, rgvarg, null, pVarResult);
      return (result == COM.S_OK) ? pVarResult : null;
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }
  private static int invoke(OleAutomation automation,
      int dispIdMember,
      int wFlags,
      Variant[] rgvarg,
      int[] rgdispidNamedArgs,
      Variant pVarResult) throws Exception {
    IDispatch objIDispatch = (IDispatch) ReflectionUtils.getFieldObject(automation, "objIDispatch");
    // get the IDispatch interface for the control
    if (objIDispatch == null)
      return COM.E_FAIL;
    // create a DISPPARAMS structure for the input parameters
    DISPPARAMS pDispParams = new DISPPARAMS();
    // store arguments in rgvarg
    if (rgvarg != null && rgvarg.length > 0) {
      pDispParams.cArgs = rgvarg.length;
      pDispParams.rgvarg =
          OS.GlobalAlloc(COM.GMEM_FIXED | COM.GMEM_ZEROINIT, VARIANT.sizeof * rgvarg.length);
      int offset = 0;
      for (int i = rgvarg.length - 1; i >= 0; i--) {
        ReflectionUtils.invokeMethod(rgvarg[i], "getData(int)", pDispParams.rgvarg + offset);
        offset += VARIANT.sizeof;
      }
    }
    // if arguments have ids, store the ids in rgdispidNamedArgs
    if (rgdispidNamedArgs != null && rgdispidNamedArgs.length > 0) {
      pDispParams.cNamedArgs = rgdispidNamedArgs.length;
      pDispParams.rgdispidNamedArgs =
          OS.GlobalAlloc(COM.GMEM_FIXED | COM.GMEM_ZEROINIT, 4 * rgdispidNamedArgs.length);
      int offset = 0;
      for (int i = rgdispidNamedArgs.length; i > 0; i--) {
        COM.MoveMemory(
            pDispParams.rgdispidNamedArgs + offset,
            new int[]{rgdispidNamedArgs[i - 1]},
            4);
        offset += 4;
      }
    }
    // invoke the method
    EXCEPINFO excepInfo = new EXCEPINFO();
    int[] pArgErr = new int[1];
    int /*long*/pVarResultAddress = 0;
    if (pVarResult != null)
      pVarResultAddress = OS.GlobalAlloc(OS.GMEM_FIXED | OS.GMEM_ZEROINIT, VARIANT.sizeof);
    int result =
        objIDispatch.Invoke(
            dispIdMember,
            new GUID(),
            COM.LOCALE_USER_DEFAULT,
            wFlags,
            pDispParams,
            pVarResultAddress,
            excepInfo,
            pArgErr);
    if (pVarResultAddress != 0) {
      // get VT_ type
      int type;
      {
        short[] dataType = new short[1];
        COM.MoveMemory(dataType, pVarResultAddress, 2);
        type = dataType[0];
      }
      // initialize "pVarResult"
      ReflectionUtils.invokeMethod(pVarResult, "setData(int)", pVarResultAddress);
      COM.VariantClear(pVarResultAddress);
      OS.GlobalFree(pVarResultAddress);
      // if was VT_BSTR and become VT_EMPTY that this is bug in SWT Variant
      if (type == COM.VT_BSTR && pVarResult.getType() == COM.VT_EMPTY) {
        ReflectionUtils.setField(pVarResult, "type", COM.VT_BSTR);
        ReflectionUtils.setField(pVarResult, "stringData", "");
      }
    }
    // free the Dispparams resources
    if (pDispParams.rgdispidNamedArgs != 0) {
      OS.GlobalFree(pDispParams.rgdispidNamedArgs);
    }
    if (pDispParams.rgvarg != 0) {
      int offset = 0;
      for (int i = 0, length = rgvarg.length; i < length; i++) {
        COM.VariantClear(pDispParams.rgvarg + offset);
        offset += VARIANT.sizeof;
      }
      OS.GlobalFree(pDispParams.rgvarg);
    }
    // save error string and cleanup EXCEPINFO
    //manageExcepinfo(result, excepInfo);
    ReflectionUtils.invokeMethod(
        automation,
        "manageExcepinfo(int,org.eclipse.swt.internal.ole.win32.EXCEPINFO)",
        result,
        excepInfo);
    return result;
  }
}
