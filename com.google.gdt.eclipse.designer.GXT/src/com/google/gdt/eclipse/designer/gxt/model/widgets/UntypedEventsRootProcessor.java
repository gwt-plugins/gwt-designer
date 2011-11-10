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
package com.google.gdt.eclipse.designer.gxt.model.widgets;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.event.EventsPropertyUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.MenuManagerEx;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Support for untyped GXT events.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model
 */
public class UntypedEventsRootProcessor implements IRootProcessor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IRootProcessor INSTANCE = new UntypedEventsRootProcessor();

  private UntypedEventsRootProcessor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IRootProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(JavaInfo root, List<JavaInfo> components) throws Exception {
    root.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void addContextMenu(List<? extends ObjectInfo> objects,
          ObjectInfo object,
          IMenuManager manager) throws Exception {
        if (object instanceof ComponentInfo) {
          ComponentInfo component = (ComponentInfo) object;
          contributeContextMenu(manager, component);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  private void contributeContextMenu(IMenuManager manager, final ComponentInfo component) {
    Map<String, MenuManagerEx> eventManagers = Maps.newTreeMap();
    for (final EventDescription description : getEventDescriptions(component)) {
      // prepare MenuManager for event
      MenuManagerEx eventManager;
      {
        String event = description.getEvent();
        eventManager = eventManagers.get(event);
        if (eventManager == null) {
          eventManager = new MenuManagerEx(event);
          eventManager.setImage(EventsPropertyUtils.LISTENER_INTERFACE_IMAGE);
          eventManagers.put(event, eventManager);
          manager.appendToGroup(IContextMenuConstants.GROUP_EVENTS2, eventManager);
        }
      }
      // add specific name
      Action action = new Action() {
        @Override
        public void runWithEvent(Event event) {
          if ((event.stateMask & SWT.CTRL) != 0) {
            removeListener(component, description);
          } else {
            openListener(component, description);
          }
        }
      };
      {
        String text = description.getName();
        MethodInvocation invocation = getInvocation(component, description);
        if (invocation != null) {
          int line = 1 + component.getEditor().getLineNumber(invocation.getStartPosition());
          text += "\tline " + line;
        }
        action.setText(text);
      }
      action.setImageDescriptor(EventsPropertyUtils.LISTENER_METHOD_IMAGE_DESCRIPTOR);
      eventManager.add(action);
    }
  }

  private List<EventDescription> getEventDescriptions(ComponentInfo component) {
    List<EventDescription> descriptions = Lists.newArrayList();
    // fill
    ComponentDescription componentDescription = component.getDescription();
    Class<?> componentClass = componentDescription.getComponentClass();
    while (componentClass != null) {
      String paramaterName = "GXT.untyped.events: " + componentClass.getName();
      String parameter = JavaInfoUtils.getParameter(component, paramaterName);
      if (parameter != null) {
        String[] lines = StringUtils.split(parameter, "\n");
        for (String line : lines) {
          String[] parts = StringUtils.split(line);
          String event = parts[0];
          for (int i = 1; i < parts.length; i++) {
            descriptions.add(new EventDescription(parts[i], event));
          }
        }
      }
      componentClass = componentClass.getSuperclass();
    }
    // sort by event and name
    Collections.sort(descriptions, new Comparator<EventDescription>() {
      public int compare(EventDescription o1, EventDescription o2) {
        {
          int eventResult = o1.getEvent().compareTo(o2.getEvent());
          if (eventResult != 0) {
            return eventResult;
          }
        }
        return o1.getName().compareTo(o2.getName());
      }
    });
    // done
    return descriptions;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Invocation
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String ADD_LISTENER_SIGNATURE = "addListener("
      + "com.extjs.gxt.ui.client.event.EventType,"
      + "com.extjs.gxt.ui.client.event.Listener)";

  /**
   * @return the {@link MethodInvocation} of <code>addListener()</code> for given event. May be
   *         <code>null</code>.
   */
  private MethodInvocation getInvocation(ComponentInfo component, EventDescription description) {
    List<MethodInvocation> invocations = component.getMethodInvocations(ADD_LISTENER_SIGNATURE);
    for (MethodInvocation invocation : invocations) {
      Expression nameExpression = DomGenerics.arguments(invocation).get(0);
      if (nameExpression.toString().endsWith(description.getName())) {
        return invocation;
      }
    }
    return null;
  }

  private void removeListener(final ComponentInfo component, EventDescription description) {
    final MethodInvocation invocation = getInvocation(component, description);
    if (invocation != null) {
      ExecutionUtils.run(component, new RunnableEx() {
        public void run() throws Exception {
          component.getEditor().removeEnclosingStatement(invocation);
        }
      });
    }
  }

  private void openListener(final ComponentInfo component, final EventDescription description) {
    // try to find existing addListener()
    {
      MethodInvocation invocation = getInvocation(component, description);
      if (invocation != null) {
        JavaInfoUtils.scheduleOpenNode(component, invocation);
        return;
      }
    }
    // generate new addListener()
    ExecutionUtils.run(component, new RunnableEx() {
      public void run() throws Exception {
        MethodInvocation invocation =
            component.addMethodInvocation(
                ADD_LISTENER_SIGNATURE,
                "com.extjs.gxt.ui.client.event.Events."
                    + description.getName()
                    + ", "
                    + "new com.extjs.gxt.ui.client.event.Listener<"
                    + description.getEvent()
                    + ">() {\n"
                    + "\tpublic void handleEvent(com.extjs.gxt.ui.client.event."
                    + description.getEvent()
                    + " e) {\n"
                    + "\t}\n"
                    + "}");
        JavaInfoUtils.scheduleOpenNode(component, invocation);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Description of single event
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class EventDescription {
    private final String m_name;
    private final String m_event;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public EventDescription(String name, String event) {
      m_name = name;
      m_event = event;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * @return the name of event, such as "Enable" or "Move".
     */
    public String getName() {
      return m_name;
    }

    /**
     * @return the name of event type, such "ComponentEvent".
     */
    public String getEvent() {
      return m_event;
    }
  }
}
