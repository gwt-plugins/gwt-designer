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
#define _GNU_SOURCE

#include <sys/types.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <gtk/gtk.h>
#include <gdk/gdkx.h>
#include <webkit/webkit.h>
#include <JavaScriptCore/JavaScript.h>
#include "gwt-jni-linux.h"
#include "utils.h"

JNIEnv* gEnv;
JavaVM* gJavaVM;
jclass c_BrowserShell;

jmethodID m_BrowserShell_windowScriptObjectAvailable;
jmethodID m_BrowserShell_doneLoading;
jmethodID m_BrowserShell_scriptAlert;

static void damage_event_cb(GtkWidget *widget, GdkEvent *event, gint *flag) {
	*flag = TRUE;
	g_signal_handlers_disconnect_by_func(widget, damage_event_cb, flag);
}

static gboolean load_error_cb(WebKitWebView  *web_view,
								WebKitWebFrame *web_frame,
								gchar          *uri,
								gpointer        web_error,
								gpointer        user_data) {
	GError *err = (GError*)web_error;
	jstring jmessage = (*gEnv)->NewStringUTF(gEnv, err->message);
	jobject callbackObject = (jobject)user_data;
	(*gEnv)->CallVoidMethod(gEnv, callbackObject, m_BrowserShell_doneLoading, err->code, jmessage);
	return TRUE;
}
static void onload_event_cb(GObject *webView, GParamSpec *pspec, gpointer user_data) {
	WebKitLoadStatus status;
	g_object_get(webView, "load-status", &status, NULL);
	if (WEBKIT_LOAD_FINISHED == status) {
		jobject callbackObject = (jobject)user_data;
		(*gEnv)->CallVoidMethod(gEnv, callbackObject, m_BrowserShell_doneLoading, 0, NULL);
	}
}
static void window_object_cleared_cb(WebKitWebView  *web_view,
                                WebKitWebFrame *frame,
                                gpointer        context,
                                gpointer        arg3,
                                gpointer        user_data) {
	JSGlobalContextRef jsContext = webkit_web_frame_get_global_context(frame);
	jobject callbackObject = (jobject)user_data;
	(*gEnv)->CallVoidMethod(gEnv, callbackObject, m_BrowserShell_windowScriptObjectAvailable, wrap_pointer(gEnv, jsContext));
}

static gboolean web_view_alert_cb(WebKitWebView* web_view, WebKitWebFrame* frame, gchar* message, gpointer user_data) 
{
	jobject jCallbackObject = (jobject)user_data;
	jstring jAlertString = (*gEnv)->NewStringUTF(gEnv, message);
	(*gEnv)->CallVoidMethod(gEnv, jCallbackObject, m_BrowserShell_scriptAlert, jAlertString);
	return TRUE;
}

static WebKitWebView* web_inspector_create_win_cb(WebKitWebInspector *inspector, WebKitWebView *view, gpointer obj)
{
	// create window for web inspector
	GtkWidget* win = gtk_window_new(GTK_WINDOW_TOPLEVEL);
 	gtk_window_set_default_size(GTK_WINDOW(win), 800, 600);
	GtkWidget *scroll = gtk_scrolled_window_new (NULL, NULL);
	gtk_container_add(GTK_CONTAINER(win), scroll);
	GtkWidget *page = webkit_web_view_new();
	gtk_container_add(GTK_CONTAINER(scroll), page);
	// center on the screen & show
	gint screen_width = gdk_screen_width();
	gint screen_height = gdk_screen_height();
	gint x = screen_width / 2 - 400;
	gint y = screen_height / 2 - 300;
	gtk_window_move(GTK_WINDOW(win), x, y);
	gtk_widget_show_all(win);
 	// done
	return WEBKIT_WEB_VIEW(page);
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1init)
	(JNIEnv *env, jclass clazz, jclass llClass)
{
	gEnv = env;
	(*env)->GetJavaVM(env, &gJavaVM);
	//
	c_BrowserShell =  (*env)->NewGlobalRef(env, llClass);
	if (!gJavaVM || !c_BrowserShell || (*env)->ExceptionCheck(env)) {
		return JNI_FALSE;
	}
	
	m_BrowserShell_windowScriptObjectAvailable = (*env)->GetMethodID(env, c_BrowserShell, "windowScriptObjectAvailable", "(Ljava/lang/Number;)V");
	m_BrowserShell_doneLoading = (*env)->GetMethodID(env, c_BrowserShell, "doneLoading", "(ILjava/lang/String;)V");
	m_BrowserShell_scriptAlert = (*env)->GetMethodID(env, c_BrowserShell, "scriptAlert", "(Ljava/lang/String;)V");

	if (!m_BrowserShell_windowScriptObjectAvailable ||
		!m_BrowserShell_doneLoading ||
		!m_BrowserShell_scriptAlert ||
		(*env)->ExceptionCheck(env)) {
		return JNI_FALSE;
	}
	
	return JNI_TRUE;
}

