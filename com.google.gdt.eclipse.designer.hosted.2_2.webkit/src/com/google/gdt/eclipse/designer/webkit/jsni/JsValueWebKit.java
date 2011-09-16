/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gdt.eclipse.designer.webkit.jsni;

import com.google.gdt.eclipse.designer.webkit.jsni.LowLevelWebKit.DispatchObject;
import com.google.gwt.dev.shell.designtime.DispatchIdOracle;
import com.google.gwt.dev.shell.designtime.JsValue;
import com.google.gwt.dev.shell.designtime.WrappersCache;

/**
 * Represents a Safari JavaScript value.
 * 
 * The basic rule is that any JSValue passed to Java code from native code will
 * always be GC-protected in the native code and Java will always unprotect it
 * when the value is finalized. It should always be stored in a JsValue object
 * immediately to make sure it is cleaned up properly when it is no longer
 * needed. This approach is required to avoid a race condition where the value
 * is allocated in JNI code but could be garbage collected before Java takes
 * ownership of the value. Java values passed into JavaScript store a GlobalRef
 * of a WebKitDispatchAdapter or MethodDispatch objects, which are freed when
 * the JS value is finalized.
 */
public class JsValueWebKit<H extends Number> extends JsValue {

  private static class JsCleanupSaf<H extends Number> implements JsCleanup {
    private final H jsval;

    /**
     * Create a cleanup object which takes care of cleaning up the underlying JS
     * object.
     * 
     * @param jsval JSValue pointer as an integer
     */
    public JsCleanupSaf(H jsval) {
      this.jsval = jsval;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.gwt.dev.shell.JsValue.JsCleanup#doCleanup()
     */
    public void doCleanup() {
      LowLevelWebKit.gcUnprotect(LowLevelWebKit.getCurrentJsContext(), jsval);
    }
  }

  /*
   * Underlying JSValue* as an integer.
   */
  private H jsval;

  /**
   * Create a Java wrapper around an undefined JSValue.
   */
  public JsValueWebKit() {
    init(LowLevelWebKit.<H>getJsUndefined(LowLevelWebKit.<H>getCurrentJsContext()));
  }

  /**
   * Create a Java wrapper around the underlying JSValue.
   * 
   * @param jsval a pointer to the underlying JSValue object as an integer
   */
  public JsValueWebKit(H jsval) {
    init(jsval);
  }

  @Override
  public boolean getBoolean() {
    H curJsContext = LowLevelWebKit.getCurrentJsContext();
    return LowLevelWebKit.toBoolean(curJsContext, jsval);
  }

  @Override
  public int getInt() {
    H currentJsContext = LowLevelWebKit.getCurrentJsContext();
    return LowLevelWebKit.toInt(currentJsContext, jsval);
  }

  @Override
  public long getJavaScriptObjectPointer() {
    assert isJavaScriptObject();
    return ((Number)jsval).longValue();
  }

  public H getJsValue() {
    return jsval;
  }

  @Override
  public double getNumber() {
    H currentJsContext = LowLevelWebKit.getCurrentJsContext();
    return LowLevelWebKit.toDouble(currentJsContext, jsval);
  }

  @Override
  public String getString() {
    final H currentJsContext = LowLevelWebKit.getCurrentJsContext();
    return LowLevelWebKit.toString(currentJsContext, jsval);
  }

  @Override
  public String getTypeString() {
    return LowLevelWebKit.getTypeString(LowLevelWebKit.getCurrentJsContext(), jsval);
  }

  @Override
  public Object getWrappedJavaObject() {
    DispatchObject<?> obj = LowLevelWebKit.unwrapDispatchObject(
        LowLevelWebKit.getCurrentJsContext(), jsval);
    return obj.getTarget();
  }

  @Override
  public boolean isBoolean() {
    return LowLevelWebKit.isJsBoolean(LowLevelWebKit.getCurrentJsContext(), jsval);
  }

  @Override
  public boolean isInt() {
    // Safari doesn't have integers, so this is always false
    return false;
  }

  @Override
  public boolean isJavaScriptObject() {
    final H currentJsContext = LowLevelWebKit.getCurrentJsContext();
    return LowLevelWebKit.isJsObject(currentJsContext, jsval)
        && !LowLevelWebKit.isDispatchObject(currentJsContext, jsval);
  }

  @Override
  public boolean isNull() {
    return LowLevelWebKit.isJsNull(LowLevelWebKit.getCurrentJsContext(), jsval);
  }

  @Override
  public boolean isNumber() {
    return LowLevelWebKit.isJsNumber(LowLevelWebKit.getCurrentJsContext(), jsval);
  }

