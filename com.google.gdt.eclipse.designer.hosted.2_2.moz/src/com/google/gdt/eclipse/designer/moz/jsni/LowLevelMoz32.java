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
package com.google.gdt.eclipse.designer.moz.jsni;

import org.eclipse.swt.widgets.Shell;

public class LowLevelMoz32/*64*/ extends LowLevelMoz {
	  /**
	   * Provides interface for methods to be exposed on JavaScript side.
	   */
	  public interface DispatchMethod32/*64*/ {
	    /**
	     * Invoke a Java method from JavaScript.
	     * 
	     * @param jsthis the wrapped Java object to invoke
	     * @param jsargs an array of JavaScript values to pass as parameters
	     * @param returnValue the JavaScript value in which to store the returned
	     *          value
	     */
	    void invoke(int /*long*/jsthis, int /*long*/[]jsargs, int /*long*/returnValue);
	  }

	  /**
	   * Provides interface for objects to be exposed to JavaScript code.
	   */
	  public interface DispatchObject32/*64*/ {
	    /**
	     * Retrieve a field from an object.
	     * 
	     * @param name the name of the field
	     * @param value pointer to the JsRootedValue to receive the field value
	     */
	    void getField(String name, int /*long*/value);

	    Object getTarget();

	    /**
	     * Set the value of a field on an object.
	     * 
	     * @param name the name of the field
	     * @param value pointer to the JsRootedValue to store into the field
	     */
	    void setField(String name, int /*long*/value);
	  }
	  /**
	   * Executes JavaScript code, retaining file and line information.
	   * 
	   * @param scriptObject An opaque handle to the script frame window
	   * @param code The JavaScript code to execute
	   * @param file A file name associated with the code
	   * @param line A line number associated with the code.
	   */
	  public static void executeScriptWithInfo(int /*long*/scriptObject, String code,
	      String file, int line) {
	    if (!_executeScriptWithInfo(scriptObject, code, file, line)) {
	      throw new RuntimeException(file + "(" + line
	          + "): Failed to execute script: " + code);
	    }
	  }

	  public static int /*long*/getScriptObjectProxy(int /*long*/domWindow) {
		  return _getScriptObjectProxy(domWindow);
	  }
	  public static void releaseScriptObjectProxy(int /*long*/domWindow) {
		  _releaseScriptObjectProxy(domWindow);
	  }
	  private static native int /*long*/_getScriptObjectProxy(int /*long*/domWindow);
	  private static native void _releaseScriptObjectProxy(int /*long*/domWindow);
	  /**
	   * Invokes a method implemented in JavaScript.
	   * 
	   * @param scriptObject An opaque handle to the script frame window
	   * @param methodName the method name on jsthis to call
	   * @param jsthis A wrapped java object as a JsRootedValue pointer
	   * @param jsargs the arguments to pass to the method as JsRootedValue pointers
	   * @param retval the jsvalue to write the result into
	   * @throws RuntimeException if the invoke fails
	   */
	  public static void invoke(int /*long*/scriptObject, String methodName, int /*long*/jsthis,
	      int[] /*long*/jsargs, int /*long*/retval) {
	    if (!_invoke(scriptObject, methodName, jsthis, jsargs, retval)) {
	      throw new RuntimeException("Failed to invoke native method: "
	          + methodName + " with " + jsargs.length + " arguments.");
	    }
	  }

	private static native boolean _executeScriptWithInfo(int /*long*/scriptObject,
			String newScript, String file, int line);

	/**
	 * Native method for invoking a JavaScript method.
	 * 
	 * @param scriptObject nsIScriptGlobalObject* as an int
	 * @param methodName name of JavaScript method
	 * @param jsThisInt JavaScript object to invoke the method on, as a
	 *          JsRootedValue int
	 * @param jsArgsInt array of arguments, as an array of JsRootedValue ints
	 * @param jsRetValint pointer to JsRootedValue to receive return value
	 * @return true on success
	 */
	private static native boolean _invoke(int /*long*/scriptObject, String methodName,
			int /*long*/jsThisInt, int /*long*/[] jsArgsInt, int /*long*/jsRetValInt);

	////////////////////////////////////////////////////////////////////////////
	//
	// TODO: rip this out when moving to D2
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Causes taking the screen shot.
	 * 
	 * @param windowHandle
	 *            the handle (GtkWidget*) of root gtk widget of {@link Shell}.
	 * @return the GdkPixmap* of {@link Shell}.
	 */
	public static native int /*long*/_makeShot(int /*long*/windowHandle);
	/**
	 * Prepares the preview window to screen shot.
	 */
	public static native boolean _begin_shot(int /*long*/windowHandle);
	/**
	 * Finalizes the process of screen shot.
	 */
	public static native boolean _end_shot(int /*long*/windowHandle);
	/**
	 * Simply calls g_object_unref() for given <code>widgetHandle</code>.
	 */
	public static native void _g_object_unref(int /*long*/widgetHandle);
}
