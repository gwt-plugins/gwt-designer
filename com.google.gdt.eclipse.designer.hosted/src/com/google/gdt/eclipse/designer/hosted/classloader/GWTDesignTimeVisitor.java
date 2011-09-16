/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gdt.eclipse.designer.hosted.classloader;

import org.eclipse.wb.internal.core.utils.asm.ToBytesClassAdapter;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

/**
 * Rewrites <code>"static boolean isDesignTime()"</code> to return <code>true</code>.
 * 
 * @author scheglov_ke
 * @coverage gwtHosted
 */
public final class GWTDesignTimeVisitor extends ToBytesClassAdapter {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GWTDesignTimeVisitor() {
    super(ClassWriter.COMPUTE_MAXS);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public MethodVisitor visitMethod(int access,
      String name,
      String desc,
      String signature,
      String[] exceptions) {
    // force "static boolean isDesignTime()" return "true"
    if ("isDesignTime".equals(name) && "()Z".equals(desc) && (access & ACC_STATIC) == ACC_STATIC) {
      MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
      return new MethodAdapter(mv) {
        @Override
        public void visitCode() {
          super.visitCode();
          mv.visitIntInsn(SIPUSH, 1);
          mv.visitInsn(IRETURN);
        }
      };
    }
    // any other method
    return super.visitMethod(access, name, desc, signature, exceptions);
  }
}
