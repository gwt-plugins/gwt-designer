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
package com.google.gwt.dev.json;

import java.io.IOException;
import java.io.Writer;

/**
 * All specific JSON types in this package implement this interface.
 */
public interface JsonValue {
  /**
   * JSON placeholder for null.
   */
  final JsonValue NULL = new JsonValue() {

    public JsonArray asArray() {
      return null;
    }

    public JsonBoolean asBoolean() {
      return null;
    }

    public JsonNumber asNumber() {
      return null;
    }

    public JsonObject asObject() {
      return null;
    }

    public JsonString asString() {
      return null;
    }

    public JsonValue copyDeeply() {
      return this;
    }

    public boolean isArray() {
      return false;
    }

    public boolean isBoolean() {
      return false;
    }

    public boolean isNumber() {
      return false;
    }

    public boolean isObject() {
      return false;
    }

    public boolean isString() {
      return false;
    }

    public void write(Writer writer) throws IOException {
      writer.append("null");
    }
  };

  JsonArray asArray();

  JsonBoolean asBoolean();

  JsonNumber asNumber();

  JsonObject asObject();

  JsonString asString();

  /**
   * Makes a full copy of the JSON data structure.
   */
  JsonValue copyDeeply();

  boolean isArray();

  boolean isBoolean();

  boolean isNumber();

  boolean isObject();

  boolean isString();

  void write(Writer writer) throws IOException;
}
