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
package com.google.gdt.eclipse.designer.model.widgets.support;

import com.google.common.collect.Lists;
import com.google.gdt.eclipse.designer.Activator;
import com.google.gdt.eclipse.designer.IExceptionConstants;
import com.google.gdt.eclipse.designer.common.Constants;
import com.google.gdt.eclipse.designer.hosted.IBrowserShell;
import com.google.gdt.eclipse.designer.hosted.IHostedModeSupport;
import com.google.gdt.eclipse.designer.hosted.IHostedModeSupportFactory;
import com.google.gdt.eclipse.designer.support.http.HttpServer;
import com.google.gdt.eclipse.designer.support.http.IModuleInitializer;
import com.google.gdt.eclipse.designer.support.http.IResourceProvider;
import com.google.gdt.eclipse.designer.util.ModuleDescription;
import com.google.gdt.eclipse.designer.util.ModuleVisitor;
import com.google.gdt.eclipse.designer.util.Utils;
import com.google.gdt.eclipse.designer.util.resources.IResourcesProvider;

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.Version;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Provides access to the GWT module, hosted mode {@link ClassLoader} and many other low level
 * things.
 * 
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage gwt.model
 */
public final class GwtState {
  private static final String CLASSPATH_URL_MARKER = "/__classpath__/";
  private static final String START_HTML = "__start.html";
  // errors for last state
  private static String m_loggerErrorMessages;
  // for tests
  public static final List<GwtState> INSTANCES = Lists.newArrayList();
  public final String m_testQualifiedName;
  // from constructor
  private final ClassLoader m_parentClassLoader;
  private final Version m_version;
  private final IJavaProject m_javaProject;
  private final ModuleDescription m_moduleDescription;
  private String m_moduleId;
  private String m_moduleName;
  private final CssSupport m_cssSupport = new CssSupport(this);
  // HTML
  private static int m_nextStateIndex = 0;
  protected final String m_moduleBaseURL = "/" + m_nextStateIndex++ + "/";
  private final String m_startHtmlUrl = m_moduleBaseURL + START_HTML;
  private boolean m_strictMode;
  private String m_html;
  // hosted mode
  private boolean m_initialized;
  private IBrowserShell m_shell;
  private IHostedModeSupport m_hostModeSupport;
  private final DOMUtils m_domUtils = new DOMUtils(this);
  private final UIObjectUtils m_uiObjectUtils = new UIObjectUtils(this);
  private Object m_body;
  ////////////////////////////////////////////////////////////////////////////
  //
  // IDevModeBridge 
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IDevModeBridge m_devModeBridge = new IDevModeBridge() {
    public ClassLoader getDevClassLoader() {
      return m_hostModeSupport.getDevClassLoader();
    }

    public Object findJType(String name) {
      return m_hostModeSupport.findJType(name);
    }

    public void invalidateRebind(String binderClassName) {
      m_hostModeSupport.invalidateRebind(binderClassName);
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GwtState(ClassLoader parentClassLoader, ModuleDescription moduleDescription)
      throws Exception {
    // remember module
    m_moduleDescription = moduleDescription;
    if (m_moduleDescription == null) {
      throw new DesignerException(IExceptionConstants.NO_MODULE_FILE);
    }
    // remember arguments
    m_parentClassLoader = parentClassLoader;
    m_javaProject = JavaCore.create(m_moduleDescription.getProject());
    // remember for access from other classes
    INSTANCES.add(this);
    m_testQualifiedName = findTestQualifiedName();
    // safe operation
    m_version = Utils.getVersion(m_javaProject);
  }

  /**
   * Prepares module, HTML and CSS information, then initializes {@link IHostedModeSupport}.
   */
  public void initialize() throws Exception {
    // prepare module
    {
      m_moduleId = m_moduleDescription.getId();
      m_moduleName = Utils.readModule(m_moduleDescription).getName();
    }
    // prepare HTML text
    {
      InputStream is = GwtState.class.getResourceAsStream(START_HTML);
      if (is == null) {
        throw new RuntimeException("Unable to load initialization page.");
      }
      try {
        m_html = IOUtils2.readString(is);
      } finally {
        IOUtils.closeQuietly(is);
      }
      // add "docType"
      {
        String docType = Utils.getDocType(m_moduleDescription);
        // null means no HTML, defaulting to strict mode
        if (docType == null) {
          m_strictMode = true;
          m_html = "<!doctype html>\n" + m_html;
        } else {
          m_strictMode =
              docType.equalsIgnoreCase("<!doctype html>")
                  || docType.equalsIgnoreCase("<!doctype html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">");
          if (!StringUtils.isEmpty(docType)) {
            m_html = docType + "\n" + m_html;
          }
        }
      }
      // set any declarations
      {
        String declarations = getDeclarations();
        m_html = StringUtils.replace(m_html, "%CSS_DECLARATIONS%", declarations);
      }
      // set inline variables
      {
        m_html = StringUtils.replace(m_html, "%MODULE_ID%", m_moduleId);
        m_html = StringUtils.replace(m_html, "%MODULE_NAME%", m_moduleName);
        m_html = StringUtils.replace(m_html, "%MODULE_BASE%", m_moduleBaseURL);
        m_html = StringUtils.replace(m_html, "%GWT_VERSION%", m_version.getStringMajorMinor());
      }
      // set default "locale"
      {
        String defaultLocale = Utils.getDefaultLocale(m_moduleDescription);
        m_html = StringUtils.replace(m_html, "%GWT_LOCALE%", defaultLocale);
      }
      // updates HTML file to support CSS files reloading
      m_html = m_cssSupport.addReloadingFeature(m_html);
    }
    // install resource provider
    HttpServer.getInstance().addResourceProvider(m_moduleBaseURL, new ResourceProvider());
    // perform initialize (long running operation)
    {
      IProgressMonitor monitor = IDesignPageSite.Helper.getProgressMonitor();
      initialize0(monitor);
    }
    // set active
    activate();
  }

  /**
   * Extracts from stack trace name of class and method of test which creates this {@link GwtState}.
   */
  private static String findTestQualifiedName() {
    for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
      String methodName = element.getMethodName();
      if (methodName.startsWith("test_")) {
        return element.getClassName() + "." + methodName;
      }
    }
    return "noTestMethod";
  }

  /**
   * Initializes {@link IHostedModeSupport} and this {@link GwtState} class.
   */
  private void initialize0(IProgressMonitor monitor) throws Exception {
    monitor.subTask("Initializing GWT Development Mode...");
    m_hostModeSupport = getHostedModeSupport();
    {
      // get shell
      m_shell = m_hostModeSupport.getBrowserShell();
      // set shell size
      m_shell.setSize(450, 300);
    }
    {
      // prepare user-agent
      m_html = StringUtils.replace(m_html, "%USER_AGENT%", m_shell.getUserAgentString());
      m_html = StringUtils.replace(m_html, "%GWT_isBrowserExplorer%", "" + isBrowserExplorer());
    }
    // prepare hosted mode
    m_hostModeSupport.startup(getBrowserStartupUrl(), m_moduleId, monitor, getHostedModeTimeout());
    m_initialized = true;
    // configure Window
    m_body = m_uiObjectUtils.getRootPanelElement();
    ReflectionUtils.invokeMethod(
        m_uiObjectUtils.getClassOfWindow(),
        "enableScrolling(boolean)",
        false);
  }

  /**
   * @return the timeout to wait for hosted mode to initialize.
   */
  private static int getHostedModeTimeout() {
    IPreferenceStore store = Activator.getStore();
    int timeout = store.getInt(Constants.P_GWT_HOSTED_INIT_TIME);
    if (timeout == 0) {
      return Integer.MAX_VALUE;
    }
    return timeout * 1000;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Browser support methods
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates a screenshot image of Browser control. This method waits for any pending images to load
   * and CSS loading Browser operations to complete, then calls native part to create the image.
   */
  public Image createBrowserScreenshot() throws Exception {
    // wait for possibly updated CSS files to be applied
    m_cssSupport.waitFor();
    // waiting for browser to download all images
    waitForImages();
    // do screen shot
    return m_shell.createBrowserScreenshot();
  }

  /**
   * Forces an outstanding messages to be processed
   */
  public void runMessagesLoop() {
    Runnable enableEventsRunnable = disableMouseAndKeyboard();
    try {
      while (Display.getCurrent().readAndDispatch()) {
        // wait
      }
    } catch (Throwable e) {
    } finally {
      enableEventsRunnable.run();
    }
  }

  /**
   * We should process messages while waiting Browser to load all images, style, etc. However this
   * means that user may perform some action, such as trying to change properties, move components,
   * etc. During execution of command. So, we should catch dangerous events and ignore them.
   * <p>
   * http://fogbugz.instantiations.com/fogbugz/default.php?43606
   */
  private Runnable disableMouseAndKeyboard() {
    final int[] events = {SWT.MouseDown, SWT.MouseUp, SWT.MouseDoubleClick, SWT.KeyDown, SWT.KeyUp};
    final Display display = DesignerPlugin.getStandardDisplay();
    final Listener listener = new Listener() {
      public void handleEvent(Event event) {
        event.doit = false;
        event.type = SWT.NONE;
      }
    };
    for (int i = 0; i < events.length; i++) {
      display.addFilter(events[i], listener);
    }
    return new Runnable() {
      public void run() {
        for (int i = 0; i < events.length; i++) {
          display.removeFilter(events[i], listener);
        }
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the internal {@link IHostedModeSupport}.
   */
  IHostedModeSupport getHostModeSupport() {
    return m_hostModeSupport;
  }

  /**
   * @return the [ERROR] entries passed into GWT log, as string. Returns "&lt;none&gt;" if no such
   *         messages. Only entries of last disposed {@link GwtState} are returned, because this is
   *         what we need during exception rewriting.
   */
  public static String getLoggerErrorMessages() {
    return m_loggerErrorMessages;
  }

  /**
   * @return the {@link UIObjectUtils} to work with <code>UIObject</code>.
   */
  public UIObjectUtils getUIObjectUtils() {
    return m_uiObjectUtils;
  }

  /**
   * @return the {@link DOMUtils} to work with <code>Element</code>.
   */
  public DOMUtils getDomUtils() {
    return m_domUtils;
  }

  public IBrowserShell getShell() {
    return m_shell;
  }

  public IDevModeBridge getDevModeBridge() {
    return m_devModeBridge;
  }

  /**
   * @return the string to set as browser start URL.
   */
  private String getBrowserStartupUrl() {
    return "http://" + HttpServer.getInstance().getTCPAddress() + m_startHtmlUrl;
  }

  /**
   * @return the {@link ModuleDescription}.
   */
  public ModuleDescription getModuleDescription() {
    return m_moduleDescription;
  }

  public String getModuleName() {
    return m_moduleId;
  }

  public Version getVersion() {
    return m_version;
  }

  /**
   * @return <code>true</code> if "strict" mode is using, so for example CSS tricks work as
   *         expected.
   */
  public boolean isStrictMode() {
    return m_strictMode;
  }

  /**
   * @return <code>true</code> if browser is the "Internet Explorer".
   */
  public boolean isBrowserExplorer() {
    String agent = m_shell.getUserAgentString();
    return "ie6".equals(agent) || "ie7".equals(agent) || "ie8".equals(agent);
  }

  /**
   * @return <code>true</code> if browser is based on WebKit.
   */
  public boolean isBrowserWebKit() {
    String agent = m_shell.getUserAgentString();
    return "safari".equals(agent);
  }

  /**
   * @return the {@link CssSupport} for CSS operations.
   */
  public CssSupport getCssSupport() {
    return m_cssSupport;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SWT Shell wrapping methods
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create platform-dependent shell with browser.
   * 
   * @return IBrowserShell instance
   */
  public void showShell() {
    m_shell.showAsPreview();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Images
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When we use images, browser requests images in background, so we should wait until all (local)
   * images are loaded before making screen shot. Note: waits no more than 500ms.
   */
  private void waitForImages() throws Exception {
    // System.out.println("*** start IMAGE wait");
    long startWait = System.currentTimeMillis();
    while (true) {
      boolean complete =
          m_hostModeSupport.invokeNativeBoolean(
              "__waitForImages",
              ArrayUtils.EMPTY_CLASS_ARRAY,
              ArrayUtils.EMPTY_OBJECT_ARRAY);
      if (complete) {
        break;
      }
      // do not wait more than 500ms
      if (System.currentTimeMillis() - startWait > 500) {
        break;
      }
      // wait more
      runMessagesLoop();
    }
    // System.out.println("*** waiting for IMAGE done in " + (System.currentTimeMillis() - startWait) + "ms");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CSS
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Prepares declarations (CSS, Script & etc) that should be placed in HTML template. Also
   * remembers used CSS files to check them later for update.
   */
  private String getDeclarations() throws Exception {
    List<String> declarations = Lists.newArrayList();
    // prepare CSS declarations
    {
      m_cssSupport.prepareResources();
      m_cssSupport.addLinkDeclarations(declarations);
    }
    // other declarations
    {
      List<IModuleInitializer> initializers =
          ExternalFactoriesHelper.getElementsInstances(
              IModuleInitializer.class,
              "com.google.gdt.eclipse.designer.moduleInitializers",
              "initializer");
      for (IModuleInitializer moduleInitializer : initializers) {
        moduleInitializer.configure(m_moduleDescription, declarations);
      }
    }
    // final result
    return StringUtils.join(declarations, "\n");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Sharing
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_shared;

  /**
   * @return <code>true</code> if this {@link GwtState} is shared, to speed up testing.
   */
  public boolean isShared() {
    return m_shared;
  }

  /**
   * Marks if this {@link GwtState} is shared or not.
   */
  public void setShared(boolean shared) {
    m_shared = shared;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this GWT session was already disposed.
   */
  public boolean isDisposed() {
    return m_shell == null || m_shell.isDisposed();
  }

  /**
   * Disposes any resources associated with this GWT session.
   */
  public void dispose() {
    // don't dispose if shared
    if (m_shared) {
      return;
    }
    // remember errors
    if (m_hostModeSupport != null) {
      m_loggerErrorMessages = m_hostModeSupport.getLogSupport().getErrorMessages();
    } else {
      m_loggerErrorMessages = "No hosted mode.";
    }
    // try to dispose
    if (!isDisposed()) {
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          disposeWindowClass();
          HttpServer.getInstance().removeResourceProvider(m_moduleBaseURL);
          m_hostModeSupport.dispose();
        }
      });
    }
    // disposed in any case
    INSTANCES.remove(this);
  }

  /**
   * Disposes top level GWT <code>Window</code> class.
   */
  private void disposeWindowClass() throws Exception {
    if (!m_initialized) {
      return;
    }
    // outstanding dispatch events spawned just after the browser shell is disposed.
    // the workaround is to call onClosed event handler before disposing and...
    Class<?> classOfWindow = m_uiObjectUtils.getClassOfWindow();
    ReflectionUtils.invokeMethod(classOfWindow, "onClosed()");
    // prevent fire again
    // < 1.6 way
    {
      // used Vector class prior to 1.4 and ArrayList class for 1.4,
      // so use reflection to invoke 'clear()'
      Field closingListenersField =
          ReflectionUtils.getFieldByName(classOfWindow, "closingListeners");
      if (closingListenersField != null) {
        Object closingListeners = ReflectionUtils.getFieldObject(classOfWindow, "closingListeners");
        // ...clear listeners
        ReflectionUtils.invokeMethod(closingListeners, "clear()");
      }
    }
    // 1.6 way: disable listeners fire
    {
      Field handlersInitedField =
          ReflectionUtils.getFieldByName(classOfWindow, "closeHandlersInitialized");
      if (handlersInitedField != null) {
        ReflectionUtils.setField(classOfWindow, "closeHandlersInitialized", false);
      }
    }
    // remove window listeners to prevent events firing when the editor closed
    m_hostModeSupport.invokeNativeVoid(
        "__wbp_cleanupEvents",
        ArrayUtils.EMPTY_CLASS_ARRAY,
        ArrayUtils.EMPTY_OBJECT_ARRAY);
  }

  /**
   * Invoked when the context changed (ex., editor switched).
   */
  public void activate() throws Exception {
    m_hostModeSupport.activate();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Modify check
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Checks if some external files related with this GWT state where modified. For example we check
   * CSS files.
   */
  public boolean isModified() {
    return m_cssSupport.isModified();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Class loader
  //
  ////////////////////////////////////////////////////////////////////////////
  private IResourcesProvider m_resourcesProvider;

  public ClassLoader getClassLoader() {
    return m_hostModeSupport.getClassLoader();
  }

  public IResourcesProvider getResourcesProvider() throws Exception {
    if (m_resourcesProvider == null) {
      m_resourcesProvider = m_moduleDescription.getResourcesProvider();
    }
    return m_resourcesProvider;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Coordinates
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the bounds of <code>Element</code> such that using them for
   *         <code>AbsolutePanel.setWidgetPosition()</code> will place this element/widget into same
   *         location.
   */
  public Rectangle getModelBounds(Object element) {
    if (element == null) {
      return new Rectangle();
    }
    Dimension size = getUIObjectSize(element);
    // for BODY we assume (0,0) as top-left
    if (isBody(element)) {
      return new Rectangle(new Point(0, 0), size);
    }
    // for any inner element
    {
      Insets margins = getMargins(element);
      int x = m_domUtils.getIntAttribute(element, "offsetLeft") - margins.left;
      int y = m_domUtils.getIntAttribute(element, "offsetTop") - margins.top;
      return new Rectangle(x, y, size.width + margins.getWidth(), size.height + margins.getHeight());
    }
  }

  /**
   * Returns absolute bounds for given element. Meaning of "absolute" is following: relative to
   * point (0,0) of screen shot that we show on design canvas. These bounds include also margins of
   * element.
   */
  public Rectangle getAbsoluteBounds(Object element) {
    if (element == null) {
      return new Rectangle();
    }
    Dimension size = getUIObjectSize(element);
    // for BODY we assume (0,0) as top-left
    Insets margins = getMargins(element);
    if (isBody(element)) {
      int x = 0;
      int y = 0;
      if (isStrictMode()) {
        size.width += margins.getWidth() + getBorders(element).getWidth();
        size.height += margins.getHeight() + getBorders(element).getHeight();
      }
      return new Rectangle(new Point(x, y), size);
    }
    // for any inner element
    {
      int x = m_domUtils.getAbsoluteLeft(element);
      int y = m_domUtils.getAbsoluteTop(element);
      // apply margins
      {
        x -= margins.left;
        y -= margins.top;
        size.width += margins.getWidth();
        size.height += margins.getHeight();
      }
      // bounds Rectangle
      return new Rectangle(x, y, size.width, size.height);
    }
  }

  /**
   * @return <code>true</code> if the given element represents BODY element.
   */
  public boolean isBody(Object element) {
    return element == m_body;
  }

  /**
   * @return the size of given element.
   */
  private Dimension getUIObjectSize(Object element) {
    int width;
    int height;
    if (isBody(element)) {
      if (isBrowserExplorer()) {
        // Kosta_20080728: at least in GWT 1.5 and IE6 we have to use "offsetXXX" even for BODY,
        //   because in other case we don't see full borders, see "Border for GWT browser" in GMail
        width = m_domUtils.getIntAttribute(element, "offsetWidth");
        height = m_domUtils.getIntAttribute(element, "offsetHeight");
      } else {
        // the size of RootPanel should be fetched from 
        // "clientWidth/clientHeight" attributes because by standards
        // using "offsetXXX" attributes we get sizes only for BODY element 
        // (Mozilla, IE in DOCTYPE mode)
        // see http://www.quirksmode.org/js/doctypes.html for details
        width = m_domUtils.getIntAttribute(element, "clientWidth");
        height = m_domUtils.getIntAttribute(element, "clientHeight");
      }
    } else {
      width = m_domUtils.getIntAttribute(element, "offsetWidth");
      height = m_domUtils.getIntAttribute(element, "offsetHeight");
    }
    return new Dimension(width, height);
  }

  public Insets getBorders(Object element) {
    int top = getBorderSideWidth(element, "top");
    int left = getBorderSideWidth(element, "left");
    int bottom = getBorderSideWidth(element, "bottom");
    int right = getBorderSideWidth(element, "right");
    return new Insets(top, left, bottom, right);
  }

  private int getBorderSideWidth(Object element, String name) {
    String style = getComputedStyle(element, "border-" + name + "-style");
    if (style == null || "none".equals(style)) {
      return 0;
    }
    return getComputedStylePx(element, "border-" + name + "-width");
  }

  public Insets getMargins(Object element) {
    String oldDisplayStyleValue = null;
    try {
      if (isBrowserWebKit()) {
        // Bug/Feature in WebKit: if the position of the element is
        // not absolute and there is a free space right to the element 
        // then this space added to element's space as 'margin-right' computed style value.
        // The workaround is to set 'display' style to 'none' while measuring margins.
        // Refs: 
        // http://fogbugz.instantiations.com/fogbugz/default.asp?45380
        // https://bugs.webkit.org/show_bug.cgi?id=13343
        Object parent = m_domUtils.getParent(element);
        String parentTag = m_domUtils.getTagName(parent);
        if (!"TD".equals(parentTag)) {
          // This workaround causes problem with "top" position of the element in "TD" - it
          // gets shifted by some value, something like half of TD height. Probably because of
          // using the default "center" vertical alignment. So, we should not touch children of TD.
          oldDisplayStyleValue = m_domUtils.getStyleAttribute(element, "display");
          m_domUtils.setStyleAttribute(element, "display", "none");
        }
      }
      int top = getComputedStylePx(element, "margin-top");
      int left = getComputedStylePx(element, "margin-left");
      int bottom = getComputedStylePx(element, "margin-bottom");
      int right = getComputedStylePx(element, "margin-right");
      return new Insets(top, left, bottom, right);
    } finally {
      if (oldDisplayStyleValue != null) {
        // restore display style
        m_domUtils.setStyleAttribute(element, "display", oldDisplayStyleValue);
      }
    }
  }

  public Insets getPaddings(Object element) {
    int top = getComputedStylePx(element, "padding-top");
    int left = getComputedStylePx(element, "padding-left");
    int bottom = getComputedStylePx(element, "padding-bottom");
    int right = getComputedStylePx(element, "padding-right");
    return new Insets(top, left, bottom, right);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Style
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the attribute of computed style.
   */
  public String getComputedStyle(Object element, String style) {
    return m_hostModeSupport.invokeNativeString(
        "__getStyle",
        new Class[]{m_uiObjectUtils.getClassOfElement(), String.class},
        new Object[]{element, style});
  }

  /**
   * @return the value of integer attribute of computed style.
   */
  private int getComputedStylePx(Object element, String style) {
    String styleString = getComputedStyle(element, style);
    return getValuePx(styleString);
  }

  /**
   * @return the number of pixels from given size style string.
   */
  public static int getValuePx(String styleString) {
    if (styleString != null && styleString.endsWith("px") /*"0px"*/) {
      styleString = StringUtils.removeEnd(styleString, "px");
      try {
        return (int) Double.parseDouble(styleString);
      } catch (NumberFormatException e) {
        return 0;
      }
    }
    // no style set or browser can't return it for designer
    return 0;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Visits public folders of given module and inherited modules (recursively).
   */
  public void accept(ModuleVisitor visitor) throws Exception {
    ModuleVisitor.accept(m_moduleDescription, visitor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // RequestHandler
  //
  ////////////////////////////////////////////////////////////////////////////
  private class ResourceProvider implements IResourceProvider {
    /**
     * Returns public resource with given path.
     * 
     * Here "public" means resource placed in folders defined as public in module descriptors.
     */
    public byte[] getResource(String requestPath) {
      String resourcePath = StringUtils.substringBefore(requestPath, "?");
      // System.out.println(System.currentTimeMillis() + ": requestPath: " + requestPath);
      // resources from classpath
      {
        int index = requestPath.indexOf(CLASSPATH_URL_MARKER);
        if (index != -1) {
          String classpathPath = requestPath.substring(index + CLASSPATH_URL_MARKER.length());
          try {
            return IOUtils2.readBytes(getResourcesProvider().getResourceAsStream(classpathPath));
          } catch (Throwable e) {
            return null;
          }
        }
      }
      // HTML
      if (m_startHtmlUrl.equals(resourcePath)) {
        return m_html.getBytes();
      }
      // prepare "public" resource path
      String publicResourcePath = StringUtils.removeStart(resourcePath, m_moduleBaseURL);
      /*synchronized (System.out) {
      	System.out.println("publicResourcePath: " + publicResourcePath);
      	System.out.flush();
      }*/
      // may be CSS "apply wait" found
      {
        byte[] result = m_cssSupport.getResourceWait(publicResourcePath);
        if (result != null) {
          return result;
        }
      }
      // load resource
      try {
        byte[] result = null;
        // load static resource
        {
          InputStream is = Utils.getResource(m_moduleDescription, publicResourcePath);
          if (is != null) {
            result = IOUtils2.readBytes(is);
          }
        }
        // may be generated resource
        if (result == null) {
          result = m_hostModeSupport.getGeneratedResource(publicResourcePath);
        }
        // may be no resource
        if (result == null) {
          return null;
        }
        // if CSS resource, then include "apply wait" class
        result = m_cssSupport.getResource(publicResourcePath, result);
        // done
        //System.out.println("-----------: " + result.length);
        return result;
      } catch (Throwable e) {
        throw ReflectionUtils.propagate(e);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IHostedModeSupportFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String HOSTED_MODE_SUPPORT_FACTORIES_POINT =
      "com.google.gdt.eclipse.designer.hosted.hostedModeFactory";

  private static List<IHostedModeSupportFactory> getHostedModeSupportFactories() {
    return ExternalFactoriesHelper.getElementsInstances(
        IHostedModeSupportFactory.class,
        HOSTED_MODE_SUPPORT_FACTORIES_POINT,
        "factory");
  }

  private IHostedModeSupport getHostedModeSupport() throws Exception {
    String versionString = m_version.getStringMajorMinor();
    // prepare factories
    List<IHostedModeSupportFactory> supportFactories = getHostedModeSupportFactories();
    if (supportFactories.isEmpty()) {
      throw new DesignerException(IExceptionConstants.NO_GWT_SDK_SUPPORT);
    }
    // ask each factory
    for (IHostedModeSupportFactory supportFactory : supportFactories) {
      IHostedModeSupport hostedModeSupport =
          supportFactory.create(versionString, m_parentClassLoader, m_moduleDescription);
      if (hostedModeSupport != null) {
        return hostedModeSupport;
      }
    }
    throw new DesignerException(IExceptionConstants.UNSUPPORTED_GWT_SDK, versionString);
  }
}