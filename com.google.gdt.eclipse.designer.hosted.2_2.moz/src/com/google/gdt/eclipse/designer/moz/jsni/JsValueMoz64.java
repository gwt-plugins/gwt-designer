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
package com.google.gdt.eclipse.designer.moz.jsni;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gdt.eclipse.designer.moz.jsni.LowLevelMoz64.DispatchMethod64;
import com.google.gdt.eclipse.designer.moz.jsni.LowLevelMoz64.DispatchObject64;
import com.google.gwt.dev.shell.designtime.DispatchIdOracle;
import com.google.gwt.dev.shell.designtime.JsValue;
import com.google.gwt.dev.shell.designtime.WrappersCache;

/**
 * Represents a Mozilla JavaScript value.
 * 
 * TODO(jat): 64-bit compatibility - currently underlying pointers are passed
 * around in a Java int, which only works on standard 32-bit platforms where
 * sizeof(void*)=4
 */
public class JsValueMoz64 extends JsValue {

  /**
   * Records debug information, only used when {@link JsValueMoz64#debugFlag} is
   * <code>true</code>.
   */
  private static class DebugLogging {
    private final Map<Long, Throwable> alreadyCleanedJsRootedValues = Collections.synchronizedMap(new HashMap<Long, Throwable>());
    private int maxActive = 0;
    private int numActive = 0;
    private final Map<Long, Throwable> seenJsRootedValues = Collections.synchronizedMap(new HashMap<Long, Throwable>());
    private int totAlloc = 0;

    /**
     * Count a JsValueMoz instance being created.
     * 
     * Verify that the underlying JsRootedValue is not currently active, and
     * mark that it is active.
     * 
     * This is debug code that is only executed if debugFlag is true. Since this
     * is a private static final field, the compiler should optimize out all
     * this code. It is useful to have for tracking down problems, so it is
     * being left in but disabled.
     */
    public void createInstance(long jsRootedValue) {
      Long jsrv = new Long(jsRootedValue);
      if (seenJsRootedValues.containsKey(jsrv)) {
        Throwable t = seenJsRootedValues.get(jsrv);
        String msg = hexString(jsRootedValue);
        System.err.println(msg + ", original caller stacktrace:");
        t.printStackTrace();
        throw new RuntimeException(msg);
      }
      Throwable t = new Throwable();
      seenJsRootedValues.put(jsrv, t);
      if (alreadyCleanedJsRootedValues.containsKey(jsrv)) {
        alreadyCleanedJsRootedValues.remove(jsrv);
      }
      if (++numActive > maxActive) {
        maxActive = numActive;
      }
      ++totAlloc;
    }

    /**
     * Count a JsValueMoz instance being destroyed.
     * 
     * Verify that this instance hasn't already been destroyed, that it has
     * previously been created, and that the underlying JsRootedValue is only
     * being cleaned once.
     * 
     * This is debug code that is only executed if debugFlag is true. Since this
     * is a private static final field, the compiler should optimize out all
     * this code. It is useful to have for tracking down problems, so it is
     * being left in but disabled.
     */
    public void destroyInstance(long jsRootedValue) {
      if (jsRootedValue == 0) {
        throw new RuntimeException("Cleaning already-cleaned JsValueMoz");
      }
      Long jsrv = new Long(jsRootedValue);
      if (!seenJsRootedValues.containsKey(jsrv)) {
        throw new RuntimeException("cleaning up 0x" + hexString(jsRootedValue)
            + ", not active");
      }
      if (alreadyCleanedJsRootedValues.containsKey(jsrv)) {
        Throwable t = seenJsRootedValues.get(jsrv);
        String msg = "Already cleaned 0x" + hexString(jsRootedValue);
        System.err.println(msg + ", original allocator stacktrace:");
        t.printStackTrace();
        throw new RuntimeException(msg);
      }
      Throwable t = new Throwable();
      alreadyCleanedJsRootedValues.put(jsrv, t);
      seenJsRootedValues.remove(jsrv);
      --numActive;
    }

    /**
     * Print collected statistics on JsValueMoz usage.
     */
    public void dumpStatistics() {
      System.gc();
      System.out.println("JsValueMoz usage:");
      System.out.println(" " + totAlloc + " total instances created");
      System.out.println(" " + maxActive + " at any one time");
      System.out.println(" " + seenJsRootedValues.size() + " uncleaned entries");
    }
  }

  private static class JsCleanupMoz implements JsCleanup {
    private final long jsRootedValue;

    public JsCleanupMoz(long jsRootedValue) {
      this.jsRootedValue = jsRootedValue;
    }

    public void doCleanup() {
      _destroyJsRootedValue(jsRootedValue);
    }
  }

  /**
   * Flag to enable debug checks on underlying JsRootedValues.
   */
  private static final boolean debugFlag = false;

  /**
   * Flag to enable debug checks on underlying JsRootedValues.
   */
  private static final DebugLogging debugInfo = debugFlag ? new DebugLogging()
      : null;

  // CHECKSTYLE_NAMING_OFF -- native methods start with '_'
  protected static native boolean _getBoolean(long jsRootedValue);

