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
package com.google.gdt.eclipse.designer.util.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Implementation of {@link IResourcesLoader} for single directory.
 * 
 * @author scheglov_ke
 * @coverage gwt.util.resources
 */
final class DirResourcesLoader implements IResourcesLoader {
  private final File m_file;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DirResourcesLoader(File file) {
    m_file = file;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IResourcesLoader
  //
  ////////////////////////////////////////////////////////////////////////////
  public void dispose() {
  }

  public InputStream getResourceAsStream(String path) throws Exception {
    File resultFile = new File(m_file, path);
    if (resultFile.exists() && resultFile.isFile()) {
      return new FileInputStream(resultFile);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IResourcesLoader: listing
  //
  ////////////////////////////////////////////////////////////////////////////
  public void appendFiles(List<String> files, String path) throws Exception {
    File root = new File(m_file, path);
    // XXX probably bug: "root" argument (first one) should be m_file?
    appendFiles(files, root, root);
  }

  /**
   * Appends files with recursion starting from given parent directory.
   */
  private static void appendFiles(List<String> files, File root, File parent) throws Exception {
    File[] children = parent.listFiles();
    if (children != null) {
      for (File child : children) {
        if (child.isDirectory()) {
          appendFiles(files, root, child);
        } else {
          files.add(getRelativePath(root, child));
        }
      }
    }
  }

  /**
   * @return the path of <code>child</code> relative to <code>root</code>.
   */
  private static String getRelativePath(File root, File child) {
    String relativePath;
    {
      String rootPath = root.getPath();
      String childPath = child.getPath();
      relativePath = childPath.substring(rootPath.length());
    }
    // normalize
    relativePath = relativePath.replace(File.separatorChar, '/');
    if (relativePath.startsWith("/")) {
      relativePath = relativePath.substring(1);
    }
    // final result
    return relativePath;
  }
}