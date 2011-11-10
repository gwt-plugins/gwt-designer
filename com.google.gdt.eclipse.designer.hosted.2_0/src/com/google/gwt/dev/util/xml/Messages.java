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

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.dev.util.msg.Message0;
import com.google.gwt.dev.util.msg.Message2IntString;
import com.google.gwt.dev.util.msg.Message2StringInt;
import com.google.gwt.dev.util.msg.Message3IntStringClass;
import com.google.gwt.dev.util.msg.Message3StringIntString;

/**
 * Static XML messages.
 */
class Messages {
  public static final Message3IntStringClass XML_ATTRIBUTE_CONVERSION_ERROR = new Message3IntStringClass(
      TreeLogger.ERROR,
      "Line $0: Unable to convert attribute '$1' to type '$2'");

  public static final Message3StringIntString XML_ATTRIBUTE_UNEXPECTED = new Message3StringIntString(
      TreeLogger.ERROR,
      "Element '$0' beginning on line $1 contains unexpected attribute '$2'");

  public static final Message2StringInt XML_CHILDREN_NOT_ALLOWED = new Message2StringInt(
      TreeLogger.ERROR, "Child element $0 on line $1 is not allowed");

  public static final Message2IntString XML_ELEMENT_HANDLER_EXCEPTION = new Message2IntString(
      TreeLogger.ERROR,
      "Line $0: Unexpected exception while processing element '$1'");

  public static final Message2IntString XML_ELEMENT_UNEXPECTED = new Message2IntString(
      TreeLogger.ERROR, "Line $0: Unexpected element '$1'");

  public static final Message0 XML_PARSE_FAILED = new Message0(
      TreeLogger.ERROR, "Failure while parsing XML");

  public static final Message3StringIntString XML_REQUIRED_ATTRIBUTE_MISSING = new Message3StringIntString(
      TreeLogger.ERROR,
      "Element '$0' beginning on line $1 is missing required attribute '$2'");

  private Messages() {
  }

}
