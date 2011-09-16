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
package com.google.gdt.eclipse.designer.util.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gdt.eclipse.designer.Activator;
import com.google.gdt.eclipse.designer.model.module.ModuleElement;
import com.google.gdt.eclipse.designer.model.web.WebUtils;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.ModuleVisitor;
import com.google.gdt.eclipse.designer.util.resources.IResourcesProvider;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.SearchPattern;

import org.apache.commons.lang.ArrayUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Dialog for selecting resource from public folder of given module and inherited modules.
 * 
 * @author scheglov_ke
 * @coverage gwt.util.ui
 */
public class ResourceSelectionDialog extends ResizableDialog {
  private final IResourcesProvider m_provider;
  private final String m_title;
  private final ResourceFolder m_root;
  ////////////////////////////////////////////////////////////////////////////
  //
  // UI objects
  //
  ////////////////////////////////////////////////////////////////////////////
  private Text m_namePatternText;
  private Button m_allFilesButton;
  private SearchPattern m_namePattern;
  private TreeViewer m_viewer;
  private Tree m_tree;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public ResourceSelectionDialog(Shell parentShell,
      IResourcesProvider provider,
      ModuleDescription moduleDescription,
      String title) throws Exception {
    super(parentShell, Activator.getDefault());
    m_provider = provider;
    m_title = title;
    // prepare resources
    m_root = new ResourceFolder(null, null);
    {
      addWarFolder(moduleDescription);
    }
    {
      final Set<String> visitedModules = Sets.newTreeSet();
      final Set<String> visitedPackages = Sets.newTreeSet();
      ModuleVisitor.accept(moduleDescription, new ModuleVisitor() {
        private ResourceFolder m_moduleFolder;

        @Override
        public boolean visitModule(ModuleElement module) {
          String moduleName = module.getName();
          if (visitedModules.contains(moduleName)) {
            return false;
          }
          //
          m_moduleFolder = new ResourceFolder(m_root, moduleName);
          m_root.add(m_moduleFolder);
          //
          visitedModules.add(moduleName);
          return true;
        }

        @Override
        public void visitPublicPackage(ModuleElement module, String packageName) throws Exception {
          if (!visitedPackages.contains(packageName)) {
            visitedPackages.add(packageName);
            String path = packageName.replace('.', '/');
            for (String file : m_provider.listFiles(path)) {
              m_moduleFolder.add(file, path + "/" + file);
            }
          }
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GWT 1.6 'war' folder resources
  //
  ////////////////////////////////////////////////////////////////////////////
  private void addWarFolder(ModuleDescription moduleDescription) {
    IProject project = moduleDescription.getProject();
    String webFolderName = WebUtils.getWebFolderName(project);
    IFolder webFolder = project.getFolder(webFolderName);
    if (webFolder != null && webFolder.exists()) {
      ResourceFolder resourceFolder = new ResourceFolder(m_root, webFolderName);
      m_root.add(resourceFolder);
      listWarFolder(resourceFolder, "", webFolder.getLocation().toFile());
    }
  }

  private void listWarFolder(ResourceFolder resFolder, String folderPublicPath, File folder) {
    for (File file : folder.listFiles()) {
      // prepare file path
      String filePublicPath;
      if (folderPublicPath.length() == 0) {
        filePublicPath = file.getName();
      } else {
        filePublicPath = folderPublicPath + "/" + file.getName();
      }
      // visit new folder or add file
      if (file.isDirectory()) {
        listWarFolder(resFolder, filePublicPath, file);
      } else {
        resFolder.add(filePublicPath, file.getAbsolutePath());
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link InputStream} for contents of given {@link ResourceFile}.
   */
  protected final InputStream getResourceAsStream(ResourceFile file) throws Exception {
    InputStream stream = m_provider.getResourceAsStream(file.getFullPath());
    if (stream == null) {
      // try absolute path
      File resourceFile = new File(file.getFullPath());
      if (resourceFile.exists() && resourceFile.isFile()) {
        return new FileInputStream(resourceFile);
      }
      return null;
    }
    return stream;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);
    GridLayoutFactory.create(container).columns(2).equalColumns();
    // create name pattern
    {
      {
        Label label = new Label(container, SWT.NONE);
        GridDataFactory.create(label).spanH(2);
        label.setText("Select a resource (? = any character, * = any string):");
      }
      {
        m_namePatternText = new Text(container, SWT.BORDER);
        GridDataFactory.create(m_namePatternText).spanH(2).grabH().fillH();
        m_namePatternText.addModifyListener(new ModifyListener() {
          public void modifyText(ModifyEvent e) {
            // prepare name pattern matcher
            String pattern = m_namePatternText.getText();
            if (pattern.length() != 0) {
              pattern = adjustPattern(pattern);
              m_namePattern = new SearchPattern();
              m_namePattern.setPattern(pattern);
            } else {
              m_namePattern = null;
            }
            // refresh viewer
            refreshViewer();
          }

          private String adjustPattern(String pattern) {
            pattern = pattern.trim();
            if (pattern.endsWith("<")) {
              // the < character indicates an exact match search
              return pattern.substring(0, pattern.length() - 1);
            }
            if (!pattern.equals("") && !pattern.endsWith("*")) {
              return pattern + "*";
            }
            return pattern;
          }
        });
      }
    }
    // create tree viewer group
    {
      Group treeGroup = new Group(container, SWT.NONE);
      GridDataFactory.create(treeGroup).spanV(2).grab().fill().hintHC(55).hintVC(25);
      GridLayoutFactory.create(treeGroup);
      treeGroup.setText("Matching resources");
      // create tree viewer
      {
        m_viewer = new TreeViewer(treeGroup, SWT.BORDER);
        m_viewer.setContentProvider(new ResourceContentProvider());
        m_viewer.setLabelProvider(new ResourceLabelProvider());
        m_viewer.setSorter(new ResourceSorter());
        m_viewer.addFilter(new ResourceViewerFilter());
        //
        m_tree = m_viewer.getTree();
        GridDataFactory.create(m_tree).grab().fill();
        // all listeners
        m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
          public void selectionChanged(SelectionChangedEvent event) {
            clearPreviewGroup();
            //
            IStructuredSelection selection = (IStructuredSelection) m_viewer.getSelection();
            AbstractResource resource = (AbstractResource) selection.getFirstElement();
            if (resource instanceof ResourceFile) {
              ResourceFile file = (ResourceFile) resource;
              previewFile(m_previewGroup, file);
              m_selectedResourcePath = file.getPublicPath();
            } else {
              m_selectedResourcePath = null;
            }
            // update OK button
            getButton(IDialogConstants.OK_ID).setEnabled(m_selectedResourcePath != null);
          }
        });
        m_viewer.addDoubleClickListener(new IDoubleClickListener() {
          public void doubleClick(DoubleClickEvent event) {
            if (m_selectedResourcePath != null) {
              okPressed();
            }
          }
        });
      }
    }
    // filters group
    {
      Group filtersGroup = new Group(container, SWT.NONE);
      GridDataFactory.create(filtersGroup).fill();
      GridLayoutFactory.create(filtersGroup);
      filtersGroup.setText("Filters");
      // add "all files" button
      {
        m_allFilesButton = new Button(filtersGroup, SWT.CHECK);
        m_allFilesButton.setText("All Files");
        m_allFilesButton.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            refreshViewer();
          }
        });
      }
      // add filters
      for (final ResourceFilter filter : m_filters) {
        // create check box
        final Button filterButton = new Button(filtersGroup, SWT.CHECK);
        filterButton.setText(filter.m_title);
        filterButton.setSelection(true);
        m_activeFilters.add(filter);
        // add listener
        filterButton.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (filterButton.getSelection()) {
              m_activeFilters.add(filter);
            } else {
              m_activeFilters.remove(filter);
            }
            refreshViewer();
          }
        });
      }
    }
    //
    createPreviewGroup(container);
    // initialize viewer
    {
      m_viewer.setInput(m_root);
      m_viewer.expandAll();
    }
    //
    return container;
  }

  private void refreshViewer() {
    m_viewer.refresh();
    m_viewer.expandAll();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preview
  //
  ////////////////////////////////////////////////////////////////////////////
  private Group m_previewGroup;

  private void createPreviewGroup(Composite parent) {
    m_previewGroup = new Group(parent, SWT.NONE);
    m_previewGroup.setText("Preview");
    GridDataFactory.create(m_previewGroup).fill();
  }

  /**
   * Removes all children of preview group that subclasses could create in
   * {@link #previewFile(Composite, ResourceFile)}
   */
  private void clearPreviewGroup() {
    for (Control child : m_previewGroup.getChildren()) {
      child.dispose();
    }
  }

  /**
   * This methods allows subclasses add some control on given parent that will show preview of given
   * file.
   */
  protected void previewFile(Composite parent, ResourceFile file) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog: shell
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(m_title);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog: buttons
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    getButton(IDialogConstants.OK_ID).setEnabled(false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_selectedResourcePath;

  /**
   * @return the public path of selected resource.
   */
  public String getSelectedResourcePath() {
    return m_selectedResourcePath;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Filters
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<ResourceFilter> m_filters = Lists.newArrayList();
  private final List<ResourceFilter> m_activeFilters = Lists.newArrayList();

  /**
   * Adds new filter for resource files.
   */
  public void addFilter(String title, String pattern) {
    m_filters.add(new ResourceFilter(title, pattern));
  }

  private boolean matchFilters(String resourceName) {
    // check, may be we should match all files
    if (m_allFilesButton.getSelection()) {
      return true;
    }
    // check active filters
    for (ResourceFilter filter : m_activeFilters) {
      if (filter.match(resourceName)) {
        return true;
      }
    }
    // no, given resource name does not match any filter
    return false;
  }

  /**
   * Filter for resources in {@link ResourceSelectionDialog}.
   * 
   * @author scheglov_ke
   */
  private static final class ResourceFilter {
    private final String m_title;
    private final SearchPattern m_pattern;

    public ResourceFilter(String title, String pattern) {
      m_title = title;
      m_pattern = new SearchPattern();
      m_pattern.setPattern(pattern);
    }

    public boolean match(String s) {
      return m_pattern.matches(s);
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Resource content provider
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class ResourceContentProvider implements ITreeContentProvider {
    public Object[] getElements(Object inputElement) {
      return ((ResourceFolder) inputElement).getChildren().toArray();
    }

    public Object[] getChildren(Object parentElement) {
      if (parentElement instanceof ResourceFolder) {
        ResourceFolder folder = (ResourceFolder) parentElement;
        return folder.getChildren().toArray();
      }
      return ArrayUtils.EMPTY_OBJECT_ARRAY;
    }

    public Object getParent(Object element) {
      return ((AbstractResource) element).getParent();
    }

    public boolean hasChildren(Object element) {
      return getChildren(element).length != 0;
    }

    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Resource label provider
  //
  ////////////////////////////////////////////////////////////////////////////
  private final class ResourceLabelProvider extends LabelProvider {
    @Override
    public String getText(Object element) {
      return ((AbstractResource) element).getName();
    }

    @Override
    public Image getImage(Object element) {
      if (element instanceof ResourceFolder) {
        return DesignerPlugin.getImage("folder_open.gif");
      }
      if (element instanceof ResourceFile) {
        ResourceFile file = (ResourceFile) element;
        return getIcon(file);
      }
      return null;
    }
  }

  protected Image getIcon(ResourceFile file) {
    String extension = file.getExtension();
    if (extension != null) {
      return UiUtils.getIcon(extension);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ResourceFilter
  //
  ////////////////////////////////////////////////////////////////////////////
  private final class ResourceViewerFilter extends ViewerFilter {
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
      return select(element);
    }

    private boolean select(Object element) {
      if (element instanceof ResourceFile) {
        ResourceFile file = (ResourceFile) element;
        String fileName = file.getName();
        // check for filters
        if (!matchFilters(fileName)) {
          return false;
        }
        // check for name filter
        if (m_namePattern != null) {
          return m_namePattern.matches(fileName);
        }
        // OK, accept this file
        return true;
      }
      if (element instanceof ResourceFolder) {
        ResourceFolder folder = (ResourceFolder) element;
        for (AbstractResource resource : folder.getChildren()) {
          if (select(resource)) {
            return true;
          }
        }
      }
      return false;
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Resource sorter
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class ResourceSorter extends ViewerSorter {
    @Override
    public int category(Object element) {
      if (element instanceof ResourceFolder) {
        return 0;
      }
      return 1;
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractResource
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Abstract resource.
   */
  private static class AbstractResource {
    private final ResourceFolder m_parent;
    private final String m_name;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public AbstractResource(ResourceFolder parent, String name) {
      m_parent = parent;
      m_name = name;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public ResourceFolder getParent() {
      return m_parent;
    }

    public String getName() {
      return m_name;
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // ResourceFolder
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Folder with resources.
   */
  private static final class ResourceFolder extends AbstractResource {
    private final List<AbstractResource> m_children = Lists.newArrayList();
    private final Map<String, AbstractResource> m_nameToChild = Maps.newTreeMap();

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public ResourceFolder(ResourceFolder parent, String name) {
      super(parent, name);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public Collection<AbstractResource> getChildren() {
      return m_children;
    }

    public AbstractResource getChild(String name) {
      return m_nameToChild.get(name);
    }

    public void add(AbstractResource resource) {
      m_children.add(resource);
      m_nameToChild.put(resource.getName(), resource);
    }

    public void add(String publicPath, String fullPath) {
      IPath name = new Path(publicPath);
      add(name, publicPath, fullPath);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Internal
    //
    ////////////////////////////////////////////////////////////////////////////
    private void add(IPath name, String publicPath, String fullPath) {
      if (name.segmentCount() == 1) {
        if (!publicPath.endsWith("/")) {
          ResourceFile file = new ResourceFile(this, name.toPortableString(), publicPath, fullPath);
          add(file);
        }
      } else {
        String folderName = name.segments()[0];
        ResourceFolder folder = (ResourceFolder) getChild(folderName);
        if (folder == null) {
          folder = new ResourceFolder(this, folderName);
          add(folder);
        }
        folder.add(name.removeFirstSegments(1), publicPath, fullPath);
      }
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // ResourceFile
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * File with some name and path.
   */
  protected static final class ResourceFile extends AbstractResource {
    private final String m_publicPath;
    private final String m_fullPath;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public ResourceFile(ResourceFolder parent, String name, String publicPath, String fullPath) {
      super(parent, name);
      m_publicPath = publicPath;
      m_fullPath = fullPath;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public String getPublicPath() {
      return m_publicPath;
    }

    public String getFullPath() {
      return m_fullPath;
    }

    public String getExtension() {
      return new Path(m_fullPath).getFileExtension();
    }
  }
}
