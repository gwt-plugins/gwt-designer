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
package com.google.gwt.dev.cfg;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Manages a list of stylesheet urls.
 */
public class Styles implements Iterable<String> {

  private final LinkedList<String> list = new LinkedList<String>();

  /**
   * Append a script.
   * 
   * @param src a partial or full url to a script to inject
   */
  public void append(String src) {
    list.addLast(src);
  }

  public boolean isEmpty() {
    return list.isEmpty();
  }

  /**
   * An iterator over stylesheet urls (each one is a String).
   */
  public Iterator<String> iterator() {
    return list.iterator();
  }
}
