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
package com.google.gdt.eclipse.designer.model.property.css;

import org.eclipse.wb.internal.css.model.CssFactory;
import org.eclipse.wb.internal.css.model.CssRuleNode;
import org.eclipse.wb.internal.css.parser.CssEditContext;

import org.apache.commons.lang.StringUtils;

import java.util.List;

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
   * @return <code>true</code> if this {@link ContextDescription} is out of sync with its resource.
   */
  public boolean isStale() throws Exception {
    return false;
  }

  /**
   * Commits {@link CssEditContext} changes.
   */
  public abstract void commit() throws Exception;

  /**
   * Cleans up all resources, such as opened files, etc.
   */
  public void dispose() throws Exception {
  }

  /**
   * @return the name of style, if possible, may be <code>null</code>.
   */
  public abstract String getStyleName(CssRuleNode rule);

  /**
   * @return the {@link CssRuleNode}s accessible using this context.
   */
  public List<CssRuleNode> getRules() {
    return m_context.getCssDocument().getRules();
  }

  /**
   * Add new {@link CssRuleNode} with given class name.
   * 
   * @return the name of style, which should be used to reference this rule.
   */
  public String addNewStyle(String styleName) throws Exception {
    String newSelector = "." + StringUtils.removeStart(styleName, ".");
    // may be already has rule with such selection
    for (CssRuleNode existingRule : getRules()) {
      if (existingRule.getSelector().getValue().equals(newSelector)) {
        return getStyleName(existingRule);
      }
    }
    // OK, add new rule
    CssRuleNode newRule = CssFactory.newRule(newSelector);
    m_context.getCssDocument().addRule(newRule);
    return getStyleName(newRule);
  }
}