JNIEXPORT jobject JNICALL OS_NATIVE(_1create)
	(JNIEnv *env, jclass jclazz, jobject jcallback)
{
	// store ref to callback
	jobject callbackObject = (*env)->NewGlobalRef(env, jcallback);
	// browser
	WebKitWebView* web_view = WEBKIT_WEB_VIEW(webkit_web_view_new());
	g_signal_connect(G_OBJECT(web_view), "window-object-cleared", G_CALLBACK(window_object_cleared_cb), callbackObject);
	g_signal_connect(G_OBJECT(web_view), "load-error", G_CALLBACK(load_error_cb), callbackObject);
	g_signal_connect(G_OBJECT(web_view), "notify::load-status", G_CALLBACK(onload_event_cb), callbackObject);
	g_signal_connect(G_OBJECT(web_view), "script-alert", G_CALLBACK(web_view_alert_cb), callbackObject);
	// web inspector
	WebKitWebSettings* settings = webkit_web_settings_new();
	g_object_set (G_OBJECT(settings), "enable-developer-extras", TRUE, NULL);
	webkit_web_view_set_settings(web_view, settings);
	WebKitWebInspector* inspector = webkit_web_view_get_inspector(web_view);
	g_signal_connect(G_OBJECT(inspector), "inspect-web-view", G_CALLBACK(web_inspector_create_win_cb), NULL);
	// give it an initial size, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=392967
	gtk_widget_set_size_request(GTK_WIDGET(web_view), 2, 2);
	// vbox
	GtkWidget* vbox = gtk_vbox_new(FALSE, 0);
	gtk_box_pack_start(GTK_BOX(vbox), GTK_WIDGET(web_view), TRUE, TRUE, 0);
	// main window
	GtkWidget* main_window = gtk_offscreen_window_new();
	gtk_container_add(GTK_CONTAINER(main_window), vbox);
	gtk_widget_realize(main_window);
	if (main_window->window == NULL) {
		// something goes wrong
		return NULL;
	}
	gtk_widget_show_all(GTK_WIDGET(main_window));
	// store web_view for fast access
	GQuark q1 = g_quark_from_string("__wbp_webview_key");
	g_object_set_qdata(G_OBJECT(main_window), q1, web_view);
	// store callback for fast access
	GQuark q2 = g_quark_from_string("__wbp_browserShell_key");
	g_object_set_qdata(G_OBJECT(main_window), q2, callbackObject);
	// store vbox for fast access
	GQuark q3 = g_quark_from_string("__wbp_vbox_key");
	g_object_set_qdata(G_OBJECT(main_window), q3, vbox);
	
	return wrap_pointer(env, main_window);
}

