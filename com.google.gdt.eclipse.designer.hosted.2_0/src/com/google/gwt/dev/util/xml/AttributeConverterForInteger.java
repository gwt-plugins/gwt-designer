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

/**
 * Subclass for converting strings into Integer.
 */
public class AttributeConverterForInteger extends AttributeConverter {
  public Object convertToArg(Schema schema, int lineNumber, String elemName,
      String attrName, String attrValue) throws UnableToCompleteException {
    try {
      return Integer.valueOf(attrValue);
    } catch (NumberFormatException e) {
      schema.onBadAttributeValue(lineNumber, elemName, attrName, attrValue,
        Integer.class);
      return null;
    }
  }
}