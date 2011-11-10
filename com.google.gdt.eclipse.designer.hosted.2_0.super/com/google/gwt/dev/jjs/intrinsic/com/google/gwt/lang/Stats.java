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
package com.google.gwt.lang;

/**
 * Provides access to the statistics collector function as an intrinsic for use
 * by the compiler. The typical use case is:
 * 
 * <pre>
 * isStatsAvailable() &amp;&amp; stats()
 * </pre>
 */
final class Stats {
  static native boolean isStatsAvailable() /*-{
    return !!$stats;
  }-*/;

  static native boolean onModuleStart(String mainClassName) /*-{
    return $stats({
      moduleName: $moduleName,
      sessionId: $sessionId,
      subSystem: "startup",
      evtGroup: "moduleStartup",
      millis : (new Date()).getTime(),
      type: "onModuleLoadStart",
      className: mainClassName,
    });
  }-*/;
}
