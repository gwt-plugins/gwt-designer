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
package com.google.gdt.eclipse.designer.gwtext.parser;

import com.google.gdt.eclipse.designer.gwtext.IExceptionConstants;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;
import com.google.gdt.eclipse.designer.parser.IClassLoaderValidator;

import org.eclipse.wb.internal.core.parser.IParseFactory;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.IJavaProject;

/**
 * {@link IParseFactory} for GWT-Ext.
 * 
 * @author scheglov_ke
 * @coverage GWTExt
 */
public final class ClassLoaderValidator implements IClassLoaderValidator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IClassLoaderValidator
  //
  ////////////////////////////////////////////////////////////////////////////
  public void validate(IJavaProject javaProject, GwtState state) throws Exception {
    ClassLoader classLoader = state.getClassLoader();
    if (javaProject.findType("com.gwtext.client.widgets.Component") != null) {
      try {
        Class<?> classComponent = classLoader.loadClass("com.gwtext.client.widgets.Component");
        ReflectionUtils.invokeMethod(classComponent, "checkExtVer()");
      } catch (Throwable e) {
        throw new DesignerException(IExceptionConstants.NOT_CONFIGURED,
            javaProject.getElementName());
      }
    }
  }
}