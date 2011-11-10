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
package com.google.gwt.dev.shell.rewrite;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;

import java.lang.annotation.Annotation;

/**
 * A simple ClassAdapter that determines if a specific annotation is declared on
 * a type (ignoring any annotatons that may be present on supertypes or
 * superinterfaces).
 */
public class HasAnnotation extends ClassAdapter {
  /**
   * A utility method to determine if the class defined in
   * <code>classBytes</code> has a particular annotation.
   * 
   * @param classBytes the class's bytecode
   * @param annotation the type of annotation to look for
   * @return <code>true</code> if the class defined in <code>classBytes</code>
   *         possesses the desired annotation
   */
  public static boolean hasAnnotation(byte[] classBytes,
      Class<? extends Annotation> annotation) {
    HasAnnotation v = new HasAnnotation(new EmptyVisitor(), annotation);
    new ClassReader(classBytes).accept(v, ClassReader.SKIP_CODE
        | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

    return v.isFound();
  }

  private boolean found;

  private final String targetDesc;

  public HasAnnotation(ClassVisitor v, Class<? extends Annotation> annotation) {
    super(v);
    targetDesc = Type.getDescriptor(annotation);
  }

  public boolean isFound() {
    return found;
  }

  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    if (targetDesc.equals(desc)) {
      found = true;
    }
    return super.visitAnnotation(desc, visible);
  }
}