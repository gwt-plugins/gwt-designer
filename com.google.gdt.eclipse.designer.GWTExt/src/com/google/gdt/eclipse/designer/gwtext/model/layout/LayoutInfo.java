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
package com.google.gdt.eclipse.designer.gwtext.model.layout;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.gwtext.model.layout.assistant.LayoutAssistant;
import com.google.gdt.eclipse.designer.gwtext.model.widgets.ContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;
import com.google.gdt.eclipse.designer.model.widgets.support.IGwtStateProvider;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.association.EmptyAssociation;
import org.eclipse.wb.core.model.broadcast.BroadcastSupport;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildAddAfter;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildAddBefore;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.core.model.broadcast.ObjectInfoTreeComplete;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.presentation.ButtonPropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;

import org.apache.commons.lang.NotImplementedException;

import java.util.List;

/**
 * Model for <code>ContainerLayout</code>.
 * 
 * @author scheglov_ke
 * @coverage GWTExt.model.layout
 */
public class LayoutInfo extends JavaInfo implements IGwtStateProvider {
  private final LayoutInfo m_this = this;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    addLayoutPropertySupport();
    addClipboardSupport();
    addBroadcastListeners();
    new LayoutNameSupport(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialize
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void initialize() throws Exception {
    super.initialize();
    initializeLayoutAssistant();
  }

  /**
   * Create layout assistant support.
   */
  protected void initializeLayoutAssistant() {
    new LayoutAssistant(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IGWTStateProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public GwtState getState() {
    return getContainer().getState();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Broadcast events
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds listeners to the {@link BroadcastSupport}.
   */
  private void addBroadcastListeners() {
    addBroadcastListener(new ObjectInfoTreeComplete() {
      public void invoke() throws Exception {
        ContainerInfo container = getContainer();
        // ensure that all widgets have LayoutData, at least virtual
        if (isActiveOnContainer(container)) {
          for (WidgetInfo widget : container.getChildrenWidgets()) {
            ensureLayoutData(widget);
          }
        }
      }
    });
    addBroadcastListener(new ObjectInfoChildAddBefore() {
      public void invoke(ObjectInfo parent, ObjectInfo child, ObjectInfo[] nextChild)
          throws Exception {
        // add new LayoutData - remove existing one 
        if (parent instanceof WidgetInfo
            && child instanceof LayoutDataInfo
            && isActiveOnContainer(parent.getParent())) {
          WidgetInfo widget = (WidgetInfo) parent;
          LayoutDataInfo existingLayoutData = getLayoutData(widget);
          if (existingLayoutData != null) {
            widget.removeChild(existingLayoutData);
          }
        }
      }
    });
    addBroadcastListener(new ObjectInfoChildAddAfter() {
      public void invoke(ObjectInfo parent, ObjectInfo child) throws Exception {
        ContainerInfo container = getContainer();
        // add this layout
        if (child == m_this) {
          // implicit layouts are bound to its parent
          if (getCreationSupport() instanceof IImplicitCreationSupport) {
            targetBroadcastListener(parent);
          }
          // create virtual LayoutData's
          for (WidgetInfo widget : container.getChildrenWidgets()) {
            ensureLayoutData(widget);
          }
        }
      }
    });
    addBroadcastListener(new ObjectInfoDelete() {
      @Override
      public void before(ObjectInfo parent, ObjectInfo child) throws Exception {
        // delete this layout
        if (child == m_this) {
          onDelete();
        }
        // delete Widget_Info from this container
        if (isActiveOnContainer(parent) && child instanceof WidgetInfo) {
          onWidgetRemoveBefore((WidgetInfo) child);
        }
      }

      @Override
      public void after(ObjectInfo parent, ObjectInfo child) throws Exception {
        // LayoutData was deleted - create virtual
        if (parent instanceof WidgetInfo
            && child instanceof LayoutDataInfo
            && shouldCreateLayoutData((WidgetInfo) parent)
            && isActiveOnContainer(parent.getParent())) {
          WidgetInfo widget = (WidgetInfo) parent;
          ensureLayoutData(widget);
        }
        // Widget_Info was deleted
        if (isActiveOnContainer(parent) && child instanceof WidgetInfo) {
          WidgetInfo widget = (WidgetInfo) child;
          if (widget.isDeleted()) {
            onWidgetRemoveAfter(widget);
          }
        }
      }
    });
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
        // new Widget_Info added, ensure layout data
        if (isActiveOnContainer(parent) && child instanceof WidgetInfo) {
          ensureLayoutData((WidgetInfo) child);
        }
      }

      @Override
      public void moveBefore(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
          throws Exception {
        // move Widget_Info FROM this container
        if (isActiveOnContainer(oldParent) && child instanceof WidgetInfo && newParent != oldParent) {
          WidgetInfo widget = (WidgetInfo) child;
          onWidgetRemoveBefore(widget);
          deleteLayoutData(widget);
        }
      }

      @Override
      public void moveAfter(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
          throws Exception {
        // move Widget_Info FROM this container
        if (isActiveOnContainer(oldParent) && child instanceof WidgetInfo && newParent != oldParent) {
          onWidgetRemoveAfter((WidgetInfo) child);
        }
        // move Widget_Info TO this container
        if (isActiveOnContainer(newParent) && child instanceof WidgetInfo && newParent != oldParent) {
          ensureLayoutData((WidgetInfo) child);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final IObjectPresentation getPresentation() {
    return new LayoutPresentation(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ContainerInfo} that contains this {@link LayoutInfo}.
   */
  public final ContainerInfo getContainer() {
    return (ContainerInfo) getParent();
  }

  /**
   * @return <code>true</code> if this {@link LayoutInfo} is active on its {@link ContainerInfo}.
   *         For example implicit {@link LayoutInfo}'s replaced by "real" {@link LayoutInfo} are
   *         inactive.
   */
  public final boolean isActive() {
    ContainerInfo container = getContainer();
    return isActiveOnContainer(container);
  }

  /**
   * @return <code>true</code> if this {@link LayoutInfo} is active on its {@link ContainerInfo}.
   */
  protected final boolean isActiveOnContainer(ObjectInfo container) {
    return container != null && container.getChildren().contains(this);
  }

  /**
   * @return the text to display for user.
   */
  protected String getTitle() {
    return getDescription().getComponentClass().getName();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout notifications
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * This method is invoked when this {@link LayoutInfo} is set on its {@link ContainerInfo}.
   */
  public void onSet() throws Exception {
  }

  /**
   * This method is invoked when this {@link LayoutInfo} is deleted from its {@link ContainerInfo}.
   */
  protected void onDelete() throws Exception {
    for (WidgetInfo widget : getContainer().getChildrenWidgets()) {
      deleteLayoutData(widget);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Widget notifications
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Notification that given {@link WidgetInfo} will be removed from container.
   */
  protected void onWidgetRemoveBefore(WidgetInfo widget) throws Exception {
  }

  /**
   * Notification that given {@link WidgetInfo} was removed from container.
   */
  protected void onWidgetRemoveAfter(WidgetInfo widget) throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Layout" property
  //
  ////////////////////////////////////////////////////////////////////////////
  private ComplexProperty m_complexProperty;

  /**
   * Adds this {@link LayoutInfo} properties to {@link ContainerInfo}.
   */
  private void addLayoutPropertySupport() {
    addBroadcastListener(new JavaInfoAddProperties() {
      public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
        // add layout properties to container properties
        if (isActiveOnContainer(javaInfo)) {
          addLayoutProperties(properties);
        }
      }
    });
  }

  /**
   * Adds properties of this {@link LayoutInfo} to the properties of its {@link ContainerInfo}.
   */
  private void addLayoutProperties(List<Property> properties) throws Exception {
    // prepare layout complex property
    if (m_complexProperty == null) {
      String text = "(" + getTitle() + ")";
      m_complexProperty = new ComplexProperty("Layout", text) {
        @Override
        public boolean isModified() throws Exception {
          return true;
        }

        @Override
        public void setValue(Object value) throws Exception {
          if (value == UNKNOWN_VALUE) {
            delete();
          }
        }
      };
      m_complexProperty.setCategory(PropertyCategory.system(5));
      m_complexProperty.setEditorPresentation(new ButtonPropertyEditorPresentation() {
        @Override
        protected Image getImage() {
          return DesignerPlugin.getImage("properties/down.png");
        }

        @Override
        protected void onClick(PropertyTable propertyTable, Property property) throws Exception {
          MenuManager manager = new MenuManager();
          getContainer().fillLayoutsManager(manager);
          Menu menu = manager.createContextMenu(propertyTable);
          UiUtils.showAndDisposeOnHide(menu);
        }
      });
      // set sub-properties
      List<Property> subProperties =
          PropertyUtils.getProperties_excludeByParameter(this, "layout.exclude-properties");
      m_complexProperty.setProperties(subProperties);
    }
    // add property
    properties.add(m_complexProperty);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LayoutData management
  //
  ////////////////////////////////////////////////////////////////////////////
  private static String KEY_DONT_CREATE_VIRTUAL_DATA =
      "don't create virtual LayoutData for this Widget_Info";

  /**
   * We may be {@link Widget)Info} that virtual {@link LayoutDataInfo} should not be created for it,
   * when we intentionally delete {@link LayoutDataInfo}, for example during process of moving this
   * {@link WidgetInfo} from this {@link LayoutInfo} or deleting this {@link LayoutInfo}.
   * 
   * @return <code>true</code> if for given {@link WidgetInfo} we should create
   *         {@link LayoutDataInfo}.
   */
  private boolean shouldCreateLayoutData(WidgetInfo widget) {
    return widget.getArbitraryValue(KEY_DONT_CREATE_VIRTUAL_DATA) == null;
  }

  /**
   * Delete {@link LayoutDataInfo} associated with given {@link WidgetInfo}.
   * <p>
   * Note that this is different than {@link LayoutDataInfo#delete()} because we don't remove
   * implicit/virtual {@link LayoutDataInfo} from list of children in
   * {@link CreationSupport#delete()}. {@link CreationSupport#delete()} has to remove only
   * {@link ASTNode}'s related with {@link LayoutDataInfo}. So, we need separate operation to remove
   * {@link LayoutDataInfo} from list of children.
   */
  protected void deleteLayoutData(WidgetInfo widget) throws Exception {
    widget.putArbitraryValue(KEY_DONT_CREATE_VIRTUAL_DATA, Boolean.TRUE);
    try {
      LayoutDataInfo layoutData = getLayoutData(widget);
      if (layoutData != null) {
        layoutData.delete();
        // if implicit/virtual, so still alive, force remove from children
        if (!layoutData.isDeleted()) {
          widget.removeChild(layoutData);
        }
      }
    } finally {
      widget.removeArbitraryValue(KEY_DONT_CREATE_VIRTUAL_DATA);
    }
  }

  /**
   * @return {@link LayoutDataInfo} associated with given {@link WidgetInfo}, or <code>null</code>
   *         if no {@link LayoutDataInfo} expected for parent {@link LayoutInfo}.
   */
  public static LayoutDataInfo getLayoutData(WidgetInfo widget) {
    // select only layout data
    List<LayoutDataInfo> objects = Lists.newArrayList();
    for (ObjectInfo object : widget.getChildren()) {
      if (object instanceof LayoutDataInfo) {
        objects.add((LayoutDataInfo) object);
      }
    }
    // check for no layout data
    if (objects.isEmpty()) {
      return null;
    }
    // only one layout data can be set
    Assert.isTrue(objects.size() == 1);
    return objects.get(0);
  }

  /**
   * Ensure that if {@link LayoutDataInfo} should exist for given component, there is "real"
   * {@link LayoutDataInfo}, or create "virtual"/"implicit" {@link LayoutDataInfo}.
   */
  private void ensureLayoutData(WidgetInfo widget) throws Exception {
    if (hasLayoutData()) {
      LayoutDataInfo layoutData = getLayoutData(widget);
      if (layoutData == null) {
        createVirtualLayoutData(widget);
      }
    }
  }

  /**
   * Creates virtual {@link LayoutDataInfo} for given {@link WidgetInfo}.
   * <p>
   * "Virtual" {@link LayoutDataInfo} is placeholder for "layout data" when "layout data" should
   * exist, but does not exist yet in source code. Most layout managers in this case use
   * "layout data" with some default values. So, we show these values in properties and allow to
   * change them, at this moment we "materialize" {@link LayoutDataInfo} in source code.
   */
  private void createVirtualLayoutData(WidgetInfo widget) throws Exception {
    Object dataObject = getDefaultVirtualDataObject(widget);
    // create model
    JavaInfo layoutData;
    {
      CreationSupport creationSupport = new VirtualLayoutDataCreationSupport(widget, dataObject);
      layoutData =
          JavaInfoUtils.createJavaInfo(getEditor(), getLayoutDataClass(widget), creationSupport);
    }
    // configure
    layoutData.setVariableSupport(new VirtualLayoutDataVariableSupport(layoutData));
    layoutData.setAssociation(new EmptyAssociation());
    // add to widget
    widget.addChild(layoutData);
  }

  /**
   * @return default object used for "virtual" {@link LayoutDataInfo}.
   */
  protected Object getDefaultVirtualDataObject(WidgetInfo widget) throws Exception {
    throw new NotImplementedException(getClass());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Virtual Layout Data Support
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String KEY_LAYOUT_DATA_HAS = "layout-data.has";
  protected static final String KEY_LAYOUT_DATA_CLASS = "layout-data.class";
  private Class<?> m_layoutDataClass;

  /**
   * @return <code>true</code> if this layout support "virtual" layout data.
   */
  private boolean hasLayoutData() {
    return JavaInfoUtils.hasTrueParameter(this, KEY_LAYOUT_DATA_HAS);
  }

  /**
   * @return {@link Class} of layout data objects.
   */
  protected Class<?> getLayoutDataClass(WidgetInfo widget) throws Exception {
    if (m_layoutDataClass == null) {
      // extract class name
      String layoutDataClassName = JavaInfoUtils.getParameter(this, KEY_LAYOUT_DATA_CLASS);
      Assert.isNotNull(layoutDataClassName);
      Assert.isTrue(layoutDataClassName.length() != 0);
      // load class
      m_layoutDataClass = JavaInfoUtils.getClassLoader(this).loadClass(layoutDataClassName);
    }
    return m_layoutDataClass;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new {@link WidgetInfo} using <code>Container.add(Widget,LayoutData)</code>.
   */
  public void command_CREATE(WidgetInfo component, WidgetInfo nextComponent) throws Exception {
    ContainerInfo container = getContainer();
    AssociationObject association = getAssociation_();
    JavaInfoUtils.add(component, association, container, nextComponent);
  }

  /**
   * Moves {@link WidgetInfo} in/to this container using
   * <code>Container.add(Widget,LayoutData)</code>.
   */
  public void command_MOVE(WidgetInfo component, WidgetInfo nextComponent) throws Exception {
    ContainerInfo container = getContainer();
    AssociationObject association = getAssociation_();
    JavaInfoUtils.move(component, association, container, nextComponent);
  }

  /**
   * @return the {@link AssociationObject} for standard GWT-Ext parent/child association - using
   *         method <code>Container.add(Widget)</code>.
   */
  private static AssociationObject getAssociation_() throws Exception {
    return AssociationObjects.invocationChild("%parent%.add(%child%)", false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  private void addClipboardSupport() {
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void clipboardCopy(JavaInfo javaInfo, List<ClipboardCommand> commands)
          throws Exception {
        if (isActiveOnContainer(javaInfo)) {
          clipboardCopy_addContainerCommands(commands);
        }
      }
    });
  }

  /**
   * Adds commands for coping parent {@link ContainerInfo}.
   */
  protected void clipboardCopy_addContainerCommands(List<ClipboardCommand> commands)
      throws Exception {
    for (WidgetInfo widget : getContainer().getChildrenWidgets()) {
      if (!(widget.getCreationSupport() instanceof IImplicitCreationSupport)) {
        clipboardCopy_addWidgetCommands(widget, commands);
      }
    }
  }

  /**
   * Adds commands for coping {@link WidgetInfo} on parent {@link ContainerInfo}.
   */
  protected void clipboardCopy_addWidgetCommands(WidgetInfo widget, List<ClipboardCommand> commands)
      throws Exception {
  }
}
