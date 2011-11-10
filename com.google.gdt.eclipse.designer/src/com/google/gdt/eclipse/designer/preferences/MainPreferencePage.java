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
package com.google.gdt.eclipse.designer.preferences;

import com.google.gdt.eclipse.designer.Activator;
import com.google.gdt.eclipse.designer.ToolkitProvider;
import com.google.gdt.eclipse.designer.common.Constants;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.preferences.bind.AbstractBindingPreferencesPage;
import org.eclipse.wb.internal.core.utils.binding.DataBindManager;
import org.eclipse.wb.internal.core.utils.ui.AbstractBindingComposite;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import java.io.File;

/**
 * {@link PreferencePage} with general GWT preferences.
 * 
 * @author scheglov_ke
 * @coverage gwt.preferences.ui
 */
public final class MainPreferencePage extends AbstractBindingPreferencesPage {
  public static final String ID = "com.google.gdt.eclipse.designer.preferences.GWTPreferencePage";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MainPreferencePage() {
    super(ToolkitProvider.DESCRIPTION);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractBindingComposite createBindingComposite(Composite parent) {
    return new ContentsComposite(parent, m_bindManager, m_preferences);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Contents
  //
  ////////////////////////////////////////////////////////////////////////////
  private class ContentsComposite extends AbstractBindingComposite {
    private Text m_locationText;

    public ContentsComposite(Composite parent,
        DataBindManager bindManager,
        IPreferenceStore preferences) {
      super(parent, bindManager, preferences);
      GridLayoutFactory.create(this).noMargins();
      // GWT_HOME
      {
        Group homeGroup = createGWTGroup(this);
        GridDataFactory.create(homeGroup).grabH().fill();
      }
      // Presentation
      {
        Group presentationGroup = createPresentationGroup();
        GridDataFactory.create(presentationGroup).grabH().fill();
      }
      // Other
      {
        Group otherGroup = createOtherGroup();
        GridDataFactory.create(otherGroup).grabH().fill();
      }
    }

    /**
     * Create {@link Group} for requesting presentation/GEF preferences.
     */
    private Group createPresentationGroup() {
      Group group = new Group(this, SWT.NONE);
      GridLayoutFactory.create(group).columns(2);
      group.setText("Presentation");
      // default size
      {
        {
          new Label(group, SWT.NONE).setText("Default form width:");
          Text text = new Text(group, SWT.BORDER | SWT.RIGHT);
          GridDataFactory.create(text).grabH().fillH();
          bindInteger(text, IPreferenceConstants.P_GENERAL_DEFAULT_TOP_WIDTH);
        }
        {
          new Label(group, SWT.NONE).setText("Default form height:");
          Text text = new Text(group, SWT.BORDER | SWT.RIGHT);
          GridDataFactory.create(text).grabH().fillH();
          bindInteger(text, IPreferenceConstants.P_GENERAL_DEFAULT_TOP_HEIGHT);
        }
      }
      // other, boolean preferences
      checkButton(
          group,
          2,
          "Highlight containers without borders",
          IPreferenceConstants.P_GENERAL_HIGHLIGHT_CONTAINERS);
      checkButton(
          group,
          2,
          "Show text in components tree",
          IPreferenceConstants.P_GENERAL_TEXT_SUFFIX);
      checkButton(
          group,
          2,
          "Show important properties dialog on component adding",
          IPreferenceConstants.P_GENERAL_IMPORTANT_PROPERTIES_AFTER_ADD);
      checkButton(
          group,
          2,
          "Automatically activate direct edit on component adding",
          IPreferenceConstants.P_GENERAL_DIRECT_EDIT_AFTER_ADD);
      if (!EnvironmentUtils.IS_MAC
          && !(EnvironmentUtils.IS_WINDOWS && EnvironmentUtils.IS_64BIT_OS)) {
        checkButton(
            group,
            2,
            "Use WebKit for rendering GWT UI (if available)",
            Constants.P_GWT_USE_WEBKIT);
      }
      return group;
    }

    /**
     * Create {@link Group} for requesting other preferences.
     */
    private Group createOtherGroup() {
      Group group = new Group(this, SWT.NONE);
      GridLayoutFactory.create(group).columns(2);
      group.setText("Other");
      {
        new Label(group, SWT.NONE).setText("Web folder name:");
        Text text = new Text(group, SWT.BORDER);
        GridDataFactory.create(text).grabH().fillH();
        bindString(text, Constants.P_WEB_FOLDER);
      }
      checkButton(group, 2, "Use names in CSS color editing", Constants.P_CSS_USE_NAMED_COLORS);
      {
        new Label(group, SWT.NONE).setText("Hosted mode initialize timeout (sec):");
        final Text text = new Text(group, SWT.BORDER);
        text.addListener(SWT.Modify, new Listener() {
          public void handleEvent(Event event) {
            String location = text.getText();
            String errorMessage = validateTimeout(location);
            setErrorMessage(errorMessage);
            setValid(errorMessage == null);
          }
        });
        GridDataFactory.create(text).grabH().fillH();
        bindInteger(text, Constants.P_GWT_HOSTED_INIT_TIME);
      }
      return group;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // GWT_HOME support
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Create {@link Group} for requesting <code>GWT_HOME</code> variable.
     */
    private Group createGWTGroup(Composite parent) {
      Group homeGroup = new Group(parent, SWT.NONE);
      GridLayoutFactory.create(homeGroup).columns(3);
      homeGroup.setText("GWT_HOME variable");
      // GWT_HOME field
      {
        new Label(homeGroup, SWT.NONE).setText("Path to GWT installation directory:");
        m_locationText = new Text(homeGroup, SWT.BORDER);
        GridDataFactory.create(m_locationText).grabH().fillH().hintHC(30);
        bindString(m_locationText, Constants.P_GWT_LOCATION);
        // Browse...
        Button button = new Button(homeGroup, SWT.PUSH);
        button.setText("&Browse...");
        button.addListener(SWT.Selection, new Listener() {
          public void handleEvent(Event event) {
            DirectoryDialog directoryDialog = new DirectoryDialog(getShell());
            directoryDialog.setMessage("Select a GWT installation directory:");
            directoryDialog.setFilterPath(m_locationText.getText());
            String path = directoryDialog.open();
            if (path != null) {
              m_locationText.setText(path);
            }
          }
        });
      }
      // create link to the "GWT download"
      {
        Link downloadLink = new Link(homeGroup, SWT.NONE);
        GridDataFactory.create(downloadLink).spanH(3);
        downloadLink.setText("You can download GWT here <a href=\""
            + Constants.GWT_DOWNLOAD_URL
            + "\">"
            + Constants.GWT_DOWNLOAD_URL
            + "</a>");
        downloadLink.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            Program.launch(Constants.GWT_DOWNLOAD_URL);
          }
        });
      }
      //
      return homeGroup;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Validation
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected String validate() throws Exception {
      if (!Utils.hasGPE()) {
        String location = m_locationText.getText();
        String errorMessage = validateLocation(location);
        if (errorMessage != null) {
          return errorMessage;
        }
      }
      // OK
      return null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Validates that GWT location in preferences is valid.
   */
  public static boolean validateLocation() {
    String location = Activator.getGWTLocation();
    return validateLocation(location) == null;
  }

  /**
   * Validate that given location is valid GWT installation directory.
   * 
   * @return <code>null</code> if success or some error message in case of some problem.
   */
  private static String validateLocation(String location) {
    // check that location is not empty
    if (location.length() == 0) {
      return "Configure GWT location.";
    }
    // check that location exists
    File locationFile = new File(location);
    if (!locationFile.exists()) {
      return "Path '" + location + "' does not exist.";
    }
    // check that location is directory
    if (!locationFile.isDirectory()) {
      return "Path '" + location + "' is not directory.";
    }
    // check location contains gwt-user.jar
    File userJarFile = new File(locationFile, "gwt-user.jar");
    if (!userJarFile.exists()) {
      return "Path '" + location + "' does not contain gwt-user.jar";
    }
    // OK, this is good location
    return null;
  }

  /**
   * Validates GWT hosted mode initialization timeout.
   * 
   * @return <code>null</code> if success or some error message in case of some problem.
   */
  private static String validateTimeout(String text) {
    try {
      int timeout = Integer.parseInt(text);
      if (timeout < 30 && timeout != 0) {
        return "Timeout value should be greater than 30 sec. Set zero to disable timeout (not recommended).";
      }
    } catch (NumberFormatException e) {
      return "Invalid timeout value";
    }
    return null;
  }
}
