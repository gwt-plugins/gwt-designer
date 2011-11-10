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
package com.google.gdt.eclipse.designer.gxt.databinding.wizards.autobindings;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gdt.eclipse.designer.ToolkitProvider;
import com.google.gdt.eclipse.designer.gxt.databinding.Activator;
import com.google.gdt.eclipse.designer.gxt.databinding.model.beans.BeanPropertyObserveInfo;
import com.google.gdt.eclipse.designer.gxt.databinding.model.beans.BeanSupport;
import com.google.gdt.eclipse.designer.gxt.databinding.ui.providers.PropertyAdapterLabelProvider;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.ui.editor.ICompleteListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.databinding.wizards.autobindings.AutomaticDatabindingFirstPage;
import org.eclipse.wb.internal.core.databinding.wizards.autobindings.DefaultAutomaticDatabindingProvider;
import org.eclipse.wb.internal.core.databinding.wizards.autobindings.DescriptorContainer;
import org.eclipse.wb.internal.core.databinding.wizards.autobindings.IImageLoader;
import org.eclipse.wb.internal.core.model.variable.FieldUniqueVariableSupport;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage.ImportsManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author lobas_av
 * 
 */
public class GxtDatabindingProvider extends DefaultAutomaticDatabindingProvider {
  private static DescriptorContainer m_widgetContainer;
  private AutomaticDatabindingFirstPage m_firstPage;
  private String m_packageName;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public static GxtDatabindingProvider create() {
    try {
      // check containers
      if (m_widgetContainer == null) {
        // load containers
        InputStream stream = Activator.getFile("templates/GXTEditors.xml");
        Map<String, DescriptorContainer> containers =
            DescriptorContainer.parseDescriptors(
                stream,
                GxtDatabindingProvider.class.getClassLoader(),
                new IImageLoader() {
                  public Image getImage(String name) {
                    return Activator.getImage(name);
                  }
                });
        IOUtils.closeQuietly(stream);
        // sets containers
        m_widgetContainer = containers.get("GXT.Widgets");
      }
      // create provider
      return new GxtDatabindingProvider(m_widgetContainer);
    } catch (Throwable e) {
      DesignerPlugin.log(e);
      return null;
    }
  }

