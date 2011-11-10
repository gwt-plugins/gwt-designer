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
package com.google.gwt.dev.util;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Represents a File that contains the serialized form of a Serializable object.
 * 
 * @param <T> the type of object serialized into the file
 */
public class FileBackedObject<T extends Serializable> implements Serializable {
  private final File backingFile;
  private final Class<T> clazz;

  /**
   * Constructs an empty FileBackedObject. A temporary File will be used and
   * this file will be deleted when the JVM exits.
   * 
   * @param clazz the type of object to be serialized
   * @throws IOException if the temporary file could not be created
   */
  public FileBackedObject(Class<T> clazz) throws IOException {
    this(clazz, File.createTempFile("fileBackedObject", ".ser"));
    backingFile.deleteOnExit();
  }

  /**
   * Constructs a FileBackedObject using an existing File object.
   * 
   * @param clazz the type of object to be serialized
   * @param backingFile the file to read from or write to
   */
  public FileBackedObject(Class<T> clazz, File backingFile) {
    this.clazz = clazz;
    this.backingFile = backingFile;
  }

  /**
   * Returns the underlying File object.
   */
  public File getFile() {
    return backingFile;
  }

  /**
   * Construct a new instance of the object stored in the backing file.
   * 
   * @param logger a sink for error messages
   * @return a new instance of the object stored in the backing file
   * @throws UnableToCompleteException if the backing store does not contain an
   *           object of type <code>T</code>
   */
  public T newInstance(TreeLogger logger) throws UnableToCompleteException {
    try {
      return Util.readFileAsObject(backingFile, clazz);
    } catch (ClassNotFoundException e) {
      logger.log(TreeLogger.ERROR, "Missing class definition", e);
      throw new UnableToCompleteException();
    } catch (IOException e) {
      logger.log(TreeLogger.ERROR, "Unable to instantiate object", e);
      throw new UnableToCompleteException();
    }
  }

  /**
   * Set the contents of the backing file.
   * 
   * @param logger a sink for error messages
   * @param object the object to store
   * @throws UnableToCompleteException if the object could not be serialized
   */
  public void set(TreeLogger logger, T object) throws IllegalStateException,
      UnableToCompleteException {
    assert clazz.isInstance(object);
    Util.writeObjectAsFile(logger, backingFile, object);
  }

  @Override
  public String toString() {
    return backingFile.toString() + "<" + clazz.getName() + ">";
  }
}
