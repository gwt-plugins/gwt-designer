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
package com.google.gdt.eclipse.designer.uibinder.editor;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.gdt.eclipse.designer.uibinder.model.util.NameSupport;
import com.google.gdt.eclipse.designer.uibinder.model.widgets.UIObjectInfo;
import com.google.gdt.eclipse.designer.uibinder.parser.UiBinderContext;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.structure.property.IPropertiesMenuContributor;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.dialogfields.AbstractValidationTitleAreaDialog;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogFieldUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.SelectionButtonDialogFieldGroup;
import org.eclipse.wb.internal.core.utils.dialogfields.StringDialogField;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.jdt.ui.JdtUiUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.xml.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.xml.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.xml.model.property.accessor.MethodExpressionAccessor;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.List;

/**
 * Contributes "Expose property..." action for UiBinder properties.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder.editor
 */
public final class ExposePropertySupport implements IPropertiesMenuContributor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IPropertiesMenuContributor INSTANCE = new ExposePropertySupport();

  private ExposePropertySupport() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IPropertiesMenuContributor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void contributeMenu(IMenuManager manager, Property property) throws Exception {
    if (property instanceof GenericPropertyImpl) {
      GenericPropertyImpl genericProperty = (GenericPropertyImpl) property;
      if (genericProperty.getObject() instanceof UIObjectInfo) {
        UIObjectInfo object = (UIObjectInfo) genericProperty.getObject();
        MethodExpressionAccessor accessor = getMethodAccessor(genericProperty);
        if (accessor != null) {
          manager.insertAfter(GROUP_EDIT, new ExposePropertyAction(genericProperty,
              object,
              accessor));
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link MethodExpressionAccessor}, may be <code>null</code>.
   */
  private static MethodExpressionAccessor getMethodAccessor(GenericPropertyImpl genericProperty) {
    List<ExpressionAccessor> accessors = genericProperty.getAccessors();
    for (ExpressionAccessor accessor : accessors) {
      if (accessor instanceof MethodExpressionAccessor) {
        return (MethodExpressionAccessor) accessor;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Action
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class ExposePropertyAction extends Action {
    private final GenericPropertyImpl m_property;
    private final UIObjectInfo m_object;
    private final UiBinderContext m_context;
    private final MethodExpressionAccessor m_accessor;
    private final String m_propertyTypeName;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public ExposePropertyAction(GenericPropertyImpl property,
        UIObjectInfo object,
        MethodExpressionAccessor accessor) {
      setImageDescriptor(DesignerPlugin.getImageDescriptor("actions/expose/exposeProperty.png"));
      setText(ModelMessages.ExposePropertyAction_text);
      setToolTipText(ModelMessages.ExposePropertyAction_tooltip);
      // model
      m_property = property;
      m_object = object;
      m_context = object.getContext();
      m_accessor = accessor;
      m_propertyTypeName =
          ReflectionUtils.getFullyQualifiedName(m_accessor.getGetter().getReturnType(), false);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Run
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void run() {
      final ExposeDialog dialog = new ExposeDialog();
      if (dialog.open() == Window.OK) {
        ExecutionUtils.run(m_object, new RunnableEx() {
          public void run() throws Exception {
            expose(dialog.isPublic());
          }
        });
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Implementation
    //
    ////////////////////////////////////////////////////////////////////////////
    private String m_exposedName;
    private String m_exposedGetter;
    private String m_exposedSetter;
    private String m_exposedSetterParameter;

    /**
     * @return the {@link AstEditor} for Java source.
     */
    private AstEditor getFormEditor() throws Exception {
      return m_context.getFormEditor();
    }

    /**
     * @return the Java form top level {@link TypeDeclaration}.
     */
    private TypeDeclaration getFormTypeDeclaration() throws Exception {
      CompilationUnit compilationUnit = getFormEditor().getAstUnit();
      return DomGenerics.types(compilationUnit).get(0);
    }

    /**
     * Sets the currently entered by user name of exposed property.<br>
     * This automatically updates all values that depend on it, and used in both
     * {@link ExposeDialog} and this action.
     */
    private void setExposedName(String exposedName) throws Exception {
      m_exposedName = exposedName;
      m_exposedGetter = "get" + StringUtils.capitalize(m_exposedName);
      m_exposedSetter = "set" + StringUtils.capitalize(m_exposedName);
      {
        CompilationUnit astUnit = getFormEditor().getAstUnit();
        final List<VariableDeclaration> variables =
            AstNodeUtils.getVariableDeclarationsAll(astUnit);
        m_exposedSetterParameter =
            CodeUtils.generateUniqueName(m_property.getTitle(), new Predicate<String>() {
              public boolean apply(String name) {
                for (VariableDeclaration variable : variables) {
                  if (variable.getName().getIdentifier().equals(name)) {
                    return false;
                  }
                }
                return true;
              }
            });
      }
    }

    /**
     * Validates the currently entered by user name of exposed property.
     * 
     * @return the error message, or <code>null</code> if given name is valid.
     */
    private String validate(final String exposedName) {
      return ExecutionUtils.runObject(new RunnableObjectEx<String>() {
        public String runObject() throws Exception {
          return validateEx(exposedName);
        }
      }, "<Exception, see log>");
    }

    /**
     * Implementation of {@link #validate(String)}.
     */
    @SuppressWarnings("deprecation")
    private String validateEx(final String exposedName) throws Exception {
      setExposedName(exposedName);
      // check for valid identifier
      {
        IStatus status = JavaConventions.validateIdentifier(m_exposedName);
        if (status.getSeverity() == IStatus.ERROR) {
          return status.getMessage();
        }
      }
      // prepare TypeDeclaration
      TypeDeclaration typeDeclaration = getFormTypeDeclaration();
      // check for existing getter
      {
        String signature = m_exposedGetter + "()";
        if (AstNodeUtils.getMethodBySignature(typeDeclaration, signature) != null) {
          return MessageFormat.format(
              ModelMessages.ExposePropertyAction_validateMethodAlreadyExists,
              signature);
        }
      }
      // check for existing setter
      {
        String signature = m_exposedSetter + "(" + m_propertyTypeName + ")";
        if (AstNodeUtils.getMethodBySignature(typeDeclaration, signature) != null) {
          return MessageFormat.format(
              ModelMessages.ExposePropertyAction_validateMethodAlreadyExists,
              signature);
        }
      }
      // OK
      return null;
    }

    /**
     * Prepares source for preview.
     * 
     * @param isPublic
     *          is <code>true</code> if <code>public</code> modifier should be used and
     *          <code>false</code> for <code>protected</code> modifier.
     */
    private String getPreviewSource(boolean isPublic) throws Exception {
      String modifierSource = isPublic ? "public" : "protected";
      String propertyTypeName = CodeUtils.getShortClass(m_propertyTypeName);
      String accessExpression = "widget.";
      // prepare source
      String source = "";
      {
        source += "...\n";
        // getter
        if (m_accessor.getGetter() != null) {
          source +=
              MessageFormat.format(
                  "\t{0} {1} {2}() '{'\n",
                  modifierSource,
                  propertyTypeName,
                  m_exposedGetter);
          source +=
              MessageFormat.format(
                  "\t\treturn {0}{1}();\n",
                  accessExpression,
                  m_accessor.getGetter().getName());
          source += "\t}\n";
        }
        // setter
        {
          source +=
              MessageFormat.format(
                  "\t{0} void {1}({2} {3}) '{'\n",
                  modifierSource,
                  m_exposedSetter,
                  propertyTypeName,
                  m_exposedSetterParameter);
          source +=
              MessageFormat.format(
                  "\t\t{0}{1}({2});\n",
                  accessExpression,
                  m_accessor.getSetter().getName(),
                  m_exposedSetterParameter);
          source += "\t}\n";
        }
        // end
        source += "...\n";
      }
      // final result
      return source;
    }

    /**
     * Generates getter/setter for exposing property.
     */
    private void expose(boolean isPublic) throws Exception {
      AstEditor m_editor = getFormEditor();
      TypeDeclaration m_typeDeclaration = getFormTypeDeclaration();
      //
      BodyDeclarationTarget methodTarget = new BodyDeclarationTarget(m_typeDeclaration, false);
      String modifierSource = isPublic ? "public" : "protected";
      String name = NameSupport.ensureName(m_object);
      // getter
      {
        String header = modifierSource + " " + m_propertyTypeName + " " + m_exposedGetter + "()";
        String body =
            MessageFormat.format("return {0}.{1}();", name, m_accessor.getGetter().getName());
        m_editor.addMethodDeclaration(header, ImmutableList.of(body), methodTarget);
      }
      // setter
      {
        String header =
            MessageFormat.format(
                "{0} void {1}({2} {3})",
                modifierSource,
                m_exposedSetter,
                m_propertyTypeName,
                m_exposedSetterParameter);
        String body =
            MessageFormat.format(
                "{0}.{1}({2});",
                name,
                m_accessor.getSetter().getName(),
                m_exposedSetterParameter);
        m_editor.addMethodDeclaration(header, ImmutableList.of(body), methodTarget);
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Expose dialog
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Dialog for requesting parameters for exposing {@link AbstractComponentInfo}.
     */
    private class ExposeDialog extends AbstractValidationTitleAreaDialog {
      ////////////////////////////////////////////////////////////////////////////
      //
      // Constructor
      //
      ////////////////////////////////////////////////////////////////////////////
      public ExposeDialog() {
        super(DesignerPlugin.getShell(),
            DesignerPlugin.getDefault(),
            ModelMessages.ExposePropertyAction_dialogShellTitle,
            ModelMessages.ExposePropertyAction_dialogTitle,
            DesignerPlugin.getImage("actions/expose/expose_banner.gif"),
            ModelMessages.ExposePropertyAction_dialogMessage);
        setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // GUI
      //
      ////////////////////////////////////////////////////////////////////////////
      private StringDialogField m_nameField;
      private SelectionButtonDialogFieldGroup m_modifierField;
      private SourceViewer m_previewViewer;

      @Override
      protected void createControls(Composite container) {
        m_fieldsContainer = container;
        GridLayoutFactory.create(container).columns(2);
        // name
        {
          m_nameField = new StringDialogField();
          doCreateField(m_nameField, ModelMessages.ExposePropertyAction_dialogPropertyLabel);
          // initial name
          {
            Class<?> componentClass = m_object.getDescription().getComponentClass();
            String shortClassName = CodeUtils.getShortClass(componentClass.getName());
            String exposedName =
                StringUtils.uncapitalize(shortClassName)
                    + StringUtils.capitalize(m_property.getTitle());
            m_nameField.setText(exposedName);
          }
        }
        // modifier
        {
          m_modifierField =
              new SelectionButtonDialogFieldGroup(SWT.RADIO,
                  new String[]{"&public", "pro&tected"},
                  1,
                  SWT.SHADOW_ETCHED_IN);
          doCreateField(m_modifierField, ModelMessages.ExposePropertyAction_dialogModifier);
        }
        // preview
        {
          new Label(container, SWT.NONE).setText(ModelMessages.ExposePropertyAction_dialogPreview);
          m_previewViewer = JdtUiUtils.createJavaSourceViewer(container, SWT.BORDER | SWT.V_SCROLL);
          GridDataFactory.create(m_previewViewer.getControl()).spanH(2).hintVC(9).grab().fill();
        }
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Validation
      //
      ////////////////////////////////////////////////////////////////////////////
      @Override
      protected String validate() throws Exception {
        // validate
        {
          String message = ExposePropertyAction.this.validate(m_nameField.getText());
          if (message != null) {
            JdtUiUtils.setJavaSourceForViewer(
                m_previewViewer,
                ModelMessages.ExposePropertyAction_dialogNoPreview);
            return message;
          }
        }
        // update preview
        {
          boolean isPublic = isPublic();
          JdtUiUtils.setJavaSourceForViewer(m_previewViewer, getPreviewSource(isPublic));
        }
        // OK
        return null;
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Access
      //
      ////////////////////////////////////////////////////////////////////////////
      public boolean isPublic() {
        return m_modifierField.getSelection()[0] == 0;
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Utils
      //
      ////////////////////////////////////////////////////////////////////////////
      private Composite m_fieldsContainer;

      /**
       * Configures given {@link DialogField} for specific of this dialog.
       */
      protected final void doCreateField(DialogField dialogField, String labelText) {
        dialogField.setLabelText(labelText);
        dialogField.setDialogFieldListener(m_validateListener);
        DialogFieldUtils.fillControls(m_fieldsContainer, dialogField, 2, 40);
      }
    }
  }
}
