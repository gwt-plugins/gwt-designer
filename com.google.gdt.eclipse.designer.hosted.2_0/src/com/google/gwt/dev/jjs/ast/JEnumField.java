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
 * A field that is an enum constant.
 */
public class JEnumField extends JField {

  private int ordinal;

  public JEnumField(SourceInfo info, String name, int ordinal,
      JEnumType enclosingType, JClassType type) {
    super(info, name, enclosingType, type, true, Disposition.FINAL);
    this.ordinal = ordinal;
  }

  @Override
  public JEnumType getEnclosingType() {
    // TODO Auto-generated method stub
    return (JEnumType) super.getEnclosingType();
  }

  public int ordinal() {
    return ordinal;
  }

  // TODO: implement traverse?

}
