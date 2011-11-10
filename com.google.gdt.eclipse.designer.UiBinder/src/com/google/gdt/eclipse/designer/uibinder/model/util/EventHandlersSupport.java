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

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.reflect.ClassMap;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectEventListeners;
import org.eclipse.wb.internal.core.xml.model.property.event.AbstractListenerProperty;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Supports adding UiBinder specific {@link AbstractListenerProperty}s.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public final class EventHandlersSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EventHandlersSupport(XmlObjectInfo rootObject) {
    rootObject.addBroadcastListener(new XmlObjectEventListeners() {
      public void invoke(XmlObjectInfo object, List<AbstractListenerProperty> properties)
          throws Exception {
        addListeners(object, properties);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final ClassMap<List<EventHandlerDescription>> m_widgetEvents = ClassMap.create();

  /**
   * Adds properties for handlers supported by given widget.
   */
  private void addListeners(XmlObjectInfo object, List<AbstractListenerProperty> properties)
      throws Exception {
    List<EventHandlerDescription> handlers = getWidgetHandlers(object);
    for (EventHandlerDescription handler : handlers) {
      properties.add(new EventHandlerProperty(object, handler));
    }
  }

  /**
   * @return the {@link EventHandlerDescription}s of handlers which are supported by given widget.
   */
  private static List<EventHandlerDescription> getWidgetHandlers(XmlObjectInfo widget) {
    Class<?> componentClass = widget.getDescription().getComponentClass();
    List<EventHandlerDescription> handlers = m_widgetEvents.get(componentClass);
    if (handlers == null) {
      handlers = Lists.newArrayList();
      m_widgetEvents.put(componentClass, handlers);
      // find "HandlerRegistration addX(EventHandler+)" methods
      for (Method addMethod : componentClass.getMethods()) {
        Class<?>[] addMethodParameters = addMethod.getParameterTypes();
        if (addMethodParameters.length == 1
            && ReflectionUtils.isSuccessorOf(
                addMethodParameters[0],
                "com.google.gwt.event.shared.EventHandler")
            && ReflectionUtils.isSuccessorOf(
                addMethod.getReturnType(),
                "com.google.gwt.event.shared.HandlerRegistration")) {
          Class<?> handlerType = addMethodParameters[0];
          Method[] handlerMethods = handlerType.getMethods();
          if (handlerMethods.length == 1) {
            handlers.add(new EventHandlerDescription(handlerMethods[0]));
          }
        }
      }
      // sort by name
      Collections.sort(handlers, new Comparator<EventHandlerDescription>() {
        public int compare(EventHandlerDescription o1, EventHandlerDescription o2) {
          String name_1 = o1.getMethodName();
          String name_2 = o2.getMethodName();
          return name_1.compareTo(name_2);
        }
      });
    }
    return handlers;
  }
}
