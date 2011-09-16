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
package com.google.gdt.eclipse.designer.gxt.model.layout;

import com.google.gdt.eclipse.designer.gxt.model.widgets.LayoutContainerInfo;
import com.google.gdt.eclipse.designer.model.widgets.WidgetInfo;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;
import com.google.gdt.eclipse.designer.model.widgets.support.IGwtStateProvider;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildAddAfter;
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
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;

import java.util.List;

/**
 * Abstract model for <code>Layout</code>.
 * 
 * @author scheglov_ke
 * @coverage ExtGWT.model.layout
 */
public class LayoutInfo extends JavaInfo implements IGwtStateProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
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
  private final LayoutDataSupport m_layoutDataSupport = new LayoutDataSupport(this) {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public LayoutContainerInfo getContainer() {
      return LayoutInfo.this.getContainer();
    }

    @Override
    protected boolean isActiveOnContainer(ObjectInfo container) {
      return LayoutInfo.this.isActiveOnContainer(container);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Layout notifications
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onSet() throws Exception {
      super.onSet();
      LayoutInfo.this.onSet();
    }

    @Override
    protected void onDelete() throws Exception {
      super.onDelete();
      LayoutInfo.this.onDelete();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Widget notifications
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onWidgetAddAfter(WidgetInfo widget) throws Exception {
      super.onWidgetAddAfter(widget);
      LayoutInfo.this.onWidgetAddAfter(widget);
    }

    @Override
    protected void onWidgetRemoveBefore(WidgetInfo widget) throws Exception {
      super.onWidgetRemoveBefore(widget);
      LayoutInfo.this.onWidgetRemoveBefore(widget);
    }

    @Override
    protected void onWidgetRemoveAfter(WidgetInfo widget) throws Exception {
      super.onWidgetRemoveAfter(widget);
      LayoutInfo.this.onWidgetRemoveAfter(widget);
    }
  };

  private void addBroadcastListeners() {
    addBroadcastListener(new ObjectInfoChildAddAfter() {
      public void invoke(ObjectInfo parent, ObjectInfo child) throws Exception {
        // add this layout
        if (child == LayoutInfo.this) {
          // implicit layouts are bound to its parent
          if (getCreationSupport() instanceof IImplicitCreationSupport) {
            targetBroadcastListener(parent);
          }
        }
      }
    });
    addBroadcastListener(new JavaEventListener() {
      @Override
      public void clipboardCopy(JavaInfo javaInfo, List<ClipboardCommand> commands)
          throws Exception {
        if (isActiveOnContainer(javaInfo)) {
          clipboardCopy_addContainerCommands(commands);
        }
      }
    });
    addBroadcastListener(new JavaInfoAddProperties() {
      public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
        // add layout properties to container properties
        if (isActiveOnContainer(javaInfo)) {
          addLayoutProperties(properties);
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
   * @return the {@link LayoutContainerInfo} that contains this {@link LayoutInfo}.
   */
  public final LayoutContainerInfo getContainer() {
    return (LayoutContainerInfo) getParent();
  }

  /**
   * @return <code>true</code> if this {@link LayoutInfo} is active on its
   *         {@link LayoutContainerInfo}. For example implicit {@link LayoutInfo}'s replaced by
   *         "real" {@link LayoutInfo} are inactive.
   */
  public final boolean isActive() {
    LayoutContainerInfo container = getContainer();
    return isActiveOnContainer(container);
  }

  /**
   * @return <code>true</code> if this {@link LayoutInfo} is active on its
   *         {@link LayoutContainerInfo}.
   */
  private boolean isActiveOnContainer(ObjectInfo container) {
    return container != null && container.getChildren().contains(this);
  }

  /**
   * @return <code>true</code> if given {@link Object} is managed by this {@link LayoutInfo}. This
   *         means:
   *         <ul>
   *         <li>this {@link LayoutInfo} is {@link #isActive()}.</li>
   *         <li>given {@link ObjectInfo} is {@link WidgetInfo} and child of {@link #getContainer()}
   *         .</li>
   *         <li>given {@link WidgetInfo} is in {@link #getWidgets()}.</li>
   *         </ul>
   */
  public final boolean isManagedObject(Object object) {
    return object instanceof WidgetInfo
        && isActive()
        && getContainer().getChildren().contains(object)
        && getWidgets().contains(object);
  }

  /**
   * @return the text to display for user.
   */
  protected String getTitle() {
    return getDescription().getComponentClass().getName();
  }

  /**
   * @return the {@link WidgetInfo} that are managed by this {@link LayoutInfo}. This excludes for
   *         example indirectly exposed {@link WidgetInfo}'s.
   */
  public final List<WidgetInfo> getWidgets() {
    return m_layoutDataSupport.getWidgets();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Layout" property
  //
  ////////////////////////////////////////////////////////////////////////////
  private ComplexProperty m_layoutComplexProperty;

  /**
   * Adds properties of this {@link LayoutInfo} to the properties of its {@link LayoutContainerInfo}
   * .
   */
  private void addLayoutProperties(List<Property> properties) throws Exception {
    // prepare layout complex property
    {
      Property[] layoutProperties = getProperties();
      if (m_layoutComplexProperty == null) {
        String text = "(" + getTitle() + ")";
        m_layoutComplexProperty = new ComplexProperty("Layout", text) {
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
        m_layoutComplexProperty.setCategory(PropertyCategory.system(5));
        m_layoutComplexProperty.setEditorPresentation(new ButtonPropertyEditorPresentation() {
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
      }
      m_layoutComplexProperty.setProperties(layoutProperties);
    }
    // add property
    properties.add(m_layoutComplexProperty);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout notifications
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * This method is invoked when this {@link LayoutInfo} is set on its {@link LayoutContainerInfo} .
   */
  public void onSet() throws Exception {
  }

  /**
   * This method is invoked when this {@link LayoutInfo} is deleted from its
   * {@link LayoutContainerInfo}.
   */
  protected void onDelete() throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Widget notifications
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Notification that given {@link WidgetInfo} was added to container: new widget or move from
   * other container.
   */
  protected void onWidgetAddAfter(WidgetInfo widget) throws Exception {
  }

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
  // LayoutData management
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link LayoutDataInfo} associated with given {@link WidgetInfo}, or <code>null</code>
   *         if no {@link LayoutDataInfo} expected for parent {@link LayoutInfo}.
   */
  public static LayoutDataInfo getLayoutData(WidgetInfo widget) {
    return LayoutDataSupport.getLayoutData(widget);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new {@link WidgetInfo} using <code>LayoutContainer.add(Widget,LayoutData)</code>.
   */
  public void command_CREATE(WidgetInfo widget, WidgetInfo nextWidget) throws Exception {
    LayoutContainerInfo container = getContainer();
    AssociationObject association = getAssociation_();
    JavaInfoUtils.add(widget, association, container, nextWidget);
  }

  /**
   * Moves {@link WidgetInfo} in/to this container using
   * <code>LayoutContainer.add(Widget,LayoutData)</code>.
   */
  public void command_MOVE(WidgetInfo widget, WidgetInfo nextWidget) throws Exception {
    LayoutContainerInfo container = getContainer();
    AssociationObject association = getAssociation_();
    JavaInfoUtils.move(widget, association, container, nextWidget);
  }

  /**
   * @return the {@link AssociationObject} for standard Ext-GWT parent/child association - using
   *         method <code>LayoutContainer.add(Widget)</code>.
   */
  private static AssociationObject getAssociation_() throws Exception {
    return AssociationObjects.invocationChild("%parent%.add(%child%)", false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Simple container
  //
  ////////////////////////////////////////////////////////////////////////////
  public final List<ObjectInfo> getSimpleContainerChildren() {
    return getContainer().getChildren();
  }

  public void command_CREATE(WidgetInfo widget) throws Exception {
    command_CREATE(widget, null);
  }

  public void command_ADD(WidgetInfo widget) throws Exception {
    command_MOVE(widget, null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds commands for coping parent {@link LayoutContainerInfo}.
   */
  protected void clipboardCopy_addContainerCommands(List<ClipboardCommand> commands)
      throws Exception {
    for (WidgetInfo widget : getContainer().getWidgets()) {
      if (!(widget.getCreationSupport() instanceof IImplicitCreationSupport)) {
        clipboardCopy_addWidgetCommands(widget, commands);
      }
    }
  }

  /**
   * Adds commands for coping {@link WidgetInfo} on parent {@link LayoutContainerInfo}.
   */
  protected void clipboardCopy_addWidgetCommands(WidgetInfo widget, List<ClipboardCommand> commands)
      throws Exception {
  }
}