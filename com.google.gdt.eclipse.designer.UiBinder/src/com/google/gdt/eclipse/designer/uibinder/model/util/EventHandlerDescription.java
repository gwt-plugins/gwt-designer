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

import java.lang.reflect.Method;

/**
 * Information about single event handler.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public final class EventHandlerDescription {
  private final Method m_method;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EventHandlerDescription(Method method) {
    m_method = method;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the name of handler method, such as "onClick".
   */
  public String getMethodName() {
    return m_method.getName();
  }

  /**
   * @return the canonical name of event type, such as "com.google.gwt.event.dom.client.ClickEvent".
   */
  public String getEventTypeName() {
    Class<?> eventType = m_method.getParameterTypes()[0];
    return ReflectionUtils.getCanonicalName(eventType);
  }
}
