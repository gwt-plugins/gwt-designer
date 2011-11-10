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
 * String array message formatter.
 */
public final class FormatterForStringArray extends Formatter {

  public String format(Object toFormat) {
    StringBuffer sb = new StringBuffer();
    String[] ss = (String[]) toFormat;
    for (int i = 0, n = ss.length; i < n; ++i) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(ss[i]);
    }
    return sb.toString();
  }

}
