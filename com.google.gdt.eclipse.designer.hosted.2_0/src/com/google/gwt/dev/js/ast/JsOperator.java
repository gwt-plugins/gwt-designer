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
package com.google.gwt.dev.js.ast;

/**
 * A JavaScript operator.
 */
public interface JsOperator {
  int INFIX = 0x02;
  int LEFT = 0x01;
  int POSTFIX = 0x04;
  int PREFIX = 0x08;

  int getPrecedence();

  String getSymbol();

  boolean isKeyword();

  boolean isLeftAssociative();

  boolean isPrecedenceLessThan(JsOperator other);

  boolean isValidInfix();

  boolean isValidPostfix();

  boolean isValidPrefix();

  String toString();
}
