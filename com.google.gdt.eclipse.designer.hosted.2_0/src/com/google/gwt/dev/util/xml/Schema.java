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
package com.google.gwt.dev.util.xml;

import com.google.gwt.core.ext.UnableToCompleteException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for reflection-based push-parsing of XML.
 */
public abstract class Schema {

  private final Map<Class<?>, AttributeConverter> convertersByType =
    new HashMap<Class<?>, AttributeConverter>();

  private Schema parent;

  private int lineNumber;

  /**
   * Finds the most recent converter in the schema chain that can convert the
   * specified type.
   */
  public AttributeConverter getAttributeConverter(Class type) {
    AttributeConverter converter = convertersByType.get(type);
    if (converter != null) {
      return converter;
    } else if (parent != null) {
      return parent.getAttributeConverter(type);
    }

    throw new IllegalStateException(
        "Unable to find an attribute converter for type " + type.getName());
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public void onBadAttributeValue(int line, String elem, String attr,
      String value, Class paramType) throws UnableToCompleteException {
    if (parent != null) {
      parent.onBadAttributeValue(line, elem, attr, value, paramType);
    }
  }

  public void onHandlerException(int line, String elem, Method method,
      Throwable e) throws UnableToCompleteException {
    if (parent != null) {
      parent.onHandlerException(line, elem, method, e);
    }
  }

  public void onMissingAttribute(int line, String elem, String attr)
      throws UnableToCompleteException {
    if (parent != null) {
      parent.onMissingAttribute(line, elem, attr);
    }
  }

  public void onUnexpectedAttribute(int line, String elem, String attr,
      String value) throws UnableToCompleteException {
    if (parent != null) {
      parent.onUnexpectedAttribute(line, elem, attr, value);
    }
  }

  public void onUnexpectedChild(int line, String elem)
      throws UnableToCompleteException {
    if (parent != null) {
      parent.onUnexpectedChild(line, elem);
    }
  }

  public void onUnexpectedElement(int line, String elem)
      throws UnableToCompleteException {
    if (parent != null) {
      parent.onUnexpectedElement(line, elem);
    }
  }

  public void registerAttributeConverter(Class type,
      AttributeConverter converter) {
    convertersByType.put(type, converter);
  }

  public void setLineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
  }

  public void setParent(Schema parent) {
    this.parent = parent;
  }
}
