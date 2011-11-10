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
package com.google.gdt.eclipse.designer.core.model.widgets;

import com.google.gdt.eclipse.designer.core.model.GwtModelTest;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.tests.Activator;

import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;

import org.eclipse.core.runtime.FileLocator;

import org.osgi.framework.Bundle;

import java.net.URL;

/**
 * Test for {@link com.google.gwt.maps.client.MapWidget}.
 * 
 * @author scheglov_ke
 */
public class MapWidgetTest extends GwtModelTest {
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
  @DisposeProjectAfter
  public void test_0() throws Exception {
    dontUseSharedGWTState();
    // add Maps
    {
      Bundle bundle = Activator.getDefault().getBundle();
      URL entry = bundle.getEntry("/resources/GWT/gwt-maps.jar");
      String path = FileLocator.toFileURL(entry).getPath();
      m_testProject.addExternalJar(path);
    }
    setFileContentSrc(
        "test/Module.gwt.xml",
        getSourceDQ(
            "<module>",
            "  <inherits name='com.google.gwt.user.User'/>",
            "  <inherits name='com.google.gwt.maps.GoogleMaps'/>",
            "</module>"));
    waitForAutoBuild();
    // parse
    parseJavaInfo(
        "import com.google.gwt.maps.client.MapWidget;",
        "import com.google.gwt.maps.client.geom.LatLng;",
        "public class Test implements EntryPoint {",
        "  public void onModuleLoad() {",
        "    RootPanel rootPanel = RootPanel.get();",
        "    {",
        "      LatLng Plovdiv = LatLng.newInstance(42.143365, 24.751032);",
        "      MapWidget map = new MapWidget(Plovdiv, 14);",
        "      rootPanel.add(map);",
        "    }",
        "  }",
        "}");
    refresh();
    assertNoErrors(m_lastParseInfo);
    // "map" is placeholder
    WidgetInfo map = getJavaInfoByName("map");
    assertTrue(map.isPlaceholder());
  }
}