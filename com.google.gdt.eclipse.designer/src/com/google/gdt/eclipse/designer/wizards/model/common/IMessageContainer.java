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
package com.google.gdt.eclipse.designer.wizards.model.common;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.wizard.WizardPage;

/**
 * Interface for displaying messages.
 * 
 * We use it to separate GUI that edits anything and needs to display messages from concrete message
 * containers, such as {@link TitleAreaDialog} or {@link WizardPage}.
 * 
 * @author scheglov_ke
 * @coverage gwt.wizard.ui
 */
public interface IMessageContainer {
  void setErrorMessage(String message);

  /**
   * Helper class for creating {@link IMessageContainer} for standard GUI objects.
   */
  public static class Util {
    /**
     * Creates {@link IMessageContainer} for {@link WizardPage}.
     */
    public static IMessageContainer forWizardPage(final WizardPage wizardPage) {
      return new IMessageContainer() {
        public void setErrorMessage(String message) {
          wizardPage.setErrorMessage(message);
        }
      };
    }

    /**
     * Creates {@link IMessageContainer} for {@link TitleAreaDialog}.
     */
    public static IMessageContainer forTitleAreaDialog(final TitleAreaDialog titleAreaDialog) {
      return new IMessageContainer() {
        public void setErrorMessage(String message) {
          titleAreaDialog.setErrorMessage(message);
        }
      };
    }
  }
}
