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
package com.google.gdt.eclipse.designer.uibinder.model.util;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectResolveTag;
import org.eclipse.wb.internal.core.xml.model.utils.TagResolverProvider;

/**
 * {@link XmlObjectResolveTag} for UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public class UiBinderTagResolverProvider implements TagResolverProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final TagResolverProvider INSTANCE = new UiBinderTagResolverProvider();

  private UiBinderTagResolverProvider() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TagResolverProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public void register(XmlObjectInfo rootObject) throws Exception {
    ClassLoader classLoader = rootObject.getContext().getClassLoader();
    if (ReflectionUtils.hasClass(classLoader, "com.google.gwt.user.client.ui.Widget")) {
      new UiBinderTagResolver(rootObject);
    }
  }
}
