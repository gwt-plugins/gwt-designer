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
package com.google.gdt.eclipse.designer.wizards.model.mvp;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.util.Utils;
import com.google.gdt.eclipse.designer.wizards.Activator;
import com.google.gdt.eclipse.designer.wizards.model.common.AbstractGwtComposite;
import com.google.gdt.eclipse.designer.wizards.model.common.GwtProjectPackageRootFilter;
import com.google.gdt.eclipse.designer.wizards.model.common.IMessageContainer;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.CheckDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogFieldUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.IStringButtonAdapter;
import org.eclipse.wb.internal.core.utils.dialogfields.StatusUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.StringButtonDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.StringDialogField;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.jdt.ui.PackageRootSelectionDialogField;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Composite that ask user for parameters of new GWT MVP-view.
 * 
 * @author sablin_aa
 * @coverage gwt.wizard.ui
 */
public class ViewComposite extends AbstractGwtComposite {
  protected static final int COLUMNS = 3;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private IPackageFragment viewPackageFragment;
  private IPackageFragment placePackageFragment;
  private IPackageFragment activityPackageFragment;
  private PackageRootSelectionDialogField sourceField;
  private StringButtonDialogField packageField;
  private StringDialogField viewField;
  private Button useJavaRadio;
  private Button useUiBinderRadio;
  private CheckDialogField placeField;
  private StringDialogField placeNameField;
  private CheckDialogField activityField;
  private StringDialogField activityNameField;
  private CheckDialogField factoryField;
  private StringButtonDialogField factoryNameField;
  private Text descriptionText;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ViewComposite(Composite parent,
      int style,
      IMessageContainer messageContainer,
      IPackageFragment initialPackageFragment) {
    super(parent, style, messageContainer);
    setLayout(new GridLayout(COLUMNS, false));
    createControls();
    initializeControls(initialPackageFragment);
  }

  protected void createControls() {
    // source folder
    {
      sourceField = PackageRootSelectionDialogField.create("Source folder:", "&Browse...");
      sourceField.setPackageRootFilter(new GwtProjectPackageRootFilter());
      sourceField.setUpdateListener(m_validateListener);
      DialogFieldUtils.fillControls(this, sourceField, COLUMNS, 60);
    }
    // package name
    {
      packageField = new StringButtonDialogField(new IStringButtonAdapter() {
        public void changeControlPressed(DialogField field) {
          packageButtonPressed();
        }
      });
      packageField.setDialogFieldListener(m_validateListener);
      packageField.setLabelText("&Package name:");
      packageField.setButtonLabel("&Browse...");
      DialogFieldUtils.fillControls(this, packageField, COLUMNS, 60);
    }
    // view name
    {
      viewField = new StringDialogField();
      viewField.setDialogFieldListener(m_validateListener);
      viewField.setLabelText("&View name:");
      DialogFieldUtils.fillControls(this, viewField, COLUMNS, 60);
    }
    // Java template (NOT UiBinder)
    {
      new Label(this, SWT.NONE);// filler
      Composite composite = new Composite(this, SWT.NONE);
      composite.setLayout(new GridLayout(2, true));
      useUiBinderRadio = new Button(composite, SWT.RADIO);
      useUiBinderRadio.setText("use UiBinder template");
      useJavaRadio = new Button(composite, SWT.RADIO);
      useJavaRadio.setText("use Java template");
      GridDataFactory.create(composite).alignHF().spanH(COLUMNS - 1);
    }
    // place name
    {
      placeField = new CheckDialogField();
      placeField.setDialogFieldListener(m_validateListener);
      placeField.setLabelText("create Place name:");
      placeField.doFillIntoGrid(this, 1);
      //
      placeNameField = new StringDialogField();
      placeNameField.setDialogFieldListener(m_validateListener);
      GridDataFactory.create(placeNameField.getTextControl(this)).alignHF().spanH(COLUMNS - 1);
    }
    // activity name
    {
      activityField = new CheckDialogField();
      activityField.setDialogFieldListener(m_validateListener);
      activityField.setLabelText("create Activity name:");
      activityField.doFillIntoGrid(this, 1);
      //
      activityNameField = new StringDialogField();
      activityNameField.setDialogFieldListener(m_validateListener);
      GridDataFactory.create(activityNameField.getTextControl(this)).alignHF().spanH(COLUMNS - 1);
    }
    // factory name
    {
      factoryField = new CheckDialogField();
      factoryField.setDialogFieldListener(m_validateListener);
      factoryField.setLabelText("use ClientFactory name:");
      factoryField.doFillIntoGrid(this, 1);
      //
      factoryNameField = new StringButtonDialogField(new IStringButtonAdapter() {
        public void changeControlPressed(DialogField field) {
          factoryButtonPressed();
        }
      });
      factoryNameField.setDialogFieldListener(m_validateListener);
      factoryNameField.setButtonLabel("&Browse...");
      GridDataFactory.create(factoryNameField.getTextControl(this)).alignHF().spanH(COLUMNS - 2);
      GridDataFactory.create(factoryNameField.getChangeControl(this)).fillH();
    }
    // description
    {
      descriptionText = new Text(this, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY);
      GridDataFactory.create(descriptionText).spanH(COLUMNS).grab().fill().hintHC(60).hintVC(4);
    }
  }

