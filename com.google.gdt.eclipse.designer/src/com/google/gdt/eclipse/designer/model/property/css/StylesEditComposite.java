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

import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.Activator;
import com.google.gdt.eclipse.designer.common.Constants;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.StringItemDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.StringItemDialogField.IStringItemAdapter;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.css.dialogs.style.StyleEditDialog;
import org.eclipse.wb.internal.css.dialogs.style.StyleEditOptions;
import org.eclipse.wb.internal.css.model.CssDocument;
import org.eclipse.wb.internal.css.model.CssRuleNode;
import org.eclipse.wb.internal.css.parser.CssEditContext;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Dialog for editing CSS file.
 * 
 * @author sablin_aa
 * @author scheglov_ke
 */
public class StylesEditComposite
    extends
      org.eclipse.wb.internal.css.editors.multi.StylesEditComposite {
  private final List<IFile> m_files;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StylesEditComposite(Composite parent,
      List<IFile> files,
      int style,
      ICommandExceptionHandler exceptionHandler) {
    super(parent, style, exceptionHandler);
    m_files = files;
    m_filesViewer.setInput(m_files);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  protected StringItemDialogField m_searchField;
  protected ListViewer m_filesViewer;

  @Override
  protected void createRulesGroup(Composite parent) {
    Composite explorerComposite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.create(explorerComposite).columns(1);
    GridDataFactory.create(explorerComposite).grab().fill();
    createFileGroup(explorerComposite);
    super.createRulesGroup(explorerComposite);
  }

  /**
   * Create CSS-file viewer.
   */
  protected void createFileGroup(Composite parent) {
    final int columns = 3;
    Group filesGroup = new Group(parent, SWT.NONE);
    filesGroup.setText("CSS files referenced from HTML");
    GridLayoutFactory.create(filesGroup).columns(columns);
    GridDataFactory.create(filesGroup).grabH().fill();
    // search
    {
      m_searchField = new StringItemDialogField(new IStringItemAdapter() {
        public void itemPressed(DialogField field) {
          searchRule();
        }
      });
      m_searchField.setLabelText("&Search CSS rule in files:");
      m_searchField.setItemImage(DesignerPlugin.getImage("find.png"));
      m_searchField.setItemToolTip("Search");
      m_searchField.doFillIntoGrid(filesGroup, 3);
    }
    {
      m_filesViewer = new ListViewer(filesGroup, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
      GridDataFactory.create(m_filesViewer.getControl()).spanH(columns).grab().fill().hintC(80, 6);
      // set providers and input
      {
        m_filesViewer.setContentProvider(new DefaultStructuredContextProvider() {
          @SuppressWarnings("unchecked")
          public Object[] getElements(Object inputElement) {
            List<IFile> files = (List<IFile>) inputElement;
            return files.toArray();
          }
        });
        m_filesViewer.setLabelProvider(new LabelProvider() {
          @Override
          public String getText(Object element) {
            IFile file = (IFile) element;
            return file.getFullPath().toPortableString();
          }
        });
      }
      // add selection listener
      m_filesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
        public void selectionChanged(SelectionChangedEvent event) {
          IFile file = (IFile) ((IStructuredSelection) event.getSelection()).getFirstElement();
          setContext(getContext(file));
          {
            Object firstElement = m_rulesViewer.getElementAt(0);
            if (firstElement != null) {
              m_rulesViewer.setSelection(new StructuredSelection(firstElement));
            }
          }
        }
      });
    }
  }

  public void initializeState(String selectionValue) {
    // always show rules of first CSS file
    if (!m_files.isEmpty()) {
      m_initialValue = selectionValue;
      // locate initial CSS style
      if (!StringUtils.isEmpty(selectionValue)) {
        if (locateRuleInFiles(selectionValue)) {
          return;
        }
      }
      m_filesViewer.setSelection(new StructuredSelection(m_files.get(0)));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI: Edit actions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected StyleEditDialog getStyleEditDialog(CssRuleNode rule) {
    StyleEditDialog dialog = super.getStyleEditDialog(rule);
    StyleEditOptions options = dialog.getOptions();
    IPreferenceStore store = Activator.getStore();
    options.useNamedColors = store.getBoolean(Constants.P_CSS_USE_NAMED_COLORS);
    return dialog;
  }

  protected String m_initialValue = null;

  @Override
  protected String getDefaultNewSelector() {
    if (StringUtils.isEmpty(m_initialValue)) {
      return super.getDefaultNewSelector();
    } else {
      if (isSelectorExists(m_initialValue)) {
        return m_initialValue + "-new";
      } else {
        return m_initialValue;
      }
    }
  }

  protected final boolean isSelectorExists(String selector) {
    CssDocument document = getContext().getCssDocument();
    List<CssRuleNode> rules = document.getRules();
    for (CssRuleNode rule : rules) {
      String ruleSelector = rule.getSelector().getValue();
      if (selector.equalsIgnoreCase(ruleSelector)) {
        return true;
      }
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns {@link IFile} for selected CSS file.
   */
  protected IFile getSelectedFile() {
    return (IFile) ((IStructuredSelection) m_filesViewer.getSelection()).getFirstElement();
  }

  /**
   * Returns {@link CssEditContext} for selected CSS file.
   */
  protected CssEditContext getSelectedContext() {
    return getContext(getSelectedFile());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Contexts
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<IFile, CssEditContext> m_fileToContext = Maps.newHashMap();

  /**
   * Returns {@link CssEditContext} for given file.
   */
  public CssEditContext getContext(IFile file) {
    try {
      CssEditContext context = m_fileToContext.get(file);
      if (context == null) {
        context = new CssEditContext(file);
        m_fileToContext.put(file, context);
      }
      return context;
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  /**
   * Saves and removes {@link CssEditContext}'s.
   */
  public void saveContexts() throws CoreException {
    for (Iterator<CssEditContext> I = m_fileToContext.values().iterator(); I.hasNext();) {
      CssEditContext context = I.next();
      context.commit();
      context.disconnect();
      I.remove();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  protected boolean locateRuleInFiles(String ruleValue) {
    for (IFile file : m_files) {
      if (locateRuleInFile(file, ruleValue)) {
        return true;
      }
    }
    return false;
  }

  protected boolean locateRuleInFile(IFile file, String searchValue) {
    CssEditContext context = getContext(file);
    List<CssRuleNode> rules = context.getCssDocument().getRules();
    for (CssRuleNode rule : rules) {
      String ruleValue = rule.getSelector().getValue();
      if (ruleValue.equals(searchValue)
          || ruleValue.startsWith(".")
          && ruleValue.substring(1).equals(searchValue)) {
        // select file
        m_filesViewer.setSelection(new StructuredSelection(file));
        // select rule
        RuleWrapper wrapper = new RuleWrapper(rule);
        m_rulesViewer.setSelection(new StructuredSelection(wrapper), true);
        return true;
      }
    }
    return false;
  }

  private void searchRule() {
    String searchText = m_searchField.getText();
    if (!StringUtils.isEmpty(searchText)) {
      if (!locateRuleInFiles(searchText)) {
        MessageDialog.openWarning(getShell(), "Search", "Rule selector \""
            + searchText
            + "\" not found.");
      }
    }
  }
}
