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
package com.google.gdt.eclipse.designer.nls;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.nls.bundle.IPropertiesAccessor;

import org.apache.tapestry.util.text.LocalizedPropertiesLoader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link IPropertiesAccessor} for GWT *.properties files (support for UTF-8).
 * 
 * @author scheglov_ke
 * @coverage gwt.nls
 */
public class GwtPropertiesAccessor implements IPropertiesAccessor {
  public static final IPropertiesAccessor INSTANCE = new GwtPropertiesAccessor();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private GwtPropertiesAccessor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IPropertiesAccessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public Map<String, String> load(InputStream is, String charset) throws Exception {
    LocalizedPropertiesLoader loader = new LocalizedPropertiesLoader(is, "UTF-8");
    Map<String, String> map = new HashMap<String, String>();
    loader.load(map);
    return map;
  }

  public void save(OutputStream out, String charset, Map<String, String> map, String comments)
      throws Exception {
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
    // comments
    if (comments != null) {
      writeComments(bw, comments);
    }
    // write values sorted by key
    synchronized (this) {
      List<String> sortedKeys = Lists.newArrayList(map.keySet());
      Collections.sort(sortedKeys);
      for (String key : sortedKeys) {
        String value = map.get(key);
        bw.write(key + "=" + value);
        bw.newLine();
      }
    }
    // done
    bw.flush();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Save
  //
  ////////////////////////////////////////////////////////////////////////////
  private static void writeComments(BufferedWriter bw, String comments) throws IOException {
    bw.write("#");
    int len = comments.length();
    int current = 0;
    int last = 0;
    char[] uu = new char[6];
    uu[0] = '\\';
    uu[1] = 'u';
    while (current < len) {
      char c = comments.charAt(current);
      if (c > '\u00ff' || c == '\n' || c == '\r') {
        if (last != current) {
          bw.write(comments.substring(last, current));
        }
        if (c > '\u00ff') {
          uu[2] = toHex(c >> 12 & 0xf);
          uu[3] = toHex(c >> 8 & 0xf);
          uu[4] = toHex(c >> 4 & 0xf);
          uu[5] = toHex(c & 0xf);
          bw.write(new String(uu));
        } else {
          bw.newLine();
          if (c == '\r' && current != len - 1 && comments.charAt(current + 1) == '\n') {
            current++;
          }
          if (current == len - 1
              || comments.charAt(current + 1) != '#'
              && comments.charAt(current + 1) != '!') {
            bw.write("#");
          }
        }
        last = current + 1;
      }
      current++;
    }
    if (last != current) {
      bw.write(comments.substring(last, current));
    }
    bw.newLine();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hex
  //
  ////////////////////////////////////////////////////////////////////////////
  /** A table of hex digits */
  private static final char[] hexDigit = {
      '0',
      '1',
      '2',
      '3',
      '4',
      '5',
      '6',
      '7',
      '8',
      '9',
      'A',
      'B',
      'C',
      'D',
      'E',
      'F'};

  /**
   * Convert a nibble to a hex character
   * 
   * @param nibble
   *          the nibble to convert.
   */
  private static char toHex(int nibble) {
    return hexDigit[(nibble & 0xF)];
  }
}
