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
package org.eclipse.wb.core.utils.reflect;

import org.osgi.framework.Bundle;

/**
 * Implementation of {@link ClassLoader} for loading classes from OSGi {@link Bundle}.
 * 
 * @author scheglov_ke
 * @coverage core.util
 */
public final class BundleClassLoader extends ClassLoader {
	private final Bundle m_bundle;
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BundleClassLoader(Bundle bundle) {
		m_bundle = bundle;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// ClassLoader
	//
	////////////////////////////////////////////////////////////////////////////
	public Class loadClass(String name) throws ClassNotFoundException {
		return m_bundle.loadClass(name);
	}
}
