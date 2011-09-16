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
package org.eclipse.wb.core.utils.external;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.NestableError;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;

/**
 * Helper for accessing external factories contributed via extension points.
 * 
 * TODO: use the appropriate ExternalFactoriesHelper from D2.
 * 
 * @author scheglov_ke
 * @coverage gwtHosted
 */
public class ExternalFactoriesHelper {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private ExternalFactoriesHelper() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Caching and reloading
  //
  ////////////////////////////////////////////////////////////////////////////
  private static Map/*<String, Map<String, List<IConfigurationElement>>>*/m_configurationElements =
      new HashMap/*<String, Map<String, List<IConfigurationElement>>>*/();
  private static Map/*<String, Map<String, List<?>>>*/m_configurationObjects =
      new HashMap/*<String, Map<String, List<?>>>*/();
  /**
   * {@link IRegistryChangeListener} for tracking changes for interesting extensions.
   */
  private static final IRegistryChangeListener m_descriptionProcessorsListener =
      new IRegistryChangeListener() {
        public void registryChanged(IRegistryChangeEvent event) {
          IExtensionDelta[] extensionDeltas = event.getExtensionDeltas();
          for (int i = 0; i < extensionDeltas.length; i++) {
            IExtensionDelta extensionDelta = extensionDeltas[i];
            String pointId = extensionDelta.getExtensionPoint().getUniqueIdentifier();
            m_configurationElements.remove(pointId);
            m_configurationObjects.remove(pointId);
          }
        }
      };
  /**
   * Install {@link IRegistryChangeListener}.
   */
  static {
    Platform.getExtensionRegistry().addRegistryChangeListener(m_descriptionProcessorsListener);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IConfigurationElement's access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Return instances for objects contributed to specified extension point.
   * <p>
   * Objects are sorted by their optional "priority" attribute (descending). If "priority" is
   * absent, "0" is used.
   * 
   * @param clazz
   *          the {@link Class} of elements instances.
   * @param pointId
   *          the qualified id of extension, e.g. <code>"org.eclipse.core.builders"</code>.
   * @param elementName
   *          the name of element inside of extension, e.g. <code>"builder"</code>.
   * 
   * @return the instances for objects contributed to specified extension point.
   */
  public static List getElementsInstances(Class clazz, String pointId, String elementName) {
    // prepare: elementName -> List<?> 
    Map/*<String, List<?>>*/elementName_to_objects = (Map) m_configurationObjects.get(pointId);
    if (elementName_to_objects == null) {
      elementName_to_objects = new HashMap/*<String, List<?>>*/();
      m_configurationObjects.put(pointId, elementName_to_objects);
    }
    // check for cached: List<?>
    List objects = (List) elementName_to_objects.get(elementName);
    if (objects == null) {
      objects = new ArrayList();
      elementName_to_objects.put(elementName, objects);
      // sort elements by "priority"
      List/*<IConfigurationElement>*/elements = getElements(pointId, elementName);
      Collections.sort(elements, new Comparator/*<IConfigurationElement>*/() {
        public int compare(Object o1, Object o2) {
          return getPriority((IConfigurationElement) o2) - getPriority((IConfigurationElement) o1);
        }

        private int getPriority(IConfigurationElement element) {
          String priorityString = element.getAttribute("priority");
          return priorityString == null ? 0 : Integer.parseInt(priorityString);
        }
      });
      // create object
      for (Iterator I = elements.iterator(); I.hasNext();) {
        IConfigurationElement element = (IConfigurationElement) I.next();
        Object object = ExternalFactoriesHelper.createExecutableExtension(element, "class");
        objects.add(object);
      }
    }
    // OK, objects created
    return objects;
  }

  /**
   * @return the result of {@link IConfigurationElement#createExecutableExtension(String)}, throws
   *         {@link NestableError} if any exception happens.
   */
  public static Object createExecutableExtension(final IConfigurationElement element,
      final String classAttributeName) {
    try {
      // well, create new instance
      return element.createExecutableExtension(classAttributeName);
    } catch (Throwable e) {
      throw new Error(e);
    }
  }

  /**
   * Returns {@link IConfigurationElement}'s, contributed to extension point.<br>
   * 
   * @param pointId
   *          the qualified id of extension, e.g. <code>"org.eclipse.core.resources.builders"</code>
   *          .
   * @param elementName
   *          the name of element inside of extension, e.g. <code>"builder"</code>.
   * 
   * @return {@link IConfigurationElement}'s of all elements for specified extension point and
   *         element name.
   */
  public static List/*<IConfigurationElement>*/getElements(String pointId, String elementName) {
    // prepare: elementName -> List<IConfigurationElement> 
    Map/*<String, List<IConfigurationElement>>*/elementName_to_elements =
        (Map) m_configurationElements.get(pointId);
    if (elementName_to_elements == null) {
      elementName_to_elements = new HashMap/*<String, List<IConfigurationElement>>*/();
      m_configurationElements.put(pointId, elementName_to_elements);
    }
    // check for cached: List<IConfigurationElement>
    List/*<IConfigurationElement>*/elements = (List) elementName_to_elements.get(elementName);
    if (elements == null) {
      elements = new ArrayList/*<IConfigurationElement>*/();
      elementName_to_elements.put(elementName, elements);
      // load elements
      IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(pointId);
      if (extensionPoint != null) {
        IExtension[] extensions = extensionPoint.getExtensions();
        for (int i = 0; i < extensions.length; i++) {
          IExtension extension = extensions[i];
          IConfigurationElement[] configurationElements = extension.getConfigurationElements();
          for (int j = 0; j < configurationElements.length; j++) {
            IConfigurationElement element = configurationElements[j];
            if (elementName.equals(element.getName())) {
              elements.add(element);
            }
          }
        }
      }
    }
    // OK, we loaded: List<IConfigurationElement>
    return elements;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bundle access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link org.osgi.framework.Bundle} that defines given {@link IConfigurationElement}.
   */
  public static org.osgi.framework.Bundle getExtensionBundle(IConfigurationElement element) {
    IExtension extension = element.getDeclaringExtension();
    return getExtensionBundle(extension);
  }

  /**
   * @return the {@link org.osgi.framework.Bundle} that defines given {@link IExtension}.
   */
  public static org.osgi.framework.Bundle getExtensionBundle(IExtension extension) {
    String id = extension.getNamespaceIdentifier();
    return Platform.getBundle(id);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Attributes access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the not-<code>null</code> value of {@link String} attribute.
   * 
   * @exception IllegalArgumentException
   *              if no attribute with such name found.
   */
  public static String getRequiredAttribute(IConfigurationElement element, String attribute) {
    String value = element.getAttribute(attribute);
    if (value == null) {
      throw new IllegalArgumentException("Attribute '"
          + attribute
          + "' expected, but not found in "
          + element);
    }
    return value;
  }

  /**
   * @return the value of <code>int</code> attribute.
   * 
   * @exception IllegalArgumentException
   *              if no attribute with such name found.
   */
  public static int getRequiredAttributeInteger(IConfigurationElement element, String attribute) {
    String valueString = getRequiredAttribute(element, attribute);
    return Integer.parseInt(valueString);
  }
}
