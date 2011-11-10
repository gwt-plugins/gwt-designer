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
package com.google.gdt.eclipse.designer.model.widgets.support;

import static org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils.invokeMethod;
import static org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils.invokeMethodEx;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.apache.commons.lang.StringUtils;

/**
 * Utilities for manipulation of GWT <code>Element</code>.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class DOMUtils {
  private final GwtState state;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DOMUtils(GwtState state) {
    this.state = state;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getTagName(Object element) {
    return getElementProperty(element, "tagName");
  }

  public String getClassName(Object element) {
    return getElementProperty(element, "className");
  }

  public String getId(Object element) {
    return getElementProperty(element, "id");
  }

  public String getInnerText(Object element) {
    return (String) invokeMethodEx(
        getClassOfDOM(),
        "getInnerText(com.google.gwt.user.client.Element)",
        element);
  }

  public String getInnerHTML(Object element) {
    return (String) invokeMethodEx(
        getClassOfDOM(),
        "getInnerHTML(com.google.gwt.user.client.Element)",
        element);
  }

  public String getElementAttribute(Object element, String name) {
    return (String) invokeMethodEx(
        getClassOfDOM(),
        "getElementAttribute(com.google.gwt.user.client.Element,java.lang.String)",
        element,
        name);
  }

  public String getElementProperty(Object element, String name) {
    return (String) invokeMethodEx(
        getClassOfDOM(),
        "getElementProperty(com.google.gwt.user.client.Element,java.lang.String)",
        element,
        name);
  }

  public String setElementProperty(Object element, String name, String value) {
    return (String) invokeMethodEx(
        getClassOfDOM(),
        "setElementProperty(com.google.gwt.user.client.Element,java.lang.String,java.lang.String)",
        element,
        name,
        value);
  }

  public String getStyleAttribute(Object element, String name) {
    return (String) invokeMethodEx(
        getClassOfDOM(),
        "getStyleAttribute(com.google.gwt.user.client.Element,java.lang.String)",
        element,
        name);
  }

  public void setStyleAttribute(Object element, String name, String value) {
    invokeMethodEx(
        getClassOfDOM(),
        "setStyleAttribute(com.google.gwt.user.client.Element,java.lang.String,java.lang.String)",
        element,
        name,
        value);
  }

  /**
   * @return <code>true</code> if element has style attribute with expected value.
   */
  public boolean hasStyleAttribute(Object element, String name, String expected) {
    String actual = getStyleAttribute(element, name);
    return StringUtils.equals(actual, expected);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Elements and parent/child
  //
  ////////////////////////////////////////////////////////////////////////////
  public String toString(Object element) {
    return (String) invokeMethodEx(
        getClassOfDOM(),
        "toString(com.google.gwt.user.client.Element)",
        element);
  }

  public Object getElementById(String id) throws Exception {
    return invokeMethod(getClassOfDOM(), "getElementById(java.lang.String)", id);
  }

  /**
   * SmartGWT wraps not yet rendered <code>Element</code> into <code>div</code>, so we should unwrap
   * it. If not SamrtGWT, then just same <code>Element</code> will be returned.
   */
  public Object unwrapElement(Object element) {
    String id = getId(element);
    if (id.startsWith("isc_") && id.contains("_wrapper")) {
      return getChild(element, 0);
    }
    return element;
  }

  public Object getParent(Object element) {
    return invokeMethodEx(getClassOfDOM(), "getParent(com.google.gwt.user.client.Element)", element);
  }

  public int getChildCount(Object parent) throws Exception {
    return (Integer) invokeMethod(
        getClassOfDOM(),
        "getChildCount(com.google.gwt.user.client.Element)",
        parent);
  }

  public int getChildIndex(Object parent, Object child) {
    return (Integer) invokeMethodEx(
        getClassOfDOM(),
        "getChildIndex(com.google.gwt.user.client.Element,com.google.gwt.user.client.Element)",
        parent,
        child);
  }

  public Object getChild(Object parent, int index) {
    return invokeMethodEx(
        getClassOfDOM(),
        "getChild(com.google.gwt.user.client.Element,int)",
        parent,
        index);
  }

  public Object[] getChildren(Object parent) throws Exception {
    int childCount = getChildCount(parent);
    Object[] children = new Object[childCount];
    for (int i = 0; i < childCount; i++) {
      children[i] = getChild(parent, i);
    }
    return children;
  }

  public void appendChild(Object parent, Object child) {
    invokeMethodEx(
        getClassOfDOM(),
        "appendChild(com.google.gwt.user.client.Element,com.google.gwt.user.client.Element)",
        parent,
        child);
  }

  public void insertChild(Object parent, Object child, int index) {
    invokeMethodEx(
        getClassOfDOM(),
        "insertChild(com.google.gwt.user.client.Element,com.google.gwt.user.client.Element,int)",
        parent,
        child,
        index);
  }

  public void removeChild(Object parent, Object child) {
    invokeMethodEx(
        getClassOfDOM(),
        "removeChild(com.google.gwt.user.client.Element,com.google.gwt.user.client.Element)",
        parent,
        child);
  }

  public void removeAllChildren(Object parent) throws Exception {
    while (getChildCount(parent) != 0) {
      Object child = getChild(parent, 0);
      removeChild(parent, child);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Style access
  //
  ////////////////////////////////////////////////////////////////////////////
  public int getAbsoluteLeft(Object element) {
    return (Integer) invokeMethodEx(
        getClassOfDOM(),
        "getAbsoluteLeft(com.google.gwt.user.client.Element)",
        element);
  }

  public int getAbsoluteTop(Object element) {
    return (Integer) invokeMethodEx(
        getClassOfDOM(),
        "getAbsoluteTop(com.google.gwt.user.client.Element)",
        element);
  }

  public int getIntAttribute(Object element, String attr) {
    return (Integer) invokeMethodEx(
        getClassOfDOM(),
        "getIntAttribute(com.google.gwt.user.client.Element,java.lang.String)",
        element,
        attr);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Create
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object createDiv() {
    return invokeMethodEx(getClassOfDOM(), "createDiv()");
  }

  public Object createButton() throws Exception {
    return invokeMethod(getClassOfDOM(), "createButton()");
  }

  public Object createTR() throws Exception {
    return invokeMethod(getClassOfDOM(), "createTR()");
  }

  public Object createTD() throws Exception {
    return invokeMethod(getClassOfDOM(), "createTD()");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Low level
  //
  ////////////////////////////////////////////////////////////////////////////
  private Class<?> loadClass(String name) throws Exception {
    return state.getClassLoader().loadClass(name);
  }

  public Class<?> getClassOfDOM() {
    try {
      return loadClass("com.google.gwt.user.client.DOM");
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }
}
