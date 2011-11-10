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
 * 1-arg message.
 */
public abstract class Message1 extends Message {

  public Message1(Type type, String fmt) {
    super(type, fmt, 1);
  }

  protected TreeLogger branch1(TreeLogger logger, Object arg1, Formatter fmt1,
      Throwable caught) {
    return logger.branch(type, compose1(arg1, fmt1), caught);
  }

  protected String compose1(Object arg1, Formatter fmt1) {
    // Format the objects.
    //
    String stringArg1 = (arg1 != null ? fmt1.format(arg1) : "null");

    // To maintain consistency with the other impls, we use an insert var.
    //
    String insert1 = stringArg1;

    // Cache the length of the inserts.
    //
    int lenInsert1 = insert1.length();

    // Cache the length of each part.
    //
    int lenPart0 = fmtParts[0].length;
    int lenPart1 = fmtParts[1].length;

    // Prep for copying.
    //
    int dest = 0;
    char[] chars = new char[minChars + lenInsert1];

    // literal + insert, part 0
    System.arraycopy(fmtParts[0], 0, chars, dest, lenPart0);
    dest += lenPart0;

    insert1.getChars(0, lenInsert1, chars, dest);
    dest += lenInsert1;

    // final literal
    System.arraycopy(fmtParts[1], 0, chars, dest, lenPart1);

    return new String(chars);
  }

  protected void log1(TreeLogger logger, Object arg1, Formatter fmt1,
      Throwable caught) {
    if (logger.isLoggable(type)) {
      logger.log(type, compose1(arg1, fmt1), caught);
    }
  }
}
