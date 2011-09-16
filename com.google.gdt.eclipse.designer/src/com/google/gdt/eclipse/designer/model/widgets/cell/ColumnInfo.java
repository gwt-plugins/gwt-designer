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
package com.google.gdt.eclipse.designer.model.widgets.cell;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.CreationDescription;
import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.converter.DoubleConverter;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDisplayPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.swt.graphics.Image;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Model for <code>com.google.gwt.user.cellview.client.Column</code>.
 * 
 * @author sablin_aa
 * @author scheglov_ke
 * @coverage gwt.model
 */
public class ColumnInfo extends AbstractComponentInfo {
  private final ColumnInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Property> getPropertyList() throws Exception {
    List<Property> properties = super.getPropertyList();
    // "header" property if available
    {
      Property property = getHeaderProperty();
      if (property != null) {
        properties.add(property);
      }
    }
    // "width" property if available
    {
      Property widthProperty = getWidthProperty();
      if (widthProperty != null) {
        properties.add(widthProperty);
      }
    }
    // "comparator" property if available
    {
      Property comparatorProperty = getComparatorProperty();
      if (comparatorProperty != null) {
        properties.add(comparatorProperty);
      }
    }
    // done
    return properties;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Header property
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final List<String> HEADER_SIG_PREFIXES =
      ImmutableList.of(
          "addColumn(com.google.gwt.user.cellview.client.Column)",
          "addColumn(com.google.gwt.user.cellview.client.Column,java.lang.String",
          "addColumn(com.google.gwt.user.cellview.client.Column,com.google.gwt.safehtml.shared.SafeHtml",
          "addColumn(com.google.gwt.user.cellview.client.Column,com.google.gwt.user.cellview.client.Header)");
  private boolean m_headerPropertyReady = false;
  private Property m_headerProperty;

  /**
   * @return synthetic {@link Property} for accessing column header, may be <code>null</code> if
   *         this {@link ColumnInfo} can not have header.
   */
  public Property getHeaderProperty() {
    if (!m_headerPropertyReady) {
      m_headerPropertyReady = true;
      createHeaderProperty();
    }
    return m_headerProperty;
  }

  private void createHeaderProperty() {
    Association association = getAssociation();
    if (association instanceof InvocationChildAssociation) {
      InvocationChildAssociation invocationAssociation = (InvocationChildAssociation) association;
      MethodInvocation associationInvocation = invocationAssociation.getInvocation();
      // check for supported association invocations
      boolean supportedAssociation = false;
      String signature = AstNodeUtils.getMethodSignature(associationInvocation);
      for (String supportedSignature : HEADER_SIG_PREFIXES) {
        if (signature.startsWith(supportedSignature)) {
          supportedAssociation = true;
          break;
        }
      }
      // create property if supported
      if (supportedAssociation) {
        m_headerProperty = new HeaderProperty();
      }
    }
  }

  private class HeaderProperty extends Property {
    public HeaderProperty() {
      super(StringPropertyEditor.INSTANCE);
      setCategory(PropertyCategory.PREFERRED);
    }

    @Override
    public String getTitle() {
      return "header";
    }

    @Override
    public boolean isModified() throws Exception {
      return getExpression() != null;
    }

    @Override
    public Object getValue() throws Exception {
      Expression expression = getExpression();
      // no expression
      if (expression == null) {
        return null;
      }
      // use String value
      if (AstNodeUtils.isSuccessorOf(expression, "java.lang.String")) {
        return JavaInfoEvaluationHelper.getValue(expression);
      }
      // not String value
      return null;
    }

    private Expression getExpression() {
      InvocationChildAssociation association = (InvocationChildAssociation) getAssociation();
      MethodInvocation invocation = association.getInvocation();
      List<Expression> arguments = DomGenerics.arguments(invocation);
      // no header argument
      if (arguments.size() < 2) {
        return null;
      }
      // has header argument
      return arguments.get(1);
    }

    @Override
    public void setValue(Object value) throws Exception {
      final MethodInvocation invocation = getInvocation();
      final List<Expression> arguments = DomGenerics.arguments(invocation);
      final AstEditor editor = m_this.getEditor();
      // prepare new value
      String newValue = (String) value;
      // process empty string as null
      if (StringUtils.isEmpty(newValue)) {
        newValue = null;
      }
      // remove value
      if (newValue == null) {
        if (arguments.size() == 2) {
          ExecutionUtils.run(m_this, new RunnableEx() {
            public void run() throws Exception {
              editor.removeInvocationArgument(invocation, 1);
            }
          });
        }
      }
      // set value
      if (newValue != null) {
        final String valueSource = StringConverter.INSTANCE.toJavaSource(m_this, newValue);
        if (arguments.size() == 1) {
          // add argument
          ExecutionUtils.run(m_this, new RunnableEx() {
            public void run() throws Exception {
              editor.addInvocationArgument(invocation, 1, valueSource);
            }
          });
        } else {
          // replace argument
          ExecutionUtils.run(m_this, new RunnableEx() {
            public void run() throws Exception {
              editor.replaceExpression(arguments.get(1), valueSource);
              // may be was not String argument
              editor.replaceInvocationBinding(invocation);
            }
          });
        }
      }
    }

    /**
     * @return the association {@link MethodInvocation}.
     */
    private MethodInvocation getInvocation() {
      InvocationChildAssociation association = (InvocationChildAssociation) getAssociation();
      return association.getInvocation();
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Width property
  //
  ////////////////////////////////////////////////////////////////////////////
  private Property m_widthProperty;
  private boolean m_widthPropertyReady;

  /**
   * @return synthetic {@link Property} for accessing column width.
   */
  public Property getWidthProperty() {
    if (!m_widthPropertyReady) {
      m_widthPropertyReady = true;
      if (hasWidthProperty()) {
        m_widthProperty = new WidthProperty();
      }
    }
    return m_widthProperty;
  }

  /**
   * @return <code>true</code> if this GWT version has "width" property support.
   */
  private boolean hasWidthProperty() {
    IJavaProject javaProject = getEditor().getJavaProject();
    return Utils.getVersion(javaProject).isHigherOrSame(Utils.GWT_2_2);
  }

  private class WidthProperty extends Property {
    public WidthProperty() {
      super(StringPropertyEditor.INSTANCE);
      setCategory(PropertyCategory.PREFERRED);
    }

    @Override
    public String getTitle() {
      return "width";
    }

    @Override
    public boolean isModified() throws Exception {
      return getInvocation() != null;
    }

    @Override
    public Object getValue() throws Exception {
      @SuppressWarnings("rawtypes")
      Map columnWidths =
          (Map) ReflectionUtils.getFieldObject(getParentJava().getObject(), "columnWidths");
      return columnWidths.get(getObject());
    }

    @Override
    public void setValue(Object valueObject) throws Exception {
      String valueString = valueObject instanceof String ? (String) valueObject : null;
      final AstEditor editor = m_this.getEditor();
      final MethodInvocation invocation = getInvocation();
      final String signaturePrefix = "setColumnWidth(com.google.gwt.user.cellview.client.Column,";
      // remove value
      if (StringUtils.isEmpty(valueString)) {
        if (invocation != null) {
          ExecutionUtils.run(m_this, new RunnableEx() {
            public void run() throws Exception {
              editor.removeEnclosingStatement(invocation);
            }
          });
        }
        return;
      }
      // new value
      if (invocation == null) {
        final String valueSource = StringConverter.INSTANCE.toJavaSource(m_this, valueString);
        ExecutionUtils.run(m_this, new RunnableEx() {
          public void run() throws Exception {
            StatementTarget target = new StatementTarget(getAssociation().getStatement(), false);
            String signature = signaturePrefix + "java.lang.String)";
            String argumentsSource = TemplateUtils.format("{0}, {1}", m_this, valueSource);
            MethodInvocation newInvocation =
                getParentJava().addMethodInvocation(target, signature, argumentsSource);
            addRelatedNodes(newInvocation);
          }
        });
        return;
      }
      // update value
      final List<Expression> arguments = DomGenerics.arguments(invocation);
      // setColumnWidth(Column,String)
      {
        String signature = signaturePrefix + "java.lang.String)";
        if (AstNodeUtils.isMethodInvocation(invocation, signature)) {
          final String valueSource = StringConverter.INSTANCE.toJavaSource(m_this, valueString);
          ExecutionUtils.run(m_this, new RunnableEx() {
            public void run() throws Exception {
              editor.replaceExpression(arguments.get(1), valueSource);
            }
          });
        }
      }
      // setColumnWidth(Column,double,Unit)
      {
        String typeNameUnit = "com.google.gwt.dom.client.Style.Unit";
        String signature = signaturePrefix + "double," + typeNameUnit + ")";
        if (AstNodeUtils.isMethodInvocation(invocation, signature)) {
          // prepare "value" and "unit"
          double sizeValue;
          Object sizeUnit;
          try {
            sizeValue = getValueFromSizeString(valueString);
            sizeUnit = getUnitFromSizeString(valueString);
          } catch (Throwable e) {
            return;
          }
          // no unit
          if (sizeUnit == null) {
            return;
          }
          // apply "value" and "unit"
          final String valueSource = DoubleConverter.INSTANCE.toJavaSource(m_this, sizeValue);
          final String unitSource = typeNameUnit + "." + sizeUnit.toString();
          ExecutionUtils.run(m_this, new RunnableEx() {
            public void run() throws Exception {
              editor.replaceExpression(arguments.get(1), valueSource);
              editor.replaceExpression(arguments.get(2), unitSource);
            }
          });
        }
      }
    }

    /**
     * @return the value for size string like "2.1cm".
     */
    private double getValueFromSizeString(String valueWithType) throws Exception {
      int lastIndex = StringUtils.indexOfAnyBut(valueWithType, "0123456789.");
      String valueString;
      if (lastIndex != StringUtils.INDEX_NOT_FOUND) {
        valueString = StringUtils.substring(valueWithType, 0, lastIndex);
      } else {
        valueString = valueWithType;
      }
      return Double.parseDouble(valueString);
    }

    /**
     * @return the <code>Unit</code> instance for size string like "2.1cm".
     */
    private Object getUnitFromSizeString(String valueWithType) throws Exception {
      int unitIndex = StringUtils.indexOfAnyBut(valueWithType, "0123456789.");
      if (unitIndex != StringUtils.INDEX_NOT_FOUND) {
        String unitType = StringUtils.substring(valueWithType, unitIndex);
        if (unitType != null) {
          return getUnitByType(unitType);
        }
      }
      return getUnitByType("px");
    }

    /**
     * @return the <code>Unit</code> instance with given <code>getType()</code>.
     */
    private Object getUnitByType(String requiredType) throws Exception {
      Class<?> classUnit =
          JavaInfoUtils.getClassLoader(m_this).loadClass("com.google.gwt.dom.client.Style$Unit");
      for (Object unit : classUnit.getEnumConstants()) {
        String unitType = (String) ReflectionUtils.invokeMethod(unit, "getType()");
        if (requiredType.equalsIgnoreCase(unitType)) {
          return unit;
        }
      }
      return null;
    }

    /**
     * @return the <code>setWidth</code> {@link MethodInvocation}, may be <code>null</code>.
     */
    private MethodInvocation getInvocation() {
      JavaInfo parent = getParentJava();
      // check setWidth(Column,*) invocations
      List<MethodInvocation> invocations = parent.getMethodInvocations();
      for (MethodInvocation invocation : invocations) {
        if (invocation.getName().getIdentifier().equals("setColumnWidth")) {
          List<Expression> arguments = DomGenerics.arguments(invocation);
          if (arguments.size() >= 2) {
            Expression columnExpression = arguments.get(0);
            if (isRepresentedBy(columnExpression)) {
              return invocation;
            }
          }
        }
      }
      // no setWidth() invocation
      return null;
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Comparator property
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_comparatorPropertyReady = false;
  private Property m_comparatorProperty;

  /**
   * @return synthetic {@link Property} for accessing column sorting {@link Comparator}, may be
   *         <code>null</code> if this version of GWT does not support this feature.
   */
  public Property getComparatorProperty() {
    if (!m_comparatorPropertyReady) {
      m_comparatorPropertyReady = true;
      if (hasComparatorProperty()) {
        m_comparatorProperty = new ComparatorProperty();
      }
    }
    return m_comparatorProperty;
  }

  /**
   * @return <code>true</code> if this GWT version has "comparator" property support.
   */
  private boolean hasComparatorProperty() {
    IJavaProject javaProject = getEditor().getJavaProject();
    return Utils.getVersion(javaProject).isHigherOrSame(Utils.GWT_2_2);
  }

  /**
   * "comparator" {@link PropertyEditor}.
   */
  private static final PropertyEditor COMPARATOR_EDITOR = new TextDisplayPropertyEditor() {
    @Override
    protected String getText(Property property) throws Exception {
      return (String) property.getValue();
    }

    @Override
    public void doubleClick(Property property, org.eclipse.swt.graphics.Point location)
        throws Exception {
      property.setValue(this);
    }
  };

  /**
   * "comparator" {@link Property}.
   */
  private class ComparatorProperty extends Property {
    public ComparatorProperty() {
      super(COMPARATOR_EDITOR);
      setCategory(PropertyCategory.PREFERRED);
    }

    @Override
    public String getTitle() {
      return "comparator";
    }

    @Override
    public boolean isModified() throws Exception {
      return getInvocation() != null;
    }

    @Override
    public Object getValue() throws Exception {
      MethodInvocation invocation = getInvocation();
      if (invocation != null) {
        return "<comparator>";
      }
      return "<empty>";
    }

    @Override
    public void setValue(Object value) throws Exception {
      MethodInvocation invocation = getInvocation();
      // remove value
      if (value == UNKNOWN_VALUE) {
        if (invocation != null) {
          removeInvocation(invocation);
        }
        return;
      }
      // generate new "setComparator"
      if (invocation == null) {
        invocation = addInvocation();
      }
      // open invocation
      if (invocation != null) {
        Expression comparatorExpression = DomGenerics.arguments(invocation).get(1);
        JavaInfoUtils.scheduleOpenNode(m_this, comparatorExpression);
      }
    }

    /**
     * @return the <code>setComparator</code> {@link MethodInvocation}, may be <code>null</code>.
     */
    private MethodInvocation getInvocation() {
      String signature =
          "setComparator(com.google.gwt.user.cellview.client.Column,java.util.Comparator)";
      for (ASTNode node : getRelatedNodes()) {
        if (node.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
          MethodInvocation invocation = (MethodInvocation) node.getParent();
          if (AstNodeUtils.getMethodSignature(invocation).equals(signature)) {
            List<Expression> arguments = DomGenerics.arguments(invocation);
            Expression columnExpression = arguments.get(0);
            if (isRepresentedBy(columnExpression)) {
              return invocation;
            }
          }
        }
      }
      return null;
    }

    /**
     * Configures {@link ColumnInfo} to don't be sortable and removes {@link Comparator}.
     */
    protected void removeInvocation(final MethodInvocation invocation) {
      ExecutionUtils.run(m_this, new RunnableEx() {
        public void run() throws Exception {
          AstEditor editor = m_this.getEditor();
          getPropertyByTitle("sortable").setValue(UNKNOWN_VALUE);
          editor.removeEnclosingStatement(invocation);
        }
      });
    }

    /**
     * Adds <code>ListHandler.setComparator(Column,Comparator)</code> invocation.
     * 
     * @return the added {@link MethodInvocation}.
     */
    private MethodInvocation addInvocation() {
      final AtomicReference<MethodInvocation> result = new AtomicReference<MethodInvocation>();
      ExecutionUtils.run(m_this, new RunnableEx() {
        public void run() throws Exception {
          String rowTypeName = getRowTypeName();
          String listHandlerName = getListHandlerName(rowTypeName);
          // enable sorting
          getPropertyByTitle("sortable").setValue(true);
          // add "setComparator" invocation
          StatementTarget target = JavaInfoUtils.getTarget(m_this);
          String line1 =
              TemplateUtils.format(
                  "{0}.setComparator({1}, new java.util.Comparator<{2}>() '{'",
                  listHandlerName,
                  m_this,
                  rowTypeName);
          String line2 =
              TemplateUtils.format("\tpublic int compare({0} o1, {0} o2) '{'", rowTypeName);
          List<String> lines = ImmutableList.of(line1, line2, "\t\treturn 0;", "\t}", "})");
          String source = Joiner.on("\n").join(lines);
          MethodInvocation invocation = (MethodInvocation) addExpressionStatement(target, source);
          result.set(invocation);
        }
      });
      return result.get();
    }

    private String getRowTypeName() {
      Expression creationExpression = (Expression) getCreationSupport().getNode();
      ITypeBinding creationBinding = AstNodeUtils.getTypeBinding(creationExpression);
      ITypeBinding rowTypeBinding =
          AstNodeUtils.getTypeBindingArgument(
              creationBinding,
              "com.google.gwt.user.cellview.client.Column",
              0);
      return AstNodeUtils.getFullyQualifiedName(rowTypeBinding, false);
    }

    /**
     * @return the not <code>null</code>, existing name of <code>ListHandler</code> instance, which
     *         is used for sorting of this <code>CellTable</code>.
     */
    private String getListHandlerName(String rowTypeName) throws Exception {
      AstEditor editor = m_this.getEditor();
      // try to find visible ListHandler instance
      {
        int position = getCreationSupport().getNode().getStartPosition();
        CompilationUnit astUnit = editor.getAstUnit();
        List<VariableDeclaration> variables =
            AstNodeUtils.getVariableDeclarationsVisibleAt(astUnit, position);
        for (VariableDeclaration variable : variables) {
          ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(variable);
          if (AstNodeUtils.isSuccessorOf(
              typeBinding,
              "com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler")) {
            if (isListHandlerForThisTable(variable)) {
              return variable.getName().getIdentifier();
            }
          }
        }
      }
      // prepare name for ListHandler instance
      String listHandlerName = editor.getUniqueVariableName(-1, "sortHandler", null);
      // declare ListHandler field
      {
        String source =
            MessageFormat.format(
                "private {0}<{1}> {2} = new {0}<{1}>(java.util.Collections.<{1}>emptyList());",
                "com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler",
                rowTypeName,
                listHandlerName);
        TypeDeclaration targetType = JavaInfoUtils.getTypeDeclaration(m_this);
        BodyDeclarationTarget target = new BodyDeclarationTarget(targetType, true);
        editor.addFieldDeclaration(source, target);
      }
      // add cellTable.addColumnSortHandler(sortHandler);
      getParentJava().addMethodInvocation(
          "addColumnSortHandler(com.google.gwt.user.cellview.client.ColumnSortEvent.Handler)",
          listHandlerName);
      // done
      return listHandlerName;
    }

    /**
     * @return <code>true</code> if given variable of <code>Handler</code> is configured as sorter
     *         for this <code>CellTable</code>.
     */
    private boolean isListHandlerForThisTable(VariableDeclaration variable) {
      String listHandlerName = variable.getName().getIdentifier();
      String signature =
          "addColumnSortHandler(com.google.gwt.user.cellview.client.ColumnSortEvent.Handler)";
      MethodInvocation invocation = getParentJava().getMethodInvocation(signature);
      Expression listHandlerArgument = DomGenerics.arguments(invocation).get(0);
      return listHandlerArgument.toString().equals(listHandlerName);
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  private static String[] CELL_IDS = {
      "button",
      "number",
      "edittext",
      "checkbox",
      "date",
      "datepicker",
      "selection"};
  private static String[] CELL_TYPES = {
      "com.google.gwt.cell.client.ButtonCell",
      "com.google.gwt.cell.client.NumberCell",
      "com.google.gwt.cell.client.EditTextCell",
      "com.google.gwt.cell.client.CheckboxCell",
      "com.google.gwt.cell.client.DateCell",
      "com.google.gwt.cell.client.DatePickerCell",
      "com.google.gwt.cell.client.SelectionCell"};
  private Image m_specialIcon;
  private final IObjectPresentation m_presentation = new DefaultJavaInfoPresentation(this) {
    @Override
    public Image getIcon() throws Exception {
      Image icon = super.getIcon();
      // replace default icon with specific
      if (icon == m_javaInfo.getDescription().getIcon() && m_specialIcon != null) {
        icon = m_specialIcon;
      }
      return icon;
    };
  };

  @Override
  public IObjectPresentation getPresentation() {
    return m_presentation;
  }

  private Image getCreationIcon(String creationId) {
    CreationDescription creation = getDescription().getCreation(creationId);
    return creation != null ? creation.getIcon() : null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh_dispose() throws Exception {
    m_specialIcon = null;
    super.refresh_dispose();
  }

  @Override
  protected void refresh_finish() throws Exception {
    super.refresh_finish();
    String cellTypeName = null;
    // if anonymous, then it was mocked as TextColumn
    if (getCreationSupport().getNode() instanceof ClassInstanceCreation) {
      ClassInstanceCreation creation = (ClassInstanceCreation) getCreationSupport().getNode();
      if (creation.getAnonymousClassDeclaration() != null) {
        ITypeBinding anonymousBinding = AstNodeUtils.getTypeBinding(creation);
        ITypeBinding superBinding = anonymousBinding.getSuperclass();
        String superName = AstNodeUtils.getFullyQualifiedName(superBinding, false);
        if ("com.google.gwt.user.cellview.client.Column".equals(superName)) {
          Expression cellExpression = DomGenerics.arguments(creation).get(0);
          cellTypeName = AstNodeUtils.getFullyQualifiedName(cellExpression, false);
        }
      }
    }
    // get Cell from Column instance
    if (cellTypeName == null) {
      Object cell = ReflectionUtils.invokeMethod2(getObject(), "getCell");
      Class<?> cellClass = cell.getClass();
      cellTypeName = ReflectionUtils.getCanonicalName(cellClass);
    }
    // use icon which corresponds to the Cell type
    if (cellTypeName != null) {
      int index = ArrayUtils.indexOf(CELL_TYPES, cellTypeName);
      if (index != ArrayUtils.INDEX_NOT_FOUND) {
        m_specialIcon = getCreationIcon(CELL_IDS[index]);
      }
    }
  }
}
