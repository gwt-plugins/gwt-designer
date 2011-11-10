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
package com.google.gdt.eclipse.designer;

import org.eclipse.wb.internal.core.utils.exception.DesignerException;

/**
 * Constants for GWT {@link DesignerException}'s.
 * 
 * @author scheglov_ke
 * @coverage gwt
 */
public interface IExceptionConstants {
  int NO_MODULE = 4000;
  int DONT_SUBCLASS_PANEL = 4001;
  int MAIL_SAMPLE_GET = 4002;
  int NO_SUCH_IMAGE_BUNDLE = 4003;
  int PANEL_ADD_INVOCATION = 4004;
  int INVALID_WEB_XML = 4006;
  int NPE_IN_COMPILED_CLASS = 4007;
  int NO_MODULE_FILE = 4008;
  int BINARY_INCOMPAT_GWT22 = 4009;
  int INVALID_MODULE_FILE = 4010;
  int INHERITS_NO_NAME = 4011;
  int NO_DESIGN_WIDGET = 4012;
  int UNSUPPORTED_GWT_SDK = 4105;
  int NO_GWT_SDK_SUPPORT = 4106;
}