  private String oldViewName;

  protected void initializeControls(IPackageFragment initialPackageFragment) {
    oldViewName = "New";
    // initialize fields
    IPackageFragmentRoot packageRoot = null;
    String packageName = null;
    if (initialPackageFragment != null) {
      try {
        packageRoot = CodeUtils.getPackageFragmentRoot(initialPackageFragment);
        sourceField.setRootWithoutUpdate(packageRoot);
      } catch (JavaModelException e) {
        DesignerPlugin.log(e);
      }
      packageName = initialPackageFragment.getElementName();
      packageField.setTextWithoutUpdate(packageName);
    }
    viewField.setTextWithoutUpdate(oldViewName + "View");
    useUiBinderRadio.setSelection(true);
    placeField.setSelectionWithoutUpdate(true);
    placeNameField.setTextWithoutUpdate(oldViewName + "Place");
    activityField.setSelectionWithoutUpdate(true);
    activityNameField.setTextWithoutUpdate(oldViewName + "Activity");
    // locate ClientFactory
    factoryField.setSelectionWithoutUpdate(false);
    if (packageRoot != null) {
      try {
        String clientFactoryClassName = getClientFactoryClassName(packageRoot, packageName);
        if (!StringUtils.isEmpty(clientFactoryClassName)) {
          factoryNameField.setTextWithoutUpdate(clientFactoryClassName);
          factoryField.setSelectionWithoutUpdate(true);
        }
      } catch (Exception e) {
        DesignerPlugin.log(e);
      }
    }
    // ready for validate
    validateAll();
  }

