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
package com.google.gwt.core.client;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This annotation is used to break out of a module's source path in hosted
 * mode. Types annotated with this annotation will not be loaded by hosted
 * mode's CompilingClassLoader. Instead, the bytecode for the type will be
 * loaded from the system classloader.
 * <p>
 * This annotation is typically combined with the <code>super-source</code> tag
 * to provide web-mode implementations of (binary-only) types that the developer
 * wishes to use in hosted mode. This can be used, for instance, to provide a
 * reference implementation to develop unit tests.
 */
@Documented
@Target(ElementType.TYPE)
public @interface GwtScriptOnly {
}
