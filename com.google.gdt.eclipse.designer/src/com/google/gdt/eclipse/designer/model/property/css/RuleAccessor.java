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
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gdt.eclipse.designer.model.property.css.ContextDescription.FileContextDescription;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;
import com.google.gdt.eclipse.designer.model.widgets.support.IGwtStateProvider;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.css.model.CssRuleNode;
import org.eclipse.wb.internal.css.parser.CssEditContext;
import org.eclipse.wb.internal.css.semantics.Semantics;

import org.eclipse.core.resources.IFile;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Accessor for {@link Semantics} by style names.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.property
 */
public class RuleAccessor {
  private final ObjectInfo m_object;
  private final GwtState m_gwtState;
  private final Map<IFile, ContextDescription> m_fileContexts = Maps.newHashMap();
  private final Set<ContextDescription> m_updatedContexts = Sets.newHashSet();
  private final Map<CssRuleNode, Semantics> m_semantics = new MapMaker().weakKeys().makeMap();
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance access
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Map<ObjectInfo, RuleAccessor> m_instances = Maps.newHashMap();

  /**
   * @return the {@link RuleAccessor} for this hierarchy.
   */
  public static RuleAccessor get(ObjectInfo object) {
    final ObjectInfo rootObject = object.getRoot();
    RuleAccessor accessor = m_instances.get(rootObject);
    if (accessor == null) {
      accessor = new RuleAccessor(rootObject);
      m_instances.put(rootObject, accessor);
      rootObject.addBroadcastListener(new ObjectEventListener() {
        @Override
        public void endEdit_aboutToRefresh() throws Exception {
          RuleAccessor accessor = m_instances.get(rootObject);
          accessor.commit();
        }

        @Override
        public void dispose() throws Exception {
          RuleAccessor accessor = m_instances.remove(rootObject);
          accessor.dispose();
        }
      });
    }
    return accessor;
  }

  /**
   * Frees resources allocated for this {@link ObjectInfo} hierarchy.
   */
  private void dispose() throws Exception {
    for (ContextDescription context : m_fileContexts.values()) {
      context.getContext().disconnect();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RuleAccessor(ObjectInfo object) {
    m_object = object;
    m_gwtState = ((IGwtStateProvider) object).getState();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the root {@link ObjectInfo} of hierarchy.
   */
  public ObjectInfo getObject() {
    return m_object;
  }

  /**
   * @return all {@link ContextDescription}s which are used for this hierarchy.
   */
  public List<ContextDescription> getContexts() throws Exception {
    List<ContextDescription> contexts = Lists.newArrayList();
    // add contexts from CSS files
    List<IFile> files = m_gwtState.getCssSupport().getFiles();
    for (IFile file : files) {
      ContextDescription contextDescription = getContextDescription(file);
      contexts.add(contextDescription);
    }
    // add extra contexts
    m_object.getBroadcast(StylePropertyEditorListener.class).addContextDescriptions(
        m_object,
        contexts);
    // done
    return contexts;
  }

  public static ContextDescription getFirstFileContent(List<ContextDescription> contexts) {
    for (ContextDescription context : contexts) {
      if (context instanceof FileContextDescription) {
        return context;
      }
    }
    return null;
  }

  /**
   * It is possible, that user expanded "style" property, but then switched to CSS file and removed
   * rule. Then he switches back to Design page, we notice that CSS file was changed and refresh
   * design canvas, including replacing properties. However just before this we try to paint
   * existing properties as is, and fail because of not existing {@link Semantics}.
   * 
   * @return <code>true</code> if {@link #getSemantics(String)} will return actual {@link Semantics}
   *         bound to some rule.
   */
  public boolean hasSemantics(String styleName) throws Exception {
    return getRule(styleName) != null;
  }

  /**
   * @return the {@link Semantics} for modifying CSS rule for given style name, may be
   *         <code>null</code>.
   */
  public Semantics getSemantics(String styleName) throws Exception {
    RuleDesc ruleDesc = getRule(styleName);
    // if no rule, use empty Semantics, to avoid null checks in many places
    if (ruleDesc == null) {
      return new Semantics();
    }
    // create Semantics for rule
    CssRuleNode rule = ruleDesc.rule;
    Semantics semantics = m_semantics.get(rule);
    if (semantics == null) {
      semantics = new Semantics();
      semantics.parse(rule);
      m_semantics.put(rule, semantics);
    }
    return semantics;
  }

  /**
   * Applies the {@link Semantics} into the source file.
   */
  public void applySemantics(String styleName) throws Exception {
    RuleDesc ruleDesc = getRule(styleName);
    Semantics semantics = getSemantics(styleName);
    semantics.update(ruleDesc.rule);
    m_updatedContexts.add(ruleDesc.contextDescription);
  }

  /**
   * Commits changes into underlying file.
   */
  private void commit() throws Exception {
    for (ContextDescription context : m_updatedContexts) {
      context.commit();
    }
    m_updatedContexts.clear();
    m_gwtState.isModified();
  }

  /**
   * @return the {@link RuleDesc} of given style name, may be <code>null</code>.
   */
  private RuleDesc getRule(String styleName) throws Exception {
    for (ContextDescription contextDescription : getContexts()) {
      CssEditContext context = contextDescription.getContext();
      for (CssRuleNode ruleNode : context.getCssDocument().getRules()) {
        if (StringUtils.equals(contextDescription.getStyleName(ruleNode), styleName)) {
          RuleDesc ruleDesc = new RuleDesc();
          ruleDesc.contextDescription = contextDescription;
          ruleDesc.rule = ruleNode;
          return ruleDesc;
        }
      }
    }
    return null;
  }

  /**
   * @return the existing or new {@link ContextDescription}, synchronized with current file.
   */
  private ContextDescription getContextDescription(IFile file) throws Exception {
    ContextDescription contextDescription = m_fileContexts.get(file);
    if (contextDescription != null) {
      String fileContent = IOUtils2.readString(file);
      String contentContent = contextDescription.getContext().getText();
      if (!ObjectUtils.equals(fileContent, contentContent)) {
        contextDescription.getContext().disconnect();
        m_fileContexts.remove(file);
      }
    }
    if (contextDescription == null) {
      CssEditContext context = new CssEditContext(file);
      contextDescription = new FileContextDescription(context);
      m_fileContexts.put(file, contextDescription);
    }
    return contextDescription;
  }

  /**
   * Structure with {@link CssEditContext} and {@link CssRuleNode} from it.
   */
  private static class RuleDesc {
    ContextDescription contextDescription;
    CssRuleNode rule;
  }
}
