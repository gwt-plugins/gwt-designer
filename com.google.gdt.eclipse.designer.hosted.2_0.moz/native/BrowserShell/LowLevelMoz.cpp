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

// Mozilla-specific hosted-mode methods

//#define DEBUG

#include <cstdio>

// JS Context Stack
static const char sJSStackContractID[] = "@mozilla.org/js/xpc/ContextStack;1"; 

// Mozilla header files
#include "mozilla-headers.h"

#include <jni.h>
#include "gwt-jni.h"
#include "js-classes.h"
#include "JStringWrap.h"
#include "JsRootedValue.h"
#include "Tracer.h"
#include "JsStringWrap.h"
#include "ScriptObjectProxy.h"

//#define FILETRACE
//#define JAVATRACE

JNIEnv* savedJNIEnv = 0;
jclass lowLevelMozClass;

// mitin_aa: JS context stack service. Warning: nsIThreadJSContextStack interface is not frozen, but 
// it wasn't changed since Mozilla 1.7.12 and it is the same in trunk (05.10.2007).
nsCOMPtr<nsIThreadJSContextStack>  g_contextStack;

static void pushContext(JSContext *cx) {
  if (g_contextStack != nsnull) {
    g_contextStack->Push(cx);
  }
}
static void popContext() {
  if (g_contextStack != nsnull) {
    g_contextStack->Pop(nsnull);
  }
}

// Only include debugging code if we are tracing somewhere.
#ifdef ANYTRACE
/*
 * Template so vsnprintf/vswprintf can be used interchangeably in the
 * append_sprintf template below.
 *   buf - pointer to the start of the output buffer
 *   len - maximum number of characters to write into the buffer
 *         (including the null terminator)
 *   fmt - printf-style format string
 *   args - stdarg-style variable arguments list
 *  Returns the number of characters written (excluding the null terminator)
 *   or -1 if an error occurred.
 * 
 * Note that %lc and %ls are only legal in the wchar_t implementation.
 */
template<class charT>
int safe_vsprintf(charT* buf, size_t len, const charT* fmt, va_list args); 

// specialization for char that maps to vsnprintf
template<>
inline int safe_vsprintf<char>(char* buf, size_t len, const char* fmt,
    va_list args) {
  return ::vsnprintf(buf, len, fmt, args);
}

// specialization for wchar_t that maps to vswprintf
template<>
inline int safe_vsprintf<wchar_t>(wchar_t* buf, size_t len, const wchar_t* fmt,
    va_list args) {
  return ::vswprintf(buf, len, fmt, args);
}

/*
 * Safely append to a string buffer, updating the output pointer and always
 * reserving the last character of the buffer for a null terminator.
 *   bufStart - pointer to the start of the output buffer
 *   bufEnd - pointer just past the end of the output buffer
 *   fmt - format string
 *   additional arguments as passed to *printf
 * Returns the number of characters actually written, not including the null
 * terminator.  Nothing is written, including the null terminator, if the
 * buffer start points beyond the output buffer.
 *
 * Templated to work with any character type that has a safe_vsprintf
 * implementation.
 */
template<class charT>
static int append_sprintf(charT* bufStart, const charT* bufEnd,
    const charT* fmt, ...) {
  va_list args;
  va_start(args, fmt); // initialize variable arguments list
  // compute space left in buffer: -1 for null terminator
  int maxlen = bufEnd - bufStart - 1;
  if (maxlen <= 0) return 0;
  int n = safe_vsprintf(bufStart, maxlen, fmt, args);
  va_end(args);
  if (n > maxlen) {
    n = maxlen;
  }
  bufStart[n] = 0;
  return n;
}

/*
 * Log a given jsval with a prefix.
 *  cx - JSContext for the JS execution context to use
 *  val - jsval to print
 *  prefix - string to print before the value, defaults to empty string
 *
 * TODO(jat): this whole printf-style logging needs to be replaced, but we
 * run into library version issues if we use C++ iostreams so we would need
 * to implement our own equivalent.  Given that this code is all likely to
 * be rewritten for out-of-process hosted mode, it seems unlikely to be worth
 * the effort until that is completed.
 */