JNIEXPORT void JNICALL OS_NATIVE(_1release)
	(JNIEnv *env, jclass that, jobject jwnd)
{
	GtkWindow* window = GTK_WINDOW(unwrap_pointer(env, jwnd));
	if (window == NULL) {
		return;
	}
	// delete callback ref
	GQuark q2 = g_quark_from_string("__wbp_browserShell_key");
	jobject	callbackObject = g_object_get_qdata(G_OBJECT(window), q2);
	(*env)->DeleteGlobalRef(env, callbackObject);
	// destroy window
	gtk_widget_destroy(GTK_WIDGET(window));
}

static void redraw_offscreen_window(GtkOffscreenWindow* window) {
	// setup waiting for window to redraw.
	gint flag = FALSE;
    g_signal_connect (window, "damage-event", G_CALLBACK(damage_event_cb), &flag);
	// ensure underlying GdkWindow	
	GtkWidget* widget = GTK_WIDGET(window);
	if (!gtk_widget_get_realized(widget)) {
		gtk_widget_realize(widget);
	}
	// create region to update
	GdkWindow* gdk_window = widget->window;
	gint width, height;
	gdk_window_get_geometry(gdk_window, NULL, NULL, &width, &height, NULL);
	// begin paint
	GdkRectangle rect;
	rect.x = 0;	rect.y = 0;	rect.width = width;	rect.height = height;
	GdkRegion *region = gdk_region_rectangle(&rect);
	gdk_window_begin_paint_region(gdk_window, region);
	// invalidate
	region = gdk_region_rectangle(&rect);
	gdk_window_invalidate_region(gdk_window, region, TRUE);
	// wait for GdkEvents to get passed
	gdk_window_process_updates(gdk_window, TRUE);
	// end paint
	gdk_window_end_paint(gdk_window);
	// wait for window to redraw.
    while (!flag) {
		gtk_main_iteration();
	}
}

static void offscreen_window_set_size(GtkOffscreenWindow* window, gint width, gint height) {
	// size as vbox child size
	GQuark q3 = g_quark_from_string("__wbp_vbox_key");
	GtkWidget* vbox = g_object_get_qdata(G_OBJECT(window), q3);
	gtk_widget_set_size_request(vbox, width, height);
	redraw_offscreen_window(window);
}

JNIEXPORT void JNICALL OS_NATIVE(_1setBounds)
	(JNIEnv *env, jclass that, jobject jwnd, jint x, jint y, jint width, jint height)
{
	GtkOffscreenWindow* window = GTK_OFFSCREEN_WINDOW(unwrap_pointer(env, jwnd));
	offscreen_window_set_size(window, width, height);
}

JNIEXPORT void JNICALL OS_NATIVE(_1getBounds)
	(JNIEnv *env, jclass that, jobject jwnd, jshortArray jbounds)
{
	GtkWindow* window = GTK_WINDOW(unwrap_pointer(env, jwnd));

	jshort values[4];
	// position
	gint x, y;
	gtk_window_get_position(window, &x, &y);
	values[0] = x;
	values[1] = y;
	// size as vbox child size
	GQuark q3 = g_quark_from_string("__wbp_vbox_key");
	GtkWidget* vbox = g_object_get_qdata(G_OBJECT(window), q3);
	values[2] = GTK_WIDGET_WIDTH(vbox);
	values[3] = GTK_WIDGET_HEIGHT(vbox);
	(*env)->SetShortArrayRegion(env, jbounds, 0, 4, values);
}

JNIEXPORT void JNICALL OS_NATIVE(_1computeTrim)
	(JNIEnv *env, jclass that, jobject jwnd, jshortArray jbounds)
{
	jshort inValues[4];
	(*env)->GetShortArrayRegion(env, jbounds, 0, 4, inValues);
	// just return it back because no decoractions used
	(*env)->SetShortArrayRegion(env, jbounds, 0, 4, inValues);
}

