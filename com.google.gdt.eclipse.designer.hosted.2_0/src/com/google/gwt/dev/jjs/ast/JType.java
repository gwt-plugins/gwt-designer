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
 * Base class for any types entity.
 */
public abstract class JType extends JNode implements HasName, CanBeFinal {

  protected final String name;
  private final JLiteral defaultValue;

  public JType(SourceInfo info, String name, JLiteral defaultValue) {
    super(info);
    this.name = name;
    this.defaultValue = defaultValue;
  }

  public abstract String getClassLiteralFactoryMethod();

  public JLiteral getDefaultValue() {
    return defaultValue;
  }

  public abstract String getJavahSignatureName();

  public abstract String getJsniSignatureName();

  public String getName() {
    return name;
  }

}
