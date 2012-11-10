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
package com.google.gdt.eclipse.designer.uibinder.model.util;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gdt.eclipse.designer.model.widgets.GwtHierarchyProvider;
import com.google.gdt.eclipse.designer.uibinder.editor.UiBinderPairResourceProvider;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.EditorActivatedListener;
import org.eclipse.wb.core.model.broadcast.EditorActivatedRequest;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.xml.model.IRootProcessor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Processor for tracking <code>ui.xml</code> template changes and requesting reparsing in both Java
 * and UiBinder versions of GWT.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public final class TemplateChangedRootProcessor
    implements
      org.eclipse.wb.core.model.IRootProcessor,
      org.eclipse.wb.internal.core.xml.model.IRootProcessor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IRootProcessor INSTANCE = new TemplateChangedRootProcessor();

  private TemplateChangedRootProcessor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IRootProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(XmlObjectInfo root) throws Exception {
    reparseOnTemplateModification(root);
  }

  public void process(JavaInfo root, List<JavaInfo> components) throws Exception {
    reparseOnTemplateModification(root);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  private static void reparseOnTemplateModification(ObjectInfo rootObject) {
    final Map<IFile, Long> templateStamps = getReferencedTemplates(rootObject);
    // wait for EditorActivatedRequest
    rootObject.addBroadcastListener(new EditorActivatedListener() {
      public void invoke(EditorActivatedRequest request) throws Exception {
        if (hasModifiedTemplate()) {
          request.requestReparse();
        }
      }

      private boolean hasModifiedTemplate() {
        for (Entry<IFile, Long> entry : templateStamps.entrySet()) {
          IFile file = entry.getKey();
          if (file.getModificationStamp() != entry.getValue()) {
            return true;
          }
        }
        return false;
      }
    });
  }

  private static Map<IFile, Long> getReferencedTemplates(final ObjectInfo rootObject) {
    final IJavaProject javaProject = (IJavaProject) GlobalState.getOtherHelper().getJavaProject();
    final Map<IFile, Long> templateStamps = Maps.newHashMap();
    final Set<Object> visitedWidgets = Sets.newHashSet();
    final Set<Class<?>> checkedTypes = Sets.newHashSet();
    rootObject.accept(new ObjectInfoVisitor() {
      @Override
      public void endVisit(ObjectInfo model) throws Exception {
        Object object = GlobalState.getOtherHelper().getObject(model);
        if (object != null) {
          visitWidgets(object);
        }
      }

      private void visitWidgets(Object widget) throws Exception {
        if (visitedWidgets.contains(widget)) {
          return;
        }
        visitedWidgets.add(widget);
        // try to find template for this widget
        {
          IFile templateFile = getTemplateFile(widget);
          if (templateFile != null) {
            templateStamps.put(templateFile, templateFile.getModificationStamp());
          }
        }
        // visit children
        Object[] children = GwtHierarchyProvider.INSTANCE.getChildrenObjects(widget);
        for (Object child : children) {
          visitWidgets(child);
        }
      }

      private IFile getTemplateFile(Object widget) throws Exception {
        Class<?> componentClass = widget.getClass();
        // check each Class only once
        if (checkedTypes.contains(componentClass)) {
          return null;
        }
        checkedTypes.add(componentClass);
        // check this Class
        String componentClassName = ReflectionUtils.getCanonicalName(componentClass);
        IType componentType = javaProject.findType(componentClassName);
        if (componentType != null) {
          ICompilationUnit componentUnit = componentType.getCompilationUnit();
          if (componentUnit != null) {
            IFile componentFile = (IFile) componentUnit.getUnderlyingResource();
            if (componentFile != null) {
              return UiBinderPairResourceProvider.INSTANCE.getPair(componentFile);
            }
          }
        }
        return null;
      }
    });
    return templateStamps;
  }
}
