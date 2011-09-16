/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include "gwt-jni.h"
#include <gtk/gtk.h>
#include <gdk/gdkx.h>
#include <stdlib.h>
#include <string.h>

#include <X11/Xatom.h>

#define _NET_WM_STATE_TOGGLE        2    /* toggle property  */
#define MAX_PROPERTY_VALUE_LEN 4096

////////////////////////////////////////////////////////////////////////////
//
// Toggle "above" state for Eclipse, move preview window to another workspace
//
////////////////////////////////////////////////////////////////////////////
int m_currentDesktop = 0;
int m_desktopCount = 0;

// gets the property of the X Window
static gchar *get_property (Display *disp, Window win, Atom xa_prop_type, gchar *prop_name, unsigned long *size) {
    Atom xa_prop_name;
    Atom xa_ret_type;
    int ret_format;
    unsigned long ret_nitems;
    unsigned long ret_bytes_after;
    unsigned long tmp_size;
    unsigned char *ret_prop;
    gchar *ret;
    
    xa_prop_name = XInternAtom(disp, prop_name, False);
    
    /* MAX_PROPERTY_VALUE_LEN / 4 explanation (XGetWindowProperty manpage):
     *
     * long_length = Specifies the length in 32-bit multiples of the
     *               data to be retrieved.
     */
    if (XGetWindowProperty(disp, win, xa_prop_name, 0, MAX_PROPERTY_VALUE_LEN / 4, False,
            xa_prop_type, &xa_ret_type, &ret_format,     
            &ret_nitems, &ret_bytes_after, &ret_prop) != Success) {
        return NULL;
    }
  
    if (xa_ret_type != xa_prop_type) {
        XFree(ret_prop);
        return NULL;
    }

    /* null terminate the result to make string handling easier */
    tmp_size = (ret_format / 8) * ret_nitems;
    ret = g_malloc(tmp_size + 1);
    memcpy(ret, ret_prop, tmp_size);
    ret[tmp_size] = '\0';

    if (size) {
        *size = tmp_size;
    }
    
    XFree(ret_prop);
    return ret;
}
// sends the client message to the X Window
static int client_msg(Display *disp, Window win, char *msg,
        unsigned long data0, unsigned long data1, 
        unsigned long data2, unsigned long data3,
        unsigned long data4) {
    XEvent event;
    long mask = SubstructureRedirectMask | SubstructureNotifyMask;

    event.xclient.type = ClientMessage;
    event.xclient.serial = 0;
    event.xclient.send_event = True;
    event.xclient.message_type = XInternAtom(disp, msg, False);
    event.xclient.window = win;
    event.xclient.format = 32;
    event.xclient.data.l[0] = data0;
    event.xclient.data.l[1] = data1;
    event.xclient.data.l[2] = data2;
    event.xclient.data.l[3] = data3;
    event.xclient.data.l[4] = data4;
    
    if (XSendEvent(disp, DefaultRootWindow(disp), False, mask, &event)) {
        return EXIT_SUCCESS;
    } else {
        fprintf(stderr, "Cannot send %s event.\n", msg);fflush(stderr);
        return EXIT_FAILURE;
    }
}
// toggles the "above" state of the X Window
static int toggle_window_state_above(Display *disp, Window win) {
    unsigned long action = _NET_WM_STATE_TOGGLE;
    Atom prop1 = XInternAtom(disp, "_NET_WM_STATE_ABOVE", False);
	int result = client_msg(disp, win, "_NET_WM_STATE", action, (unsigned long)prop1, 0, 0, 0);
	return result;
}
// determines is the X Window has "above" state
static Bool isWindowAbove (Display *disp, Window win) {
    Atom	  actual;
    int		  format;
    unsigned long n, left;
    unsigned char *data;

    int result = XGetWindowProperty(disp, win, XInternAtom(disp, "_NET_WM_STATE", False),
				 0L, 1024L, FALSE, XA_ATOM, &actual, &format, &n, &left, &data);

    if (result == Success && n && data) {
		Atom *a = (Atom *) data;
		while (n--) {
			if (XInternAtom (disp, "_NET_WM_STATE_ABOVE", 0) == *a++) {
				XFree ((void *) data);
				return True;
			}
		}
		XFree ((void *) data);
    }

    return False;
}

