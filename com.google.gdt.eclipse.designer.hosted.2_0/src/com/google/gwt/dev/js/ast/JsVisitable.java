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
package com.google.gwt.dev.js.ast;

/**
 * Abstracts the idea that a class can be traversed.
 * 
 * @param <T>
 */
public interface JsVisitable<T extends JsVisitable<T>> {

  /**
   * Causes this object to have the visitor visit itself and its children.
   * 
   * @param visitor the visitor that should traverse this node
   * @param ctx the context of an existing traversal
   */
  void traverse(JsVisitor visitor, JsContext<T> ctx);
}
