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
package com.google.gdt.eclipse.designer.model.widgets;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDisplayPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Helper that supports width/height manipulations and properties for {@link UIObjectInfo}.
 * 
 * @author scheglov_ke
 * @coverage gwt.model
 */
public class UIObjectSizeSupport implements IUIObjectSizeSupport {
  protected final UIObjectInfo m_object;
  private final PropertyEditor m_sizePropertyEditor = new Size_PropertyEditor();
  private final Property m_sizeProperty = new Size_Property();
  private final Property[] m_sizeElementProperties = new Property[]{
      new SizeWidth_Propety(),
      new SizeHeight_Property()};

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UIObjectSizeSupport(UIObjectInfo object) {
    m_object = object;
    addClipboardSupport();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Broadcasts
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds support for size coping.
   */
  private void addClipboardSupport() {
    m_object.addBroadcastListener(new JavaEventListener() {
      @Override
      public void clipboardCopy(JavaInfo javaInfo, List<ClipboardCommand> commands)
          throws Exception {
        if (javaInfo == m_object) {
          final SizeInfo sizeInfo = getSizeInfo();
          if (sizeInfo.isModified()) {
            commands.add(new ClipboardCommand() {
              private static final long serialVersionUID = 0L;

              @Override
              public void execute(JavaInfo javaInfo) throws Exception {
                UIObjectInfo object = (UIObjectInfo) javaInfo;
                object.getSizeSupport().setSize(sizeInfo.m_width, sizeInfo.m_height);
              }
            });
          }
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setSize(int width, int height) throws Exception {
    setSize(new Dimension(width, height));
  }

  public void setSize(Dimension size) throws Exception {
    if (size != null) {
      int clientWidth = size.width;
      int clientHeight = size.height;
      // exclude decorations
      if (m_object.getObject() != null) {
        if (executeBooleanScript("setSize.excludeMargin")) {
          Insets margins = m_object.getMargins();
          clientWidth -= margins.getWidth();
          clientHeight -= margins.getHeight();
        }
        if (executeBooleanScript("setSize.excludeBorder")) {
          Insets borders = m_object.getBorders();
          clientWidth -= borders.getWidth();
          clientHeight -= borders.getHeight();
        }
        if (executeBooleanScript("setSize.excludePadding")) {
          Insets paddings = m_object.getPaddings();
          clientWidth -= paddings.getWidth();
          clientHeight -= paddings.getHeight();
        }
      }
      // set size in pixels
      String width = clientWidth + "px";
      String height = clientHeight + "px";
      setSize(width, height);
    } else {
      setSize(NO_SIZE, NO_SIZE);
    }
  }

  public void setSize(final String width, final String height) throws Exception {
    if (width != null || height != null) {
      ExecutionUtils.run(m_object, new RunnableEx() {
        public void run() throws Exception {
          setSize0(width, height);
        }
      });
    }
  }

  /**
   * @return {@link Boolean} result of executing script.
   */
  private boolean executeBooleanScript(String scriptName) throws Exception {
    Map<String, Object> variables = Maps.newTreeMap();
    variables.put("model", m_object);
    variables.put("object", m_object.getObject());
    String script = JavaInfoUtils.getParameter(m_object, scriptName);
    return (Boolean) m_object.getUIObjectUtils().executeScript(script, variables);
  }

  /**
   * Implementation for {@link #setSize(String, String)}.
   */
  private void setSize0(String width, String height) throws Exception {
    SizeInfo sizeInfo = getSizeInfo();
    if (width == null) {
      width = sizeInfo.m_width;
    }
    if (height == null) {
      height = sizeInfo.m_height;
    }
    boolean widthHas = width != null && width != NO_SIZE;
    boolean heightHas = height != null && height != NO_SIZE;
    String widthString = StringConverter.INSTANCE.toJavaSource(m_object, width);
    String heightString = StringConverter.INSTANCE.toJavaSource(m_object, height);
    // remember existing size-related invocations (we can not remove them, because they are targets)
    List<MethodInvocation> oldInvocations;
    {
      oldInvocations = Lists.newArrayList();
      setSize0_addInvocationsToRemove(oldInvocations);
    }
    // add new invocation
    setSize0_addInvocation(sizeInfo.m_target, widthHas, heightHas, widthString, heightString);
    // remove old size-related invocations
    for (MethodInvocation invocation : oldInvocations) {
      m_object.getEditor().removeEnclosingStatement(invocation);
    }
  }

  /**
   * Adds {@link MethodInvocation} which also set size, so should be removed.
   */
  protected void setSize0_addInvocationsToRemove(List<MethodInvocation> oldInvocations) {
    oldInvocations.addAll(getMethodInvocations("setSize(java.lang.String,java.lang.String)"));
    oldInvocations.addAll(getMethodInvocations("setPixelSize(int,int)"));
    oldInvocations.addAll(getMethodInvocations("setWidth(java.lang.String)"));
    oldInvocations.addAll(getMethodInvocations("setHeight(java.lang.String)"));
  }

  /**
   * Adds {@link MethodInvocation} that sets size.
   */
  protected void setSize0_addInvocation(StatementTarget target,
      boolean widthHas,
      boolean heightHas,
      String widthString,
      String heightString) throws Exception {
    // we have width/height, use setSize(String,String)
    if (widthHas && heightHas) {
      String whString = widthString + ", " + heightString;
      addMethodInvocation(target, "setSize(java.lang.String,java.lang.String)", whString);
    }
    // we have only width, use setWidth(String)
    if (widthHas && !heightHas) {
      addMethodInvocation(target, "setWidth(java.lang.String)", widthString);
    }
    // we have only height, use setHeight(String)
    if (!widthHas && heightHas) {
      addMethodInvocation(target, "setHeight(java.lang.String)", heightString);
    }
  }

  /**
   * @return the complex "Size" property.
   */
  public Property getSizeProperty() {
    return m_sizeProperty;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Information about "size" state.
   */
  private static final class SizeInfo implements Serializable {
    private static final long serialVersionUID = 0L;
    String m_width;
    String m_height;
    transient StatementTarget m_target;

    boolean isModified() {
      return m_width != null || m_height != null;
    }
  }

  /**
   * @return the {@link SizeInfo} for current size state of this {@link UIObjectInfo}.
   */
  private SizeInfo getSizeInfo() throws Exception {
    String width = null;
    String height = null;
    StatementTarget target = null;
    // setSize(String,String)
    for (MethodInvocation invocation : getMethodInvocations("setSize(java.lang.String,java.lang.String)")) {
      width = getExpressionString(invocation, 0, width);
      height = getExpressionString(invocation, 1, height);
      if (target == null) {
        target = getTarget(invocation);
      }
    }
    // setPixelSize(int,int)
    for (MethodInvocation invocation : getMethodInvocations("setPixelSize(int,int)")) {
      width = getExpressionString(invocation, 0, width);
      height = getExpressionString(invocation, 1, height);
      if (target == null) {
        target = getTarget(invocation);
      }
    }
    // setWidth(String)
    for (MethodInvocation invocation : getMethodInvocations("setWidth(java.lang.String)")) {
      width = getExpressionString(invocation, 0, width);
      if (target == null) {
        target = getTarget(invocation);
      }
    }
    // setHeight(String)
    for (MethodInvocation invocation : getMethodInvocations("setHeight(java.lang.String)")) {
      height = getExpressionString(invocation, 0, height);
      if (target == null) {
        target = getTarget(invocation);
      }
    }
    // final result
    SizeInfo sizeInfo = new SizeInfo();
    sizeInfo.m_width = width;
    sizeInfo.m_height = height;
    sizeInfo.m_target = target;
    return sizeInfo;
  }

  /**
   * @return the {@link StatementTarget} for position before given {@link MethodInvocation}.
   */
  private static StatementTarget getTarget(MethodInvocation invocation) {
    return new StatementTarget(AstNodeUtils.getEnclosingStatement(invocation), true);
  }

  /**
   * @return the CSS size string of given {@link MethodInvocation} argument.
   */
  private static String getExpressionString(MethodInvocation invocation, int index, String def) {
    Expression expression = DomGenerics.arguments(invocation).get(index);
    Object value = JavaInfoEvaluationHelper.getValue(expression);
    if (value instanceof String) {
      return (String) value;
    }
    if (value instanceof Integer) {
      return ((Integer) value).toString() + "px";
    }
    return def;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Redirects
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Redirect for {@link JavaInfo#getMethodInvocations(String)}.
   */
  protected final List<MethodInvocation> getMethodInvocations(String signature) {
    return m_object.getMethodInvocations(signature);
  }

  /**
   * Redirect for {@link JavaInfo#addMethodInvocation(StatementTarget, String, String)}.
   */
  private void addMethodInvocation(StatementTarget target, String signature, String arguments)
      throws Exception {
    if (target != null) {
      m_object.addMethodInvocation(target, signature, arguments);
    } else {
      m_object.addMethodInvocation(signature, arguments);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SizeWidth_Property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link Property} for "width" of size.
   */
  private final class SizeWidth_Propety extends Property {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public SizeWidth_Propety() {
      super(StringPropertyEditor.INSTANCE);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Property
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public String getTitle() {
      return "width";
    }

    @Override
    public boolean isModified() throws Exception {
      return getValue() != null;
    }

    @Override
    public Object getValue() throws Exception {
      return getSizeInfo().m_width;
    }

    @Override
    public void setValue(Object value) throws Exception {
      if (value instanceof String) {
        setSize((String) value, null);
      } else if (value == Property.UNKNOWN_VALUE) {
        setSize(NO_SIZE, null);
      }
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // SizeHeight_Property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link Property} for "height" of size.
   */
  private final class SizeHeight_Property extends Property {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public SizeHeight_Property() {
      super(StringPropertyEditor.INSTANCE);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Property
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public String getTitle() {
      return "height";
    }

    @Override
    public boolean isModified() throws Exception {
      return getValue() != null;
    }

    @Override
    public Object getValue() throws Exception {
      return getSizeInfo().m_height;
    }

    @Override
    public void setValue(Object value) throws Exception {
      if (value instanceof String) {
        setSize(null, (String) value);
      } else if (value == Property.UNKNOWN_VALUE) {
        setSize(null, NO_SIZE);
      }
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Size_Property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Property to set size of {@link UIObjectInfo}.
   * 
   * @author scheglov_ke
   * @coverage gwt.property
   */
  private final class Size_Property extends Property {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public Size_Property() {
      super(m_sizePropertyEditor);
      setCategory(PropertyCategory.system(8));
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Property
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public String getTitle() {
      return "Size";
    }

    @Override
    public Object getValue() throws Exception {
      return null;
    }

    @Override
    public boolean isModified() throws Exception {
      return getSizeInfo().isModified();
    }

    @Override
    public void setValue(Object value) throws Exception {
      if (value == Property.UNKNOWN_VALUE) {
        setSize(NO_SIZE, NO_SIZE);
      }
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Size_PropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link PropertyEditor} for complex "Size" property.
   */
  private final class Size_PropertyEditor extends TextDisplayPropertyEditor
      implements
        IComplexPropertyEditor {
    ////////////////////////////////////////////////////////////////////////////
    //
    // TextDisplayPropertyEditor
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected String getText(Property property) throws Exception {
      SizeInfo sizeInfo = getSizeInfo();
      if (sizeInfo.isModified()) {
        return "(" + sizeInfo.m_width + ", " + sizeInfo.m_height + ")";
      } else {
        Rectangle bounds = m_object.getBounds();
        return bounds != null ? "(" + bounds.width + ", " + bounds.height + ")" : "(unknown)";
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IComplexPropertyEditor
    //
    ////////////////////////////////////////////////////////////////////////////
    public Property[] getProperties(Property property) throws Exception {
      return m_sizeElementProperties;
    }
  }
}
