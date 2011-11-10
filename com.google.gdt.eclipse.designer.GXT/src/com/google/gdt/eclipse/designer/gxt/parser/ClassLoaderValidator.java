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
package com.google.gdt.eclipse.designer.gxt.parser;

import com.google.gdt.eclipse.designer.gxt.IExceptionConstants;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;
import com.google.gdt.eclipse.designer.parser.IClassLoaderValidator;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.IJavaProject;

import java.util.List;

/**
 * {@link IClassLoaderValidator} for GXT.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT
 */
public final class ClassLoaderValidator implements IClassLoaderValidator {
  private static final String[] VALID_VERSIONS = {"2\\.0\\.1", "2\\.1\\.\\d*", "2\\.2\\.\\d*"};
  private static final String VALID_VERSIONS_STRING = "2.0.1, 2.1.x, 2.2.x";

  ////////////////////////////////////////////////////////////////////////////
  //
  // IClassLoaderValidator
  //
  ////////////////////////////////////////////////////////////////////////////
  public void validate(IJavaProject javaProject, GwtState state) throws Exception {
    if (hasGXT(javaProject, state)) {
      ClassLoader classLoader = state.getClassLoader();
      // version
      {
        String description = getWrongVersionDescription(classLoader);
        if (description != null) {
          throw new DesignerException(IExceptionConstants.INCORRECT_VERSION,
              VALID_VERSIONS_STRING,
              description);
        }
      }
      // required resources
      if (!hasRequiredStyles(state)) {
        throw new DesignerException(IExceptionConstants.NO_RESOURCES);
      }
    }
  }

  /**
   * @return <code>true</code> if given {@link GwtState} has access to GXT.
   */
  private static boolean hasGXT(IJavaProject javaProject, GwtState state) {
    if (ProjectUtils.hasType(javaProject, "com.extjs.gxt.ui.client.widget.Component")) {
      try {
        state.getClassLoader().loadClass("com.extjs.gxt.ui.client.Version");
        return true;
      } catch (ClassNotFoundException e) {
      }
    }
    return false;
  }

  /**
   * @return the description of invalid version, or <code>null</code> if version in valid.
   */
  private static String getWrongVersionDescription(ClassLoader classLoader) {
    try {
      Class<?> classVersion = classLoader.loadClass("com.extjs.gxt.ui.client.Version");
      String release = ReflectionUtils.getFieldString(classVersion, "release");
      for (int i = 0; i < VALID_VERSIONS.length; i++) {
        String validPattern = VALID_VERSIONS[i];
        if (release.matches(validPattern)) {
          return null;
        }
      }
      return release;
    } catch (Throwable e) {
      DesignerPlugin.log(e);
      return "See logged exception";
    }
  }

  /**
   * @return <code>true</code> if {@link GwtState} has required CSS files for GXT.
   */
  private static boolean hasRequiredStyles(GwtState state) {
    List<String> cssResources = state.getCssSupport().getResources();
    for (String cssResource : cssResources) {
      if (cssResource.contains("gxt-")
          && cssResource.endsWith(".css")
          && isExistingResource(state, cssResource)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isExistingResource(final GwtState state, final String resource) {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Boolean>() {
      public Boolean runObject() throws Exception {
        return Utils.isExistingResource(state.getModuleDescription(), resource);
      }
    }, false);
  }
}