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
package com.google.gdt.eclipse.designer.uibinder.model.util;

import com.google.gdt.eclipse.designer.model.property.css.ContextDescription;
import com.google.gdt.eclipse.designer.model.property.css.StylePropertyEditor;
import com.google.gdt.eclipse.designer.model.property.css.StylePropertyEditorListener;
import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderContext;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.utils.xml.DocumentTextNode;
import org.eclipse.wb.internal.core.xml.model.EditorContextCommitListener;
import org.eclipse.wb.internal.core.xml.model.utils.NamespacesHelper;
import org.eclipse.wb.internal.css.model.CssRuleNode;
import org.eclipse.wb.internal.css.parser.CssEditContext;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import java.util.List;

/**
 * Support for UiBinder template specific in {@link StylePropertyEditor}.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.model
 */
public final class StylePropertySupport {
  private final UiBinderContext m_context;
  private final ContextDescription m_contextDescription;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StylePropertySupport(UiBinderContext context) throws Exception {
    m_context = context;
    m_contextDescription = createContextDescription(context);
    m_context.getBroadcastSupport().addListener(null, new StylePropertyEditorListener() {
      @Override
      public void addContextDescriptions(ObjectInfo object, List<ContextDescription> contexts)
          throws Exception {
        if (m_contextDescription != null) {
          contexts.add(0, m_contextDescription);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ContextDescription} for "<ui:style>" element, may be <code>null</code>.
   */
  private static ContextDescription createContextDescription(UiBinderContext uiContent)
      throws Exception {
    DocumentElement styleElement = getStyleElement(uiContent);
    if (styleElement != null) {
      DocumentTextNode textNode = styleElement.getTextNode();
      String cssSource = textNode != null ? textNode.getRawText() : "";
      IDocument cssDocument = new Document(cssSource);
      CssEditContext cssContext = new CssEditContext(cssDocument);
      return new UiBinderContextDescription(uiContent, cssContext);
    }
    return null;
  }

  /**
   * @return the "<ui:style>" element, may be <code>null</code>.
   */
  private static DocumentElement getStyleElement(UiBinderContext uiContent) {
    DocumentElement rootElement = uiContent.getRootElement();
    String uiName = NamespacesHelper.getName(rootElement, "urn:ui:com.google.gwt.uibinder");
    return rootElement.getChild(uiName + ":style", true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // UiBinderContextDescription
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link ContextDescription} for context from "<ui:style>" element.
   */
  private static class UiBinderContextDescription extends ContextDescription {
    private final UiBinderContext m_uiContent;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public UiBinderContextDescription(UiBinderContext uiContent, CssEditContext cssContext) {
      super(cssContext);
      m_uiContent = uiContent;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public String getStyleName(CssRuleNode rule) {
      String selector = rule.getSelector().getValue();
      if (selector.startsWith(".")) {
        return "{style." + selector.substring(1) + "}";
      }
      return null;
    }

    @Override
    public void commit() throws Exception {
      getCommitListener().aboutToCommit();
      try {
        DocumentElement styleElement = getStyleElement(m_uiContent);
        if (styleElement != null) {
          DocumentTextNode textNode = styleElement.getTextNode();
          String cssSource = getContext().getText();
          textNode.setText(cssSource);
        }
      } finally {
        getCommitListener().doneCommit();
      }
    }

    private EditorContextCommitListener getCommitListener() {
      return m_uiContent.getBroadcastSupport().getListener(EditorContextCommitListener.class);
    }
  }
}
