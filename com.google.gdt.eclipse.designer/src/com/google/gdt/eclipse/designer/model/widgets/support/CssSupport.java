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
package com.google.gdt.eclipse.designer.model.widgets.support;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gdt.eclipse.designer.Activator;
import com.google.gdt.eclipse.designer.util.DefaultModuleDescription;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.Document;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Support for CSS resources in {@link GwtState}.
 * 
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage gwt.model
 */
public final class CssSupport {
  private final GwtState state;
  private List<String> resources;
  private List<IFile> files;
  private long nextRequestId;
  private final Map<IFile, Long> filesStampMap = Maps.newHashMap();
  private final Set<String> waitRequestSet = Sets.newHashSet();
  private final Set<String> waitApplySet = Sets.newHashSet();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CssSupport(GwtState state) {
    this.state = state;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the CSS resources.
   */
  public List<String> getResources() {
    return resources;
  }

  /**
   * @return the {@link IFile}s for CSS resources.
   */
  public List<IFile> getFiles() {
    return files;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Private access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Prepares CSS resources and files.
   */
  void prepareResources() throws Exception {
    ModuleDescription moduleDescription = state.getModuleDescription();
    resources = Utils.getCssResources(moduleDescription);
    if (moduleDescription instanceof DefaultModuleDescription) {
      files =
          Utils.getFilesForResources(
              ((DefaultModuleDescription) moduleDescription).getFile(),
              resources);
      for (IFile file : files) {
        filesStampMap.put(file, file.getModificationStamp());
      }
    }
  }

  /**
   * Adds links to the CSS resources.
   */
  void addLinkDeclarations(List<String> declarations) {
    for (String cssResource : resources) {
      declarations.add(MessageFormat.format(
          "<link rel=''stylesheet'' type=''text/css'' href=''{0}''/>",
          cssResource));
    }
  }

  /**
   * Adds DIVs for CSS "apply wait", see {@link #waitFor()} JavaDoc.
   */
  String addReloadingFeature(String html) {
    StringBuilder declarations = new StringBuilder();
    for (String resource : resources) {
      String name = getWaitRequestName(resource);
      declarations.append("<div class='");
      declarations.append(name);
      declarations.append("'></div>\n");
    }
    return StringUtils.replace(html, "%CSS_WAIT_DECLARATIONS%", declarations.toString());
  }

  /**
   * @return <code>true</code> if one or more CSS files were modified (and schedules them for
   *         reloading with next refresh).
   */
  boolean isModified() {
    waitRequestSet.clear();
    waitApplySet.clear();
    boolean modified = false;
    // check CSS files
    boolean hasModifiedCSSFiles = false;
    for (Map.Entry<IFile, Long> entry : filesStampMap.entrySet()) {
      IFile file = entry.getKey();
      long storedStamp = entry.getValue();
      long fileStamp = file.getModificationStamp();
      if (fileStamp != storedStamp) {
        modified = true;
        filesStampMap.put(file, fileStamp);
        hasModifiedCSSFiles = true;
      }
    }
    // schedule CSS load waiting
    if (hasModifiedCSSFiles) {
      synchronized (waitRequestSet) {
        for (String resource : resources) {
          String waitRequestName = getWaitRequestName(resource);
          waitRequestSet.add(waitRequestName);
        }
      }
    }
    // if has modified CSS files, ask Browser for reload
    if (modified) {
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          state.getHostModeSupport().invokeNativeVoid(
              "__reload_css",
              ArrayUtils.EMPTY_CLASS_ARRAY,
              ArrayUtils.EMPTY_OBJECT_ARRAY);
        }
      });
      waitFor();
    }
    // return final modification state
    return modified;
  }

  /**
   * Provides wait operation for CSS files to be applied.
   * 
   * CSS files waiting works as following:
   * <p>
   * When HTTP-server recognizes request for CSS resource it added into requested CSS-file fake CSS
   * class with style which applying caused browser to request some resource from HTTP-server, e.g.
   * HTTP-server adds something like that:
   * 
   * <pre> 
   * .gwt__wait_stylesheetXXXX {
   *   visibility: hidden;
   *   background: url("cssFileName_cssFileTimestamp");
   * }
   * </pre>
   * Where: XXXX is some unique identifier (hashCode for CSS file name), cssFileName is the name of
   * requested CSS file cssFileTimestamp is the timestamp of requested CSS file. So, when the
   * browser applies received CSS file with fake CSS class it requests
   * "cssFileName_cssFileTimestamp" resource from HTTP-server.
   * <p>
   * Note: browser would not request "cssFileName_cssFileTimestamp" resource without any
   * HTML-element with fake CSS class applied, thats why its needed to dynamically generate the HTML
   * element with fake CSS class for every CSS file in project (see set CSS wait declarations in
   * constructor).
   */
  void waitFor() {
    long startWait = System.currentTimeMillis();
    while (true) {
      // may be done
      boolean done = true;
      synchronized (waitRequestSet) {
        done &= waitRequestSet.isEmpty();
      }
      synchronized (waitApplySet) {
        done &= waitApplySet.isEmpty();
      }
      if (done) {
        break;
      }
      // do not wait more than 500ms
      if (System.currentTimeMillis() - startWait > 500) {
        break;
      }
      // wait more
      state.runMessagesLoop();
    }
  }

  /**
   * @return the name of CSS class to use for waiting given CSS file which is referred by
   *         {@link IFile} or public resource path.
   */
  private static String getWaitRequestName(String path) {
    String name = path;
    name = StringUtils.replace(name, "/", "_");
    name = StringUtils.removeEndIgnoreCase(name, ".css");
    return "wbp__wait_stylesheet_" + name;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Content access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the empty PNG image bytes, if this resource path is "wait for CSS" marker, or
   *         <code>null</code> if some other resource for requested.
   */
  byte[] getResourceWait(String publicResourcePath) {
    synchronized (waitApplySet) {
      if (waitApplySet.remove(publicResourcePath)) {
        // IE requires the content, otherwise 'image.complete' is always false.
        // This doesn't affect other browsers though, but returning some content is the right way.
        return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<byte[]>() {
          public byte[] runObject() throws Exception {
            return IOUtils2.readBytes(Activator.getFile("icons/empty.png"));
          }
        }, null);
      }
    }
    return null;
  }

  /**
   * @param result
   *          the bytes of CSS file.
   * @return the updates bytes of CSS file, with "wait for loading" markers added.
   */
  byte[] getResource(String publicResourcePath, byte[] result) throws Exception {
    if (publicResourcePath.toLowerCase().endsWith(".css")) {
      if (resources.contains(publicResourcePath)) {
        // prepare "wait apply" resource, should be image
        String waitRequestName = getWaitRequestName(publicResourcePath);
        String waitApplyName = waitRequestName + "_" + nextRequestId++ + ".png";
        // prepare content
        String cssContent = new String(result);
        cssContent = addRulesMarkers(publicResourcePath, cssContent);
        // add "wait apply" class to the end of CSS content
        {
          cssContent += "\n." + waitRequestName + "{";
          cssContent += "visibility: hidden; ";
          cssContent += "background-image: url('";
          cssContent += state.m_moduleBaseURL + waitApplyName;
          cssContent += "'); }\n";
          result = cssContent.getBytes();
        }
        // update request/apply sets
        synchronized (waitApplySet) {
          waitApplySet.add(waitApplyName);
        }
        synchronized (waitRequestSet) {
          waitRequestSet.remove(waitRequestName);
        }
      }
    }
    return result;
  }

  private String addRulesMarkers(String publicResourcePath, String content) throws Exception {
    Document sourceDocument = new Document(content);
    /*CssEditContext editContext = new CssEditContext(sourceDocument);
    CssDocument document = editContext.getCssDocument();
    for (CssRuleNode rule : document.getRules()) {
      int ruleId = m_lastTrackedRule++;
      rule.addDeclaration(CssFactory.newDeclaration("wbp-css-rule-" + ruleId, "0"));
    }
    System.out.println(sourceDocument.get());*/
    return sourceDocument.get();
  }
}