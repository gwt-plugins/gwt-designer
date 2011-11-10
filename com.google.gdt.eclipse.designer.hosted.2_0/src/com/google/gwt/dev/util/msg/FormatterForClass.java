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
package com.google.gwt.dev.util.msg;

/**
 * Class message formatter.
 */
public final class FormatterForClass extends Formatter {

  public String format(Object toFormat) {
    return getNiceTypeName((Class<?>) toFormat);
  }

  private String getNiceTypeName(Class<?> targetType) {
    // Screen out common cases.
    // Otherwise, just pass along the class name.
    if (targetType.isArray()) {
      return getNiceTypeName(targetType.getComponentType()) + "[]";
    } else {
      return targetType.getName();
    }
  }
}