  private GxtDatabindingProvider(DescriptorContainer widgetContainer) {
    super(widgetContainer, null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SuperClass
  //
  ////////////////////////////////////////////////////////////////////////////
  public String[] getSuperClasses() {
    return new String[]{
        "com.extjs.gxt.ui.client.widget.Composite",
        "com.extjs.gxt.ui.client.widget.Dialog",
        "com.extjs.gxt.ui.client.widget.LayoutContainer"};
  }

  public String getInitialSuperClass() {
    return "com.extjs.gxt.ui.client.widget.Composite";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WizardPage
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setCurrentWizardData(AutomaticDatabindingFirstPage firstPage,
      ICompleteListener pageListener) {
    super.setCurrentWizardData(firstPage, pageListener);
    m_firstPage = firstPage;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  public void configure(ChooseClassAndPropertiesConfiguration configuration) {
    configuration.setBaseClassName("com.extjs.gxt.ui.client.data.ModelData");
    configuration.setPropertiesLabelProvider(new PropertyAdapterLabelProvider());
    configuration.setValueScope("beans");
  }

  @Override
  public boolean getPropertiesViewerFilterInitState() {
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<PropertyAdapter> getProperties0(Class<?> choosenClass) throws Exception {
    List<PropertyAdapter> properties = Lists.newArrayList();
    BeanSupport beanSupport = new BeanSupport(m_classLoader, null, m_javaProject);
    //
    for (BeanPropertyObserveInfo observe : beanSupport.getProperties(choosenClass, null, false)) {
      properties.add(new PropertyAdapter(observe.getPresentation().getText(),
          observe.getObjectType()));
    }
    //
    return properties;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Finish
  //
  ////////////////////////////////////////////////////////////////////////////
  public InputStream getTemplateFile(String superClassName) {
    return Activator.getFile("templates/" + ClassUtils.getShortClassName(superClassName) + ".jvt");
  }

  public String performSubstitutions(String code, ImportsManager imports) throws Exception {
    // bean class, field, name, field access
    String beanClassName = m_beanClass.getName().replace('$', '.');
    String beanClassShortName = ClassUtils.getShortClassName(beanClassName);
    String fieldPrefix = JavaCore.getOption(JavaCore.CODEASSIST_FIELD_PREFIXES);
    String fieldName = fieldPrefix + StringUtils.uncapitalize(beanClassShortName);
    //
    Collection<String> importList = Sets.newHashSet();
    //
    final List<PropertyAdapter> properties = Lists.newArrayList();
    Display.getDefault().syncExec(new Runnable() {
      public void run() {
        m_packageName = m_firstPage.getPackageFragment().getElementName();
        CollectionUtils.addAll(properties, m_propertiesViewer.getCheckedElements());
      }
    });
    //
    if (!ClassUtils.getPackageName(beanClassName).equals(m_packageName)) {
      importList.add(beanClassName);
    }
    beanClassName = beanClassShortName;
    //
    code = StringUtils.replace(code, "%BeanClass%", beanClassName);
    //
    if (ReflectionUtils.getConstructorBySignature(m_beanClass, "<init>()") == null) {
      code = StringUtils.replace(code, "%BeanField%", fieldName);
    } else {
      code = StringUtils.replace(code, "%BeanField%", fieldName + " = new " + beanClassName + "()");
    }
    //
    IPreferenceStore preferences = ToolkitProvider.DESCRIPTION.getPreferences();
    String accessPrefix =
        preferences.getBoolean(FieldUniqueVariableSupport.P_PREFIX_THIS) ? "this." : "";
    String beanFieldAccess = accessPrefix + fieldName;
    //
    code = StringUtils.replace(code, "%BeanFieldAccess%", beanFieldAccess);
    code = StringUtils.replace(code, "%BeanName%", StringUtils.capitalize(beanClassShortName));
    //
    boolean useGenerics = CoreUtils.useGenerics(m_javaProject);
    //
    StringBuffer fieldsCode = new StringBuffer();
    StringBuffer widgetsCode = new StringBuffer();
    StringBuffer bindingsCode = new StringBuffer();
    //
    for (Iterator<PropertyAdapter> I = properties.iterator(); I.hasNext();) {
      PropertyAdapter property = I.next();
      Object[] editorData = m_propertyToEditor.get(property);
      GxtWidgetDescriptor widgetDescriptor = (GxtWidgetDescriptor) editorData[0];
      //
      String propertyName = property.getName();
      String widgetClassName = ClassUtils.getShortClassName(widgetDescriptor.getWidgetClass());
      String widgetFieldName = fieldPrefix + propertyName + widgetClassName;
      String widgetFieldAccess = accessPrefix + widgetFieldName;
      //
      if (useGenerics && widgetDescriptor.isGeneric()) {
        widgetClassName += "<" + convertTypes(property.getType().getName()) + ">";
      }
      //
      fieldsCode.append("\r\nfield\r\n\tprivate " + widgetClassName + " " + widgetFieldName + ";");
      //
      widgetsCode.append("\t\t" + widgetFieldName + " = new " + widgetClassName + "();\r\n");
      widgetsCode.append("\t\t"
          + widgetFieldAccess
          + ".setFieldLabel(\""
          + StringUtils.capitalize(propertyName)
          + "\");\r\n");
      widgetsCode.append("\t\t"
          + accessPrefix
          + "m_formPanel.add("
          + widgetFieldAccess
          + ", new FormData(\"100%\"));\r\n");
      widgetsCode.append("\t\t//");
      //
      importList.add(widgetDescriptor.getBindingClass());
      bindingsCode.append("\t\tm_formBinding.addFieldBinding(new "
          + ClassUtils.getShortClassName(widgetDescriptor.getBindingClass())
          + "("
          + widgetFieldAccess
          + ",\""
          + propertyName
          + "\"));\r\n");
      //
      importList.add(widgetDescriptor.getWidgetClass());
      //
      if (I.hasNext()) {
        fieldsCode.append("\r\n");
        widgetsCode.append("\r\n");
      }
    }
    //
    bindingsCode.append("\t\t//\r\n");
    bindingsCode.append("\t\tm_formBinding.bind(" + beanFieldAccess + ");");
    // replace template patterns
    code = StringUtils.replace(code, "%WidgetFields%", fieldsCode.toString());
    code = StringUtils.replace(code, "%Widgets%", widgetsCode.toString());
    code = StringUtils.replace(code, "%Bindings%", bindingsCode.toString());
    // add imports
    for (String qualifiedTypeName : importList) {
      imports.addImport(qualifiedTypeName);
    }
    //
    return code;
  }

  private static String convertTypes(String className) {
    if (ArrayUtils.contains(
        new String[]{"boolean", "byte", "short", "long", "float", "double"},
        className)) {
      return StringUtils.capitalize(className);
    } else if ("char".equals(className)) {
      return "Character";
    } else if ("int".equals(className)) {
      return "Integer";
    } else if ("java.lang.String".equals(className)) {
      return "String";
    }
    return className;
  }
}