  protected static native int _getInt(long jsRootedValue);

  protected static native long _getJsval(long jsRootedValue);

  protected static native double _getNumber(long jsRootedValue);

  protected static native String _getString(long jsRootedValue);

  protected static native String _getTypeString(long jsRootedValue);

  protected static native DispatchObject64 _getWrappedJavaObject(long jsRootedValue);

  protected static native boolean _isBoolean(long jsRootedValue);

  protected static native boolean _isInt(long jsRootedValue);

  protected static native boolean _isJavaScriptObject(long jsRootedValue);

  protected static native boolean _isJavaScriptString(long jsRootedValue);

  protected static native boolean _isNull(long jsRootedValue);

  protected static native boolean _isNumber(long jsRootedValue);

  protected static native boolean _isString(long jsRootedValue);

  protected static native boolean _isUndefined(long jsRootedValue);

  protected static native boolean _isWrappedJavaObject(long jsRootedValue);

  protected static native void _setBoolean(long jsRootedValue, boolean val);

  protected static native void _setDouble(long jsRootedValue, double val);

  protected static native void _setInt(long jsRootedValue, int val);

  protected static native void _setJsRootedValue(long jsRootedValue,
      long jsOtherRootedValue);

  protected static native void _setJsval(long jsRootedValue, long jsval);

  protected static native void _setNull(long jsRootedValue);

  protected static native void _setString(long jsRootedValue, String val);

  protected static native void _setUndefined(long jsRootedValue);

  protected static native void _setWrappedFunction(long jsRootedValue,
      String methodName, DispatchMethod64 dispatchMethod);

  protected static native void _setWrappedJavaObject(long jsRootedValue,
      DispatchObject64 val, Class<?> objClass);

  private static native long _copyJsRootedValue(long jsRootedValue);

  /**
   * Create a JsRootedValue and return a pointer to it as a Java int.
   * 
   * @return pointer to JsRootedValue object as an integer
   */
  private static native long _createJsRootedValue();

  /**
   * Destroy a JsRootedValue.
   * 
   * @param jsRootedValue pointer to underlying JsRootedValue as an integer.
   */
  private static native void _destroyJsRootedValue(long jsRootedValue);

  // CHECKSTYLE_NAMING_ON

  /**
   * Convert an address to a hex string.
   * 
   * @param jsRootedValue underlying JavaScript value as an opaque integer
   * @return a string with the JavaScript value represented as hex
   */
  private static String hexString(long jsRootedValue) {
    return Long.toHexString(jsRootedValue);
  }

  // pointer to underlying JsRootedValue object as an integer
  private long jsRootedValue;

  /**
   * Create a JsValueMoz object representing the undefined value.
   */
  public JsValueMoz64() {
    this.jsRootedValue = _createJsRootedValue();
    if (debugFlag) {
      debugInfo.createInstance(jsRootedValue);
    }
  }

  /**
   * Create a JsValueMoz object wrapping a JsRootedValue object given the
   * pointer to it as an integer.
   * 
   * @param jsRootedValue pointer to underlying JsRootedValue as an integer.
   */
  public JsValueMoz64(long jsRootedValue) {
    this.jsRootedValue = jsRootedValue;
    if (debugFlag) {
      debugInfo.createInstance(jsRootedValue);
    }
  }

