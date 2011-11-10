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
package com.google.gdt.eclipse.designer.ie.util;

import java.text.MessageFormat;

import org.eclipse.core.runtime.AssertionFailedException;

/**
 * <code>Assert</code> is useful for for embedding runtime sanity checks in code. The predicate methods all
 * test a condition and throw some type of unchecked exception if the condition does not hold.
 * <p>
 * Assertion failure exceptions, like most runtime exceptions, are thrown when something is misbehaving.
 * Assertion failures are invariably unspecified behavior; consequently, clients should never rely on these
 * being thrown (and certainly should not being catching them specifically).
 * 
 * @author scheglov_ke
 * @coverage core.util
 */
public final class Assert {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private Assert() {
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// not "null"
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Asserts that the given object is not <code>null</code>. If this is not the case, some kind of unchecked
	 * exception is thrown.
	 * 
	 * @param object
	 *            the value to test
	 */
	public static void isNotNull(Object object) {
		isNotNull(object, ""); //$NON-NLS-1$
	}
	/**
	 * Asserts that the given object is not <code>null</code>. If this is not the case, some kind of unchecked
	 * exception is thrown. The given message is included in that exception, to aid debugging.
	 * 
	 * @param object
	 *            the value to test
	 * @param message
	 *            the message to include in the exception
	 */
	public static void isNotNull(Object object, String message) {
		if (object == null) {
			fail("null argument: " + message); //$NON-NLS-1$
		}
	}
	/**
	 * Asserts that the given object is not <code>null</code>. If this is not the case, some kind of unchecked
	 * exception is thrown. The given message is included in that exception, to aid debugging.
	 * 
	 * @param object
	 *            the value to test
	 * @param errorFormat
	 *            the format of error message to produce if the check fails
	 * @param args
	 *            the arguments for {@code errorFormat}
	 */
	public static void isNotNull(Object object, String errorFormat, Object... args) {
		if (object == null) {
			fail("null argument: " + String.format(errorFormat, args)); //$NON-NLS-1$
		}
	}
	public static void isNotNull2(Object object, String errorFormat, Object... args) {
		if (object == null) {
			String message = "null argument: " + MessageFormat.format(errorFormat, args); //$NON-NLS-1$
			fail(message);
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Fail
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Fails with given message.
	 * 
	 * @param message
	 *            the message to include in the exception
	 */
	public static void fail(String message) {
		throw new AssertionFailedException(message);
	}
}