static void PrintJSValue(JSContext* cx, jsval val, char* prefix="") {
  JSType type = JS_TypeOfValue(cx, val);
  const char* typeString=JS_GetTypeName(cx, type);
  static const int BUF_SIZE = 256;
  char buf[BUF_SIZE];
  const char *bufEnd = buf + BUF_SIZE;
  char* p = buf;
  p += append_sprintf(p, bufEnd, "%s%s", prefix, typeString);
  switch(type) {
    case JSTYPE_VOID:
      break;
    case JSTYPE_BOOLEAN:
      p += append_sprintf(p, bufEnd, ": %s",
          JSVAL_TO_BOOLEAN(val) ? "true" : "false");
      break;
    case JSTYPE_NUMBER:
      if (JSVAL_IS_INT(val)) {
        p += append_sprintf(p, bufEnd, ": %d", JSVAL_TO_INT(val));
      } else {
        p += append_sprintf(p, bufEnd, ": %lf", (double)*JSVAL_TO_DOUBLE(val));
      }
      break;
    case JSTYPE_OBJECT: {
      JSObject* obj = JSVAL_TO_OBJECT(val);
      if (!JSVAL_IS_OBJECT(val)) break;
      JSClass* clazz = obj ? JS_GET_CLASS(cx, obj) : 0;
      p += append_sprintf(p, bufEnd, " @ %08x, class %s",
          (unsigned)obj, clazz ? clazz->name : "<null>");
      break;
    }
    case JSTYPE_FUNCTION:
    case JSTYPE_LIMIT:
      break;
    case JSTYPE_STRING: {
      /*
       * TODO(jat): support JS strings with international characters
       */
      JsStringWrap str(cx, JSVAL_TO_STRING(val));
      p += append_sprintf(p, bufEnd, ": %.*s", str.length(), str.bytes());
      break;
    }
  }
  Tracer::log("%s", buf);
}
#else
// Include a null version just to keep from cluttering up call sites.
static inline void PrintJSValue(JSContext* cx, jsval val, char* prefix="") { }
#endif


static bool InitGlobals(JNIEnv* env, jclass llClass) {
  if (savedJNIEnv)
    return false;

#ifdef FILETRACE
  Tracer::setFile("/home/mitin_aa/gwt-ll.log");
#endif // FILETRACE

#ifdef JAVATRACE
  Tracer::setJava(env, llClass);
#endif // JAVATRACE

#ifdef DEBUG
  Tracer::setLevel(Tracer::LEVEL_DEBUG);
#endif

  savedJNIEnv = env;
  lowLevelMozClass = static_cast<jclass>(env->NewGlobalRef(llClass));

  // init JS context service.
  g_contextStack = do_GetService(sJSStackContractID);

  JSContext *safeJSContext = NULL;
  nsresult rv = g_contextStack->GetSafeJSContext(&safeJSContext);
  if (NS_FAILED(rv)) {
    fprintf(stderr, "Can't get the the safe JSContext instance.\n");fflush(stderr);
    return false;
  }

  JsRootedValue::ensureRuntime(safeJSContext);

  return true;
}

/*
 * Class:     com_google_gwt_dev_shell_moz_LowLevelMoz
 * Method:    _executeScriptWithInfo
 * Signature: (ILjava/lang/String;Ljava/lang/String;I)Z
 */
extern "C" JNIEXPORT jboolean JNICALL
OS_NATIVE_LL(_1executeScriptWithInfo)
    (JNIEnv* env, jclass llClass, JHANDLE scriptObjInt, jstring code,
     jstring file, jint line)
{
  Tracer tracer("LowLevelMoz._executeScriptWithInfo");
  JStringWrap jcode(env, code);
  if (!jcode.jstr()) {
    tracer.setFail("null code string");
    return JNI_FALSE;
  }
  JStringWrap jfile(env, file);
  if (!jfile.str()) {
    tracer.setFail("null file name");
    return JNI_FALSE;
  }
  tracer.log("code=%s, file=%s, line=%d", jcode.str(), jfile.str(), line);
    ScriptObjectProxy *proxy = reinterpret_cast<ScriptObjectProxy*>(scriptObjInt);
	if (NS_FAILED(proxy->EvaluateString(jcode.jstr()))) {
		tracer.setFail("EvaluateString failed");                                                                                                                                       
		return JNI_FALSE;
	}
	return JNI_TRUE;
}