  @Override
  public boolean isString() {
    return LowLevelWebKit.isJsString(LowLevelWebKit.getCurrentJsContext(), jsval);
  }

  @Override
  public boolean isUndefined() {
    return LowLevelWebKit.isJsUndefined(LowLevelWebKit.getCurrentJsContext(), jsval);
  }

  @Override
  public boolean isWrappedJavaObject() {
    return LowLevelWebKit.isDispatchObject(LowLevelWebKit.getCurrentJsContext(),
        jsval);
  }

  @Override
  public void setBoolean(boolean val) {
    setJsVal(LowLevelWebKit.<H>toJsBoolean(LowLevelWebKit.<H>getCurrentJsContext(), val));
  }

  @Override
  public void setByte(byte val) {
    setJsVal(LowLevelWebKit.<H>toJsNumber(LowLevelWebKit.<H>getCurrentJsContext(), val));
  }

  @Override
  public void setChar(char val) {
    setJsVal(LowLevelWebKit.<H>toJsNumber(LowLevelWebKit.<H>getCurrentJsContext(), val));
  }

  @Override
  public void setDouble(double val) {
    setJsVal(LowLevelWebKit.<H>toJsNumber(LowLevelWebKit.<H>getCurrentJsContext(), val));
  }

  @Override
  public void setInt(int val) {
    setJsVal(LowLevelWebKit.<H>toJsNumber(LowLevelWebKit.<H>getCurrentJsContext(), val));
  }

  @Override
  public void setNull() {
    setJsVal(LowLevelWebKit.<H>getJsNull(LowLevelWebKit.<H>getCurrentJsContext()));
  }

  @Override
  public void setShort(short val) {
    setJsVal(LowLevelWebKit.<H>toJsNumber(LowLevelWebKit.<H>getCurrentJsContext(), val));
  }

  @Override
  public void setString(String val) {
    setJsVal(LowLevelWebKit.<H>toJsString(LowLevelWebKit.<H>getCurrentJsContext(), val));
  }

  @Override
  public void setUndefined() {
     setJsVal(LowLevelWebKit.<H>getJsUndefined(LowLevelWebKit.<H>getCurrentJsContext()));
  }

  @Override
  public void setValue(JsValue other) {
    @SuppressWarnings("unchecked")
  JsValueWebKit<H> jsValueWebKit = (JsValueWebKit<H>) other;
  H jsvalOther = jsValueWebKit.jsval;
    /*
     * Add another lock to this jsval, since both the other object and this one
     * will eventually release it.
     */
    LowLevelWebKit.gcProtect(LowLevelWebKit.getCurrentJsContext(), jsvalOther);
    setJsVal(jsvalOther);
  }

  @Override
  public <T> void setWrappedJavaObject(ClassLoader cl, DispatchIdOracle dispIdOracle, T val) {
    DispatchObject<?> dispObj;
    if (val == null) {
      setNull();
      return;
    } else if (val instanceof DispatchObject) {
      dispObj = (DispatchObject<?>) val;
    } else {
      dispObj = (DispatchObject<?>) WrappersCache.getWrapperForObject(cl, val);
      if (dispObj == null) {
        dispObj = new WebKitDispatchAdapter<H>(cl, dispIdOracle, val);
        WrappersCache.putWrapperForObject(cl, val, dispObj);
      }
    }
    setJsVal(LowLevelWebKit.<H>wrapDispatchObject(LowLevelWebKit.<H>getCurrentJsContext(),
        dispObj));
  }

  @Override
  protected JsCleanup createCleanupObject() {
    return new JsCleanupSaf<H>(jsval);
  }

  /**
   * Initialization helper method.
   * 
   * @param jsval underlying JSValue*
   */
  private void init(H jsval) {
    this.jsval = jsval;

    // If protection checking is enabled, we check to see if the value we are
    // accepting is protected as it should be.
    if (LowLevelWebKit.isJsValueProtectionCheckingEnabled()
        && !LowLevelWebKit.isGcProtected(jsval)) {
      throw new RuntimeException("Cannot accepted unprotected JSValue ("
          + Long.toHexString(jsval.longValue()) + ", "
          + LowLevelWebKit.getTypeString(LowLevelWebKit.getCurrentJsContext(), jsval)
          + ")");
    }
  }

  /**
   * Set a new value. Unlock the previous value, but do *not* lock the new value
   * (see class comment).
   * 
   * @param jsval the new value to set
   */
  private void setJsVal(H jsval) {
    LowLevelWebKit.gcUnprotect(LowLevelWebKit.getCurrentJsContext(), this.jsval);
    init(jsval);
  }

}
