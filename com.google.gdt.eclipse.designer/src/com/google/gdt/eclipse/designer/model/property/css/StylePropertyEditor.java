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
package com.google.gdt.eclipse.designer.model.property.css;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.Activator;
import com.google.gdt.eclipse.designer.model.widgets.support.IGwtStateProvider;

import org.eclipse.wb.core.controls.CComboBox;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.ObjectProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.AbstractComboBoxPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.StringComboPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.presentation.ButtonPropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.editor.presentation.CompoundPropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.editor.presentation.PropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.css.model.CssRuleNode;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Combo {@link PropertyEditor} for selecting/editing CSS style.
 * 
 * @author sablin_aa
 * @author scheglov_ke
 * @coverage gwt.model.property
 */
public final class StylePropertyEditor extends AbstractComboBoxPropertyEditor
    implements
      IComplexPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final StylePropertyEditor INSTANCE = new StylePropertyEditor();

  private StylePropertyEditor() {
    m_compoundPresentation.add(m_presentationNew);
    m_compoundPresentation.add(m_presentationEdit);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public PropertyEditorPresentation getPresentation() {
    return m_compoundPresentation;
  }

  @Override
  protected String getText(Property property) throws Exception {
    Object value = property.getValue();
    if (value instanceof String) {
      return (String) value;
    }
    return null;
  }

  private final CompoundPropertyEditorPresentation m_compoundPresentation =
      new CompoundPropertyEditorPresentation();
  private final PropertyEditorPresentation m_presentationEdit =
      new ButtonPropertyEditorPresentation() {
        @Override
        protected void onClick(PropertyTable propertyTable, Property property) throws Exception {
          openEditDialog(property);
        }

        @Override
        protected Image getImage() {
          return Activator.getImage("css_editor2.png");
        }
      };
  private final PropertyEditorPresentation m_presentationNew =
      new ButtonPropertyEditorPresentation() {
        @Override
        protected void onClick(PropertyTable propertyTable, Property property) throws Exception {
          openNewDialog(property);
        }

        @Override
        protected Image getImage() {
          return Activator.getImage("css_addStyle.png");
        }
      };

  private void openEditDialog(Property property) throws Exception {
    ObjectProperty objectProperty = (ObjectProperty) property;
    ObjectInfo objectInfo = objectProperty.getObjectInfo();
    IGwtStateProvider stateProvider = (IGwtStateProvider) objectInfo;
    // prepare not empty array of CSS files
    List<IFile> cssFiles = stateProvider.getState().getCssSupport().getFiles();
    if (cssFiles.isEmpty()) {
      MessageDialog.openError(
          DesignerPlugin.getShell(),
          "Error",
          "There are no CSS files referenced from module or HTML.");
      return;
    }
    // prepare dialog
    StylesEditDialog stylesDialog;
    {
      String currentText = getText(property);
      stylesDialog =
          new StylesEditDialog(DesignerPlugin.getShell(),
              cssFiles,
              StringUtils.isEmpty(currentText) ? "" : "." + currentText,
              true);
    }
    // open dialog and edit CSS files
    int result = stylesDialog.open();
    if (result == Window.CANCEL) {
      return;
    }
    // wait for auto-build - for coping CSS files from source folder to binary
    ProjectUtils.waitForAutoBuild();
    // check CSS files modification
    stateProvider.getState().isModified();
    // do edit
    switch (result) {
      case Window.OK :
        // "OK" button pressed, refresh for displaying modified styles
        ExecutionUtils.refresh(objectInfo);
        break;
      case IDialogConstants.PROCEED_ID :
        // "Apply" button pressed, set new style name
        String newSelector = stylesDialog.getSelectionValue();
        String newStyleName = extractStyleName(newSelector);
        property.setValue(newStyleName);
        break;
    }
  }

  private static String extractStyleName(String selector) {
    return StringUtils.removeStart(selector, ".");
  }

  private void openNewDialog(Property property) throws Exception {
    ObjectProperty objectProperty = (ObjectProperty) property;
    ObjectInfo objectInfo = objectProperty.getObjectInfo();
    IGwtStateProvider stateProvider = (IGwtStateProvider) objectInfo;
    // prepare default name
    String initialName = "newStyleName";
    if (property.getValue() instanceof String) {
      initialName = (String) property.getValue();
    }
    // ask for style name
    InputDialog inputDialog =
        new InputDialog(DesignerPlugin.getShell(),
            "New style name",
            "Enter new style name (without leading '.'):",
            initialName,
            null);
    if (inputDialog.open() == Window.CANCEL) {
      return;
    }
    String newStyleName = inputDialog.getValue();
    // actually add style
    {
      RuleAccessor ruleAccessor = RuleAccessor.get(objectInfo);
      List<ContextDescription> contexts = ruleAccessor.getContexts();
      ContextDescription context = getTargetContextDescription(contexts, newStyleName);
      if (context != null) {
        newStyleName = context.addNewStyle(newStyleName);
        context.commit();
      }
    }
    // check CSS files modification
    stateProvider.getState().isModified();
    // apply style name
    property.setValue(newStyleName);
  }

  /**
   * @return the {@link ContextDescription} for given new style name, e.g. file based for "gwt-"
   *         style, may be <code>null</code>.
   */
  private static ContextDescription getTargetContextDescription(List<ContextDescription> contexts,
      String newStyleName) {
    if (newStyleName.startsWith("gwt-")) {
      for (ContextDescription context : contexts) {
        if (context instanceof FileContextDescription) {
          return context;
        }
      }
    }
    if (!contexts.isEmpty()) {
      return contexts.get(0);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addItems(Property property, CComboBox combo) throws Exception {
    ObjectProperty objectProperty = (ObjectProperty) property;
    // prepare accessor
    ObjectInfo objectInfo = objectProperty.getObjectInfo();
    RuleAccessor accessor = RuleAccessor.get(objectInfo);
    // prepare style names
    List<String> styleNames = Lists.newArrayList();
    for (ContextDescription contextDescription : accessor.getContexts()) {
      for (CssRuleNode ruleNode : contextDescription.getRules()) {
        String styleName = contextDescription.getStyleName(ruleNode);
        if (styleName != null) {
          styleNames.add(styleName);
        }
      }
    }
    // sort and fill combo box items
    Collections.sort(styleNames);
    for (String styleName : styleNames) {
      combo.addItem(styleName);
    }
    combo.setFullDropdownTableWidth(true);
  }

  @Override
  protected void selectItem(Property property, CComboBox combo) throws Exception {
    String currentStyleName = getText(property);
    int itemCount = combo.getItemCount();
    for (int i = 0; i < itemCount; i++) {
      if (combo.getItemLabel(i).equals(currentStyleName)) {
        combo.setSelectionIndex(i);
        break;
      }
    }
  }

  @Override
  protected void toPropertyEx(Property property, CComboBox combo) throws Exception {
    String styleName = combo.getEditText();
    if (!StringUtils.isBlank(styleName) && !StringUtils.equals(styleName, getText(property))) {
      property.setValue(StringUtils.isEmpty(styleName) ? Property.UNKNOWN_VALUE : styleName);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IComplexPropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  public Property[] getProperties(Property property) throws Exception {
    final String styleName = getText(property);
    // prepare model
    ObjectProperty objectProperty = (ObjectProperty) property;
    ObjectInfo objectInfo = objectProperty.getObjectInfo();
    // use RuleAccessor
    final RuleAccessor accessor = RuleAccessor.get(objectInfo);
    if (accessor.hasSemantics(styleName)) {
      return new Property[]{
          new StyleColorValueProperty(accessor, styleName, "color", "color"),
          new StyleComplexProperty(accessor,
              "background",
              new Property[]{
                  new StyleColorValueProperty(accessor, styleName, "background.color", "color"),
                  new StyleSimpleValueProperty(accessor,
                      styleName,
                      "background.image",
                      "image",
                      StringPropertyEditor.INSTANCE),
                  new StyleSimpleValueProperty(accessor,
                      styleName,
                      "background.repeat",
                      "repeat",
                      new StringComboPropertyEditor("",
                          "repeat",
                          "repeat-x",
                          "repeat-y",
                          "no-repeat")),
                  new StyleSimpleValueProperty(accessor,
                      styleName,
                      "background.attachment",
                      "attachment",
                      new StringComboPropertyEditor("", "scroll", "fixed"))}),
          new StyleBorderProperty(accessor, styleName),
          new StyleFontProperty(accessor, styleName),
          new StyleTextProperty(accessor, styleName),
          new StyleLengthSidedProperty(accessor, styleName, "margin", "margin"),
          new StyleLengthSidedProperty(accessor, styleName, "padding", "padding"),};
    }
    return new Property[0];
  }
}