static Bool isOverrideRedirect(Display* disp, Window win) {
	XWindowAttributes attrs;
	XGetWindowAttributes(disp, win, &attrs);
	return attrs.override_redirect;
}
static void gtk_widget_show_map_callback (GtkWidget *widget, GdkEvent *event, gint *flag) {
	*flag = TRUE;
	g_signal_handlers_disconnect_by_func (widget,gtk_widget_show_map_callback, flag);
}

// moves the preview window onto another workspace (desktop).
static int moveWindowToDesktop(GtkWidget *shellWidget) {
	if (GTK_WIDGET_VISIBLE(shellWidget)) {
		// can't do anything, because it's already visible
		return JNI_FALSE;
	}
	// get underlying X resources
	GdkWindow *window = shellWidget->window;
	Window x11window = GDK_WINDOW_XID(window);
	Display* disp = GDK_DRAWABLE_XDISPLAY(window);
	// remove "override_redirect" flag
	// https://fogbugz.instantiations.com:443/default.php?41586
	Bool wasOverride = isOverrideRedirect(disp, x11window);
	if (wasOverride) {
		gdk_window_set_override_redirect(window, FALSE);
	}
	// disable showing preview window by any helpers
	gtk_window_set_focus_on_map((GtkWindow*)shellWidget, FALSE);
	gtk_window_set_skip_taskbar_hint((GtkWindow*)shellWidget, TRUE);
	gtk_window_set_skip_pager_hint((GtkWindow*)shellWidget, TRUE);
	// get current desktop/desktop count
    unsigned long *desktopCountPtr = NULL;
    unsigned long *currentDesktopPtr = NULL;
    Window root = DefaultRootWindow(disp);
	if (!(desktopCountPtr = (unsigned long *)get_property(disp, root,
		XA_CARDINAL, "_NET_NUMBER_OF_DESKTOPS", NULL))) {
		if (!(desktopCountPtr = (unsigned long *)get_property(disp, root,
			XA_CARDINAL, "_WIN_WORKSPACE_COUNT", NULL))) {
			return JNI_FALSE;
		}
	}
    if (!(currentDesktopPtr = (unsigned long *)get_property(disp, root,
            XA_CARDINAL, "_NET_CURRENT_DESKTOP", NULL))) {
        if (!(currentDesktopPtr = (unsigned long *)get_property(disp, root,
                XA_CARDINAL, "_WIN_WORKSPACE", NULL))) {
			return JNI_FALSE;        
		}
    }
	m_currentDesktop = *currentDesktopPtr;
	m_desktopCount = *desktopCountPtr;
	g_free(currentDesktopPtr);
	g_free(desktopCountPtr);
	// determine the desktop number on which the preview window would be moved
	int desktop;
	int desktopCountActual = m_desktopCount;
	if (m_desktopCount == 1 ) { 
		// create new desktop if the only one exists, would be removed later
	    if (!client_msg(disp, DefaultRootWindow(disp), "_NET_NUMBER_OF_DESKTOPS", 2, 0, 0, 0, 0)) {
			// success
			desktopCountActual++;
			XSync(disp, False);
		}
	}
	if (m_currentDesktop == desktopCountActual - 1) {
		desktop = desktopCountActual - 2;
	} else {
		desktop = m_currentDesktop + 1;
	}
	// show widget required, only mapped windows can be moved to another desktop.
	// this could cause flickering in rare cases (a few events in queue?)
	gint flag = FALSE;
	gtk_widget_show(shellWidget);
	// move window to another desktop
	client_msg(disp, x11window, "_NET_WM_DESKTOP", (unsigned long)desktop, 0, 0, 0, 0);
	// wait for window to be completely shown: wait for ConfigureNotify which has no above window.
    g_signal_connect (shellWidget, "configure-event", G_CALLBACK (gtk_widget_show_map_callback),&flag);
    while (!flag) {
		gtk_main_iteration();
	}
	if (wasOverride) {
		gdk_window_set_override_redirect(window, TRUE);
	}
	// success
	return JNI_TRUE;
}