/*
 * Class:     com_google_gwt_dev_shell_moz_LowLevelMoz
 * Method:    _invoke
 * Signature: (ILjava/lang/String;I[I)I
 */
extern "C" JNIEXPORT jboolean JNICALL
OS_NATIVE_LL(_1invoke)
    (JNIEnv* env, jclass, JHANDLE scriptObjInt, jstring methodName, JHANDLE jsThisInt,
#ifdef WBP_ARCH64
     jlongArray jsArgsInt, 
#else
     jintArray jsArgsInt, 
#endif
JHANDLE jsRetValInt)
{
  Tracer tracer("LowLevelMoz._invoke");

  JStringWrap methodStr(env, methodName);
  if (!methodStr.str()) {
    tracer.setFail("null method name");
    return JNI_FALSE;
  }
  JsRootedValue* jsThisRV = reinterpret_cast<JsRootedValue*>(jsThisInt);
  jint jsArgc = env->GetArrayLength(jsArgsInt);
  tracer.log("method=%s, jsthis=%08x, #args=%d", methodStr.str(), jsThisInt,
     jsArgc);
  JSContext* cx = JsRootedValue::currentContext();
  ScriptObjectProxy *proxy = reinterpret_cast<ScriptObjectProxy*>(scriptObjInt);
  JSObject* scriptWindow = proxy->GetGlobalJSObject();
  
  jsval fval;
  if (!JS_GetProperty(cx, scriptWindow, methodStr.str(), &fval)) {
    tracer.setFail("JS_GetProperty(method) failed");
    return JNI_FALSE;
  }
  JSFunction* jsFunction = JS_ValueToFunction(cx, fval);
  if (!jsFunction) {
    tracer.setFail("JS_ValueToFunction failed");
    return JNI_FALSE;
  }
  
  // extract arguments in jsval form
  nsAutoArrayPtr<JHANDLE> jsargvals(new JHANDLE[jsArgc]);
  if (!jsargvals) {
    tracer.setFail("failed to allocate arg array");
    return JNI_FALSE;
  }
#ifdef WBP_ARCH64
  env->GetLongArrayRegion(jsArgsInt, 0, jsArgc, jsargvals);
#else
  env->GetIntArrayRegion(jsArgsInt, 0, jsArgc, jsargvals);
#endif
  jthrowable exc = env->ExceptionOccurred();
  if (exc != NULL) {
    tracer.setFail("copy from Java array failed");
    env->DeleteLocalRef(exc);
    return JNI_FALSE;
  }
  nsAutoArrayPtr<jsval> jsargs(new jsval[jsArgc]);
  for (int i = 0; i < jsArgc; ++i) {
    JsRootedValue* arg = reinterpret_cast<JsRootedValue*>(jsargvals[i]);
    jsargs[i] = arg->getValue();
  }

  jsval jsrval;
  JSObject* jsThis;
  if (jsThisRV->isNull()) {
    jsThis = scriptWindow;
  } else {
    jsThis = jsThisRV->getObject();
  }
  
  PrintJSValue(cx, OBJECT_TO_JSVAL(jsThis), "jsThis=");
  for (int i = 0; i < jsArgc; ++i) {
    char buf[4096];
    snprintf(buf, sizeof(buf), "arg[%d]=", i);
    PrintJSValue(cx, jsargs[i], buf);
  }
  //tracer.log("fval = %08x, args=%08x", fval, jsargs.get());
  // printf(">>: cx = %p go = %p fval = %p jsThis = %p: %s\n" , cx, scriptWindow, (void*)fval, jsThis, methodStr.str());fflush(stdout);

  // mitin_aa: feature in Mozilla: calling creation of some HTML DOM objects (ex. "var a = new Option();", 
  // see nsHTMLOptionElement.cpp, NS_NewHTMLOptionElement function) uses ContextStack service to get current 
  // js context (see nsContentUtils::GetDocumentFromCaller). If we call JS function directly we should add 
  // the current js context to stack in ContextStack service.
  // I don't know why it works for GWT guys, maybe because of they own Mozilla distribution, v.1.7.12 have 
  // slightly different implementation of mentioned methods.
  pushContext(cx);
  if (!JS_CallFunctionValue(cx, jsThis, fval, jsArgc, jsargs.get(), &jsrval)) {
    tracer.setFail("JS_CallFunctionValue failed");
    popContext();
    return JNI_FALSE;
  }
  popContext();

  PrintJSValue(cx, jsrval, "return value=");
  JsRootedValue* returnVal = reinterpret_cast<JsRootedValue*>(jsRetValInt);
  returnVal->setValue(jsrval);
  return JNI_TRUE;
}


