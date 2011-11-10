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
package com.google.gdt.eclipse.designer.actions.deploy;

import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.wizards.Activator;
import com.google.gdt.eclipse.designer.wizards.model.common.AbstractGwtComposite;
import com.google.gdt.eclipse.designer.wizards.model.common.IMessageContainer;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.dialogfields.ComboDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogFieldUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.IStringButtonAdapter;
import org.eclipse.wb.internal.core.utils.dialogfields.StringButtonDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.StringDialogField;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableTitleAreaDialog;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Dialog for selecting parameters of deployment.
 * 
 * @author scheglov_ke
 * @coverage gwt.deploy
 */
public class DeployDialog extends ResizableTitleAreaDialog {
  private static final String DEPLOYMENT_FILE_NAME = ".deployment";
  private static final String WAR_NAME_KEY = "war.name";
  private static final String SERVER_PATH_KEY = "server.path";
  private static final String COMPILER_STYLE_KEY = "compiler.style";
  private static final String COMPILER_MEMORY_MAX = "compiler.memory.max";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final ModuleDescription m_module;
  private final IFolder m_moduleFolder;
  private DeployComposite m_deployComposite;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DeployDialog(Shell parentShell, AbstractUIPlugin plugin, ModuleDescription module) {
    super(parentShell, plugin);
    m_module = module;
    m_moduleFolder = m_module.getModuleFolder();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    // create composite
    {
      m_deployComposite =
          new DeployComposite(area, SWT.NONE, IMessageContainer.Util.forTitleAreaDialog(this));
      GridDataFactory.create(m_deployComposite).grab().fill();
    }
    // configure dialog title area
    {
      setTitle("Deployment on application server");
      setMessage("Enter parameters of deployment.");
      setTitleImage(Activator.getImage("deployment/banner.gif"));
    }
    //
    return area;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog: shell
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Deployment");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog: buttons
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void okPressed() {
    try {
      m_deployComposite.saveProperties();
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
    super.okPressed();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getWarName() {
    return m_deployComposite.m_warNameField.getText();
  }

  public String getServerPath() {
    return m_deployComposite.m_serverPathField.getText().trim().replace('\\', '/');
  }

  public String getCompilerStyle() {
    return m_deployComposite.m_compilerStyleField.getText();
  }

  public String getCompilerMaxMemory() {
    return m_deployComposite.m_compilerMaxMemoryField.getText();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Composite
  //
  ////////////////////////////////////////////////////////////////////////////
  private class DeployComposite extends AbstractGwtComposite {
    private final StringDialogField m_warNameField;
    private final StringButtonDialogField m_serverPathField;
    private final ComboDialogField m_compilerStyleField;
    private final ComboDialogField m_compilerMaxMemoryField;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public DeployComposite(Composite parent, int style, IMessageContainer messageContainer) {
      super(parent, style, messageContainer);
      int columns = 3;
      //GridLayoutFactory.create(this).columns(columns);
      GridLayoutFactory.create(this);
      // location
      {
        Group locationGroup = new Group(this, SWT.NONE);
        GridDataFactory.create(locationGroup).grabH().fillH();
        GridLayoutFactory.create(locationGroup).columns(columns);
        locationGroup.setText("Location");
        // WAR name field
        {
          m_warNameField = new StringDialogField();
          m_warNameField.setDialogFieldListener(m_validateListener);
          m_warNameField.setLabelText("WAR file name:");
          //
          DialogFieldUtils.fillControls(locationGroup, m_warNameField, columns, 60);
        }
        // server path field
        {
          m_serverPathField = new StringButtonDialogField(new IStringButtonAdapter() {
            public void changeControlPressed(DialogField field) {
              DirectoryDialog directoryDialog = new DirectoryDialog(getShell());
              directoryDialog.setFilterPath(m_serverPathField.getText());
              String newPath = directoryDialog.open();
              if (newPath != null) {
                m_serverPathField.setText(newPath);
              }
            }
          });
          m_serverPathField.setDialogFieldListener(m_validateListener);
          m_serverPathField.setLabelText("Server path to copy WAR:");
          m_serverPathField.setButtonLabel("&Browse...");
          //
          DialogFieldUtils.fillControls(locationGroup, m_serverPathField, columns, 60);
        }
      }
      // compiler
      {
        Group compilerGroup = new Group(this, SWT.NONE);
        GridDataFactory.create(compilerGroup).grabH().fillH();
        GridLayoutFactory.create(compilerGroup).columns(columns);
        compilerGroup.setText("GWT compiler");
        // compiler style field
        {
          m_compilerStyleField = new ComboDialogField(SWT.READ_ONLY);
          m_compilerStyleField.setLabelText("Style:");
          m_compilerStyleField.setItems(new String[]{"OBFUSCATED", "PRETTY", "DETAILED"});
          m_compilerStyleField.setDialogFieldListener(m_validateListener);
          //
          DialogFieldUtils.fillControls(compilerGroup, m_compilerStyleField, columns, 60);
        }
        // maximum memory
        {
          m_compilerMaxMemoryField = new ComboDialogField(SWT.NONE);
          m_compilerMaxMemoryField.setLabelText("Maximum memory:");
          m_compilerMaxMemoryField.setItems(new String[]{"128m", "256m", "512m", "1024m"});
          m_compilerMaxMemoryField.setDialogFieldListener(m_validateListener);
          //
          DialogFieldUtils.fillControls(compilerGroup, m_compilerMaxMemoryField, columns, 60);
        }
      }
      // bind fields
      try {
        bind(m_warNameField, WAR_NAME_KEY, m_module.getSimpleName() + ".war");
        bind(m_serverPathField, SERVER_PATH_KEY, "");
        bind(m_compilerStyleField, COMPILER_STYLE_KEY, "OBFUSCATED");
        bind(m_compilerMaxMemoryField, COMPILER_MEMORY_MAX, "256m");
      } catch (Throwable e) {
        DesignerPlugin.log(e);
      }
      // set defaults for developers
      if (EnvironmentUtils.DEVELOPER_HOST) {
        m_serverPathField.setText("C:/Work/GWT/apache-tomcat-5.5.31/webapps");
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Binding
    //
    ////////////////////////////////////////////////////////////////////////////
    private Properties m_properties;
    private final Map<String, DialogField> m_keyToField = new TreeMap<String, DialogField>();

    /**
     * Binds given {@link DialogField} to the given property in deployment descriptor.
     */
    private void bind(DialogField dialogField, String key, String defaultValue) throws Exception {
      // remember binding
      m_keyToField.put(key, dialogField);
      // prepare properties
      if (m_properties == null) {
        m_properties = new Properties();
        try {
          IFile deploymentFile = m_moduleFolder.getFile(DEPLOYMENT_FILE_NAME);
          m_properties.load(deploymentFile.getContents(true));
        } catch (Throwable e) {
        }
      }
      // prepare value
      String value = m_properties.getProperty(key, defaultValue);
      if (dialogField instanceof StringDialogField) {
        StringDialogField stringDialogField = (StringDialogField) dialogField;
        stringDialogField.setTextWithoutUpdate(value);
      } else if (dialogField instanceof ComboDialogField) {
        ComboDialogField comboDialogField = (ComboDialogField) dialogField;
        comboDialogField.setTextWithoutUpdate(value);
      } else {
        throw new IllegalArgumentException("Unknown type of DialogField: " + dialogField);
      }
    }

    /**
     * Saves bound {@link DialogField} back to the deployment descriptor.
     */
    public void saveProperties() throws Exception {
      for (Entry<String, DialogField> entry : m_keyToField.entrySet()) {
        String key = entry.getKey();
        DialogField dialogField = entry.getValue();
        // prepare value
        String value;
        if (dialogField instanceof StringDialogField) {
          StringDialogField stringDialogField = (StringDialogField) dialogField;
          value = stringDialogField.getText();
        } else if (dialogField instanceof ComboDialogField) {
          ComboDialogField comboDialogField = (ComboDialogField) dialogField;
          value = comboDialogField.getText();
        } else {
          throw new IllegalArgumentException("Unknown type of DialogField: " + dialogField);
        }
        // put value
        m_properties.put(key, value);
      }
      // save properties into file
      {
        IFile deploymentFile = m_moduleFolder.getFile(DEPLOYMENT_FILE_NAME);
        IOUtils2.storeProperties(deploymentFile, m_properties);
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Validation
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected String validate() {
      // WAR name
      {
        String warName = m_warNameField.getText().trim();
        if (warName.length() == 0) {
          return "WAR name can not be empty";
        }
        if (!warName.toLowerCase().endsWith(".war")) {
          return "WAR name should end with '.war'";
        }
      }
      // server path
      {
        String serverPath = m_serverPathField.getText().trim();
        if (serverPath.length() == 0) {
          return "Server path can not be empty";
        }
        File serverPathFile = new File(serverPath);
        if (!serverPathFile.exists()) {
          return "Server path does not exist";
        }
        if (!serverPathFile.isDirectory()) {
          return "Server path should be directory";
        }
      }
      // compiler max memory
      {
        String compilerMaxMemory = m_compilerMaxMemoryField.getText();
        if (compilerMaxMemory.length() == 0) {
          return "Max memory field can not be empty.";
        }
      }
      //
      return super.validate();
    }
  }
}
