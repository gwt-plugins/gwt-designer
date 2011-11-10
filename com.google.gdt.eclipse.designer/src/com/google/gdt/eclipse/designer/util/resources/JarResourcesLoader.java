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
package com.google.gdt.eclipse.designer.util.resources;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Implementation of {@link IResourcesLoader} for single JAR file.
 * 
 * @author scheglov_ke
 * @coverage gwt.util.resources
 */
final class JarResourcesLoader implements IResourcesLoader {
  private final JarFile m_jarFile;
  private List<String> m_entryNames;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JarResourcesLoader(File file) {
    try {
      m_jarFile = new JarFile(file);
    } catch (Throwable e) {
      throw new Error("Unable to open file: " + file, e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IResourcesLoader
  //
  ////////////////////////////////////////////////////////////////////////////
  public void dispose() {
    ExecutionUtils.runIgnore(new RunnableEx() {
      public void run() throws Exception {
        m_jarFile.close();
      }
    });
  }

  public InputStream getResourceAsStream(final String path) throws Exception {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<InputStream>() {
      public InputStream runObject() throws Exception {
        ZipEntry entry = m_jarFile.getEntry(path);
        if (entry != null) {
          return m_jarFile.getInputStream(entry);
        }
        return null;
      }
    }, null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IResourcesLoader: listing
  //
  ////////////////////////////////////////////////////////////////////////////
  public void appendFiles(List<String> files, String path) throws Exception {
    // prepare full list of entries names
    if (m_entryNames == null) {
      m_entryNames = Lists.newArrayList();
      //
      Enumeration<JarEntry> enumeration = m_jarFile.entries();
      while (enumeration.hasMoreElements()) {
        JarEntry entry = enumeration.nextElement();
        m_entryNames.add(entry.getName());
      }
    }
    // add into "files" only entries that are inside of given "path"
    for (String entryName : m_entryNames) {
      if (entryName.startsWith(path)) {
        String relativePath = entryName.substring(path.length());
        // normalize relative path
        if (relativePath.startsWith("/")) {
          relativePath = relativePath.substring(1);
        }
        // add relative path
        if (relativePath.length() != 0) {
          files.add(relativePath);
        }
      }
    }
  }
}