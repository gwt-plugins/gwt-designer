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
package com.google.gwt.dev.cfg;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.javac.StandardGeneratorContext;

/**
 * A rule to replace the type being rebound with a class whose name is
 * determined by a generator class. Generators usually generate new classes
 * during the deferred binding process, but it is not required.
 * 
 * XXX >>> Instantiations modified
 * Since we use our own class-loader and change it for every time we parse the code it becomes 
 * unacceptable to store the class itself. And because the module cached in static map, it doesn't 
 * re-parse and re-create rules. These lead to errors while searching generator annotations because 
 * generator class not equals (because of different class-loaders) to being searching by.
 * XXX <<< Instantiations
 */
public class RuleGenerateWith extends Rule {

  private final String generatorClassName;
	
  public RuleGenerateWith(Class<? extends Generator> generatorClass) {
    this.generatorClassName = generatorClass.getName();
  }

  @SuppressWarnings("unchecked")
  public String realize(TreeLogger logger, StandardGeneratorContext context,
      String typeName) throws UnableToCompleteException {
	ClassLoader cl = Thread.currentThread().getContextClassLoader();
	Class<?> generatorClass;
	try {
		generatorClass = cl.loadClass(generatorClassName);
	} catch (ClassNotFoundException e) {
		throw new UnableToCompleteException();
	}
    return context.runGenerator(logger, (Class<? extends Generator>) generatorClass, typeName);
  }

  public String toString() {
    return "<generate-with class='" + generatorClassName + "'/>";
  }
}
