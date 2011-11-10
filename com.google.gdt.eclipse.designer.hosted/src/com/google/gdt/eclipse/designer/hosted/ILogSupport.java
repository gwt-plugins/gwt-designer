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
package com.google.gdt.eclipse.designer.hosted;

/**
 * Interface providing the access to GWT logger.
 * 
 * @author mitin_aa
 * @coverage gwtHosted
 */
public interface ILogSupport {
	/**
	 * Sets the type of messages to be logged.
	 */
	void setLogLevel(int logLevel);
	/**
	 * @return the TreeLogger instance.
	 */
	Object getLogger();
	/**
	 * @return error messages passed into GWT log.
	 */
	String getErrorMessages();
	/**
	 * Testing time environment property name
	 */
	public static final String WBP_TESTING_TIME = "wbp.testing.time";
}
