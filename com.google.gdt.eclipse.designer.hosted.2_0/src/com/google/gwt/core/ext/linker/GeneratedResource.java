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

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.Linker;

/**
 * A resource created by a {@link Generator} invoking
 * {@link com.google.gwt.core.ext.GeneratorContext#tryCreateResource(com.google.gwt.core.ext.TreeLogger, String)}
 * during the compilation process.
 */
public abstract class GeneratedResource extends EmittedArtifact {
  private final String generatorTypeName;
  private transient Class<? extends Generator> generatorType;

  protected GeneratedResource(Class<? extends Linker> linkerType,
      Class<? extends Generator> generatorType, String partialPath) {
    super(linkerType, partialPath);
    this.generatorTypeName = generatorType.getName();
    this.generatorType = generatorType;
  }

  /**
   * The type of Generator that created the resource.
   */
  public final Class<? extends Generator> getGenerator() {
    // generatorType is null when deserialized.
    if (generatorType == null) {
      try {
        Class<?> clazz = Class.forName(generatorTypeName, false,
            Thread.currentThread().getContextClassLoader());
        generatorType = clazz.asSubclass(Generator.class);
      } catch (ClassNotFoundException e) {
        // The class may not be available.
        generatorType = Generator.class;
      }
    }
    return generatorType;
  }
}
