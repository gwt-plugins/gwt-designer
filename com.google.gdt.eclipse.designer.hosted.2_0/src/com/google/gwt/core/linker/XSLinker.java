/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.core.linker;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.LinkerOrder.Order;
import com.google.gwt.core.ext.linker.impl.SelectionScriptLinker;

/**
 * Generates a cross-site compatible bootstrap sequence.
 */
@LinkerOrder(Order.PRIMARY)
public class XSLinker extends SelectionScriptLinker {

	@Override
	protected String getCompilationExtension(TreeLogger logger, LinkerContext context)
			throws UnableToCompleteException {
		return null;
	}

	@Override
	protected String getModulePrefix(TreeLogger logger, LinkerContext context, String strongName)
			throws UnableToCompleteException {
		return null;
	}

	@Override
	protected String getModuleSuffix(TreeLogger logger, LinkerContext context)
			throws UnableToCompleteException {
		return null;
	}

	@Override
	protected String getSelectionScriptTemplate(TreeLogger logger, LinkerContext context)
			throws UnableToCompleteException {
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}
 
}
