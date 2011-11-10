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
 * String & Integer message.
 */
public final class Message2StringInt extends Message2 {

  public Message2StringInt(Type type, String fmt) {
    super(type, fmt);
  }

  public TreeLogger branch(TreeLogger logger, String s, int x, Throwable caught) {
    Integer xi = Integer.valueOf(x);
    return branch2(logger, s, xi, getFormatter(s), getFormatter(xi), caught);
  }

  public void log(TreeLogger logger, String s, int x, Throwable caught) {
    Integer xi = Integer.valueOf(x);
    log2(logger, s, xi, getFormatter(s), getFormatter(xi), caught);
  }

}
