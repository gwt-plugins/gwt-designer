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

#ifndef JNI_LINUX_JSSTRINGWRAP_H_
#define JNI_LINUX_JSSTRINGWRAP_H_

/*
 * Wrapper arouond JavaScript Strings, keeps pointers to unpacked strings
 * and makes sure they are not cleaned up early. 
 */
class JsStringWrap {
  private:
    JSContext* context_;
    JSString* string_;
    const char* bytes_;
    const wchar_t* chars_;

  public:
    JsStringWrap(JSContext* context, JSString* str)
        : context_(context), string_(str), bytes_(0), chars_(0) {
      JS_AddRoot(context_, &string_);
      JS_AddRoot(context_, &bytes_);
      JS_AddRoot(context_, &chars_);
    }
    JsStringWrap(JSContext* context, jsval str)
        : context_(context), string_(JSVAL_TO_STRING(str)), bytes_(0), chars_(0) {
      JS_AddRoot(context_, &string_);
      JS_AddRoot(context_, &bytes_);
      JS_AddRoot(context_, &chars_);
    }
    ~JsStringWrap() {
      JS_RemoveRoot(context_, &string_);
      JS_RemoveRoot(context_, &bytes_);
      JS_RemoveRoot(context_, &chars_);
    }
    const char* bytes() {
      if (!bytes_) bytes_ = JS_GetStringBytes(string_);
      return bytes_;
    }
    const wchar_t* chars() {
      if (!chars_) {
        chars_ = reinterpret_cast<wchar_t*>(JS_GetStringChars(string_));
      }
      return chars_;
    }
    int length() {
      return JS_GetStringLength(string_);
    }
};

#endif // JNI_LINUX_JSSTRINGWRAP_H_