JNIEXPORT void JNICALL OS_NATIVE(_1setVisible)
	(JNIEnv *env, jclass that, jobject jwnd, jboolean jvisible)
{
	GtkWindow* window = GTK_WINDOW(unwrap_pointer(env, jwnd));
	if (jvisible == JNI_TRUE) {
		gtk_widget_show_all(GTK_WIDGET(window));
	} else {
		gtk_widget_hide(GTK_WIDGET(window));
	}
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1isVisible)
	(JNIEnv* env, jclass that, jobject jwnd)
{
	GtkWindow* window = GTK_WINDOW(unwrap_pointer(env, jwnd));
	return GTK_WIDGET_VISIBLE(window) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL OS_NATIVE(_1setUrl)
	(JNIEnv* env, jclass that, jobject jwnd, jstring jlocation)
{
	// get webView from window
	GtkWindow* window = GTK_WINDOW(unwrap_pointer(env, jwnd));
	GQuark q = g_quark_from_string("__wbp_webview_key");
	WebKitWebView* web_view = g_object_get_qdata(G_OBJECT(window), q);
	// set url
	const char* location = (*env)->GetStringUTFChars(env, jlocation, NULL);
	webkit_web_view_open(web_view, location);
	(*env)->ReleaseStringUTFChars(env, jlocation, location);
}

JNIEXPORT jobject JNICALL OS_NATIVE(_1makeShot)
	(JNIEnv *env, jclass jclazz, jobject jwnd)
{
	GtkOffscreenWindow* window = GTK_OFFSCREEN_WINDOW(unwrap_pointer(env, jwnd));
	redraw_offscreen_window(window);
	GdkPixmap* pixmap = gtk_offscreen_window_get_pixmap(window);
	// we own this pointer now
	g_object_ref(pixmap);
	return wrap_pointer(env, pixmap);
}

static gboolean preview_delete_event_cb(GtkWidget *widget, GdkEvent  *event, gpointer data) {
	// get offscreen window
	GtkWidget* window = GTK_WIDGET(data);
	// get old web view parent
	GQuark q1 = g_quark_from_string("__wbp_vbox_key");
	GtkWidget* vbox = g_object_get_qdata(G_OBJECT(window), q1);
	// get web view and add ref to it
	GQuark q2 = g_quark_from_string("__wbp_webview_key");
	GtkWidget* web_view = g_object_get_qdata(G_OBJECT(window), q2);
	g_object_ref(web_view);
	// unparent web view and parent it back to the vbox on the offscreen window
	GtkWidget* old_parent = gtk_widget_get_parent(web_view);
	gtk_container_remove(GTK_CONTAINER(old_parent), web_view);
	gtk_container_add(GTK_CONTAINER(vbox), web_view);
	// unref web view
	g_object_unref(web_view);
	// let window manager kill the preview window
	return FALSE;
}

JNIEXPORT jobject JNICALL OS_NATIVE(_1showAsPreview)
	(JNIEnv *envir, jobject that, jobject jwnd)
{
	GtkWidget* offscreen_window = GTK_WIDGET(unwrap_pointer(envir, jwnd));
	// create new window and hook it's delete event (when user clicks 'close' button)
	GtkWidget* window = gtk_window_new(GTK_WINDOW_TOPLEVEL);
	g_signal_connect(G_OBJECT(window), "delete-event", G_CALLBACK(preview_delete_event_cb), offscreen_window);
	// get web view and add ref to it
	GQuark q3 = g_quark_from_string("__wbp_webview_key");
	GtkWidget* web_view = g_object_get_qdata(G_OBJECT(offscreen_window), q3);
	g_object_ref(web_view);
	// unparent web view & store it bounds for later usage
	GtkWidget* old_parent = gtk_widget_get_parent(web_view);
	gint window_width = GTK_WIDGET_WIDTH(old_parent);
	gint window_height = GTK_WIDGET_HEIGHT(old_parent);
	gint web_view_width = GTK_WIDGET_WIDTH(web_view);
	gint web_view_height = GTK_WIDGET_HEIGHT(web_view);
	gtk_container_remove(GTK_CONTAINER(old_parent), web_view);
	// create vbox, add web view to it and then put it onto top-level window
	GtkWidget* vbox = gtk_vbox_new(FALSE, 0);
	gtk_box_pack_start(GTK_BOX(vbox), GTK_WIDGET(web_view), TRUE, TRUE, 0);
	gtk_container_add(GTK_CONTAINER(window), vbox);
	gtk_widget_set_size_request(web_view, web_view_width, web_view_height);
	// unref our ref
	g_object_unref(web_view);
	// center on the screen
	gint screen_width = gdk_screen_width();
	gint screen_height = gdk_screen_height();
	gint x = screen_width / 2 - window_width / 2;
	gint y = screen_height / 2 - window_height / 2;
	// show the preview window
	gtk_window_move(GTK_WINDOW(window), x, y);
	gtk_widget_show_all(GTK_WIDGET(window));
	// return pointer to the window to be checked for visibility
	return wrap_pointer(envir, window);
}

static gboolean readFileAndCompare(FILE* file)
{
	int bufSize = 1024;
	int bc = 0;
	char* buffer = (char*)malloc(bufSize);
	// read until eof, because ftell() returns 0
	while(!feof(file)) {
		int br = fread(buffer + bc, 1, 1024, file);
		bc += br;
		if (bc >= bufSize) {
			int oldBufSize = bufSize;
			bufSize *= 2;
			char* newBuffer = (char*)malloc(bufSize);
			memcpy(newBuffer, buffer, oldBufSize);
			free((void*)buffer);
			buffer = newBuffer;
		}
	}
	void* result = memmem(buffer, bufSize, "GDK_NATIVE_WINDOWS", 18);
	free(buffer);
	fclose(file);
	// if set, the result is not null
	return result != NULL;
}
static gboolean isGdkNativeWindowsSet()
{
	// see http://forums.instantiations.com/viewtopic.php?f=11&t=5288&start=15
	// additionally, GDK unsets "GDK_NATIVE_WINDOWS" env variable once in detected 
	// for some reason (getenv() returns NULL), so use this tricky way.
	pid_t pid = getpid();
	char fileName[4096];
	memset(fileName, 0, sizeof(fileName));
	// read '/proc/<pid>/environ' file
	sprintf(fileName, "/proc/%d/environ", pid);
	FILE* envFile = fopen(fileName, "rb");
	if (envFile == NULL) {
		return FALSE;
	}
	// read file contents into buffer and compare
	return readFileAndCompare(envFile);
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1isAvailable)
	(JNIEnv *env, jclass that)
{
	// check for WebKit version
	int major = webkit_major_version();
	int minor = webkit_minor_version();
	int micro = webkit_micro_version();
	// g_print("WebKitGTK version: %d.%d.%d\n", major, minor, micro);
	gboolean success1 = major > 1 ||
			(major == 1 && minor > 2) ||
			(major == 1 && minor == 2 && micro >= 0);
	// check for GTK version
	major = gtk_major_version;
	minor = gtk_minor_version;
	micro = gtk_micro_version;
	// g_print("GTK version: %d.%d.%d\n", major, minor, micro);
	gboolean success2 = major > 2 ||
			(major == 2 && minor > 20) ||
			(major == 2 && minor == 20 && micro >= 0);
	// check for GDK_NATIVE_WINDOWS
	gboolean isGdkNative = isGdkNativeWindowsSet();
	if (isGdkNative) {
		g_print("'GDK_NATIVE_WINDOWS' set, won't use WebKit.\n");
		return JNI_FALSE;
	}
	return (success1 && success2) ? JNI_TRUE : JNI_FALSE;
}

// unref
JNIEXPORT void JNICALL OS_NATIVE(_1g_1object_1unref)
	(JNIEnv *envir, jobject that, jobject jhandle) 
{
	g_object_unref((GObject*)unwrap_pointer(envir, jhandle));
}

