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

import com.google.gwt.thirdparty.guava.common.collect.Interner;
import com.google.gwt.thirdparty.guava.common.collect.Interners;

/**
 * A utility class for reducing String memory waste. Note that this does not use
 * the String.intern() method which would prevent GC and fill the PermGen space.
 * Instead, we use a Google Collections WeakInterner.
 */
public class StringInterner {
  private static final StringInterner instance = new StringInterner();

  public static StringInterner get() {
    return instance;
  }

  private final Interner<String> stringPool = Interners.newWeakInterner();

  protected StringInterner() {
  }

  public String intern(String s) {
    return stringPool.intern(s);
  }

}
