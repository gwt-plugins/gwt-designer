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
package com.google.gdt.eclipse.designer.launch;

import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.common.Constants;
import com.google.gdt.eclipse.designer.common.GwtLabelProvider;
import com.google.gdt.eclipse.designer.model.web.WebUtils;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;
import com.google.gdt.eclipse.designer.util.resources.IResourcesProvider;
import com.google.gdt.eclipse.designer.util.ui.HtmlSelectionDialog;
import com.google.gdt.eclipse.designer.wizards.model.common.AbstractGwtComposite;
import com.google.gdt.eclipse.designer.wizards.model.common.IMessageContainer;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.dialogfields.BooleanDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.ComboDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogFieldUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.IStringButtonAdapter;
import org.eclipse.wb.internal.core.utils.dialogfields.StringButtonDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.StringButtonDirectoryDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.StringDialogField;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import java.util.List;
import java.util.Map;

/**
 * @author scheglov_ke
 * @coverage gwt.launch
 */
public class MainTab extends JavaLaunchTab {
  private LaunchComposite m_composite;

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  public void createControl(Composite parent) {
    m_composite = new LaunchComposite(parent, SWT.NONE, m_messageContainer);
    setControl(m_composite);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Message container
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link IMessageContainer} for this launch configuration tab.
   */
  private final IMessageContainer m_messageContainer = new IMessageContainer() {
    public void setErrorMessage(String message) {
      MainTab.this.setErrorMessage(message);
      MainTab.this.updateLaunchConfigurationDialog();
    }
  };

