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
package com.google.gdt.eclipse.designer.gxt.databinding.model.bindings;

import com.google.gdt.eclipse.designer.gxt.databinding.model.ObserveInfo;

/**
 * 
 * @author lobas_av
 * 
 */
public class TimeFieldBindingInfo extends FieldBindingInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TimeFieldBindingInfo(ObserveInfo target, ObserveInfo targetProperty, String parsedProperty) {
    super(target, targetProperty, parsedProperty);
    m_baseClassName = "com.extjs.gxt.ui.client.binding.TimeFieldBinding";
  }
}