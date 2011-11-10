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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Collect data from a single field.
 */
public class CollectFieldData extends EmptyVisitor {

  private List<CollectAnnotationData> annotations = new ArrayList<CollectAnnotationData>();
  private int access;
  private String name;
  private String desc;
  private String signature;
  private Object value;

  public CollectFieldData(int access, String name, String desc,
      String signature, Object value) {
    this.access = access;
    this.name = name;
    this.desc = desc;
    this.signature = signature;
    this.value = value;
  }

  /**
   * @return the access
   */
  public int getAccess() {
    return access;
  }

  /**
   * @return the annotations
   */
  public List<CollectAnnotationData> getAnnotations() {
    return annotations;
  }

  /**
   * @return the desc
   */
  public String getDesc() {
    return desc;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the signature
   */
  public String getSignature() {
    return signature;
  }

  /**
   * @return the value
   */
  public Object getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "field " + name;
  }

  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    CollectAnnotationData av = new CollectAnnotationData(desc,
        visible);
    annotations.add(av);
    return av;
  }
}
