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
package com.google.gdt.eclipse.designer.uibinder;

import org.eclipse.wb.internal.core.utils.exception.DesignerException;

/**
 * Constants for GWT UiBinder {@link DesignerException}'s.
 * 
 * @author scheglov_ke
 * @coverage GWT.UiBinder
 */
public interface IExceptionConstants {
  int DONT_OPEN_JAVA = 4501;
  int WRONG_VERSION = 4502;
  int NO_FORM_TYPE = 4503;
  // 4504 not used
  int NO_FORM_PACKAGE = 4505;
  int ONLY_WIDGET_BASED = 4506;
  int UI_FIELD_FACTORY_FEATURE = 4507;
  int UI_FIELD_EXCEPTION = 4508;
  int UI_FACTORY_EXCEPTION = 4509;
  int NOT_CLIENT_PACKAGE = 4510;
}
