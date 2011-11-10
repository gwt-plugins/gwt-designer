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
package com.google.gdt.eclipse.designer.support.http;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * 
 * @author mitin_aa
 * @coverage gwt.http
 */
public class MimeTypes {
  private static Map<String, String> m_mimeTypes = Maps.newHashMap();
  static {
    m_mimeTypes.put(".txt", "text/plain");
    m_mimeTypes.put(".html", "text/html");
    m_mimeTypes.put(".htm", "text/html");
    m_mimeTypes.put(".css", "text/css");
    m_mimeTypes.put(".gif", "image/gif");
    m_mimeTypes.put(".jpg", "image/jpeg");
    m_mimeTypes.put(".jpeg", "image/jpeg");
    m_mimeTypes.put(".jpe", "image/jpeg");
    m_mimeTypes.put(".png", "image/png");
    m_mimeTypes.put(".bmp", "image/bmp");
    m_mimeTypes.put(".wav", "audio/x-wav");
    m_mimeTypes.put(".avi", "video/avi");
    m_mimeTypes.put(".js", "application/x-javascript");
  }

  public static String getMimeType(String url) {
    int dotIndex = url.lastIndexOf('.');
    String ext = url.substring(dotIndex);
    synchronized (m_mimeTypes) {
      String type = m_mimeTypes.get(ext.toLowerCase());
      return type != null ? type : "application/octet-stream";
    }
  }
}