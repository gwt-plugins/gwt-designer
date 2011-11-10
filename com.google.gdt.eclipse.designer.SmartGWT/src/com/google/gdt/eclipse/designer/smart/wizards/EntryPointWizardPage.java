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
package com.google.gdt.eclipse.designer.smart.wizards;

import com.google.common.collect.ImmutableList;
import com.google.gdt.eclipse.designer.common.Constants;
import com.google.gdt.eclipse.designer.model.module.EntryPointElement;
import com.google.gdt.eclipse.designer.model.module.ModuleElement;
import com.google.gdt.eclipse.designer.smart.Activator;
import com.google.gdt.eclipse.designer.util.DefaultModuleProvider;
import com.google.gdt.eclipse.designer.util.DefaultModuleProvider.ModuleModification;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.List;

/**
 * @author sablin_aa
 * @coverage SmartGWT.wizard
 */
public final class EntryPointWizardPage extends SmartGwtWizardPage {
  private static final List<String> INTERFACES_NAMES =
      ImmutableList.of(Constants.CLASS_ENTRY_POINT);
  private final List<String> containerTypeNames = ImmutableList.of(
      "com.smartgwt.client.widgets.Canvas",
      "com.smartgwt.client.widgets.layout.VLayout",
      "com.smartgwt.client.widgets.layout.HLayout",
      "com.smartgwt.client.widgets.layout.FlowLayout",
      "com.smartgwt.client.widgets.calendar.Calendar");
  private Group m_containersGroup;
  private String m_selectedContainerName;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EntryPointWizardPage() {
    setTitle("Create EntryPoint");
    setImageDescriptor(Activator.getImageDescriptor("wizards/banner.png"));
    setDescription("Create EntryPoint");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createLocalControls(Composite parent, int nColumns) {
    m_containersGroup = new Group(parent, SWT.NONE);
    m_containersGroup.setText("Root container");
    GridDataFactory.create(m_containersGroup).spanH(nColumns).grab().fill();
    GridLayoutFactory.create(m_containersGroup);
    boolean selectFirst = true;
    for (String containerTypeName : containerTypeNames) {
      Button button = new Button(m_containersGroup, SWT.RADIO);
      button.setText(containerTypeName);
      button.addSelectionListener(m_radiolistener);
      if (selectFirst) {
        button.setSelection(selectFirst);
        m_selectedContainerName = containerTypeName;
        selectFirst = false;
      }
    }
  }

  private final SelectionListener m_radiolistener = new SelectionListener() {
    public void widgetDefaultSelected(SelectionEvent e) {
    }

    public void widgetSelected(SelectionEvent e) {
      Button button = (Button) e.widget;
      m_selectedContainerName = button.getText();
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Configuration
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initTypePage(IJavaElement elem) {
    super.initTypePage(elem);
    setSuperInterfaces(INTERFACES_NAMES, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Type creation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createTypeMembers(final IType newType,
      ImportsManager imports,
      IProgressMonitor monitor) throws CoreException {
    // create EntryPoint class
    String shortClassName = CodeUtils.getShortClass(m_selectedContainerName);
    InputStream stream = getTemplate2("EntryPoint" + shortClassName + ".jvt");
    try {
      fillTypeFromTemplate(newType, imports, monitor, stream);
    } finally {
      IOUtils.closeQuietly(stream);
    }
    // modify module file
    try {
      ModuleDescription module = Utils.getSingleModule(newType);
      DefaultModuleProvider.modify(module, new ModuleModification() {
        public void modify(ModuleElement moduleElement) throws Exception {
          EntryPointElement entryPointElement = new EntryPointElement();
          moduleElement.addChild(entryPointElement);
          entryPointElement.setClassName(newType.getFullyQualifiedName());
        }
      });
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }
}