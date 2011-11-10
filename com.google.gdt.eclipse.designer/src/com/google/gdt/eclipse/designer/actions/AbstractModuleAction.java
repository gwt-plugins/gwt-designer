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
package com.google.gdt.eclipse.designer.actions;

import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.Utils;

import org.eclipse.wb.core.branding.BrandingUtils;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * Action to perform some operation with selected GWT module.
 * 
 * @author scheglov_ke
 * @coverage gwt.actions
 */
public abstract class AbstractModuleAction extends Action implements IWorkbenchWindowActionDelegate {
  protected ModuleDescription m_selectedModule;

  ////////////////////////////////////////////////////////////////////////////
  //
  // IWorkbenchWindowActionDelegate
  //
  ////////////////////////////////////////////////////////////////////////////
  public void init(IWorkbenchWindow window) {
  }

  public void dispose() {
  }

  public void selectionChanged(IAction action, ISelection selection) {
    try {
      m_selectedModule = getSelectedModule(selection);
    } catch (Throwable e) {
    }
    selectedModuleChanged(action);
  }

  public void run(IAction action) {
    if (m_selectedModule != null) {
      try {
        runWithSelectedModule();
      } catch (Throwable e) {
        showException(e);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  protected abstract void selectedModuleChanged(IAction action);

  protected abstract void runWithSelectedModule() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Finds module corresponding given {@link ISelection}.
   */
  public static ModuleDescription getSelectedModule(ISelection selection) throws Exception {
    if (selection instanceof IStructuredSelection) {
      IStructuredSelection structuredSelection = (IStructuredSelection) selection;
      Object selectedObject = structuredSelection.getFirstElement();
      if (selectedObject instanceof IResource) {
        return getModuleFromResource(selectedObject);
      }
      if (selectedObject instanceof IAdaptable) {
        Object resource = ((IAdaptable) selectedObject).getAdapter(IResource.class);
        return getModuleFromResource(resource);
      }
    } else if (selection instanceof ITextSelection) {
      IEditorPart activeEditor = DesignerPlugin.getActiveEditor();
      if (activeEditor != null) {
        IEditorInput editorInput = activeEditor.getEditorInput();
        if (editorInput instanceof IFileEditorInput) {
          IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
          return getModuleFromResource(fileEditorInput.getFile());
        }
      }
    }
    return null;
  }

  /**
   * If given object is {@link IResource} instance, finds module corresponding this resource.
   */
  private static ModuleDescription getModuleFromResource(Object o) throws Exception {
    if (o instanceof IResource) {
      IResource resource = (IResource) o;
      return Utils.getSingleModule(resource);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // UI utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Shows the {@link Throwable} to user.
   */
  public static void showException(final Throwable exception) {
    // log
    DesignerPlugin.log(exception);
    // show
    {
      final Throwable rootCause = DesignerExceptionUtils.getDesignerCause(exception);
      final Status status =
          new Status(IStatus.ERROR,
              DesignerPlugin.getDefault().toString(),
              IStatus.ERROR,
              "Parse error or internal Designer error.",
              rootCause);
      ErrorDialog dialog =
          new ErrorDialog(
              DesignerPlugin.getShell(),
              "Exception happened",
              "Designer error occurred.\nSelect Details>> for more information.\nSee the Error Log for more information.",
              status, IStatus.ERROR) {
            private Clipboard clipboard;

            @Override
            protected org.eclipse.swt.widgets.List createDropDownList(Composite parent) {
              final org.eclipse.swt.widgets.List list = super.createDropDownList(parent);
              list.removeAll();
              // populate list using custom PrintWriter
              list.add("Plug-in Provider: Google");
              list.add("Plug-in Name: " + BrandingUtils.getBranding().getProductName());
              list.add("Plug-in ID: org.eclipse");
              //list.add("Plug-in Version: " + String.valueOf(product.getVersion()));
              list.add("");
              final PrintWriter printWriter = new PrintWriter(new Writer() {
                @Override
                public void write(char[] cbuf, int off, int len) throws IOException {
                  if (len != 2 && !(cbuf[0] == '\r' || cbuf[0] == '\n')) {
                    list.add(StringUtils.replace(new String(cbuf, off, len), "\t", "    "));
                  }
                }

                @Override
                public void flush() throws IOException {
                }

                @Override
                public void close() throws IOException {
                }
              });
              if (rootCause != null) {
                rootCause.printStackTrace(printWriter);
                list.add("");
                list.add("Full stack trace (to see full context):");
              }
              exception.printStackTrace(printWriter);
              // print config
              list.add("");
              // install own context menu
              Menu menu = list.getMenu();
              menu.dispose();
              Menu copyMenu = new Menu(list);
              MenuItem copyItem = new MenuItem(copyMenu, SWT.NONE);
              copyItem.setText(JFaceResources.getString("copy")); //$NON-NLS-1$
              copyItem.addSelectionListener(new SelectionListener() {
                public void widgetSelected(SelectionEvent e) {
                  copyList(list);
                }

                public void widgetDefaultSelected(SelectionEvent e) {
                  copyList(list);
                }
              });
              list.setMenu(copyMenu);
              return list;
            }

            private void copyList(org.eclipse.swt.widgets.List list) {
              if (clipboard != null) {
                clipboard.dispose();
              }
              StringBuffer statusBuffer = new StringBuffer();
              for (int i = 0; i < list.getItemCount(); ++i) {
                statusBuffer.append(list.getItem(i));
                statusBuffer.append("\r\n");
              }
              clipboard = new Clipboard(list.getDisplay());
              clipboard.setContents(
                  new Object[]{statusBuffer.toString()},
                  new Transfer[]{TextTransfer.getInstance()});
            }

            @Override
            public boolean close() {
              if (clipboard != null) {
                clipboard.dispose();
              }
              return super.close();
            }
          };
      dialog.open();
    }
  }
}
