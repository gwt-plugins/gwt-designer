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
package com.google.gdt.eclipse.designer.gxt.model.widgets;

import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * The processor for cleaning up GXT static state.
 * <p>
 * http://www.instantiations.com/forum/viewtopic.php?f=11&t=3704
 * 
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage ExtGWT.model
 */
public final class GxtCleanupRootProcessor implements IRootProcessor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IRootProcessor INSTANCE = new GxtCleanupRootProcessor();

  private GxtCleanupRootProcessor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IRootProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(JavaInfo root, List<JavaInfo> components) throws Exception {
    cleanup_IconHelper(root);
    cleanup_TextMetrics(root);
  }

  private void cleanup_IconHelper(JavaInfo root) {
    try {
      final ClassLoader classLoader = JavaInfoUtils.getClassLoader(root);
      final Class<?> classIconHelper =
          classLoader.loadClass("com.extjs.gxt.ui.client.util.IconHelper");
      root.addBroadcastListener(new ObjectEventListener() {
        @Override
        public void refreshDispose() throws Exception {
          // 2.0.1
          {
            Field field = ReflectionUtils.getFieldByName(classIconHelper, "initialized");
            if (field != null) {
              field.set(null, false);
            }
          }
          // 2.1.0
          {
            Field field = ReflectionUtils.getFieldByName(classIconHelper, "cacheMap");
            if (field != null) {
              field.set(null, null);
            }
          }
        }
      });
    } catch (Throwable e) {
    }
  }

  private void cleanup_TextMetrics(JavaInfo root) {
    try {
      final ClassLoader classLoader = JavaInfoUtils.getClassLoader(root);
      final Class<?> classTextMetics =
          classLoader.loadClass("com.extjs.gxt.ui.client.util.TextMetrics");
      root.addBroadcastListener(new ObjectEventListener() {
        @Override
        public void refreshDispose() throws Exception {
          ReflectionUtils.setField(classTextMetics, "instance", null);
        }
      });
    } catch (Throwable e) {
    }
  }
}
