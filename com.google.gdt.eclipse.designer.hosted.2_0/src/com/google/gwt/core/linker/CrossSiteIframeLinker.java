/*
 * Copyright 2010 Google Inc.
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
 * This linker uses an iframe to hold the code and a script tag to download the code. It can download code
 * cross-site, because it uses a script tag to download it and because it never uses XHR. The iframe,
 * meanwhile, makes it trivial to install additional code as the app runs.
 */
@LinkerOrder(Order.PRIMARY)
public class CrossSiteIframeLinker extends SelectionScriptLinker {

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