  /**
   * Copy constructor.
   * 
   * @param other JsValueMoz instance to copy
   */
  public JsValueMoz64/*64*/(JsValueMoz64/*64*/ other) {
    jsRootedValue = _copyJsRootedValue(other.jsRootedValue);
    if (debugFlag) {
      debugInfo.createInstance(jsRootedValue);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.dev.shell.JsValue#getBoolean()
   */
  @Override
  public boolean getBoolean() {
    return _getBoolean(jsRootedValue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.dev.shell.JsValue#getInt()
   */
  @Override
  public int getInt() {
    return _getInt(jsRootedValue);
  }

  @Override
  public long getJavaScriptObjectPointer() {
    assert isJavaScriptObject();
    return _getJsval(jsRootedValue);
  }

  /**
   * Returns the underlying JavaScript object pointer as an integer.
   */
  public long getJsRootedValue() {
    return jsRootedValue;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.dev.shell.JsValue#getNumber()
   */
  @Override
  public double getNumber() {
    return _getNumber(jsRootedValue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.dev.shell.JsValue#getString()
   */
  @Override
  public String getString() {
    return _getString(jsRootedValue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.dev.shell.JsValue#getTypeString()
   */
  @Override
  public String getTypeString() {
    return _getTypeString(jsRootedValue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.dev.shell.JsValue#getWrappedJavaObject()
   */
  @Override
  public Object getWrappedJavaObject() {
    DispatchObject64 obj = _getWrappedJavaObject(jsRootedValue);
    return obj.getTarget();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.dev.shell.JsValue#isBoolean()
   */
  @Override
  public boolean isBoolean() {
    return _isBoolean(jsRootedValue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.dev.shell.JsValue#isInt()
   */
  @Override
  public boolean isInt() {
    return _isInt(jsRootedValue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.dev.shell.JsValue#isJavaScriptObject()
   */
  @Override
  public boolean isJavaScriptObject() {
    return _isJavaScriptObject(jsRootedValue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.dev.shell.JsValue#isNull()
   */
  @Override
  public boolean isNull() {
    return _isNull(jsRootedValue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.dev.shell.JsValue#isNumber()
   */
  @Override
  public boolean isNumber() {
    return _isNumber(jsRootedValue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.dev.shell.JsValue#isString()
   */
  @Override
  public boolean isString() {
    // String objects are acceptable for String value returns
    return _isString(jsRootedValue) || _isJavaScriptString(jsRootedValue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.dev.shell.JsValue#isUndefined()
   */
  @Override
  public boolean isUndefined() {
    return _isUndefined(jsRootedValue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.dev.shell.JsValue#isWrappedJavaObject()
   */
  @Override
  public boolean isWrappedJavaObject() {
    return _isWrappedJavaObject(jsRootedValue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.dev.shell.JsValue#setBoolean(boolean)
   */
  @Override
  public void setBoolean(boolean val) {
    _setBoolean(jsRootedValue, val);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.dev.shell.JsValue#setByte(byte)
   */
  @Override
  public void setByte(byte val) {
    _setInt(jsRootedValue, val);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.dev.shell.JsValue#setChar(char)
   */
  @Override
  public void setChar(char val) {
    _setInt(jsRootedValue, val);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.dev.shell.JsValue#setDouble(double)
   */
  @Override
  public void setDouble(double val) {
    _setDouble(jsRootedValue, val);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.dev.shell.JsValue#setInt(int)
   */
  @Override
  public void setInt(int val) {
    _setInt(jsRootedValue, val);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.dev.shell.JsValue#setNull()
   */
  @Override
  public void setNull() {
    _setNull(jsRootedValue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.dev.shell.JsValue#setShort(short)
   */
  @Override
  public void setShort(short val) {
    _setInt(jsRootedValue, val);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.dev.shell.JsValue#setString(java.lang.String)
   */
  @Override
  public void setString(String val) {
    _setString(jsRootedValue, val);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.dev.shell.JsValue#setUndefined()
   */
  @Override
  public void setUndefined() {
    _setUndefined(jsRootedValue);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.dev.shell.JsValue#setValue(com.google.gwt.dev.shell.JsValue)
   */
  @Override
  public void setValue(JsValue other) {
    _setJsRootedValue(jsRootedValue, ((JsValueMoz64/*64*/) other).jsRootedValue);
  }

  /**
   * Wrap a function call to a Java method in this JavaScript value.
   * 
   * @param methodName the name of the method to invoke
   * @param dispatchMethod the wrapper object
   */
  public void setWrappedFunction(String methodName, DispatchMethod64 dispatchMethod) {
	Long jsval = (Long) LowLevelMoz.sObjectToJsval.get(dispatchMethod);
    if (jsval != null) {
	  _setJsval(jsRootedValue, jsval.longValue());
    } else {
      _setWrappedFunction(jsRootedValue, methodName, dispatchMethod);
	  LowLevelMoz.sObjectToJsval.put(dispatchMethod, new Long(_getJsval(jsRootedValue)));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.gwt.dev.shell.JsValue#setWrappedJavaObject(com.google.gwt.dev.shell.CompilingClassLoader,
   *      java.lang.Object)
   */
  @Override
  public <T> void setWrappedJavaObject(ClassLoader cl, DispatchIdOracle dispIdOracle, T val) {
    if (val == null) {
      setNull();
      return;
    }
    DispatchObject64 dispObj;
    if (val instanceof DispatchObject64) {
      dispObj = (DispatchObject64) val;
    } else {
      dispObj = (DispatchObject64) WrappersCache.getWrapperForObject(cl, val);
      if (dispObj == null) {
        dispObj = new GeckoDispatchAdapter64(cl, dispIdOracle, val);
        WrappersCache.putWrapperForObject(cl, val, dispObj);
      }
    }
    setWrappedJavaObject(dispObj);
  }

  /**
   * Same as {@link #setWrappedJavaObject(CompilingClassLoader, Object)} but without caching in {@link CompilingClassLoader}.
   */
  public void setWrappedJavaObject(DispatchObject64 dispObj) {
    Long cached = (Long) LowLevelMoz.sObjectToJsval.get(dispObj);
    if (cached != null) {
      _setJsval(jsRootedValue, cached);
    } else {
      _setWrappedJavaObject(jsRootedValue, dispObj, dispObj.getClass());
      LowLevelMoz.sObjectToJsval.put(dispObj, new Long(_getJsval(jsRootedValue)));
    }
  }

  /**
   * Create a cleanup object that will free the underlying JsRootedValue object.
   */
  @Override
  protected JsCleanup createCleanupObject() {
    JsCleanup cleanup = new JsCleanupMoz(jsRootedValue);
    if (debugFlag) {
      debugInfo.destroyInstance(jsRootedValue);
      jsRootedValue = 0;
    }
    return cleanup;
  }

}