  private void packageButtonPressed() {
    IPackageFragmentRoot packageRoot = getRoot();
    if (packageRoot == null) {
      return;
    }
    List<IPackageFragment> packages = Lists.newArrayList();
    try {
      IJavaElement[] javaElements = packageRoot.getChildren();
      for (IJavaElement javaElement : javaElements) {
        if (javaElement instanceof IPackageFragment) {
          IPackageFragment packageFragment = (IPackageFragment) javaElement;
          if (Utils.isModuleSourcePackage(packageFragment)) {
            packages.add(packageFragment);
          }
        }
      }
    } catch (Exception e) {
      DesignerPlugin.log(e);
    }
    ElementListSelectionDialog dialog =
        new ElementListSelectionDialog(getShell(),
            new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT));
    dialog.setIgnoreCase(false);
    dialog.setTitle("Package selection");
    dialog.setMessage("Select package for View:");
    dialog.setHelpAvailable(false);
    dialog.setElements(packages.toArray());
    {
      IPackageFragment packageFragment = packageRoot.getPackageFragment(packageField.getText());
      if (packageFragment != null && packageFragment.exists()) {
        dialog.setInitialSelections(new Object[]{packageFragment});
      }
    }
    if (dialog.open() == Window.OK) {
      Object element = dialog.getFirstResult();
      if (element instanceof IPackageFragment) {
        IPackageFragment packageFragment = (IPackageFragment) element;
        packageField.setTextWithoutUpdate(packageFragment.getElementName());
      }
    }
  }

  private void factoryButtonPressed() {
    final IPackageFragmentRoot packageRoot = getRoot();
    if (packageRoot == null) {
      return;
    }
    Shell shell = Display.getCurrent().getActiveShell();
    ILabelProvider labelProvider =
        new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);
    ITreeContentProvider contentProvider = new StandardJavaElementContentProvider();
    ElementTreeSelectionDialog dialog =
        new ElementTreeSelectionDialog(shell, labelProvider, contentProvider);
    dialog.setTitle("ClientFactory interface selection");
    dialog.setMessage("Choose a ClientFactory interface:");
    dialog.setAllowMultiple(false);
    dialog.setHelpAvailable(false);
    //
    dialog.setValidator(new ISelectionStatusValidator() {
      public IStatus validate(Object[] selection) {
        if (selection.length == 1) {
          if (selection[0] instanceof ICompilationUnit) {
            try {
              if (validateFactoryCompilationUnit((ICompilationUnit) selection[0]) == null) {
                return StatusUtils.OK_STATUS;
              }
            } catch (Exception e) {
              DesignerPlugin.log(e);
            }
          }
        }
        return StatusUtils.ERROR_STATUS;
      }
    });
    dialog.addFilter(new ViewerFilter() {
      @Override
      public boolean select(Viewer viewer, Object parentElement, Object element) {
        // check project
        if (element instanceof IJavaProject) {
          return element.equals(packageRoot.getJavaProject());
        }
        // check package fragment root
        if (element instanceof IPackageFragmentRoot) {
          return element.equals(packageRoot);
        }
        // check package fragment
        if (element instanceof IPackageFragment) {
          try {
            IPackageFragment packageFragment = (IPackageFragment) element;
            return packageFragment.getCompilationUnits().length > 0
                && Utils.isModuleSourcePackage(packageFragment);
          } catch (Exception e) {
            DesignerPlugin.log(e);
            return false;
          }
        }
        // check *.java
        if (element instanceof ICompilationUnit) {
          return true;
        }
        return false;
      }
    });
    dialog.setInput(packageRoot);
    {
      String factoryClassName = factoryNameField.getText();
      if (!StringUtils.isEmpty(factoryClassName)) {
        IPackageFragment packageFragment =
            packageRoot.getPackageFragment(CodeUtils.getPackage(factoryClassName));
        if (packageFragment.exists()) {
          ICompilationUnit compilationUnit =
              packageFragment.getCompilationUnit(CodeUtils.getShortClass(factoryClassName)
                  + ".java");
          if (compilationUnit.exists()) {
            dialog.setInitialSelection(compilationUnit);
          }
        }
      }
    }
    if (dialog.open() == Window.OK) {
      Object element = dialog.getFirstResult();
      if (element instanceof ICompilationUnit) {
        ICompilationUnit unit = (ICompilationUnit) element;
        factoryNameField.setText(unit.findPrimaryType().getFullyQualifiedName());
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String validate() {
    // reset values state
    viewPackageFragment = null;
    placePackageFragment = null;
    activityPackageFragment = null;
    descriptionText.setText("");
    // process names fields
    validateNames();
    // validate values ...
    IPackageFragmentRoot packageRoot = null;
    // validate source folder
    {
      if (sourceField.getText().length() == 0) {
        return "Source folder name can not be empty.";
      }
      packageRoot = sourceField.getRoot();
      if (packageRoot == null) {
        return "Source folder is invalid.";
      } else {
        // validate project
        if (!Utils.supportMvp(packageRoot.getJavaProject())) {
          return "Current project nor support MVP framework (need gwt-2.1 or higher).";
        }
      }
    }
    // validate package name
    String packageName;
    IPackageFragment basePkg;
    {
      packageName = packageField.getText();
      if (packageName.length() == 0) {
        return "Package can not be empty.";
      }
      // check that package name is valid
      IStatus status = JavaConventions.validatePackageName(packageName, null, null);
      if (status.getSeverity() == IStatus.ERROR) {
        return status.getMessage();
      }
      // check for client package
      basePkg = packageRoot.getPackageFragment(packageName);
      try {
        if (!Utils.isModuleSourcePackage(basePkg)) {
          return "GWT widgets can be used only in client package of some GWT module.";
        }
      } catch (Exception e) {
        return "Exception: " + e.getMessage();
      }
    }
    // try get special packages for Places & Activities
    IPackageFragment viewPkg = basePkg;
    IPackageFragment placePkg = basePkg;
    IPackageFragment activityPkg = basePkg;
    {
      try {
        IPackageFragment rootSourcePackage = Utils.getRootSourcePackage(basePkg);
        List<String> packageNameTemplates =
            generatePackageNameTemplates(packageName, rootSourcePackage.getElementName());
        // select 'place' package
        {
          IPackageFragment existsPackageFragment =
              selectExistsPackage(packageRoot, packageNameTemplates, "place", "places");
          if (existsPackageFragment != null) {
            placePkg = existsPackageFragment;
          }
        }
        // select 'activity' package
        {
          IPackageFragment existsPackageFragment =
              selectExistsPackage(packageRoot, packageNameTemplates, "activity", "activities");
          if (existsPackageFragment != null) {
            activityPkg = existsPackageFragment;
          }
        }
      } catch (Exception e) {
        DesignerPlugin.log(e);
      }
    }
    // validate view name
    {
      String validateMessage =
          validateNewCompilationUnit(viewPkg, viewField.getText(), "View name");
      if (validateMessage != null) {
        if (viewPkg != basePkg) {
          validateMessage = validateNewCompilationUnit(basePkg, viewField.getText(), "View name");
          if (validateMessage != null) {
            return validateMessage;
          } else {
            viewPackageFragment = basePkg;
          }
        } else {
          return validateMessage;
        }
      } else {
        viewPackageFragment = viewPkg;
      }
    }
    // validate place name
    if (placeNameField.isEnabled()) {
      String validateMessage =
          validateNewCompilationUnit(placePkg, placeNameField.getText(), "Place name");
      if (validateMessage != null) {
        if (placePkg != basePkg) {
          validateMessage =
              validateNewCompilationUnit(basePkg, placeNameField.getText(), "Place name");
          if (validateMessage != null) {
            return validateMessage;
          } else {
            placePackageFragment = basePkg;
          }
        } else {
          return validateMessage;
        }
      } else {
        placePackageFragment = placePkg;
      }
    }
    // validate activity name
    if (activityNameField.isEnabled()) {
      String validateMessage =
          validateNewCompilationUnit(activityPkg, activityNameField.getText(), "Activity name");
      if (validateMessage != null) {
        if (activityPkg != basePkg) {
          validateMessage =
              validateNewCompilationUnit(basePkg, activityNameField.getText(), "Activity name");
          if (validateMessage != null) {
            return validateMessage;
          } else {
            activityPackageFragment = basePkg;
          }
        } else {
          return validateMessage;
        }
      } else {
        activityPackageFragment = activityPkg;
      }
    }
    // validate factory
    if (factoryNameField.isEnabled()) {
      String factoryFullName = factoryNameField.getText();
      if (factoryFullName.length() == 0) {
        return "ClientFactory name can not be empty.";
      }
      String factoryPackageName = CodeUtils.getPackage(factoryFullName);
      String factoryClassName = CodeUtils.getShortClass(factoryFullName);
      IPackageFragment packageFragment = packageRoot.getPackageFragment(factoryPackageName);
      ICompilationUnit compilationUnit =
          packageFragment.getCompilationUnit(factoryClassName + ".java");
      if (!compilationUnit.exists()) {
        return "ClientFactory unit " + factoryFullName + " does not exists.";
      }
      try {
        String validateMessage = validateFactoryCompilationUnit(compilationUnit);
        if (validateMessage != null) {
          return validateMessage;
        }
      } catch (Exception e) {
        DesignerPlugin.log(e);
      }
    }
    // description
    {
      StringBuffer message = new StringBuffer();
      message.append("Generating View class " + getViewPackageName() + "." + getViewName());
      if (placeNameField.isEnabled()) {
        message.append("\nGenerating Place class " + getPlacePackageName() + "." + getPlaceName());
      }
      if (activityNameField.isEnabled()) {
        message.append("\nGenerating Activity class "
            + getActivityPackageName()
            + "."
            + getActivityName());
      }
      if (factoryField.getSelection() && factoryField.isEnabled()) {
        message.append("\nModifying ClientFactory class "
            + getClientFactoryPackageName()
            + "."
            + getClientFactoryName());
      }
      descriptionText.setText(message.toString());
    }
    return null;
  }

  private void validateNames() {
    String newViewName = viewField.getText();
    // extract template
    {
      if (StringUtils.endsWith(newViewName, "View")) {
        newViewName = StringUtils.replace(newViewName, "View", "");
      } else if (StringUtils.endsWith(newViewName, "Composite")) {
        newViewName = StringUtils.replace(newViewName, "Composite", "");
      } else if (StringUtils.endsWith(newViewName, "Frame")) {
        newViewName = StringUtils.replace(newViewName, "Frame", "");
      }
    }
    if (StringUtils.isEmpty(oldViewName)) {
      placeNameField.setTextWithoutUpdate(newViewName + placeNameField.getText());
      activityNameField.setTextWithoutUpdate(newViewName + activityNameField.getText());
    } else {
      placeNameField.setTextWithoutUpdate(StringUtils.replaceOnce(
          placeNameField.getText(),
          oldViewName,
          newViewName));
      activityNameField.setTextWithoutUpdate(StringUtils.replaceOnce(
          activityNameField.getText(),
          oldViewName,
          newViewName));
    }
    oldViewName = newViewName;
    // enabled fields
    placeNameField.setEnabled(placeField.getSelection());
    activityNameField.setEnabled(activityField.getSelection());
    factoryField.setEnabled(activityField.getSelection());
    factoryNameField.setEnabled(factoryField.isEnabled() && factoryField.getSelection());
  }

  /**
   * Generate possible variants package name templates.
   */
  private static List<String> generatePackageNameTemplates(String packageName,
      String rootSourcePackageName) {
    // initial names as sub-packages 
    List<String> templatePackageNames = Lists.newArrayList(packageName + ".%keyName%");
    // generate names as 'view'-template
    {
      final String[] searchingViewsTemplates = new String[]{".view", ".views", ".ui", ".uis"};
      String subPath = StringUtils.replace(packageName, rootSourcePackageName, "");
      for (String searchingViewTemplate : searchingViewsTemplates) {
        if (StringUtils.indexOfIgnoreCase(subPath, searchingViewTemplate) != -1) {
          templatePackageNames.add(rootSourcePackageName
              + StringUtils.replaceOnce(subPath, searchingViewTemplate, ".%keyName%"));
        }
      }
    }
    // generate names in parent 
    {
      String parentPackageName = CodeUtils.getPackage(packageName);
      while (parentPackageName.length() >= rootSourcePackageName.length()) {
        templatePackageNames.add(parentPackageName + ".%keyName%");
        parentPackageName = CodeUtils.getPackage(parentPackageName);
      }
    }
    return templatePackageNames;
  }

  private static IPackageFragment selectExistsPackage(IPackageFragmentRoot root,
      List<String> packageNameTemplates,
      String... keyNames) {
    for (String packageNameTemplate : packageNameTemplates) {
      for (String keyName : keyNames) {
        String placePackageName = StringUtils.replace(packageNameTemplate, "%keyName%", keyName);
        IPackageFragment packageFragment = root.getPackageFragment(placePackageName);
        if (packageFragment.exists()) {
          return packageFragment;
        }
      }
    }
    return null;
  }

  /**
   * Validate ability to create class source file with specified name in specified package.
   * 
   * @return if all Ok then <code>null</code> else error message
   */
  private static String validateNewCompilationUnit(IPackageFragment packageFragment,
      String className,
      String messagePrefix) {
    if (className.length() == 0) {
      return messagePrefix + " can not be empty.";
    }
    // check that view name is valid identifier
    IStatus status = JavaConventions.validateIdentifier(className, null, null);
    if (status.getSeverity() == IStatus.ERROR) {
      return status.getMessage() + " for " + messagePrefix;
    }
    // check that view not exists
    if (packageFragment.exists()) {
      String classSourceName = className + ".java";
      ICompilationUnit compilationUnit = packageFragment.getCompilationUnit(classSourceName);
      if (compilationUnit.exists()) {
        return "Source file " + classSourceName + " already exists.";
      }
    }
    return null;
  }

  /**
   * Validate ability to use specified {@link ICompilationUnit} as ClientFactory.
   * 
   * @param compilationUnit
   */
  private static String validateFactoryCompilationUnit(ICompilationUnit compilationUnit)
      throws Exception {
    if (getFactoryPlaceController(compilationUnit) != null) {
      return null;
    }
    return "ClientFactory unit "
        + compilationUnit.findPrimaryType().getFullyQualifiedName()
        + " does not contains PlaceController getter.";
  }

  /**
   * Try to locate PlaceController getter in specified {@link ICompilationUnit}.
   * 
   * @return located PlaceController getter or <code>null</code> if non.
   */
  private static MethodDeclaration getFactoryPlaceController(ICompilationUnit compilationUnit)
      throws Exception {
    if (compilationUnit != null && compilationUnit.exists()) {
      AstEditor editor = new AstEditor(compilationUnit);
      TypeDeclaration typeDeclaration = editor.getPrimaryType();
      MethodDeclaration[] methodDeclarations = typeDeclaration.getMethods();
      for (MethodDeclaration methodDeclaration : methodDeclarations) {
        Type returnType = methodDeclaration.getReturnType2();
        if (returnType != null
            && methodDeclaration.parameters().size() == 0
            && AstNodeUtils.isSuccessorOf(returnType, "com.google.gwt.place.shared.PlaceController")) {
          return methodDeclaration;
        }
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public IPackageFragmentRoot getRoot() {
    return sourceField.getRoot();
  }

  public String getViewPackageName() {
    return viewPackageFragment == null
        ? packageField.getText()
        : viewPackageFragment.getElementName();
  }

  public String getViewName() {
    return viewField.getText();
  }

  public boolean isUseJavaTemplate() {
    return useJavaRadio.getSelection();
  }

  public String getPlacePackageName() {
    return placePackageFragment == null
        ? packageField.getText()
        : placePackageFragment.getElementName();
  }

  public String getPlaceName() {
    return placeField.getSelection() ? placeNameField.getText() : null;
  }

  public String getActivityPackageName() {
    return activityPackageFragment == null
        ? packageField.getText()
        : activityPackageFragment.getElementName();
  }

  public String getActivityName() {
    return activityField.getSelection() ? activityNameField.getText() : null;
  }

  public String getClientFactoryPackageName() {
    return factoryField.getSelection() && factoryField.isEnabled()
        ? CodeUtils.getPackage(factoryNameField.getText())
        : null;
  }

  public String getClientFactoryName() {
    return factoryField.getSelection() && factoryField.isEnabled()
        ? CodeUtils.getShortClass(factoryNameField.getText())
        : null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resource utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final QualifiedName KEY_CLIENT_FACTORY = new QualifiedName(Activator.PLUGIN_ID,
      "clientFactory");

  public static String getClientFactoryName(IPackageFragment packageFragment) throws Exception {
    return packageFragment.getResource().getPersistentProperty(KEY_CLIENT_FACTORY);
  }

  public static void setClientFactoryName(IPackageFragment packageFragment, String value)
      throws Exception {
    packageFragment.getResource().setPersistentProperty(KEY_CLIENT_FACTORY, value);
  }

  public static String getClientFactoryClassName(IPackageFragmentRoot packageRoot,
      String packageName) throws Exception {
    if (StringUtils.isEmpty(packageName)) {
      return null;
    }
    IPackageFragment packageFragment = packageRoot.getPackageFragment(packageName);
    if (packageFragment.exists()) {
      String factoryClassName = getClientFactoryName(packageFragment);
      if (!StringUtils.isEmpty(factoryClassName)) {
        IPackageFragment factoryPackageFragment =
            packageRoot.getPackageFragment(CodeUtils.getPackage(factoryClassName));
        if (factoryPackageFragment.exists()) {
          ICompilationUnit compilationUnit =
              factoryPackageFragment.getCompilationUnit(CodeUtils.getShortClass(factoryClassName)
                  + ".java");
          if (compilationUnit.exists() && validateFactoryCompilationUnit(compilationUnit) == null) {
            return factoryClassName;
          }
        }
      }
    }
    return getClientFactoryClassName(packageRoot, CodeUtils.getPackage(packageName));
  }
}