/*
 * Class:     com_google_gwt_dev_shell_moz_LowLevelMoz
 * Method:    _raiseJavaScriptException
 * Signature: ()Z
 */
extern "C" JNIEXPORT jboolean JNICALL
Java_com_google_gwt_dev_shell_moz_LowLevelMoz__1raiseJavaScriptException // *not using OS_NATIVE macro
    (JNIEnv* env, jclass)
{
  Tracer tracer("LowLevelMoz._raiseJavaScriptException");
  JS_SetPendingException(JsRootedValue::currentContext(), JSVAL_NULL);
  return JNI_TRUE;
}

/*
 * Class:     com_google_gwt_dev_shell_moz_LowLevelMoz
 * Method:    _registerExternalFactoryHandler
 * Signature: ()Z
 */
/*extern "C" JNIEXPORT jboolean JNICALL
Java_com_google_gwt_dev_shell_moz_LowLevelMoz__1registerExternalFactoryHandler
    (JNIEnv* env, jclass llClass)
{
  if (!InitGlobals(env, llClass))
    return JNI_FALSE;

  // tracing isn't setup until after InitGlobals is called
  Tracer tracer("LowLevelMoz._registerExternalFactoryHandler");

  char buf[256];
  sprintf(buf, " jniEnv=%08x, llClass=%08x", (unsigned)env, (unsigned)llClass);
  tracer.log(buf);
  
  // Register "window.external" as our own class
  if (NS_FAILED(nsComponentManager::RegisterFactory(
      kGwtExternalCID, "externalFactory", GWT_EXTERNAL_CONTRACTID,
      new nsRpExternalFactory(), PR_TRUE))) {
    tracer.setFail("RegisterFactory failed");
    return JNI_FALSE;
  }

  nsCOMPtr<nsICategoryManager> categoryManager =
      do_GetService(NS_CATEGORYMANAGER_CONTRACTID);
  if (!categoryManager) {
    tracer.setFail("unable to get category manager");
    return JNI_FALSE;
  }

  nsXPIDLCString previous;
  if (NS_FAILED(categoryManager->AddCategoryEntry(
      JAVASCRIPT_GLOBAL_PROPERTY_CATEGORY, "external", GWT_EXTERNAL_CONTRACTID,
      PR_TRUE, PR_TRUE, getter_Copies(previous)))) {
    tracer.setFail("AddCategoryEntry failed");
    return JNI_FALSE;
  }

  return JNI_TRUE;
}*/

extern "C" JNIEXPORT JHANDLE JNICALL
OS_NATIVE_LL(_1getScriptObjectProxy)(JNIEnv* env, jclass llClass, JHANDLE jdomWindow) {

  	InitGlobals(env, llClass);

	nsIDOMWindow *domWindow = reinterpret_cast<nsIDOMWindow*>(jdomWindow);
	ScriptObjectProxy *proxy = ScriptObjectProxy::GetScriptObjectProxy(domWindow);
/*	if (proxy != NULL) {
	    JsRootedValue::ensureRuntime(proxy->GetNativeContext());
	}*/
	return (JHANDLE)proxy;
}
extern "C" JNIEXPORT void JNICALL
OS_NATIVE_LL(_1releaseScriptObjectProxy)(JNIEnv* env, jclass llClass, JHANDLE jdomWindow) {
	ScriptObjectProxy *proxy = reinterpret_cast<ScriptObjectProxy *>(jdomWindow);
	delete proxy;
}
