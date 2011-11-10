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
package com.google.gwt.dev.util.log.speedtracer;

import com.google.gwt.dev.util.log.speedtracer.SpeedTracerLogger.EventType;

/**
 * Represents a type of event whose performance is tracked while running
 * {@link com.google.gwt.dev.DevMode}
 */
public enum DevModeEventType implements EventType {
  CLASS_BYTES_REWRITE("Class bytes rewrite", "DarkBlue"), //
  COMPILATION_STATE_BUILDER_PROCESS("CompilationStateBuilder process",
      "Teal"), //
  CREATE_UI("Create UI", "BlueViolet"), //
  GENERATED_UNITS_ADD("Generated units add", "Brown"), //
  JAVA_TO_JS_CALL("Java to JS call", "LightSkyBlue"), //
  JETTY_STARTUP("Jetty startup", "Orchid"), //
  JS_TO_JAVA_CALL("JS to Java call", "Orange"), //
  MODULE_INIT("Module init", "Khaki"), //
  MODULE_SPACE_CLASS_LOAD("ModuleSpace class load", "MintCream"), //
  MODULE_SPACE_HOST_CREATE("ModuleSpaceHost create", "Peachpuff"), //
  MODULE_SPACE_HOST_READY("ModuleSpaceHost ready", "Linen"), //
  MODULE_SPACE_LOAD("ModuleSpace load", "LemonChiffon"), //
  MODULE_SPACE_REBIND_AND_CREATE("ModuleSpace rebindAndCreate", "Crimson"), //
  ON_MODULE_LOAD("onModuleLoad", "LightGreen"), //
  REBIND("Rebind", "DarkOrange"), //
  SLOW_STARTUP("Slow startup", "DarkSeaGreen"), //
  STARTUP("Startup", "LimeGreen");

  final String cssColor;
  final String name;

  DevModeEventType(String name, String cssColor) {
    this.name = name;
    this.cssColor = cssColor;
  }

  public String getColor() {
    return cssColor;
  }

  public String getName() {
    return name;
  }
}
