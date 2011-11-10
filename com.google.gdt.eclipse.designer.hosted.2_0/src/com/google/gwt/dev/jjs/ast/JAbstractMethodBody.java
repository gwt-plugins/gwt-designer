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
package com.google.gwt.dev.jjs.ast;

import com.google.gwt.dev.jjs.SourceInfo;

/**
 * Represents a the body of a method. Can be Java or JSNI.
 */
public abstract class JAbstractMethodBody extends JNode {
  protected JMethod method;

  protected JAbstractMethodBody(SourceInfo info) {
    super(info);
  }

  public JMethod getMethod() {
    return method;
  }

  public abstract boolean isNative();

  public void setMethod(JMethod method) {
    this.method = method;
  }
}
