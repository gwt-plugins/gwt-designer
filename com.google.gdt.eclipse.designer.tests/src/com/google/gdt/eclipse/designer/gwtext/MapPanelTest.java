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
package com.google.gdt.eclipse.designer.gwtext;

import com.google.gdt.eclipse.designer.model.widgets.panels.RootPanelInfo;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;

/**
 * Tests for <code>MapPanel</code>.
 * 
 * @author scheglov_ke
 */
public class MapPanelTest extends GwtExtModelTest {
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
  public void test_GoogleMap_liveImage() throws Exception {
    RootPanelInfo frame =
        parseJavaInfo(
            "public class Test implements EntryPoint {",
            "  public void onModuleLoad() {",
            "    RootPanel rootPanel = RootPanel.get();",
            "  }",
            "}");
    frame.refresh();
    // no Google scripts, so no live
    boolean enabledGoogleMap =
        (Boolean) ScriptUtils.evaluate(
            JavaInfoUtils.getClassLoader(frame),
            "com.gwtext.client.widgets.map.MapFactory.isGoogleAvailable()");
    assertFalse(enabledGoogleMap);
  }
}