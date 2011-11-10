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

#include "Tracer.h"

#ifdef ENABLE_TRACING

// initialize static fields
FILE* Tracer::outfp = 0;
JNIEnv* Tracer::jniEnv = 0;
jclass Tracer::traceClass;
jmethodID Tracer::traceMethod;
int Tracer::indentation = 0;
Tracer::LogLevel Tracer::logLevel = Tracer::LEVEL_WARNING;

/*
 * Sets a JNI environment and Java class to pass trace messages to.
 * 
 * env - JNI environment to use for trace calls
 * clazz - Java class, which must provide static void trace(String)
 */
bool Tracer::setJava(JNIEnv* env, jclass clazz) {
  jniEnv = env;
  if (!env) {
    return true;
  }
  traceClass = static_cast<jclass>(env->NewGlobalRef(clazz));
  if (!traceClass || env->ExceptionCheck()) {
    return false;
  }
  traceMethod = env->GetStaticMethodID(traceClass, "trace",
    "(Ljava/lang/String;)V");
  if (!traceMethod || env->ExceptionCheck()) {
    return false;
  }

  jstring msg = jniEnv->NewStringUTF("== Java trace started ==");
  jniEnv->CallStaticVoidMethod(traceClass, traceMethod, msg);
  return true;
}

#endif // ENABLE_TRACING

/*
 * Throw a HostedModeException and log a failure message.
 * 
 * Creates a new HostedModeException with the failure message,
 * and also logs the failure message 
 * 
 * env - JNI environment to throw the exception in
 * msg - failure message 
 */
void Tracer::throwHostedModeException(JNIEnv* env, const char* msg) {
#ifdef ENABLE_TRACING
  setFail(msg);
#endif // ENABLE_TRACING
  jclass exceptionClass
      = env->FindClass("com/google/gwt/dev/shell/HostedModeException");
  env->ThrowNew(exceptionClass, msg);
}
