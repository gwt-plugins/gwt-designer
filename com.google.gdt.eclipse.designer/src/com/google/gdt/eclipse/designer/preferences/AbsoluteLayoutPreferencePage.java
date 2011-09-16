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
package com.google.gdt.eclipse.designer.preferences;

import com.google.gdt.eclipse.designer.ToolkitProvider;

import org.eclipse.wb.core.controls.jface.preference.FieldLayoutPreferencePage;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.layout.absolute.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Main {@link PreferencePage} for GWT absolute-based panels.
 * 
 * @author mitin_aa
 * @coverage gwt.preferences.ui
 */
public final class AbsoluteLayoutPreferencePage extends FieldLayoutPreferencePage
    implements
      IWorkbenchPreferencePage,
      IPreferenceConstants {
  /**
   * @return The {@link IPreferenceStore} of GWT Toolkit Support plugin
   */
  @Override
  public IPreferenceStore getPreferenceStore() {
    return getToolkit().getPreferences();
  }

  /**
   * @return the {@link ToolkitDescription}.
   */
  private ToolkitDescription getToolkit() {
    return ToolkitProvider.DESCRIPTION;
  }

  @Override
  protected Control createPageContents(Composite parent) {
    Composite container = new Composite(parent, SWT.NULL);
    GridLayoutFactory.create(container);
    createBooleanFieldEditor(P_USE_FREE_MODE, "Use \"Free\" component placement style", container);
    {
      final Composite freeStylePropertiesComposite = new Composite(container, SWT.NONE);
      GridDataFactory.create(freeStylePropertiesComposite).fillH().alignVM();
      GridLayoutFactory.create(freeStylePropertiesComposite).marginsH(0).columns(3);
      {
        final Group componentGapsGroup = new Group(freeStylePropertiesComposite, SWT.NONE);
        GridLayoutFactory.create(componentGapsGroup);
        GridDataFactory.create(componentGapsGroup).fillV();
        componentGapsGroup.setText("Component Gaps");
        createIntegerFieldEditor(P_COMPONENT_GAP_LEFT, "Left", componentGapsGroup, true);
        createIntegerFieldEditor(P_COMPONENT_GAP_RIGHT, "Right", componentGapsGroup, true);
        createIntegerFieldEditor(P_COMPONENT_GAP_TOP, "Top", componentGapsGroup, true);
        createIntegerFieldEditor(P_COMPONENT_GAP_BOTTOM, "Bottom", componentGapsGroup, true);
      }
      {
        final Group containerGapsGroup = new Group(freeStylePropertiesComposite, SWT.NONE);
        GridLayoutFactory.create(containerGapsGroup);
        GridDataFactory.create(containerGapsGroup).fillV();
        containerGapsGroup.setText("Container Gaps");
        createIntegerFieldEditor(P_CONTAINER_GAP_LEFT, "Left", containerGapsGroup, true);
        createIntegerFieldEditor(P_CONTAINER_GAP_RIGHT, "Right", containerGapsGroup, true);
        createIntegerFieldEditor(P_CONTAINER_GAP_TOP, "Top", containerGapsGroup, true);
        createIntegerFieldEditor(P_CONTAINER_GAP_BOTTOM, "Bottom", containerGapsGroup, true);
      }
    }
    createBooleanFieldEditor(P_CREATION_FLOW, "Apply 'grid' creation flow", container);
    createBooleanFieldEditor(P_USE_GRID, "Use Grid Snapping", container);
    {
      final Group gridGroup = new Group(container, SWT.NONE);
      GridDataFactory.create(gridGroup).fillH().alignVM();
      gridGroup.setText("Grid Properties");
      GridLayoutFactory.create(gridGroup);
      createIntegerFieldEditor(P_GRID_STEP_X, "Grid Step X", gridGroup, false);
      createIntegerFieldEditor(P_GRID_STEP_Y, "Grid Step Y", gridGroup, false);
      createBooleanFieldEditor(P_DISPLAY_GRID, "Display Grid", gridGroup);
    }
    createBooleanFieldEditor(
        P_DISPLAY_LOCATION_SIZE_HINTS,
        "Display location/size hints",
        container);
    return container;
  }

  /**
   * Helper method Create {@link #BooleanFieldEditor} within own composite on specified parent
   */
  private void createBooleanFieldEditor(final String key, final String text, final Composite parent) {
    final Composite composite = new Composite(parent, SWT.NONE);
    addField(new BooleanFieldEditor(key, text, composite));
  }

  /**
   * Helper method Create {@link #IntegerFieldEditor} within own composite on specified parent
   * 
   * @param alignedRight
   *          if this parameter is true then align filed editor to the right
   */
  private void createIntegerFieldEditor(final String key,
      final String text,
      final Composite parent,
      boolean alignedRight) {
    final Composite composite = new Composite(parent, SWT.NONE);
    if (alignedRight) {
      GridDataFactory.create(composite).grabH().alignHR().alignVM();
    }
    // set min width of text control to 40
    IntegerFieldEditor integerFieldEditor = new IntegerFieldEditor(key, text, composite);
    Text textControl = integerFieldEditor.getTextControl(composite);
    GridDataFactory.modify(textControl).hintHC(5);
    addField(integerFieldEditor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  //	IWorkbenchPreferencePage
  //
  ////////////////////////////////////////////////////////////////////////////
  public void init(IWorkbench workbench) {
  }
}
