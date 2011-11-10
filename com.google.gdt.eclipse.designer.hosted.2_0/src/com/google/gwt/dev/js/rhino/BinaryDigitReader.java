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

package com.google.gwt.dev.js.rhino;

final class BinaryDigitReader {
    int lgBase;         // Logarithm of base of number
    int digit;          // Current digit value in radix given by base
    int digitPos;       // Bit position of last bit extracted from digit
    String digits;      // String containing the digits
    int start;          // Index of the first remaining digit
    int end;            // Index past the last remaining digit

    BinaryDigitReader(int base, String digits, int start, int end) {
        lgBase = 0;
        while (base != 1) {
            lgBase++;
            base >>= 1;
        }
        digitPos = 0;
        this.digits = digits;
        this.start = start;
        this.end = end;
    }

    /* Return the next binary digit from the number or -1 if done */
    int getNextBinaryDigit()
    {
        if (digitPos == 0) {
            if (start == end)
                return -1;

            char c = digits.charAt(start++);
            if ('0' <= c && c <= '9')
                digit = c - '0';
            else if ('a' <= c && c <= 'z')
                digit = c - 'a' + 10;
            else digit = c - 'A' + 10;
            digitPos = lgBase;
        }
        return digit >> --digitPos & 1;
    }
}
