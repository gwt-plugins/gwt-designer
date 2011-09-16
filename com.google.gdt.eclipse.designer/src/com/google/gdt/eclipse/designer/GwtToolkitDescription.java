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
package com.google.gdt.eclipse.designer;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.model.generation.preview.GenerationPreviewFieldInitializerBlock;
import com.google.gdt.eclipse.designer.model.generation.preview.GenerationPreviewFieldInitializerFlat;
import com.google.gdt.eclipse.designer.model.generation.preview.GenerationPreviewFieldUniqueBlock;
import com.google.gdt.eclipse.designer.model.generation.preview.GenerationPreviewFieldUniqueFlat;
import com.google.gdt.eclipse.designer.model.generation.preview.GenerationPreviewLazy;
import com.google.gdt.eclipse.designer.model.generation.preview.GenerationPreviewLocalUniqueBlock;
import com.google.gdt.eclipse.designer.model.generation.preview.GenerationPreviewLocalUniqueFlat;
import com.google.gdt.eclipse.designer.preferences.IPreferenceConstants;

import org.eclipse.wb.core.branding.BrandingUtils;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.generation.GenerationSettings;
import org.eclipse.wb.internal.core.model.generation.preview.GenerationPreview;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.generation.statement.block.BlockStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.generation.statement.flat.FlatStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.generation.statement.lazy.LazyStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.variable.NamesManager;
import org.eclipse.wb.internal.core.model.variable.NamesManager.ComponentNameDescription;
import org.eclipse.wb.internal.core.model.variable.description.FieldInitializerVariableDescription;
import org.eclipse.wb.internal.core.model.variable.description.FieldUniqueVariableDescription;
import org.eclipse.wb.internal.core.model.variable.description.LazyVariableDescription;
import org.eclipse.wb.internal.core.model.variable.description.LocalUniqueVariableDescription;

import org.eclipse.jface.preference.IPreferenceStore;

import org.osgi.framework.Bundle;

import java.util.List;

/**
 * {@link ToolkitDescription} for GWT.
 * 
 * @author scheglov_ke
 * @coverage gwt
 */
public final class GwtToolkitDescription extends ToolkitDescription {
  public static final ToolkitDescription INSTANCE = new GwtToolkitDescription();
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
  private final GenerationSettings settings = new GenerationSettings(store);
  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialization
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_initialized;

  void initialize() {
    if (!m_initialized) {
      m_initialized = true;
      configureGenerators();
      configureCodeGeneration();
      configureTypeSpecific();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getId() {
    return IPreferenceConstants.TOOLKIT_ID;
  }

  @Override
  public String getName() {
    return "GWT toolkit";
  }

  @Override
  public String getProductName() {
    return BrandingUtils.getBranding().getProductName();
  }

  @Override
  public Bundle getBundle() {
    return Activator.getDefault().getBundle();
  }

  @Override
  public IPreferenceStore getPreferences() {
    return store;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public GenerationSettings getGenerationSettings() {
    return settings;
  }

  private void configureGenerators() {
    StatementGeneratorDescription[] usualStatements =
        new StatementGeneratorDescription[]{
            FlatStatementGeneratorDescription.INSTANCE,
            BlockStatementGeneratorDescription.INSTANCE};
    // local unique
    settings.addGenerators(
        LocalUniqueVariableDescription.INSTANCE,
        usualStatements,
        new GenerationPreview[]{
            GenerationPreviewLocalUniqueFlat.INSTANCE,
            GenerationPreviewLocalUniqueBlock.INSTANCE});
    // field unique
    settings.addGenerators(
        FieldUniqueVariableDescription.INSTANCE,
        usualStatements,
        new GenerationPreview[]{
            GenerationPreviewFieldUniqueFlat.INSTANCE,
            GenerationPreviewFieldUniqueBlock.INSTANCE});
    // field with initializer
    settings.addGenerators(
        FieldInitializerVariableDescription.INSTANCE,
        usualStatements,
        new GenerationPreview[]{
            GenerationPreviewFieldInitializerFlat.INSTANCE,
            GenerationPreviewFieldInitializerBlock.INSTANCE});
    // lazy
    settings.addGenerators(
        LazyVariableDescription.INSTANCE,
        new StatementGeneratorDescription[]{LazyStatementGeneratorDescription.INSTANCE},
        new GenerationPreview[]{GenerationPreviewLazy.INSTANCE});
  }

  private void configureCodeGeneration() {
    settings.setDefaultDeduceSettings(true);
    settings.setDefaultVariable(LocalUniqueVariableDescription.INSTANCE);
    settings.setDefaultStatement(FlatStatementGeneratorDescription.INSTANCE);
  }

  private void configureTypeSpecific() {
    List<ComponentNameDescription> descriptions = Lists.newArrayList();
    NamesManager.setDefaultNameDescriptions(this, descriptions);
  }
}