static int restoreWindow(GtkWidget *shellWidget) {
	if (m_desktopCount == 0) {
		// nothing to do?
		return JNI_FALSE;
	}
	// get underlying X resources
	GdkWindow *window = shellWidget->window;
	Display* disp = GDK_DRAWABLE_XDISPLAY(window);
	// remove the extra desktop maybe created above
	if (m_desktopCount == 1) { 
	    client_msg(disp, DefaultRootWindow(disp), "_NET_NUMBER_OF_DESKTOPS", 1, 0, 0, 0, 0);
	}
	// cleanup
	m_currentDesktop = 0;
	m_desktopCount = 0;
	// success
	return JNI_TRUE;
}

////////////////////////////////////////////////////////////////////////////
//
// Shell screen shot
//
////////////////////////////////////////////////////////////////////////////
static GdkPixmap* copyPixmap(GdkPixmap *source, gint width, gint height) {
	if (source) {	
		GdkPixmap* pixmap = gdk_pixmap_new(source, width, height, -1);
		GdkGC *gc = gdk_gc_new(source);
		gdk_draw_drawable(pixmap, gc, source, 0, 0, 0, 0, width, height);
		g_object_unref(gc);
		g_object_unref(source);
		return pixmap;
	}
	return NULL;
}
/*static GdkPixmap* makeShot2(GtkWidget *widget) {
	GdkPixmap* source = gtk_widget_get_snapshot(widget, NULL);
	if (source == NULL) {
		return NULL;	
	}
	// determine snapshot rectangle
	int x = widget->allocation.x;
	int y = widget->allocation.y;
	int width = widget->allocation.width;
	int height = widget->allocation.height;
	// grow snapshot rectangle to cover all widget windows
	if (widget->parent && !GTK_WIDGET_NO_WINDOW (widget)){
		GdkWindow *parent_window = gtk_widget_get_parent_window (widget);
		GList *windows = NULL, *list;
		for (list = gdk_window_peek_children (parent_window); list; list = list->next) {
			GdkWindow *subwin = list->data;
			gpointer windata;
			int wx, wy, ww, wh;
			gdk_window_get_user_data (subwin, &windata);
			if (windata != widget) {
				continue;
			}
			windows = g_list_prepend (windows, subwin);
			gdk_window_get_position (subwin, &wx, &wy);
			gdk_drawable_get_size (subwin, &ww, &wh);
			// grow snapshot rectangle by extra widget sub window
			if (wx < x) {
				width += x - wx;
				x = wx;
			}
			if (wy < y) {
				height += y - wy;
				y = wy;
			}
			if (x + width < wx + ww) {
				width += wx + ww - (x + width);
			}
			if (y + height < wy + wh) {
				height += wy + wh - (y + height);
			}
		}
	} else if (!widget->parent) {
    	x = y = 0; // toplevel
	}
	// return it copied to avoid incompatibility with SWT.
	return copyPixmap(source, width, height);
}*/

