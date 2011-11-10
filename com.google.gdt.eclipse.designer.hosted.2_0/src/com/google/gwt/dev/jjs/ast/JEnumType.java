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
import com.google.gwt.dev.util.collect.Lists;

import java.util.List;

/**
 * Java enum type reference expression.
 */
public class JEnumType extends JClassType {
  /*
   * TODO: implement traverse?
   */

  private List<JEnumField> enumList = Lists.create();

  public JEnumType(SourceInfo info, String name) {
    super(info, name, false, false);
  }

  @Override
  public void addField(JField field) {
    if (field instanceof JEnumField) {
      JEnumField enumField = (JEnumField) field;
      int ordinal = enumField.ordinal();
      while (ordinal >= enumList.size()) {
        enumList = Lists.add(enumList, null);
      }
      enumList = Lists.set(enumList, ordinal, enumField);
    }
    super.addField(field);
  }

  @Override
  public String getClassLiteralFactoryMethod() {
    return "Class.createForEnum";
  }

  /**
   * Returns the list of enum fields in this enum.
   */
  public List<JEnumField> getEnumList() {
    return enumList;
  }

  @Override
  public JEnumType isEnumOrSubclass() {
    return this;
  }
}
