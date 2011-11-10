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
 * 0-arg message.
 */
public final class Message0 extends Message {

  public Message0(Type type, String fmt) {
    super(type, fmt, 0);
  }

  public TreeLogger branch(TreeLogger logger, Throwable caught) {
    // Always branch, even if the branch root is not loggable.
    // See TreeLogger.branch() for details as to why.
    //
    return logger.branch(type, new String(fmtParts[0]), caught);
  }

  public void log(TreeLogger logger, Throwable caught) {
    if (logger.isLoggable(type)) {
      logger.log(type, new String(fmtParts[0]), caught);
    }
  }
}
