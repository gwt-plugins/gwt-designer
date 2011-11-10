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
package com.google.gwt.dev.resource.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.commons.lang.StringUtils;

/**
 * Represents a resource contained in a jar or zip file.
 */
public class ZipFileResource extends AbstractResource {

  private final ZipFileClassPathEntry classPathEntry;
  private final String path;
  // >>> Instantiations
  private final String[] pathParts;
  // <<< Instantiations

  public ZipFileResource(ZipFileClassPathEntry classPathEntry, String path) {
    this.classPathEntry = classPathEntry;
    this.path = path;
    // >>> Instantiations
    this.pathParts = StringUtils.split(path, '/');
    // <<< Instantiations
  }

  @Override
  public ZipFileClassPathEntry getClassPathEntry() {
    return classPathEntry;
  }

  @Override
  public long getLastModified() {
    // XXX < Instantiations
    ZipEntry entry = getEntry();
    if (entry == null) {
    	return -1;
    }
    // XXX > Instantiations
	return entry.getTime();
  }

  @Override
  public String getLocation() {
    // CHECKSTYLE_OFF
    String proto = classPathEntry.getZipFile() instanceof JarFile ? "jar:"
        : "zip:";
    // CHECKSTYLE_ON
    return proto + classPathEntry.getLocation() + "!/" + path;
  }

  @Override
  public String getPath() {
    return path;
  }
  // XXX < Instantiations
  public String[] getPathParts() {
	return pathParts;
  }
  // XXX > Instantiations
  /**
   * Since we don't dynamically reload zips during a run, zip-based resources
   * cannot become stale.
   */
  @Override
  public boolean isStale() {
    return false;
  }

  @Override
  public InputStream openContents() {
    try {
      return classPathEntry.getZipFile().getInputStream(getEntry());
    } catch (IOException e) {
      // The spec for this method says it can return null.
      return null;
    }
  }

  @Override
  public boolean wasRerooted() {
    return false;
  }

  private ZipEntry getEntry() {
    return classPathEntry.getZipFile().getEntry(path);
  }
}
