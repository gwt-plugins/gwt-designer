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
package com.google.gdt.eclipse.designer.mobile.device.command;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.List;

/**
 * Abstract command for modifying devices.
 * 
 * @author scheglov_ke
 * @coverage gwt.mobile.device
 */
public abstract class Command {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Execution
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Executes this {@link Command}.
   */
  public abstract void execute();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Writing
  //
  ////////////////////////////////////////////////////////////////////////////
  private StringBuffer m_stringBuffer;

  /**
   * @return the {@link String} that contains information about this {@link Command}. It will be
   *         passed back to constructor during reading.
   */
  @Override
  public final String toString() {
    m_stringBuffer = new StringBuffer();
    m_stringBuffer.append("\t<");
    // use ID as tag
    {
      String id = ReflectionUtils.getFieldString(getClass(), "ID");
      m_stringBuffer.append(id);
    }
    //
    addAttributes();
    m_stringBuffer.append("/>");
    return m_stringBuffer.toString();
  }

  /**
   * Adds single attribute.
   */
  protected final void addAttribute(String name, boolean value) {
    addAttribute(name, value ? "true" : "false");
  }

  /**
   * Adds single attribute.
   */
  protected final void addAttribute(String name, String value) {
    if (value != null) {
      m_stringBuffer.append("\n\t\t");
      m_stringBuffer.append(name);
      m_stringBuffer.append("=\"");
      //
      value = StringEscapeUtils.escapeXml(value);
      {
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
          char c = value.charAt(i);
          if (c < 0x20) {
            escaped.append("&#");
            escaped.append((int) c);
            escaped.append(";");
          } else {
            escaped.append(c);
          }
        }
        value = escaped.toString();
      }
      m_stringBuffer.append(value);
      //
      m_stringBuffer.append("\"");
    }
  }

  /**
   * Subclasses should implement this methods and use {@link #addAttribute(String, String)} to add
   * separate attributes.
   */
  protected void addAttributes() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Add command in given list, possible with some optimizations.
   */
  public void addToCommandList(List<Command> commands) {
    commands.add(this);
  }
}