//
typedef struct _GdkWindowPaint GdkWindowPaint;
struct _GdkWindowPaint {
	GdkRegion *region;
	GdkPixmap *pixmap;
	gint x_offset;
	gint y_offset;
};
// actually we need only a bin_window member
typedef struct _GtkTreeViewPrivateStub GtkTreeViewPrivateStub;
struct _GtkTreeViewPrivateStub {
	void *model;
	guint flags;
	void *tree;
	void *button_pressed_node;
	void *button_pressed_tree;
	GList *children;
	gint width;
	gint height;
	gint expander_size;
	void *hadjustment;
	void *vadjustment;
	GdkWindow *bin_window;
	GdkWindow *header_window;
	GdkWindow *drag_window;
	GdkWindow *drag_highlight_window;
};
static void exposeAllWidgetsCallback(GtkWidget *widget, gpointer data);
//
#define PREPARE_EVENT \
		GdkEventExpose ev;\
		ev.type = GDK_EXPOSE;\
		ev.send_event = TRUE;\
		ev.area.x = 0;\
		ev.area.y = 0;\
		ev.count = 0;

#define UPDATE_EVENT \
		gdk_window_get_geometry(ev.window, NULL, NULL, &ev.area.width, &ev.area.height, NULL);\
		ev.region = gdk_region_rectangle(&ev.area);

static void exposeWidget(GtkWidget *widget) {
	GdkWindow *window = widget->window;
	if (!GTK_WIDGET_REALIZED(widget)) {
		return;
	}
	if (GTK_IS_ENTRY(widget)) {
		// single text
		GtkWidgetClass *clazz = (GtkWidgetClass *)GTK_ENTRY_GET_CLASS(widget);
		{
			PREPARE_EVENT
			ev.window = window;
			UPDATE_EVENT
			clazz->expose_event(widget, &ev);
		}
		//
		{
			PREPARE_EVENT
			ev.window = ((GtkEntry*)widget)->text_area;
			UPDATE_EVENT
			clazz->expose_event(widget, &ev);
		}			
	} else if (GTK_IS_TEXT_VIEW(widget)) {
		// multi-line text
		{
			PREPARE_EVENT
			ev.window = gtk_text_view_get_window((GtkTextView *)widget, GTK_TEXT_WINDOW_TEXT);
			UPDATE_EVENT
			gtk_widget_send_expose(widget, (GdkEvent*)&ev);
		}			
	} else if (GTK_IS_TREE_VIEW(widget)) {
		// tree
		GtkWidgetClass *clazz = (GtkWidgetClass *)GTK_TREE_VIEW_GET_CLASS(widget);
		GtkTreeView *tree_view = GTK_TREE_VIEW (widget);
		{
			PREPARE_EVENT
			GtkTreeViewPrivateStub *priv = (GtkTreeViewPrivateStub *)tree_view->priv;
			ev.window = priv->bin_window;
			UPDATE_EVENT
			clazz->expose_event(widget, &ev);
		}			
	} else {
		// everything else
		{
			PREPARE_EVENT
			ev.window = window;
			UPDATE_EVENT
			gtk_widget_send_expose(widget, (GdkEvent*)&ev);
		}			
	}
}

static void exposeAllWidgets(GtkWidget *widget) {
	if (!GTK_IS_WIDGET(widget)) {
		return;
	}
	exposeWidget(widget);
	if (!GTK_IS_CONTAINER(widget)) {
		return;
	}
	GtkContainer *container = GTK_CONTAINER(widget);
	gtk_container_forall(container, exposeAllWidgetsCallback, 0);
}

static GdkPixmap* getPixmap(GdkWindow *window) {
	gint width, height;
	gdk_window_get_geometry(window, NULL, NULL, &width, &height, NULL);
	//
	GdkRectangle rect;
	rect.x = 0;	rect.y = 0;	rect.width = width;	rect.height = height;
	//
	GdkRegion *region = gdk_region_rectangle(&rect);
	gdk_window_begin_paint_region(window, region);
	//
	region = gdk_region_rectangle(&rect);
	gdk_window_invalidate_region(window, region, TRUE);
	//
	gpointer widget = NULL;
	gdk_window_get_user_data(window, &widget);
	if (widget != NULL) {
		exposeAllWidgets((GtkWidget*)widget);
	}
	//
	gdk_window_process_updates(window, TRUE);
	//
	GdkWindowObject *private = (GdkWindowObject *)(window);
	GdkPixmap *internalPixmap = ((GdkWindowPaint *)private->paint_stack->data)->pixmap;
	if (internalPixmap == NULL) {
		return NULL;
	}
	//
	g_object_ref(internalPixmap);
	GdkPixmap *pixmap = copyPixmap(internalPixmap, width, height);
	gdk_window_end_paint(window);
	return pixmap;
}

