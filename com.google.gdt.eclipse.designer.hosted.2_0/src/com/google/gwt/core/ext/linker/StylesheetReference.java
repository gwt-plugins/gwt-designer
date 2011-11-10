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
package com.google.gwt.core.ext.linker;

import com.google.gwt.core.ext.Linker;

/**
 * An external stylesheet referenced in the module manifest. The index is
 * important because output order must match module declaration order.
 */
public abstract class StylesheetReference extends Artifact<StylesheetReference> {
  private final String src;
  private final int index;

  protected StylesheetReference(Class<? extends Linker> linkerType, String src,
      int index) {
    super(linkerType);
    this.src = src;
    this.index = index;
  }

  /**
   * The <code>src</code> attribute of the resource. This string is returned
   * raw and may be a partial path or a URL.
   */
  public final String getSrc() {
    return src;
  }

  @Override
  public final int hashCode() {
    return getSrc().hashCode();
  }

  @Override
  public String toString() {
    return "<style src='" + getSrc() + "'>";
  }

  @Override
  protected final int compareToComparableArtifact(StylesheetReference o) {
    return index - o.index;
  }

  @Override
  protected Class<StylesheetReference> getComparableArtifactType() {
    return StylesheetReference.class;
  }
}
