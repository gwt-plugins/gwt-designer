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
package com.google.gdt.eclipse.designer.gxt.model.layout.assistant;

import com.google.gdt.eclipse.designer.gxt.model.property.Margins;

import org.eclipse.wb.core.controls.CSpinner;
import org.eclipse.wb.core.controls.CSpinnerDeferredNotifier;
import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

/**
 * GXT assistant for <code>Layout</code> and <code>LayoutData</code>.
 * 
 * @author sablin_aa
 * @coverage ExtGWT.model.layout.assistant
 */
public abstract class AbstractExtGwtAssistantPage extends AbstractAssistantPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractExtGwtAssistantPage(Composite parent, Object selection) {
    super(parent, selection);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Margins
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adapter for <code>Margins</code> property.
   */
  private final class MarginsPropertyInfo extends PropertyInfo {
    private final CSpinner m_allSpinner;
    private final CSpinner m_topSpinner;
    private final CSpinner m_bottomSpinner;
    private final CSpinner m_rightSpinner;
    private final CSpinner m_leftSpinner;
    private final Listener m_listener = new Listener() {
      public void handleEvent(Event event) {
        if (event.doit) {
          saveValue();
        }
      }
    };

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public MarginsPropertyInfo(String property,
        CSpinner allSpinner,
        CSpinner topSpinner,
        CSpinner rightSpinner,
        CSpinner bottomSpinner,
        CSpinner leftSpinner) {
      super(property);
      m_allSpinner = allSpinner;
      new CSpinnerDeferredNotifier(m_allSpinner, 500, new Listener() {
        public void handleEvent(Event event) {
          if (event.doit) {
            int all = m_allSpinner.getSelection();
            m_topSpinner.setSelection(all);
            m_rightSpinner.setSelection(all);
            m_bottomSpinner.setSelection(all);
            m_leftSpinner.setSelection(all);
            saveValue();
          }
        }
      });
      m_topSpinner = topSpinner;
      new CSpinnerDeferredNotifier(m_topSpinner, 500, m_listener);
      m_rightSpinner = rightSpinner;
      new CSpinnerDeferredNotifier(m_rightSpinner, 500, m_listener);
      m_bottomSpinner = bottomSpinner;
      new CSpinnerDeferredNotifier(m_bottomSpinner, 500, m_listener);
      m_leftSpinner = leftSpinner;
      new CSpinnerDeferredNotifier(m_leftSpinner, 500, m_listener);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // PropertyInfo
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void showValue() {
      Object value = getValue();
      int all = 0;
      if (Margins.isMargins(value)) {
        Margins marginsValue = new Margins(value);
        m_topSpinner.setSelection(marginsValue.top);
        m_rightSpinner.setSelection(marginsValue.right);
        m_bottomSpinner.setSelection(marginsValue.bottom);
        m_leftSpinner.setSelection(marginsValue.left);
        //
        if (marginsValue.top == marginsValue.right
            && marginsValue.right == marginsValue.bottom
            && marginsValue.bottom == marginsValue.left) {
          all = marginsValue.top;
        }
      } else {
        m_topSpinner.setSelection(0);
        m_rightSpinner.setSelection(0);
        m_bottomSpinner.setSelection(0);
        m_leftSpinner.setSelection(0);
      }
      m_allSpinner.setSelection(all);
    }

    @Override
    protected void doSaveValue() {
      Margins marginsValue = new Margins();
      marginsValue.top = m_topSpinner.getSelection();
      marginsValue.right = m_rightSpinner.getSelection();
      marginsValue.bottom = m_bottomSpinner.getSelection();
      marginsValue.left = m_leftSpinner.getSelection();
      setValue(marginsValue);
    }
  }

  /**
   * Create editor for single <code>Margins</code> property.
   */
  protected final Group addMarginProperty(Composite parent, String property, String title) {
    Group group = new Group(parent, SWT.NONE);
    group.setText(title);
    GridLayoutFactory.create(group).columns(2);
    MarginsPropertyInfo propertyInfo =
        new MarginsPropertyInfo(property, createSpiner(group, "Margin all:"), createSpiner(
            group,
            "Margin top:"), createSpiner(group, "Margin right:"), createSpiner(
            group,
            "Margin bottom:"), createSpiner(group, "Margin left:"));
    add(propertyInfo);
    return group;
  }

  private static CSpinner createSpiner(Composite parent, String title) {
    new Label(parent, SWT.NONE).setText(title);
    CSpinner spiner = new CSpinner(parent, SWT.BORDER);
    spiner.setMinimum(Integer.MIN_VALUE);
    spiner.setMaximum(Integer.MAX_VALUE);
    GridDataFactory.create(spiner).hintHC(10);
    return spiner;
  }
}