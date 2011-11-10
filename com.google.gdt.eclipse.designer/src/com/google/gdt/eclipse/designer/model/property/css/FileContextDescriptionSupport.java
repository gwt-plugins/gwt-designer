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
package com.google.gdt.eclipse.designer.model.property.css;

import com.google.common.collect.Maps;
import com.google.gdt.eclipse.designer.model.widgets.support.GwtState;
import com.google.gdt.eclipse.designer.model.widgets.support.IGwtStateProvider;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.BroadcastSupport;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;

import org.eclipse.core.resources.IFile;

import java.util.List;
import java.util.Map;

/**
 * Support for {@link IFile} based {@link ContextDescription}s.
 * 
 * @author scheglov_ke
 * @coverage gwt.model.property
 */
public final class FileContextDescriptionSupport {
  private final ObjectInfo m_object;
  private final GwtState m_gwtState;
  private final Map<IFile, FileContextDescription> m_fileContexts = Maps.newHashMap();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FileContextDescriptionSupport(ObjectInfo object) {
    m_object = object;
    m_gwtState = ((IGwtStateProvider) object).getState();
    BroadcastSupport broadcastSupport = m_object.getBroadcastSupport();
    // add IFile based context descriptions
    broadcastSupport.addListener(null, new StylePropertyEditorListener() {
      @Override
      public void addContextDescriptions(ObjectInfo object, List<ContextDescription> contexts)
          throws Exception {
        addFileContextDesccriptions(contexts);
      }
    });
    // dispose on hierarchy dispose
    broadcastSupport.addListener(null, new ObjectEventListener() {
      @Override
      public void dispose() throws Exception {
        for (ContextDescription context : m_fileContexts.values()) {
          context.dispose();
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  private void addFileContextDesccriptions(List<ContextDescription> contexts) throws Exception {
    List<IFile> files = m_gwtState.getCssSupport().getFiles();
    for (IFile file : files) {
      ContextDescription contextDescription = getContextDescription(file);
      contexts.add(contextDescription);
    }
  }

  /**
   * @return the existing or new {@link ContextDescription}, synchronized with current file.
   */
  private ContextDescription getContextDescription(IFile file) throws Exception {
    FileContextDescription contextDescription = m_fileContexts.get(file);
    // may be exists, but stale
    if (contextDescription != null) {
      if (contextDescription.isStale()) {
        contextDescription.dispose();
        m_fileContexts.remove(file);
        contextDescription = null;
      }
    }
    // create new
    if (contextDescription == null) {
      contextDescription = new FileContextDescription(file);
      m_fileContexts.put(file, contextDescription);
    }
    // done
    return contextDescription;
  }
}
