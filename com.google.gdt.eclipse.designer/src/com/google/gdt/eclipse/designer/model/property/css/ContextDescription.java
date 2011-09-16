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

import org.eclipse.wb.internal.css.model.CssDocument;
import org.eclipse.wb.internal.css.model.CssFactory;
import org.eclipse.wb.internal.css.model.CssRuleNode;
import org.eclipse.wb.internal.css.parser.CssEditContext;

import org.apache.commons.lang.StringUtils;

/**
 * Description for {@link CssEditContext} and its style name transformation.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.property
 */
public abstract class ContextDescription {
  private final CssEditContext m_context;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ContextDescription(CssEditContext context) {
    m_context = context;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link CssEditContext}.
   */
  public CssEditContext getContext() {
    return m_context;
  }

  /**
   * @return the name of style, if possible, may be <code>null</code>.
   */
  public abstract String getStyleName(CssRuleNode rule);

  /**
   * Commits {@link CssEditContext} changes.
   */
  public abstract void commit() throws Exception;

  /**
   * Add new {@link CssRuleNode} with given class name.
   * 
   * @return the name of style, which should be used to reference this rule.
   */
  public String addNewStyle(String styleName) throws Exception {
    String newSelector = "." + StringUtils.removeStart(styleName, ".");
    // may be already has rule with such selection
    CssDocument cssDocument = m_context.getCssDocument();
    for (CssRuleNode existingRule : cssDocument.getRules()) {
      if (existingRule.getSelector().getValue().equals(newSelector)) {
        return getStyleName(existingRule);
      }
    }
    // OK, add new rule
    CssRuleNode newRule = CssFactory.newRule(newSelector);
    cssDocument.addRule(newRule);
    return getStyleName(newRule);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // FileContextDescription
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link ContextDescription} for context in standalone CSS file.
   */
  public static final class FileContextDescription extends ContextDescription {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public FileContextDescription(CssEditContext context) {
      super(context);
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
        return selector.substring(1);
      }
      return null;
    }

    @Override
    public void commit() throws Exception {
      getContext().commit();
    }
  }
}
