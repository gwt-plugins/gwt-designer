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
package com.google.gdt.eclipse.designer.model.module;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import org.apache.tools.ant.types.ZipScanner;

import java.util.List;

/**
 * @author scheglov_ke
 * @coverage gwt.model.module
 */
public class ModuleElement extends AbstractModuleElement {
  private String m_id;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ModuleElement() {
    super("module");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setId(String id) {
    m_id = id;
  }

  public String getId() {
    return m_id;
  }

  public String getName() {
    String renameTo = getRenameTo();
    return renameTo != null ? renameTo : m_id;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // rename-to
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setRenameTo(String renameTo) {
    setAttribute("rename-to", renameTo);
  }

  public String getRenameTo() {
    return getAttribute("rename-to");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Typed children
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<EntryPointElement> getEntryPointElements() {
    return getChildren(EntryPointElement.class);
  }

  public List<InheritsElement> getInheritsElements() {
    return getChildren(InheritsElement.class);
  }

  public InheritsElement getInheritsElement(String name) {
    List<InheritsElement> inheritsElements = getInheritsElements();
    for (InheritsElement inheritsElement : inheritsElements) {
      if (inheritsElement.getName().equals(name)) {
        return inheritsElement;
      }
    }
    return null;
  }

  public List<PublicElement> getPublicElements() {
    return getChildren(PublicElement.class);
  }

  public List<ScriptElement> getScriptElements() {
    return getChildren(ScriptElement.class);
  }

  public List<ServletElement> getServletElements() {
    return getChildren(ServletElement.class);
  }

  public List<SourceElement> getSourceElements() {
    return getChildren(SourceElement.class);
  }

  public List<StylesheetElement> getStylesheetElements() {
    return getChildren(StylesheetElement.class);
  }

  public List<ExtendPropertyElement> getExtendPropertyElements() {
    return getChildren(ExtendPropertyElement.class);
  }

  public List<SetPropertyFallbackElement> getSetPropertyFallbackElements() {
    return getChildren(SetPropertyFallbackElement.class);
  }

  public List<SuperSourceElement> getSuperSourceElements() {
    return getChildren(SuperSourceElement.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // More access
  //
  ////////////////////////////////////////////////////////////////////////////
  private Predicate<String> m_sourceFolderPredicate;

  /**
   * This method is invoked when loading of module is finished and should perform final
   * initialization steps.
   */
  public void finalizeLoading() throws Exception {
    prepareSourceFolderPredicate();
  }

  private void prepareSourceFolderPredicate() {
    List<String> includes = Lists.newArrayList();
    List<String> excludes = Lists.newArrayList();
    // add "super source"
    for (SuperSourceElement sourceElement : getSuperSourceElements()) {
      includes.add(sourceElement.getPath() + "/**");
    }
    // add "source"
    for (SourceElement sourceElement : getSourceElements()) {
      includes.add(sourceElement.getPath() + "/**");
      for (ExcludeElement excludeElement : sourceElement.getExcludeElements()) {
        excludes.add(excludeElement.getName());
      }
    }
    // if no other "source", use default "client"
    if (includes.isEmpty()) {
      includes.add("client/**");
    }
    // final result
    final ZipScanner scanner = new ZipScanner();
    scanner.setIncludes(includes.toArray(new String[includes.size()]));
    scanner.setExcludes(excludes.toArray(new String[excludes.size()]));
    scanner.init();
    m_sourceFolderPredicate = new Predicate<String>() {
      public boolean apply(String t) {
        return scanner.match(t);
      }
    };
    //return folders;
  }

  /**
   * @return <code>true</code> if given folder is included into source folders and not excluded.
   */
  public boolean isInSourceFolder(String name) {
    return m_sourceFolderPredicate.apply(name);
  }

  //private static Predicate<String> createSourceFolderPredicate(SuperSourceElement sourceElement) {
  //}
  /**
   * @return all source folders of this module.
   */
  public List<String> getSourceFolders() {
    List<String> folders = Lists.newArrayList();
    // add "super source"
    for (SuperSourceElement sourceElement : getSuperSourceElements()) {
      folders.add(sourceElement.getPath());
    }
    // add "source"
    for (SourceElement sourceElement : getSourceElements()) {
      folders.add(sourceElement.getPath());
    }
    // if no other "source", use default "client"
    if (folders.isEmpty()) {
      folders.add("client");
    }
    // final result
    return folders;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new {@link InheritsElement}.
   */
  public void addInheritsElement(String name) {
    InheritsElement element = new InheritsElement();
    // prepare "index"
    int index;
    {
      List<InheritsElement> inheritsElements = getInheritsElements();
      index = inheritsElements.size();
      if (index != 0) {
        index = getChildren().indexOf(inheritsElements.get(index - 1)) + 1;
      }
    }
    // do add
    addChild(element, index);
    element.setName(name);
  }

  /**
   * Adds new {@link StylesheetElement}.
   */
  public void addStylesheetElement(String src) {
    StylesheetElement element = new StylesheetElement();
    addChild(element);
    element.setSrc(src);
  }

  /**
   * Adds new {@link ScriptElement}.
   */
  public void addScriptElement(String src) {
    ScriptElement element = new ScriptElement();
    addChild(element);
    element.setSrc(src);
  }
}
