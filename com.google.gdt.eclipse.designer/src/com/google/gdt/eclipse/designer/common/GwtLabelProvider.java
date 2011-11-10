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
package com.google.gdt.eclipse.designer.common;

import com.google.gdt.eclipse.designer.util.ModuleDescription;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;

/**
 * Label provider for GWT objects.
 * 
 * @author scheglov_ke
 * @coverage gwt.common
 */
public final class GwtLabelProvider extends LabelProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final ILabelProvider INSTANCE = new GwtLabelProvider();

  private GwtLabelProvider() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LabelProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getText(Object element) {
    if (element == null) {
      return "";
    } else if (element instanceof ModuleDescription) {
      return ((ModuleDescription) element).getId();
    }
    return super.getText(element);
  }
}
