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
package com.google.gdt.eclipse.designer.util.type;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.ui.dialogs.ITypeInfoFilterExtension;

/**
 * Implementation of {@link IChooseBeanContributor} based on lists of valid/invalid types (and its
 * subtypes).
 * 
 * @author scheglov_ke
 * @coverage gwt.util.beanSelection
 */
public abstract class YesNoListChooseBeanContributor implements IChooseBeanContributor {
  private final String m_name;
  private final String[] m_yesTypes;
  private final String[] m_noTypes;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public YesNoListChooseBeanContributor(String name, String[] yesTypes, String[] noTypes) {
    m_name = name;
    m_yesTypes = yesTypes;
    m_noTypes = noTypes;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IChooseBeanContributor
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getName() {
    return m_name;
  }

  public ITypeInfoFilterExtension getFilter(IPackageFragment pkg, IProgressMonitor monitor) {
    return new YesNoFilter(m_yesTypes, m_noTypes, pkg, monitor);
  }
}
