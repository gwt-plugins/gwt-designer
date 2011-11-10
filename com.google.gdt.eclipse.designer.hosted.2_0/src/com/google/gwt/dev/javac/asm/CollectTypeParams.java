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
package com.google.gwt.dev.javac.asm;

import com.google.gwt.core.ext.typeinfo.JTypeParameter;

import java.util.List;

/**
 * Collects formal type parameters into a JTypeParameter list.
 */
public class CollectTypeParams extends EmptySignatureVisitor {

  private final List<JTypeParameter> typeParams;

  /**
   * Collect declared type parameters from a generic signature.
   * 
   * @param typeParams list to store type parameters in
   */
  public CollectTypeParams(List<JTypeParameter> typeParams) {
    this.typeParams = typeParams;
  }

  @Override
  public void visitFormalTypeParameter(String name) {
    typeParams.add(new JTypeParameter(name, typeParams.size()));
  }
}
