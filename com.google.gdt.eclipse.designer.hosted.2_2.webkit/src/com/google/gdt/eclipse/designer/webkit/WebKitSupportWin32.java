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
package com.google.gdt.eclipse.designer.webkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * WebKit deploying helper.
 * 
 * @author mitin_aa
 */
public class WebKitSupportWin32 {
  private static final String WEBKIT_VERSION_NAME = "webkit.version";
  private static File WEBKIT_DIR =
      new File(WebKitActivator.getDefault().getStateLocation().toFile(), "WebKit");
  private static boolean m_initialized;
  private static boolean m_available;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private WebKitSupportWin32() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Deployment
  //
  ////////////////////////////////////////////////////////////////////////////	
  public static void deployIfNeededAndLoad() {
    if (!m_initialized) {
      try {
        // extract
        Bundle bundle = Platform.getBundle("com.google.gdt.eclipse.designer.hosted.2_0.webkit");
        URL resource = FileLocator.resolve(bundle.getResource("WebKit.zip"));
		ZipFile zipFile = new ZipFile(resource.getPath());
        try {
          if (deployNeeded(zipFile)) {
            extract(zipFile);
          }
        } finally {
          zipFile.close();
        }
        load();
        m_available = true;
      } catch (Throwable e) {
        // ignore
      }
      m_initialized = true;
    }
  }

  public static boolean isAvailable() {
    return m_available;
  }

  private static boolean deployNeeded(ZipFile zipFile) {
    if (WEBKIT_DIR.exists()) {
      // check if changed
      try {
        File versionFile = new File(WEBKIT_DIR, WEBKIT_VERSION_NAME);
        if (!versionFile.exists()) {
          return true;
        }
        String currentVersion = readString(new FileInputStream(versionFile));
        // new version
        String newVersion =
            readString(zipFile.getInputStream(zipFile.getEntry(WEBKIT_VERSION_NAME)));
        return !currentVersion.equals(newVersion);
      } catch (Throwable e) {
        // ignore, means deploy needed
      }
    }
    return true;
  }

  private static String readString(InputStream inputStream) throws IOException {
    String stringValue = IOUtils.toString(inputStream);
    IOUtils.closeQuietly(inputStream);
    return stringValue;
  }

  private static void load() {
    String webkitDir = WEBKIT_DIR.getAbsolutePath() + File.separator;
    System.load(webkitDir + "icudt40.dll");
    System.load(webkitDir + "icuuc40.dll");
    System.load(webkitDir + "icuin40.dll");
    System.load(webkitDir + "CFLite.dll");
    System.load(webkitDir + "pthreadVC2.dll");
    System.load(webkitDir + "JavaScriptCore.dll");
    System.load(webkitDir + "libxml2.dll");
    System.load(webkitDir + "libxslt.dll");
    System.load(webkitDir + "cairo.dll");
    System.load(webkitDir + "libcurl.dll");
    System.load(webkitDir + "WebKit.dll");
  }

  private static void extract(ZipFile zipFile) throws Exception {
    // remove any possibly corrupted contents
    FileUtils.deleteQuietly(WEBKIT_DIR);
    WEBKIT_DIR.mkdirs();
    Enumeration<? extends ZipEntry> entries = zipFile.entries();
    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      if (entry.isDirectory()) {
        new File(WEBKIT_DIR, entry.getName()).mkdirs();
        continue;
      }
      InputStream inputStream = zipFile.getInputStream(entry);
      File outputFile = new File(WEBKIT_DIR, entry.getName());
      FileOutputStream outputStream = new FileOutputStream(outputFile);
      IOUtils.copy(inputStream, outputStream);
      IOUtils.closeQuietly(inputStream);
      IOUtils.closeQuietly(outputStream);
    }
  }
}
