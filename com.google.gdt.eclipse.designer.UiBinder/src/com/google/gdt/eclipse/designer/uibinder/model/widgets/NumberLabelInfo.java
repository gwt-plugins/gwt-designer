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
package com.google.gdt.eclipse.designer.uibinder.model.widgets;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.google.gdt.eclipse.designer.uibinder.Activator;
import com.google.gdt.eclipse.designer.util.GwtInvocationEvaluatorInterceptor;

import org.eclipse.wb.core.controls.BrowserComposite;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.JavaDocUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.property.XmlProperty;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * Model for <code>com.google.gwt.user.client.ui.NumberLabel</code> in GWT UiBinder.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public final class NumberLabelInfo extends ValueLabelInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NumberLabelInfo(EditorContext context,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(context, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refresh_afterCreate() throws Exception {
    super.refresh_afterCreate();
    GwtInvocationEvaluatorInterceptor.setValueLabelText(getObject(), "NumberLabel");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Property m_formatProperty = new NumberFormatProperty(this);

  @Override
  protected List<Property> getPropertyList() throws Exception {
    List<Property> properties = super.getPropertyList();
    properties.add(m_formatProperty);
    return properties;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // NumberFormatProperty
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class NumberFormatProperty extends XmlProperty {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public NumberFormatProperty(XmlObjectInfo object) {
      super(object, "format", PropertyCategory.PREFERRED, NumberFormatPropertyEditor.INSTANCE);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Property
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean isModified() throws Exception {
      return getValue() != null;
    }

    @Override
    public Object getValue() throws Exception {
      String currencyCode = m_object.getAttribute("currencyCode");
      // format
      {
        String format = m_object.getAttribute("format");
        if (format != null) {
          return format;
        }
      }
      // predefinedFormat
      {
        String format = m_object.getAttribute("predefinedFormat");
        if (format != null) {
          if (format.equals("CURRENCY") && currencyCode != null) {
            return format + " " + currencyCode;
          }
          return format;
        }
      }
      // customFormat
      {
        String format = m_object.getAttribute("customFormat");
        if (format != null) {
          if (currencyCode != null) {
            return format + " " + currencyCode;
          }
          return format;
        }
      }
      // no format
      return null;
    }

    @Override
    public void setValueEx(Object value) throws Exception {
      clearAttributes();
      if (value instanceof String[]) {
        String[] attributes = (String[]) value;
        // format
        m_object.setAttribute(attributes[0], attributes[1]);
        // currency
        if (attributes[2] != null) {
          m_object.setAttribute("currencyCode", attributes[2]);
        }
      }
    }

    private void clearAttributes() {
      m_object.removeAttribute("format");
      m_object.removeAttribute("predefinedFormat");
      m_object.removeAttribute("customFormat");
      m_object.removeAttribute("currencyData");
      m_object.removeAttribute("currencyCode");
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // NumberFormatPropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class NumberFormatPropertyEditor extends TextDialogPropertyEditor {
    private static final PropertyEditor INSTANCE = new NumberFormatPropertyEditor();

    ////////////////////////////////////////////////////////////////////////////
    //
    // Presentation
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected String getText(Property property) throws Exception {
      return (String) property.getValue();
    }

    @Override
    protected void openDialog(Property property) throws Exception {
      NumberFormatProperty formatProperty = (NumberFormatProperty) property;
      XmlObjectInfo object = formatProperty.getObject();
      NumberFormatDialog dialog = new NumberFormatDialog(object);
      if (dialog.open() == Window.OK) {
        formatProperty.setValue(dialog.getResult());
      }
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // NumberFormatDialog
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class NumberFormatDialog extends ResizableDialog {
    private final XmlObjectInfo m_object;
    private Button m_predefinedButton;
    private Combo m_predefinedCombo;
    private Button m_customButton;
    private Text m_customText;
    private Button m_currencyButton;
    private Combo m_currencyCombo;
    private String[] m_result;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    protected NumberFormatDialog(XmlObjectInfo object) {
      super(DesignerPlugin.getShell(), Activator.getDefault());
      m_object = object;
      setShellStyle(SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.RESIZE);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * @return the result of editing, should be passed into {@link NumberFormatProperty}.
     */
    public String[] getResult() {
      return m_result;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // GUI
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("format");
    }

    @Override
    protected void okPressed() {
      String currencyCode =
          m_currencyButton.isEnabled() && m_currencyButton.getSelection()
              ? m_currencyCombo.getText()
              : null;
      if (m_predefinedButton.getSelection()) {
        m_result = new String[]{"predefinedFormat", m_predefinedCombo.getText(), currencyCode};
      }
      if (m_customButton.getSelection()) {
        m_result = new String[]{"customFormat", m_customText.getText(), currencyCode};
      }
      super.okPressed();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
      Composite dialogArea = (Composite) super.createDialogArea(parent);
      GridLayoutFactory.modify(dialogArea).columns(2);
      // predefined
      {
        {
          m_predefinedButton = new Button(dialogArea, SWT.RADIO);
          m_predefinedButton.setText("Predefined format:");
        }
        {
          m_predefinedCombo = new Combo(dialogArea, SWT.READ_ONLY);
          GridDataFactory.create(m_predefinedCombo).grabH().fillH();
          m_predefinedCombo.setItems(new String[]{"DECIMAL", "CURRENCY", "PERCENT", "SCIENTIFIC"});
        }
        {
          String format = m_object.getAttribute("predefinedFormat");
          m_predefinedButton.setSelection(format != null);
          if (format != null) {
            m_predefinedCombo.setText(format);
          }
        }
      }
      // custom
      {
        {
          m_customButton = new Button(dialogArea, SWT.RADIO);
          m_customButton.setText("Custom format:");
        }
        {
          m_customText = new Text(dialogArea, SWT.BORDER);
          GridDataFactory.create(m_customText).hintHC(64).grabH().fillH();
        }
        {
          String format = m_object.getAttribute("customFormat");
          m_customButton.setSelection(format != null);
          if (format != null) {
            m_customText.setText(format);
          }
        }
      }
      // currency
      {
        m_currencyButton = new Button(dialogArea, SWT.CHECK);
        m_currencyButton.setText("Currency code:");
      }
      {
        m_currencyCombo = new Combo(dialogArea, SWT.READ_ONLY);
        GridDataFactory.create(m_currencyCombo).grabH().fillH();
        for (String currencyCode : getCurrencyCodes()) {
          m_currencyCombo.add(currencyCode);
        }
      }
      {
        String currencyCode = m_object.getAttribute("currencyCode");
        m_currencyButton.setSelection(currencyCode != null);
        if (currencyCode != null) {
          m_currencyCombo.setText(currencyCode);
        }
      }
      // update enablement
      {
        Listener listener = new Listener() {
          public void handleEvent(Event event) {
            updateEnabledFormat();
          }
        };
        m_predefinedButton.addListener(SWT.Selection, listener);
        m_predefinedCombo.addListener(SWT.Selection, listener);
        m_customButton.addListener(SWT.Selection, listener);
        m_currencyButton.addListener(SWT.Selection, listener);
        updateEnabledFormat();
      }
      // JavaDoc
      {
        final BrowserComposite browserComposite = new BrowserComposite(dialogArea, SWT.BORDER);
        GridDataFactory.create(browserComposite).spanH(2).hintC(60, 10).grab().fill();
        ExecutionUtils.runIgnore(new RunnableEx() {
          public void run() throws Exception {
            IJavaProject javaProject = m_object.getContext().getJavaProject();
            IType type = javaProject.findType("com.google.gwt.i18n.client.NumberFormat");
            List<String> lines = JavaDocUtils.getJavaDocLines(type, false);
            String text = Joiner.on(" ").join(lines);
            browserComposite.setText(text);
          }
        });
      }
      return dialogArea;
    }

    private void updateEnabledFormat() {
      // predefined
      boolean isPredefined = m_predefinedButton.getSelection();
      m_predefinedCombo.setEnabled(isPredefined);
      if (StringUtils.isEmpty(m_predefinedCombo.getText())) {
        m_predefinedCombo.select(0);
      }
      // custom
      boolean isCustom = m_customButton.getSelection();
      m_customText.setEnabled(isCustom);
      if (StringUtils.isEmpty(m_customText.getText())) {
        m_customText.setText("0.0");
      }
      // currency
      {
        boolean canHaveCurrency =
            isPredefined && "CURRENCY".equals(m_predefinedCombo.getText()) || isCustom;
        boolean hasCurrency = m_currencyButton.getSelection();
        m_currencyButton.setEnabled(canHaveCurrency);
        m_currencyCombo.setEnabled(canHaveCurrency && hasCurrency);
        if (StringUtils.isEmpty(m_currencyCombo.getText())) {
          m_currencyCombo.select(0);
        }
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Currency
    //
    ////////////////////////////////////////////////////////////////////////////
    private static final Collection<String> m_currencyCodes = Sets.newTreeSet();

    private Collection<String> getCurrencyCodes() {
      if (m_currencyCodes.isEmpty()) {
        Locale[] availableLocales = Locale.getAvailableLocales();
        for (Locale locale : availableLocales) {
          try {
            Currency currency = Currency.getInstance(locale);
            m_currencyCodes.add(currency.getCurrencyCode());
          } catch (Throwable e) {
          }
        }
      }
      return m_currencyCodes;
    }
  }
}