  @Override
  public boolean isValid(ILaunchConfiguration launchConfig) {
    return getErrorMessage() == null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Composite
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class LaunchComposite extends AbstractGwtComposite {
    private final StringButtonDialogField m_projectField;
    private final StringDialogField m_urlField;
    private final StringButtonDialogField m_moduleField;
    private final StringButtonDialogField m_moduleHtmlField;
    private final StringDialogField m_parametersField;
    // class loader support
    private IJavaProject m_currentProject;
    private IResourcesProvider m_resourcesProvider;
    // flags
    private final StringDialogField m_portField;
    private final BooleanDialogField m_noServerField;
    private final StringDialogField m_whiteListField;
    private final StringDialogField m_blackListField;
    private final ComboDialogField m_logLevelField;
    private final StringButtonDirectoryDialogField m_dirGenField;
    private final StringButtonDirectoryDialogField m_dirWarField;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public LaunchComposite(Composite parent, int style, IMessageContainer messageContainer) {
      super(parent, style, messageContainer);
      int rootColumns = 3;
      GridLayoutFactory.create(this).columns(rootColumns);
      // project
      {
        m_projectField = new StringButtonDialogField(new IStringButtonAdapter() {
          public void changeControlPressed(DialogField field) {
            chooseProject();
          }
        });
        m_projectField.setDialogFieldListener(m_validateListener);
        m_projectField.setLabelText("Project:");
        m_projectField.setButtonLabel("&Browse...");
        DialogFieldUtils.fillControls(this, m_projectField, rootColumns, 60);
      }
      // module
      {
        m_moduleField = new StringButtonDialogField(new IStringButtonAdapter() {
          public void changeControlPressed(DialogField field) {
            chooseModule();
          }
        });
        m_moduleField.setDialogFieldListener(m_validateListener);
        m_moduleField.setButtonLabel("&Search...");
        doCreateField(this, m_moduleField, rootColumns, "Module:", null);
      }
      // module html
      {
        m_moduleHtmlField = new StringButtonDialogField(new IStringButtonAdapter() {
          public void changeControlPressed(DialogField field) {
            chooseModuleHtml();
          }
        });
        m_moduleHtmlField.setButtonLabel("&Browse...");
        doCreateField(this, m_moduleHtmlField, rootColumns, "HTML:", null);
      }
      // URL
      {
        m_parametersField = new StringDialogField();
        doCreateField(
            this,
            m_parametersField,
            rootColumns,
            "Parameters:",
            "Parameters, for example: ?module=myModule&&&objId=12345");
      }
      // URL
      {
        m_urlField = new StringDialogField();
        doCreateField(
            this,
            m_urlField,
            rootColumns,
            "URL:",
            "URL to the external server (use with -noserver flag, leave empty in usual cases)");
      }
      // HostedMode flags
      {
        int flagsColumns = 3;
        Group shellFlagsGroup = new Group(this, SWT.NONE);
        GridLayoutFactory.create(shellFlagsGroup).columns(flagsColumns);
        GridDataFactory.create(shellFlagsGroup).spanH(rootColumns).fillH();
        shellFlagsGroup.setText("HostedMode flags (hover over label for description)");
        // -noserver
        {
          m_noServerField = new BooleanDialogField();
          doCreateField(
              shellFlagsGroup,
              m_noServerField,
              flagsColumns,
              "-noserver",
              "Prevents the embedded web server from running");
        }
        // -port
        {
          m_portField = new StringDialogField();
          doCreateField(
              shellFlagsGroup,
              m_portField,
              flagsColumns,
              "-port",
              "Specifies the TCP port for the embedded web server (defaults to 8888)");
        }
        // -whitelist
        {
          m_whiteListField = new StringDialogField();
          doCreateField(
              shellFlagsGroup,
              m_whiteListField,
              flagsColumns,
              "-whitelist",
              "Allows the user to browse URLS that match the specified regexes (comma or space separated)");
        }
        // -blacklist
        {
          m_blackListField = new StringDialogField();
          doCreateField(
              shellFlagsGroup,
              m_blackListField,
              flagsColumns,
              "-blacklist",
              "Prevents the user browsing URLS that match the specified regexes (comma or space separated)");
        }
        // -logLevel
        {
          m_logLevelField = new ComboDialogField(SWT.READ_ONLY);
          m_logLevelField.setItems(new String[]{
              "ERROR",
              "WARN",
              "INFO",
              "TRACE",
              "DEBUG",
              "SPAM",
              "ALL"});
          doCreateField(
              shellFlagsGroup,
              m_logLevelField,
              flagsColumns,
              "-logLevel",
              "The level of logging detail: ERROR, WARN, INFO, TRACE, DEBUG, SPAM, or ALL");
          UiUtils.setVisibleItemCount(m_logLevelField.getComboControl(null), 10);
        }
        // -gen
        {
          m_dirGenField = new StringButtonDirectoryDialogField();
          doCreateField(
              shellFlagsGroup,
              m_dirGenField,
              flagsColumns,
              "-gen",
              "The directory into which generated files will be written for review");
        }
        // -war
        {
          m_dirWarField = new StringButtonDirectoryDialogField();
          doCreateField(
              shellFlagsGroup,
              m_dirWarField,
              flagsColumns,
              "-war",
              "The war directory to write output files into (defaults to war)");
        }
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // GUI Utils
    //
    ////////////////////////////////////////////////////////////////////////////
    private final Map<DialogField, Control[]> m_fieldControls = Maps.newHashMap();

    private void doCreateField(Composite parent,
        DialogField field,
        int columns,
        String label,
        String tooltip) {
      field.setLabelText(label);
      field.setDialogFieldListener(m_validateListener);
      {
        Control[] controls = DialogFieldUtils.fillControls(parent, field, columns, 60);
        m_fieldControls.put(field, controls);
      }
      if (tooltip != null) {
        field.getLabelControl(null).setToolTipText(tooltip);
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Validation
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected String validate() {
      // project
      IJavaProject javaProject = getProject();
      {
        if (javaProject != m_currentProject) {
          if (javaProject != null && !javaProject.equals(m_currentProject)) {
            m_currentProject = javaProject;
            clearClassLoader();
          }
        }
        String projectName = m_projectField.getText();
        if (projectName.length() == 0) {
          return "Enter GWT project name";
        }
        //
        if (javaProject == null) {
          return "Project " + projectName + " does not exist";
        }
        if (!Utils.isGWTProject(javaProject)) {
          return "Project " + projectName + " is not GWT project";
        }
      }
      // module
      ModuleDescription module;
      try {
        String moduleName = m_moduleField.getText();
        if (moduleName.length() == 0) {
          return "Enter GWT module";
        }
        // check for module existence
        module = getModule();
        if (module == null) {
          return "Module " + moduleName + " does not exist";
        }
      } catch (Throwable e) {
        DesignerPlugin.log(e);
        return "Exception during module file checking.";
      }
      // ClassLoader
      try {
        ensureCreateClassLoader();
      } catch (Throwable e) {
        DesignerPlugin.log(e);
        return "Unable to create ClassLoader, check that GWT_HOME refers valid GWT installation."
            + " See \"Error log\" for more information.";
      }
      // module HTML
      {
        String moduleHtml = m_moduleHtmlField.getText();
        String moduleName = getModuleName();
        // check empty file
        if (moduleHtml.length() == 0) {
          moduleHtml = Utils.getDefaultHTMLName(moduleName);
          m_moduleHtmlField.setTextWithoutUpdate(moduleHtml);
        }
        // check exist file
        try {
          if (!Utils.isExistingResource(module, moduleHtml)) {
            return "Module HTML " + moduleHtml + " does not exist";
          }
        } catch (Throwable e) {
          return "Exception during checking module HTML. Ensure that *.gwt.xml files are correct.";
        }
      }
      // OK
      return null;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Project
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Choose GWT project using dialog.
     */
    private void chooseProject() {
      // prepare dialog
      ILabelProvider labelProvider =
          new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
      ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
      dialog.setTitle("Project Selection");
      dialog.setMessage("Select a GWT project:");
      // set elements
      try {
        dialog.setElements(Utils.getGWTProjects().toArray());
      } catch (Throwable e) {
        DesignerPlugin.log(e);
      }
      // set initial selection
      {
        IJavaProject project = getProject();
        if (project != null) {
          dialog.setInitialSelections(new Object[]{project});
        }
      }
      // open dialog and set new project
      if (dialog.open() == Window.OK) {
        IJavaProject project = (IJavaProject) dialog.getFirstResult();
        m_projectField.setText(project.getElementName());
        m_dirWarField.setText(WebUtils.getWebFolderName(project));
      }
    }

    /**
     * Return the {@link IJavaProject} corresponding to the project name in the project name text
     * field, or <code>null</code> if the text does not match a project name.
     */
    private IJavaProject getProject() {
      // prepare project name
      String projectName = m_projectField.getText().trim();
      if (projectName.length() == 0) {
        return null;
      }
      // prepare project
      IJavaProject javaProject = Utils.getJavaModel().getJavaProject(projectName);
      if (!javaProject.exists()) {
        return null;
      }
      return javaProject;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Module
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Choose a module using dialog.
     */
    private void chooseModule() {
      IJavaProject javaProject = getProject();
      if (javaProject == null) {
        return;
      }
      // prepare dialog
      ILabelProvider labelProvider = GwtLabelProvider.INSTANCE;
      ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
      dialog.setTitle("Module Selection");
      dialog.setMessage("Select a GWT module:");
      // set elements
      try {
        List<ModuleDescription> modules = Utils.getModules(javaProject);
        dialog.setElements(modules.toArray());
      } catch (Throwable e) {
        DesignerPlugin.log(e);
      }
      // open dialog and return result
      if (dialog.open() == Window.OK) {
        ModuleDescription module = (ModuleDescription) dialog.getFirstResult();
        String moduleId = module.getId();
        m_moduleField.setText(moduleId);
      }
    }

    /**
     * @return current module name.
     */
    private String getModuleName() {
      return m_moduleField.getText();
    }

    /**
     * @return current module file.
     */
    private ModuleDescription getModule() throws Exception {
      return Utils.getModule(getProject(), getModuleName());
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Module HTML
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Choose a module HTML using dialog.
     */
    private void chooseModuleHtml() {
      try {
        ensureCreateClassLoader();
        HtmlSelectionDialog dialog =
            new HtmlSelectionDialog(getShell(),
                m_resourcesProvider,
                getModule(),
                "Select module HTML file");
        if (dialog.open() == IDialogConstants.OK_ID) {
          m_moduleHtmlField.setText(dialog.getSelectedResourcePath());
        }
      } catch (Throwable e) {
        DesignerPlugin.log(e);
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // ClassLoader
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Check dispose GWT class loader.
     */
    public void clearClassLoader() {
      // check clear
      if (m_resourcesProvider != null) {
        m_resourcesProvider.dispose();
        m_resourcesProvider = null;
      }
    }

    /**
     * Check create GWT class loader.
     */
    private void ensureCreateClassLoader() {
      // check java project
      if (m_currentProject == null) {
        m_currentProject = getProject();
      }
      // check class loader
      if (m_resourcesProvider == null) {
        ExecutionUtils.runLog(new RunnableEx() {
          public void run() throws Exception {
            ModuleDescription module = getModule();
            m_resourcesProvider = module.getResourcesProvider();
          }
        });
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Configuration
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Initializes this composite from given config.
     */
    public void initializeFrom(ILaunchConfiguration config) {
      try {
        m_projectField.setTextWithoutUpdate(config.getAttribute(
            IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
            ""));
        m_moduleField.setTextWithoutUpdate(config.getAttribute(Constants.LAUNCH_ATTR_MODULE, ""));
        m_moduleHtmlField.setTextWithoutUpdate(config.getAttribute(
            Constants.LAUNCH_ATTR_MODULE_HTML,
            ""));
        m_parametersField.setTextWithoutUpdate(config.getAttribute(
            Constants.LAUNCH_ATTR_PARAMETERS,
            ""));
        // HostedMode flags
        m_urlField.setTextWithoutUpdate(config.getAttribute(Constants.LAUNCH_ATTR_URL, ""));
        m_portField.setTextWithoutUpdate(config.getAttribute(Constants.LAUNCH_ATTR_PORT, "8888"));
        m_noServerField.setSelectionWithoutUpdate(config.getAttribute(
            Constants.LAUNCH_ATTR_NO_SERVER,
            false));
        m_whiteListField.setTextWithoutUpdate(config.getAttribute(
            Constants.LAUNCH_ATTR_WHITE_LIST,
            ""));
        m_blackListField.setTextWithoutUpdate(config.getAttribute(
            Constants.LAUNCH_ATTR_BLACK_LIST,
            ""));
        m_logLevelField.setTextWithoutUpdate(config.getAttribute(
            Constants.LAUNCH_ATTR_LOG_LEVEL,
            ""));
        m_dirGenField.setTextWithoutUpdate(config.getAttribute(Constants.LAUNCH_ATTR_DIR_GEN, ""));
        m_dirWarField.setTextWithoutUpdate(config.getAttribute(Constants.LAUNCH_ATTR_DIR_WAR, ""));
        //
        validateAll();
      } catch (Throwable e) {
        DesignerPlugin.log(e);
      }
    }

    /**
     * Updates given config using values entered in this composite.
     */
    public void applyTo(ILaunchConfigurationWorkingCopy config) {
      mapResources(config);
      config.setAttribute(
          IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
          m_projectField.getText());
      config.setAttribute(Constants.LAUNCH_ATTR_MODULE, m_moduleField.getText());
      config.setAttribute(Constants.LAUNCH_ATTR_MODULE_HTML, m_moduleHtmlField.getText());
      config.setAttribute(Constants.LAUNCH_ATTR_PARAMETERS, m_parametersField.getText());
      config.setAttribute(Constants.LAUNCH_ATTR_URL, m_urlField.getText());
      config.setAttribute(Constants.LAUNCH_ATTR_PORT, m_portField.getText());
      config.setAttribute(Constants.LAUNCH_ATTR_NO_SERVER, m_noServerField.getSelection());
      config.setAttribute(Constants.LAUNCH_ATTR_WHITE_LIST, m_whiteListField.getText());
      config.setAttribute(Constants.LAUNCH_ATTR_BLACK_LIST, m_blackListField.getText());
      config.setAttribute(Constants.LAUNCH_ATTR_LOG_LEVEL, m_logLevelField.getText());
      config.setAttribute(Constants.LAUNCH_ATTR_DIR_GEN, m_dirGenField.getText());
      config.setAttribute(Constants.LAUNCH_ATTR_DIR_WAR, m_dirWarField.getText());
    }

    /**
     * Maps the config to associated java project. So, user will be asked about deleting launch
     * configuration during project delete.
     */
    private void mapResources(ILaunchConfigurationWorkingCopy config) {
      IJavaProject javaProject = getProject();
      IResource[] resources = null;
      if (javaProject != null) {
        resources = new IResource[]{javaProject.getProject()};
      }
      config.setMappedResources(resources);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getName() {
    return "Main";
  }

  @Override
  public Image getImage() {
    return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Buttons
  //
  ////////////////////////////////////////////////////////////////////////////
  public void performApply(ILaunchConfigurationWorkingCopy config) {
    m_composite.applyTo(config);
  }

  public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    config.setAttribute(Constants.LAUNCH_ATTR_PORT, "8888");
    config.setAttribute(Constants.LAUNCH_ATTR_NO_SERVER, false);
    config.setAttribute(Constants.LAUNCH_ATTR_WHITE_LIST, "");
    config.setAttribute(Constants.LAUNCH_ATTR_BLACK_LIST, "");
    config.setAttribute(Constants.LAUNCH_ATTR_LOG_LEVEL, "ERROR");
    config.setAttribute(Constants.LAUNCH_ATTR_DIR_GEN, "");
    config.setAttribute(Constants.LAUNCH_ATTR_DIR_WAR, "www");
    config.setAttribute(Constants.LAUNCH_ATTR_STYLE, "OBFUSCATED");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Configuration
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void initializeFrom(ILaunchConfiguration config) {
    m_composite.initializeFrom(config);
    super.initializeFrom(config);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dispose
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void dispose() {
    if (m_composite != null) {
      m_composite.clearClassLoader();
    }
    super.dispose();
  }
}