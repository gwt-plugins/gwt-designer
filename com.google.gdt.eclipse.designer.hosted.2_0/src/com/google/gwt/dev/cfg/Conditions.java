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

import com.google.gwt.dev.util.collect.Lists;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * A typed collection of {@link Condition} objects.
 */
public class Conditions implements Serializable {

  private List<Condition> list = Lists.create();

  /**
   * Appends a condition.
   */
  public void add(Condition condition) {
    list = Lists.add(list, condition);
  }

  public Iterator<Condition> iterator() {
    return list.iterator();
  }
}
