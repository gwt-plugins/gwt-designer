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
package com.google.gwt.dev.util.collect;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * A memory-efficient identity hash map.
 * 
 * @param <K> the key type
 * @param <V> the value type
 */
public class IdentityHashMap<K, V> extends HashMap<K, V> {

  public IdentityHashMap() {
  }

  public IdentityHashMap(Map<? extends K, ? extends V> m) {
    super(m);
  }

  @Override
  protected boolean keyEquals(Object a, Object b) {
    return a == b;
  }

  @Override
  protected int keyHashCode(Object k) {
    return System.identityHashCode(k);
  }

  @Override
  protected boolean valueEquals(Object a, Object b) {
    return a == b;
  }

  @Override
  protected int valueHashCode(Object k) {
    return System.identityHashCode(k);
  }

  private void readObject(ObjectInputStream in) throws IOException,
      ClassNotFoundException {
    in.defaultReadObject();
    doReadObject(in);
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
    doWriteObject(out);
  }
}
