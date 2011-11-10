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
// Modified by Google

// API class

package com.google.gwt.dev.js.rhino;


/**
 * Java reflection of JavaScript exceptions.  (Possibly wrapping a Java exception.)
 *
 * @author Mike McCabe
 */
public class JavaScriptException extends Exception {

    /**
     * Create a JavaScript exception wrapping the given JavaScript value.
     *
     * Instances of this class are thrown by the JavaScript 'throw' keyword.
     *
     * @param value the JavaScript value thrown.
     */
    public JavaScriptException(Object value) {
        super(value.toString());
        this.value = value;
    }

    /**
     * Get the exception value originally thrown.  This may be a
     * JavaScript value (null, undefined, Boolean, Number, String,
     * Scriptable or Function) or a Java exception value thrown from a
     * host object or from Java called through LiveConnect.
     *
     * @return the value wrapped by this exception
     */
    public Object getValue() {
        return value;
    }

    /**
     * The JavaScript exception value.  This value is not
     * intended for general use; if the JavaScriptException wraps a
     * Java exception, getScriptableValue may return a Scriptable
     * wrapping the original Java exception object.
     *
     * We would prefer to go through a getter to encapsulate the value,
     * however that causes the bizarre error "nanosecond timeout value
     * out of range" on the MS JVM.
     * @serial
     */
    Object value;
}
