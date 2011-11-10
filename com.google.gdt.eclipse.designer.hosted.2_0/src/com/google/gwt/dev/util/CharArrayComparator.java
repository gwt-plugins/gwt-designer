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
package com.google.gwt.dev.util;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Performs a case-sensitive comparison of char arrays.
 */
public class CharArrayComparator implements Comparator<char[]>, Serializable {

  public static final CharArrayComparator INSTANCE = new CharArrayComparator();

  public int compare(char[] a, char[] b) {
    int ai = 0;
    int bi = 0;

    for (; ai < a.length && bi < b.length; ++ai, ++bi) {
      int c = a[ai] - b[bi];
      if (c != 0) {
        return c;
      }
    }

    if (ai == a.length && bi < b.length) {
      // a is shorter
      return -1;
    }

    if (ai < a.length && bi == b.length) {
      // b is shorter
      return 1;
    }

    // they are equal
    //
    return 0;
  }

}
