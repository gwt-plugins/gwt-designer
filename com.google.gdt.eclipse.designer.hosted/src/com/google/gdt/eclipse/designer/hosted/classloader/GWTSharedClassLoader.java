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
package com.google.gdt.eclipse.designer.hosted.classloader;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.hosted.IModuleDescription;

import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The {@link ClassLoader} for gwt-user classes and project classes.
 * 
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage gwtHosted
 */
public final class GWTSharedClassLoader extends URLClassLoader {
  private class State {
    IModuleDescription activeModule;
    ClassLoader activeLoader;
    Map<IModuleDescription, ClassLoader> loaders = Maps.newHashMap();
  }

  private final ThreadLocal<State> stateTL = new ThreadLocal<State>() {
    @Override
    protected State initialValue() {
      return new State();
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GWTSharedClassLoader(ClassLoader parent, URL[] urls) throws Exception {
    super(urls, parent);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds the {@link IModuleDescription} to this class loader and sets it as active to resources
   * from it. If the project has been already added just sets it as active.
   */
  public void setActiveProject(IModuleDescription module) throws Exception {
    State state = stateTL.get();
    if (state.loaders.get(module) == null) {
      state.loaders.put(module, module.getClassLoader());
    }
    state.activeModule = module;
    state.activeLoader = state.loaders.get(module);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Disposing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Frees any used resources.
   */
  public void dispose(IModuleDescription module) {
    State state = stateTL.get();
    state.loaders.remove(state.activeModule);
    if (state.activeModule == module) {
      state.activeModule = null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Loading
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public URL[] getURLs() {
    List<URL> urls = Lists.newArrayList();
    CollectionUtils.addAll(urls, super.getURLs());
    try {
      URL[] moduleURLs = stateTL.get().activeModule.getURLs();
      CollectionUtils.addAll(urls, moduleURLs);
    } catch (Throwable e) {
    }
    return urls.toArray(new URL[urls.size()]);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resources
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Enumeration<URL> findResources(String name) throws IOException {
    final Enumeration<URL> superResources = super.findResources(name);
    final Enumeration<URL> moduleResources = stateTL.get().activeLoader.getResources(name);
    Iterator<URL> allResources =
        Iterators.concat(
            Iterators.forEnumeration(superResources),
            Iterators.forEnumeration(moduleResources));
    return Iterators.asEnumeration(allResources);
  }

  @Override
  public URL findResource(String name) {
    URL url = super.findResource(name);
    if (url != null) {
      return url;
    }
    return stateTL.get().activeLoader.getResource(name);
  }
}
