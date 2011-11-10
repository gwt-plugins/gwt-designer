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
#ifndef __BROWSER_SHELL_H
#define __BROWSER_SHELL_H

#include <jni.h>

extern JNIEnv* gEnv;
extern JavaVM* gJavaVM;
extern jclass c_BrowserShell;

extern jmethodID m_BrowserShell_windowScriptObjectAvailable; 
extern jmethodID m_BrowserShell_doneLoading; 

jstring createJavaString(JNIEnv* env, NSString* nsString);

#define OS_NATIVE(func) Java_com_instantiations_designer_gwt_mac_BrowserShellMacImplCarbon_##func

#endif
