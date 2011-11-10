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
package com.google.gwt.dev.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Adapts {@link TextOutput} to an internal text buffer.
 */
public class DefaultTextOutput extends AbstractTextOutput {

  private final StringWriter sw = new StringWriter();
  private final PrintWriter out;

  public DefaultTextOutput(boolean compact) {
    super(compact);
    setPrintWriter(out = new PrintWriter(sw));
  }

  public String toString() {
    out.flush();
    if (sw != null) {
      return sw.toString();
    } else {
      return super.toString();
    }
  }
}
