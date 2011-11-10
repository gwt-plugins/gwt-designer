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
package com.google.gwt.core.ext.linker.impl;

import java.util.Comparator;
import java.util.SortedSet;

import com.google.gwt.core.ext.Linker;
import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.SelectionProperty;
import com.google.gwt.dev.cfg.ModuleDef;

/**
 * An implementation of {@link LinkerContext} that is initialized from a
 * {@link ModuleDef}.
 */
public class StandardLinkerContext extends Linker implements LinkerContext {

	public static final Comparator<? super SelectionProperty> SELECTION_PROPERTY_COMPARATOR = null;

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public ArtifactSet link(TreeLogger logger, LinkerContext context, ArtifactSet artifacts)
			throws UnableToCompleteException {
		return null;
	}

	public SortedSet<com.google.gwt.core.ext.ConfigurationProperty> getConfigurationProperties() {
		return null;
	}

	public String getModuleFunctionName() {
		return null;
	}

	public long getModuleLastModified() {
		return 0;
	}

	public String getModuleName() {
		return null;
	}

	public SortedSet<SelectionProperty> getProperties() {
		return null;
	}

	public boolean isOutputCompact() {
		return false;
	}

	public String optimizeJavaScript(TreeLogger logger, String jsProgram) throws UnableToCompleteException {
		return null;
	}

}
