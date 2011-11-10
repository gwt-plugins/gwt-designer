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

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;

/**
 * String, Integer, & String message.
 */
public final class Message3StringIntString extends Message3 {

  public Message3StringIntString(Type type, String fmt) {
    super(type, fmt);
  }

  public TreeLogger branch(TreeLogger logger, String s1, int x, String s2,
      Throwable caught) {
    Integer xi = Integer.valueOf(x);
    return branch3(logger, s1, xi, s2, getFormatter(s1), getFormatter(xi),
        getFormatter(s2), caught);
  }

  public void log(TreeLogger logger, String s1, int x, String s2,
      Throwable caught) {
    Integer xi = Integer.valueOf(x);
    log3(logger, s1, xi, s2, getFormatter(s1), getFormatter(xi),
        getFormatter(s2), caught);
  }

}
