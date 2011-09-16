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
package com.google.gdt.eclipse.designer.util.type;

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.dialogs.ITypeInfoFilterExtension;
import org.eclipse.jdt.ui.dialogs.ITypeInfoRequestor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link ITypeInfoFilterExtension} based on lists of valid/invalid types (and its
 * subtypes).
 * 
 * @author scheglov_ke
 * @coverage gwt.util.beanSelection
 */
public final class YesNoFilter implements ITypeInfoFilterExtension {
  private final String[] m_yesTypes;
  private final String[] m_noTypes;
  private List<String> m_yesTypeFQNs;
  private List<String> m_noTypeFQNs;
  private final IPackageFragment m_package;
  private final IProgressMonitor m_monitor;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public YesNoFilter(String[] yesTypes,
      String[] noTypes,
      IPackageFragment pkg,
      IProgressMonitor monitor) {
    m_yesTypes = yesTypes;
    m_noTypes = noTypes;
    m_package = pkg;
    m_monitor = monitor;
    // force types fetching
    {
      int num =
          (yesTypes == null ? 0 : yesTypes.length / 2) + (noTypes == null ? 0 : noTypes.length / 2);
      m_monitor.beginTask("Calculating dependencies for type: ", num);
      getYesTypeFQNs();
      getNoTypeFQNs();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ITypeInfoFilterExtension
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean select(ITypeInfoRequestor typeInfoRequestor) {
    StringBuffer fqnBuffer = new StringBuffer();
    if (typeInfoRequestor.getPackageName() != null
        && typeInfoRequestor.getPackageName().length() > 0) {
      fqnBuffer.append(typeInfoRequestor.getPackageName());
      fqnBuffer.append("."); //$NON-NLS-1$
    }
    if (typeInfoRequestor.getEnclosingName() != null
        && typeInfoRequestor.getEnclosingName().length() > 0) {
      fqnBuffer.append(typeInfoRequestor.getEnclosingName());
      fqnBuffer.append("."); //$NON-NLS-1$
    }
    if (typeInfoRequestor.getTypeName() != null && typeInfoRequestor.getTypeName().length() > 0) {
      fqnBuffer.append(typeInfoRequestor.getTypeName());
    }
    String fqn = fqnBuffer.toString();
    if (!getNoTypeFQNs().contains(fqn) && getYesTypeFQNs().contains(fqn)) {
      return true;
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Yes/No types
  //
  ////////////////////////////////////////////////////////////////////////////
  private List<String> getYesTypeFQNs() {
    if (m_yesTypeFQNs == null) {
      m_yesTypeFQNs = getSubTypes(m_yesTypes);
    }
    return m_yesTypeFQNs;
  }

  private List<String> getNoTypeFQNs() {
    if (m_noTypeFQNs == null) {
      m_noTypeFQNs = getSubTypes(m_noTypes);
    }
    return m_noTypeFQNs;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Type selection support
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Expects {"pkg1","class1", "pkg2", "class2"}
   */
  private List<String> getSubTypes(String[] types) {
    if (types == null) {
      return Collections.emptyList();
    }
    //
    IJavaProject javaProject = m_package.getJavaProject();
    List<String> subTypesList = new ArrayList<String>();
    for (int baseIndex = 0; baseIndex < types.length; baseIndex += 2) {
      try {
        IType baseType = javaProject.findType(types[baseIndex], types[baseIndex + 1]);
        if (baseType != null) {
          subTypesList.add(baseType.getFullyQualifiedName());
          // prepare sub-types
          IType[] subTypes;
          {
            m_monitor.subTask(baseType.getFullyQualifiedName());
            ITypeHierarchy th = baseType.newTypeHierarchy(javaProject, new NullProgressMonitor());
            subTypes = th.getAllSubtypes(baseType);
            m_monitor.worked(1);
          }
          //
          for (int subTypeIndex = 0; subTypeIndex < subTypes.length; subTypeIndex++) {
            subTypesList.add(subTypes[subTypeIndex].getFullyQualifiedName());
          }
        }
      } catch (JavaModelException e) {
        DesignerPlugin.log(e);
      }
    }
    return subTypesList;
  }
}
