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
package com.google.gdt.eclipse.designer.gwtext.model.widgets;

import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.util.List;

/**
 * When GWT-Ext creates <code>Component</code>, it sets it new unique ID. This ID is used for
 * checking that component instance was rendered, etc. If component was rendered, we can not set
 * some properties, such as layout. Usually this is not problem, because for each instance of
 * Component, new ID is generated.
 * <p>
 * However when we sets some specific ID for component, GWT-Ext thinks after refresh that we still
 * mean old component, that was rendered, so prevents <code>setLayout()</code> execution.
 * <p>
 * We should somehow dispose/destroy components.
 * 
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage GWTExt.model
 */
public final class ComponentRootProcessor implements IRootProcessor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IRootProcessor INSTANCE = new ComponentRootProcessor();

  private ComponentRootProcessor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IRootProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(final JavaInfo root, List<JavaInfo> components) throws Exception {
    root.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void refreshDispose() throws Exception {
        destroyComponents(root);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Destroy
  //
  ////////////////////////////////////////////////////////////////////////////
  private static void destroyComponents(final JavaInfo root) {
    ExecutionUtils.runIgnore(new RunnableEx() {
      public void run() throws Exception {
        destroyComponents0(root);
      }
    });
  }

  private static void destroyComponents0(JavaInfo root) throws Exception {
    root.accept(new ObjectInfoVisitor() {
      @Override
      public void endVisit(ObjectInfo objectInfo) throws Exception {
        if (objectInfo instanceof ComponentInfo) {
          ComponentInfo componentInfo = (ComponentInfo) objectInfo;
          Object componentObject = componentInfo.getObject();
          if (componentObject != null) {
            ReflectionUtils.invokeMethod(componentObject, "destroy()");
          }
        }
      }
    });
  }
}