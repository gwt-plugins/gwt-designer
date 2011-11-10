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

import java.util.IdentityHashMap;
import java.util.Map;

import com.google.gwt.dev.shell.JsValue;


/**
 * Various low-level helper methods for dealing with Gecko.
 */
public abstract class LowLevelMoz {
  /**
   * Stores a map from DispatchObject/DispatchMethod to the live underlying
   * jsval. This is used to both preserve identity for the same Java Object and
   * also prevent GC.
   */
  static Map<Object, Number> sObjectToJsval = new IdentityHashMap<Object, Number>();

  /**
   * Call this to raise an exception in JavaScript before returning control.
   * Currently, the JavaScript exception throw is always null.
   */
  public static void raiseJavaScriptException() {
    if (!_raiseJavaScriptException()) {
      throw new RuntimeException(
          "Failed to raise Java Exception into JavaScript.");
    }
  }

  /**
   * Called from native code to do tracing.
   * 
   * @param s the string to trace
   */
  protected static void trace(String s) {
    System.out.println(s);
    System.out.flush();
  }
  
  /**
   * Native code accessor to remove the mapping upon GC.
   */
  static void removeJsvalForObject(Object o) {
    sObjectToJsval.remove(o);
  }

	private static native boolean _raiseJavaScriptException();

  /**
   * Print debug information for a JS method invocation.
   * 
   * TODO(jat): remove this method
   * 
   * @param methodName the name of the JS method being invoked
   * @param jsthis the JS object with the named method
   * @param jsargs an array of arguments to the method
   */
  private static void printInvocationParams(String methodName, JsValue jsthis, JsValue[] jsargs) {
    System.out.println("LowLevelMoz.invoke:");
    System.out.println(" method = " + methodName);
    System.out.println(" # args = " + (jsargs.length));
    System.out.println(" jsthis = " + jsthis.toString());
    for (int i = 0; i < jsargs.length; ++i) {
      System.out.println(" jsarg[" + i + "] = " + jsargs[i].toString());
    }
    System.out.println("");
  }

}
