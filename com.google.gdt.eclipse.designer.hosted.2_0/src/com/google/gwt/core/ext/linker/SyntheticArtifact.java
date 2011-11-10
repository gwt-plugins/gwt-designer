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
package com.google.gwt.core.ext.linker;

import com.google.gwt.core.ext.Linker;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.util.DiskCache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Artifacts created by {@link AbstractLinker}.
 */
public class SyntheticArtifact extends EmittedArtifact {
  private static final DiskCache diskCache = new DiskCache();

  private final long lastModified;
  private transient long token;

  public SyntheticArtifact(Class<? extends Linker> linkerType,
      String partialPath, byte[] data) {
    this(linkerType, partialPath, data, System.currentTimeMillis());
  }

  public SyntheticArtifact(Class<? extends Linker> linkerType,
      String partialPath, byte[] data, long lastModified) {
    super(linkerType, partialPath);
    assert data != null;
    this.lastModified = lastModified;
    this.token = diskCache.writeByteArray(data);
  }

  @Override
  public InputStream getContents(TreeLogger logger)
      throws UnableToCompleteException {
    return new ByteArrayInputStream(diskCache.readByteArray(token));
  }

  @Override
  public long getLastModified() {
    return lastModified;
  }

  @Override
  public void writeTo(TreeLogger logger, OutputStream out)
      throws UnableToCompleteException {
    diskCache.transferToStream(token, out);
  }

  private void readObject(ObjectInputStream stream) throws IOException,
      ClassNotFoundException {
    stream.defaultReadObject();
    token = diskCache.transferFromStream(stream);
  }

  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    diskCache.transferToStream(token, stream);
  }
}