static GdkPixmap* traverse(GdkWindow *window){
	gint depth;
	gdk_window_get_geometry(window, NULL, NULL, NULL, NULL, &depth);
	// strange window
	if (depth == 0) {
		return NULL;
	}
	//
	GdkPixmap *pixmap = getPixmap(window);
	if (pixmap == NULL) {
		return NULL;
	}
	//
	GdkGC *gc = gdk_gc_new(pixmap);
	GList *children = gdk_window_get_children(window);
	guint length = g_list_length(children);
	//
	guint i;
    for (i = 0; i < length; i++) {
		GdkWindow *win = g_list_nth_data(children, i);
		GdkPixmap* pix = traverse(win);
		if (pix == NULL) {
			continue;
		}
		gint x, y, width, height;
		gdk_window_get_geometry(win, &x, &y, &width, &height, NULL);
		gdk_draw_drawable(pixmap, gc, pix, 0, 0, x, y, width, height);
		g_object_unref(pix);
    }
 	g_object_unref(gc);
	return pixmap;
}

static void exposeAllWidgetsCallback(GtkWidget *widget, gpointer data) {
	exposeAllWidgets(widget);
}
static GdkPixmap* makeShot(GtkWidget* shellWidget) {
	GdkWindow *window = shellWidget->window;
	return traverse(window);
}
////////////////////////////////////////////////////////////////////////////
//
// JNI
//
////////////////////////////////////////////////////////////////////////////
JNIEXPORT jboolean JNICALL 
	OS_NATIVE_LL(_1toggle_1above)
		(JNIEnv *envir, jobject that, JHANDLE widgetHandle, jboolean forceToggle) {

	GtkWidget* shellWidget = (GtkWidget*)widgetHandle;
	GdkWindow *window = shellWidget->window;

	Window x11window = GDK_WINDOW_XWINDOW(window);
	Display* display = GDK_DRAWABLE_XDISPLAY(window);
	if (!isWindowAbove(display, x11window) || forceToggle == JNI_TRUE) {
		toggle_window_state_above(display, x11window);
		return JNI_TRUE;
	}
	return JNI_FALSE;
}
JNIEXPORT jboolean JNICALL 
	OS_NATIVE_LL(_1begin_1shot)
		(JNIEnv *envir, jobject that, JHANDLE widgetHandle) {

	GtkWidget* shellWidget = (GtkWidget*)widgetHandle;
	return moveWindowToDesktop(shellWidget);
}
JNIEXPORT jboolean JNICALL 
	OS_NATIVE_LL(_1end_1shot)
		(JNIEnv *envir, jobject that, JHANDLE widgetHandle) {

	GtkWidget* shellWidget = (GtkWidget*)widgetHandle;
	return restoreWindow(shellWidget);
}
// shot
JNIEXPORT JHANDLE JNICALL OS_NATIVE_LL(_1makeShot)(
			JNIEnv *envir, jobject that, JHANDLE widgetHandle) {
	// make shot
	GdkPixmap* pixmap = makeShot((GtkWidget*)widgetHandle);
	return (JHANDLE)pixmap;
}
// unref
JNIEXPORT void JNICALL OS_NATIVE_LL(_1g_1object_1unref)(
			JNIEnv *envir, jobject that, JHANDLE jhandle) {
	g_object_unref((GObject*)jhandle);
}
