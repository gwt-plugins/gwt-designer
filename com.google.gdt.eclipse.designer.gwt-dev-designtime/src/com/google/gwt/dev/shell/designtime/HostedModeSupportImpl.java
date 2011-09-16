/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gwt.dev.shell.designtime;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.dev.cfg.BindingProperty;
import com.google.gwt.dev.cfg.ModuleDef;
import com.google.gwt.dev.cfg.ModuleDefLoader;
import com.google.gwt.dev.cfg.Properties;
import com.google.gwt.dev.cfg.Property;
import com.google.gwt.dev.javac.CompilationState;
import com.google.gwt.dev.javac.typemodel.TypeOracle;
import com.google.gwt.dev.shell.ArtifactAcceptor;
import com.google.gwt.dev.shell.ModuleSpaceHost;
import com.google.gwt.dev.shell.ShellModuleSpaceHost;
import com.google.gwt.dev.util.log.AbstractTreeLogger;
import com.google.gwt.dev.util.log.PrintWriterTreeLogger;

import java.io.File;
import java.io.PrintWriter;

/**
 * Bridge between GWT Designer and GWT dev classes.
 */
public final class HostedModeSupportImpl {
  private static final TreeLogger.Type[] TREE_LOGGER_TYPES = {
      TreeLogger.ERROR, TreeLogger.WARN, TreeLogger.INFO, TreeLogger.TRACE,
      TreeLogger.DEBUG, TreeLogger.SPAM, TreeLogger.ALL};

  private ModuleDef moduleDef;
  private TypeOracle typeOracle;

  private TreeLogger logger;

  public HostedModeSupportImpl() {
    super();
  }

  public DelegatingModuleSpace createDelegatingModuleSpace(
      Object moduleSpaceHost, String moduleName, Object delegate) {
    return new DelegatingModuleSpace(getLogger(),
        (ModuleSpaceHost) moduleSpaceHost, moduleName, delegate);
  }

  public TreeLogger createLogger(PrintWriter writer, int logLevel) {
    if (writer == null) {
      this.logger = TreeLogger.NULL;
    } else {
      this.logger = new PrintWriterTreeLogger(writer);
      ((AbstractTreeLogger) this.logger).setMaxDetail(convertLogLevel(logLevel));
    }
    return this.logger;
  }

  private TreeLogger.Type convertLogLevel(int logLevel) {
    if (logLevel >= 0 && logLevel < TREE_LOGGER_TYPES.length + 1) {
      return TREE_LOGGER_TYPES[logLevel];
    }
    return TreeLogger.ERROR;
  }

  public ModuleSpaceHost createModuleSpaceHost(final String moduleName,
      File genDir, String userAgentString) throws Exception {
    moduleDef = loadModule(moduleName);
    fixUserAgentProperty(moduleDef, userAgentString);
    // Create a sandbox for the module.
    CompilationState compilationState = moduleDef.getCompilationState(getLogger());
    typeOracle = compilationState.getTypeOracle();
    ModuleSpaceHost moduleSpaceHost =
        (ModuleSpaceHost) ShellModuleSpaceHost.class.getConstructors()[0].newInstance(getLogger(),
            compilationState, moduleDef, genDir, new ArtifactAcceptor() {
              public void accept(TreeLogger logger, ArtifactSet newlyGeneratedArtifacts)
                  throws UnableToCompleteException {
              }
            }, null);
    return moduleSpaceHost;
  }

  public Object findJType(String name) {
    assert typeOracle != null : "Load a module first";
    return typeOracle.findType(name);
  }

  private ModuleDef loadModule(String moduleName) throws Exception {
    ModuleDef moduleDef = ModuleDefLoader.loadFromClassPath(getLogger(),
        moduleName, true);
    assert moduleDef != null : "Required module state is absent";
    return moduleDef;
  }

  private TreeLogger getLogger() {
    return this.logger;
  }

  /**
   * Forcibly set 'user.agent' property to current platform.
   * http://fogbugz.instantiations.com/default.php?41513
   */
  private void fixUserAgentProperty(ModuleDef module, String userAgentString) {
    Properties properties = module.getProperties();
    for (Property property : properties) {
      if ("user.agent".equals(property.getName())) {
        BindingProperty bindingProperty = (BindingProperty) property;
        bindingProperty.setAllowedValues(bindingProperty.getRootCondition(),
            userAgentString);
        return;
      }
    }
  }
}